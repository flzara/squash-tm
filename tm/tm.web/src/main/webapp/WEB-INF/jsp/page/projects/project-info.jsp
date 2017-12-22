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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"%>
<%@ taglib prefix="pc" tagdir="/WEB-INF/tags/project-components"%>
<%@ taglib prefix="json" uri="http://org.squashtest.tm/taglib/json"%>
<%@ taglib prefix="at" tagdir="/WEB-INF/tags/attachments"%>
<%@ taglib prefix="csst" uri="http://org.squashtest.tm/taglib/css-transform" %>


<%@ taglib prefix="sec"
	uri="http://www.springframework.org/security/tags"%>

<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%------------------------------------- URLs et back button ----------------------------------------------%>


<f:message var="confirmLabel"     key="label.Confirm" />
<f:message var="cancelLabel"      key="label.Cancel" />
<f:message var="closeLabel"          key="label.Close" />
<f:message var="renameLabel"      key="label.Rename" />


<s:url var="projectUrl" value="/generic-projects/{projectId}">
	<s:param name="projectId" value="${adminproject.project.id}" />
</s:url>

<s:url var="projectsUrl" value="/administration/projects" />

<s:url var="permissionPopupUrl"
	value="/generic-projects/{projectId}/unbound-parties">
	<s:param name="projectId" value="${adminproject.project.id}" />
</s:url>


<s:url var="customFieldManagerURL" 	value="/administration/projects/{projectId}/custom-fields-binding">
	<s:param name="projectId" 		value="${adminproject.project.id}"/>
</s:url>

<s:url var="infoListManagerURL" 	value="/administration/projects/{projectId}/infoList-binding">
	<s:param name="projectId" 		value="${adminproject.project.id}"/>
</s:url>

<s:url var="pluginsManagerURL"      value="/administration/projects/{projectId}/plugins">
	<s:param name="projectId" 	    value="${adminproject.project.id}" />
</s:url>

<s:url var="milestoneManagerURL"      value="/administration/projects/{projectId}/milestone-binding">
	<s:param name="projectId" 	    value="${adminproject.project.id}" />
</s:url>

<layout:info-page-layout titleKey="workspace.project.info.title" isSubPaged="true" main="project-page">
	<jsp:attribute name="head">
		<comp:sq-css name="squash.grey.css" />
	</jsp:attribute>

	<jsp:attribute name="titlePane">
		<h2 class="admin"><f:message key="label.administration" /></h2>
	</jsp:attribute>
	<jsp:attribute name="subPageTitle">
		<h2><f:message key="workspace.project.info.title" /></h2>
	</jsp:attribute>

	<jsp:attribute name="subPageButtons">
		<f:message var="backButtonLabel" key="label.Back" />
		<f:message var="backButtonLabel" key="label.Back" />
		<input type="button" class="button" value="${backButtonLabel}" onClick="history.back();"/>
	</jsp:attribute>
	<jsp:attribute name="informationContent">
	<c:choose>
	<c:when test="${ adminproject.template }">
		<f:message var="headerLabel" key="label.projectTemplate"/>
	</c:when><c:otherwise>
		<f:message var="headerLabel" key="label.Project"/>
	</c:otherwise>
	</c:choose>
		<div id="project-name-div"
			class="ui-widget-header ui-corner-all ui-state-default fragment-header">

			<div class="snap-left" style="height: 3em">
				<h2>
					<label for="project-name-header">
            <f:message key="${ adminproject.template ? 'label.projectTemplate' : 'label.project' }" />
					</label>
          <a id="project-name-header" >
            <c:out value="${ adminproject.project.name }" escapeXml="true" />
					</a>
				</h2>
			</div>

			<div class="unsnap"></div>

		</div>

		<%---INFO + Toolbar ---------------------%>
			<div id="project-toolbar" class="toolbar-class ui-corner-all">

				<div class="snap-left">
					<comp:general-information-panel auditableEntity="${adminproject.project}" entityUrl="${ projectUrl }" />
				</div>

				<div class="toolbar-button-panel">
<sec:authorize access="hasRole('ROLE_TM_PROJECT_MANAGER') or hasRole('ROLE_ADMIN')">
<c:if test="${ adminproject.template }">
                    <button   id="coerce" class="sq-btn" data-template-id="${ adminproject.id }" >
                  <f:message key='label.coerceTemplateIntoProject' />
                  </button>
                    <div id="coerce-warning-dialog" title="<f:message key="title.coerceTemplateIntoProject" />" class="alert not-displayed">
                      <f:message key="message.coerceTemplateIntoProject" />
                      <input type="button" value="<f:message key='label.Confirm' />" />
                      <input type="button" value="<f:message key='label.Cancel' />" />
                    </div>
</c:if>
					<f:message var="rename" key="project.button.rename.label" />
					<button   value="${ rename }" id="rename-project-button" title="<f:message key='project.button.renameproject.label' />"
								class="sq-btn" >
								<f:message key="project.button.renameproject.label" />
								</button>
</sec:authorize>
<sec:authorize access="hasRole('ROLE_ADMIN')">
    				<f:message var="delete" key='project.button.delete.label' />

 					<%-------------------------- Trash appear but too much padding.   ------------------------%>
    				<button id="delete-project-button" ${ delete }  class="sq-btn"  title="<f:message key='project.button.deleteproject.label' />" >
        			   <span class="ui-icon ui-icon-trash">-</span>&nbsp;<f:message key="label.Delete" />
      				</button>

</sec:authorize>
				</div>
				<div class="unsnap"></div>
			</div>
			<%-------------------------------------------------------------END INFO + Toolbar ---------------%>

			<%------------------------------------------------ BODY -----------------------------------------------%>
			<csst:jq-tab>
			<div class="fragment-tabs fragment-body">
			<ul class="tab-menu">
				<li><a href="#main-informations"><f:message key="tabs.label.mainpanel"/></a></li>
				<li><a href="${customFieldManagerURL}"><f:message key="tabs.label.cufbinding"/></a></li>
				<li><a href="${infoListManagerURL}"><f:message key="tabs.label.infoList"/></a></li>
			   <c:if test="${milestoneFeatureEnabled }">
			    <li><a href="${milestoneManagerURL}"><f:message key="tabs.label.milestone"/></a></li>
				</c:if>
				<li><a href="${pluginsManagerURL}"><f:message key="tabs.label.plugins"/></a></li>

			</ul>

			<%----------------------------------- INFORMATION PANEL -----------------------------------------------%>
			<div id="main-informations">

            <comp:toggle-panel id="project-info-panel"
				titleKey="project.info.panel.title" open="true">

				<jsp:attribute name="body">
					<div id="project-description-table" class="display-table">
						<div class="display-table-row">
							<label for="project-label" class="display-table-cell">
							<f:message key="label.tag" />
							</label>
							<div class="display-table-cell editable text-editable" data-def="url=${projectUrl}, maxlength=255" id="project-label">${ adminproject.project.label }</div>
						</div>
						<div class="display-table-row">
							<label for="project-description" class="display-table-cell">
							<f:message key="label.Description" />
							</label>
							<div class="display-table-cell editable rich-editable" data-def="url=${projectUrl}" id="project-description">${ adminproject.project.description }</div>
						</div>
						<%-- 	Waiting for implementation of deactivation	<comp:project-active adminproject="${ adminproject }"/> --%>
					</div>
				</jsp:attribute>
			</comp:toggle-panel>
			<%-----------------------------------END INFORMATION PANEL -----------------------------------------------%>
					<%----------------------------------- BUGTRACKER PANEL -----------------------------------------------%>

		<c:if test="${!bugtrackersListEmpty}">
			<comp:toggle-panel id="project-bugtracker-panel"
					titleKey="label.Bugtracker"
					open="true" >
				<jsp:attribute name="body">

					<div id="project-bugtracker-table" class="display-table">

						<div class="display-table-row">
							<label for="project-bugtracker" class="display-table-cell">
								<f:message key="label.Bugtracker" />
							</label>
							<div class="display-table-cell">
								<div id="project-bugtracker">
									<c:choose>
										<c:when test="${ !adminproject.project.bugtrackerConnected }">
											<f:message key="project.bugtracker.name.undefined" />
										</c:when>
										<c:otherwise>
											${ adminproject.project.bugtrackerBinding.bugtracker.name }
										</c:otherwise>
									</c:choose>
								</div>


								<span id="project-bugtracker"/>


							</div>
						</div>
						<c:if test="${ ! adminproject.template }">
						<div class="display-table-row"	id="project-bugtracker-project-name-row"
								<c:if test="${ !adminproject.project.bugtrackerConnected }">style="display:none"</c:if>>
							<label for="project-bugtracker-project-name"
									class="display-table-cell" style="vertical-align: middle";
									>
								<f:message key="project.bugtracker.project.name.label" />
							</label>

<ul id="project-bugtracker-project-name"  class="tagprop tagit ui-widget ui-widget-content squash-tagit" style="margin:0;line-height:normal;" value="">
						</div>
						</c:if>
					</div>
				</jsp:attribute>
			</comp:toggle-panel>
		</c:if>
		<%-----------------------------------END BUGTRACKER PANEL -----------------------------------------------%>
				<%----------------------------------- USER PANEL -----------------------------------------------%>
			<f:message key="title.AddPermission" var="addButtonTitle" />
			<f:message key="title.RemovePermission" var="removeButtonTitle" />
			<comp:toggle-panel id="project-users-panel"
				titleKey="label.Permissions" open="true">

				<jsp:attribute name="panelButtons">
        <button id="add-permission-button" title="${addButtonTitle}" class="sq-icon-btn btn-sm">
          <span class="ui-icon ui-icon-plus squared-icons">+</span>
        <button id="remove-permission-button" title="${removeButtonTitle}" class="sq-icon-btn btn-sm">
          <span class="ui-icon ui-icon-minus squared-icons">-</span>
        </button>
				</jsp:attribute>

				<jsp:attribute name="body">
					<table id="user-permissions-table">
						<thead>
							<tr>
								<th class="party-index">#</th>
								<th class="party-id"></th>
								<th class="party-name datatable-filterable"><f:message key="party.header.title" /></th>
								<th class="user-permission"><f:message key="project.permission.table.profile.label" /></th>
								<th class="party-type"><f:message key="party.type" /></th>
								<th class="empty-delete-holder"> </th>
							</tr>
						</thead>
					</table>
					<div class="not-displayed permission-select-template">
						<select>
						<c:forEach var="perm" items="${availablePermissions}">
						<option value="${perm.qualifiedName}"><f:message key="user.project-rights.${perm.simpleName}.label"/></option>
						</c:forEach>
						</select>
					</div>
					<tbody>
					</tbody>
				</jsp:attribute>
			</comp:toggle-panel>
			<%-----------------------------------END USERS PANEL -----------------------------------------------%>


			<%----------------------------------------EXEC OPTIONS PANEL----------------------------------------------------%>
			<f:message var="active" key="label.active" />
			<f:message var="inactive" key="label.inactive" />
			<comp:toggle-panel id="exec-option-panel" titleKey="label.execution.option" open="true">
				<jsp:attribute name="body">

				<div id="project-exec-option-table" class="display-table">
						<div class="display-table-row">
							<div class="display-table-cell">
								<label for="toggle-EXECUTION-checkbox" class="display-table-cell" style="vertical-align:bottom">
									<f:message key="label.execution.modification" />
								</label>
							</div>

							<div class="display-table-cell">
	                  			<input id="toggle-EXECUTION-checkbox" type="checkbox"
	                  				data-def="width=35, on_label=${inactive}, off_label=${active}, checked=${!allowTcModifDuringExec}" style="display: none;"/>
	                  		</div>
						</div>

				</div>
				</jsp:attribute>
		    </comp:toggle-panel>


			<%----------------------------------------END EXEC OPTIONS PANEL----------------------------------------------------%>


			<%----------------------------------------STATUS----------------------------------------------------%>
			<f:message var="statusAllowedLabel" key="label.status.options.allowed" />
			<f:message var="statusForbiddenLabel" key="label.status.options.forbidden" />
			<comp:toggle-panel id="project-status-panel" titleKey="label.status.options" open="true">
				<jsp:attribute name="body">
					<div id="project-description-table" class="display-table">
						<div class="display-table-row">
							<div class="display-table-cell" style="vertical-align: middle">
								<label for="toggle-UNTESTABLE-checkbox" class="display-table-cell">
									<f:message key="label.status.options.optional" />
								</label>
							</div>
							<div class="display-table-cell" style="vertical-align: middle">
								<span class="display-table-cell exec-status-label exec-status-untestable">
									<f:message key="execution.execution-status.UNTESTABLE" />
								</span>
							</div>
							<div class="display-table-cell" >
	                  			<input id="toggle-UNTESTABLE-checkbox" type="checkbox"
	                  				data-def="width=35, on_label=${statusAllowedLabel}, off_label=${statusForbiddenLabel}, checked=${allowedStatuses['UNTESTABLE']}" style="display: none;"/>
	                  		</div>
						</div>
						<div class="display-table-row">
							<div class="display-table-cell">
							</div>
							<div class="display-table-cell" style="vertical-align: middle">
								<span class="display-table-cell exec-status-label exec-status-settled">
									<f:message key="execution.execution-status.SETTLED" />
								</span>
							</div>
							<div class="display-table-cell" style="vertical-align: middle">
	                  			<input id="toggle-SETTLED-checkbox" type="checkbox"
	                  				data-def="width=35,on_label=${statusAllowedLabel}, off_label=${statusForbiddenLabel}, checked=${allowedStatuses['SETTLED']}" style="display: none;"/>
	                  		</div>
						</div>
					</div>
				</jsp:attribute>
			</comp:toggle-panel>
			<%---------------------------------------/STATUS----------------------------------------------------%>
			<%------------------------------ TEST AUTOMATION PROJECT -------------------------------------------%>

			<pc:automation-panel
				project="${adminproject.project}"
				availableTAServers="${availableTAServers}"/>

			<%----------------------------- /TEST AUTOMATION PROJECT -------------------------------------------%>
			<%----------------------------- ATTACHMENT -------------------------------------------%>

			<at:attachment-bloc editable="${ true }"  workspaceName="administration" attachListId="${adminproject.project.attachmentList.id}" attachmentSet="${attachments}"/>
			<%----------------------------- /ATTACHMENT -------------------------------------------%>

			</div> <%-- /div#main-informations --%>

			</div>	<%-- /div#project-administration-content --%>
			</csst:jq-tab>
		<%---------------------------------------------------------------END  BODY -----------------------------------------------%>

		<%----------------------------------- add User Popup-----------------------------------------------%>
		<div id="add-permission-dialog" class="popup-dialog not-displayed" title="<f:message key='title.AddPermission' />">

			<input type="hidden" id="source-status"></input>

			<div data-def="state=loading">
				<comp:waiting-pane/>
			</div>

			<div data-def="state=normal" class="display-table">
				<div class="display-table-row">
					<span class="display-table-cell"> <f:message key="party.label" /></span>
					<div class="display-table-cell"><input id="party-input"/></div>
				</div>
				<div class="display-table-row">
					<span class="display-table-cell"><f:message key="label.Permission"/></span>
					<div class="display-table-cell">
						<select id="permission-input">
						<c:forEach items="${availablePermissions}" var="permission">
							<option value="${permission.qualifiedName}" id="${permission.simpleName}">
								<f:message	key="user.project-rights.${permission.simpleName}.label" />
							</option>
						</c:forEach>
						</select>
					</div>
				</div>
			</div>

			<div data-def="state=allbound"><span><f:message key="message.AllUsersAlreadyLinkedToProject"/></span></div>

			<div data-def="state=noselect"><span><f:message key="error.permissions.noUserSelected"/></span></div>

			<div class="popup-dialog-buttonpane">
				<input type="button" value="${confirmLabel}" data-def="state=normal, mainbtn=normal, evt=confirm"/>
				<input type="button" value="${cancelLabel}" data-def="state=normal loading, mainbtn=loading, evt=cancel" />
				<input type="button" value="${closeLabel}"  data-def="state=allbound noselect, mainbtn=allbound noselect, evt=cancel"/>
			</div>

		</div>

		<%----------------------------------- /add User Popup-----------------------------------------------%>

		<%----------------------------------- remove User Popup-----------------------------------------------%>


		<f:message var="removeuserTitle" key="tooltips.permissions.remove" />
		<div id="remove-permission-dialog" class="popup-dialog not-displayed" title="${removeuserTitle}">

		<f:message key="message.permissions.remove.teamOrUser.first"/>

		<div class="popup-dialog-buttonpane">
				<input type="button" value="${confirmLabel}" data-def="state=normal, mainbtn=normal, evt=confirm"/>
				<input type="button" value="${cancelLabel}" data-def="state=normal loading, mainbtn=loading, evt=cancel" />
		</div>

		</div>

		<%----------------------------------- /remove User Popup-----------------------------------------------%>

		<%------------------------------------replace status popup ------------------------------------------%>

		<div id="replace-status-dialog" class="popup-dialog not-displayed" title="<f:message key="label.status.options.popup.label"/>">

			<!--  warning message template -->
			<div class="replace-status-warning-template not-displayed"><f:message key="label.status.options.popup.text"/></div>

			<div data-def="state=loading">
				<comp:waiting-pane/>
			</div>


			<div data-def="state=normal" class="display-table">
				<!--  message actually displayed -->
				<div class="display-table-row replace-status-warning">

				</div>
				<div class="display-table-row">
					<f:message key="label.Status"/>
					<select id="status-input">
					</select>
				</div>
			</div>

			<div class="popup-dialog-buttonpane">
				<input type="button" value="${confirmLabel }" data-def="state=normal, mainbtn=normal, evt=confirm"/>
				<input type="button" value="${cancelLabel}" data-def="state=normal loading, mainbtn=loading, evt=cancel" />
				<input type="button" value="${okLabel}"  data-def="state=allbound noselect, mainbtn=allbound noselect, evt=cancel"/>
			</div>
		</div>

		<%-----------------------------------/replace status popup ------------------------------------------%>

	</jsp:attribute>
</layout:info-page-layout>

<!-- --------------------------------RENAME POPUP--------------------------------------------------------- -->
<sec:authorize access="hasRole('ROLE_TM_PROJECT_MANAGER') or hasRole('ROLE_ADMIN')">

  <f:message var="renameTitle" key="dialog.rename-project.title" />
  <div id="rename-project-dialog" class="popup-dialog not-displayed" title="${renameTitle}">
    <div>
        <label>
          <f:message key="dialog.rename.label" />
	    </label>
	     <input type="text" id="rename-project-input" maxlength="255" size="50" />
		  <br />
		<comp:error-message forField="name" />
    </div>
    <div class="popup-dialog-buttonpane">
      <input type="button" value="${confirmLabel}" data-def="evt=confirm, mainbtn" />
      <input type="button" value="${cancelLabel}" data-def="evt=cancel" />
    </div>
  </div>

</sec:authorize>

<!-- ------------------------------------END RENAME POPUP------------------------------------------------------- -->
<script type="text/javascript">
/* popup renaming success handler */


var squashtm = squashtm || {};
squashtm.app = squashtm.app || {} ;
squashtm.app.messages = squashtm.app.messages || {} ;
squashtm.app.messages["message.notBlank"] =  "<f:message key='message.notBlank' />";

require(["common"], function() {

	require(["jquery", "projects-manager","squash.configmanager", "jquery.squash.fragmenttabs", "squash.attributeparser",
	         "project/ProjectToolbar", "jquery.squash.oneshotdialog", "app/ws/squashtm.notification", "squash.translator",
	         "squashtable", "jquery.squash.formdialog", "jquery.switchButton",
	         "app/ws/squashtm.workspace", "jquery.squash.formdialog",  "jquery.squash.tagit"],
	         function($, projectsManager, confman, Frag, attrparser, ProjectToolbar, oneshot, notification, translator){



	function clickProjectBackButton(){
		document.location.href = "${projectsUrl}";
	}


	function projectBugTrackerCallBack (value) {

		<c:if test="${ ! adminproject.template }">
			  if(value != "<f:message key='project.bugtracker.name.undefined'/>"){
	        	 $("#project-bugtracker-project-name-row").show();
	        	 initBugTrackerTag();

		     }else{
	        	 $("#project-bugtracker-project-name-row").hide();
	         }
	      </c:if>
	}

	function initBugtrackerProjectEditable(){

	$('#project-bugtracker').editable( "${projectUrl}", {
	      type: 'select',
	      placeholder: '<f:message key="rich-edit.placeholder" />',
	      submit: '<f:message key="rich-edit.button.ok.label" />',
	      cancel: '<f:message key="label.Cancel" />',
	      onblur : function() {},
	      callback : function(value, settings){projectBugTrackerCallBack(value);},
	      data : JSON.stringify(${bugtrackersList}),
	      indicator : '<span class="processing-indicator" />'
	    }).addClass("editable");

	}

	function initBugTrackerTag(){
		var tagconf = confman.getStdTagit();
		var $tag = $("#project-bugtracker-project-name");

		tagconf.validate = function(){
			var assignedTags = $tag.squashTagit('assignedTags');
			//need at least one project name
			return assignedTags.length > 0 ? true : false;
		}

		$tag.squashTagit(tagconf).sortable({
			stop: function() {
				sendBugTrackerTag($tag.squashTagit('assignedTags'));
				}
		});

		$tag.on('squashtagitbeforetagremoved', function(event, ui){
			var assignedTags = $tag.squashTagit('assignedTags');
			//don't remove if there is only one
			return assignedTags.length > 1 ? true : false;
		});


		$tag.on('squashtagitaftertagadded squashtagitaftertagremoved', function(event, ui){

			if (!ui.duringInitialization) {
			if (! $tag.squashTagit("validate", event, ui)){
				return;
			}
			sendBugTrackerTag($tag.squashTagit('assignedTags'));
			}
		});

		$.ajax({type: 'GET',
			url: "${projectUrl}/bugtracker/projectName"}).done(
					function(data){
						data.forEach(function(val){
							$tag.squashTagit("createTag", val, "", true);
						});
						});
	}

	function sendBugTrackerTag(tags){
		$.ajax({type: 'POST',
			url: "${projectUrl}",
			data : {id:"project-bugtracker-project-name",
				values:tags}
	});


	}

	$(function() {

		 		init(projectsManager, Frag);
		 		configureActivation("UNTESTABLE");
		 		configureActivation("SETTLED");
		 		configureActivation("EXECUTION");

		 		$("#toggle-EXECUTION-checkbox").change(function(){
		 			toogleExec();
		 		});

		 		$("#toggle-UNTESTABLE-checkbox").change(function(){
		 			toggleStatusActivation("UNTESTABLE");
		 		});
		 		$("#toggle-SETTLED-checkbox").change(function(){
		 			toggleStatusActivation("SETTLED");
		 		});

		 		initBugtrackerProjectEditable();
		 		if (${adminproject.project.bugtrackerConnected}) {
		 		initBugTrackerTag();
		 		}
		 		new ProjectToolbar();
	});

	function toogleExec(){
		var shouldActivate = ! $("#toggle-EXECUTION-checkbox").prop('checked');

			$.ajax({
				type: 'POST',
				url: "${projectUrl}",
				data : {
					value : shouldActivate
				}
			});

	}

	function refreshTableAndPopup(){
		$("#user-permissions-table").squashTable().refresh();
	}

	function configureActivation(status){

		var activCbx = $("#toggle-"+status+"-checkbox"),
			activConf = attrparser.parse(activCbx.data('def'));
		activConf.checked = activConf.checked == 'true';
		activCbx.switchButton(activConf);

		//a bit of css tweak now
		activCbx.siblings('.switch-button-background').css({position : 'relative',   top : '5px'});


	}

	// status popup
	var statuspopup = $("#replace-status-dialog");
	statuspopup.formDialog();

	statuspopup.on('formdialogopen', function(){

		statuspopup.formDialog('setState', 'normal');
		var removedstatus = statuspopup.data('removed-status'),
			statusname = $(".exec-status-label.exec-status-"+removedstatus.toLowerCase()).text();

		var txt = statuspopup.find('.replace-status-warning-template').text().replace('{0}',statusname);
		statuspopup.find('.replace-status-warning').text(txt);

		$.getJSON("${projectUrl}/execution-status/"+removedstatus).done(function(json){
				$("#status-input").empty();
				$.each(json, function(key){
					var o = new Option(key, json[key]);
					$(o).html(key);
					$("#status-input").append(o);
				});
			statuspopup.formDialog('setState', "normal");
		});
	});

	statuspopup.on('formdialogcancel', function(){
		var removedstatus = statuspopup.data('removed-status');
		 $("#toggle-"+removedstatus+"-checkbox").switchButton({
			  checked: true
		});
		$("#status-input").html("");
		statuspopup.formDialog('close');
	});

	statuspopup.on('formdialogconfirm', function(){
		var target = $("#status-input").val();
		var source = $("#source-status").val();
		$.ajax({
			type: 'POST',
			url: "${projectUrl}/replace-execution-status",
			data : {
				sourceExecutionStatus : source,
				targetExecutionStatus : target,
				success : function(){
					deactivateStatus(source);
					statuspopup.formDialog('close');
				}
			}
		});
	});

	function toggleStatusActivation(status){
		var shouldActivate = $("#toggle-"+status+"-checkbox").prop('checked');
		if (shouldActivate){
			activateStatus(status);
		}
		else{
			$.ajax({
				type: 'GET',
				 url: "${projectUrl}/execution-status-is-used/"+status,
				 success : function(data){
						if(data){
							$("#source-status").val(status);
							statuspopup.data('removed-status', status);
							statuspopup.formDialog('open');
						}
						else {
						 	deactivateStatus(status);
						}
				 }
			});


		}

	}

	function activateStatus(status){
		$.ajax({
			type: 'POST',
			 url: "${projectUrl}/enable-execution-status/"+status,
		});
	}

	function deactivateStatus(status){
		$.ajax({
			type: 'POST',
			 url: "${projectUrl}/disable-execution-status/"+status,
		});
	}

	function init(projectsManager, Frag){


		// back button
		$("#back").click(clickProjectBackButton);


		// rename popup
		var renameDialog = $("#rename-project-dialog"),
			nameHeader = $('#project-name-header');
		renameDialog.formDialog();

		renameDialog.on('formdialogopen', function(){
			var name = $.trim(nameHeader.text());
			$("#rename-project-input").val(name);
		});

		renameDialog.on('formdialogconfirm', function(){
			var name = $("#rename-project-input").val();
            $.ajax({
              url : "${projectUrl}",
              type : 'POST',
              data : {newName : name }
            })
            .success(function(){
            	nameHeader.text(name);
            	renameDialog.formDialog('close');
            });
		});
		renameDialog.on('formdialogcancel', function(){
			renameDialog.formDialog('close');
		});
		$("#rename-project-button").on('click', function(){
			renameDialog.formDialog('open');
		});


		// permissions popup
		var permpopup = $("#add-permission-dialog");
		permpopup.formDialog();

		permpopup.on('formdialogopen', function(){

			permpopup.formDialog('setState', 'loading');

			$.getJSON("${permissionPopupUrl}").done(function(json){
				if (json.length === 0){
					permpopup.formDialog('setState', "allbound");
				}
				else{
					permpopup.data('source', json);
					$("#party-input").autocomplete({
						source : json
					});
					permpopup.formDialog('setState', "normal");
				}
			});
		});

		permpopup.on('formdialogconfirm', function(){

			var partyname = $("#party-input").val();
			var permission = $("#permission-input").val();
			var validselection = $.grep(permpopup.data('source'), function(e){ return (e.label === partyname);});

			if (validselection.length === 1){
				var partyId = validselection[0].id;
				var url = squashtm.app.contextRoot+"/generic-projects/${adminproject.project.id}/parties/"+partyId+"/permissions/"+permission+"/";
				permpopup.formDialog('setState','loading');
				$.ajax({
					url : url,
					type : 'PUT',
					success : function(){
						$("#user-permissions-table").squashTable().refresh();
						permpopup.formDialog('close');
					}
				})
			}else{
				permpopup.formDialog('setState', 'noselect');
			}

		});

		permpopup.on('formdialogcancel', function(){
			permpopup.formDialog('close');
		});

		// permission mgt
		$("#add-permission-button").on('click', function(){
			permpopup.formDialog('open');
		});

		var permremovepopup = $("#remove-permission-dialog");
		permremovepopup.formDialog();

		permremovepopup.on('formdialogopen', function(){

			permpopup.formDialog('setState', 'loading');

			$.getJSON("${permissionPopupUrl}").done(function(json){
				if (json.length === 0){
					permpopup.formDialog('setState', "allbound");
				}
				else{
					permpopup.data('source', json);
					$("#party-input").autocomplete({
						source : json
					});
					permpopup.formDialog('setState', "normal");
				}
			});
		});

		permremovepopup.on('formdialogconfirm', function(){
			var table = $("#user-permissions-table");
			var ids = table.squashTable().getSelectedIds() ;
		for(key in ids) {
			val = ids[key];
			$.ajax({
				type : 'delete',
				dataType : "json",
				url : squashtm.app.contextRoot + "/generic-projects/" + permSettings.basic.projectId +
				"/parties/" + val +"/permissions"
			}).done(function() {
				table.squashTable().refresh();
			});
		}

			$(this).formDialog('close');

		});

		permremovepopup.on('formdialogcancel', function(){
			permremovepopup.formDialog('close');
		});

		$("#remove-permission-button").on('click', function(){
			var hasPermission = ($("#user-permissions-table").squashTable().getSelectedIds().length > 0);
			if (hasPermission) {
				permremovepopup.formDialog('open');
				}
			 else {
				 notification.showError(translator.get('message.NoMemberSelected'));
			}

		});

		//user permissions table
		var permSettings = {
			basic : {
				projectId : ${adminproject.project.id},
				userPermissions : ${json:serialize(userPermissions)}
			},
			language : {
				ok : '${confirmLabel}',
				cancel : '${cancelLabel}',
				deleteMessage : "<f:message key='message.permissions.remove.teamOrUser'/>",
				deleteTooltip : '<f:message key="tooltips.permissions.remove"/>'
			}
		};

		projectsManager.projectInfo.initUserPermissions(permSettings);


		Frag.init();
	}


	<sec:authorize access=" hasRole('ROLE_ADMIN')">
	$(function() {
		function deleteProject(){
		<c:if test="${adminproject.deletable}">
			oneshot.show('dialog.delete-project.title',
			"<div class='display-table-row'><div class='display-table-cell warning-cell'><div class='generic-error-signal'></div></div><div class='display-table-cell'><f:message key='message.project.remove.first'/><span class='red-warning-message'> <f:message key='message.project.remove.second'/> </span><f:message key='message.project.remove.third'/><span class='bold-warning-message'> <f:message key='message.project.remove.fourth'/> </span></div></div>"
			).done(function(){
				requestProjectDeletion().done(deleteProjectSuccess);
			});
			</c:if>
			<c:if test="${!adminproject.deletable}">
				$.squash.openMessage("<f:message key='popup.title.info'/>","<f:message key='project.delete.cannot.exception'/>");
			</c:if>
		}

		function requestProjectDeletion(){
			return $.ajax({
				type : 'delete',
				dataType : "json",
				url : "${ projectUrl }"
			});
		}

		function deleteProjectSuccess(data){
			clickProjectBackButton();
		}

		$('#delete-project-button').click(deleteProject);
	});
	</sec:authorize>
});
});
</script>
