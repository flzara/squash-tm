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

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.execution.ExecutionStep;
import org.squashtest.tm.service.campaign.CustomIterationModificationService;
import org.squashtest.tm.service.execution.ExecutionProcessingService;
import org.squashtest.tm.web.internal.controller.AcceptHeaders;
import org.squashtest.tm.web.internal.model.json.JsonStepInfo;

@Controller
@RequestMapping("/execute/{executionId}")
public class ExecutionProcessingController {
	private static final String OPTIMIZED = "optimized";
	private static final String ACCEPT_HTML_HEADER = AcceptHeaders.CONTENT_HTTP;
	/**
	 * Step partial URL
	 */
	private static final String STEP_URL = "/step/index/{stepIndex}";

	private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionProcessingController.class);

	@Inject
	private ExecutionRunnerControllerHelper helper;

	@Inject
	private ExecutionProcessingService executionProcService;

	@Inject
	private CustomIterationModificationService itpService;


	private void addCurrentStepUrl(long executionId, Model model) {
		model.addAttribute("currentStepsUrl", "/execute/" + executionId + "/step");
	}

	private String getRedirectToPrologue(long executionId, boolean optimized) {
		return "/execute/" + executionId + "/step/prologue?optimized=" + optimized;
	}

	private String getRedirectToStep(long executionId, int stepIndex, boolean optimized) {
		return "/execute/" + executionId + "/step/index/" + stepIndex + "?optimized=" + optimized;
	}

	// ************************** getters for the main execution fragments **************************************

	@RequestMapping(method = RequestMethod.GET, params = OPTIMIZED)
	public String executeFirstRunnableStep(@PathVariable long executionId,
										   @RequestParam(OPTIMIZED) boolean optimized) {

		if (executionProcService.wasNeverRun(executionId)) {
			return "redirect:" + getRedirectToPrologue(executionId, optimized);
		} else {
			int stepIndex = executionProcService.findRunnableExecutionStep(executionId).getExecutionStepOrder();
			return "redirect:" + getRedirectToStep(executionId, stepIndex, optimized);
		}

	}

	@RequestMapping(value = "/step/prologue", method = RequestMethod.GET, params = OPTIMIZED)
	public String getExecutionPrologue(@PathVariable  long executionId,
			@RequestParam(OPTIMIZED) boolean optimized, Model model) {

		addCurrentStepUrl(executionId, model);
		helper.popuplateExecutionPreview(executionId, optimized, model);

		return ExecutionRunnerViewName.PROLOGUE_STEP;

	}

	@RequestMapping(value = STEP_URL, method = RequestMethod.GET, params = "optimized=false", headers = ACCEPT_HTML_HEADER)
	public String getClassicSingleExecutionStepFragment(@PathVariable long executionId, @PathVariable int stepIndex,
			Model model) {

		helper.populateStepAtIndexModel(executionId, stepIndex, model);
		helper.populateClassicSingleModel(model);

		return ExecutionRunnerViewName.CLASSIC_STEP;

	}

	@RequestMapping(value = STEP_URL, method = RequestMethod.GET, params = "optimized=true", headers = ACCEPT_HTML_HEADER)
	public String getOptimizedSingleExecutionStepFragment(@PathVariable long executionId, @PathVariable int stepIndex,
			Model model) {

		helper.populateStepAtIndexModel(executionId, stepIndex, model);
		helper.populateOptimizedSingleModel(model);

		return ExecutionRunnerViewName.OPTIMIZED_RUNNER_STEP;

	}

	@RequestMapping(value = STEP_URL, method = RequestMethod.GET, params = OPTIMIZED, headers = "Accept=application/json")
	@ResponseBody
	public StepState getStepState(@PathVariable Long executionId, @PathVariable Integer stepIndex) {

		ExecutionStep executionStep = executionProcService.findStepAt(executionId, stepIndex);

		return new StepState(executionStep);

	}

	@RequestMapping(value = "/step/{stepId}", params = "optimized=false")
	public String startResumeExecutionStepInClassicRunner(@PathVariable long executionId, @PathVariable long stepId) {
		Execution execution = executionProcService.findExecution(executionId);
		int stepIndex = execution.getStepIndex(stepId);
		// simple case here : the context is simply the popup. We redirect to the execution processing view controller.
		return "redirect:" + getRedirectToStep(executionId, stepIndex, false);

	}




	// ************************* other stuffs ********************************************

	@RequestMapping(value = "/step/index/{stepIndex}/general", method = RequestMethod.GET)
	@ResponseBody
	public JsonStepInfo getBasicInfos(@PathVariable long executionId, @PathVariable int stepIndex) {

		ExecutionStep executionStep = executionProcService.findStepAt(executionId, stepIndex);

		return new JsonStepInfo(
				executionStep.getLastExecutedOn(),
				executionStep.getLastExecutedBy()
				);

	}

	@RequestMapping(value = "/step/{stepId}", method = RequestMethod.POST, params = { "id=execution-comment", VALUE })
	@ResponseBody
	public String updateComment(@RequestParam(VALUE) String newComment, @PathVariable long stepId) {
		executionProcService.setExecutionStepComment(stepId, newComment);
		LOGGER.trace("ExecutionStep {}: updated comment to {}", stepId,  newComment);
		return newComment;
	}

	@RequestMapping(value = "/step/{stepId}", method = RequestMethod.POST, params = "executionStatus")
	@ResponseBody
	public void updateExecutionStatus(@RequestParam String executionStatus, @PathVariable long stepId) {
		ExecutionStatus status = ExecutionStatus.valueOf(executionStatus);
		executionProcService.changeExecutionStepStatus(stepId, status);
	}

	@RequestMapping(value = "/update-from-tc", method = RequestMethod.POST)
	@ResponseBody
	public Long updateExecutionFromTc(@PathVariable long executionId) {

		Execution exec = itpService.updateExecutionFromTc(executionId);

		return exec.getId();
	}

}
