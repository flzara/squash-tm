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

import org.jooq.Field;
import org.squashtest.tm.core.foundation.i18n.Internationalizable;
import org.squashtest.tm.domain.EntityType;

import static org.jooq.impl.DSL.groupConcatDistinct;
import static org.squashtest.tm.jooq.domain.Tables.*;
import static org.squashtest.tm.jooq.domain.tables.CampaignLibraryNode.CAMPAIGN_LIBRARY_NODE;
import static org.squashtest.tm.jooq.domain.tables.Iteration.ITERATION;
import static org.squashtest.tm.jooq.domain.tables.TestSuite.TEST_SUITE;

public enum CustomExportColumnLabel implements Internationalizable {

	// --- CAMPAIGN ---
	CAMPAIGN_LABEL(
		I18nKeys.I18N_KEY_LABEL,
		CAMPAIGN_LIBRARY_NODE.NAME,
		EntityType.CAMPAIGN),

	CAMPAIGN_ID(
		I18nKeys.I18N_KEY_ID,
		CAMPAIGN.CLN_ID,
		EntityType.CAMPAIGN),

	CAMPAIGN_REFERENCE(
		I18nKeys.I18N_KEY_REFERENCE,
		CAMPAIGN.REFERENCE,
		EntityType.CAMPAIGN),

	CAMPAIGN_DESCRIPTION(
		I18nKeys.I18N_KEY_DESCRIPTION,
		CAMPAIGN_LIBRARY_NODE.DESCRIPTION,
		EntityType.CAMPAIGN),

	CAMPAIGN_STATE(
		I18nKeys.I18N_KEY_STATE,
		CAMPAIGN.CAMPAIGN_STATUS,
		EntityType.CAMPAIGN),

	CAMPAIGN_PROGRESS_STATUS(
		I18nKeys.I18N_KEY_PROGRESS_STATUS,
		null,
		EntityType.CAMPAIGN),

	CAMPAIGN_MILESTONE(
		I18nKeys.I18N_KEY_MILESTONE,
		MILESTONE.as("camp_milestone").LABEL,
		EntityType.CAMPAIGN),

	CAMPAIGN_SCHEDULED_START(
		I18nKeys.I18N_KEY_SCHEDULED_START,
		CAMPAIGN.SCHEDULED_START_DATE,
		EntityType.CAMPAIGN),

	CAMPAIGN_SCHEDULED_END(
		I18nKeys.I18N_KEY_SCHEDULED_END,
		CAMPAIGN.SCHEDULED_END_DATE,
		EntityType.CAMPAIGN),

	CAMPAIGN_ACTUAL_START(
		I18nKeys.I18N_KEY_ACTUAL_START,
		CAMPAIGN.ACTUAL_START_DATE,
		EntityType.CAMPAIGN),

	CAMPAIGN_ACTUAL_END(
		I18nKeys.I18N_KEY_ACTUAL_END,
		CAMPAIGN.ACTUAL_END_DATE,
		EntityType.CAMPAIGN),

	// --- ITERATION ---
	ITERATION_LABEL(
		I18nKeys.I18N_KEY_LABEL,
		ITERATION.NAME,
		EntityType.ITERATION),

	ITERATION_ID(
		I18nKeys.I18N_KEY_ID,
		ITERATION.ITERATION_ID,
		EntityType.ITERATION),

	ITERATION_REFERENCE(
		I18nKeys.I18N_KEY_REFERENCE,
		ITERATION.REFERENCE,
		EntityType.ITERATION),

	ITERATION_DESCRIPTION(
		I18nKeys.I18N_KEY_DESCRIPTION,
		ITERATION.DESCRIPTION,
		EntityType.ITERATION),

	ITERATION_STATE(
		I18nKeys.I18N_KEY_STATE,
		ITERATION.ITERATION_STATUS,
		EntityType.ITERATION),

	ITERATION_SCHEDULED_START(
		I18nKeys.I18N_KEY_SCHEDULED_START,
		ITERATION.SCHEDULED_START_DATE,
		EntityType.ITERATION),

	ITERATION_SCHEDULED_END(
		I18nKeys.I18N_KEY_SCHEDULED_END,
		ITERATION.SCHEDULED_END_DATE,
		EntityType.ITERATION),

	ITERATION_ACTUAL_START(
		I18nKeys.I18N_KEY_ACTUAL_START,
		ITERATION.ACTUAL_START_DATE,
		EntityType.ITERATION),

	ITERATION_ACTUAL_END(
		I18nKeys.I18N_KEY_ACTUAL_END,
		ITERATION.ACTUAL_END_DATE,
		EntityType.ITERATION),

	// -- TEST SUITE ---
	TEST_SUITE_LABEL(
		I18nKeys.I18N_KEY_LABEL,
		TEST_SUITE.NAME,
		EntityType.TEST_SUITE),

	TEST_SUITE_ID(
		I18nKeys.I18N_KEY_ID,
		TEST_SUITE.ID,
		EntityType.TEST_SUITE),

	TEST_SUITE_DESCRIPTION(
		I18nKeys.I18N_KEY_DESCRIPTION,
		TEST_SUITE.DESCRIPTION,
		EntityType.TEST_SUITE),

	TEST_SUITE_EXECUTION_STATUS(
		"chart.column.EXECUTION_STATUS",
		TEST_SUITE.EXECUTION_STATUS,
		EntityType.TEST_SUITE),

	TEST_SUITE_PROGRESS_STATUS(
		"test-suite.progress_status.label",
		null,
		EntityType.TEST_SUITE),

	// --- TEST CASE ---
	TEST_CASE_PROJECT(
		"label.project",
		PROJECT.NAME,
		EntityType.TEST_CASE),

	TEST_CASE_MILESTONE(
		I18nKeys.I18N_KEY_MILESTONE,
		MILESTONE.as("tc_milestone").LABEL,
		EntityType.TEST_CASE),

	TEST_CASE_LABEL(
		I18nKeys.I18N_KEY_ID,
		TEST_CASE_LIBRARY_NODE.NAME,
		EntityType.TEST_CASE),

	TEST_CASE_ID(
		I18nKeys.I18N_KEY_ID,
		TEST_CASE.TCLN_ID,
		EntityType.TEST_CASE),

	TEST_CASE_REFERENCE(
		I18nKeys.I18N_KEY_REFERENCE,
		TEST_CASE.REFERENCE,
		EntityType.TEST_CASE),

	TEST_CASE_DESCRIPTION(
		I18nKeys.I18N_KEY_DESCRIPTION,
		TEST_CASE_LIBRARY_NODE.DESCRIPTION,
		EntityType.TEST_CASE),

	TEST_CASE_STATUS(
		I18nKeys.LABEL_STATUS,
		TEST_CASE.TC_STATUS,
		EntityType.TEST_CASE),

	TEST_CASE_IMPORTANCE(
		"label.Importance",
		TEST_CASE.IMPORTANCE,
		EntityType.TEST_CASE),

	TEST_CASE_NATURE(
		"chart.column.TEST_CASE_NATURE",
		INFO_LIST_ITEM.as("type_nature").LABEL,
		EntityType.TEST_CASE),

	TEST_CASE_TYPE(
		"label.Type",
		INFO_LIST_ITEM.as("type_list").LABEL,
		EntityType.TEST_CASE),

	TEST_CASE_DATASET(
		"label.Dataset",
		DATASET.NAME,
		EntityType.TEST_CASE),

	TEST_CASE_PREREQUISITE(
		"generics.prerequisite.title",
		TEST_CASE.PREREQUISITE,
		EntityType.TEST_CASE),

	TEST_CASE_LINKED_REQUIREMENTS_IDS(
		"custom-export.column.TEST_CASE.LINKED_REQUIREMENTS_IDS",
		groupConcatDistinct(REQUIREMENT_VERSION_COVERAGE.as("tc_rvc").VERIFIED_REQ_VERSION_ID).separator(", ").as("tc_rvc_ids"),
		EntityType.TEST_CASE),

	// --- EXECUTION ---
	EXECUTION_EXECUTION_MODE(
		"label.ExecutionMode",
		EXECUTION.EXECUTION_MODE,
		EntityType.EXECUTION),

	EXECUTION_STATUS(
		I18nKeys.LABEL_STATUS,
		EXECUTION.EXECUTION_STATUS,
		EntityType.EXECUTION),

	EXECUTION_SUCCESS_RATE(
		"shortLabel.SuccessRate",
		null,
		EntityType.EXECUTION),

	EXECUTION_USER(
		I18nKeys.I18N_KEY_USER,
		CORE_USER.LOGIN,
		EntityType.EXECUTION),

	EXECUTION_EXECUTION_DATE(
		"iteration.executions.table.column-header.execution-date.label",
		EXECUTION.LAST_EXECUTED_ON,
		EntityType.EXECUTION),

	EXECUTION_COMMENT(
		"executions.steps.table.column-header.comment.label",
		EXECUTION.DESCRIPTION,
		EntityType.EXECUTION),

	// --- EXECUTION STEP ---
	EXECUTION_STEP_STEP_NUMBER(
		"custom-export.column.EXECUTION_STEP.EXECUTION_STEP_NUMBER",
		EXECUTION_EXECUTION_STEPS.EXECUTION_STEP_ORDER,
		EntityType.EXECUTION_STEP),

	EXECUTION_STEP_ACTION(
		"label.action",
		EXECUTION_STEP.ACTION,
		EntityType.EXECUTION_STEP),

	EXECUTION_STEP_RESULT(
		"custom-export.column.EXECUTION_STEP.RESULT",
		EXECUTION_STEP.EXPECTED_RESULT,
		EntityType.EXECUTION_STEP),

	EXECUTION_STEP_STATUS(
		I18nKeys.LABEL_STATUS,
		EXECUTION_STEP.EXECUTION_STATUS,
		EntityType.EXECUTION_STEP),

	EXECUTION_STEP_USER(
		I18nKeys.I18N_KEY_USER,
		CORE_USER.LOGIN,
		EntityType.EXECUTION_STEP),

	EXECUTION_STEP_EXECUTION_DATE(
		"iteration.executions.table.column-header.execution-date.label",
		EXECUTION_STEP.LAST_EXECUTED_ON,
		EntityType.EXECUTION_STEP),

	EXECUTION_STEP_COMMENT(
		"executions.steps.table.column-header.comment.label",
		EXECUTION_STEP.COMMENT,
		EntityType.EXECUTION_STEP),

	EXECUTION_STEP_LINKED_REQUIREMENTS_IDS(
		"custom-export.column.EXECUTION_STEP.STEP_LINKED_REQUIREMENTS_IDS",
		groupConcatDistinct(REQUIREMENT_VERSION_COVERAGE.as("es_rvc").VERIFIED_REQ_VERSION_ID).separator(", ").as("es_rvc_ids"),
		EntityType.EXECUTION_STEP),

	// --- ISSUE ---
	ISSUE_EXECUTION_AND_EXECUTION_STEP_ISSUES(
		"custom-export.column.ISSUE.ALL_LINKED_ISSUES",
		groupConcatDistinct(ISSUE.as("exec_issue").ISSUE_ID).separator(", ").as("exec_and_es_issue_ids"),
		EntityType.ISSUE),

	ISSUE_EXECUTION_ISSUES(
		"custom-export.column.ISSUE.STEP_LINKED_ISSUES",
		groupConcatDistinct(ISSUE.as("es_issue").ISSUE_ID).separator(", ").as("es_issue_ids"),
		EntityType.ISSUE);

	private String i18nKey;
	private Field jooqTableField;
	private EntityType entityType;

	CustomExportColumnLabel(String i18nKey, Field jooqTableField, EntityType entityType) {
		this.i18nKey = i18nKey;
		this.jooqTableField = jooqTableField;
		this.entityType = entityType;
	}

	@Override
	public String getI18nKey() {
		return i18nKey;
	}

	public Field getJooqTableField() {
		return jooqTableField;
	}

	public EntityType getEntityType() {
		return entityType;
	}

	private static class I18nKeys {
		public static final String I18N_KEY_LABEL = "label.Label";
		public static final String I18N_KEY_ID = "label.id";
		public static final String I18N_KEY_REFERENCE = "label.Reference";
		public static final String I18N_KEY_DESCRIPTION = "label.Description";
		public static final String I18N_KEY_STATE = "label.State";
		public static final String I18N_KEY_PROGRESS_STATUS = "campaign.progress_status.label";
		public static final String I18N_KEY_MILESTONE = "label.Milestone";
		public static final String I18N_KEY_SCHEDULED_START = "chart.column.CAMPAIGN_SCHED_START";
		public static final String I18N_KEY_SCHEDULED_END = "chart.column.CAMPAIGN_SCHED_END";
		public static final String I18N_KEY_ACTUAL_START = "chart.column.CAMPAIGN_ACTUAL_START";
		public static final String I18N_KEY_ACTUAL_END = "chart.column.CAMPAIGN_ACTUAL_END";
		public static final String I18N_KEY_USER = "label.User";
		public static final String LABEL_STATUS = "label.Status";
	}
}

