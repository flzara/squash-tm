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

<%@ attribute name="testCase" required="true" type="java.lang.Object"  description="the testcase" %>

<c:url var="testCaseUrl"	value="/test-cases/${testCase.id}"/>



<div id="test-case-name-div" class="ui-widget-header ui-corner-all ui-state-default fragment-header">
<c:if test="${ not param.isInfoPage }">
  <div id="right-frame-button">
    <f:message var="toggleLibraryTooltip" key="tooltip.toggleLibraryDisplay" />
    <input type="button" class="sq-btn btn-sm" id="toggle-expand-left-frame-button" title="${toggleLibraryTooltip}"/>
  </div>
</c:if>
	<div style="float: left; height: 100%;" class="small-margin-left">
		<h2>
	
			
			<a id="test-case-name" href="${ testCaseUrl }/info">
				<c:out value="${testCase.fullName}" escapeXml="true" /> 
			</a>
			
			<%-- raw reference and name because we need to get the name and only the name for modification, and then re-compose the title with the reference  --%>
			<span id="test-case-raw-reference" style="display: none">
				<c:out value="${ testCase.reference }" escapeXml="true" /> 
			</span> 
			
			<span id="test-case-raw-name" style="display: none">
				<c:out value="${ testCase.name }" escapeXml="true" /> 
			</span>
		</h2>
	</div>

	<div class="unsnap"></div>
</div>
