package nl.surfnet.teams.mock.controller;

import static junit.framework.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:teams-mockpoc-context.xml"})
public class AddLicenseInfoControllerTest {
  
  @Autowired
  private AddLicenseInfoController controller;
  
  @Test
  public void testRetrieveGroups() {
    String license = controller.searchForLicense("urn:collab:person:example.com:admin", "http://mock-sp");
    assertEquals("2345-HS-21A", license);
  }
}
