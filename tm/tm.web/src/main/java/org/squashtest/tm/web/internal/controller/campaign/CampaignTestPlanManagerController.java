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
package org.squashtest.tm.web.internal.controller.campaign;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.apache.commons.collections.MultiMap;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.tm.core.foundation.collection.ColumnFiltering;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndMultiSorting;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.testcase.Dataset;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.service.campaign.CampaignTestPlanManagerService;
import org.squashtest.tm.service.campaign.IndexedCampaignTestPlanItem;
import org.squashtest.tm.service.internal.dto.UserDto;
import org.squashtest.tm.service.internal.dto.json.JsonMilestone;
import org.squashtest.tm.service.milestone.ActiveMilestoneHolder;
import org.squashtest.tm.service.milestone.MilestoneModelService;
import org.squashtest.tm.service.user.UserAccountService;
import org.squashtest.tm.service.workspace.WorkspaceDisplayService;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.controller.milestone.MilestoneFeatureConfiguration;
import org.squashtest.tm.web.internal.controller.milestone.MilestoneUIConfigurationService;
import org.squashtest.tm.web.internal.helper.JsTreeHelper;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.builder.DriveNodeBuilder;
import org.squashtest.tm.web.internal.model.builder.JeditableComboHelper;
import org.squashtest.tm.web.internal.model.builder.JsTreeNodeListBuilder;
import org.squashtest.tm.web.internal.model.datatable.DataTableColumnFiltering;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelConstants;
import org.squashtest.tm.web.internal.model.datatable.DataTableMultiSorting;
import org.squashtest.tm.web.internal.model.jquery.TestPlanAssignableUser;
import org.squashtest.tm.service.internal.dto.json.JsTreeNode;
import org.squashtest.tm.web.internal.model.viewmapper.DatatableMapper;
import org.squashtest.tm.web.internal.model.viewmapper.NameBasedMapper;

import java.util.Optional;

/**
 * @author Agnes Durand
 */
@Controller
public class CampaignTestPlanManagerController {

	private static final String TESTCASES_IDS_REQUEST_PARAM = "testCasesIds[]";

	@Inject
	@Named("testCase.driveNodeBuilder")
	private Provider<DriveNodeBuilder<TestCaseLibraryNode>> driveNodeBuilder;

	@Inject
	private CampaignTestPlanManagerService testPlanManager;

	@Inject
	private InternationalizationHelper messageSource;

	@Inject
	private ActiveMilestoneHolder activeMilestoneHolder;

	@Inject
	private MilestoneUIConfigurationService milestoneConfService;

	@Inject
	private WorkspaceDisplayService testCaseWorkspaceDisplayService;

	@Inject
	protected UserAccountService userAccountService;

	@Inject
	protected MilestoneModelService milestoneModelService;

	private final DatatableMapper<String> testPlanMapper = new NameBasedMapper()
		.map("entity-index", "index(CampaignTestPlanItem)")
		.mapAttribute(DataTableModelConstants.PROJECT_NAME_KEY, "name", Project.class)
		.mapAttribute("reference", "reference", TestCase.class)
		.mapAttribute("tc-name", "name", TestCase.class)
		.mapAttribute("dataset.selected.name", "name", Dataset.class)
		.mapAttribute("assigned-user", "login", User.class)
		.mapAttribute("importance", "importance", TestCase.class)
		.mapAttribute("exec-mode", "automatedTest", TestCase.class)
		.map("milestone-dates", "endDate");


	@RequestMapping(value = "/campaigns/{campaignId}/test-plan/manager", method = RequestMethod.GET)
	public ModelAndView showManager(@PathVariable long campaignId,
									@CookieValue(value = "jstree_open", required = false, defaultValue = "") String[] openedNodes) {


		Campaign campaign = testPlanManager.findCampaign(campaignId);
//		List<TestCaseLibrary> linkableLibraries = testPlanManager.findLinkableTestCaseLibraries();
		MilestoneFeatureConfiguration milestoneConf = milestoneConfService.configure(campaign);

//		List<JsTreeNode> linkableLibrariesModel = createLinkableLibrariesModel(linkableLibraries, openedNodes);

		MultiMap expansionCandidates = JsTreeHelper.mapIdsByType(openedNodes);
		UserDto currentUser = userAccountService.findCurrentUserDto();

		List<Long> linkableRequirementLibraryIds = testPlanManager.findLinkableTestCaseLibraries().stream()
			.map(TestCaseLibrary::getId).collect(Collectors.toList());
		Optional<Long> activeMilestoneId = activeMilestoneHolder.getActiveMilestoneId();
		Collection<JsTreeNode> linkableLibrariesModel = testCaseWorkspaceDisplayService.findAllLibraries(linkableRequirementLibraryIds, currentUser, expansionCandidates, activeMilestoneId.get());

		ModelAndView mav = new ModelAndView("page/campaign-workspace/show-campaign-test-plan-manager");
		mav.addObject("campaign", campaign);
		mav.addObject("linkableLibrariesModel", linkableLibrariesModel);
		mav.addObject("milestoneConf", milestoneConf);

		return mav;
	}


	@ResponseBody
	@RequestMapping(value = "campaigns/{campaignId}/test-plan", params = RequestParams.S_ECHO_PARAM)
	public DataTableModel getTestCasesTableModel(@PathVariable(RequestParams.CAMPAIGN_ID) long campaignId,
												 final DataTableDrawParameters params, final Locale locale) {

		DataTableMultiSorting sorter = new DataTableMultiSorting(params, testPlanMapper);

		ColumnFiltering filter = new DataTableColumnFiltering(params);

		PagedCollectionHolder<List<IndexedCampaignTestPlanItem>> holder = testPlanManager.findTestPlan(campaignId, sorter, filter);

		return new CampaignTestPlanTableModelHelper(messageSource, locale).buildDataModel(holder, params.getsEcho());
	}


	@ResponseBody
	@RequestMapping(value = "/campaigns/{campaignId}/test-plan", method = RequestMethod.POST,
		params = TESTCASES_IDS_REQUEST_PARAM)
	public void addTestCasesToCampaign(@RequestParam(TESTCASES_IDS_REQUEST_PARAM) List<Long> testCasesIds,
									   @PathVariable long campaignId) {
		testPlanManager.addTestCasesToCampaignTestPlan(testCasesIds, campaignId);
	}

	@ResponseBody
	@RequestMapping(value = "/campaigns/{campaignId}/test-plan/{testPlanIds}", method = RequestMethod.DELETE)
	public void removeItemsFromTestPlan(@PathVariable(RequestParams.CAMPAIGN_ID) long campaignId,
										@PathVariable("testPlanIds") List<Long> itemsIds) {
		testPlanManager.removeTestPlanItems(campaignId, itemsIds);
	}


	private List<JsTreeNode> createLinkableLibrariesModel(List<TestCaseLibrary> linkableLibraries,
														  String[] openedNodes) {
		MultiMap expansionCandidates = JsTreeHelper.mapIdsByType(openedNodes);

		DriveNodeBuilder<TestCaseLibraryNode> dNodeBuilder = driveNodeBuilder.get();

		Optional<Milestone> activeMilestone = activeMilestoneHolder.getActiveMilestone();

		if (activeMilestone.isPresent()) {
			dNodeBuilder.filterByMilestone(activeMilestone.get());
		}

		JsTreeNodeListBuilder<TestCaseLibrary> listBuilder = new JsTreeNodeListBuilder<>(dNodeBuilder);


		return listBuilder.expand(expansionCandidates).setModel(linkableLibraries).build();
	}

	@ResponseBody
	@RequestMapping(value = "/campaigns/{campaignId}/test-plan/{itemId}/assign-user", method = RequestMethod.POST, params = "userId")
	public void assignUserToCampaignTestPlanItem(@PathVariable long itemId, @PathVariable long campaignId,
												 @RequestParam long userId) {
		testPlanManager.assignUserToTestPlanItem(itemId, campaignId, userId);
	}


	@ResponseBody
	@RequestMapping(value = "/campaigns/{campaignId}/assignable-users", method = RequestMethod.GET)
	public List<TestPlanAssignableUser> getAssignUserForCampaignTestPlanItem(
		@PathVariable(RequestParams.CAMPAIGN_ID) long campaignId, final Locale locale) {

		List<User> usersList = testPlanManager.findAssignableUserForTestPlan(campaignId);

		String unassignedLabel = formatUnassigned(locale);
		List<TestPlanAssignableUser> jsonUsers = new LinkedList<>();

		jsonUsers.add(new TestPlanAssignableUser(User.NO_USER_ID.toString(), unassignedLabel));

		for (User user : usersList) {
			jsonUsers.add(new TestPlanAssignableUser(user));
		}

		return jsonUsers;
	}


	@ResponseBody
	@RequestMapping(value = "/campaigns/{campaignId}/test-plan/{testPlanIds}", method = RequestMethod.POST, params = {"assignee"})
	public Long assignUserToCampaignTestPlanItem(@PathVariable("testPlanIds") List<Long> testPlanIds, @PathVariable(RequestParams.CAMPAIGN_ID) long campaignId,
												 @RequestParam("assignee") long assignee) {
		testPlanManager.assignUserToTestPlanItems(testPlanIds, campaignId, assignee);
		return assignee;
	}

	@RequestMapping(value = "/campaigns/{campaignId}/test-plan/{itemIds}/position/{newIndex}", method = RequestMethod.POST)
	@ResponseBody
	public void moveTestPlanItems(@PathVariable(RequestParams.CAMPAIGN_ID) long campaignId, @PathVariable("newIndex") int newIndex, @PathVariable("itemIds") List<Long> itemIds) {
		testPlanManager.moveTestPlanItems(campaignId, newIndex, itemIds);
	}


	@ResponseBody
	@RequestMapping(value = "/campaigns/{campaignId}/test-plan/{testPlanId}", method = RequestMethod.POST, params = {"dataset"})
	public Long setDataset(@PathVariable("testPlanId") long testPlanId, @RequestParam("dataset") Long datasetId) {
		testPlanManager.changeDataset(testPlanId, JeditableComboHelper.coerceIntoEntityId(datasetId));
		return datasetId;
	}


	/**
	 * Will reorder the test plan according to the current sorting instructions.
	 *
	 * @param campaignId
	 * @return
	 */
	@RequestMapping(value = "/campaigns/{campaignId}/test-plan/order", method = RequestMethod.POST)
	@ResponseBody
	public void reorderTestPlan(@PathVariable(RequestParams.CAMPAIGN_ID) long campaignId, DataTableDrawParameters parameters) {

		PagingAndMultiSorting sorting = new DataTableMultiSorting(parameters, testPlanMapper);
		testPlanManager.reorderTestPlan(campaignId, sorting);
	}

	private String formatUnassigned(Locale locale) {
		return messageSource.internationalize("label.Unassigned", locale);
	}

}
