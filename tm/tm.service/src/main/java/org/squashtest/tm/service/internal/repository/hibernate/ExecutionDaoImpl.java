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
package org.squashtest.tm.service.internal.repository.hibernate;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.core.foundation.collection.Paging;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.bugtracker.IssueDetector;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.campaign.QIterationTestPlanItem;
import org.squashtest.tm.domain.execution.*;
import org.squashtest.tm.service.internal.foundation.collection.JpaPagingUtils;
import org.squashtest.tm.service.internal.foundation.collection.PagingUtils;
import org.squashtest.tm.service.internal.foundation.collection.SortingUtils;
import org.squashtest.tm.service.internal.repository.CustomExecutionDao;
import org.squashtest.tm.service.internal.repository.ParameterNames;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ExecutionDaoImpl implements CustomExecutionDao {
	private static final String TEST_CASE = "TestCase";
	private static final String TEST_SUITE = "TestSuite";
	private static final String EXECUTION = "Execution";
	private static final String TEST_PLAN = "TestPlan";
	private static final String EXECUTION_COUNT_STATUS = "Execution.countStatus";
	private static final String CAMPAIGN = "Campaign";
	private static final String CAMPAIGN_PROJECT = "Campaign.project";
	private static final String ITERATION = "Iteration";
	private static final String PROJECT = "Project";
	private static final String PROJECT_ID = "Project.id";
	private static final String ITERATION_CAMPAIGN = "Iteration.campaign";
	private static final String EXECUTION_STATUS = "executionStatus";
	private static final String STATUS = "status";

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public Execution findAndInit(long executionId) {
		Execution execution = findById(executionId);
		Hibernate.initialize(execution.getReferencedTestCase());
		Hibernate.initialize(execution.getSteps());
		return execution;
	}

	private Execution findById(long executionId) {
		return entityManager.getReference(Execution.class, executionId);
	}

	@Override
	public int findExecutionRank(long executionId) {
		IterationTestPlanItem testPlan = new JPAQueryFactory(entityManager)
			.selectFrom(QIterationTestPlanItem.iterationTestPlanItem)
			.innerJoin(QIterationTestPlanItem.iterationTestPlanItem.executions, QExecution.execution)
			.where(QExecution.execution.id.eq(executionId))
			.fetchOne();

		int index = 0;
		for (Execution execution : testPlan.getExecutions()) {
			if (execution.getId().equals(executionId)) {
				return index;
			}
			index++;
		}
		return index;
	}

	@Override
	public ExecutionStatusReport getStatusReport(final long executionId) {

		ExecutionStatusReport report = new ExecutionStatusReport();

		for (ExecutionStatus status : ExecutionStatus.values()) {
			final ExecutionStatus fStatus = status;

			Long lResult = (Long) entityManager.createNamedQuery(EXECUTION_COUNT_STATUS)
				.setParameter("execId", executionId)
				.setParameter("status", fStatus)
				.getSingleResult();

			report.set(status, lResult.intValue());
		}

		return report;
	}


	/*
	 * same than for HibernateTestCaseDao#findStepsByIdFiltered :
	 *
	 * because we need to get the ordered list and we can't access the join table to sort them (again), we can't use the
	 * Criteria API. So we're playing it old good java here.
	 *
	 * Note GRF : looks like service code to me
	 */

	@Override
	public List<ExecutionStep> findStepsFiltered(final Long executionId, final Paging filter) {

		Execution execution = findById(executionId);
		int listSize = execution.getSteps().size();

		int startIndex = filter.getFirstItemIndex();
		int lastIndex = filter.getFirstItemIndex() + filter.getPageSize();

		// prevent IndexOutOfBoundException :
		if (startIndex >= listSize) {
			return new LinkedList<>(); // ie resultset is empty
		}
		if (lastIndex >= listSize) {
			lastIndex = listSize;
		}

		return execution.getSteps().subList(startIndex, lastIndex);

	}

	// ************** special execution status deactivation section ***************

	@SuppressWarnings("unchecked")
	@Override
	public List<ExecutionStep> findAllExecutionStepsWithStatus(Long projectId, ExecutionStatus executionStatus) {

		Criteria crit = entityManager.unwrap(Session.class).createCriteria(ExecutionStep.class, "ExecutionStep");
		crit.createAlias("execution", EXECUTION, JoinType.INNER_JOIN);
		crit.createAlias("Execution.testPlan.iteration", ITERATION, JoinType.INNER_JOIN);
		crit.createAlias(ITERATION_CAMPAIGN, CAMPAIGN, JoinType.INNER_JOIN);
		crit.createAlias(CAMPAIGN_PROJECT, PROJECT, JoinType.INNER_JOIN);
		crit.add(Restrictions.eq(PROJECT_ID, projectId));
		crit.add(Restrictions.eq(EXECUTION_STATUS, executionStatus));

		return crit.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<IterationTestPlanItem> findAllIterationTestPlanItemsWithStatus(Long projectId, ExecutionStatus executionStatus) {


		Criteria crit = entityManager.unwrap(Session.class).createCriteria(IterationTestPlanItem.class, TEST_PLAN);
		crit.createAlias("iteration", ITERATION, JoinType.INNER_JOIN);
		crit.createAlias(ITERATION_CAMPAIGN, CAMPAIGN, JoinType.INNER_JOIN);
		crit.createAlias(CAMPAIGN_PROJECT, PROJECT, JoinType.INNER_JOIN);
		crit.add(Restrictions.eq(PROJECT_ID, projectId));
		crit.add(Restrictions.eq(EXECUTION_STATUS, executionStatus));

		return crit.list();
	}

	@Override
	public boolean projectUsesExecutionStatus(long projectId, ExecutionStatus executionStatus) {
		return hasExecStepWithStatus(projectId, executionStatus)
			|| hasItemTestPlanWithStatus(projectId, executionStatus)
			|| hasExecWithStatus(projectId, executionStatus);
	}

	private boolean hasItemTestPlanWithStatus(long projectId, ExecutionStatus executionStatus) {
		Query qStep = entityManager.createNamedQuery("executionStep.countAllStatus");
		qStep.setParameter(STATUS, executionStatus);
		qStep.setParameter(ParameterNames.PROJECT_ID, projectId);
		Long nStep = (Long) qStep.getSingleResult();
		return nStep > 0;
	}

	private boolean hasExecWithStatus(long projectId, ExecutionStatus executionStatus) {
		Query qExec = entityManager.createNamedQuery("execution.countAllStatus");
		qExec.setParameter(STATUS, executionStatus);
		qExec.setParameter(ParameterNames.PROJECT_ID, projectId);
		Long nExec = (Long) qExec.getSingleResult();
		return nExec > 0;
	}

	private boolean hasExecStepWithStatus(long projectId, ExecutionStatus executionStatus) {
		Query qITP = entityManager.createNamedQuery("iterationTestPlanItem.countAllStatus");
		qITP.setParameter(STATUS, executionStatus);
		qITP.setParameter(ParameterNames.PROJECT_ID, projectId);
		Long nITP = (Long) qITP.getSingleResult();
		return nITP > 0;
	}

// ************** /special execution status deactivation section **************

	@Override
	public List<IssueDetector> findAllIssueDetectorsForExecution(Long execId) {
		// NOTE GRF looks like service code to me
		Execution execution = findById(execId);
		List<ExecutionStep> steps = execution.getSteps();
		List<IssueDetector> issueDetectors = new ArrayList<>(steps.size() + 1);
		issueDetectors.add(execution);
		issueDetectors.addAll(steps);
		return issueDetectors;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Execution> findAllByTestCaseIdOrderByRunDate(long testCaseId, Paging paging) {
		Query query = entityManager.createNamedQuery("execution.findAllByTestCaseIdOrderByRunDate");
		JpaPagingUtils.addPaging(query, paging);
		query.setParameter("testCaseId", testCaseId);

		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Execution> findAllByTestCaseId(long testCaseId, PagingAndSorting pas) {
		Criteria crit = entityManager.unwrap(Session.class).createCriteria(Execution.class, EXECUTION)
			.createAlias("Execution.testPlan", TEST_PLAN, JoinType.LEFT_OUTER_JOIN)
			.createAlias("TestPlan.iteration", ITERATION, JoinType.LEFT_OUTER_JOIN)
			.createAlias(ITERATION_CAMPAIGN, CAMPAIGN, JoinType.LEFT_OUTER_JOIN)
			.createAlias(CAMPAIGN_PROJECT, PROJECT, JoinType.LEFT_OUTER_JOIN)
			.createAlias("Execution.referencedTestCase", TEST_CASE, JoinType.LEFT_OUTER_JOIN)
			.createAlias("TestPlan.testSuites", TEST_SUITE, JoinType.LEFT_OUTER_JOIN);

		crit.add(Restrictions.eq("TestCase.id", testCaseId));

		crit.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);

		PagingUtils.addPaging(crit, pas);
		SortingUtils.addOrder(crit, pas);

		return crit.list();
	}

}
