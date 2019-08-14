<%--

        This file is part of the Squashtest platform.
        Copyright (C) Henix, henix.fr

        See the NOTICE file distributed with this work for additional
        information regarding copyright ownership.

        This is free software: you can redistribute it and/or modify
        it under the terms of the GNU Lesser General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        this software is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU Lesser General Public License for more details.

        You should have received a copy of the GNU Lesser General Public License
        along with this software.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ tag body-content="empty" description="the calling test case table" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="tc" tagdir="/WEB-INF/tags/test-cases-components" %>
<%@ taglib prefix="hu" uri="http://org.squashtest.tm/taglib/html-utils" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>


<%@ attribute name="testCase" required="true" type="java.lang.Object" description="the testcase" %>
<%@ attribute name="isRemoteAutomationWorkflowUsed" required="true" type="java.lang.Boolean" description="whether a remote automation workflow is used for the project" %>
<%@ attribute name="writable"  required="true" type="java.lang.Boolean"  description="if the user has write permission on this test case" %>

<c:set var="toInstruct" 	value="${(testCase.automatable == 'M') ? 'checked=\"checked\"' : ''}" />
<c:set var="toAutomate" 	value="${(testCase.automatable == 'Y') ? 'checked=\"checked\"' : ''}" />
<c:set var="toNotAutomate" 	value="${(testCase.automatable == 'N') ? 'checked=\"checked\"' : ''}" />
<c:set var="uuid" 	value="${testCase.uuid}" />

<c:url var="testCaseUrl" value="/test-cases/${testCase.id}" />

<f:message var="labelAutomation" key="label.automation" />
<f:message var="transmitLabel" key="automation.label.to_transmit" />
<f:message var="displayDateFormat" key="squashtm.dateformat" />

<comp:toggle-panel id="test-case-automation-panel"
				   title='${labelAutomation}'
				   open="true">

	<jsp:attribute name="panelButtons">
		<c:if test="${ writable }">
			<input type="button" value="${transmitLabel}"  title="${transmitLabel}"
            	id="transmit-test-case-autom-request-button" class="sq-btn test-case-automation-request-block" />
			</button>
		</c:if>
	</jsp:attribute>

	<jsp:attribute name="body">
	<div id="test-case-automation-table" class="display-table" style="width: 80%">

<div class="div-test-case-automatable" style="float:left;width:600px;">
		<div class="display-table-row">
			<label class="display-table-cell" for="test-case-automation-indicator"><f:message key="test-case.automation-indicator.label"/></label>
			<div class="display-table-cell" id="test-case-automation-indicator">
				  <label>
				  	<input type="radio" name="test-case-automatable" value="M" ${toInstruct}>
				  	<f:message key="test-case.automatable.M" />
				  </label>
				  <label>
					<input type="radio" name="test-case-automatable" value="Y" ${toAutomate}>
					<f:message key="test-case.automatable.Y" />
				  </label>
				  <label>
					<input type="radio" name="test-case-automatable" value="N" ${toNotAutomate}>
					<f:message key="test-case.automatable.N" />
				  </label>
			</div>
		</div>

    <div class="display-table-row test-case-automation-request-block">
      <label class="display-table-cell" for="automation-request-priority">
        <f:message key="test-case.automation-priority.label"/>
      </label>
      <%-- The below tags Must be on the same line, otherwise the editor will add extra spaces... --%>
      <div class="display-table-cell" id="automation-request-priority"><c:out value="${ testCase.automationRequest.automationPriority }" escapeXml="true"/></div>
    </div>

    <div class="display-table-row test-case-automation-request-block">
      <label id="automation-request-status-label" class="display-table-cell" for="automation-request-status">
        <f:message key="test-case.automation-status.label"/>
      </label>
      <div class="display-table-cell" id="automation-request-status">
        <span id="automation-request-status">${ automReqStatusLabel }</span>
      </div>
    </div>

	  <div class="display-table-row test-case-automation-request-block">
        <label for="test-case-uuid" class="display-table-cell"><f:message key="test-case.automation-uuid.label" /></label>
        <div class="display-table-cell">
          <span id="test-case-uuid">${uuid}</span>
        </div>
    </div>

    <c:if test="${ hasProjectWithTaServer ==true }">
         <tc:testcase-test-automation testCase="${testCase}"  canModify="${writable}"/>
    </c:if>
   <div class="display-table-row test-case-remote-automation-request-block">
         <label class="display-table-cell" for="automation-last-transmitted-on">
           <f:message key="automation.datatable.headers.transmittedon" />
         </label>
         <div class="display-table-cell" id="automation-last-transmitted-on">
           <span><f:formatDate value="${ automReqLastTransmittedOn }" pattern="${displayDateFormat}" /></span>
         </div>
     </div>

    </div><%-- div-test-case-automatable--%>

     <%-- When the automation workflow is the native one, the fields are editable, but not with the remote ones --%>
     <%--== If remote Automation Workflow is used ==--%>
<div class="div-test-case-automatable1" style="float:right;width:600px;">
     <div class="display-table-row test-case-remote-automation-request-block">
       <label class="display-table-cell" for="remote-automation-request-status">
         <f:message key="test-case.automation-status-remote.label" />
       </label>
       <div class="display-table-cell" id="remote-automation-request-status">
         <span>${ remoteReqStatusLabel }</span>
       </div>
       </div>
       <%--== invisible field==--%>
        <input type="hidden" id="finalStatusConfiged" name="finalStatusConfiged" value=${ finalStatusConfiged }>

       <div class="display-table-row test-case-remote-automation-request-block">
        <label class="display-table-cell" for="test-case-automatisable">
          <f:message key="test-case.automatisable.label" />
        </label>
        <div class="display-table-cell" id="test-case-automatisable">
          <span>${ automatedTestCase }</span>
        </div>
        </div>

     <div class="display-table-row test-case-remote-automation-request-block">
       <label class="display-table-cell" for="remote-automation-request-url">
         <f:message key="label.Url" />
       </label>
       <div id="remote-automation-request-url" class="display-table-cell"  >
       <c:if test="${ remoteReqUrl != '-' }">
       <a id="testUrl" href="${remoteReqUrl}" target="_blank"><c:out value="${remoteIssueKey}" /></a>
       </c:if>
       <c:if test="${ remoteReqUrl == '-' or (empty remoteReqUrl) }">
         <span id="span-remote-req-url"> ${ remoteReqUrl }</span>
        </c:if>
       </div>
     </div>
      <div class="display-table-row test-case-remote-automation-request-block">
        <label class="display-table-cell" for="remote-automation-request-assignedTo">
          <f:message key="label.assigned" />
        </label>
        <div class="display-table-cell" id="remote-automation-request-assignedTo">
          <span>${ remoteReqAssignedTo }</span>
        </div>
      </div>

</div><%-- test1--%>



	</div>
	</jsp:attribute>
</comp:toggle-panel>
