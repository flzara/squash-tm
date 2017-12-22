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
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="authz" tagdir="/WEB-INF/tags/authz" %>
<%@ taglib prefix="at" tagdir="/WEB-INF/tags/attachments" %>
<%@ taglib prefix="issues" tagdir="/WEB-INF/tags/issues" %>
<%@ taglib prefix="json" uri="http://org.squashtest.tm/taglib/json" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>


<c:url var="executionUrl" value="/executions/${execution.id}"/>
<s:url var="attachmentsUrl" value="/attach-list/${execution.attachmentList.id}/attachments"/>


<%-- ----------------------------------- Authorization ----------------------------------------------%>
<c:set var="editable" value="${false}"/>

<c:if test="${not milestoneConf.locked}">
  <authz:authorized hasRole="ROLE_ADMIN" hasPermission="EXECUTE" domainObject="${ execution }">
    <c:set var="editable" value="${ true }"/>
  </authz:authorized>
</c:if>


<%-- -----------------------------test automation ------------------------------ --%>

<c:set var="automated" value="${ execution.executionMode == 'AUTOMATED' }"/>
<c:set var="taDisassociated" value="${ automated and execution.automatedExecutionExtender.projectDisassociated}"/>

<f:message var="taDisassociatedLabel" key="squashtm.itemdeleted"/>
<f:message var="confirmLabel" key="label.Confirm"/>
<f:message var="cancelLabel" key="label.Cancel"/>
<f:message var="deleteExecutionButton" key="execution.execute.remove.button.label"/>

<%-- ============ module configuration ===================== --%>

<script type="text/javascript">

  requirejs.config({
    config: {
      'execution-page': {
        basic: {
          executionId: ${execution.id},
          automated: ${automated},
          hasBugtracker: ${execution.project.bugtrackerConnected},
          stepstable: {
            colDefs: ${stepsAoColumnDefs},
            cufDefs: ${json:serializeCustomfields(stepsCufDefinitions)}
          },
          cufs: {
            normal: ${json:marshall(executionCufValues)},
            denoCufs: ${json:marshall(executionDenormalizedValues)}
          },
          statuses: ${json:serialize(statuses)}
        },
        permissions: {
          editable: ${editable}
        },
        urls: {
          attachmentsURL: "${attachmentsUrl}"
        }
      }
    }
  });

  require(["common"], function () {
    require(['execution-page'], function () {
    });
  });
</script>
<%--  TODO ditch the require statement right above --%>


<%-- ============ /module configuration ==================== --%>

<%-- ===================== DOM ============================= --%>

<div
  class="ui-widget-header ui-state-default ui-corner-all fragment-header">

  <div style="float: left; height: 100%; width: 90%;">
    <h2>
      <a id="execution-name" href="${ executionUrl }"><f:message key="label.ExecuteDot"/> &#35;<c:out
        value="${executionRank} - ${ execution.name }" escapeXml="true"/>
      </a>
    </h2>
  </div>

  <div class="snap-right">
    <f:message var="back" key="label.Back"/>
    <input id="back" type="button" value="${ back }" class="sq-btn"/>
  </div>


  <div class="unsnap"></div>
</div>

<div class="fragment-body">

  <div id="execution-toolbar" class="toolbar-class ui-corner-all ">
    <div class="toolbar-information-panel">
      <comp:execution-information-panel auditableEntity="${execution}" entityUrl="${executionUrl}"/>
    </div>
    <div class="toolbar-button-panel">
      <c:if test="${ editable }">
        <comp:execution-execute-buttons execution="${ execution }"/>
        <input type="button" class="sq-btn"
               value='${deleteExecutionButton}'
               id="delete-execution-button"/>
      </c:if>
    </div>

    <c:if test="${ editable }">
      <comp:opened-object otherViewers="${ otherViewers }" objectUrl="${ executionUrl }"/>
    </c:if>

    <comp:milestone-messages milestoneConf="${milestoneConf}" nodeType="execution"/>

    <div class="unsnap"></div>
  </div>


  <%----------------------------------- Information -----------------------------------------------%>

  <comp:toggle-panel id="execution-information-panel"
                     titleKey="label.Description"
                     open="true">
		<jsp:attribute name="body">
		<div id="execution-information-table" class="display-table">
      <div class="display-table-row">
        <label class="display-table-cell" for="testcase-reference"><f:message key="test-case.reference.label"/></label>
        <div id="testcase-reference" class="display-table-cell">${ execution.reference }</div>
      </div>
      <div class="display-table-row">
        <label class="display-table-cell" for="testcase-description"><f:message key="label.Description"/></label>
        <div id="testcase-description" class="display-table-cell">${ execution.tcdescription }</div>
      </div>


      <div class="display-table-row">
        <label class="display-table-cell" for="testcase-status"><f:message key="test-case.status.label"/></label>
        <div class="display-table-cell">
          <span id="test-case-status-icon" class="test-case-status-${ execution.status }"> &nbsp &nbsp </span> <span
          id="test-case-status"><comp:level-message level="${ execution.status }"/></span>
        </div>
      </div>

      <c:if test="${execution.datasetLabel != null}">
          <div class="display-table-row">
            <label class="display-table-cell" for="testcase-dataset"><f:message key="label.dataset"/></label>
            <div class="display-table-cell">
                    <span>
      <f:message var="noneLabel" key="label.None"/>
      <c:out value="${(fn:length(execution.datasetLabel) == 0) ? noneLabel : execution.datasetLabel}"/>
    </span>
            </div>
          </div>
</c:if>

      <c:if test="${execution.automated}">
			<div class="display-table-row">
        <label class="display-table-cell" for="automated-script"><f:message
          key="test-case.testautomation.section.label"/></label>
        <div class="display-table-cell"
             id="automated-script">${ taDisassociated ? taDisassociatedLabel : execution.automatedExecutionExtender.automatedTest.fullLabel }</div>
      </div>
</c:if>
    </div>
	     </jsp:attribute>
  </comp:toggle-panel>


  <script type="text/javascript">
    publish('reload.executions.toolbar');
  </script>

  <%----------------------------------- Attribute -----------------------------------------------%>

  <comp:toggle-panel id="test-case-attribut-panel"
                     titleKey="label.Attributes"
                     open="true">

	<jsp:attribute name="body">
	<div id="test-case-attribut-table" class="display-table">
    <div class="display-table-row">
      <label for="test-case-importance" class="display-table-cell"><f:message
        key="test-case.importance.combo.label"/></label>
      <div class="display-table-cell">
        <span id="test-case-importance-icon" class="test-case-importance-${ execution.importance }">&nbsp&nbsp</span>
        <span id="test-case-importance"><comp:level-message level="${ execution.importance }"/></span>
      </div>
    </div>

    <div class="display-table-row">
      <label class="display-table-cell" for="test-case-nature"><f:message key="test-case.nature.label"/></label>
      <div class="display-table-cell">
        <span id="test-case-nature-icon" class="sq-icon sq-icon-${execution.nature.iconName}"></span>
        <span id="test-case-nature"><s:message code="${execution.nature.label}" text="${execution.nature.label}"
                                               htmlEscape="true"/></span>
      </div>
    </div>
    <div class="display-table-row">
      <label class="display-table-cell" for="test-case-type"><f:message key="test-case.type.label"/></label>
      <div class="display-table-cell">
        <span id="test-case-type-icon" class="sq-icon  sq-icon-${execution.type.iconName}"></span>
        <span id="test-case-type"><s:message code="${execution.type.label}" text="${execution.type.label}"
                                             htmlEscape="true"/></span>
      </div>
    </div>
  </div>

	</jsp:attribute>
  </comp:toggle-panel>

  <%----------------------------------- Prerequisites -----------------------------------------------%>

  <comp:toggle-panel id="execution-prerequisite-panel"
                     titleKey="generics.prerequisite.title"
                     open="${ not empty execution.prerequisite }">
		<jsp:attribute name="body">
		<div id="execution-prerequisite-table" class="display-table">
      <div class="display-table-row">
        <div class="display-table-cell">${ execution.prerequisite }</div>
      </div>
    </div>
	</jsp:attribute>
  </comp:toggle-panel>


  <%----------------------------------- result summary -----------------------------------------------%>

  <c:if test="${execution.automated}">
    <comp:toggle-panel id="auto-execution-result-summary-panel"
                       titleKey="label.resultSummary"
                       open="${ not empty execution.resultSummary }">
		<jsp:attribute name="body">
			<span>${execution.resultSummary}</span>
		</jsp:attribute>
    </comp:toggle-panel>
  </c:if>

  <%---------------------------- execution step summary status --------------------------------------%>

  <comp:toggle-panel id="execution-steps-panel"
                     titleKey="executions.execution-steps-summary.panel.title"
                     open="true">
		<jsp:attribute name="body">
		<table id="execution-execution-steps-table" class="unstyled-table">
      <thead>
      <tr>
        <th>Id(masked)</th>
        <th><f:message
          key="executions.steps.table.column-header.rank.label"/>
        </th>
        <c:forEach var="label" items="${stepsDfvsLabels}">
						<th>${label}</th>
					</c:forEach>
        <th><f:message
          key="executions.steps.table.column-header.action.label"/>
        </th>
        <th><f:message
          key="executions.steps.table.column-header.expected-result.label"/>
        </th>
        <th><f:message
          key="executions.steps.table.column-header.status.label"/>
        </th>
        <th><f:message
          key="executions.steps.table.column-header.last-execution.label"/>
        </th>
        <th><f:message
          key="executions.steps.table.column-header.user.label"/>
        </th>
        <th><f:message
          key="executions.steps.table.column-header.comment.label"/>
        </th>
        <th>bug list (masked)</th>
        <th><f:message
          key="executions.steps.table.column-header.bugged.label"/>
        </th>
        <th>numberOfAttch(masked)</th>
        <th><f:message
          key="executions.steps.table.column-header.attachment.label"/>
        </th>
        <th><f:message key="label.short.execute"/></th>
      </tr>
      </thead>
      <tbody>
        <%-- Will be populated through ajax --%>
      </tbody>
    </table>
		<br/>
	</jsp:attribute>
  </comp:toggle-panel>

  <script type="text/javascript">
    publish('reload.executions.stepstable');
  </script>

  <%-------------------------------------- Comment --------------------------------------------------%>

  <!-- XXX the attributes here should be 'comment' and not 'description' -->
  <c:if test="${ editable }">
    <c:set var="descrRicheditAttributes" value="class='editable rich-editable' data-def='url=${executionUrl}'"/>
  </c:if>
  <f:message var="executionComment" key="execution.description.panel.title"/>
  <comp:toggle-panel id="execution-description-panel" title="${executionComment}" open="false">
		<jsp:attribute name="body">
		<div id="execution-description" ${descrRicheditAttributes} >${ execution.description }</div>
	</jsp:attribute>
  </comp:toggle-panel>

  <%------------------------------ Attachments bloc ---------------------------------------------%>

  <at:attachment-bloc editable="${ editable }"
                      attachListId="${execution.attachmentList.id}"
                      attachmentSet="${attachmentSet}"
                      autoJsInit="false"/>

  <script type="text/javascript">
    publish('reload.executions.attachments');
  </script>

  <%-- ----------------------- bugtracker (if present)----------------------------------------%>
  <c:if test="${execution.project.bugtrackerConnected}">
    <issues:butracker-panel entity="${execution}" issueDetector="true"/>

    <script type="text/javascript">
      publish('reload.executions.bugtracker');
    </script>
  </c:if>

  <%-- ----------------------- /bugtracker (if present)----------------------------------------%>


  <%------------------------------ /bugs section -------------------------------%>
  <%--------------------------- Deletion confirmation popup -------------------------------------%>

  <f:message var="deletionDialogTitle" key="dialog.delete-execution.title"/>
  <div id="delete-execution-dialog" class="popup-dialog not-displayed" title="${deletionDialogTitle}">

    <span style="font-weight:bold;"><f:message key="dialog.delete-execution.message"/></span>

    <div class="popup-dialog-buttonpane">
      <input type="button" value="${confirmLabel}" data-def="evt=confirm, mainbtn"/>
      <input type="button" value="${cancelLabel}" data-def="evt=cancel"/>
    </div>
  </div>

  <script type="text/javascript">
    publish('reload.executions.dialogs');
  </script>
  <%--------------------------- /Deletion confirmation popup -------------------------------------%>

  <script type="text/javascript">
    publish('reload.executions.complete');
  </script>


</div>

