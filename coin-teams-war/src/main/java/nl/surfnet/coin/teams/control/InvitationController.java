/*
 * Copyright 2012 SURFnet bv, The Netherlands
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.surfnet.coin.teams.control;

import nl.surfnet.coin.api.client.domain.Person;
import nl.surfnet.coin.janus.Janus;
import nl.surfnet.coin.shared.domain.ErrorMail;
import nl.surfnet.coin.shared.service.ErrorMessageMailer;
import nl.surfnet.coin.teams.domain.*;
import nl.surfnet.coin.teams.interceptor.LoginInterceptor;
import nl.surfnet.coin.teams.service.GrouperTeamService;
import nl.surfnet.coin.teams.service.TeamInviteService;
import nl.surfnet.coin.teams.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.view.RedirectView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static nl.surfnet.coin.teams.util.PersonUtil.getFirstEmail;
import static nl.surfnet.coin.teams.util.PersonUtil.isGuest;

/**
 * {@link Controller} that handles the accept/decline of an Invitation
 */
@Controller
@SessionAttributes({ "invitation", TokenUtil.TOKENCHECK })
public class InvitationController {

  private static final Logger LOG = LoggerFactory.getLogger(InvitationController.class);


  @Autowired
  private TeamInviteService teamInviteService;

  @Autowired
  private TeamEnvironment teamEnvironment;

  @Autowired
  private GrouperTeamService grouperTeamService;

  @Autowired
  private ControllerUtil controllerUtil;

  @Resource
  private Janus janus;

  @Resource(name = "errorMessageMailer")
  private ErrorMessageMailer errorMessageMailer;

  /**
   * RequestMapping to show the accept invitation page.
   *
   * @param modelMap {@link ModelMap}
   * @param request
   *          {@link HttpServletRequest}
   * @return accept invitation page
   * @throws UnsupportedEncodingException
   *           if the server does not support utf-8
   */
  @RequestMapping(value = "/acceptInvitation.shtml")
  public String accept(ModelMap modelMap, HttpServletRequest request) throws UnsupportedEncodingException {
    Invitation invitation = getInvitationByRequest(request);
    if (invitation == null) {
      modelMap.addAttribute("action", "missing");
      return "invitationexception";
    }
    if (invitation.isDeclined()) {
      modelMap.addAttribute("action", "declined");
      return "invitationexception";
    }
    if (invitation.isAccepted()) {
      modelMap.addAttribute("action", "accepted");
      String teamId = invitation.getTeamId();
      String teamUrl = "detailteam.shtml?team=" + URLEncoder.encode(teamId, "utf-8")
              + "&view=" + ViewUtil.getView(request);
      modelMap.addAttribute("teamUrl", teamUrl);
      return "invitationexception";
    }
    String teamId = invitation.getTeamId();
    if (!StringUtils.hasText(teamId)) {
      throw new RuntimeException("Invalid invitation");
    }
    Team team = controllerUtil.getTeamById(teamId);
    
    if (null != team.getAttributes() && null != team.getAttributes().get("Quantity")) {
      String quantity = team.getAttributes().get("Quantity");
      Integer teamSize = Integer.parseInt(quantity);
      if (team.getMembers().size() >= teamSize) {
        modelMap.addAttribute("teamFull", true);
      }
    }

    if (team.getAttributes() != null && team.getAttributes().get("SP Entity ID") != null) {
      String spEntityId = team.getAttributes().get("SP Entity ID");
      String idpEntityId = (String) request.getSession().getAttribute(LoginInterceptor.IDP_ENTITY_ID_SESSION_KEY);
      LOG.debug("SP Entity ID attribute: {}, Idp entity id: {}", spEntityId, idpEntityId);
      List<String> allowedSps = janus.getAllowedSps(idpEntityId);
      LOG.debug("According to SR, allowed SPs: {}", allowedSps);
      if (!allowedSps.contains(spEntityId)) {
        LOG.debug("Not allowed according to SR. Will notify user and send mail.");
        modelMap.addAttribute("licenseInviteButIdpSpNotAllowed", true);
        String msg = String.format("User accepted invitation for license %s while her IdP is not allowed to access the SP",
                team.getAttributes().get("License Number"));
        ErrorMail mail = new ErrorMail(msg, msg, msg, "teams-server", "teams");
        mail.setSp(spEntityId);
        mail.setIdp(idpEntityId);
        mail.setUserId(((Person) request.getSession().getAttribute(LoginInterceptor.PERSON_SESSION_KEY)).getId());
        mail.setLocation(this.getClass().getName());
        errorMessageMailer.sendErrorMail(mail);
      }
    }


    modelMap.addAttribute("invitation", invitation);
    modelMap.addAttribute("team", team);
    modelMap.addAttribute("date", new Date(invitation.getTimestamp()));
    ViewUtil.addViewToModelMap(request, modelMap);
    return "acceptinvitation";
  }

  /**
   * RequestMapping to accept an invitation. If everything is okay, it redirects
   * to your new team detail view.
   *
   * @param request
   *          {@link HttpServletRequest}
   * @return detail view of your new team
   * @throws UnsupportedEncodingException
   *           if the server does not support utf-8
   */
  @RequestMapping(value = "/doAcceptInvitation.shtml")
  public RedirectView doAccept(HttpServletRequest request)
      throws UnsupportedEncodingException {
    Person person = (Person) request.getSession().getAttribute(
        LoginInterceptor.PERSON_SESSION_KEY);

    Invitation invitation = getInvitationByRequest(request);
    if (invitation == null) {
      throw new IllegalArgumentException(
          "Cannot find your invitation. Invitations expire after 14 days.");
    }
    if (invitation.isDeclined()) {
      throw new RuntimeException("Invitation is Declined");
    }
    if (invitation.isAccepted()) {
      throw new IllegalStateException("Invitation is already Accepted");
    }
    String teamId = invitation.getTeamId();
    if (!StringUtils.hasText(teamId)) {
      throw new RuntimeException("Invalid invitation");
    }
    controllerUtil.getTeamById(teamId);

    String memberId = person.getId();
    grouperTeamService.addMember(teamId, person);

    Role intendedRole = invitation.getIntendedRole();
    if (isGuest(person) && Role.Admin.equals(intendedRole)) {
      // cannot make a guest Admin
      invitation.setIntendedRole(Role.Manager);
    }
    intendedRole = invitation.getIntendedRole();
    grouperTeamService.addMemberRole(teamId, memberId, intendedRole, teamEnvironment.getGrouperPowerUser());
    AuditLog.log("User {} accepted invitation for team {} with intended role {}", person.getId(), teamId, intendedRole);
    invitation.setAccepted(true);
    teamInviteService.saveOrUpdate(invitation);

    return new RedirectView("detailteam.shtml?team="
        + URLEncoder.encode(teamId, "utf-8") + "&view="
        + ViewUtil.getView(request));
  }

  /**
   * RequestMapping to decline an invitation as receiver.
   * This URL is bypassed in {@link LoginInterceptor}
   *
   * @param modelMap {@link ModelMap}
   * @param request
   *          {@link HttpServletRequest}
   * @return view for decline result
   */
  @RequestMapping(value = "/declineInvitation.shtml")
  public String decline(ModelMap modelMap,
                        HttpServletRequest request) {
    String viewTemplate = "invitationdeclined";

    Person person = (Person) request.getSession().getAttribute(
      LoginInterceptor.PERSON_SESSION_KEY);

    Invitation invitation = getInvitationByRequest(request);

    if (invitation == null) {
      // even if we can't find the invitation, we'll display success!
      return viewTemplate;
    }

    invitation.setDeclined(true);
    teamInviteService.saveOrUpdate(invitation);
    AuditLog.log("User {} declined invitation for team {} with intended role {}", person.getId(), invitation.getTeamId(), invitation.getIntendedRole());
    ViewUtil.addViewToModelMap(request, modelMap);
    return viewTemplate;
  }

  /**
   * RequestMapping to delete an invitation as admin
   *
   *
   * @param request
   *          {@link javax.servlet.http.HttpServletRequest}
   * @return redirect to detailteam if everything is okay
   * @throws UnsupportedEncodingException
   *           in the rare condition utf-8 is not supported
   */
  @RequestMapping(value = "/deleteInvitation.shtml")
  public RedirectView deleteInvitation(HttpServletRequest request,
                                       @ModelAttribute(TokenUtil.TOKENCHECK) String sessionToken,
                                       @RequestParam() String token,
                                       SessionStatus status,
                                       ModelMap modelMap)
          throws UnsupportedEncodingException {
    TokenUtil.checkTokens(sessionToken, token, status);
    Person person = (Person) request.getSession().getAttribute(
        LoginInterceptor.PERSON_SESSION_KEY);
    if (person == null) {
      status.setComplete();
      return new RedirectView("landingpage.shtml");
    }
    Invitation invitation = getAllInvitationByRequest(request);
    String teamId = invitation.getTeamId();
    teamInviteService.delete(invitation);
    AuditLog.log("User {} deleted invitation for email {} for team {} with intended role {}", person.getId(), invitation.getEmail(), invitation.getTeamId(), invitation.getIntendedRole());

    status.setComplete();
    modelMap.clear();
    return new RedirectView("detailteam.shtml?team="
        + URLEncoder.encode(teamId, "utf-8") + "&view="
        + ViewUtil.getView(request));
  }

  @RequestMapping("/resendInvitation.shtml")
  public String resendInvitation(ModelMap modelMap, HttpServletRequest request) {
    Person person = (Person) request.getSession().getAttribute(
            LoginInterceptor.PERSON_SESSION_KEY);
    Invitation invitation = getAllInvitationByRequest(request);
        if (invitation == null) {
      throw new IllegalArgumentException(
          "Cannot find the invitation. Invitations expire after 14 days.");
    }

    Member member = grouperTeamService.findMember(invitation.getTeamId(), person.getId());
    if (member == null) {
      throw new SecurityException("You are not a member of this team");
    }
    Set<Role> roles = member.getRoles();
    if (!(roles.contains(Role.Admin) || roles.contains(Role.Manager))) {
      throw new SecurityException("You have insufficient rights to perform this action.");
    }

    modelMap.addAttribute("invitation", invitation);
    Role[] inviteRoles = {Role.Member, Role.Manager, Role.Admin};
    modelMap.addAttribute("roles", inviteRoles);
    InvitationMessage invitationMessage = invitation.getLatestInvitationMessage();
    if (invitationMessage != null) {
      modelMap.addAttribute("messageText", invitationMessage.getMessage());
    }
    ViewUtil.addViewToModelMap(request, modelMap);
    return "resendinvitation";
  }
  @RequestMapping("/myinvitations.shtml")
  public String myInvitations(ModelMap modelMap, HttpServletRequest request) {
    Person person = (Person) request.getSession().getAttribute(
            LoginInterceptor.PERSON_SESSION_KEY);
    String email = getFirstEmail(person);
    if (!StringUtils.hasText(email)) {
      throw new IllegalArgumentException("Your profile does not contain an email address");
    }
    List<Invitation> invitations = teamInviteService.findPendingInvitationsByEmail(email);
    modelMap.addAttribute("invitations", invitations);
    List<Team> invitedTeams = new ArrayList<Team>();
    for(Invitation invitation : invitations) {
      Team team = controllerUtil.getTeamById(invitation.getTeamId());
      if(team != null) {
        invitedTeams.add(team);
      }
    }
    modelMap.addAttribute("teams", invitedTeams);
    ViewUtil.addViewToModelMap(request, modelMap);
    return "myinvitations";
  }

  private Invitation getInvitationByRequest(HttpServletRequest request) {
    String invitationId = request.getParameter("id");

    if (!StringUtils.hasText(invitationId)) {
      throw new IllegalArgumentException(
          "Missing parameter to identify the invitation");
    }

    return teamInviteService.findInvitationByInviteId(invitationId);
  }

  private Invitation getAllInvitationByRequest(HttpServletRequest request) {
    String invitationId = request.getParameter("id");

    if (!StringUtils.hasText(invitationId)) {
      throw new IllegalArgumentException(
          "Missing parameter to identify the invitation");
    }

    return teamInviteService.findAllInvitationById(invitationId);
  }
}
