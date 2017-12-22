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

import static org.squashtest.tm.web.internal.helper.JEditablePostParams.VALUE;

import java.text.MessageFormat;
import java.util.Locale;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.csp.core.bugtracker.core.BugTrackerConnectorFactory;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.csp.core.bugtracker.spi.BugTrackerInterfaceDescriptor;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.exception.NoBugTrackerBindingException;
import org.squashtest.tm.exception.execution.TestPlanItemNotExecutableException;
import org.squashtest.tm.exception.execution.TestPlanTerminatedOrNoStepsException;
import org.squashtest.tm.service.bugtracker.BugTrackersLocalService;
import org.squashtest.tm.service.campaign.TestSuiteExecutionProcessingService;
import org.squashtest.tm.service.campaign.TestSuiteFinder;
import org.squashtest.tm.service.execution.ExecutionProcessingService;
import org.squashtest.tm.web.internal.controller.AcceptHeaders;
import org.squashtest.tm.web.internal.model.json.JsonStepInfo;

/**
 *
 * @author Gregory Fouquet
 *
 */
@Controller
@RequestMapping("/test-suites/{testSuiteId}/test-plan")
public class TestSuiteExecutionRunnerController {

	private static final Logger LOGGER = LoggerFactory.getLogger(TestSuiteExecutionRunnerController.class);

	private static class RequestMappingPattern {
		public static final String INIT_EXECUTION_RUNNER = "/execution/runner";
		public static final String INIT_NEXT_EXECUTION_RUNNER = "/{testPlanItemId}/next-execution/runner";
		public static final String DELETE_ALL_EXECUTIONS = "/executions";
		public static final String STEP = "/{testPlanItemId}/executions/{executionId}/steps/{stepId}";
		public static final String INDEXED_STEP = "/{testPlanItemId}/executions/{executionId}/steps/index/{stepIndex}";
	}

	private static class ResourceUrlPattern {
		public static final String TEST_PLAN_ITEM = "/test-suites/{0,number,######}/test-plan/{1,number,######}";
		public static final String EXECUTION = TEST_PLAN_ITEM + "/executions/{2,number,######}";
		public static final String STEPS = EXECUTION + "/steps";
		public static final String STEP_INDEX = STEPS + "/index/{3,number,######}";
		public static final String PROLOGUE_STEP = STEPS + "/prologue";
		public static final String STEP = STEPS + "/{3,number,######}";
	}

	private static final String OPTIMIZED_RUNNER_MAIN = "page/executions/oer-main-page";

	@Inject
	private BugTrackerConnectorFactory btFactory;

	@Inject
	private TestSuiteExecutionProcessingService testSuiteExecutionRunner;

	@Inject
	private ExecutionProcessingService executionRunner;

	@Inject
	private TestSuiteFinder suiteFinder;

	@Inject
	private ExecutionRunnerControllerHelper helper;

	@Inject
	private ExecutionProcessingController executionProcessingController;

	@Inject
	private ServletContext servletContext;

	@Inject
	private BugTrackersLocalService bugTrackersLocalService;


	public TestSuiteExecutionRunnerController() {
		super();
	}

	private String getExecutionUrl(long testSuiteId, Execution execution, boolean optimized) {
		return MessageFormat.format(ResourceUrlPattern.EXECUTION, testSuiteId, execution.getTestPlan().getId(),
				execution.getId()) + "?optimized=" + optimized;
	}

	/**
	 * TODO remplacer le test de l'url par un param "dry-run"
	 *
	 * @param testSuiteId
	 */
	@ResponseBody
	@RequestMapping(value = RequestMappingPattern.INIT_EXECUTION_RUNNER, method = RequestMethod.POST, params = {"mode=start-resume", "dry-run"})
	public
	void testStartResumeExecutionInClassicRunner(@PathVariable long testSuiteId) {
		try {
			testSuiteExecutionRunner.startResume(testSuiteId);
		} catch (TestPlanItemNotExecutableException e) {
			throw new TestPlanTerminatedOrNoStepsException(e);
		}
	}

	@RequestMapping(value = RequestMappingPattern.INIT_EXECUTION_RUNNER, params = { "optimized=false", "!dry-run" })
	public String startResumeExecutionInClassicRunner(@PathVariable long testSuiteId) {
		LOGGER.trace("startResumeExecutionInClassicRunner({})", testSuiteId);

		Execution execution = testSuiteExecutionRunner.startResume(testSuiteId);

		return "redirect:" + getExecutionUrl(testSuiteId, execution, false);

	}

	@RequestMapping(value = RequestMappingPattern.INIT_EXECUTION_RUNNER, params = { "optimized=true", "!dry-run" })
	public String startResumeExecutionInOptimizedRunner(@PathVariable long testSuiteId, Model model, Locale locale) {
		LOGGER.trace("startResumeExecutionInOptimizedRunner({})", testSuiteId);

		Execution execution = testSuiteExecutionRunner.startResume(testSuiteId);
		RunnerState state = helper.createOptimizedRunnerState(testSuiteId, execution, contextPath(), locale);
		state.setBaseStepUrl(stepsAbsoluteUrl(testSuiteId, execution));
		model.addAttribute("config", state);

		try{
			Project project = suiteFinder.findById(testSuiteId).getProject();
			BugTracker bugtracker = project.findBugTracker();
			BugTrackerInterfaceDescriptor descriptor = bugTrackersLocalService.getInterfaceDescriptor(bugtracker);
			model.addAttribute("interfaceDescriptor", descriptor);
			model.addAttribute("bugTracker", bugtracker);
			model.addAttribute("isOslc", btFactory.isOslcConnector(bugtracker.getKind()));
		}
		catch(NoBugTrackerBindingException ex){
			LOGGER.debug("Well, no bugtracker then. It's fine.", ex);
		}

		return OPTIMIZED_RUNNER_MAIN;

	}

	/**
	 * Returns absolute url for the steps collection of an execution, ie including this webapp's context path.
	 *
	 * @param testSuiteId
	 * @param execution
	 * @return
	 */
	private String stepsAbsoluteUrl(long testSuiteId, Execution execution) {
		return contextPath()
				+ MessageFormat.format(ResourceUrlPattern.STEPS, testSuiteId, execution.getTestPlan().getId(),
						execution.getId());
	}

	/**
	 * That method will create if necessary the next execution then redirect a view to its runner. It matches the
	 * "next test case" button in classic mode
	 *
	 * \o/ There should be a params = "optimized" in this method's {@link RequestMapping}. We omitted it because
	 * "params" has more precedence than "headers", which leads to requests meant to be processed by
	 * {@link #getNextTestCaseRunnerState(long, long, Locale)} being routed to this method instead.
	 *
	 * @param testPlanItemId
	 * @param testSuiteId
	 * @param optimized
	 * @return
	 */
	@RequestMapping(value = RequestMappingPattern.INIT_NEXT_EXECUTION_RUNNER)
	public String moveToNextTestCase(@PathVariable("testPlanItemId") long testPlanItemId,
			@PathVariable("testSuiteId") long testSuiteId, @RequestParam("optimized") boolean optimized) {
		LOGGER.trace("moveToNextTestCase({}, {})", testPlanItemId, testSuiteId);

		Execution execution = testSuiteExecutionRunner.startResumeNextExecution(testSuiteId, testPlanItemId);

		return "redirect:" + getExecutionUrl(testSuiteId, execution, optimized);

	}

	/**
	 * That method will create if necessary the next execution then return the RunnerState that corresponds to it note
	 * that most of the time it corresponds to an ieo working in test suite mode so we skip 'optimized' and 'suitemode'
	 * parameters here
	 *
	 * It is called by the optimized runner only
	 *
	 * headers parameter is required otherwise there is an ambiguity with
	 * {@link #moveToNextTestCase(long, long, boolean)}
	 *
	 * @param testPlanItemId
	 * @param testSuiteId
	 * @param locale
	 * @return
	 */
	@RequestMapping(value = RequestMappingPattern.INIT_NEXT_EXECUTION_RUNNER, headers = AcceptHeaders.CONTENT_JSON)
	@ResponseBody
	public RunnerState getNextTestCaseRunnerState(@PathVariable("testPlanItemId") long testPlanItemId,
			@PathVariable("testSuiteId") long testSuiteId, Locale locale) {
		LOGGER.trace("getNextTestCaseRunnerState({}, {})", testPlanItemId, testSuiteId);

		Execution nextExecution = testSuiteExecutionRunner.startResumeNextExecution(testSuiteId, testPlanItemId);
		RunnerState state = helper.createOptimizedRunnerState(testSuiteId, nextExecution, contextPath(), locale);
		state.setBaseStepUrl(stepsAbsoluteUrl(testSuiteId, nextExecution));

		return state;

	}

	/**
	 * /!\ Context path includes a heading "/"
	 * @return this webapp's context path
	 */
	private String contextPath() {
		return servletContext.getContextPath();
	}

	@ResponseBody
	@RequestMapping(value = RequestMappingPattern.DELETE_ALL_EXECUTIONS, method = RequestMethod.DELETE)
	public
	void deleteAllExecutions(@PathVariable long testSuiteId) {
		testSuiteExecutionRunner.deleteAllExecutions(testSuiteId);
	}

	/**
	 * requests a view for the first executable step of the given execution.
	 *
	 * @param testSuiteId
	 * @param testPlanItemId
	 * @param executionId
	 * @param optimized
	 * @return
	 */
	@RequestMapping(value = "{testPlanItemId}/executions/{executionId}", method = RequestMethod.GET)
	public String runFirstRunnableStep(@PathVariable long testSuiteId, @PathVariable long testPlanItemId,
			@PathVariable long executionId, @RequestParam(defaultValue = "false") boolean optimized) {

		String viewName;

		if (executionRunner.wasNeverRun(executionId)) {
			viewName = MessageFormat.format(ResourceUrlPattern.PROLOGUE_STEP, testSuiteId, testPlanItemId, executionId);
		} else {
			int stepIndex = executionRunner.findRunnableExecutionStep(executionId).getExecutionStepOrder();
			viewName = MessageFormat.format(ResourceUrlPattern.STEP_INDEX, testSuiteId, testPlanItemId, executionId,
					stepIndex);

		}

		return "redirect:" + viewName + "?optimized=" + optimized;

	}

	/**
	 * returns the execution prologue
	 *
	 * @param testSuiteId
	 * @param testPlanItemId
	 * @param executionId
	 * @param optimized
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "{testPlanItemId}/executions/{executionId}/steps/prologue", method = RequestMethod.GET)
	public String runPrologue(@PathVariable long testSuiteId, @PathVariable long testPlanItemId,
			@PathVariable long executionId, @RequestParam(defaultValue = "false") boolean optimized, Model model) {

		addStepsUrl(testSuiteId, testPlanItemId, executionId, model);
		helper.populateExecutionPreview(executionId, optimized, suiteRunnerState(testSuiteId, testPlanItemId), model);

		return ExecutionRunnerViewName.PROLOGUE_STEP;

	}

	/**
	 * @param testSuiteId
	 * @param testPlanItemId
	 * @return
	 */
	private RunnerState suiteRunnerState(long testSuiteId, long testPlanItemId) {
		RunnerState state = new RunnerState();
		state.setTestSuiteId(testSuiteId);
		state.setTestPlanItemId(testPlanItemId);
		return state;
	}

	/**
	 * Returns classic runner fragment for the given step.
	 *
	 * @param testSuiteId
	 * @param testPlanItemId
	 * @param executionId
	 * @param stepIndex
	 * @param model
	 * @return
	 */
	//note : add to the mapping ', headers = ACCEPT_HTML_HEADER' if needed.
	@RequestMapping(value = RequestMappingPattern.INDEXED_STEP, method = RequestMethod.GET, params = { "optimized=false" })
	public String getClassicTestSuiteExecutionStepFragment(@PathVariable long testSuiteId,
			@PathVariable long testPlanItemId, @PathVariable long executionId, @PathVariable int stepIndex, Model model) {

		populateExecutionStepFragment(testSuiteId, testPlanItemId, executionId, stepIndex, model);


		return ExecutionRunnerViewName.CLASSIC_STEP;

	}

	private void populateExecutionStepFragment(long testSuiteId, long testPlanItemId, long executionId, int stepIndex,
			Model model) {
		helper.populateStepAtIndexModel(executionId, stepIndex, model);

		model.addAttribute("testPlanItemUrl",
				MessageFormat.format(ResourceUrlPattern.TEST_PLAN_ITEM, testSuiteId, testPlanItemId));

		boolean hasNextTestCase = testSuiteExecutionRunner.hasMoreExecutableItems(testSuiteId, testPlanItemId);
		model.addAttribute("hasNextTestCase", hasNextTestCase);

		addStepsUrl(testSuiteId, testPlanItemId, executionId, model);
	}

	private void addStepsUrl(long testSuiteId, long testPlanItemId, long executionId, Model model) {
		model.addAttribute("currentStepsUrl",
				MessageFormat.format(ResourceUrlPattern.STEPS, testSuiteId, testPlanItemId, executionId));
	}

	/**
	 * Returns Optimized runner fragment for given step.
	 *
	 * @param testSuiteId
	 * @param testPlanItemId
	 * @param executionId
	 * @param stepIndex
	 * @param model
	 * @return
	 */
	//note : add to the mapping ', headers = ACCEPT_HTML_HEADER' if needed.
	@RequestMapping(value = RequestMappingPattern.INDEXED_STEP, method = RequestMethod.GET, params = { "optimized=true" })
	public String getOptimizedTestSuiteExecutionStepFragment(@PathVariable long testSuiteId,
			@PathVariable long testPlanItemId, @PathVariable long executionId, @PathVariable int stepIndex, Model model) {

		populateExecutionStepFragment(testSuiteId, testPlanItemId, executionId, stepIndex, model);


		return ExecutionRunnerViewName.OPTIMIZED_RUNNER_STEP;

	}

	/**
	 * changes execution comment
	 *
	 * @param newComment
	 * @param stepId
	 * @return
	 */
	@RequestMapping(value = RequestMappingPattern.STEP, method = RequestMethod.POST, params = { "id=execution-comment",
			VALUE })
	@ResponseBody
	public String changeComment(@RequestParam(VALUE) String newComment, @PathVariable long stepId) {
		return executionProcessingController.updateComment(newComment, stepId);
	}

	/**
	 * changes execution step status
	 *
	 * @param executionStatus
	 * @param stepId
	 */
	@RequestMapping(value = RequestMappingPattern.STEP, method = RequestMethod.POST, params = "executionStatus")
	@ResponseBody
	public void changeExecutionStatus(@RequestParam String executionStatus, @PathVariable long stepId) {
		executionProcessingController.updateExecutionStatus(executionStatus, stepId);
	}

	@RequestMapping(value = RequestMappingPattern.INDEXED_STEP + "/general", method = RequestMethod.GET)
	@ResponseBody
	public JsonStepInfo getBasicInfos(@PathVariable long executionId, @PathVariable int stepIndex) {
		return executionProcessingController.getBasicInfos(executionId, stepIndex);
	}

	@RequestMapping(value = RequestMappingPattern.INDEXED_STEP, method = RequestMethod.GET, params = "optimized", headers = AcceptHeaders.CONTENT_JSON)
	@ResponseBody
	public StepState getStepState(@PathVariable long executionId, @PathVariable int stepIndex) {
		return executionProcessingController.getStepState(executionId, stepIndex);

	}
}
