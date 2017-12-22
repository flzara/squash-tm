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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.tm.domain.audit.AuditableMixin;
import org.squashtest.tm.domain.campaign.TestPlanStatistics;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.service.campaign.IterationTestPlanFinder;
import org.squashtest.tm.service.campaign.TestSuiteModificationService;
import org.squashtest.tm.service.customfield.CustomFieldValueFinderService;
import org.squashtest.tm.service.internal.repository.TestSuiteDao;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.controller.generic.ServiceAwareAttachmentTableModelHelper;
import org.squashtest.tm.web.internal.controller.milestone.MilestoneFeatureConfiguration;
import org.squashtest.tm.web.internal.controller.milestone.MilestoneUIConfigurationService;
import org.squashtest.tm.web.internal.controller.testcase.TestCaseImportanceJeditableComboDataBuilder;
import org.squashtest.tm.web.internal.controller.testcase.TestCaseModeJeditableComboDataBuilder;
import org.squashtest.tm.web.internal.controller.testcase.executions.ExecutionStatusJeditableComboDataBuilder;
import org.squashtest.tm.web.internal.http.ContentTypes;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.jquery.RenameModel;
import org.squashtest.tm.web.internal.model.json.JsonGeneralInfo;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.*;

import static org.squashtest.tm.web.internal.helper.JEditablePostParams.VALUE;

@Controller
@RequestMapping("/test-suites/{suiteId}")
public class TestSuiteModificationController {

	private static final String TEST_SUITE = "testSuite";
	private static final Logger LOGGER = LoggerFactory.getLogger(TestSuiteModificationController.class);

	private static final String NAME = "name";

	@Inject
	private TestSuiteModificationService service;

	@Inject
	private TestSuiteDao testSuiteDao;

	@Inject
	private IterationTestPlanFinder iterationTestPlanFinder;

	@Inject
	private CustomFieldValueFinderService cufValueService;

	@Inject
	private ServiceAwareAttachmentTableModelHelper attachmentsHelper;

	@Inject
	private InternationalizationHelper messageSource;

	@Inject
	private Provider<TestCaseImportanceJeditableComboDataBuilder> importanceComboBuilderProvider;

	@Inject
	private Provider<TestCaseModeJeditableComboDataBuilder> modeComboBuilderProvider;

	@Inject
	private Provider<ExecutionStatusJeditableComboDataBuilder> executionStatusComboBuilderProvider;

	@Inject
	private MilestoneUIConfigurationService milestoneConfService;


	// will return the fragment only
	@RequestMapping(method = RequestMethod.GET)
	public String showTestSuite(Model model, @PathVariable("suiteId") long suiteId) {

		populateTestSuiteModel(model, suiteId);
		return "fragment/test-suites/test-suite";
	}

	// will return the iteration in a full page
	@RequestMapping(value = "/info", method = RequestMethod.GET)
	public String showTestSuiteInfo(Model model, @PathVariable("suiteId") long suiteId) {

		populateTestSuiteModel(model, suiteId);
		return "page/campaign-workspace/show-test-suite";
	}

	private void populateTestSuiteModel(Model model, long testSuiteId) {

		TestSuite testSuite = service.findById(testSuiteId);
		TestPlanStatistics testSuiteStats = service.findTestSuiteStatistics(testSuiteId);
		boolean hasCUF = cufValueService.hasCustomFields(testSuite);
		DataTableModel attachmentsModel = attachmentsHelper.findPagedAttachments(testSuite);
		Map<String, String> assignableUsers = getAssignableUsers(testSuiteId);
		Map<String, String> weights = getWeights();

		MilestoneFeatureConfiguration milestoneConf = milestoneConfService.configure(testSuite);


		model.addAttribute(TEST_SUITE, testSuite);
		model.addAttribute("statistics", testSuiteStats);
		model.addAttribute("hasCUF", hasCUF);
		model.addAttribute("attachmentsModel", attachmentsModel);
		model.addAttribute("assignableUsers", assignableUsers);
		model.addAttribute("allowsSettled",
				testSuite.getProject().getCampaignLibrary().allowsStatus(ExecutionStatus.SETTLED));
		model.addAttribute("allowsUntestable",
				testSuite.getProject().getCampaignLibrary().allowsStatus(ExecutionStatus.UNTESTABLE));
		model.addAttribute("weights", weights);
		model.addAttribute("modes", getModes());
		model.addAttribute("statuses", getStatuses(testSuite.getProject().getId()));
		model.addAttribute("milestoneConf", milestoneConf);
	}

	/**
	 * Will fetch the active {@link ExecutionStatus} for the project matching the given id
	 *
	 * @param projectId
	 *            : the id of the concerned {@link Project}
	 * @return a map representing the active statuses for the given project with :
	 *         <ul>
	 *         <li>key: the status name</li>
	 *         <li>value: the status internationalized label</li>
	 *         </ul>
	 */
	private Map<String, String> getStatuses(long projectId) {
		Locale locale = LocaleContextHolder.getLocale();
		return executionStatusComboBuilderProvider.get().useContext(projectId).useLocale(locale).buildMap();
	}

	private Map<String, String> getModes() {
		Locale locale = LocaleContextHolder.getLocale();
		return modeComboBuilderProvider.get().useLocale(locale).buildMap();
	}

	private Map<String, String> getWeights() {
		Locale locale = LocaleContextHolder.getLocale();
		return importanceComboBuilderProvider.get().useLocale(locale).buildMap();
	}

	private Map<String, String> getAssignableUsers(long testSuiteId) {

		Locale locale = LocaleContextHolder.getLocale();
		TestSuite ts = service.findById(testSuiteId);

		List<User> usersList = iterationTestPlanFinder.findAssignableUserForTestPlan(ts.getIteration().getId());
		Collections.sort(usersList, new UserLoginComparator());

		String unassignedLabel = messageSource.internationalize("label.Unassigned", locale);

		Map<String, String> jsonUsers = new LinkedHashMap<>(usersList.size());

		jsonUsers.put(User.NO_USER_ID.toString(), unassignedLabel);
		for (User user : usersList) {
			jsonUsers.put(user.getId().toString(), user.getLogin());
		}

		return jsonUsers;
	}

	@RequestMapping(value = "/general", method = RequestMethod.GET, produces = ContentTypes.APPLICATION_JSON)
	@ResponseBody
	public JsonGeneralInfo refreshGeneralInfos(@PathVariable("suiteId") long suiteId) {
		TestSuite testSuite = service.findById(suiteId);
		return new JsonGeneralInfo((AuditableMixin) testSuite);

	}

	@RequestMapping(value = "/statistics", method = RequestMethod.GET)
	public ModelAndView refreshStats(@PathVariable("suiteId") long suiteId) {

		TestPlanStatistics testSuiteStats = service.findTestSuiteStatistics(suiteId);
		TestSuite testSuite = service.findById(suiteId);
		ModelAndView mav = new ModelAndView("fragment/generics/statistics-fragment");
		mav.addObject("statisticsEntity", testSuiteStats);
		mav.addObject("allowsSettled", testSuite.getProject().getCampaignLibrary()
				.allowsStatus(ExecutionStatus.SETTLED));
		mav.addObject("allowsUntestable",
				testSuite.getProject().getCampaignLibrary().allowsStatus(ExecutionStatus.UNTESTABLE));

		return mav;
	}

	@RequestMapping(value = "/exec-button", method = RequestMethod.GET)
	public ModelAndView refreshExecButton(@PathVariable("suiteId") long suiteId) {

		TestPlanStatistics testSuiteStats = service.findTestSuiteStatistics(suiteId);

		ModelAndView mav = new ModelAndView("fragment/test-suites/test-suite-execution-button");

		mav.addObject("testSuiteId", suiteId);
		mav.addObject("statisticsEntity", testSuiteStats);

		return mav;
	}

	@RequestMapping(method = RequestMethod.POST, params = { "id=test-suite-description", VALUE })
	@ResponseBody
	public String updateDescription(@RequestParam(VALUE) String newDescription, @PathVariable("suiteId") long suiteId) {

		service.changeDescription(suiteId, newDescription);
		LOGGER.trace("Test-suite " + suiteId + ": updated description to " + newDescription);
		return newDescription;

	}

	@RequestMapping(method = RequestMethod.POST, params = {"id=test-suite-execution-status", VALUE})
	@ResponseBody
	public void updateExecutionStatus(@RequestParam(VALUE) String value, @PathVariable long suiteId) {

		ExecutionStatus executionStatus = ExecutionStatus.valueOf(value);
		service.changeExecutionStatus(suiteId, executionStatus);
		LOGGER.trace("Test-suite " + suiteId + ": updated status to " + value);
	}

	@RequestMapping(value = "/getExecutionStatus", method = RequestMethod.GET)
	@ResponseBody
	public String getExecutionStatus(@PathVariable long suiteId) {

		return testSuiteDao.findOne(suiteId).getExecutionStatus().toString();

	}

	@RequestMapping(method = RequestMethod.POST, params = { "newName" })
	@ResponseBody
	public Object rename(@RequestParam("newName") String newName,
						 @PathVariable("suiteId") long suiteId) {

		LOGGER.info("TestSuiteModificationController : renaming " + suiteId + " as " + newName);
		service.rename(suiteId, newName);
		return new RenameModel(newName);

	}

	// that method is redundant but don't remove it yet.
	@ResponseBody
	@RequestMapping(value = "/rename", method = RequestMethod.POST, params = RequestParams.NAME)
	public
	Map<String, String> renameTestSuite(@PathVariable("suiteId") Long suiteId, @RequestParam(RequestParams.NAME) String name) {
		service.rename(suiteId, name);
		Map<String, String> result = new HashMap<>();
		result.put("id", suiteId.toString());
		result.put(NAME, name);
		return result;
	}



	// ******************** other stuffs ********************

	private static final class UserLoginComparator implements Comparator<User> {
		@Override
		public int compare(User u1, User u2) {
			return u1.getLogin().compareTo(u2.getLogin());
		}

	}

}
