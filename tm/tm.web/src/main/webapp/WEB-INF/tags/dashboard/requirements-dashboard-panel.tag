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
<%@ tag language="java" pageEncoding="utf-8" body-content="empty" description="structure of a dashboard for requirements. No javascript."%>

<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ attribute name="url" required="true" description="url where to get the data" %>
<%@ attribute name="cacheKey" required="false" description="if set, will use the cache using that key" %>

<c:set var="showMilestoneDashboardButton" value="${(empty isMilestoneDashboard) ? false : isMilestoneDashboard}" />


<div id="dashboard-master" data-def="url=${url}">
  <div class="expand sq-tg">
    <div class="tg-head">
      <h3>
        <f:message key="title.Dashboard"/>
      </h3>
      <div class="tg-toolbar">
        <span class="dashboard-timestamp not-displayed"><f:message key="dashboard.meta.timestamp.label"/></span>
        <input type="button" class="dashboard-refresh-button sq-btn" role="button" value="<f:message key='label.Refresh' />" title="<f:message key='label.Refresh' />"/>
         <c:if test="${not showMilestoneDashboardButton}">
			<input type="button" class="show-favorite-dashboard-button sq-btn" role="button" value="<f:message key='label.favorite-dashboard' />" title="<f:message key='label.favorite-dashboard' />"/>
		</c:if>
		<c:if test="${showMilestoneDashboardButton}">
			 <input type="button" class="show-milestone-favorite-dashboard-button sq-btn" role="button" value="<f:message key='label.favorite-dashboard' />" title="<f:message key='label.favorite-dashboard' />"/>
		</c:if>
      </div>
    </div>
    <div class="tg-body">
      <div class="dashboard-figleaf">
        <div class="dashboard-figleaf-figures not-displayed">

          <div id="dashboard-item-bound-tcs" class="dashboard-item dashboard-pie" data-def="model-attribute=boundTestCasesStatistics">

            <div id="dashboard-bound-tcs-view" class="dashboard-item-view"></div>

            <div class="dashboard-item-meta">
              <h2 class="dashboard-item-title"><f:message key="dashboard.requirements.bound-tcs.title"/></h2>

              <div class="dashboard-item-legend">
                <div>
                  <div class="dashboard-legend-sample-color" style="background-color:#9CCBE0"></div>
                  <span><f:message key="dashboard.requirements.bound-tcs.legend.no-reqs" /></span>
                </div>
                <div>
                  <div class="dashboard-legend-sample-color" style="background-color:#69B1D1"></div>
                  <span><f:message key="dashboard.requirements.bound-tcs.legend.one-req" /></span>
                </div>
                <div>
                  <div class="dashboard-legend-sample-color" style="background-color:#3383A7"></div>
                  <span><f:message key="dashboard.requirements.bound-tcs.legend.many-reqs" /></span>
                </div>
              </div>
            </div>
          </div>

          <div id="dashboard-item-requirements-status" class="dashboard-item dashboard-pie" data-def="model-attribute=statusesStatistics">
            <div id="dashboard-requirement-status-view" class="dashboard-item-view">

            </div>

            <div class="dashboard-item-meta">
              <h2 class="dashboard-item-title"><f:message key="dashboard.requirements.status.title"/></h2>

              <div class="dashboard-item-legend">
                <div>
                  <div class="dashboard-legend-sample-color" style="background-color:#C9E8AA"></div>
                  <span><f:message key="requirement.status.WORK_IN_PROGRESS" /></span>
                </div>
                <div>
                  <div class="dashboard-legend-sample-color" style="background-color:#A3D86E"></div>
                  <span><f:message key="requirement.status.UNDER_REVIEW" /></span>
                </div>
                <div>
                  <div class="dashboard-legend-sample-color" style="background-color:#56AD25"></div>
                  <span><f:message key="requirement.status.APPROVED" /></span>
                </div>
                <div>
                  <div class="dashboard-legend-sample-color" style="background-color:#D9D9D9"></div>
                  <span><f:message key="requirement.status.OBSOLETE" /></span>
                </div>
              </div>
            </div>
          </div>

          <div id="dashboard-item-requirements-criticality" class="dashboard-item dashboard-pie" data-def="model-attribute=criticalityStatistics">
            <div id="dashboard-requirement-criticality-view" class="dashboard-item-view">

            </div>

            <div class="dashboard-item-meta">
              <h2 class="dashboard-item-title"><f:message key="dashboard.requirements.criticality.title"/></h2>

              <div class="dashboard-item-legend">
                <div>
                  <div class="dashboard-legend-sample-color" style="background-color:#FCEDB6"></div>
                  <span><f:message key="requirement.criticality.UNDEFINED" /></span>
                </div>
                <div>
                  <div class="dashboard-legend-sample-color" style="background-color:#FBD329"></div>
                  <span><f:message key="requirement.criticality.MINOR" /></span>
                </div>
                <div>
                  <div class="dashboard-legend-sample-color" style="background-color:#FDA627"></div>
                  <span><f:message key="requirement.criticality.MAJOR" /></span>
                </div>
                <div>
                  <div class="dashboard-legend-sample-color" style="background-color:#FD7927"></div>
                  <span><f:message key="requirement.criticality.CRITICAL" /></span>
                </div>
              </div>
            </div>
          </div>

          <div id="dashboard-item-bound-desc" class="dashboard-item dashboard-pie" data-def="model-attribute=boundDescriptionStatistics">

            <div id="dashboard-bound-description-view" class="dashboard-item-view">

            </div>

            <div class="dashboard-item-meta">
              <h2 class="dashboard-item-title"><f:message key="dashboard.requirements.bound-desc.title"/></h2>

              <div class="dashboard-item-legend">
                <div>
                  <div class="dashboard-legend-sample-color" style="background-color:#DFC3EF"></div>
                  <span><f:message key="dashboard.requirements.bound-desc.legend.has-no-description" /></span>
                </div>
                <div>
                  <div class="dashboard-legend-sample-color" style="background-color:#993CCC"></div>
                  <span><f:message key="dashboard.requirements.bound-desc.legend.has-description" /></span>
                </div>
              </div>
            </div>
          </div>

          <div id="dashboard-item-coverage" class="dashboard-item" data-def="model-attribute=coverageStatistics">

			<div id="dashboard-coverage-view" class="dashboard-item-view"></div>

			<div class="dashboard-item-meta">

				<h2 class="dashboard-item-title"><f:message key="dashboard.requirements.coverage.title"/></h2>
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
						<span class="serie serie0"><f:message key="requirement.criticality.CRITICAL" /></span>
					</div>
					<div>
						<div class="dashboard-legend-icon dashboard-donut-lvl3"></div>
						<span class="serie serie1"><f:message key="requirement.criticality.MAJOR" /></span>
					</div>
					<div>
						<div class="dashboard-legend-icon dashboard-donut-lvl2"></div>
						<span class="serie serie2"><f:message key="requirement.criticality.MINOR" /></span>
					</div>
					<div>
						<div class="dashboard-legend-icon dashboard-donut-lvl1"></div>
						<span class="serie serie3"><f:message key="requirement.criticality.UNDEFINED" /></span>
					</div>

					<div>
						<div class="dashboard-legend-sample-color" style="background-color:#99CC00"></div>
						<span><f:message key="dashboard.requirements.coverage.covered" /></span>
					</div>
					<div>
						<div class="dashboard-legend-sample-color" style="background-color:#FF3300"></div>
						<span><f:message key="dashboard.requirements.coverage.notCovered" /></span>
					</div>
				</div>
		  	</div>
		  </div>

          <div id="dashboard-item-validation" class="dashboard-item" data-def="model-attribute=validationStatistics">

			<div id="dashboard-validation-view" class="dashboard-item-view"></div>

			<div class="dashboard-item-meta">

				<h2 class="dashboard-item-title"><f:message key="dashboard.requirements.validation.title"/></h2>
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
						<span class="serie serie0"><f:message key="requirement.criticality.CRITICAL" /></span>
					</div>
					<div>
						<div class="dashboard-legend-icon dashboard-donut-lvl3"></div>
						<span class="serie serie1"><f:message key="requirement.criticality.MAJOR" /></span>
					</div>
					<div>
						<div class="dashboard-legend-icon dashboard-donut-lvl2"></div>
						<span class="serie serie2"><f:message key="requirement.criticality.MINOR" /></span>
					</div>
					<div>
						<div class="dashboard-legend-icon dashboard-donut-lvl1"></div>
						<span class="serie serie3"><f:message key="requirement.criticality.UNDEFINED" /></span>
					</div>

					<div>
						<div class="dashboard-legend-sample-color" style="background-color:#99CC00"></div>
						<span><f:message key="dashboard.requirements.validation.CONCLUSIVE" /></span>
					</div>
					<div>
						<div class="dashboard-legend-sample-color" style="background-color:#FF3300"></div>
						<span><f:message key="dashboard.requirements.validation.INCONCLUSIVE" /></span>
					</div>
					<div>
						<div class="dashboard-legend-sample-color" style="background-color:#969696"></div>
						<span><f:message key="dashboard.requirements.validation.UNDEFINED" /></span>
					</div>
				</div>
		  	</div>
		  </div>

          <div class="unsnap"> </div>
          <span class="dashboard-summary"><f:message key="dashboard.requirements.summary"/></span>

        </div>

        <div class="dashboard-figleaf-notready" style="text-align : center">
          <h3 class="dashboard-figleaf-notready-title"><f:message key="dashboard.notready.title"/></h3>
        </div>
      </div>
    </div>
  </div>
</div>
