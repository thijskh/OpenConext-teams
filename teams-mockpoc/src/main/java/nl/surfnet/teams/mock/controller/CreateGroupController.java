package nl.surfnet.teams.mock.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import edu.internet2.middleware.grouperClient.GrouperClient;
import edu.internet2.middleware.grouperClient.api.GcGetAttributeAssignments;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetAttributeAssignmentsResults;

@Controller()
public class CreateGroupController {
  private Logger LOG = LoggerFactory.getLogger(CreateGroupController.class);
  
  @Autowired
  private GrouperClient grouperClient;

  @RequestMapping("/createGroup.html")
  public void createGroup(HttpServletResponse response) throws IOException {
    LOG.debug("create mock group with attributes");
    GcGetAttributeAssignments query = new GcGetAttributeAssignments();
    query.assignAttributeAssignType("group");
    
    query.addOwnerGroupName("nl:surfnet:diensten:csa_admin");
    WsGetAttributeAssignmentsResults qresult = query.execute();
    response.getWriter().println(qresult.getWsAttributeAssigns()[0].getAttributeDefName());
    response.getWriter().println(qresult.getWsAttributeAssigns()[0].getWsAttributeAssignValues()[0].getValueSystem());
  }
}
