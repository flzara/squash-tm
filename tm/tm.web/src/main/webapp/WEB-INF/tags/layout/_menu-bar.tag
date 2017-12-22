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
<%@ tag description="the main menu bar, the one displayed on the top right"%>


<%@ taglib tagdir="/WEB-INF/tags/component" prefix="comp"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://org.squashtest.tm/taglib/workspace-utils" prefix="wu"%>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec"%>

<c:url var="projectFilterStatusUrl" value="/global-filter/filter-status" />
<c:url var="administrationUrl" value="/administration" />
<c:url var="userAccountUrl" value="/user-account" />
<c:url var="filterUrl" value="/global-filter/filter" />

<c:set var="filter" value="${wu:getProjectFilter(pageContext.servletContext)}" />
<c:set var="filterCheckedClause" value="${filter.enabled ? 'checked=\"checked\"' : ''}" />
<c:set var="filterLabelClass" value="${filter.enabled ? 'filter-enabled' : '' }" />

<f:message var="filterLabelText">${filter.enabled ? 'workspace.menubar.filter.enabled.label' : 'workspace.menubar.filter.disabled.label' }</f:message>
<f:message var="filterPopupTitle" key="dialog.settings.filter.title" />
<f:message var="confirmLabel" key="label.Confirm" />
<f:message var="cancelLabel" key="label.Cancel" />


	<div style="display: inline-flex;">
 	 <input type="checkbox" id="menu-toggle-filter-ckbox" ${filterCheckedClause}></input>
  	 <div class="icon iconmargin menubarmargintop"><span class="ui-icon  ui-icon-volume-off rotateright"></span></div>
 	 <a id="menu-project-filter-link" style=" margin-top: 3px" href="#" class="${filterLabelClass}">${filterLabelText}</a>
	</div>
		
<sec:authorize access="hasRole('ROLE_TM_PROJECT_MANAGER') or hasRole('ROLE_ADMIN')">
  <div>
    <div class="icon"><span class="ui-icon ui-icon-wrench"></span></div>
    <a id="menu-administration-link" href="${ administrationUrl }">
      <f:message key="workspace.menubar.administration.label" />
    </a>
  </div>
</sec:authorize>

<div>
  <div class="icon"><span class="ui-icon ui-icon-person"></span></div>
  <a id="menu-account-link" href="${userAccountUrl}">
    <f:message key="workspace.menubar.account.label" />
    &nbsp;(
    <c:out value="${sessionScope['SPRING_SECURITY_CONTEXT'].authentication.name}" />
    )
  </a>
</div>

<sec:authorize access="isAuthenticated()">

  <c:url var="logoutUrl" value="/logout" />
</sec:authorize>
<div>
  <div class="icon"><span class="ui-icon ui-icon-power"></span></div>
  <a id="menu-logout-link" href="${ logoutUrl }">
    <f:message key="workspace.menubar.logout.label" />
  </a>
</div>


<%-- ====== project filter popup ========  --%>

<div id="project-filter-popup" class="project-picker popup-dialog not-displayed" style="display: none"
  title="${filterPopupTitle}" data-url="${filterUrl}">
  <div id="dialog-settings-filter-maincontent">
    <div id="dialog-settings-filter-projectlist" class="project-filter-list dataTables_wrapper">
     <span class="filter-warning not-displayed" ><f:message key="message.projectPicker.warnFilterOn"/></span>
         
      <table>
        <thead>
          <tr>
            <th width="25px" class="th-check ui-state-default"></th>
            <th class="th-name ui-state-default">
              <f:message key="label.Name" />
            </th>
            <th class="th-type ui-state-default">
              <f:message key="label.tag" />
            </th>
          </tr>
        </thead>
        <tbody class="available-fields">
          <c:forEach var="item" items="${filter.projectData}" varStatus="status">
            <c:set var="checkedClause" value="${item[2] ? 'checked=\"checked\"' : ''}" />
            <tr>
              <td class="td-check" data-id="5">
                <input type="checkbox" class="project-checkbox" id="project-checkbox-${item[0]}" value="${item[0]}"
                  data-previous-checked="${item[2]}" ${checkedClause} />
              </td>
              <td class="project-name">${item[1]}</td>
              <td class="project-label">${item[3]}</td>
            </tr>
          </c:forEach>
        </tbody>
      </table>
    </div>
    <div id="dialog-settings-filter-controls" class="project-filter-controls">
      <ul>
        <li id="dialog-settings-filter-selectall" class="project-picker-selall cursor-pointer">
          <f:message key="dialog.settings.filter.controls.selectall" />
        </li>
        <li id="dialog-settings-filter-deselectall" class="project-picker-deselall cursor-pointer">
          <f:message key="dialog.settings.filter.controls.deselectall" />
        </li>
        <li id="dialog-settings-filter-invertselect" class="project-picker-invsel cursor-pointer">
          <f:message key="dialog.settings.filter.controls.invertselect" />
        </li>
      </ul>
    </div>
    <div class="unsnap not-displayed"></div>
  </div>
  <div class="popup-dialog-buttonpane">
    <input type="button" value="${confirmLabel}" />
    <input type="button" value="${cancelLabel}" />
  </div>
</div>
<script type="text/javascript">
	publish("load.projectFilter");
</script>
