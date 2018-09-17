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

import org.jooq.Record;
import org.jooq.Record5;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.campaign.export.CampaignExportCSVModel;
import org.squashtest.tm.service.internal.dto.*;

import java.text.SimpleDateFormat;
import java.util.*;

import static org.squashtest.tm.jooq.domain.Tables.*;

/*
 * TODO :
 *
 * omg the amount of data processed here can become huge quickly and holding the whole model in memory might not be
 * workable anymore in the future. I suggest a more low level implementation using specific services, instead of being
 * lazy like here :
 *
 * - using hibernate cursors to maintain the size of the cache to an acceptable level, - iterate over the execution
 * steps directly instead of the clumsy iterator mechanics, - The datacells should return data only when requested -
 * fetch the number of issues for itp and test steps more efficiently !!
 *
 * edit aguilhem 29/08/2018: optimization done, using jooq request instead of hibernate. Probably some more optimization can be made...
 */
@Component
@Scope("prototype")
public class CampaignExportCSVFullModelImpl extends AbstractCampaignExportCSVModel {

	private static final Logger LOGGER = LoggerFactory.getLogger(CampaignExportCSVModel.class);

	private static final String UNCHECKED = "unchecked";

	private int nbRows;

	public CampaignExportCSVFullModelImpl() {
		super();
	}

	@Override
	void initIterationsAndCustomFields() {

		LOGGER.info("campaign full export : processing model");

		Iterator<Record> iterator = getIterationJooqQueryIterator();

		List<Long> allTestCaseIds = new ArrayList<>();

		List<Long> allExecutionIds = new ArrayList<>();

		populateCampaignDto(iterator, allTestCaseIds, allExecutionIds);

		Set<Long> allIterationIds = campaignDto.getIterationMap().keySet();

		List<Long> allExecutionStepIds = collectLatestExecutionStepId(campaignDto.getIterationMap().values());

		nbRows = allExecutionStepIds.size();

		// cufs for the campaign
		populateCampCUFModelAndCampCUFValues();

		// cufs for the iterations
		populateCUFModelAndCufValues("ITERATION", iterCUFModel, iterCUFValues, allIterationIds);

		// cufs for the test cases
		populateCUFModelAndCufValues("TEST_CASE", tcCUFModel, tcCUFValues, allTestCaseIds);

		// cufs for the executions
		populateCUFModelAndCufValues("EXECUTION", execCUFModel, execCUFValues, allExecutionIds);

		// cufs for the execution steps
		populateCUFModelAndCufValues("EXECUTION_STEP", esCUFModel, esCUFValues, allExecutionStepIds);

		nbColumns = 35 + campCUFModel.size() + iterCUFModel.size() + tcCUFModel.size() + execCUFModel.size() + esCUFModel.size();

		LOGGER.info("campaign full export : model processed");
	}

	@Override
	Iterator<Record> getIterationJooqQueryIterator() {
		return
			DSL.select(
				ITERATION_ID, ITERATION_NAME, ITERATION_SCHEDULED_END_DATE, ITERATION_SCHEDULED_START_DATE, ITERATION_ACTUAL_END_DATE, ITERATION_ACTUAL_START_DATE,
				ITPI_ID, ITPI_STATUS, USER_LOGIN, ITPI_LAST_EXECUTED_ON, ITPI_EXECUTION, DATASET_NAME, IT_MILESTONE.LABEL,
				TC_ID, TC_IMPORTANCE, TC_REFERENCE, TC_NATURE, TC_TYPE, TC_STATUS, TC_REQUIREMENT_VERIFIED,
				TC_NAME, TC_PREREQUISITE, TC_DESCRIPTION, PROJECT_ID, PROJECT_NAME, ITPI_ISSUE,
				TSu_NAME, TC_MILESTONE.LABEL,
				TS_ORDER, TS_ID, CTS_CALLED_TS, TS_REQUIREMENT_VERIFIED,
				EXECUTION_ID, EXECUTION_MODE, EXECUTION_STATUS,
				EXECUTION_STEP_ID, EXECUTION_STEP_STATUS, ES_LAST_EXECUTED_BY, ES_LAST_EXECUTED_ON, ES_COMMENT, ES_ORDER, ES_TS_ID, ES_ISSUE, ES_REQUIREMENT_VERIFIED
			)
				.from(ITERATION)
				.innerJoin(CAMPAIGN_ITERATION).on(ITERATION_ID.eq(CAMPAIGN_ITERATION.ITERATION_ID))
				.innerJoin(CAMPAIGN).on(CAMPAIGN.CLN_ID.eq(CAMPAIGN_ITERATION.CAMPAIGN_ID))
				.innerJoin(ITEM_TEST_PLAN_LIST).on(ITEM_TEST_PLAN_LIST.ITERATION_ID.eq(ITERATION_ID))
				.innerJoin(ITERATION_TEST_PLAN_ITEM).on(ITPI_ID.eq(ITEM_TEST_PLAN_LIST.ITEM_TEST_PLAN_ID))
				.leftJoin(TEST_CASE).on(TC_ID.eq(ITERATION_TEST_PLAN_ITEM.TCLN_ID))
				.leftJoin(TEST_CASE_LIBRARY_NODE).on(TEST_CASE_LIBRARY_NODE.TCLN_ID.eq(TC_ID))
				.leftJoin(TEST_CASE_STEPS).on(TEST_CASE_STEPS.TEST_CASE_ID.eq(TC_ID))
				.leftJoin(CALL_TEST_STEP).on(CALL_TEST_STEP.TEST_STEP_ID.eq(TS_ID))
				.leftJoin(ACTION_TEST_STEP).on(ACTION_TEST_STEP.TEST_STEP_ID.eq(TS_ID))
				.leftJoin(VERIFYING_STEPS.as("ts_verifying_step")).on(VERIFYING_STEPS.as("ts_verifying_step").TEST_STEP_ID.eq(ACTION_TEST_STEP.TEST_STEP_ID))
				.leftJoin(REQUIREMENT_VERSION_COVERAGE.as("ts_rvc")).on(TS_REQUIREMENT_VERIFIED.eq(VERIFYING_STEPS.as("ts_verifying_step").REQUIREMENT_VERSION_COVERAGE_ID))
				.innerJoin(PROJECT).on(PROJECT_ID.eq(TEST_CASE_LIBRARY_NODE.PROJECT_ID))
				.leftJoin(REQUIREMENT_VERSION_COVERAGE.as("tc_rvc")).on(REQUIREMENT_VERSION_COVERAGE.as("tc_rvc").VERIFYING_TEST_CASE_ID.eq(TC_ID))
				.leftJoin(DATASET).on(DATASET.DATASET_ID.eq(ITERATION_TEST_PLAN_ITEM.DATASET_ID))
				.leftJoin(ITEM_TEST_PLAN_EXECUTION).on(ITEM_TEST_PLAN_EXECUTION.ITEM_TEST_PLAN_ID.eq(ITPI_ID))
				.leftJoin(EXECUTION).on(EXECUTION_ID.eq(ITEM_TEST_PLAN_EXECUTION.EXECUTION_ID))
				.leftJoin(EXECUTION_EXECUTION_STEPS).on(EXECUTION_EXECUTION_STEPS.EXECUTION_ID.eq(EXECUTION.EXECUTION_ID))
				.leftJoin(EXECUTION_STEP).on(EXECUTION_STEP.EXECUTION_STEP_ID.eq(EXECUTION_EXECUTION_STEPS.EXECUTION_STEP_ID))
				.leftJoin(ISSUE_LIST.as("exec_issue_list")).on(ISSUE_LIST.as("exec_issue_list").ISSUE_LIST_ID.eq(EXECUTION.ISSUE_LIST_ID).or(ISSUE_LIST.as("exec_issue_list").ISSUE_LIST_ID.eq(EXECUTION_STEP.ISSUE_LIST_ID)))      // used to get execution's issues (which includes its steps issues)
				.leftJoin(ISSUE.as("exec_issue")).on(ISSUE.as("exec_issue").ISSUE_LIST_ID.eq(ISSUE_LIST.as("exec_issue_list").ISSUE_LIST_ID))
				.leftJoin(ISSUE_LIST.as("es_issue_list")).on(ISSUE_LIST.as("es_issue_list").ISSUE_LIST_ID.eq(EXECUTION_STEP.ISSUE_LIST_ID))     // used to get only the step's issues
				.leftJoin(ISSUE.as("es_issue")).on(ISSUE.as("es_issue").ISSUE_LIST_ID.eq(ISSUE_LIST.as("es_issue_list").ISSUE_LIST_ID))
				.leftJoin(VERIFYING_STEPS.as("es_verifying_step")).on(VERIFYING_STEPS.as("es_verifying_step").TEST_STEP_ID.eq(EXECUTION_STEP.TEST_STEP_ID))
				.leftJoin(REQUIREMENT_VERSION_COVERAGE.as("es_rvc")).on(ES_REQUIREMENT_VERIFIED.eq(VERIFYING_STEPS.as("es_verifying_step").REQUIREMENT_VERSION_COVERAGE_ID))
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
				.orderBy(ITERATION_ID, ITPI_ID, EXECUTION_ID, ES_ORDER)
				.fetch().iterator();
	}

	private void populateCampaignDto(Iterator<Record> iterator, List<Long> allTestCaseIds, List<Long> allExecutionIds) {
		IterationDto currentIteration = new IterationDto();
		ITPIDto currentItpi = new ITPIDto();
		TestCaseDto currentTestCase = new TestCaseDto();
		TestStepDto currentTestStep = new TestStepDto();
		ExecutionDto currentExecution = new ExecutionDto();
		ExecutionStepDto currentExecutionStep = new ExecutionStepDto();

		while (iterator.hasNext()) {
			Record r = iterator.next();
			if (campaignDto.getIteration(r.get(ITERATION_ID)) == null) {
				campaignDto.addIteration(
					new IterationDto(r.get(ITERATION_ID), r.get(ITERATION_NAME), r.get(ITERATION_SCHEDULED_START_DATE), r.get(ITERATION_SCHEDULED_END_DATE), r.get(ITERATION_ACTUAL_START_DATE), r.get(ITERATION_ACTUAL_END_DATE))
				);
				currentIteration = campaignDto.getIteration(r.get(ITERATION_ID));
			}
			if (r.get(IT_MILESTONE.LABEL) != null) {
				currentIteration.addMilestone(r.get(IT_MILESTONE.LABEL));
			}
			if (currentIteration.getTestPlan(r.get(ITPI_ID)) == null) {

				ITPIDto newItpi = createNewItpiDto(r);

				TestCaseDto newTestCase = createNewTestCaseDto(r);

				allTestCaseIds.add(r.get(TC_ID));

				newItpi.setTestCase(newTestCase);

				currentIteration.addTestPlan(newItpi);
				currentItpi = currentIteration.getTestPlan(r.get(ITPI_ID));
				currentTestCase = currentItpi.getTestCase();

				if (r.get(TS_ID) != null) {
					TestStepDto step = createTestStepDto(r);
					currentTestCase.addStep(step);
					currentTestStep = currentTestCase.getStep(r.get(TS_ID));
				}


				if (r.get(EXECUTION_ID) != null) {
					ExecutionDto newExecution = createNewExecutionDto(r);

					currentItpi.addExecution(newExecution);

					currentExecution = currentItpi.getExecution(r.get(EXECUTION_ID));

					if (r.get(EXECUTION_STEP_ID) != null) {
						currentExecutionStep = currentExecution.getStep(r.get(EXECUTION_STEP_ID));
					}

					allExecutionIds.add(r.get(EXECUTION_ID));
				}


			} else {

				populateItpi(r, currentItpi);

				populateTestCase(r, currentTestCase);

				if (currentTestStep.getId().equals(r.get(TS_ID)) && r.get(TS_REQUIREMENT_VERIFIED) != null) {
					currentTestStep.addRequirement(r.get(TS_REQUIREMENT_VERIFIED));
				} else if (r.get(TS_ID) != null) {
					TestStepDto step = createTestStepDto(r);
					currentTestCase.addStep(step);
					currentTestStep = currentTestCase.getStep(r.get(TS_ID));
				}

				if (r.get(EXECUTION_ID) != null) {
					if (!currentExecution.getId().equals(r.get(EXECUTION_ID))) {

						ExecutionDto newExecution = createNewExecutionDto(r);

						currentItpi.addExecution(newExecution);

						currentExecution = currentItpi.getExecution(r.get(EXECUTION_ID));
						allExecutionIds.add(r.get(EXECUTION_ID));

					} else if (r.get(EXECUTION_STEP_ID) != null) {
						if (!currentExecutionStep.getId().equals(r.get(EXECUTION_STEP_ID))) {
							ExecutionStepDto newES = createExecutionStepDto(r);

							currentExecution.addStep(newES);

							currentExecutionStep = currentExecution.getStep(r.get(EXECUTION_STEP_ID));
						} else {
							populateExecutionStepDto(r, currentExecutionStep);
						}
					}
				}


			}
		}
	}

	private ExecutionStepDto createExecutionStepDto(Record r) {
		ExecutionStepDto es = new ExecutionStepDto(r.get(EXECUTION_STEP_ID), r.get(EXECUTION_STEP_STATUS), r.get(ES_ORDER), r.get(ES_TS_ID));
		if (r.get(ES_COMMENT) != null) {
			es.setComment(r.get(ES_COMMENT));
		}
		if (r.get(ES_LAST_EXECUTED_BY) != null) {
			es.setLastExecutedBy(r.get(ES_LAST_EXECUTED_BY));
		}
		es.setLastExecutedOn(r.get(ES_LAST_EXECUTED_ON));
		populateExecutionStepDto(r, es);
		return es;
	}

	private TestStepDto createTestStepDto(Record r) {
		TestStepDto step = new TestStepDto(r.get(TS_ID), r.get(TS_ORDER));
		if (r.get(CTS_CALLED_TS) != null) {
			step.setCalledTestCaseId(r.get(CTS_CALLED_TS));
		} else if (r.get(TS_REQUIREMENT_VERIFIED) != null) {
			step.addRequirement(r.get(TS_REQUIREMENT_VERIFIED));
		}
		return step;
	}

	@Override
	protected void populateItpi(Record r, ITPIDto itpi) {
		if (r.get(TSu_NAME) != null) {
			itpi.getTestSuiteSet().add(r.get(TSu_NAME));
		}

		if (r.get(ITPI_ISSUE) != null) {
			itpi.addIssue(r.get(ITPI_ISSUE));
		}
	}

	private void populateExecutionStepDto(Record r, ExecutionStepDto executionStepDto) {
		if (r.get(ES_REQUIREMENT_VERIFIED) != null) {
			executionStepDto.addRequirement(r.get(ES_REQUIREMENT_VERIFIED));
		}
		if (r.get(ES_ISSUE) != null) {
			executionStepDto.addIssue(r.get(ES_ISSUE));
		}
	}

	private TestCaseDto createNewTestCaseDto(Record r) {
		TestCaseDto newTestCase = new TestCaseDto(r.get(TC_ID), r.get(TC_REFERENCE), r.get(TC_NAME), r.get(TC_IMPORTANCE), r.get(TC_NATURE), r.get(TC_TYPE), r.get(TC_STATUS), r.get(PROJECT_ID), r.get(PROJECT_NAME));
		if (r.get(TC_DESCRIPTION) != null) {
			newTestCase.setDescription(r.get(TC_DESCRIPTION));
		}
		if (r.get(TC_PREREQUISITE) != null) {
			newTestCase.setPrerequisite(r.get(TC_PREREQUISITE));
		}
		populateTestCase(r, newTestCase);

		return newTestCase;
	}

	private ExecutionDto createNewExecutionDto(Record r) {
		ExecutionDto newExecution = new ExecutionDto(r.get(EXECUTION_ID), r.get(EXECUTION_STATUS), r.get(EXECUTION_MODE).equals("AUTOMATED"));
		if (r.get(EXECUTION_STEP_ID) != null) {
			ExecutionStepDto executionStepDto = createExecutionStepDto(r);
			newExecution.addStep(executionStepDto);
		}
		return newExecution;
	}

	private List<Long> collectLatestExecutionStepId(Collection<IterationDto> iterations) {
		List<Long> execStepIds = new ArrayList<>();
		for (IterationDto iteration : iterations) {
			for (ITPIDto item : iteration.getTestPlanList()) {
				if (!item.isTestCaseDeleted() && item.getLatestExecution() != null) {
					execStepIds.addAll(item.getLatestExecution().getSteps().keySet());
				}
			}
		}
		return execStepIds;
	}

	@Override
	public Row getHeader() {

		List<CellImpl> headerCells = new ArrayList<>(nbColumns);

		// campaign fixed fields (4)
		headerCells.add(new CellImpl("CPG_SCHEDULED_START_ON"));
		headerCells.add(new CellImpl("CPG_SCHEDULED_END_ON"));
		headerCells.add(new CellImpl("CPG_ACTUAL_START_ON"));
		headerCells.add(new CellImpl("CPG_ACTUAL_END_ON"));

		// iteration fixed fields (7)
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

		// test case fixed fields (17)
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

		// test step fixed fields (8)
		headerCells.add(new CellImpl("STEP_ID"));
		headerCells.add(new CellImpl("STEP_NUM"));
		headerCells.add(new CellImpl("STEP_#_REQ"));
		headerCells.add(new CellImpl("EXEC_STEP_STATUS"));
		headerCells.add(new CellImpl("EXEC_STEP_DATE"));
		headerCells.add(new CellImpl("EXEC_STEP_USER"));
		headerCells.add(new CellImpl("EXEC_STEP_#_ISSUES"));
		headerCells.add(new CellImpl("EXEC_STEP_COMMENT"));

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

		// execution custom fields
		for (CustomFieldDto cufModel : execCUFModel) {
			headerCells.add(new CellImpl("EXEC_CUF_" + cufModel.getCode()));
		}

		// execution steps custom fields
		for (CustomFieldDto cufModel : esCUFModel) {
			headerCells.add(new CellImpl("STEP_CUF_" + cufModel.getCode()));
		}

		return new RowImpl(headerCells, separator);

	}

	@Override
	public Iterator<Row> dataIterator() {
		return new DataIterator();
	}

	// ********************************** nested classes ********************************************

	private class DataIterator implements Iterator<Row> {

		private static final String N_A = "n/a";
		// initial state : null is a meaningful value here.
		private IterationDto iteration = null;
		private ITPIDto itp = null;
		private ExecutionDto exec = null;
		private ExecutionStepDto execStep = null;
		private TestStepDto testStep = null;

		private int iterIndex = -1;
		private int itpIndex = -1;
		private int stepIndex = -1;
		private int testStepIndex = -1;

		private boolean globalHasNext = true;

		private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

		// ** caching **
		// slight optimization, but will not make up for the need for refactoring (see comments on top)

		private List<CellImpl> cachedItpcellFixed = new ArrayList<>(17);
		private List<CellImpl> cachedItpcellCuf = new ArrayList<>(iterCUFModel.size());
		private boolean cachedItpcellReady = false;

		private int logcount = 0;

		public DataIterator() {

			super();
			moveToNextStep();

		}

		// ************************************** model population ******************************

		// See getHeader() for reference
		@Override
		public Row next() {

			List<CellImpl> dataCells = new ArrayList<>(nbColumns);

			// the campaign
			populateCampaignFixedRowData(dataCells);

			// the iteration
			populateIterationFixedRowData(dataCells);

			// the test case
			populateTestCaseFixedRowData(dataCells);

			// the step
			populateTestStepFixedRowData(dataCells);

			// the campaign custom fields
			populateCampaignCUFRowData(dataCells);

			// the iteration custom fields
			populateIterationCUFRowData(dataCells);

			// the test case custom fields
			populateTestCaseCUFRowData(dataCells);

			// the execution steps custom fields
			populateExecutionCUFRowData(dataCells);

			// the execution steps custom fields
			populateExecutionStepCUFRowData(dataCells);

			// move to the next occurence
			moveToNextStep();

			// for logging purposes
			logcount++;
			if (logcount % 99 == 0) {
				LOGGER.info("campaign full export : processed " + (logcount + 1) + " lines out of " + (nbRows + 1)
					+ " (maximum estimate)");
			}

			return new RowImpl(dataCells, separator);

		}

		@SuppressWarnings(UNCHECKED)
		private void populateExecutionCUFRowData(List<CellImpl> dataCells) {
			ExecutionDto exe = exec;
			if (exe != null) {
				Collection<CustomFieldValueDto> execValues = (Collection<CustomFieldValueDto>) execCUFValues
					.get(exec.getId());
				for (CustomFieldDto model : execCUFModel) {
					String strValue = getValue(execValues, model);
					dataCells.add(new CellImpl(strValue));
				}
			}
		}


		@SuppressWarnings(UNCHECKED)
		private void populateExecutionStepCUFRowData(List<CellImpl> dataCells) {
			ExecutionStepDto eStep = execStep;
			if (eStep != null) {
				Collection<CustomFieldValueDto> esValues = (Collection<CustomFieldValueDto>) esCUFValues
					.get(execStep.getId());
				for (CustomFieldDto model : esCUFModel) {
					String strValue = getValue(esValues, model);
					dataCells.add(new CellImpl(strValue));
				}
			}
		}


		@SuppressWarnings(UNCHECKED)
		private void populateTestCaseCUFRowData(List<CellImpl> dataCells) {

			if (cachedItpcellReady) {
				dataCells.addAll(cachedItpcellCuf);
			} else {
				TestCaseDto testCase = itp.getTestCase();

				Collection<CustomFieldValueDto> tcValues = (Collection<CustomFieldValueDto>) tcCUFValues
					.get(testCase.getId());

				for (CustomFieldDto model : tcCUFModel) {
					String strValue = getValue(tcValues, model);
					CellImpl cell = new CellImpl(strValue);
					dataCells.add(cell);
					cachedItpcellCuf.add(cell);
				}
				cachedItpcellReady = true;
			}
		}

		@SuppressWarnings(UNCHECKED)
		private void populateIterationCUFRowData(List<CellImpl> dataCells) {
			Collection<CustomFieldValueDto> iValues = (Collection<CustomFieldValueDto>) iterCUFValues.get(iteration.getId());
			for (CustomFieldDto model : iterCUFModel) {
				String strValue = getValue(iValues, model);
				dataCells.add(new CellImpl(strValue));
			}
		}

		private void populateCampaignCUFRowData(List<CellImpl> dataCells) {
			for (CustomFieldDto model : campCUFModel) {
				String strValue = getCampaignCufValue(campCUFValues.get(model.getId()), model);
				dataCells.add(new CellImpl(strValue));
			}
		}

		private void populateTestStepFixedRowData(List<CellImpl> dataCells) {


			if (execStep == null && testStep != null) {
				dataCells.add(new CellImpl(N_A));
				dataCells.add(new CellImpl(String.valueOf(testStepIndex + 1)));
				dataCells.add(new CellImpl(formatStepRequirements()));
				dataCells.add(new CellImpl(N_A));
				dataCells.add(new CellImpl(N_A));
				dataCells.add(new CellImpl(N_A));
				dataCells.add(new CellImpl(N_A));
				dataCells.add(new CellImpl(N_A));

			} else if (execStep != null) {
				dataCells.add(new CellImpl(Long.toString(execStep.getId())));
				dataCells.add(new CellImpl(String.valueOf(stepIndex + 1)));
				dataCells.add(new CellImpl(formatStepRequirements()));
				dataCells.add(new CellImpl(execStep.getStatus()));
				dataCells.add(new CellImpl(formatDate(execStep.getLastExecutedOn())));
				dataCells.add(new CellImpl(execStep.getLastExecutedBy()));
				dataCells.add(new CellImpl(Integer.toString(execStep.getIssueSet().size())));
				dataCells.add(new CellImpl(formatLongText(execStep.getComment())));

			} else {
				/* Issue 6351: We also have to import ITPI without any Test Step. */
				dataCells.add(new CellImpl(N_A));
				dataCells.add(new CellImpl(N_A));
				dataCells.add(new CellImpl(N_A));
				dataCells.add(new CellImpl(N_A));
				dataCells.add(new CellImpl(N_A));
				dataCells.add(new CellImpl(N_A));
				dataCells.add(new CellImpl(N_A));
				dataCells.add(new CellImpl(N_A));
			}
		}

		private String formatLongText(String text) {
			return text == null ? "" : text.trim();
		}

		private void populateTestCaseFixedRowData(List<CellImpl> dataCells) {

			if (cachedItpcellReady) {
				dataCells.addAll(cachedItpcellFixed);
			} else {

				TestCaseDto testCase = itp.getTestCase();

				cachedItpcellFixed.add(new CellImpl(testCase.getId().toString()));
				cachedItpcellFixed.add(new CellImpl(testCase.getName()));
				cachedItpcellFixed.add(new CellImpl(testCase.getProjectId().toString()));
				cachedItpcellFixed.add(new CellImpl(testCase.getProjectName()));
				if (milestonesEnabled) {
					cachedItpcellFixed.add(new CellImpl(formatMilestone(testCase.getMilestoneSet())));
				}
				cachedItpcellFixed.add(new CellImpl(testCase.getImportance()));
				cachedItpcellFixed.add(new CellImpl(itp.getTestSuiteNames().replace(", ", ",").replace("<", "&lt;")
					.replace(">", "&gt;")));

				cachedItpcellFixed.add(new CellImpl(Integer.toString(itp.getExecutionMap().size())));
				cachedItpcellFixed
					.add(new CellImpl(Integer.toString(testCase.getRequirementSet().size())));
				cachedItpcellFixed.add(new CellImpl(Integer.toString(itp.getIssueSet().size())));
				cachedItpcellFixed.add(new CellImpl(itp.getDataset()));

				cachedItpcellFixed.add(new CellImpl(itp.getStatus()));
				cachedItpcellFixed.add(new CellImpl(itp.getUserName()));
				cachedItpcellFixed.add(new CellImpl(formatDate(itp.getLastExecutedOn())));

				cachedItpcellFixed.add(new CellImpl(testCase.getReference()));
				cachedItpcellFixed.add(new CellImpl(testCase.getNature()));
				cachedItpcellFixed.add(new CellImpl(testCase.getType()));
				cachedItpcellFixed.add(new CellImpl(testCase.getStatus()));

				dataCells.addAll(cachedItpcellFixed);

			}

		}

		private void populateIterationFixedRowData(List<CellImpl> dataCells) {

			dataCells.add(new CellImpl(iteration.getId().toString()));
			dataCells.add(new CellImpl(String.valueOf(iterIndex + 1)));
			dataCells.add(new CellImpl(iteration.getName()));
			if (milestonesEnabled) {
				dataCells.add(new CellImpl(formatMilestone(iteration.getMilestoneSet())));
			}
			dataCells.add(new CellImpl(formatDate(iteration.getScheduledStartDate())));
			dataCells.add(new CellImpl(formatDate(iteration.getScheduledEndDate())));
			dataCells.add(new CellImpl(formatDate(iteration.getActualStartDate())));
			dataCells.add(new CellImpl(formatDate(iteration.getActualEndDate())));

		}

		private void populateCampaignFixedRowData(List<CellImpl> dataCells) {

			dataCells.add(new CellImpl(formatDate(campaign.getScheduledStartDate())));
			dataCells.add(new CellImpl(formatDate(campaign.getScheduledEndDate())));
			dataCells.add(new CellImpl(formatDate(campaign.getActualStartDate())));
			dataCells.add(new CellImpl(formatDate(campaign.getActualEndDate())));

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

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		// ******************************** data formatting ***************************

		private String getCampaignCufValue(CustomFieldValueDto customFieldValueDto, CustomFieldDto model) {
			if (customFieldValueDto != null && customFieldValueDto.getValue() != null) {
				if (model.getInputType().equals("NUMERIC")) {
					return NumericCufHelper.formatOutputNumericCufValue(customFieldValueDto.getValue());
				}
				return customFieldValueDto.getValue();
			}
			return "";
		}

		// returns the correct value if found, or "--" if not found
		private String getValue(Collection<CustomFieldValueDto> values, CustomFieldDto model) {

			if (values != null) {
				return formatOutputValue(model, values);
			}

			return "--";
		}

		private String formatOutputValue(CustomFieldDto model, Collection<CustomFieldValueDto> values) {
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

		private String formatStepRequirements() {
			String res;
			if (execStep != null) {
				res = Integer.toString(execStep.getRequirementSet().size());
			} else if (testStep != null) {

				res = Integer.toString(testStep.getRequirementSet().size());

			} else {
				res = "?";
			}


			return res;
		}

		// ****************** hairy iterator mechanics here ****************

		@Override
		public boolean hasNext() {
			return globalHasNext;
		}

		private void moveToNextStep() {

			boolean foundNextStep = false;
			boolean nextTCSucc;

			do {
				// test if we must move to the next test case
				if (execStep == null && testStep == null) {
					nextTCSucc = moveToNextTestCase();
					if (!nextTCSucc) {
						// that was the last test case and we cannot iterate further more : we break the loop forcibly
						globalHasNext = false;
						return;
					} else {
						resetCachedItpcell();
						resetStepIndex();
						resetTestStepIndex();
					}
				}

				// find a suitable execution step
				if (itp.getLatestExecution() != null) {
					exec = itp.getLatestExecution();
					List<ExecutionStepDto> steps = new ArrayList<>(exec.getSteps().values());

					int stepsSize = steps.size();
					stepIndex++;

					if (stepIndex < stepsSize) {
						execStep = steps.get(stepIndex);
						testStep = null;
						foundNextStep = true;
						/* Issue 6351: We also have to import ITPI without any Test Step. */
					} else if (stepsSize == 0) {
						execStep = null;
						testStep = null;
						foundNextStep = true;
					} else {
						execStep = null;
						testStep = null;
					}

				} else {
					exec = null;
					TestCaseDto testCase = itp.getTestCase();
					List<TestStepDto> testSteps = getActionTestStepList(testCase);

					int actionTestStepSize = testSteps.size();
					testStepIndex++;

					if (testStepIndex < actionTestStepSize) {
						testStep = testSteps.get(testStepIndex);
						execStep = null;
						foundNextStep = true;
						/* Issue 6351: We also have to import ITPI without any Test Step. */
					} else if (actionTestStepSize == 0) {
						testStep = null;
						execStep = null;
						foundNextStep = true;
					} else {
						execStep = null;
						testStep = null;
					}

					execStep = null;
				}

			} while (!foundNextStep);

		}

		private List<TestStepDto> getActionTestStepList(TestCaseDto testCase) {

			List<TestStepDto> result = new ArrayList<>();
			List<TestStepDto> steps = new ArrayList<>(testCase.getStepMap().values());
			steps.sort(Comparator.comparing(TestStepDto::getStepOrder));

			result.addAll(getActionTestStepListRec(steps));

			return result;
		}

		private List<TestStepDto> getActionTestStepListRec(List<TestStepDto> steps) {

			List<TestStepDto> result = new ArrayList<>();

			for (TestStepDto step : steps) {
				if (!step.isCallStep()) {
					result.add(step);
				} else {
					TestCaseDto calledTestCase = getCalledTestCase(step.getCalledTestCaseId());
					result.addAll(getActionTestStepList(calledTestCase));
				}
			}

			return result;
		}

		private TestCaseDto getCalledTestCase(Long calledTestId) {
			TestCaseDto result = new TestCaseDto();
			TestStepDto currentStep = new TestStepDto();
			result.setId(calledTestId);
			Iterator<Record5<Long, Integer, Long, Long, Long>> iterator = DSL.select(TC_ID, TS_ORDER, TS_ID, CTS_CALLED_TS, TS_REQUIREMENT_VERIFIED)
				.from(TEST_CASE)
				.leftJoin(TEST_CASE_STEPS).on(TEST_CASE_STEPS.TEST_CASE_ID.eq(TC_ID))
				.leftJoin(CALL_TEST_STEP).on(CALL_TEST_STEP.TEST_STEP_ID.eq(TS_ID))
				.leftJoin(ACTION_TEST_STEP).on(ACTION_TEST_STEP.TEST_STEP_ID.eq(TS_ID))
				.leftJoin(VERIFYING_STEPS.as("ts_verifying_step")).on(VERIFYING_STEPS.as("ts_verifying_step").TEST_STEP_ID.eq(ACTION_TEST_STEP.TEST_STEP_ID))
				.leftJoin(REQUIREMENT_VERSION_COVERAGE.as("ts_rvc")).on(TS_REQUIREMENT_VERIFIED.eq(VERIFYING_STEPS.as("ts_verifying_step").REQUIREMENT_VERSION_COVERAGE_ID))
				.where(TC_ID.eq(calledTestId))
				.orderBy(TS_ORDER)
				.fetch().iterator();

			while (iterator.hasNext()) {
				Record r = iterator.next();
				if (!currentStep.getId().equals(r.get(TS_ID))) {
					TestStepDto dto = createTestStepDto(r);
					result.addStep(dto);
					currentStep = result.getStep(r.get(TS_ID));
				} else if (r.get(TS_REQUIREMENT_VERIFIED) != null) {
					currentStep.addRequirement(r.get(TS_REQUIREMENT_VERIFIED));
				}
			}
			return result;
		}

		private boolean moveToNextTestCase() {

			boolean foundNextTC;
			boolean nextIterSucc;

			do {
				// test if we must move to the next iteration
				if (itp == null) {
					nextIterSucc = moveToNextIteration();
					if (!nextIterSucc) {
						return false;
					} else {
						resetTCIndex();
					}
				}

				// find a suitable execution step
				List<ITPIDto> items = iteration.getTestPlanList();
				int itemSize = items.size();
				itpIndex++;

				// see if we reached the end of the collection
				if (itpIndex >= itemSize) {
					itp = null;
					foundNextTC = false;
				}
				// check that the test case wasn't deleted
				else if (items.get(itpIndex).isTestCaseDeleted()) {
					foundNextTC = false;
				} else {
					itp = items.get(itpIndex);
					foundNextTC = true;
				}

			} while (!foundNextTC);

			return foundNextTC;
		}

		private boolean moveToNextIteration() {

			boolean foundIter = false;
			iterIndex++;

			List<IterationDto> iterations = campaignDto.getIterationList();
			int iterSize = iterations.size();

			if (iterIndex < iterSize) {
				iteration = iterations.get(iterIndex);
				foundIter = true;
			}

			return foundIter;

		}

		private void resetStepIndex() {
			stepIndex = -1;
		}

		private void resetTestStepIndex() {
			testStepIndex = -1;
		}

		private void resetTCIndex() {
			itpIndex = -1;
		}

		private void resetCachedItpcell() {
			cachedItpcellFixed.clear();
			cachedItpcellCuf.clear();
			cachedItpcellReady = false;
		}

	}

	// ******************** implementation for the rows and cells **********************

}
