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

import java.util.Optional;
import org.apache.commons.collections.MultiMap;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.tm.core.foundation.collection.ColumnFiltering;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndMultiSorting;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.testcase.Dataset;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.service.campaign.IndexedIterationTestPlanItem;
import org.squashtest.tm.service.campaign.IterationFinder;
import org.squashtest.tm.service.campaign.IterationTestPlanManagerService;
import org.squashtest.tm.service.internal.dto.UserDto;
import org.squashtest.tm.service.internal.dto.json.JsTreeNode;
import org.squashtest.tm.service.milestone.ActiveMilestoneHolder;
import org.squashtest.tm.service.milestone.MilestoneModelService;
import org.squashtest.tm.service.user.UserAccountService;
import org.squashtest.tm.service.workspace.WorkspaceDisplayService;
import org.squashtest.tm.web.internal.controller.AcceptHeaders;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.controller.milestone.MilestoneFeatureConfiguration;
import org.squashtest.tm.web.internal.controller.milestone.MilestoneUIConfigurationService;
import org.squashtest.tm.web.internal.helper.JsTreeHelper;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.builder.DriveNodeBuilder;
import org.squashtest.tm.web.internal.model.builder.JeditableComboHelper;
import org.squashtest.tm.web.internal.model.builder.JsTreeNodeListBuilder;
import org.squashtest.tm.web.internal.model.datatable.*;
import org.squashtest.tm.web.internal.model.jquery.TestPlanAssignableUser;
import org.squashtest.tm.web.internal.model.json.JsonIterationTestPlanItem;
import org.squashtest.tm.web.internal.model.json.JsonTestCase;
import org.squashtest.tm.web.internal.model.json.JsonTestCaseBuilder;
import org.squashtest.tm.web.internal.model.viewmapper.DatatableMapper;
import org.squashtest.tm.web.internal.model.viewmapper.NameBasedMapper;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 *
 * @author R.A
 */
@Controller
public class IterationTestPlanManagerController {

	private static final String TESTCASES_IDS_REQUEST_PARAM = "testCasesIds[]";

	private static final String ITPI_IDS_REQUEST_PARAM = "itpiIds[]";

	@Inject
	private IterationTestPlanManagerService iterationTestPlanManagerService;

	@Inject
	private InternationalizationHelper messageSource;

	@Inject
	@Named("testCase.driveNodeBuilder")
	private Provider<DriveNodeBuilder<TestCaseLibraryNode>> driveNodeBuilder;

	@Inject
	private IterationFinder iterationFinder;

	@Inject
	private Provider<JsonTestCaseBuilder> jsonTestCaseBuilder;



	@Inject
	private MilestoneUIConfigurationService milestoneConfService;

	@Inject
	private ActiveMilestoneHolder activeMilestoneHolder;

	@Inject
	private WorkspaceDisplayService testCaseWorkspaceDisplayService;

	@Inject
	protected UserAccountService userAccountService;

	@Inject
	protected MilestoneModelService milestoneModelService;

	private final DatatableMapper<String> testPlanMapper = new NameBasedMapper()
	.map("entity-index", "index(IterationTestPlanItem)")
	// index is a special case which means : no sorting.
	.mapAttribute(DataTableModelConstants.PROJECT_NAME_KEY, "name", Project.class).mapAttribute("reference", "reference", TestCase.class)
	.mapAttribute("tc-name", "name", TestCase.class).mapAttribute("importance", "importance", TestCase.class)
	.mapAttribute("dataset.selected.name", "name", Dataset.class)
	.mapAttribute("status", "executionStatus", IterationTestPlanItem.class)
	.mapAttribute("assignee-login", "login", User.class)
	.mapAttribute("last-exec-on", "lastExecutedOn", IterationTestPlanItem.class)
	.mapAttribute("exec-mode", "automatedTest", TestCase.class)
	.map("suite", "suitenames")
	.map("milestone-dates", "endDate");

	@RequestMapping(value = "/iterations/{iterationId}/test-plan-manager", method = RequestMethod.GET)
	public ModelAndView showManager(@PathVariable long iterationId,
			@CookieValue(value = "jstree_open", required = false, defaultValue = "") String[] openedNodes) {

		Iteration iteration = iterationFinder.findById(iterationId);
//		List<TestCaseLibrary> linkableLibraries = iterationTestPlanManagerService.findLinkableTestCaseLibraries();
//		List<JsTreeNode> linkableLibrariesModel = createLinkableLibrariesModel(linkableLibraries, openedNodes);
		MilestoneFeatureConfiguration milestoneConf = milestoneConfService.configure(iteration);
		ModelAndView mav = new ModelAndView("page/campaign-workspace/show-iteration-test-plan-manager");

		MultiMap expansionCandidates = JsTreeHelper.mapIdsByType(openedNodes);
		UserDto currentUser = userAccountService.findCurrentUserDto();

		List<Long> linkableRequirementLibraryIds = iterationTestPlanManagerService.findLinkableTestCaseLibraries().stream()
			.map(TestCaseLibrary::getId).collect(Collectors.toList());
		Optional<Long> activeMilestoneId = activeMilestoneHolder.getActiveMilestoneId();
		Collection<JsTreeNode> linkableLibrariesModel = testCaseWorkspaceDisplayService.findAllLibraries(linkableRequirementLibraryIds, currentUser, expansionCandidates, activeMilestoneId.get());

		mav.addObject("iteration", iteration);
		mav.addObject("baseURL", "/iterations/" + iterationId);
		mav.addObject("linkableLibrariesModel", linkableLibrariesModel);
		mav.addObject("milestoneConf", milestoneConf);

		return mav;
	}

	@ResponseBody
	@RequestMapping(value = "/iterations/{iterationId}/test-plan", params = RequestParams.S_ECHO_PARAM)
	public
	DataTableModel getTestPlanModel(@PathVariable long iterationId, final DataTableDrawParameters params,
			final Locale locale) {

		PagingAndMultiSorting paging = new DataTableMultiSorting(params, testPlanMapper);

		ColumnFiltering filter = new DataTableColumnFiltering(params);

		PagedCollectionHolder<List<IndexedIterationTestPlanItem>> holder =
				iterationTestPlanManagerService.findAssignedTestPlan(iterationId, paging, filter);

		return new TestPlanTableModelHelper(messageSource, locale).buildDataModel(holder, params.getsEcho());

	}

	@ResponseBody
	@RequestMapping(value = "/iterations/{iterationId}/test-plan", method = RequestMethod.POST, params = ITPI_IDS_REQUEST_PARAM)
	public void addIterationTestPlanItemToIteration(
			@RequestParam(ITPI_IDS_REQUEST_PARAM) List<Long> iterationTestPlanIds, @PathVariable long iterationId) {

		iterationTestPlanManagerService.copyTestPlanItems(iterationTestPlanIds, iterationId);

	}

	@ResponseBody
	@RequestMapping(value = "/iterations/{iterationId}/test-plan", method = RequestMethod.POST, params = TESTCASES_IDS_REQUEST_PARAM)
	public
	void addTestCasesToIteration(@RequestParam(TESTCASES_IDS_REQUEST_PARAM) List<Long> testCasesIds,
			@PathVariable long iterationId) {
		iterationTestPlanManagerService.addTestCasesToIteration(testCasesIds, iterationId);
	}

	/**
	 * Fetches and returns a list of json test cases from an iteration id
	 *
	 * @param iterationId
	 *            : the id of an {@link Iteration}
	 * @return the list of {@link JsonTestCase} representing the iteration's planned test-cases
	 *
	 */
	@ResponseBody
	@RequestMapping(value = "/iterations/{iterationId}/test-cases", method = RequestMethod.GET, headers = AcceptHeaders.CONTENT_JSON)
	public
	List<JsonTestCase> getJsonTestCases(@PathVariable long iterationId, Locale locale) {
		List<TestCase> testCases = iterationFinder.findPlannedTestCases(iterationId);
		return jsonTestCaseBuilder.get().locale(locale).entities(testCases).toJson();
	}

	/***
	 * Method called when you drag a test case and change its position in the selected iteration
	 *
	 * @param iterationId
	 *            : the iteration owning the moving test plan items
	 *
	 * @param itemIds
	 *            the ids of the items we are trying to move
	 *
	 * @param newIndex
	 *            the new position of the first of them
	 */
	@RequestMapping(value = "/iterations/{iterationId}/test-plan/{itemIds}/position/{newIndex}", method = RequestMethod.POST)
	@ResponseBody
	public void moveTestPlanItems(@PathVariable("iterationId") long iterationId,
			@PathVariable("newIndex") int newIndex, @PathVariable("itemIds") List<Long> itemIds) {
		iterationTestPlanManagerService.changeTestPlanPosition(iterationId, newIndex, itemIds);

	}

	/**
	 * Will reorder the test plan according to the current sorting instructions.
	 *
	 * @param iterationId
	 * @return
	 */
	@RequestMapping(value = "/iterations/{iterationId}/test-plan/order", method = RequestMethod.POST)
	@ResponseBody
	public void reorderTestPlan(@PathVariable("iterationId") long iterationId, DataTableDrawParameters parameters) {

		PagingAndMultiSorting sorting = new DataTableMultiSorting(parameters, testPlanMapper);
		iterationTestPlanManagerService.reorderTestPlan(iterationId, sorting);
	}

	@ResponseBody
	@RequestMapping(value = "/iterations/{iterationId}/test-plan/{testPlanItemsIds}", method = RequestMethod.DELETE)
	public
	Boolean removeTestPlanItemsFromIteration(@PathVariable("testPlanItemsIds") List<Long> testPlanItemsIds,
			@PathVariable long iterationId) {
		// check if a test plan item was already executed and therefore not removed
		return iterationTestPlanManagerService.removeTestPlansFromIteration(testPlanItemsIds, iterationId);
	}

	private List<JsTreeNode> createLinkableLibrariesModel(List<TestCaseLibrary> linkableLibraries,
			String[] openedNodes) {


		MultiMap expansionCandidates = JsTreeHelper.mapIdsByType(openedNodes);

		DriveNodeBuilder<TestCaseLibraryNode> dNodeBuilder = driveNodeBuilder.get();

		Optional<Milestone> activeMilestone = activeMilestoneHolder.getActiveMilestone();
		if (activeMilestone.isPresent()) {
			dNodeBuilder.filterByMilestone(activeMilestone.get());
		}

		JsTreeNodeListBuilder<TestCaseLibrary> listBuilder = new JsTreeNodeListBuilder<>(
				driveNodeBuilder.get());

		return listBuilder.expand(expansionCandidates).setModel(linkableLibraries).build();
	}

	@ResponseBody
	@RequestMapping(value = "/iterations/{iterationId}/assignable-users", method = RequestMethod.GET)
	public
	List<TestPlanAssignableUser> getAssignUserForIterationTestPlanItem(@PathVariable long iterationId,
			final Locale locale) {

		List<User> usersList = iterationTestPlanManagerService.findAssignableUserForTestPlan(iterationId);

		String unassignedLabel = formatUnassigned(locale);
		List<TestPlanAssignableUser> jsonUsers = new LinkedList<>();

		jsonUsers.add(new TestPlanAssignableUser(User.NO_USER_ID.toString(), unassignedLabel));

		for (User user : usersList) {
			jsonUsers.add(new TestPlanAssignableUser(user));
		}

		return jsonUsers;
	}

	@ResponseBody
	@RequestMapping(value = "/iterations/{iterationId}/test-plan/{testPlanIds}", method = RequestMethod.POST, params = {"assignee"})
	public
	Long assignUserToIterationTestPlanItem(@PathVariable("testPlanIds") List<Long> testPlanIds,
			@RequestParam("assignee") long assignee) {
		iterationTestPlanManagerService.assignUserToTestPlanItems(testPlanIds, assignee);
		return assignee;
	}

	@ResponseBody
	@RequestMapping(value = "/iterations/{iterationId}/test-plan/{testPlanIds}", method = RequestMethod.POST, params = {"status"})
	public
	JsonIterationTestPlanItem editStatusOfIterationTestPlanItems(@PathVariable("testPlanIds") List<Long> testPlanIds,
			@RequestParam("status") String status) {
		List<IterationTestPlanItem> itpis = iterationTestPlanManagerService.forceExecutionStatus(testPlanIds, status);
		return createJsonITPI(itpis.get(0));

	}

	@ResponseBody
	@RequestMapping(value = "/iterations/test-plan/{testPlanIds}", method = RequestMethod.POST, params = {"status"})
	public void editIterationTestPlanItemsStatus(@PathVariable("testPlanIds") List<Long> testPlanIds,
												 @RequestParam("status") String status) {
		iterationTestPlanManagerService.forceExecutionStatus(testPlanIds, status);
	}

	@ResponseBody
	@RequestMapping(value = "/iterations/{iterationId}/test-plan/{testPlanId}", method = RequestMethod.POST, params = {"dataset"})
	public
	Long setDataset(@PathVariable("testPlanId") long testPlanId, @RequestParam("dataset") Long datasetId) {
		iterationTestPlanManagerService.changeDataset(testPlanId, JeditableComboHelper.coerceIntoEntityId(datasetId));
		return datasetId;
	}

	@RequestMapping(value = "/iterations/{iterationId}/test-plan/{testPlanId}/last-execution", method = RequestMethod.GET)
	public String goToLastExecution(@PathVariable("testPlanId") Long testPlanId){
		IterationTestPlanItem item = iterationTestPlanManagerService.findTestPlanItem(testPlanId);
		Execution exec = item.getLatestExecution();
		return "redirect:/executions/"+exec.getId();
	}

	private String formatUnassigned(Locale locale) {
		return messageSource.internationalize("label.Unassigned", locale);
	}

	private JsonIterationTestPlanItem createJsonITPI(IterationTestPlanItem item) {
		String name = item.isTestCaseDeleted() ? null : item.getReferencedTestCase().getName();
		return new JsonIterationTestPlanItem(item.getId(), item.getExecutionStatus(), name, item.getLastExecutedOn(),
				item.getLastExecutedBy(), item.getUser(), item.isTestCaseDeleted(), item.isAutomated());
	}

}
