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
package org.squashtest.tm.web.internal.controller.customexport;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.tm.domain.EntityReference;
import org.squashtest.tm.domain.EntityType;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.customreport.CustomReportCustomExport;
import org.squashtest.tm.domain.customreport.CustomReportLibraryNode;
import org.squashtest.tm.domain.customreport.CustomReportNodeType;
import org.squashtest.tm.service.campaign.CustomCampaignModificationService;
import org.squashtest.tm.service.campaign.IterationFinder;
import org.squashtest.tm.service.campaign.TestSuiteFinder;
import org.squashtest.tm.service.customfield.CustomFieldFinderService;
import org.squashtest.tm.service.customfield.CustomFieldValueFinderService;
import org.squashtest.tm.service.customreport.CustomReportCustomExportCSVService;
import org.squashtest.tm.service.customreport.CustomReportCustomExportService;
import org.squashtest.tm.service.customreport.CustomReportCustomExportModificationService;
import org.squashtest.tm.service.customreport.CustomReportLibraryNodeService;
import org.squashtest.tm.service.internal.dto.CustomFieldBindingModel;
import org.squashtest.tm.service.internal.repository.ExecutionStepDao;
import org.squashtest.tm.web.internal.helper.JsonHelper;
import org.squashtest.tm.web.internal.http.ContentTypes;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.builder.JsonProjectBuilder;

import javax.inject.Inject;
import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("custom-exports")
public class CustomExportController {

	@Inject
	private InternationalizationHelper i18nHelper;
	@Inject
	private CustomReportLibraryNodeService reportLibraryNodeService;
	@Inject
	private CustomReportCustomExportModificationService customExportModificationService;
	@Inject
	private CustomReportCustomExportService customExportService;
	@Inject
	private CustomReportCustomExportCSVService csvExportService;
	@Inject
	private CustomCampaignModificationService customCampaignModificationService;
	@Inject
	private IterationFinder iterationFinder;
	@Inject
	private TestSuiteFinder testSuiteFinder;
	@Inject
	private JsonProjectBuilder jsonProjectBuilder;
	@Inject
	private CustomFieldFinderService cufService;
	@Inject
	private CustomFieldValueFinderService cufValueService;
	@Inject
	private ExecutionStepDao executionStepDao;


	@RequestMapping("/wizard/{parentId}")
	public ModelAndView getWizard(@PathVariable Long parentId, Locale locale) {
		ModelAndView mav = new ModelAndView("custom-exports/wizard/wizard.html");

		CustomReportLibraryNode crln = reportLibraryNodeService.findCustomReportLibraryNodeById(parentId);

		if (crln.getEntityType().getTypeName().equals(CustomReportNodeType.CUSTOM_EXPORT_NAME)) {
			CustomReportCustomExport customExportDefinition = (CustomReportCustomExport) crln.getEntity();
			mav.addObject("customExportDefinition", JsonHelper.serialize(customExportDefinition));
			mav.addObject("scopeEntityName", getScopeEntityName(customExportDefinition.getScope().get(0)));
			mav.addObject("availableCustomFields", getCustomFieldBindingsData(customExportDefinition.getScope().get(0).getType(), customExportDefinition.getScope().get(0).getId()));
		}

		mav.addObject("parentId", parentId);
		return mav;
	}

	private String getScopeEntityName(EntityReference entityReference) {
		String scopeEntityName = customExportService.getScopeEntityName(entityReference);
		return scopeEntityName.isEmpty() ?
			i18nHelper.internationalize("wizard.perimeter.msg.perimeter.choose", LocaleContextHolder.getLocale()) : scopeEntityName;
	}

	@ResponseBody
	@RequestMapping(value = "/new/{parentNodeId}", method = RequestMethod.POST, consumes = ContentTypes.APPLICATION_JSON)
	public String createNewCustomExport(@RequestBody @Valid CustomReportCustomExport customExport, @PathVariable("parentNodeId") long parentNodeId) {
		CustomReportLibraryNode newNode = reportLibraryNodeService.createNewNode(parentNodeId, customExport);
		return String.valueOf(newNode.getId());
	}

	@ResponseBody
	@RequestMapping(value = "/update/{nodeId}", method = RequestMethod.POST, consumes = ContentTypes.APPLICATION_JSON)
	public String updateCustomExport(@RequestBody CustomReportCustomExport modifiedCustomExport, @PathVariable("nodeId") long nodeId) {
		customExportModificationService.updateCustomExport(nodeId, modifiedCustomExport);
		return String.valueOf(nodeId);
	}

	@ResponseBody
	@RequestMapping(value = "/generate/{customExportId}", method = RequestMethod.GET)
	public FileSystemResource generateCustomExport(@PathVariable long customExportId, HttpServletResponse response, Locale locale) {
		CustomReportCustomExport customExport = reportLibraryNodeService.findCustomExportByNodeId(customExportId);

		// prepare the response
		response.setContentType("application/octet-stream");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");

		response.setHeader(
			"Content-Disposition",
			"attachment; filename=" + "EXPORT_" + customExport.getName().replace(" ", "_") + "_" + sdf.format(new Date()) + ".csv");

		File exported = createCustomExportFile(customExport, locale);
		return new FileSystemResource(exported);
	}

	private File createCustomExportFile(CustomReportCustomExport customExport, Locale locale) {
		File file;
		PrintWriter writer = null;
		CustomExportCSVHelper csvHelper = new CustomExportCSVHelper(csvExportService,cufService, cufValueService, i18nHelper, locale, executionStepDao);

		try {
			file = File.createTempFile("custom-export", "tmp");
			file.deleteOnExit();
			writer = new PrintWriter(file);
			// print headers
			writer.write(csvHelper.getInternationalizedHeaders(customExport));
			// print the data
			writer.write(csvHelper.getWritableRowsData(customExport));
			writer.close();
			return file;
		} catch (IOException ioEx) {
			throw new RuntimeException(ioEx);
		} finally {
			if(writer != null) {
				writer.close();
			}
		}
	}

	@ResponseBody
	@RequestMapping(value = "/cuf-data", method = RequestMethod.GET)
	public Map<String, List<CustomFieldBindingModel>> getCustomFieldBindingsData(@RequestParam EntityType entityType, @RequestParam Long entityId) {
		Long mainProjectId = null;
		List<IterationTestPlanItem> itpis = null;

		switch (entityType) {
			case CAMPAIGN:
				Campaign campaign = customCampaignModificationService.findCampaigWithExistenceCheck(entityId);
				if (campaign != null) {
					itpis = campaign.getIterations().stream()
						.map(Iteration::getTestPlans)
						.flatMap(Collection::stream).collect(Collectors.toList());
					mainProjectId = campaign.getProject().getId();
				}
				break;
			case ITERATION:
				Iteration iteration = iterationFinder.findById(entityId);
				if (iteration != null) {
					mainProjectId = iteration.getProject().getId();
					itpis = iteration.getTestPlans();
				}
				break;
			case TEST_SUITE:
				try {
					TestSuite testSuite = testSuiteFinder.findById(entityId);
					mainProjectId = testSuite.getId();
					itpis = testSuite.getTestPlan();
					break;
				} catch (EntityNotFoundException e) {
					return new HashMap<>();
				}
			default:
				throw new IllegalArgumentException("Entity of type " + entityType.name() + " is not supported");
		}
		return mainProjectId != null ? getCustomFieldsData(mainProjectId, itpis) : new HashMap<>();
	}

	private Map<String, List<CustomFieldBindingModel>> getCustomFieldsData(Long mainProjectId, List<IterationTestPlanItem> itpis) {
		// Main Map
		Map<String, List<CustomFieldBindingModel>> map = jsonProjectBuilder.buildProjectCufBindingsMap(mainProjectId);

		return customExportService.getCustomFieldsData(mainProjectId, itpis, map);
	}
}
