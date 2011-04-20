/**
 * 
 */
package nl.surfnet.coin.teams.control;

import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.opensocial.models.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.LocaleResolver;

import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.interceptor.LoginInterceptor;
import nl.surfnet.coin.teams.service.TeamService;
import nl.surfnet.coin.teams.util.ViewUtil;

/**
 * @author steinwelberg
 * 
 *         {@link Controller} that handles the home page of a logged in user.
 */
@Controller
public class HomeController {
  
  @Autowired
  private MessageSource messageSource;
  
  @Autowired
  private LocaleResolver localeResolver;

  @Autowired
  private TeamService teamService;

  private static final String STEM_PARAM = "stem";

  @RequestMapping("/home.shtml")
  public String start(ModelMap modelMap, HttpServletRequest request) {

    Person person = (Person) request.getSession().getAttribute(
        LoginInterceptor.PERSON_SESSION_KEY);
    String display = request.getParameter("teams");
    String query = request.getParameter("teamSearch");

    setStemOnSession(request);
    // Set the display to my if no display is selected
    if (!StringUtils.hasText(display)) {
      display = "my";
    }

    addTeams(query, person.getId(), display, modelMap, request);

    ViewUtil.addViewToModelMap(request, modelMap);

    return "home";
  }

  private void addTeams(String query, String person, String display, ModelMap modelMap,
      HttpServletRequest request) {
    
    Locale locale = localeResolver.resolveLocale(request);
    
    if (messageSource.getMessage("jsp.home.SearchTeam", null, locale).equals(query)) {
        query = null;
    }
    
    // Display all teams when the person is empty or when display equals "all"
    if ("all".equals(display) || !StringUtils.hasText(person)) {
      List<Team> teams;
      if (!StringUtils.hasText(query)) {
        teams = teamService.findAllTeams(getStemName(request));
      } else {
        teams = teamService.findTeams(query);
      }
      
      modelMap.addAttribute("display", "all");
      modelMap.addAttribute("teams", teams);
      
      for (Team team : teams) {
        team.setViewerRole(person);
      }
        
      // else always display my teams
    } else {
      
      List<Team> teams = null;
      if (!StringUtils.hasText(query)) {
        teams = teamService.getTeamsByMember(person);
      } else {
        teams = teamService.findTeams(query, person);
      }
      
      for (Team team : teams) {
        team.setViewerRole(person);
      }
      
      modelMap.addAttribute("display", "my");
      modelMap.addAttribute("teams", teams);
    }
    
    modelMap.addAttribute("query", query);
  }

  /**
   * Sets the stem on the session based on the request param {@link #STEM_PARAM}.
   * If the param is missing and stem is on the session, it will be removed
   * @param request {@link HttpServletRequest}
   */
  private void setStemOnSession(HttpServletRequest request) {
    String stem = request.getParameter(STEM_PARAM);
    if (StringUtils.hasText(stem)) {
      request.getSession(true).setAttribute(STEM_PARAM, stem);
    } else if (request.getSession(false) != null) {
      request.getSession(false).removeAttribute(STEM_PARAM);
    }
  }

  /**
   * Returns the stem name for this request
   *
   * @param request {@link HttpServletRequest}
   * @return the stem name on the session or
   *         {@literal null} if there is no stem
   */
  static String getStemName(final HttpServletRequest request) {
    if (request.getSession(false) == null) {
      return null;
    }
    Object stemObj = request.getSession(false).getAttribute(STEM_PARAM);
    if (stemObj instanceof String) {
      return (String) stemObj;
    }
    return null;
  }
  
}
