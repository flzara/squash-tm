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

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.testcase.TestCaseExecutionMode;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.service.internal.dto.json.JsonProject;

@Component
public class CampaignSearchInterfaceDescription extends SearchInterfaceDescription {

	private static final String COLUMN_1 = "column1";

	public SearchInputPanelModel createGeneralInfoPanel(Locale locale) {
		SearchInputPanelModel panel = new SearchInputPanelModel();
		panel.setTitle(getMessageSource().internationalize("search.testcase.generalinfos.panel.title", locale));
		panel.setOpen(true);
		panel.setId("general-information-fullsize");
		panel.setLocation(COLUMN_1);
		panel.addCssClass("search-icon-information");

		SearchInputFieldModel idField = new SearchInputFieldModel("referencedTestCase.id",
				getMessageSource().internationalize(
"referenced-tc.id", locale), TEXTFIELD);
		panel.addField(idField);
		SearchInputFieldModel referenceField = new SearchInputFieldModel("referencedTestCase.reference",
				getMessageSource()
				.internationalize("label.reference", locale), TEXTFIELDREFERENCE);
		panel.addField(referenceField);

		SearchInputFieldModel labelField = new SearchInputFieldModel("referencedTestCase.name",
				getMessageSource().internationalize("label.Label", locale), TEXTFIELD);
		panel.addField(labelField);

		return panel;
	}

	public SearchInputPanelModel createAttributePanel(Locale locale, List<String> users) {
		SearchInputPanelModel panel = new SearchInputPanelModel();
		panel.setTitle(getMessageSource().internationalize("search.testcase.attributes.panel.title", locale));
		panel.setOpen(true);
		panel.setId("attributes");
		panel.setLocation(COLUMN_1);
		panel.addCssClass("search-icon-attributes");

		SearchInputFieldModel importanceField = new SearchInputFieldModel("referencedTestCase.importance",
				getMessageSource()
				.internationalize("test-case.importance.label", locale), MULTISELECT);
		panel.addField(importanceField);

		List<SearchInputPossibleValueModel> importanceOptions = levelComboBuilder(TestCaseImportance.values())
				.useLocale(locale).build();
		importanceField.addPossibleValues(importanceOptions);

		// *************** Assignment ****************************

		SearchInputFieldModel assignmentField = new SearchInputFieldModel("user",
				getMessageSource()
				.internationalize("search.execution.assignation", locale), MULTIAUTOCOMPLETE);
		panel.addField(assignmentField);

		/* TODO : Get all assignmentable users */
		OptionBuilder optionBuilder = optionBuilder(locale);

		for (String user : users) {
			assignmentField.addPossibleValue(optionBuilder.label(user).optionKey(user).build());
		}

		return panel;
	}


	public SearchInputPanelModel createExecutionPanel(Locale locale,List<String> users) {

		SearchInputPanelModel panel = new SearchInputPanelModel();
		panel.setTitle(getMessageSource().internationalize("search.execution.label", locale));
		panel.setOpen(true);
		panel.setId("execution");
		panel.setLocation(COLUMN_1);
		panel.addCssClass("search-icon-execution");

		// Created on
		SearchInputFieldModel lastExecuted = new SearchInputFieldModel("lastExecutedOn",
				getMessageSource()
				.internationalize("search.execution.executed.label", locale), DATE);
		panel.addField(lastExecuted);



		OptionBuilder optionBuilder = optionBuilder(locale);


		SearchInputFieldModel authorizedUsersField = new SearchInputFieldModel("lastExecutedBy",
				getMessageSource().internationalize("search.execution.executedby.label", locale), MULTIAUTOCOMPLETE);
			for (String user : users) {
			authorizedUsersField.addPossibleValue(optionBuilder.label(user).optionKey(user).build());
		}

		panel.addField(authorizedUsersField);

		// Status
		SearchInputFieldModel statusField = new SearchInputFieldModel("executionStatus", getMessageSource()
				.internationalize(
				"test-case.status.label", locale), MULTISELECT);
		panel.addField(statusField);


		ExecutionStatus[] values = ExecutionStatus.values();
		List<SearchInputPossibleValueModel> statusOptions = levelComboBuilder(values)
				.useLocale(locale).build();
		statusField.addPossibleValues(statusOptions);

		// Mode
		SearchInputFieldModel executionModeField = new SearchInputFieldModel("executionMode", getMessageSource()
				.internationalize("search.execution.mode.label", locale), MULTISELECT);
		panel.addField(executionModeField);

		List<SearchInputPossibleValueModel> modeOptions = levelComboBuilder(TestCaseExecutionMode.values())
				.useLocale(locale).build();
		executionModeField.addPossibleValues(modeOptions);

		return panel;
	}

	public SearchInputPanelModel createPerimeterPanel(Locale locale,Collection<JsonProject> jsProjects) {
		return perimeterPanelBuilder(locale).cssClass("search-icon-perimeter").htmlId("project.id").build(jsProjects);
	}

}
