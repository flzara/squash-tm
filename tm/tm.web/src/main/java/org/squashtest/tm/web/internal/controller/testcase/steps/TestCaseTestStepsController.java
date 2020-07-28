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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.Paging;
import org.squashtest.tm.core.foundation.collection.PagingBackedPagedCollectionHolder;
import org.squashtest.tm.domain.bdd.Keyword;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.customfield.RawValue;
import org.squashtest.tm.domain.customfield.RenderingLocation;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.KeywordTestCase;
import org.squashtest.tm.domain.testcase.KeywordTestStep;
import org.squashtest.tm.domain.testcase.ParameterAssignationMode;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestStep;
import org.squashtest.tm.service.actionword.ActionWordService;
import org.squashtest.tm.service.customfield.CustomFieldHelper;
import org.squashtest.tm.service.customfield.CustomFieldHelperService;
import org.squashtest.tm.service.internal.dto.CustomFieldJsonConverter;
import org.squashtest.tm.service.internal.dto.CustomFieldModel;
import org.squashtest.tm.service.internal.repository.KeywordTestStepDao;
import org.squashtest.tm.service.testcase.CallStepManagerService;
import org.squashtest.tm.service.testcase.TestCaseModificationService;
import org.squashtest.tm.service.testcase.bdd.KeywordTestCaseFinder;
import org.squashtest.tm.service.testcase.bdd.KeywordTestCaseService;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.controller.milestone.MilestoneFeatureConfiguration;
import org.squashtest.tm.web.internal.controller.milestone.MilestoneUIConfigurationService;
import org.squashtest.tm.web.internal.controller.testcase.TestCaseModificationController;
import org.squashtest.tm.web.internal.controller.testcase.steps.ActionStepFormModel.ActionStepFormModelValidator;
import org.squashtest.tm.web.internal.controller.testcase.steps.KeywordTestStepModel.KeywordTestStepModelValidator;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.datatable.DataTablePaging;
import org.squashtest.tm.web.internal.util.HTMLCleanupUtils;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.util.Objects.nonNull;
import static org.squashtest.tm.web.internal.helper.JEditablePostParams.VALUE;

// XSS OK
@Controller
@RequestMapping("/test-cases/{testCaseId}/steps")
public class TestCaseTestStepsController {

	/**
	 *
	 */
	private static final String TEST_CASE = "testCase";

	private static final String TEST_CASE_ = "test case ";

	@Inject
	private CustomFieldHelperService cufHelperService;

	@Inject
	private KeywordTestCaseService keywordTestCaseService;

	@Inject
	private KeywordTestCaseFinder keywordTestCaseFinder;

	@Inject
	private KeywordTestStepDao keywordTestStepDao;

	@Autowired(required = false)
	private ActionWordService actionWordService;

	@Inject
	private CustomFieldJsonConverter converter;

	@Inject
	private InternationalizationHelper internationalizationHelper;

	@Inject
	private CallStepManagerService callStepManager;

	@Inject
	private TestCaseModificationService testCaseModificationService;

	@Inject
	private MilestoneUIConfigurationService milestoneConfService;

	private static final String COPIED_STEP_ID_PARAM = "copiedStepId[]";
	private static final Logger LOGGER = LoggerFactory.getLogger(TestCaseModificationController.class);

	@RequestMapping(value = "/panel")
	public String getTestStepsPanel(@PathVariable("testCaseId") long testCaseId, Model model) {

		// the main entities
		TestCase testCase = testCaseModificationService.findById(testCaseId);
		// FIXME loads all steps, should perform a paged query !
		List<TestStep> steps = testCase.getSteps().subList(0, Math.min(50, testCase.getSteps().size()));

		// the custom fields definitions
		CustomFieldHelper<ActionTestStep> helper = cufHelperService.newStepsHelper(steps, testCase.getProject())
			.setRenderingLocations(RenderingLocation.STEP_TABLE).restrictToCommonFields();

		List<CustomFieldModel> cufDefinitions = convertToJsonCustomField(helper.getCustomFieldConfiguration());
		List<CustomFieldValue> cufValues = helper.getCustomFieldValues();

		// process the data
		TestStepsTableModelBuilder builder = new TestStepsTableModelBuilder();
		builder.usingCustomFields(cufValues, cufDefinitions.size());
		Collection<Object> stepsData = builder.buildRawModel(steps, 1);


		// the milestone feature
		MilestoneFeatureConfiguration milestoneConf = milestoneConfService.configure(testCase);

		// populate the model
		model.addAttribute(TEST_CASE, testCase);
		model.addAttribute("stepsData", stepsData);
		model.addAttribute("cufDefinitions", cufDefinitions);
		model.addAttribute("milestoneConf", milestoneConf);


		// return
		return "test-cases-tabs/test-steps-tab.html";

	}

	@RequestMapping(params = RequestParams.S_ECHO_PARAM)
	@ResponseBody
	public DataTableModel getStepsTableModel(@PathVariable long testCaseId, DataTableDrawParameters params) {

		LOGGER.trace("TestCaseModificationController: getStepsTableModel called ");

		Paging filter = new DataTablePaging(params);

		PagedCollectionHolder<List<TestStep>> holder = testCaseModificationService.findStepsByTestCaseIdFiltered(
			testCaseId, filter);
		Project project = testCaseModificationService.findById(testCaseId).getProject();
		// cufs
		CustomFieldHelper<ActionTestStep> helper = cufHelperService
			.newStepsHelper(holder.getPagedItems(), project)
			.setRenderingLocations(RenderingLocation.STEP_TABLE)
			.restrictToCommonFields();

		List<CustomFieldValue> cufValues = helper.getCustomFieldValues();

		// generate the model
		TestStepsTableModelBuilder builder = new TestStepsTableModelBuilder();
		builder.usingCustomFields(cufValues);
		return builder.buildDataModel(holder, params.getsEcho());

	}

	@RequestMapping(value = "/keyword-test-step-table", params = RequestParams.S_ECHO_PARAM)
	@ResponseBody
	public DataTableModel getKeywordTestStepTableModel (@PathVariable long testCaseId, DataTableDrawParameters params) {
		TestCase testCase = testCaseModificationService.findById(testCaseId);
		List<TestStep> steps = testCase.getSteps();

		Paging filter = new DataTablePaging(params);

		PagedCollectionHolder<List<TestStep>> holder = new PagingBackedPagedCollectionHolder<>(filter, steps.size(), steps);

		// generate the model
		KeywordTestStepTableModelBuilder builder = new KeywordTestStepTableModelBuilder();
		return builder.buildDataModel(holder, params.getsEcho());

	}

	@RequestMapping(value = "/add", method = RequestMethod.POST, consumes = "application/json")
	@ResponseBody
	public Long addActionTestStep(@RequestBody ActionStepFormModel stepModel,
								  @PathVariable long testCaseId) throws BindException {

		BindingResult validation = new BeanPropertyBindingResult(stepModel, "add-test-step");
		ActionStepFormModelValidator validator = new ActionStepFormModelValidator(internationalizationHelper);
		validator.validate(stepModel, validation);

		if (validation.hasErrors()) {
			throw new BindException(validation);
		}

		ActionTestStep step = stepModel.getActionTestStep();

		Map<Long, RawValue> customFieldValues = stepModel.getCufs();
		int index = stepModel.getIndex();

		ActionTestStep addActionTestStep;

		if (index != 0) {
			addActionTestStep = testCaseModificationService.addActionTestStep(testCaseId, step, customFieldValues, index);
		} else {
			addActionTestStep = testCaseModificationService.addActionTestStep(testCaseId, step, customFieldValues);
		}
		LOGGER.trace(TEST_CASE_ + testCaseId + ": step added, action : " + step.getAction() + ", expected result : "
			+ step.getExpectedResult());
		return addActionTestStep.getId();
	}

	@RequestMapping(value = "/keyword-test-step-panel")
	public String getKeywordTestStepPanel(@PathVariable("testCaseId") long testCaseId, Model model) {
		KeywordTestCase keywordTestCase = keywordTestCaseFinder.findById(testCaseId);
		List<TestStep> steps = keywordTestCase.getSteps();
		model.addAttribute(TEST_CASE, keywordTestCase);
		model.addAttribute("projectId", keywordTestCase.getProject().getId());

		//create keyword test step table model
		KeywordTestStepTableModelBuilder builder = new KeywordTestStepTableModelBuilder();
		Collection<Object> stepData = builder.buildRawModel(steps, 1);
		model.addAttribute("isAutocompleteActive", nonNull(actionWordService));
		model.addAttribute("stepData", stepData);
		model.addAttribute("keywordMap", createKeywordMap(keywordTestCase.getProject()));
		model.addAttribute("generated_script", keywordTestCaseService.writeScriptFromTestCase(keywordTestCase, true));
		return "test-cases-tabs/keyword-test-steps-tab.html";
	}

	@PostMapping(value = "/add-keyword-test-step", consumes = "application/json")
	@ResponseBody
	public Long addKeywordTestStep(@RequestBody KeywordTestStepModel keywordTestStepDto, @PathVariable long testCaseId) throws BindException {
		validateDto(keywordTestStepDto);

		String keyword = keywordTestStepDto.getKeyword();
		String actionWord = keywordTestStepDto.getActionWord();

		KeywordTestStep step = testCaseModificationService.addKeywordTestStep(testCaseId, keyword, actionWord);
		return step.getId();
	}

	private void validateDto(@RequestBody KeywordTestStepModel keywordTestStepDto) throws BindException {
		BindingResult validation = new BeanPropertyBindingResult(keywordTestStepDto, "add-keyword-test-step");
		KeywordTestStepModelValidator validator = new KeywordTestStepModelValidator(internationalizationHelper);
		validator.validate(keywordTestStepDto, validation);

		if (validation.hasErrors()) {
			throw new BindException(validation);
		}
	}

	@RequestMapping(value = "/paste", method = RequestMethod.POST, params = {COPIED_STEP_ID_PARAM})
	@ResponseBody
	public boolean pasteStep(@RequestParam(COPIED_STEP_ID_PARAM) List<Long> copiedStepIds,
							 @RequestParam(value = "idPosition") long idPosition, @PathVariable long testCaseId) {

		callStepManager.checkForCyclicStepCallBeforePaste(testCaseId, copiedStepIds);
		return testCaseModificationService.pasteCopiedTestSteps(testCaseId, idPosition, copiedStepIds);

	}


	@RequestMapping(value = "/paste-last-index", method = RequestMethod.POST, params = {COPIED_STEP_ID_PARAM})
	@ResponseBody
	public boolean pasteStepLastIndex(@RequestParam(COPIED_STEP_ID_PARAM) List<Long> copiedStepIds,
									  @PathVariable long testCaseId) {

		callStepManager.checkForCyclicStepCallBeforePaste(testCaseId, copiedStepIds);
		return testCaseModificationService.pasteCopiedTestStepToLastIndex(testCaseId, copiedStepIds);
	}


	@RequestMapping(value = "/{stepId}", method = RequestMethod.POST, params = "newIndex")
	@ResponseBody
	public void changeStepIndex(@PathVariable long stepId, @RequestParam int newIndex, @PathVariable long testCaseId) {

		testCaseModificationService.changeTestStepPosition(testCaseId, stepId, newIndex);
		LOGGER.trace(TEST_CASE_ + testCaseId + ": step " + stepId + " moved to " + newIndex);

	}

	@RequestMapping(value = "/move", method = RequestMethod.POST, params = {"newIndex", "itemIds[]"})
	@ResponseBody
	public void changeStepsIndex(@RequestParam("itemIds[]") List<Long> itemIds, @RequestParam("newIndex") int newIndex,
								 @PathVariable long testCaseId) {

		testCaseModificationService.changeTestStepsPosition(testCaseId, newIndex, itemIds);

	}

	@RequestMapping(value = "/{stepIds}", method = RequestMethod.DELETE)
	@ResponseBody
	public int deleteSteps(@PathVariable("stepIds") List<Long> stepIds, @PathVariable long testCaseId) {
		List<TestStep> teststeps = testCaseModificationService.removeListOfSteps(testCaseId, stepIds);
		return teststeps.size();
	}

	@RequestMapping(value = "/{stepId}/action", method = RequestMethod.POST, params = {"id", VALUE}, produces = "text/plain;charset=UTF-8")
	@ResponseBody
	public String changeStepAction(@PathVariable long stepId, @RequestParam(VALUE) String newAction) {
		testCaseModificationService.updateTestStepAction(stepId, newAction);
		LOGGER.trace("TestCaseModificationController : updated action for step {}", stepId);
		return HTMLCleanupUtils.cleanHtml(newAction);
	}

	@RequestMapping(value = "/{stepId}/result", method = RequestMethod.POST, params = {"id", VALUE}, produces = "text/plain;charset=UTF-8")
	@ResponseBody
	public String changeStepDescription(@PathVariable long stepId, @RequestParam(VALUE) String newResult) {
		testCaseModificationService.updateTestStepExpectedResult(stepId, newResult);
		LOGGER.trace("TestCaseModificationController : updated action for step {}", stepId);
		return HTMLCleanupUtils.cleanHtml(newResult);
	}


	@RequestMapping(value = "{stepId}/parameter-assignation-mode", method = RequestMethod.POST, params = {"mode", "datasetId"})
	@ResponseBody
	public void changeParameterAssignationMode(@PathVariable("stepId") Long stepId,
											   @RequestParam(value = "mode") ParameterAssignationMode mode,
											   @RequestParam(value = "datasetId", required = false) Long datasetId) {

		callStepManager.setParameterAssignationMode(stepId, mode, datasetId);

	}

	@RequestMapping(value = "/{stepId}/keyword", method = RequestMethod.POST, params = {"id", VALUE})
	@ResponseBody
	public String changeStepKeyword(@PathVariable long stepId, @RequestParam(VALUE) String keyword) {
		Keyword updatedKeyword = Keyword.valueOf(keyword);
		testCaseModificationService.updateKeywordTestStep(stepId, updatedKeyword);
		LOGGER.trace("TestCaseModificationController : updated keyword for step {}", stepId);
		Locale locale = keywordTestStepDao.findById(stepId).getTestCase().getProject().getBddScriptLanguage().getLocale();
		return internationalizationHelper.internationalize(updatedKeyword.i18nKeywordNameKey(), locale);
	}

	@RequestMapping(value = "/{stepId}/action-word", method = RequestMethod.POST, params = {VALUE})
	@ResponseBody
	public String changeStepActionWord(@PathVariable long stepId, @RequestParam(VALUE) String actionWord) {
		testCaseModificationService.updateKeywordTestStep(stepId, actionWord);
		LOGGER.trace("TestCaseModificationController : updated action word for step {}", stepId);
		return actionWord;
	}

	@RequestMapping(value = "/{stepId}/action-word-html", method = RequestMethod.GET)
	@ResponseBody
	public String getActionWordHtml(@PathVariable long stepId) {
		KeywordTestStep keywordTestStep = keywordTestStepDao.findById(stepId);
		return new KeywordTestStepTableModelBuilder().createActionWordWithParamValues(keywordTestStep);
	}

	@RequestMapping(value = "/{stepId}/action-word-unstyled", method = RequestMethod.GET)
	@ResponseBody
	public String getActionWordUnstyled(@PathVariable long stepId) {
		KeywordTestStep keywordTestStep = keywordTestStepDao.findById(stepId);
		return keywordTestStep.writeTestStepActionWordScript(true);
	}

	private List<CustomFieldModel> convertToJsonCustomField(Collection<CustomField> customFields) {
		List<CustomFieldModel> models = new ArrayList<>(customFields.size());
		for (CustomField field : customFields) {
			models.add(converter.toJson(field));
		}
		return models;
	}

	private EnumMap<Keyword, String> createKeywordMap(Project project) {
		Locale locale = project.getBddScriptLanguage().getLocale();
		EnumMap<Keyword, String> keywordMap = new EnumMap<>(Keyword.class);
		for (Keyword keyword : Keyword.values()) {
			keywordMap.put(keyword, internationalizationHelper.internationalize(keyword.i18nKeywordNameKey(), locale));
		}
		return keywordMap;
	}
}
