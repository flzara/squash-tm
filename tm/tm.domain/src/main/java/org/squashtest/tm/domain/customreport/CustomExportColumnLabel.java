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
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.squashtest.tm.core.foundation.i18n.Internationalizable;
import org.squashtest.tm.domain.EntityType;

import java.util.HashMap;
import java.util.Map;

import static org.jooq.impl.DSL.concat;
import static org.jooq.impl.DSL.count;
import static org.jooq.impl.DSL.countDistinct;
import static org.jooq.impl.DSL.groupConcatDistinct;
import static org.jooq.impl.DSL.nullif;
import static org.jooq.impl.DSL.val;
import static org.squashtest.tm.jooq.domain.Tables.CAMPAIGN;
import static org.squashtest.tm.jooq.domain.Tables.CAMPAIGN_ITERATION;
import static org.squashtest.tm.jooq.domain.Tables.CORE_USER;
import static org.squashtest.tm.jooq.domain.Tables.DATASET;
import static org.squashtest.tm.jooq.domain.Tables.EXECUTION;
import static org.squashtest.tm.jooq.domain.Tables.EXECUTION_EXECUTION_STEPS;
import static org.squashtest.tm.jooq.domain.Tables.EXECUTION_STEP;
import static org.squashtest.tm.jooq.domain.Tables.INFO_LIST_ITEM;
import static org.squashtest.tm.jooq.domain.Tables.ISSUE;
import static org.squashtest.tm.jooq.domain.Tables.ITEM_TEST_PLAN_LIST;
import static org.squashtest.tm.jooq.domain.Tables.ITERATION_TEST_PLAN_ITEM;
import static org.squashtest.tm.jooq.domain.Tables.MILESTONE;
import static org.squashtest.tm.jooq.domain.Tables.PROJECT;
import static org.squashtest.tm.jooq.domain.Tables.REQUIREMENT_VERSION_COVERAGE;
import static org.squashtest.tm.jooq.domain.Tables.TEST_CASE;
import static org.squashtest.tm.jooq.domain.Tables.TEST_CASE_LIBRARY_NODE;
import static org.squashtest.tm.jooq.domain.Tables.TEST_SUITE_TEST_PLAN_ITEM;
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
		Fields.CAMPAIGN_PROGRESS_STATUS,
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

	CAMPAIGN_CUF(
		null,
		null,
		EntityType.CAMPAIGN
	),

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

	ITERATION_CUF(
		null,
		null,
		EntityType.ITERATION
	),

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
		Fields.TEST_SUITE_PROGRESS_STATUS,
		EntityType.TEST_SUITE),

	TEST_SUITE_CUF(
		null,
		null,
		EntityType.TEST_SUITE
	),

	// --- TEST CASE ---
	TEST_CASE_PROJECT(
		"label.project",
		PROJECT.NAME,
		EntityType.TEST_CASE),

	TEST_CASE_MILESTONE(
		I18nKeys.I18N_KEY_MILESTONES,
		groupConcatDistinct(MILESTONE.as("tc_milestone").LABEL).separator(", ").as("tc_milestone_labels"),
		EntityType.TEST_CASE),

	TEST_CASE_LABEL(
		I18nKeys.I18N_KEY_LABEL,
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
		I18nKeys.I18N_KEY_STATUS,
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
		"label.dataset",
		DATASET.NAME,
		EntityType.TEST_CASE),

	TEST_CASE_PREREQUISITE(
		"generics.prerequisite.title",
		TEST_CASE.PREREQUISITE,
		EntityType.TEST_CASE),

	TEST_CASE_LINKED_REQUIREMENTS_NUMBER(
			I18nKeys.I18N_KEY_LINKED_REQUIREMENTS_NUMBER,
		countDistinct(REQUIREMENT_VERSION_COVERAGE.as("tc_rvc").VERIFIED_REQ_VERSION_ID).as("tc_rvc_number"),
		EntityType.TEST_CASE
	),

	TEST_CASE_LINKED_REQUIREMENTS_IDS(
		I18nKeys.I18N_KEY_CUSTOM_EXPORT_COLUMN_LINKED_REQUIREMENTS_IDS,
		groupConcatDistinct(REQUIREMENT_VERSION_COVERAGE.as("tc_rvc").VERIFIED_REQ_VERSION_ID).separator(", ").as("tc_rvc_ids"),
		EntityType.TEST_CASE),

	TEST_CASE_CUF(
		null,
		null,
		EntityType.TEST_CASE
	),

	// --- EXECUTION ---
	EXECUTION_EXECUTION_MODE(
		"label.ExecutionMode",
		EXECUTION.EXECUTION_MODE,
		EntityType.EXECUTION),

	EXECUTION_STATUS(
		I18nKeys.I18N_KEY_STATUS,
		EXECUTION.EXECUTION_STATUS,
		EntityType.EXECUTION),

	EXECUTION_SUCCESS_RATE(
		"shortLabel.SuccessRate",
		Fields.EXECUTION_SUCCESS_RATE,
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

	EXECUTION_CUF(
		null,
		null,
		EntityType.EXECUTION
	),

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
		I18nKeys.I18N_KEY_STATUS,
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

	EXECUTION_STEP_LINKED_REQUIREMENTS_NUMBER(
		I18nKeys.I18N_KEY_LINKED_REQUIREMENTS_NUMBER,
		countDistinct(REQUIREMENT_VERSION_COVERAGE.as("es_rvc").VERIFIED_REQ_VERSION_ID).as("es_rvc_number"),
		EntityType.EXECUTION_STEP
	),

	EXECUTION_STEP_LINKED_REQUIREMENTS_IDS(
		I18nKeys.I18N_KEY_CUSTOM_EXPORT_COLUMN_LINKED_REQUIREMENTS_IDS,
		groupConcatDistinct(REQUIREMENT_VERSION_COVERAGE.as("es_rvc").VERIFIED_REQ_VERSION_ID).separator(", ").as("es_rvc_ids"),
		EntityType.EXECUTION_STEP),

	EXECUTION_STEP_CUF(
		null,
		null,
		EntityType.EXECUTION_STEP
	),

	// --- ISSUE ---
	ISSUE_EXECUTION_AND_EXECUTION_STEP_ISSUES_NUMBER(
		"custom-export.wizard.attributes.ISSUE.ALL_LINKED_ISSUES_COUNT",
		countDistinct(ISSUE.as("exec_issue").ISSUE_ID).as("exec_and_es_issue_number"),
		EntityType.ISSUE
	),

	ISSUE_EXECUTION_AND_EXECUTION_STEP_ISSUES_IDS(
		"label.Execution",
		groupConcatDistinct(ISSUE.as("exec_issue").ISSUE_ID).separator(", ").as("exec_and_es_issue_ids"),
		EntityType.ISSUE),

	ISSUE_EXECUTION_STEP_ISSUES_NUMBER(
		"custom-export.wizard.attributes.ISSUE.STEP_LINKED_ISSUES_COUNT",
		countDistinct(ISSUE.as("es_issue").ISSUE_ID).as("es_issue_number"),
		EntityType.ISSUE
	),

	ISSUE_EXECUTION_STEP_ISSUES_IDS(
		"chart.entityType.EXECUTION_STEP",
		groupConcatDistinct(ISSUE.as("es_issue").ISSUE_ID).separator(", ").as("es_issue_ids"),
		EntityType.ISSUE);

	private String i18nKey;
	private Field jooqTableField;
	/**
	 * The EntityType corresponding to the column.
	 */
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

	public String getShortenedEntityType() {
		return ShortenedNames.getShortenedEntityType(entityType);
	}

	private static final class I18nKeys {
		private static final String I18N_KEY_ACTUAL_END = "dialog.label.campaign.actual_end.label";
		private static final String I18N_KEY_ACTUAL_START = "dialog.label.campaign.actual_start.label";
		private static final String I18N_KEY_LINKED_REQUIREMENTS_NUMBER = "custom-export.column.LINKED_REQUIREMENTS_COUNT";
		private static final String I18N_KEY_CUSTOM_EXPORT_COLUMN_LINKED_REQUIREMENTS_IDS = "custom-export.column.LINKED_REQUIREMENTS_IDS";
		private static final String I18N_KEY_DESCRIPTION = "label.Description";
		private static final String I18N_KEY_ID = "label.id";
		private static final String I18N_KEY_LABEL = "label.Label";
		private static final String I18N_KEY_MILESTONE = "label.Milestone";
		private static final String I18N_KEY_MILESTONES = "label.Milestones";
		private static final String I18N_KEY_PROGRESS_STATUS = "campaign.progress_status.label";
		private static final String I18N_KEY_REFERENCE = "label.Reference";
		private static final String I18N_KEY_SCHEDULED_END = "dialog.label.campaign.scheduled_end.label";
		private static final String I18N_KEY_SCHEDULED_START = "dialog.label.campaign.scheduled_start.label";
		private static final String I18N_KEY_STATE = "label.State";
		private static final String I18N_KEY_STATUS = "label.Status";
		private static final String I18N_KEY_USER = "label.User";
	}

	private static final class Fields {

		private final static Field FIELD_ITPI_DONE_COUNT =
			DSL.select(count(ITERATION_TEST_PLAN_ITEM.ITEM_TEST_PLAN_ID).cast(SQLDataType.DOUBLE))
				.from(CAMPAIGN.as("campaign_itpi_done"))
				.leftJoin(CAMPAIGN_ITERATION).on(CAMPAIGN_ITERATION.CAMPAIGN_ID.eq(CAMPAIGN.as("campaign_itpi_done").CLN_ID))
				.leftJoin(ITERATION).on(ITERATION.ITERATION_ID.eq(CAMPAIGN_ITERATION.ITERATION_ID))
				.leftJoin(ITEM_TEST_PLAN_LIST).on(ITEM_TEST_PLAN_LIST.ITERATION_ID.eq(ITERATION.ITERATION_ID))
				.leftJoin(ITERATION_TEST_PLAN_ITEM).on(ITERATION_TEST_PLAN_ITEM.ITEM_TEST_PLAN_ID.eq(ITEM_TEST_PLAN_LIST.ITEM_TEST_PLAN_ID))
				.where(CAMPAIGN.as("campaign_itpi_done").CLN_ID.eq(CAMPAIGN.CLN_ID))
				.and(ITERATION_TEST_PLAN_ITEM.EXECUTION_STATUS.in("SETTLED", "UNTESTABLE", "BLOCKED", "FAILURE", "SUCCESS"))
				.asField();

		private final static Field FIELD_ITPI_TOTAL_COUNT =
			DSL.select(count(ITERATION_TEST_PLAN_ITEM.ITEM_TEST_PLAN_ID).cast(SQLDataType.DOUBLE))
				.from(CAMPAIGN.as("campaign_itpi_total"))
				.leftJoin(CAMPAIGN_ITERATION).on(CAMPAIGN_ITERATION.CAMPAIGN_ID.eq(CAMPAIGN.as("campaign_itpi_total").CLN_ID))
				.leftJoin(ITERATION).on(ITERATION.ITERATION_ID.eq(CAMPAIGN_ITERATION.ITERATION_ID))
				.leftJoin(ITEM_TEST_PLAN_LIST).on(ITEM_TEST_PLAN_LIST.ITERATION_ID.eq(ITERATION.ITERATION_ID))
				.leftJoin(ITERATION_TEST_PLAN_ITEM).on(ITERATION_TEST_PLAN_ITEM.ITEM_TEST_PLAN_ID.eq(ITEM_TEST_PLAN_LIST.ITEM_TEST_PLAN_ID))
				.where(CAMPAIGN.as("campaign_itpi_total").CLN_ID.eq(CAMPAIGN.CLN_ID))
				.asField();

		private final static Field CAMPAIGN_PROGRESS_STATUS = concat(
			DSL.round(FIELD_ITPI_DONE_COUNT.div(nullif(FIELD_ITPI_TOTAL_COUNT, 0)).mul(100L), 2)
				.cast(SQLDataType.VARCHAR(5)),
			val(" "),
			val("%"));

		private final static Field FIELD_TEST_SUITE_ITPI_DONE_COUNT =
			DSL.select(count(ITERATION_TEST_PLAN_ITEM.ITEM_TEST_PLAN_ID).cast(SQLDataType.DOUBLE))
				.from(TEST_SUITE.as("suite_itpi_done"))
				.leftJoin(TEST_SUITE_TEST_PLAN_ITEM).on(TEST_SUITE_TEST_PLAN_ITEM.SUITE_ID.eq(TEST_SUITE.as("suite_itpi_done").ID))
				.leftJoin(ITERATION_TEST_PLAN_ITEM).on(ITERATION_TEST_PLAN_ITEM.ITEM_TEST_PLAN_ID.eq(TEST_SUITE_TEST_PLAN_ITEM.TPI_ID))
				.where(TEST_SUITE.as("suite_itpi_done").ID.eq(TEST_SUITE.ID))
				.and(ITERATION_TEST_PLAN_ITEM.EXECUTION_STATUS.in("SETTLED", "UNTESTABLE", "BLOCKED", "FAILURE", "SUCCESS"))
				.asField();

		private final static Field FIELD_TEST_SUITE_ITPI_TOTAL_COUNT =
			DSL.select(count(ITERATION_TEST_PLAN_ITEM.ITEM_TEST_PLAN_ID).cast(SQLDataType.DOUBLE))
				.from(TEST_SUITE.as("suite_itpi_total"))
				.leftJoin(TEST_SUITE_TEST_PLAN_ITEM).on(TEST_SUITE_TEST_PLAN_ITEM.SUITE_ID.eq(TEST_SUITE.as("suite_itpi_total").ID))
				.leftJoin(ITERATION_TEST_PLAN_ITEM).on(ITERATION_TEST_PLAN_ITEM.ITEM_TEST_PLAN_ID.eq(TEST_SUITE_TEST_PLAN_ITEM.TPI_ID))
				.where(TEST_SUITE.as("suite_itpi_total").ID.eq(TEST_SUITE.ID))
				.asField();

		private final static Field TEST_SUITE_PROGRESS_STATUS = concat(
			DSL.round(FIELD_TEST_SUITE_ITPI_DONE_COUNT.div(nullif(FIELD_TEST_SUITE_ITPI_TOTAL_COUNT, 0)).mul(100L), 2)
				.cast(SQLDataType.VARCHAR(5)),
			val(" "),
			val("%"));

		private final static Field FIELD_EXECUTION_STEP_SUCCESS_COUNT =
			DSL.select(count(EXECUTION_STEP.EXECUTION_STEP_ID).cast(SQLDataType.DOUBLE))
				.from(EXECUTION.as("exec_step_done"))
				.leftJoin(EXECUTION_EXECUTION_STEPS).on(EXECUTION_EXECUTION_STEPS.EXECUTION_ID.eq(EXECUTION.as("exec_step_done").EXECUTION_ID))
				.leftJoin(EXECUTION_STEP).on(EXECUTION_STEP.EXECUTION_STEP_ID.eq(EXECUTION_EXECUTION_STEPS.EXECUTION_STEP_ID))
				.where(EXECUTION.as("exec_step_done").EXECUTION_ID.eq(EXECUTION.EXECUTION_ID))
				.and(EXECUTION_STEP.EXECUTION_STATUS.eq("SUCCESS"))
				.asField();

		private final static Field FIELD_EXECUTION_STEP_TOTAL_COUNT =
			DSL.select(count(EXECUTION_STEP.EXECUTION_STEP_ID).cast(SQLDataType.DOUBLE))
				.from(EXECUTION.as("exec_step_total"))
				.leftJoin(EXECUTION_EXECUTION_STEPS).on(EXECUTION_EXECUTION_STEPS.EXECUTION_ID.eq(EXECUTION.as("exec_step_total").EXECUTION_ID))
				.leftJoin(EXECUTION_STEP).on(EXECUTION_STEP.EXECUTION_STEP_ID.eq(EXECUTION_EXECUTION_STEPS.EXECUTION_STEP_ID))
				.where(EXECUTION.as("exec_step_total").EXECUTION_ID.eq(EXECUTION.EXECUTION_ID))
				.asField();

		private final static Field EXECUTION_SUCCESS_RATE = concat(
			DSL.round(FIELD_EXECUTION_STEP_SUCCESS_COUNT.div(nullif(FIELD_EXECUTION_STEP_TOTAL_COUNT, 0)).mul(100L), 2)
			.cast(SQLDataType.VARCHAR(5)),
			val(" "),
			val("%"));
	}

	private static final class ShortenedNames {

		private static final Map<EntityType, String> ENTITY_TYPE_TO_SHORTEN_ENTITY_NAME_MAP = new HashMap<>(7);

		static {
			ENTITY_TYPE_TO_SHORTEN_ENTITY_NAME_MAP.put(EntityType.CAMPAIGN, "CPG");
			ENTITY_TYPE_TO_SHORTEN_ENTITY_NAME_MAP.put(EntityType.ITERATION, "IT");
			ENTITY_TYPE_TO_SHORTEN_ENTITY_NAME_MAP.put(EntityType.TEST_SUITE, "SUI");
			ENTITY_TYPE_TO_SHORTEN_ENTITY_NAME_MAP.put(EntityType.TEST_CASE, "TC");
			ENTITY_TYPE_TO_SHORTEN_ENTITY_NAME_MAP.put(EntityType.EXECUTION, "EXEC");
			ENTITY_TYPE_TO_SHORTEN_ENTITY_NAME_MAP.put(EntityType.EXECUTION_STEP, "EXEC_STEP");
			ENTITY_TYPE_TO_SHORTEN_ENTITY_NAME_MAP.put(EntityType.ISSUE, "BUG");
		}

		private static String getShortenedEntityType(EntityType entityType) {
			return ENTITY_TYPE_TO_SHORTEN_ENTITY_NAME_MAP.get(entityType);
		}
	}
}

