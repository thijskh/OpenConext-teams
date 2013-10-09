package nl.surfnet.teams.mock.controller;

import edu.internet2.middleware.grouperClient.GrouperClient;
import edu.internet2.middleware.grouperClient.api.GcAddMember;
import edu.internet2.middleware.grouperClient.api.GcAssignAttributes;
import edu.internet2.middleware.grouperClient.api.GcAssignGrouperPrivileges;
import edu.internet2.middleware.grouperClient.api.GcGroupSave;
import edu.internet2.middleware.grouperClient.ws.beans.*;
import nl.surfnet.teams.mock.form.CreateGroupForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Scanner;
import java.util.UUID;

@Controller()
public class CreateGroupController {

  private static final String SP_ENTITY_ID = "http://mock-sp";
  private static final String TEAM_PREFIX = "nl:surfnet:diensten:";
  public static final String ATTR_LICENSENUMBER = "nl:surfnet:diensten:licenseNumber";
  public static final String ATTR_QUANTITY = "nl:surfnet:diensten:quantity";
  public static final String ATTR_SP_ENTITY_ID = "nl:surfnet:diensten:spEntityId";

  public static final String MAILSERVER = "localhost";

  private Logger LOG = LoggerFactory.getLogger(CreateGroupController.class);
  
  @Autowired
  private GrouperClient grouperClient;

  @RequestMapping(value="/createGroup.html", method=RequestMethod.POST)
  public void createGroup(@ModelAttribute("CreateGroupForm") CreateGroupForm form, HttpServletResponse response) throws IOException {
    String loid = generateLicenseNumber();
    String teamId = TEAM_PREFIX + generateTeamId(form.getProduct(), form.getProductvariation(), loid);
    String teamName = generateTeamName(form.getProduct(), form.getProductvariation(), loid);
    String spEntityId = SP_ENTITY_ID;

    LOG.debug("create group {} with attributes: {} {} {}", teamName, loid, form.getQuantity(), spEntityId);
    addTeam(teamId, teamName, "description");
    addAdminMember(teamId);
    addAttribute(ATTR_LICENSENUMBER, loid, teamId);
    addAttribute(ATTR_QUANTITY, form.getQuantity(), teamId);
    addAttribute(ATTR_SP_ENTITY_ID, spEntityId, teamId);

    String mailtemplate = new Scanner(new ClassPathResource("mailtemplate.txt").getFile()).useDelimiter("\\Z").next();
    String mailBody = String.format(mailtemplate, form.getProduct(), loid, form.getQuantity(), spEntityId);

    if (StringUtils.hasText(form.getManagermail())) {
      sendMail("noreply@surfconext.nl", form.getManagermail(), "Activate your license team", mailBody);
    }
    response.getWriter().println(String.format("License bought. (team created: %s)", teamId));
  }

  private String generateTeamName(String product, String productvariation, String loid) {
    return product + ", " + productvariation + ", license " + loid;
  }

  private String generateTeamId(String product, String productvariation, String loid) {
    return sanitize(product.substring(0, 6) + "-" + productvariation.substring(0,6) + "-" + loid);
  }

  private String sanitize(String s) {
    return s.replaceAll("/[^\\s\\d]/i", "");
  }

  private String generateLicenseNumber() {
    return UUID.randomUUID().toString();
  }

  void addAdminMember(String teamId) {
    // add member
    GcAddMember addMember = new GcAddMember();
    WsSubjectLookup actAsSubject = new WsSubjectLookup();
    actAsSubject.setSubjectId("GrouperSystem");
    addMember.assignActAsSubject(actAsSubject);
    addMember.assignGroupName(teamId);
    addMember.addSubjectId("urn:collab:person:example.com:admin");
    addMember.execute();
    
    //assign admin privileges to member
    GcAssignGrouperPrivileges assignPrivilege = new GcAssignGrouperPrivileges();
    assignPrivilege.assignActAsSubject(actAsSubject);
    assignPrivilege.assignGroupLookup(new WsGroupLookup(teamId, null));
    WsSubjectLookup subject = new WsSubjectLookup();
    subject.setSubjectId("urn:collab:person:example.com:admin");
    assignPrivilege.addSubjectLookup(subject);
    assignPrivilege.assignPrivilegeType("access");
    assignPrivilege.addPrivilegeName("admin");
    assignPrivilege.addPrivilegeName("read");
    assignPrivilege.addPrivilegeName("optout");
    assignPrivilege.addPrivilegeName("update");
    assignPrivilege.assignAllowed(true);
  
    WsAssignGrouperPrivilegesResults result = assignPrivilege.execute();
    if (!result.getResultMetadata().getResultCode().equals("SUCCESS")) {
      LOG.error("Group change failed, no admin added");
    }
  }
  
  void addTeam(String teamId, String displayName, String teamDescription) {
    WsGroup wsGroup = new WsGroup();
    wsGroup.setDescription(teamDescription);
    String dispName = displayName.substring(displayName.lastIndexOf(":")+1, displayName.length());
    wsGroup.setDisplayExtension(dispName);
    wsGroup.setName(teamId);

    WsGroupToSave group = new WsGroupToSave();
    group.setSaveMode("INSERT");
    group.setWsGroup(wsGroup);

    GcGroupSave groupSave = new GcGroupSave();
    WsSubjectLookup actAsSubject = new WsSubjectLookup();
    actAsSubject.setSubjectId("GrouperSystem");
    groupSave.assignActAsSubject(actAsSubject);
    groupSave.addGroupToSave(group);
    groupSave.execute();
  }
  
  void addAttribute(String key, String value, String teamId) {
    GcAssignAttributes assignAttributes = new GcAssignAttributes();
    assignAttributes.assignAttributeAssignType("group");
    assignAttributes.assignAttributeAssignOperation("assign_attr");
    assignAttributes.addAttributeDefNameName(key);
    assignAttributes.addOwnerGroupName(teamId);
    assignAttributes.assignAttributeAssignValueOperation("assign_value");
    WsAttributeAssignValue wsAttributeAssignValue = new WsAttributeAssignValue();
    wsAttributeAssignValue.setValueSystem(value);
    assignAttributes.addValue(wsAttributeAssignValue);
    WsSubjectLookup actAsSubject = new WsSubjectLookup();
    actAsSubject.setSubjectId("GrouperSystem");
    assignAttributes.assignActAsSubject(actAsSubject);
    assignAttributes.execute();
  }

  public void sendMail(String from, String to, String subject, String body) {
    System.getProperties().setProperty("mail.smtp.host", MAILSERVER);
    Session session = Session.getDefaultInstance(System.getProperties());
    try{
      MimeMessage message = new MimeMessage(session);
      message.setFrom(new InternetAddress(from));
      message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
      message.setSubject(subject);
      message.setText(body);
      Transport.send(message);
    }catch (MessagingException mex) {
      throw new RuntimeException(mex);
    }
  }
}
