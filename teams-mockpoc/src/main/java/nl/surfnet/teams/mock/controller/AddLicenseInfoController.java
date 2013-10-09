package nl.surfnet.teams.mock.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import edu.internet2.middleware.grouperClient.GrouperClient;
import edu.internet2.middleware.grouperClient.api.GcGetAttributeAssignments;
import edu.internet2.middleware.grouperClient.api.GcGetMemberships;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssign;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetAttributeAssignmentsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembershipsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;

@Controller
public class AddLicenseInfoController {
  private Logger LOG = LoggerFactory.getLogger(AddLicenseInfoController.class);
  
  @Autowired
  private GrouperClient grouperClient;
  
  @RequestMapping(value="/license.html",produces=MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  public String addLicenInfo(@RequestParam("userId") String userId, @RequestParam("serviceProviderEntityId") String spEntityId, @RequestParam("identityProviderEntityId") String idpEntityId) {
    LOG.debug("retuning license information for {} on sp {} and idp {}", userId, spEntityId, idpEntityId);
    String licenseNumber = searchForLicense(userId, spEntityId);
    return "{\"status\":{\"licenseStatus\": \""+licenseNumber+"\"}}";
  }
  
  String searchForLicense(String userId, String spEntityId) {
    GcGetMemberships getMemberships = new GcGetMemberships();
    WsSubjectLookup actAsSubject = new WsSubjectLookup();
    actAsSubject.setSubjectId("GrouperSystem");
    getMemberships.assignActAsSubject(actAsSubject);
    WsSubjectLookup theUser = new WsSubjectLookup();
    theUser.setSubjectId(userId);
    getMemberships.addWsSubjectLookup(theUser);
    WsGetMembershipsResults qresult = getMemberships.execute();
    for (WsGroup current : qresult.getWsGroups()) {
      Map<String, String> attributes = findTeamAttributesByTeam(current.getDisplayName());
      if (null != attributes.get("nl:surfnet:diensten:spEntityId")) {
        LOG.info("found license for " + attributes.get("nl:surfnet:diensten:spEntityId"));
        if (attributes.get("nl:surfnet:diensten:spEntityId").equals(spEntityId)) {
          return attributes.get("nl:surfnet:diensten:licenseNumber");
        }
      }
    }
    return "";
  }
  
  Map<String, String> findTeamAttributesByTeam(String teamId) {
    Map<String, String> result = new HashMap<String, String>();
    
    GcGetAttributeAssignments query = new GcGetAttributeAssignments();
    query.assignAttributeAssignType("group");
    query.addOwnerGroupName(teamId);
    WsGetAttributeAssignmentsResults qresult = query.execute();
    if (null != qresult.getWsAttributeAssigns() && qresult.getWsAttributeAssigns().length > 0) {
      for (WsAttributeAssign assign : qresult.getWsAttributeAssigns()) {
        String attributeName = assign.getAttributeDefNameName();
        if (null != assign.getWsAttributeAssignValues() && assign.getWsAttributeAssignValues().length == 1) {
          String attributeValue = assign.getWsAttributeAssignValues()[0].getValueSystem();
          result.put(attributeName, attributeValue);
        }
      }
    }
    return result;
  }
}
