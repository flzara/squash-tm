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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="authz" tagdir="/WEB-INF/tags/authz" %>
<%@ taglib prefix="at" tagdir="/WEB-INF/tags/attachments"%>
<%@ taglib prefix="dashboard" tagdir="/WEB-INF/tags/dashboard"%>

<?xml version="1.0" encoding="utf-8" ?>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>


<s:url var="folderUrl" value="/${ updateUrl }/{folderId}">
	<s:param name="folderId" value="${folder.id}" />
</s:url>

<s:url var="statsUrl" value="/test-case-browser/statistics">
	<s:param name="libraries" value="" />
	<s:param name="nodes" value="${folder.id}"/>
</s:url>


<c:if test="${empty editable}">
	<c:set var="editable" value="${ false }" />
	<authz:authorized hasRole="ROLE_ADMIN" hasPermission="WRITE" domainObject="${ folder }">
		<c:set var="editable" value="${ true }" />
	</authz:authorized>
</c:if>



<c:if test="${ editable }">
   <c:set var="descrRicheditAttributes" value="class='editable rich-editable' data-def='url=${folderUrl}'"/>
</c:if>


<div class="ui-widget-header ui-corner-all ui-state-default fragment-header">
  <div id="right-frame-button">
    <f:message var="toggleLibraryTooltip" key="tooltip.toggleLibraryDisplay" />
	<input type="button" class="sq-btn btn-sm" id="toggle-expand-left-frame-button" title="${toggleLibraryTooltip}"/>
  </div>

	<h2>
		<span id="folder-name"><c:out value="${ folder.name }" escapeXml="true"/></span>
	</h2>
</div>


<div class="fragment-body">

	<%-- statistics panel --%>
<c:if test="${shouldShowDashboard}">
    <dashboard:favorite-dashboard />
</c:if>

<c:if test="${not shouldShowDashboard}">
	<dashboard:test-cases-dashboard-panel url="${statsUrl}"/>
</c:if>





	<%-- description panel --%>

	<comp:toggle-panel id="folder-description-panel" titleKey="label.Description"  open="true">
		<jsp:attribute name="body">
			<div id="folder-description" ${descrRicheditAttributes}>${ folder.description }</div>
		</jsp:attribute>
	</comp:toggle-panel>


	<%-- attachments panel --%>

	<at:attachment-bloc editable="${ editable }" workspaceName="${ workspaceName }" attachListId="${ folder.attachmentList.id }" attachmentSet="${attachments}"/>

	<script type="text/javascript">

	var identity = { resid : ${folder.id}, restype : 'test-case-folders'  };
	var shouldShowDashboard = ${shouldShowDashboard};

	require(["common"], function(){
			require(["jquery", "squash.basicwidgets","contextual-content-handlers",  "test-case-folder-management", "favorite-dashboard"],
					function($, basic, contentHandlers, TCFM, favoriteMain){
		$(function(){

				basic.init();

				var nameHandler = contentHandlers.getSimpleNameHandler();

				nameHandler.identity = identity;
				nameHandler.nameDisplay = "#folder-name";


				//init the default dashboard
				if(shouldShowDashboard){
          squashtm.workspace.canShowFavoriteDashboard = ${canShowDashboard};
          squashtm.workspace.shouldShowFavoriteDashboard = shouldShowDashboard;
          favoriteMain.init();
				}
				else {
          TCFM.initDashboardPanel({
            master : '#dashboard-master',
            cacheKey : 'dashboard-tcfold${folder.id}'
          });
         }
			});
		});
	});

</script>

</div>


