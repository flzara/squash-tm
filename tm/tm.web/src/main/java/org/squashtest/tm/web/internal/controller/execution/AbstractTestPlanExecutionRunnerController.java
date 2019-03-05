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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.csp.core.bugtracker.spi.BugTrackerInterfaceDescriptor;
import org.squashtest.tm.core.foundation.exception.ActionException;
import org.squashtest.tm.domain.campaign.TestPlanOwner;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.exception.NoBugTrackerBindingException;
import org.squashtest.tm.exception.execution.TestPlanItemNotExecutableException;
import org.squashtest.tm.exception.execution.TestPlanTerminatedOrNoStepsException;
import org.squashtest.tm.service.bugtracker.BugTrackersLocalService;
import org.squashtest.tm.service.campaign.EntityFinder;
import org.squashtest.tm.service.execution.ExecutionProcessingService;
import org.squashtest.tm.service.internal.bugtracker.BugTrackerConnectorFactory;
import org.squashtest.tm.service.campaign.TestPlanExecutionProcessingService;
import org.squashtest.tm.web.internal.helper.JsonHelper;
import org.squashtest.tm.web.internal.model.json.JsonStepInfo;
import org.squashtest.tm.web.internal.util.HTMLCleanupUtils;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import java.text.MessageFormat;
import java.util.Locale;

import static java.util.stream.Collectors.toList;

/**
 * Abstract class for controller classes responsible of running an entire test plan
 * @param <E> class of the entity owning the test plan (for now, a {@link org.squashtest.tm.domain.campaign.TestSuite} or an {@link org.squashtest.tm.domain.campaign.Iteration}
 * @author aguilhem
 */
public abstract class AbstractTestPlanExecutionRunnerController<E extends TestPlanOwner> {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTestPlanExecutionRunnerController.class);

	 static class RequestMappingPattern {
		static final String INIT_EXECUTION_RUNNER = "/execution/runner";
		static final String INIT_NEXT_EXECUTION_RUNNER = "/{testPlanItemId}/next-execution/runner";
		static final String DELETE_ALL_EXECUTIONS = "/executions";
		static final String STEP = "/{testPlanItemId}/executions/{executionId}/steps/{stepId}";
		static final String INDEXED_STEP = "/{testPlanItemId}/executions/{executionId}/steps/index/{stepIndex}";
	}

	private static class ResourceUrlPattern {
		static final String TEST_PLAN_ITEM = "/{0,number,######}/test-plan/{1,number,######}";
		static final String EXECUTION = TEST_PLAN_ITEM + "/executions/{2,number,######}";
		static final String STEPS = EXECUTION + "/steps";
		static final String STEP_INDEX = STEPS + "/index/{3,number,######}";
		static final String PROLOGUE_STEP = STEPS + "/prologue";
		static final String STEP = STEPS + "/{3,number,######}";
	}

	private static final String OPTIMIZED_RUNNER_MAIN = "page/executions/oer-main-page";
	private static final String REDIRECT ="redirect:";

	@Inject
	private TestPlanExecutionProcessingService<E> testPlanExecutionRunner;

	@Inject
	private BugTrackerConnectorFactory btFactory;

	@Inject
	private ExecutionProcessingService executionRunner;

	@Inject
	EntityFinder<E> entityFinder;

	@Inject
	ExecutionRunnerControllerHelper helper;

	@Inject
	private ExecutionProcessingController executionProcessingController;

	@Inject
	private ServletContext servletContext;

	@Inject
	private BugTrackersLocalService bugTrackersLocalService;

	/**
	 * Concatenate prefix specific to the {@link TestPlanOwner} to an url pattern
	 * @param urlPattern URL pattern to complete
	 * @return the completed url pattern
	 */
	abstract String completeRessourceUrlPattern(String urlPattern);

	/**
	 * Getter for the project link to the {@link TestPlanOwner}
	 * @param entity the {@link TestPlanOwner}
	 * @return the test plan owner's project
	 */
	abstract Project getEntityProject(E entity);

	/**
	 * Create a {@link RunnerState} for an {@link Execution}.
	 * @param testPlanOwnerId id of the {@link TestPlanOwner}
	 * @param execution the {@link Execution} to execute
	 * @param contextPath the web app's context path
	 * @param locale the {@link Locale} to use
	 * @return a {@link RunnerState} optimized for the {@link Execution} and the {@link TestPlanOwner}.
	 */
	abstract RunnerState createOptimizedRunnerState(long testPlanOwnerId, Execution execution, String contextPath, Locale locale);

	String getExecutionUrl(long testPlanOwnerId, Execution execution, boolean optimized) {
		return MessageFormat.format(completeRessourceUrlPattern(ResourceUrlPattern.EXECUTION), testPlanOwnerId, execution.getTestPlan().getId(),
			execution.getId()) + "?optimized=" + optimized;
	}

	/**
	 * TODO remplacer le test de l'url par un param "dry-run"
	 *
	 * @param testPlanOwnerId id of the {@link TestPlanOwner}
	 */
	public void testStartResumeExecutionInClassicRunner(long testPlanOwnerId) {
		boolean hasDeletedTestCaseInTestPlan = hasDeletedTestCaseInTestPlan(testPlanOwnerId);
		if (! hasDeletedTestCaseInTestPlan) {
			try {
				testPlanExecutionRunner.startResume(testPlanOwnerId);
			} catch (TestPlanItemNotExecutableException e) {
				throw new TestPlanTerminatedOrNoStepsException(e);
			}
		} else {
			throw getTestPlanHasDeletedTestCaseException();
		}
	}

	/**
	 * Getter for the {@link TestPlanOwner} specific "HasDeletedTestCaseException"
	 * @return {@link TestPlanOwner} specific "HasDeletedTestCaseException".
	 * For example, {@link org.squashtest.tm.exception.execution.IterationTestPlanHasDeletedTestCaseException}
	 */
	abstract ActionException getTestPlanHasDeletedTestCaseException();

	/**
	 * Issue 7366
	 * Method which tests if a {@link TestPlanOwner} has at least one deleted test case in its test plan
	 *
	 * @param testPlanOwnerId id of the {@link TestPlanOwner}
	 * @return true if test suite has at least one deleted test case in its test plan
	 */
	abstract boolean hasDeletedTestCaseInTestPlan(long testPlanOwnerId);

	String startResumeExecutionInClassicRunner(long testPlanOwnerId) {
		LOGGER.trace("startResumeExecutionInClassicRunner({})", testPlanOwnerId);

		Execution execution = testPlanExecutionRunner.startResume(testPlanOwnerId);

		return REDIRECT + getExecutionUrl(testPlanOwnerId, execution, false);

	}

	String startResumeExecutionInOptimizedRunner(long testPlanOwnerId, Model model, Locale locale) {
		LOGGER.trace("startResumeExecutionInOptimizedRunner({})", testPlanOwnerId);

		Execution execution = testPlanExecutionRunner.startResume(testPlanOwnerId);
		RunnerState state = createOptimizedRunnerState(testPlanOwnerId, execution, contextPath(), locale);
		state.setBaseStepUrl(stepsAbsoluteUrl(testPlanOwnerId, execution));
		model.addAttribute("config", state);

		try{
			Project project = getEntityProject(entityFinder.findById(testPlanOwnerId));
			BugTracker bugtracker = project.findBugTracker();
			BugTrackerInterfaceDescriptor descriptor = bugTrackersLocalService.getInterfaceDescriptor(bugtracker);
			String projectNames = JsonHelper.serialize(project.getBugtrackerBinding().getProjectNames()
				.stream()
				.map(HTMLCleanupUtils::cleanAndUnescapeHTML)
				.collect(toList()));
			model.addAttribute("interfaceDescriptor", descriptor);
			model.addAttribute("bugTracker", bugtracker);
			model.addAttribute("isOslc", btFactory.isOslcConnector(bugtracker.getKind()));
			model.addAttribute("projectId", project.getId());
			model.addAttribute("projectNames", projectNames);
		}
		catch(NoBugTrackerBindingException ex){
			LOGGER.debug("Well, no bugtracker then. It's fine.", ex);
		}

		return OPTIMIZED_RUNNER_MAIN;

	}

	/**
	 * That method will create if necessary the next execution then redirect a view to its runner. It matches the
	 * "next test case" button in classic mode
	 *
	 * @param testPlanItemId {@link org.squashtest.tm.domain.campaign.IterationTestPlanItem} id to move to.
	 * @param testPlanOwnerId {@link TestPlanOwner} id
	 * @param optimized whether the execution runner will be optimized or not
	 * @return a redirection url to the next test case execution
	 */
	String moveToNextTestCase(long testPlanItemId, long testPlanOwnerId, boolean optimized) {
		LOGGER.trace("moveToNextTestCase({}, {})", testPlanItemId, testPlanOwnerId);

		Execution execution = testPlanExecutionRunner.startResumeNextExecution(testPlanOwnerId, testPlanItemId);

		return REDIRECT + getExecutionUrl(testPlanOwnerId, execution, optimized);

	}

	/**
	 * That method will create if necessary the next execution then return the RunnerState that corresponds to it note
	 * that most of the time it corresponds to an ieo working in test suite mode so we skip 'optimized' and 'suitemode'
	 * parameters here
	 *
	 * It is called by the optimized runner only
	 *
	 * @param testPlanItemId id of the {@link org.squashtest.tm.domain.campaign.IterationTestPlanItem} to execute
	 * @param testPlanOwnerId {@link TestPlanOwner} id
	 * @param locale the {@link Locale} to use
	 * @return the {@link RunnerState} for the newly created {@link Execution}
	 */
	RunnerState getNextTestCaseRunnerState(long testPlanItemId, long testPlanOwnerId, Locale locale) {
		LOGGER.trace("getNextTestCaseRunnerState({}, {})", testPlanItemId, testPlanOwnerId);

		Execution nextExecution = testPlanExecutionRunner.startResumeNextExecution(testPlanOwnerId, testPlanItemId);
		RunnerState state = createOptimizedRunnerState(testPlanOwnerId, nextExecution, contextPath(), locale);
		state.setBaseStepUrl(stepsAbsoluteUrl(testPlanOwnerId, nextExecution));

		return state;

	}

	/**
	 * Delete all {@link Execution} of the {@link TestPlanOwner}'s {@link org.squashtest.tm.domain.campaign.IterationTestPlanItem}
	 * @param testPlanOwnerId id of the {@link TestPlanOwner}
	 */
	void deleteAllExecutions(long testPlanOwnerId) {
		testPlanExecutionRunner.deleteAllExecutions(testPlanOwnerId);
	}

	/**
	 * requests a view for the first executable step of the given execution.
	 *
	 * @param testPlanOwnerId {@link TestPlanOwner} id
	 * @param testPlanItemId id of the {@link org.squashtest.tm.domain.campaign.IterationTestPlanItem} to execute
	 * @param executionId id of the {@link Execution} to execute
	 * @param optimized optimized parameter
	 * @return a redirection url to the view
	 */
	String runFirstRunnableStep(long testPlanOwnerId, long testPlanItemId, long executionId, boolean optimized) {

		String viewName;

		if (executionRunner.wasNeverRun(executionId)) {
			viewName = MessageFormat.format(completeRessourceUrlPattern(ResourceUrlPattern.PROLOGUE_STEP), testPlanOwnerId, testPlanItemId, executionId);
		} else {
			int stepIndex = executionRunner.findRunnableExecutionStep(executionId).getExecutionStepOrder();
			viewName = MessageFormat.format(completeRessourceUrlPattern(ResourceUrlPattern.STEP_INDEX), testPlanOwnerId, testPlanItemId, executionId,
				stepIndex);

		}

		return REDIRECT + viewName + "?optimized=" + optimized;

	}

	/**
	 * returns the execution prologue
	 *
	 * @param testPlanOwnerId {@link TestPlanOwner} id
	 * @param testPlanItemId {@link org.squashtest.tm.domain.campaign.IterationTestPlanItem} id to run
	 * @param executionId {@link Execution} to run
	 * @param optimized optimized parameter
	 * @param model {@link Model} to populate
	 * @return the {@link ExecutionRunnerViewName}
	 */
	String runPrologue(long testPlanOwnerId, long testPlanItemId, long executionId, boolean optimized, Model model) {

		addStepsUrl(testPlanOwnerId, testPlanItemId, executionId, model);
		helper.populateExecutionPreview(executionId, optimized, testPlanRunnerState(testPlanOwnerId, testPlanItemId), model);

		return ExecutionRunnerViewName.PROLOGUE_STEP;

	}

	/**
	 * Initialize the test plan runner state
	 * @param testPlanOwnerId id of the {@link TestPlanOwner}
	 * @param testPlanItemId {@link org.squashtest.tm.domain.campaign.IterationTestPlanItem} id to run
	 * @return the {@link RunnerState}
	 */
	private RunnerState testPlanRunnerState(long testPlanOwnerId, long testPlanItemId) {
		RunnerState state = new RunnerState();
		state.setTestSuiteId(testPlanOwnerId);
		state.setTestPlanItemId(testPlanItemId);
		return state;
	}

	/**
	 * Returns classic runner fragment for the given step.
	 *
	 * @param testPlanOwnerId id of the {@link TestPlanOwner}
	 * @param testPlanItemId id of the {@link org.squashtest.tm.domain.campaign.IterationTestPlanItem}
	 * @param executionId id of the {@link Execution}
	 * @param stepIndex index of the {@link org.squashtest.tm.domain.execution.ExecutionStep}
	 * @param model {@link Model} to populate
	 * @return the {@link ExecutionRunnerViewName}
	 */
	//note : add to the mapping ', headers = ACCEPT_HTML_HEADER' if needed.
	String getClassicTestSuiteExecutionStepFragment(@PathVariable long testPlanOwnerId,
														   @PathVariable long testPlanItemId, @PathVariable long executionId, @PathVariable int stepIndex, Model model) {

		populateExecutionStepFragment(testPlanOwnerId, testPlanItemId, executionId, stepIndex, model);


		return ExecutionRunnerViewName.CLASSIC_STEP;

	}

	private void populateExecutionStepFragment(long testPlanOwnerId, long testPlanItemId, long executionId, int stepIndex,
											   Model model) {
		helper.populateStepAtIndexModel(executionId, stepIndex, model);

		model.addAttribute("testPlanItemUrl",
			MessageFormat.format(completeRessourceUrlPattern(ResourceUrlPattern.TEST_PLAN_ITEM), testPlanOwnerId, testPlanItemId));

		boolean hasNextTestCase = testPlanExecutionRunner.hasMoreExecutableItems(testPlanOwnerId, testPlanItemId);
		model.addAttribute("hasNextTestCase", hasNextTestCase);

		addStepsUrl(testPlanOwnerId, testPlanItemId, executionId, model);
	}

	private void addStepsUrl(long testPlanOwnerId, long testPlanItemId, long executionId, Model model) {
		model.addAttribute("currentStepsUrl",
			MessageFormat.format(completeRessourceUrlPattern(ResourceUrlPattern.STEPS), testPlanOwnerId, testPlanItemId, executionId));
	}

	/**
	 * Returns Optimized runner fragment for given step.
	 *
	 * @param testPlanOwnerId id of the {@link TestPlanOwner}
	 * @param testPlanItemId id of the {@link org.squashtest.tm.domain.campaign.IterationTestPlanItem}
	 * @param executionId id of the {@link Execution}
	 * @param stepIndex index of the {@link org.squashtest.tm.domain.execution.ExecutionStep}
	 * @param model {@link Model} to populate
	 * @return the {@link ExecutionRunnerViewName}
	 */
	//note : add to the mapping ', headers = ACCEPT_HTML_HEADER' if needed.
	String getOptimizedTestSuiteExecutionStepFragment(long testPlanOwnerId, long testPlanItemId, long executionId, int stepIndex, Model model) {

		populateExecutionStepFragment(testPlanOwnerId, testPlanItemId, executionId, stepIndex, model);


		return ExecutionRunnerViewName.OPTIMIZED_RUNNER_STEP;

	}

	/**
	 * changes execution comment
	 *
	 * @param newComment the new comment
	 * @param stepId id of the {@link org.squashtest.tm.domain.execution.ExecutionStep} to comment
	 * @return the HTML cleaned new comment
	 */
	String changeComment(String newComment, long stepId) {
		return HTMLCleanupUtils.cleanHtml(executionProcessingController.updateComment(newComment, stepId));
	}

	/**
	 * changes execution step status
	 *
	 * @param executionStatus the new execution status
	 * @param stepId id of the {@link org.squashtest.tm.domain.execution.ExecutionStep} to change execution status
	 */
	void changeExecutionStatus(String executionStatus, long stepId) {
		executionProcessingController.updateExecutionStatus(executionStatus, stepId);
	}

	JsonStepInfo getBasicInfos(long executionId, int stepIndex) {
		return executionProcessingController.getBasicInfos(executionId, stepIndex);
	}

	StepState getStepState(long executionId, int stepIndex) {
		return executionProcessingController.getStepState(executionId, stepIndex);

	}
	/**
	 * /!\ Context path includes a heading "/"
	 * @return this webapp's context path
	 */
	private String contextPath() {
		return servletContext.getContextPath();
	}

	/**
	 * Returns absolute url for the steps collection of an execution, ie including this webapp's context path.
	 *
	 * @param testPlanOwnerId {@link TestPlanOwner} id
	 * @param execution {@link Execution} we want steps collection absolute url from
	 * @return absolute url for the steps collection of the given execution
	 */
	private String stepsAbsoluteUrl(long testPlanOwnerId, Execution execution) {
		return contextPath()
			+ MessageFormat.format(completeRessourceUrlPattern(ResourceUrlPattern.STEPS), testPlanOwnerId, execution.getTestPlan().getId(),
			execution.getId());
	}
}
