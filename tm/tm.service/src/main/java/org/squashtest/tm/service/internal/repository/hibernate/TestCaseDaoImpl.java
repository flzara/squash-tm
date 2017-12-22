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
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.commons.collections.ListUtils;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.type.LongType;
import org.squashtest.tm.core.foundation.collection.DefaultSorting;
import org.squashtest.tm.core.foundation.collection.Paging;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.SortOrder;
import org.squashtest.tm.core.foundation.collection.Sorting;
import org.squashtest.tm.domain.IdentifiedUtil;
import org.squashtest.tm.domain.NamedReference;
import org.squashtest.tm.domain.NamedReferencePair;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.testcase.CallTestStep;
import org.squashtest.tm.domain.testcase.ExportTestCaseData;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseFolder;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.domain.testcase.TestStep;
import org.squashtest.tm.service.internal.foundation.collection.PagingUtils;
import org.squashtest.tm.service.internal.foundation.collection.SortingUtils;
import org.squashtest.tm.service.internal.repository.CustomTestCaseDao;
/**
 * DAO for org.squashtest.tm.domain.testcase.TestCase
 *
 * @author bsiri
 *
 */

public class TestCaseDaoImpl extends HibernateEntityDao<TestCase> implements CustomTestCaseDao {


	/**
	 * "Standard" name for a query parameter representing a test case id.
	 */
	private static final String TEST_CASE_ID_PARAM_NAME = "testCaseId";
	private static final String TEST_CASE_IDS_PARAM_NAME = "testCaseIds";


	private static final String FIND_DESCENDANT_QUERY = "select DESCENDANT_ID from TCLN_RELATIONSHIP where ANCESTOR_ID in (:list)";

	private static final String FIND_ALL_DESCENDANT_TESTCASE_QUERY = "select tc.tcln_id from TCLN_RELATIONSHIP_CLOSURE tclnrc "+
			"inner join TEST_CASE tc on tclnrc.DESCENDANT_ID = tc.tcln_id "+
			"where tclnrc.ANCESTOR_ID in (:nodeIds)";

	private static final String FIND_ALL_CALLING_TEST_CASE_MAIN_HQL = "select distinct TestCase from TestCase as TestCase left join TestCase.project as Project "
			+ " join TestCase.steps as Steps where Steps.calledTestCase.id = :" + TEST_CASE_ID_PARAM_NAME;

	// in that query we only want the steps, but we join also on the caller test cases and projects because we can sort on them
	private static final String FIND_ALL_CALLING_TEST_STEPS_MAIN_HQL = "select Steps from TestCase as TestCase join TestCase.project as Project " +
			"join TestCase.steps as Steps where Steps.calledTestCase.id = :" + TEST_CASE_ID_PARAM_NAME;

	private static List<DefaultSorting> defaultVerifiedTcSorting;

	static {
		defaultVerifiedTcSorting = new LinkedList<>();
		defaultVerifiedTcSorting.add(new DefaultSorting("TestCase.reference"));
		defaultVerifiedTcSorting.add(new DefaultSorting("TestCase.name"));
		ListUtils.unmodifiableList(defaultVerifiedTcSorting);
	}

	@Override
	public void safePersist(TestCase testCase) {

		if (testCase.getSteps().isEmpty()) {
			super.persist(testCase);
		} else {
			persistTestCaseAndSteps(testCase);
		}
	}

	@Override
	public void persistTestCaseAndSteps(TestCase testCase) {
		persistEntity(testCase);
	}

	@Override
	// FIXME Uh, should be init'd by a query !
	public TestCase findAndInit(Long testCaseId) {
		Session session = currentSession();
		TestCase tc = (TestCase) session.get(TestCase.class, testCaseId);
		if (tc == null) {
			return null;
		}
		Hibernate.initialize(tc.getSteps());

		return tc;
	}

	/*
	 * Implementation note : the query :
	 *
	 *  select steps from TestStep steps left join fetch steps.attachmentList al left join fetch al.attachments where steps.testCase.id = :tcId order by index(steps)
	 *
	 *  or any other variant would not work : we need the left join, the fetch joins and order by index etc. The only way to make it work is to fetch the test case with all the required
	 *  features then return the hibernate collection of steps...
	 *
	 *
	 * (non-Javadoc)
	 * @see org.squashtest.tm.service.internal.repository.CustomTestCaseDao#findTestSteps(long)
	 */
	@Override
	public List<TestStep> findTestSteps(long testCaseId) {
		TestCase tc = (TestCase)currentSession().getNamedQuery("TestCase.findInitialized").setParameter("tcId", testCaseId).uniqueResult();
		if (tc == null){
			return null;
		}
		else{
			return new ArrayList<>(tc.getSteps());
		}
	}

	private SetQueryParametersCallback idParameter(final long testCaseId) {
		return new SetIdParameter(TEST_CASE_ID_PARAM_NAME, testCaseId);
	}

	private static final class SetIdsParameter implements SetQueryParametersCallback {
		private Collection<Long> testCasesIds;

		private SetIdsParameter(Collection<Long> testCasesIds) {
			this.testCasesIds = testCasesIds;
		}

		@Override
		public void setQueryParameters(Query query) {
			query.setParameterList("testCasesIds", testCasesIds);
		}
	}

	@Override
	public List<TestStep> findAllStepsByIdFiltered(final long testCaseId, final Paging filter) {
		/*
		 * we can't use the Criteria API because we need to get the ordered list and we can't access the join table to sort
		 * them (again).
		 */
		final int firstIndex = filter.getFirstItemIndex();
		final int lastIndex = filter.getFirstItemIndex() + filter.getPageSize() - 1;

		SetQueryParametersCallback callback = new SetIdsIndexesParameters(testCaseId, firstIndex, lastIndex);

		return executeListNamedQuery("testCase.findAllStepsByIdFiltered", callback);
	}

	private static final class SetIdsIndexesParameters implements SetQueryParametersCallback {
		private int firstIndex;
		private long testCaseId;
		private int lastIndex;

		private SetIdsIndexesParameters(long testCaseId, int firstIndex, int lastIndex) {
			this.testCaseId = testCaseId;
			this.firstIndex = firstIndex;
			this.lastIndex = lastIndex;
		}

		@Override
		public void setQueryParameters(Query query) {

			query.setParameter(TEST_CASE_ID_PARAM_NAME, testCaseId);
			query.setParameter("firstIndex", firstIndex);
			query.setParameter("lastIndex", lastIndex);

		}
	}



	// dynamic
	@Override
	public TestCase findTestCaseByTestStepId(final long testStepId) {
		Query query = currentSession().getNamedQuery("testStep.findParentNode");
		query.setParameter("childId", testStepId);
		return (TestCase) query.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Long> findTestCasesHavingCaller(Collection<Long> testCasesIds) {
		Query query = currentSession().getNamedQuery("testCase.findTestCasesHavingCaller");
		query.setParameterList("testCasesIds", testCasesIds);
		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Long> findAllTestCasesIdsCalledByTestCases(Collection<Long> testCasesIds) {
		Query query = currentSession().getNamedQuery("testCase.findAllTestCasesIdsCalledByTestCases");
		query.setParameterList("testCasesIds", testCasesIds);
		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Long> findAllTestCasesIdsCallingTestCases(List<Long> testCasesIds) {
		if(testCasesIds.isEmpty()){
			return Collections.emptyList();
		}
		Query query = currentSession().getNamedQuery("testCase.findAllTestCasesIdsCallingTestCases");
		query.setParameterList("testCasesIds", testCasesIds);
		return query.list();
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<TestCase> findAllCallingTestCases(final long testCaseId, final PagingAndSorting sorting) {
		String orderBy = "";

		if (sorting != null) {
			orderBy = " order by " + sorting.getSortedAttribute() + ' ' + sorting.getSortOrder().getCode();
		}

		Query query = currentSession().createQuery(FIND_ALL_CALLING_TEST_CASE_MAIN_HQL + orderBy);
		query.setParameter(TEST_CASE_ID_PARAM_NAME, testCaseId);

		if (sorting != null) {
			query.setMaxResults(sorting.getPageSize());
			query.setFirstResult(sorting.getFirstItemIndex());
		}
		return query.list();

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<TestCase> findAllCallingTestCases(long calleeId) {
		Query query = currentSession().createQuery(FIND_ALL_CALLING_TEST_CASE_MAIN_HQL);
		query.setParameter(TEST_CASE_ID_PARAM_NAME, calleeId);
		return query.list();
	}


	@Override
	@SuppressWarnings("unchecked")
	public List<CallTestStep> findAllCallingTestSteps(long testCaseId, PagingAndSorting sorting) {
		String orderBy = "";

		if (sorting != null) {
			orderBy = " order by " + sorting.getSortedAttribute() + ' ' + sorting.getSortOrder().getCode();
		}

		Query query = currentSession().createQuery(FIND_ALL_CALLING_TEST_STEPS_MAIN_HQL + orderBy);
		query.setParameter(TEST_CASE_ID_PARAM_NAME, testCaseId);

		if (sorting != null) {
			query.setMaxResults(sorting.getPageSize());
			query.setFirstResult(sorting.getFirstItemIndex());
		}
		return query.list();
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<CallTestStep> findAllCallingTestSteps(long testCaseId) {

		// TODO : maybe move this to package-info.java along the other queries if
		// the use cases calling this prove to be stable
		String orderBy = " order by Project.name asc, TestCase.reference asc, TestCase.name asc, index(Steps) asc";

		Query query = currentSession().createQuery(FIND_ALL_CALLING_TEST_STEPS_MAIN_HQL + orderBy);
		query.setParameter(TEST_CASE_ID_PARAM_NAME, testCaseId);

		return query.list();
	}

	@SuppressWarnings("unchecked")
	private List<NamedReference> findTestCaseDetails(Collection<Long> ids){
		if (ids.isEmpty()){
			return Collections.emptyList();
		}
		Query q = currentSession().getNamedQuery("testCase.findTestCaseDetails");
		q.setParameterList(TEST_CASE_IDS_PARAM_NAME, ids, LongType.INSTANCE);
		return q.list();
	}


	@Override
	/*
	 * implementation note : the following query could not use a right outer join. So we'll do the job manually. Hence
	 * the weird things done below.
	 */
	public List<NamedReferencePair> findTestCaseCallsUpstream(final Collection<Long> testCaseIds) {

		// get the node pairs when a caller/called pair was found.
		List<NamedReferencePair> result =  findTestCaseCallsDetails(testCaseIds, "testCase.findTestCasesHavingCallerDetails");

		// now we must also add dummy Object[] for the test case ids that hadn't any caller
		Collection<Long> remainingIds = new HashSet<>(testCaseIds);
		for (NamedReferencePair pair : result){
			remainingIds.remove(pair.getCalled().getId());
		}

		List<NamedReference> noncalledReferences = findTestCaseDetails(remainingIds);

		for (NamedReference ref : noncalledReferences){
			result.add(new NamedReferencePair(null, null, ref.getId(), ref.getName()));
		}

		return result;

	}


	@Override
	public List<NamedReferencePair> findTestCaseCallsDownstream(final Collection<Long> testCaseIds) {

		// get the node pairs when a caller/called pair was found.
		List<NamedReferencePair> result = findTestCaseCallsDetails(testCaseIds, "testCase.findTestCasesHavingCallStepsDetails");

		// now we must also add dummy Object[] for the test case ids that hadn't any caller
		Collection<Long> remainingIds = new HashSet<>(testCaseIds);
		for (NamedReferencePair pair : result){
			remainingIds.remove(pair.getCaller().getId());
		}

		List<NamedReference> noncalledReferences = findTestCaseDetails(remainingIds);

		for (NamedReference ref : noncalledReferences){
			result.add(new NamedReferencePair(ref.getId(), ref.getName(), null, null));
		}

		return result;
	}


	private List<NamedReferencePair> findTestCaseCallsDetails(final Collection<Long> testCaseIds, String mainQuery){
		if (testCaseIds.isEmpty()) {
			return Collections.emptyList();
		}

		// the easy part : fetch the informations for those who are called
		SetQueryParametersCallback queryCallback = new SetQueryParametersCallback() {
			@Override
			public void setQueryParameters(Query query) {
				query.setParameterList(TEST_CASE_IDS_PARAM_NAME, testCaseIds, new LongType());
				query.setReadOnly(true);
			}
		};

		return executeListNamedQuery(mainQuery,	queryCallback);

	}



	@SuppressWarnings("unchecked")
	@Override
	public List<Long> findCalledTestCaseOfCallSteps(List<Long> testStepsIds) {
		Query query = currentSession().getNamedQuery("testCase.findCalledTestCaseOfCallSteps");
		query.setParameterList("testStepsIds", testStepsIds);
		return query.list();
	}



	/*
	 * (non-Javadoc)
	 *
	 * @see org.squashtest.csp.tm.internal.repository.TestCaseDao#findAllVerifyingRequirementVersion(long,
	 * org.squashtest.tm.core.foundation.collection.PagingAndSorting)
	 */

	/*
	 * Issue #1629
	 *
	 * Observed problem : test cases sorted by references are indeed sorted by reference, but no more by name. Actual
	 * problem : We always want them to be sorted by reference and name, even when we want primarily sort them by
	 * project or execution type or else. Solution : The resultset will be sorted on all the attributes (ascending), and
	 * the Sorting specified by the user will have an higher priority.
	 *
	 * See #createEffectiveSorting(Sorting sorting), just below
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<TestCase> findAllByVerifiedRequirementVersion(long verifiedId, PagingAndSorting sorting) {

		// create the sorting, see comments above
		List<Sorting> effectiveSortings = createEffectiveSorting(sorting);

		// we have to fetch our query and modify the hql a bit, hence the weird operation below
		Query namedquery = currentSession().getNamedQuery("testCase.findVerifyingTestCases");
		String hql = namedquery.getQueryString();
		hql = SortingUtils.addOrders(hql, effectiveSortings);

		Query q = currentSession().createQuery(hql);
		if(!sorting.shouldDisplayAll()){
			PagingUtils.addPaging(q, sorting);
		}

		q.setParameter("versionId", verifiedId);

		List<Object[]> raw = q.list();

		// now we have to collect from the result set the only thing
		// we want : the test cases
		List<TestCase> res = new ArrayList<>(raw.size());
		for (Object[] tuple : raw){
			res.add((TestCase)tuple[0]);
		}

		if ("endDate".equals(sorting.getSortedAttribute())){
			 Collections.sort(res, new Comparator<TestCase>() {
				@Override
				public int compare(TestCase tc1, TestCase tc2) {
					return compareTcMilestoneDate(tc1, tc2);
				}
			});

			if (sorting.getSortOrder() == SortOrder.ASCENDING){
				Collections.reverse(res);
			}
		}

		return res;

	}

	private int compareTcMilestoneDate(TestCase tc1, TestCase tc2){

		boolean isEmpty1 = tc1.getMilestones().isEmpty();
		boolean isEmpty2 = tc2.getMilestones().isEmpty();

		if (isEmpty1 && isEmpty2){
			return 0;
		} else if (isEmpty1){
			return 1;
		} else if (isEmpty2){
			return -1;
		} else {
			return getMinDate(tc1).before(getMinDate(tc2)) ? getMinDate(tc1).after(getMinDate(tc2)) ? 0 : 1 : -1;


		}
	}

	private Date getMinDate(TestCase tc){
		return Collections.min(tc.getMilestones(), new Comparator<Milestone>(){
			@Override
			public int compare(Milestone m1, Milestone m2) {
				return m1.getEndDate().before(m2.getEndDate()) ? -1 : 1;
			}
		}).getEndDate();
	}


	/**
	 * @param userSorting
	 * @return
	 */
	/*
	 * Issue #1629
	 *
	 * Observed problem : test cases sorted by references are indeed sorted by reference, but no more by name. Actual
	 * problem : We always want them to be sorted by reference and name, even when we want primarily sort them by
	 * project or execution type or else. Solution : The resultset will be sorted on all the attributes (ascending), and
	 * the Sorting specified by the user will have an higher priority.
	 *
	 * See #createEffectiveSorting(Sorting sorting), just below
	 */

	private List<Sorting> createEffectiveSorting(Sorting userSorting) {

		LinkedList<Sorting> sortings = new LinkedList<>(defaultVerifiedTcSorting);

		// from that list we filter out the redundant element, considering the argument.
		// note that the sorting order is irrelevant here.
		ListIterator<Sorting> iterator = sortings.listIterator();
		while (iterator.hasNext()) {
			Sorting defaultSorting = iterator.next();
			if (defaultSorting.getSortedAttribute().equals(userSorting.getSortedAttribute())) {
				iterator.remove();
				break;
			}
		}

		// now we can set the Sorting specified by the user in first position
		sortings.addFirst(userSorting);

		return sortings;
	}

	/**
	 * @see org.squashtest.tm.service.internal.repository.TestCaseDao#countByVerifiedRequirementVersion(long)
	 */
	@Override
	public long countByVerifiedRequirementVersion(final long verifiedId) {
		return (Long) executeEntityNamedQuery("testCase.countByVerifiedRequirementVersion", new SetVerifiedIdParameter(
				verifiedId));
	}

	private static final class SetVerifiedIdParameter implements SetQueryParametersCallback {
		private long verifiedId;

		private SetVerifiedIdParameter(long verifiedId) {
			this.verifiedId = verifiedId;
		}

		@Override
		public void setQueryParameters(Query query) {
			query.setLong("verifiedId", verifiedId);

		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<TestCase> findUnsortedAllByVerifiedRequirementVersion(long requirementVersionId) {
		Query query = currentSession().getNamedQuery("testCase.findUnsortedAllByVerifiedRequirementVersion");
		query.setParameter("requirementVersionId", requirementVersionId);
		return query.list();
	}


	@Override
	public List<Execution> findAllExecutionByTestCase(Long tcId) {
		SetQueryParametersCallback callback = idParameter(tcId);
		return executeListNamedQuery("testCase.findAllExecutions", callback);
	}

	/* ----------------------------------------------------EXPORT METHODS----------------------------------------- */


	@Override
	public List<ExportTestCaseData> findTestCaseToExportFromNodes(List<Long> params) {
		if (!params.isEmpty()) {
			return doFindTestCaseToExportFromNodes(params);

		} else {
			return Collections.emptyList();

		}
	}

	private List<ExportTestCaseData> doFindTestCaseToExportFromNodes(List<Long> params) {
		// find root leafs
		List<TestCase> rootTestCases = findRootContentTestCase(params);
		// find all leafs contained in ids and contained by folders in ids
		List<Long> descendantIds = findDescendantIds(params, FIND_DESCENDANT_QUERY);

		// Case 1. Only root leafs are found
		if (descendantIds == null || descendantIds.isEmpty()) {
			List<Object[]> testCasesWithParentFolder = new ArrayList<>();
			return formatExportResult(mergeRootWithTestCasesWithParentFolder(rootTestCases, testCasesWithParentFolder));
		}

		// Case 2. More than root leafs are found
		List<Long> tcIds = findTestCaseIdsInIdList(descendantIds);
		List<Object[]> testCasesWithParentFolder = findTestCaseAndParentFolder(tcIds);

		if (!rootTestCases.isEmpty()) {
			mergeRootWithTestCasesWithParentFolder(rootTestCases, testCasesWithParentFolder);
		}

		return formatExportResult(testCasesWithParentFolder);
	}

	private List<Object[]> findTestCaseAndParentFolder(List<Long> tcIds) {
		if (!tcIds.isEmpty()) {
			SetQueryParametersCallback newCallBack1 = new SetIdsParameter(tcIds);
			return executeListNamedQuery("testCase.findTestCasesWithParentFolder", newCallBack1);

		} else {
			return Collections.emptyList();
		}
	}

	private List<Long> findTestCaseIdsInIdList(List<Long> nodesIds) {
		if (!nodesIds.isEmpty()) {

			List<TestCase> resultList = findAllByIds(nodesIds);
			return IdentifiedUtil.extractIds(resultList);

		} else {
			return Collections.emptyList();
		}
	}

	private List<Object[]> mergeRootWithTestCasesWithParentFolder(List<TestCase> rootTestCases,
			List<Object[]> testCasesWithParentFolder) {
		for (TestCase testCase : rootTestCases) {
			Object[] testCaseWithNullParentFolder = { testCase, null };
			testCasesWithParentFolder.add(testCaseWithNullParentFolder);
		}
		return testCasesWithParentFolder;
	}

	private List<TestCase> findRootContentTestCase(final List<Long> params) {
		if (!params.isEmpty()) {
			SetQueryParametersCallback newCallBack1 = new SetParamIdsParametersCallback(params);
			return executeListNamedQuery("testCase.findRootContentTestCase", newCallBack1);

		} else {
			return Collections.emptyList();

		}
	}

	private List<ExportTestCaseData> formatExportResult(List<Object[]> list) {
		if (!list.isEmpty()) {
			List<ExportTestCaseData> exportList = new ArrayList<>();

			for (Object[] tuple : list) {
				TestCase tc = (TestCase) tuple[0];
				TestCaseFolder folder = (TestCaseFolder) tuple[1];
				ExportTestCaseData etcd = new ExportTestCaseData(tc, folder);
				exportList.add(etcd);
			}

			return exportList;
		} else {
			return Collections.emptyList();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Long> findAllTestCaseIdsByNodeIds(Collection<Long> nodeIds) {
		if (nodeIds.isEmpty()){
			return Collections.emptyList();
		}

		Query query = currentSession().createSQLQuery(FIND_ALL_DESCENDANT_TESTCASE_QUERY);
		query.setParameterList("nodeIds", nodeIds, LongType.INSTANCE);
		query.setResultTransformer(new SqLIdResultTransformer());

		return query.list();
	}

	/* ----------------------------------------------------/EXPORT METHODS----------------------------------------- */


	@Override
	public List<TestCase> findAllLinkedToIteration(List<Long> nodeIds) {
		return executeListNamedQuery("testCase.findAllLinkedToIteration", new SetIdsParameter(nodeIds));
	}

	@Override
	public Map<Long, TestCaseImportance> findAllTestCaseImportanceWithImportanceAuto(Collection<Long> testCaseIds) {
		Map<Long, TestCaseImportance> resultMap = new HashMap<>();
		if(testCaseIds.isEmpty()){
			return resultMap;
		}
		List<Object[]> resultList = executeListNamedQuery("testCase.findAllTCImpWithImpAuto", new SetIdsParameter(testCaseIds));
		for(Object [] resultEntry : resultList){
			Long id = (Long) resultEntry[0];
			TestCaseImportance imp = (TestCaseImportance) resultEntry[1];
			resultMap.put(id, imp);
		}
		return resultMap;
	}



}
