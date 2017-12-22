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

/**
 * thanks to the Hibernate support to pure scalar native queries, let's create another query respository.
 *
 * TODO 2015/09/25 : that's not true anymore with @NamedNativeQuery. Maybe move them all to this annotation
 * when applicable ?
 *
 * @author bsiri
 *
 */

public final class NativeQueries {

	public static final String ATTACHMENT_LIST_SQL_REMOVE_FROM_ATTACHMENT_LIST_CONTENT = " delete from ATTACHMENT_LIST_CONTENT where al_id in (:alIds)";

	public static final String TEST_CASE_FOLDER_SQL_FIND_PAIRED_CONTENT_FOR_FOLDERS = "select * from TCLN_RELATIONSHIP where ancestor_id in (:folderIds)";
	public static final String TEST_CASE_FOLDER_SQL_FIND_CONTENT_FOR_FOLDER = "select * from TCLN_RELATIONSHIP where ancestor_id in (:folderIds)";

	public static final String REQUIREMENT_FOLDER_SQL_FIND_PAIRED_CONTENT_FOR_FOLDERS = "select * from RLN_RELATIONSHIP where ancestor_id in (:folderIds)";
	public static final String REQUIREMENT_FOLDER_SQL_FIND_CONTENT_FOR_FOLDER = "select * from RLN_RELATIONSHIP where ancestor_id in (:folderIds)";

	public static final String CAMPAIGN_FOLDER_SQL_FIND_PAIRED_CONTENT_FOR_FOLDERS = "select * from CLN_RELATIONSHIP where ancestor_id in (:folderIds)";
	public static final String CAMPAIGN_FOLDER_SQL_FIND_CONTENT_FOR_FOLDER = "select * from CLN_RELATIONSHIP where ancestor_id in (:folderIds)";

	/* ***************************** deletion queries ************************************** */

	public static final String TESTCASE_SQL_REMOVE = "delete from TEST_CASE where tcln_id in (:nodeIds)";
	public static final String TESTCASELIBRARYNODE_SQL_REMOVE = "delete from TEST_CASE_LIBRARY_NODE where tcln_id in (:nodeIds)";
	public static final String TESTCASEFOLDER_SQL_REMOVE = "delete from TEST_CASE_FOLDER where tcln_id in (:nodeIds)";
	public static final String TESTCASELIBRARYNODE_SQL_FILTERFOLDERIDS = "select folder.tcln_id from TEST_CASE_FOLDER folder where folder.tcln_id in (:testcaseIds)";

	public static final String TESTCASE_SQL_REMOVEFROMFOLDER = "delete from TCLN_RELATIONSHIP where ancestor_id in (:ancIds) or descendant_id in (:descIds)";
	public static final String TESTCASE_SQL_REMOVEFROMLIBRARY = "delete from TEST_CASE_LIBRARY_CONTENT where content_id in (:testCaseIds)";
	public static final String TESTCASE_SQL_UNBIND_MILESTONE = "delete from MILESTONE_TEST_CASE where MILESTONE_ID = :milestoneId and TEST_CASE_ID in (:testCaseIds)";
	public static final String TESTCASE_SQL_FINDNOTDELETED = "select TCLN_ID from TEST_CASE where TCLN_ID in (:allTestCaseIds)";

	public static final String TESTSTEP_SQL_REMOVEACTIONSTEPS = "delete from ACTION_TEST_STEP where test_step_id in (:testStepIds)";
	public static final String TESTSTEP_SQL_REMOVECALLSTEPS = "delete from CALL_TEST_STEP where test_step_id in (:testStepIds)";
	public static final String TESTSTEP_SQL_REMOVETESTSTEPS = "delete from TEST_STEP where test_step_id in (:testStepIds)";

	public static final String REQUIREMENT_VERSION_FIND_ID_FROM_REQUIREMENT = "select req_v.res_id from REQUIREMENT_VERSION req_v where req_v.requirement_id in (:requirementIds)";
	public static final String SIMPLE_RESOURCE_FIND_ID_FROM_FOLDER = "select folder.res_id from REQUIREMENT_FOLDER folder where folder.rln_id in (:folderIds)";


	public static final String REQUIREMENT_SET_NULL_REQUIREMENT_VERSION = "update REQUIREMENT set current_version_id = null where rln_id in (:requirementIds);";
	public static final String REQUIREMENT_FOLDER_SET_NULL_SIMPLE_RESOURCE = "update REQUIREMENT_FOLDER set res_id = null where rln_id in (:folderIds)";


	public static final String REQUIREMENT_VERSION_SQL_REMOVE = "delete from REQUIREMENT_VERSION where res_id in (:requirementVersionIds)";
	public static final String SIMPLE_RESOURCE_SQL_REMOVE = "delete from SIMPLE_RESOURCE where res_id in (:simpleResourceIds)";
	public static final String RESOURCE_SQL_REMOVE = "delete from RESOURCE where res_id in (:resourceIds)";
	public static final String REQUIREMENT_SQL_REMOVE = "delete from REQUIREMENT where rln_id in (:nodeIds)";
	public static final String REQUIREMENTLIBRARYNODE_SQL_REMOVE = "delete from REQUIREMENT_LIBRARY_NODE where rln_id in (:nodeIds)";
	public static final String REQUIREMENT_FOLDER_SQL_REMOVE = "delete from REQUIREMENT_FOLDER where rln_id in (:nodeIds)";
	public static final String REQUIREMENTLIBRARYNODE_SQL_FILTERFOLDERIDS = "select folder.rln_id from REQUIREMENT_FOLDER folder where folder.rln_id in (:requirementIds)";

	public static final String REQUIREMENT_SQL_REMOVE_FROM_FOLDER = "delete from RLN_RELATIONSHIP where ancestor_id in (:ancIds) or descendant_id in (:descIds)";
	public static final String REQUIREMENT_SQL_REMOVE_FROM_LIBRARY = "delete from REQUIREMENT_LIBRARY_CONTENT where content_id in (:requirementIds)";
	public static final String REQUIREMENT_SQL_UNBIND_MILESTONE = "delete from MILESTONE_REQ_VERSION where MILESTONE_ID = :milestoneId and REQ_VERSION_ID in " +
			"(select v.RES_ID from REQUIREMENT_VERSION v where v.REQUIREMENT_ID in (:requirementIds) )";
	public static final String REQUIREMENT_SQL_FINDNOTDELETED = "select RLN_ID from REQUIREMENT where RLN_ID in (:allRequirementIds)";



	public static final String CAMPAIGN_SQL_REMOVE = "delete from CAMPAIGN where cln_id in (:nodeIds)";
	public static final String CAMPAIGNLIBRARYNODE_SQL_REMOVE = "delete from CAMPAIGN_LIBRARY_NODE where cln_id in (:nodeIds)";
	public static final String CAMPAIGNLIBRARYNODE_SQL_FILTERFOLDERIDS = "select folder.cln_id from CAMPAIGN_FOLDER folder where folder.cln_id in (:campaignIds)";
	public static final String CAMPAIGNFOLDER_SQL_REMOVE = "delete from CAMPAIGN_FOLDER where cln_id in (:nodeIds)";

	public static final String CAMPAIGN_SQL_REMOVEFROMFOLDER = "delete from CLN_RELATIONSHIP where ancestor_id in (:ancIds) or descendant_id in (:descIds)";
	public static final String CAMPAIGN_SQL_REMOVEFROMLIBRARY = "delete from CAMPAIGN_LIBRARY_CONTENT where content_id in (:campaignIds)";
	public static final String CAMPAIGN_SQL_UNBIND_MILESTONE = "delete from MILESTONE_CAMPAIGN where MILESTONE_ID = :milestoneId and CAMPAIGN_ID in (:campaignIds)";
	public static final String CAMPAIGN_SQL_FINDNOTDELETED = "select CLN_ID from CAMPAIGN where CLN_ID in (:allCampaignIds)";

	/*
	 * ********************************************** consequences of test case deletion on campaign item test plans
	 * *******************************************
	 */

	/*
	 * that query will count for each campaign item test plan, how many of them will be deleted before them
	 */
	public static final String TESTCASE_SQL_GETCALLINGCAMPAIGNITEMTESTPLANORDEROFFSET = " select ctpi1.ctpi_id , count(ctpi1.ctpi_id) "
			+ " from CAMPAIGN_TEST_PLAN_ITEM as ctpi1, "
			+ " CAMPAIGN_TEST_PLAN_ITEM as ctpi2 "
			+ " where ctpi1.campaign_id = ctpi2.campaign_id "
			+ " and ctpi2.test_case_id in (:removedItemIds1) "
			+ " and ctpi1.test_plan_order > ctpi2.test_plan_order "
			+ " and ctpi1.test_case_id not in (:removedItemIds2) "
			+ " group by ctpi1.ctpi_id";

	public static final String TESTCASE_SQL_UPDATECALLINGCAMPAIGNITEMTESTPLAN = "update CAMPAIGN_TEST_PLAN_ITEM "
			+ " set test_plan_order = test_plan_order - :offset" + " where ctpi_id in (:reorderedItemIds)";

	public static final String TESTCASE_SQL_REMOVECALLINGCAMPAIGNITEMTESTPLAN = "delete from CAMPAIGN_TEST_PLAN_ITEM where test_case_id in (:testCaseIds)";

	/*
	 * ********************************************* consequences of test case deletion on item test plans and test suites
	 * ******************************************************
	 */

	public static final String TESTCASE_SQL_SELECTCALLINGITERATIONITEMTESTPLANHAVINGEXECUTIONS = " select * from ITERATION_TEST_PLAN_ITEM itp "
			+ " inner join ITEM_TEST_PLAN_EXECUTION itpe on itp.item_test_plan_id = itpe.item_test_plan_id "
			+ " where itp.tcln_id in (:testCaseIds) ";

	public static final String TESTCASE_SQL_SELECTCALLINGITERATIONITEMTESTPLANHAVINGNOEXECUTIONS = " select * from ITERATION_TEST_PLAN_ITEM itp "
			+ " where itp.tcln_id in (:testCaseIds) "
			+ " and itp.item_test_plan_id not in (select distinct itpe.item_test_plan_id from ITEM_TEST_PLAN_EXECUTION itpe)";

	public static final String TESTCASE_SQL_SETNULLCALLINGITERATIONITEMTESTPLANHAVINGEXECUTIONS = " update ITERATION_TEST_PLAN_ITEM set tcln_id = NULL "
			+ " where item_test_plan_id in (:itpHavingExecIds) ";



	// ********** reordering test plan for iterations
	public static final String TESTCASE_SQL_GETCALLINGITERATIONITEMTESTPLANORDEROFFSET = " select itp1.item_test_plan_id, count(itp1.item_test_plan_id) "
			+ " from ITEM_TEST_PLAN_LIST as itp1, "
			+ " ITEM_TEST_PLAN_LIST as itp2 "
			+ " where itp1.iteration_id = itp2.iteration_id "
			+ " and itp1.item_test_plan_order > itp2.item_test_plan_order "
			+ " and itp2.item_test_plan_id in (:removedItemIds1) "
			+ " and itp1.item_test_plan_id not in (:removedItemIds2) " + " group by itp1.item_test_plan_id";

	public static final String TESTCASE_SQL_UPDATECALLINGITERATIONITEMTESTPLANORDER = " update ITEM_TEST_PLAN_LIST "
			+ " set item_test_plan_order = item_test_plan_order - :offset "
			+ " where item_test_plan_id in (:reorderedItemIds)";


	// ************ reordering test plan for test suites
	public static final String TESTCASE_SQL_GETCALLINGTESTSUITEITEMTESTPLANORDEROFFSET = " select itp1.tpi_id, count(itp1.tpi_id) "
			+ " from TEST_SUITE_TEST_PLAN_ITEM as itp1, "
			+ " TEST_SUITE_TEST_PLAN_ITEM as itp2 "
			+ " where itp1.suite_id = itp2.suite_id "
			+ " and itp1.test_plan_order > itp2.test_plan_order "
			+ " and itp2.tpi_id in (:removedItemIds1) "
			+ " and itp1.tpi_id not in (:removedItemIds2) " + " group by itp1.tpi_id";

	public static final String TESTCASE_SQL_UPDATECALLINGTESTSUITEITEMTESTPLANORDER = " update TEST_SUITE_TEST_PLAN_ITEM "
			+ " set test_plan_order = test_plan_order - :offset "
			+ " where tpi_id in (:reorderedItemIds)";

	public static final String TESTCASE_SQL_REMOVECALLINGTESTSUITEITEMTESTPLAN = "delete from TEST_SUITE_TEST_PLAN_ITEM where tpi_id in (:itpHavingNoExecIds)";
	public static final String TESTCASE_SQL_REMOVECALLINGITERATIONITEMTESTPLANFROMLIST = "delete from ITEM_TEST_PLAN_LIST  where item_test_plan_id in (:itpHavingNoExecIds)";
	public static final String TESTCASE_SQL_REMOVECALLINGITERATIONITEMTESTPLAN = "delete from ITERATION_TEST_PLAN_ITEM  where item_test_plan_id in (:itpHavingNoExecIds) ";


	/* ************************************ /consequences of test case deletion on item test plans  ******************************************************* */


	public static final String TESTCASE_SQL_SETNULLCALLINGEXECUTIONS = "update EXECUTION set tcln_id = null where tcln_id in (:testCaseIds)";

	public static final String TESTCASE_SQL_SET_NULL_CALLING_EXECUTION_STEPS = "update EXECUTION_STEP set test_step_id = null where test_step_id in (:testStepIds)";

	public static final String TESTCASE_SQL_REMOVEVERIFYINGTESTCASELIST = "delete from REQUIREMENT_VERSION_COVERAGE where verifying_test_case_id in (:testCaseIds)";

	public static final String TESTCASE_SQL_REMOVEVERIFYINGTESTSTEPLIST = "delete from VERIFYING_STEPS where TEST_STEP_ID in (:testStepIds)";

	public static final String TESTCASE_SQL_REMOVETESTSTEPFROMLIST = "delete from TEST_CASE_STEPS where step_id in (:testStepIds)";

	public static final String REQUIREMENT_SQL_REMOVEFROMVERIFIEDVERSIONSLISTS = " delete from REQUIREMENT_VERSION_COVERAGE "
			+ " where verified_req_version_id in (:versionIds)";

	public static final String REQUIREMENT_SQL_REMOVEFROMLINKEDVERSIONSLISTS = "delete from REQUIREMENT_VERSION_LINK" +
		" where requirement_version_id in (:versionIds) or related_requirement_version_id in (:versionIds)";

	public static final String REQUIREMENT_SQL_REMOVEFROMVERIFIEDREQUIREMENTLISTS = " delete from REQUIREMENT_VERSION_COVERAGE "
			+ " where verified_req_version_id in ( "
			+ " select req_v.res_id from REQUIREMENT_VERSION req_v where req_v.requirement_id in (:requirementIds) "
			+ ")";

	public static final String REQUIREMENT_SQL_REMOVE_TEST_STEP_COVERAGE_BY_REQ_VERSION_IDS = "delete from VERIFYING_STEPS where REQUIREMENT_VERSION_COVERAGE_ID in (select REQUIREMENT_VERSION_COVERAGE_ID from REQUIREMENT_VERSION_COVERAGE where VERIFIED_REQ_VERSION_ID in (:versionIds))";

	public static final String REQUIREMENT_SQL_REMOVE_TEST_STEP_BY_COVERAGE_ID = "delete from VERIFYING_STEPS where requirement_version_coverage_id = :covId";

	/* ********************************************* tree path queries ********************************************************************* */
	private static final String CLN_FIND_SORTED_PARENTS = " from CAMPAIGN_LIBRARY_NODE cln "+
			"inner join CLN_RELATIONSHIP_CLOSURE clos "+
			"on clos.ancestor_id = cln.cln_id "+
			"where clos.descendant_id = :nodeId "+
			"order by clos.depth desc";

	public static final String CLN_FIND_SORTED_PARENT_NAMES = "select cln.name "+CLN_FIND_SORTED_PARENTS;
	public static final String CLN_FIND_SORTED_PARENT_IDS = "select cln.cln_id "+CLN_FIND_SORTED_PARENTS;


	public static final String RLN_FIND_SORTED_PARENT_NAMES = "select rs.name from RESOURCE rs "+
			"join REQUIREMENT_FOLDER rf "+
			"on rs.res_id = rf.res_id "+
			"join REQUIREMENT_LIBRARY_NODE rln "+
			"on rf.rln_id = rln.rln_id "+
			"inner join RLN_RELATIONSHIP_CLOSURE clos "+
			"on clos.ancestor_id = rln.rln_id "+
			"where clos.descendant_id = :nodeId "+
			"order by clos.depth desc";

	public static final String RLN_FIND_SORTED_PARENT_IDS = "select rln.rln_id from REQUIREMENT_LIBRARY_NODE rln "+
			"inner join RLN_RELATIONSHIP_CLOSURE clos "+
			"on clos.ancestor_id = rln.rln_id "+
			"where clos.descendant_id = :nodeId "+
			"order by clos.depth desc";


	private NativeQueries() {
		super();
	}


}
