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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.squashtest.tm.api.workspace.WorkspaceType;
import org.squashtest.tm.domain.customreport.CustomReportLibraryNode;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.requirement.RequirementCriticality;
import org.squashtest.tm.domain.requirement.RequirementStatus;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.domain.testcase.TestCaseStatus;
import org.squashtest.tm.service.bugtracker.BugTrackerFinderService;
import org.squashtest.tm.service.customreport.CustomReportLibraryNodeService;
import org.squashtest.tm.service.customreport.CustomReportWorkspaceService;
import org.squashtest.tm.service.infolist.InfoListModelService;
import org.squashtest.tm.service.internal.dto.UserDto;
import org.squashtest.tm.service.milestone.ActiveMilestoneHolder;
import org.squashtest.tm.service.milestone.MilestoneModelService;
import org.squashtest.tm.service.project.ProjectFinder;
import org.squashtest.tm.service.user.UserAccountService;
import org.squashtest.tm.service.workspace.WorkspaceDisplayService;
import org.squashtest.tm.service.workspace.WorkspaceHelperService;
import org.squashtest.tm.web.internal.helper.I18nLevelEnumInfolistHelper;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.builder.CustomReportTreeNodeBuilder;
import org.squashtest.tm.service.internal.dto.json.JsonMilestone;
import org.squashtest.tm.service.internal.dto.json.JsTreeNode;

import java.util.Optional;
import org.squashtest.tm.service.internal.dto.json.JsonProject;

/**
 * This controller is dedicated to the initial page of Custom Reports
 */
@Controller
@RequestMapping("/custom-report-workspace")
public class CustomReportWorkspaceController {

	public static final Logger LOGGER = LoggerFactory.getLogger(CustomReportWorkspaceController.class);

	private final String cookieDelimiter = "-";

	@Inject
	@Named("org.squashtest.tm.service.customreport.CustomReportWorkspaceService")
	private CustomReportWorkspaceService workspaceService;

	@Inject
	private CustomReportLibraryNodeService customReportLibraryNodeService;

	@Inject
	@Named("customReport.nodeBuilder")
	private Provider<CustomReportTreeNodeBuilder> builderProvider;

	@Inject
	private I18nLevelEnumInfolistHelper i18nLevelEnumInfolistHelper;

	@Inject
	protected InternationalizationHelper i18nHelper;

	@Inject
	private ActiveMilestoneHolder activeMilestoneHolder;

	@Inject
	protected ProjectFinder projectFinder;

	@Inject
	protected UserAccountService userAccountService;

	@Inject
	@Named("customReportWorkspaceDisplayService")
	private WorkspaceDisplayService workspaceDisplayService;


	@Inject
	private WorkspaceHelperService workspaceHelperService;

	@Inject
	private BugTrackerFinderService bugTrackerFinderService;


	@Inject
	private MilestoneModelService milestoneModelService;

	@Inject
	private InfoListModelService infoListModelService;

	@RequestMapping(method = RequestMethod.GET)
	public String showWorkspace(Model model, Locale locale,
			@CookieValue(value = "jstree_open", required = false, defaultValue = "") String[] openedNodes,
			@CookieValue(value = "jstree_select", required = false, defaultValue = "") String elementId) {

		List<CustomReportLibraryNode> libraries = workspaceService.findRootNodes();

		Set<Long> nodeIdToOpen = new HashSet<>();
		nodeIdToOpen.addAll(convertCookieIds(openedNodes));
		//Every node above selected node should be opened and it should be not necessary to get ancestors.
		//But we have corner cases like when we create a new chart in different screen...
		if (StringUtils.isNotBlank(elementId)) {
			nodeIdToOpen.addAll(findAncestorsOfselectedNode(elementId));
		}

		List<JsTreeNode> rootNodes = new ArrayList<>();

		for (CustomReportLibraryNode crl : libraries) {
			JsTreeNode treeNode = builderProvider.get().buildWithOpenedNodes(crl, nodeIdToOpen);
			rootNodes.add(treeNode);
		}

		model.addAttribute("rootModel", rootNodes);

		Optional<Long> activeMilestoneId = activeMilestoneHolder.getActiveMilestoneId();

		// also, milestones
		if (activeMilestoneId.isPresent()) {
			JsonMilestone jsMilestone =
				milestoneModelService.findMilestoneModel(activeMilestoneId.get());
			model.addAttribute("activeMilestone", jsMilestone);
		}

		UserDto currentUser = userAccountService.findCurrentUserDto();
		List<Long> readableProjectIds = projectFinder.findAllReadableIds(currentUser);
		Collection<JsonProject> projects = workspaceDisplayService.findAllProjects(readableProjectIds, currentUser);

		model.addAttribute("projects", projects);


		//defaults lists and enums levels
		model.addAttribute("defaultInfoLists", infoListModelService.findSystemInfoListItemLabels());
		model.addAttribute("testCaseImportance", i18nLevelEnumInfolistHelper.getI18nLevelEnum(TestCaseImportance.class,locale));
		model.addAttribute("testCaseStatus", i18nLevelEnumInfolistHelper.getI18nLevelEnum(TestCaseStatus.class,locale));
		model.addAttribute("requirementStatus", i18nLevelEnumInfolistHelper.getI18nLevelEnum(RequirementStatus.class,locale));
		model.addAttribute("requirementCriticality", i18nLevelEnumInfolistHelper.getI18nLevelEnum(RequirementCriticality.class,locale));
		model.addAttribute("executionStatus",
				i18nLevelEnumInfolistHelper.getI18nLevelEnum(ExecutionStatus.class, locale));

		model.addAttribute("projectFilter", workspaceHelperService.findFilterModel(currentUser, readableProjectIds));
		model.addAttribute("bugtrackers", bugTrackerFinderService.findDistinctBugTrackersForProjects(readableProjectIds));

		return getWorkspaceViewName();
	}

	private Collection<Long> findAncestorsOfselectedNode(String elementId) {
		List<Long> ancestorIds = new ArrayList<>();
		try {
			Long nodeId = convertCookieId(elementId);
			ancestorIds = customReportLibraryNodeService.findAncestorIds(nodeId);
			//The selected node isn't opened by default (it can be a leaf node !).
			//So it will be open ONLY if he's also in open node cookies.
			ancestorIds.remove(nodeId);
		} catch (NumberFormatException e) {
			LOGGER.error("Error on parsing js_open cookie. Workspace will be shown with closed tree");
		}
		return ancestorIds;
	}

	protected String getWorkspaceViewName() {
		return "custom-report-workspace.html";
	}

	/**
	 * @see org.squashtest.tm.web.internal.controller.generic.WorkspaceController#getWorkspaceType()
	 */
	protected WorkspaceType getWorkspaceType() {
		return WorkspaceType.CUSTOM_REPORT_WORKSPACE;
	}

	private Long convertCookieId(String cookieValue){
		String[] cookieSplits = cookieValue.split(cookieDelimiter);
		if (cookieSplits.length>0) {
			return Long.parseLong(cookieSplits[cookieSplits.length-1]);
		}
		return Long.parseLong(cookieValue);
	}

	private Set<Long> convertCookieIds(String[] cookieValues){
		Set<Long> nodeIdToOpen = new HashSet<>();
		for (String value : cookieValues) {
			try {
				nodeIdToOpen.add(convertCookieId(value));
			} catch (NumberFormatException e) {
				LOGGER.error("Error on parsing js_open cookie. Workspace will be shown with closed tree");
			}
		}
		return nodeIdToOpen;
	}

}
