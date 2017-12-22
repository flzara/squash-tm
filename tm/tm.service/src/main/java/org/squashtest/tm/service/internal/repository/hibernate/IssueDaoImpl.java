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
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.bugtracker.Issue;
import org.squashtest.tm.domain.bugtracker.IssueDetector;
import org.squashtest.tm.domain.bugtracker.QIssue;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.CampaignFolder;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStep;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.internal.bugtracker.Pair;
import org.squashtest.tm.service.internal.bugtracker.RequirementIssueSupport;
import org.squashtest.tm.service.internal.foundation.collection.PagingUtils;
import org.squashtest.tm.service.internal.foundation.collection.SortingUtils;
import org.squashtest.tm.service.internal.repository.CustomIssueDao;
import org.squashtest.tm.service.internal.repository.IssueDao;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class IssueDaoImpl implements CustomIssueDao {
	private static final Logger LOGGER = LoggerFactory.getLogger(IssueDaoImpl.class);

	private static final String SELECT_ISSUES_INTRO =
			"select Issue from Issue Issue ";


	private static final String SELECT_ISSUES_OUTRO =
			"and Issue.bugtracker.id in (" +
					"select bt.id " +
					"from ExecutionStep estep " +
					"inner join estep.execution exec " +
					"inner join exec.testPlan tp " +
					"inner join tp.iteration it " +
					"inner join it.campaign cp " +
					"inner join cp.project proj " +
					"inner join proj.bugtrackerBinding binding " +
					"inner join binding.bugtracker bt " +
					"where estep.id in (:executionStepsIds) " +
					") ";

	private static final String WHERE_CLAUSE_FOR_ISSUES_FROM_EXEC_AND_EXEC_STEP =
			// ------------------------------------Where issues is from the given
			// Executions
			"where (" +
			"Issue.id in ( "+
			"select isExec.id "+
			"from Execution exec "+
			"inner join exec.issueList ile "+
			"inner join ile.issues isExec "+
			"where exec.id in (:executionsIds) " +
			") "+
			"or Issue.id in (" +
			"select isStep.id " +
			"from ExecutionStep estep " +
			"inner join estep.issueList ils " +
			"inner join ils.issues isStep " +
			"where estep.id in (:executionStepsIds) " +
			") " +
			") ";

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	@SuppressWarnings("unchecked")
	public List<Pair<Execution, Issue>> findAllExecutionIssuePairsByCampaign(Campaign campaign, PagingAndSorting sorter) {
		String hql = SortingUtils.addOrder("select new org.squashtest.tm.service.internal.bugtracker.Pair(ex, Issue) from Execution ex join ex.testPlan tp join tp.iteration i join i.campaign c join ex.issues Issue where c = :camp", sorter);

		Query query = entityManager.unwrap(Session.class).createQuery(hql).setParameter("camp", campaign);
		PagingUtils.addPaging(query, sorter);

		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Issue> findSortedIssuesFromIssuesLists(final Collection<Long> issueListIds,
			final PagingAndSorting sorter, Long bugtrackerId) {

		if (issueListIds.isEmpty()) {
			return Collections.emptyList();
		}

		Criteria crit = entityManager.unwrap(Session.class).createCriteria(Issue.class, "Issue")
				.add(Restrictions.in("Issue.issueList.id", issueListIds))
				.add(Restrictions.eq("Issue.bugtracker.id", bugtrackerId));

		SortingUtils.addOrder(crit, sorter);
		PagingUtils.addPaging(crit, sorter);

		return crit.list();

	}

	/**
	 * @see {@linkplain IssueDao#findSortedIssuesFromExecutionAndExecutionSteps(List, List, PagingAndSorting)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Issue> findSortedIssuesFromExecutionAndExecutionSteps(List<Long> executionsIds,
			List<Long> executionStepsIds, PagingAndSorting sorter) {
		if (!executionsIds.isEmpty() && !executionStepsIds.isEmpty()) {

			String queryString = SELECT_ISSUES_INTRO + WHERE_CLAUSE_FOR_ISSUES_FROM_EXEC_AND_EXEC_STEP + SELECT_ISSUES_OUTRO;

			queryString += " order by " + sorter.getSortedAttribute() + " " + sorter.getSortOrder().getCode();

			Query query = entityManager.unwrap(Session.class).createQuery(queryString);
			query.setParameterList("executionsIds", executionsIds);
			query.setParameterList("executionStepsIds", executionStepsIds);

			if (!sorter.shouldDisplayAll()) {
				PagingUtils.addPaging(query, sorter);
			}

			return query.list();

		}

		return Collections.emptyList();
	}

	@Override
	public IssueDetector findIssueDetectorByIssue(long id) {
		IssueDetector res;
		try {
			res = findExecutionByIssue(id);
		} catch (NoResultException e) {
			res = findExecutionStepByIssue(id);
		}
		return res;
	}

	private Execution findExecutionByIssue(long issueId) throws NoResultException {
		return (Execution) entityManager.createNamedQuery("Issue.findExecution")
			.setParameter("id", issueId)
			.getSingleResult();
	}

	private ExecutionStep findExecutionStepByIssue(long issueId) throws NoResultException {
		return (ExecutionStep) entityManager.createNamedQuery("Issue.findExecutionStep")
			.setParameter("id", issueId)
			.getSingleResult();
	}

	@Override
	public TestCase findTestCaseRelatedToIssue(long id) {

		TestCase testCase = null;

		try {
			Execution exec = findExecutionByIssue(id);
			testCase = exec.getReferencedTestCase();

		} catch (NoResultException e) {
			try {
				ExecutionStep step = findExecutionStepByIssue(id);

				if (step.getExecution() != null) {
				testCase = step.getExecution().getReferencedTestCase();
			}

			} catch (NoResultException ex) {
				// NOOP - not too sure if this can happen, former hibernate based code would return null in this case
				LOGGER.warn("Could not find execution step for issue id {}", id, ex);
			}
		}

		return testCase;
	}

    @Override
    public Execution findExecutionRelatedToIssue(long id) {
		Execution exec = null;

		try {
			exec = findExecutionByIssue(id);

		} catch (NoResultException e) {
			try {
				ExecutionStep step = findExecutionStepByIssue(id);

				if (step.getExecution() != null) {
        		 exec = step.getExecution();
        	 }
			} catch (Exception ex) {
				// NOOP - not too sure if this can happen, former hibernate based code would return null in this case
				LOGGER.warn("Could not find execution step for issue id {}", id, ex);
         }
		}

         return exec;
    }

	@SuppressWarnings("unchecked")
	@Override
	public List<Issue> getAllIssueFromBugTrackerId(Long bugtrackerId) {
		return new JPAQueryFactory(entityManager)
			.selectFrom(QIssue.issue)
			.where(QIssue.issue.bugtracker.id.eq(bugtrackerId))
			.fetch();
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Issue> findIssueListByRemoteIssue(String remoteid, BugTracker bugtracker) {
		return new JPAQueryFactory(entityManager)
			.selectFrom(QIssue.issue)
			.where(
				QIssue.issue.bugtracker.eq(bugtracker)
				.and(QIssue.issue.remoteIssueId.eq(remoteid))
			).fetch();
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Pair<Execution, Issue>> findAllDeclaredExecutionIssuePairsByExecution(Execution execution, PagingAndSorting sorter) {
		String hql = SortingUtils.addOrder("select new org.squashtest.tm.service.internal.bugtracker.Pair(ex, Issue) from Execution ex join ex.issueList il join il.issues Issue where ex = :execution", sorter);

		Query query = entityManager.unwrap(Session.class).createQuery(hql).setParameter("execution", execution);
		PagingUtils.addPaging(query, sorter);

		return query.list();
}

	@Override
	@SuppressWarnings("unchecked")
	public List<Pair<Execution, Issue>> findAllExecutionIssuePairsByIteration(Iteration iteration, PagingAndSorting sorter) {
		String hql = SortingUtils.addOrder("select new org.squashtest.tm.service.internal.bugtracker.Pair(ex, Issue) from Execution ex join ex.testPlan tp join tp.iteration i join ex.issues Issue where i = :iteration", sorter);

		Query query = entityManager.unwrap(Session.class).createQuery(hql).setParameter("iteration", iteration);
		PagingUtils.addPaging(query, sorter);

		return query.list();
}

	@Override
	@SuppressWarnings("unchecked")
	public List<Pair<Execution, Issue>> findAllExecutionIssuePairsByTestSuite(TestSuite testSuite, PagingAndSorting sorter) {
		String hql = SortingUtils.addOrder("select new org.squashtest.tm.service.internal.bugtracker.Pair(ex, Issue) from TestSuite ts join ts.testPlan tp join tp.executions ex join ex.issues Issue where ts = :testSuite", sorter);

		Query query = entityManager.unwrap(Session.class).createQuery(hql).setParameter("testSuite", testSuite);
		PagingUtils.addPaging(query, sorter);

		return query.list();
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Pair<Execution, Issue>> findAllExecutionIssuePairsByCampaignFolder(CampaignFolder folder, PagingAndSorting sorter) {
		String hql = SortingUtils.addOrder("select new org.squashtest.tm.service.internal.bugtracker.Pair(ex, Issue) from Execution ex join ex.issues Issue where ex.testPlan.iteration.campaign.id in (select cpe.descendantId from CampaignPathEdge cpe where cpe.ancestorId = :folderId)", sorter);

		Query query = entityManager.unwrap(Session.class).createQuery(hql).setParameter("folderId", folder.getId());
		PagingUtils.addPaging(query, sorter);

		return query.list();
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Pair<Execution, Issue>> findAllExecutionIssuePairsByTestCase(TestCase testCase, PagingAndSorting sorter) {
		String hql = SortingUtils.addOrder("select new org.squashtest.tm.service.internal.bugtracker.Pair(ex, Issue) from Execution ex join ex.issues Issue join ex.testPlan tp join tp.referencedTestCase tc where tc = :testCase", sorter);

		Query query = entityManager.unwrap(Session.class).createQuery(hql).setParameter("testCase", testCase);
		PagingUtils.addPaging(query, sorter);

		return query.list();
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<RequirementIssueSupport> findAllExecutionIssuePairsByRequirementVersions(List<RequirementVersion> requirementVersions, PagingAndSorting sorter) {
		String hql = SortingUtils.addOrder("select new org.squashtest.tm.service.internal.bugtracker.RequirementIssueSupport(rv, ex, Issue) " +
			"from Execution ex " +
			"inner join ex.issues Issue " +
			"inner join ex.referencedTestCase.requirementVersionCoverages rvc  " +
			"inner join rvc.verifiedRequirementVersion rv " +
			"where rv in (:requirementVersions)", sorter);

		Query query = entityManager.unwrap(Session.class)
			.createQuery(hql)
			.setParameterList("requirementVersions", requirementVersions);
		PagingUtils.addPaging(query, sorter);

		return query.list();
	}


	@Override
	public List<Issue> findAllByExecutionStep(ExecutionStep executionStep, PagingAndSorting sorter) {
		String hql = SortingUtils.addOrder("select Issue from ExecutionStep s join s.issueList il join il.issues Issue where s = :step", sorter);

		Query query = entityManager.unwrap(Session.class).createQuery(hql).setParameter("step", executionStep);
		PagingUtils.addPaging(query, sorter);

		return query.list();
}

	@Override
	public List<Pair<ExecutionStep, Issue>> findAllExecutionStepIssuePairsByExecution(Execution execution, PagingAndSorting sorter) {
		String hql = SortingUtils.addOrder("select new org.squashtest.tm.service.internal.bugtracker.Pair(s, Issue) from ExecutionStep s join s.issueList il join il.issues Issue join s.execution ex where ex = :execution", sorter);

		Query query = entityManager.unwrap(Session.class).createQuery(hql).setParameter("execution", execution);
		PagingUtils.addPaging(query, sorter);

		return query.list();
	}

}
