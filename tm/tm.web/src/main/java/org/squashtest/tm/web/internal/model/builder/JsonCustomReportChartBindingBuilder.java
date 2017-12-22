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
import org.squashtest.tm.domain.chart.ChartInstance;
import org.squashtest.tm.domain.customreport.CustomReportChartBinding;
import org.squashtest.tm.service.chart.ChartModificationService;
import org.squashtest.tm.web.internal.controller.chart.JsonChartInstance;
import org.squashtest.tm.web.internal.model.json.JsonCustomReportChartBinding;

import javax.inject.Inject;

@Component("customReport.chartBindingBuilder")
@Scope("prototype")
public class JsonCustomReportChartBindingBuilder {

	private ChartModificationService chartService;

	private JsonCustomReportChartBinding json = new JsonCustomReportChartBinding();

	@Inject
	public JsonCustomReportChartBindingBuilder(ChartModificationService chartService) {
		super();
		this.chartService = chartService;
	}

	public JsonCustomReportChartBinding build(CustomReportChartBinding binding){
		json.setId(binding.getId());
		json.setChartDefinitionId(binding.getChart().getId());
		json.setDashboardId(binding.getDashboard().getId());
		json.setCol(binding.getCol());
		json.setRow(binding.getRow());
		json.setSizeX(binding.getSizeX());
		json.setSizeY(binding.getSizeY());
		ChartInstance chartInstance = chartService.generateChart(binding.getChart(),null,binding.getDashboard().getId());
		JsonChartInstance jsonChartInstance = new JsonChartInstance(chartInstance);
		json.setChartInstance(jsonChartInstance);

		return json;
	}

}
