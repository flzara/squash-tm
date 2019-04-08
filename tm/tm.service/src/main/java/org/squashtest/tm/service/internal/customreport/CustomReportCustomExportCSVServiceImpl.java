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

import com.google.common.collect.Lists;
import org.jooq.*;
import org.springframework.stereotype.Service;
import org.squashtest.tm.domain.customreport.CustomReportCustomExport;
import org.squashtest.tm.domain.customreport.CustomReportCustomExportColumn;
import org.squashtest.tm.service.customreport.CustomReportCustomExportCSVService;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.groupConcat;
import static org.jooq.impl.DSL.select;
import static org.squashtest.tm.jooq.domain.Tables.*;

@Service
public class CustomReportCustomExportCSVServiceImpl implements CustomReportCustomExportCSVService {

	@Inject
	private DSLContext DSL;

	@Override
	public String getRowsData(CustomReportCustomExport customExport) {
		List<CustomReportCustomExportColumn> selectedColumns = customExport.getColumns();
		// Extract jooqTableFields from the selected columns
		List<Field<?>> fieldsList = selectedColumns.stream()
			.filter(column -> column.getLabel().getJooqTableField() != null)
			.map(column -> column.getLabel().getJooqTableField())
			.collect(Collectors.toList());
		// Fetch data from database
		Iterator<Record> resultSet = fetchData(customExport.getScope().get(0).getId(), fieldsList);
		// Build the result String
		return buildResultString(resultSet, selectedColumns);
	}

	private String buildResultString(Iterator<Record> resultSet, List<CustomReportCustomExportColumn> selectedColumns) {
		StringBuilder dataBuilder = new StringBuilder();
		resultSet.forEachRemaining(record -> {
				for (CustomReportCustomExportColumn column : selectedColumns) {
					Field columnField = column.getLabel().getJooqTableField();
					if (columnField != null) {
						Object value = record.get(columnField);
						if (value != null) {
							dataBuilder.append("\"")
								.append(value)
								.append("\"")
								.append(";");
						} else {
							dataBuilder.append("N/A;");
						}
					}
				}
				dataBuilder.append("\n");
			}
		);
		return dataBuilder.toString();
	}

	@SuppressWarnings("unchecked")
	private Iterator<Record> fetchData(long campaignId, Collection<Field<?>> fieldList) {
/*		SelectSelectStep query = DSL.select(groupConcat(REQUIREMENT_VERSION_COVERAGE.as("tc_rvc").VERIFIED_REQ_VERSION_ID, ",").as("tc_rvc"));
		query.select(groupConcat(REQUIREMENT_VERSION_COVERAGE.as("es_rvc").VERIFIED_REQ_VERSION_ID, ","));
		query.select(groupConcat(ISSUE.as("exec_issue").ISSUE_ID, ","));
		query.select(groupConcat(ISSUE.as("es_issue").ISSUE_ID, ","));*/
		SelectSelectStep query = DSL.select(fieldList);

		return query
			.from(CAMPAIGN)
			.innerJoin(CAMPAIGN_LIBRARY_NODE).on(CAMPAIGN_LIBRARY_NODE.CLN_ID.eq(CAMPAIGN.CLN_ID))
			.leftJoin(MILESTONE_CAMPAIGN).on(MILESTONE_CAMPAIGN.CAMPAIGN_ID.eq(CAMPAIGN.CLN_ID))
			.leftJoin(MILESTONE.as("camp_milestone")).on(MILESTONE.as("camp_milestone").MILESTONE_ID.eq(MILESTONE_CAMPAIGN.MILESTONE_ID))

			.leftJoin(CAMPAIGN_ITERATION).on(CAMPAIGN_ITERATION.CAMPAIGN_ID.eq(CAMPAIGN.CLN_ID))
			.leftJoin(ITERATION).on(ITERATION.ITERATION_ID.eq(CAMPAIGN_ITERATION.ITERATION_ID))

			// These 2 lines are redundant compared to the Test_Suite table joined with the ITPIs

//			.leftJoin(ITERATION_TEST_SUITE).on(ITERATION_TEST_SUITE.ITERATION_ID.eq(ITERATION.ITERATION_ID))
//			.leftJoin(TEST_SUITE).on(TEST_SUITE.ID.eq(ITERATION_TEST_SUITE.TEST_SUITE_ID))

			.leftJoin(ITEM_TEST_PLAN_LIST).on(ITEM_TEST_PLAN_LIST.ITERATION_ID.eq(ITERATION.ITERATION_ID))
			.leftJoin(ITERATION_TEST_PLAN_ITEM).on(ITERATION_TEST_PLAN_ITEM.ITEM_TEST_PLAN_ID.eq(ITEM_TEST_PLAN_LIST.ITEM_TEST_PLAN_ID))

			.leftJoin(CORE_USER).on(CORE_USER.PARTY_ID.eq(ITERATION_TEST_PLAN_ITEM.USER_ID))
			.leftJoin(DATASET).on(DATASET.DATASET_ID.eq(ITERATION_TEST_PLAN_ITEM.DATASET_ID))

			.leftJoin(TEST_CASE).on(TEST_CASE.TCLN_ID.eq(ITERATION_TEST_PLAN_ITEM.TCLN_ID))
			.leftJoin(TEST_CASE_LIBRARY_NODE).on(TEST_CASE_LIBRARY_NODE.TCLN_ID.eq(TEST_CASE.TCLN_ID))
			.leftJoin(PROJECT).on(PROJECT.TCL_ID.eq(TEST_CASE_LIBRARY_NODE.PROJECT_ID))
			.leftJoin(MILESTONE_TEST_CASE).on(MILESTONE_TEST_CASE.TEST_CASE_ID.eq(TEST_CASE.TCLN_ID))
			.leftJoin(MILESTONE.as("tc_milestone")).on(MILESTONE.as("tc_milestone").MILESTONE_ID.eq(MILESTONE_TEST_CASE.MILESTONE_ID))
			.leftJoin(INFO_LIST_ITEM.as("type_list")).on(INFO_LIST_ITEM.as("type_list").ITEM_ID.eq(TEST_CASE.TC_TYPE))
			.leftJoin(INFO_LIST_ITEM.as("type_nature")).on(INFO_LIST_ITEM.as("type_nature").ITEM_ID.eq(TEST_CASE.TC_NATURE))
			.leftJoin(REQUIREMENT_VERSION_COVERAGE.as("tc_rvc")).on(REQUIREMENT_VERSION_COVERAGE.as("tc_rvc").VERIFYING_TEST_CASE_ID.eq(TEST_CASE.TCLN_ID))

			.leftJoin(TEST_CASE_STEPS).on(TEST_CASE_STEPS.TEST_CASE_ID.eq(TEST_CASE.TCLN_ID))

			// These four lines are actually not used because TEST_STEPS coverage is included in TEST_CASE coverage...

//			.leftJoin(CALL_TEST_STEP).on(CALL_TEST_STEP.TEST_STEP_ID.eq(TEST_CASE_STEPS.STEP_ID))
//			.leftJoin(ACTION_TEST_STEP).on(ACTION_TEST_STEP.TEST_STEP_ID.eq(TEST_CASE_STEPS.STEP_ID))
//			.leftJoin(VERIFYING_STEPS.as("ts_vs")).on(VERIFYING_STEPS.as("ts_vs").TEST_STEP_ID.eq(ACTION_TEST_STEP.TEST_STEP_ID))
//			.leftJoin(REQUIREMENT_VERSION_COVERAGE.as("ts_rvc")).on(REQUIREMENT_VERSION_COVERAGE.as("ts_rvc").REQUIREMENT_VERSION_COVERAGE_ID.eq(VERIFYING_STEPS.as("ts_vs").REQUIREMENT_VERSION_COVERAGE_ID))

			.leftJoin(TEST_SUITE_TEST_PLAN_ITEM).on(TEST_SUITE_TEST_PLAN_ITEM.TPI_ID.eq(ITERATION_TEST_PLAN_ITEM.ITEM_TEST_PLAN_ID))
			.leftJoin(TEST_SUITE).on(TEST_SUITE.ID.eq(TEST_SUITE_TEST_PLAN_ITEM.SUITE_ID))

			.leftJoin(ITEM_TEST_PLAN_EXECUTION).on(ITEM_TEST_PLAN_EXECUTION.ITEM_TEST_PLAN_ID.eq(ITERATION_TEST_PLAN_ITEM.ITEM_TEST_PLAN_ID))
			.leftJoin(EXECUTION).on(EXECUTION.EXECUTION_ID.eq(ITEM_TEST_PLAN_EXECUTION.EXECUTION_ID))

			.leftJoin(EXECUTION_EXECUTION_STEPS).on(EXECUTION_EXECUTION_STEPS.EXECUTION_ID.eq(EXECUTION.EXECUTION_ID))
			.leftJoin(EXECUTION_STEP).on(EXECUTION_STEP.EXECUTION_STEP_ID.eq(EXECUTION_EXECUTION_STEPS.EXECUTION_STEP_ID))

			.leftJoin(VERIFYING_STEPS.as("es_vs")).on(VERIFYING_STEPS.as("es_vs").TEST_STEP_ID.eq(EXECUTION_STEP.TEST_STEP_ID))
			.leftJoin(REQUIREMENT_VERSION_COVERAGE.as("es_rvc")).on(REQUIREMENT_VERSION_COVERAGE.as("es_rvc").REQUIREMENT_VERSION_COVERAGE_ID.eq(VERIFYING_STEPS.as("es_vs").REQUIREMENT_VERSION_COVERAGE_ID))

			.leftJoin(ISSUE_LIST.as("es_issue_list")).on(ISSUE_LIST.as("es_issue_list").ISSUE_LIST_ID.eq(EXECUTION_STEP.ISSUE_LIST_ID))
			.leftJoin(ISSUE.as("es_issue")).on(ISSUE.as("es_issue").ISSUE_LIST_ID.eq(ISSUE_LIST.as("es_issue_list").ISSUE_LIST_ID))

			.leftJoin(ISSUE_LIST.as("exec_issue_list")).on(ISSUE_LIST.as("exec_issue_list").ISSUE_LIST_ID.eq(EXECUTION.ISSUE_LIST_ID))
			.leftJoin(ISSUE.as("exec_issue")).on(ISSUE.as("exec_issue").ISSUE_LIST_ID.eq(ISSUE_LIST.as("exec_issue_list").ISSUE_LIST_ID).or(ISSUE.as("exec_issue").ISSUE_LIST_ID.eq(ISSUE_LIST.as("es_issue_list").ISSUE_LIST_ID)))

			.where(CAMPAIGN.CLN_ID.eq(campaignId))

			.groupBy(getGroupByFieldList(fieldList))

			.orderBy(ITERATION.ITERATION_ID, ITERATION_TEST_PLAN_ITEM.ITEM_TEST_PLAN_ID, EXECUTION_STEP.EXECUTION_STEP_ID)

			.fetch()
			.iterator();
	}

	private List<Field<?>> getGroupByFieldList(Collection<Field<?>> simpleFieldList) {
		List<Field<?>> groupByFieldList = Lists.newArrayList(CAMPAIGN.CLN_ID,
			ITERATION.ITERATION_ID,
			ITERATION_TEST_PLAN_ITEM.ITEM_TEST_PLAN_ID,
			CORE_USER.LOGIN, DATASET.NAME,
			TEST_SUITE.ID,
			TEST_CASE.TCLN_ID, PROJECT.NAME,
			MILESTONE.as("tc_milestone").MILESTONE_ID,
			EXECUTION.EXECUTION_ID,
			EXECUTION_STEP.EXECUTION_STEP_ID,
			EXECUTION_EXECUTION_STEPS.EXECUTION_STEP_ORDER);

		groupByFieldList.addAll(simpleFieldList);

		return groupByFieldList;
	}
}
