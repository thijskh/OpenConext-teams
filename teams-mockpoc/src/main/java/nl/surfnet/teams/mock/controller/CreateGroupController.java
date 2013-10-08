package nl.surfnet.teams.mock.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import nl.surfnet.teams.mock.form.CreateGroupForm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import edu.internet2.middleware.grouperClient.GrouperClient;
import edu.internet2.middleware.grouperClient.api.GcAddMember;
import edu.internet2.middleware.grouperClient.api.GcAssignAttributes;
import edu.internet2.middleware.grouperClient.api.GcAssignGrouperPrivileges;
import edu.internet2.middleware.grouperClient.api.GcGroupSave;
import edu.internet2.middleware.grouperClient.ws.beans.WsAssignGrouperPrivilegesResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssignValue;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupToSave;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;

@Controller()
public class CreateGroupController {
  private Logger LOG = LoggerFactory.getLogger(CreateGroupController.class);
  
  @Autowired
  private GrouperClient grouperClient;

  @RequestMapping(value="/createGroup.html", method=RequestMethod.POST)
  public void createGroup(@ModelAttribute("CreateGroupForm") CreateGroupForm form, HttpServletResponse response) throws IOException {
    LOG.debug("create mock group {} with attributes: {} {}", form.getGroupName(), form.getLicenseNumber(), form.getQuantity());
    addTeam(form.getGroupName(), form.getGroupName(), "description");
    addAdminMember(form.getGroupName());
    addAttribute("nl:surfnet:diensten:licenseNumber", form.getLicenseNumber(), form.getGroupName());
    addAttribute("nl:surfnet:diensten:quantity", form.getQuantity(), form.getGroupName());
    
    response.getWriter().println("group created!");
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
    boolean success = result.getResultMetadata().getResultCode().equals("SUCCESS") ? true
        : false;
    if (!success) {
      LOG.error("Group change failed, no admin added");
    }
  }
  
  void addTeam(String teamId, String displayName,
      String teamDescription) {
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
  
  void addAttribute(String key, String value, String groupId) {
    GcAssignAttributes assignAttributes = new GcAssignAttributes();
    assignAttributes.assignAttributeAssignType("group");
    assignAttributes.assignAttributeAssignOperation("assign_attr");
    assignAttributes.addAttributeDefNameName(key);
    assignAttributes.addOwnerGroupName(groupId);
    assignAttributes.assignAttributeAssignValueOperation("assign_value");
    WsAttributeAssignValue wsAttributeAssignValue = new WsAttributeAssignValue();
    wsAttributeAssignValue.setValueSystem(value);
    assignAttributes.addValue(wsAttributeAssignValue);
    WsSubjectLookup actAsSubject = new WsSubjectLookup();
    actAsSubject.setSubjectId("GrouperSystem");
    assignAttributes.assignActAsSubject(actAsSubject);
    assignAttributes.execute();
  }
}
