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

import org.apache.commons.collections.map.MultiValueMap;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.TableField;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.jooq.domain.tables.Milestone;
import org.squashtest.tm.jooq.domain.tables.records.*;
import org.squashtest.tm.service.feature.FeatureManager;
import org.squashtest.tm.service.internal.dto.*;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.*;

import static org.squashtest.tm.jooq.domain.Tables.*;
import static org.squashtest.tm.jooq.domain.Tables.MILESTONE;
import static org.squashtest.tm.jooq.domain.Tables.TEST_SUITE;

public abstract class AbstractCampaignExportCSVModel implements WritableCampaignCSVModel {

	static final TableField<IterationRecord, Long> ITERATION_ID = ITERATION.ITERATION_ID;

	static final TableField<IterationRecord, String> ITERATION_NAME = ITERATION.NAME;

	static final TableField<IterationRecord, Timestamp> ITERATION_SCHEDULED_END_DATE = ITERATION.SCHEDULED_END_DATE;

	static final TableField<IterationRecord, Timestamp> ITERATION_SCHEDULED_START_DATE = ITERATION.SCHEDULED_START_DATE;

	static final TableField<IterationRecord, Timestamp> ITERATION_ACTUAL_END_DATE = ITERATION.ACTUAL_END_DATE;

	static final TableField<IterationRecord, Timestamp> ITERATION_ACTUAL_START_DATE = ITERATION.ACTUAL_START_DATE;

	static final TableField<IterationTestPlanItemRecord, Long> ITPI_ID = ITERATION_TEST_PLAN_ITEM.ITEM_TEST_PLAN_ID;

	static final TableField<IterationTestPlanItemRecord, String> ITPI_STATUS = ITERATION_TEST_PLAN_ITEM.EXECUTION_STATUS;

	static final TableField<CoreUserRecord, String> USER_LOGIN = CORE_USER.LOGIN;

	static final TableField<IterationTestPlanItemRecord, Timestamp> ITPI_LAST_EXECUTED_ON = ITERATION_TEST_PLAN_ITEM.LAST_EXECUTED_ON;

	static final TableField<ItemTestPlanExecutionRecord, Long> ITPI_EXECUTION = ITEM_TEST_PLAN_EXECUTION.EXECUTION_ID;

	static final TableField<DatasetRecord, String> DATASET_NAME = DATASET.NAME;

	static final TableField<TestCaseRecord, Long> TC_ID = TEST_CASE.TCLN_ID;

	static final TableField<TestCaseRecord, String> TC_IMPORTANCE = TEST_CASE.IMPORTANCE;

	static final TableField<TestCaseRecord, String> TC_REFERENCE = TEST_CASE.REFERENCE;

	static final TableField<InfoListItemRecord, String> TC_NATURE = INFO_LIST_ITEM.as("info_list_1").CODE;

	static final TableField<InfoListItemRecord, String> TC_TYPE = INFO_LIST_ITEM.as("info_list_2").CODE;

	static final TableField<TestCaseRecord, String> TC_STATUS = TEST_CASE.TC_STATUS;

	static final TableField<TestCaseRecord, String> TC_PREREQUISITE = TEST_CASE.PREREQUISITE;

	static final TableField<TestCaseLibraryNodeRecord, String> TC_DESCRIPTION = TEST_CASE_LIBRARY_NODE.DESCRIPTION;

	static final TableField<RequirementVersionCoverageRecord, Long> TC_REQUIREMENT_VERIFIED = REQUIREMENT_VERSION_COVERAGE.REQUIREMENT_VERSION_COVERAGE_ID;

	static final TableField<TestCaseLibraryNodeRecord, String> TC_NAME = TEST_CASE_LIBRARY_NODE.NAME;

	static final TableField<ProjectRecord, Long> PROJECT_ID = PROJECT.PROJECT_ID;

	static final TableField<ProjectRecord, String> PROJECT_NAME = PROJECT.NAME;

	static final TableField<IssueRecord, Long> ITPI_ISSUE = ISSUE.ISSUE_ID;

	static final TableField<TestSuiteRecord, String> TS_NAME = TEST_SUITE.NAME;

	static final Milestone TC_MILESTONE = MILESTONE.as("tc_milestone");

	static final Milestone IT_MILESTONE = MILESTONE.as("it_milestone");

	static final TableField<ExecutionRecord, Long> EXECUTION_ID = EXECUTION.EXECUTION_ID;

	static final TableField<ExecutionRecord, String> EXECUTION_MODE = EXECUTION.EXECUTION_MODE;

	static final TableField<ExecutionRecord, String> EXECUTION_STATUS = EXECUTION.EXECUTION_STATUS;

	static final TableField<ExecutionStepRecord, Long> EXECUTION_STEP_ID = EXECUTION_STEP.EXECUTION_STEP_ID;

	static final TableField<ExecutionStepRecord, String> EXECUTION_STEP_STATUS = EXECUTION_STEP.EXECUTION_STATUS;

	@Inject
	private FeatureManager featureManager;

	@Inject
	protected DSLContext DSL;

	char separator = ';';

	int nbColumns;

	boolean milestonesEnabled;

	Campaign campaign;
	CampaignDto campaignDto;

	List<CustomFieldDto> campCUFModel = new ArrayList<>();
	SortedSet<CustomFieldDto> iterCUFModel = new TreeSet<>(Comparator.comparing(CustomFieldDto::getId));
	SortedSet<CustomFieldDto> tcCUFModel = new TreeSet<>(Comparator.comparing(CustomFieldDto::getId));
	SortedSet<CustomFieldDto> execCUFModel = new TreeSet<>(Comparator.comparing(CustomFieldDto::getId));

	Map<Long, CustomFieldValueDto> campCUFValues = new HashMap<>();
	MultiValueMap iterCUFValues = new MultiValueMap(); // <Long, Collection<CustomFieldValueDto>>
	MultiValueMap tcCUFValues = new MultiValueMap(); // same here
	MultiValueMap execCUFValues = new MultiValueMap(); // same here

	public AbstractCampaignExportCSVModel() {
		super();

	}

	@Override
	public void setCampaign(Campaign campaign) {
		this.campaign = campaign;
	}

	@Override
	public void setSeparator(char separator) {
		this.separator = separator;
	}

	@Override
	public char getSeparator() {
		return separator;
	}

	@Override
	public void init() {
		campaignDto = createCampaignDto(campaign);
		initIterationsAndCustomFields();
		milestonesEnabled = featureManager.isEnabled(FeatureManager.Feature.MILESTONE);
	}

	private CampaignDto createCampaignDto(Campaign campaign) {
		return new CampaignDto(campaign.getId(), campaign.getScheduledStartDate(), campaign.getScheduledEndDate(), campaign.getActualStartDate(), campaign.getActualEndDate());
	}

	abstract void initIterationsAndCustomFields();

	abstract Iterator<Record> getIterationJooqQueryIterator();

	void populateCUFModelAndCufValues(String entityType, Collection<CustomFieldDto> cufModel, MultiValueMap cufValues, Collection<Long> entityIdList) {
		DSL.select(CUSTOM_FIELD_VALUE.CFV_ID, CUSTOM_FIELD_VALUE.BOUND_ENTITY_ID, CUSTOM_FIELD_VALUE.CF_ID, CUSTOM_FIELD_VALUE.VALUE,
			CUSTOM_FIELD.CODE, CUSTOM_FIELD.INPUT_TYPE)
			.from(CUSTOM_FIELD_VALUE)
			.innerJoin(CUSTOM_FIELD_BINDING).on(CUSTOM_FIELD_BINDING.CFB_ID.eq(CUSTOM_FIELD_VALUE.CFB_ID))
			.innerJoin(CUSTOM_FIELD).on(CUSTOM_FIELD.CF_ID.eq(CUSTOM_FIELD_BINDING.CF_ID))
			.where((CUSTOM_FIELD_VALUE.BOUND_ENTITY_ID.in(entityIdList)).and(CUSTOM_FIELD_VALUE.BOUND_ENTITY_TYPE.eq(entityType)))
			.fetch().forEach(r ->{
			CustomFieldDto newCFDto = new CustomFieldDto(r.get(CUSTOM_FIELD_VALUE.CF_ID), r.get(CUSTOM_FIELD.CODE), r.get(CUSTOM_FIELD.INPUT_TYPE));
			cufModel.add(newCFDto);

			CustomFieldValueDto newCFVDto = new CustomFieldValueDto(r.get(CUSTOM_FIELD_VALUE.CFV_ID), r.get(CUSTOM_FIELD_VALUE.BOUND_ENTITY_ID), r.get(CUSTOM_FIELD_VALUE.CF_ID), r.get(CUSTOM_FIELD_VALUE.VALUE));
			cufValues.put(r.get(CUSTOM_FIELD_VALUE.BOUND_ENTITY_ID), newCFVDto);
		});
	}

	void populateCampCUFModelAndCampCUFValues() {
		DSL.select(CUSTOM_FIELD_VALUE.CFV_ID, CUSTOM_FIELD_VALUE.BOUND_ENTITY_ID, CUSTOM_FIELD_VALUE.CF_ID, CUSTOM_FIELD_VALUE.VALUE,
			CUSTOM_FIELD.CODE, CUSTOM_FIELD.INPUT_TYPE)
			.from(CUSTOM_FIELD_VALUE)
			.innerJoin(CUSTOM_FIELD_BINDING).on(CUSTOM_FIELD_BINDING.CFB_ID.eq(CUSTOM_FIELD_VALUE.CFB_ID))
			.innerJoin(CUSTOM_FIELD).on(CUSTOM_FIELD.CF_ID.eq(CUSTOM_FIELD_BINDING.CF_ID))
			.where((CUSTOM_FIELD_VALUE.BOUND_ENTITY_ID.eq(campaign.getId())).and(CUSTOM_FIELD_VALUE.BOUND_ENTITY_TYPE.eq("CAMPAIGN")))
			.fetch().forEach(r ->{
			CustomFieldDto newCFDto = new CustomFieldDto(r.get(CUSTOM_FIELD_VALUE.CF_ID), r.get(CUSTOM_FIELD.CODE), r.get(CUSTOM_FIELD.INPUT_TYPE));
			campCUFModel.add(newCFDto);

			CustomFieldValueDto newCFVDto = new CustomFieldValueDto(r.get(CUSTOM_FIELD_VALUE.CFV_ID), r.get(CUSTOM_FIELD_VALUE.BOUND_ENTITY_ID), r.get(CUSTOM_FIELD_VALUE.CF_ID), r.get(CUSTOM_FIELD_VALUE.VALUE));
			campCUFValues.put(r.get(CUSTOM_FIELD_VALUE.CF_ID), newCFVDto);
		});
	}


}