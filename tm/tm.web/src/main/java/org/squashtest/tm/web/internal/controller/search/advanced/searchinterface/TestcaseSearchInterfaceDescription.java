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
package org.squashtest.tm.web.internal.controller.search.advanced.searchinterface;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.domain.testcase.TestCaseStatus;
import org.squashtest.tm.service.internal.dto.json.JsonInfoList;
import org.squashtest.tm.service.internal.dto.json.JsonInfoListItem;
import org.squashtest.tm.service.internal.dto.json.JsonProject;
import org.squashtest.tm.service.testcase.TestCaseAdvancedSearchService;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;

@Component
public class TestcaseSearchInterfaceDescription extends SearchInterfaceDescription {

	private static final String COLUMN_1 = "column1";

	@Inject
	private TestCaseAdvancedSearchService advancedSearchService;

	public SearchInputPanelModel createGeneralInfoPanel(Locale locale) {
		SearchInputPanelModel panel = new SearchInputPanelModel();
		panel.setTitle(getMessageSource().internationalize("search.testcase.generalinfos.panel.title", locale));
		panel.setOpen(true);
		panel.setId("general-information");
		panel.setLocation(COLUMN_1);
		panel.addCssClass("search-icon-information");

		SearchInputFieldModel idField = new SearchInputFieldModel("id", getMessageSource().internationalize("label.id",
				locale), TEXTFIELDID);
		panel.addField(idField);
		SearchInputFieldModel referenceField = new SearchInputFieldModel("reference", getMessageSource()
				.internationalize("label.reference", locale), TEXTFIELDREFERENCE);
		panel.addField(referenceField);
		SearchInputFieldModel labelField = new SearchInputFieldModel("name", getMessageSource().internationalize(
				"label.Label", locale), TEXTFIELD);
		panel.addField(labelField);
		SearchInputFieldModel descriptionField = new SearchInputFieldModel("description", getMessageSource()
				.internationalize("label.Description", locale), TEXTAREA);
		panel.addField(descriptionField);
		SearchInputFieldModel prerequisiteField = new SearchInputFieldModel("prerequisite", getMessageSource()
				.internationalize("test-case.prerequisite.label", locale), TEXTAREA);
		panel.addField(prerequisiteField);

		return panel;
	}

	public SearchInputPanelModel createAttributePanel(Locale locale, Collection<JsonProject> jsProjects) {
		SearchInputPanelModel panel = new SearchInputPanelModel();
		panel.setTitle(getMessageSource().internationalize("search.testcase.attributes.panel.title", locale));
		panel.setOpen(true);
		panel.setId("attributes");
		panel.setLocation(COLUMN_1);
		panel.addCssClass("search-icon-attributes");

		SearchInputFieldModel importanceField = new SearchInputFieldModel("importance", getMessageSource()
				.internationalize("test-case.importance.label", locale), MULTISELECT);
		panel.addField(importanceField);

		List<SearchInputPossibleValueModel> importanceOptions = levelComboBuilder(TestCaseImportance.values())
				.useLocale(locale).build();
		importanceField.addPossibleValues(importanceOptions);

		// **************** /natures and types ************************


		SearchInputFieldModel natureField = buildNatureFieldModel(locale,jsProjects);
		panel.addField(natureField);

		SearchInputFieldModel typeField = buildTypeFieldModel(locale,jsProjects);
		panel.addField(typeField);


		// *************** /natures and types ****************************

		SearchInputFieldModel statusField = new SearchInputFieldModel("status", getMessageSource().internationalize(
				"test-case.status.label", locale), MULTISELECT);
		panel.addField(statusField);

		List<SearchInputPossibleValueModel> statusOptions = levelComboBuilder(TestCaseStatus.values())
				.useLocale(locale).build();
		statusField.addPossibleValues(statusOptions);

		return panel;
	}


	public SearchInputPanelModel createAssociationPanel(Locale locale) {

		SearchInputPanelModel panel = new SearchInputPanelModel();

		panel.setTitle(getMessageSource().internationalize("search.testcase.association.panel.title", locale));
		panel.setOpen(true);
		panel.setId("association");
		panel.setLocation(COLUMN_1);
		panel.addCssClass("search-icon-associations");

		SearchInputFieldModel requirementsField = new SearchInputFieldModel("requirements", getMessageSource()
				.internationalize("search.testcase.association.requirement.label", locale), RANGE);
		panel.addField(requirementsField);

		SearchInputFieldModel iterationsField = new SearchInputFieldModel("iterations", getMessageSource()
				.internationalize("search.testcase.association.iteration.label", locale), EXISTS);
		panel.addField(iterationsField);

		OptionBuilder optionBuilder = optionBuilder(locale);
		iterationsField.addPossibleValue(optionBuilder.labelI18nKey("search.testcase.association.iteration.atleastone")
				.optionKey(ATLEASTONE).build());
		iterationsField.addPossibleValue(optionBuilder.labelI18nKey("search.testcase.association.iteration.none")
				.optionKey(NONE).build());

		SearchInputFieldModel executionsField = new SearchInputFieldModel("executions", getMessageSource()
				.internationalize("search.testcase.association.execution.label", locale), EXISTS);
		panel.addField(executionsField);

		executionsField.addPossibleValue(optionBuilder.labelI18nKey("search.testcase.association.execution.atleastone")
				.optionKey(ATLEASTONE).build());
		executionsField.addPossibleValue(optionBuilder.labelI18nKey("search.testcase.association.execution.none")
				.optionKey(NONE).build());

		SearchInputFieldModel issuesField = new SearchInputFieldModel("issues", getMessageSource().internationalize(
				"search.testcase.association.issue.label", locale), RANGE);
		panel.addField(issuesField);

		return panel;
	}

	public SearchInputPanelModel createPerimeterPanel(Locale locale,Collection<JsonProject> jsProjects) {
		return perimeterPanelBuilder(locale).cssClass("search-icon-perimeter").htmlId("project.id").build(jsProjects);
	}

	public SearchInputPanelModel createRequirementPerimeterPanel(Locale locale,Collection<JsonProject> jsProjects) {
		return perimeterPanelBuilder(locale).cssClass("search-icon-perimeter").htmlId("requirement.project.id").build(jsProjects);
	}

	public SearchInputPanelModel createContentPanel(Locale locale) {

		SearchInputPanelModel panel = new SearchInputPanelModel();
		panel.setTitle(getMessageSource().internationalize("search.testcase.content.panel.title", locale));
		panel.setOpen(true);
		panel.setId("content");
		panel.setLocation(COLUMN_1);
		panel.addCssClass("search-icon-content");

		SearchInputFieldModel teststepField = new SearchInputFieldModel("steps", getMessageSource().internationalize(
				"search.testcase.content.teststep.label", locale), RANGE);
		panel.addField(teststepField);

		SearchInputFieldModel parameterField = new SearchInputFieldModel("parameters", getMessageSource()
				.internationalize("search.testcase.content.parameter.label", locale), EXISTS);
		panel.addField(parameterField);

		OptionBuilder optionBuilder = optionBuilder(locale);
		parameterField.addPossibleValue(optionBuilder.labelI18nKey("search.testcase.content.parameter.atleastone")
				.optionKey(ATLEASTONE).build());
		parameterField.addPossibleValue(optionBuilder.labelI18nKey("search.testcase.content.parameter.none")
				.optionKey(NONE).build());

		SearchInputFieldModel datasetField = new SearchInputFieldModel("datasets", getMessageSource().internationalize(
				"search.testcase.content.dataset.label", locale), EXISTS);
		panel.addField(datasetField);

		datasetField.addPossibleValue(optionBuilder.labelI18nKey("search.testcase.content.dataset.atleastone")
				.optionKey(ATLEASTONE).build());
		datasetField.addPossibleValue(optionBuilder.labelI18nKey("search.testcase.content.dataset.none")
				.optionKey(NONE).build());

		SearchInputFieldModel callstepField = new SearchInputFieldModel("callsteps", getMessageSource()
				.internationalize("search.testcase.content.callstep.label", locale), EXISTS);
		panel.addField(callstepField);

		callstepField.addPossibleValue(optionBuilder.labelI18nKey("search.testcase.content.callstep.atleastone")
				.optionKey(ATLEASTONE).build());
		callstepField.addPossibleValue(optionBuilder.labelI18nKey("search.testcase.content.callstep.none")
				.optionKey(NONE).build());

		SearchInputFieldModel attachmentField = new SearchInputFieldModel("attachments", getMessageSource()
				.internationalize("search.testcase.content.attachment.label", locale), EXISTS);
		panel.addField(attachmentField);

		attachmentField.addPossibleValue(optionBuilder.labelI18nKey("search.testcase.content.attachment.atleastone")
				.optionKey(ATLEASTONE).build());
		attachmentField.addPossibleValue(optionBuilder.labelI18nKey("search.testcase.content.attachment.none")
				.optionKey(NONE).build());

		return panel;
	}

	public SearchInputPanelModel createTestCaseHistoryPanel(Locale locale, List<Long> idList) {

		SearchInputPanelModel panel = new SearchInputPanelModel();
		panel.setTitle(getMessageSource().internationalize("search.testcase.history.panel.title", locale));
		panel.setOpen(true);
		panel.setId("history");
		panel.setLocation(COLUMN_1);
		panel.addCssClass("search-icon-history");

		OptionBuilder optionBuilder = optionBuilder(locale);

		SearchInputFieldModel createdByField = new SearchInputFieldModel("createdBy", getMessageSource()
				.internationalize("search.testcase.history.createdBy.label", locale), MULTIAUTOCOMPLETE);
		panel.addField(createdByField);

		List<String> users = advancedSearchService.findAllUsersWhoCreatedTestCases(idList);
		for (String user : users) {
			createdByField.addPossibleValue(optionBuilder.label(user).optionKey(user).build());
		}

		SearchInputFieldModel createdOnField = new SearchInputFieldModel("createdOn", getMessageSource()
				.internationalize("search.testcase.history.createdOn.label", locale), DATE);
		panel.addField(createdOnField);

		SearchInputFieldModel modifiedByField = new SearchInputFieldModel("lastModifiedBy", getMessageSource()
				.internationalize("search.testcase.history.modifiedBy.label", locale), MULTIAUTOCOMPLETE);
		panel.addField(modifiedByField);

		List<String> users2 = advancedSearchService.findAllUsersWhoModifiedTestCases(idList);
		for (String user : users2) {
			if (StringUtils.isBlank(user)) {
				modifiedByField.addPossibleValue(optionBuilder.labelI18nKey("label.NeverModified").optionKey("")
						.build());
			} else {
				modifiedByField.addPossibleValue(optionBuilder.label(user).optionKey(user).build());
			}
		}

		SearchInputFieldModel modifiedOnField = new SearchInputFieldModel("lastModifiedOn", getMessageSource()
				.internationalize("search.testcase.history.modifiedOn.label", locale), DATE);
		panel.addField(modifiedOnField);

		return panel;
	}



	private SearchInputFieldModel buildNatureFieldModel(Locale locale, Collection<JsonProject> jsProjects){

		SearchInputFieldModel natureField = new SearchInputFieldModel("nature", getMessageSource().internationalize(
				"test-case.nature.label", locale), MULTICASCADEFLAT);

		Collection<JsonInfoList> natures = new ArrayList<>();

		for (JsonProject p : jsProjects){
			natures.add(p.getTestCaseNatures());
		}

		populateInfoListFieldModel(natureField, natures, locale);

		return natureField;

	}

	private SearchInputFieldModel buildTypeFieldModel(Locale locale,Collection<JsonProject> jsProjects ){

		SearchInputFieldModel typeField = new SearchInputFieldModel("type", getMessageSource().internationalize(
				"test-case.type.label", locale), MULTICASCADEFLAT);

		Collection<JsonInfoList> types = new ArrayList<>();

		for (JsonProject p : jsProjects){
			types.add(p.getTestCaseTypes());
		}
		populateInfoListFieldModel(typeField, types, locale);

		return typeField;

	}

	// get ready to puke !
	private void populateInfoListFieldModel(SearchInputFieldModel model, Collection<JsonInfoList> infoLists, Locale locale){

		InternationalizationHelper messages = getMessageSource();
		Map<String, SearchInputPossibleValueModel> listsByListCode = new HashMap<>();

		for (JsonInfoList list : infoLists){
			if (! listsByListCode.containsKey(list.getCode())){

				String listName = messages.getMessage(list.getLabel(), null, list.getLabel(), locale);
				String listCode = list.getCode();
				SearchInputPossibleValueModel listValues = new SearchInputPossibleValueModel(listName, listCode);

				SearchInputFieldModel subInput = new SearchInputFieldModel();

				for (JsonInfoListItem item : list.getItems()){
					String itemName = messages.getMessage(item.getLabel(), null, item.getLabel(), locale);
					String itemCode = item.getCode();
					subInput.addPossibleValue(new SearchInputPossibleValueModel(itemName, itemCode));
				}

				listValues.setSubInput(subInput);

				listsByListCode.put(list.getCode(), listValues);

			}
		}

		model.setPossibleValues(new ArrayList<>(listsByListCode.values()));

	}
}
