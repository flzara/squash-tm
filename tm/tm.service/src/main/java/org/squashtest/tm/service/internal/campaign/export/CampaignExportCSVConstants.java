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
package org.squashtest.tm.service.internal.campaign.export;

public class CampaignExportCSVConstants {

	protected static final String HEADER_CPG_SCHEDULED_START_ON = "CPG_SCHEDULED_START_ON";
	protected static final String HEADER_CPG_SCHEDULED_END_ON = "CPG_SCHEDULED_END_ON";
	protected static final String HEADER_CPG_ACTUAL_START_ON = "CPG_ACTUAL_START_ON";
	protected static final String HEADER_CPG_ACTUAL_END_ON = "CPG_ACTUAL_END_ON";

	// Only used in Standard (S) export, concatenation "#" + IT_NUM + IT_NAME
	protected static final String HEADER_ITERATION = "ITERATION";
	// The 3 headers below are only used in Light (L) and Full (F) export
	protected static final String HEADER_IT_ID = "IT_ID";
	protected static final String HEADER_IT_NUM = "IT_NUM";
	protected static final String HEADER_IT_NAME = "IT_NAME";
	// Only in milestone mode, but this is the campaign milestone, this header is named wrong
	// TODO: RENAME
	protected static final String HEADER_IT_MILESTONE = "IT_MILESTONE";

	protected static final String HEADER_IT_SCHEDULED_START_ON = "IT_SCHEDULED_START_ON";
	protected static final String HEADER_IT_SCHEDULED_END_ON = "IT_SCHEDULED_END_ON";
	protected static final String HEADER_IT_ACTUAL_START_ON = "IT_ACTUAL_START_ON";
	protected static final String HEADER_IT_ACTUAL_END_ON = "IT_ACTUAL_END_ON";
	// Only used in Standard (S) export, name of the test case
	protected static final String HEADER_TEST_CASE = "TEST_CASE";
	// The 2 headers below are only used in Light (L) and Full (F) export
	protected static final String HEADER_TC_ID = "TC_ID";
	protected static final String HEADER_TC_NAME = "TC_NAME";

	// Only used in Full (F) export
	protected static final String HEADER_TC_PROJECT_ID = "TC_PROJECT_ID";
	// Name of the project
	protected static final String HEADER_TC_PROJECT = "TC_PROJECT";
	protected static final String HEADER_TC_MILESTONE = "TC_MILESTONE";
	protected static final String HEADER_TC_WEIGHT = "TC_WEIGHT";
	// Name of the execution's test suite
	protected static final String HEADER_TEST_SUITE = "TEST_SUITE";
	// Amount of executions
	protected static final String HEADER_HASH_EXECUTIONS = "#_EXECUTIONS";
	// Amount of associated requirements
	protected static final String HEADER_HASH_REQUIREMENTS = "#_REQUIREMENTS";
	// Amount of declared issues
	protected static final String HEADER_HASH_ISSUES = "#_ISSUES";
	// Name of the dataset
	protected static final String HEADER_DATASET = "DATASET";

	protected static final String HEADER_EXEC_STATUS = "EXEC_STATUS";
	protected static final String HEADER_EXEC_USER = "EXEC_USER";
	// Only used in Standard (S) export
	protected static final String HEADER_EXEC_SUCCESS_RATE = "EXEC_SUCCESS_RATE";
	protected static final String HEADER_EXECUTION_DATE = "EXECUTION_DATE";
	// Only used in Standard (S) export
	protected static final String HEADER_DESCRIPTION = "DESCRIPTION";

	protected static final String HEADER_TC_REF = "TC_REF";
	protected static final String HEADER_TC_NATURE = "TC_NATURE";
	protected static final String HEADER_TC_TYPE = "TC_TYPE";
	protected static final String HEADER_TC_STATUS = "TC_STATUS";
	// Only used in Standard (S) export
	protected static final String HEADER_PREREQUISITE = "PREREQUISITE";

	// The steps are only exported in Full (F) export
	protected static final String HEADER_STEP_ID = "STEP_ID";
	protected static final String HEADER_STEP_NUM = "STEP_NUM";
	protected static final String HEADER_STEP_REQ = "STEP_#_REQ";
	protected static final String HEADER_EXEC_STEP_STATUS = "EXEC_STEP_STATUS";
	protected static final String HEADER_EXEC_STEP_DATE = "EXEC_STEP_DATE";
	protected static final String HEADER_EXEC_STEP_USER = "EXEC_STEP_USER";
	protected static final String HEADER_EXEC_STEP_ISSUES = "EXEC_STEP_#_ISSUES";
	protected static final String HEADER_EXEC_STEP_COMMENT = "EXEC_STEP_COMMENT";

	protected static final String HEADER_CPG_CUF_ = "CPG_CUF_";
	protected static final String HEADER_IT_CUF_ = "IT_CUF_";
	protected static final String HEADER_TC_CUF_ = "TC_CUF_";
	protected static final String HEADER_EXEC_CUF_ = "HEADER_EXEC_CUF_";
	protected static final String HEADER_STEP_CUF = "STEP_CUF_";

	private CampaignExportCSVConstants() {}


}
