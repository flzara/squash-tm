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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.squashtest.tm.domain.campaign.CampaignTestPlanItem;
import org.squashtest.tm.domain.testcase.Dataset;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.service.campaign.IndexedCampaignTestPlanItem;
import org.squashtest.tm.web.internal.controller.campaign.TestPlanTableModelHelper.DatasetInfos;
import org.squashtest.tm.web.internal.controller.milestone.MilestoneModelUtils;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.builder.JeditableComboHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelBuilder;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelConstants;
import org.squashtest.tm.web.internal.model.json.JsonDataset;

final class CampaignTestPlanTableModelHelper extends DataTableModelBuilder<IndexedCampaignTestPlanItem> {

	private Locale locale;
	private InternationalizationHelper messageSource;

	CampaignTestPlanTableModelHelper(InternationalizationHelper messageSource, Locale locale) {
		this.messageSource = messageSource;
		this.locale = locale;
	}


	private String formatNoData(Locale locale) {
		return messageSource.noData(locale);
	}

	@Override
	public Map<String, Object> buildItemData(IndexedCampaignTestPlanItem indexedItem) {

		Integer index = indexedItem.getIndex() + 1;
		CampaignTestPlanItem item = indexedItem.getItem();

		Map<String, Object> result = new HashMap<>();

		TestCase testCase = item.getReferencedTestCase();
		String user = item.getUser() != null ? item.getUser().getLogin() : formatNoData(locale);
		Long assigneeId = item.getUser() != null ? item.getUser().getId() : User.NO_USER_ID;
		String reference = testCase.getReference().isEmpty() ? formatNoData(locale) : testCase.getReference();
		DatasetInfos dsInfos = makeDatasetInfo(item);

		result.put(DataTableModelConstants.DEFAULT_ENTITY_ID_KEY, item.getId());
		result.put(DataTableModelConstants.DEFAULT_ENTITY_INDEX_KEY, index);
		result.put(DataTableModelConstants.PROJECT_NAME_KEY, testCase.getProject().getName());
		result.put("reference", reference);
		result.put("tc-name", testCase.getName());
		result.put("assigned-user", user);
		result.put("assigned-to", assigneeId);
		result.put("importance", formatImportance(testCase.getImportance(), locale));
		result.put("exec-mode", testCase.isAutomated() ? "A" : "M");
		result.put("dataset", dsInfos);
		result.put(DataTableModelConstants.DEFAULT_EMPTY_DELETE_HOLDER_KEY, " ");
		result.put("milestone-dates", MilestoneModelUtils.timeIntervalToString(testCase.getMilestones(), messageSource, locale));
		result.put("tc-id", testCase.getId());
		result.put("milestone-labels", MilestoneModelUtils.milestoneLabelsOrderByDate(testCase.getMilestones()));

		return result;

	}

	/*
	 * TODO : This code is a copy pasta of the same thing in TestPlanTableModelHelper.
	 *
	 * If you want to move that in an helper class you should consider that :
	 * - you should pass it the message source,
	 * - you should move the relevant methods from CampaignTestPlanItem and IterationTestPlanItem
	 * 	in a common interface
	 * - and while you're at it, make a common ancestor for XTestPlanTableModelHelper and subclass it
	 * where necessary.
	 *
	 * OR
	 *
	 * you can just tell SONAR to stfu.
	 */
	private DatasetInfos makeDatasetInfo(CampaignTestPlanItem item){	// NOSONAR copy pasta blahblahblah.
		if (item.getReferencedTestCase().getDatasets().isEmpty()){
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


	private String formatImportance(TestCaseImportance importance, Locale locale) {
		return messageSource.internationalizeAbbreviation(importance, locale);
	}


}
