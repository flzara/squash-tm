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
<%@ taglib prefix="aggr" tagdir="/WEB-INF/tags/aggregates" %>
<%@ taglib prefix="authz" tagdir="/WEB-INF/tags/authz" %>
<%@ taglib prefix="at" tagdir="/WEB-INF/tags/attachments" %>
<%@ taglib prefix="it" tagdir="/WEB-INF/tags/iterations-components" %>
<%@ taglib prefix="csst" uri="http://org.squashtest.tm/taglib/css-transform" %>
<%@ taglib prefix="dashboard" tagdir="/WEB-INF/tags/dashboard" %>
<%@ taglib prefix="issues" tagdir="/WEB-INF/tags/issues" %>

<f:message var="squashlocale" key="squashtm.locale"/>

<comp:datepicker-manager locale="${squashlocale}"/>

<s:url var="iterationUrl" value="/iterations/{iterId}">
  <s:param name="iterId" value="${iteration.id}"/>
</s:url>
<s:url var="iterationPlanningUrl" value="/iterations/{iterId}/planning">
  <s:param name="iterId" value="${iteration.id}"/>
</s:url>
<s:url var="iterationDashboardStatisticsUrl" value="/iterations/{iterId}/dashboard-statistics">
  <s:param name="iterId" value="${iteration.id}"/>
</s:url>

<c:url var="iterationStatisticsPrintUrl" value="/iterations/${iteration.id}/dashboard"/>

<s:url var="btEntityUrl" value="/bugtracker/iteration/{id}">
  <s:param name="id" value="${iteration.id}"/>
</s:url>

<s:url var="customFieldsValuesURL" value="/custom-fields/values">
  <s:param name="boundEntityId" value="${iteration.boundEntityId}"/>
  <s:param name="boundEntityType" value="${iteration.boundEntityType}"/>
</s:url>

<f:message var='deleteMessageStart' key='dialog.label.delete-node.label.start'/>
<f:message var="deleteMessage" key="dialog.label.delete-nodes.iteration.label"/>
<f:message var='deleteMessageCantBeUndone' key='dialog.label.delete-node.label.cantbeundone'/>
<f:message var='deleteMessageConfirm' key='dialog.label.delete-node.label.confirm'/>
<f:message var="labelConfirm" key="label.Confirm"/>
<f:message var="labelCancel" key="label.Cancel"/>

<%-- ----------------------------------- Authorization ----------------------------------------------%>

<%-- should be programmatically stuffed into page context --%>

<c:set var="writable" value="${false}"/>
<c:set var="moreThanReadOnly" value="${false}"/>
<c:set var="attachable" value="${false}"/>
<c:set var="linkable" value="${false}"/>
<c:set var="executable" value="${false}"/>
<c:set var="deletable" value="${false}"/>
<c:set var="extendedDeletable" value="${false}"/>


<c:if test="${not milestoneConf.locked}">

  <authz:authorized hasRole="ROLE_ADMIN" hasPermission="CREATE" domainObject="${ iteration }">
    <c:set var="moreThanReadOnly" value="${ true }"/>
  </authz:authorized>
  <authz:authorized hasRole="ROLE_ADMIN" hasPermission="WRITE" domainObject="${ iteration }">
    <c:set var="writable" value="${ true }"/>
  </authz:authorized>
  <authz:authorized hasRole="ROLE_ADMIN" hasPermission="ATTACH" domainObject="${ iteration }">
    <c:set var="attachable" value="${ true }"/>
  </authz:authorized>
  <authz:authorized hasRole="ROLE_ADMIN" hasPermission="DELETE" domainObject="${ iteration }">
    <c:set var="deletable" value="${true}"/>
  </authz:authorized>
  <authz:authorized hasRole="ROLE_ADMIN" hasPermission="EXTENDED_DELETE" domainObject="${ iteration }">
    <c:set var="extendedDeletable" value="${true}"/>
  </authz:authorized>
  <authz:authorized hasRole="ROLE_ADMIN" hasPermission="LINK" domainObject="${ iteration }">
    <c:set var="linkable" value="${ true }"/>
  </authz:authorized>
  <authz:authorized hasRole="ROLE_ADMIN" hasPermission="EXECUTE" domainObject="${ iteration }">
    <c:set var="executable" value="${ true }"/>
  </authz:authorized>

  <c:set var="moreThanReadOnly"
         value="${moreThanReadOnly or writable or attachable or deletable or extendedDeletable or linkable or executable}"/>

</c:if>

<%-- ----------------------------------- /Authorization ----------------------------------------------%>


<f:message key="tabs.label.issues" var="tabIssueLabel"/>
<script type="text/javascript">
  squashtm = squashtm || {};
  squashtm.page = squashtm.page || {};
  var config = squashtm.page;
  config.isFullPage = ${ not empty param.isInfoPage and param.isInfoPage };
  config.writable = ${not empty writable and writable};
  config.hasFields = ${ hasCUF };
  config.hasBugtracker = ${ iteration.project.bugtrackerConnected };
  config.identity = {resid: ${iteration.id}, restype: "iterations"};
  config.bugtracker = {url: "${btEntityUrl}", style: "fragment-tab"};
  config.customFields = {url: "${customFieldsValuesURL}"};
  config.iterationURL = "${iterationUrl}";
  config.iterationStatusComboJson = ${iterationStatusComboJson};
</script>

<div class="ui-widget-header ui-state-default ui-corner-all fragment-header ctx-title">
  <c:if test="${ not param.isInfoPage }">
    <div id="right-frame-button">
      <f:message var="toggleLibraryTooltip" key="tooltip.toggleLibraryDisplay"/>
      <input type="button" class="sq-btn btn-sm" id="toggle-expand-left-frame-button" title="${toggleLibraryTooltip}"/>
    </div>
  </c:if>

  <div class="small-margin-left">
    <h2>

      <a id="iteration-name" href="${ iterationUrl }/info">
        <c:out value="${ iteration.fullName }" escapeXml="true"/>
      </a>

      <%-- raw reference and name because we need to get the name and only the name for modification, and then re-compose the title with the reference  --%>
      <span id="iteration-raw-reference" style="display: none">
        <c:out value="${ iteration.reference }" escapeXml="true"/>
      </span>

      <span id="iteration-raw-name" style="display: none">
        <c:out value="${ iteration.name }" escapeXml="true"/>
      </span>

    </h2>
  </div>

</div>

<div id="iteration-toolbar" class="toolbar-class ui-corner-all cf">
  <div class="toolbar-information-panel">
    <div id="general-informations-panel">
      <comp:general-information-panel auditableEntity="${iteration}" entityUrl="${iterationUrl}"/>
    </div>
  </div>
  <div class="toolbar-button-panel btn-toolbar right">
    <c:if test="${ executable && iteration.project.testAutomationEnabled }">
      <comp:execute-auto-button url="${ iterationUrl }"/>

    </c:if>
    <c:if test="${ writable }">
      <input type="button" value=' <f:message key="iteration.test-plan.testsuite.manage.label"/>'
             title=' <f:message key="iteration.button.testsuite.tooltip"/>'
             id="manage-test-suites-button" class="sq-btn"/>
    </c:if>
    <c:if test="${ writable }">
      <input type="button" value='<f:message key="iteration.button.rename.label" />' id="rename-iteration-button"
             title="<f:message key="dialog.rename-iteration.title" />"
             class="sq-btn"/>
    </c:if>

  </div>

  <c:if test="${ moreThanReadOnly }">
    <comp:opened-object otherViewers="${ otherViewers }" objectUrl="${ iterationUrl }"/>
  </c:if>

  <comp:milestone-messages milestoneConf="${milestoneConf}" nodeType="iteration"/>

  <div class="unsnap"></div>
</div>

<csst:jq-tab activeContentIndex="2">
  <div class="fragment-tabs fragment-body">
    <ul class="tab-menu">
      <li>
        <a href="#dashboard-iteration" id="dashboard-tab-list-item">
          <f:message key="title.Dashboard"/>
        </a>
      </li>
      <li>
        <a href="#tabs-1">
          <f:message key="tabs.label.information"/>
        </a>
      </li>

      <li>
        <a href="#iteration-test-plans-panel">
          <f:message key="tabs.label.test-plan"/>
        </a>
      </li>
      <li>
        <a href="#tabs-3">
          <f:message key="label.Attachments"/>
          <c:if test="${ iteration.attachmentList.notEmpty }">
            <span class="hasAttach">!</span>
          </c:if>
        </a>
      </li>

      <c:if test="${iteration.project.bugtrackerConnected}">
        <li>
            <%-- div#bugtracker-section-main-div is declared in tagfile issues:bugtracker-panel.tag --%>
          <a href="#bugtracker-section-main-div"><f:message key="tabs.label.issues"/></a>
        </li>
      </c:if>

    </ul>
    <div id="tabs-1">

      <c:if test="${ writable }">
        <c:set var="descrRicheditAttributes" value="class='editable rich-editable' data-def='url=${iterationUrl}'"/>
      </c:if>
      <f:message var="labelDescription" key="label.Description"/>
      <comp:toggle-panel id="iteration-description-panel"
                         title='${labelDescription} <span class="small txt-discreet">[ID = ${ iteration.id }]</span>'
                         open="true">
        <jsp:attribute name="body">
              <div class="display-table-row">
                <label for="iteration-reference" class="display-table-cell"><f:message key="label.Reference"/></label>

                <div class="display-table-cell" id="iteration-reference">${ iteration.reference }</div>
              </div>

              <div class="display-table-row">
                <label for="iteration-description" class="display-table-cell"><f:message
                  key="label.Description"/></label>

                <div id="iteration-description" ${descrRicheditAttributes}>${ iteration.description }</div>
              </div>

            <div class="display-table-row">
              <label for="iteration-status" class="display-table-cell"><f:message
                key="iteration.status.combo.label"/></label>

              <div>
                <span id="iteration-status-icon" style="vertical-align:middle"
                      class="sq-icon iteration-status-${iteration.status}"> &nbsp &nbsp</span>
                <span id="iteration-status">${ iterationStatusLabel }</span>
              </div>
            </div>

		</jsp:attribute>
      </comp:toggle-panel>


        <%----------------------------------- Custom Fields -----------------------------------------------%>

      <comp:toggle-panel id="iteration-custom-fields" titleKey="generics.customfieldvalues.title" open="${hasCUF}">
        <jsp:attribute name="body">
				<div id="iteration-custom-fields-content">
          <c:if test="${hasCUF}">
            <comp:waiting-pane/>
          </c:if>
        </div>
			</jsp:attribute>
      </comp:toggle-panel>


        <%--------------------------- Planning section ------------------------------------%>

      <comp:toggle-panel id="datepicker-panel" titleKey="label.Planning" open="true">
        <jsp:attribute name="body">
			<div class="datepicker-panel">
        <table class="datepicker-table">
          <tr>
            <td class="datepicker-table-col">
              <div class="datepicker-col-scheduled">
                <comp:datepickers-pair/>
              </div>
            </td>
            <td class="datepicker-table-col">
              <div class="datepicker-col-actual">
                <comp:datepickers-auto-pair/>
              </div>
            </td>
          </tr>
        </table>
      </div>
			</jsp:attribute>
      </comp:toggle-panel>

    </div>

      <%-- ------------------ test plan ------------------------------ --%>

    <it:iteration-test-plan-panel
      iteration="${iteration}"
      assignableUsers="${assignableUsers}"
      weights="${weights}"
      modes="${modes}"
      statuses="${statuses}"
      linkable="${linkable}"
      editable="${writable}"
      executable="${executable}"
      reorderable="${linkable}"
      deletable="${deletable}"
      extendedDeletable="${extendedDeletable}"
      milestoneConf="${milestoneConf}"/>

      <%-- ------------------ /test plan ----------------------------- --%>

      <%------------------------------- Dashboard ---------------------------------------------------%>
    <div id="dashboard-iteration">
        <%-- statistics panel --%>
      <c:if test="${shouldShowDashboard}">
        <dashboard:favorite-dashboard/>
      </c:if>

      <c:if test="${not shouldShowDashboard}">
        <dashboard:iteration-dashboard-panel url="${iterationDashboardStatisticsUrl}"
                                             printUrl="${iterationStatisticsPrintUrl}" allowsSettled="${allowsSettled}"
                                             allowsUntestable="${allowsUntestable}"/>
      </c:if>

    </div>


      <%------------------------------ Attachments bloc ------------------------------------------- --%>

    <at:attachment-tab tabId="tabs-3" entity="${ iteration }" editable="${ attachable }"
                       tableModel="${attachmentsModel}"/>


      <%-------------------------------- Rename popup --------------------------------------------- --%>

    <c:if test="${ writable }">
      <f:message var="renameDialogTitle" key="dialog.rename-iteration.title"/>
      <div id="rename-iteration-dialog" title="${renameDialogTitle}" class="not-displayed popup-dialog">
        <div>
          <label>
            <f:message key="dialog.rename.label"/>
          </label>
          <input type="text" id="rename-iteration-name" maxlength="255" size="50"/>
          <br/>
          <comp:error-message forField="name"/>
        </div>

        <div class="popup-dialog-buttonpane">
          <input type="button" value="${labelConfirm}" data-def="evt=confirm, mainbtn"/>
          <input type="button" value="${labelCancel}" data-def="evt=cancel"/>
        </div>
      </div>

    </c:if>

      <%-- ----------------------------------- Test Suite Management -------------------------------------------------- --%>
    <c:if test="${ writable }">
      <it:test-suite-managment iteration="${iteration}"/>
    </c:if>
      <%-- ----------------------------------- /Test Suite Management -------------------------------------------------- --%>

      <%-- ----------------------- bugtracker (if present)----------------------------------------%>
    <c:if test="${iteration.project.bugtrackerConnected}">
      <issues:butracker-panel entity="${iteration}"/>
    </c:if>

      <%-- ----------------------- /bugtracker (if present)----------------------------------------%>


  </div>
</csst:jq-tab>
<%------------------------------------------automated suite overview --------------------------------------------%>
<c:if test="${ executable && iteration.project.testAutomationEnabled }">
  <comp:automated-suite-overview-popup/>
</c:if>
<%------------------------------------------/automated suite overview --------------------------------------------%>


<script type="text/javascript">
  publish("reload.iteration");
  if (!squashtm.page.isFullPage) {
    require(["common"], function () {
      require(["iteration-page"], function () {
        require(["datepicker/datepickers-pair", "datepicker/datepickers-auto-pair"], function (datePickers, datePickersAuto) {
          var conf = {
            data: {
              planningUrl: "${iterationPlanningUrl}",
              initialScheduledStartDate: "${iteration.scheduledStartDate.time}",
              initialScheduledEndDate: "${iteration.scheduledEndDate.time}",
              initialActualStartDate: "${iteration.actualStartDate.time}",
              initialActualEndDate: "${iteration.actualEndDate.time}",
              initialActualStartAuto: ${iteration.actualStartAuto},
              initialActualEndAuto: ${iteration.actualEndAuto}
            },
            features: {
              editable: ${writable}
            }
          };
          datePickers.init(conf);
          datePickersAuto.init(conf);
          //favorite dashboard
          squashtm.workspace.canShowFavoriteDashboard = ${canShowDashboard};
          squashtm.workspace.shouldShowFavoriteDashboard = ${shouldShowDashboard};
        });
      });
    });
  }
</script>


