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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.search.AdvancedSearchListFieldModel;
import org.squashtest.tm.domain.search.AdvancedSearchModel;
import org.squashtest.tm.domain.search.AdvancedSearchSingleFieldModel;
import org.squashtest.tm.service.campaign.CampaignAdvancedSearchService;
import org.squashtest.tm.service.internal.dto.UserDto;
import org.squashtest.tm.service.internal.dto.json.JsonProject;
import org.squashtest.tm.service.milestone.ActiveMilestoneHolder;
import org.squashtest.tm.service.project.ProjectFinder;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.user.UserAccountService;
import org.squashtest.tm.service.workspace.WorkspaceDisplayService;
import org.squashtest.tm.web.internal.controller.search.advanced.searchinterface.SearchInputInterfaceHelper;
import org.squashtest.tm.web.internal.controller.search.advanced.searchinterface.SearchInputInterfaceModel;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Created by jsimon on 04/05/2016.
 */

@RequestMapping("advanced-search")
public abstract class GlobalSearchController {

	protected static final String PROJECTS_META = "projects";
	protected static final Logger LOGGER = LoggerFactory.getLogger(GlobalSearchController.class);
	protected static final String NAME = "name";
	protected static final String IDS = "ids[]";
	protected static final String CAMPAIGN = "campaign";
	protected static final String TESTCASE = "test-case";
	protected static final String REQUIREMENT = "requirement";
	protected static final String SEARCH_MODEL = "searchModel";
	protected static final String FORM_MODEL = "formModel";
	protected static final String SEARCH_DOMAIN = "searchDomain";
	protected static final String TESTCASE_VIA_REQUIREMENT = "testcaseViaRequirement";
	protected static final String RESULTS = "/results";
	protected static final String TABLE = "/table";
	protected Map<String, FormModelBuilder> formModelBuilder = new HashMap<>();

	@Inject
	private SearchInputInterfaceHelper searchInputInterfaceHelper;
	@Inject
	private InternationalizationHelper messageSource;
	@Inject
	private PermissionEvaluationService permissionService;
	@Inject
	private ActiveMilestoneHolder activeMilestoneHolder;
	@Inject
	private CampaignAdvancedSearchService campaignAdvancedSearchService;
	@Inject
	private UserAccountService userAccountService;
	@Inject
	private ProjectFinder projectFinder;

	{
		formModelBuilder.put(TESTCASE, new FormModelBuilder() {
			@Override
			public SearchInputInterfaceModel build(Locale locale, boolean isMilestoneMode) {
				UserDto currentUser = userAccountService.findCurrentUserDto();
				List<Long> readableProjectIds = projectFinder.findAllReadableIds(currentUser);
				Collection<JsonProject> jsProjects = projectFinder.findAllProjects(readableProjectIds, currentUser);
				Integer allowAutomationWorkflow = projectFinder.countProjectsAllowAutomationWorkflow();
				SearchInputInterfaceModel model = searchInputInterfaceHelper.getTestCaseSearchInputInterfaceModel(locale,
					isMilestoneMode, readableProjectIds, jsProjects, allowAutomationWorkflow);
				populateMetadata(model, jsProjects);
				return model;
			}
		});

		formModelBuilder.put(TESTCASE_VIA_REQUIREMENT, new FormModelBuilder() {
			@Override
			public SearchInputInterfaceModel build(Locale locale, boolean isMilestoneMode) {
				UserDto currentUser = userAccountService.findCurrentUserDto();
				List<Long> readableProjectIds = projectFinder.findAllReadableIds(currentUser);
				Collection<JsonProject> jsProjects = projectFinder.findAllProjects(readableProjectIds, currentUser);
				Integer allowAutomationWorkflow = projectFinder.countProjectsAllowAutomationWorkflow();
				SearchInputInterfaceModel model = searchInputInterfaceHelper.
					getRequirementSearchInputInterfaceModel(locale, isMilestoneMode, readableProjectIds, jsProjects, allowAutomationWorkflow);
				populateMetadata(model, jsProjects);
				return model;
			}
		});

		formModelBuilder.put(CAMPAIGN, new FormModelBuilder() {
			@Override
			public SearchInputInterfaceModel build(Locale locale, boolean isMilestoneMode) {
				UserDto currentUser = userAccountService.findCurrentUserDto();
				List<Long> readableProjectIds = projectFinder.findAllReadableIds(currentUser);
				Collection<JsonProject> jsProjects = projectFinder.findAllProjects(readableProjectIds, currentUser);
				Integer allowAutomationWorkflow = projectFinder.countProjectsAllowAutomationWorkflow();
				SearchInputInterfaceModel model = searchInputInterfaceHelper.getCampaignSearchInputInterfaceModel(locale,
					isMilestoneMode, readableProjectIds, allowAutomationWorkflow);
				populateMetadata(model, jsProjects);
				return model;
			}
		});

		formModelBuilder.put(REQUIREMENT, new FormModelBuilder() {
			@Override
			public SearchInputInterfaceModel build(Locale locale, boolean isMilestoneMode) {
				UserDto currentUser = userAccountService.findCurrentUserDto();
				List<Long> readableProjectIds = projectFinder.findAllReadableIds(currentUser);
				Collection<JsonProject> jsProjects = projectFinder.findAllProjects(readableProjectIds, currentUser);
				Integer allowAutomationWorkflow = projectFinder.countProjectsAllowAutomationWorkflow();
				SearchInputInterfaceModel model = searchInputInterfaceHelper
					.getRequirementSearchInputInterfaceModel(locale, isMilestoneMode, readableProjectIds, jsProjects, allowAutomationWorkflow);
				populateMetadata(model, jsProjects);
				return model;
			}
		});
	}

	public InternationalizationHelper getMessageSource() {
		return messageSource;
	}

	public PermissionEvaluationService getPermissionService() {
		return permissionService;
	}

	public ActiveMilestoneHolder getActiveMilestoneHolder() {
		return activeMilestoneHolder;
	}

	protected void initSearchPageModel(Model model,  String searchModel, String associationType, Long associationId, String domain) {
		
		initModelForPage(model, associationType, associationId);
		
		model.addAttribute(SEARCH_DOMAIN, domain);

		FormModelBuilder builder = formModelBuilder.get(domain);
		if (builder != null) {
			Locale locale = LocaleContextHolder.getLocale();
			Optional<Milestone> activeMilestone = activeMilestoneHolder.getActiveMilestone();
			model.addAttribute(FORM_MODEL, builder.build(locale, activeMilestone.isPresent()));
		} 
		else {
			LOGGER.error(
				"Could not find a FormModelBuilder for search domain : {}. This is either caused by a bug or a hand-written request",
				domain);
		}
		if (!searchModel.isEmpty()) {
			model.addAttribute(SEARCH_MODEL, searchModel);
		}

	}

	protected void initResultModel(Model model, String searchModel, String associationType, Long associationId, String domain) {
				
		initModelForPage(model, associationType, associationId);
		if (!searchModel.isEmpty()) {
			model.addAttribute(SEARCH_MODEL, searchModel);
		}
		model.addAttribute(SEARCH_DOMAIN, domain);
		populateMetadata(model);
	}

	protected void addMilestoneToSearchModel(AdvancedSearchModel searchModel) {
		// yes this is a list field for only one value ! But this allow us to handle milestone mode same as reference
		// mode

		Optional<Milestone> activeMilestone = activeMilestoneHolder.getActiveMilestone();

		if (activeMilestone.isPresent()) {
			AdvancedSearchListFieldModel milestoneIds = new AdvancedSearchListFieldModel();
			AdvancedSearchSingleFieldModel activeMilestoneMode = new AdvancedSearchSingleFieldModel();

			List<String> milestones = new ArrayList<>();
			milestones.add(activeMilestone.get().getId().toString());

			milestoneIds.setValues(milestones);
			activeMilestoneMode.setValue("true");

			searchModel.addField("milestones.id", milestoneIds);
			searchModel.addField("searchByMilestone", activeMilestoneMode);
		}
	}

	protected boolean isInAssociationContext(String associationType) {
		return associationType != null;
	}

	private void initModelForPage(Model model, String associationType, Long associationId) {
		Optional<Milestone> activeMilestone = activeMilestoneHolder.getActiveMilestone();
		model.addAttribute("isMilestoneMode", activeMilestone.isPresent());
		
		Integer projectsAllowAtuomationWorkflow = projectFinder.countProjectsAllowAutomationWorkflow();
		Boolean automationColVisible = false;
		if (projectsAllowAtuomationWorkflow > 0) {
			automationColVisible = true;
		}
		
		model.addAttribute("automationColVisible", automationColVisible);
		if (StringUtils.isNotBlank(associationType)) {
			model.addAttribute("associateResult", true);
			model.addAttribute("associationType", associationType);
			model.addAttribute("associationId", associationId);
		} 
		else {
			model.addAttribute("associateResult", false);
		}
	}

	private void populateMetadata(Model model) {
		model.addAttribute(PROJECTS_META, readableJsonProjects());
	}

	private void populateMetadata(SearchInputInterfaceModel model, Collection<JsonProject> jsProjects) {
		model.addMetadata(PROJECTS_META, jsProjects);
	}

	private Collection<JsonProject> readableJsonProjects() {

		UserDto currentUser = new UserDto(null, null, null, true);
		return projectFinder.findAllProjects(campaignAdvancedSearchService.findAllReadablesId(), currentUser);
	}

	protected abstract WorkspaceDisplayService workspaceDisplayService();

	protected interface FormModelBuilder {
		SearchInputInterfaceModel build(Locale locale, boolean isMilestoneMode);
	}

}

