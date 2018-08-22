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

import java.text.SimpleDateFormat;
import java.util.*;

import org.jooq.Record;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.squashtest.tm.service.internal.dto.*;

import static org.squashtest.tm.jooq.domain.Tables.*;

@Component
@Scope("prototype")
public class SimpleCampaignExportCSVModelImpl extends AbstractCampaignExportCSVModel {

	public SimpleCampaignExportCSVModelImpl() {
		super();

	}

	void initIterationsAndCustomFields() {

		Iterator<Record> iterator = getIterationJooqQueryIterator();

		List<Long> allTestCaseIds = populateCampaignDto(iterator);

		Set<Long> allIterationIds = campaignDto.getIterationMap().keySet();

		// cufs for the campaign
		populateCampCUFModelAndCampCUFValues();

		// cufs for the iterations
		populateCUFModelAndCufValues("ITERATION", iterCUFModel, iterCUFValues, allIterationIds);

		// cufs for the test cases
		populateCUFModelAndCufValues("TEST_CASE", tcCUFModel, tcCUFValues, allTestCaseIds);

		nbColumns = 25 + campCUFModel.size() + iterCUFModel.size() + tcCUFModel.size();

	}

	Iterator<Record> getIterationJooqQueryIterator() {
		return
			DSL.select(
			ITERATION_ID, ITERATION_NAME, ITERATION_SCHEDULED_END_DATE, ITERATION_SCHEDULED_START_DATE, ITERATION_ACTUAL_END_DATE, ITERATION_ACTUAL_START_DATE,
			ITPI_ID, ITPI_STATUS, USER_LOGIN, ITPI_LAST_EXECUTED_ON, ITPI_EXECUTION, DATASET_NAME, IT_MILESTONE.LABEL,
			TC_ID, TC_IMPORTANCE, TC_REFERENCE, TC_NATURE, TC_TYPE, TC_STATUS, TC_REQUIREMENT_VERIFIED,
			TC_NAME, PROJECT_ID, PROJECT_NAME, ITPI_ISSUE,
			TS_NAME, TC_MILESTONE.LABEL)
			.from(ITERATION)
			.leftJoin(CAMPAIGN_ITERATION).on(ITERATION_ID.eq(CAMPAIGN_ITERATION.ITERATION_ID))
			.leftJoin(CAMPAIGN).on(CAMPAIGN.CLN_ID.eq(CAMPAIGN_ITERATION.CAMPAIGN_ID))
			.leftJoin(ITEM_TEST_PLAN_LIST).on(ITEM_TEST_PLAN_LIST.ITERATION_ID.eq(ITERATION_ID))
			.leftJoin(ITERATION_TEST_PLAN_ITEM).on(ITPI_ID.eq(ITEM_TEST_PLAN_LIST.ITEM_TEST_PLAN_ID))
			.leftJoin(TEST_CASE).on(TC_ID.eq(ITERATION_TEST_PLAN_ITEM.TCLN_ID))
			.leftJoin(TEST_CASE_LIBRARY_NODE).on(TEST_CASE_LIBRARY_NODE.TCLN_ID.eq(TC_ID))
			.leftJoin(PROJECT).on(PROJECT_ID.eq(TEST_CASE_LIBRARY_NODE.PROJECT_ID))
			.leftJoin(REQUIREMENT_VERSION_COVERAGE).on(REQUIREMENT_VERSION_COVERAGE.VERIFYING_TEST_CASE_ID.eq(TC_ID))
			.leftJoin(DATASET).on(DATASET.DATASET_ID.eq(ITERATION_TEST_PLAN_ITEM.DATASET_ID))
			.leftJoin(ITEM_TEST_PLAN_EXECUTION).on(ITEM_TEST_PLAN_EXECUTION.ITEM_TEST_PLAN_ID.eq(ITPI_ID))
			.leftJoin(EXECUTION).on(EXECUTION.EXECUTION_ID.eq(ITEM_TEST_PLAN_EXECUTION.EXECUTION_ID))
			.leftJoin(ISSUE_LIST).on(ISSUE_LIST.ISSUE_LIST_ID.eq(EXECUTION.ISSUE_LIST_ID))
			.leftJoin(ISSUE).on(ISSUE.ISSUE_LIST_ID.eq(ISSUE_LIST.ISSUE_LIST_ID))
			.leftJoin(TEST_SUITE_TEST_PLAN_ITEM).on(TEST_SUITE_TEST_PLAN_ITEM.TPI_ID.eq(ITPI_ID))
			.leftJoin(TEST_SUITE).on(TEST_SUITE.ID.eq(TEST_SUITE_TEST_PLAN_ITEM.SUITE_ID))
			.leftJoin(MILESTONE_TEST_CASE).on(MILESTONE_TEST_CASE.TEST_CASE_ID.eq(TC_ID))
			.leftJoin(TC_MILESTONE).on(TC_MILESTONE.MILESTONE_ID.eq(MILESTONE_TEST_CASE.MILESTONE_ID))
			.leftJoin(MILESTONE_CAMPAIGN).on(MILESTONE_CAMPAIGN.CAMPAIGN_ID.eq(CAMPAIGN.CLN_ID))
			.leftJoin(IT_MILESTONE).on(IT_MILESTONE.MILESTONE_ID.eq(MILESTONE_CAMPAIGN.MILESTONE_ID))
			.leftJoin(CORE_USER).on(CORE_USER.PARTY_ID.eq(ITERATION_TEST_PLAN_ITEM.USER_ID))
			.leftJoin(INFO_LIST_ITEM.as("info_list_1")).on(INFO_LIST_ITEM.as("info_list_1").ITEM_ID.eq(TEST_CASE.TC_TYPE))
			.leftJoin(INFO_LIST_ITEM.as("info_list_2")).on(INFO_LIST_ITEM.as("info_list_2").ITEM_ID.eq(TEST_CASE.TC_NATURE))
			.where(CAMPAIGN.CLN_ID.eq(campaign.getId()))
			.orderBy(ITERATION_ID, TC_ID)
			.fetch().iterator();
	}

	private List<Long> populateCampaignDto(Iterator<Record> iterator) {

		List<Long> allTestCases = new ArrayList<>();

		IterationDto currentIteration = new IterationDto();
		ITPIDto currentItpi = new ITPIDto();
		TestCaseDto currentTestCase = new TestCaseDto();

		while (iterator.hasNext()){
			Record r = iterator.next();
			if(campaignDto.getIteration(r.get(ITERATION_ID)) == null){
				campaignDto.addIteration(
					new IterationDto(r.get(ITERATION_ID), r.get(ITERATION_NAME), r.get(ITERATION_SCHEDULED_START_DATE), r.get(ITERATION_SCHEDULED_END_DATE), r.get(ITERATION_ACTUAL_START_DATE), r.get(ITERATION_ACTUAL_END_DATE))
				);
				currentIteration = campaignDto.getIteration(r.get(ITERATION_ID));
			}
			if(r.get(IT_MILESTONE.LABEL) != null){
				currentIteration.addMilestone(r.get(IT_MILESTONE.LABEL));
			}
			if(currentIteration.getTestPlan(r.get(ITPI_ID)) == null){

				ITPIDto newItpi = createNewItpiDto(r);

				TestCaseDto newTestCase = createNewTestCaseDto(r);

				allTestCases.add(r.get(TC_ID));

				newItpi.setTestCase(newTestCase);

				currentIteration.addTestPlan(newItpi);
				currentItpi = currentIteration.getTestPlan(r.get(ITPI_ID));

				currentTestCase = currentItpi.getTestCase();

			} else {

				populateItpi(r, currentItpi);

				populateTestCase(r, currentTestCase);

			}
		}

		return allTestCases;
	}

	private void populateItpi(Record r, ITPIDto itpi) {
		if(r.get(TS_NAME) != null){
			itpi.getTestSuiteSet().add(r.get(TS_NAME));
		}

		if(r.get(ITPI_EXECUTION) != null){
			itpi.addExecution(new ExecutionDto(r.get(ITPI_EXECUTION)));
		}

		if(r.get(ITPI_ISSUE) != null){
			itpi.addIssue(r.get(ITPI_ISSUE));
		}
	}

	private void populateTestCase(Record r, TestCaseDto currentTestCase) {

		if(r.get(TC_MILESTONE.LABEL) != null){
			currentTestCase.addMilestone(r.get(TC_MILESTONE.LABEL));
		}

		if(r.get(TC_REQUIREMENT_VERIFIED) != null){
			currentTestCase.addRequirement(r.get(TC_REQUIREMENT_VERIFIED));
		}
	}

	private TestCaseDto createNewTestCaseDto(Record r) {
		TestCaseDto newTestCase = new TestCaseDto(r.get(TC_ID), r.get(TC_REFERENCE), r.get(TC_NAME), r.get(TC_IMPORTANCE), r.get(TC_NATURE), r.get(TC_TYPE), r.get(TC_STATUS), r.get(PROJECT_ID), r.get(PROJECT_NAME));

		populateTestCase(r, newTestCase);

		return newTestCase;
	}

	private ITPIDto createNewItpiDto(Record r) {
		ITPIDto newItpi = new ITPIDto(r.get(ITPI_ID), r.get(ITPI_STATUS), r.get(USER_LOGIN), r.get(ITPI_LAST_EXECUTED_ON));

		populateItpi(r, newItpi);

		if(r.get(DATASET_NAME) != null){
			newItpi.setDataset(r.get(DATASET_NAME));
		}

		return newItpi;
	}

	@Override
	public Row getHeader() {

		List<CellImpl> headerCells = new ArrayList<>(nbColumns);

		// campaign fixed fields
		headerCells.add(new CellImpl("CPG_SCHEDULED_START_ON"));
		headerCells.add(new CellImpl("CPG_SCHEDULED_END_ON"));
		headerCells.add(new CellImpl("CPG_ACTUAL_START_ON"));
		headerCells.add(new CellImpl("CPG_ACTUAL_END_ON"));

		// iteration fixed fields
		headerCells.add(new CellImpl("IT_ID"));
		headerCells.add(new CellImpl("IT_NUM"));
		headerCells.add(new CellImpl("IT_NAME"));
		if (milestonesEnabled) {
			headerCells.add(new CellImpl("IT_MILESTONE"));
		}
		headerCells.add(new CellImpl("IT_SCHEDULED_START_ON"));
		headerCells.add(new CellImpl("IT_SCHEDULED_END_ON"));
		headerCells.add(new CellImpl("IT_ACTUAL_START_ON"));
		headerCells.add(new CellImpl("IT_ACTUAL_END_ON"));

		// test case fixed fields
		headerCells.add(new CellImpl("TC_ID"));
		headerCells.add(new CellImpl("TC_NAME"));
		headerCells.add(new CellImpl("TC_PROJECT_ID"));
		headerCells.add(new CellImpl("TC_PROJECT"));
		if (milestonesEnabled) {
			headerCells.add(new CellImpl("TC_MILESTONE"));
		}
		headerCells.add(new CellImpl("TC_WEIGHT"));
		headerCells.add(new CellImpl("TEST_SUITE"));
		headerCells.add(new CellImpl("#_EXECUTIONS"));
		headerCells.add(new CellImpl("#_REQUIREMENTS"));
		headerCells.add(new CellImpl("#_ISSUES"));
		headerCells.add(new CellImpl("DATASET"));
		headerCells.add(new CellImpl("EXEC_STATUS"));
		headerCells.add(new CellImpl("EXEC_USER"));
		headerCells.add(new CellImpl("EXECUTION_DATE"));
		headerCells.add(new CellImpl("TC_REF"));
		headerCells.add(new CellImpl("TC_NATURE"));
		headerCells.add(new CellImpl("TC_TYPE"));
		headerCells.add(new CellImpl("TC_STATUS"));

		campCUFModel.sort(Comparator.comparing(CustomFieldDto::getId));

		// campaign custom fields
		for (CustomFieldDto cufModel : campCUFModel) {
			headerCells.add(new CellImpl("CPG_CUF_" + cufModel.getCode()));
		}

		// iteration custom fields
		for (CustomFieldDto cufModel : iterCUFModel) {
			headerCells.add(new CellImpl("IT_CUF_" + cufModel.getCode()));
		}

		// test case custom fields
		for (CustomFieldDto cufModel : tcCUFModel) {
			headerCells.add(new CellImpl("TC_CUF_" + cufModel.getCode()));
		}

		return new RowImpl(headerCells, separator);

	}

	@Override
	public Iterator<Row> dataIterator() {
		return new DataIterator();
	}

	// ********************************** nested classes ********************************************

	private class DataIterator implements Iterator<Row> {

		private int iterIndex = -1;
		private int itpIndex = -1;

		private IterationDto iteration = new IterationDto(); // initialized to dummy value for for bootstrap purposes
		private ITPIDto itp; // null means "no more"

		private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

		public DataIterator() {

			super();
			moveNext();

		}

		@Override
		public boolean hasNext() {

			return itp != null;

		}

		// See getHeader() for reference
		@Override
		public Row next() {

			List<CellImpl> dataCells = new ArrayList<>(nbColumns);

			// the campaign
			populateCampaignRowData(dataCells);

			// the iteration
			populateIterationRowData(dataCells);

			// the test case
			populateTestCaseRowData(dataCells);

			// custom fields
			populateCustomFields(dataCells);

			// move to the next occurence
			moveNext();

			return new RowImpl(dataCells, separator);

		}

		@SuppressWarnings("unchecked")
		private void populateCustomFields(List<CellImpl> dataCells) {

			// ensure that the CUF values are processed in the correct order
			for (CustomFieldDto model : campCUFModel) {
				String strValue = getCampaignCufValue(campCUFValues.get(model.getId()), model);
				dataCells.add(new CellImpl(strValue));
			}

			Collection<CustomFieldValueDto> iValues = (Collection<CustomFieldValueDto>) iterCUFValues.get(iteration.getId());
			for (CustomFieldDto model : iterCUFModel) {
				String strValue = getValue(iValues, model);
				dataCells.add(new CellImpl(strValue));
			}

			TestCaseDto testCase = itp.getTestCase();

			Collection<CustomFieldValueDto> tcValues = (Collection<CustomFieldValueDto>) tcCUFValues.get(testCase.getId());
			for (CustomFieldDto model : tcCUFModel) {
				String strValue = getValue(tcValues, model);
				dataCells.add(new CellImpl(strValue));
			}
		}

		private void populateTestCaseRowData(List<CellImpl> dataCells) {

			TestCaseDto testCase = itp.getTestCase();
			dataCells.add(new CellImpl(testCase.getId().toString()));
			dataCells.add(new CellImpl(testCase.getName()));
			dataCells.add(new CellImpl(testCase.getProjectId().toString()));
			dataCells.add(new CellImpl(testCase.getProjectName()));
			if (milestonesEnabled) {
				dataCells.add(new CellImpl(formatMilestone(testCase.getMilestoneSet())));
			}
			dataCells.add(new CellImpl(testCase.getImportance()));
			dataCells.add(new CellImpl(itp.getTestSuiteNames().replace(", ", ",").replace("<", "&lt;").replace(">", "&gt;")));
			dataCells.add(new CellImpl(Integer.toString(itp.getExecutionMap().size())));
			dataCells.add(new CellImpl(Integer.toString(testCase.getRequirementSet().size())));
			dataCells.add(new CellImpl(Integer.toString(itp.getIssueSet().size())));
			dataCells.add(new CellImpl(itp.getDataset()));
			dataCells.add(new CellImpl(itp.getStatus()));
			dataCells.add(new CellImpl(itp.getUserName()));
			dataCells.add(new CellImpl(formatDate(itp.getLastExecutedOn())));
			dataCells.add(new CellImpl(testCase.getReference()));
			dataCells.add(new CellImpl(testCase.getNature()));
			dataCells.add(new CellImpl(testCase.getType()));
			dataCells.add(new CellImpl(testCase.getStatus()));
		}

		private String formatMilestone(Set<String> milestones) {

			StringBuilder sb = new StringBuilder();
			for (String m : milestones) {
				sb.append(m);
				sb.append("|");
			}
			sb.setLength(Math.max(sb.length() - 1, 0));

			return sb.toString();
		}

		private void populateIterationRowData(List<CellImpl> dataCells) {

			dataCells.add(new CellImpl(iteration.getId().toString()));
			dataCells.add(new CellImpl(Integer.toString(iterIndex + 1)));
			dataCells.add(new CellImpl(iteration.getName()));
			if (milestonesEnabled) {
				dataCells.add(new CellImpl(formatMilestone(iteration.getMilestoneSet())));
			}
			dataCells.add(new CellImpl(formatDate(iteration.getScheduledStartDate())));
			dataCells.add(new CellImpl(formatDate(iteration.getScheduledEndDate())));
			dataCells.add(new CellImpl(formatDate(iteration.getActualStartDate())));
			dataCells.add(new CellImpl(formatDate(iteration.getActualEndDate())));


		}

		private void populateCampaignRowData(List<CellImpl> dataCells) {
			dataCells.add(new CellImpl(formatDate(campaignDto.getScheduledStartDate())));
			dataCells.add(new CellImpl(formatDate(campaignDto.getScheduledEndDate())));
			dataCells.add(new CellImpl(formatDate(campaignDto.getActualStartDate())));
			dataCells.add(new CellImpl(formatDate(campaignDto.getActualEndDate())));
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		// ******************************** data formatting ***************************

		private String getCampaignCufValue(CustomFieldValueDto customFieldValueDto, CustomFieldDto model) {
			if(customFieldValueDto != null && customFieldValueDto.getValue() != null){
				if(model.getInputType().equals("NUMERIC")){
					return NumericCufHelper.formatOutputNumericCufValue(customFieldValueDto.getValue());
				}
				return customFieldValueDto.getValue();
			}
			return "";
		}
		// returns the correct value if found, or "" if not found
		private String getValue(Collection<CustomFieldValueDto> values, CustomFieldDto model) {

			if (values != null) {
				return formatOutputValue(values, model);
			}
			return "";
		}

		private String formatOutputValue(Collection<CustomFieldValueDto> values ,CustomFieldDto model) {
			for (CustomFieldValueDto value : values) {
				Long customFieldId = value.getCufId();
				if (customFieldId == model.getId()) {
					if (model.getInputType().equals("NUMERIC")) {
						return NumericCufHelper.formatOutputNumericCufValue(value.getValue());
					}
					return value.getValue();
				}
			}
			return "";
		}

		private String formatDate(Date date) {

			return date == null ? "" : dateFormat.format(date);

		}


		// ****************** iterator mechanics here ****************

		private void moveNext() {

			boolean moveITPSuccess = moveToNextTestCase();

			if (!moveITPSuccess) {

				boolean moveIterSuccess = moveToNextIteration();

				if (moveIterSuccess) {
					moveNext();
				} else {
					itp = null; // terminal state
				}

			}

		}

		// returns true if could move the pointer to the next iteration
		// returns false if there are no more iterations to visit
		private boolean moveToNextIteration() {

			iterIndex++;
			if (campaignDto.getIterationList().size() > iterIndex) {
				iteration = campaignDto.getIterationList().get(iterIndex);
				itpIndex = -1;

				return true;
			} else {
				return false;
			}

		}

		// returns true if the current iteration had a next test case
		// returns false if the current iteration had no more.
		// if successful, the inner pointer to the next test case will be set accordingly
		private boolean moveToNextTestCase() {

			ITPIDto nextITP = null;

			List<ITPIDto> items = iteration.getTestPlanList();
			int nbItems = items.size();

			do {

				itpIndex++;

				if (nbItems <= itpIndex) {
					break;
				}

				ITPIDto item = items.get(itpIndex);
				if (!item.isTestCaseDeleted()) {
					nextITP = item;
				}

			}
			while (nextITP == null && nbItems > itpIndex); // NOSONAR might always be true but I dont wanna induce bugs

			itp = nextITP;

			return itp != null;
		}

	}

}
