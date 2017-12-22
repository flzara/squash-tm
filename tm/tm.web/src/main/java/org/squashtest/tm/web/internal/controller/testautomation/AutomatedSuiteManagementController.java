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
package org.squashtest.tm.web.internal.controller.testautomation;

import static org.squashtest.tm.web.internal.controller.RequestParams.ITERATION_ID;
import static org.squashtest.tm.web.internal.controller.RequestParams.TEST_PLAN_ITEMS_IDS;
import static org.squashtest.tm.web.internal.controller.RequestParams.TEST_SUITE_ID;
import static org.squashtest.tm.web.internal.http.ContentTypes.APPLICATION_JSON;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.domain.testautomation.AutomatedSuite;
import org.squashtest.tm.service.testautomation.AutomatedSuiteManagerService;
import org.squashtest.tm.service.testautomation.model.SuiteExecutionConfiguration;
import org.squashtest.tm.service.testautomation.model.TestAutomationProjectContent;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.controller.execution.AutomatedExecutionViewUtils;
import org.squashtest.tm.web.internal.controller.execution.AutomatedExecutionViewUtils.AutomatedSuiteOverview;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;

@Controller
@RequestMapping("/automated-suites")
public class AutomatedSuiteManagementController {

	@Inject
	private InternationalizationHelper messageSource;

	@Inject
	private AutomatedSuiteManagerService service;

	@RequestMapping(value = "/new", method = RequestMethod.POST, params = { ITERATION_ID, "!testPlanItemsIds[]" }, produces = APPLICATION_JSON)
	@ResponseBody
	public AutomatedSuiteDetails createNewAutomatedSuiteForIteration(@RequestParam(ITERATION_ID) long iterationId) {
		AutomatedSuite suite = service.createFromIterationTestPlan(iterationId);
		return toProjectContentModel(suite);
	}

	@RequestMapping(value = "/new", method = RequestMethod.POST, params = { TEST_SUITE_ID, "!testPlanItemsIds[]" }, produces = APPLICATION_JSON)
	@ResponseBody
	public AutomatedSuiteDetails createNewAutomatedSuiteForTestSuite(@RequestParam(TEST_SUITE_ID) long testSuiteId) {
		AutomatedSuite suite = service.createFromTestSuiteTestPlan(testSuiteId);
		return toProjectContentModel(suite);
	}

	@RequestMapping(value = "/new", method = RequestMethod.POST, params = { TEST_PLAN_ITEMS_IDS, ITERATION_ID }, produces = APPLICATION_JSON)
	@ResponseBody
	public AutomatedSuiteDetails createNewAutomatedSuiteForIterationItems(
			@RequestParam("testPlanItemsIds[]") List<Long> testPlanIds, @RequestParam(ITERATION_ID) long iterationId) {
		if (testPlanIds.isEmpty()) {
			createNewAutomatedSuiteForIteration(iterationId);
		}

		AutomatedSuite suite = service.createFromItemsAndIteration(testPlanIds, iterationId);
		return toProjectContentModel(suite);
	}

	@RequestMapping(value = "/new", method = RequestMethod.POST, params = { TEST_PLAN_ITEMS_IDS, TEST_SUITE_ID }, produces = APPLICATION_JSON)
	@ResponseBody
	public AutomatedSuiteDetails createNewAutomatedSuiteForTestSuiteItems(
			@RequestParam("testPlanItemsIds[]") List<Long> testPlanIds, @RequestParam("testSuiteId") long testSuiteId) {
		if (testPlanIds.isEmpty()) {
			createNewAutomatedSuiteForTestSuite(testSuiteId);
		}

		AutomatedSuite suite = service.createFromItemsAndTestSuite(testPlanIds, testSuiteId);
		return toProjectContentModel(suite);
	}

	@RequestMapping(value = "/{suiteId}/executor", method = RequestMethod.POST, produces = APPLICATION_JSON)
	@ResponseBody
	public AutomatedSuiteOverview runAutomatedSuite(@PathVariable String suiteId,
			@RequestBody Collection<Map<String, ?>> rawConf,
			Locale locale) {

		/*
		 * ROUGH CODE ALERT
		 * 
		 * As you noticed the type of 'rawConf' in the signature is 'Collection<Map>'
		 * instead of 'Collection<SuiteExecutionConfiguration>'.
		 * 
		 * This is because Jackson wouldn't deserialized a collection to the right content type because of type erasure.
		 * So we manually convert the content that was serialized as a Map, to SuiteExecutionConfiguration
		 */
		Collection<SuiteExecutionConfiguration> configuration = new ArrayList<>(
				rawConf.size());
		for (Map<String, ?> rawC : rawConf) {
			long projectId = ((Integer) rawC.get(RequestParams.PROJECT_ID)).longValue();
			String node = (String) rawC.get("node");
			configuration.add(new SuiteExecutionConfiguration(projectId, node));
		}

		// now let's start the thing
		service.start(suiteId, configuration);
		return updateExecutionInfo(suiteId, locale);
	}

	@RequestMapping(value = "/{suiteId}/executions", method = RequestMethod.GET, produces = APPLICATION_JSON)
	@ResponseBody
	public AutomatedSuiteOverview updateExecutionInfo(@PathVariable String suiteId, Locale locale) {
		AutomatedSuite suite = service.findById(suiteId);
		return AutomatedExecutionViewUtils.buildExecInfo(suite, locale, messageSource);
	}

	@RequestMapping(value = "/{suiteId}", method = RequestMethod.DELETE)
	@ResponseBody
	public void deleteAutomatedSuite(@PathVariable("suiteId") String suiteId) {
		service.delete(suiteId);
	}

	@RequestMapping(value = "/{suiteId}/details", method = RequestMethod.GET, produces = APPLICATION_JSON)
	public AutomatedSuiteDetails getSuiteDetails(@PathVariable("suiteId") String suiteId) {
		AutomatedSuite suite = service.findById(suiteId);
		return toProjectContentModel(suite);
	}

	private AutomatedSuiteDetails toProjectContentModel(AutomatedSuite suite) {
		Collection<TestAutomationProjectContent> projectContents = service.sortByProject(suite);

		Collection<TestAutomationProjectContentModel> models =
				new ArrayList<>(projectContents.size());

		for (TestAutomationProjectContent content : projectContents) {
			models.add(new TestAutomationProjectContentModel(content));
		}

		return new AutomatedSuiteDetails(suite, models);
	}

	private static final class AutomatedSuiteDetails {

		private final String id;
		private final Collection<TestAutomationProjectContentModel> contexts;
		private final boolean manualNodeSelection;

		public AutomatedSuiteDetails(AutomatedSuite suite, Collection<TestAutomationProjectContentModel> projectContents) {
			super();
			this.id = suite.getId();
			this.contexts = projectContents;
			this.manualNodeSelection = suite.isManualNodeSelection();
		}

		@SuppressWarnings("unused")
		public String getId() {
			return id;
		}

		@SuppressWarnings("unused")
		public Collection<TestAutomationProjectContentModel> getContexts() {
			return contexts;
		}

		/**
		 * @return the manualNodeSelection
		 */
		@SuppressWarnings("unused")
		public boolean isManualNodeSelection() {
			return manualNodeSelection;
		}

	}

}
