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
package org.squashtest.tm.service.internal.customreport;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SelectJoinStep;
import org.jooq.SelectOnConditionStep;
import org.jooq.SelectSelectStep;
import org.jooq.TableField;
import org.springframework.stereotype.Service;
import org.squashtest.tm.domain.EntityType;
import org.squashtest.tm.domain.customfield.InputType;
import org.squashtest.tm.domain.customreport.CustomReportCustomExport;
import org.squashtest.tm.domain.customreport.CustomReportCustomExportColumn;
import org.squashtest.tm.service.customfield.CustomFieldFinderService;
import org.squashtest.tm.service.customreport.CustomReportCustomExportCSVService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.groupConcatDistinct;
import static org.squashtest.tm.domain.customreport.CustomExportColumnLabel.CAMPAIGN_MILESTONE;
import static org.squashtest.tm.domain.customreport.CustomExportColumnLabel.EXECUTION_STEP_LINKED_REQUIREMENTS_IDS;
import static org.squashtest.tm.domain.customreport.CustomExportColumnLabel.EXECUTION_STEP_LINKED_REQUIREMENTS_NUMBER;
import static org.squashtest.tm.domain.customreport.CustomExportColumnLabel.ISSUE_EXECUTION_AND_EXECUTION_STEP_ISSUES_IDS;
import static org.squashtest.tm.domain.customreport.CustomExportColumnLabel.ISSUE_EXECUTION_AND_EXECUTION_STEP_ISSUES_NUMBER;
import static org.squashtest.tm.domain.customreport.CustomExportColumnLabel.ISSUE_EXECUTION_STEP_ISSUES_IDS;
import static org.squashtest.tm.domain.customreport.CustomExportColumnLabel.ISSUE_EXECUTION_STEP_ISSUES_NUMBER;
import static org.squashtest.tm.domain.customreport.CustomExportColumnLabel.TEST_CASE_LINKED_REQUIREMENTS_IDS;
import static org.squashtest.tm.domain.customreport.CustomExportColumnLabel.TEST_CASE_LINKED_REQUIREMENTS_NUMBER;
import static org.squashtest.tm.domain.customreport.CustomExportColumnLabel.TEST_CASE_MILESTONE;
import static org.squashtest.tm.jooq.domain.Tables.CAMPAIGN;
import static org.squashtest.tm.jooq.domain.Tables.CAMPAIGN_ITERATION;
import static org.squashtest.tm.jooq.domain.Tables.CAMPAIGN_LIBRARY_NODE;
import static org.squashtest.tm.jooq.domain.Tables.CORE_USER;
import static org.squashtest.tm.jooq.domain.Tables.CUSTOM_FIELD_VALUE;
import static org.squashtest.tm.jooq.domain.Tables.CUSTOM_FIELD_VALUE_OPTION;
import static org.squashtest.tm.jooq.domain.Tables.DATASET;
import static org.squashtest.tm.jooq.domain.Tables.EXECUTION;
import static org.squashtest.tm.jooq.domain.Tables.EXECUTION_EXECUTION_STEPS;
import static org.squashtest.tm.jooq.domain.Tables.EXECUTION_STEP;
import static org.squashtest.tm.jooq.domain.Tables.INFO_LIST_ITEM;
import static org.squashtest.tm.jooq.domain.Tables.ISSUE;
import static org.squashtest.tm.jooq.domain.Tables.ISSUE_LIST;
import static org.squashtest.tm.jooq.domain.Tables.ITEM_TEST_PLAN_EXECUTION;
import static org.squashtest.tm.jooq.domain.Tables.ITEM_TEST_PLAN_LIST;
import static org.squashtest.tm.jooq.domain.Tables.ITERATION;
import static org.squashtest.tm.jooq.domain.Tables.ITERATION_TEST_PLAN_ITEM;
import static org.squashtest.tm.jooq.domain.Tables.MILESTONE;
import static org.squashtest.tm.jooq.domain.Tables.MILESTONE_CAMPAIGN;
import static org.squashtest.tm.jooq.domain.Tables.MILESTONE_TEST_CASE;
import static org.squashtest.tm.jooq.domain.Tables.PROJECT;
import static org.squashtest.tm.jooq.domain.Tables.REQUIREMENT_VERSION_COVERAGE;
import static org.squashtest.tm.jooq.domain.Tables.TEST_CASE;
import static org.squashtest.tm.jooq.domain.Tables.TEST_CASE_LIBRARY_NODE;
import static org.squashtest.tm.jooq.domain.Tables.TEST_SUITE;
import static org.squashtest.tm.jooq.domain.Tables.TEST_SUITE_TEST_PLAN_ITEM;
import static org.squashtest.tm.jooq.domain.Tables.VERIFYING_STEPS;

@Service
public class CustomReportCustomExportCSVServiceImpl implements CustomReportCustomExportCSVService {

	@Inject
	CustomFieldFinderService cufService;

	private static final Map<EntityType, Field> cufJoinColumnMap = new HashMap<>();

	static {
		cufJoinColumnMap.put(EntityType.CAMPAIGN, CAMPAIGN.CLN_ID);
		cufJoinColumnMap.put(EntityType.ITERATION, ITERATION.ITERATION_ID);
		cufJoinColumnMap.put(EntityType.TEST_SUITE, TEST_SUITE.ID);
		cufJoinColumnMap.put(EntityType.TEST_CASE, TEST_CASE.TCLN_ID);
		cufJoinColumnMap.put(EntityType.EXECUTION, EXECUTION.EXECUTION_ID);
		cufJoinColumnMap.put(EntityType.EXECUTION_STEP, EXECUTION_STEP.EXECUTION_STEP_ID);
	}

	@Inject
	private DSLContext DSL;

	@Override
	public Iterator<Record> getRowsData(CustomReportCustomExport customExport) {
		List<CustomReportCustomExportColumn> selectedColumns = customExport.getColumns();
		// Extract EntityTypes from the selected columns
		List<EntityType> entityList = selectedColumns.stream()
			.map(column -> column.getLabel().getEntityType())
			.distinct()
			.collect(Collectors.toList());
		// Extract jooqTableFields from the selected columns
		List<Field<?>> fieldsList = selectedColumns.stream()
			.filter(column -> column.getLabel().getJooqTableField() != null)
			.map(column -> column.getLabel().getJooqTableField())
			.collect(Collectors.toList());
		// Extract Cuf Map group by EntityTypes
		Map<EntityType, List<Long>> cufMap = selectedColumns.stream()
			.filter(column -> column.getCufId() != null)
			.collect(
				Collectors.groupingBy(column->column.getLabel().getEntityType(),
					Collectors.mapping(CustomReportCustomExportColumn::getCufId, Collectors.toList()))
			);
		// Fetch data from database
		return fetchData(customExport.getScope().get(0).getId(), fieldsList, entityList, cufMap);
	}

	/**
	 * Return the depth of the Query.
	 * @param entityTypeList The list of the EntityTypes involved in the Query
	 * @return The depth of the Query
	 */
	private int getQueryDepth(List<EntityType> entityTypeList) {
		if(entityTypeList.contains(EntityType.ISSUE))
			return 6;
		if(entityTypeList.contains(EntityType.EXECUTION_STEP))
			return 5;
		if(entityTypeList.contains(EntityType.EXECUTION))
			return 4;
		if(entityTypeList.contains(EntityType.TEST_CASE) || entityTypeList.contains(EntityType.TEST_SUITE))
			return 3;
		if(entityTypeList.contains(EntityType.ITERATION))
			return 2;
		// default
		return 1;
	}

	private boolean isFetchTestSuite(List<EntityType> entityList) {
		return entityList.contains(EntityType.TEST_SUITE);
	}

	@SuppressWarnings("unchecked")
	private Iterator<Record> fetchData(long campaignId, Collection<Field<?>> fieldList, List<EntityType> entityList, Map<EntityType, List<Long>> cufMap) {

		int queryDepth = getQueryDepth(entityList);

		SelectSelectStep selectQuery = DSL.select();

		selectQuery.select(fieldList);

		// Select cuf columns
		List<Field<?>> cufFieldList = new ArrayList<>();
		for(Map.Entry<EntityType, List<Long>> entry : cufMap.entrySet()) {
			for(Long cufId : entry.getValue()) {
				// Getting cuf type in order to select the right column
				InputType cufType = cufService.findById(cufId).getInputType();
				switch(cufType) {
					case PLAIN_TEXT:
					case DATE_PICKER:
					case CHECKBOX:
					case DROPDOWN_LIST:
						cufFieldList.add(CUSTOM_FIELD_VALUE.as(buildCufColumnAliasName(entry.getKey(), cufId)).VALUE);
						break;
					case NUMERIC:
						cufFieldList.add(CUSTOM_FIELD_VALUE.as(buildCufColumnAliasName(entry.getKey(), cufId)).NUMERIC_VALUE);
						break;
					case RICH_TEXT:
						cufFieldList.add(CUSTOM_FIELD_VALUE.as(buildCufColumnAliasName(entry.getKey(), cufId)).LARGE_VALUE);
						break;
					case TAG:
						cufFieldList.add(
							groupConcatDistinct(CUSTOM_FIELD_VALUE_OPTION.as(buildCufOptionColumnAliasName(entry.getKey(), cufId)).LABEL)
								.separator(", ")
								.as(buildAggregateCufColumnAliasName(entry.getKey(), cufId))
						);
				}
			}
		}
		selectQuery.select(cufFieldList);

		SelectJoinStep fromQuery = selectQuery.from(CAMPAIGN);
		SelectOnConditionStep joinQuery = fromQuery.innerJoin(CAMPAIGN_LIBRARY_NODE).on(CAMPAIGN_LIBRARY_NODE.CLN_ID.eq(CAMPAIGN.CLN_ID));

		if (fieldList.contains(CAMPAIGN_MILESTONE.getJooqTableField())) {
			// only if CAMPAIGN_MILESTONE was selected
			joinQuery.leftJoin(MILESTONE_CAMPAIGN).on(MILESTONE_CAMPAIGN.CAMPAIGN_ID.eq(CAMPAIGN.CLN_ID))
				.leftJoin(MILESTONE.as("camp_milestone")).on(MILESTONE.as("camp_milestone").MILESTONE_ID.eq(MILESTONE_CAMPAIGN.MILESTONE_ID));
		}

		if (queryDepth > 1) {
			joinQuery.leftJoin(CAMPAIGN_ITERATION).on(CAMPAIGN_ITERATION.CAMPAIGN_ID.eq(CAMPAIGN.CLN_ID))
				.leftJoin(ITERATION).on(ITERATION.ITERATION_ID.eq(CAMPAIGN_ITERATION.ITERATION_ID));
		}
		// These 2 lines are redundant compared to the Test_Suite table joined with the ITPIs

//			.leftJoin(ITERATION_TEST_SUITE).on(ITERATION_TEST_SUITE.ITERATION_ID.eq(ITERATION.ITERATION_ID))
//			.leftJoin(TEST_SUITE).on(TEST_SUITE.ID.eq(ITERATION_TEST_SUITE.TEST_SUITE_ID))

		if (queryDepth > 2) {
			joinQuery.leftJoin(ITEM_TEST_PLAN_LIST).on(ITEM_TEST_PLAN_LIST.ITERATION_ID.eq(ITERATION.ITERATION_ID))
				.leftJoin(ITERATION_TEST_PLAN_ITEM).on(ITERATION_TEST_PLAN_ITEM.ITEM_TEST_PLAN_ID.eq(ITEM_TEST_PLAN_LIST.ITEM_TEST_PLAN_ID))

				.leftJoin(CORE_USER).on(CORE_USER.PARTY_ID.eq(ITERATION_TEST_PLAN_ITEM.USER_ID))
				.leftJoin(DATASET).on(DATASET.DATASET_ID.eq(ITERATION_TEST_PLAN_ITEM.DATASET_ID))

				.leftJoin(TEST_CASE).on(TEST_CASE.TCLN_ID.eq(ITERATION_TEST_PLAN_ITEM.TCLN_ID))
				.leftJoin(TEST_CASE_LIBRARY_NODE).on(TEST_CASE_LIBRARY_NODE.TCLN_ID.eq(TEST_CASE.TCLN_ID))
				.leftJoin(PROJECT).on(PROJECT.TCL_ID.eq(TEST_CASE_LIBRARY_NODE.PROJECT_ID));

			if (fieldList.contains(TEST_CASE_MILESTONE.getJooqTableField())) {
				// only if TEST_CASE_MILESTONE was selected
				joinQuery.leftJoin(MILESTONE_TEST_CASE).on(MILESTONE_TEST_CASE.TEST_CASE_ID.eq(TEST_CASE.TCLN_ID))
					.leftJoin(MILESTONE.as("tc_milestone")).on(MILESTONE.as("tc_milestone").MILESTONE_ID.eq(MILESTONE_TEST_CASE.MILESTONE_ID));
			}
			joinQuery.leftJoin(INFO_LIST_ITEM.as("type_list")).on(INFO_LIST_ITEM.as("type_list").ITEM_ID.eq(TEST_CASE.TC_TYPE))
				.leftJoin(INFO_LIST_ITEM.as("type_nature")).on(INFO_LIST_ITEM.as("type_nature").ITEM_ID.eq(TEST_CASE.TC_NATURE))
				.leftJoin(REQUIREMENT_VERSION_COVERAGE.as("tc_rvc")).on(REQUIREMENT_VERSION_COVERAGE.as("tc_rvc").VERIFYING_TEST_CASE_ID.eq(TEST_CASE.TCLN_ID));


			// These four lines are actually not used because TEST_STEPS coverage is included in TEST_CASE coverage...
/*
			.leftJoin(TEST_CASE_STEPS).on(TEST_CASE_STEPS.TEST_CASE_ID.eq(TEST_CASE.TCLN_ID));
			.leftJoin(CALL_TEST_STEP).on(CALL_TEST_STEP.TEST_STEP_ID.eq(TEST_CASE_STEPS.STEP_ID))
			.leftJoin(ACTION_TEST_STEP).on(ACTION_TEST_STEP.TEST_STEP_ID.eq(TEST_CASE_STEPS.STEP_ID))
			.leftJoin(VERIFYING_STEPS.as("ts_vs")).on(VERIFYING_STEPS.as("ts_vs").TEST_STEP_ID.eq(ACTION_TEST_STEP.TEST_STEP_ID))
			.leftJoin(REQUIREMENT_VERSION_COVERAGE.as("ts_rvc")).on(REQUIREMENT_VERSION_COVERAGE.as("ts_rvc").REQUIREMENT_VERSION_COVERAGE_ID.eq(VERIFYING_STEPS.as("ts_vs").REQUIREMENT_VERSION_COVERAGE_ID))
*/
			if (isFetchTestSuite(entityList)) {
				joinQuery.leftJoin(TEST_SUITE_TEST_PLAN_ITEM).on(TEST_SUITE_TEST_PLAN_ITEM.TPI_ID.eq(ITERATION_TEST_PLAN_ITEM.ITEM_TEST_PLAN_ID))
					.leftJoin(TEST_SUITE).on(TEST_SUITE.ID.eq(TEST_SUITE_TEST_PLAN_ITEM.SUITE_ID));
			}
		}
		if (queryDepth > 3) {
			joinQuery.leftJoin(ITEM_TEST_PLAN_EXECUTION).on(ITEM_TEST_PLAN_EXECUTION.ITEM_TEST_PLAN_ID.eq(ITERATION_TEST_PLAN_ITEM.ITEM_TEST_PLAN_ID))
				.leftJoin(EXECUTION).on(EXECUTION.EXECUTION_ID.eq(ITEM_TEST_PLAN_EXECUTION.EXECUTION_ID));
		}
		if (queryDepth > 4) {
			joinQuery.leftJoin(EXECUTION_EXECUTION_STEPS).on(EXECUTION_EXECUTION_STEPS.EXECUTION_ID.eq(EXECUTION.EXECUTION_ID))
				.leftJoin(EXECUTION_STEP).on(EXECUTION_STEP.EXECUTION_STEP_ID.eq(EXECUTION_EXECUTION_STEPS.EXECUTION_STEP_ID))

				.leftJoin(VERIFYING_STEPS.as("es_vs")).on(VERIFYING_STEPS.as("es_vs").TEST_STEP_ID.eq(EXECUTION_STEP.TEST_STEP_ID))
				.leftJoin(REQUIREMENT_VERSION_COVERAGE.as("es_rvc")).on(REQUIREMENT_VERSION_COVERAGE.as("es_rvc").REQUIREMENT_VERSION_COVERAGE_ID.eq(VERIFYING_STEPS.as("es_vs").REQUIREMENT_VERSION_COVERAGE_ID));
		}
		if (queryDepth > 5) {
			joinQuery.leftJoin(ISSUE_LIST.as("es_issue_list")).on(ISSUE_LIST.as("es_issue_list").ISSUE_LIST_ID.eq(EXECUTION_STEP.ISSUE_LIST_ID))
				.leftJoin(ISSUE.as("es_issue")).on(ISSUE.as("es_issue").ISSUE_LIST_ID.eq(ISSUE_LIST.as("es_issue_list").ISSUE_LIST_ID))

				.leftJoin(ISSUE_LIST.as("exec_issue_list")).on(ISSUE_LIST.as("exec_issue_list").ISSUE_LIST_ID.eq(EXECUTION.ISSUE_LIST_ID))
				.leftJoin(EXECUTION_EXECUTION_STEPS.as("side_execution_execution_steps")).on(EXECUTION_EXECUTION_STEPS.as("side_execution_execution_steps").EXECUTION_ID.eq(EXECUTION.EXECUTION_ID))
				.leftJoin(EXECUTION_STEP.as("side_execution_step")).on(EXECUTION_STEP.as("side_execution_step").EXECUTION_STEP_ID.eq(EXECUTION_EXECUTION_STEPS.as("side_execution_execution_steps").EXECUTION_STEP_ID))
				.leftJoin(ISSUE_LIST.as("side_es_issue_list")).on(ISSUE_LIST.as("side_es_issue_list").ISSUE_LIST_ID.eq(EXECUTION_STEP.as("side_execution_step").ISSUE_LIST_ID))
				.leftJoin(ISSUE.as("exec_issue")).on(ISSUE.as("exec_issue").ISSUE_LIST_ID.eq(ISSUE_LIST.as("exec_issue_list").ISSUE_LIST_ID).or(ISSUE.as("exec_issue").ISSUE_LIST_ID.eq(ISSUE_LIST.as("side_es_issue_list").ISSUE_LIST_ID)));
		}

		// Joining on all cuf columns
		for(EntityType entityType : cufMap.keySet()) {
			for (Long cufId : cufMap.get(entityType)) {
				String valueColumnAlias = buildCufColumnAliasName(entityType, cufId);
				String optionColumnAlias = buildCufOptionColumnAliasName(entityType, cufId);
				joinQuery.leftJoin(CUSTOM_FIELD_VALUE.as(valueColumnAlias))
					.on(CUSTOM_FIELD_VALUE.as(valueColumnAlias).BOUND_ENTITY_ID.eq(cufJoinColumnMap.get(entityType)))
					.and(CUSTOM_FIELD_VALUE.as(valueColumnAlias).BOUND_ENTITY_TYPE.eq(entityType.toString()))
					.and(CUSTOM_FIELD_VALUE.as(valueColumnAlias).CF_ID.eq(cufId));
				if(InputType.TAG.equals(cufService.findById(cufId).getInputType())) {
					joinQuery.leftJoin(CUSTOM_FIELD_VALUE_OPTION.as(optionColumnAlias))
						.on(CUSTOM_FIELD_VALUE_OPTION.as(optionColumnAlias).CFV_ID.eq(CUSTOM_FIELD_VALUE.as(valueColumnAlias).CFV_ID));
				}
			}
		}
		joinQuery.where(CAMPAIGN.CLN_ID.eq(campaignId))

			.groupBy(getGroupByFieldList(queryDepth, fieldList, cufFieldList, entityList))

			.orderBy(getOrderByFieldList(queryDepth));

		return joinQuery.fetch().iterator();
	}

	private List<Field<?>> getOrderByFieldList(int queryDepth) {
		List<Field<?>> orderByFieldList = new ArrayList<>();
		if(queryDepth > 1) {
			orderByFieldList.add(ITERATION.ITERATION_ID);
		}
		if(queryDepth > 2) {
			orderByFieldList.add(ITERATION_TEST_PLAN_ITEM.ITEM_TEST_PLAN_ID);
		}
		if(queryDepth > 3) {
			orderByFieldList.add(EXECUTION.EXECUTION_ID);
		}
		if(queryDepth > 4) {
			orderByFieldList.add(EXECUTION_STEP.EXECUTION_STEP_ID);
		}
		return orderByFieldList;
	}

	private List<Field<?>> getGroupByFieldList(int queryDepth, Collection<Field<?>> simpleFieldList, Collection<Field<?>> cufFieldList, List<EntityType> entityList) {
		List<Field<?>> groupByFieldList = new ArrayList<>();
		groupByFieldList.add(CAMPAIGN.CLN_ID);
		if(queryDepth > 1) {
			groupByFieldList.add(ITERATION.ITERATION_ID);
		}
		if(queryDepth > 2) {
			groupByFieldList.add(ITERATION_TEST_PLAN_ITEM.ITEM_TEST_PLAN_ID);
			groupByFieldList.add(CORE_USER.LOGIN);
			groupByFieldList.add(DATASET.NAME);
//			if (simpleFieldList.contains(TEST_CASE_MILESTONE.getJooqTableField())) {
//				groupByFieldList.add(TEST_SUITE.ID);
//			}
			if (isFetchTestSuite(entityList)) {
				groupByFieldList.add(TEST_SUITE.ID);
			}
			groupByFieldList.add(TEST_CASE.TCLN_ID);
			groupByFieldList.add(PROJECT.NAME);
//			if(simpleFieldList.contains(TEST_CASE_MILESTONE.getJooqTableField())) {
//				groupByFieldList.add(MILESTONE.as("tc_milestone").MILESTONE_ID);
//			}
		}
		if(queryDepth > 3) {
			groupByFieldList.add(EXECUTION.EXECUTION_ID);
		}
		if(queryDepth > 4) {
			groupByFieldList.add(EXECUTION_STEP.EXECUTION_STEP_ID);
			groupByFieldList.add(EXECUTION_EXECUTION_STEPS.EXECUTION_STEP_ORDER);
		}

		groupByFieldList.addAll(
			simpleFieldList.stream().filter(field ->
				!field.equals(TEST_CASE_MILESTONE.getJooqTableField()) &&
					!field.equals(TEST_CASE_LINKED_REQUIREMENTS_NUMBER.getJooqTableField()) &&
					!field.equals(TEST_CASE_LINKED_REQUIREMENTS_IDS.getJooqTableField()) &&
					!field.equals(EXECUTION_STEP_LINKED_REQUIREMENTS_NUMBER.getJooqTableField()) &&
					!field.equals(EXECUTION_STEP_LINKED_REQUIREMENTS_IDS.getJooqTableField()) &&
					!field.equals(ISSUE_EXECUTION_AND_EXECUTION_STEP_ISSUES_NUMBER.getJooqTableField()) &&
					!field.equals(ISSUE_EXECUTION_AND_EXECUTION_STEP_ISSUES_IDS.getJooqTableField()) &&
					!field.equals(ISSUE_EXECUTION_STEP_ISSUES_NUMBER.getJooqTableField()) &&
					!field.equals(ISSUE_EXECUTION_STEP_ISSUES_IDS.getJooqTableField())
			).collect(Collectors.toList())
		);
		groupByFieldList.addAll(
			cufFieldList.stream().filter(field -> {
					try {
						((TableField) field).getTable();
						return true;
					} catch (ClassCastException ex) {
						return false;
					}
				}
			).collect(Collectors.toList())
		);

		return groupByFieldList;
	}

	@Override
	public String buildCufColumnAliasName(EntityType entityType, long cufId) {
		return entityType.toString() + "_cuf_" + cufId;
	}

	private String buildCufOptionColumnAliasName(EntityType entityType, long cufId) {
		return entityType.toString() + "_cuf_option_" + cufId;
	}

	@Override
	public String buildAggregateCufColumnAliasName(EntityType entityType, long cufId) {
		return entityType.toString() + "_aggregate_cuf_" + cufId;
	}
}
