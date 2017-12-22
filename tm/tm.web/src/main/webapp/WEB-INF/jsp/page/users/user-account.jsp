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

<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="json" uri="http://org.squashtest.tm/taglib/json"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"%>


<f:message var="userAccountPasswordLabel" key="label.password" />
<f:message var="bindMilestoneDialogTitle" key="message.PickAMilestone"/>
<f:message var="confirmLabel" key="label.Confirm"/>
<f:message var="cancelLabel" key="label.Cancel"/>

<c:url var="userAccountUrl" value="/user-account/update" />

<layout:info-page-layout titleKey="dialog.settings.account.title" highlightedWorkspace="home">
	<jsp:attribute name="head">
		<comp:sq-css name="squash.grey.css" />
	</jsp:attribute>

	<jsp:attribute name="titlePane">
		<h2><f:message key="dialog.settings.account.title" /></h2>
	</jsp:attribute>

	<jsp:attribute name="informationContent">

	<script type="text/javascript">
		require(["common"], function() {
				require(["jquery","squash.basicwidgets"], function($, basic){
					$(function(){
					basic.init();
					$("#back").click(function(){
						history.back();
					});
				});

					if(localStorage["requirement-tree-pref"] == 1){
						$('#user-preferences-tree-requirement option:eq(1)').prop('selected', true);
					}

					if(localStorage["test-case-tree-pref"] == 1){
						$('#user-preferences-tree-test-case option:eq(1)').prop('selected', true);
					}

					if(localStorage["campaign-tree-pref"] == 1){
						$('#user-preferences-tree-campaign option:eq(1)').prop('selected', true);
					}

					$("#user-preferences-tree-requirement").change(function(){
						localStorage["requirement-tree-pref"] = $("#user-preferences-tree-requirement").val();
					});

					$("#user-preferences-tree-test-case").change(function(){
						localStorage["test-case-tree-pref"] = $("#user-preferences-tree-test-case").val();
					});

					$("#user-preferences-tree-campaign").change(function(){
						localStorage["campaign-tree-pref"] = $("#user-preferences-tree-campaign").val();
					});

			});

      require(["jquery",  "squash.configmanager", "jquery.squash.fragmenttabs", "squash.attributeparser", "jquery.squash.oneshotdialog", "app/ws/squashtm.notification", "squash.translator",
          "squashtable", "jquery.squash.formdialog", "jquery.switchButton",
          "app/ws/squashtm.workspace", "jquery.squash.formdialog", "jquery.squash.tagit"],
        function ($, confman, Frag, attrparser ) {

          $(function () {
            configureActivation();
            $("#toggle-BUGTRACKER-MODE-checkbox").change(function () {
              toogleBugtrackerMode();
            });

            function toogleBugtrackerMode() {
              var shouldActivate = !$("#toggle-BUGTRACKER-MODE-checkbox").prop('checked');
              console.log(shouldActivate);
              if(shouldActivate == true){ bugtrackerMode = "Manual"; }else{ bugtrackerMode = "Automatic"; }

              $.ajax({
                type: 'POST',
                url: "user-account/update",
                data: {
                  value: bugtrackerMode
                }
              });
            }

            function configureActivation() {

              var activCbx = $("#toggle-BUGTRACKER-MODE-checkbox"),
                activConf = attrparser.parse(activCbx.data('def'));
              var bugMode =  "${bugtrackerMode}";
              var test="";

              if (bugMode == "Manual"){ test = true;}else{test = false;}
              activConf.checked = activConf.checked == test;

              activCbx.switchButton(activConf);
              //a bit of css tweak now
              activCbx.siblings('.switch-button-background').css({position: 'relative', top: '5px'});
            }
          });
        });
		});
	</script>
	<div id="user-login-div" class="ui-widget-header ui-corner-all ui-state-default fragment-header">

		<div style="float: left; height: 3em">
			<h2>
				<label for="user-login-header"><f:message key="user.header.title" /></label>
				<c:out value="${ user.login }" escapeXml="true" />
			</h2>
		</div>
		<div class="snap-right"><f:message var="back" key="label.Back" />
			<input id="back" type="button" value="${ back }" class="sq-btn" />
		</div>

		<div class="unsnap"></div>

	</div>

	<div class="fragment-body">

		<comp:toggle-panel id="basic-info-panel" titleKey="user.account.basicinfo.label" open="true" >
			<jsp:attribute name="body">
				<div class="display-table">
					<div class="user-account-unmodifiable-field display-table-row">
						<label><f:message key="label.Name"/></label>
						<div class="display-table-cell"><span>${user.firstName } ${user.lastName}</span></div>
					</div>
					<div class="display-table-row">
						<label ><f:message key="label.Email"/></label>
						<div id="user-account-email" class="display-table-cell editable text-editable" data-def="url=${userAccountUrl}, width=200"><span id="user-account-email">${user.email}</span></div>
					</div>
					<div class="display-table-row">
						<label ><f:message key="label.Group"/></label>
						<div class="display-table-cell"><span><f:message key="user.account.group.${user.group.qualifiedName}.label" /></span></div>
					</div>
        <c:if test="${ authenticationProvider.managedPassword }">
        <div class="display-table-row">
          <label for="managed-pwd"><f:message key="label.password"/></label>
          <div class="display-table-cell">
            <span id="managed-pwd"><f:message key="message.managedPassword" /></span>
          </div>
        </div>
        </c:if>
				</div>
				<br/>
        <c:if test="${ not authenticationProvider.managedPassword }">
				<input type="button" id="change-password-button" value="${ userAccountPasswordLabel }" class="button" />
        </c:if>
			</jsp:attribute>
		</comp:toggle-panel>
		<comp:toggle-panel id="project-permission-panel" titleKey="user.project-rights.title.label" open="true">
			<jsp:attribute name="body">
				<table id="project-permission-table" data-def="hover">
				<thead>
					<tr>
						<th data-def="sortable, target=0"><f:message key="label.project" /></th>
						<th data-def="sortable, target=1"><f:message key="label.Permission" /></th>
					</tr>
				</thead>
				<tbody>
					<c:forEach var="projectPermission" items="${ projectPermissions }">
					<tr><td>${ projectPermission.project.name }</td><td><f:message key="user.project-rights.${projectPermission.permissionGroup.simpleName}.label" /></td></tr>
					</c:forEach>
				</tbody>
			</table>
			</jsp:attribute>
		</comp:toggle-panel>

		<comp:toggle-panel id="tree-order-panel" titleKey="user-preferences.tree-order.title" open="true" >
			<jsp:attribute name="body">
				<div class="display-table">
				<div class="display-table-row">
						<label><f:message key="user-preferences.tree-order.requirement.title"/></label>
						<div class="display-table-cell">
							<select id="user-preferences-tree-requirement">
								<option value="0"><f:message key="user-preferences.tree-order.alphabetical"/></option>
								<option value="1"><f:message key="user-preferences.tree-order.custom"/></option>
							</select>
						</div>
				</div>
			    <div class="display-table-row">
						<label><f:message key="user-preferences.tree-order.testcase.title"/></label>
						<div class="display-table-cell">
							<span>
							<select id="user-preferences-tree-test-case">
								<option value="0"><f:message key="user-preferences.tree-order.alphabetical"/></option>
								<option value="1"><f:message key="user-preferences.tree-order.custom"/></option>
							</select>
							</span>
						</div>
				</div>
				<div class="display-table-row">
										<label><f:message key="user-preferences.tree-order.campaign.title"/></label>
						<div class="display-table-cell">
							<span>
							<select id="user-preferences-tree-campaign">
								<option value="0"><f:message key="user-preferences.tree-order.alphabetical"/></option>
								<option value="1"><f:message key="user-preferences.tree-order.custom"/></option>
							</select>
							</span>
						</div>
				</div>
				</div>
			</jsp:attribute>
		</comp:toggle-panel>

    <f:message var="automatic" key="user-preferences.bugtracker-management.label.automatic"/>
    <f:message var="manual" key="user-preferences.bugtracker-management.label.manual"/>
    <comp:toggle-panel id="bugtracker-configuration-panel" titleKey="user-preferences.bugtracker-management.title"  open="true">
		  <jsp:attribute name="body">

        <div id="user-preferences.bugtracker-management-table" class="display-table">
          <div class="display-table-row">
            <div class="display-table-cell">
              <label for="toggle-BUGTRACKER-MODE-checkbox" class="display-table-cell" style="vertical-align:bottom">
                <f:message key="user-preferences.bugtracker-management.presentation.label"/>
              </label>
            </div>

            <div class="display-table-cell">
              <input id="toggle-BUGTRACKER-MODE-checkbox" type="checkbox"
                     data-def="width=35, on_label=${automatic}, off_label=${manual}, checked=${test}"
                     style="display: none;"/>
            </div>
          </div>
        </div>
     	</jsp:attribute>
   	</comp:toggle-panel>




        <f:message var="milestoneReferentialMode" key="user-preferences.tree-order.referentiel.label" />
        <f:message var="milestoneMilestoneMode" key="user-preferences.milestone" />

          <c:if test="${ milestoneFeatureEnabled }">
        	<comp:toggle-panel id="library-display-mode-panel" titleKey="user-preferences.tree-order.mode.title" open="true" >
			<jsp:attribute name="body">
				<div class="display-table">
				<div class="display-table-row">
					<div class="display-table-cell" style="vertical-align: bottom;">
					<c:choose>
				     <c:when  test= "${ milestoneList.size() != 0}">
					<label for="toggle-activation-checkbox" ><f:message key="user-preferences.tree-order.mode.label"/></label>
					</div>
					<div class="display-table-cell">
					<div class="display-table-cell">
                 		<input id="toggle-milestone-checkbox" type="checkbox"
                 	          data-def="width=35, on_label='${milestoneMilestoneMode}', off_label='${milestoneReferentialMode}'" style="display: none;"/>
                 		</div>
                 	</div>
        			</div>
        			<div class="display-table-row">
        				<div class="display-table-cell">
        				    <label for="choose-your-mode" ><f:message key="user-preferences.milestone"/></label>
        				</div>
        				<div id="labelchoose" class="customHeigth">
                            <c:if test="${not empty activeMilestone}">
                            <span id="toggle-milestone-label" >${activeMilestone.label}</span>
                            </c:if>
                            <c:if test="${empty activeMilestone}">
                             <span id="toggle-milestone-label" class="disabled-transparent"><f:message key="label.Choose"/></span>
                            </c:if>

        			     </div>

                <div class="bind-milestone-dialog popup-dialog not-displayed" title="${bindMilestoneDialogTitle}">
                  <div>

                    <table class="bind-milestone-dialog-table" data-def="filter, pre-sort=2-desc">
                      <thead>
                        <th data-def="sClass=bind-milestone-dialog-check, map=empty-delete-holder"></th>
                        <th data-def="map=label, sortable" ><f:message key="label.Label"/></th>
                        <th data-def="map=status, sortable"><f:message key="label.Status"/></th>
                        <th data-def="map=date, sortable"><f:message key="label.EndDate"/></th>
                        <th data-def="map=description, sortable" ><f:message key="label.Description"/></th>
                      </thead>
                      <tbody>

                      </tbody>
                    </table>

                    <div class="bind-milestone-dialog-selectors">
                      <ul style="list-style-type: none;">
                        <li class="clickable-item extra-small-margin-top"><span class="bind-milestone-dialog-selectall"    ><f:message key="label.selectAllForSelection"/></span></li>
                        <li class="clickable-item extra-small-margin-top"><span class="bind-milestone-dialog-selectnone"   ><f:message key="label.selectNoneForSelection"/></span></li>
                        <li class="clickable-item extra-small-margin-top"><span class="bind-milestone-dialog-invertselect" ><f:message key="label.invertSelect"/></span></li>
                      </ul>
                    </div>

                  </div>

                  <div class="popup-dialog-buttonpane" >
                    <input type="button" class="bind-milestone-dialog-confirm" data-def="evt=confirm, mainbtn" value="${confirmLabel}" />
                    <input type="button" class="bind-milestone-dialog-cancel" data-def="evt=cancel" value="${cancelLabel}" />
                  </div>

                </div>
        		</c:when>
			   <c:otherwise>
                   <f:message key="message.library-display-mode.no-milestones"/>
               </c:otherwise>
              </c:choose>
			</jsp:attribute>
			</comp:toggle-panel>
		 </c:if>

	</div>
    <c:if test="${ not authenticationProvider.managedPassword }">
	   <comp:user-account-password-popup/>
    </c:if>
	</jsp:attribute>
</layout:info-page-layout>

<script type="text/javascript">
  require(["common"], function() {
    require(["jquery", "projects-manager", "jquery.squash.fragmenttabs", "squash.attributeparser",
  	         "project/ProjectToolbar", "app/ws/squashtm.notification", "squash.translator",
  	         "user-account/milestones-preferences", "squashtable", "jquery.switchButton", "jquery.cookie"],
        function($, projectsManager, Frag, attrparser, ProjectToolbar, notification,
        		translator, milestonesPrefs){

        	  $("#project-permission-table").squashTable({
        		  'bServerSide' : false,
        		  'sDom' : '<r>t<i>',
        		  'sPaginationType' : 'full_numbers'
        	  },{});


          	  $(function() {
              		<c:if test="${ not milestoneList.isEmpty()}">
              		milestonesPrefs.init();
                    </c:if>

                    Frag.init();

             		new ProjectToolbar();

				});
  		});
  });
</script>
