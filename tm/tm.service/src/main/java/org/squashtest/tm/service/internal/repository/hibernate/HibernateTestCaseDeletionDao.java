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

import org.hibernate.Query;
import org.hibernate.query.NativeQuery;
import org.hibernate.type.LongType;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.Select;
import org.jooq.SelectConditionStep;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.domain.milestone.MilestoneStatus;
import org.squashtest.tm.domain.testcase.TestCaseFolder;
import org.squashtest.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.tm.jooq.domain.tables.records.CampaignTestPlanItemRecord;
import org.squashtest.tm.jooq.domain.tables.records.ItemTestPlanListRecord;
import org.squashtest.tm.jooq.domain.tables.records.TestSuiteTestPlanItemRecord;
import org.squashtest.tm.service.internal.repository.ParameterNames;
import org.squashtest.tm.service.internal.repository.TestCaseDeletionDao;

import javax.inject.Inject;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.squashtest.tm.jooq.domain.Tables.CAMPAIGN_TEST_PLAN_ITEM;
import static org.squashtest.tm.jooq.domain.Tables.ITEM_TEST_PLAN_EXECUTION;
import static org.squashtest.tm.jooq.domain.Tables.ITEM_TEST_PLAN_LIST;
import static org.squashtest.tm.jooq.domain.Tables.ITERATION_TEST_PLAN_ITEM;
import static org.squashtest.tm.jooq.domain.Tables.TEST_SUITE_TEST_PLAN_ITEM;


/*
 * we'll perform a lot of operation using SQL because Hibernate whine at bulk-delete on polymorphic entities.
 *
 * See bugs : HHH-4183, HHH-1361, HHH-1657
 *
 */

@Repository
public class HibernateTestCaseDeletionDao extends HibernateDeletionDao implements TestCaseDeletionDao {

	private static final String TEST_CASES_IDS = "testCaseIds";
	private static final String AUTOMATION_REQUEST_IDS = "automationRequestIds";
	private static final String TEST_STEP_IDS = "testStepIds";
	private static final String FOLDER_IDS = "folderIds";
	private static final String ITP_HAVING_NO_EXEC_IDS = "itpHavingNoExecIds";
	private static final String ITP_HAVING_EXEC_IDS = "itpHavingExecIds";
	private static final String DATABASE_LABEL_POSTGRESQL = "postgresql";

	@Inject
	private DataSourceProperties dataSourceProperties;

	@Inject
	protected DSLContext DSL;

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
	public void removeAutomationRequestLibraryContent(List<Long> automationRequestIds) {
		if (!automationRequestIds.isEmpty()) {
			executeDeleteSQLQuery(NativeQueries.AUTOMATION_REQUEST_SQL_REMOVE_LIBRARY_CONTENT_FROMLIST, AUTOMATION_REQUEST_IDS, automationRequestIds);
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

	/*
	 * Cleanup in Campaign test plans prior to test case deletion.
	 *
	 * Because we have unique key constraints and we need to batch update CTPIs to update their order, we'll proceed
	 * like so :
	 * - Fetch CTPIs that need to be reordered
	 * - Remove all affected CTPIs (those that are effectively deleted and those that need reordering)
	 * - Re-insert the CTPIs that need reordering with their new position.
	 */
	@Override
	public void removeCampaignTestPlanInboundReferences(List<Long> testCasesToRemove) {
		if (testCasesToRemove.isEmpty()) { return; }

		Map<Long, Result<CampaignTestPlanItemRecord>> itemsToReorderByCampaignId = fetchCampaignItemsToReorder(testCasesToRemove);
		deleteAffectedCampaignItems(testCasesToRemove);
		batchInsertReorderedCampaignItems(itemsToReorderByCampaignId);
	}

	private Map<Long, Result<CampaignTestPlanItemRecord>> fetchCampaignItemsToReorder(List<Long> testCasesToRemove) {
		SelectConditionStep<Record1<Long>> affectedCampaigns = getAffectedCampaigns(testCasesToRemove);
		Select<Record1<Long>> itemIdsToRemove = DSL.select(CAMPAIGN_TEST_PLAN_ITEM.CTPI_ID)
			.from(CAMPAIGN_TEST_PLAN_ITEM)
			.where(CAMPAIGN_TEST_PLAN_ITEM.TEST_CASE_ID.in(testCasesToRemove));

		return DSL.selectFrom(CAMPAIGN_TEST_PLAN_ITEM)
			.where(CAMPAIGN_TEST_PLAN_ITEM.CAMPAIGN_ID.in(affectedCampaigns))
			.and(CAMPAIGN_TEST_PLAN_ITEM.CTPI_ID.notIn(itemIdsToRemove))
			.orderBy(CAMPAIGN_TEST_PLAN_ITEM.TEST_PLAN_ORDER)
			.fetchGroups(CAMPAIGN_TEST_PLAN_ITEM.CAMPAIGN_ID);
	}

	private SelectConditionStep<Record1<Long>> getAffectedCampaigns(List<Long> testCasesToRemove) {
		return DSL.select(CAMPAIGN_TEST_PLAN_ITEM.CAMPAIGN_ID)
			.from(CAMPAIGN_TEST_PLAN_ITEM)
			.where(CAMPAIGN_TEST_PLAN_ITEM.TEST_CASE_ID.in(testCasesToRemove));
	}

	private void deleteAffectedCampaignItems(List<Long> testCasesToRemove) {
		executeWithinHibernateSession(DSL
			.delete(CAMPAIGN_TEST_PLAN_ITEM)
			.where(CAMPAIGN_TEST_PLAN_ITEM.CAMPAIGN_ID.in(getAffectedCampaigns(testCasesToRemove))));
	}

	private void batchInsertReorderedCampaignItems(Map<Long, Result<CampaignTestPlanItemRecord>> groupedItemsToReorder) {
		groupedItemsToReorder.values().forEach(records -> IntStream
			.range(0, records.size())
			.forEach(position -> {
				CampaignTestPlanItemRecord record = records.get(position);
				record.setTestPlanOrder(position);
				record.changed(true);	// We need to set this flag to avoid 'null' columns...
			}));

		DSL.batchInsert(
			groupedItemsToReorder.values()
				.stream()
				.flatMap(Collection::stream)
				.collect(Collectors.toList())
		).execute();
	}

	/*
	 * The process to update an iteration test plan due to test cases removal is similar to the one used for campaign
	 * test plans (see note for removeCampaignTestPlanInboundReferences). We need to reorder Iteration TPIs and Test
	 * Suite TPIs that are affected by the test cases deletion. We'll do this in-memory by first fetching items that
	 * need reordering, deleting all affected items, and reinsert items with a new order.
	 */
	@Override
	public void removeOrSetIterationTestPlanInboundReferencesToNull(List<Long> testCasesToRemove) {

		if (testCasesToRemove.isEmpty()) {
			return;
		}

		Select<Record1<Long>> itemIdsToRemove = getIterationTestPlanItemsWithoutExecution(testCasesToRemove);
		Select<Record1<Long>> itemIdsToReorder = getIterationTestPlanItemsToReorder(itemIdsToRemove);

		Map<Long, Result<ItemTestPlanListRecord>> itemsByIteration = fetchIterationItemsToReorder(itemIdsToReorder);
		Map<Long, Result<TestSuiteTestPlanItemRecord>> itemsByTestSuite = fetchTestSuiteItemsToReorder(itemIdsToRemove);

		deleteAffectedIterationTestPlanItemsAndBindings(itemIdsToRemove, itemIdsToReorder);

		batchInsertReorderedIterationItems(itemsByIteration);
		batchInsertReorderedTestSuiteItems(itemsByTestSuite);
		nullifyReferencedTestCaseForExecutedITPIs(testCasesToRemove);
	}

	private Select<Record1<Long>> getIterationTestPlanItemsToReorder(Select<Record1<Long>> itemIdsToRemove) {
		Select<Record1<Long>> affectedIterationIds = DSL
			.select(ITEM_TEST_PLAN_LIST.ITERATION_ID)
			.from(ITEM_TEST_PLAN_LIST)
			.join(ITERATION_TEST_PLAN_ITEM)
			.on(ITEM_TEST_PLAN_LIST.ITEM_TEST_PLAN_ID.eq(ITERATION_TEST_PLAN_ITEM.ITEM_TEST_PLAN_ID))
			.where(ITERATION_TEST_PLAN_ITEM.ITEM_TEST_PLAN_ID.in(itemIdsToRemove));

		return DSL.select(ITERATION_TEST_PLAN_ITEM.ITEM_TEST_PLAN_ID)
			.from(ITERATION_TEST_PLAN_ITEM)
			.join(ITEM_TEST_PLAN_LIST).on(ITERATION_TEST_PLAN_ITEM.ITEM_TEST_PLAN_ID.eq(ITEM_TEST_PLAN_LIST.ITEM_TEST_PLAN_ID))
			.where(ITEM_TEST_PLAN_LIST.ITERATION_ID.in(affectedIterationIds))
			.and(ITERATION_TEST_PLAN_ITEM.ITEM_TEST_PLAN_ID.notIn(itemIdsToRemove));
	}

	private Select<Record1<Long>> getIterationTestPlanItemsWithoutExecution(List<Long> testCaseIds) {
		return DSL.select(ITERATION_TEST_PLAN_ITEM.ITEM_TEST_PLAN_ID)
			.from(ITERATION_TEST_PLAN_ITEM)
			.where(ITERATION_TEST_PLAN_ITEM.TCLN_ID.in(testCaseIds))
			.and(ITERATION_TEST_PLAN_ITEM.ITEM_TEST_PLAN_ID.notIn(
				DSL.selectDistinct(ITEM_TEST_PLAN_EXECUTION.ITEM_TEST_PLAN_ID).from(ITEM_TEST_PLAN_EXECUTION)));
	}

	private Map<Long, Result<ItemTestPlanListRecord>> fetchIterationItemsToReorder(Select<Record1<Long>> itemIdsToReorder) {
		return DSL
			.selectFrom(ITEM_TEST_PLAN_LIST)
			.where(ITEM_TEST_PLAN_LIST.ITEM_TEST_PLAN_ID.in(itemIdsToReorder))
			.orderBy(ITEM_TEST_PLAN_LIST.ITEM_TEST_PLAN_ORDER)
			.fetchGroups(ITEM_TEST_PLAN_LIST.ITERATION_ID);
	}

	private Map<Long, Result<TestSuiteTestPlanItemRecord>> fetchTestSuiteItemsToReorder(Select<Record1<Long>> itemIdsToRemove) {
		return DSL
			.selectFrom(TEST_SUITE_TEST_PLAN_ITEM)
			.where(TEST_SUITE_TEST_PLAN_ITEM.TPI_ID.notIn(itemIdsToRemove))
			.orderBy(TEST_SUITE_TEST_PLAN_ITEM.TEST_PLAN_ORDER)
			.fetchGroups(TEST_SUITE_TEST_PLAN_ITEM.SUITE_ID);
	}

	private void nullifyReferencedTestCaseForExecutedITPIs(List<Long> testCasesToRemove) {
		Select<Record1<Long>> itemsToRemoveWithExecutions = DSL
			.select(ITERATION_TEST_PLAN_ITEM.ITEM_TEST_PLAN_ID)
			.from(ITERATION_TEST_PLAN_ITEM)
			.where(ITERATION_TEST_PLAN_ITEM.TCLN_ID.in(testCasesToRemove))
			.and(ITERATION_TEST_PLAN_ITEM.ITEM_TEST_PLAN_ID.in(
				DSL.selectDistinct(ITEM_TEST_PLAN_EXECUTION.ITEM_TEST_PLAN_ID).from(ITEM_TEST_PLAN_EXECUTION)));

		org.jooq.Query nullifyQuery = DSL.update(ITERATION_TEST_PLAN_ITEM)
			.set(ITERATION_TEST_PLAN_ITEM.TCLN_ID, (Long) null)
			.where(ITERATION_TEST_PLAN_ITEM.ITEM_TEST_PLAN_ID.in(itemsToRemoveWithExecutions));

		// We don't use executeWithinHibernateSession because the type information for the 'null' parameter
		// would be lost, causing a SQL error
		NativeQuery<?> nativeQuery = getSession().createNativeQuery(nullifyQuery.getSQL());
		List<Object> bindValues = nullifyQuery.getBindValues();

		nativeQuery.setParameter(1, null, LongType.INSTANCE);

		for (int i = 1; i < bindValues.size(); i++) {
			nativeQuery.setParameter(i + 1, bindValues.get(i));
		}

		nativeQuery.executeUpdate();
	}

	private void deleteAffectedIterationTestPlanItemsAndBindings(Select<Record1<Long>> itemIdsToRemove, Select<Record1<Long>> itemIdsToReorder) {
		// Delete all test plan items
		executeWithinHibernateSession(DSL.deleteFrom(TEST_SUITE_TEST_PLAN_ITEM));

		// Delete all iteration test plan items that need to be reordered
		executeWithinHibernateSession(DSL.deleteFrom(ITEM_TEST_PLAN_LIST)
			.where(ITEM_TEST_PLAN_LIST.ITEM_TEST_PLAN_ID.in(itemIdsToReorder)));

		// Delete all item test plan lists for ITPIs that are removed
		executeWithinHibernateSession(DSL.deleteFrom(ITEM_TEST_PLAN_LIST)
			.where(ITEM_TEST_PLAN_LIST.ITEM_TEST_PLAN_ID.in(itemIdsToRemove)));

		// Delete all iteration test plan items that are removed
		executeWithinHibernateSession(DSL.deleteFrom(ITERATION_TEST_PLAN_ITEM)
			.where(ITERATION_TEST_PLAN_ITEM.ITEM_TEST_PLAN_ID.in(itemIdsToRemove)));
	}

	private void batchInsertReorderedTestSuiteItems(Map<Long, Result<TestSuiteTestPlanItemRecord>> itemsByTestSuite) {
		itemsByTestSuite.values().forEach(records -> IntStream
			.range(0, records.size())
			.forEach(position -> {
				TestSuiteTestPlanItemRecord record = records.get(position);
				record.setTestPlanOrder(position);
				record.changed(true);
			}));

		DSL.batchInsert(
			itemsByTestSuite.values()
				.stream()
				.flatMap(Collection::stream)
				.collect(Collectors.toList())
		).execute();
	}

	private void batchInsertReorderedIterationItems(Map<Long, Result<ItemTestPlanListRecord>> itemsByIteration) {
		itemsByIteration.values().forEach(records -> IntStream
			.range(0, records.size())
			.forEach(position -> {
				ItemTestPlanListRecord record = records.get(position);
				record.setItemTestPlanOrder(position);
				record.changed(true);
			}));

		DSL.batchInsert(
			itemsByIteration.values()
				.stream()
				.flatMap(Collection::stream)
				.collect(Collectors.toList())
		).execute();
	}

	private void executeWithinHibernateSession(org.jooq.Query jooqQuery) {
		NativeQuery<?> nativeQuery = getSession().createNativeQuery(jooqQuery.getSQL());
		List<Object> bindValues = jooqQuery.getBindValues();

		for (int i = 0; i < bindValues.size(); i++) {
			nativeQuery.setParameter(i + 1, bindValues.get(i));
		}

		nativeQuery.executeUpdate();
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
			query.setParameterList(TEST_CASES_IDS, testCaseIds, LongType.INSTANCE);
			query.setParameter("milestoneId", milestoneId);
			query.executeUpdate();
		}

	}

	@Override
	public List<Long> findTestCasesWhichMilestonesForbidsDeletion(List<Long> originalId) {
		if (! originalId.isEmpty()){
			MilestoneStatus[] lockedStatuses = new MilestoneStatus[]{ MilestoneStatus.PLANNED, MilestoneStatus.LOCKED};
			Query query = getSession().getNamedQuery("testCase.findTestCasesWhichMilestonesForbidsDeletion");
			query.setParameterList(TEST_CASES_IDS, originalId, LongType.INSTANCE);
			query.setParameterList("lockedStatuses", lockedStatuses);
			return query.list();
		}else{
			return new ArrayList<>();
		}
	}


}
