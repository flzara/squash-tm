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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.core.foundation.collection.ColumnFiltering;
import org.squashtest.tm.core.foundation.collection.PagingAndMultiSorting;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.SingleToMultiSortingAdapter;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.CampaignLibraryNode;
import org.squashtest.tm.domain.campaign.CampaignTestPlanItem;
import org.squashtest.tm.domain.campaign.TestPlanStatistics;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.testcase.TestCaseExecutionMode;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.service.campaign.IndexedCampaignTestPlanItem;
import org.squashtest.tm.service.internal.foundation.collection.JpaPagingUtils;
import org.squashtest.tm.service.internal.foundation.collection.PagingUtils;
import org.squashtest.tm.service.internal.foundation.collection.SortingUtils;
import org.squashtest.tm.service.internal.repository.CampaignDao;
import org.squashtest.tm.service.internal.repository.ParameterNames;

@Repository
public class HibernateCampaignDao extends HibernateEntityDao<Campaign> implements CampaignDao {

	private static final String CONTAINER_ID = "containerId";
	private static final String PROJECT_FILTER = "projectFilter";
	private static final String REFERENCE_FILTER = "referenceFilter";
	private static final String TESTCASE_FILTER = "testcaseFilter";
	private static final String USER_FILTER = "userFilter";
	private static final String WEIGHT_FILTER = "weightFilter";
	private static final String DATASET_FILTER = "datasetFilter";

	private static final String PROJECT_DATA = "project-name";
	private static final String REFERENCE_DATA = "reference";
	private static final String TESTCASE_DATA = "tc-name";
	private static final String USER_DATA = "assigned-user";
	private static final String WEIGHT_DATA = "importance";
	private static final String MODE_DATA = "exec-mode";
	private static final String DATASET_DATA = "dataset.selected.name";

	/*
	 * Because it is impossible to sort over the indices of ordered collection in a criteria query
	 * we must then build an hql string which will let us do that.
	 */
	private static final String HQL_INDEXED_TEST_PLAN =
			"select index(CampaignTestPlanItem), CampaignTestPlanItem, " +
					"(select min(m.endDate) from CampaignTestPlanItem ctpi " +
					"left join ctpi.referencedTestCase ctc left join ctc.milestones m where ctpi.id = CampaignTestPlanItem.id) as endDate "+
					"from Campaign as Campaign inner join Campaign.testPlan as CampaignTestPlanItem "+
					"left outer join CampaignTestPlanItem.referencedTestCase as TestCase " +
					"left outer join CampaignTestPlanItem.referencedDataset as Dataset " +
					"left outer join TestCase.project as Project " +
					"left outer join CampaignTestPlanItem.user as User "+
					"where Campaign.id = :campaignId ";

	private static final String HQL_INDEXED_TEST_PLAN_PROJECT_FILTER = "and Project.name like :projectFilter ";

	private static final String HQL_INDEXED_TEST_PLAN_REFERENCE_FILTER = "and TestCase.reference like :referenceFilter ";

	private static final String HQL_INDEXED_TEST_PLAN_TESTCASE_FILTER = "and TestCase.name like :testcaseFilter ";

	private static final String HQL_INDEXED_TEST_PLAN_USER_FILTER = "and CampaignTestPlanItem.user.id = :userFilter ";

	private static final String HQL_INDEXED_TEST_PLAN_NULL_USER_FILTER = "and CampaignTestPlanItem.user is null ";

	private static final String HQL_INDEXED_TEST_PLAN_WEIGHT_FILTER = "and TestCase.importance = :weightFilter ";

	private static final String HQL_INDEXED_TEST_PLAN_MODEAUTO_FILTER = "and TestCase.automatedTest is not null ";

	private static final String HQL_INDEXED_TEST_PLAN_MODEMANUAL_FILTER = "and TestCase.automatedTest is null ";

	private static final String HQL_INDEXED_TEST_PLAN_DATASET_FILTER = "and Dataset.name like :datasetFilter ";

	@Override
	public Campaign findByIdWithInitializedIterations(long campaignId) {
		Campaign c = findById(campaignId);
		Hibernate.initialize(c.getIterations());
		return c;
	}



	@Override
	public List<Long> findAllCampaignIdsByLibraries(Collection<Long> libraryIds) {
		if (! libraryIds.isEmpty()){
			Query query = currentSession().getNamedQuery("campaign.findAllCampaignIdsByLibraries");
			query.setParameterList("libraryIds", libraryIds, LongType.INSTANCE);
			return query.list();
		}
		else{
			return Collections.emptyList();
		}
	}

	@Override
	public List<Long> findAllCampaignIdsByNodeIds(Collection<Long> nodeIds) {
		if (! nodeIds.isEmpty()){
			Query query = currentSession().getNamedQuery("campaign.findAllCampaignIdsByNodeIds");
			query.setParameterList("nodeIds", nodeIds, LongType.INSTANCE);
			return query.list();		}
		else{
			return Collections.emptyList();
		}
	}

	@Override
	public List<Long> filterByMilestone(Collection<Long> campaignIds, Long milestoneId) {
		List<Long> res;

		if (milestoneId == null){
			res  = new ArrayList(campaignIds);
		}
		else if (! campaignIds.isEmpty()){
			Query query = currentSession().getNamedQuery("campaign.filterByMilestone");
			query.setParameterList("campaignIds", campaignIds, LongType.INSTANCE);
			query.setParameter("milestoneId", milestoneId, LongType.INSTANCE);
			res = query.list();
		}
		else {
			res = Collections.emptyList();
		}

		return res;
	}

	@Override
	public List<Long> findAllIdsByMilestone(Long milestoneId) {

		if (milestoneId != null){
			Query query = currentSession().getNamedQuery("campaign.findAllIdsByMilestoneId");
			query.setParameter("milestoneId", milestoneId, LongType.INSTANCE);
			return query.list();
		}
		else{
			throw new IllegalArgumentException("milestoneId should not be null");
		}
	}


	@Override
	public List<CampaignTestPlanItem> findAllTestPlanByIdFiltered(final long campaignId, final PagingAndSorting filter) {
		javax.persistence.Query q = entityManager.createNamedQuery("campaign.findTestPlanFiltered")
			.setParameter(ParameterNames.CAMPAIGN_ID, campaignId);
		JpaPagingUtils.addPaging(q, filter);
		return q.getResultList();

	}

	@Override
	public List<CampaignTestPlanItem> findTestPlan(long campaignId, PagingAndMultiSorting sorting) {
		List<Object[]> tuples = findIndexedTestPlanAsTuples(campaignId, sorting);
		return buildItems(tuples);
	}

	@Override
	public List<IndexedCampaignTestPlanItem> findIndexedTestPlan(long campaignId, PagingAndMultiSorting sorting) {
		List<Object[]> tuples = findIndexedTestPlanAsTuples(campaignId, sorting);
		return buildIndexedItems(tuples);
	}

	@Override
	public List<IndexedCampaignTestPlanItem> findIndexedTestPlan(long campaignId, PagingAndSorting sorting) {
		return findIndexedTestPlan(campaignId, new SingleToMultiSortingAdapter(sorting));
	}


	@Override
	public List<IndexedCampaignTestPlanItem> findFilteredIndexedTestPlan(long campaignId,
			PagingAndMultiSorting sorting, ColumnFiltering filtering) {
		List<Object[]> tuples = findIndexedTestPlanAsTuples(campaignId, sorting, filtering);
		return buildIndexedItems(tuples);
	}


	@SuppressWarnings("unchecked")
	private List<Object[]> findIndexedTestPlanAsTuples(final long campaignId, PagingAndMultiSorting sorting) {

		StringBuilder hqlbuilder = new StringBuilder(HQL_INDEXED_TEST_PLAN);

		// tune the sorting to make hql happy
		LevelImplementorSorter wrapper = new LevelImplementorSorter(sorting);
		wrapper.map("TestCase.importance", TestCaseImportance.class);

		SortingUtils.addOrder(hqlbuilder, wrapper);

		Query query = currentSession().createQuery(hqlbuilder.toString());

		query.setParameter(ParameterNames.CAMPAIGN_ID, campaignId, LongType.INSTANCE);

		PagingUtils.addPaging(query, sorting);

		return query.list();
	}

	private String buildIndexedTestPlanQueryString(PagingAndMultiSorting sorting, ColumnFiltering filtering) {

		StringBuilder hqlbuilder = buildIndexedTestPlanQueryBody(filtering);

		// tune the sorting to make hql happy
		LevelImplementorSorter wrapper = new LevelImplementorSorter(sorting);
		wrapper.map("TestCase.importance", TestCaseImportance.class);

		SortingUtils.addOrder(hqlbuilder, wrapper);

		return hqlbuilder.toString();
	}

	private String buildIndexedTestPlanQueryStringWithoutSorting(ColumnFiltering filtering) {

		StringBuilder hqlbuilder = buildIndexedTestPlanQueryBody(filtering);

		return hqlbuilder.toString();
	}

	private StringBuilder buildIndexedTestPlanQueryBody(ColumnFiltering filtering) {
		StringBuilder hqlbuilder = new StringBuilder(HQL_INDEXED_TEST_PLAN);

		applySimpleFilter(hqlbuilder, filtering, PROJECT_DATA, HQL_INDEXED_TEST_PLAN_PROJECT_FILTER);

		if (filtering.hasFilter(MODE_DATA)) {
			if (TestCaseExecutionMode.MANUAL.name().equals(filtering.getFilter(MODE_DATA))) {
				hqlbuilder.append(HQL_INDEXED_TEST_PLAN_MODEMANUAL_FILTER);
			} else {
				hqlbuilder.append(HQL_INDEXED_TEST_PLAN_MODEAUTO_FILTER);
			}
		}

		applySimpleFilter(hqlbuilder, filtering, REFERENCE_DATA, HQL_INDEXED_TEST_PLAN_REFERENCE_FILTER);

		applySimpleFilter(hqlbuilder, filtering, TESTCASE_DATA, HQL_INDEXED_TEST_PLAN_TESTCASE_FILTER);

		applySimpleFilter(hqlbuilder, filtering, DATASET_DATA, HQL_INDEXED_TEST_PLAN_DATASET_FILTER);

		if (filtering.hasFilter(USER_DATA)) {
			if ("0".equals(filtering.getFilter(USER_DATA))) {
				hqlbuilder.append(HQL_INDEXED_TEST_PLAN_NULL_USER_FILTER);
			} else {
				hqlbuilder.append(HQL_INDEXED_TEST_PLAN_USER_FILTER);
			}
		}

		applySimpleFilter(hqlbuilder, filtering, WEIGHT_DATA, HQL_INDEXED_TEST_PLAN_WEIGHT_FILTER);

		return hqlbuilder;
	}


	private void applySimpleFilter(StringBuilder hqlbuilder, ColumnFiltering filtering, String propertyName, String hqlWhereClause){
		if (filtering.hasFilter(propertyName)){
			hqlbuilder.append(hqlWhereClause);
		}
	}




	@SuppressWarnings("unchecked")
	private List<Object[]> findIndexedTestPlanAsTuples(final long campaignId, PagingAndMultiSorting sorting,
			ColumnFiltering filtering) {

		String queryString = buildIndexedTestPlanQueryString(sorting, filtering);

		Query query = assignParameterValuesToTestPlanQuery(queryString, campaignId, filtering);

		PagingUtils.addPaging(query, sorting);

		return query.list();
	}

	private Query assignParameterValuesToTestPlanQuery(String queryString, long campaignId, ColumnFiltering filtering) {

		Query query = currentSession().createQuery(queryString);

		query.setParameter(ParameterNames.CAMPAIGN_ID, campaignId, LongType.INSTANCE);

		if (filtering.hasFilter(PROJECT_DATA)) {
			query.setParameter(PROJECT_FILTER, "%" + filtering.getFilter(PROJECT_DATA) + "%", StringType.INSTANCE);
		}
		if (filtering.hasFilter(REFERENCE_DATA)) {
			query.setParameter(REFERENCE_FILTER, "%" + filtering.getFilter(REFERENCE_DATA) + "%", StringType.INSTANCE);
		}
		if (filtering.hasFilter(TESTCASE_DATA)) {
			query.setParameter(TESTCASE_FILTER, "%" + filtering.getFilter(TESTCASE_DATA) + "%", StringType.INSTANCE);
		}
		if (filtering.hasFilter(DATASET_DATA)) {
			query.setParameter(DATASET_FILTER, "%" + filtering.getFilter(DATASET_DATA) + "%", StringType.INSTANCE);
		}
		if (filtering.hasFilter(USER_DATA) && !"0".equals(filtering.getFilter(USER_DATA))) {
			query.setParameter(USER_FILTER, Long.parseLong(filtering.getFilter(USER_DATA)), LongType.INSTANCE);
		}
		if (filtering.hasFilter(WEIGHT_DATA)) {
			query.setParameter(WEIGHT_FILTER, filtering.getFilter(WEIGHT_DATA), StringType.INSTANCE);
		}

		return query;
	}




	@Override
	public long countFilteredTestPlanById(long campaignId, ColumnFiltering filtering) {

		String queryString = buildIndexedTestPlanQueryStringWithoutSorting(filtering);

		Query query = assignParameterValuesToTestPlanQuery(queryString, campaignId, filtering);

		return query.list().size();
	}

	@Override
	public long countTestPlanById(long campaignId) {
		return (Long) executeEntityNamedQuery("campaign.countTestCasesById", idParameter(campaignId));
	}

	@Override
	public int countIterations(long campaignId) {
		Long count = executeEntityNamedQuery("campaign.countIterations", idParameter(campaignId));
		return count.intValue();
	}


	private SetQueryParametersCallback idParameter(long campaignId) {
		return new SetIdParameter(ParameterNames.CAMPAIGN_ID, campaignId);
	}

	@Override
	public List<String> findNamesInFolderStartingWith(final long folderId, final String nameStart) {
		SetQueryParametersCallback newCallBack1 = new ContainerIdNameStartParameterCallback(folderId, nameStart);
		return executeListNamedQuery("campaign.findNamesInFolderStartingWith", newCallBack1);
	}

	@Override
	public List<String> findNamesInCampaignStartingWith(final long campaignId, final String nameStart) {
		SetQueryParametersCallback newCallBack1 = new ContainerIdNameStartParameterCallback(campaignId, nameStart);
		return executeListNamedQuery("campaign.findNamesInCampaignStartingWith", newCallBack1);
	}

	@Override
	public List<String> findAllNamesInCampaign(final long campaignId) {
		SetQueryParametersCallback newCallBack1 = new SetQueryParametersCallback() {

			@Override
			public void setQueryParameters(Query query) {
				query.setParameter(CONTAINER_ID, campaignId);
			}
		};
		return executeListNamedQuery("campaign.findAllNamesInCampaign", newCallBack1);
	}

	@Override
	public List<String> findNamesInLibraryStartingWith(final long libraryId, final String nameStart) {
		SetQueryParametersCallback newCallBack1 = new ContainerIdNameStartParameterCallback(libraryId, nameStart);
		return executeListNamedQuery("campaign.findNamesInLibraryStartingWith", newCallBack1);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<CampaignLibraryNode> findAllByNameContaining(final String tokenInName, boolean groupByProject) {
		Criteria criteria = currentSession().createCriteria(CampaignLibraryNode.class, "campaignLibraryNode")
				.createAlias("campaignLibraryNode.project", "project")
				.add(Restrictions.ilike("campaignLibraryNode.name", tokenInName, MatchMode.ANYWHERE));

		if (groupByProject) {
			criteria = criteria.addOrder(Order.asc("project.id"));
		}

		criteria = criteria.addOrder(Order.asc("campaignLibraryNode.name"));

		return criteria.list();
	}

	@Override
	public List<Execution> findAllExecutionsByCampaignId(Long campaignId) {
		SetQueryParametersCallback callback = idParameter(campaignId);
		return executeListNamedQuery("campaign.findAllExecutions", callback);
	}

	@Override
	public TestPlanStatistics findCampaignStatistics(long campaignId) {

		Query q = currentSession().getNamedQuery("campaign.countStatuses");
		q.setParameter("campaignId", campaignId);
		List<Object[]> result = q.list();

		return new TestPlanStatistics(result);
	}

	@Override
	public long countRunningOrDoneExecutions(long campaignId) {
		return (Long) executeEntityNamedQuery("campaign.countRunningOrDoneExecutions", idParameter(campaignId));
	}


	@Override
	public List<Long> findNonBoundCampaign(Collection<Long> nodeIds, Long milestoneId) {
		Query q = currentSession().getNamedQuery("campaign.findNonBoundCampaign");
		q.setParameterList("nodeIds", nodeIds, LongType.INSTANCE);
		q.setParameter("milestoneId", milestoneId);
		return q.list();
	}

	@Override
	public List<Long> findCampaignIdsHavingMultipleMilestones(List<Long> nodeIds) {
		Query q = currentSession().getNamedQuery("campaign.findCampaignIdsHavingMultipleMilestones");
		q.setParameterList("nodeIds", nodeIds, LongType.INSTANCE);
		return q.list();
	}



	// ******************** utils ***************************

	private List<CampaignTestPlanItem> buildItems(List<Object[]> tuples) {

		List<CampaignTestPlanItem> items = new ArrayList<>(tuples.size());

		for (Object[] tuple : tuples) {
			CampaignTestPlanItem ctpi = (CampaignTestPlanItem) tuple[1];
			items.add(ctpi);
		}

		return items;
	}

	private List<IndexedCampaignTestPlanItem> buildIndexedItems(List<Object[]> tuples) {
		List<IndexedCampaignTestPlanItem> indexedItems = new ArrayList<>(tuples.size());

		for (Object[] tuple : tuples) {
			Integer index = (Integer) tuple[0];
			CampaignTestPlanItem ctpi = (CampaignTestPlanItem) tuple[1];
			indexedItems.add(new IndexedCampaignTestPlanItem(index, ctpi));
		}

		return indexedItems;
	}


}
