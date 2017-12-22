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
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"  %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>

<s:url var="administrationUrl" value="/administration" />

<f:message var="addAnotherLabel"       key="label.addAnother"/>
<f:message var="addLabel"       key="label.Add"/>
<f:message var="confirmLabel"   key="label.Confirm"/>
<f:message var="cancelLabel"    key="label.Cancel"/>
<f:message var="closeLabel"    key="label.Close"/>

<layout:info-page-layout titleKey="squashtm.bugtrackers.title" isSubPaged="true" main="bugtracker-manager/bugtracker-manager.js">
	<jsp:attribute  name="head">	
		<comp:sq-css name="squash.grey.css" />
		
	</jsp:attribute>
	
	<jsp:attribute name="titlePane">
		<h2 class="admin"><f:message key="label.administration" /></h2>
	</jsp:attribute>
		<jsp:attribute name="subPageTitle">
		<h2><f:message key="workspace.bugtracker.title" /></h2>
	</jsp:attribute>
	
	<jsp:attribute name="subPageButtons">
		<f:message var="backButtonLabel" key="label.Back" />
		<input type="button" class="sq-btn" value="${backButtonLabel}" onClick="document.location.href= '${administrationUrl}'"/>	
	</jsp:attribute>
	<jsp:attribute name="informationContent">
		<c:url var="bugtrackersUrl" value="/administration/bugtrackers/list" />
		<c:url var="addBugtrackerUrl" value="/administration/bugtrackers" />
		<c:url var="bugtrackerDetailsBaseUrl" value="/bugtracker" />
		<c:url var="dtMessagesUrl" value="/datatables/messages" />

		
		<%----------------------------------- BugTracker Table -----------------------------------------------%>


<div class="fragment-body">

   <div class="toolbar">
   	
	<button class="snap-right sq-btn" type="button" title="<f:message key="label.deleteBugtracker" />"  id="delete-bugtracker-button"/>
	<span class="ui-icon ui-icon-trash">-</span><f:message key='label.Delete' />
	<button class="snap-right sq-btn" type="button" title="<f:message key='label.AddBugtracker' />"  id="new-bugtracker-button"/>
	<span class="ui-icon ui-icon-plusthick">+</span><f:message key='label.Add' />
	</button>

	</button>
  </div>

		
	<div style="clear:both"></div>
	
	<table id="bugtrackers-table" class="unstyled-table" data-def="ajaxsource=${bugtrackersUrl}, hover, pre-sort=1-asc">
		<thead>
			<tr>
				<th data-def="map=index, select">#</th>
				<th data-def="map=name, sortable, link=${bugtrackerDetailsBaseUrl}/{entity-id}/info"><f:message key="label.Name" /></th>
				<th data-def="map=kind, sortable"><f:message key="label.Kind" /></th>
				<th data-def="map=url, sortable, link-new-tab={url}"><f:message key="label.Url" /></th>
				<th data-def="map=iframe-friendly"><f:message key="label.lower.iframe" /></th>
				<th data-def="map=delete, delete-button=#delete-bugtracker-popup"></th>
			</tr>
		</thead>
		<tbody><%-- Will be populated through ajax --%></tbody>
	</table>



	<f:message var="deleteBugtrackerTitle" key="dialog.delete-bugtracker.title" />
	<f:message var="warningDelete" key="dialog.deleteBugTracker.warning" />
	<div id="delete-bugtracker-popup" class="popup-dialog not-displayed" title="${deleteBugtrackerTitle}">

        <comp:notification-pane type="error" txtcontent="${warningDelete}"/>


		<div class="popup-dialog-buttonpane">
		    <input class="confirm" type="button" value="${confirmLabel}" />
		    <input class="cancel" type="button" value="${cancelLabel}" />				
		</div>
	
	</div>	

    <f:message var="addBugtrackerTitle" key="dialog.new-bugtracker.title"/>
    <div id="add-bugtracker-dialog" class="not-displayed popup-dialog" 
          title="${addBugtrackerTitle}" />
          
        <table>
          <tr>
            <td><label for="add-bugtracker-name"><f:message
              key="label.Name" /></label></td>
            <td><input id="add-bugtracker-name" type="text" size="50" data-def="maininput"/>
            <comp:error-message forField="name" /></td>
          </tr>
          <tr>
            <td><label for="add-bugtracker-kind"><f:message
              key="label.Kind" /></label></td>
            <td><select id="add-bugtracker-kind" class="combobox">
            <c:forEach items="${ bugtrackerKinds }" var="status" > 
            <option value = "${status}" >${status}</option>
            </c:forEach>
            </select>
            
            <comp:error-message forField="kind" /></td>
          </tr>
          <tr>
            <td><label for="add-bugtracker-url"><f:message
              key="dialog.new-bugtracker.url.label" /></label></td>
            <td><input id="add-bugtracker-url" type="text" size="50"/>
            <comp:error-message forField="url" /></td>
          </tr>
          <tr>
            <td><label for="add-bugtracker-iframeFriendly"><f:message
              key="label.DisplaysInIframe" /></label></td>
            <td><input id="add-bugtracker-iframeFriendly" type="checkbox" />
            <comp:error-message forField="iframeFriendly" /></td>
          </tr>
        </table>

     
      <div class="popup-dialog-buttonpane">
     	 <input type="button" value="${addAnotherLabel}" data-def="mainbtn, evt=addanother"/>
        <input type="button" value="${addLabel}" data-def="mainbtn, evt=confirm"/>
        <input type="button" value="${closeLabel}" data-def="evt=cancel"/>
      </div>
          
</div>


</jsp:attribute>
</layout:info-page-layout>