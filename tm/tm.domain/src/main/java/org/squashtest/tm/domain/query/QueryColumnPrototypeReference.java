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
package org.squashtest.tm.domain.query;

/**
 * That class contains the label of all columns that can be found in the database.
 *
 */
public interface QueryColumnPrototypeReference {

	public static final String AUTOMATED_EXECUTION_EXTENDER_ENTITY = "AUTOMATED_EXECUTION_EXTENDER_ENTITY";
	public static final String AUTOMATED_EXECUTION_EXTENDER_ID = "AUTOMATED_EXECUTION_EXTENDER_ID";
	public static final String AUTOMATED_TEST_ENTITY = "AUTOMATED_TEST_ENTITY";
	public static final String AUTOMATED_TEST_ID = "AUTOMATED_TEST_ID";
	public static final String AUTOMATION_REQUEST_ENTITY = "AUTOMATION_REQUEST_ENTITY";
	public static final String AUTOMATION_REQUEST_ID = "AUTOMATION_REQUEST_ID";
	public static final String AUTOMATION_REQUEST_STATUS = "AUTOMATION_REQUEST_STATUS";
	public static final String CAMPAIGN_ACTUAL_END = "CAMPAIGN_ACTUAL_END";
	public static final String CAMPAIGN_ACTUAL_START = "CAMPAIGN_ACTUAL_START";
	public static final String CAMPAIGN_ATTACHMENT_ENTITY = "CAMPAIGN_ATTACHMENT_ENTITY";
	public static final String CAMPAIGN_ATTACHMENT_ID = "CAMPAIGN_ATTACHMENT_ID";
	public static final String CAMPAIGN_ATTLIST_ENTITY = "CAMPAIGN_ATTLIST_ENTITY";
	public static final String CAMPAIGN_ATTLIST_ID = "CAMPAIGN_ATTLIST_ID";
	public static final String CAMPAIGN_CUF_CHECKBOX = "CAMPAIGN_CUF_CHECKBOX";
	public static final String CAMPAIGN_CUF_DATE = "CAMPAIGN_CUF_DATE";
	public static final String CAMPAIGN_CUF_LIST = "CAMPAIGN_CUF_LIST";
	public static final String CAMPAIGN_CUF_NUMERIC = "CAMPAIGN_CUF_NUMERIC";
	public static final String CAMPAIGN_CUF_TAG = "CAMPAIGN_CUF_TAG";
	public static final String CAMPAIGN_CUF_TEXT = "CAMPAIGN_CUF_TEXT";
	public static final String CAMPAIGN_ENTITY = "CAMPAIGN_ENTITY";
	public static final String CAMPAIGN_ID = "CAMPAIGN_ID";
	public static final String CAMPAIGN_ISSUECOUNT = "CAMPAIGN_ISSUECOUNT";
	public static final String CAMPAIGN_ITERCOUNT = "CAMPAIGN_ITERCOUNT";
	public static final String CAMPAIGN_MILESTONE_END_DATE = "CAMPAIGN_MILESTONE_END_DATE";
	public static final String CAMPAIGN_MILESTONE_ENTITY = "CAMPAIGN_MILESTONE_ENTITY";
	public static final String CAMPAIGN_MILESTONE_ID = "CAMPAIGN_MILESTONE_ID";
	public static final String CAMPAIGN_MILESTONE_LABEL = "CAMPAIGN_MILESTONE_LABEL";
	public static final String CAMPAIGN_MILESTONE_STATUS = "CAMPAIGN_MILESTONE_STATUS";
	public static final String CAMPAIGN_NAME = "CAMPAIGN_NAME";
	public static final String CAMPAIGN_PROJECT = "CAMPAIGN_PROJECT";
	public static final String CAMPAIGN_PROJECT_ENTITY = "CAMPAIGN_PROJECT_ENTITY";
	public static final String CAMPAIGN_PROJECT_ID = "CAMPAIGN_PROJECT_ID";
	public static final String CAMPAIGN_PROJECT_NAME = "CAMPAIGN_PROJECT_NAME";
	public static final String CAMPAIGN_REFERENCE = "CAMPAIGN_REFERENCE";
	public static final String CAMPAIGN_SCHED_END = "CAMPAIGN_SCHED_END";
	public static final String CAMPAIGN_SCHED_START = "CAMPAIGN_SCHED_START";
	public static final String DATASET_ENTITY = "DATASET_ENTITY";
	public static final String DATASET_ID = "DATASET_ID";
	public static final String EXECUTION_CUF_CHECKBOX = "EXECUTION_CUF_CHECKBOX";
	public static final String EXECUTION_CUF_DATE = "EXECUTION_CUF_DATE";
	public static final String EXECUTION_CUF_LIST = "EXECUTION_CUF_LIST";
	public static final String EXECUTION_CUF_NUMERIC = "EXECUTION_CUF_NUMERIC";
	public static final String EXECUTION_CUF_TAG = "EXECUTION_CUF_TAG";
	public static final String EXECUTION_CUF_TEXT = "EXECUTION_CUF_TEXT";
	public static final String EXECUTION_DS_LABEL = "EXECUTION_DS_LABEL";
	public static final String EXECUTION_ENTITY = "EXECUTION_ENTITY";
	public static final String EXECUTION_EXECUTION_MODE = "EXECUTION_EXECUTION_MODE";
	public static final String EXECUTION_ID = "EXECUTION_ID";
	public static final String EXECUTION_ISAUTO = "EXECUTION_ISAUTO";
	public static final String EXECUTION_ISSUECOUNT = "EXECUTION_ISSUECOUNT";
	public static final String EXECUTION_LABEL = "EXECUTION_LABEL";
	public static final String EXECUTION_LASTEXEC = "EXECUTION_LASTEXEC";
	public static final String EXECUTION_LAST_EXECUTED_ON = "EXECUTION_LAST_EXECUTED_ON";
	public static final String EXECUTION_STATUS = "EXECUTION_STATUS";
	public static final String EXECUTION_TESTER_LOGIN = "EXECUTION_TESTER_LOGIN";
	public static final String ISSUE_BUGTRACKER = "ISSUE_BUGTRACKER";
	public static final String ISSUE_ENTITY = "ISSUE_ENTITY";
	public static final String ISSUE_ID = "ISSUE_ID";
	public static final String ISSUE_REMOTE_ID = "ISSUE_REMOTE_ID";
	public static final String ISSUE_SEVERITY = "ISSUE_SEVERITY";
	public static final String ISSUE_STATUS = "ISSUE_STATUS";
	public static final String ITEM_SUITE_ENTITY = "ITEM_SUITE_ENTITY";
	public static final String ITEM_SUITE_ID = "ITEM_SUITE_ID";
	public static final String ITEM_TEST_PLAN_AUTOEXCOUNT = "ITEM_TEST_PLAN_AUTOEXCOUNT";
	public static final String ITEM_TEST_PLAN_DATASET_LABEL = "ITEM_TEST_PLAN_DATASET_LABEL";
	public static final String ITEM_TEST_PLAN_DSCOUNT = "ITEM_TEST_PLAN_DSCOUNT";
	public static final String ITEM_TEST_PLAN_ENTITY = "ITEM_TEST_PLAN_ENTITY";
	public static final String ITEM_TEST_PLAN_ID = "ITEM_TEST_PLAN_ID";
	public static final String ITEM_TEST_PLAN_ISSUECOUNT = "ITEM_TEST_PLAN_ISSUECOUNT";
	public static final String ITEM_TEST_PLAN_IS_EXECUTED = "ITEM_TEST_PLAN_IS_EXECUTED";
	public static final String ITEM_TEST_PLAN_LABEL = "ITEM_TEST_PLAN_LABEL";
	public static final String ITEM_TEST_PLAN_LASTEXECON = "ITEM_TEST_PLAN_LASTEXECON";
	public static final String ITEM_TEST_PLAN_LASTEXECBY = "ITEM_TEST_PLAN_LASTEXECBY";
	public static final String ITEM_TEST_PLAN_MANEXCOUNT = "ITEM_TEST_PLAN_MANEXCOUNT";
	public static final String ITEM_TEST_PLAN_STATUS = "ITEM_TEST_PLAN_STATUS";
	public static final String ITEM_TEST_PLAN_SUITECOUNT = "ITEM_TEST_PLAN_SUITECOUNT";
	public static final String ITEM_TEST_PLAN_TC_DELETED = "ITEM_TEST_PLAN_TC_DELETED";
	public static final String ITEM_TEST_PLAN_TC_ID = "ITEM_TEST_PLAN_TC_ID";
	public static final String ITEM_TEST_PLAN_TESTER = "ITEM_TEST_PLAN_TESTER";
	public static final String ITERATION_ACTUAL_END = "ITERATION_ACTUAL_END";
	public static final String ITERATION_ACTUAL_START = "ITERATION_ACTUAL_START";
	public static final String ITERATION_CUF_CHECKBOX = "ITERATION_CUF_CHECKBOX";
	public static final String ITERATION_CUF_DATE = "ITERATION_CUF_DATE";
	public static final String ITERATION_CUF_LIST = "ITERATION_CUF_LIST";
	public static final String ITERATION_CUF_NUMERIC = "ITERATION_CUF_NUMERIC";
	public static final String ITERATION_CUF_TAG = "ITERATION_CUF_TAG";
	public static final String ITERATION_CUF_TEXT = "ITERATION_CUF_TEXT";
	public static final String ITERATION_ENTITY = "ITERATION_ENTITY";
	public static final String ITERATION_ID = "ITERATION_ID";
	public static final String ITERATION_ISSUECOUNT = "ITERATION_ISSUECOUNT";
	public static final String ITERATION_ITEMCOUNT = "ITERATION_ITEMCOUNT";
	public static final String ITERATION_NAME = "ITERATION_NAME";
	public static final String ITERATION_REFERENCE = "ITERATION_REFERENCE";
	public static final String ITERATION_SCHED_END = "ITERATION_SCHED_END";
	public static final String ITERATION_SCHED_START = "ITERATION_SCHED_START";
	public static final String ITERATION_TEST_PLAN_ASSIGNED_USER_ENTITY = "ITERATION_TEST_PLAN_ASSIGNED_USER_ENTITY";
	public static final String ITERATION_TEST_PLAN_ASSIGNED_USER_ID = "ITERATION_TEST_PLAN_ASSIGNED_USER_ID";
	public static final String ITERATION_TEST_PLAN_ASSIGNED_USER_LOGIN = "ITERATION_TEST_PLAN_ASSIGNED_USER_LOGIN";
	public static final String PARAMETER_ENTITY = "PARAMETER_ENTITY";
	public static final String PARAMETER_ID = "PARAMETER_ID";
	public static final String REQUIREMENT_CATEGORY = "REQUIREMENT_CATEGORY";
	public static final String REQUIREMENT_CRITICALITY = "REQUIREMENT_CRITICALITY";
	public static final String REQUIREMENT_ENTITY = "REQUIREMENT_ENTITY";
	public static final String REQUIREMENT_ID = "REQUIREMENT_ID";
	public static final String REQUIREMENT_NB_VERSIONS = "REQUIREMENT_NB_VERSIONS";
	public static final String REQUIREMENT_PROJECT = "REQUIREMENT_PROJECT";
	public static final String REQUIREMENT_PROJECT_ENTITY = "REQUIREMENT_PROJECT_ENTITY";
	public static final String REQUIREMENT_PROJECT_ID = "REQUIREMENT_PROJECT_ID";
	public static final String REQUIREMENT_PROJECT_NAME = "REQUIREMENT_PROJECT_NAME";
	public static final String REQUIREMENT_STATUS = "REQUIREMENT_STATUS";
	public static final String REQUIREMENT_VERSION_ATTACHMENT_ENTITY = "REQUIREMENT_VERSION_ATTACHMENT_ENTITY";
	public static final String REQUIREMENT_VERSION_ATTACHMENT_ID = "REQUIREMENT_VERSION_ATTACHMENT_ID";
	public static final String REQUIREMENT_VERSION_ATTCOUNT = "REQUIREMENT_VERSION_ATTCOUNT";
	public static final String REQUIREMENT_VERSION_ATTLIST_ENTITY = "REQUIREMENT_VERSION_ATTLIST_ENTITY";
	public static final String REQUIREMENT_VERSION_ATTLIST_ID = "REQUIREMENT_VERSION_ATTLIST_ID";
	public static final String REQUIREMENT_VERSION_CATEGORY = "REQUIREMENT_VERSION_CATEGORY";
	public static final String REQUIREMENT_VERSION_CATEGORY_ENTITY = "REQUIREMENT_VERSION_CATEGORY_ENTITY";
	public static final String REQUIREMENT_VERSION_CATEGORY_ID = "REQUIREMENT_VERSION_CATEGORY_ID";
	public static final String REQUIREMENT_VERSION_CATEGORY_LABEL = "REQUIREMENT_VERSION_CATEGORY_LABEL";
	public static final String REQUIREMENT_VERSION_CREATED_BY = "REQUIREMENT_VERSION_CREATED_BY";
	public static final String REQUIREMENT_VERSION_CREATED_ON = "REQUIREMENT_VERSION_CREATED_ON";
	public static final String REQUIREMENT_VERSION_CRITICALITY = "REQUIREMENT_VERSION_CRITICALITY";
	public static final String REQUIREMENT_VERSION_CUF_CHECKBOX = "REQUIREMENT_VERSION_CUF_CHECKBOX";
	public static final String REQUIREMENT_VERSION_CUF_DATE = "REQUIREMENT_VERSION_CUF_DATE";
	public static final String REQUIREMENT_VERSION_CUF_LIST = "REQUIREMENT_VERSION_CUF_LIST";
	public static final String REQUIREMENT_VERSION_CUF_NUMERIC = "REQUIREMENT_VERSION_CUF_NUMERIC";
	public static final String REQUIREMENT_VERSION_CUF_TAG = "REQUIREMENT_VERSION_CUF_TAG";
	public static final String REQUIREMENT_VERSION_CUF_TEXT = "REQUIREMENT_VERSION_CUF_TEXT";
	public static final String REQUIREMENT_VERSION_DESCRIPTION = "REQUIREMENT_VERSION_DESCRIPTION";
	public static final String REQUIREMENT_VERSION_ENTITY = "REQUIREMENT_VERSION_ENTITY";
	public static final String REQUIREMENT_VERSION_ID = "REQUIREMENT_VERSION_ID";
	public static final String REQUIREMENT_VERSION_MILCOUNT = "REQUIREMENT_VERSION_MILCOUNT";
	public static final String REQUIREMENT_VERSION_MILESTONE_END_DATE = "REQUIREMENT_VERSION_MILESTONE_END_DATE";
	public static final String REQUIREMENT_VERSION_MILESTONE_ENTITY = "REQUIREMENT_VERSION_MILESTONE_ENTITY";
	public static final String REQUIREMENT_VERSION_MILESTONE_ID = "REQUIREMENT_VERSION_MILESTONE_ID";
	public static final String REQUIREMENT_VERSION_MILESTONE_LABEL = "REQUIREMENT_VERSION_MILESTONE_LABEL";
	public static final String REQUIREMENT_VERSION_MILESTONE_STATUS = "REQUIREMENT_VERSION_MILESTONE_STATUS";
	public static final String REQUIREMENT_VERSION_MODIFIED_BY = "REQUIREMENT_VERSION_MODIFIED_BY";
	public static final String REQUIREMENT_VERSION_MODIFIED_ON = "REQUIREMENT_VERSION_MODIFIED_ON";
	public static final String REQUIREMENT_VERSION_NAME = "REQUIREMENT_VERSION_NAME";
	public static final String REQUIREMENT_VERSION_REFERENCE = "REQUIREMENT_VERSION_REFERENCE";
	public static final String REQUIREMENT_VERSION_STATUS = "REQUIREMENT_VERSION_STATUS";
	public static final String REQUIREMENT_VERSION_TCCOUNT = "REQUIREMENT_VERSION_TCCOUNT";
	public static final String REQUIREMENT_VERSION_VERS_NUM = "REQUIREMENT_VERSION_VERS_NUM";
	public static final String TEST_CASE_ATTACHMENT_ENTITY = "TEST_CASE_ATTACHMENT_ENTITY";
	public static final String TEST_CASE_ATTACHMENT_ID = "TEST_CASE_ATTACHMENT_ID";
	public static final String TEST_CASE_ATTCOUNT = "TEST_CASE_ATTCOUNT";
	public static final String TEST_CASE_ATTLIST_ENTITY = "TEST_CASE_ATTLIST_ENTITY";
	public static final String TEST_CASE_ATTLIST_ID = "TEST_CASE_ATTLIST_ID";
	public static final String TEST_CASE_AUTOMATABLE = "TEST_CASE_AUTOMATABLE";
	public static final String TEST_CASE_CALLSTEPCOUNT = "TEST_CASE_CALLSTEPCOUNT";
	public static final String TEST_CASE_CREATED_BY = "TEST_CASE_CREATED_BY";
	public static final String TEST_CASE_CREATED_ON = "TEST_CASE_CREATED_ON";
	public static final String TEST_CASE_CUF_CHECKBOX = "TEST_CASE_CUF_CHECKBOX";
	public static final String TEST_CASE_CUF_DATE = "TEST_CASE_CUF_DATE";
	public static final String TEST_CASE_CUF_LIST = "TEST_CASE_CUF_LIST";
	public static final String TEST_CASE_CUF_NUMERIC = "TEST_CASE_CUF_NUMERIC";
	public static final String TEST_CASE_CUF_TAG = "TEST_CASE_CUF_TAG";
	public static final String TEST_CASE_CUF_TEXT = "TEST_CASE_CUF_TEXT";
	public static final String TEST_CASE_DATASETCOUNT = "TEST_CASE_DATASETCOUNT";
	public static final String TEST_CASE_DESCRIPTION = "TEST_CASE_DESCRIPTION";
	public static final String TEST_CASE_ENTITY = "TEST_CASE_ENTITY";
	public static final String TEST_CASE_EXECOUNT = "TEST_CASE_EXECOUNT";
	public static final String TEST_CASE_HASAUTOSCRIPT = "TEST_CASE_HASAUTOSCRIPT";
	public static final String TEST_CASE_ID = "TEST_CASE_ID";
	public static final String TEST_CASE_IMPORTANCE = "TEST_CASE_IMPORTANCE";
	public static final String TEST_CASE_ITERCOUNT = "TEST_CASE_ITERCOUNT";
	public static final String TEST_CASE_KIND = "TEST_CASE_KIND";
	public static final String TEST_CASE_MILCOUNT = "TEST_CASE_MILCOUNT";
	public static final String TEST_CASE_MILESTONE_END_DATE = "TEST_CASE_MILESTONE_END_DATE";
	public static final String TEST_CASE_MILESTONE_ENTITY = "TEST_CASE_MILESTONE_ENTITY";
	public static final String TEST_CASE_MILESTONE_ID = "TEST_CASE_MILESTONE_ID";
	public static final String TEST_CASE_MILESTONE_LABEL = "TEST_CASE_MILESTONE_LABEL";
	public static final String TEST_CASE_MILESTONE_STATUS = "TEST_CASE_MILESTONE_STATUS";
	public static final String TEST_CASE_MODIFIED_BY = "TEST_CASE_MODIFIED_BY";
	public static final String TEST_CASE_MODIFIED_ON = "TEST_CASE_MODIFIED_ON";
	public static final String TEST_CASE_NAME = "TEST_CASE_NAME";
	public static final String TEST_CASE_NATURE = "TEST_CASE_NATURE";
	public static final String TEST_CASE_NATURE_ENTITY = "TEST_CASE_NATURE_ENTITY";
	public static final String TEST_CASE_NATURE_ID = "TEST_CASE_NATURE_ID";
	public static final String TEST_CASE_NATURE_LABEL = "TEST_CASE_NATURE_LABEL";
	public static final String TEST_CASE_PARAMCOUNT = "TEST_CASE_PARAMCOUNT";
	public static final String TEST_CASE_PREQUISITE = "TEST_CASE_PREQUISITE";
	public static final String TEST_CASE_PROJECT = "TEST_CASE_PROJECT";
	public static final String TEST_CASE_PROJECT_ENTITY = "TEST_CASE_PROJECT_ENTITY";
	public static final String TEST_CASE_PROJECT_ID = "TEST_CASE_PROJECT_ID";
	public static final String TEST_CASE_PROJECT_NAME = "TEST_CASE_PROJECT_NAME";
	public static final String TEST_CASE_REFERENCE = "TEST_CASE_REFERENCE";
	public static final String TEST_CASE_STATUS = "TEST_CASE_STATUS";
	public static final String TEST_CASE_STEPCOUNT = "TEST_CASE_STEPCOUNT";
	public static final String TEST_CASE_STEP_CLASS = "TEST_CASE_STEP_CLASS";
	public static final String TEST_CASE_STEP_ENTITY = "TEST_CASE_STEP_ENTITY";
	public static final String TEST_CASE_STEP_ID = "TEST_CASE_STEP_ID";
	public static final String TEST_CASE_TYPE = "TEST_CASE_TYPE";
	public static final String TEST_CASE_TYPE_ENTITY = "TEST_CASE_TYPE_ENTITY";
	public static final String TEST_CASE_TYPE_ID = "TEST_CASE_TYPE_ID";
	public static final String TEST_CASE_TYPE_LABEL = "TEST_CASE_TYPE_LABEL";
	public static final String TEST_CASE_VERSCOUNT = "TEST_CASE_VERSCOUNT";

}
