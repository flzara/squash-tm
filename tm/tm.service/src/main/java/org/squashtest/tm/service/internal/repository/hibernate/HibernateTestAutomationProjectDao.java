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

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.core.foundation.lang.Couple;
import org.squashtest.tm.domain.EntityReference;
import org.squashtest.tm.domain.EntityType;
import org.squashtest.tm.domain.campaign.QIterationTestPlanItem;
import org.squashtest.tm.domain.testautomation.QAutomatedTest;
import org.squashtest.tm.domain.testautomation.QTestAutomationProject;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.service.internal.repository.ParameterNames;
import org.squashtest.tm.service.internal.repository.TestAutomationProjectDao;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


import static org.squashtest.tm.domain.campaign.QIteration.iteration;
import static org.squashtest.tm.domain.campaign.QTestSuite.testSuite;
import static org.squashtest.tm.domain.campaign.QIterationTestPlanItem.iterationTestPlanItem;
import static org.squashtest.tm.domain.testcase.QTestCase.testCase;
import static org.squashtest.tm.domain.testautomation.QAutomatedTest.automatedTest;
import static org.squashtest.tm.domain.testautomation.QTestAutomationProject.testAutomationProject;


@Repository
public class HibernateTestAutomationProjectDao implements TestAutomationProjectDao {

	@PersistenceContext
	private EntityManager em;

	/**
	 * @see org.squashtest.tm.service.internal.repository.TestAutomationProjectDao#persist(TestAutomationProject)
	 */
	@Override
	public void persist(TestAutomationProject newProject) {
		em.persist(newProject);
	}

	/**
	 * @see org.squashtest.tm.service.internal.repository.TestAutomationProjectDao#findById(Long)
	 */
	@Override
	public TestAutomationProject findById(Long id) {
		return em.getReference(TestAutomationProject.class, id);
	}

	/**
	 * @see org.squashtest.tm.service.internal.repository.TestAutomationProjectDao#findByExample(TestAutomationProject)
	 */
	@Override
	public TestAutomationProject findByExample(TestAutomationProject example) {
		Criteria criteria = em.unwrap(Session.class).createCriteria(TestAutomationProject.class);
		criteria = criteria.add(Example.create(example));
		criteria = criteria.add(Restrictions.eq("server", example.getServer()));

		List<?> res = criteria.list();

		if (res.isEmpty()) {
			return null;
		} else if (res.size() == 1) {
			return (TestAutomationProject) res.get(0);
		} else {
			throw new NonUniqueEntityException();
		}
	}
	@Override
	public Collection<Long> findAllByTMProject(long tmProjectId) {
		Query query = em.createNamedQuery("testAutomationProject.findAllByTMPRoject");
		query.setParameter("tmProjectId", tmProjectId);
		return query.getResultList();
	}





	/**
	 * @see org.squashtest.tm.service.internal.repository.TestAutomationProjectDao#haveExecutedTestsByIds(Collection)
	 */
	@Override
	public boolean haveExecutedTestsByIds(Collection<Long> projectIds) {
		if (projectIds.isEmpty()) {
			return false;
		}

		Query q = em.createNamedQuery("testAutomationProject.haveExecutedTestsByIds");
		q.setParameter(ParameterNames.PROJECT_IDS, projectIds);
		Long count = (Long) q.getSingleResult();
		return count > 0;
	}

	/**
	 * @see org.squashtest.tm.service.internal.repository.TestAutomationProjectDao#deleteProjectsByIds(Collection)
	 */
	@Override
	public void deleteProjectsByIds(Collection<Long> projectIds) {
		if (! projectIds.isEmpty()){
			dereferenceAutomatedExecutionExtender(projectIds);
			dereferenceTestCases(projectIds);
			em.flush();
			deleteAutomatedTests(projectIds);
			deleteTestAutomationProjects(projectIds);
			em.flush();
		}
	}

	/**
	 * @see org.squashtest.tm.service.internal.repository.TestAutomationProjectDao#deleteAllHostedProjects(long)
	 */
	@Override
	public void deleteAllHostedProjects(long serverId) {
		List<Long> hostedProjectIds = findHostedProjectIds(serverId);
		deleteProjectsByIds(hostedProjectIds);
	}

	/**
	 * @see org.squashtest.tm.service.internal.repository.TestAutomationProjectDao#findAllHostedProjects(long)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<TestAutomationProject> findAllHostedProjects(long serverId) {
		Query query = em.createNamedQuery("testAutomationServer.findAllHostedProjects");
		query.setParameter(ParameterNames.SERVER_ID, serverId);
		return query.getResultList();
	}

	/**
	 * @see org.squashtest.tm.service.internal.repository.TestAutomationProjectDao#findHostedProjectIds(long)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Long> findHostedProjectIds(long serverId) {
		Query q = em.createNamedQuery("testAutomationProject.findHostedProjectIds");
		q.setParameter(ParameterNames.SERVER_ID, serverId);
		return q.getResultList();
	}



	@Override
	public List<Couple<TestAutomationProject, Long>> findAllCalledByTestPlan(EntityReference context, Collection<Long> testPlanSubset) {

		// context must be not null and reference either an iteration or a test suite.
		if (context == null || ! (context.getType() != EntityType.ITERATION || context.getType() != EntityType.TEST_SUITE)){
			throw new IllegalArgumentException("invalid context : expected a reference to an Iteration or a TestSuite, but got "+context);
		}

		EntityType type = context.getType();
		Long id = context.getId();

		// init the querydsl context
		JPAQueryFactory factory = new JPAQueryFactory(em);
		JPAQuery<Couple<TestAutomationProject, Long>> query = null;

		// a few aliases
		QIterationTestPlanItem item = iterationTestPlanItem;
		QAutomatedTest autoTest = automatedTest;
		QTestAutomationProject autoProject = testAutomationProject;

		// initialize the select clause
		query = factory.select(Projections.constructor(Couple.class, autoProject, item.count().as("itemCount")));

		// initialize the initial selected entity
		if (type == EntityType.ITERATION){
			query = query.from(iteration)
						.innerJoin(iteration.testPlans, item)
						.where(iteration.id.eq(id));
		}
		else{
			query = query.from(testSuite)
						.innerJoin(testSuite.testPlan, item)
						.where(testSuite.id.eq(id));
		}

		// if a test plan subset is defined, apply it
		// note : this is the second time we invoke where(...), hopefully it is treated as a AND condition regarding
		// the first clause, and that is what we need. Otherwise we would need to build the where clause apart.
		if (testPlanSubset != null && ! testPlanSubset.isEmpty()){
			query = query.where(item.id.in(testPlanSubset));
		}

		// the rest of the query
		query = query.innerJoin(item.referencedTestCase, testCase)
					.innerJoin(testCase.automatedTest, autoTest)
					.innerJoin(autoTest.project, autoProject)
					.groupBy(autoProject)
					.orderBy(autoProject.label.asc());

		// return
		return query.fetch();


	}

	// ************************ private stuffs **********************************

	private void dereferenceAutomatedExecutionExtender(Collection<Long> projectIds) {
		Query q = em.createNamedQuery(
				"testAutomationProject.dereferenceAutomatedExecutionExtender");
		q.setParameter(ParameterNames.PROJECT_IDS, projectIds);
		q.executeUpdate();
	}

	private void dereferenceTestCases(Collection<Long> projectIds) {
		Query q = em.createNamedQuery("testAutomationProject.dereferenceTestCases");
		q.setParameter(ParameterNames.PROJECT_IDS, projectIds);
		q.executeUpdate();
	}

	private void deleteAutomatedTests(Collection<Long> projectIds) {
		Query q = em.createNamedQuery("testAutomationProject.deleteAutomatedTests");
		q.setParameter(ParameterNames.PROJECT_IDS, projectIds);
		q.executeUpdate();
	}

	private void deleteTestAutomationProjects(Collection<Long> projectIds) {
		Query q = em.createNamedQuery("testAutomationProject.delete");
		q.setParameter(ParameterNames.PROJECT_IDS, projectIds);
		q.executeUpdate();
	}

}
