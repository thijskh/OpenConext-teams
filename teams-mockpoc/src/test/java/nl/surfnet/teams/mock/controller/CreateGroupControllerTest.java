package nl.surfnet.teams.mock.controller;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:teams-mockpoc-context.xml"})
public class CreateGroupControllerTest {
  
  @Autowired
  private CreateGroupController controller;
  
   @Test
   @Ignore
  public void testGroupCreation() {
    controller.addTeam("nl:surfnet:diensten:mockTeam", "mockDisplayName", "mockTeamDescription");
    controller.addAttribute("nl:surfnet:diensten:licenseNumber", "A21-1425-Vrijgeleide 38", "nl:surfnet:diensten:mockTeam");
    controller.addAttribute("nl:surfnet:diensten:quantity", "50", "nl:surfnet:diensten:mockTeam");
  }
  
  @Test
  @Ignore
  public void getGroup() throws IOException {
   controller.createGroup(null, null); 
  }
}
