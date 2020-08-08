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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.search.AdvancedSearchModel;
import org.squashtest.tm.domain.search.AdvancedSearchQueryModel;
import org.squashtest.tm.service.requirement.RequirementVersionAdvancedSearchService;
import org.squashtest.tm.service.requirement.VerifiedRequirement;
import org.squashtest.tm.service.requirement.VerifiedRequirementsManagerService;
import org.squashtest.tm.service.workspace.WorkspaceDisplayService;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.controller.search.advanced.tablemodels.RequirementSearchResultDataTableModelBuilder;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelConstants;
import org.squashtest.tm.web.internal.model.datatable.SpringPagination;
import org.squashtest.tm.web.internal.model.viewmapper.DatatableMapper;
import org.squashtest.tm.web.internal.model.viewmapper.NameBasedMapper;

import javax.inject.Inject;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

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
		.mapAttribute("requirement-modified-by", "lastModifiedBy", RequirementVersion.class)
		.mapAttribute("links", "links", RequirementVersion.class);


	// ************** the search page handlers *******************

	@RequestMapping(method = RequestMethod.GET, params = "searchDomain="+REQUIREMENT)
	public String showRequirementSearchPage(Model pageModel,
	                                        @RequestParam(required = false, defaultValue = "") String associationType,
	                                        @RequestParam(required = false) Long associationId) {

		initSearchPageModel(pageModel, "" , associationType, associationId, REQUIREMENT);
		return "requirement-search-input.html";
	}



	@RequestMapping(method = RequestMethod.POST, params = "searchDomain="+REQUIREMENT)
	public String showRequirementSearchPagedWithSearchModel(Model pageModel,
	                                                        @RequestParam String searchModel,
	                                                        @RequestParam(required = false, defaultValue = "") String associationType,
	                                                        @RequestParam(required = false) Long associationId) {

		initSearchPageModel(pageModel, searchModel, associationType, associationId, REQUIREMENT);
		return "requirement-search-input.html";
	}

	// ******************* the result page handlers ****************

	@RequestMapping(method = RequestMethod.POST, value = RESULTS, params = "searchDomain="+REQUIREMENT)
	public String showRequirementSearchResultPageWithSearchModel(Model pageModel,
                                                          @RequestParam String searchModel,
                                                          @RequestParam(required = false) String associationType,
                                                          @RequestParam(required = false) Long associationId) {

		initResultModel(pageModel, searchModel, associationType, associationId, REQUIREMENT);
		return "requirement-search-result.html";
	}


	@RequestMapping(method = RequestMethod.GET, value = RESULTS, params = "searchDomain="+REQUIREMENT)
	public String showRequirementSearchResultPage(Model pageModel,
	                                             @RequestParam(required = false) String associationType,
	                                             @RequestParam(required = false) Long associationId) {

		initResultModel(pageModel, "", associationType, associationId, REQUIREMENT);
		return "requirement-search-result.html";
	}



	// ********************* other methods **********************************

	@RequestMapping(value = TABLE, method = RequestMethod.POST, params = {RequestParams.MODEL, REQUIREMENT,
		RequestParams.S_ECHO_PARAM})
	@ResponseBody
	public DataTableModel getRequirementTableModel(final DataTableDrawParameters params, final Locale locale,
	                                               @RequestParam(value = RequestParams.MODEL) String model,
	                                               @RequestParam(required = false) String associationType,
	                                               @RequestParam(required = false) Long associationId)
		throws IOException {

		AdvancedSearchModel searchModel = new ObjectMapper().readValue(model, AdvancedSearchModel.class);

		addMilestoneToSearchModel(searchModel);

		Pageable paging = SpringPagination.pageable(params, requirementSearchResultMapper, (String key)-> key);

		AdvancedSearchQueryModel queryModel = new AdvancedSearchQueryModel(paging, requirementSearchResultMapper.getMappedKeys(), searchModel);

		Page<RequirementVersion> holder = requirementVersionAdvancedSearchService
			.searchForRequirementVersions(queryModel, paging, getMessageSource(), locale);

		boolean isInAssociationContext = isInAssociationContext(associationType);

		Set<Long> ids = null;

		if (isInAssociationContext) {
			ids = getIdsOfRequirementsAssociatedWithObjects(associationType, associationId);
		}

		return new RequirementSearchResultDataTableModelBuilder(locale, getMessageSource(), getPermissionService(),
			isInAssociationContext, ids).buildDataModel(holder, params.getsEcho());
	}

	private Set<Long> getIdsOfRequirementsAssociatedWithObjects(String associationType, Long id) {

		Set<Long> ids = new HashSet<>();

		if (TESTCASE.equals(associationType)) {
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
