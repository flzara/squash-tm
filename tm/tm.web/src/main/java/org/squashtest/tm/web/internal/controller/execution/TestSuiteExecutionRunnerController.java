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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.csp.core.bugtracker.spi.BugTrackerInterfaceDescriptor;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.exception.NoBugTrackerBindingException;
import org.squashtest.tm.exception.execution.TestPlanItemNotExecutableException;
import org.squashtest.tm.exception.execution.TestPlanTerminatedOrNoStepsException;
import org.squashtest.tm.exception.execution.TestSuiteTestPlanHasDeletedTestCaseException;
import org.squashtest.tm.web.internal.controller.AcceptHeaders;
import org.squashtest.tm.web.internal.helper.JsonHelper;
import org.squashtest.tm.web.internal.model.json.JsonStepInfo;
import org.squashtest.tm.web.internal.util.HTMLCleanupUtils;

import java.text.MessageFormat;
import java.util.Locale;

import static java.util.stream.Collectors.toList;
import static org.squashtest.tm.web.internal.helper.JEditablePostParams.VALUE;

/**
 *
 * @author Gregory Fouquet
 *
 */
//XSS ok bflessel
@Controller
@RequestMapping("/test-suites/{testSuiteId}/test-plan")
public class TestSuiteExecutionRunnerController extends AbstractTestPlanExecutionRunnerController<TestSuite> {

	private static final String TEST_SUITE = "/test-suites";


	public TestSuiteExecutionRunnerController() {
		super();
	}

	/**
	 * TODO remplacer le test de l'url par un param "dry-run"
	 *
	 * @param testSuiteId
	 */
	@ResponseBody
	@RequestMapping(value = RequestMappingPattern.INIT_EXECUTION_RUNNER, method = RequestMethod.POST, params = {"mode=start-resume", "dry-run"})
	public void testStartResumeExecutionInClassicRunner(@PathVariable long testSuiteId) {
		super.testStartResumeExecutionInClassicRunner(testSuiteId);
	}

	@RequestMapping(value = RequestMappingPattern.INIT_EXECUTION_RUNNER, params = { "optimized=false", "!dry-run" })
	@ResponseStatus(HttpStatus.MOVED_PERMANENTLY)
	public String startResumeExecutionInClassicRunner(@PathVariable long testSuiteId) {
		return super.startResumeExecutionInClassicRunner(testSuiteId);
	}

	@RequestMapping(value = RequestMappingPattern.INIT_EXECUTION_RUNNER, params = { "optimized=true", "!dry-run" })
	public String startResumeExecutionInOptimizedRunner(@PathVariable long testSuiteId, Model model, Locale locale) {
		return super.startResumeExecutionInOptimizedRunner(testSuiteId, model, locale);
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
	@ResponseStatus(HttpStatus.MOVED_PERMANENTLY)
	public String moveToNextTestCase(@PathVariable("testPlanItemId") long testPlanItemId,
			@PathVariable("testSuiteId") long testSuiteId, @RequestParam("optimized") boolean optimized) {
		return super.moveToNextTestCase(testPlanItemId, testSuiteId, optimized);
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
		return super.getNextTestCaseRunnerState(testPlanItemId, testSuiteId, locale);
	}

	@ResponseBody
	@RequestMapping(value = RequestMappingPattern.DELETE_ALL_EXECUTIONS, method = RequestMethod.DELETE)
	public void deleteAllExecutions(@PathVariable long testSuiteId) {
		super.deleteAllExecutions(testSuiteId);
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
	@ResponseStatus(HttpStatus.MOVED_PERMANENTLY)
	public String runFirstRunnableStep(@PathVariable long testSuiteId, @PathVariable long testPlanItemId,
			@PathVariable long executionId, @RequestParam(defaultValue = "false") boolean optimized) {

		return super.runFirstRunnableStep(testSuiteId, testPlanItemId, executionId, optimized);

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

		return super.runPrologue(testSuiteId, testPlanItemId, executionId, optimized, model);

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

		return super.getClassicTestSuiteExecutionStepFragment(testSuiteId, testPlanItemId, executionId, stepIndex, model);

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

		return super.getOptimizedTestSuiteExecutionStepFragment(testSuiteId, testPlanItemId, executionId, stepIndex, model);

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
		return super.changeComment(newComment, stepId);
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
		super.changeExecutionStatus(executionStatus, stepId);
	}

	@RequestMapping(value = RequestMappingPattern.INDEXED_STEP + "/general", method = RequestMethod.GET)
	@ResponseBody
	public JsonStepInfo getBasicInfos(@PathVariable long executionId, @PathVariable int stepIndex) {
		return super.getBasicInfos(executionId, stepIndex);
	}

	@RequestMapping(value = RequestMappingPattern.INDEXED_STEP, method = RequestMethod.GET, params = "optimized", headers = AcceptHeaders.CONTENT_JSON)
	@ResponseBody
	public StepState getStepState(@PathVariable long executionId, @PathVariable int stepIndex) {
		return super.getStepState(executionId, stepIndex);

	}

	@Override
	Project getEntityProject(TestSuite entity) {
		return entity.getProject();
	}

	@Override
	String completeRessourceUrlPattern(String urlPattern) {
		return TEST_SUITE + urlPattern;
	}

	@Override
	boolean hasDeletedTestCaseInTestPlan(long testSuiteId) {
		return entityFinder.findById(testSuiteId).getTestPlan().stream().anyMatch(c -> c.getReferencedTestCase() == null);
	}
}
