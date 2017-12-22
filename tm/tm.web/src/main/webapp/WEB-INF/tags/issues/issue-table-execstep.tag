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
<%@ tag description="Table displaying the issues for an ExecutionStep" body-content="empty" %>

<%@ tag language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="json" uri="http://org.squashtest.tm/taglib/json" %>

<%@ attribute name="interfaceDescriptor" type="java.lang.Object" required="true" description="an object holding the labels for the interface"%>
<%@ attribute name="dataUrl" required="true" description="where the table will fetch its data" %>
<%@ attribute name="bugTrackerUrl" required="true" description="where the delete buttons send the delete instruction" %>
<%@ attribute name="entityId" required="true" description="id of the current execution step" %>
<%@ attribute name="executable" required="true" description="if the user has EXECUTE rights on the execution" %>
<%@ attribute name="tableEntries" required="false" type="java.lang.Object" description="if set, must be valid aaData for datatables. Will then defer the ajax loading of the table." %>

<%--

	columns are :

		- URL (not shown)
		- ID
		- owner
		- Priority
		- Summary

 --%>

<c:url var="tLanguageUrl" value="/datatables/messages"/>
<c:if test="${executable}">
	<c:set var="deleteBtnClause" value=", sClass=centered delete-button"/>
</c:if>
<table id="issue-table" data-def="pre-sort=0-desc">
	<thead>
		<tr>
			<th style="cursor:pointer" data-def="link-new-tab={issue-url}, select, map=remote-id, sortable, narrow, sClass=id-header, sortable">${interfaceDescriptor.tableIssueIDHeader}</th>
				<th data-def="map=BtProject"><f:message key="bugtracker.project" /></th>
			<th data-def="map=summary">${interfaceDescriptor.tableSummaryHeader}</th>
			<th data-def="map=priority">${interfaceDescriptor.tablePriorityHeader}</th>
			<th data-def="narrow, map=empty-delete-holder${ deleteBtnClause }"></th>
		</tr>
	</thead>
	<tbody><%-- Will be populated through ajax --%></tbody>
</table>


<c:set var="deferLoading" value="${tableEntries == null? 0 : tableEntries.iTotalRecords}"/>

<script type="text/javascript">
require( ["common"], function(){
		require(["jquery","workspace.event-bus", "issues/issues-table"], function($,eventBus, it){
	function issueTableRowCallback(row, data, displayIndex) {
		var td=$(row).find("td:eq(2)");
		var encodedSummary = $("<div/>").text(data["summary"]).html();
		$(td).html(encodedSummary);
		return row;
	}
	$(function(){
			it.initTestStepIssueTable({
				target : '#issue-table',
				urls : {
					bugtracker : "${bugTrackerUrl}",
				},
				language : {
					removeMessage : '<f:message key="dialog.remove-testcase-association.message" />',
					removeTooltip : '<f:message key="test-case.verified_requirement_item.remove.button.label" />'
				},
				tblSettings : {
					iDeferLoading : ${deferLoading},
	        		<c:if test="${not empty tableEntries}">
	        		'aaData' : ${json:serialize(tableEntries.aaData)},
	        		</c:if>
					'ajax' : {
						url : "${dataUrl}",
						error : function(xhr){
							eventBus.trigger('bugtracker.ajaxerror', xhr);
							return false;
						}
					},
					'fnRowCallback' : issueTableRowCallback,
				}
			});
		});
	});
});
</script>
