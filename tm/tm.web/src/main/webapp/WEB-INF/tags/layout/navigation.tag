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
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f"  uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="wu" uri="http://org.squashtest.tm/taglib/workspace-utils" %>


<%@ attribute name="highlighted" description="which button should be highlithed"%>

<c:set var="rootctxt" value="${pageContext.servletContext.contextPath}"/>

<f:message var="homeTitle" 	key="workspace.home.button.label"/>
<f:message var="reqTitle" 	key="workspace.requirement.button.label"/>
<f:message var="tcTitle" 	key="workspace.test-case.button.label"/>
<f:message var="campTitle" 	key="workspace.campaign.button.label"/>
<f:message var="bugTitle" 	key="workspace.bugtracker.button.label"/>
<f:message var="repoTitle" 	key="workspace.report.button.label"/>
<f:message var="customReportTitle" 	key="workspace.custom-report.title.long"/>

<c:set var="visibleBugtrackers" value="${wu:getVisibleBugtrackers(pageContext.servletContext)}"/>
<c:set var="hideClass"			value="${empty visibleBugtrackers ? 'not-displayed' : ''}"/>

<div id="navigation" data-highlight="${ highlighted }">
	<div id="test_mgt_nav">
		<a id="requirement-link" 	style="margin-top: 15px;"	class="navigation-link navigation-requirement"	href="${rootctxt}/requirement-workspace/"	title="${reqTitle}"></a>
		<a id="test-case-link"	 	style="margin-top: 10px;"	class="navigation-link navigation-test-case" 	href="${rootctxt}/test-case-workspace/"		title="${tcTitle}"></a>
		<a id="campaign-link"	 	style="margin-top: 10px;"	class="navigation-link navigation-campaign"		href="${rootctxt}/campaign-workspace/"		title="${campTitle}"></a>
	</div>

	<div id="nav_logo">
		<div style="margin-bottom: 40px;">
			<a id="home-link" 		 	class="navigation-link navigation-home" 		href="${rootctxt}/home-workspace/"			title="${homeTitle}"></a>
			<a id="custom-report-link"	 	class="navigation-link navigation-custom-report" 			href="${rootctxt}/custom-report-workspace/"			title="${customReportTitle}"></a>
			<a id="bugtracker-link"	 	class="navigation-link navigation-bugtracker ${hideClass}" 	title="${bugTitle}"></a>
			<ul class="not-displayed width:130px;" style="max-height: 12.8em;">
			<c:forEach var="bugtracker" items="${visibleBugtrackers}">
				<li>
					<c:url var="btUrl" 			value="${bugtracker.iframeFriendly ? '/bugtracker/'.concat(bugtracker.id.toString()).concat('/workspace') : bugtracker.URL}"/>
					<c:set var="targetClause" 	value="${bugtracker.iframeFriendly ? 'target=\"_blank\"' : '' }"/>
					<a id="bugtracker-${bugtracker.id }" href="${btUrl}" ${targetClause}>${bugtracker.name}</a>
				</li>
			</c:forEach>
			</ul>
		</div>
		<div class="vertical-logo"></div>
	</div>
</div>
<script type="text/javascript">
publish("load.navBar");
</script>

