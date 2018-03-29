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
import org.squashtest.tm.domain.customreport.CustomReportReportBinding;
import org.squashtest.tm.domain.report.ReportDefinition;
import org.squashtest.tm.service.report.ReportModificationService;
import org.squashtest.tm.web.internal.controller.report.JsonReportInstance;
import org.squashtest.tm.web.internal.helper.ReportHelper;
import org.squashtest.tm.web.internal.model.json.JsonCustomReportReportBinding;
import org.squashtest.tm.web.internal.report.ReportsRegistry;

import javax.inject.Inject;

/**
 * @author zyang
 */

@Component("customReport.reportBindingBuilder")
@Scope("prototype")
public class JsonCustomReportReportBindingBuilder {

	private ReportModificationService reportService;

	@Inject
	private ReportHelper reportHelper;

	@Inject
	private ReportsRegistry reportsRegistry;

	private JsonCustomReportReportBinding json = new JsonCustomReportReportBinding();

	@Inject
	public JsonCustomReportReportBindingBuilder(ReportModificationService reportService) {
		super();
		this.reportService = reportService;
	}

	public JsonCustomReportReportBinding build(CustomReportReportBinding binding) {
		ReportDefinition reportDefinition = binding.getReport();
		Report report = reportsRegistry.findReport(reportDefinition.getPluginNamespace());

		json.setId(binding.getId());
		json.setReportDefinitionId(reportDefinition.getId());
		json.setDashboardId(binding.getDashboard().getId());
		json.setCol(binding.getCol());
		json.setRow(binding.getRow());
		json.setSizeX(binding.getSizeX());
		json.setSizeY(binding.getSizeY());

		JsonReportInstance jsonReportInstance = new JsonReportInstance(reportDefinition);
		jsonReportInstance.setLabel(report.getLabel());
		jsonReportInstance.setReportAttributes(reportHelper.getAttributesFromReportDefinition(reportDefinition));
		json.setReportInstance(jsonReportInstance);
		return json;
	}

}
