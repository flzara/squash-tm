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
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>


<%@ attribute name="testCase" required="true" type="java.lang.Object" description="the testcase" %>
<%@ attribute name="writable" required="true" type="java.lang.Boolean"
			  description="if the user has write permission on this test case" %>

<c:url var="testCaseUrl" value="/test-cases/${testCase.id}"/>


<comp:toggle-panel id="test-case-autom-attributes-panel"
				   titleKey='label.automation'
				   open="true">

	<jsp:attribute name="body">
	<div id="test-case-autom-attributes-table" class="display-table">

			<%-- Test Automation structure --%>
    <div class="display-table-row">
      <label for="test-case-automated-test-technology" class="display-table-cell">
        <f:message key="test-case.automated-test-technology.label" />
      </label>
      <div class="display-table-cell">
        <span id="test-case-automated-test-technology"><c:out value="${ testCase.automatedTestTechnology.name }" escapeXml="true"/></span>
      </div>
    </div>
    <div class="display-table-row">
        <label class="display-table-cell" for="test-case-source-code-repository-url"><f:message
          key="test-case.source-code-repository-url.label"/></label>
        <div class="display-table-cell" id="test-case-source-code-repository-url"><c:out value="${ testCase.sourceCodeRepositoryUrl }" escapeXml="true"/></div>
      </div>
      <div class="display-table-row">
        <label class="display-table-cell" for="test-case-automated-test-reference"><f:message
          key="test-case.automated-test-reference.label"/></label>
        <div class="display-table-cell" id="test-case-automated-test-reference"><c:out value="${ testCase.automatedTestReference }" escapeXml="true"/></div>
      </div>
			<%--/Test Automation structure --%>

	</div>
	</jsp:attribute>
</comp:toggle-panel>
