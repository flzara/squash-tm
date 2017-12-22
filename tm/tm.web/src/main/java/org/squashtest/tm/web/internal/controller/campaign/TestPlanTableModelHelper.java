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
package org.squashtest.tm.web.internal.controller.campaign;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.web.util.HtmlUtils;
import org.squashtest.tm.core.foundation.lang.DateUtils;
import org.squashtest.tm.domain.IdentifiedUtil;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.execution.ExecutionStep;
import org.squashtest.tm.domain.testcase.Dataset;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.service.campaign.IndexedIterationTestPlanItem;
import org.squashtest.tm.web.internal.controller.milestone.MilestoneModelUtils;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.builder.JeditableComboHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelBuilder;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelConstants;
import org.squashtest.tm.web.internal.model.json.JsonDataset;

class TestPlanTableModelHelper extends DataTableModelBuilder<IndexedIterationTestPlanItem> {

	private InternationalizationHelper messageSource;
	private Locale locale;

	private static final String NONE = "";

	TestPlanTableModelHelper(InternationalizationHelper messageSource, Locale locale) {
		this.messageSource = messageSource;
		this.locale = locale;
	}

	@Override
	public Map<String, Object> buildItemData(IndexedIterationTestPlanItem indexedItem) {

		Integer index = indexedItem.getIndex() + 1;
		IterationTestPlanItem item = indexedItem.getItem();

		Map<String, Object> res = new HashMap<>();

		//automation mode
		final String automationMode = item.isAutomated() ? "A" : "M";

		//assigne
		//String assigneeLogin = formatString(item.getLastExecutedBy(), locale); commented because of 3009
		String assigneeLogin = formatString(null, locale);	// we want the "null" login before checking if there is an actual assignee
		Long assigneeId = User.NO_USER_ID;
		User assignee = item.getUser();
		if  (assignee  != null) {
			assigneeId = assignee.getId();
			assigneeLogin = assignee.getLogin();
		}

		//if test case deleted
		String projectName;
		String testCaseName;
		Long tcId;
		String importance;
		String reference;
		String milestoneDates = "-";
		String milestoneLabels = "-";

		if (item.isTestCaseDeleted()) {
			projectName = NONE; // the empty string trick is a cheap way to solve #5585
			testCaseName = formatDeleted(locale);
			tcId = null;
			importance = formatNoData(locale);
			reference = NONE; // the empty string trick is a cheap way to solve #5585
		} else {
			projectName = item.getReferencedTestCase().getProject().getName();
			testCaseName = item.getReferencedTestCase().getName();
			tcId = item.getReferencedTestCase().getId();
			if(item.getReferencedTestCase().getReference().isEmpty()){
				reference = formatNoData(locale);
			}else{
				reference = item.getReferencedTestCase().getReference();
			}
			importance 		= messageSource.internationalizeAbbreviation(item.getReferencedTestCase().getImportance(), locale);
			milestoneDates 	= MilestoneModelUtils.timeIntervalToString(item.getReferencedTestCase().getMilestones(), messageSource, locale);
			milestoneLabels = MilestoneModelUtils.milestoneLabelsOrderByDate(item.getReferencedTestCase().getMilestones());
		}


		//dataset
		DatasetInfos dsIndos = makeDatasetInfo (item);

		// test suite name
		String testSuiteNameList;
		String testSuiteNameListTot;
		List<Long> testSuiteIdsList;

		if (item.getTestSuites().isEmpty()) {
			testSuiteNameList = formatNoData(locale);
			testSuiteNameListTot = formatNoData(locale);
			testSuiteIdsList = Collections.emptyList();
		} else {
			testSuiteNameList = HtmlUtils.htmlEscape(TestSuiteHelper.buildEllipsedSuiteNameList(item.getTestSuites(), 20));
			testSuiteNameListTot = TestSuiteHelper.buildSuiteNameList(item.getTestSuites());
			testSuiteIdsList = IdentifiedUtil.extractIds(item.getTestSuites());
		}

		int succesPercent = 0;

		Execution lastExec = item.getLatestExecution();
		if (lastExec != null) {
			int succes = 0;
			List<ExecutionStep> steps = item.getLatestExecution().getSteps();
			for (ExecutionStep step : steps) {
				if (step.getExecutionStatus() == ExecutionStatus.SUCCESS) {
					succes++;
				}
			}
			int totalSteps = steps.size();

			// I think it's not possible to have total step = 0, because we need at least 1 step to execute
			// but maybe there's a case i don't see so better give 0 than divide by 0 exception.
			succesPercent = totalSteps > 0 ? succes * 100 / totalSteps : 0;
		}

		// now stuff the map
		res.put(DataTableModelConstants.DEFAULT_ENTITY_ID_KEY, item.getId());
		res.put(DataTableModelConstants.DEFAULT_ENTITY_INDEX_KEY, index);
		res.put(DataTableModelConstants.PROJECT_NAME_KEY, projectName);
		res.put("reference", reference);
		res.put("tc-id", tcId);
		res.put("tc-name", testCaseName);
		res.put("importance", importance);
		res.put("suite", testSuiteNameList);
		res.put("suitesTot", testSuiteNameListTot);
		res.put("suiteIds", testSuiteIdsList);
		res.put("status",item.getExecutionStatus().getCanonicalStatus());	// as of issue 2956, we now restrict the status to the canonical status only
		res.put("assignee-id", assigneeId);
		res.put("assignee-login", assigneeLogin);
		res.put("last-exec-on", DateUtils.formatIso8601DateTime(item.getLastExecutedOn()));
		res.put("exec-exists", lastExec!=null);
		res.put("is-tc-deleted", item.isTestCaseDeleted());
		res.put("succesPercent", succesPercent + " %");
		res.put(DataTableModelConstants.DEFAULT_EMPTY_EXECUTE_HOLDER_KEY, " ");
		res.put(DataTableModelConstants.DEFAULT_EMPTY_DELETE_HOLDER_KEY, " ");
		res.put("exec-mode", automationMode);
		res.put("milestone-dates", milestoneDates);
		res.put("dataset", dsIndos);
		res.put("milestone-labels", milestoneLabels);

		return res;
	}

	/* **************** other private stufs ********************** */

	public static final class DatasetInfos{

		static {
			JsonDataset emptyDs = new JsonDataset();
			emptyDs.setId(JeditableComboHelper.coerceIntoComboId(null));
			emptyDs.setName("-");

			EMPTY_INFOS = new DatasetInfos(emptyDs, Collections.<JsonDataset> emptyList());
		}

		public static final DatasetInfos EMPTY_INFOS;

		private JsonDataset selected;
		private Collection<JsonDataset> available;

		DatasetInfos(JsonDataset selected, Collection<JsonDataset> available){
			this.selected = selected;
			this.available = available;
		}

		public JsonDataset getSelected() {
			return selected;
		}

		public Collection<JsonDataset> getAvailable() {
			return available;
		}

	}

	/* ***************** data formatter *************************** */

	private DatasetInfos makeDatasetInfo(IterationTestPlanItem item){
		if (item.isTestCaseDeleted() || item.getReferencedTestCase().getDatasets().isEmpty()){
			return DatasetInfos.EMPTY_INFOS;
		}
		else{

			Dataset selected = item.getReferencedDataset();
			Collection<Dataset> available = item.getReferencedTestCase().getDatasets();

			JsonDataset jsonSelected = convert(selected);
			Collection<JsonDataset> jsonAvailable = new ArrayList<>(available.size()+1);
			jsonAvailable.add(convert(null));	// that one corresponds to dataset 'None'
			for (Dataset ds : available){
				jsonAvailable.add(convert(ds));
			}

			return new DatasetInfos(jsonSelected, jsonAvailable);
		}
	}


	private JsonDataset convert(Dataset ds){
		JsonDataset jsds = new JsonDataset();
		if (ds == null){
			jsds.setName(messageSource.internationalize("label.noneDSEscaped", locale));
			jsds.setId(JeditableComboHelper.coerceIntoComboId(null));
		}else{
			jsds.setName(ds.getName());
			jsds.setId(JeditableComboHelper.coerceIntoComboId(ds.getId()));
		}
		return jsds;
	}


	private String formatString(String arg, Locale locale) {
		return messageSource.messageOrNoData(arg, locale);
	}

	private String formatNoData(Locale locale) {
		return messageSource.noData(locale);
	}

	private String formatDeleted(Locale locale) {
		return messageSource.itemDeleted(locale);
	}

}
