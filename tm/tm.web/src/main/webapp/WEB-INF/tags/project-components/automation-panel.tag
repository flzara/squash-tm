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
<%@ tag description="test automation panel (project level)" body-content="empty"%>

<%@ attribute name="project" type="java.lang.Object" required="true" description="the TM Project"%>
<%@ attribute name="availableTAServers" type="java.util.Collection" required="true"
  description="the list of the available TA servers"%>

<%@ tag language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="json" uri="http://org.squashtest.tm/taglib/json"%>
<%@ taglib prefix="pc" tagdir="/WEB-INF/tags/project-components"%>

<f:message var="confirmLabel" key="label.Confirm" />
<f:message var="cancelLabel" key="label.Cancel" />
<f:message var="closeLabel" key="label.Close" />
<f:message var="okLabel" key="label.Ok" />
<f:message var="noServerLabel" key="label.NoServer" />
<f:message var="active" key="label.active" />
<f:message var="inactive" key="label.inactive" />

<c:url var="listRemoteProjectsURL" value="/test-automation/servers/projects-list" />

<s:url var="projectUrl" value="/generic-projects/{projectId}">
  <s:param name="projectId" value="${project.id}" />
</s:url>

<s:url var="localProjectsURL" value="/generic-projects/{projectId}/test-automation-projects">
  <s:param name="projectId" value="${project.id}" />
</s:url>


<c:set var="inputSize" value="50" />

<c:choose>
  <c:when test="${chosenAutomationWorkflow == 'REMOTE_WORKFLOW'}">
    <c:set var="chosenAutomationWorkflow" value="REMOTE_WORKFLOW"/>
  </c:when>
  <c:when test="${! availableAutomationWorkflows.containsKey(chosenAutomationWorkflow)}">
    <c:set var="chosenAutomationWorkflow" value="NATIVE"/>
  </c:when>
  <c:otherwise/>
</c:choose>

<c:set var="chosenBddImplTechnology" value="${chosenBddImplTechnology}" />
<c:set var="chosenBddScriptLanguage" value="${chosenBddScriptLanguage}" />
<c:set var="currentAutomatedSuitesLifetime" value="${automatedSuitesLifetime}" />

<comp:toggle-panel id="test-automation-management-panel" titleKey="project.testauto.panel.title" open="true">

  <jsp:attribute name="body">

    <!-- BDD Script Technology -->
    <div id="project-bdd-implementation-technology" class="display-table">
      <div class="display-table-row">
        <div class="display-table-cell">
          <label class="display-table-cell">
            <f:message key="label.project.bdd.implementation-technology" />
          </label>
        </div>
        <div class="display-table-cell">
          <div id="project-bdd-implementation-technology-select" style="display: inline;">
            <c:out value="${availableBddImplTechnologies[chosenBddImplTechnology]}" />
          </div>
        </div>
      </div>
    </div>
    <!-- BDD Script Language -->
    <div id="project-bdd-script-language" class="display-table">
      <div class="display-table-row">
        <div class="display-table-cell">
          <label class="display-table-cell">
            <f:message key="label.project.bdd.script-language" />
          </label>
        </div>
        <div id="project-bdd-script-language-select-container" class="display-table-cell">
          <div id="project-bdd-script-language-select" style="display: inline;">
            <c:out value="${availableBddScriptLanguages[chosenBddScriptLanguage]}" />
          </div>
        </div>
      </div>
    </div>

    <br/>

    <!-- Automated Suites Lifetime -->
    <div id="project-automated-suites-lifetime" class="display-table">
      <div class="display-table-row">
        <div class="display-table-cell">
          <label class="display-table-cell">
            <f:message key="label.project.automated-suites-lifetime" />
          </label>
        </div>
        <div id="project-automated-suites-lifetime-editable-container" class="display-table-cell">
          <span id="automated-suites-lifetime"><c:out value="${currentAutomatedSuitesLifetime}"/></span>
        </div>
      </div>
    </div>

    <br/>

    <!-- Automation Workflow Menu -->
    <div id="project-workflow-option-table" class="display-table">
    <div class="display-table-row">
      <div class="display-table-cell">
        <label class="display-table-cell">
          <f:message key="label.workflow.modification" />
        </label>
      </div>
      <div id="project-workflows-select-container" class="display-table-cell">
        <div id ="project-workflows-select" style="display: inline">
          <c:out value="${availableAutomationWorkflows[chosenAutomationWorkflow]}" />
        </div>
      </div>
    </div>
  </div>

  <div class="ta-main-div">

      	<%-- =================================== server block =============================================================== --%>

      <fieldset class="ta-server-block ta-block">
        <legend>
          <f:message key="label.executionServer" />
        </legend>

		  <c:set var="serverName" value="${(not empty project.testAutomationServer) ? project.testAutomationServer.name : noServerLabel }" />
        <div id="selected-ta-server-span" class="std-margin-top std-margin-bottom"><c:out value="${serverName}" escapeXml="true" /></div>

      </fieldset>
      <%-- =================================== /server block =============================================================== --%>


      <%-- =================================== projects block =============================================================== --%>

      <f:message var="addTAProjectLabel" key="title.associateJob"  />
      <c:if test="${ empty project.testAutomationServer || project.testAutomationServer.kind != 'jenkins'}">
      <c:set var="dispayedJobBlock" value="display: none"/>
      </c:if>



      <fieldset class="ta-projects-block  ta-block" style="${dispayedJobBlock}">
        <legend>
          <f:message key="label.jobs" />
          <button id="ta-projects-bind-button" title="${addTAProjectLabel}" class="sq-icon-btn btn-sm">
            <span class="ui-icon ui-icon-plus squared-icons"></span>
          </button>
        </legend>


        <table id="ta-projects-table" class="ta-projects-table"
          data-def="ajaxsource=${localProjectsURL}, hover, deferloading=${fn:length(project.testAutomationProjects)}">
          <thead>
            <tr>
              <th data-def="map=entity-id, invisible" class="not-displayed">#</th>
               <th data-def="map=slaves, invisible" class="not-displayed">#</th>
              <th data-def="map=entity-index,narrow, select">#</th>
              <th data-def="map=label">
                <f:message key="label.Label" />
              </th>
               <th data-def="map=jobName">
                <f:message key="label.testAutomationProject.jobName" />
              </th>
              <th data-def="map=url, link-new-tab={url}">
               <f:message key="label.testAutomationProject.url" />
              </th>
              <th data-def="map=gherkin, sWidth=8em, render-yesno">
              	<f:message key="label.canRunGherkin"/>
              </th>
              <th data-def="map=empty-edit-holder, narrow, sClass=edit-job-button">&nbsp;</th>
              <th data-def="map=empty-delete-holder, unbind-button=#ta-projects-unbind-popup">&nbsp;</th>
            </tr>
          </thead>
          <tbody>
            <c:forEach items="${project.testAutomationProjects}" var="taproj" varStatus="status">
            <tr>
              <td>${taproj.id}</td>
              <td>${taproj.slaves}</td>
              <td>${status.index +1}</td>
              <td><c:out value="${taproj.label}" escapeXml="true" /></td>
              <td><c:out value="${taproj.jobName}" escapeXml="true" /></td>
              <td>${jobUrls[taproj.jobName]}</td>
              <td>${taproj.canRunGherkin}</td>
              <td> </td>
              <td> </td>
            </tr>
            </c:forEach>
          </tbody>
        </table>
        <br />
      </fieldset>
      <%-- =================================== /projects block =============================================================== --%>

    </div>

    <%-- =============== Source Code Management =============== --%>

    <div id="scm-panel-container" class="${allowAutomationWorkflow ? '' : 'not-displayed'}">
		<pc:scm-panel project="${adminproject.project}" availableScmServers="${availableScmServers}" />
    </div>

    <%-- =============== /Source Code Management =============== --%>

  </jsp:attribute>
</comp:toggle-panel>
<%-- ==================================================================== POPUPS =============================================================== --%>


<%-- ================================================

	Change TA server confirmation popup


 ================================================= --%>


<div id="ta-server-confirm-popup" class="popup-dialog not-displayed" title="${confirmLabel}">

  <!-- _____________CASE 1_______________ -->
  <div data-def="state=case1">
    <p>
      <f:message key="message.testAutomationBinding.removeJobs" />
    </p>
    <p>
      <label><f:message key="label.warning" /></label>
      <f:message key="message.testAutomationServer.noExecution.warning" />
    </p>
    <p>
      <f:message key="message.testAutomationServer.change.confirm" />
    </p>
  </div>
  <!-- _____________CASE 2_______________ -->
  <div data-def="state=case2">
    <p>
      <f:message key="message.testAutomationBinding.removeJobs" />
    </p>
    <p>
      <label><f:message key="label.warning" /></label>
      <f:message key="message.testAutomationServer.withExecution.warning" />
    </p>
    <p>
      <f:message key="message.testAutomationServer.change.confirm" />
    </p>
  </div>
  <!-- _____________Progression_______________ -->
  <div data-def="state=pleasewait">
    <comp:waiting-pane />
  </div>

  <!-- _____________Buttons_______________ -->
  <div class="popup-dialog-buttonpane">
    <input class="confirm" type="button" value="${confirmLabel}" data-def="evt=confirm,  state=case1, mainbtn=case1" />
    <input class="confirm" type="button" value="${confirmLabel}" data-def="evt=confirm,  state=case2, mainbtn=case2" />
    <input class="cancel" type="button" value="${cancelLabel}" data-def="evt=cancel" />
  </div>
</div>

<%-- ================================================
	Automation Server Authentication Popup
================================================ --%>

<f:message var="testAutomationServerAuthentication" key="title.testAutomationServer.authentication" />
<div id="add-ta-projects-login-dialog" data-def="width=300" class="popup-dialog not-displayed" title="${testAutomationServerAuthentication}" >

  <div data-def="error-pane">
    <span class="error-message"></span>
  </div>

  <div>
    <div class="centered">
      <div class="display-table">
        <div class="display-table-row">
          <div class="display-table-cell"><label><f:message key="label.Login" /></label></div>
          <div class="display-table-cell"><input type="text" id="login-dialog-login" /></div>
        </div>
        <div class="display-table-row">
          <div class="display-table-cell"><label><f:message key="label.password" /></label></div>
          <div class="display-table-cell"><input type="password"  id="login-dialog-password"/></div>
        </div>
      </div>
    </div>
  </div>

  <div class="popup-dialog-buttonpane">
    <input type="button" value="${confirmLabel}"  data-def="evt=confirm, mainbtn"/>
    <input type="button" value="${cancelLabel}" data-def="evt=cancel"/>
  </div>
</div>

<%-- ================================================
	Add Project Popup.
================================================= --%>


<f:message var="bindProjectPopup" key="title.associateJob" />
<div id="ta-projects-bind-popup" title="${bindProjectPopup}" class="popup-dialog not-displayed">

  <div data-def="state=pleasewait">
    <comp:waiting-pane />
  </div>

  <div data-def="state=main" class="ta-projects-bind-maindiv">
    <p>
      <label>
        <f:message key="message.selectJobs" />
      </label>
    </p>
    <table class="ta-project-bind-listdiv">
    </table>

  </div>

  <div data-def="state=noTAProjectAvailable">
    <p>
      <f:message key="message.project.bindJob.noJobToBind" />
    </p>
  </div>

  <f:message var="popupTMLabelLabel" key="label.taProjectTmLabel"/>
  <f:message var="popupTFLabelLabel" key="label.testAutomationProject.jobName"/>
  <f:message var="canRunScriptLabel" key="label.canRunGherkin"/>

  <script id="default-item-tpl" type="text/x-handlebars-template" th:inline="text">
    <tr class="listdiv-item-head">
      <td></td>
      <td><label class="afterDisabled">${popupTFLabelLabel}</label></td>
      <td><label class="afterDisabled">${popupTMLabelLabel}</label></td>
      <td style="text-align: center;"><label class="afterDisabled">${canRunScriptLabel}</label></td>
    </tr>
    {{#each this}}
    <tr class="listdiv-item control-group">
      <td>
        <input type="checkbox" name="select" value="{{this.name}}"/>
      </td>
      <td>{{this.name}}</td>
      <td class="ta-project-tm-label controls">
        <input id="add-job-label-{{this.name}}" name="tm-label" type="text" style="display: none;" value="{{this.name}}"/>
        <span class="help-inline">&nbsp;</span>
      </td>
      <td style="text-align: center;">
        <input type="checkbox" style="display: none" name="canExecuteScripts">
      </td>
    </tr>
    {{/each}}
  </script>

  <div class="ta-projectsadd-error">
    <span> </span>
  </div>


  <div class="popup-dialog-buttonpane">
    <input type="button" value="${cancelLabel}" data-def="mainbtn=main, evt=cancel, state=pleasewait" />
    <input type="button" value="${confirmLabel}" data-def="mainbtn=main, evt=confirm, state=main" />
    <input type="button" value="${cancelLabel}" data-def="evt=cancel, state=main" />
    <input type="button" value="${closeLabel}" data-def="mainbtn=noTAProjectAvailable, evt=cancel, state=noTAProjectAvailable" />
  </div>

</div>

<%-- ================================================
  Project edit popup.
================================================= --%>
<f:message var="editTAProjectTitle" key="title.editTAProject" />
<div id="ta-project-edit-popup" class="not-displayed popup-dialog form-horizontal" title="${editTAProjectTitle }">
  <div data-def="state=pleasewait">
    <comp:waiting-pane />
  </div>

  <div data-def="state=main">
    <div class="display-table-row">
      <label for="ta-project-label">
        <f:message key="label.Label" />
      </label>
      <div class="display-table-cell">
        <input id="ta-project-label" name="label" class="strprop" value="" maxlength="255" type="text" />
        <span class="help-inline">&nbsp;</span>
      </div>
    </div>

    <div class="display-table-row">
      <label for="ta-project-jobName">
        <f:message key="label.job.input" />
      </label>
      <div class="display-table-cell">
        <select id="ta-project-jobName" name="jobName"></select>
      </div>
    </div>

    <div class="display-table-row">
      <label for="ta-project-cangherkin">
        <f:message key="label.canRunGherkin" />
      </label>
      <div class="display-table-cell">
        <input type="checkbox" id="ta-project-cangherkin" name="canRunGherkin"/>
      </div>
    </div>

    <div class="control-group">
      <label for="slaves">
        <f:message key="label.slaves.input" />
      </label>
      <div>
        <input id="ta-project-slaves" name="slaves" class="strprop" value="" size="50" maxlength="255" type="text" />
        <span class="help-inline">&nbsp;</span>
      </div>
    </div>
  </div>
  <div class="ta-projectsedit-error">
    <span> </span>
  </div>
  <div class="popup-dialog-buttonpane">
    <input type="button" value="${cancelLabel}" data-def="mainbtn=main, evt=cancel, state=pleasewait" />
    <input type="button" value="${confirmLabel}" data-def="mainbtn=main, evt=confirm, state=main" />
    <input type="button" value="${cancelLabel}" data-def="evt=cancel, state=main" />
  </div>

</div>

<%-- =======================the project unbind confirmation popup================================ --%>

<f:message var="unbindPopupTitle" key="dialog.unbind-ta-project.tooltip" />
<div id="ta-projects-unbind-popup" class="popup-dialog not-displayed" title="${unbindPopupTitle}">
  <!-- _____________CASE 1_______________ -->
  <div data-def="state=case1">
    <p class="remove-message">fbfbfbd</p>
  </div>

  <!-- _____________CASE 2_______________ -->
  <div data-def="state=case2">
    <p class="remove-message">fbfbfbd</p>
    <p>
      <label>
        <f:message key="label.warning" />
      </label>
      <f:message key="message.testAutomationServer.withExecution.warning" />
    </p>

  </div>
  <!-- _____________Progression_______________ -->
  <div data-def="state=pleasewait">
    <comp:waiting-pane />
  </div>
  <script id="remove-message-tpl-case1" type="text/x-handlebars-template">
  <f:message key="message.testAutomationBinding.removeJob" />
  </script>
  <script id="remove-message-tpl-case2" type="text/x-handlebars-template">
  <f:message key="message.testAutomationBinding.removeExecutedJob" />
  </script>
  <!-- _____________Buttons_______________ -->
  <div class="popup-dialog-buttonpane">
    <input class="confirm" type="button" value="${confirmLabel}" data-def="evt=confirm,  state=case1, mainbtn=case1" />
    <input class="confirm" type="button" value="${confirmLabel}" data-def="evt=confirm,  state=case2, mainbtn=case2" />
    <input class="cancel" type="button" value="${cancelLabel}" data-def="evt=cancel" />
  </div>
</div>

<%-- ================================================
  Automation Workflow explanation popup.
================================================= --%>
<f:message var="automationPopupTitle" key="dialog.project.automation-workflow.title" />
<div id="automation-workflow-popup" class="popup-dialog not-displayed" title="${automationPopupTitle}">
  <div>
  	  <div><f:message key="message.project.automation.workflow.active.explanation" /></div>
    </div>
  <!-- _____________Buttons_______________ -->
  <div class="popup-dialog-buttonpane">
    <input class="confirm" type="button" value="${confirmLabel}" data-def="evt=confirm" />
    <input class="cancel" type="button" value="${cancelLabel}" data-def="evt=cancel" />
  </div>
</div>

<%-- ================================================
  disabled the plugin popup.
================================================= --%>
<div id="disabled-plugin-wa" title="<f:message key="label.disabled.plugin"/>" class="not-displayed">
	<div id="detail">
		<span style="font-weight:normal"><f:message key="message.disabled.plugin" /></span>
		<p>
			<input type="checkbox" id="save-conf" checked />
			<span style="font-weight:normal"><f:message key="message.save.conf" /></span>
		</p>
		<span style="font-weight:normal; white-space: pre-line"><f:message key="message.disabled.plugins" /></span>
		<p>
      		<span style="font-weight:bold"><f:message key="message.disabled.plugin.confirm" /> </span>
    	</p>
	</div>
    <div class="popup-dialog-buttonpane">
        <input class="confirm" type="button" value="${confirmLabel}" data-def="evt=confirm" />
        <input class="cancel"  type="button" value="${cancelLabel}" data-def="evt=cancel" />
    </div>
</div>

<%-- ===================================
	Js initialization
==================================== --%>
<f:message var="duplicateTMLabel" key="message.project.bindJob.duplicatelabels" />
<f:message var="checkOneJob" key="message.project.bindJob.noneChecked" />
<script type="text/javascript">
require(["common"], function() {
  require(["jquery", "projects-manager/project-info/automation-panel", "squashtable"], function($, automationBlock){
      squashtm = squashtm ? squashtm : {};
      squashtm.app = squashtm.app ? squashtm.app : {};
      squashtm.app.messages = squashtm.app.messages ? squashtm.app.messages : {};
      squashtm.app.messages["message.project.bindJob.duplicatelabels"] = "${duplicateTMLabel}";
      squashtm.app.messages["message.project.bindJob.noneChecked"] = "${checkOneJob}";
      $(function(){
        var chosenAutomationWorkflow = "${chosenAutomationWorkflow}";
        var availableAutomationWorkflows = "${availableAutomationWorkflows}";
        if (availableAutomationWorkflows.indexOf(chosenAutomationWorkflow) === -1 && chosenAutomationWorkflow !== "REMOTE_WORKFLOW") {
            chosenAutomationWorkflow = "NATIVE";
        }

        var automationSettings = {
          projectId: ${project.id},
        	isAdmin: ${isAdmin},
          tmProjectURL : "${projectUrl}",
          availableServers: ${json:serialize(availableTAServers)},
          TAServerId : ${(empty project.testAutomationServer) ? 0 : project.testAutomationServer.id},
          pluginAutomHasConf:"${pluginAutomHasConf}",
          availableAutomationWorkflows: ${json:serialize(availableAutomationWorkflows)},
          chosenAutomationWorkflow: chosenAutomationWorkflow,
          availableBddImplTechnologies: ${json:serialize(availableBddImplTechnologies)},
          chosenBddImplTechnology: "${chosenBddImplTechnology}",
          availableBddScriptLanguages: ${json:serialize(availableBddScriptLanguages)},
          chosenBddScriptLanguage: "${chosenBddScriptLanguage}"
        };

        automationBlock.init(automationSettings);
      });

  });
});












</script>
