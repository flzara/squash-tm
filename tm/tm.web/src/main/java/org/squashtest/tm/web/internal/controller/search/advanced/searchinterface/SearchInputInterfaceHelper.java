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
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.customfield.*;
import org.squashtest.tm.service.campaign.CampaignAdvancedSearchService;
import org.squashtest.tm.service.feature.FeatureManager;
import org.squashtest.tm.service.internal.dto.CustomFieldModel;
import org.squashtest.tm.service.internal.dto.CustomFieldModelFactory;
import org.squashtest.tm.service.internal.dto.UserDto;
import org.squashtest.tm.service.internal.dto.json.JsonProject;
import org.squashtest.tm.service.testcase.TestCaseAdvancedSearchService;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;

@Component
public class SearchInputInterfaceHelper {

	private static final String TEXTFIELD = "textfield";
	private static final String COMBOMULTISELECT = "combomultiselect";
	private static final String TAGS = "tags";

	@Inject
	protected InternationalizationHelper messageSource;

	@Inject
	private RequirementVersionSearchInterfaceDescription requirementVersionSearchInterfaceDescription;

	@Inject
	private TestcaseSearchInterfaceDescription testcaseVersionSearchInterfaceDescription;

	@Inject
	private CampaignSearchInterfaceDescription campaignSearchInterfaceDescription;

	@Inject
	private FeatureManager featureManager;

	@Inject
	private TestCaseAdvancedSearchService advancedSearchService;

	@Inject
	private CampaignAdvancedSearchService campaignAdvancedSearchService;

	public SearchInputInterfaceModel getRequirementSearchInputInterfaceModel(Locale locale, boolean isMilestoneMode,UserDto currentUser,List<Long> readableProjectIds,Collection<JsonProject> jsProjects) {

		SearchInputInterfaceModel model = new SearchInputInterfaceModel();

		// Perimeter
		model.addPanel(requirementVersionSearchInterfaceDescription.createRequirementPerimeterPanel(locale,jsProjects));

		// Information
		model.addPanel(requirementVersionSearchInterfaceDescription.createRequirementInformationPanel(locale));

		// History
		model.addPanel(requirementVersionSearchInterfaceDescription.createRequirementHistoryPanel(locale,readableProjectIds));

		// Attributes
		model.addPanel(requirementVersionSearchInterfaceDescription.createRequirementAttributePanel(locale,currentUser,readableProjectIds, jsProjects));

		// Milestones
		if (!isMilestoneMode && featureManager.isEnabled(FeatureManager.Feature.MILESTONE)) {
			model.addPanel(requirementVersionSearchInterfaceDescription.createMilestonePanel(locale));
		}

		// Version
		model.addPanel(requirementVersionSearchInterfaceDescription.createRequirementVersionPanel(locale));

		// Content
		model.addPanel(requirementVersionSearchInterfaceDescription.createRequirementContentPanel(locale));

		// Associations
		model.addPanel(requirementVersionSearchInterfaceDescription.createRequirementAssociationPanel(locale));

		// CUFs
		model.addPanel(createCUFPanel(locale, BindableEntity.REQUIREMENT_VERSION,readableProjectIds));

		return model;
	}

	public SearchInputInterfaceModel getTestCaseSearchInputInterfaceModel(Locale locale, boolean isMilestoneMode,UserDto currentUser ,List<Long> readableProjectIds,Collection<JsonProject> jsProjects) {

		SearchInputInterfaceModel model = new SearchInputInterfaceModel();


		// Perimeter
		model.addPanel(testcaseVersionSearchInterfaceDescription.createPerimeterPanel(locale,jsProjects));

		// Information
		model.addPanel(testcaseVersionSearchInterfaceDescription.createGeneralInfoPanel(locale));

		// History
		model.addPanel(testcaseVersionSearchInterfaceDescription.createTestCaseHistoryPanel(locale,readableProjectIds));

		// Attributes
		model.addPanel(testcaseVersionSearchInterfaceDescription.createAttributePanel(locale,jsProjects));

		// Milestones
		if (!isMilestoneMode && featureManager.isEnabled(FeatureManager.Feature.MILESTONE)) {
			model.addPanel(testcaseVersionSearchInterfaceDescription.createMilestonePanel(locale));
		}

		// Content
		model.addPanel(testcaseVersionSearchInterfaceDescription.createContentPanel(locale));

		// Associations
		model.addPanel(testcaseVersionSearchInterfaceDescription.createAssociationPanel(locale));

		// CUF
		model.addPanel(createCUFPanel(locale, BindableEntity.TEST_CASE,readableProjectIds));

		return model;
	}

	public SearchInputInterfaceModel getCampaignSearchInputInterfaceModel(Locale locale, boolean isMilestoneMode,UserDto currentUser,List<Long> readableProjectIds,Collection<JsonProject> jsProjects) {

		SearchInputInterfaceModel model = new SearchInputInterfaceModel();
		List<String> users = campaignAdvancedSearchService.findAllAuthorizedUsersForACampaign(readableProjectIds);
		// Information
		model.addPanel(campaignSearchInterfaceDescription.createGeneralInfoPanel(locale));

		// Attributes
		model.addPanel(campaignSearchInterfaceDescription.createAttributePanel(locale,users ));

		// Milestones
		if (!isMilestoneMode && featureManager.isEnabled(FeatureManager.Feature.MILESTONE)) {
			model.addPanel(requirementVersionSearchInterfaceDescription.createMilestonePanel(locale));
		}

		model.addPanel(campaignSearchInterfaceDescription.createExecutionPanel(locale,users));

		return model;
	}

	private SearchInputPanelModel createCUFPanel(Locale locale, BindableEntity bindableEntity,List<Long> readableProjectIds ) {

		SearchInputPanelModel panel = getCustomFielModel(locale, bindableEntity,readableProjectIds);
		panel.setTitle(messageSource.internationalize("search.testcase.cuf.panel.title", locale));
		panel.setOpen(true);
		panel.setId("cuf");
		panel.setLocation("column1");
		panel.addCssClass("search-icon-cuf");
		return panel;
	}

	private SearchInputPanelModel getCustomFielModel(Locale locale, BindableEntity bindableEntity,List<Long> readableProjectIds) {

		List<CustomFieldModel> customFields = advancedSearchService
				.findAllQueryableCustomFieldsByBoundEntityType(bindableEntity,readableProjectIds);
		return convertToSearchInputPanelModel(customFields, locale);
	}

	private SearchInputPanelModel convertToSearchInputPanelModel(List<CustomFieldModel> customFields, Locale locale) {
		SearchInputPanelModel model = new SearchInputPanelModel();
		for (CustomFieldModel customField : customFields) {

			switch (InputType.valueOf(customField.getInputType().getEnumName())) {
			case DROPDOWN_LIST:
				CustomFieldModelFactory.SingleSelectFieldModel singleSelectFieldModel = (CustomFieldModelFactory.SingleSelectFieldModel)customField;
				SingleSelectField selectField = new SingleSelectField();
				selectField.setCode(singleSelectFieldModel.getCode());
				selectField.setDefaultValue(singleSelectFieldModel.getDefaultValue().toString());
				selectField.setLabel(singleSelectFieldModel.getLabel());
				selectField.setName(singleSelectFieldModel.getName());
				selectField.setOptional(singleSelectFieldModel.isOptional());
				for(CustomFieldModelFactory.CustomFieldOptionModel optionModel : singleSelectFieldModel.getOptions()){
					CustomFieldOption customFieldModel = new CustomFieldOption(optionModel.getLabel(),optionModel.getCode());
					if(!selectField.getOptions().contains(customFieldModel)) {
						if(customFieldModel.getCode()!=null&&customFieldModel.getLabel()!=null) {
							selectField.addOption(customFieldModel);
						}
					}
				}
				model.getFields().add(dropdownConvertToSearchInputFieldModel(selectField, locale));
				break;

			case PLAIN_TEXT:
				model.getFields().add(convertToSearchInputFieldModel(customField));
				break;

			case NUMERIC:
				model.getFields().add(createNumericRangeField(customField));
				break;

			case CHECKBOX:
				model.getFields().add(createCheckBoxField(customField, locale));
				break;

			case DATE_PICKER:
				model.getFields().add(createDateCustomFieldSearchModel(customField));
				break;

			case TAG:
				CustomFieldModelFactory.MultiSelectFieldModel multiSelectFieldModel = (CustomFieldModelFactory.MultiSelectFieldModel)customField;
				MultiSelectField multiSelectField = new MultiSelectField();
				multiSelectField.setCode(multiSelectFieldModel.getCode());
				multiSelectField.setDefaultValue(multiSelectFieldModel.getDefaultValue().toString());
				multiSelectField.setLabel(multiSelectFieldModel.getLabel());
				multiSelectField.setName(multiSelectFieldModel.getName());
				multiSelectField.setOptional(multiSelectFieldModel.isOptional());
				for(CustomFieldModelFactory.CustomFieldOptionModel optionModel :  multiSelectFieldModel.getOptions()) {
					CustomFieldOption customFieldOptionModel = new CustomFieldOption(optionModel.getLabel());
					if (!multiSelectField.getOptions().contains(customFieldOptionModel)) {
						multiSelectField.addOption(optionModel.getLabel());
					}
				}
				model.getFields().add(multiSelectFieldConvertToSearchInputFieldModel(multiSelectField));
				break;

			case RICH_TEXT:
				break; // not supported for now
			}

		}
		return model;
	}

	private SearchInputFieldModel createDateCustomFieldSearchModel(CustomFieldModel customField) {

		SearchInputFieldModel model = new SearchInputFieldModel();
		model.setInputType("CF_TIME_INTERVAL");
		model.setTitle(customField.getLabel());
		model.setId(customField.getCode());
		model.setIgnoreBridge(true);
		return model;
	}

	private SearchInputFieldModel createCheckBoxField(CustomFieldModel customFieldModel, Locale locale) {
		SearchInputFieldModel model = new SearchInputFieldModel();

		List<SearchInputPossibleValueModel> possibleValues = new ArrayList<>();

		possibleValues
				.add(new SearchInputPossibleValueModel(messageSource.internationalize("label.True", locale), "true"));
		possibleValues
				.add(new SearchInputPossibleValueModel(messageSource.internationalize("label.False", locale), "false"));

		model.setPossibleValues(possibleValues);
		model.setInputType(COMBOMULTISELECT);
		model.setTitle(customFieldModel.getLabel());
		model.setId(customFieldModel.getCode());
		model.setIgnoreBridge(true);
		return model;
	}

	private SearchInputFieldModel createNumericRangeField (CustomFieldModel customField) {
		SearchInputFieldModel model = new SearchInputFieldModel();
		model.setInputType(SearchInterfaceDescription.NUMERICRANGE);
		model.setTitle(customField.getLabel());
		model.setId(customField.getCode());
		model.setIgnoreBridge(true);
		return model;
	}

	private SearchInputFieldModel convertToSearchInputFieldModel(CustomFieldModel customField) {
		SearchInputFieldModel model = new SearchInputFieldModel();
		model.setInputType(TEXTFIELD);
		model.setTitle(customField.getLabel());
		model.setId(customField.getCode());
		model.setIgnoreBridge(true);
		return model;
	}

	private SearchInputFieldModel dropdownConvertToSearchInputFieldModel(SingleSelectField selectField, Locale locale) {
		List<SearchInputPossibleValueModel> possibleValues = new ArrayList<>();
		possibleValues.add(new SearchInputPossibleValueModel(messageSource.internationalize("label.Empty", locale), ""));
		for (CustomFieldOption option : selectField.getOptions()) {
			possibleValues.add(new SearchInputPossibleValueModel(option.getLabel(), option.getLabel()));
		}
		SearchInputFieldModel model = new SearchInputFieldModel();
		model.setInputType(COMBOMULTISELECT);
		model.setTitle(selectField.getLabel());
		model.setPossibleValues(possibleValues);
		model.setId(selectField.getCode());
		model.setIgnoreBridge(true);
		return model;
	}

	private SearchInputFieldModel multiSelectFieldConvertToSearchInputFieldModel(MultiSelectField multifield) {
		List<SearchInputPossibleValueModel> possibleValues = new ArrayList<>(multifield.getOptions().size());

		for (CustomFieldOption option : multifield.getOptions()) {
			possibleValues.add(new SearchInputPossibleValueModel(option.getLabel(), option.getLabel()));
		}

		SearchInputFieldModel model = new SearchInputFieldModel();
		model.setInputType(TAGS);
		model.setTitle(multifield.getLabel());
		model.setPossibleValues(possibleValues);
		model.setId(multifield.getCode());
		model.setIgnoreBridge(true);
		return model;

	}


}
