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
package org.squashtest.tm.web.internal.controller.testcase.steps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.util.HtmlUtils;
import org.squashtest.tm.domain.attachment.Attachment;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.execution.ExecutionStep;
import org.squashtest.tm.domain.testcase.TestStep;
import org.squashtest.tm.service.customfield.CustomFieldValueFinderService;
import org.squashtest.tm.service.execution.ExecutionFinder;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.testcase.TestStepModificationService;
import org.squashtest.tm.web.internal.controller.generic.ServiceAwareAttachmentTableModelHelper;
import org.squashtest.tm.web.internal.controller.milestone.MilestoneFeatureConfiguration;
import org.squashtest.tm.web.internal.controller.milestone.MilestoneUIConfigurationService;
import org.squashtest.tm.web.internal.controller.testcase.requirement.RequirementVerifierView;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.service.internal.dto.CustomFieldJsonConverter;
import org.squashtest.tm.service.internal.dto.CustomFieldValueModel;

@Controller
@RequestMapping("/test-steps/{testStepId}")
public class TestStepController {
	private static final Logger LOGGER = LoggerFactory.getLogger(TestStepController.class);
	private static final String OPTIMIZED = "optimized";

	@Inject
	private TestStepModificationService testStepService;

	@Inject
	private CustomFieldValueFinderService cufValueFinder;

	@Inject
	private PermissionEvaluationService permissionEvaluationService;


	@Inject
	private ServiceAwareAttachmentTableModelHelper attachmentHelper;

	@Inject
	private CustomFieldJsonConverter cufJsonConverter;

	@Inject
	private MilestoneUIConfigurationService milestoneConfService;

	@Inject
	private InternationalizationHelper internationalizationHelper;

	@Inject
	private ExecutionFinder executionService;

	/**
	 * Shows the step modification page.
	 *
	 * @param testStepId
	 *            the id of the step to show
	 * @param model
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String showStepInfos(@PathVariable long testStepId, Model model) {

		LOGGER.info("Show Test Step initiated");
		LOGGER.debug("Find and show TestStep #{}", testStepId);
		TestStep testStep = testStepService.findById(testStepId);
		TestStepView testStepView = new TestStepViewBuilder().buildTestStepView(testStep);
		generateTestStepInfo(model, testStepView, testStep);

		return "edit-test-step.html";
	}

	private void generateTestStepInfo(Model model, AbstractTestStepView<?> testStepView,
			TestStep testStep) {
		Locale locale = LocaleContextHolder.getLocale();
		model.addAttribute("testStepView", testStepView);
		if(testStepView.actionStep != null){
			model.addAttribute("testStepViewAction", HtmlUtils.htmlEscape(testStepView.actionStep.getAction()));
			model.addAttribute("testStepViewExpectedResult", HtmlUtils.htmlEscape(testStepView.actionStep.getExpectedResult()));
		}
		model.addAttribute("workspace", "test-case");
		model.addAttribute("testCase", testStepView.testCase);
		model.addAttribute("testStep", testStep);

		// ------------------------------------ MILLESTONE FEATURE
		MilestoneFeatureConfiguration milestoneConf = milestoneConfService.configure(testStep.getTestCase());
		model.addAttribute("milestoneConf", milestoneConf);

		// ------------------------------------RIGHTS PART
		// waiting for [Task 1843]
		boolean writable = permissionEvaluationService.hasRoleOrPermissionOnObject("ROLE_ADMIN", "WRITE", testStep);
		writable = writable && milestoneConf.isEditable();
		model.addAttribute("writable", writable); // right to modify steps

		boolean attachable = permissionEvaluationService.hasRoleOrPermissionOnObject("ROLE_ADMIN", "ATTACH", testStep);
		attachable = attachable && milestoneConf.isEditable();
		model.addAttribute("attachable", attachable); // right to modify steps

		boolean linkable = permissionEvaluationService.hasRoleOrPermissionOnObject("ROLE_ADMIN", "LINK", testStep);
		model.addAttribute("linkable", linkable); // right to bind steps to requirement

		// end waiting for [Task 1843]
		// ------------------------------------ATTACHMENT AND CUF PART
		boolean hasCUF = false;
		List<CustomFieldValue> values = Collections.emptyList();
		// Properties for ActionTestStep only
		if (testStepView.getActionStep() != null) {
			// attachments
			model.addAttribute("attachableEntity", testStepView.getActionStep());
			Set<Attachment> attachments = attachmentHelper.findAttachments(testStepView.getActionStep());
			model.addAttribute("attachmentSet", attachments);

			// cufs
			values = cufValueFinder.findAllCustomFieldValues(testStepView.getActionStep().getBoundEntityId(),
					testStepView.getActionStep().getBoundEntityType());
			hasCUF = cufValueFinder.hasCustomFields(testStepView.getActionStep());
			// verified requirements
			RequirementVerifierView requirementVerifierView = new RequirementVerifierView(testStepView.getActionStep(),internationalizationHelper,locale);
			model.addAttribute("requirementVerifier", requirementVerifierView);

		} else {
			values = Collections.emptyList();
		}


		List<CustomFieldValueModel> cufModels = new ArrayList<>(values.size());
		for (CustomFieldValue value : values){
			cufModels.add(cufJsonConverter.toJson(value));
		}
		model.addAttribute("cufDefinitions", cufModels);

		model.addAttribute("hasCUF", hasCUF);



	}

	@RequestMapping(value = "/from-exec", method = RequestMethod.GET, params = OPTIMIZED)
	public String showStepInfosFromExec(@PathVariable("testStepId") int execStepId,
 Model model,
			@RequestParam(OPTIMIZED) boolean optimized) {


		ExecutionStep execStep = executionService.findExecutionStepById(execStepId);
		TestStep testStep = execStep.getReferencedTestStep();
		TestStepViewFromExec testStepView = new TestStepViewFromExecBuilder().buildTestStepViewFromExec(execStep);

		if (testStep != null) {
			generateTestStepInfo(model, testStepView, testStep);
		} else {
			model.addAttribute("testStepView", testStepView);
			if(testStepView.actionStep != null){
				model.addAttribute("testStepViewAction", HtmlUtils.htmlEscape(testStepView.actionStep.getAction()));
				model.addAttribute("testStepViewExpectedResult", HtmlUtils.htmlEscape(testStepView.actionStep.getExpectedResult()));
			}
			model.addAttribute("workspace", "test-case");
			model.addAttribute("writable", true);
		}
		model.addAttribute("fromExec", execStep.getExecution().getId());
		model.addAttribute("isIEO", optimized);

		return "edit-test-step.html";
	}

	/**
	 * update the TestStep infos
	 *
	 * @param testStepId
	 * @param testStepModel
	 */
	@RequestMapping(method = RequestMethod.POST, headers = { "Content-Type=application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.NO_CONTENT)
	public void updateStep(@PathVariable Long testStepId, @RequestBody TestStepUpdateFormModel testStepModel) {
		testStepService.updateTestStep(testStepId, testStepModel.getAction(), testStepModel.getExpectedResult(), testStepModel.getCufs());
	}
}
