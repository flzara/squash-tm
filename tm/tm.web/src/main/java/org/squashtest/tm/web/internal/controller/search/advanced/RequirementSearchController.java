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
package org.squashtest.tm.web.internal.controller.search.advanced;

import java.io.IOException;
import java.util.*;

import javax.inject.Inject;

import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndMultiSorting;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.search.AdvancedSearchModel;
import org.squashtest.tm.service.requirement.RequirementVersionAdvancedSearchService;
import org.squashtest.tm.service.requirement.VerifiedRequirement;
import org.squashtest.tm.service.requirement.VerifiedRequirementsManagerService;
import org.squashtest.tm.service.workspace.WorkspaceDisplayService;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.controller.search.advanced.tablemodels.RequirementSearchResultDataTableModelBuilder;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelConstants;
import org.squashtest.tm.web.internal.model.datatable.DataTableMultiSorting;
import org.squashtest.tm.web.internal.model.viewmapper.DatatableMapper;
import org.squashtest.tm.web.internal.model.viewmapper.NameBasedMapper;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by jsimon on 04/05/2016.
 */
@Controller
public class RequirementSearchController extends GlobalSearchController {


	@Inject
	private VerifiedRequirementsManagerService verifiedRequirementsManagerService;

	@Inject
	private RequirementVersionAdvancedSearchService requirementVersionAdvancedSearchService;

	@Inject
	private WorkspaceDisplayService requirementWorkspaceDisplayService;

	private DatatableMapper<String> requirementSearchResultMapper = new NameBasedMapper(14)
		.mapAttribute(DataTableModelConstants.PROJECT_NAME_KEY, NAME, Project.class)
		.mapAttribute("requirement-id", "requirement.id", RequirementVersion.class)
		.mapAttribute("requirement-reference", "reference", RequirementVersion.class)
		.mapAttribute("requirement-label", "labelUpperCased", RequirementVersion.class)
		.mapAttribute("requirement-criticality", "criticality", RequirementVersion.class)
		.mapAttribute("requirement-category", "category", RequirementVersion.class)
		.mapAttribute("requirement-status", "status", RequirementVersion.class)
		.mapAttribute("requirement-milestone-nb", "milestones", RequirementVersion.class)
		.mapAttribute("requirement-version", "versionNumber", RequirementVersion.class)
		.mapAttribute("requirement-version-nb", "versions", Requirement.class)
		.mapAttribute("requirement-testcase-nb", "testcases", RequirementVersion.class)
		.mapAttribute("requirement-attachment-nb", "attachments", RequirementVersion.class)
		.mapAttribute("requirement-created-by", "createdBy", RequirementVersion.class)
		.mapAttribute("requirement-modified-by", "lastModifiedBy", RequirementVersion.class);



	@RequestMapping(value = RESULTS, params = REQUIREMENT)
	public String getRequirementSearchResultPage(Model model, @RequestParam String searchModel,
			@RequestParam(required = false) String associateResultWithType, @RequestParam(required = false) Long id) {

		Optional<Milestone> activeMilestone = activeMilestoneHolder.getActiveMilestone();
		initResultModel(model, searchModel, associateResultWithType, id, REQUIREMENT,activeMilestone);
		return "requirement-search-result.html";
	}


	@RequestMapping(method = RequestMethod.GET, params = "searchDomain=requirement")
	public String showRequirementSearchPage(Model model,
										 @RequestParam(required = false, defaultValue = "") String associateResultWithType,
										 @RequestParam(required = false, defaultValue = "") Long id, Locale locale) {

		Optional<Milestone> activeMilestone = activeMilestoneHolder.getActiveMilestone();
		initModel(model, associateResultWithType, id, locale, REQUIREMENT,activeMilestone);
		return  "requirement-search-input.html";
	}


	@RequestMapping(method = RequestMethod.POST, params = "searchDomain=requirement")
	public String showRequirementSearchPageFilledWithParams(Model model,
														 @RequestParam String searchModel, @RequestParam(required = false) String associateResultWithType,
														 @RequestParam(required = false) Long id, Locale locale) {

		model.addAttribute(SEARCH_MODEL, searchModel);
		return showRequirementSearchPage(model, associateResultWithType, id, locale);
	}


	@RequestMapping(value = TABLE, method = RequestMethod.POST, params = { RequestParams.MODEL, REQUIREMENT,
		RequestParams.S_ECHO_PARAM })
	@ResponseBody
	public DataTableModel getRequirementTableModel(final DataTableDrawParameters params, final Locale locale,
												   @RequestParam(value = RequestParams.MODEL) String model,
												   @RequestParam(required = false) String associateResultWithType, @RequestParam(required = false) Long id)
		throws IOException {

		AdvancedSearchModel searchModel = new ObjectMapper().readValue(model, AdvancedSearchModel.class);

		addMilestoneToSearchModel(searchModel);

		PagingAndMultiSorting paging = new DataTableMultiSorting(params, requirementSearchResultMapper);

		PagedCollectionHolder<List<RequirementVersion>> holder = requirementVersionAdvancedSearchService
			.searchForRequirementVersions(searchModel, paging, messageSource, locale);

		boolean isInAssociationContext = isInAssociationContext(associateResultWithType);

		Set<Long> ids = null;

		if (isInAssociationContext) {
			ids = getIdsOfRequirementsAssociatedWithObjects(associateResultWithType, id);
		}

		return new RequirementSearchResultDataTableModelBuilder(locale, messageSource, permissionService,
			isInAssociationContext, ids).buildDataModel(holder, params.getsEcho());
	}

	private Set<Long> getIdsOfRequirementsAssociatedWithObjects(String associateResultWithType, Long id) {

		Set<Long> ids = new HashSet<>();

		if (TESTCASE.equals(associateResultWithType)) {
			List<VerifiedRequirement> requirements = verifiedRequirementsManagerService
				.findAllVerifiedRequirementsByTestCaseId(id);
			for (VerifiedRequirement requirement : requirements) {
				ids.add(requirement.getId());
			}
		}

		return ids;
	}

	@Override
	protected WorkspaceDisplayService workspaceDisplayService() {
		return requirementWorkspaceDisplayService;
	}

}
