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
package org.squashtest.tm.service.internal.execution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.execution.ExecutionStatusReport;
import org.squashtest.tm.domain.execution.ExecutionStep;
import org.squashtest.tm.domain.testautomation.AutomatedExecutionExtender;
import org.squashtest.tm.exception.execution.ExecutionHasNoRunnableStepException;
import org.squashtest.tm.exception.execution.ExecutionHasNoStepsException;
import org.squashtest.tm.service.campaign.CustomTestSuiteModificationService;
import org.squashtest.tm.service.campaign.IterationTestPlanManagerService;
import org.squashtest.tm.service.execution.ExecutionModificationService;
import org.squashtest.tm.service.execution.ExecutionProcessingService;
import org.squashtest.tm.service.internal.repository.ExecutionDao;
import org.squashtest.tm.service.internal.repository.ExecutionStepDao;
import org.squashtest.tm.service.security.UserContextService;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;

import static org.squashtest.tm.service.security.Authorizations.OR_HAS_ROLE_ADMIN;

@Service("squashtest.tm.service.ExecutionProcessingService")
@Transactional
public class ExecutionProcessingServiceImpl implements ExecutionProcessingService {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionProcessingServiceImpl.class);

	@Inject
	private UserContextService userContextService;

	@Inject
	private ExecutionDao executionDao;

	@Inject
	private ExecutionStepDao executionStepDao;

	@Inject
	private ExecutionModificationService execModService;

	@Inject
	private IterationTestPlanManagerService testPlanService;

	@Inject
	private CustomTestSuiteModificationService customTestSuiteModificationService;

	@Override
	public ExecutionStep findExecutionStep(Long executionStepId) {
		return executionStepDao.findById(executionStepId);
	}

	@Override
	public boolean wasNeverRun(Long executionId) {
		return executionDao.countSteps(executionId) - executionDao.countStatus(executionId, ExecutionStatus.READY) == 0;
	}

	@Override
	public ExecutionStep findRunnableExecutionStep(long executionId) throws ExecutionHasNoStepsException {
		Execution execution = executionDao.findOne(executionId);

		ExecutionStep step;
		try {
			step = execution.findFirstRunnableStep();
		} catch (ExecutionHasNoRunnableStepException e) { // NOSONAR : this exception is part of the nominal use case
			step = execution.getLastStep();
		}

		return step;
	}

	@Override
	public ExecutionStep findStepAt(long executionId, int executionStepIndex) {
		Execution execution = executionDao.findAndInit(executionId);

		return execution.getSteps().get(executionStepIndex);
	}

	@Override
	public void changeExecutionStepStatus(Long executionStepId, ExecutionStatus status) {
		ExecutionStep step = executionStepDao.findById(executionStepId);
		ExecutionStatus formerStatus = step.getExecutionStatus();

		step.setExecutionStatus(status);

		// update execution data for step and update execution and item test plan status and execution data
		forwardAndUpdateStatus(step, formerStatus);
	}

	@Override
	public void setExecutionStepComment(Long executionStepId, String comment) {
		ExecutionStep step = executionStepDao.findById(executionStepId);
		step.setComment(comment);
	}

	@Override
	public Execution findExecution(Long executionId) {
		return execModService.findAndInitExecution(executionId);
	}

	@Override
	public List<ExecutionStep> getExecutionSteps(Long executionId) {
		return execModService.findExecutionSteps(executionId);
	}

	@Override
	public int findExecutionStepRank(Long executionStepId) {
		ExecutionStep step = executionStepDao.findById(executionStepId);
		return step.getExecutionStepOrder();
	}

	@Override
	public int findTotalNumberSteps(Long executionId) {
		Execution execution = executionDao.findAndInit(executionId);
		return execution.getSteps().size();
	}

	@Override
	public void setExecutionStatus(Long executionId, ExecutionStatus status) {
		Execution execution = executionDao.findOne(executionId);
		execution.setExecutionStatus(status);

	}

	@Override
	public ExecutionStatusReport getExecutionStatusReport(Long executionId) {
		return executionDao.getStatusReport(executionId);
	}

	/***
	 * Method which update :<br>
	 * * execution and item test plan status * execution data for the step, execution and item test plan
	 *
	 * @param executionStep
	 * @param formerStepStatus
	 */
	private void forwardAndUpdateStatus(ExecutionStep executionStep, ExecutionStatus formerStepStatus) {
		// update step execution data
		updateStepExecutionData(executionStep);

		Execution execution = executionStepDao.findParentExecution(executionStep.getId());

		ExecutionStatus formerExecutionStatus = execution.getExecutionStatus();
		ExecutionStatus newStepStatus = executionStep.getExecutionStatus();

		// let's see if we can autocompute with only 3 these statuses
		ExecutionStatus newExecutionStatus = newStepStatus.deduceNewStatus(formerExecutionStatus, formerStepStatus);

		if (newExecutionStatus == null) { // means we couldn't autocompute with only 3 statuses
			ExecutionStatusReport report = executionDao.getStatusReport(execution.getId());
			newExecutionStatus = ExecutionStatus.computeNewStatus(report);
		}

		execution.setExecutionStatus(newExecutionStatus);

		// update execution and item test plan data, only if its new status is different from "READY"
		if (execution.getExecutionStatus().compareTo(ExecutionStatus.READY) != 0) {
			updateExecutionMetadata(execution);
		}

		for (TestSuite testSuite : execution.getTestPlan().getTestSuites()) {
			customTestSuiteModificationService.updateExecutionStatus(testSuite);

		}
	}

	/***
	 * Update the execution step lastExecutionBy and On values depending on the status
	 *
	 * @param executionStep
	 *            the step to update
	 */
	@Override
	public void updateStepExecutionData(ExecutionStep executionStep) {
		// check the execution step status
		if (executionStep.getExecutionStatus().compareTo(ExecutionStatus.READY) == 0) {
			// if the item test plan status is READY, we reset the data
			executionStep.setLastExecutedBy(null);
			executionStep.setLastExecutedOn(null);
		} else {
			// we update the step execution data
			executionStep.setLastExecutedBy(userContextService.getUsername());
			executionStep.setLastExecutedOn(new Date());
		}
	}

	@PreAuthorize("hasPermission(#execution, 'EXECUTE')" + OR_HAS_ROLE_ADMIN)
	@Override
	public void updateExecutionMetadata(Execution execution) {
		LOGGER.debug("update the executed by/on for given execution and it's test plan.");

		// Get the date and user of the most recent step which status is not at READY
		ExecutionStep mostRecentStep = getMostRecentExecutionStep(execution);
		execution.setLastExecutedBy(mostRecentStep.getLastExecutedBy());
		execution.setLastExecutedOn(mostRecentStep.getLastExecutedOn());

		// forward to the test plan
		IterationTestPlanItem testPlan = execution.getTestPlan();
		testPlanService.updateMetadata(testPlan);

	}

	@PreAuthorize("hasPermission(#extender, 'EXECUTE') or hasRole('ROLE_TA_API_CLIENT')" + OR_HAS_ROLE_ADMIN)
	@Override
	public void updateExecutionMetadata(AutomatedExecutionExtender extender) {

		Execution execution = extender.getExecution();

		execution.setLastExecutedOn(new Date());
		execution.setLastExecutedBy(userContextService.getUsername());

		// forward to the test plan
		IterationTestPlanItem testPlan = execution.getTestPlan();
		testPlanService.updateMetadata(testPlan);
	}

	/***
	 * Method which gets the most recent execution step which status is not at READY
	 *
	 * @param givenExecution
	 *            the execution from which we get the steps
	 * @return the most recent Execution Step which is not "READY"
	 */
	private ExecutionStep getMostRecentExecutionStep(Execution givenExecution) {
		// Start at the fist one
		ExecutionStep mostRecentExecutionStep = givenExecution.getSteps().get(0);
		List<ExecutionStep> stepList = givenExecution.getSteps();
		for (ExecutionStep executionStep : stepList) {
			// first the status
			if (executionStep.getExecutionStatus().compareTo(ExecutionStatus.READY) != 0) {
				// first the most recent execution step has no execution date
				if (mostRecentExecutionStep.getLastExecutedOn() == null) {
					mostRecentExecutionStep = executionStep;
				}
				// we compare the date and update the value if the step date is greater
				else if (executionStep.getLastExecutedOn() != null
					&& mostRecentExecutionStep.getLastExecutedOn().compareTo(executionStep.getLastExecutedOn()) < 0) {
					mostRecentExecutionStep = executionStep;
				}
			}
		}
		return mostRecentExecutionStep;
	}

	@Override
	public void setExecutionStatus(Long executionId, ExecutionStatusReport report) {
		Execution execution = executionDao.findAndInit(executionId);

		ExecutionStatus newStatus = ExecutionStatus.computeNewStatus(report);

		execution.setExecutionStatus(newStatus);

	}

}
