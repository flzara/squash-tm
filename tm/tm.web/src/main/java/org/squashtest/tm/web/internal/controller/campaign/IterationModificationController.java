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

import static org.squashtest.tm.web.internal.helper.JEditablePostParams.VALUE;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.HtmlUtils;
import org.squashtest.tm.core.foundation.lang.DateUtils;
import org.squashtest.tm.domain.Workspace;
import org.squashtest.tm.domain.audit.AuditableMixin;
import org.squashtest.tm.domain.campaign.*;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.service.campaign.*;
import org.squashtest.tm.service.customfield.CustomFieldValueFinderService;
import org.squashtest.tm.service.customreport.CustomReportDashboardService;
import org.squashtest.tm.service.deletion.OperationReport;
import org.squashtest.tm.service.statistics.iteration.IterationStatisticsBundle;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.controller.generic.ServiceAwareAttachmentTableModelHelper;
import org.squashtest.tm.web.internal.controller.milestone.MilestoneFeatureConfiguration;
import org.squashtest.tm.web.internal.controller.milestone.MilestoneUIConfigurationService;
import org.squashtest.tm.web.internal.controller.testcase.TestCaseImportanceJeditableComboDataBuilder;
import org.squashtest.tm.web.internal.controller.testcase.TestCaseModeJeditableComboDataBuilder;
import org.squashtest.tm.web.internal.controller.testcase.executions.ExecutionStatusJeditableComboDataBuilder;
import org.squashtest.tm.web.internal.helper.LevelLabelFormatter;
import org.squashtest.tm.web.internal.http.ContentTypes;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.jquery.RenameModel;
import org.squashtest.tm.web.internal.model.jquery.TestSuiteModel;
import org.squashtest.tm.web.internal.model.json.JsonGeneralInfo;

@Controller
@RequestMapping("/iterations/{iterationId}")
public class IterationModificationController {

	private static final String NAME = "name";

	private static final Logger LOGGER = LoggerFactory.getLogger(IterationModificationController.class);

	private static final String ITERATION_KEY = "iteration";
	private static final String ITERATION_ID_KEY = "iterationId";
	private static final String PLANNING_URL = "/planning";

	@Inject
	private IterationModificationService iterationModService;

	@Inject
	private IterationTestPlanManagerService iterationTestPlanManagerService;

	@Inject
	private CustomFieldValueFinderService cufValueService;

	@Inject
	private IterationTestPlanFinder testPlanFinder;

	@Inject
	private ServiceAwareAttachmentTableModelHelper attachmentHelper;

	@Inject
	private Provider<TestCaseImportanceJeditableComboDataBuilder> importanceComboBuilderProvider;

	@Inject
	private InternationalizationHelper messageSource;

	@Inject
	private Provider<TestCaseModeJeditableComboDataBuilder> modeComboBuilderProvider;

	@Inject
	private Provider<ExecutionStatusJeditableComboDataBuilder> executionStatusComboBuilderProvider;

	@Inject
	private Provider<LevelLabelFormatter> levelLabelFormatterProvider;

	@Inject
	private Provider<IterationStatusJeditableComboDataBuilder> statusComboBuilderProvider;

	@Inject
	private MilestoneUIConfigurationService milestoneConfService;

	@Inject
	private CustomReportDashboardService customReportDashboardService;

	@RequestMapping(method = RequestMethod.GET)
	public String showIteration(Model model, @PathVariable long iterationId) {

		populateIterationModel(model, iterationId);


		return "fragment/iterations/iteration";
	}

	// will return the iteration in a full page
	@RequestMapping(value = "/info", method = RequestMethod.GET)
	public String showIterationInfo(Model model, @PathVariable long iterationId) {

		populateIterationModel(model, iterationId);
		return "page/campaign-workspace/show-iteration";
	}

	private void populateIterationModel(Model model, long iterationId) {

		Iteration iteration = iterationModService.findById(iterationId);
		boolean hasCUF = cufValueService.hasCustomFields(iteration);
		DataTableModel attachmentsModel = attachmentHelper.findPagedAttachments(iteration);
		Map<String, String> assignableUsers = getAssignableUsers(iterationId);
		Map<String, String> weights = getWeights();

		MilestoneFeatureConfiguration milestoneConf = milestoneConfService.configure(iteration);

		model.addAttribute(ITERATION_KEY, iteration);
		model.addAttribute("hasCUF", hasCUF);
		model.addAttribute("attachmentsModel", attachmentsModel);
		model.addAttribute("assignableUsers", assignableUsers);
		model.addAttribute("weights", weights);
		model.addAttribute("modes", getModes());
		model.addAttribute("statuses", getStatuses(iteration.getProject().getId()));
		model.addAttribute("milestoneConf", milestoneConf);
		model.addAttribute("iterationStatusComboJson", buildStatusComboData());
		model.addAttribute("iterationStatusLabel", formatStatus(iteration.getStatus()));

		boolean shouldShowDashboard = customReportDashboardService.shouldShowFavoriteDashboardInWorkspace(Workspace.CAMPAIGN);
		boolean canShowDashboard = customReportDashboardService.canShowDashboardInWorkspace(Workspace.CAMPAIGN);

		model.addAttribute("shouldShowDashboard", shouldShowDashboard);
		model.addAttribute("canShowDashboard", canShowDashboard);


		populateOptionalExecutionStatuses(iteration, model);

	}

	private void populateOptionalExecutionStatuses(Iteration iteration, Model model) {
		model.addAttribute("allowsSettled",
			iteration.getCampaign().getProject().getCampaignLibrary().allowsStatus(ExecutionStatus.SETTLED));
		model.addAttribute("allowsUntestable",
			iteration.getCampaign().getProject().getCampaignLibrary().allowsStatus(ExecutionStatus.UNTESTABLE));
	}

	/**
	 * Will fetch the active {@link ExecutionStatus} for the project matching the given id
	 *
	 * @param projectId : the id of the concerned {@link Project}
	 * @return a map representing the active statuses for the given project with :
	 * <ul><li>key: the status name</li><li>value: the status internationalized label</li></ul>
	 */
	private Map<String, String> getStatuses(long projectId) {
		Locale locale = LocaleContextHolder.getLocale();
		return executionStatusComboBuilderProvider.get().useContext(projectId).useLocale(locale).buildMap();
	}

	private Map<String, String> getModes() {
		Locale locale = LocaleContextHolder.getLocale();
		return modeComboBuilderProvider.get().useLocale(locale).buildMap();
	}

	private String formatStatus(IterationStatus status) {
		Locale locale = LocaleContextHolder.getLocale();
		return levelLabelFormatterProvider.get().useLocale(locale).formatLabel(status);
	}

	@RequestMapping(value = "/status-combo-data", method = RequestMethod.GET)
	@ResponseBody
	private String buildStatusComboData() {
		Locale locale = LocaleContextHolder.getLocale();
		return statusComboBuilderProvider.get().useLocale(locale).buildMarshalled();
	}


	private Map<String, String> getWeights() {
		Locale locale = LocaleContextHolder.getLocale();
		return importanceComboBuilderProvider.get().useLocale(locale).buildMap();
	}

	private Map<String, String> getAssignableUsers(@PathVariable long iterationId) {

		Locale locale = LocaleContextHolder.getLocale();

		List<User> usersList = iterationTestPlanManagerService.findAssignableUserForTestPlan(iterationId);
		Collections.sort(usersList, new UserLoginComparator());

		String unassignedLabel = messageSource.internationalize("label.Unassigned", locale);

		Map<String, String> jsonUsers = new LinkedHashMap<>(usersList.size());

		jsonUsers.put(User.NO_USER_ID.toString(), unassignedLabel);
		for (User user : usersList) {
			jsonUsers.put(user.getId().toString(), user.getLogin());
		}

		return jsonUsers;
	}

	//URL should have been /statistics, but that was already used by another method in this controller
	@ResponseBody
	@RequestMapping(value = "/dashboard-statistics", method = RequestMethod.GET, produces = ContentTypes.APPLICATION_JSON)
	public IterationStatisticsBundle getStatisticsAsJson(@PathVariable("iterationId") long iterationId) {

		return iterationModService.gatherIterationStatisticsBundle(iterationId);
	}

	@RequestMapping(value = "/dashboard", method = RequestMethod.GET, produces = ContentTypes.TEXT_HTML)
	public ModelAndView getDashboard(Model model, @PathVariable("iterationId") long iterationId) {

		Iteration iteration = iterationModService.findById(iterationId);
		IterationStatisticsBundle bundle = iterationModService.gatherIterationStatisticsBundle(iterationId);

		ModelAndView mav = new ModelAndView("fragment/iterations/iteration-dashboard");
		mav.addObject("iteration", iteration);
		mav.addObject("dashboardModel", bundle);

		populateOptionalExecutionStatuses(iteration, model);

		return mav;
	}


	@RequestMapping(method = RequestMethod.POST, params = {"id=iteration-description", VALUE})
	@ResponseBody
	public String updateDescription(@RequestParam(VALUE) String newDescription, @PathVariable long iterationId) {

		iterationModService.changeDescription(iterationId, newDescription);
		LOGGER.trace("Iteration " + iterationId + ": updated description to " + newDescription);
		return newDescription;

	}

	@RequestMapping(method = RequestMethod.POST, params = {"id=iteration-reference", VALUE})
	@ResponseBody
	public String updateReference(@RequestParam(VALUE) String newReference, @PathVariable long iterationId) {

		iterationModService.changeReference(iterationId, newReference);
		LOGGER.trace("Iteration " + iterationId + ": updated reference to " + newReference);
		return HtmlUtils.htmlEscape(newReference);

	}

	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, params = { "id=iteration-status", VALUE })
	public String changeStatus(@PathVariable long iterationId, @RequestParam(VALUE) IterationStatus status) {
		iterationModService.changeStatus(iterationId, status);
		return formatStatus(status);
	}

	@RequestMapping(method = RequestMethod.POST, params = {"newName"})
	@ResponseBody
	public Object rename(@RequestParam("newName") String newName,
						 @PathVariable long iterationId) {

		LOGGER.info("IterationModificationController : renaming {} as {}", iterationId, newName);
		iterationModService.rename(iterationId, newName);
		return new RenameModel(newName);

	}

	@ResponseBody
	@RequestMapping(value = "/duplicateTestSuite/{testSuiteId}", method = RequestMethod.POST)
	public Long duplicateTestSuite(@PathVariable(ITERATION_ID_KEY) Long iterationId,
								   @PathVariable("testSuiteId") Long testSuiteId) {
		TestSuite duplicate = iterationModService.copyPasteTestSuiteToIteration(testSuiteId, iterationId);
		return duplicate.getId();
	}


	@RequestMapping(value = "/general", method = RequestMethod.GET, produces = ContentTypes.APPLICATION_JSON)
	@ResponseBody
	public JsonGeneralInfo refreshGeneralInfos(@PathVariable long iterationId) {
		Iteration iteration = iterationModService.findById(iterationId);
		return new JsonGeneralInfo((AuditableMixin) iteration);

	}

	/* *************************************** planning ********************************* */

	/**
	 * returns null if the string is empty, or a date otherwise. No check regarding the actual content of strDate.
	 */
	private Date strToDate(String strDate) {
		return DateUtils.millisecondsToDate(strDate);
	}

	private String dateToStr(Date date) {
		return DateUtils.dateToMillisecondsAsString(date);
	}

	@ResponseBody
	@RequestMapping(value = PLANNING_URL, params = {"scheduledStart"})
	public String setScheduledStart(@PathVariable long iterationId,
									@RequestParam(value = "scheduledStart") String strDate) {

		Date newScheduledStart = strToDate(strDate);
		String toReturn = dateToStr(newScheduledStart);

		LOGGER.info("IterationModificationController : setting scheduled start date for iteration {}, new date : {}", iterationId, newScheduledStart);

		iterationModService.changeScheduledStartDate(iterationId, newScheduledStart);

		return toReturn;

	}

	@RequestMapping(value = PLANNING_URL, params = {"scheduledEnd"})
	@ResponseBody
	public String setScheduledEnd(@PathVariable long iterationId,
								  @RequestParam(value = "scheduledEnd") String strDate) {

		Date newScheduledEnd = strToDate(strDate);
		String toReturn = dateToStr(newScheduledEnd);

		LOGGER.info("IterationModificationController : setting scheduled end date for iteration {}, new date : {}", iterationId, newScheduledEnd);

		iterationModService.changeScheduledEndDate(iterationId, newScheduledEnd);

		return toReturn;

	}

	/**
	 * the next functions may receive null arguments : empty string
	 **/

	@RequestMapping(value = PLANNING_URL, params = {"actualStart"})
	@ResponseBody
	public String setActualStart(@PathVariable long iterationId,
								 @RequestParam(value = "actualStart") String strDate) {

		Date newActualStart = strToDate(strDate);
		String toReturn = dateToStr(newActualStart);

		LOGGER.info("IterationModificationController : setting actual start date for iteration {}, new date : {}", iterationId, newActualStart);

		iterationModService.changeActualStartDate(iterationId, newActualStart);

		return toReturn;

	}

	@RequestMapping(value = PLANNING_URL, params = {"actualEnd"})
	@ResponseBody
	public String setActualEnd(@PathVariable long iterationId,
							   @RequestParam(value = "actualEnd") String strDate) {

		Date newActualEnd = strToDate(strDate);
		String toReturn = dateToStr(newActualEnd);

		LOGGER.info("IterationModificationController : setting actual end date for iteration {}, new date : {}", iterationId, newActualEnd);

		iterationModService.changeActualEndDate(iterationId, newActualEnd);

		return toReturn;

	}

	@RequestMapping(value = PLANNING_URL, params = {"setActualStartAuto"})
	@ResponseBody
	public String setActualStartAuto(@PathVariable long iterationId,
									 @RequestParam(value = "setActualStartAuto") boolean auto) {

		LOGGER.info("IterationModificationController : autosetting actual start date for iteration {}, new value {}", iterationId, auto);

		iterationModService.changeActualStartAuto(iterationId, auto);
		Iteration iteration = iterationModService.findById(iterationId);

		return dateToStr(iteration.getActualStartDate());
	}

	@RequestMapping(value = PLANNING_URL, params = {"setActualEndAuto"})
	@ResponseBody
	public String setActualEndAuto(@PathVariable long iterationId,
								   @RequestParam(value = "setActualEndAuto") boolean auto) {
		LOGGER.info("IterationModificationController : autosetting actual end date for campaign {}, new value {}", iterationId, auto);

		iterationModService.changeActualEndAuto(iterationId, auto);
		Iteration iteration = iterationModService.findById(iterationId);

		return dateToStr(iteration.getActualEndDate());

	}

	/* *************************************** test plan ********************************* */


	// returns the ID of the newly created execution
	@ResponseBody
	@RequestMapping(value = "/test-plan/{testPlanItemId}/executions/new", method = RequestMethod.POST, params = {"mode=manual"})
	public String addManualExecution(@PathVariable long testPlanItemId, @PathVariable long iterationId) {
		LOGGER.trace("Add manual execution : creating execution");

		Execution newExecution = iterationModService.addExecution(testPlanItemId);

		LOGGER.trace("Add manual execution : completed in");

		return newExecution.getId().toString();

	}


	/*
	 * TODO : should that method be in IterationTestPlanManagerController ?
	 */
	@RequestMapping(value = "/test-plan/{itemId}/executions", method = RequestMethod.GET)
	public ModelAndView getExecutionsForTestPlan(@PathVariable("iterationId") long iterationId,
												 @PathVariable("itemId") long itemId) {

		List<Execution> executionList = iterationModService.findExecutionsByTestPlan(iterationId,
			itemId);
		// get the iteraction to check access rights
		Iteration iter = iterationModService.findById(iterationId);
		IterationTestPlanItem testPlanItem = testPlanFinder.findTestPlanItem(itemId);
		ModelAndView mav = new ModelAndView("fragment/iterations/iteration-test-plan-row");

		MilestoneFeatureConfiguration milestoneConf = milestoneConfService.configure(iter);

		mav.addObject("testPlanItem", testPlanItem);
		mav.addObject(ITERATION_ID_KEY, iterationId);
		mav.addObject(ITERATION_KEY, iter);
		mav.addObject("executions", executionList);
		mav.addObject("milestoneConf", milestoneConf);
		return mav;

	}


	/* ********************** test suites **************************** */

	@ResponseBody
	@RequestMapping(value = "/test-suites/new", params = NAME, method = RequestMethod.POST)
	public Map<String, String> addTestSuite(@PathVariable long iterationId,
											@Valid @ModelAttribute("new-test-suite") TestSuite suite) {
		iterationModService.addTestSuite(iterationId, suite);
		Map<String, String> res = new HashMap<>();
		res.put("id", suite.getId().toString());
		res.put(NAME, suite.getName());
		return res;
	}

	@ResponseBody
	@RequestMapping(value = "/test-suites", method = RequestMethod.GET)
	public List<TestSuiteModel> getTestSuites(@PathVariable long iterationId) {
		Collection<TestSuite> testSuites = iterationModService.findAllTestSuites(iterationId);
		List<TestSuiteModel> result = new ArrayList<>();
		for (TestSuite testSuite : testSuites) {
			TestSuiteModel model = new TestSuiteModel(testSuite.getId(), testSuite.getName());
			result.add(model);
		}
		return result;
	}

	@ResponseBody
	@RequestMapping(value = "/test-suites/delete", method = RequestMethod.POST, params = {RequestParams.IDS})
	public OperationReport removeTestSuites(@RequestParam(RequestParams.IDS) List<Long> ids) {
		OperationReport report = iterationModService.removeTestSuites(ids);
		LOGGER.debug("removal of {} Test Suites", report.getRemoved().size());
		return report;
	}


	// ******************** other stuffs ***********************

	private static final class UserLoginComparator implements Comparator<User>, Serializable {
		@Override
		public int compare(User u1, User u2) {
			return u1.getLogin().compareTo(u2.getLogin());
		}

	}

}
