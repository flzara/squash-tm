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
<%@ taglib prefix="authz" tagdir="/WEB-INF/tags/authz" %>
<%@ taglib prefix="at" tagdir="/WEB-INF/tags/attachments" %>
<%@ taglib prefix="tc" tagdir="/WEB-INF/tags/test-cases-components" %>
<%@ taglib prefix="issues" tagdir="/WEB-INF/tags/issues" %>
<%@ taglib prefix="csst" uri="http://org.squashtest.tm/taglib/css-transform" %>
<%@ taglib prefix="json" uri="http://org.squashtest.tm/taglib/json" %>

<?xml version="1.0" encoding="utf-8" ?>
<%@ page language="java" contentType="text/html; charset=utf-8"
         pageEncoding="utf-8" %>


<f:message key="tabs.label.issues" var="tabIssueLabel"/>

<%-- used for copy/paste of steps --%>
<script>
  require(["common"], function () {
    require(["jquery.cookie"]);
  })
</script>

<%------------------------------------- URLs ----------------------------------------------%>


<c:url var="testCaseUrl" value="/test-cases/${testCase.id}"/>
<c:url var="executionsTabUrl" value="/test-cases/${testCase.id}/executions?tab="/>
<c:url var="stepTabUrl" value="/test-cases/${testCase.id}/steps/panel"/>
<c:url var="importanceAutoUrl" value="/test-cases/${testCase.id}/importanceAuto"/>
<c:url var="customFieldsValuesURL" value="/custom-fields/values"/>
<c:url var="btEntityUrl" value="/bugtracker/test-case/${testCase.id}"/>
<c:url var="automationUrl" value="/test-cases/${testCase.id}/test-automation"/>

<%-- ----------------------------------- Authorization ----------------------------------------------%>
<%--
	if no variable 'editable' was provided in the context, we'll set one according to the authorization the user
	was granted for that object.

    Also if there is an active milestone that prevent modification,
    all permissions shall remain to false.
--%>


<c:set var="writable" value="${false}"/>
<c:set var="moreThanReadOnly" value="${false}"/>
<c:set var="attachable" value="${false}"/>
<c:set var="deletable" value="${false}"/>
<c:set var="linkable" value="${false}"/>

<%-- permission 'linkable' is not subject to the milestone statuses, ACL only --%>
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="LINK"
                  domainObject="${ testCase }">
  <c:set var="linkable" value="${ true }"/>
</authz:authorized>

<%-- other permissions. ACL should be evaluated only if the milestone statuses allows it.  --%>
<c:if test="${not milestoneConf.locked}">

  <authz:authorized hasRole="ROLE_ADMIN" hasPermission="WRITE"
                    domainObject="${ testCase }">
    <c:set var="writable" value="${ true }"/>
    <c:set var="moreThanReadOnly" value="${ true }"/>
  </authz:authorized>
  <authz:authorized hasRole="ROLE_ADMIN" hasPermission="ATTACH"
                    domainObject="${ testCase }">
    <c:set var="attachable" value="${ true }"/>
    <c:set var="moreThanReadOnly" value="${ true }"/>
  </authz:authorized>

  <authz:authorized hasRole="ROLE_ADMIN" hasPermission="DELETE"
                    domainObject="${ testCase }">
    <c:set var="moreThanReadOnly" value="${ true }"/>
  </authz:authorized>
  <authz:authorized hasRole="ROLE_ADMIN" hasPermission="CREATE"
                    domainObject="${ testCase }">
    <c:set var="creatable" value="${true }"/>
    <c:set var="moreThanReadOnly" value="${ true }"/>
  </authz:authorized>
  <c:set var="linkable" value="${false }"/>
  <authz:authorized hasRole="ROLE_ADMIN" hasPermission="LINK"
                    domainObject="${ testCase }">
    <c:set var="linkable" value="${ true }"/>
    <c:set var="moreThanReadOnly" value="${ true }"/>
  </authz:authorized>

</c:if>

<%-- ----------------------------------- Variables ----------------------------------------------%>

<c:set var="scripted" value="${testCase.isScripted()}"/>
<style type="text/css" media="screen">
  body {
    overflow: hidden;
  }

  .tc-script-editor {
    margin: 0;
    position: absolute;
    top: 35px;
    bottom: 0;
    right: 0;
  }

  .tc-script-editor-option-open {
    left: 300px;
  }

  .tc-script-editor-option-closed {
    left: 0;
  }

  .option-panel-wrapper {
    position: absolute;
    top: 35px;
    bottom: 0;
    left: 0;
    right: 0;
    width: 300px;
  }

  #optionsPanel {
    margin: 0;
    width: 100%;
    height: 80%;
    overflow-y: auto;
  }
</style>

<%---------------------------- Test Case Header ------------------------------%>

<tc:test-case-header testCase="${testCase}"/>


<%---------------------------- Test Case Informations ------------------------------%>

<tc:test-case-toolbar testCase="${testCase}" isInfoPage="${param.isInfoPage}" otherViewers="${otherViewers}"
                      moreThanReadOnly="${moreThanReadOnly}" writable="${writable}"
                      milestoneConf="${milestoneConf}"/>

<%-- --------------------------------------- Test Case body --------------------------------------- --%>

<csst:jq-tab activeContentIndex="1">
  <div class="fragment-tabs fragment-body">

      <%--  ------------------ main tab panel --------------------------------- --%>
    <ul class="tab-menu">
      <li>
        <a href="#tab-tc-informations"><f:message key="tabs.label.information"/></a>
      </li>
      <c:choose>
        <c:when test="${scripted}">
          <li>
            <a href="#tab-tc-script-editor"><f:message key="label.Script"/></a>
          </li>
        </c:when>
        <c:otherwise>
          <li>
            <a href="${stepTabUrl}"><f:message key="tabs.label.steps"/></a>
          </li>
          <li>
            <a href="${testCaseUrl}/parameters/panel"><f:message key="label.parameters"/></a>
          </li>
        </c:otherwise>
      </c:choose>
      <c:if test="${milestoneConf.displayTab}">
        <li>
          <a href="${testCaseUrl}/milestones/panel"><f:message key="tabs.label.milestone"/></a>
        </li>
      </c:if>
      <li>
        <a href="#tabs-tc-attachments"><f:message key="label.Attachments"/>
          <c:if test="${testCase.attachmentList.notEmpty}">
            <span class="hasAttach">!</span>
          </c:if>
        </a>
      </li>
      <li>
        <a href="${executionsTabUrl}"><f:message key="label.executions"/> </a>
      </li>
      <c:if test="${testCase.project.bugtrackerConnected}">
        <li>
            <%-- div#bugtracker-section-main-div is declared in tagfile issues:bugtracker-panel.tag --%>
          <a href="#bugtracker-section-main-div"><f:message key="tabs.label.issues"/></a>
        </li>
      </c:if>
    </ul>


    <div id="tab-tc-informations">

        <%-- ------------------------- Description Panel ------------------------- --%>

      <tc:test-case-description testCase="${testCase}"
                                testCaseImportanceLabel="${testCaseImportanceLabel}"
                                writable="${writable}"/>

      <tc:test-case-attribut testCase="${testCase}" writable="${writable}"
                             testCaseImportanceLabel="${testCaseImportanceLabel}"/>


        <%----------------------------------- Prerequisites -----------------------------------------------%>
      <c:if test="${!scripted}">
        <tc:test-case-prerequisites testCase="${testCase}"/>
      </c:if>
        <%--------------------------- Verified Requirements section ------------------------------------%>

      <tc:test-case-verified-requirement-bloc linkable="${ linkable }" testCase="${testCase}"
                                              containerId="contextual-content" milestoneConf="${milestoneConf}"/>


        <%--------------------------- calling test case section ------------------------------------%>

      <c:if test="${!scripted}">
        <tc:calling-test-cases-panel testCase="${testCase}"/>
      </c:if>

    </div>

      <%-- ------------------------- /Description Panel ------------------------- --%>

      <%------------------------------ Script Editor Only for scripted test case ---------------------------------------------%>
    <c:if test="${scripted}">


      <div id="tab-tc-script-editor">
          <%-- ==================== toolbar definition ===================--%>
        <div>

          <div class="left btn-toolbar">
            <span class="group">
              <button id="tc-script-save-button" title="Ctrl+Alt+S"
                      data-icon="ui-icon ui-icon-plusthick"
                      class="sq-btn ui-icon-plusthick" disabled='disabled'>
                    <f:message key="label.save"/>
              </button>
            </span>
          </div>
          <div class="right btn-toolbar">
            <span class="group">
              <button id="tc-script-toggle-option-panel"
                      data-icon="ui-icon-plusthick"
                      class="sq-btn">
                    <f:message key="label.Help"/>
              </button>
              <button id="tc-script-activate-editor"
                      data-icon="ui-icon-plusthick"
                      class="sq-btn">
                    <f:message key="label.Edit"/>
              </button>
              <button id="tc-script-cancel"
                      data-icon="ui-icon-plusthick"
                      class="sq-btn" style="display: none">
                    <f:message key="label.Cancel"/>
              </button>
            </span>
          </div>
        </div>
        <div class="option-panel-wrapper" style="display: none">
          <div id="optionsPanel"></div>
        </div>
        <div id="tc-script-editor" class="tc-script-editor tc-script-editor-option-closed"></div>
      </div>
    </c:if>

      <%------------------------------ /Script Editor  ---------------------------------------------%>

      <%------------------------------ Attachments  ---------------------------------------------%>

    <at:attachment-tab tabId="tabs-tc-attachments" entity="${ testCase }" editable="${ attachable }"
                       tableModel="${attachmentsModel}"/>

      <%------------------------------ /Attachments  ---------------------------------------------%>

      <%-- ----------------------- bugtracker (if present)----------------------------------------%>
    <c:if test="${testCase.project.bugtrackerConnected}">
      <issues:butracker-panel entity="${testCase}"/>
    </c:if>

      <%-- ----------------------- /bugtracker (if present)----------------------------------------%>

  </div>
</csst:jq-tab>

<%-- ===================================== popups =============================== --%>

<tc:test-case-popups writable="${writable}" milestoneConf="${milestoneConf}"/>

<%-- ===================================== /popups =============================== --%>


<%-- ===================================== INIT =============================== --%>

<script type="text/javascript" th:inline="javascript">
  /*<![CDATA[*/
  var squashtm = squashtm || {};
  squashtm.app = squashtm.app || {};
  require(["common"], function () {
    require(["jquery", "test-case-management", "workspace.event-bus"],
      function ($, testCaseManagement, eventBus) {
        var settings = {
          urls: {
            testCaseUrl: "${testCaseUrl}",
            importanceAutoUrl: "${importanceAutoUrl}",
            cufValuesUrl: "${customFieldsValuesURL}?boundEntityId=${testCase.boundEntityId}&boundEntityType=${testCase.boundEntityType}",
            bugtrackerUrl: "${btEntityUrl}",
            automationUrl: "${automationUrl}"
          },
          writable: ${writable},
          testCaseImportanceComboJson: ${testCaseImportanceComboJson},
          testCaseNatures: ${json:serialize(testCaseNatures)},
          testCaseTypes: ${json:serialize(testCaseTypes)},
          testCaseStatusComboJson: ${testCaseStatusComboJson},
          importanceAuto: ${testCase.importanceAuto},
          testCaseId: ${testCase.id},
          callingTestCases: ${json:serialize(callingTestCasesModel.aaData)},
          hasCufs: ${hasCUF},
          hasBugtracker: ${testCase.project.bugtrackerConnected},
          isAutomated: ${testCase.project.testAutomationEnabled},
          isScripted: ${scripted}
          <c:if test="${scripted}">
          , scriptExender: ${json:serialize(testCase.scriptedTestCaseExtender)}
          </c:if>
          <c:if test="${not empty milestoneConf.activeMilestone}">
          , milestone: ${json:serialize(milestoneConf.activeMilestone)}
          </c:if>
        };

        $(function () {
          testCaseManagement.initStructure(settings);
          testCaseManagement.initInfosTab(settings);
          if (settings.isScripted) {
            testCaseManagement.initScriptEditorTab(settings);
          }
        });

        <c:if test="${param.isInfoPage}">
        <c:url var="newversionUrl" value="/test-cases/{id}/info"/>
        eventBus.on('test-case.new-version', function (evt, version) {
          var url = "${newversionUrl}".replace('{id}', version.id);
          document.location.href = url;
        });
        </c:if>
      });
  });
  /*]]>*/
</script>

