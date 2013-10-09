<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="teams"%>
<%--
  Copyright 2012 SURFnet bv, The Netherlands

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  --%>
<c:set var="pageTitle"><spring:message code="jsp.acceptinvitation.Title" /></c:set>
<teams:genericpage pageTitle="${pageTitle}">
<%-- = Content --%>
<div id="Content">
  <h1>${pageTitle}</h1>
  <c:choose>
  <c:when test="${not teamFull}">
  <p><spring:message code="jsp.acceptinvitation.Explanation" /></p>
  <c:if test="${not empty team.attributes}">
    <h4>Note this team can contain no more than <c:out value="${team.attributes['nl:surfnet:diensten:quantity']}" /> members </h4>
  </c:if>
  <br class="clear" />
  <div class="column-container">
    <div class="column first-column">
      <h3><spring:message code="jsp.acceptinvitation.PersonDetails" /></h3>
      <dl>
        <dt><spring:message code="jsp.acceptinvitation.UserName" /></dt>
        <dd><c:out value="${sessionScope.person.displayName}" /></dd>
        <dt><spring:message code="jsp.acceptinvitation.UserID" /></dt>
        <dd><c:out value="${sessionScope.person.id}" /></dd>
        <dt><spring:message code="jsp.acceptinvitation.HomeOrganization" /></dt>
        <dd class="last"><c:out value="${header.schacHomeOrganization}" /></dd>
      </dl>
    </div>
    <div class="column second-column">
      <h3><spring:message code="jsp.acceptinvitation.InvitationDetails" /></h3>
      <dl>
        <dt><spring:message code="jsp.acceptinvitation.InvitedFor" /></dt>
        <dd><c:out value="${team.name}" /></dd>
        <dt><spring:message code="jsp.acceptinvitation.CreatedOn" /></dt>
        <dd class="last"><fmt:formatDate value="${date}" pattern="dd-MM-yyyy"/></dd>
      </dl>
    </div>
  </div>
  <br class="clear" />
    <c:if test="${licenseInviteButIdpSpNotAllowed}">
      <p>
        If you accept this invitation, you are licensed to use the service this invitation is about.<br />
        However, your institution as a whole is not allowed to use this service.<br />
        A notification regarding this issue has been sent to the administrator of your institution.<br />
      </p>
    </c:if>
  <form action="doAcceptInvitation.shtml" id="AcceptInvitationForm">
    <fieldset>
      <input type="hidden" name="view" value="${view}"/>
      <input type="hidden" name="id" value="<c:out value="${invitation.invitationHash}"/>">
      <p class="label-field-wrapper">
        <input id ="TeamConsent" type="checkbox" name="consent" /><label for="TeamConsent" class="consent"><spring:message code='jsp.jointeam.Consent' /></label>
      </p>
    </fieldset>
    <fieldset class="center">
      <p class="submit-wrapper">
        <input class="button-disabled" type="submit" disabled="disabled" name="joinTeam"
               value="<spring:message code='jsp.acceptinvitation.Accept' />" />
        <input class="button-secondary" type="submit" name="cancelJoinTeam"
               value="<spring:message code='jsp.general.Cancel' />" />
      </p>
    </fieldset>
  </form>
  </c:when>
  <c:otherwise>
  <br class="clear" />
  <p>
    sorry, this team has exceeded the maximum number of members
  </p>
  </c:otherwise>
  </c:choose>
<%-- / Content --%>
</div>
</teams:genericpage>


