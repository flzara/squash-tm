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
package org.squashtest.tm.web.internal.controller.chart;

import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.tm.domain.chart.ChartDefinition;
import org.squashtest.tm.domain.chart.ChartInstance;
import org.squashtest.tm.domain.customreport.CustomReportLibraryNode;
import org.squashtest.tm.domain.customreport.CustomReportNodeType;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.requirement.RequirementCriticality;
import org.squashtest.tm.domain.requirement.RequirementStatus;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.domain.testcase.TestCaseStatus;
import org.squashtest.tm.service.chart.ChartModificationService;
import org.squashtest.tm.service.customfield.CustomFieldBindingModificationService;
import org.squashtest.tm.service.customreport.CustomReportLibraryNodeService;
import org.squashtest.tm.service.infolist.InfoListFinderService;
import org.squashtest.tm.service.project.GenericProjectFinder;
import org.squashtest.tm.service.project.ProjectFinder;
import org.squashtest.tm.service.user.UserAccountService;
import org.squashtest.tm.web.internal.helper.I18nLevelEnumInfolistHelper;
import org.squashtest.tm.web.internal.helper.JsonHelper;
import org.squashtest.tm.web.internal.http.ContentTypes;
import org.squashtest.tm.web.internal.model.builder.JsonProjectBuilder;
import org.squashtest.tm.web.internal.model.json.JsonChartWizardData;

@Controller
@RequestMapping("charts")
public class ChartController {

	@Inject
	private UserAccountService userService;

	@Inject
	private ChartModificationService chartService;

	@Inject
	private CustomReportLibraryNodeService reportNodeService;

	@Inject
	private ProjectFinder projectFinder;

	@Inject
	private InfoListFinderService infoListFinder;

	@Inject
	private I18nLevelEnumInfolistHelper i18nLevelEnumInfolistHelper;

	@Inject
	private CustomFieldBindingModificationService cufBindingService;

	@Inject
	protected JsonProjectBuilder jsonProjectBuilder;


	@RequestMapping(method = RequestMethod.GET, produces = ContentTypes.APPLICATION_JSON)
	@ResponseBody
	public JsonChartWizardData getWizardData() {
		List<Project> readableProjects = projectFinder.findAllReadable();
		return new JsonChartWizardData(chartService.getColumnPrototypes(), readableProjects, infoListFinder,
				cufBindingService);
	}

	@RequestMapping(value = "/wizard/{parentId}", method = RequestMethod.GET)
	public ModelAndView getWizard(@PathVariable Long parentId, Locale locale) {
		ModelAndView mav = new ModelAndView("charts/wizard/wizard.html");

		CustomReportLibraryNode crln = reportNodeService.findCustomReportLibraryNodeById(parentId);

		if (crln.getEntityType().getTypeName().equals(CustomReportNodeType.CHART_NAME)) {
			ChartDefinition def = (ChartDefinition) crln.getEntity();

			mav.addObject("chartDef", JsonHelper.serialize(def));
		}

		GenericProject project = reportNodeService.findCustomReportLibraryNodeById(parentId).getCustomReportLibrary()
				.getProject();
		mav.addObject("parentId", parentId);
		mav.addObject("defaultProject", project.getId());

		//defaults lists and enums levels
		mav.addObject("defaultInfoLists", i18nLevelEnumInfolistHelper.getInternationalizedDefaultList(locale));
		mav.addObject("testCaseImportance", i18nLevelEnumInfolistHelper.getI18nLevelEnum(TestCaseImportance.class,locale));
		mav.addObject("testCaseStatus", i18nLevelEnumInfolistHelper.getI18nLevelEnum(TestCaseStatus.class,locale));
		mav.addObject("requirementStatus", i18nLevelEnumInfolistHelper.getI18nLevelEnum(RequirementStatus.class,locale));
		mav.addObject("requirementCriticality", i18nLevelEnumInfolistHelper.getI18nLevelEnum(RequirementCriticality.class,locale));
		mav.addObject("executionStatus", i18nLevelEnumInfolistHelper.getI18nLevelEnum(ExecutionStatus.class, locale));

		// project metadata
		mav.addObject("projectsMeta", jsonProjectBuilder.getExtendedReadableProjects());

		return mav;
	}

	@ResponseBody
	@RequestMapping(value = "/{definitionId}/instance", method = RequestMethod.GET)
	public JsonChartInstance generate(@PathVariable("definitionId") Long definitionId){
		ChartInstance instance = chartService.generateChart(definitionId,null,null);
		return new JsonChartInstance(instance);
	}

	@ResponseBody
	@RequestMapping(value = "/instance", method = RequestMethod.POST)
	public JsonChartInstance generate(@RequestBody @Valid ChartDefinition definition) {
		ChartInstance instance = chartService.generateChart(definition,null,null);
		return new JsonChartInstance(instance);
	}

	//as above but require a project id. We need it for squash tm 1.15 because now the default perimeter is the current
	//project of chart, so for the preview, the definition has no project, we need to pass one...
	@ResponseBody
	@RequestMapping(value = "/instance/{projectId}", method = RequestMethod.POST)
	public JsonChartInstance generateWithoutProject(@PathVariable("projectId") Long projectId, @RequestBody @Valid ChartDefinition definition) {
		ChartInstance instance = chartService.generateChart(definition,projectId);
		return new JsonChartInstance(instance);
	}



	@ResponseBody
	@RequestMapping(value = "/update/{id}", method = RequestMethod.POST, consumes = ContentTypes.APPLICATION_JSON)
	public String updateChartDefinition(@RequestBody @Valid ChartDefinition definition,
										@PathVariable("id") long id) {
		ChartDefinition oldDef = reportNodeService.findChartDefinitionByNodeId(id);
		chartService.updateDefinition(definition, oldDef);
		return String.valueOf(id);
	}

	@ResponseBody
	@RequestMapping(value = "/new/{id}", method = RequestMethod.POST, consumes = ContentTypes.APPLICATION_JSON)
	public String createNewChartDefinition(@RequestBody @Valid ChartDefinition definition,
										   @PathVariable("id") long id) {

		definition.setOwner(userService.findCurrentUser());
		CustomReportLibraryNode node = reportNodeService.createNewNode(id, definition);
		return node.getId().toString();
	}

}
