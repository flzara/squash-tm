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
<%@ taglib prefix="su" uri="http://org.squashtest.tm/taglib/string-utils" %>
<%@ taglib prefix="authz" tagdir="/WEB-INF/tags/authz" %>
<%@ taglib prefix="at" tagdir="/WEB-INF/tags/attachments"%>
<%@ taglib prefix="csst" uri="http://org.squashtest.tm/taglib/css-transform"%>
<%@ taglib prefix="issues" tagdir="/WEB-INF/tags/issues"%>
<%@ taglib prefix="dashboard" tagdir="/WEB-INF/tags/dashboard" %>

<?xml version="1.0" encoding="utf-8" ?>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>


<s:url var="folderUrl" value="/${ updateUrl }/{folderId}">
  <s:param name="folderId" value="${folder.id}" />
</s:url>

<c:url var="folderStatisticsUrl" value="/campaign-folders/${folder.id}/dashboard-statistics" />
<c:url var="folderStatisticsPrintUrl" value="/campaign-folders/${folder.id}/dashboard?printmode=true" />


<c:set var="hasBugtracker" value="${folder.project.bugtrackerConnected}"/>

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


  <csst:jq-tab>
    <div class="fragment-tabs fragment-body">
      <ul class="tab-menu">
        <li>
          <a href="#folder-dashboard">
            <f:message key="title.Dashboard"/>
          </a>
        </li>

<c:if test="${hasBugtracker}">
        <li>
          <%-- div#bugtracker-section-main-div is declared in tagfile issues:bugtracker-panel.tag  --%>
          <a href="#bugtracker-section-main-div"><f:message key="tabs.label.issues"/></a>
        </li>
</c:if>

        <li>
          <a href="#folder-infos">
            <f:message key="tabs.label.information" />
          </a>
        </li>
      </ul>

      <div id="folder-dashboard">
        <c:if test="${shouldShowDashboard}">
            <dashboard:favorite-dashboard />
        </c:if>

        <c:if test="${not shouldShowDashboard}">
          <dashboard:campaign-folder-dashboard-panel url="${folderStatisticsUrl}"
                                                                printUrl="${folderStatisticsPrintUrl}"
                                                                allowsSettled="${allowsSettled}"
                                                                allowsUntestable="${allowsUntestable}" />
        </c:if>
      </div>

      <%-- ----------------------- bugtracker (if present)----------------------------------------%>

      <c:if test="${hasBugtracker}">
              <issues:butracker-panel entity="${folder}" />
      </c:if>

      <div id="folder-infos">

        <comp:toggle-panel id="folder-description-panel" titleKey="label.Description" open="true">
          <jsp:attribute name="body">
            <div id="folder-description" ${descrRicheditAttributes}>${ folder.description }</div>
          </jsp:attribute>
        </comp:toggle-panel>

        <at:attachment-bloc editable="${ editable }" workspaceName="${ workspaceName }" attachListId="${ folder.attachmentList.id }" attachmentSet="${attachments}"/>

      </div>

    </div>
  </csst:jq-tab>



<script type="text/javascript">

  var identity = { resid : ${folder.id}, restype : '${su:camelCaseToHyphened(folder["class"].simpleName)}s'  };

  require(["common"], function(){
      require(["campaign-folder-management", "workspace.routing"],
          function(CFManager, routing){
            $(function(){

                var conf = {
                	basic : {
                		identity : identity
                	},
                	bugtracker : {
                		hasBugtracker : ${hasBugtracker},
                		url : routing.buildURL('bugtracker.campaignfolder', ${folder.id}),
                		style : "fragment-tab"
                	}
                };

                //favorite dashboard
                squashtm.workspace.canShowFavoriteDashboard = ${canShowDashboard};
                squashtm.workspace.shouldShowFavoriteDashboard = ${shouldShowDashboard};

                CFManager.init(conf);
     	 	});
    	});
  });

</script>

