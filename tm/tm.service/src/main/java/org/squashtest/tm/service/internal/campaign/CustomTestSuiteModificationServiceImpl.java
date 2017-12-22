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

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.campaign.TestPlanStatistics;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.execution.ExecutionStatusReport;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.exception.DuplicateNameException;
import org.squashtest.tm.service.campaign.CustomTestSuiteModificationService;
import org.squashtest.tm.service.campaign.IterationModificationService;
import org.squashtest.tm.service.campaign.TestSuiteModificationService;
import org.squashtest.tm.service.internal.repository.TestSuiteDao;
import org.squashtest.tm.service.milestone.MilestoneMembershipFinder;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.security.PermissionsUtils;
import org.squashtest.tm.service.user.UserAccountService;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collection;

import static org.squashtest.tm.service.security.Authorizations.OR_HAS_ROLE_ADMIN;

@Service("CustomTestSuiteModificationService")
public class CustomTestSuiteModificationServiceImpl implements CustomTestSuiteModificationService {
	private static final String HAS_WRITE_PERMISSION_ID = "hasPermission(#suiteId, 'org.squashtest.tm.domain.campaign.TestSuite', 'WRITE') ";
	private static final String HAS_READ_PERMISSION_ID = "hasPermission(#suiteId, 'org.squashtest.tm.domain.campaign.TestSuite','READ') ";
	private static final String PERMISSION_EXECUTE_ITEM = "hasPermission(#testPlanItemId, 'org.squashtest.tm.domain.campaign.IterationTestPlanItem', 'EXECUTE') ";

	@Inject
	private TestSuiteDao testSuiteDao;

	@Inject
	private IterationModificationService iterationService;

	@Inject
	private UserAccountService userService;

	@Inject
	private PermissionEvaluationService permissionEvaluationService;

	@Inject
	private MilestoneMembershipFinder milestoneService;

	@Inject
	private TestSuiteModificationService testSuiteModificationService;

	@Override
	@PreAuthorize(HAS_WRITE_PERMISSION_ID + OR_HAS_ROLE_ADMIN)
	public void rename(long suiteId, String newName) throws DuplicateNameException {
		TestSuite suite = testSuiteDao.findOne(suiteId);
		suite.rename(newName);
	}

	@Override
	public void updateExecutionStatus(TestSuite testsuite) {
		ExecutionStatusReport report = testSuiteDao.getStatusReport(testsuite.getId());
		ExecutionStatus newExecutionStatus = ExecutionStatus.computeNewStatus(report);
		testsuite.setExecutionStatus(newExecutionStatus);
	}


	@Override
	@PreAuthorize(HAS_READ_PERMISSION_ID + OR_HAS_ROLE_ADMIN)
	public TestSuite findById(long suiteId) {
		return testSuiteDao.findOne(suiteId);
	}

	@Override
	@PreAuthorize(HAS_READ_PERMISSION_ID + OR_HAS_ROLE_ADMIN)
	public TestPlanStatistics findTestSuiteStatistics(long suiteId) {
		try {
			PermissionsUtils.checkPermission(permissionEvaluationService, Arrays.asList(suiteId), "READ_UNASSIGNED", TestSuite.class.getName());
			return testSuiteDao.getTestSuiteStatistics(suiteId);

		} catch (AccessDeniedException ade) {
			String userLogin = userService.findCurrentUser().getLogin();
			return testSuiteDao.getTestSuiteStatistics(suiteId, userLogin);

		}

	}


	@Override
	@PreAuthorize(PERMISSION_EXECUTE_ITEM + OR_HAS_ROLE_ADMIN)
	public Execution addExecution(long testPlanItemId) {
		return iterationService.addExecution(testPlanItemId);
	}

	@Override
	@Transactional(readOnly = true)
	@PreAuthorize(HAS_READ_PERMISSION_ID + OR_HAS_ROLE_ADMIN)
	public Collection<Milestone> findAllMilestones(long suiteId) {
		return milestoneService.findMilestonesForTestSuite(suiteId);
	}

}
