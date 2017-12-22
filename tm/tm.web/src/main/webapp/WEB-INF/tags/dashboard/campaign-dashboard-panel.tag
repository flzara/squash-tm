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
<%@ tag language="java" pageEncoding="utf-8" body-content="empty" description="structure of a dashboard for test cases. No javascript."%>

<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<%@ attribute name="url" required="true" description="url where to get the data" %>
<%@ attribute name="allowsSettled" required="true" description="whether execution status settled is allowed" %>
<%@ attribute name="allowsUntestable" required="true" description="whether execution status  untestable is allowed" %>
<%@ attribute name="printUrl" required="false" description="url where to fetch an html version" %>
<%@ attribute name="printmode" required="false" type="java.lang.Boolean"
			description="if set to true, renders in print mode. This means among other things that the toolbar will not be rendered." %>



<f:message var="advanceTitle" key="title.CampaignCumulativeAdvancement"/>
<f:message var="statisticsTitle" key="title.CampaignStatistics"/>
<f:message var="inventoryTitle" key="title.TestInventoryByIteration"/>
<f:message var="refreshLabel" key="label.Refresh" />
<f:message var="dialogTitle" key="dialog.label.iteration.scheduled_dates.label"/>
<f:message var="buttonOK" key="label.Ok"/>
<f:message var="dateformatSupershort" key="squashtm.dateformatSupershort" />
<f:message var="dateformatJs" key="squashtm.dateformatShort" />
<f:message var="testWeightHelper" key="dashboard.campaigns.importance.helper"/>
<f:message var="campaignProgressHelper" key="dashboard.campaigns.progression.helper"/>
<f:message var="favoriteLabel" key="label.favorite-dashboard" />

<div id="dashboard-master" data-def="url=${url}">

	<div class="right btn-toolbar">
		<span class="dashboard-timestamp not-displayed"><f:message key="dashboard.meta.timestamp.label"/></span>
<c:if test="${empty printmode or (not printmode) }">
		<input type="button" class="dashboard-refresh-button sq-btn btn-sm" value="${refreshLabel}" title="${refreshLabel}"/>
		<input type="button" class="show-favorite-dashboard-button sq-btn btn-sm" role="button" value="${favoriteLabel}" title="${favoriteLabel}" />
    <a id="campaign-dashboard-print" href="${printUrl}" target="_blank" class="sq-icon-btn btn-sm"  role="button" title="<f:message key='label.print' />">
      <span class="ui-icon ui-icon-print"></span>
    </a>
</c:if>
	</div>


	<%-- alternate contents : when no data are available we'll display an empty pane, when there are some we'll display the rest. --%>

	<div class="dashboard-figleaf">

		<div class="dashboard-figleaf-notready" style="text-align : center">
			<h3 class="dashboard-figleaf-notready-title"><f:message key="dashboard.notready.title"/></h3>
		</div>

		<div class="dashboard-figleaf-figures not-displayed">

			<%-- first dashboard : cumulative progression of this campaign --%>

			<comp:toggle-panel id="" title="${advanceTitle}">

				<jsp:attribute name="panelButtons">
					<div class="icon-helper no-print" title="${campaignProgressHelper}"> </div>
				</jsp:attribute>


				<jsp:attribute name="body">

				<div id="dashboard-cumulative-progression" data-def="model-attribute=campaignProgressionStatistics, dateformat=${dateformatSupershort}">

					<div class="dashboard-figures  dashboard-alternative-content" style="height : 300px;">
						<div id="dashboard-cumulative-progression-view" class="dashboard-item-view" style="width:95%;float:none;margin:auto">


						</div>
					</div>

					<div class="dashboard-cumulative-progression-error not-displayed  dashboard-alternative-content">
						<span class="cumulative-progression-errormsg"></span> <a class="dashboard-cumulative-progression-details" href="#">(<f:message key="error.generic.button.details.label"/>...)</a>
					</div>

					<div class="dashboard-cumulative-progression-iterpopup popup-dialog not-displayed"
						title="${dialogTitle}" data-def="dateformat=${dateformatJs}">

						<table class="iterpopup-content" >
							<thead>
								<tr>
									<th><f:message key="label.Name"/></th>
									<th><f:message key="dialog.label.iteration.scheduled_start.label"/></th>
									<th><f:message key="dialog.label.iteration.scheduled_end.label"/></th>
								</tr>
							</thead>
							<tbody>

							</tbody>
						</table>

						<div class="popup-dialog-buttonpane">
							<input type="button" value="${buttonOK}" data-def="evt=close, mainbtn"/>
						</div>

					</div>

				</div>
				</jsp:attribute>
			</comp:toggle-panel>

			<%-- second dashboard : campaign statistics --%>
			<comp:toggle-panel id="dashboard-statistics" title="${statisticsTitle}">
				<jsp:attribute name="body">


					<div id="dashboard-testcase-status" class="dashboard-narrow-item dashboard-pie" data-def="model-attribute=campaignTestCaseStatusStatistics">

						<h2 class="dashboard-item-title"><f:message key="dashboard.campaigns.status.title"/></h2>

						<div class="dashboard-figures">
							<div id="dashboard-testcase-status-view" class="dashboard-item-view"></div>
						</div>

						<div class="dashboard-item-meta">
							<div class="dashboard-item-legend">
								<div>
									<div class="dashboard-legend-sample-color" style="background-color:#BDD3FF"></div>
									<span><f:message key="execution.execution-status.READY" /></span>
								</div>
								<div>
									<div class="dashboard-legend-sample-color" style="background-color:#6699FF"></div>
									<span><f:message key="execution.execution-status.RUNNING" /></span>
								</div>
								<div>
									<div class="dashboard-legend-sample-color" style="background-color:#99CC00"></div>
									<span><f:message key="execution.execution-status.SUCCESS" /></span>
								</div>

								<div class="${allowsSettled ? '' : 'not-displayed' }">
									<div class="dashboard-legend-sample-color" style="background-color:#99FF99" ></div>
									<span><f:message key="execution.execution-status.SETTLED" /></span>
								</div>

								<div>
									<div class="dashboard-legend-sample-color" style="background-color:#FF3300"></div>
									<span><f:message key="execution.execution-status.FAILURE" /></span>
								</div>
								<div>
									<div class="dashboard-legend-sample-color" style="background-color:#FFCC00"></div>
									<span><f:message key="execution.execution-status.BLOCKED" /></span>
								</div>

								<div class="${allowsUntestable ? '' : 'not-displayed' }">
									<div class="dashboard-legend-sample-color" style="background-color:#969696" ></div>
									<span><f:message key="execution.execution-status.UNTESTABLE" /></span>
								</div>

							</div>
						</div>
					</div>

					<div id="dashboard-success-rate" class="dashboard-narrow-item" data-def="model-attribute=campaignTestCaseSuccessRateStatistics">

						<h2 class="dashboard-item-title"><f:message key="dashboard.campaigns.successrate.title"/></h2>

						<div class="dashboard-figures">
							<div id="dashboard-success-rate-view" class="dashboard-item-view"></div>
						</div>

						<div class="dashboard-item-meta">

							<div class="dashboard-item-subplot">
								<span style="font-weight:bold;size:1.3em;"><f:message key="label.GlobalRates"/></span>

								<div class="dashboard-subplot-legend success-rate-total-success"
									 style="background-color:#99CC00; color:black;"></div>

								<div class="dashboard-subplot-legend success-rate-total-failure"
									 style="background-color:#FF3300; color:white;"></div>
							</div>

							<div class="dashboard-item-legend">
								<div >
									<div class="dashboard-legend-icon dashboard-donut-lvl4"></div>
									<span class="serie serie0"><f:message key="test-case.importance.VERY_HIGH" /></span>
								</div>
								<div>
									<div class="dashboard-legend-icon dashboard-donut-lvl3"></div>
									<span class="serie serie1"><f:message key="test-case.importance.HIGH" /></span>
								</div>
								<div >
									<div class="dashboard-legend-icon dashboard-donut-lvl2"></div>
									<span class="serie serie2"><f:message key="test-case.importance.MEDIUM" /></span>
								</div>
								<div >
									<div class="dashboard-legend-icon dashboard-donut-lvl1"></div>
									<span class="serie serie3"><f:message key="test-case.importance.LOW" /></span>
								</div>


								<div>
									<div class="dashboard-legend-sample-color" style="background-color:#99CC00"></div>
									<span><f:message key="label.SuccessRate" /></span>
								</div>
								<div>
									<div class="dashboard-legend-sample-color" style="background-color:#FF3300"></div>
									<span><f:message key="label.FailureRate" /></span>
								</div>
								<div>
									<div class="dashboard-legend-sample-color" style="background-color:#969696"></div>
									<span><f:message key="label.Other" /></span>
								</div>
							</div>
						</div>
					</div>


					<div id="dashboard-nonexecuted-testcase-importance" class="dashboard-narrow-item dashboard-pie" data-def="model-attribute=campaignNonExecutedTestCaseImportanceStatistics">

						<h2 class="dashboard-item-title"><f:message key="dashboard.campaigns.importance.title"/><div class="icon-helper no-print small-margin-left" title="${testWeightHelper}"></div></h2>

						<div class="dashboard-figures">
							<div id="dashboard-nonexecuted-testcase-importance-view" class="dashboard-item-view"></div>
						</div>

						<div class="dashboard-item-meta">

							<div class="dashboard-item-legend">

								<div>
									<div class="dashboard-legend-sample-color" style="background-color:#FCEDB6"></div>
									<span><f:message key="test-case.importance.LOW" /></span>
								</div>
								<div>
									<div class="dashboard-legend-sample-color" style="background-color:#FBD329"></div>
									<span><f:message key="test-case.importance.MEDIUM" /></span>
								</div>
								<div>
									<div class="dashboard-legend-sample-color" style="background-color:#FDA627"></div>
									<span><f:message key="test-case.importance.HIGH" /></span>
								</div>
								<div>
									<div class="dashboard-legend-sample-color" style="background-color:#FD7927"></div>
									<span><f:message key="test-case.importance.VERY_HIGH" /></span>
								</div>
							</div>
						</div>
					</div>

				</jsp:attribute>
			</comp:toggle-panel>



			<comp:toggle-panel id="" title="${inventoryTitle}">
				<jsp:attribute name="body">
				<div class="dashboard-figures">
					<table id="dashboard-test-inventory" class="dashboard-table" data-def="model-attribute=iterationTestInventoryStatisticsList">
						<thead>
							<tr >
								<th style="width:25%"><span><f:message key="label.iteration"/></span></th>
								<th class="status-color-ready std-border"><span><f:message key="label.Ready"/></span></th>
								<th class="status-color-running"><span><f:message key="label.Running"/></span></th>
								<th class="status-color-success"><span><f:message key="label.Success"/></span></th>
								<th class="${allowsSettled ? 'status-color-settled std-border' : 'status-color-settled std-border not-displayed' }"><span><f:message key="label.Settled"/></span></th>
								<th class="status-color-failure"><span><f:message key="label.Failure"/></span></th>
								<th class="status-color-blocked"><span><f:message key="label.Blocked"/></span></th>
								<th class="${allowsUntestable ? 'status-color-untestable std-border' : 'status-color-untestable std-border not-displayed' }"><span><f:message key="label.Untestable"/></span></th>
								<th ><span><f:message key="dashboard.campaigns.testinventory.legend.testnumber"/></span></th>
								<th ><span><f:message key="dashboard.campaigns.testinventory.legend.advancementexecution"/></span></th>
							</tr>
						</thead>

						<tbody>
							<tr class="dashboard-table-template-emptyrow">
								<td colspan="9" class="std-border"><f:message key="generics.datatable.zeroRecords"/></td>
							</tr>
							<tr class="dashboard-table-template-datarow">
								<td class="std-border light-border" style="text-align:left;padding-left:10pt;font-weight:bold">{{this.[0]}}</td>
								<td class="std-border light-border">{{this.[1]}}</td>
								<td class="std-border light-border">{{this.[2]}}</td>
								<td class="std-border light-border">{{this.[3]}}</td>
								<td class="${allowsSettled ? 'std-border light-border' : 'std-border light-border not-displayed' }">{{this.[4]}}</td>
								<td class="std-border light-border">{{this.[5]}}</td>
								<td class="std-border light-border">{{this.[6]}}</td>
								<td class="${allowsUntestable ? 'std-border light-border' : 'std-border light-border not-displayed' }">{{this.[7]}}</td>
								<td class="std-border light-border" style="font-weight:bold;">{{this.[8]}}</td>
								<td class="std-border light-border"style="font-weight:bold;">{{this.[9]}}</td>
							</tr>
						</tbody>
					</table>
				</div>
				</jsp:attribute>
			</comp:toggle-panel>
		</div>

	</div>
</div>
