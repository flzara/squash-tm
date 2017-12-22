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
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="json" uri="http://org.squashtest.tm/taglib/json" %>

<%@ attribute name="interfaceDescriptor" type="java.lang.Object" required="true" description="an object holding the labels for the interface" %>
<%@ attribute name="dataUrl" required="true" description="where the table will fetch its data" %>
<%@ attribute name="bugTrackerUrl" required="true" description="where the delete buttons send the delete instruction" %>
<%@ attribute name="tableEntries" required="false" type="java.lang.Object" description="if set, must be valid aaData for datatables. Will then defer the ajax loading of the table." %>
<%@ attribute name="entityId" required="true" description="id of the current execution" %>
<%@ attribute name="executable" required="true" description="if the user has EXECUTE rights on the execution" %>
<%--
	columns are :

		- URL  (not shown)
		- ID
		- owner
		- Priority
		- Summary
		- Status
		- Assignation

 --%>

<c:set var="deferLoading" value="${tableEntries == null? 0 : tableEntries.iTotalRecords}"/>

<script type="text/javascript">

	function refreshTestPlan() {
		$('#issue-table').squashTable().refresh();
	}

	function issueTableRowCallback(row, data, displayIndex) {
		checkEmptyValues(row, data);
		var td=$(row).find("td:eq(2)");
		var encodedSummary = $("<div/>").text(data["summary"]).html();
		$(td).html(encodedSummary);
		return row;
	}

	<%-- we check the assignee only (for now) --%>
	function checkEmptyValues(row, data){
		var assignee = data['assignee'];
		var correctAssignee = (assignee!=="") ? assignee : "${interfaceDescriptor.tableNoAssigneeLabel}";
		var td=$(row).find("td:eq(5)");
		$(td).html(correctAssignee);
	}

	/* ************************** datatable settings ********************* */


	$(function() {

		var tableSettings = {
				"aaSorting" : [[0,'desc']],
				"fnRowCallback" : issueTableRowCallback,
				'iDeferLoading' : ${deferLoading},
        		<c:if test="${not empty tableEntries}">
        		'aaData' : ${json:serialize(tableEntries.aaData)},
        		</c:if>
				'ajax' : {
					url : "${dataUrl}",
					error : function(xhr){
						squashtm.workspace.eventBus.trigger('bugtracker.ajaxerror', xhr);
						return false;
					}
				}
			};

			var squashSettings = {
				enableDnD : false,
				deleteButtons : {
					url : '${bugTrackerUrl}/issues/{local-id}',
					popupmessage : '<f:message key="dialog.remove-testcase-association.message" />',
					tooltip : '<f:message key="test-case.verified_requirement_item.remove.button.label" />',
					success : function(data) {
						refreshTestPlan();
					}
				}
			};

			$("#issue-table").squashTable(tableSettings, squashSettings);
	});

</script>

<c:url value='/datatables/messages' var="tableLangUrl" />
<c:if test="${executable}">
	<c:set var="deleteBtnClause" value=", sClass=delete-button"/>
</c:if>
<table id="issue-table" >
	<thead >
		<tr>
			<th data-def="map=remote-id, link-new-tab={issue-url}, center, select, double-narrow, sortable">${interfaceDescriptor.tableIssueIDHeader}</th>
			<th data-def="map=BtProject"><f:message key="bugtracker.project" /></th>
			<th data-def="map=summary">${interfaceDescriptor.tableSummaryHeader}</th>
			<th data-def="map=priority">${interfaceDescriptor.tablePriorityHeader}</th>
			<th data-def="map=status">${interfaceDescriptor.tableStatusHeader}</th>
			<th data-def="map=assignee">${interfaceDescriptor.tableAssigneeHeader}</th>
			<th data-def="map=owner">${interfaceDescriptor.tableReportedInHeader}</th>
			<th data-def="map=empty-delete-holder, narrow, center${deleteBtnClause}"></th>
		</tr>
	</thead>
	<tbody><%-- Will be populated through ajax --%></tbody>
</table>



