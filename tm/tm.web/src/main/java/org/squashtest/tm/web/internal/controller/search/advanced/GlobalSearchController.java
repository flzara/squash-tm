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

import java.util.*;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.search.AdvancedSearchListFieldModel;
import org.squashtest.tm.domain.search.AdvancedSearchModel;
import org.squashtest.tm.domain.search.AdvancedSearchSingleFieldModel;
import org.squashtest.tm.service.campaign.CampaignAdvancedSearchService;
import org.squashtest.tm.service.internal.campaign.CampaignWorkspaceDisplayService;
import org.squashtest.tm.service.internal.dto.UserDto;
import org.squashtest.tm.service.milestone.ActiveMilestoneHolder;
import org.squashtest.tm.service.project.ProjectFinder;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.testcase.TestCaseAdvancedSearchService;
import org.squashtest.tm.service.user.UserAccountService;
import org.squashtest.tm.service.workspace.WorkspaceDisplayService;
import org.squashtest.tm.web.internal.controller.search.advanced.searchinterface.SearchInputInterfaceHelper;
import org.squashtest.tm.web.internal.controller.search.advanced.searchinterface.SearchInputInterfaceModel;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.service.internal.dto.json.JsonProject;

import java.util.Optional;

/**
 * Created by jsimon on 04/05/2016.
 */

@RequestMapping("/advanced-search")
public abstract class GlobalSearchController {

	@Inject
	private SearchInputInterfaceHelper searchInputInterfaceHelper;

	@Inject
	protected InternationalizationHelper messageSource;

	@Inject
	protected PermissionEvaluationService permissionService;

	@Inject
	protected ActiveMilestoneHolder activeMilestoneHolder;

	@Inject
	private CampaignAdvancedSearchService campaignAdvancedSearchService;

	@Inject
	protected UserAccountService userAccountService;


	@Inject
	private ProjectFinder projectFinder;

	protected static final String PROJECTS_META = "projects";
	protected static final Logger LOGGER = LoggerFactory.getLogger(GlobalSearchController.class);

	protected interface FormModelBuilder {
		SearchInputInterfaceModel build(Locale locale, boolean isMilestoneMode);
	}

	protected static final String NAME = "name";
	protected static final String IDS = "ids[]";
	protected static final String CAMPAIGN = "campaign";
	protected static final String TESTCASE = "test-case";
	protected static final String REQUIREMENT = "requirement";
	protected static final String SEARCH_MODEL = "searchModel";
	protected static final String SEARCH_DOMAIN = "searchDomain";
	protected static final String TESTCASE_VIA_REQUIREMENT = "testcaseViaRequirement";
	protected static final String RESULTS = "/results";
	protected static final String TABLE = "/table";
	protected static final String INPUT = "/input";

	protected Map<String, FormModelBuilder> formModelBuilder = new HashMap<>();

	{
				formModelBuilder.put(TESTCASE, new FormModelBuilder() {
			@Override
			public SearchInputInterfaceModel build(Locale locale, boolean isMilestoneMode) {
				UserDto currentUser = userAccountService.findCurrentUserDto();
				List<Long> readableProjectIds = projectFinder.findAllReadableIds(currentUser);
				Collection<JsonProject> jsProjects = workspaceDisplayService().findAllProjects(readableProjectIds, currentUser);

				SearchInputInterfaceModel model = searchInputInterfaceHelper.getTestCaseSearchInputInterfaceModel(locale,
						isMilestoneMode, currentUser,readableProjectIds,jsProjects);
				populateMetadata(model,jsProjects);
				return model;
			}
		});

		formModelBuilder.put(TESTCASE_VIA_REQUIREMENT, new FormModelBuilder() {
			@Override
			public SearchInputInterfaceModel build(Locale locale, boolean isMilestoneMode) {
				UserDto currentUser = userAccountService.findCurrentUserDto();
				List<Long> readableProjectIds = projectFinder.findAllReadableIds(currentUser);
				Collection<JsonProject> jsProjects = workspaceDisplayService().findAllProjects(readableProjectIds, currentUser);

				SearchInputInterfaceModel model = searchInputInterfaceHelper.getRequirementSearchInputInterfaceModel(locale,
					isMilestoneMode,currentUser,readableProjectIds,jsProjects);
				populateMetadata(model,jsProjects);
				return model;
			}
		});

		formModelBuilder.put(CAMPAIGN, new FormModelBuilder() {
			@Override
			public SearchInputInterfaceModel build(Locale locale, boolean isMilestoneMode) {
				UserDto currentUser = userAccountService.findCurrentUserDto();
				List<Long> readableProjectIds = projectFinder.findAllReadableIds(currentUser);
				Collection<JsonProject> jsProjects = workspaceDisplayService().findAllProjects(readableProjectIds, currentUser);
				SearchInputInterfaceModel model = searchInputInterfaceHelper.getCampaignSearchInputInterfaceModel(locale,
						isMilestoneMode,currentUser,readableProjectIds,jsProjects);
				populateMetadata(model,jsProjects);
				return model;
			}
		});

		formModelBuilder.put(REQUIREMENT, new FormModelBuilder() {
			@Override
			public SearchInputInterfaceModel build(Locale locale, boolean isMilestoneMode) {
				UserDto currentUser = userAccountService.findCurrentUserDto();
				List<Long> readableProjectIds = projectFinder.findAllReadableIds(currentUser);
				Collection<JsonProject> jsProjects = workspaceDisplayService().findAllProjects(readableProjectIds, currentUser);

				SearchInputInterfaceModel model = searchInputInterfaceHelper
						.getRequirementSearchInputInterfaceModel(locale, isMilestoneMode,currentUser,readableProjectIds,jsProjects);
				populateMetadata(model,jsProjects);
				return model;
			}
		});
	}


	protected void initModel(Model model, String associateResultWithType, Long id, Locale locale, String domain,Optional<Milestone> activeMilestone){
		initModelForPage(model, associateResultWithType, id,activeMilestone);
		model.addAttribute(SEARCH_DOMAIN, domain);

		FormModelBuilder builder = formModelBuilder.get(domain);
		if (builder != null) {
			model.addAttribute("formModel", builder.build(locale, activeMilestone.isPresent()));
		} else {
			LOGGER.error(
				"Could not find a FormModelBuilder for search domain : {}. This is either caused by a bug or a hand-written request",
				domain);
		}

	}


	protected void initResultModel(Model model, String searchModel, String associateResultWithType, Long id, String domain,Optional<Milestone> activeMilestone){
		initModelForPage(model, associateResultWithType, id,activeMilestone);
		model.addAttribute(SEARCH_MODEL, searchModel);
		model.addAttribute(SEARCH_DOMAIN, domain);
		populateMetadata(model);
	}


	protected void addMilestoneToSearchModel(AdvancedSearchModel searchModel) {
		// yes this is a list field for only one value ! But this allow us to handle milestone mode same as reference
		// mode

		Optional<Milestone> activeMilestone = activeMilestoneHolder.getActiveMilestone();

		if (activeMilestone.isPresent()) {
			AdvancedSearchListFieldModel model = new AdvancedSearchListFieldModel();
			AdvancedSearchSingleFieldModel activeMilestoneMode = new AdvancedSearchSingleFieldModel();
			List<String> milestones = new ArrayList<>();
			milestones.add(activeMilestone.get().getId().toString());
			model.setValues(milestones);
			activeMilestoneMode.setValue("true");
			searchModel.addField("milestones.id", model);
			searchModel.addField("activeMilestoneMode",activeMilestoneMode);
		}
	}

	protected boolean isInAssociationContext(String associateResultWithType) {
		return associateResultWithType != null;
	}

	private void initModelForPage(Model model, String associateResultWithType, Long id,Optional<Milestone> activeMilestone ) {
		model.addAttribute("isMilestoneMode", activeMilestone.isPresent());
		if (StringUtils.isNotBlank(associateResultWithType)) {
			model.addAttribute("associateResult", true);
			model.addAttribute("associateResultWithType", associateResultWithType);
			model.addAttribute("associateId", id);
		} else {
			model.addAttribute("associateResult", false);
		}
	}

	private void populateMetadata(Model model) {
		model.addAttribute("projects", readableJsonProjects());
	}

	private void populateMetadata(SearchInputInterfaceModel model,Collection<JsonProject> jsProjects) {
		model.addMetadata(PROJECTS_META, jsProjects);
	}

	private Collection<JsonProject> readableJsonProjects() {

		UserDto currentUser = new UserDto(null,null,null,true);
		return  workspaceDisplayService().findAllProjects(campaignAdvancedSearchService.findAllReadablesId(), currentUser);
	}
	protected abstract WorkspaceDisplayService workspaceDisplayService();

}

