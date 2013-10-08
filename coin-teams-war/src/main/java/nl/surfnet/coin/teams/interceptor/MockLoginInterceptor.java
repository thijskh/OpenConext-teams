/*
 * Copyright 2011 SURFnet bv, The Netherlands
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

package nl.surfnet.coin.teams.interceptor;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import nl.surfnet.coin.api.client.domain.Email;
import nl.surfnet.coin.api.client.domain.Person;

/**
 * Like the LoginInterceptor but gets the user id from the environment instead
 * of Shibboleth.
 */
public class MockLoginInterceptor extends LoginInterceptor {

  /**
   * {@inheritDoc}
   */
  @Override
  void handleGuestStatus(HttpSession session, Person person) {
    session.setAttribute(USER_STATUS_SESSION_KEY,
            getTeamEnvironment().getMockUserStatus());
  }
  
  @Override
  public boolean preHandle(HttpServletRequest request,
      HttpServletResponse response, Object handler) throws Exception {
    HttpSession session = request.getSession();

    Person person = new Person();
    person.setId("urn:collab:person:example.com:admin");
    person.setDisplayName("admin");
    Email email = new Email("root@demo.openconext.org");
    Set<Email> emails = new HashSet<Email>();
    emails.add(email);
    person.setEmails(emails);
    session.setAttribute(PERSON_SESSION_KEY, person);
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getRemoteUser(HttpServletRequest request) {
    return getTeamEnvironment().getMockLogin();
  }

}
