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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.type.IntegerType;
import org.hibernate.type.LongType;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.domain.milestone.MilestoneStatus;
import org.squashtest.tm.domain.testcase.TestCaseFolder;
import org.squashtest.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.tm.service.internal.repository.ParameterNames;
import org.squashtest.tm.service.internal.repository.TestCaseDeletionDao;

/*
 * we'll perform a lot of operation using SQL because Hibernate whine at bulk-delete on polymorphic entities.
 *
 * See bugs : HHH-4183, HHH-1361, HHH-1657
 *
 */

@Repository
public class HibernateTestCaseDeletionDao extends HibernateDeletionDao implements TestCaseDeletionDao {

	private static final String TEST_CASES_IDS = "testCaseIds";
	private static final String TEST_STEP_IDS = "testStepIds";
	private static final String FOLDER_IDS = "folderIds";

	@Override
	public void removeEntities(final List<Long> entityIds) {
		if (!entityIds.isEmpty()) {

			for (Long entityId : entityIds) {

				TestCaseLibraryNode node = (TestCaseLibraryNode) getSession().get(TestCaseLibraryNode.class, entityId);

				removeEntityFromParentLibraryIfExists(entityId, node);

				removeEntityFromParentFolderIfExists(entityId, node);

				if (node != null) {
					getSession().delete(node);
					getSession().flush();
				}
			}
		}

	}

	private void removeEntityFromParentLibraryIfExists(Long entityId, TestCaseLibraryNode node) {
		Query query = getSession().getNamedQuery("testCaseLibraryNode.findParentLibraryIfExists");
		query.setParameter(ParameterNames.LIBRARY_NODE_ID, entityId);
		TestCaseLibrary library = (TestCaseLibrary) query.uniqueResult();
		if (library != null) {
			library.removeContent(node);
		}
	}

	private void removeEntityFromParentFolderIfExists(Long entityId, TestCaseLibraryNode node) {
		Query query = getSession().getNamedQuery("testCaseLibraryNode.findParentFolderIfExists");
		query.setParameter(ParameterNames.LIBRARY_NODE_ID, entityId);
		TestCaseFolder folder = (TestCaseFolder) query.uniqueResult();
		if (folder != null) {
			folder.removeContent(node);
		}
	}

	@Override
	public void removeAllSteps(List<Long> testStepIds) {
		if (!testStepIds.isEmpty()) {
			executeDeleteSQLQuery(NativeQueries.TESTCASE_SQL_REMOVETESTSTEPFROMLIST, TEST_STEP_IDS, testStepIds);

			executeDeleteSQLQuery(NativeQueries.TESTSTEP_SQL_REMOVEACTIONSTEPS, TEST_STEP_IDS, testStepIds);
			executeDeleteSQLQuery(NativeQueries.TESTSTEP_SQL_REMOVECALLSTEPS, TEST_STEP_IDS, testStepIds);
			executeDeleteSQLQuery(NativeQueries.TESTSTEP_SQL_REMOVETESTSTEPS, TEST_STEP_IDS, testStepIds);
		}
	}

	@Override
	public List<Long> findTestSteps(List<Long> testCaseIds) {
		if (!testCaseIds.isEmpty()) {
			return executeSelectNamedQuery("testCase.findAllSteps", TEST_CASES_IDS, testCaseIds);
		}
		return Collections.emptyList();
	}

	@Override
	public List<Long> findTestCaseAttachmentListIds(List<Long> testCaseIds) {
		if (!testCaseIds.isEmpty()) {
			return executeSelectNamedQuery("testCase.findAllAttachmentLists", TEST_CASES_IDS, testCaseIds);
		}
		return new ArrayList<>();
	}

	@Override
	public List<Long> findTestCaseFolderAttachmentListIds(List<Long> folderIds) {
		if (!folderIds.isEmpty()) {
			return executeSelectNamedQuery("testCaseFolder.findAllAttachmentLists", FOLDER_IDS, folderIds);
		}
		return Collections.emptyList();
	}

	@Override
	public List<Long> findTestStepAttachmentListIds(List<Long> testStepIds) {
		if (!testStepIds.isEmpty()) {
			return executeSelectNamedQuery("testStep.findAllAttachmentLists", TEST_STEP_IDS, testStepIds);
		}
		return Collections.emptyList();
	}

	@Override
	/*
	 * we're bound to use sql since hql offers no solution here.
	 *
	 * that method will perform the following :
	 *
	 * - update the order of all campaign item test plan ranked after the ones we're about to delete - delete the
	 * campaign item test plans.
	 *
	 * Also, because MySQL do not support sub queries selecting from the table being updated we have to proceed with the
	 * awkward treatment that follows :
	 */
	public void removeCampaignTestPlanInboundReferences(List<Long> testCaseIds) {

		if (!testCaseIds.isEmpty()) {

			// first we must reorder the campaign_item_test_plans
			reorderTestPlan(NativeQueries.TESTCASE_SQL_GETCALLINGCAMPAIGNITEMTESTPLANORDEROFFSET,
					NativeQueries.TESTCASE_SQL_UPDATECALLINGCAMPAIGNITEMTESTPLAN, testCaseIds);

			// now we can delete the items
			executeDeleteSQLQuery(NativeQueries.TESTCASE_SQL_REMOVECALLINGCAMPAIGNITEMTESTPLAN, TEST_CASES_IDS,
					testCaseIds);

		}

	}

	/*
	 * same comment than for HibernateTestCaseDeletionDao#removeCallingCampaignItemTestPlan
	 *
	 * (non-Javadoc)
	 *
	 * @see
	 * org.squashtest.csp.tm.internal.repository.TestCaseDeletionDao#removeOrSetNullCallingIterationItemTestPlan(java
	 * .util.List)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void removeOrSetIterationTestPlanInboundReferencesToNull(List<Long> testCaseIds) {

		if (!testCaseIds.isEmpty()) {
			SQLQuery query1 = getSession().createSQLQuery(
					NativeQueries.TESTCASE_SQL_SELECTCALLINGITERATIONITEMTESTPLANHAVINGEXECUTIONS);
			query1.addScalar("item_test_plan_id", LongType.INSTANCE);
			query1.setParameterList(TEST_CASES_IDS, testCaseIds, LongType.INSTANCE);
			List<Long> itpHavingExecIds = query1.list();

			SQLQuery query2 = getSession().createSQLQuery(
					NativeQueries.TESTCASE_SQL_SELECTCALLINGITERATIONITEMTESTPLANHAVINGNOEXECUTIONS);
			query2.addScalar("item_test_plan_id", LongType.INSTANCE);
			query2.setParameterList(TEST_CASES_IDS, testCaseIds, LongType.INSTANCE);
			List<Long> itpHavingNoExecIds = query2.list();

			setNullCallingIterationItemTestPlanHavingExecutions(itpHavingExecIds);
			removeCallingIterationItemTestPlanHavingNoExecutions(itpHavingNoExecIds);
		}

	}

	private void setNullCallingIterationItemTestPlanHavingExecutions(List<Long> itpHavingExecIds) {
		if (!itpHavingExecIds.isEmpty()) {
			executeDeleteSQLQuery(NativeQueries.TESTCASE_SQL_SETNULLCALLINGITERATIONITEMTESTPLANHAVINGEXECUTIONS,
					"itpHavingExecIds", itpHavingExecIds);
		}
	}

	private void removeCallingIterationItemTestPlanHavingNoExecutions(List<Long> itpHavingNoExecIds) {
		if (!itpHavingNoExecIds.isEmpty()) {

			// reorder the test plans for iterations
			reorderTestPlan(NativeQueries.TESTCASE_SQL_GETCALLINGITERATIONITEMTESTPLANORDEROFFSET,
					NativeQueries.TESTCASE_SQL_UPDATECALLINGITERATIONITEMTESTPLANORDER, itpHavingNoExecIds);

			// reorder the test plans for test suites
			reorderTestPlan(NativeQueries.TESTCASE_SQL_GETCALLINGTESTSUITEITEMTESTPLANORDEROFFSET,
					NativeQueries.TESTCASE_SQL_UPDATECALLINGTESTSUITEITEMTESTPLANORDER, itpHavingNoExecIds);

			// remove the elements from their collection
			executeDeleteSQLQuery(NativeQueries.TESTCASE_SQL_REMOVECALLINGTESTSUITEITEMTESTPLAN, "itpHavingNoExecIds",
					itpHavingNoExecIds);

			executeDeleteSQLQuery(NativeQueries.TESTCASE_SQL_REMOVECALLINGITERATIONITEMTESTPLANFROMLIST,
					"itpHavingNoExecIds", itpHavingNoExecIds);

			// remove the elements themselves
			executeDeleteSQLQuery(NativeQueries.TESTCASE_SQL_REMOVECALLINGITERATIONITEMTESTPLAN, "itpHavingNoExecIds",
					itpHavingNoExecIds);

		}
	}

	@SuppressWarnings("unchecked")
	private void reorderTestPlan(String selectOffsetQuery, String updateOrderQuery, List<Long> removedItems) {

		Query query0 = getSession().createSQLQuery(selectOffsetQuery);
		query0.setParameterList("removedItemIds1", removedItems);
		query0.setParameterList("removedItemIds2", removedItems);
		List<Object[]> pairIdOffset = query0.list();

		Map<Integer, List<Long>> mapOffsets = buildMapOfOffsetAndIds(pairIdOffset);

		for (Entry<Integer, List<Long>> offsetEntry : mapOffsets.entrySet()) {
			Query query = getSession().createSQLQuery(updateOrderQuery);
			query.setParameter("offset", offsetEntry.getKey(), IntegerType.INSTANCE);
			query.setParameterList("reorderedItemIds", offsetEntry.getValue(), LongType.INSTANCE);
			query.executeUpdate();
		}

	}

	private Map<Integer, List<Long>> buildMapOfOffsetAndIds(List<Object[]> list) {
		Map<Integer, List<Long>> result = new HashMap<>();

		for (Object[] pair : list) {
			Integer offset = ((BigInteger) pair[1]).intValue();

			// we skip if the offset is 0
			if (offset == 0) {
				continue;
			}

			if (!result.containsKey(offset)) {
				result.put(offset, new LinkedList<Long>());
			}

			result.get(offset).add(((BigInteger) pair[0]).longValue());
		}

		return result;

	}

	@Override
	public void setExecStepInboundReferencesToNull(List<Long> testStepIds) {
		if (!testStepIds.isEmpty()) {
			Query query = getSession().createSQLQuery(NativeQueries.TESTCASE_SQL_SET_NULL_CALLING_EXECUTION_STEPS);
			query.setParameterList(TEST_STEP_IDS, testStepIds, LongType.INSTANCE);
			query.executeUpdate();
		}
	}

	@Override
	public void setExecutionInboundReferencesToNull(List<Long> testCaseIds) {
		if (! testCaseIds.isEmpty()){
			Query query = getSession().createSQLQuery(NativeQueries.TESTCASE_SQL_SETNULLCALLINGEXECUTIONS);
			query.setParameterList(TEST_CASES_IDS, testCaseIds, LongType.INSTANCE);
			query.executeUpdate();
		}
	}

	@Override
	public void removeFromVerifyingTestCaseLists(List<Long> testCaseIds) {
		if (!testCaseIds.isEmpty()) {
			Query query = getSession().createSQLQuery(NativeQueries.TESTCASE_SQL_REMOVEVERIFYINGTESTCASELIST);
			query.setParameterList(TEST_CASES_IDS, testCaseIds, LongType.INSTANCE);
			query.executeUpdate();

		}
	}

	@Override
	public void removeFromVerifyingTestStepsList(List<Long> testStepIds) {
		if (!testStepIds.isEmpty()) {
			Query query = getSession().createSQLQuery(NativeQueries.TESTCASE_SQL_REMOVEVERIFYINGTESTSTEPLIST);
			query.setParameterList(TEST_STEP_IDS, testStepIds, LongType.INSTANCE);
			query.executeUpdate();

		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Long>[] separateFolderFromTestCaseIds(List<Long> originalIds) {
		List<Long> folderIds = new ArrayList<>();
		List<Long> testcaseIds = new ArrayList<>();

		List<BigInteger> filtredFolderIds = executeSelectSQLQuery(
				NativeQueries.TESTCASELIBRARYNODE_SQL_FILTERFOLDERIDS, "testcaseIds", originalIds);

		for (Long oId : originalIds) {
			if (filtredFolderIds.contains(BigInteger.valueOf(oId))) {
				folderIds.add(oId);
			} else {
				testcaseIds.add(oId);
			}
		}

		List<Long>[] result = new List[2];
		result[0] = folderIds;
		result[1] = testcaseIds;

		return result;
	}

	@Override
	public List<Long> findRemainingTestCaseIds(List<Long> originalIds) {
		List<BigInteger> rawids = executeSelectSQLQuery(NativeQueries.TESTCASE_SQL_FINDNOTDELETED, "allTestCaseIds", originalIds);
		List<Long> tcIds = new ArrayList<>(rawids.size());
		for (BigInteger rid : rawids){
			tcIds.add(rid.longValue());
		}
		return tcIds;
	}

	@Override
	public void unbindFromMilestone(List<Long> testCaseIds, Long milestoneId){

		if (! testCaseIds.isEmpty()){
			Query query = getSession().createSQLQuery(NativeQueries.TESTCASE_SQL_UNBIND_MILESTONE);
			query.setParameterList("testCaseIds", testCaseIds, LongType.INSTANCE);
			query.setParameter("milestoneId", milestoneId);
			query.executeUpdate();
		}

	}

	@Override
	public List<Long> findTestCasesWhichMilestonesForbidsDeletion(List<Long> originalId) {
		if (! originalId.isEmpty()){
			MilestoneStatus[] lockedStatuses = new MilestoneStatus[]{ MilestoneStatus.PLANNED, MilestoneStatus.LOCKED};
			Query query = getSession().getNamedQuery("testCase.findTestCasesWhichMilestonesForbidsDeletion");
			query.setParameterList("testCaseIds", originalId, LongType.INSTANCE);
			query.setParameterList("lockedStatuses", lockedStatuses);
			return query.list();
		}else{
			return new ArrayList<>();
		}
	}


}
