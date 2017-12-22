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

<%@ page language="java" contentType="text/html; charset=utf-8"
  pageEncoding="utf-8"%>
<%------------------------------------- URLs et back button ----------------------------------------------%>
<c:url var="ckeConfigUrl" value="/styles/ckeditor/ckeditor-config.js" />
<s:url var="milestoneUrl" value="/milestones/{milestoneId}">
  <s:param name="milestoneId" value="${milestone.id}" />
</s:url>

<s:url var="deleteMilestoneUrl" value="/administration/milestones/{milestoneId}">
  <s:param name="milestoneId" value="${milestone.id}" />
</s:url>

<s:url var="milestonesUrl" value="/administration/milestones" />

<s:url var="projectsUrl" value="/milestones-binding/milestone/{milestoneId}/project">
  <s:param name="milestoneId" value="${milestone.id}" />
</s:url>


<s:url var="projectDetailBaseUrl" value="/administration/projects" />

<f:message var="confirmLabel" key="label.Confirm" />
<f:message var="confirmLabelDelete"   key="label.ConfirmDelete"/>
<f:message var="renameLabel" key="label.Rename" />
<f:message var="cancelLabel" key="label.Cancel" />
<f:message var="dateFormat" key="squashtm.dateformatShort" />
<f:formatDate value="${ milestone.endDate }" var="formatedEndDate" pattern="${dateFormat}"/>


<layout:info-page-layout titleKey="workspace.milestone.info.title" isSubPaged="true" >
  <jsp:attribute name="head">
    <comp:sq-css name="squash.grey.css" />
  </jsp:attribute>

  <jsp:attribute name="titlePane"><h2 class="admin"><f:message key="label.administration" /></h2></jsp:attribute>
  <jsp:attribute name="subPageTitle">
    <h2><f:message key="workspace.milestone.info.title" /></h2>
  </jsp:attribute>


  <jsp:attribute name="subPageButtons">
    <f:message var="backButtonLabel" key="label.Back" />
    <input type="button" class="button" id="back" value="${backButtonLabel}" "/>
  </jsp:attribute>
  <jsp:attribute name="informationContent">

    <div id="milestone-name-div"
      class="ui-widget-header ui-corner-all ui-state-default fragment-header">

      <div style="float: left; height: 3em">
        <h2>
          <label for="milestone-name-header"><f:message
              key="label.Milestone" />
          </label><a id="milestone-name-header" ><c:out
              value="${ milestone.label }" escapeXml="true" />
          </a>
        </h2>
      </div>
      <div class="unsnap"></div>

    </div>

      <div id="milestone-toolbar" class="toolbar-class ui-corner-all">
  <div class="snap-left">
<comp:general-information-panel auditableEntity="${milestone}"  />
        </div>
  </div>


    <div class="fragment-body">
      <%------------------------------------------------ BODY -----------------------------------------------%>

      <div id="milestone-toolbar" classes="toolbar-class ui-corner-all">
        <%--- Toolbar ---------------------%>

      <div class="toolbar-button-panel">
        <f:message var="rename" key="rename" />
      <c:if test="${canEdit }">
        <input type="button" value="${ rename }" id="rename-milestone-button"
              class="sq-btn" title="<f:message key="milestone.button.rename.label" />"/>

  <f:message var="delete" key='button.delete.label' />
         <input type="button" value="${ delete }" id="delete-milestone-button" class="sq-btn" title="<f:message key='milestone.button.delete.label' />"/>
              </c:if>
      </div>
      </div>
      <%--------End Toolbar ---------------%>

      <%----------------------------------- INFORMATION PANEL -----------------------------------------------%>
      <br />
      <br />
      <comp:toggle-panel id="milestone-info-panel"
        titleKey="label.MilestoneInformations" open="true">

        <jsp:attribute name="body">
          <div id="milestone-description-table" class="display-table">

            <div class="display-table-row">
              <label for="milestone-end-date" class="display-table-cell">
              <f:message key="label.EndDate" />
              </label>
              <div class="display-table-cell" ><span id="milestone-end-date" >${formatedEndDate}</span></div>
            </div>

          <div class="display-table-row">
              <label for="milestone-status" class="display-table-cell">
              <f:message key="label.Status" />
              </label>
              <div class="display-table-cell" ><span id="milestone-status" >  ${ milestoneStatusLabel } </span></div>
            </div>

                <div class="display-table-row">
              <label for="milestone-range" class="display-table-cell">
              <f:message key="label.Range" />
              </label>
              <div class="display-table-cell" >
              <c:choose>
              <c:when test="${isAdmin}">
              <span id="milestone-range" >  ${ milestoneRangeLabel } </span>
              </c:when>
              <c:otherwise>
              ${ milestoneRangeLabel }
              </c:otherwise>
              </c:choose>
              </div>
            </div>

                <div class="display-table-row">
              <label for="milestone-owner" class="display-table-cell">
              <f:message key="label.Owner" />
              </label>

              <div class="display-table-cell" id="milestone-owner-cell">
                  <c:choose>
            <c:when test="${ milestone.range == 'GLOBAL'}">
                  <f:message  key="label.milestone.global.owner" />
              </c:when>
                 <c:otherwise>
                <span id="milestone-owner" >${ milestone.owner.name } </span>
                    </c:otherwise>
            </c:choose>
            </div>

            </div>






            <div class="display-table-row">
              <label for="milestone-description" class="display-table-cell">
              <f:message key="label.Description" />
              </label>
              <c:choose>
                  <c:when test="${ canEdit }">
              <div class="display-table-cell editable rich-editable" data-def="url=${milestoneUrl}" id="milestone-description">${ milestone.description }</div>
                 </c:when>
             <c:otherwise>
            <div class="display-table-cell">  ${ milestone.description } </div>
                          </c:otherwise>
            </c:choose>
            </div>

          </div>
        </jsp:attribute>
      </comp:toggle-panel>


      <%-----------------------------------END INFORMATION PANEL -----------------------------------------------%>
    <comp:toggle-panel id="milestone-project-panel"
        titleKey="label.projects" open="true">

    <jsp:attribute name="panelButtons">
      <c:if test="${canEdit }">
        <button id="bind-project-button" title="<f:message key="label.milestone.bindProject" />" class="sq-icon-btn btn-sm">
          <span class="ui-icon ui-icon-plus squared-icons">+</span>
        </button>
             <button id="unbind-project-button" title="<f:message key="label.milestone.unbindProject" />" class="sq-icon-btn btn-sm">
          <span class="ui-icon ui-icon-minus squared-icons">-</span>
          </c:if>
        </button>

        </jsp:attribute>
        <jsp:attribute name="body">

        <table id="projects-table" class="unstyled-table" data-def="ajaxsource=${projectsUrl}?binded, hover, filter, pre-sort=2-asc"
          data-project-url="${projectDetailBaseUrl}/{{entity-id}}/info">
    <thead>
      <tr>
        <th data-def="map=entity-id, invisible"> </th>
        <th data-def="map=entity-index, select">#</th>
        <th data-def="map=name, sortable"  class="datatable-filterable"><f:message key="label.project" /></th>
       <th data-def="map=binded, sortable, sClass=binded-to-project" class="datatable-filterable"><f:message key="label.project.isBoundToMilestone" /></th>
        <th data-def="map=isUsed, sortable"><f:message key="label.used" /></th>
        <th data-def="map=empty-delete-holder, unbind-button=#unbind-project-popup"></th>
        <th data-def="map=link, invisible"></th>
      </tr>
    </thead>
    <tbody><%-- Will be populated through ajax --%></tbody>
  </table>

        </jsp:attribute>
      </comp:toggle-panel>

      <%-----------------------------------START PROJECT PANEL -----------------------------------------------%>




    <%-----------------------------------END PROJECT PANEL -----------------------------------------------%>

      </div>

    <%---------------------------------------------------------------END  BODY -----------------------------------------------%>
  </jsp:attribute>
</layout:info-page-layout>




<!-- --------------------------------BIND PROJECT POPUP--------------------------------------------------------- -->


    <f:message var="bindProjectTitle" key="dialog.milestone.bind.project" />
    <div id="bind-project-dialog" class="not-displayed popup-dialog"
        title="${bindProjectTitle}">

  <table id="bind-to-projects-table" class="unstyled-table" data-def="ajaxsource=${projectsUrl}?bindable, hover, filter, pre-sort=1-asc">
    <thead>
      <tr>
        <th data-def="map=checkbox, searchable=false, checkbox"></th>
        <th data-def="map=raw-type, searchable=false, invisible">raw type (not shown)</th>
        <th data-def="map=type, searchable=false, sClass=icon-cell type">&nbsp;</th>
        <th data-def="map=name, sortable"  class="datatable-filterable"><f:message key="label.Name" /></th>
        <th data-def="map=label, sortable"><f:message key="label.tag"/></th>
      </tr>
    </thead>
    <tbody><%-- Will be populated through ajax --%></tbody>
  </table>

<div>
<ul>

<li><a class="clickable-item" id="checkAll"> <f:message key="label.selectAllForSelection" /></a></li>
<li><a class="clickable-item" id="uncheckAll"><f:message key="label.selectNoneForSelection" /></a></li>
<li><a class="clickable-item" id="invertSelect"><f:message key="label.InvertSelection" /></a></li>

</ul>
</div>

        <div class="popup-dialog-buttonpane">
          <input type="button" value="${confirmLabel}" data-def="mainbtn, evt=confirm"/>
          <input type="button" value="${cancelLabel}" data-def="evt=cancel"/>
        </div>
    </div>



<!-- ------------------------------------END BIND PROJECT POPUP------------------------------------------------------- -->


<!-- ------------------------------------UNBIND PROJECT POPUP------------------------------------------------------- -->
  <f:message var="unbindProjectTitle" key="dialog.milestone.unbind.project.title" />
  <f:message var="warningUnbind" key="dialog.milestone.unbind.milestone.warning.single" />
  <div id="unbind-project-popup" class="popup-dialog not-displayed" title="${unbindProjectTitle}">

        <comp:notification-pane type="error" txtcontent="${warningUnbind}"/>

    <div class="popup-dialog-buttonpane">
        <input class="confirm" type="button" value="${confirmLabel}" />
        <input class="cancel" type="button" value="${cancelLabel}" />
    </div>

  </div>

<!-- ------------------------------------END UNBIND PROJECT POPUP------------------------------------------------------- -->


<!-- ------------------------------------UNBIND PROJECT BUT KEEP IN PERIMETER POPUP------------------------------------------------------- -->
  <f:message var="unbindProjectTitle" key="dialog.milestone.unbind.project.title" />
  <f:message var="warningUnbind" key="dialog.milestone.unbind.project.warning" />
  <div id="unbind-project-but-keep-in-perimeter-popup" class="popup-dialog not-displayed" title="${unbindProjectTitle}">

        <comp:notification-pane type="error" txtcontent="${warningUnbind}"/>

    <div class="popup-dialog-buttonpane">
        <input class="confirm" type="button" value="${confirmLabel}" />
        <input class="cancel" type="button" value="${cancelLabel}" />
    </div>

  </div>

<!-- ------------------------------------END UNBIND PROJECT BUT KEEP IN PERIMETER  POPUP------------------------------------------------------- -->


<!-- ------------------------------------CHANGE RANGE WITH TEMPLATE POPUP------------------------------------------------------- -->
  <f:message var="changeRangeTitle" key="dialog.milestone.changerange.title" />
  <f:message var="warningChangeRange" key="dialog.milestone.changerange.warning" />
  <div id="changeRange-popup" class="popup-dialog not-displayed" title="${changeRangeTitle}">

        <comp:notification-pane type="error" txtcontent="${warningChangeRange}"/>

    <div class="popup-dialog-buttonpane">
        <input class="confirm" type="button" value="${confirmLabel}" />
        <input class="cancel" type="button" value="${cancelLabel}" />
    </div>

  </div>

<!-- ------------------------------------END CHANGE RANGE WITH TEMPLATE POPUP------------------------------------------------------- -->


<!-- ------------------------------------CHANGE STATUS POPUP------------------------------------------------------- -->
  <f:message var="changeStatusTitle" key="dialog.milestone.changestatus.title" />
  <f:message var="warningChangeStatus" key="dialog.milestone.changestatus.warning" />
  <div id="changeStatus-popup" class="popup-dialog not-displayed" title="${changeStatusTitle}">

         <comp:notification-pane type="error" txtcontent="${warningChangeStatus}"/>

    <div class="popup-dialog-buttonpane">
        <input class="confirm" type="button" value="${confirmLabel}" />
        <input class="cancel" type="button" value="${cancelLabel}" />
    </div>

  </div>

<!-- ------------------------------------END CHANGE STATUS POPUP------------------------------------------------------- -->

<!-- ------------------------------------CHANGE OWNER POPUP------------------------------------------------------- -->
  <f:message var="changeOwnerTitle" key="dialog.milestone.changeower.title" />
  <f:message var="warningChangeOwner" key="dialog.milestone.changeowner.warning" />
  <div id="changeOwner-popup" class="popup-dialog not-displayed" title="${changeOwnerTitle}">

         <comp:notification-pane type="error" txtcontent="${warningChangeOwner}"/>

    <div class="popup-dialog-buttonpane">
        <input class="confirm" type="button" value="${confirmLabel}" />
        <input class="cancel" type="button" value="${cancelLabel}" />
    </div>

  </div>


<!-- ------------------------------------END CHANGE OWNER POPUP------------------------------------------------------- -->


<!-- --------------------------------RENAME POPUP--------------------------------------------------------- -->

        <f:message var="renameMilestoneTitle" key="dialog.rename-milestone.title" />
      <div id="rename-milestone-dialog" class="not-displayed popup-dialog"
        title="${renameMilestoneTitle}">

         <tr>
            <td><label for="rename-milestone-input"><f:message
              key="label.Label" /></label></td>
            <td><input id="rename-milestone-input" type="text" size="30" maxlength="30"/>
            <comp:error-message forField="label" /></td>
          </tr>

        <div class="popup-dialog-buttonpane">
          <input type="button" value="${confirmLabel}" data-def="mainbtn, evt=confirm"/>
          <input type="button" value="${cancelLabel}" data-def="evt=cancel"/>
        </div>
    </div>



<!-- ------------------------------------END RENAME POPUP------------------------------------------------------- -->


<f:message var="deleteMilestoneTitle" key="dialog.delete-milestone.title" />
  <f:message var="warningDelete" key="dialog.delete-milestone.message" />
  <div id="delete-milestone-popup" class="popup-dialog not-displayed" title="${deleteMilestoneTitle}">

    <div class="display-table-row">
            <div class="display-table-cell warning-cell">
                <div class="generic-error-signal"></div>
            </div>
            <div id="warning-delete" class="display-table-cell">
            </div>
      <div class="display-table-cell">
         <p>
                <span id="errorMessageDeleteMilestone">    </span>
              </p>
          </div>
    </div>

    <div class="popup-dialog-buttonpane">
        <input class="confirm" type="button" value="${confirmLabel}" />
        <input class="cancel" type="button" value="${cancelLabel}" />
    </div>




<script type="text/javascript">


requirejs.config({
  config : {
    'milestone-manager/milestone-info' : {
      urls: {
        milestonesUrl : "${milestonesUrl}",
        milestoneUrl :  "${milestoneUrl}",
        deleteMilestoneUrl: "${deleteMilestoneUrl}"
            },
      data: {
        canEdit : ${canEdit},
        currentUser : "${currentUser}",
        isAdmin : ${isAdmin},
        userList : ${userList},
        milestone : {
          currentStatus : '${milestone.status}',
          status : '${milestoneStatus}',
          id: '${milestone.id}',
          currentRange: '${ milestone.range}',
          range: '${milestoneRange}'
                    }

             }
                   }
      }
    });

require(["common"], function(){
  require(["milestone-manager/milestone-info"], function(){});
});


</script>