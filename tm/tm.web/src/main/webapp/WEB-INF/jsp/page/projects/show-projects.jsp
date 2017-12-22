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
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ page import="org.squashtest.tm.tm.domain.project.*" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"  %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="pu" uri="http://org.squashtest.tm/taglib/project-utils" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>

<s:url var="administrationUrl" value="/administration" />
<s:url var="dtModel" value="/generic-projects"/>
<s:url var="dtLanguage"  value="/datatables/messages" />
<s:url var="projectsInfo" value="/administration/projects/{project-id}/info" />

<layout:info-page-layout titleKey="squashtm.project.title" isSubPaged="true" main="project-manager">
  <jsp:attribute  name="head">
    <comp:sq-css name="squash.grey.css" />
  </jsp:attribute>

  <jsp:attribute name="titlePane">
    <h2 class="admin"><f:message key="label.administration" /></h2>
  </jsp:attribute>
    <jsp:attribute name="subPageTitle">
    <h2><f:message key="workspace.project.title" /></h2>
  </jsp:attribute>

  <jsp:attribute name="subPageButtons">
    <f:message var="backButtonLabel" key="label.Back" />
    <input type="button" class="sq-btn" value="${backButtonLabel}" onClick="document.location.href= '${administrationUrl}'"/>
  </jsp:attribute>
  <jsp:attribute name="informationContent">
    <%----------------------------------- Projects Table -----------------------------------------------%>
<div class="fragment-body">
  <sec:authorize access=" hasRole('ROLE_ADMIN')">
   <div class="btn-toolbar right">
    <button id="add-template-button" role="buttonmenu" class="buttonmenu sq-btn buttonmenu-button ">
    <span class="ui-icon ui-icon-plusthick" >+</span>
    <span>
    <f:message key='label.addTemplate'/>
    </span>
    </button>
    <ul id="add-template-menu" class="not-displayed">
                  <li id="new-template-button" class="cursor-pointer">
                    <a>
                    <f:message key='label.createTemplate'/>
                    </a>
                  </li>
                  <li id="new-template-from-project-button" class="cursor-pointer disabled ui-state-disabled">
                    <a>
                    <f:message key='label.createTemplateFromProject'/>
                    </a>
                  </li>
	</ul>
   <button id="new-project-button" type="button" class="sq-btn"
     title="<f:message key='project.button.add.label' />" >
    <span class="ui-icon ui-icon-plusthick" >+</span><span class="ui-button-text"><f:message key='label.Add' /></span>
    </button>

  </div>
  </sec:authorize>
  <table id="projects-table" class="unstyled-table"
    data-def="ajaxsource=${dtModel}, hover, datakeys-id=project-id, deferLoading=${fn:length(projects)}, filter, pre-sort=2-desc">
    <thead>
      <tr>
        <th data-def="map=project-id,invisible">Id(not shown)</th>
        <th data-def="map=index, select, sClass=button-cell">#</th>
        <th data-def="map=name, sortable, link=${projectsInfo}" class="datatable-filterable"><f:message key="label.Name" /></th>
        <th data-def="map=raw-type, invisible">raw type (not shown)</th>
        <th data-def="map=type, sClass=icon-cell type" >&nbsp;</th>
        <th data-def="map=label, sortable" class="datatable-filterable"><f:message key="label.tag" /></th>
        <th data-def="map=active, invisible"><f:message key="label.active" /></th>
        <th data-def="map=created-on, sortable"><f:message key="label.CreatedOn" /></th>
        <th data-def="map=created-by, sortable" class="datatable-filterable"><f:message key="label.createdBy" /></th>
        <th data-def="map=last-mod-on, sortable"><f:message key="label.modifiedOn" /></th>
        <th data-def="map=last-mod-by, sortable" class="datatable-filterable"><f:message key="label.modifiedBy" /></th>
       <th data-def="map=habilitation, sortable" class="datatable-filterable"><f:message key="label.Permissions" /></th>
       <th data-def="map=bugtracker, sortable" class="datatable-filterable"><f:message key="label.Bugtracker" /></th>
      <th data-def="map=automation, sortable" class="datatable-filterable"><f:message key="label.TestAutomationServer" /></th>
      </tr>
    </thead>
    <tbody>
      <c:forEach var="project" items="${ projects }" varStatus="status">
      <tr>
        <td class="project-id">${ project.id }</td>
        <td class="button-cell select-handle centered">${ status.index + 1}</td>
        <td class="name">${ project.name }</td>
        <c:choose>
        <c:when test="${ pu:isTemplate(project) }">
        <td class="raw-type">template</td>
        <td class="type-template type" title="<f:message key='label.projectTemplate' />">&nbsp</td>
        </c:when>
        <c:otherwise>
        <td class="raw-type">project</td>
        <td class="icon-cell type-project" title="<f:message key='label.project' />">&nbsp</td>
        </c:otherwise>
        </c:choose>
        <td>${ project.label }</td>
        <td><f:message key="squashtm.yesno.${ project.active }" /></td>
        <td><comp:date value="${ project.createdOn }" /></td>
        <td><comp:user value="${ project.createdBy }" /></td>
        <td><comp:date value="${ project.lastModifiedOn }" /></td>
        <td><comp:user value="${ project.lastModifiedBy }" /></td>
      </tr>
      </c:forEach>
    </tbody>
  </table>

  <script type="text/javascript">
  squashtm.app.projectsManager = {
    deferLoading: ${ fn:length(projects) },
    tooltips: {
      template: "<f:message key='label.projectTemplate' />",
      project: "<f:message key='label.project' />"
    },
    messages: {
      info : "<f:message key='popup.title.info'/>",
      noProjectTemplateMessage : "<f:message key='message.noProjectTemplateSource'/>"
      }
  };
  publish("load.projectManager");
  </script>

  <sec:authorize access="hasRole('ROLE_ADMIN')">
<!--   ===========================CREATE FROM TEMPLATE DIALOG=======================================  -->
   <script id="add-project-from-template-dialog-tpl" type="text/x-handlebars-template">
  <div id="add-project-from-template-dialog" class="not-displayed popup-dialog form-horizontal" title="<f:message key='title.addProject' />">
    <table class="form-horizontal">
      <tr class="control-group">
        <td>
          <label class="control-label" for="add-project-from-template-name">
            <f:message key="label.Name" />
          </label>
        </td>
        <td class="controls">
          <input id="add-project-from-template-name" name="add-project-from-template-name" type="text" size="50" maxlength="255" data-prop="name" data-object="jsonProjectFromTemplate" data-def="maininput"/>
          <span class="help-inline">&nbsp;</span>
        </td>
      </tr>
         <tr class="control-group">
        <td>
          <label class="control-label" for="add-project-from-template-description"><f:message key="label.Description" /></label>
        </td>
        <td class="controls">
          <textarea id="add-project-from-template-description" name="add-project-from-template-description" data-def="isrich" data-prop="description"></textarea>
          <span class="help-inline">&nbsp;</span>
        </td>
      </tr>
      <tr class="control-group">
        <td>
          <label class="control-label" for="add-project-from-template-label"><f:message key="label.tag" /></label>
        </td>
        <td class="controls">
          <input id="add-project-from-template-label" name="add-project-from-template-label" data-prop="label"  type="text" size="50" maxlength="255" />
          <span class="help-inline">&nbsp;</span>
        </td>
      </tr>
      <!--       TEMPLATE COMBO -->
      <tr class="control-group">
        <td>
          <label class="control-label" for="add-project-from-template-tempate"><f:message key="label.projectTemplate" /></label>
        </td>
      	<td class="controls">
  			<select id="add-project-from-template-template" data-prop="templateId">
       	 		{{#each items}}
       	 			<option value="{{this.id}}">{{this.name}}</option>
        		{{/each}}
      		</select>
         </td>
      </tr>
      <tr class="control-group">
      <td>
          <label class="control-label" for="add-project-from-template-tempate"><f:message key="label.parametersFromTemplate" /></label>
      </td>
      <td>
      <!--        CHECKBOXES -->
        <input id="copyPermissions" name="copyPermissions" type="checkbox" data-prop="copyPermissions"/>
        <label class=" afterDisabled" for="copyPermissions"><f:message key="label.copyPermissions" /></label>
         <br/>
         <input id="copyCUF"  name="copyCUF" type="checkbox" data-prop="copyCUF"/>
         <label class=" afterDisabled" for="copyCUF"><f:message key="label.copyCUF" /></label>
         <br/>
          <input id="copyBugtrackerBinding" name="copyBugtrackerBinding" type="checkbox" data-prop="copyBugtrackerBinding"/>
         <label class=" afterDisabled" for="copyBugtrackerBinding"><f:message key="label.copyBugtrackerBinding" /></label>
         <br/>
         <input id="copyAutomatedProjects" name="copyAutomatedProjects" type="checkbox" data-prop="copyAutomatedProjects"/>
         <label class=" afterDisabled" for="copyAutomatedProjects"><f:message key="label.copyAutomatedProjects" /></label>
         <br/>
         <input id="copyInfolists" name="copyInfolists" type="checkbox" data-prop="copyInfolists"/>
         <label class=" afterDisabled" for="copyInfolists"><f:message key="label.copyInfolists" /></label>
                <br/>
         <input id="copyMilestone" name="copyMilestone" type="checkbox" data-prop="copyMilestone"/>
         <label class=" afterDisabled" for="copyMilestone"><f:message key="label.copyMilestone" /></label>
 		<br/>
		<input id="copyAllowTcModifFromExec" name="copyAllowTcModifFromExec" type="checkbox" data-prop="copyAllowTcModifFromExec"/>
         <label class=" afterDisabled" for="copyAllowTcModifFromExec"><f:message key="label.copyAllowTcModifFromExec" /></label>
        </td>
      </table>

   <div class="popup-dialog-buttonpane">
      <input class="confirm" type="button" value="<f:message key='label.addAnother' />" data-def="mainbtn, evt=addanother"/>
      <input class="confirm" type="button" value="<f:message key='label.Add' />" data-def="evt=confirm"/>
      <input class="cancel" type="button" value="<f:message key='label.Close' />" data-def="evt=cancel"/>
    </div>



  </div>
     </script>
  <!--   ===========================/CREATE FROM TEMPLATE DIALOG=======================================  -->

<!--   =========================== CREATE TEMPLATE FROM PROJECT DIALOG =======================================  -->
<script id="add-template-from-project-dialog-tpl" type="text/x-handlebars-template">
  <div id="add-template-from-project-dialog" class="not-displayed popup-dialog form-horizontal" title="<f:message key='title.addTemplateFromProject' />">
      <div id="templateFromProjectMessage"></div>
    <table class="form-horizontal">
      <tr class="control-group">
        <td>
          <label class="control-label" for="add-template-from-project-name" >
            <f:message key="label.Name" />
          </label>
        </td>
        <td class="controls">
          <input id="add-template-from-project-name" name="add-template-from-project-name" type="text" size="50" maxlength="255" data-prop="name" data-object="jsonTemplateFromProject" data-def="maininput"/>
          <span class="help-inline">&nbsp;</span>
        </td>
      </tr>
         <tr class="control-group">
        <td>
          <label class="control-label" for="add-template-from-project-description"><f:message key="label.Description" /></label>
        </td>
        <td class="controls">
          <textarea id="add-template-from-project-description" name="add-template-from-project-description" data-def="isrich" data-prop="description"></textarea>
          <span class="help-inline">&nbsp;</span>
        </td>
      </tr>
      <tr class="control-group">
        <td>
          <label class="control-label" for="add-template-from-project-label"><f:message key="label.tag" /></label>
        </td>
        <td class="controls">
          <input id="add-template-from-project-label" name="add-template-from-project-label"  type="text" size="50" maxlength="255" data-prop="label"/>
          <span class="help-inline">&nbsp;</span>
        </td>
      </tr>
      <tr class="control-group">
      <td>
          <label class="control-label"><f:message key="label.parametersFromTemplate" /></label>
      </td>
      <td>
      <!--        CHECKBOXES -->
          <input id="add-template-from-project-copyPermissions" name="add-template-from-project-copyPermissions" type="checkbox" data-prop="copyPermissions"/>
          <label class=" afterDisabled" for="add-template-from-project-copyPermissions"><f:message key="label.copyPermissions" /></label>
          <br/>
          <input id="add-template-from-project-copyCUF"  name="add-template-from-project-copyCUF" type="checkbox" data-prop="copyCUF"/>
          <label class=" afterDisabled" for="add-template-from-project-copyCUF"><f:message key="label.copyCUF" /></label>
          <br/>
          <input id="add-template-from-project-copyBugtrackerBinding" name="add-template-from-project-copyBugtrackerBinding" type="checkbox" data-prop="copyBugtrackerBinding"/>
          <label class=" afterDisabled" for="add-template-from-project-copyBugtrackerBinding"><f:message key="label.copyBugtrackerBinding" /></label>
          <br/>
          <input id="add-template-from-project-copyAutomatedProjects" name="add-template-from-project-copyAutomatedProjects" type="checkbox" data-prop="copyAutomatedProjects"/>
          <label class=" afterDisabled" for="add-template-from-project-copyAutomatedProjects"><f:message key="label.copyAutomatedProjects" /></label>
          <br/>
          <input id="add-template-from-project-copyInfolists" name="add-template-from-project-copyInfolists" type="checkbox" data-prop="copyInfolists"/>
          <label class=" afterDisabled" for="add-template-from-project-copyInfolists"><f:message key="label.copyInfolists" /></label>
          <br/>
          <input id="add-template-from-project-copyMilestone" name="add-template-from-project-copyMilestone" type="checkbox" data-prop="copyMilestone"/>
          <label class=" afterDisabled" for="add-template-from-project-copyMilestone"><f:message key="label.copyMilestone" /></label>
          <br/>
	  <input id="add-template-from-project-copyAllowTcModifFromExec" name="add-template-from-project-copyAllowTcModifFromExec" type="checkbox" data-prop="copyAllowTcModifFromExec"/>
         <label class=" afterDisabled" for="add-template-from-project-copyAllowTcModifFromExec"><f:message key="label.copyAllowTcModifFromExec" /></label>
         </td>

 </td>
      </table>

    <div class="popup-dialog-buttonpane">
        <input class="confirm" type="button" value="<f:message key='label.addAnother' />" data-def="mainbtn, evt=addanother"/>
      	<input class="confirm" type="button" value="<f:message key='label.Add' />" data-def="evt=confirm"/>
      	<input class="cancel" type="button" value="<f:message key='label.Close' />" data-def="evt=cancel"/>
    </div>

  </div>
</script>
  <!--   =========================== /CREATE TEMPLATE FROM PROJECT DIALOG =======================================  -->

  <!--   ===========================CREATE TEMPLATE DIALOG=======================================  -->
  <script id="add-template-dialog-tpl" type="text/x-handlebars-template">
  <div id="add-template-dialog" class="not-displayed popup-dialog form-horizontal" title="<f:message key='title.addTemplate' />">
    <table class="form-horizontal">
      <tr class="control-group">
        <td>
          <label class="control-label" for="add-template-name">
            <f:message key="label.Name" />
          </label>
        </td>
        <td class="controls">
          <input id="add-template-name" name="add-template-name" type="text" size="50" maxlength="255" data-prop="name" data-object="projectTemplate" data-def="maininput"/>
          <span class="help-inline">&nbsp;</span>
        </td>
      </tr>
      <tr class="control-group">
        <td>
          <label class="control-label" for="add-template-description"><f:message key="label.Description" /></label>
        </td>
        <td class="controls">
          <textarea id="add-template-description" name="add-template-description" data-def="isrich" data-prop="description" data-object="projectTemplate"></textarea>
          <span class="help-inline">&nbsp;</span>
        </td>
      </tr>
      <tr class="control-group">
        <td>
          <label class="control-label" for="add-template-label"><f:message key="label.tag" /></label>
        </td>
        <td class="controls">
          <input id="add-template-label" name="add-template-label" data-prop="label" data-object="projectTemplate" type="text" size="50" maxlength="255" />
          <span class="help-inline">&nbsp;</span>
        </td>
      </tr>
    </table>

    <div class="popup-dialog-buttonpane">
      <input class="confirm" type="button" value="<f:message key='label.addAnother' />" data-def="mainbtn, evt=addanother"/>
      <input class="confirm" type="button" value="<f:message key='label.Add' />" data-def="evt=confirm"/>
      <input class="cancel" type="button" value="<f:message key='label.Close' />" data-def="evt=cancel"/>
    </div>
  </div>
 	</script>
  <!--   ===========================/CREATE TEMPLATE DIALOG=======================================  -->
  </sec:authorize>
</div>
</jsp:attribute>
</layout:info-page-layout>
