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

<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="teams" tagdir="/WEB-INF/tags/teams-components" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="json" uri="http://org.squashtest.tm/taglib/json" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<s:url var="rootContext" value="/" />
<s:url var="baseUrl" value="/administration/users"/>
<s:url var="userInfoUrl" value="/administration/users/{user-id}/info" />
<s:url var="tableModelUrl" value="/administration/users/table" />

<c:set var="tableModel"     value="${json:serialize(userList)}"/>
<c:set var="tableModelLength"   value="${fn:length(tableModel)}" />


<f:message var="addAnotherLabel" key="label.addAnother" />
<f:message var="addLabel" key="label.Add" />
<f:message var="cancelLabel" key="label.Close"/>

<f:message var="missingNewPassword" key="user.account.newpass.error"/>
<f:message var="missingConfirmPassword" key="user.account.confirmpass.error"/>
<f:message var="differentConfirmation" key="user.account.newpass.differ.error"/>
<f:message var="okLabel" key="label.Confirm"/>


<layout:info-page-layout titleKey="workspace.user.title" isSubPaged="true" main="user-manager" >
  <jsp:attribute  name="head">
    <comp:sq-css name="squash.grey.css" />
    <script type="text/javascript">
    //<![CDATA[
      squashtm.app.teamsManager = {
        table : {
          deleteButtons : {
            popupmessage : "<div class='display-table-row'><div class='display-table-cell warning-cell'><div class='generic-error-signal'></div></div><div class='display-table-cell'><f:message key='message.team.remove.first'/><span class='red-warning-message'> <f:message key='message.team.remove.second'/> </span><f:message key='message.team.remove.third'/><span class='bold-warning-message'> <f:message key='message.team.remove.fourth'/> </span></div></div>",
            tooltip : "<f:message key='label.deleteTeam' />"
          }
        },
      settings : {
          data : {
            tableData : ${tableModel}
          },
          urls : {
            rootContext : "${rootContext}",
            baseUrl : "${baseUrl}"
          },
          language : {
            missingNewPassword : "${missingNewPassword}",
            missingConfirmPassword : "${missingConfirmPassword}",
            differentConfirmation : "${differentConfirmation}",
            ok : "${okLabel}",
            cancel :"${cancelLabel}"
          },
          managedPassword: ${ authenticationProvider.managedPassword }
        }
      }
    //]]>
    </script>
  </jsp:attribute>

  <jsp:attribute name="titlePane">
    <h2 class="admin"><f:message key="label.administration" /></h2>
  </jsp:attribute>
  <jsp:attribute name="subPageTitle">
    <h2><f:message key="workspace.user.title" /></h2>
  </jsp:attribute>

  <jsp:attribute name="subPageButtons">
    <a id="back" type="button" href="<s:url value='/administration' />" class="sq-btn"><f:message key="label.Back" /></a>&nbsp;
  </jsp:attribute>
  <jsp:attribute name="informationContent">
      <div class="fragment-tabs fragment-body">
      <ul class="tab-menu">
        <li><a href="#users-table-pane"><f:message key="label.users"/></a></li>
        <li><a href="#team-table-pane"><f:message key="label.teams"/></a></li>
      </ul>
      <div id="users-table-pane" class="table-tab">
        <!-- z-index fix needed for button pane because it would be partially shadowed by the table filter input otherwise -->
        <div class="btn-toolbar right" style="z-index:1;">
          <button id="add-user-button" class="sq-icon-btn btn-sm"
            title="<f:message key="user.add.label" />" >
            <span class="ui-icon ui-icon-plusthick">+</span> <span class="ui-button-text"><f:message key="label.Add" />&nbsp;</span>
          </button>
          <button id="activate-user-button" class="sq-icon-btn btn-sm">
            <span class="ui-icon ui-icon-none"></span>
            <f:message key="label.activate-users" />
          </button>
          <button id="deactivate-user-button" class="sq-icon-btn btn-sm">
            <span class="ui-icon ui-icon-none"></span>
            <f:message key="label.deactivate-users" />
          </button>
          <button type="button" id="delete-user-button" href="#" class="sq-icon-btn btn-sm"
            title="<f:message key="label.delete-users" />" >
            <span class="ui-icon ui-icon-trash"></span>
            <span class="ui-button-text"><f:message key="label.Delete" />&nbsp;</span>
          </button>
        </div>
        <div class="table-tab-wrap">
          <table id="users-list-table"  class="unstyled-table"
              data-def="ajaxsource=${tableModelUrl}, datakeys-id=user-id, deferLoading=${tableModelLength},
              pagesize=50, filter, pre-sort=2-asc">
            <thead>
              <tr>
                <th class="user-index"               data-def="map=user-index, select">#</th>
                <th class="user-active"              data-def="map=user-active, narrow, center, sClass=user-active-cell"><f:message key="label.active"/></th>
                <th class="user-login datatable-filterable"   data-def="map=user-login, sortable, link=${userInfoUrl}"><f:message key="label.Login" /></th>
                <th class="user-group"               data-def="map=user-group, sortable"><f:message key="label.Group" /></th>
                <th class="user-firstname datatable-filterable" data-def="map=user-firstname, sortable"><f:message key="label.FirstName" /></th>
                <th class="user-lastname datatable-filterable"  data-def="map=user-lastname, sortable"><f:message key="label.LastName" /></th>
                <th class="user-email datatable-filterable"    data-def="map=user-email, sortable"><f:message key="label.Email" /></th>
                <th class="user-created-on"            data-def="map=user-created-on, sortable"><f:message key="label.CreatedOn" /></th>
                <th class="user-created-by datatable-filterable"data-def="map=user-created-by, sortable"><f:message key="label.createdBy" /></th>
                <th class="user-modified-on"          data-def="map=user-modified-on, sortable"><f:message key="label.modifiedOn" /></th>
                <th class="user-modified-by datatable-filterable"data-def="map=user-modified-by, sortable"><f:message key="label.modifiedBy" /></th>
                <th class="user-connected-on"          data-def="map=user-connected-on, sortable"><f:message key="label.connectedOn" /></th>
                <th class="empty-delete-holder"         data-def="map=empty-delete-holder, delete-button=#delete-user-dialog"></th>
              </tr>
            </thead>
            <tbody>
              <%-- Will be populated through ajax --%>
            </tbody>
          </table>

        </div>

      </div><%-- /div#users-table-pane --%>

      <teams:teams-table-tab />


  </div><%-- /div.fragment-body.fragment-tabs --%>


  <%-- ------------------------------ Add User Dialog ------------------------------------------------ --%>

        <f:message var="adduserTitle" key="title.AddUser"/>
        <div id="add-user-dialog" class="popup-dialog not-displayed" title="${adduserTitle}">

        <table id="add-user-table">
          <tr>
            <td><label  for="add-user-login"><f:message key="label.Login" /></label></td>
            <td><input type="text" id="add-user-login" size="30" data-def="maininput"/></td>
          </tr>
          <tr><td> <comp:error-message forField="user-login" /> </td>
          </tr>
          <tr>
            <td><label  for="add-user-firstName"><f:message key="label.FirstName" /></label></td>
            <td><input type="text" id="add-user-firstName" size="30"/></td>
          </tr>
          <tr>
            <td><comp:error-message forField="user-firstName" /></td>
          </tr>
          <tr>
            <td><label  for="add-user-lastName"><f:message key="label.LastName" /></label></td>
            <td><input type="text" id="add-user-lastName" size="30"/></td>
          </tr>
          <tr>
            <td><comp:error-message forField="user-lastName" /></td>
          </tr>
          <tr>
            <td><label  for="add-user-email"><f:message key="label.Email" /></label></td>
            <td><input type="email" id="add-user-email" size="30"/></td>
          </tr>
          <tr>
            <td><comp:error-message forField="user-email" /></td>
          </tr>
          <tr>
            <td><label  for="add-user-group"><f:message key="label.Group" /></label></td>
            <td>
            <select id="add-user-group">
              <c:forEach var="group" items="${ usersGroupList }">
                <c:choose>
                  <c:when test="${ group.simpleName == 'User' }">
                    <option id="${group.qualifiedName}" value="${group.id}" selected="selected"><f:message key="user.account.group.${group.qualifiedName}.label" /></option>
                  </c:when>
                  <c:otherwise>
                    <option id="${group.qualifiedName}" value="${group.id}"><f:message key="user.account.group.${group.qualifiedName}.label" /></option>
                  </c:otherwise>
                </c:choose>
              </c:forEach>
            </select>
            </td>
          </tr>
<c:if test="${ not authenticationProvider.managedPassword }">
                <tr>
                  <td> <label for="add-user-password"><f:message key="user.account.newpass.label" /></label> </td>
                  <td><input type="password" id="add-user-password" size="30" /></td>
                </tr>
                <tr>
                  <td><comp:error-message forField="password" /></td>
                </tr>
                <tr>
                  <td><label for="new-user-confirmpass"><f:message key="user.account.confirmpass.label"/></label></td>
                  <td><input type="password" id="new-user-confirmpass" size="30"/> </td>
                </tr>
                <tr>
                  <td><comp:error-message forField="confirmpass" /></td>
                </tr>
</c:if>
 <c:if test="${ authenticationProvider.managedPassword }">
                <tr>
                  <td><label><f:message key="label.password" /></label></td>
                  <td><span><f:message key="message.managedPassword" /></span></td>
                </tr>
</c:if>
        </table>


          <div class="popup-dialog-buttonpane">
      <input type="button" value="${addAnotherLabel}" data-def="mainbtn, evt=addanother"/>
            <input type="button" value="${addLabel}" data-def="mainbtn, evt=confirm"/>
            <input type="button" value="${cancelLabel}" data-def="evt=cancel" />
          </div>
        </div>



    <f:message var="deleteUsersTitle" key="title.delete-users" />
    <div id="delete-user-dialog" class="popup-dialog not-displayed" title="${deleteUsersTitle}">

            <comp:notification-pane type="error">
              <jsp:attribute name="htmlcontent">
                <div class="display-table-cell">
          <span> <f:message key="message.user.remove"/></span>
        </div>
              </jsp:attribute>
            </comp:notification-pane>

      <div class="popup-dialog-buttonpane">
          <input class="confirm" type="button" value="<f:message key='label.Confirm' />" />
          <input class="cancel" type="button" value="<f:message key='label.Cancel' />" />
      </div>

    </div>
  </jsp:attribute>
</layout:info-page-layout>
