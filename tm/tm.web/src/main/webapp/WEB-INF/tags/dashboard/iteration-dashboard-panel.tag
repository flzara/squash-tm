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
<%@ tag language="java" pageEncoding="utf-8" body-content="empty" description="structure of a dashboard for iterations. No javascript."%>

<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>


<%@ attribute name="url" required="true" description="url where to get the data" %>
<%@ attribute name="allowsSettled" required="true" description="whether execution status settled is allowed" %>
<%@ attribute name="allowsUntestable" required="true" description="whether execution status  untestable is allowed" %>
<%@ attribute name="printUrl" required="false" description="url where to fetch an html version" %>
<%@ attribute name="printmode" required="false" type="java.lang.Boolean"
			description="if set to true, renders in print mode. This means among other things that the toolbar will not be rendered." %>


<f:message var="advanceTitle" key="title.IterationCumulativeAdvancement"/>
<f:message var="statisticsTitle" key="title.IterationStatistics"/>
<f:message var="inventoryTitle" key="title.TestInventoryByTestSuite"/>
<f:message var="TestSuiteLabel" key="label.testSuite"/>
<f:message var="TotalLabel" key="statisticsbytestsuite.total.label"/>
<f:message var="ToDoLabel" key="statisticsbytestsuite.toDo.label"/>
<f:message var="DoneLabel" key="statisticsbytestsuite.Done.label"/>
<f:message var="ToExecuteLabel" key="label.Ready"/>
<f:message var="RunningLabel" key="label.Running"/>
<f:message var="SuccessLabel" key="label.Success"/>
<f:message var="FailureLabel" key="label.Failure"/>
<f:message var="BlockedLabel" key="label.Blocked"/>
<f:message var="NonExecutableLabel" key="label.Untestable"/>
<f:message var="ProgressLabel" key="statisticsbytestsuite.Progress.label"/>
<f:message var="SuccessRateLabel" key="statisticsbytestsuite.SuccessRate.label"/>
<f:message var="FailureRateLabel" key="statisticsbytestsuite.FailureRate.label"/>
<f:message var="ProgressVsPrevLabel" key="statisticsbytestsuite.ProgressVsPrev.label"/>
<f:message var="ToDoVsPrevLabel" key="statisticsbytestsuite.ToDoVsPrev.label"/>
<f:message var="VeryHighLabel" key="test-case.importance.VERY_HIGH"/>
<f:message var="HighLabel" key="test-case.importance.HIGH"/>
<f:message var="MediumLabel" key="test-case.importance.MEDIUM"/>
<f:message var="LowLabel" key="test-case.importance.LOW"/>
<f:message var="testsWithoutSuite" key="dashboard.iteration.tests-without-suite"/>
<f:message var="testWeightHelper" key="dashboard.campaigns.importance.helper"/>
<f:message var="dateformatSupershort" key="squashtm.dateformatSupershort" />


<f:message var="refreshLabel" key="label.Refresh" />
<f:message var="favoriteLabel" key="label.favorite-dashboard" />
<f:message var="campaignProgressHelper" key="dashboard.campaigns.progression.helper"/>

<div id="dashboard-master" data-def="url=${url}">

	<div class="right btn-toolbar">
		<span class="dashboard-timestamp not-displayed"><f:message key="dashboard.meta.timestamp.label"/></span>
<c:if test="${empty printmode or (not printmode) }">
		<input type="button" class="dashboard-refresh-button sq-btn btn-sm" role="button" value="${refreshLabel}" title="${refreshLabel}" />
		<input type="button" class="show-favorite-dashboard-button sq-btn btn-sm" role="button" value="${favoriteLabel}" title="${favoriteLabel}" />
    <a id="iteration-dashboard-print" href="${printUrl}" target="_blank" class="sq-icon-btn btn-sm" role="button" title="<f:message key='label.print' />" >
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
	<%-- first dashboard : cumulative progression of this iteration --%>

			<comp:toggle-panel id="" title="${advanceTitle}">

				<jsp:attribute name="panelButtons">
					<div class="icon-helper no-print" title="${campaignProgressHelper}"> </div>
				</jsp:attribute>


				<jsp:attribute name="body">

				<div id="dashboard-cumulative-progression" data-def="model-attribute=iterationProgressionStatistics, dateformat=${dateformatSupershort}">

					<div class="dashboard-figures  dashboard-alternative-content" style="height : 300px;">
						<div id="dashboard-cumulative-progression-view" class="dashboard-item-view" style="width:95%;float:none;margin:auto">


						</div>
					</div>

					<div class="dashboard-cumulative-progression-error not-displayed  dashboard-alternative-content">
						<span class="cumulative-progression-errormsg"></span> <a class="dashboard-cumulative-progression-details" href="#">(<f:message key="error.generic.button.details.label"/>...)</a>
					</div>


				</div>
				</jsp:attribute>
			</comp:toggle-panel>
			<%-- second dashboard : campaign statistics --%>
			<comp:toggle-panel id="dashboard-statistics" title="${statisticsTitle}">
				<jsp:attribute name="body">


					<div id="dashboard-testcase-status" class="dashboard-narrow-item dashboard-pie" data-def="model-attribute=iterationTestCaseStatusStatistics">

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
									<div class="dashboard-legend-sample-color" style="background-color:#99FF99"></div>
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
									<div class="dashboard-legend-sample-color" style="background-color:#969696"></div>
									<span><f:message key="execution.execution-status.UNTESTABLE" /></span>
								</div>

							</div>
						</div>
					</div>

					<div id="dashboard-success-rate" class="dashboard-narrow-item" data-def="model-attribute=iterationTestCaseSuccessRateStatistics">

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

								<div>
									<div class="dashboard-legend-icon dashboard-donut-lvl4"></div>
									<span class="serie serie0"><f:message key="test-case.importance.VERY_HIGH" /></span>
								</div>

								<div>
									<div class="dashboard-legend-icon dashboard-donut-lvl3"></div>
									<span class="serie serie1"><f:message key="test-case.importance.HIGH" /></span>
								</div>

								<div>
									<div class="dashboard-legend-icon dashboard-donut-lvl2"></div>
									<span class="serie serie2"><f:message key="test-case.importance.MEDIUM" /></span>
								</div>

								<div>
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


					<div id="dashboard-nonexecuted-testcase-importance" class="dashboard-narrow-item dashboard-pie" data-def="model-attribute=iterationNonExecutedTestCaseImportanceStatistics">

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

			<comp:toggle-panel id="test-suite-statistics" title="${inventoryTitle}">
				<jsp:attribute name="body">
				<div class="dashboard-figures">
					<table id="dashboard-test-inventory" class="dashboard-table" data-def="model-attribute=testsuiteTestInventoryStatisticsList">
						<thead>
							<tr>
								<th style="border:none;" colspan="1"></th>
								<th style="border:none;"></th>
								<th class="status-color-untestable std-border light-border" colspan="3" ><f:message key="label.Synthesis"/></th>
								<th style="border:none;"></th>
								<th class="status-color-untestable std-border light-border" colspan="${allowsSettled? (allowsUntestable? 12 : 11) : (allowsUntestable? 11 : 10)}"><f:message key="label.ExecutionProgress"/> </th>
								<th style="border:none;"></th>
								<th class="status-color-untestable std-border light-border" colspan="4"><f:message key="label.NeverExecuted"/></th>
							</tr>
							<tr >
								<th title="${TestSuiteLabel}" style="width:10%" class="std-border light-border" >${TestSuiteLabel}<%--<f:message key="shortLabel.TestSuite"/> --%></th>
								<th style="border:none;"></th>
								<th title="${TotalLabel}" class="std-border light-border" ><f:message key="shortLabel.Total"/></th>
								<th title="${ToDoLabel}" class="std-border light-border" ><f:message key="shortLabel.ToDo"/></th>
							    <th title="${DoneLabel}" class="std-border light-border" ><f:message key="shortLabel.Done"/></th>
								<th style="border:none;"></th>
								<th title="${ToExecuteLabel}" class="status-color-ready std-border light-border"><f:message key="shortLabel.Ready"/></th>
								<th title="${RunningLabel}" class="status-color-running std-border light-border"><f:message key="shortLabel.Running"/></th>
								<th title="${SuccessLabel}" class="status-color-success std-border light-border"><f:message key="shortLabel.Success"/></th>
								<th title="${SettledLabel}" class="${allowsSettled ? 'status-color-settled' : 'not-displayed' } std-border light-border" ><f:message key="shortLabel.Settled"/></th>
								<th title="${FailureLabel}" class="status-color-failure std-border light-border"><f:message key="shortLabel.Failure"/></th>
								<th title="${BlockedLabel}" class="status-color-blocked std-border light-border"><f:message key="shortLabel.Blocked"/></th>
								<th class="${allowsUntestable ? 'status-color-untestable std-border light-border' : 'not-displayed' }" title="${NonExecutableLabel}"><f:message key="shortLabel.NonExecutable"/></th>
								<th style="border:none;"></th>
								<th title="${ProgressLabel}" class="std-border light-border" ><f:message key="shortLabel.ExecutionProgress"/></th>
								<th title="${SuccessRateLabel}" class="std-border light-border" ><f:message key="shortLabel.SuccessRate"/></th>
								<th title="${FailureRateLabel}" class="std-border light-border" ><f:message key="shortLabel.FailureRate"/></th>
								<th title="${ProgressVsPrevLabel}" class="std-border light-border" style="width:8%"><f:message key="shortLabel.ExecutionProgressComparedToPrev"/></th>
								<th style="border:none;"></th>
								<th title="${VeryHighLabel}"  class="std-border light-border"><f:message key="shortLabel.VeryHigh"/></th>
								<th title="${HighLabel}"  class="std-border light-border"><f:message key="shortLabel.High"/></th>
								<th title="${MediumLabel}"  class="std-border light-border"><f:message key="shortLabel.Medium"/></th>
								<th title="${LowLabel}"  class="std-border light-border"><f:message key="shortLabel.Low"/></th>
							</tr>
						</thead>

						<tbody>
							<tr class="dashboard-table-template-emptyrow ">
								<td colspan="19" class="std-border "><f:message key="generics.datatable.zeroRecords"/></td>
							</tr>
							<tr class="dashboard-table-template-datarow status-color-untestable">
								<!--  the following columns displays conditional style and content, for the tests belonging to no test suite -->
								<td class="iteration-dashbord-test-suite-col std-border light-border {{#unless this.[0]}}nota-bene{{/unless}}">{{this.[0]}}{{#unless this.[0]}}${testsWithoutSuite}{{/unless}} </td>
								<td style="border:none;"></td>
								<td class="std-border light-border iteration-dashbord-total-col">{{this.[1]}}</td>
								<td class="std-border light-border" style="color:blue;">{{this.[2]}}</td>
								<td class="std-border light-border" style="color:purple;">{{this.[3]}}</td>
								<td style="border:none;"></td>
								<td class="std-border light-border iteration-dashbord-ready-col">{{this.[4]}}</td>
								<td class="std-border light-border iteration-dashbord-running-col">{{this.[5]}}</td>
								<td class="std-border light-border iteration-dashbord-passed-col">{{this.[6]}}</td>
								<td class="${allowsSettled ? 'std-border light-border' : 'not-displayed' }">{{this.[7]}}</td>
								<td class="std-border light-border iteration-dashbord-failed-col">{{this.[8]}}</td>
								<td class="std-border light-border iteration-dashbord-blocked-col">{{this.[9]}}</td>
								<td class="${allowsUntestable ? 'std-border light-border iteration-dashbord-untestable-col' : 'not-displayed' }">{{this.[10]}}</td>
								<td style="border:none;"></td>
								<td class="std-border light-border">{{this.[11]}}%</td>
								<td class="std-border light-border">{{this.[12]}}%</td>
								<td class="std-border light-border">{{this.[13]}}%</td>
								<td class="std-border light-border">{{this.[14]}}%</td>
								<td style="border:none;"></td>
								<td class="std-border light-border">{{this.[15]}}</td>
								<td class="std-border light-border">{{this.[16]}}</td>
								<td class="std-border light-border">{{this.[17]}}</td>
								<td class="std-border light-border ">{{this.[18]}}</td>

							</tr>
						</tbody>
					</table>
				</div>
				</jsp:attribute>
			</comp:toggle-panel>

		</div>
	</div>
</div>
