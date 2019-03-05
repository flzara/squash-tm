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
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.service.internal.repository.IterationDao;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.user.UserAccountService;

import javax.inject.Inject;

import java.util.List;

import static org.squashtest.tm.service.security.Authorizations.OR_HAS_ROLE_ADMIN;

@Service("squashtest.tm.service.IterationExecutionProcessingService")
@Transactional
public class IterationExecutionProcessingServiceImpl extends AbstractTestPlanExecutionProcessingService<Iteration> {

	private static final String CAN_EXECUTE_BY_ITERATION_ID = "hasPermission(#iterationId, 'org.squashtest.tm.domain.campaign.Iteration', 'EXECUTE')" + OR_HAS_ROLE_ADMIN;

	@Inject
	private IterationDao iterationDao;

	IterationExecutionProcessingServiceImpl(CampaignNodeDeletionHandler campaignDeletionHandler, IterationTestPlanManager testPlanManager, UserAccountService userService, PermissionEvaluationService permissionEvaluationService) {
		super(campaignDeletionHandler, testPlanManager, userService, permissionEvaluationService);
	}

	/**
	 * @see org.squashtest.tm.service.campaign.TestPlanExecutionProcessingService#startResume(long)
	 */
	@Override
	@PreAuthorize(CAN_EXECUTE_BY_ITERATION_ID)
	public Execution startResume(long iterationId) {
		return super.startResume(iterationId);
	}

	/**
	 * @see org.squashtest.tm.service.campaign.TestPlanExecutionProcessingService#startResumeNextExecution(long, long)
	 */
	@Override
	@PreAuthorize(CAN_EXECUTE_BY_ITERATION_ID)
	public Execution startResumeNextExecution(long iterationId, long testPlanItemId) {
		return super.startResumeNextExecution(iterationId, testPlanItemId);
	}

	/**
	 * @see org.squashtest.tm.service.campaign.TestPlanExecutionProcessingService#deleteAllExecutions(long)
	 */
	@Override
	@PreAuthorize(CAN_EXECUTE_BY_ITERATION_ID)
	public void deleteAllExecutions(long iterationId) {
		super.deleteAllExecutions(iterationId);
	}


	/**
	 * @see org.squashtest.tm.service.campaign.TestPlanExecutionProcessingService#hasMoreExecutableItems(long, long)
	 */
	@Override
	public boolean hasMoreExecutableItems(long iterationId, long testPlanItemId) {
		return super.hasMoreExecutableItems(iterationId, testPlanItemId);
	}

	/**
	 * @see org.squashtest.tm.service.campaign.TestPlanExecutionProcessingService#hasPreviousExecutableItems(long,
	 *      long)
	 */
	@Override
	public boolean hasPreviousExecutableItems(long iterationId, long testPlanItemId) {
		return super.hasPreviousExecutableItems(iterationId, testPlanItemId);
	}

	@Override
	Iteration getTestPlanOwner(long iterationId) {
		return iterationDao.findById(iterationId);
	}

	@Override
	List<IterationTestPlanItem> getTestPlan(Iteration iteration) {
		return iteration.getTestPlans();
	}

	@Override
	IterationTestPlanItem findFirstExecutableTestPlanItem(String testerLogin, Iteration iteration) {
		return iteration.findFirstExecutableTestPlanItem(testerLogin);
	}

	@Override
	boolean isLastExecutableTestPlanItem(Iteration iteration, long testPlanItemId, String testerLogin) {
		return iteration.isLastExecutableTestPlanItem(testPlanItemId, testerLogin);
	}

	@Override
	boolean isFirstExecutableTestPlanItem(Iteration iteration, long testPlanItemId, String testerLogin) {
		return iteration.isFirstExecutableTestPlanItem(testPlanItemId, testerLogin);
	}

	@Override
	IterationTestPlanItem findNextExecutableTestPlanItem(Iteration iteration, long testPlanItemId, String testerLogin) {
		return iteration.findNextExecutableTestPlanItem(testPlanItemId, testerLogin);
	}

	@Override
	IterationTestPlanItem findNextExecutableTestPlanItem(Iteration iteration, long testPlanItemId) {
		return iteration.findNextExecutableTestPlanItem(testPlanItemId);
	}

}
