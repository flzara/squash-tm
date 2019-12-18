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
<?xml version="1.0" encoding="utf-8" ?>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"  %>
<%@ taglib prefix="reqs" tagdir="/WEB-INF/tags/requirements-components"  %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix='fn' uri='http://java.sun.com/jsp/jstl/functions' %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="authz" tagdir="/WEB-INF/tags/authz" %>
<%@ taglib prefix="json" uri="http://org.squashtest.tm/taglib/json" %>

<s:url var="requirementUrl" value="/requirements/{reqId}">
	<s:param name="reqId" value="${requirement.id}" />
</s:url>
<c:set var="displayedVersions" value="10" />

<c:url var="dtModel"	value='/requirements/${requirement.id}/versions/table' />
<c:url var="dtMessages" value="/datatables/messages"/>

<layout:common-import-outer-frame-layout highlightedWorkspace="requirement" titleKey="squashtm.library.requirement.title">
	<jsp:attribute  name="head">	
		<comp:sq-css name="squash.blue.css" />
		<%-- css override is needed in case of a sub page. --%>
		<comp:sq-css name="squash.core.override.css" />
		<comp:sq-css name="squash.subpage.override.css" />
	</jsp:attribute>
	
	<jsp:attribute name="titlePane">
		<h2><f:message key="squashtm.library.requirement.title" /></h2>	
	</jsp:attribute>
	
	<jsp:attribute name="content">
		<div id="sub-page" class="sub-page" >
			<div id="sub-page-header" class="sub-page-header">
			
				<div id="sub-page-title" class="sub-page-title">
					<h2><f:message key="requirement-versions.manager.title" /></h2>
				</div>
				
				<div id="sub-page-buttons" class="sub-page-buttons">
					<f:message var="backButtonLabel" key="label.Back" />
					<input type="button" id="back" class="sq-btn" value="${backButtonLabel}" onClick="document.location.href=squashtm.workspace.backurl"/>	
				</div>
				
				
				<authz:authorized hasRole="ROLE_ADMIN" hasPermission="WRITE" domainObject="${ requirement }">
					<comp:opened-object otherViewers="${ otherViewers }" objectUrl="${ requirementUrl }" />
				</authz:authorized>
				<div class="unsnap"></div>
			</div>
      
			<c:set var="milestoneVisibility" value="${(milestoneConf.milestoneDatesColumnVisible) ? '' : ', invisible'}"/> 
			<div id="sub-page-list-panel" class="sub-page-list-panel shadow ui-corner-all ui-helper-reset ui-widget ui-widget-content" >
				<table id="versions-table" class="unstyled-table" data-def="ajaxsource=${dtModel}, hover, pre-sort=1-desc, deferLoading=${fn:length(versions)}">
					<thead>
						<tr>
							<th data-def="map=entity-id, invisible">Id</th>
							<th data-def="map=version-number, select"><f:message key="requirement.versions.table.col-header.version-number" /></th>
							<th data-def="map=reference"><f:message key="requirement.versions.table.col-header.reference" /></th>
							<th data-def="map=name"><f:message key="label.Name" /></th>
							<th data-def="map=status"><f:message key="requirement.versions.table.col-header.status" /></th>
							<th data-def="map=criticality"><f:message key="requirement.versions.table.col-header.criticality" /></th>
							<th data-def="map=category"><f:message key="requirement.versions.table.col-header.category" /></th>
                            <th data-def="map=milestone-dates, tooltip-target=milestone ${milestoneVisibility}"><f:message key="label.Milestones"/></th>
                            <th data-def="map=milestone, invisible"></th>
						</tr>
					</thead>
					<tbody >
                      <%-- populated by ajax/js --%>
					</tbody>
				</table>
			</div>	
			
			<div id="sub-page-selection-panel" class="sub-page-selection-panel shadow ui-corner-all ui-component">
				<div id="contextual-content">
				<reqs:requirement-version-editor requirementVersion="${ selectedVersion }" jsonCriticalities="${ criticalityList }" 
											   jsonCategories="${ categoryList }" verifyingTestCasesModel="${verifyingTestCasesModel}"/>
				</div>
			</div>	
			
				
				
			<script type="text/javascript">
			require(["common"], function() {
				
					require(['jquery', 'workspace.contextual-content', 'squashtable'], function($, ctxcontent){
				$(function(){
						var table = $("#versions-table").squashTable({
							aaData : ${json:serialize(versionsTableModel.aaData)},
							// select the initially selected version 
							fnInitComplete : function(){
								table.find('tbody tr').filter(function(){ 
									return $('td:first-child', this).text()==='${selectedVersion.versionNumber}';
								})
								.addClass('ui-state-row-selected');
							}
						}, {});			
						
						var showSelectedVersion = function(table) {
							var ids = table.getSelectedIds();
							
							// the test is supposed to always hold true but if shoite happens better have it fail gracefully
							if (ids.length>0){
								var id = ids[0];
								var urlPattern = "<c:url value='/requirement-versions/selectedVersionId/editor-fragment' />";						
								ctxcontent.loadWith(urlPattern.replace("selectedVersionId", id));						
							}
						}
						
						table.on('click', 'tbody tr', function() {
							
							var row = $(this);					
							table.deselectRows();
							row.addClass('ui-state-row-selected');
							showSelectedVersion(table);				
							
						});
						
						/* refreshes table on ajax success for any actions in the editor below */
						$(document).ajaxSuccess(function(event, xrh, settings) {
							if (settings.type == 'POST' && settings.url.match(/requirement-versions\/\d+$/g)) {
								table.refresh();
							}
						});
					});
				});
			});
			</script>
		</div>
	
	</jsp:attribute>
</layout:common-import-outer-frame-layout>

