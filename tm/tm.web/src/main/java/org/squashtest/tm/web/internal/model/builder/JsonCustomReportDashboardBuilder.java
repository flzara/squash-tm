/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.web.internal.model.builder;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.squashtest.tm.api.report.Report;
import org.squashtest.tm.domain.EntityReference;
import org.squashtest.tm.domain.Workspace;
import org.squashtest.tm.domain.audit.AuditableMixin;
import org.squashtest.tm.domain.chart.ChartInstance;
import org.squashtest.tm.domain.customreport.CustomReportChartBinding;
import org.squashtest.tm.domain.customreport.CustomReportDashboard;
import org.squashtest.tm.domain.customreport.CustomReportReportBinding;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.report.ReportDefinition;
import org.squashtest.tm.service.chart.ChartModificationService;
import org.squashtest.tm.service.milestone.ActiveMilestoneHolder;
import org.squashtest.tm.web.internal.controller.chart.JsonChartInstance;
import org.squashtest.tm.web.internal.controller.report.JsonReportInstance;
import org.squashtest.tm.web.internal.helper.ReportHelper;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.i18n.MessageObject;
import org.squashtest.tm.web.internal.model.json.JsonCustomReportChartBinding;
import org.squashtest.tm.web.internal.model.json.JsonCustomReportDashboard;
import org.squashtest.tm.web.internal.model.json.JsonCustomReportReportBinding;
import org.squashtest.tm.web.internal.report.IdentifiedReportDecorator;
import org.squashtest.tm.web.internal.report.ReportsRegistry;

import javax.inject.Inject;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component("customReport.dashboardBuilder")
@Scope("prototype")
public class JsonCustomReportDashboardBuilder {

	private ChartModificationService chartService;

	private JsonCustomReportDashboard json = new JsonCustomReportDashboard();

	private CustomReportDashboard dashboard;

	private InternationalizationHelper i18nHelper;

	private List<EntityReference> scope;

	private String i18nKeyDateFormat = "squashtm.dateformat";

	private boolean isMilestoneDashboard =false;

	private Milestone milestone;

	private Workspace workspace;

	@Inject
	private ReportHelper reportHelper;

	@Inject
	private ReportsRegistry reportsRegistry;

	@Inject
	public JsonCustomReportDashboardBuilder(ChartModificationService chartService, InternationalizationHelper i18nHelper, ActiveMilestoneHolder activeMilestoneHolder) {
		super();
		this.chartService = chartService;
		this.i18nHelper = i18nHelper;
		this.milestone = activeMilestoneHolder.getActiveMilestone().orElse(null);
	}

	public JsonCustomReportDashboard build(CustomReportDashboard dashboard, Locale locale){
		return getJsonCustomReportDashboard(dashboard, locale);
	}

	public JsonCustomReportDashboard build(CustomReportDashboard dashboard, Locale locale, List<EntityReference> scope){
		this.scope = scope;
		return getJsonCustomReportDashboard(dashboard, locale);
	}

	public JsonCustomReportDashboard build(CustomReportDashboard dashboard, Locale locale, List<EntityReference> scope, boolean isMilestoneDashboard, Workspace workspace){
		this.scope = scope;
		this.isMilestoneDashboard = isMilestoneDashboard;
		this.workspace = workspace;
		return getJsonCustomReportDashboard(dashboard, locale);
	}

	private JsonCustomReportDashboard getJsonCustomReportDashboard(CustomReportDashboard dashboard, Locale locale) {
		this.dashboard = dashboard;
		doBaseAttributes();
		doBindings();
		doDateAttributes(locale);
		return json;
	}

	private void doBindings() {
		Set<CustomReportChartBinding> bindings = dashboard.getChartBindings();
		for (CustomReportChartBinding binding : bindings) {
			JsonCustomReportChartBinding jsonBinding = new JsonCustomReportChartBinding();
			jsonBinding.setId(binding.getId());
			jsonBinding.setDashboardId(dashboard.getId());
			jsonBinding.setChartDefinitionId(binding.getChart().getId());
			jsonBinding.setRow(binding.getRow());
			jsonBinding.setCol(binding.getCol());
			jsonBinding.setSizeX(binding.getSizeX());
			jsonBinding.setSizeY(binding.getSizeY());
			ChartInstance chartInstance;
			if(isMilestoneDashboard){
				chartInstance = chartService.generateChartForMilestoneDashboard(binding.getChart(),milestone.getId(),workspace);
			} else if(milestone != null) {
				chartInstance = chartService.generateChartInMilestoneMode(binding.getChart(),this.scope,workspace);
			} else {
				chartInstance = chartService.generateChart(binding.getChart(),this.scope,dashboard.getId());
			}
			jsonBinding.setChartInstance(new JsonChartInstance(chartInstance));
			json.getChartBindings().add(jsonBinding);
		}

		Set<CustomReportReportBinding> reportBindings = dashboard.getReportBindings();
		for (CustomReportReportBinding reportBinding : reportBindings) {
			ReportDefinition reportDefinition = reportBinding.getReport();
			Report report = reportsRegistry.findReport(reportDefinition.getPluginNamespace());

			JsonCustomReportReportBinding jsonBinding = new JsonCustomReportReportBinding();
			jsonBinding.setId(reportBinding.getId());
			jsonBinding.setDashboardId(dashboard.getId());
			jsonBinding.setReportDefinitionId(reportDefinition.getId());
			jsonBinding.setRow(reportBinding.getRow());
			jsonBinding.setCol(reportBinding.getCol());
			jsonBinding.setSizeX(reportBinding.getSizeX());
			jsonBinding.setSizeY(reportBinding.getSizeY());

			JsonReportInstance jsonReportInstance = new JsonReportInstance(reportDefinition);

			jsonReportInstance.setLabel(report.getLabel());
			jsonReportInstance.setReportAttributes(reportHelper.getAttributesFromReportDefinition(reportDefinition));
			jsonReportInstance.setDocx(((IdentifiedReportDecorator) report).isDocxTemplate());
			jsonReportInstance.setPdfViews(report.getViews().length);
			jsonBinding.setReportInstance(jsonReportInstance);
			json.getReportBindings().add(jsonBinding);
		}
	}

	private void doBaseAttributes() {
		json.setId(dashboard.getId());
		json.setName(dashboard.getName());
		AuditableMixin audit = (AuditableMixin) dashboard;//NOSONAR it's just for eclipse...
		json.setCreatedBy(audit.getCreatedBy());
		json.setLastModifiedBy(audit.getLastModifiedBy());
	}

	private void doDateAttributes(Locale locale) {
		AuditableMixin	audit = (AuditableMixin) dashboard;//NOSONAR it's just for eclipse...
		String dateFormat = findI18nDateFormat(locale);
		DateFormat formater = new SimpleDateFormat(dateFormat);
		json.setCreatedOn(formater.format(audit.getCreatedOn()));
		if (audit.getLastModifiedBy()!=null) {
			json.setLastModifiedOn(formater.format(audit.getLastModifiedOn()));
		}
		else {
			json.setLastModifiedOn("");
		}
	}

	private String findI18nDateFormat(Locale locale) {
		MessageObject message = new MessageObject();
		message.put(i18nKeyDateFormat, i18nKeyDateFormat);
		i18nHelper.resolve(message, locale);
		return (String) message.get(i18nKeyDateFormat);//NOSONAR it's a map <String,String>
	}
}
