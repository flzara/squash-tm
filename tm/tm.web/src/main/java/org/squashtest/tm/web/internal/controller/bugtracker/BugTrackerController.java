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
package org.squashtest.tm.web.internal.controller.bugtracker;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.csp.core.bugtracker.core.BugTrackerConnectorFactory;
import org.squashtest.csp.core.bugtracker.core.BugTrackerNoCredentialsException;
import org.squashtest.csp.core.bugtracker.core.BugTrackerRemoteException;
import org.squashtest.csp.core.bugtracker.domain.BTIssue;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.csp.core.bugtracker.spi.BugTrackerInterfaceDescriptor;
import org.squashtest.tm.bugtracker.advanceddomain.AdvancedIssue;
import org.squashtest.tm.bugtracker.advanceddomain.DelegateCommand;
import org.squashtest.tm.bugtracker.definition.Attachment;
import org.squashtest.tm.bugtracker.definition.RemoteIssue;
import org.squashtest.tm.core.foundation.collection.*;
import org.squashtest.tm.core.foundation.exception.NullArgumentException;
import org.squashtest.tm.domain.Identified;
import org.squashtest.tm.domain.bugtracker.IssueDetector;
import org.squashtest.tm.domain.bugtracker.IssueOwnership;
import org.squashtest.tm.domain.bugtracker.RemoteIssueDecorator;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.CampaignFolder;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStep;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.servers.AuthenticationStatus;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.bugtracker.BugTrackerManagerService;
import org.squashtest.tm.service.bugtracker.BugTrackersLocalService;
import org.squashtest.tm.service.bugtracker.RequirementVersionIssueOwnership;
import org.squashtest.tm.service.campaign.CampaignFinder;
import org.squashtest.tm.service.campaign.CampaignLibraryNavigationService;
import org.squashtest.tm.service.campaign.IterationFinder;
import org.squashtest.tm.service.campaign.TestSuiteFinder;
import org.squashtest.tm.service.execution.ExecutionFinder;
import org.squashtest.tm.service.requirement.RequirementVersionManagerService;
import org.squashtest.tm.service.testcase.TestCaseFinder;
import org.squashtest.tm.web.internal.controller.attachment.UploadedData;
import org.squashtest.tm.web.internal.controller.attachment.UploadedDataPropertyEditorSupport;
import org.squashtest.tm.web.internal.controller.authentication.ThirdPartyServersAuthenticationController;
import org.squashtest.tm.web.internal.helper.JsonHelper;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import oslcdomain.OslcIssue;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 *
 * Note : as of 1.13 the questions regarding authentications has been moved to
 * {@link ThirdPartyServersAuthenticationController}
 *
 * @author bsiri
 *
 */
@Controller
@RequestMapping("/bugtracker")
public class BugTrackerController {

	private static final String SORTING_DEFAULT_ATTRIBUTE = "Issue.remoteIssueId"; // here is the real fix for #5683 ;)

	private static final Logger LOGGER = LoggerFactory.getLogger(BugTrackerController.class);

	@Inject
	private BugTrackerConnectorFactory btFactory;

	@Inject
	private BugTrackersLocalService bugTrackersLocalService;
	@Inject
	private RequirementVersionManagerService requirementVersionManager;
	@Inject
	private CampaignFinder campaignFinder;
	@Inject
	private IterationFinder iterationFinder;
	@Inject
	private TestSuiteFinder testSuiteFinder;
	@Inject
	private ExecutionFinder executionFinder;
	@Inject
	private TestCaseFinder testCaseFinder;
	@Inject
	private BugTrackerManagerService bugTrackerManagerService;
	@Inject
	private InternationalizationHelper messageSource;
	@Inject
	private BugTrackerControllerHelper helper;
	@Inject
	private CampaignLibraryNavigationService clnService;

	// TODO add *private*, plus it may already be defined someplace else
	static final String EXECUTION_STEP_TYPE = "execution-step";
	static final String EXECUTION_TYPE = "execution";
	static final String ITERATION_TYPE = "iteration";
	static final String CAMPAIGN_TYPE = "campaign";
	static final String TEST_SUITE_TYPE = "test-suite";
	static final String TEST_CASE_TYPE = "test-case";
	static final String CAMPAIGN_FOLDER_TYPE = "campaign-folder";
	static final String REQUIREMENT_VERSION_TYPE = "requirement-version";

	private static final String BUGTRACKER_ID = "bugTrackerId";
	private static final String EMPTY_BUGTRACKER_MAV = "fragment/bugtracker/bugtracker-panel-empty";

	private static final String STYLE_ARG = "style";
	private static final String STYLE_TOGGLE = "toggle";

	private static final String MODEL_TABLE_ENTRIES = "tableEntries";
	private static final String MODEL_BUG_TRACKER_STATUS = "bugTrackerStatus";


	@InitBinder
	public void initBinder(ServletRequestDataBinder binder) throws ServletException {
		binder.registerCustomEditor(UploadedData.class, new UploadedDataPropertyEditorSupport());
	}

	/*
	 * ************************************************************************************************************** *
	 * Navigation button * *
	 * ***********************************************************************************************************
	 */

	@RequestMapping(value = "{bugtrackerId}/workspace", method = RequestMethod.GET)
	public ModelAndView showWorkspace(@PathVariable Long bugtrackerId) {
		BugTracker bugTracker = bugTrackerManagerService.findById(bugtrackerId);
		ModelAndView mav = new ModelAndView("page/bugtrackers/bugtracker-workspace");
		mav.addObject("bugtrackerUrl", bugTracker.getUrl());
		return mav;
	}

	/*
	 * ************************************************************************************************************** *
	 * ExecutionStep level section * *
	 * ***********************************************************************************************************
	 */

	/**
	 * <p>
	 * returns the panel displaying the current bugs of that execution step and the stub for the report form. Remember
	 * that the report bug dialog will be populated later.
	 * </p>
	 * <p>
	 * Note : accepts as optional parameter :
	 * <ul>
	 * <li>useParentContextPopup : will tell the panel to use a delegate report issue popup (that's how the OER works)
	 * </p>
	 *
	 */
	@RequestMapping(value = EXECUTION_STEP_TYPE + "/{stepId}", method = RequestMethod.GET)
	public ModelAndView getExecStepIssuePanel(@PathVariable Long stepId, Locale locale,
			@RequestParam(value = STYLE_ARG, required = false, defaultValue = STYLE_TOGGLE) String panelStyle,
			@RequestParam(value = "useDelegatePopup", required = false, defaultValue = "false") Boolean useParentPopup) {

		ExecutionStep step = executionFinder.findExecutionStepById(stepId);
		ModelAndView mav = makeIssuePanel(step, EXECUTION_STEP_TYPE, locale, panelStyle, step.getProject());
		mav.addObject("useParentContextPopup", useParentPopup);

		/*
		 * issue 4178 eagerly fetch the row entries if the user is authenticated (we need the table to be shipped along
		 * with the panel in one call)
		 */
		populateWithKnownIssuesIfNeeded(mav, EXECUTION_STEP_TYPE, stepId);

		return mav;
	}

	/**
	 * json Data for the known issues table.
	 */
	@ResponseBody
	@RequestMapping(value = EXECUTION_STEP_TYPE + "/{stepId}/known-issues", method = RequestMethod.GET)
	public DataTableModel getExecStepKnownIssuesData(@PathVariable("stepId") Long stepId,
													 final DataTableDrawParameters params) {

		PagingAndSorting sorter = new IssueCollectionSorting(params);

		return getKnownIssuesData(EXECUTION_STEP_TYPE, stepId, sorter, params.getsEcho());

	}


	/**
	 * will prepare a bug report for an execution step. The returned json infos will populate the form.
	 */

	@RequestMapping(value = EXECUTION_STEP_TYPE + "/{stepId}/new-issue", method = RequestMethod.GET)
	@ResponseBody
	public RemoteIssue getExecStepReportStub(@PathVariable Long stepId, Locale locale, HttpServletRequest request, @RequestParam(value="project-name", required=true) String projectName) {

		ExecutionStep step = executionFinder.findExecutionStepById(stepId);

		String executionUrl = BugTrackerControllerHelper.buildExecutionUrl(request, step.getExecution());

		return makeReportIssueModel(step, locale, executionUrl, projectName);
	}


	/**
	 * posts a new issue (simple model)
	 */
	@RequestMapping(value = EXECUTION_STEP_TYPE + "/{stepId}/new-issue", method = RequestMethod.POST)
	@ResponseBody
	public Object postExecStepIssueReport(@PathVariable("stepId") Long stepId, @RequestBody BTIssue jsonIssue) {
		LOGGER.trace("BugTrackerController: posting a new issue for execution-step " + stepId);

		IssueDetector entity = executionFinder.findExecutionStepById(stepId);

		if (jsonIssue.hasBlankId()) {
			return processIssue(jsonIssue, entity);
		} else {
			return attachIssue(jsonIssue, entity);
		}
	}

	/**
	 * posts a new issue (advanced model)
	 */
	@RequestMapping(value = EXECUTION_STEP_TYPE + "/{stepId}/new-advanced-issue", method = RequestMethod.POST)
	@ResponseBody
	public Object postExecStepAdvancedIssueReport(@PathVariable("stepId") Long stepId,
			@RequestBody AdvancedIssue jsonIssue) {
		LOGGER.trace("BugTrackerController: posting a new issue for execution-step " + stepId);

		IssueDetector entity = executionFinder.findExecutionStepById(stepId);

		if (jsonIssue.hasBlankId()) {
			return processIssue(jsonIssue, entity);
		} else {
			return attachIssue(jsonIssue, entity);
		}
	}

	/**
	 * posts a new issue (oslc model)
	 */
	@RequestMapping(value = EXECUTION_STEP_TYPE + "/{stepId}/new-oslc-issue", method = RequestMethod.POST)
	@ResponseBody
	public Object postExecStepIssueReport(@PathVariable("stepId") Long stepId, @RequestBody String issueId) {
		LOGGER.trace("BugTrackerController: posting a new issue for execution-step " + stepId);

		IssueDetector entity = executionFinder.findExecutionStepById(stepId);

		OslcIssue issue = new OslcIssue();
		issue.setId(issueId);
		return attachIssue(issue, entity);

	}

	/*
	 * **************************************************************************************************************
	 *
	 * Execution level section
	 *
	 * ***********************************************************************************************************
	 */

	/**
	 * returns the panel displaying the current bugs of that execution and the stub for the report form. Remember that
	 * the report bug dialog will be populated later.
	 *
	 */
	@RequestMapping(value = EXECUTION_TYPE + "/{execId}", method = RequestMethod.GET)
	public ModelAndView getExecIssuePanel(@PathVariable Long execId, Locale locale,
			@RequestParam(value = STYLE_ARG, required = false, defaultValue = STYLE_TOGGLE) String panelStyle) {

		Execution bugged = executionFinder.findById(execId);
		ModelAndView mav = makeIssuePanel(bugged, EXECUTION_TYPE, locale, panelStyle, bugged.getProject());

		/*
		 * issue 4178 eagerly fetch the row entries if the user is authenticated (we need the table to be shipped along
		 * with the panel in one call)
		 */
		populateWithKnownIssuesIfNeeded(mav, EXECUTION_TYPE, execId);

		return mav;

	}

	/**
	 * json Data for the known issues table.
	 */
	@ResponseBody
	@RequestMapping(value = EXECUTION_TYPE + "/{execId}/known-issues", method = RequestMethod.GET)
	public DataTableModel getExecKnownIssuesData(@PathVariable("execId") Long execId,
			final DataTableDrawParameters params) {

		PagingAndSorting sorter = new IssueCollectionSorting(params);

		return getKnownIssuesData(EXECUTION_TYPE, execId, sorter, params.getsEcho());

	}

	/**
	 * will prepare a bug report for an execution. The returned json infos will populate the form.
	 */
	@RequestMapping(value = EXECUTION_TYPE + "/{execId}/new-issue")
	@ResponseBody
	public RemoteIssue getExecReportStub(@PathVariable Long execId, Locale locale, HttpServletRequest request, @RequestParam(value="project-name", required=true) String projectName) {
		Execution execution = executionFinder.findById(execId);
		String executionUrl = BugTrackerControllerHelper.buildExecutionUrl(request, execution);
		return makeReportIssueModel(execution, locale, executionUrl, projectName);
	}

	/**
	 * posts a new issue (simple model)
	 */
	@RequestMapping(value = EXECUTION_TYPE + "/{execId}/new-issue", method = RequestMethod.POST)
	@ResponseBody
	public Object postExecIssueReport(@PathVariable("execId") Long execId, @RequestBody BTIssue jsonIssue) {
		LOGGER.trace("BugTrackerController: posting a new issue for execution-step " + execId);

		Execution entity = executionFinder.findById(execId);

		if (jsonIssue.hasBlankId()) {
			return processIssue(jsonIssue, entity);
		} else {
			return attachIssue(jsonIssue, entity);
		}
	}

	/**
	 * posts a new issue (advanced model)
	 */
	@RequestMapping(value = EXECUTION_TYPE + "/{execId}/new-advanced-issue", method = RequestMethod.POST)
	@ResponseBody
	public Object postExecAdvancedIssueReport(@PathVariable("execId") Long execId,
			@RequestBody AdvancedIssue jsonIssue) {
		LOGGER.trace("BugTrackerController: posting a new issue for execution-step " + execId);

		Execution entity = executionFinder.findById(execId);

		if (jsonIssue.hasBlankId()) {
			return processIssue(jsonIssue, entity);
		} else {
			return attachIssue(jsonIssue, entity);
		}
	}

	/**
	 * posts a new issue (oslc model)
	 */
	@RequestMapping(value = EXECUTION_TYPE + "/{execId}/new-oslc-issue", method = RequestMethod.POST)
	@ResponseBody
	public Object postExecIssueReport(@PathVariable("execId") Long execId, @RequestBody String issueId) {
		LOGGER.trace("BugTrackerController: posting a new issue for execution " + execId);

		Execution entity = executionFinder.findById(execId);

		OslcIssue issue = new OslcIssue();
		issue.setId(issueId);
		return attachIssue(issue, entity);

	}

	/*
	 * ************************************************************************************************************** *
	 * RequirementVersion level section * *
	 * ***********************************************************************************************************
	 */

	/**
	 * returns the panel displaying the current bugs of that RequirementVersion and the stub for the report form. Remember that
	 * the report bug dialog will be populated later.
	 *
	 * @param rvId
	 * @return
	 */
	@RequestMapping(value = REQUIREMENT_VERSION_TYPE + "/{rvId}/{panelSource}", method = RequestMethod.GET)
	public ModelAndView getRequirementWorkspaceIssuePanel(@PathVariable("rvId") Long rvId, Locale locale, @RequestParam(value = STYLE_ARG, required = false, defaultValue = STYLE_TOGGLE) String panelStyle,
															@PathVariable("panelSource") String panelSource) {

		RequirementVersion requirementVersion = requirementVersionManager.findById(rvId);

		ModelAndView mav = makeIssuePanel(requirementVersion, REQUIREMENT_VERSION_TYPE, locale, panelStyle, requirementVersion.getProject());

		if (shouldGetTableData(mav)) {
			DefaultPagingAndSorting pas = new DefaultPagingAndSorting(SORTING_DEFAULT_ATTRIBUTE);
			pas.setSortOrder(SortOrder.DESCENDING);
			DataTableModel issues = getKnownIssuesDataForRequirementVersion(REQUIREMENT_VERSION_TYPE, rvId, panelSource,
				pas, "0");
			mav.addObject(MODEL_TABLE_ENTRIES, issues);
		}

		return mav;

	}

	/**
	 * json Data for the known issues table.
	 */
	@ResponseBody
	@RequestMapping(value = REQUIREMENT_VERSION_TYPE + "/{rvId}/known-issues/{panelSource}", method = RequestMethod.GET)
	public DataTableModel getRequirementVersionKnownIssuesData(@PathVariable("rvId") Long rvId, final DataTableDrawParameters params,  @PathVariable("panelSource") String panelSource) {

		PagingAndSorting sorter = new IssueCollectionSorting(params);

		return getKnownIssuesDataForRequirementVersion(REQUIREMENT_VERSION_TYPE, rvId, panelSource, sorter, params.getsEcho());

	}

	/*
	 * ************************************************************************************************************** *
	 * TestCase level section * *
	 * ***********************************************************************************************************
	 */

	/**
	 * returns the panel displaying the current bugs of that testCase and the stub for the report form. Remember that
	 * the report bug dialog will be populated later.
	 *
	 * @param tcId
	 * @return
	 */
	@RequestMapping(value = TEST_CASE_TYPE + "/{tcId}", method = RequestMethod.GET)
	public ModelAndView getTestCaseIssuePanel(@PathVariable Long tcId, Locale locale,
			@RequestParam(value = STYLE_ARG, required = false, defaultValue = STYLE_TOGGLE) String panelStyle) {

		TestCase testCase = testCaseFinder.findById(tcId);

		ModelAndView mav = makeIssuePanel(testCase, TEST_CASE_TYPE, locale, panelStyle, testCase.getProject());

		/*
		 * issue 4178 eagerly fetch the row entries if the user is authenticated (we need the table to be shipped along
		 * with the panel in one call)
		 */
		populateWithKnownIssuesIfNeeded(mav, TEST_CASE_TYPE, tcId);

		return mav;

	}


	/**
	 * json Data for the known issues table.
	 */
	@ResponseBody
	@RequestMapping(value = TEST_CASE_TYPE + "/{tcId}/known-issues", method = RequestMethod.GET)
	public DataTableModel getTestCaseKnownIssuesData(@PathVariable("tcId") Long tcId,
			final DataTableDrawParameters params) {

		PagingAndSorting sorter = new IssueCollectionSorting(params);

		return getKnownIssuesData(TEST_CASE_TYPE, tcId, sorter, params.getsEcho());

	}

	/*
	 * ************************************************************************************************************** *
	 * Iteration level section * *
	 * ***********************************************************************************************************
	 */

	/**
	 * returns the panel displaying the current bugs of that iteration and the stub for the report form. Remember that
	 * the report bug dialog will be populated later.
	 *
	 */
	@RequestMapping(value = ITERATION_TYPE + "/{iterId}", method = RequestMethod.GET)
	public ModelAndView getIterationIssuePanel(@PathVariable Long iterId, Locale locale,
			@RequestParam(value = STYLE_ARG, required = false, defaultValue = STYLE_TOGGLE) String panelStyle) {

		Iteration iteration = iterationFinder.findById(iterId);
		ModelAndView mav = makeIssuePanel(iteration, ITERATION_TYPE, locale, panelStyle, iteration.getProject());

		/*
		 * issue 4178 eagerly fetch the row entries if the user is authenticated (we need the table to be shipped along
		 * with the panel in one call)
		 */
		populateWithKnownIssuesIfNeeded(mav, ITERATION_TYPE, iterId);

		return mav;

	}

	/**
	 * json Data for the known issues table.
	 */
	@ResponseBody
	@RequestMapping(value = ITERATION_TYPE + "/{iterId}/known-issues", method = RequestMethod.GET)
	public DataTableModel getIterationKnownIssuesData(@PathVariable("iterId") Long iterId,
			final DataTableDrawParameters params) {

		PagingAndSorting sorter = new IssueCollectionSorting(params);

		return getKnownIssuesData(ITERATION_TYPE, iterId, sorter, params.getsEcho());

	}

	/*
	 * ************************************************************************************************************** *
	 * Campaign level section * *
	 * ***********************************************************************************************************
	 */

	/**
	 * returns the panel displaying the current bugs of that campaign and the stub for the report form. Remember that
	 * the report bug dialog will be populated later.
	 *
	 */
	@RequestMapping(value = CAMPAIGN_TYPE + "/{campId}", method = RequestMethod.GET)
	public ModelAndView getCampaignIssuePanel(@PathVariable Long campId, Locale locale,
			@RequestParam(value = STYLE_ARG, required = false, defaultValue = STYLE_TOGGLE) String panelStyle) {

		Campaign campaign = campaignFinder.findById(campId);
		ModelAndView mav = makeIssuePanel(campaign, CAMPAIGN_TYPE, locale, panelStyle, campaign.getProject());

		/*
		 * issue 4178 eagerly fetch the row entries if the user is authenticated (we need the table to be shipped along
		 * with the panel in one call)
		 */
		populateWithKnownIssuesIfNeeded(mav, CAMPAIGN_TYPE, campId);

		return mav;
	}

	/**
	 * json Data for the known issues table.
	 */
	@ResponseBody
	@RequestMapping(value = CAMPAIGN_TYPE + "/{campId}/known-issues", method = RequestMethod.GET)
	public DataTableModel getCampaignKnownIssuesData(@PathVariable("campId") Long campId,
			final DataTableDrawParameters params) {

		PagingAndSorting sorter = new IssueCollectionSorting(params);

		return getKnownIssuesData(CAMPAIGN_TYPE, campId, sorter, params.getsEcho());
	}

	/*
	 * ************************************************************************************************************** *
	 * TestSuite level section * *
	 * ***********************************************************************************************************
	 */

	/**
	 * returns the panel displaying the current bugs of that test-suite and the stub for the report form. Remember that
	 * the report bug dialog will be populated later.
	 *
	 */
	@RequestMapping(value = TEST_SUITE_TYPE + "/{testSuiteId}", method = RequestMethod.GET)
	public ModelAndView getTestSuiteIssuePanel(@PathVariable Long testSuiteId, Locale locale,
			@RequestParam(value = STYLE_ARG, required = false, defaultValue = STYLE_TOGGLE) String panelStyle) {

		TestSuite testSuite = testSuiteFinder.findById(testSuiteId);
		ModelAndView mav = makeIssuePanel(testSuite, TEST_SUITE_TYPE, locale, panelStyle,
				testSuite.getIteration().getProject());

		/*
		 * issue 4178 eagerly fetch the row entries if the user is authenticated (we need the table to be shipped along
		 * with the panel in one call)
		 */
		populateWithKnownIssuesIfNeeded(mav, TEST_SUITE_TYPE, testSuiteId);


		return mav;
	}


	/**
	 * json Data for the known issues table.
	 */
	@ResponseBody
	@RequestMapping(value = TEST_SUITE_TYPE + "/{testSuiteId}/known-issues", method = RequestMethod.GET)
	public DataTableModel getTestSuiteKnownIssuesData(@PathVariable("testSuiteId") Long testSuiteId,
													  final DataTableDrawParameters params) {

		PagingAndSorting sorter = new IssueCollectionSorting(params);

		return getKnownIssuesData(TEST_SUITE_TYPE, testSuiteId, sorter, params.getsEcho());

	}

	/*
	 * **************************************************************************************************************
	 *
	 * Campaign folder level section
	 *
	 ************************************************************************************************************/

	/**
	 * returns the panel displaying the current bugs of that test-suite and the stub for the report form. Remember that
	 * the report bug dialog will be populated later.
	 *
	 * @param campaignFolderId
	 * @return
	 */
	@RequestMapping(value = CAMPAIGN_FOLDER_TYPE + "/{campaignFolderId}", method = RequestMethod.GET)
	public ModelAndView getCampaignFolderIssuePanel(@PathVariable("campaignFolderId") Long campaignFolderId,
			Locale locale,
			@RequestParam(value = STYLE_ARG, required = false, defaultValue = STYLE_TOGGLE) String panelStyle) {

		CampaignFolder campaignFolder = clnService.findFolder(campaignFolderId);
		ModelAndView mav = makeIssuePanel(campaignFolder, CAMPAIGN_FOLDER_TYPE, locale, panelStyle,
				campaignFolder.getProject());

		/*
		 * issue 4178 eagerly fetch the row entries if the user is authenticated (we need the table to be shipped along
		 * with the panel in one call)
		 */
		populateWithKnownIssuesIfNeeded(mav, CAMPAIGN_FOLDER_TYPE, campaignFolderId);

		return mav;
	}

	/**
	 * json Data for the known issues table.
	 */
	@ResponseBody
	@RequestMapping(value = CAMPAIGN_FOLDER_TYPE + "/{campaignFolderId}/known-issues", method = RequestMethod.GET)
	public DataTableModel getCampaignFolderKnownIssuesData(
		@PathVariable("campaignFolderId") Long campaignFolderId, final DataTableDrawParameters params) {

		PagingAndSorting sorter = new IssueCollectionSorting(params);

		return getKnownIssuesData(CAMPAIGN_FOLDER_TYPE, campaignFolderId, sorter, params.getsEcho());

	}

	/* ************************* Generic code section ************************** */

	private void populateWithKnownIssuesIfNeeded(ModelAndView mav, String entityType, Long entityId) {
		if (shouldGetTableData(mav)) {
			DataTableModel issues = getKnownIssuesDataInDescendingOrder(entityType, entityId);
			mav.addObject(MODEL_TABLE_ENTRIES, issues);
		}
	}

	private DataTableModel getKnownIssuesDataInDescendingOrder(String entityType, Long entityId) {
		DefaultPagingAndSorting pas = new DefaultPagingAndSorting(SORTING_DEFAULT_ATTRIBUTE);
		pas.setSortOrder(SortOrder.DESCENDING);
		return getKnownIssuesData(entityType, entityId, pas, "0");
	}

	@RequestMapping(value = "/find-issue/{remoteKey}", method = RequestMethod.GET, params = { BUGTRACKER_ID })
	@ResponseBody
	public RemoteIssue findIssue(@PathVariable("remoteKey") String remoteKey,
			@RequestParam(BUGTRACKER_ID) long bugTrackerId, @RequestParam("projectNames[]") List<String> projectNames,
			Locale locale) {
		BugTracker bugTracker = bugTrackerManagerService.findById(bugTrackerId);
		RemoteIssue issue = bugTrackersLocalService.getIssue(remoteKey, bugTracker);

		String projectName = issue.getProject().getName();

		// Dirty fix to Issue 5767. As the bugtracker "trac" do not provide project name, we have to ignore the case of
		// projectName is null
		// yeah it's sucks because we can let some invalid issues from other bugtracker pass trough the control
		// we should modify API to do that correctly but no time for this in 1.13
		if (!projectNames.contains(projectName) && StringUtils.isNotEmpty(projectName)) {
			throw new BugTrackerRemoteException(
					messageSource.internationalize("bugtracker.issue.notfoundinprojects", locale), new Throwable());
		}

		return bugTrackersLocalService.getIssue(remoteKey, bugTracker);
	}

	// FIXME : check first if a bugtracker is defined and if the credentials are set
	private Map<String, String> processIssue(RemoteIssue issue, IssueDetector entity) {
		final RemoteIssue postedIssue = bugTrackersLocalService.createIssue(entity, issue);
		final URL issueUrl = bugTrackersLocalService.getIssueUrl(postedIssue.getId(), entity.getBugTracker());

		Map<String, String> result = new HashMap<>();
		result.put("url", issueUrl.toString());
		result.put("issueId", postedIssue.getId());

		return result;
	}

	private Map<String, String> attachIssue(final RemoteIssue issue, IssueDetector entity) {

		bugTrackersLocalService.attachIssue(entity, issue.getId());
		final URL issueUrl = bugTrackersLocalService.getIssueUrl(issue.getId(), entity.getBugTracker());

		Map<String, String> result = new HashMap<>();
		result.put("url", issueUrl.toString());
		result.put("issueId", issue.getId());

		return result;
	}

	@ResponseBody
	@RequestMapping(value = "/issues/{issueId}", method = RequestMethod.DELETE)
	public void detachIssue(@PathVariable("issueId") Long issueId) {
		bugTrackersLocalService.detachIssue(issueId);
	}

	@ResponseBody
	@RequestMapping(value = "/{btName}/remote-issues/{remoteIssueId}/attachments", method = RequestMethod.POST)
	public void forwardAttachmentsToIssue(@PathVariable("btName") String btName,
										  @PathVariable("remoteIssueId") String remoteIssueId,
										  @RequestParam("attachment[]") List<UploadedData> uploads) {

		List<Attachment> issueAttachments = new ArrayList<>(uploads.size());
		for (UploadedData upload : uploads) {
			Attachment newAttachment = new Attachment(upload.getName(), upload.getSizeInBytes(), upload.getStream());
			issueAttachments.add(newAttachment);
		}

		bugTrackersLocalService.forwardAttachments(remoteIssueId, btName, issueAttachments);

		// now ensure that the input streams are closed
		for (Attachment attachment : issueAttachments) {
			try {
				attachment.getStreamContent().close();
			} catch (IOException ex) {
				LOGGER.warn("issue attachments : could not close stream for " + attachment.getName()
						+ ", this is non fatal anyway", ex);
			}
		}

	}

	@ResponseBody
	@RequestMapping(value = "{btName}/command", method = RequestMethod.POST)
	public Object forwardDelegateCommand(@PathVariable("btName") String bugtrackerName,
										 @RequestBody DelegateCommand command) {
		return bugTrackersLocalService.forwardDelegateCommand(command, bugtrackerName);
	}

	/* ********* generates a json model for an issue ******* */

	private RemoteIssue makeReportIssueModel(Execution exec, Locale locale, String executionUrl, String projectName) {
		String defaultDescription = BugTrackerControllerHelper.getDefaultDescription(exec, locale, messageSource,
				executionUrl);
		return makeReportIssueModel(exec, defaultDescription, projectName);
	}

	private RemoteIssue makeReportIssueModel(ExecutionStep step, Locale locale, String executionUrl,
			String projectName) {
		String defaultDescription = BugTrackerControllerHelper.getDefaultDescription(step, locale, messageSource,
				executionUrl);
		String defaultAdditionalInformations = BugTrackerControllerHelper.getDefaultAdditionalInformations(step, locale,
				messageSource);
		return makeReportIssueModel(step, defaultDescription, locale, projectName);
	}

	private RemoteIssue makeReportIssueModel(ExecutionStep step, String defaultDescription,
											 Locale locale, String projectName) {
		RemoteIssue emptyIssue = makeReportIssueModel(step, defaultDescription, projectName);
		String comment = BugTrackerControllerHelper.getDefaultAdditionalInformations(step, locale, messageSource);
		emptyIssue.setComment(comment);
		return emptyIssue;
	}

	private RemoteIssue makeReportIssueModel(IssueDetector entity, String defaultDescription, String projectName) {

		RemoteIssue emptyIssue = bugTrackersLocalService.createReportIssueTemplate(projectName, entity.getBugTracker());

		emptyIssue.setDescription(defaultDescription);

		return emptyIssue;

	}

	/*
	 * generates the ModelAndView for the bug section.
	 *
	 * If the bugtracker isn'st defined no panel will be sent at all.
	 */
	private ModelAndView makeIssuePanel(Identified entity, String type, Locale locale, String panelStyle,
			Project project) {
		if (project.isBugtrackerConnected()) {
			AuthenticationStatus status = checkStatus(project.getId());
			// JSON STATUS TODO

			BugTracker bugtracker = project.findBugTracker();
			BugTrackerInterfaceDescriptor descriptor = bugTrackersLocalService.getInterfaceDescriptor(bugtracker);
			descriptor.setLocale(locale);
			ModelAndView mav = new ModelAndView("fragment/bugtracker/bugtracker-panel-content");
			mav.addObject("entity", entity);
			mav.addObject("entityType", type);
			mav.addObject("interfaceDescriptor", descriptor);
			mav.addObject("panelStyle", panelStyle);
			mav.addObject(MODEL_BUG_TRACKER_STATUS, status);
			mav.addObject("project", project);
			mav.addObject("bugTracker", bugtracker);
			mav.addObject("projectNames", JsonHelper.serialize(project.getBugtrackerBinding().getProjectNames()));
			mav.addObject("projectId", project.getId());
			mav.addObject("delete", "");
			mav.addObject("isOslc", btFactory.isOslcConnector(bugtracker.getKind()));

			return mav;
		} else {
			return new ModelAndView(EMPTY_BUGTRACKER_MAV);
		}

	}

	/*
	 * ************************************************************************************************************** *
	 * administration section * *
	 * ***********************************************************************************************************
	 */

	@ResponseBody
	@RequestMapping(value = "/{bugtrackerIds}", method = RequestMethod.DELETE)
	public void deleteBugtrackers(@PathVariable("bugtrackerIds") List<Long> bugtrackerIds) {
		LOGGER.debug("ids of bugtracker to delete " + bugtrackerIds.toString());
		bugTrackerManagerService.deleteBugTrackers(bugtrackerIds);
	}

	/* ******************************* private methods ********************************************** */

	private DataTableModel getKnownIssuesDataForRequirementVersion(String entityType, Long id, String panelSource,PagingAndSorting paging, String sEcho) {

		PagedCollectionHolder<List<RequirementVersionIssueOwnership<RemoteIssueDecorator>>> filteredCollection;
		try {
			filteredCollection = bugTrackersLocalService.findSortedIssueOwnershipForRequirmentVersion(id, panelSource, paging);
		}
		catch (BugTrackerNoCredentialsException | NullArgumentException exception) {
			filteredCollection = makeEmptyIssueDecoratorCollectionHolderForRequirement(entityType, id, exception, paging);
		}

		return helper.createModelBuilderForRequirementVersion().buildDataModel(filteredCollection, sEcho);
	}

	private DataTableModel getKnownIssuesData(String entityType, Long id, PagingAndSorting paging, String sEcho) {

		PagedCollectionHolder<List<IssueOwnership<RemoteIssueDecorator>>> filteredCollection;

		try {
			switch (entityType) {
			case TEST_CASE_TYPE:
				filteredCollection = bugTrackersLocalService.findSortedIssueOwnershipForTestCase(id, paging);
				break;
			case CAMPAIGN_FOLDER_TYPE:
				filteredCollection = bugTrackersLocalService.findSortedIssueOwnershipForCampaignFolder(id, paging);
				break;
			case CAMPAIGN_TYPE:
				filteredCollection = bugTrackersLocalService.findSortedIssueOwnershipsForCampaign(id, paging);
				break;
			case ITERATION_TYPE:
				filteredCollection = bugTrackersLocalService.findSortedIssueOwnershipForIteration(id, paging);
				break;
			case TEST_SUITE_TYPE:
				filteredCollection = bugTrackersLocalService.findSortedIssueOwnershipsForTestSuite(id, paging);
				break;
			case EXECUTION_TYPE:
				filteredCollection = bugTrackersLocalService.findSortedIssueOwnershipsforExecution(id, paging);
				break;
			case EXECUTION_STEP_TYPE:
				filteredCollection = bugTrackersLocalService.findSortedIssueOwnerShipsForExecutionStep(id, paging);
				break;
			default:
				String error = "BugTrackerController : cannot fetch issues for unknown entity type '" + entityType
						+ "'";
				if (LOGGER.isErrorEnabled()) {
					LOGGER.error(error);
				}
				throw new IllegalArgumentException(error);
			}
		}
		// no credentials exception are okay, the rest is to be treated as usual
		catch (BugTrackerNoCredentialsException | NullArgumentException exception) {
			filteredCollection = makeEmptyIssueDecoratorCollectionHolder(entityType, id, exception, paging);
		}

		return helper.createModelBuilderFor(entityType).buildDataModel(filteredCollection, sEcho);
	}

	private AuthenticationStatus checkStatus(long projectId) {
		return bugTrackersLocalService.checkBugTrackerStatus(projectId);
	}

	private static final class IssueCollectionSorting implements PagingAndSorting {

		private DataTableDrawParameters params;

		private IssueCollectionSorting(final DataTableDrawParameters params) {
			this.params = params;
		}

		@Override
		public int getFirstItemIndex() {
			return params.getiDisplayStart();
		}

		@Override
		public String getSortedAttribute() {
			return SORTING_DEFAULT_ATTRIBUTE;
		}

		@Override
		public int getPageSize() {
			return params.getiDisplayLength();
		}

		@Override
		public boolean shouldDisplayAll() {
			return getPageSize() < 0;
		}

		/**
		 * @see org.squashtest.tm.core.foundation.collection.Sorting#getSortOrder()
		 */
		@Override
		public SortOrder getSortOrder() {
			return SortOrder.coerceFromCode(params.getsSortDir_0());
		}

		@Override
		public Pageable toPageable() {
			return SpringPaginationUtils.toPageable(this);
		}

	}

	private PagedCollectionHolder<List<RequirementVersionIssueOwnership<RemoteIssueDecorator>>> makeEmptyIssueDecoratorCollectionHolderForRequirement(
		String entityName, Long entityId, Exception cause, PagingAndSorting paging) {
		LOGGER.trace("BugTrackerController : fetching known issues for  " + entityName + " " + entityId
			+ " failed, exception : ", cause);
		List<RequirementVersionIssueOwnership<RemoteIssueDecorator>> emptyList = new LinkedList<>();
		return new PagingBackedPagedCollectionHolder<>(paging, 0, emptyList);
	}

	private PagedCollectionHolder<List<IssueOwnership<RemoteIssueDecorator>>> makeEmptyIssueDecoratorCollectionHolder(
			String entityName, Long entityId, Exception cause, PagingAndSorting paging) {
		LOGGER.trace("BugTrackerController : fetching known issues for  " + entityName + " " + entityId
				+ " failed, exception : ", cause);
		List<IssueOwnership<RemoteIssueDecorator>> emptyList = new LinkedList<>();
		return new PagingBackedPagedCollectionHolder<>(paging, 0, emptyList);
	}

	private boolean shouldGetTableData(ModelAndView mav) {
		return mav.getModel().get(MODEL_BUG_TRACKER_STATUS) == AuthenticationStatus.AUTHENTICATED;
	}

}
