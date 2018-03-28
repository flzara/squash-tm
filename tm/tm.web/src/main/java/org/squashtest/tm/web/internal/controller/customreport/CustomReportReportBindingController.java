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
package org.squashtest.tm.web.internal.controller.customreport;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.squashtest.tm.domain.customreport.CustomReportDashboard;
import org.squashtest.tm.domain.customreport.CustomReportReportBinding;
import org.squashtest.tm.domain.report.ReportDefinition;
import org.squashtest.tm.service.customreport.CustomReportDashboardService;
import org.squashtest.tm.service.customreport.CustomReportLibraryNodeService;
import org.squashtest.tm.web.internal.model.builder.JsonCustomReportReportBindingBuilder;
import org.squashtest.tm.web.internal.model.json.FormCustomReportReportBinding;
import org.squashtest.tm.web.internal.model.json.JsonCustomReportReportBinding;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.io.IOException;


@Controller
public class CustomReportReportBindingController {

	@Inject
	private CustomReportLibraryNodeService crlnservice;

	@Inject
	private CustomReportDashboardService dashboardService;

	@Inject
	@Named("customReport.reportBindingBuilder")
	private Provider<JsonCustomReportReportBindingBuilder> builderProvider;

	@ResponseBody
	@RequestMapping(value = "/custom-report-report-binding", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.CREATED)
	public JsonCustomReportReportBinding createCustomReportReportBinding(@RequestBody FormCustomReportReportBinding formReportBinding) throws IOException {
		CustomReportReportBinding crrb = formReportBinding.convertToEntity();

		//setting the nested entities.
		ReportDefinition reportDefinition = crlnservice.findReportDefinitionByNodeId(formReportBinding.getReportNodeId());
		CustomReportDashboard dashboard = crlnservice.findCustomReportDashboardById(formReportBinding.getDashboardNodeId());
		crrb.setReport(reportDefinition);
		crrb.setDashboard(dashboard);

		//do binding and return.
		dashboardService.bindReport(crrb);
		return builderProvider.get().build(crrb);
	}

}
