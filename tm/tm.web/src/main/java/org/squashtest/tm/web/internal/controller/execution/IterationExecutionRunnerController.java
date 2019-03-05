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

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.squashtest.tm.core.foundation.exception.ActionException;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.exception.execution.IterationTestPlanHasDeletedTestCaseException;
import org.squashtest.tm.web.internal.controller.AcceptHeaders;
import org.squashtest.tm.web.internal.model.json.JsonStepInfo;

import java.util.Locale;

import static org.squashtest.tm.web.internal.helper.JEditablePostParams.VALUE;

/**
 * @author aguilhem
 */
@Controller
@RequestMapping("/iterations/{iterationId}/test-plan")
public class IterationExecutionRunnerController extends AbstractTestPlanExecutionRunnerController<Iteration> {

	private static final String ITERATION = "/iterations";


	public IterationExecutionRunnerController() {
		super();
	}

	/**
	 * TODO remplacer le test de l'url par un param "dry-run"
	 *
	 * @param iterationId id of the {@link Iteration} to execute.
	 */
	@ResponseBody
	@RequestMapping(value = RequestMappingPattern.INIT_EXECUTION_RUNNER, method = RequestMethod.POST, params = {"mode=start-resume", "dry-run"})
	public void testStartResumeExecutionInClassicRunner(@PathVariable long iterationId) {
		super.testStartResumeExecutionInClassicRunner(iterationId);
	}

	@Override
	ActionException getTestPlanHasDeletedTestCaseException() {
		return new IterationTestPlanHasDeletedTestCaseException();
	}

	@RequestMapping(value = RequestMappingPattern.INIT_EXECUTION_RUNNER, params = { "optimized=false", "!dry-run" })
	@ResponseStatus(HttpStatus.MOVED_PERMANENTLY)
	public String startResumeExecutionInClassicRunner(@PathVariable long iterationId) {
		return super.startResumeExecutionInClassicRunner(iterationId);
	}

	@RequestMapping(value = RequestMappingPattern.INIT_EXECUTION_RUNNER, params = { "optimized=true", "!dry-run" })
	public String startResumeExecutionInOptimizedRunner(@PathVariable long iterationId, Model model, Locale locale) {
		return super.startResumeExecutionInOptimizedRunner(iterationId, model, locale);
	}

	/**
	 * @see AbstractTestPlanExecutionRunnerController#moveToNextTestCase(long, long, boolean)
	 *
	 * \o/ There should be a params = "optimized" in this method's {@link RequestMapping}. We omitted it because
	 * "params" has more precedence than "headers", which leads to requests meant to be processed by
	 * {@link #getNextTestCaseRunnerState(long, long, Locale)} being routed to this method instead.
	 */
	@RequestMapping(value = RequestMappingPattern.INIT_NEXT_EXECUTION_RUNNER)
	@ResponseStatus(HttpStatus.MOVED_PERMANENTLY)
	public String moveToNextTestCase(@PathVariable("testPlanItemId") long testPlanItemId,
									 @PathVariable("iterationId") long iterationId, @RequestParam("optimized") boolean optimized) {
		return super.moveToNextTestCase(testPlanItemId, iterationId, optimized);
	}

	/**
	 * @see AbstractTestPlanExecutionRunnerController#getNextTestCaseRunnerState(long, long, Locale)
	 *
	 * headers parameter is required otherwise there is an ambiguity with
	 * {@link #moveToNextTestCase(long, long, boolean)}
	 *
	 */
	@RequestMapping(value = RequestMappingPattern.INIT_NEXT_EXECUTION_RUNNER, headers = AcceptHeaders.CONTENT_JSON)
	@ResponseBody
	public RunnerState getNextTestCaseRunnerState(@PathVariable("testPlanItemId") long testPlanItemId,
												  @PathVariable("iterationId") long iterationId, Locale locale) {
		return super.getNextTestCaseRunnerState(testPlanItemId, iterationId, locale);
	}

	@ResponseBody
	@RequestMapping(value = RequestMappingPattern.DELETE_ALL_EXECUTIONS, method = RequestMethod.DELETE)
	public void deleteAllExecutions(@PathVariable long iterationId) {
		super.deleteAllExecutions(iterationId);
	}

	/**
	 * @see AbstractTestPlanExecutionRunnerController#runFirstRunnableStep(long, long, long, boolean)
	 */
	@RequestMapping(value = "{testPlanItemId}/executions/{executionId}", method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.MOVED_PERMANENTLY)
	public String runFirstRunnableStep(@PathVariable long iterationId, @PathVariable long testPlanItemId,
									   @PathVariable long executionId, @RequestParam(defaultValue = "false") boolean optimized) {

		return super.runFirstRunnableStep(iterationId, testPlanItemId, executionId, optimized);

	}

	/**
	 * @see AbstractTestPlanExecutionRunnerController#runPrologue(long, long, long, boolean, Model)
	 */
	@RequestMapping(value = "{testPlanItemId}/executions/{executionId}/steps/prologue", method = RequestMethod.GET)
	public String runPrologue(@PathVariable long iterationId, @PathVariable long testPlanItemId,
							  @PathVariable long executionId, @RequestParam(defaultValue = "false") boolean optimized, Model model) {

		return super.runPrologue(iterationId, testPlanItemId, executionId, optimized, model);

	}

	/**
	 * @see AbstractTestPlanExecutionRunnerController#getClassicTestSuiteExecutionStepFragment(long, long, long, int, Model)
	 */
	//note : add to the mapping ', headers = ACCEPT_HTML_HEADER' if needed.
	@RequestMapping(value = RequestMappingPattern.INDEXED_STEP, method = RequestMethod.GET, params = { "optimized=false" })
	public String getClassicTestSuiteExecutionStepFragment(@PathVariable long iterationId,
														   @PathVariable long testPlanItemId, @PathVariable long executionId, @PathVariable int stepIndex, Model model) {

		return super.getClassicTestSuiteExecutionStepFragment(iterationId, testPlanItemId, executionId, stepIndex, model);

	}

	/**
	 * @see AbstractTestPlanExecutionRunnerController#getOptimizedTestSuiteExecutionStepFragment(long, long, long, int, Model)
	 */
	//note : add to the mapping ', headers = ACCEPT_HTML_HEADER' if needed.
	@RequestMapping(value = RequestMappingPattern.INDEXED_STEP, method = RequestMethod.GET, params = { "optimized=true" })
	public String getOptimizedTestSuiteExecutionStepFragment(@PathVariable long iterationId,
															 @PathVariable long testPlanItemId, @PathVariable long executionId, @PathVariable int stepIndex, Model model) {

		return super.getOptimizedTestSuiteExecutionStepFragment(iterationId, testPlanItemId, executionId, stepIndex, model);

	}

	/**
	 * @see AbstractTestPlanExecutionRunnerController#changeComment(String, long)
	 */
	@RequestMapping(value = RequestMappingPattern.STEP, method = RequestMethod.POST, params = { "id=execution-comment",
		VALUE })
	@ResponseBody
	public String changeComment(@RequestParam(VALUE) String newComment, @PathVariable long stepId) {
		return super.changeComment(newComment, stepId);
	}

	/**
	 * @see AbstractTestPlanExecutionRunnerController#changeExecutionStatus(String, long)
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
	String completeRessourceUrlPattern(String urlPattern) {
		return ITERATION + urlPattern;
	}

	@Override
	Project getEntityProject(Iteration entity) {
		return entity.getProject();
	}

	@Override
	RunnerState createOptimizedRunnerState(long iterationId, Execution execution, String contextPath, Locale locale) {
		return helper.createOptimizedRunnerStateForIteration(iterationId, execution, contextPath, locale);
	}

	@Override
	boolean hasDeletedTestCaseInTestPlan(long iterationId) {
		return entityFinder.findById(iterationId).getTestPlans().stream().anyMatch(c -> c.getReferencedTestCase() == null);
	}
}
