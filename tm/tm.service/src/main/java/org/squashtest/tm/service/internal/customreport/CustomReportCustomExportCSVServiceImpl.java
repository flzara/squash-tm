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
import org.jooq.SelectSelectStep;
import org.jooq.impl.SQLDataType;
import org.springframework.stereotype.Service;
import org.squashtest.tm.domain.EntityReference;
import org.squashtest.tm.domain.EntityType;
import org.squashtest.tm.domain.customreport.CustomExportColumnLabel;
import org.squashtest.tm.domain.customreport.CustomReportCustomExport;
import org.squashtest.tm.domain.customreport.CustomReportCustomExportColumn;
import org.squashtest.tm.jooq.domain.tables.Iteration;
import org.squashtest.tm.service.customfield.CustomFieldFinderService;
import org.squashtest.tm.service.customreport.CustomReportCustomExportCSVService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.concat;
import static org.jooq.impl.DSL.count;
import static org.jooq.impl.DSL.nullif;
import static org.jooq.impl.DSL.val;
import static org.squashtest.tm.domain.customreport.CustomExportColumnLabel.CAMPAIGN_MILESTONE;
import static org.squashtest.tm.domain.customreport.CustomExportColumnLabel.TEST_CASE_DESCRIPTION;
import static org.squashtest.tm.domain.customreport.CustomExportColumnLabel.TEST_CASE_LABEL;
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

	@Inject
	private DSLContext DSL;


	@Override
	public Iterator<Record> getRowsData(CustomReportCustomExport customExport, Set<EntityType> cufEntityList) {
		List<CustomReportCustomExportColumn> selectedColumns = customExport.getColumns();
		// Extract EntityTypes from the selected columns
		List<EntityType> fullEntityList = selectedColumns.stream()
			.map(column -> column.getLabel().getEntityType())
			.distinct()
			.collect(Collectors.toList());

		// Extract jooqTableFields from the selected columns
		List<Field<?>> fieldsList = new ArrayList<>();/*selectedColumns.stream()
			.filter(column -> column.getLabel().getJooqTableField() != null)
			.map(column -> column.getLabel().getJooqTableField())
			.collect(Collectors.toList());*/

		// We need to include the Entity id field in the List if at least one CustomField is requested for this Entity
		for(EntityType entityType : cufEntityList) {
			Field entityIdTableField = CustomExportColumnLabel.getEntityTypeToIdTableFieldMap().get(entityType);
			if(!fieldsList.contains(entityIdTableField)) {
				fieldsList.add(entityIdTableField);
			}
		}
		// Fetch data from database
		EntityReference campaign = customExport.getScope().get(0);
		return fetchData(campaign.getId(), fieldsList, fullEntityList, selectedColumns);
	}

	@SuppressWarnings("unchecked")
	private Iterator<Record> fetchData(long campaignId, Collection<Field<?>> fieldList, List<EntityType> fullEntityList, List<CustomReportCustomExportColumn> selectedColumns) {

		int queryDepth = getQueryDepth(fullEntityList);

		boolean isTestSuiteRequested = fullEntityList.contains(EntityType.TEST_SUITE);

		SelectSelectStep selectQuery = DSL.select(fieldList);

		SelectJoinStep fromQuery = buildFromClauseOfMainQuery(fieldList, isTestSuiteRequested, queryDepth, selectQuery);

		fromQuery.where(CAMPAIGN.CLN_ID.eq(campaignId))

			.groupBy(buildGroupByFieldList(queryDepth, selectedColumns))

			.orderBy(buildOrderByFieldList(queryDepth, isTestSuiteRequested));

		return fromQuery.fetch().iterator();
	}

	/**
	 * Build the From clause of the main Query.
	 * @param fieldList The List of all the requested Fields in the Query
	 * @param isTestSuiteRequested Whether Test Suites are requested in the Query
	 * @param queryDepth The depth of the Query
	 * @param selectQuery The previously built Select clause of the Query
	 * @return The From clause of the Query
	 */
	private SelectJoinStep buildFromClauseOfMainQuery(Collection<Field<?>> fieldList, boolean isTestSuiteRequested, int queryDepth, SelectSelectStep selectQuery) {
		SelectJoinStep fromQuery = selectQuery.from(CAMPAIGN);

		fromQuery.innerJoin(CAMPAIGN_LIBRARY_NODE).on(CAMPAIGN_LIBRARY_NODE.CLN_ID.eq(CAMPAIGN.CLN_ID));

		if (fieldList.contains(CAMPAIGN_MILESTONE.getJooqTableField())) {
			// only if CAMPAIGN_MILESTONE was selected
			fromQuery.leftJoin(MILESTONE_CAMPAIGN).on(MILESTONE_CAMPAIGN.CAMPAIGN_ID.eq(CAMPAIGN.CLN_ID))
				.leftJoin(MILESTONE.as("camp_milestone")).on(MILESTONE.as("camp_milestone").MILESTONE_ID.eq(MILESTONE_CAMPAIGN.MILESTONE_ID));
		}

		if (queryDepth > 1) {
			fromQuery.leftJoin(CAMPAIGN_ITERATION).on(CAMPAIGN_ITERATION.CAMPAIGN_ID.eq(CAMPAIGN.CLN_ID))
				.leftJoin(ITERATION).on(ITERATION.ITERATION_ID.eq(CAMPAIGN_ITERATION.ITERATION_ID));
		}

		if (queryDepth > 2) {
			fromQuery.leftJoin(ITEM_TEST_PLAN_LIST).on(ITEM_TEST_PLAN_LIST.ITERATION_ID.eq(ITERATION.ITERATION_ID))
				.leftJoin(ITERATION_TEST_PLAN_ITEM).on(ITERATION_TEST_PLAN_ITEM.ITEM_TEST_PLAN_ID.eq(ITEM_TEST_PLAN_LIST.ITEM_TEST_PLAN_ID))

				.leftJoin(CORE_USER).on(CORE_USER.PARTY_ID.eq(ITERATION_TEST_PLAN_ITEM.USER_ID))
				.leftJoin(DATASET).on(DATASET.DATASET_ID.eq(ITERATION_TEST_PLAN_ITEM.DATASET_ID))

				.leftJoin(TEST_CASE).on(TEST_CASE.TCLN_ID.eq(ITERATION_TEST_PLAN_ITEM.TCLN_ID))
				.leftJoin(TEST_CASE_LIBRARY_NODE).on(TEST_CASE_LIBRARY_NODE.TCLN_ID.eq(TEST_CASE.TCLN_ID))
				.leftJoin(PROJECT).on(PROJECT.TCL_ID.eq(TEST_CASE_LIBRARY_NODE.PROJECT_ID));

			if (fieldList.contains(TEST_CASE_MILESTONE.getJooqTableField())) {
				// only if TEST_CASE_MILESTONE was selected
				fromQuery.leftJoin(MILESTONE_TEST_CASE).on(MILESTONE_TEST_CASE.TEST_CASE_ID.eq(TEST_CASE.TCLN_ID))
					.leftJoin(MILESTONE.as("tc_milestone")).on(MILESTONE.as("tc_milestone").MILESTONE_ID.eq(MILESTONE_TEST_CASE.MILESTONE_ID));
			}
			fromQuery.leftJoin(INFO_LIST_ITEM.as("type_list")).on(INFO_LIST_ITEM.as("type_list").ITEM_ID.eq(TEST_CASE.TC_TYPE))
				.leftJoin(INFO_LIST_ITEM.as("type_nature")).on(INFO_LIST_ITEM.as("type_nature").ITEM_ID.eq(TEST_CASE.TC_NATURE))
				.leftJoin(REQUIREMENT_VERSION_COVERAGE.as("tc_rvc")).on(REQUIREMENT_VERSION_COVERAGE.as("tc_rvc").VERIFYING_TEST_CASE_ID.eq(TEST_CASE.TCLN_ID));

			if (isTestSuiteRequested) {
				// only if TEST_SUITE attributes were selected
				fromQuery.leftJoin(TEST_SUITE_TEST_PLAN_ITEM).on(TEST_SUITE_TEST_PLAN_ITEM.TPI_ID.eq(ITERATION_TEST_PLAN_ITEM.ITEM_TEST_PLAN_ID))
					.leftJoin(TEST_SUITE).on(TEST_SUITE.ID.eq(TEST_SUITE_TEST_PLAN_ITEM.SUITE_ID));
			}
		}
		if (queryDepth > 3) {
			fromQuery.leftJoin(ITEM_TEST_PLAN_EXECUTION).on(ITEM_TEST_PLAN_EXECUTION.ITEM_TEST_PLAN_ID.eq(ITERATION_TEST_PLAN_ITEM.ITEM_TEST_PLAN_ID))
				.leftJoin(EXECUTION).on(EXECUTION.EXECUTION_ID.eq(ITEM_TEST_PLAN_EXECUTION.EXECUTION_ID));
		}
		if (queryDepth > 4) {
			fromQuery.leftJoin(EXECUTION_EXECUTION_STEPS).on(EXECUTION_EXECUTION_STEPS.EXECUTION_ID.eq(EXECUTION.EXECUTION_ID))
				.leftJoin(EXECUTION_STEP).on(EXECUTION_STEP.EXECUTION_STEP_ID.eq(EXECUTION_EXECUTION_STEPS.EXECUTION_STEP_ID))

				.leftJoin(VERIFYING_STEPS.as("es_vs")).on(VERIFYING_STEPS.as("es_vs").TEST_STEP_ID.eq(EXECUTION_STEP.TEST_STEP_ID))
				.leftJoin(REQUIREMENT_VERSION_COVERAGE.as("es_rvc")).on(REQUIREMENT_VERSION_COVERAGE.as("es_rvc").REQUIREMENT_VERSION_COVERAGE_ID.eq(VERIFYING_STEPS.as("es_vs").REQUIREMENT_VERSION_COVERAGE_ID));
		}
		if (queryDepth > 5) {
			fromQuery.leftJoin(ISSUE_LIST.as("es_issue_list")).on(ISSUE_LIST.as("es_issue_list").ISSUE_LIST_ID.eq(EXECUTION_STEP.ISSUE_LIST_ID))
				.leftJoin(ISSUE.as("es_issue")).on(ISSUE.as("es_issue").ISSUE_LIST_ID.eq(ISSUE_LIST.as("es_issue_list").ISSUE_LIST_ID))

				.leftJoin(ISSUE_LIST.as("exec_issue_list")).on(ISSUE_LIST.as("exec_issue_list").ISSUE_LIST_ID.eq(EXECUTION.ISSUE_LIST_ID))
				.leftJoin(EXECUTION_EXECUTION_STEPS.as("side_execution_execution_steps")).on(EXECUTION_EXECUTION_STEPS.as("side_execution_execution_steps").EXECUTION_ID.eq(EXECUTION.EXECUTION_ID))
				.leftJoin(EXECUTION_STEP.as("side_execution_step")).on(EXECUTION_STEP.as("side_execution_step").EXECUTION_STEP_ID.eq(EXECUTION_EXECUTION_STEPS.as("side_execution_execution_steps").EXECUTION_STEP_ID))
				.leftJoin(ISSUE_LIST.as("side_es_issue_list")).on(ISSUE_LIST.as("side_es_issue_list").ISSUE_LIST_ID.eq(EXECUTION_STEP.as("side_execution_step").ISSUE_LIST_ID))
				.leftJoin(ISSUE.as("exec_issue")).on(ISSUE.as("exec_issue").ISSUE_LIST_ID.eq(ISSUE_LIST.as("exec_issue_list").ISSUE_LIST_ID).or(ISSUE.as("exec_issue").ISSUE_LIST_ID.eq(ISSUE_LIST.as("side_es_issue_list").ISSUE_LIST_ID)));
		}
		return fromQuery;
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

	/**
	 * Build the List of Fields that compose the Order By clause of the Query.
	 * @param queryDepth The depth of the Query
	 * @param isTestSuiteRequested Whether Test Suites are requested in the Query
	 * @return The List of Fields composing the Order By clause of the Query
	 */
	private List<Field<?>> buildOrderByFieldList(int queryDepth, boolean isTestSuiteRequested) {
		List<Field<?>> orderByFieldList = new ArrayList<>();
		if(queryDepth > 1) {
			orderByFieldList.add(ITERATION.ITERATION_ID);
		}
		if(queryDepth > 2) {
			if(isTestSuiteRequested) {
				orderByFieldList.add(TEST_SUITE.ID);
			}
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

	/**
	 * Build the Set of Fields that compose the Group By clause of the Query.
	 * @param queryDepth The depth of the Query
	 * @param selectedColumns All the selected columns in the CustomExport
	 * @return The Set of Fields composing the Group By clause of the Query
	 */
	private Set<Field<?>> buildGroupByFieldList(int queryDepth, List<CustomReportCustomExportColumn> selectedColumns) {
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
			// Some corner cases:
			// - If TEST_CASE_LABEL or TEST_CASE_DESCRIPTION are requested but no TEST_CASE columns are,
			// and since they are linked to TEST_CASE_LIBRARY_NODE table, we must add TEST_CASE_ID in the Group By
			if(TEST_CASE_LABEL.equals(column.getLabel()) || TEST_CASE_DESCRIPTION.equals(column.getLabel())) {
				groupByFieldList.add(TEST_CASE.TCLN_ID);
			}
		}


		return groupByFieldList;
	}

	@Override
	public Object computeCampaignProgressRate(CustomReportCustomExport customExport) {
		long campaignId = customExport.getScope().get(0).getId();
		return getCampaignProgressRateData(campaignId);
	}

	/**
	 * Compute the Campaign progress rate. Formula is (nbrOfItpiDone / totalNbrOfItpi) in the Campaign.
	 * @param campaignId The Campaign id
	 * @return The value of the Campaign progress rate
	 */
	private Object getCampaignProgressRateData(long campaignId) {
		return DSL
			.select(getCampaignProgressRateField(campaignId))
			.fetchOne(getCampaignProgressRateField(campaignId));
	}

	/**
	 * Build the Jooq Field that computes the given Campaign progress rate.
	 * @param campaignId The Campaign id
	 * @return The Jooq Field of the Campaign progress rate
	 */
	private Field getCampaignProgressRateField(long campaignId) {
		return concat(
			org.jooq.impl.DSL.round(
				getItpiDoneCountField(campaignId)
					.div(
						nullif(getItpiTotalCountField(campaignId), 0)).mul(100L), 2)
				.cast(SQLDataType.VARCHAR(5)),
			val(" "),
			val("%"));
	}

	/**
	 * Build the Jooq Field that computes the Number of Itpis Done in the Campaign.
	 * @param campaignId The Campaign id
	 * @return The Jooq Field computing the Number of Itpis Done in the Campaign
	 */
	private Field getItpiDoneCountField(long campaignId) {
		return org.jooq.impl.DSL.select(count(ITERATION_TEST_PLAN_ITEM.ITEM_TEST_PLAN_ID).cast(SQLDataType.DOUBLE))
			.from(CAMPAIGN.as("campaign_itpi_done"))
			.leftJoin(CAMPAIGN_ITERATION).on(CAMPAIGN_ITERATION.CAMPAIGN_ID.eq(CAMPAIGN.as("campaign_itpi_done").CLN_ID))
			.leftJoin(Iteration.ITERATION).on(Iteration.ITERATION.ITERATION_ID.eq(CAMPAIGN_ITERATION.ITERATION_ID))
			.leftJoin(ITEM_TEST_PLAN_LIST).on(ITEM_TEST_PLAN_LIST.ITERATION_ID.eq(Iteration.ITERATION.ITERATION_ID))
			.leftJoin(ITERATION_TEST_PLAN_ITEM).on(ITERATION_TEST_PLAN_ITEM.ITEM_TEST_PLAN_ID.eq(ITEM_TEST_PLAN_LIST.ITEM_TEST_PLAN_ID))
			.where(CAMPAIGN.as("campaign_itpi_done").CLN_ID.eq(campaignId))
			.and(ITERATION_TEST_PLAN_ITEM.EXECUTION_STATUS.in("SETTLED", "UNTESTABLE", "BLOCKED", "FAILURE", "SUCCESS"))
			.asField();
	}
	/**
	 * Build the Jooq Field that computes the Total Number of Itpis in the Campaign.
	 * @param campaignId The Campaign id
	 * @return The Jooq Field computing the Total Number of Itpis in the Campaign
	 */
	private Field getItpiTotalCountField(long campaignId) {
		return org.jooq.impl.DSL.select(count(ITERATION_TEST_PLAN_ITEM.ITEM_TEST_PLAN_ID).cast(SQLDataType.DOUBLE))
			.from(CAMPAIGN.as("campaign_itpi_total"))
			.leftJoin(CAMPAIGN_ITERATION).on(CAMPAIGN_ITERATION.CAMPAIGN_ID.eq(CAMPAIGN.as("campaign_itpi_total").CLN_ID))
			.leftJoin(Iteration.ITERATION).on(Iteration.ITERATION.ITERATION_ID.eq(CAMPAIGN_ITERATION.ITERATION_ID))
			.leftJoin(ITEM_TEST_PLAN_LIST).on(ITEM_TEST_PLAN_LIST.ITERATION_ID.eq(Iteration.ITERATION.ITERATION_ID))
			.leftJoin(ITERATION_TEST_PLAN_ITEM).on(ITERATION_TEST_PLAN_ITEM.ITEM_TEST_PLAN_ID.eq(ITEM_TEST_PLAN_LIST.ITEM_TEST_PLAN_ID))
			.where(CAMPAIGN.as("campaign_itpi_total").CLN_ID.eq(campaignId))
			.asField();
	}



}
