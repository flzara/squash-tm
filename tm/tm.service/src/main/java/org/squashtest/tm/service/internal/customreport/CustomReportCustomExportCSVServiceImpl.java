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
import org.springframework.stereotype.Service;
import org.squashtest.tm.domain.EntityType;
import org.squashtest.tm.domain.customreport.CustomReportCustomExport;
import org.squashtest.tm.domain.customreport.CustomReportCustomExportColumn;
import org.squashtest.tm.service.customfield.CustomFieldFinderService;
import org.squashtest.tm.service.customreport.CustomReportCustomExportCSVService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.squashtest.tm.domain.customreport.CustomExportColumnLabel.CAMPAIGN_MILESTONE;
import static org.squashtest.tm.domain.customreport.CustomExportColumnLabel.TEST_CASE_MILESTONE;
import static org.squashtest.tm.jooq.domain.Tables.CAMPAIGN;
import static org.squashtest.tm.jooq.domain.Tables.CAMPAIGN_ITERATION;
import static org.squashtest.tm.jooq.domain.Tables.CAMPAIGN_LIBRARY_NODE;
import static org.squashtest.tm.jooq.domain.Tables.CORE_USER;
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
		// Fetch data from database
		return fetchData(customExport.getScope().get(0).getId(), fieldsList, entityList, selectedColumns);
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
	private Iterator<Record> fetchData(long campaignId, Collection<Field<?>> fieldList, List<EntityType> entityList, List<CustomReportCustomExportColumn> selectedColumns) {

		int queryDepth = getQueryDepth(entityList);

		// We need to include the Entity IDs if at least one CustomField linked to this Entity is requested
		if(entityList.contains(EntityType.CAMPAIGN) && !fieldList.contains(CAMPAIGN.CLN_ID))
			fieldList.add(CAMPAIGN.CLN_ID);
		if(entityList.contains(EntityType.ITERATION) && !fieldList.contains(ITERATION.ITERATION_ID))
			fieldList.add(ITERATION.ITERATION_ID);
		if(entityList.contains(EntityType.TEST_SUITE) && !fieldList.contains(TEST_SUITE.ID))
			fieldList.add(TEST_SUITE.ID);
		if(entityList.contains(EntityType.TEST_CASE) && !fieldList.contains(TEST_CASE.TCLN_ID))
			fieldList.add(TEST_CASE.TCLN_ID);
		if(entityList.contains(EntityType.EXECUTION))
			fieldList.add(EXECUTION.EXECUTION_ID);
		if(entityList.contains(EntityType.EXECUTION_STEP))
			fieldList.add(EXECUTION_STEP.EXECUTION_STEP_ID);

		SelectSelectStep selectQuery = DSL.select();
		selectQuery.select(fieldList);

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

		joinQuery.where(CAMPAIGN.CLN_ID.eq(campaignId))

			.groupBy(getGroupByFieldList(queryDepth, selectedColumns))

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

	private Collection<Field<?>> getGroupByFieldList(int queryDepth, List<CustomReportCustomExportColumn> selectedColumns) {
		Set<Field<?>> groupByFieldList = new HashSet<>();

		// Add mandatory primary keys to the group by clause
		groupByFieldList.add(CAMPAIGN.CLN_ID);
		if(queryDepth > 1) {
			groupByFieldList.add(ITERATION.ITERATION_ID);
		}
		if(queryDepth > 2) {
			groupByFieldList.add(ITERATION_TEST_PLAN_ITEM.ITEM_TEST_PLAN_ID);
		}
		if(queryDepth > 3) {
			groupByFieldList.add(EXECUTION.EXECUTION_ID);
		}
		if(queryDepth > 4) {
			groupByFieldList.add(EXECUTION_STEP.EXECUTION_STEP_ID);
		}
		// Add required primary keys to the group by clause according to the selected columns
		for(CustomReportCustomExportColumn column : selectedColumns) {
			if(column.getLabel().getJooqTablePkField() != null) {
				groupByFieldList.add(column.getLabel().getJooqTablePkField());
			}
		}

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
