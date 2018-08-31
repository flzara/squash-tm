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

import org.springframework.data.domain.Example;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.Paging;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.PagingBackedPagedCollectionHolder;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.execution.ExecutionStep;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.exception.execution.ExecutionHasNoStepsException;
import org.squashtest.tm.service.advancedsearch.IndexationService;
import org.squashtest.tm.service.deletion.SuppressionPreviewReport;
import org.squashtest.tm.service.execution.ExecutionModificationService;
import org.squashtest.tm.service.internal.campaign.CampaignNodeDeletionHandler;
import org.squashtest.tm.service.internal.repository.ExecutionDao;
import org.squashtest.tm.service.internal.repository.ExecutionStepDao;
import org.squashtest.tm.service.security.Authorizations;

import javax.inject.Inject;
import java.util.List;

import static org.squashtest.tm.service.security.Authorizations.EXECUTE_EXECSTEP_OR_ROLE_ADMIN;
import static org.squashtest.tm.service.security.Authorizations.EXECUTE_EXECUTION_OR_ROLE_ADMIN;
import static org.squashtest.tm.service.security.Authorizations.OR_HAS_ROLE_ADMIN;

@Service("squashtest.tm.service.ExecutionModificationService")
public class ExecutionModificationServiceImpl implements ExecutionModificationService {

	@Inject
	private ExecutionDao executionDao;

	@Inject
	private ExecutionStepDao executionStepDao;

	@Inject
	private CampaignNodeDeletionHandler deletionHandler;

	@Inject
	private IndexationService indexationService;

	@Inject
	private ExecutionStepModificationHelper executionStepModifHelper;


	@Override
	@Transactional(readOnly = false)
	public Execution findAndInitExecution(Long executionId) {
		return executionDao.findAndInit(executionId);
	}

	@Override
	@Transactional(readOnly = false)
	@PreAuthorize(EXECUTE_EXECUTION_OR_ROLE_ADMIN)
	public void setExecutionDescription(Long executionId, String description) {
		Execution execution = executionDao.getOne(executionId);
		execution.setDescription(description);
	}

	@Override
	@Transactional(readOnly = true)
	public List<ExecutionStep> findExecutionSteps(long executionId) {
		return executionDao.findSteps(executionId);
	}

	@Override
	@Transactional(readOnly = true)
	public int findExecutionRank(Long executionId) {
		return executionDao.findExecutionRank(executionId);
	}

	@Override
	@Transactional(readOnly = false)
	@PreAuthorize(EXECUTE_EXECSTEP_OR_ROLE_ADMIN)
	public void setExecutionStepComment(Long executionStepId, String comment) {
		ExecutionStep executionStep = executionStepDao.findById(executionStepId);
		executionStep.setComment(comment);
	}

	@Override
	@Transactional(readOnly = false)
	public PagedCollectionHolder<List<ExecutionStep>> findExecutionSteps(long executionId, Paging filter) {
		List<ExecutionStep> list = executionDao.findStepsFiltered(executionId, filter);
		long count = executionDao.countSteps(executionId);
		return new PagingBackedPagedCollectionHolder<>(filter, count, list);
	}

	@Override
	@PreAuthorize(EXECUTE_EXECUTION_OR_ROLE_ADMIN)
	@Transactional(readOnly = false)
	public List<SuppressionPreviewReport> simulateExecutionDeletion(Long executionId) {
		return deletionHandler.simulateExecutionDeletion(executionId);
	}

	@Override
	@Transactional(readOnly = false)
	@PreAuthorize("hasPermission(#execution, 'EXECUTE') " + OR_HAS_ROLE_ADMIN)
	public void deleteExecution(Execution execution) {
		TestCase testCase = execution.getReferencedTestCase();
		deletionHandler.deleteExecution(execution);
		if (testCase != null) {
			indexationService.reindexTestCase(testCase.getId());
		}
	}

	@Override
	@Transactional(readOnly = true)
	public Execution findById(long id) {
		return executionDao.getOne(id);
	}

	@Override
	@Transactional(readOnly = true)
	public ExecutionStep findExecutionStepById(long id) {
		return executionStepDao.findById(id);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Execution> findAllByTestCaseIdOrderByRunDate(long testCaseId, Paging paging) {
		return executionDao.findAllByTestCaseIdOrderByRunDate(testCaseId, paging);
	}

	@Override
	@Transactional(readOnly = true)
	public PagedCollectionHolder<List<Execution>> findAllByTestCaseId(long testCaseId, PagingAndSorting pas) {
		List<Execution> executions = executionDao.findAllByTestCaseId(testCaseId, pas);
		long count = executionDao.countByTestCaseId(testCaseId);
		return new PagingBackedPagedCollectionHolder<>(pas, count, executions);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean exists(long id) {
		// !!! since recent change in Spring Data, the former dao method #exists(ID) have been
		// deleted, leaving us only with the query-by-example option. I'd rather load the entity outright.
		return executionDao.findById(id).isPresent();
	}

	@Override
	@Transactional(readOnly = false)
	@PreAuthorize(EXECUTE_EXECUTION_OR_ROLE_ADMIN)
	public void setExecutionStatus(Long executionId, ExecutionStatus status) {
		Execution execution = executionDao.getOne(executionId);
		execution.setExecutionStatus(status);

	}

	@Override
	@Transactional(readOnly = false)
	public long updateSteps(long executionId) {
		Execution execution = executionDao.getOne(executionId);
		List<ExecutionStep> toBeUpdated = executionStepModifHelper.findStepsToUpdate(execution);

		long result = executionStepModifHelper.doUpdateStep(toBeUpdated, execution);

		if (execution.getSteps().isEmpty()) {
			throw new ExecutionHasNoStepsException();
		}
		return result;
	}

}
