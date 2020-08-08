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
package org.squashtest.tm.service.internal.campaign;

import gherkin.ast.Background;
import gherkin.ast.GherkinDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.squashtest.tm.core.foundation.lang.Wrapped;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.campaign.TestPlanOwner;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.testcase.KeywordTestCase;
import org.squashtest.tm.domain.testcase.ScriptedTestCase;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseVisitor;
import org.squashtest.tm.service.campaign.TestPlanExecutionProcessingService;
import org.squashtest.tm.service.internal.testcase.scripted.gherkin.GherkinStepGenerator;
import org.squashtest.tm.service.internal.testcase.scripted.gherkin.GherkinTestCaseParser;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.security.PermissionsUtils;
import org.squashtest.tm.service.security.SecurityCheckableObject;
import org.squashtest.tm.service.user.UserAccountService;

import javax.inject.Inject;
import java.util.List;

/**
 * Abstract class for services whose purposes is to process executions of a test plan
 * @param <E> class of the test plan owner
 * @author aguilhem
 */
public abstract class AbstractTestPlanExecutionProcessingService<E extends TestPlanOwner> implements TestPlanExecutionProcessingService<E> {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTestPlanExecutionProcessingService.class);

	private CampaignNodeDeletionHandler campaignDeletionHandler;

	private IterationTestPlanManager testPlanManager;

	private UserAccountService userService;

	private PermissionEvaluationService permissionEvaluationService;

	// Injection is made through constructor to allow simple mock injection in unit test.
	@Inject
	AbstractTestPlanExecutionProcessingService(CampaignNodeDeletionHandler campaignDeletionHandler, IterationTestPlanManager testPlanManager,
											   UserAccountService userService, PermissionEvaluationService permissionEvaluationService){
		this.campaignDeletionHandler = campaignDeletionHandler;
		this.testPlanManager = testPlanManager;
		this.userService = userService;
		this.permissionEvaluationService = permissionEvaluationService;
	}

	@Override
	public void deleteAllExecutions(long testPlanOwnerId) {
		// getTest plan
		E testPlanOwner = getTestPlanOwner(testPlanOwnerId);
		List<IterationTestPlanItem> testPlan = getTestPlan(testPlanOwner);
		if (!testPlan.isEmpty()) {
			// delete all executions
			deleteAllExecutionsOfTestPlan(testPlan, testPlanOwner);
		}
	}

	private void deleteAllExecutionsOfTestPlan(List<IterationTestPlanItem> testPlan, E testPlanOwner) {
		String testerLogin = findUserLoginIfTester(testPlanOwner);
		for (IterationTestPlanItem iterationTestPlanItem : testPlan) {
			if (testerLogin == null
				|| iterationTestPlanItem.getUser() != null && iterationTestPlanItem.getUser().getLogin()
				.equals(testerLogin)) {
				List<Execution> executions = iterationTestPlanItem.getExecutions();
				if (!executions.isEmpty()) {
					campaignDeletionHandler.deleteExecutions(executions);
				}
			}
		}
	}

	@Override
	public Execution startResumeNextExecution(long testPlanOwnerId, long testPlanItemId) {
		Execution execution;
		E testPlanOwner = getTestPlanOwner(testPlanOwnerId);
		String testerLogin = findUserLoginIfTester(testPlanOwner);
		IterationTestPlanItem item = findNextExecutableTestPlanItem(testPlanOwner, testPlanItemId, testerLogin);
		execution = findUnexecutedOrCreateExecution(item);
		while (execution == null || execution.getSteps().isEmpty()) {
			item = findNextExecutableTestPlanItem(testPlanOwner, item.getId());
			execution = findUnexecutedOrCreateExecution(item);
		}
		return execution;
	}

	@Override
	public Execution startResume(long testPlanOwnerId) {
		Execution execution;
		E testPlanOwner = getTestPlanOwner(testPlanOwnerId);
		String testerLogin = findUserLoginIfTester(testPlanOwner);
		IterationTestPlanItem item = findFirstExecutableTestPlanItem(testerLogin, testPlanOwner);
		execution = findUnexecutedOrCreateExecution(item);
		if (execution == null || execution.getSteps().isEmpty()) {
			startResumeNextExecution(testPlanOwnerId, item.getId());
		}
		return execution;
	}

	@Override
	public boolean hasMoreExecutableItems(long testPlanOwnerId, long testPlanItemId) {
		E testPlanOwner = getTestPlanOwner(testPlanOwnerId);
		String testerLogin = findUserLoginIfTester(testPlanOwner);
		return !isLastExecutableTestPlanItem(testPlanOwner, testPlanItemId, testerLogin);
	}

	@Override
	public boolean hasPreviousExecutableItems(long testPlanOwnerId, long testPlanItemId) {
		E testPlanOwner = getTestPlanOwner(testPlanOwnerId);
		String testerLogin = findUserLoginIfTester(testPlanOwner);
		return !isFirstExecutableTestPlanItem(testPlanOwner, testPlanItemId, testerLogin);
	}

	/**
	 * Getter for a {@link TestPlanOwner} with th given id
	 * @param testPlanOwnerId the wanted {@link TestPlanOwner} id
	 * @return the {@link TestPlanOwner} with the given id
	 */
	abstract E getTestPlanOwner(long testPlanOwnerId);

	/**
	 * Getter for the {@link TestPlanOwner}'s test plan
	 * @param testPlanOwner the {@link TestPlanOwner} from whom we want the test plan
	 * @return the given {@link TestPlanOwner}'s test plan
	 */
	abstract List<IterationTestPlanItem> getTestPlan(E testPlanOwner);

	/**
	 * Find the first executable {@link IterationTestPlanItem}, for the given {@link TestPlanOwner}, assigned to the {@link org.squashtest.tm.domain.users.User} with the given testerLogin
	 * @param testerLogin a {@link org.squashtest.tm.domain.users.User} login
	 * @param testPlanOwner a {@link TestPlanOwner}
	 * @return the first executable {@link IterationTestPlanItem}, for the given {@link TestPlanOwner}, assigned to the {@link org.squashtest.tm.domain.users.User} with the given testerLogin
	 */
	abstract IterationTestPlanItem findFirstExecutableTestPlanItem(String testerLogin, E testPlanOwner);

	/**
	 * Determine if the {@link IterationTestPlanItem} with the given testPlanItemId is the last executable {@link IterationTestPlanItem}
	 * assigned to the {@link org.squashtest.tm.domain.users.User} with the given testerLogin in the {@link TestPlanOwner}'s test plan
	 * @param testPlanOwner a {@link TestPlanOwner}
	 * @param testPlanItemId {@link IterationTestPlanItem} id
	 * @param testerLogin a {@link org.squashtest.tm.domain.users.User} login
	 * @return true if the {@link IterationTestPlanItem} is the last executable in the {@link TestPlanOwner}'s test plan
	 */
	private boolean isLastExecutableTestPlanItem(E testPlanOwner, long testPlanItemId, String testerLogin){
		List<IterationTestPlanItem> testPlans = getTestPlan(testPlanOwner);
		for (int i = testPlans.size() - 1; i >= 0; i--) {
			IterationTestPlanItem item = testPlans.get(i);
			// We have to check if the referenced test case has execution steps
			TestCase testCase = null;
			if (!item.isTestCaseDeleted()) {
				testCase = item.getReferencedTestCase();
				if (item.isExecutableThroughTestSuite() && testCaseHasSteps(testCase) && (testerLogin == null || item.isAssignedToUser(testerLogin))) {
					return testPlanItemId == item.getId();
				}
			}
		}

		return false;
	}

	private boolean testCaseHasSteps(TestCase testCase) {
		Wrapped<Boolean> hasSteps = new Wrapped<>();
		TestCaseVisitor visitor = new TestCaseVisitor() {
			@Override
			public void visit(TestCase testCase) {
				hasSteps.setValue(testCase.getSteps() != null && !testCase.getSteps().isEmpty());
			}

			@Override
			public void visit(KeywordTestCase keywordTestCase) {
				hasSteps.setValue(testCase.getSteps() != null && !testCase.getSteps().isEmpty());
			}

			@Override
			public void visit(ScriptedTestCase scriptedTestCase) {
				hasSteps.setValue(scriptedTestCase.getScript() != null && !scriptedTestCase.getScript().isEmpty() && hasScenarios(scriptedTestCase));
			}
		};
		testCase.accept(visitor);
		return hasSteps.getValue();

	}

	private boolean hasScenarios(ScriptedTestCase scriptedTestCase){
		GherkinTestCaseParser gherkinParser = new GherkinTestCaseParser(new GherkinStepGenerator());
		GherkinDocument gherkinScript = gherkinParser.parseToGherkinDocument(scriptedTestCase);
		return gherkinScript != null && gherkinScript.getFeature() != null && !gherkinScript.getFeature().getChildren().isEmpty()
			&& gherkinScript.getFeature().getChildren().stream().anyMatch( definition -> !(definition instanceof Background));
	}

	/**
	 * Determine if the {@link IterationTestPlanItem} with the given testPlanItemId is the first executable {@link IterationTestPlanItem}
	 * assigned to the {@link org.squashtest.tm.domain.users.User} with the given testerLogin in the {@link TestPlanOwner}'s test plan
	 * @param testPlanOwner a {@link TestPlanOwner}
	 * @param testPlanItemId {@link IterationTestPlanItem} id
	 * @param testerLogin a {@link org.squashtest.tm.domain.users.User} login
	 * @return true if the {@link IterationTestPlanItem} is the first executable in the {@link TestPlanOwner}'s test plan
	 */
	abstract boolean isFirstExecutableTestPlanItem(E testPlanOwner, long testPlanItemId, String testerLogin);

	/**
	 * Find the next executable {@link IterationTestPlanItem} after the one with the given testPlanItemId,
	 * assigned to the {@link org.squashtest.tm.domain.users.User} with the given testerLogin, in the {@link TestPlanOwner}'s test plan
	 * @param testPlanOwner a {@link TestPlanOwner}
	 * @param testPlanItemId {@link IterationTestPlanItem} id
	 * @param testerLogin a {@link org.squashtest.tm.domain.users.User} login
	 * @return the next executable {@link IterationTestPlanItem} after the one with the given testPlanItemId,
	 * assigned to the {@link org.squashtest.tm.domain.users.User} with the given testerLogin, in the {@link TestPlanOwner}'s test plan
	 */
	abstract IterationTestPlanItem findNextExecutableTestPlanItem(E testPlanOwner, long testPlanItemId, String testerLogin);

	/**
	 * Find the next executable {@link IterationTestPlanItem} after the one with the given testPlanItemId in the {@link TestPlanOwner}'s test plan
	 * @param testPlanOwner a {@link TestPlanOwner}
	 * @param testPlanItemId {@link IterationTestPlanItem} id
	 * @return the next executable {@link IterationTestPlanItem} after the one with the given testPlanItemId,
	 * assigned to the {@link org.squashtest.tm.domain.users.User} with the given testerLogin, in the {@link TestPlanOwner}'s test plan
	 */
	abstract IterationTestPlanItem findNextExecutableTestPlanItem(E testPlanOwner, long testPlanItemId);

	private String findUserLoginIfTester(Object domainObject) {
		String testerLogin = null;
		try {
			PermissionsUtils.checkPermission(permissionEvaluationService, new SecurityCheckableObject(domainObject,
				"READ_UNASSIGNED"));
		} catch (AccessDeniedException ade) {
			LOGGER.error(ade.getMessage(), ade);
			testerLogin = userService.findCurrentUser().getLogin();
		}
		return testerLogin;
	}

	/**
	 * if has executions: will return last execution if not terminated,<br>
	 * if has no execution and is not test-case deleted : will return new execution<br>
	 * else will return null
	 *
	 * @param testPlanItem an {@link IterationTestPlanItem}
	 *
	 */
	private Execution findUnexecutedOrCreateExecution(IterationTestPlanItem testPlanItem) {
		Execution executionToReturn = null;
		if (testPlanItem.isExecutableThroughTestSuite()) {
			executionToReturn = testPlanItem.getLatestExecution();
			if (executionToReturn == null) {
				executionToReturn = testPlanManager.addExecution(testPlanItem);
			}
		}
		return executionToReturn;
	}

}
