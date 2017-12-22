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
<%@ tag body-content="empty" description="the test plan panel for an iteration"%>


<%@ attribute name="linkable" type="java.lang.Boolean" description="can the user link this iteration to test cases ?"%>
<%@ attribute name="editable" type="java.lang.Boolean" description="can the user modify the existing test plan items ?"%>
<%@ attribute name="executable" type="java.lang.Boolean" description="can the user execute the test plan ?"%>
<%@ attribute name="reorderable" type="java.lang.Boolean" description="can the user reorder the test plan en masse ?"%>
<%@ attribute name="deletable" type="java.lang.Boolean" description="can the user remove an item which has not been executed yet ?"%>
<%@ attribute name="extendedDeletable" type="java.lang.Boolean" description="can the user remove an item which has been executed ?"%>

<%@ attribute name="assignableUsers" type="java.lang.Object"
  description="a map of users paired by id -> login. The id must be a string."%>
<%@ attribute name="weights" type="java.lang.Object"
  description="a map of weights paired by id -> internationalized text. The id must be a string."%>
<%@ attribute name="modes" type="java.lang.Object"
  description="a map of modes paired by id -> internationalized text. The id must be a string."%>
<%@ attribute name="statuses" type="java.lang.Object"
  description="a map of execution statuses paired by id -> internationalized text. The id must be a string."%>
<%@ attribute name="testSuite" type="java.lang.Object" description="the instance of test suite"%>
<%@ attribute name="milestoneConf" type="java.lang.Object" description="an instance of MilestoneFeatureConfiguration" %>


<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="json" uri="http://org.squashtest.tm/taglib/json"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="fn"   uri="http://java.sun.com/jsp/jstl/functions"%>

<s:url var="dtMessagesUrl" value="/datatables/messages" />
<s:url var="tableModelUrl" value="/test-suites/{suiteId}/test-plan">
  <s:param name="suiteId" value="${testSuite.id}" />
</s:url>

<s:url var="testcaseUrl"  value="/test-cases/{tc-id}/info" />
<s:url var="workspaceUrl" value="/test-case-workspace" />

<div id="test-suite-test-plans-panel" class="table-tab">

  <%-- ==================== THE TOOLBAR ==================== --%>

  <div class="cf">

    <f:message var="tooltipSortmode" key="tooltips.TestPlanSortMode" />
    <f:message var="messageSortmode" key="message.TestPlanSortMode" />
    <f:message var="reorderLabel" key="label.Reorder" />
    <f:message var="filterLabel" key="label.Filter" />
    <f:message var="filterTooltip" key="tooltips.FilterTestPlan" />
    <f:message var="reorderTooltip" key="tooltips.ReorderTestPlan" />
    <f:message var="associateLabel" key="label.Add" />
    <f:message var="removeLabel" key="label.removeFromExecutionPlan" />
    <f:message var="removeTooltip" key="label.removeFromExecutionPlan" />
    <f:message var="statusLabel" key="label.Status" />
    <f:message var="assignLabel" key="label.Assign" />
    <f:message var="removeEverywhereLabel" key="label.RemoveTSAndIT" />
    <f:message var="tooltipStatus" key="tooltips.changeExecutionStatuses" />
    <f:message var="tooltipAddTPI" key="tooltips.AddTPIToTP" />
    <f:message var="tooltipRemoveTPI" key="tooltips.RemoveTPIFromTP" />
    <f:message var="tooltipAssign" key="tooltips.AssignUserToTPI" />
    <f:message var="confirmLabel" key="label.Confirm" />
    <f:message var="cancelLabel" key="label.Cancel" />
    <f:message var="closeLabel" key="label.Close" />
    <f:message var="modeLabel" key="label.Mode" />
    <f:message var="tooltipReference" key="label.Reference"/>
    <f:message var="tooltipImportance" key="label.Importance"/>

    <c:if test="${ reorderable }">
      <div class="left btn-toolbar">
        <span class="group left-buttons">
          <button id="filter-test-plan-button" class="sq-btn btn-sm" title="${filterTooltip}">
            <span class="ui-icon ui-icon-refresh"></span>
            ${filterLabel}
          </button>
          <button id="reorder-test-plan-button" class="sq-btn btn-sm" title="${reorderTooltip}">
            <span class="ui-icon ui-icon-refresh"></span>
            ${reorderLabel}
          </button>
          <span id="test-plan-sort-mode-message" class="not-displayed sort-mode-message small"
            title="${tooltipSortmode}">${messageSortmode}</span>
        </span>
      </div>
    </c:if>

    <c:if test="${linkable or editable }">
      <div class="right btn-toolbar">
        <c:if test="${ editable }">
          <span class="btn-group">
            <button id="change-status-button" class="sq-btn btn-sm" title="${tooltipStatus}">
              <span class="ui-icon ui-icon-radio-off"></span>
              ${statusLabel}
            </button>
          </span>
          <span class="btn-group">
            <button id="assign-users-button" class="sq-btn btn-sm" title="${tooltipAssign}">
              <span class="ui-icon ui-icon-person"></span>
              ${assignLabel}
            </button>
          </span>
        </c:if>
        <c:if test="${linkable }">
          <span class="btn-group">
            <button id="navigate-test-plan-manager" class="sq-btn btn-sm" title="${tooltipAddTPI}">
              <span class="ui-icon ui-icon-plusthick"></span>
              ${associateLabel}
            </button>
          <button id="remove-test-plan-button" class="sq-btn btn-sm" title="${removeTooltip}">
            <span class="ui-icon ui-icon-trash"></span>
            ${removeLabel}
          </button>
          </span>
        </c:if>
      </div>
    </c:if>
  </div>

  <%-- ===================== THE TABLE ===================== --%>
  <%--
    Because the filtering/sorting system might not like that a column may be defined or not,
    the column must always be present. It may, however, be displayed or not.

    As per stupid specification, instead of the normal conditions the milestone dates column
    must be displayed if the feature is globally-enabled but not user-enabled

    for f*** sakes
   --%>
 <c:set var="milestoneVisibility" value="${(milestoneConf.globallyEnabled and not milestoneConf.userEnabled) ? '' : ', invisible'}"/>

  <div class="table-tab-wrap">

    <table id="test-suite-test-plans-table" class="test-plan-table unstyled-table" data-def="ajaxsource=${tableModelUrl}"  data-entity-id="${testSuite.id}" data-entity-type="testSuite">
      <thead>
        <tr>
          <th class="no-user-select"
            data-def="map=entity-index, select, sortable, center, sClass=drag-handle, sWidth=2.5em">#</th>
          <th class="no-user-select tp-th-filter tp-th-project-name" data-def="map=project-name, sortable, link=${workspaceUrl}, link-cookie=workspace-prefs=TEST_CASE-{tc-id}">
            <f:message key="label.Location" />
          </th>
          <th class="no-user-select" data-def="sortable, map=milestone-dates, tooltip-target=milestone-labels ${milestoneVisibility}">
            <f:message key="label.Milestone"/>
          </th>
          <th title=<f:message key="label.Mode" /> class="no-user-select tp-th-filter tp-th-exec-mode"
            data-def="map=exec-mode, sortable, center, visible=${testSuite.project.testAutomationEnabled}, sClass=exec-mode, sWidth=5%">
            <f:message key="label.Mode" />
          </th>
          <th class="no-user-select tp-th-filter tp-th-reference" title="${tooltipReference}"  data-def="map=reference, sortable, link=${testcaseUrl}">
            <f:message key="label.Reference.short" />
          </th>
          <th class="no-user-select tp-th-filter tp-th-name" data-def="map=tc-name, sortable, sClass=toggle-row">
            <f:message key="label.TestCase.short" />
          </th>
          <th class="no-user-select tp-th-filter tp-th-importance"  title="${tooltipImportance}" data-def="map=importance, sortable">
            <f:message key="label.Importance.short" />
          </th>
          <th class="no-user-select tp-th-filter tp-th-dataset" data-def="map=dataset.selected.name, sortable, sWidth=10%, sClass=dataset-combo">
            <f:message key="label.Dataset" />
          </th>
          <th class="no-user-select tp-th-filter tp-th-status" data-def="map=status, sortable, sWidth=10%, sClass=status-display status-combo">
            <f:message key="iteration.executions.table.column-header.status.label" />
          </th>
          <th class="no-user-select tp-th-succesPercent" data-def="map=succesPercent, sWidth=6%, center">
            <f:message key="iteration.executions.table.column-header.succesPercent.label" />
          </th>
          <th class="no-user-select tp-th-filter tp-th-assignee"
            data-def="map=assignee-login, sortable, sWidth=10%, sClass=assignee-combo">
            <f:message key="iteration.executions.table.column-header.user.label" />
          </th>
          <th class="no-user-select tp-th-filter tp-th-exec-on" data-def="map=last-exec-on, sortable, sWidth=10%, sClass=exec-on">
            <f:message key="label.LastExecutionOn" />
          </th>
          <th class="no-user-select" data-def="map=empty-execute-holder, narrow, center, sClass=execute-button">&nbsp;</th>
          <th class="no-user-select" data-def="map=empty-delete-holder, sClass=unbind-or-delete">&nbsp;</th>
        </tr>
      </thead>
      <tbody>
        <%-- Will be populated through ajax --%>
      </tbody>
    </table>

    <div id="shortcut-exec-menu-template" class="not-displayed">
      <div class="buttonmenu execute-arrow cursor-pointer"></div>
      <ul style="display: none">
        <li class="cursor-pointer">
          <a data-tpid="#placeholder-tpid#" class="run-menu-item run-popup">
            <f:message key="test-suite.execution.classic.label" />
          </a>
        </li>
        <li class="cursor-pointer">
          <a data-tpid="#placeholder-tpid#" class="run-menu-item run-oer">
            <f:message key="test-suite.execution.optimized.label" />
          </a>
        </li>
      </ul>
    </div>


    <%-- ============================== THE DIALOGS ========================= --%>


    <div id="ts-test-plan-delete-dialog" class="not-displayed popup-dialog"
      title="<f:message key="dialog.remove-testcase-testsuite-associations.title" />">

      <comp:notification-pane type="warning">
        <jsp:attribute name="htmlcontent">
          <span data-def="state=single-tp" >
            <f:message key="dialog.remove-testcase-testsuite-association.message" />
            <br/>
            <f:message key="message.permissions.confirm"/>
          </span>
          <span data-def="state=multiple-tp">
            <f:message key="dialog.remove-testcase-testsuite-associations.message" />
            <br/>
            <f:message key="message.permissions.confirm"/>
          </span>
        </jsp:attribute>
      </comp:notification-pane>

      <div class="popup-dialog-buttonpane">
        <input type="button" value="${removeLabel}"
          data-def="state=single-tp multiple-tp, mainbtn=single-tp multiple-tp, evt=confirm" />
        <input type="button" value="${removeEverywhereLabel}" data-def="state=single-tp multiple-tp, evt=confirmall" />
        <input type="button" value="${closeLabel}" data-def="evt=cancel" />
      </div>
    </div>

    <div id="ts-test-plan-delete-execution-dialog" class="not-displayed popup-dialog"
      title="<f:message key="dialog.delete-execution.title" />">
      <span style="font-weight: bold;">
        <f:message key="dialog.delete-execution.message" />
      </span>
      <div class="popup-dialog-buttonpane">
        <input type="button" value="${confirmLabel}" />
        <input type="button" value="${cancelLabel}" />
      </div>
    </div>

    <div id="ts-test-plan-batch-assign" class="not-displayed popup-dialog" title="<f:message key="label.AssignUser"/>">
      <div data-def="state=assign">
        <span>
          <f:message key="message.AssignTestCaseToUser" />
        </span>
        <select class="batch-select">
          <c:forEach var="user" items="${assignableUsers}">
            <option value="${user.key}">${user.value}</option>
          </c:forEach>
        </select>
      </div>

      <div class="popup-dialog-buttonpane">
        <input type="button" value="${confirmLabel}" data-def="state=assign, mainbtn=assign, evt=confirm" />
        <input type="button" value="${cancelLabel}" data-def="evt=cancel" />
      </div>
    </div>
    <div id="ts-test-plan-batch-edit-status" class="not-displayed popup-dialog"
      title="<f:message key="title.batchEditStatus"/>">
      <div data-def="state=edit">
        <span>
          <f:message key="message.batchEditStatus" />
        </span>
        <select class="execution-status-combo-class">
          <c:forEach var="status" items="${statuses}">
            <option class="exec-status-option exec-status-${fn:toLowerCase(status.key)}" value="${status.key}">${status.value}</option>
          </c:forEach>
        </select>
      </div>

      <div class="popup-dialog-buttonpane">
        <input type="button" value="${confirmLabel}" data-def="state=edit, mainbtn=edit, evt=confirm" />
        <input type="button" value="${cancelLabel}" data-def="evt=cancel" />
      </div>
    </div>
    <div id="ts-test-plan-reorder-dialog" class="not-displayed popup-dialog" title="${reorderLabel}">
      <span>
        <f:message key="message.ReorderTestPlan" />
      </span>
      <div class="popup-dialog-buttonpane">
        <input type="button" value="${confirmLabel}" />
        <input type="button" value="${cancelLabel}" />
      </div>
    </div>

  </div>
</div>
<!-- /test plan panel end -->

<script type="text/javascript">
require(["common"], function(){
	require(['test-suite-management'], function(tsInit){
			$(function() {
			var conf = {
				permissions : {
					linkable : ${linkable},
					editable : ${editable},
					executable : ${executable},
					reorderable : ${reorderable},
					deletable : ${deletable},
					extendedDeletable : ${extendedDeletable}
				},
				basic : {
					testsuiteId : ${testSuite.id},
					assignableUsers : ${ json:serialize(assignableUsers) },
					weights : ${ json:serialize(weights)},
					modes : ${ json:serialize(modes)},
					statuses :  ${ json:serialize(statuses)}
				}
			};
			tsInit.initTestPlanPanel(conf);
		});
	});
});
</script>

