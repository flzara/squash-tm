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
package org.squashtest.tm.web.internal.controller.execution;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.squashtest.tm.domain.attachment.Attachment;
import org.squashtest.tm.domain.campaign.CampaignLibrary;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.execution.ExecutionStep;
import org.squashtest.tm.domain.execution.ExecutionVisitor;
import org.squashtest.tm.domain.execution.IsScriptedExecutionVisitor;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.users.Party;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.service.campaign.TestPlanExecutionProcessingService;
import org.squashtest.tm.service.customfield.CustomFieldValueFinderService;
import org.squashtest.tm.service.denormalizedfield.DenormalizedFieldValueManager;
import org.squashtest.tm.service.execution.ExecutionProcessingService;
import org.squashtest.tm.service.requirement.VerifiedRequirement;
import org.squashtest.tm.service.requirement.VerifiedRequirementsFinderService;
import org.squashtest.tm.service.user.PartyPreferenceService;
import org.squashtest.tm.service.user.UserAccountService;
import org.squashtest.tm.web.internal.controller.generic.ServiceAwareAttachmentTableModelHelper;
import org.squashtest.tm.web.internal.controller.milestone.MilestoneFeatureConfiguration;
import org.squashtest.tm.web.internal.controller.milestone.MilestoneUIConfigurationService;
import org.squashtest.tm.web.internal.util.HTMLCleanupUtils;

import javax.inject.Inject;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
//XSS ok bflessel
/**
 * Helper class for Controllers which need to show classic and optimized execution runners.
 *
 * WTF Alert ! This class required major editing to fix Issue #2130 without time enough to make it clean afterwards.
 * Hence uglynesses such as overwriting in controller RunnerState properties set in this class.
 *
 * @author Gregory Fouquet
 *
 */
@Component
public class ExecutionRunnerControllerHelper {

	public static final String TEST_PLAN_ITEM_URL_PATTERN = "/test-suites/{0,number,####}/test-plan/{1,number,####}";
	public static final String TEST_SUITE_NEXT_EXECUTION_URL = "/test-suites/{0,number,####}/test-plan/{1,number,####}/next-execution/runner";
	public static final String ITERATION_NEXT_EXECUTION_URL = "/iterations/{0,number,####}/test-plan/{1,number,####}/next-execution/runner";
	public static final String CURRENT_STEP_URL_PATTERN = "/execute/{0,number,####}/step";

	public static final String COMPLETION_POPUP_TITLE = "popup.title.info";
	public static final String COMPLETED_SUITE_MESSAGE = "squashtm.action.exception.testsuite.end";
	public static final String COMPLETED_ITERATION_MESSAGE = "squashtm.action.exception.iteration.end";
	public static final String COMPLETED_STEP_MESSAGE = "execute.alert.test.complete";

	@Inject
	private ExecutionProcessingService executionProcessingService;

	@Inject
	private TestPlanExecutionProcessingService<TestSuite> testSuiteExecutionProcessingService;

	@Inject
	private TestPlanExecutionProcessingService<Iteration> iterationExecutionProcessingService;

	@Inject
	private ServiceAwareAttachmentTableModelHelper attachmentHelper;

	@Inject
	private MessageSource messageSource;

	@Inject
	private DenormalizedFieldValueManager denormalizedFieldValueFinder;

	@Inject
	private CustomFieldValueFinderService customFieldValueFinderService;

	@Inject
	private MilestoneUIConfigurationService milestoneConfService;

	@Inject
	private UserAccountService userService;

	@Inject
	private PartyPreferenceService preferenceService;

	@Inject
	private VerifiedRequirementsFinderService verifiedRequirementsFinderService;

	private ExecutionStep findStepAtIndex(long executionId, int stepIndex) {

		int stepCount = executionProcessingService.findTotalNumberSteps(executionId);

		if (stepIndex >= stepCount) {
			return executionProcessingService.findStepAt(executionId, stepCount - 1);
		}

		ExecutionStep executionStep = executionProcessingService.findStepAt(executionId, stepIndex);

		if (executionStep == null) {
			executionStep = executionProcessingService.findStepAt(executionId, stepCount - 1);
		}

		return executionStep;
	}

	public void populateStepAtIndexModel(long executionId, int stepIndex, Model model) {

		Execution execution = executionProcessingService.findExecution(executionId);
		ExecutionStep executionStep = findStepAtIndex(executionId, stepIndex);

		populateExecutionStepModel(execution, executionStep, model);
	}

	private void populateExecutionStepModel(Execution execution, ExecutionStep executionStep, Model model) {

		int stepOrder = 0;
		int total = execution.getSteps().size();

		Set<Attachment> attachments = Collections.emptySet();
		String bugtrackerMode = getBugTrackerModeFromUserPreferences();

		boolean hasDenormFields = false;
		boolean hasCustomFields = false;

		//TODO : check why we could want to process that page while part of the model is null (it should fail earlier when the DB cannot find this step)
		if (executionStep != null) {
			stepOrder = executionStep.getExecutionStepOrder();
			hasDenormFields = denormalizedFieldValueFinder.hasDenormalizedFields(executionStep);
			hasCustomFields = customFieldValueFinderService.hasCustomFields(executionStep);
			attachments = attachmentHelper.findAttachments(executionStep);
		}

		MilestoneFeatureConfiguration milestoneConf = milestoneConfService.configure(execution.getIteration());

		IsScriptedExecutionVisitor executionVisitor = new IsScriptedExecutionVisitor();
		execution.accept(executionVisitor);

		model.addAttribute("execution", execution);
		model.addAttribute("isExecutionScripted", executionVisitor.isScripted());
		model.addAttribute("executionStep", executionStep);
		model.addAttribute("hasDenormFields", hasDenormFields);
		model.addAttribute("hasCustomFields", hasCustomFields);
		model.addAttribute("totalSteps", total);
		model.addAttribute("hasNextStep", stepOrder != total - 1);
		model.addAttribute("attachments", attachments);
		model.addAttribute("allowsUntestable", execution.getProject().getCampaignLibrary().allowsStatus(ExecutionStatus.UNTESTABLE));
		model.addAttribute("allowsSettled", execution.getProject().getCampaignLibrary().allowsStatus(ExecutionStatus.SETTLED));
		model.addAttribute("milestoneConf", milestoneConf);
		model.addAttribute("bugtrackerMode", bugtrackerMode);

		addCurrentStepUrl(execution.getId(), model);
	}

	public void popuplateExecutionPreview(long executionId, boolean isOptimized, Model model) {
		populateExecutionPreview(executionId, isOptimized, new RunnerState(), model);
	}

	public void populateClassicSingleModel(Model model) {

		model.addAttribute("optimized", false);
	}

	public void populateOptimizedSingleModel(Model model) {

		model.addAttribute("optimized", true);
	}

	// ******************* IEO context model stuffing methods *******************************

	public RunnerState initOptimizedSingleContext(long executionId, String contextPath, Locale locale) {

		RunnerState state = createRunnerState(true);

		populatePopupMessages(state, locale, COMPLETED_SUITE_MESSAGE);
		populatePrologueStatus(executionId, state);
		populateEntitiesInfos(executionId, state, contextPath);

		return state;
	}

	public RunnerState createOptimizedRunnerStateForTestSuite(long testSuiteId, Execution execution, String contextPath,
												  Locale locale) {
		RunnerState state = createRunnerState(false);

		populateUndifferentiatedRunnerState(state, testSuiteId, execution, contextPath);
		populatePopupMessages(state, locale, COMPLETED_SUITE_MESSAGE);

		IterationTestPlanItem item = execution.getTestPlan();

		boolean hasNextTestCase = testSuiteExecutionProcessingService.hasMoreExecutableItems(testSuiteId, item.getId());

		populateNextExecutionUrl(state, contextPath, TEST_SUITE_NEXT_EXECUTION_URL, testSuiteId, item.getId());

		state.setLastTestCase(!hasNextTestCase);

		return state;
	}

	public RunnerState createOptimizedRunnerStateForIteration(long iterationId, Execution execution, String contextPath,
												  Locale locale) {
		RunnerState state = createRunnerState(false);

		populateUndifferentiatedRunnerState(state, iterationId, execution, contextPath);
		populatePopupMessages(state, locale, COMPLETED_ITERATION_MESSAGE);

		IterationTestPlanItem item = execution.getTestPlan();

		populateNextExecutionUrl(state, contextPath, ITERATION_NEXT_EXECUTION_URL, iterationId, item.getId());

		boolean hasNextTestCase = iterationExecutionProcessingService.hasMoreExecutableItems(iterationId, item.getId());

		state.setLastTestCase(!hasNextTestCase);

		return state;
	}

	// ******************* IEO runner state factory methods *************************

	public RunnerState createRunnerState(boolean isOptimized) {
		RunnerState state = new RunnerState();
		state.setOptimized(isOptimized);

		return state;

	}

	private void populateUndifferentiatedRunnerState(RunnerState state, long testPlanOwnerId, Execution execution, String contextPath){
		state.setTestSuiteId(testPlanOwnerId);
		state.setTestPlanItemId(execution.getTestPlan().getId());

		populatePrologueStatus(execution.getId(), state);
		populateEntitiesInfos(execution.getId(), state, contextPath);
	}

	private void populateNextExecutionUrl(RunnerState state, String contextPath, String nextExecutionUrlPattern, long testPlanOwnerId, long itemId){
		String nextExecutionUrl = contextPath  + MessageFormat.format(nextExecutionUrlPattern, testPlanOwnerId, itemId);
		state.setNextTestCaseUrl(nextExecutionUrl);
	}

	private void populatePrologueStatus(long executionId, RunnerState state) {
		if (executionProcessingService.wasNeverRun(executionId)) {
			state.setPrologue(true);
		} else {
			state.setPrologue(false);
		}
	}

	private void populateEntitiesInfos(long executionId, RunnerState state, String contextPath) {

		ExecutionStep step = executionProcessingService.findRunnableExecutionStep(executionId);
		int totalSteps = executionProcessingService.findTotalNumberSteps(executionId);

		boolean wasNeverExecuted = executionProcessingService.wasNeverRun(executionId);
		int stepOrder = wasNeverExecuted ? 0 : step.getExecutionStepOrder() + 1;

		String currentStepUrl = contextPath + MessageFormat.format(CURRENT_STEP_URL_PATTERN, executionId);

		state.setBaseStepUrl(currentStepUrl);
		// Issue 7137 - setting null to the referencedTestcaseId attribute when the test case is deleted
		Execution execution = executionProcessingService.findExecution(executionId);
		TestCase tc = execution.getReferencedTestCase();
		Long tcId = tc != null ? tc.getId() : null;
		state.setReferencedTestCaseId(tcId);

		state.setCurrentExecutionId(executionId);
		state.setCurrentStepId(step.getId());

		state.setFirstStepIndex(0);
		state.setLastStepIndex(totalSteps);
		state.setCurrentStepIndex(stepOrder); // +1 here : the interface uses 1-based counter
		state.setCurrentStepStatus(step.getExecutionStatus());

		CampaignLibrary lib = step.getProject().getCampaignLibrary();
		state.setAllowsSettled(lib.allowsStatus(ExecutionStatus.SETTLED));
		state.setAllowsUntestable(lib.allowsStatus(ExecutionStatus.UNTESTABLE));

	}

	private void populatePopupMessages(RunnerState state, Locale locale, String completeTestPlanMessage) {
		String popupTitle = messageSource.getMessage(COMPLETION_POPUP_TITLE, null, locale);
		String completeTestMessage = messageSource.getMessage(COMPLETED_STEP_MESSAGE, null, locale);
		String completeSuiteMessage = messageSource.getMessage(completeTestPlanMessage, null, locale);

		state.setCompleteTitle(HTMLCleanupUtils.cleanHtml(popupTitle));
		state.setCompleteTestMessage(HTMLCleanupUtils.cleanHtml(completeTestMessage));
		state.setCompleteSuiteMessage(HTMLCleanupUtils.cleanHtml(completeSuiteMessage));
	}

	// ************************ private stuff **************************

	// XXX BROKEN ! lameass fix : overwrite this prop afterwards. in some cases.
	private void addCurrentStepUrl(long executionId, Model model) {
		String currentStepUrl = MessageFormat.format(CURRENT_STEP_URL_PATTERN, executionId);
		model.addAttribute("currentStepsUrl", currentStepUrl);
	}

	/**
	 * @param executionId
	 * @param optimized
	 * @param runnerState
	 * @param model
	 */
	public void populateExecutionPreview(long executionId, boolean optimized, RunnerState runnerState, Model model) {
		Execution execution = executionProcessingService.findExecution(executionId);
		int totalSteps = executionProcessingService.findTotalNumberSteps(executionId);
		boolean hasCustomFields = customFieldValueFinderService.hasCustomFields(execution);
		boolean hasDenormFields = denormalizedFieldValueFinder.hasDenormalizedFields(execution);
		String bugtrackerMode = getBugTrackerModeFromUserPreferences();

		runnerState.setOptimized(optimized);
		runnerState.setPrologue(true);

		IsScriptedExecutionVisitor isScriptedExecVisitor = new IsScriptedExecutionVisitor();
		execution.accept(isScriptedExecVisitor);

		model.addAttribute("execution", execution);
		model.addAttribute("isExecutionScripted", isScriptedExecVisitor.isScripted());
		model.addAttribute("config", runnerState);
		model.addAttribute("totalSteps", totalSteps);
		model.addAttribute("attachments", attachmentHelper.findAttachments(execution));
		model.addAttribute("hasCustomFields", hasCustomFields);
		model.addAttribute("hasDenormFields", hasDenormFields);
		model.addAttribute("bugtrackerMode", bugtrackerMode);
		if(execution.getReferencedTestCase() != null) {
			List<VerifiedRequirement> verifiedRequirements = verifiedRequirementsFinderService.findAllVerifiedRequirementsByTestCaseId(execution.getReferencedTestCase().getId());
			List<RequirementVersion> requirementVersions = new ArrayList<>();
			for (VerifiedRequirement verifiedRequirement : verifiedRequirements) {
				requirementVersions.add(verifiedRequirement.getVerifiedRequirementVersion());
			}
			model.addAttribute("verifiedReqVersions", requirementVersions);
		}

	}

	private String getBugTrackerModeFromUserPreferences() {
		User user = userService.findCurrentUser();
		Party party = userService.getParty(user.getId());
		Map<String, String> map = preferenceService.findPreferences(party);
		return map.get("squash.bug.tracker.mode");
	}

}
