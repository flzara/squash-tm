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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.collections.map.MultiValueMap;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.customfield.InputType;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.service.bugtracker.BugTrackersLocalService;
import org.squashtest.tm.service.customfield.CustomFieldHelper;
import org.squashtest.tm.service.customfield.CustomFieldHelperService;
import org.squashtest.tm.service.feature.FeatureManager;
import org.squashtest.tm.service.feature.FeatureManager.Feature;
import org.squashtest.tm.service.internal.dto.NumericCufHelper;

@Component
@Scope("prototype")
public class SimpleCampaignExportCSVModelImpl implements WritableCampaignCSVModel {

	@Inject
	private CustomFieldHelperService cufHelperService;

	@Inject
	private BugTrackersLocalService bugTrackerService;

	@Inject
	private FeatureManager featureManager;

	private char separator = ';';

	private Campaign campaign;

	private List<CustomField> campCUFModel;
	private List<CustomField> iterCUFModel;
	private List<CustomField> tcCUFModel;

	private List<CustomFieldValue> campCUFValues;
	private MultiValueMap iterCUFValues; // <Long, Collection<CustomFieldValue>>
	private MultiValueMap tcCUFValues; // same here

	private int nbColumns;

	private boolean milestonesEnabled;

	public SimpleCampaignExportCSVModelImpl() {
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
		initCustomFields();
		milestonesEnabled = featureManager.isEnabled(Feature.MILESTONE);
	}

	private void initCustomFields() {

		List<Iteration> iterations = campaign.getIterations();
		List<TestCase> allTestCases = collectAllTestCases(iterations);

		// cufs for the campaign
		CustomFieldHelper<Campaign> campHelper = cufHelperService.newHelper(campaign);
		campCUFModel = campHelper.getCustomFieldConfiguration();
		campCUFValues = campHelper.getCustomFieldValues();

		// cufs for the iterations
		CustomFieldHelper<Iteration> iterHelper = cufHelperService.newHelper(iterations).includeAllCustomFields();
		iterCUFModel = iterHelper.getCustomFieldConfiguration();
		List<CustomFieldValue> iterValues = iterHelper.getCustomFieldValues();

		// cufs for the test cases
		CustomFieldHelper<TestCase> tcHelper = cufHelperService.newHelper(allTestCases).includeAllCustomFields();
		tcCUFModel = tcHelper.getCustomFieldConfiguration();
		List<CustomFieldValue> tcValues = tcHelper.getCustomFieldValues();

		nbColumns = 25 + campCUFModel.size() + iterCUFModel.size() + tcCUFModel.size();

		// index the custom field values with a map for faster reference later
		createCustomFieldValuesIndex(iterValues, tcValues);

	}

	private List<TestCase> collectAllTestCases(List<Iteration> iterations) {
		// aggregate the test cases in one collection
		List<TestCase> allTestCases = new ArrayList<>();
		for (Iteration iteration : iterations) {
			addIterationTestCases(iteration, allTestCases);
		}
		return allTestCases;
	}

	private void addIterationTestCases(Iteration iteration, List<TestCase> allTestCases) {
		for (IterationTestPlanItem item : iteration.getTestPlans()) {
			if (!item.isTestCaseDeleted()) {
				allTestCases.add(item.getReferencedTestCase());
			}
		}
	}

	private void createCustomFieldValuesIndex(List<CustomFieldValue> iterValues, List<CustomFieldValue> tcValues) {

		iterCUFValues = new MultiValueMap();
		tcCUFValues = new MultiValueMap();

		for (CustomFieldValue value : iterValues) {
			iterCUFValues.put(value.getBoundEntityId(), value);
		}

		for (CustomFieldValue value : tcValues) {
			tcCUFValues.put(value.getBoundEntityId(), value);
		}
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

		// campaign custom fields
		for (CustomField cufModel : campCUFModel) {
			headerCells.add(new CellImpl("CPG_CUF_" + cufModel.getCode()));
		}

		// iteration custom fields
		for (CustomField cufModel : iterCUFModel) {
			headerCells.add(new CellImpl("IT_CUF_" + cufModel.getCode()));
		}

		// test case custom fields
		for (CustomField cufModel : tcCUFModel) {
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

		private Iteration iteration = new Iteration(); // initialized to dummy value for for bootstrap purposes
		private IterationTestPlanItem itp; // null means "no more"

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

			List<CustomFieldValue> cValues = campCUFValues;
			// ensure that the CUF values are processed in the correct order
			for (CustomField model : campCUFModel) {
				String strValue = getValue(cValues, model);
				dataCells.add(new CellImpl(strValue));
			}

			Collection<CustomFieldValue> iValues = (Collection<CustomFieldValue>) iterCUFValues.get(iteration.getId());
			for (CustomField model : iterCUFModel) {
				String strValue = getValue(iValues, model);
				dataCells.add(new CellImpl(strValue));
			}

			TestCase testCase = itp.getReferencedTestCase();

			Collection<CustomFieldValue> tcValues = (Collection<CustomFieldValue>) tcCUFValues.get(testCase.getId());
			for (CustomField model : tcCUFModel) {
				String strValue = getValue(tcValues, model);
				dataCells.add(new CellImpl(strValue));
			}
		}

		private void populateTestCaseRowData(List<CellImpl> dataCells) {

			TestCase testCase = itp.getReferencedTestCase();
			dataCells.add(new CellImpl(testCase.getId().toString()));
			dataCells.add(new CellImpl(itp.getLabel()));
			dataCells.add(new CellImpl(testCase.getProject().getId().toString()));
			dataCells.add(new CellImpl(testCase.getProject().getName()));
			if (milestonesEnabled) {
				dataCells.add(new CellImpl(formatMilestone(testCase.getMilestones())));
			}
			dataCells.add(new CellImpl(testCase.getImportance().toString()));
			dataCells.add(new CellImpl(itp.getTestSuiteNames().replace(", ", ",").replace("<", "&lt;").replace(">", "&gt;")));
			dataCells.add(new CellImpl(Integer.toString(itp.getExecutions().size())));
			dataCells.add(new CellImpl(Integer.toString(testCase.getRequirementVersionCoverages().size())));
			dataCells.add(new CellImpl(Integer.toString(getNbIssues(itp))));
			dataCells.add(new CellImpl((itp.getReferencedDataset() == null) ? "" : itp.getReferencedDataset().getName()));
			dataCells.add(new CellImpl(itp.getExecutionStatus().toString()));
			dataCells.add(new CellImpl(formatUser(itp.getUser())));
			dataCells.add(new CellImpl(formatDate(itp.getLastExecutedOn())));
			dataCells.add(new CellImpl(testCase.getReference()));
			dataCells.add(new CellImpl(testCase.getNature().getCode()));
			dataCells.add(new CellImpl(testCase.getType().getCode()));
			dataCells.add(new CellImpl(testCase.getStatus().toString()));
		}

		private String formatMilestone(Set<Milestone> milestones) {

			StringBuilder sb = new StringBuilder();
			for (Milestone m : milestones) {
				sb.append(m.getLabel());
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
				dataCells.add(new CellImpl(formatMilestone(iteration.getMilestones())));
			}
			dataCells.add(new CellImpl(formatDate(iteration.getScheduledStartDate())));
			dataCells.add(new CellImpl(formatDate(iteration.getScheduledEndDate())));
			dataCells.add(new CellImpl(formatDate(iteration.getActualStartDate())));
			dataCells.add(new CellImpl(formatDate(iteration.getActualEndDate())));


		}

		private void populateCampaignRowData(List<CellImpl> dataCells) {
			dataCells.add(new CellImpl(formatDate(campaign.getScheduledStartDate())));
			dataCells.add(new CellImpl(formatDate(campaign.getScheduledEndDate())));
			dataCells.add(new CellImpl(formatDate(campaign.getActualStartDate())));
			dataCells.add(new CellImpl(formatDate(campaign.getActualEndDate())));
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		// ******************************** data formatting ***************************

		// returns the correct value if found, or "" if not found
		private String getValue(Collection<CustomFieldValue> values, CustomField model) {

			if (values != null) {
				for (CustomFieldValue value : values) {
					CustomField customField = value.getBinding().getCustomField();
					if (customField.getCode().equals(model.getCode())) {
						if (customField.getInputType().equals(InputType.NUMERIC)) {
							return NumericCufHelper.formatOutputNumericCufValue(value.getValue());
						}
						return value.getValue();
					}
				}
			}

			return "";
		}

		private int getNbIssues(IterationTestPlanItem itp) {

			return bugTrackerService.findNumberOfIssueForItemTestPlanLastExecution(itp.getId());

		}

		private String formatDate(Date date) {

			return date == null ? "" : dateFormat.format(date);

		}


		private String formatUser(User user) {
			return user == null ? "" : user.getLogin();

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
			if (campaign.getIterations().size() > iterIndex) {

				iteration = campaign.getIterations().get(iterIndex);
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

			IterationTestPlanItem nextITP = null;

			List<IterationTestPlanItem> items = iteration.getTestPlans();
			int nbItems = items.size();

			do {

				itpIndex++;

				if (nbItems <= itpIndex) {
					break;
				}

				IterationTestPlanItem item = items.get(itpIndex);
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
