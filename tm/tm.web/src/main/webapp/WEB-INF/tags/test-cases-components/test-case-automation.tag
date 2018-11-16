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


<%@ attribute name="testCase" required="true" type="java.lang.Object" description="the testcase" %>

<c:set var="toInstruct" 	value="${(testCase.automatable == 'M') ? 'checked=\"checked\"' : ''}"/>
<c:set var="toAutomate" 	value="${(testCase.automatable == 'Y') ? 'checked=\"checked\"' : ''}"/>
<c:set var="toNotAutomate" 	value="${(testCase.automatable == 'N') ? 'checked=\"checked\"' : ''}"/>
<c:set var="requestStatus" 	value="${(testCase.automationRequest != null) ? testCase.automationRequest.requestStatus.getI18nKey() : 'automation-request.request_status.TO_VALIDATE'}" />


<c:url var="testCaseUrl" value="/test-cases/${testCase.id}"/>
<c:url var="automationRequestUrl" value="/automation-requests/${testCase.automationRequest.id}"/>

<f:message var="labelAutomation" key="label.automation"/>


<comp:toggle-panel id="test-case-automation-panel"
				   title='${labelAutomation}'
				   open="true">

	<jsp:attribute name="body">
	<div id="test-case-automation-table" class="display-table">

		<div class="display-table-row">
			<label class="display-table-cell" for="test-case-automation-indicator"><f:message key="test-case.automation-indicator.label"/></label>
			<div class="display-table-cell" id="test-case-automation-indicator">
				  <span>
				  	<input type="radio" name="test-case-automatable" value="M" ${toInstruct}>
				  	<f:message key="test-case.automation-to-instruct" />
				  </span>
				  <span>
					<input type="radio" name="test-case-automatable" value="Y" ${toAutomate}>
					<f:message key="test-case.automation-to-automate" />
				  </span>
				  <span>
					<input type="radio" name="test-case-automatable" value="N" ${toNotAutomate}>
					<f:message key="test-case.automation-to-not-automate" />
				  </span>
			</div>
		</div>
		<div class="display-table-row test-case-automation-request-block">
			<label class="display-table-cell" for="automation-request-priority">
				<f:message key="label.priority"/>
			</label>
			<div class="display-table-cell" id="automation-request-priority">
				<c:out value="${ testCase.automationRequest.automationPriority }" escapeXml="true"/>
			</div>
		</div>

		<div class="display-table-row test-case-automation-request-block">
			<label class="display-table-cell" for="automation-request-status">
				<f:message key="test-case.automation-status.label"/>
			</label>
			<div class="display-table-cell" id="automation-request-status">
				<f:message key="${requestStatus}"/>
			</div>
		</div>

	</div>
	</jsp:attribute>
</comp:toggle-panel>
