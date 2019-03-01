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

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.exception.execution.TestPlanItemNotExecutableException;
import org.squashtest.tm.exception.execution.TestPlanTerminatedOrNoStepsException;
import org.squashtest.tm.exception.execution.TestSuiteTestPlanHasDeletedTestCaseException;
import org.squashtest.tm.service.bugtracker.BugTrackersLocalService;
import org.squashtest.tm.service.campaign.EntityFinder;
import org.squashtest.tm.service.execution.ExecutionProcessingService;
import org.squashtest.tm.service.internal.bugtracker.BugTrackerConnectorFactory;
import org.squashtest.tm.service.internal.campaign.TestPlanExecutionProcessingService;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import java.text.MessageFormat;

/**
 * Abstract class for controller responsible of running an entire test plan
 * @param <E> class of the entity owning the test plan
 */
public abstract class AbstractTestPlanExecutionRunnerController<E> {
	 static class RequestMappingPattern {
		static final String INIT_EXECUTION_RUNNER = "/execution/runner";
		static final String INIT_NEXT_EXECUTION_RUNNER = "/{testPlanItemId}/next-execution/runner";
		static final String DELETE_ALL_EXECUTIONS = "/executions";
		static final String STEP = "/{testPlanItemId}/executions/{executionId}/steps/{stepId}";
		static final String INDEXED_STEP = "/{testPlanItemId}/executions/{executionId}/steps/index/{stepIndex}";
	}

	static class ResourceUrlPattern {
		static final String TEST_PLAN_ITEM = "/{0,number,######}/test-plan/{1,number,######}";
		static final String EXECUTION = TEST_PLAN_ITEM + "/executions/{2,number,######}";
		static final String STEPS = EXECUTION + "/steps";
		static final String STEP_INDEX = STEPS + "/index/{3,number,######}";
		static final String PROLOGUE_STEP = STEPS + "/prologue";
		static final String STEP = STEPS + "/{3,number,######}";
	}

	static final String OPTIMIZED_RUNNER_MAIN = "page/executions/oer-main-page";
	static final String REDIRECT ="redirect:";

	@Inject
	TestPlanExecutionProcessingService<E> testSuiteExecutionRunner;

	@Inject
	BugTrackerConnectorFactory btFactory;

	@Inject
	ExecutionProcessingService executionRunner;

	@Inject
	EntityFinder<E> suiteFinder;

	@Inject
	ExecutionRunnerControllerHelper helper;

	@Inject
	ExecutionProcessingController executionProcessingController;

	@Inject
	ServletContext servletContext;

	@Inject
	BugTrackersLocalService bugTrackersLocalService;

	abstract String completeRessourceUrlPattern(String urlPattern);

	String getExecutionUrl(long testSuiteId, Execution execution, boolean optimized) {
		return MessageFormat.format(completeRessourceUrlPattern(ResourceUrlPattern.EXECUTION), testSuiteId, execution.getTestPlan().getId(),
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
		boolean hasDeletedTestCaseInTestPlan = hasDeletedTestCaseInTestPlan(testSuiteId);
		if (! hasDeletedTestCaseInTestPlan) {
			try {
				testSuiteExecutionRunner.startResume(testSuiteId);
			} catch (TestPlanItemNotExecutableException e) {
				throw new TestPlanTerminatedOrNoStepsException(e);
			}
		} else {
			throw new TestSuiteTestPlanHasDeletedTestCaseException();
		}
	}

	/**
	 * Issue 7366
	 * Method which tests if test suite has at least one deleted test case in its test plan
	 *
	 * @param testSuiteId testSuiteId
	 * @return true if test suite has at least one deleted test case in its test plan
	 */
	abstract boolean hasDeletedTestCaseInTestPlan(long testSuiteId);
}
