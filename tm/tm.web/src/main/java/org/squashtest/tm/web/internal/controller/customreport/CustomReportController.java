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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.squashtest.tm.domain.EntityReference;
import org.squashtest.tm.domain.Workspace;
import org.squashtest.tm.domain.chart.ChartDefinition;
import org.squashtest.tm.domain.chart.ChartInstance;
import org.squashtest.tm.domain.customreport.CustomReportDashboard;
import org.squashtest.tm.domain.customreport.CustomReportFolder;
import org.squashtest.tm.domain.customreport.CustomReportLibrary;
import org.squashtest.tm.domain.customreport.CustomReportLibraryNode;
import org.squashtest.tm.domain.report.ReportDefinition;
import org.squashtest.tm.service.chart.ChartModificationService;
import org.squashtest.tm.service.customreport.CustomReportDashboardService;
import org.squashtest.tm.service.customreport.CustomReportLibraryNodeService;
import org.squashtest.tm.web.internal.controller.chart.JsonChartInstance;
import org.squashtest.tm.web.internal.model.builder.JsonCustomReportDashboardBuilder;
import org.squashtest.tm.web.internal.model.jquery.RenameModel;
import org.squashtest.tm.web.internal.model.json.JsonCustomReportDashboard;
import org.squashtest.tm.web.internal.model.json.JsonDynamicScope;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.util.List;
import java.util.Locale;

/**
 * This controller is dedicated to the contextual content of custom report (ie the right part of the screen)
 */
@Controller
public class CustomReportController {
	public static final Logger LOGGER = LoggerFactory.getLogger(CustomReportController.class);

	@Inject
	private CustomReportLibraryNodeService customReportLibraryNodeService;

	@Inject
	private ChartModificationService chartService;

	@Inject
	private CustomReportDashboardService dashboardService;

	@Inject
	@Named("customReport.dashboardBuilder")
	private Provider<JsonCustomReportDashboardBuilder> builderProvider;



	//---- SHOW DETAIL METHODS -----

	@ResponseBody
	@RequestMapping(value = "custom-report-library/{id}", method = RequestMethod.GET)
	public CustomReportLibrary getLibraryDetails(@PathVariable Long id){
		return customReportLibraryNodeService.findLibraryByTreeNodeId(id);
	}

	@ResponseBody
	@RequestMapping(value = "custom-report-folder/{id}", method = RequestMethod.GET)
	public CustomReportFolder getFolderDetails(@PathVariable Long id){
		return customReportLibraryNodeService.findFolderByTreeNodeId(id);
	}

	@ResponseBody
	@RequestMapping(value = "custom-report-chart/{id}", method = RequestMethod.GET)
	public JsonChartInstance getChartDetails(@PathVariable Long id){
		ChartDefinition chartDef = customReportLibraryNodeService.findChartDefinitionByNodeId(id);
		ChartInstance instance = chartService.generateChart(chartDef.getId(),null,null);
		return new JsonChartInstance(instance);
	}

	@ResponseBody
	@RequestMapping(value = "custom-report-report/{id}", method = RequestMethod.GET)
	public ReportDefinition getReportDetails(@PathVariable Long id){
		ReportDefinition ReportDef = customReportLibraryNodeService.findReportDefinitionByNodeId(id);
		return ReportDef;
	}

	@ResponseBody
	@RequestMapping(value = "custom-report-dashboard/{id}", method = RequestMethod.GET)
	public JsonCustomReportDashboard getDashboardDetails(@PathVariable Long id, Locale locale){
		CustomReportDashboard dashboard = customReportLibraryNodeService.findCustomReportDashboardById(id);
		return builderProvider.get().build(dashboard, locale);
	}

	@ResponseBody
	@RequestMapping(value = "custom-report-dashboard/{id}", method = RequestMethod.POST )
	public JsonCustomReportDashboard getDashboardDetailsWithDynamicScope(@PathVariable Long id, Locale locale, @RequestBody JsonDynamicScope dynamicScope){
		List<EntityReference> entityReferences = dynamicScope.convertToEntityReferences();
		CustomReportDashboard dashboard = customReportLibraryNodeService.findCustomReportDashboardById(id);
		Workspace workspace = Workspace.getWorkspaceFromShortName(dynamicScope.getWorkspaceName());

		return builderProvider.get().build(dashboard, locale, entityReferences, dynamicScope.isMilestoneDashboard(), workspace);
	}

	//---- RENAME ----

	@RequestMapping(method = RequestMethod.POST, value="custom-report-folders/{nodeId}",params = { "newName" })
	@ResponseBody
	public RenameModel renameCRF(@PathVariable long nodeId, @RequestParam String newName) {
		return renameNode(nodeId, newName);
	}

	@RequestMapping(method = RequestMethod.POST, value="custom-report-dashboard/{nodeId}",params = { "newName" })
	@ResponseBody
	public RenameModel renameCRD(@PathVariable long nodeId, @RequestParam String newName) {
		return renameNode(nodeId, newName);
	}

	@RequestMapping(method = RequestMethod.POST, value="custom-report-chart/{nodeId}",params = { "newName" })
	@ResponseBody
	public RenameModel renameChartDefinition(@PathVariable long nodeId, @RequestParam String newName) {
		return renameNode(nodeId, newName);
	}

	@RequestMapping(method = RequestMethod.POST, value="custom-report-report/{nodeId}",params = { "newName" })
	@ResponseBody
	public RenameModel renameReportDefinition(@PathVariable long nodeId, @RequestParam String newName) {
		return renameNode(nodeId, newName);
	}

	/**
	 * Change the favorite dashboard for current user, and keep the info in party preferences.
	 * @param nodeId This is the {@link CustomReportLibraryNode} id, not the {@link CustomReportDashboard} id.
     */
	@RequestMapping(method = RequestMethod.POST, value="custom-report-dashboard/favorite/{workspace}/{nodeId}")
	@ResponseBody
	public void changeFavoriteDashboard(@PathVariable long nodeId, @PathVariable String workspace) {
		Workspace workspaceId = Workspace.getWorkspaceFromShortName(workspace);
		dashboardService.chooseFavoriteDashboardForCurrentUser(workspaceId,nodeId);
	}

	private RenameModel renameNode (long nodeId, String newName){
		customReportLibraryNodeService.renameNode(nodeId, newName);
		return new RenameModel(newName);
	}

}
