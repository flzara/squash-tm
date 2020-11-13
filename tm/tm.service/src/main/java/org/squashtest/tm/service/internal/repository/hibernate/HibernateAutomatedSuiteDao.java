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

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.core.foundation.collection.ColumnFiltering;
import org.squashtest.tm.core.foundation.collection.PagingAndMultiSorting;
import org.squashtest.tm.core.foundation.lang.Couple;
import org.squashtest.tm.domain.EntityReference;
import org.squashtest.tm.domain.EntityType;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.execution.ExecutionStatusReport;
import org.squashtest.tm.domain.testautomation.AutomatedExecutionExtender;
import org.squashtest.tm.domain.testautomation.AutomatedSuite;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.service.internal.repository.AutomatedSuiteDao;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.squashtest.tm.domain.campaign.QIteration.iteration;
import static org.squashtest.tm.domain.campaign.QIterationTestPlanItem.iterationTestPlanItem;
import static org.squashtest.tm.domain.campaign.QTestSuite.testSuite;
import static org.squashtest.tm.domain.execution.ExecutionStatus.READY;
import static org.squashtest.tm.domain.execution.ExecutionStatus.RUNNING;
import static org.squashtest.tm.domain.testautomation.QAutomatedTest.automatedTest;
import static org.squashtest.tm.domain.testautomation.QTestAutomationProject.testAutomationProject;
import static org.squashtest.tm.domain.testcase.QTestCase.testCase;

@Repository
public class HibernateAutomatedSuiteDao implements AutomatedSuiteDao {

	private static final String AUDIT = "audit";

	private static final String AUTOMATED_SUITE_COUNT_STATUS = "automatedSuite.countStatuses";

	private static final String CREATED_ON = "createdOn";

	private static final String EXECUTION = "execution";

	private static final String EXECUTION_EXTENDERS = "executionExtenders";

	private static final String ID = "id";

	private static final String ITERATION = "iteration";

	private static final String TEST_PLAN = "testPlan";

	private static final String TEST_SUITE = "testSuite";

	private static final String TEST_SUITES = "testSuites";

	private static final String UNCHECKED = "unchecked";

	private static final String STRAIGHT_JOIN_HINT = "STRAIGHT_JOIN";

	@PersistenceContext
	private EntityManager em;


	@Override
	public void delete(String id) {
		AutomatedSuite suite = findById(id);
		em.remove(suite);
	}


	@Override
	public void delete(AutomatedSuite suite) {
		em.remove(suite);
	}

	@Override
	public AutomatedSuite createNewSuite() {
		AutomatedSuite suite = new AutomatedSuite();
		em.persist(suite);
		return suite;
	}

	@Override
	public AutomatedSuite findById(String id) {
		return em.getReference(AutomatedSuite.class, id);
	}

	@SuppressWarnings(UNCHECKED)
	@Override
	public List<AutomatedSuite> findAll() {
		Query query = em.createNamedQuery("automatedSuite.findAll");
		return query.getResultList();
	}

	@SuppressWarnings(UNCHECKED)
	@Override
	public List<AutomatedSuite> findAllByIds(Collection<String> ids) {
		if (ids.isEmpty()) {
			return Collections.emptyList();
		} else {
			Query query = em.createNamedQuery("automatedSuite.findAllById");
			query.setParameter("suiteIds", ids);
			return query.getResultList();
		}
	}

	@SuppressWarnings(UNCHECKED)
	@Override
	public Collection<AutomatedExecutionExtender> findAllExtenders(String suiteId) {
		Query query = em.createNamedQuery("automatedSuite.findAllExtenders");
		query.setParameter("suiteId", suiteId);
		return query.getResultList();
	}

	@Override
	public Collection<AutomatedExecutionExtender> findAllWaitingExtenders(String suiteId) {
		return findAllExtendersByStatus(suiteId, READY);
	}

	@Override
	public Collection<AutomatedExecutionExtender> findAllRunningExtenders(String suiteId) {
		return findAllExtendersByStatus(suiteId, RUNNING);
	}

	@Override
	public Collection<AutomatedExecutionExtender> findAllCompletedExtenders(String suiteId) {
		return findAllExtendersByStatus(suiteId, ExecutionStatus.getTerminatedStatusSet());
	}

	@SuppressWarnings(UNCHECKED)
	@Override
	public Collection<AutomatedExecutionExtender> findAllExtendersByStatus(final String suiteId,
			final Collection<ExecutionStatus> statusList) {

		Query query = em.createNamedQuery("automatedSuite.findAllExtendersHavingStatus");

		query.setParameter("suiteId", suiteId);

		query.setParameter("statusList", statusList);

		return query.getResultList();
	}

	@Override
	public List<AutomatedExecutionExtender> findAndFetchForAutomatedExecutionCreation(String suiteId) {
		Query query = em.createNamedQuery("automatedSuite.fetchForAutomationExecution");
		//MariaDB optimizer makes strange join order optimization sometimes with this request, resulting in an extremely slow query.
		//Therefore we add STRAIGHT_JOIN hint to force the respect of the original join order
		org.hibernate.query.Query hibernateQuery = (org.hibernate.query.Query) query;
		hibernateQuery.setParameter("suiteId", suiteId);
		hibernateQuery.addQueryHint(STRAIGHT_JOIN_HINT);
		return hibernateQuery.getResultList();
	}

	@Override
	public List<AutomatedSuite> findAutomatedSuitesByIterationID(Long iterationId, PagingAndMultiSorting paging, ColumnFiltering filter) {

		return createQueryFindAutomatedSuites(ITERATION, iterationId, paging).getResultList();
	}


	@Override
	public List<AutomatedSuite> findAutomatedSuitesByTestSuiteID(Long suiteId, PagingAndMultiSorting paging, ColumnFiltering filter) {

		return createQueryFindAutomatedSuites(TEST_SUITE, suiteId, paging).getResultList();
	}

	private TypedQuery<AutomatedSuite> createQueryFindAutomatedSuites(String discriminatingEntityName, Long discriminatingEntityId, PagingAndMultiSorting paging) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<AutomatedSuite> criteriaQuery = builder.createQuery(AutomatedSuite.class);
		Root<AutomatedSuite> root = criteriaQuery.from(AutomatedSuite.class);

		criteriaQuery.distinct(true).select(root)
			.where(builder.or(getPredicateArray(root, discriminatingEntityName, discriminatingEntityId)))
			.orderBy(builder.desc(root.join(AUDIT).get(CREATED_ON)));

		TypedQuery<AutomatedSuite> query = em.createQuery(criteriaQuery);

		if(!paging.shouldDisplayAll()){
			query.setFirstResult(paging.getFirstItemIndex());
			query.setMaxResults(paging.getPageSize());
		}

		return query;
	}

	@Override
	public long countSuitesByIterationId(Long iterationId, ColumnFiltering filter) {

		return createQueryCountAutomatedSuites(ITERATION, iterationId).getSingleResult();
	}

	@Override
	public long countSuitesByTestSuiteId(Long suiteId, ColumnFiltering filter) {

		return createQueryCountAutomatedSuites(TEST_SUITE, suiteId).getSingleResult();
	}

	@Override
	public ExecutionStatusReport getStatusReport(String uuid) {
		ExecutionStatusReport report = new ExecutionStatusReport();

		Query query = em.createNamedQuery(
			AUTOMATED_SUITE_COUNT_STATUS);
		query.setParameter(ID, uuid);

		List<Object[]> tuples = query.getResultList();

		for (Object[] tuple:tuples) {
			report.set(((ExecutionStatus) tuple[0]).getCanonicalStatus(), ((Long) tuple[1]).intValue());
		}

		return report;
	}

	private TypedQuery<Long> createQueryCountAutomatedSuites(String discriminatingEntityName, Long discriminatingEntityId) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);
		Root<AutomatedSuite> root = criteriaQuery.from(AutomatedSuite.class);

		criteriaQuery.select(builder.countDistinct(root))
			.where(builder.or(getPredicateArray(root, discriminatingEntityName, discriminatingEntityId)));

		TypedQuery<Long> query = em.createQuery(criteriaQuery);

		return query;
	}

	private Predicate[] getPredicateArray(Root<AutomatedSuite> queryRoot, String discriminatingEntityName, Long discriminatingEntityId){

		List<Predicate> predicateList = new ArrayList<>();

		String discriminatingTestPlanAttributeName;

		if(discriminatingEntityName.equals(ITERATION)){
			discriminatingTestPlanAttributeName = ITERATION;

			Predicate isIndirectlyLinkedToDiscriminatingEntity = queryRoot.join(TEST_SUITE, JoinType.LEFT).join(ITERATION, JoinType.LEFT).get(ID).in(discriminatingEntityId);
			predicateList.add(isIndirectlyLinkedToDiscriminatingEntity);
		} else {
			discriminatingTestPlanAttributeName = TEST_SUITES;
		}

		Predicate hasExecutionsLinkedToDiscriminatingEntity = queryRoot.join(EXECUTION_EXTENDERS, JoinType.LEFT)
			.join(EXECUTION, JoinType.LEFT)
			.join(TEST_PLAN, JoinType.LEFT)
			.join(discriminatingTestPlanAttributeName, JoinType.LEFT)
			.get(ID).in(discriminatingEntityId);

		Predicate isDirectlyLinkedToDiscriminatingEntity = queryRoot.join(discriminatingEntityName, JoinType.LEFT).get(ID).in(discriminatingEntityId);

		predicateList.add(hasExecutionsLinkedToDiscriminatingEntity);
		predicateList.add(isDirectlyLinkedToDiscriminatingEntity);

		return predicateList.toArray(new Predicate[0]);
	}

	// TODO : either make it private (core Squash at least doesn't call it anywhere but here), either declare it in the interface
	public Collection<AutomatedExecutionExtender> findAllExtendersByStatus(String suiteId,
			ExecutionStatus... statusArray) {
		Collection<ExecutionStatus> statusList = Arrays.asList(statusArray);
		return findAllExtendersByStatus(suiteId, statusList);

	}




	@Override
	public List<Couple<TestAutomationProject, Long>> findAllCalledByTestPlan(EntityReference context, Collection<Long> testPlanSubset) {

		// init the query
		JPAQuery<Couple<TestAutomationProject, Long>> query = createBaseTestplanQueryFromSpec(context, testPlanSubset);

		// now set the select clause
		query = query.select(Projections.constructor(Couple.class, testAutomationProject, iterationTestPlanItem.count().as("itemCount")));

		// and the group by, order etc
		query = query.groupBy(testAutomationProject)
					.orderBy(testAutomationProject.label.asc());

		// return
		return query.fetch();

	}

	@Override
	public List<String> findTestPathForAutomatedSuiteAndProject(EntityReference context, Collection<Long> testPlanSubset, long automationProjectId) {
		// init the query
		JPAQuery<String> query = createBaseTestplanQueryFromSpec(context, testPlanSubset);

		// select clause
		query = query.select(testAutomationProject.label.concat("/").concat(automatedTest.name).as("path"));

		// another where clause
		query = query.where(testAutomationProject.id.eq(automationProjectId));

		// order by
		query.orderBy(automatedTest.name.asc());

		return query.fetch();

	}


	/*
		Private function used by findAllCalledByTestPlan and findTestPathForAutomatedSuiteAndProject.
		This function will create a headless base query that will care neither of the select clause nor group by etc.
		The caller will do whatever it needs with the result.
	 */
	private <T> JPAQuery<T> createBaseTestplanQueryFromSpec(EntityReference context, Collection<Long> testPlanSubset){
		// context must be not null and reference either an iteration or a test suite.
		if (context == null || ! (context.getType() != EntityType.ITERATION || context.getType() != EntityType.TEST_SUITE)){
			throw new IllegalArgumentException("invalid context : expected a reference to an Iteration or a TestSuite, but got "+context);
		}

		EntityType type = context.getType();
		Long id = context.getId();

		// init the querydsl context
		JPAQueryFactory factory = new JPAQueryFactory(em);
		JPAQuery<T> query = null;

		// initialize the initial selected entity
		if (type == EntityType.ITERATION){
			query = (JPAQuery<T>) factory.from(iteration)
									  .innerJoin(iteration.testPlans, iterationTestPlanItem)
									  .where(iteration.id.eq(id));
		}
		else{
			query = (JPAQuery<T>) factory.from(testSuite)
									  .innerJoin(testSuite.testPlan, iterationTestPlanItem)
									  .where(testSuite.id.eq(id));
		}

		// if a test plan subset is defined, apply it
		// note : this is the second time we invoke where(...), hopefully it is treated as a AND condition regarding
		// the first clause, and that is what we need. Otherwise we would need to build the where clause apart.
		if (testPlanSubset != null && ! testPlanSubset.isEmpty()){
			query = query.where(iterationTestPlanItem.id.in(testPlanSubset));
		}

		// the rest of the query
		query = query.innerJoin(iterationTestPlanItem.referencedTestCase, testCase)
					.innerJoin(testCase.automatedTest, automatedTest)
					.innerJoin(automatedTest.project, testAutomationProject);

		return query;

	}

	@Override
	public List<String> getOldAutomatedSuiteIds(LocalDateTime limitDateTime) {
		Instant limitInstant = limitDateTime.atZone(ZoneId.systemDefault()).toInstant();
		Date limitDate = Date.from(limitInstant);

		Query fetchQuery = em.createNamedQuery("AutomatedSuite.findOldAutomatedSuiteIds");
		fetchQuery.setParameter("limitDate", limitDate);
		List<String> oldAutomatedSuites = fetchQuery.getResultList();
		return oldAutomatedSuites;
	}

	@Override
	public void deleteAllByIds(List<String> automatedSuiteIds) {
		if (automatedSuiteIds.isEmpty()) {
			return;
		}
		Query deleteQuery = em.createNamedQuery("AutomatedSuite.deleteAllByIds");
		deleteQuery.setParameter("automatedSuiteIds", automatedSuiteIds);
		deleteQuery.executeUpdate();
	}
}
