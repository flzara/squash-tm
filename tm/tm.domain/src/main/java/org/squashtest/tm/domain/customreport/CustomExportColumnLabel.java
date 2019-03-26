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
package org.squashtest.tm.domain.customreport;

public enum CustomExportColumnLabel {

	CAMPAIGN_LABEL,
	CAMPAIGN_ID,
	CAMPAIGN_REFERENCE,
	CAMPAIGN_DESCRIPTION,
	CAMPAIGN_STATE,
	CAMPAIGN_PROGRESS_STATUS,
	CAMPAIGN_MILESTONE,
	CAMPAIGN_SCHEDULED_START,
	CAMPAIGN_SCHEDULED_END,
	CAMPAIGN_ACTUAL_START,
	CAMPAIGN_ACTUAL_END,

	ITERATION_LABEL,
	ITERATION_ID,
	ITERATION_REFERENCE,
	ITERATION_DESCRIPTION,
	ITERATION_STATE,
	ITERATION_SCHEDULED_START,
	ITERATION_SCHEDULED_END,
	ITERATION_ACTUAL_START,
	ITERATION_ACTUAL_END,

	TEST_SUITE_LABEL,
	TEST_SUITE_ID,
	TEST_SUITE_DESCRIPTION,
	TEST_SUITE_EXECUTION_STATUS,
	TEST_SUITE_PROGRESS_STATUS,

	TEST_CASE_PROJECT,
	TEST_CASE_MILESTONE,
	TEST_CASE_LABEL,
	TEST_CASE_ID,
	TEST_CASE_REFERENCE,
	TEST_CASE_DESCRIPTION,
	TEST_CASE_STATUS,
	TEST_CASE_IMPORTANCE,
	TEST_CASE_NATURE,
	TEST_CASE_TYPE,
	TEST_CASE_DATASET,
	TEST_CASE_PREREQUISITE,
	TEST_CASE_LINKED_REQUIREMENTS_IDS,

	EXECUTION_EXECUTION_MODE,
	EXECUTION_STATUS,
	EXECUTION_SUCCESS_RATE,
	EXECUTION_USER,
	EXECUTION_EXECUTION_DATE,
	EXECUTION_COMMENT,

	EXECUTION_STEP_STEP_NUMBER,
	EXECUTION_STEP_ACTION,
	EXECUTION_STEP_RESULT,
	EXECUTION_STEP_STATUS,
	EXECUTION_STEP_USER,
	EXECUTION_STEP_EXECUTION_DATE,
	EXECUTION_STEP_COMMENT,
	EXECUTION_STEP_LINKED_REQUIREMENTS_IDS,

	ISSUE_EXECUTION_AND_EXECUTION_STEP_ISSUES,
	ISSUE_EXECUTION_ISSUES

}

