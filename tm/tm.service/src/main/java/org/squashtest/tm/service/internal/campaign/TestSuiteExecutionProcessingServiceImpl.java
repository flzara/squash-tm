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

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.service.internal.repository.TestSuiteDao;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.user.UserAccountService;

import javax.inject.Inject;
import java.util.List;

import static org.squashtest.tm.service.security.Authorizations.OR_HAS_ROLE_ADMIN;

@Service("squashtest.tm.service.TestSuiteExecutionProcessingService")
@Transactional
public class TestSuiteExecutionProcessingServiceImpl extends AbstractTestPlanExecutionProcessingService<TestSuite> {

	private static final String CAN_EXECUTE_BY_TESTSUITE_ID = "hasPermission(#testSuiteId, 'org.squashtest.tm.domain.campaign.TestSuite', 'EXECUTE')" + OR_HAS_ROLE_ADMIN;

	@Inject
	private TestSuiteDao suiteDao;

	TestSuiteExecutionProcessingServiceImpl(CampaignNodeDeletionHandler campaignDeletionHandler, IterationTestPlanManager testPlanManager, UserAccountService userService, PermissionEvaluationService permissionEvaluationService) {
		super(campaignDeletionHandler, testPlanManager, userService, permissionEvaluationService);
	}

	/**
	 * @see org.squashtest.tm.service.campaign.TestPlanExecutionProcessingService#startResume(long)
	 */
	@Override
	@PreAuthorize(CAN_EXECUTE_BY_TESTSUITE_ID)
	public Execution startResume(long testSuiteId) {
		return super.startResume(testSuiteId);
	}

	/**
	 * @see org.squashtest.tm.service.campaign.TestPlanExecutionProcessingService#startResumeNextExecution(long, long)
	 */
	@Override
	@PreAuthorize(CAN_EXECUTE_BY_TESTSUITE_ID)
	public Execution startResumeNextExecution(long testSuiteId, long testPlanItemId) {
		return super.startResumeNextExecution(testSuiteId, testPlanItemId);
	}

	/**
	 * @see org.squashtest.tm.service.campaign.TestPlanExecutionProcessingService#deleteAllExecutions(long)
	 */
	@Override
	@PreAuthorize(CAN_EXECUTE_BY_TESTSUITE_ID)
	public void deleteAllExecutions(long testSuiteId) {
		super.deleteAllExecutions(testSuiteId);
	}


	/**
	 * @see org.squashtest.tm.service.campaign.TestPlanExecutionProcessingService#hasMoreExecutableItems(long, long)
	 */
	@Override
	public boolean hasMoreExecutableItems(long testSuiteId, long testPlanItemId) {
		return super.hasMoreExecutableItems(testSuiteId, testPlanItemId);
	}

	/**
	 * @see org.squashtest.tm.service.campaign.TestPlanExecutionProcessingService#hasPreviousExecutableItems(long,
	 *      long)
	 */
	@Override
	public boolean hasPreviousExecutableItems(long testSuiteId, long testPlanItemId) {
		return super.hasPreviousExecutableItems(testSuiteId, testPlanItemId);
	}

	@Override
	TestSuite getTestPlanOwner(long testSuiteId) {
		return suiteDao.getOne(testSuiteId);
	}

	@Override
	List<IterationTestPlanItem> getTestPlan(TestSuite suite) {
		return suite.getTestPlan();
	}

	@Override
	IterationTestPlanItem findFirstExecutableTestPlanItem(String testerLogin, TestSuite suite) {
		return suite.findFirstExecutableTestPlanItem(testerLogin);
	}

	@Override
	boolean isLastExecutableTestPlanItem(TestSuite suite, long testPlanItemId, String testerLogin) {
		return suite.isLastExecutableTestPlanItem(testPlanItemId, testerLogin);
	}

	@Override
	boolean isFirstExecutableTestPlanItem(TestSuite suite, long testPlanItemId, String testerLogin) {
		return suite.isFirstExecutableTestPlanItem(testPlanItemId, testerLogin);
	}

	@Override
	IterationTestPlanItem findNextExecutableTestPlanItem(TestSuite suite, long testPlanItemId, String testerLogin) {
		return suite.findNextExecutableTestPlanItem(testPlanItemId, testerLogin);
	}

	@Override
	IterationTestPlanItem findNextExecutableTestPlanItem(TestSuite suite, long testPlanItemId) {
		return suite.findNextExecutableTestPlanItem(testPlanItemId);
	}

}
