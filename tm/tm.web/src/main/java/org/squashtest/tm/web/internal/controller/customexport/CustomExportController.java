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
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.CustomFieldBinding;
import org.squashtest.tm.domain.customreport.CustomReportCustomExport;
import org.squashtest.tm.domain.customreport.CustomReportLibraryNode;
import org.squashtest.tm.domain.customreport.CustomReportNodeType;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.campaign.CampaignFinder;
import org.squashtest.tm.service.customfield.CustomFieldBindingFinderService;
import org.squashtest.tm.service.customfield.CustomFieldFinderService;
import org.squashtest.tm.service.customfield.CustomFieldValueFinderService;
import org.squashtest.tm.service.customreport.CustomReportCustomExportCSVService;
import org.squashtest.tm.service.customreport.CustomReportCustomExportModificationService;
import org.squashtest.tm.service.customreport.CustomReportLibraryNodeService;
import org.squashtest.tm.service.internal.dto.CustomFieldBindingModel;
import org.squashtest.tm.service.internal.dto.CustomFieldJsonConverter;
import org.squashtest.tm.web.internal.helper.JsonHelper;
import org.squashtest.tm.web.internal.http.ContentTypes;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.builder.JsonProjectBuilder;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("custom-exports")
public class CustomExportController {

	@Inject
	InternationalizationHelper i18nHelper;
	@Inject
	private CustomReportLibraryNodeService reportLibraryNodeService;
	@Inject
	private CustomReportCustomExportModificationService customExportModificationService;
	@Inject
	private CustomReportCustomExportCSVService csvExportService;
	@Inject
	private CampaignFinder campaignFinder;
	@Inject
	private JsonProjectBuilder jsonProjectBuilder;
	@Inject
	private CustomFieldFinderService cufService;
	@Inject
	private CustomFieldValueFinderService cufValueService;
	@Inject
	private CustomFieldBindingFinderService cufBindingService;
	@Inject
	private CustomFieldJsonConverter customFieldConverter;


	@RequestMapping("/wizard/{parentId}")
	public ModelAndView getWizard(@PathVariable Long parentId, Locale locale) {
		ModelAndView mav = new ModelAndView("custom-exports/wizard/wizard.html");

		CustomReportLibraryNode crln = reportLibraryNodeService.findCustomReportLibraryNodeById(parentId);

		if(crln.getEntityType().getTypeName().equals(CustomReportNodeType.CUSTOM_EXPORT_NAME)) {
			CustomReportCustomExport customExportDefinition = (CustomReportCustomExport) crln.getEntity();
			mav.addObject("customExportDefinition", JsonHelper.serialize(customExportDefinition));
			mav.addObject("scopeCampaignName", getScopeCampaignName(customExportDefinition.getScope().get(0)));
			mav.addObject("availableCustomFields", getCustomFieldBindingsData(customExportDefinition.getScope().get(0).getId()));
		}

		mav.addObject("parentId", parentId);
		return mav;
	}

	private String getScopeCampaignName(EntityReference entity) {
		return campaignFinder.findById(entity.getId()).getName();
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
		CustomExportCSVHelper csvHelper = new CustomExportCSVHelper(csvExportService,cufService, cufValueService, i18nHelper, locale);

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
	public Map<String, List<CustomFieldBindingModel>> getCustomFieldBindingsData(@RequestParam Long campaignId) {
		Campaign campaign = campaignFinder.findById(campaignId);
		Project mainProject = campaign.getProject();
		long mainProjectId = mainProject.getId();

		// Main Map
		Map<String, List<CustomFieldBindingModel>> map = jsonProjectBuilder.buildProjectCufBindingsMap(mainProjectId);

		// Need to add the Cufs bound to the linked Test Cases
		List<Iteration> iterations = campaign.getIterations();

		// Get the ids of the projects of the test case linked to the given campaign (excluding the main project)
		List<Long> projectIds = iterations.stream()
			.map(Iteration::getTestPlans)
			.flatMap(Collection::stream)
			.map(itpi -> {
				// for deleted TestCases
				TestCase testCase = itpi.getReferencedTestCase();
				if(testCase != null) {
					return testCase.getProject().getId();
				} else {
					return null;
				}
			})
			.distinct()
			.filter(projectId -> projectId!= null && !projectId.equals(mainProjectId))
			.collect(Collectors.toList());

		for(Long projectId : projectIds) {
			List<CustomFieldBinding> cufs = cufBindingService.findCustomFieldsForProjectAndEntity(projectId, BindableEntity.TEST_CASE);
			for(CustomFieldBinding binding : cufs) {
				map.get(BindableEntity.TEST_CASE.toString()).add(customFieldConverter.toJson(binding));
			}
		}
		return map;
	}
}
