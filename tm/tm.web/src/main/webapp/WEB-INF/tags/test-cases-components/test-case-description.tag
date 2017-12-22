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

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="tc" tagdir="/WEB-INF/tags/test-cases-components"%>


<%@ attribute name="testCase" required="true" type="java.lang.Object"  description="the testcase" %>
<%@ attribute name="writable"  required="true" type="java.lang.Boolean"  description="if the user has write permission on this test case" %>
<%@ attribute name="testCaseImportanceLabel"  required="true" type="java.lang.String"  description="a label related to test case importance, not sure to remember what." %>


<c:url var="testCaseUrl" 					value="/test-cases/${testCase.id}"/>

<f:message var="labelDescription" key="label.Description" />


<comp:toggle-panel id="test-case-description-panel"
				   title=  '${labelDescription} <span class="small txt-discreet">[ID = ${ testCase.id }]</span>'
				   open="true">

	<jsp:attribute name="body">
	<div id="test-case-description-table"  class="display-table">

		<div class="display-table-row">
			<label class="display-table-cell" for="test-case-reference"><f:message key="test-case.reference.label" /></label>
			<div class="display-table-cell" id="test-case-reference">
				<c:out value="${ testCase.reference }" escapeXml="true" />
			</div>
		</div>

		<div class="display-table-row">
			<label for="test-case-description" class="display-table-cell"><f:message key="label.Description" /></label>
			<div class="display-table-cell" id="test-case-description">${ testCase.description }</div>
		</div>



		<div class="display-table-row">
			<label for="test-case-status" class="display-table-cell"><f:message key="test-case.status.combo.label" /></label>
			<div class="display-table-cell">
			<span id="test-case-status-icon" style="vertical-align:middle" class="test-case-status-${testCase.status}"> &nbsp &nbsp</span>
            <span id="test-case-status">${ testCaseStatusLabel }</span>
			</div>
		</div>



		<%-- Test Automation structure --%>
		<c:if test="${testCase.project.testAutomationEnabled}">
		<tc:testcase-test-automation testCase="${testCase}"
										  canModify="${writable}" />
		</c:if>
		<%--/Test Automation structure --%>

	</div>
	</jsp:attribute>
</comp:toggle-panel>
