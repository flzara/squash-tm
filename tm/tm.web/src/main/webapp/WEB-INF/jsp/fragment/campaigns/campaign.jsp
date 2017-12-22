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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="camp" tagdir="/WEB-INF/tags/campaigns-components"%>
<%@ taglib prefix="authz" tagdir="/WEB-INF/tags/authz"%>
<%@ taglib prefix="at" tagdir="/WEB-INF/tags/attachments"%>
<%@ taglib prefix="csst" uri="http://org.squashtest.tm/taglib/css-transform"%>
<%@ taglib prefix="dashboard" tagdir="/WEB-INF/tags/dashboard"%>
<%@ taglib prefix="json" uri="http://org.squashtest.tm/taglib/json"%>
<%@ taglib prefix="issues" tagdir="/WEB-INF/tags/issues"%>

<f:message var="squashlocale" key="squashtm.locale" />
<f:message var="iterationPlanningTitle" key="campaigns.planning.iterations.scheduled_dates" />
<f:message var="iterationPlanningButton" key="campaigns.planning.iterations.button" />
<f:message var="buttonOK" key="label.Ok" />
<f:message var="buttonCancel" key="label.Cancel" />
<f:message var="dateformat" key="squashtm.dateformatShort" />

<comp:datepicker-manager locale="${squashlocale}" />

<c:url var="ckeConfigUrl" value="/styles/ckeditor/ckeditor-config.js" />
<c:url var="campaignUrl" value="/campaigns/${campaign.id}" />
<c:url var="campaignInfoUrl" value="/campaigns/${campaign.id}/general" />
<c:url var="campaignPlanningUrl" value="/campaigns/${campaign.id}/planning" />
<c:url var="campaignStatisticsUrl" value="/campaigns/${campaign.id}/dashboard-statistics" />
<c:url var="campaignStatisticsPrintUrl" value="/campaigns/${campaign.id}/dashboard?printmode=true" />
<c:url var="campaignInfoStatisticsUrl" value="/campaigns/${campaign.id}/statistics" />


<c:url var="workspaceUrl" value="/campaign-workspace/#" />
<c:url var="btEntityUrl" value="/bugtracker/campaign/${campaign.id}" />
<c:url var="customFieldsValuesURL" value="/custom-fields/values" />

<%-- ----------------------------------- Authorization ----------------------------------------------%>
<c:set var="writable" value="${ false }" />
<c:set var="attachable" value="${ false }" />
<c:set var="deletable" value="${false }" />
<c:set var="creatable" value="${false }" />
<c:set var="linkable" value="${ false }" />
<c:set var="moreThanReadOnly" value="${ false }" />


<c:if test="${not milestoneConf.locked}">

<authz:authorized hasRole="ROLE_ADMIN" hasPermission="WRITE" domainObject="${ campaign }">
  <c:set var="writable" value="${ true }" />
  <c:set var="moreThanReadOnly" value="${ true }" />
</authz:authorized>
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="ATTACH" domainObject="${ campaign }">
  <c:set var="attachable" value="${ true }" />
  <c:set var="moreThanReadOnly" value="${ true }" />
</authz:authorized>
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="DELETE" domainObject="${ campaign }">
  <c:set var="deletable" value="${true }" />
  <c:set var="moreThanReadOnly" value="${ true }" />
</authz:authorized>
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="CREATE" domainObject="${ campaign }">
  <c:set var="creatable" value="${true }" />
  <c:set var="moreThanReadOnly" value="${ true }" />
</authz:authorized>
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="LINK" domainObject="${ campaign }">
  <c:set var="linkable" value="${ true }" />
  <c:set var="moreThanReadOnly" value="${ true }" />
</authz:authorized>

</c:if>

<f:message var="okLabel" key="label.Ok" />
<f:message var="confirmLabel" key="label.Confirm" />
<f:message var="cancelLabel" key="label.Cancel" />

<div class="ui-widget-header ui-state-default ui-corner-all fragment-header">
  <c:if test="${ not param.isInfoPage }">
    <div id="right-frame-button">
		<f:message var="toggleLibraryTooltip" key="tooltip.toggleLibraryDisplay" />
		<input type="button" class="sq-btn btn-sm" id="toggle-expand-left-frame-button" title="${toggleLibraryTooltip}"/>
    </div>
  </c:if>

  <div class="snap-left" style="height: 100%;" class="small-margin-left">
    <h2>
      <a id="campaign-name" href="${ campaignUrl }/info">
        <c:out value="${ campaign.fullName }" escapeXml="true" />
      </a>
      <%-- raw reference and name because we need to get the name and only the name for modification, and then re-compose the title with the reference  --%>
      <span id="campaign-raw-reference" style="display: none">
        <c:out value="${ campaign.reference }" escapeXml="true" />
      </span>

      <span id="campaign-raw-name" style="display: none">
        <c:out value="${ campaign.name }" escapeXml="true" />
      </span>
    </h2>
  </div>

  <div class="unsnap"></div>
  <c:if test="${writable}">

    <f:message var="renameTitle" key="dialog.rename-campaign.title" />
    <div id="rename-campaign-dialog" class="popup-dialog not-displayed" title="${renameTitle}">

      <label>
        <f:message key="dialog.rename.label" />
      </label>
      <input type="text" id="rename-campaign-name" maxlength="255" size="50" />
      <br />
      <comp:error-message forField="name" />

      <div class="popup-dialog-buttonpane">
        <input type="button" class="button" value="${confirmLabel}" data-def="evt=confirm, mainbtn" />
        <input type="button" class="button" value="${cancelLabel}" data-def="evt=cancel" />
      </div>

    </div>

  </c:if>
</div>

<div id="campaign-toolbar" class="toolbar-class ui-corner-all ">
  <div class="toolbar-information-panel">
    <comp:general-information-panel auditableEntity="${campaign}" entityUrl="${campaignUrl}" />
  </div>
  <div class="toolbar-button-panel">


    <c:if test="${ writable }">

      <input type="button" class="sq-btn" value='<f:message key="label.Rename" />' id="rename-campaign-button" title='<f:message key="dialog.rename-campaign.title" />' />
    </c:if>

  </div>

  <c:if test="${ moreThanReadOnly }">
    <comp:opened-object otherViewers="${ otherViewers }" objectUrl="${ campaignUrl }" />

  </c:if>

  <comp:milestone-messages milestoneConf="${milestoneConf}" nodeType = "campaign"/>
  <div class="unsnap"></div>
</div>


<csst:jq-tab>
  <div class="fragment-tabs fragment-body">
    <ul class="tab-menu">
      <li>
        <a href="#campaign-dashboard">
          <f:message key="title.Dashboard" />
        </a>
      </li>
      <li>
        <a href="#tabs-1">
          <f:message key="tabs.label.information" />
        </a>
      </li>
      <c:if test="${milestoneConf.displayTab}">
        <li>
            <a href="${campaignUrl}/milestones/panel"><f:message key="tabs.label.milestone"/></a>
        </li>
      </c:if>
      <li>
        <a href="#tabs-2">
          <f:message key="tabs.label.test-plan" />
        </a>
      </li>
      <li>
        <a href="#tabs-3">
          <f:message key="label.Attachments" />
          <c:if test="${ campaign.attachmentList.notEmpty }">
            <span class="hasAttach">!</span>
          </c:if>
        </a>
      </li>

<c:if test="${campaign.project.bugtrackerConnected}">
        <li>
          <%-- div#bugtracker-section-main-div is declared in tagfile issues:bugtracker-panel.tag  --%>
          <a href="#bugtracker-section-main-div"><f:message key="tabs.label.issues"/></a>
        </li>
</c:if>

    </ul>


    <div id="tabs-1">
      <c:if test="${ writable }">
        <c:set var="descrRichAttributes" value="class='editable rich-editable' data-def='url=${campaignUrl}' " />
      </c:if>
<f:message var="labelDescription" key="label.Description" />

      <comp:toggle-panel id="campaign-description-panel" title='${labelDescription} <span class="small txt-discreet">[ID = ${ campaign.id }]</span>' open="true">
        <jsp:attribute name="body">
              <div class="display-table-row">
                <label class="display-table-cell" for="campaign-reference"><f:message key="label.Reference" /></label>
                <div class="display-table-cell" id="campaign-reference">${ campaign.reference }</div>
              </div>

              <div class="display-table-row">
                <label for="campaign-description" class="display-table-cell"><f:message key="label.Description" /></label>
		        <div id="campaign-description" ${descrRichAttributes}>${ campaign.description }</div>
              </div>

            <div class="display-table-row">
              <label for="campaign-status" class="display-table-cell"><f:message key="campaign.status.combo.label" /></label>
              <div>
                <span id="campaign-status-icon" style="vertical-align:middle" class="sq-icon campaign-status-${campaign.status}"> &nbsp &nbsp</span>
                <span id="campaign-status">${ campaignStatusLabel }</span>
              </div>
            </div>

            <div class="display-table-row">
              <label for="campaign-progress-status" class="display-table-cell"><f:message key="campaign.progress_status.label" /></label>
              <span id="campaign-progress-status"><f:message key="${ statistics.status.i18nKey }" /></span>
            </div>
	    </jsp:attribute>
      </comp:toggle-panel>


      <%----------------------------------- Custom Fields -----------------------------------------------%>

      <comp:toggle-panel id="campaign-custom-fields" titleKey="generics.customfieldvalues.title" open="${hasCUF}">
        <jsp:attribute name="body">
				<div id="campaign-custom-fields-content">
<c:if test="${hasCUF}">
				<comp:waiting-pane />
</c:if>
				</div>
			</jsp:attribute>
      </comp:toggle-panel>




      <%--------------------------- Planning section ------------------------------------%>

      <comp:toggle-panel id="datepicker-panel" titleKey="label.Planning" open="true">
        <jsp:attribute name="panelButtons">
				<c:if test="${writable}">
				<input id="iteration-planning-button" class="sq-btn" type="button" role="button" value="${iterationPlanningButton}" />
				</c:if>
		</jsp:attribute>
        <jsp:attribute name="body">
	<div class="datepicker-panel">
		<table class="datepicker-table">
			<tr>
				<td class="datepicker-table-col">
					<div class="datepicker-col-scheduled">
						<comp:datepickers-pair />
					</div>
				</td>
				<td class="datepicker-table-col">
					<div class="datepicker-col-actual">
						<comp:datepickers-auto-pair />
					</div>
				</td>
			</tr>
		</table>
	</div>
	</jsp:attribute>
      </comp:toggle-panel>


      <div id="iteration-planning-popup" class="popup-dialog not-displayed" title="${iterationPlanningTitle}"
        data-def="dateformat=${dateformat}, campaignId=${campaign.id}">

        <div data-def="state=edit">
          <table class="iteration-planning-content" class="unstyled-table">
            <thead>
              <tr>
                <th>
                  <f:message key="label.Name" />
                </th>
                <th>
                  <f:message key="campaigns.planning.iterations.scheduledstart" />
                </th>
                <th>
                  <f:message key="campaigns.planning.iterations.scheduledend" />
                </th>
              </tr>
            </thead>
            <tbody>

            </tbody>
          </table>
        </div>

        <div data-def="state=loading">
          <comp:waiting-pane />
        </div>

        <div class="popup-dialog-buttonpane">
          <input type="button" value="${buttonOK}" data-def="evt=confirm, mainbtn=edit" />
          <input type="button" value="${buttonCancel}" data-def="evt=cancel, mainbtn" />
        </div>

      </div>

      <%--------------------------- /Planning section ------------------------------------%>
      <%-- ------------------ statistiques --------------------------- --%>
      <comp:statistics-panel statisticsEntity="${ statistics }" statisticsUrl="${ campaignInfoStatisticsUrl }" />
      <%-- ------------------ /statistiques --------------------------- --%>
    </div>
    <div id="tabs-2" class="table-tab">

      <%--------------------------- Test plan section ------------------------------------%>

      <camp:campaign-test-plan-panel
          editable="${ linkable }"
          reorderable="${linkable}"
          linkable="${linkable}"
          campaign="${campaign}"
          milestoneConf="${milestoneConf}"/>

    </div>

    <%------------------------------ Attachments bloc ---------------------------------------------%>

    <at:attachment-tab tabId="tabs-3" entity="${ campaign }" editable="${ attachable }" tableModel="${attachmentsModel}" />


    <%------------------------------- Dashboard ---------------------------------------------------%>
    <div id="campaign-dashboard">

     <%-- statistics panel --%>
        <c:if test="${shouldShowDashboard}">
          <dashboard:favorite-dashboard/>
        </c:if>

        <c:if test="${not shouldShowDashboard}">
            <dashboard:campaign-dashboard-panel url="${campaignStatisticsUrl}" printUrl="${campaignStatisticsPrintUrl}"
                   allowsSettled="${allowsSettled}" allowsUntestable="${allowsUntestable}" />
        </c:if>

    </div>



        <%-- ----------------------- bugtracker (if present)----------------------------------------%>

<c:if test="${campaign.project.bugtrackerConnected}">
        <issues:butracker-panel entity="${campaign}" />
</c:if>

    <%-- ----------------------- /bugtracker (if present)----------------------------------------%>


  </div>
</csst:jq-tab>


<script type="text/javascript">

squashtm.workspace.shouldShowFavoriteDashboard = ${shouldShowDashboard};
squashtm.workspace.canShowFavoriteDashboard = ${canShowDashboard};

	require(["common"], function(){
		require(["campaign-management"], function(manager){

			var conf = {
				data : {
					campaignId : ${campaign.id},
					campaignUrl : "${campaignUrl}",
					bugtrackerUrl : "${btEntityUrl}",
					cufValuesUrl : "${customFieldsValuesURL}",
					assignableUsers : ${ json:serialize(assignableUsers)},
					weights	: ${ json:serialize(weights)},
					modes : ${ json:serialize(modes)},
					planningUrl: '${campaignPlanningUrl}',
					initialScheduledStartDate: "${campaign.scheduledStartDate.time}",
					initialScheduledEndDate: "${campaign.scheduledEndDate.time}",
					initialActualStartDate: "${campaign.actualStartDate.time}",
					initialActualEndDate: "${campaign.actualEndDate.time}",
					initialActualStartAuto: ${campaign.actualStartAuto},
					initialActualEndAuto: ${campaign.actualEndAuto},
          campaignStatusComboJson : ${campaignStatusComboJson},
				},
				features : {
					editable : ${writable},
					reorderable : ${linkable},
					linkable : ${linkable},
					writable : ${writable},
					hasBugtracker : ${campaign.project.bugtrackerConnected},
					hasCUF : ${hasCUF}
				}
			};

			manager.init(conf);
		});
	});

</script>
