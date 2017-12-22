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

import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.core.foundation.collection.*;
import org.squashtest.tm.domain.campaign.*;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.testcase.TestCaseExecutionMode;
import org.squashtest.tm.domain.testcase.TestCaseExecutionStatus;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.service.campaign.IndexedIterationTestPlanItem;
import org.squashtest.tm.service.internal.foundation.collection.JpaPagingUtils;
import org.squashtest.tm.service.internal.foundation.collection.SortingUtils;
import org.squashtest.tm.service.internal.repository.IterationDao;
import org.squashtest.tm.service.internal.repository.ParameterNames;

import javax.persistence.Query;
import java.util.*;
import java.util.Map.Entry;

@Repository
public class HibernateIterationDao extends HibernateEntityDao<Iteration> implements IterationDao {
	private static final Logger LOGGER = LoggerFactory.getLogger(HibernateIterationDao.class);

	/*
	 * Because it is impossible to sort over the indices of ordered collection in a criteria query we must then build an
	 * hql string which will let us do that.
	 */
	private static final String HQL_INDEXED_TEST_PLAN_TEMPLATE_START = "select index(IterationTestPlanItem), " +
		"IterationTestPlanItem, " +
		"coalesce(group_concat(TestSuite.name, 'order by', TestSuite.name), '') as suitenames, " +
		"(select min(m.endDate) from IterationTestPlanItem itpi " +
		"left join itpi.referencedTestCase ctc left join ctc.milestones m where itpi.id = IterationTestPlanItem.id) as endDate "
		+ "from Iteration as Iteration inner join Iteration.testPlans as IterationTestPlanItem "
		+ "left outer join IterationTestPlanItem.referencedTestCase as TestCase "
		+ "left outer join TestCase.project as Project "
		+ "left outer join IterationTestPlanItem.referencedDataset as Dataset "
		+ "left outer join IterationTestPlanItem.user as User "
		+ "left outer join IterationTestPlanItem.testSuites as TestSuite "
		+ "where Iteration.id = :iterationId {whereClause} ";

	/*
	 * note : group by Iteration, ITPI is broken : produces `GROUP BY ITERATION.ID, null` SQL (HHH-1615)
	 * note : we have to group by Iteration.id *and* ITPI.iteration.id, otherwise a group by clause on the join table is
	 * missing. This may be a side effect of Iteration<->ITPI mapping : because of issue HHH-TBD, this is mapped as 2
	 * unidirectional associations instead of 1 bidi association.
	 */
	private static final String HQL_INDEXED_TEST_PLAN_TEMPLATE_END = "group by IterationTestPlanItem.iteration.id, IterationTestPlanItem.id, Iteration.id, index(IterationTestPlanItem) ";
	private static final String HQL_INDEXED_TEST_PLAN_TESTSUITE_FILTER = " having group_concat(TestSuite.name, 'order by', TestSuite.name) like :testsuiteFilter ";

	/*
	 * the following collection will forbid group by on certain columns
	 * because Hibernate would not call those columns as we asked it to.
	 */
	private static final Collection<String> HQL_NO_GROUP_BY_COLUMNS = Arrays.asList("suitenames", "endDate");
	/**
	 * HQL query which looks up the whole iteration test plan
	 */
	private final String hqlFullIndexedTestPlan = HQL_INDEXED_TEST_PLAN_TEMPLATE_START.replace("{whereClause}", "");
	/**
	 * HQL query which looks up the test plan assigned to a given user
	 */
	private final String hqlUserFilteredIndexedTestPlan = HQL_INDEXED_TEST_PLAN_TEMPLATE_START.replace("{whereClause}",
		"and User.login = :userLogin ");


	private static final Map<String, Map<String, String>> VALUE_DEPENDENT_FILTER_CLAUSES = new HashMap<>();
	private static final String VDFC_DEFAULT_KEY = "VDFC_DEFAULT_KEY";

	static {
		Map<String, String> modeDataMap = new HashMap<>(2);
		modeDataMap.put(TestCaseExecutionMode.MANUAL.name(),
			TestPlanFilteringHelper.HQL_INDEXED_TEST_PLAN_MODEMANUAL_FILTER);
		modeDataMap.put(VDFC_DEFAULT_KEY, TestPlanFilteringHelper.HQL_INDEXED_TEST_PLAN_MODEAUTO_FILTER);
		VALUE_DEPENDENT_FILTER_CLAUSES.put(TestPlanFilteringHelper.MODE_DATA, modeDataMap);

		Map<String, String> userData = new HashMap<>(2);
		userData.put("0", TestPlanFilteringHelper.HQL_INDEXED_TEST_PLAN_NULL_USER_FILTER);
		userData.put(VDFC_DEFAULT_KEY, TestPlanFilteringHelper.HQL_INDEXED_TEST_PLAN_USER_FILTER);
		VALUE_DEPENDENT_FILTER_CLAUSES.put(TestPlanFilteringHelper.USER_DATA, userData);

	}

	@Override
	public List<Iteration> findAllByCampaignId(long campaignId) {

		return executeListNamedQuery("iterationDao.findAllByCampaignId", new SetIdParameter(ParameterNames.CAMPAIGN_ID, campaignId));
	}

	@Override
	public List<Iteration> findAllIterationContainingTestCase(long testCaseId) {
		return executeListNamedQuery("iterationDao.findAllIterationContainingTestCase", new SetIdParameter(
			"testCaseId", testCaseId));
	}

	/*
	 * as long as the ordering of a collection is managed by @OrderColumn, but you can't explicitly reference the
	 * ordering column in the join table, initialize the collection itself is the only solution
	 *
	 * (non-Javadoc)
	 *
	 * @see org.squashtest.csp.tm.internal.repository.IterationDao#findOrderedExecutionsByIterationId(long)
	 */
	@Override
	public Iteration findAndInit(long iterationId) {
		Iteration iteration = findById(iterationId);
		Hibernate.initialize(iteration.getExecutions());
		return iteration;
	}

	@Override
	public void removeFromCampaign(Iteration iteration) {

		Campaign campaign = findCampaignByIterationId(iteration.getId());

		if (campaign == null) {
			return;
		}

		ListIterator<Iteration> iterator = campaign.getIterations().listIterator();
		while (iterator.hasNext()) {
			Iteration ts = iterator.next();
			if (ts.getId().equals(iteration.getId())) {
				iterator.remove();
				break;
			}
		}

	}

	/*
	 * Returns a Campaign if it contains an Iteration with the provided Id Returns null otherwise
	 *
	 * Note : as long as the relation between Campaign and Iteration is OneToMany, there will be either 0 either 1
	 * results, no more.
	 */
	@SuppressWarnings("unchecked")
	private Campaign findCampaignByIterationId(Long iterationId) {
		Session session = currentSession();

		List<Campaign> tcList = session.createCriteria(Campaign.class).createCriteria("iterations")
			.add(Restrictions.eq("id", iterationId)).list();

		if (!tcList.isEmpty()) {
			Campaign ca = tcList.get(0);
			Hibernate.initialize(ca.getIterations());
			return ca;
		} else {
			return null;
		}
	}

	@Override
	public List<Execution> findOrderedExecutionsByIterationId(long iterationId) {
		return findAllByIterationId("iteration.findAllExecutions", iterationId);
	}

	@Override
	public List<Execution> findOrderedExecutionsByIterationAndTestPlan(final long iterationId, final long testPlanId) {
		return entityManager.createNamedQuery("iteration.findAllExecutionsByTestPlan")
			.setParameter(ParameterNames.ITERATION_ID, iterationId)
			.setParameter("testPlanId", testPlanId)
			.getResultList();
	}

	@Override
	public List<TestSuite> findAllTestSuites(final long iterationId) {
		return findAllByIterationId("iteration.findAllTestSuites", iterationId);
	}

	private <R> List<R> findAllByIterationId(String queryName, long iterationId) {
		return entityManager.createNamedQuery(queryName)
			.setParameter(ParameterNames.ITERATION_ID, iterationId)
			.getResultList();
	}

	private <R> R findByIterationId(String queryName, long iterationId) {
		return (R) entityManager.createNamedQuery(queryName)
			.setParameter(ParameterNames.ITERATION_ID, iterationId)
			.getSingleResult();
	}


	/**
	 * <p>Will persist a new Iteration, if its test plan contains transient test plan items they will be persisted too.</p>
	 * <p>
	 * Deprecation notice : As of TM 1.15 the simpler method {@link #persist(org.​squashtest.​tm.​domain.​campaign.Iteration) will just
	 * do the same.
	 * </p>
	 *
	 * @param iteration
	 * @deprecated
	 */
	@Override
	@Deprecated
	public void persistIterationAndTestPlan(Iteration iteration) {
		persist(iteration);
	}


	@Override
	public List<Execution> findAllExecutionByIterationId(long iterationId) {
		return findAllByIterationId("iteration.findAllExecutions", iterationId);
	}

	@Override
	@SuppressWarnings("unchecked")
	public TestPlanStatistics getIterationStatistics(long iterationId) {
		List<Object[]> result = findAllByIterationId("iteration.countStatuses", iterationId);

		return new TestPlanStatistics(result);
	}


	@Override
	public long countRunningOrDoneExecutions(long iterationId) {
		return findByIterationId("iteration.countRunningOrDoneExecutions", iterationId);
	}

	// **************************** TEST PLAN ******************************

	@Override
	public List<IterationTestPlanItem> findTestPlan(long iterationId, PagingAndMultiSorting sorting,
													Filtering filtering, ColumnFiltering columnFiltering) {

		// get the data
		List<Object[]> tuples = findIndexedTestPlanData(iterationId, sorting, filtering, columnFiltering);

		// filter them
		List<IterationTestPlanItem> items = new ArrayList<>(tuples.size());

		for (Object[] tuple : tuples) {
			IterationTestPlanItem itpi = (IterationTestPlanItem) tuple[1];
			items.add(itpi);
		}

		return items;
	}

	@Override
	public List<IndexedIterationTestPlanItem> findIndexedTestPlan(long iterationId, PagingAndSorting sorting,
																  Filtering filtering, ColumnFiltering columnFiltering) {
		return findIndexedTestPlan(iterationId, new SingleToMultiSortingAdapter(sorting), filtering, columnFiltering);
	}

	@Override
	public List<IndexedIterationTestPlanItem> findIndexedTestPlan(final long iterationId,
																  PagingAndMultiSorting sorting, Filtering filtering, ColumnFiltering columnFiltering) {

		/* get the data */
		List<Object[]> tuples = findIndexedTestPlanData(iterationId, sorting, filtering, columnFiltering);

		/* format them */
		List<IndexedIterationTestPlanItem> indexedItems = new ArrayList<>(tuples.size());

		for (Object[] tuple : tuples) {
			Integer index = (Integer) tuple[0];
			IterationTestPlanItem itpi = (IterationTestPlanItem) tuple[1];
			indexedItems.add(new IndexedIterationTestPlanItem(index, itpi));
		}

		return indexedItems;

	}


	private StringBuilder buildTestPlanQueryBody(Filtering filtering, ColumnFiltering columnFiltering,
												 MultiSorting multiSorting) {
		StringBuilder hqlBuilder = new StringBuilder();

		String hql = filtering.isDefined() ? hqlUserFilteredIndexedTestPlan : hqlFullIndexedTestPlan;
		hqlBuilder.append(hql);

		// additional where clauses
		TestPlanFilteringHelper.appendFilteringRestrictions(hqlBuilder, columnFiltering);

		for (Entry<String, Map<String, String>> valueDependantFilterClause : VALUE_DEPENDENT_FILTER_CLAUSES.entrySet()) {
			String filterName = valueDependantFilterClause.getKey();
			Map<String, String> clausesByValues = valueDependantFilterClause.getValue();
			if (columnFiltering.hasFilter(filterName)) {
				String filterValue = columnFiltering.getFilter(filterName);
				String clause = clausesByValues.get(filterValue);
				if (clause == null) {
					clause = clausesByValues.get(VDFC_DEFAULT_KEY);
				}
				hqlBuilder.append(clause);
			}
		}

		// group by
		hqlBuilder.append(HQL_INDEXED_TEST_PLAN_TEMPLATE_END);

		// Strict SQL (postgres) : sort colums have to appear in group by clause.
		for (Sorting sorting : multiSorting.getSortings()) {
			if (!HQL_NO_GROUP_BY_COLUMNS.contains(sorting.getSortedAttribute())) {
				hqlBuilder.append(", ").append(sorting.getSortedAttribute());
			}
		}

		if (columnFiltering.hasFilter(TestPlanFilteringHelper.TESTSUITE_DATA)) {
			hqlBuilder.append(HQL_INDEXED_TEST_PLAN_TESTSUITE_FILTER);
		}

		return hqlBuilder;
	}

	private String buildIndexedTestPlanQueryString(PagingAndMultiSorting sorting, Filtering filtering,
												   ColumnFiltering columnFiltering) {

		StringBuilder hqlbuilder = buildTestPlanQueryBody(filtering, columnFiltering, sorting);

		// tune the sorting to make hql happy
		LevelImplementorSorter wrapper = new LevelImplementorSorter(sorting);
		wrapper.map("TestCase.importance", TestCaseImportance.class);
		wrapper.map("IterationTestPlanItem.executionStatus", ExecutionStatus.class);

		return SortingUtils.addOrder(hqlbuilder.toString(), wrapper);
	}

	// this method will use one or another strategy to fetch its data depending on what the user is requesting.
	@SuppressWarnings("unchecked")
	private List<Object[]> findIndexedTestPlanData(final long iterationId, PagingAndMultiSorting sorting,
												   Filtering filtering, ColumnFiltering columnFiltering) {

		String queryString = buildIndexedTestPlanQueryString(sorting, filtering, columnFiltering);

		Query query = assignParameterValuesToTestPlanQuery(queryString, iterationId, filtering, columnFiltering);

		JpaPagingUtils.addPaging(query, sorting);

		return query.getResultList();
	}

	@Override
	public long countTestPlans(Long iterationId, Filtering filtering) {
		if (!filtering.isDefined()) {
			return (Long) findByIterationId("iteration.countTestPlans", iterationId);
		} else {
			return (Long) entityManager.createNamedQuery("iteration.countTestPlansFiltered")
				.setParameter(ParameterNames.ITERATION_ID, iterationId)
				.setParameter("userLogin", filtering.getFilter())
				.getSingleResult();
		}
	}

	private Query assignParameterValuesToTestPlanQuery(String queryString, Long iterationId, Filtering filtering,
													   ColumnFiltering columnFiltering) {
		Query query = entityManager.createQuery(queryString);
		query.setParameter(ParameterNames.ITERATION_ID, iterationId);
		TestPlanFilteringHelper.setFilters(query, filtering, columnFiltering);

		return query;
	}

	@Override
	public long countTestPlans(Long iterationId, Filtering filtering, ColumnFiltering columnFiltering) {

		StringBuilder hqlbuilder = buildTestPlanQueryBody(filtering, columnFiltering, new MultiSorting() {

			@Override
			public List<Sorting> getSortings() {
				return Collections.emptyList();
			}
		});

		Query query = assignParameterValuesToTestPlanQuery(hqlbuilder.toString(), iterationId, filtering,
			columnFiltering);

		// TODO this should probably be a count query for better performance. otherwise explain why not possible in comment
		return query.getResultList().size();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<TestCaseExecutionStatus> findExecStatusForIterationsAndTestCases(List<Long> testCasesIds, List<Long> iterationsIds) {

		if (testCasesIds.isEmpty()) {
			return Collections.emptyList();
		}

		List<Object[]> results = entityManager.createNamedQuery("iteration.findITPIByTestCaseGroupByStatus")
			.setParameter("testCasesIds", testCasesIds)
			.setParameter("iterationsIds", iterationsIds)
			.getResultList();

		List<TestCaseExecutionStatus> formatedResult = new ArrayList<>(results.size());

		for (Object[] result : results) {
			formatedResult.add(new TestCaseExecutionStatus((ExecutionStatus) result[0], (Long) result[1]));
		}
		return formatedResult;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Long> findVerifiedTcIdsInIterations(List<Long> testCasesIds, List<Long> iterationIds) {
		return findAllByTestCasesAndIterations("iteration.findVerifiedTcIdsInIterations", testCasesIds, iterationIds);
	}

	private <R> List<R> findAllByTestCasesAndIterations(String queryName, List<Long> testCaseIds, List<Long> iterationIds) {
		if (testCaseIds.isEmpty()) {
			return Collections.emptyList();
		}

		return entityManager.createNamedQuery(queryName)
			.setParameter("testCasesIds", testCaseIds)
			.setParameter("iterationsIds", iterationIds)
			.getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Long> findVerifiedTcIdsInIterationsWithExecution(List<Long> tcIds, List<Long> iterationsIds) {
		return findAllByTestCasesAndIterations("iteration.findVerifiedAndExecutedTcIdsInIterations", tcIds, iterationsIds);
	}

	@SuppressWarnings("unchecked")
	@Override
	public MultiMap findVerifiedITPI(List<Long> tcIds, List<Long> iterationsIds) {
		List<Object[]> itpis = findAllByTestCasesAndIterations("iteration.findITPIByTestCaseGroupByStatus", tcIds, iterationsIds);
		MultiMap result = new MultiValueMap();

		for (Object[] itpi : itpis) {
			TestCaseExecutionStatus tcStatus = new TestCaseExecutionStatus((ExecutionStatus) itpi[0], (Long) itpi[1]);
			result.put(tcStatus.getTestCaseId(), tcStatus);
		}

		return result;
	}

}
