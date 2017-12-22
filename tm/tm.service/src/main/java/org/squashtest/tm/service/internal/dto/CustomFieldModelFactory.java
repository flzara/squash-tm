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
package org.squashtest.tm.service.internal.dto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.inject.Inject;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.customfield.CustomFieldOption;
import org.squashtest.tm.domain.customfield.InputType;
import org.squashtest.tm.domain.customfield.MultiSelectField;
import org.squashtest.tm.domain.customfield.SingleSelectField;
import org.squashtest.tm.domain.denormalizedfield.DenormalizedFieldValue;
import org.squashtest.tm.domain.denormalizedfield.DenormalizedMultiSelectField;
import org.squashtest.tm.domain.denormalizedfield.DenormalizedSingleSelectField;


@Component
public class CustomFieldModelFactory {

	@Inject
	private MessageSource messageSource;

	InputTypeModel createInputTypeModel(InputType type) {

		InputTypeModel model = new InputTypeModel();

		model.setEnumName(type.toString());
		model.setFriendlyName(getMessage(type.getI18nKey()));

		return model;
	}

	CustomFieldModel<?> createCustomFieldModel(CustomField customField) {

		CustomFieldModel<?> model;

		switch (customField.getInputType()) {
		case DATE_PICKER:
			model = createDatePickerFieldModel(customField);
			break;

		case DROPDOWN_LIST:
			model = createSingleSelectFieldModel((SingleSelectField) customField); // NOSONAR a CustomField which has
			// InputType == DROPDOWN_LIST is
			// always a SingleSelectField
			break;

		case TAG:
			model = createMultiSelectFieldModel((MultiSelectField) customField); // NOSONAR a CustomField which has
			// InputType == TAG is always a
			// MultiSelectField
			break;

		default:
			model = createCustomField(customField);
			break;
		}

		return model;
	}

	CustomFieldModel<?> createCustomFieldModel(DenormalizedFieldValue customField) {

		CustomFieldModel<?> model;

		switch (customField.getInputType()) {
		case DATE_PICKER:
			model = createDatePickerFieldModel(customField);
			break;

		case DROPDOWN_LIST:
			model = createSingleSelectFieldModel((DenormalizedSingleSelectField) customField); // NOSONAR a CustomField which has
			// InputType == DROPDOWN_LIST is
			// always a SingleSelectField
			break;

		case TAG:

			model = createMultiSelectFieldModel((DenormalizedMultiSelectField) customField); // NOSONAR a CustomField which has
			// InputType == TAG is always a
			// MultiSelectField
			break;

		default:
			model = createCustomField(customField);
			break;
		}

		return model;
	}


	// ************ private area. Get out. Tss. ************************ */

	// ********* methods for the (live) CustomFieldValue *************** */

	private CustomFieldModel<?> createCustomField(CustomField customField) {

		CustomFieldModel<String> model = new SingleValuedCustomFieldModel();

		populateCustomFieldModel(model, customField);

		model.setDefaultValue(customField.getDefaultValue());

		return model;

	}

	private CustomFieldModel<?> createSingleSelectFieldModel(SingleSelectField customField) {

		SingleSelectFieldModel model = new SingleSelectFieldModel();

		populateCustomFieldModel(model, customField);

		model.setDefaultValue(customField.getDefaultValue());

		for (CustomFieldOption option : customField.getOptions()) {
			CustomFieldOptionModel newOption = new CustomFieldOptionModel();
			newOption.setLabel(option.getLabel());
			newOption.setCode(option.getCode());
			model.addOption(newOption);
		}

		return model;
	}

	private CustomFieldModel<?> createDatePickerFieldModel(CustomField field) {

		Locale locale = LocaleContextHolder.getLocale();

		DatePickerFieldModel model = new DatePickerFieldModel();

		populateCustomFieldModel(model, field);

		model.setDefaultValue(field.getDefaultValue());
		model.setFormat(getMessage("squashtm.dateformatShort.datepicker"));
		model.setLocale(locale.toString());

		return model;

	}

	private CustomFieldModel<?> createMultiSelectFieldModel(MultiSelectField field) {

		MultiSelectFieldModel model = new MultiSelectFieldModel();

		populateCustomFieldModel(model, field);

		for (CustomFieldOption option : field.getOptions()) {
			CustomFieldOptionModel newOption = new CustomFieldOptionModel();
			newOption.setLabel(option.getLabel());
			newOption.setCode(option.getCode());
			model.addOption(newOption);
		}

		for (String value : field.getDefaultValue().split(MultiSelectField.SEPARATOR_EXPR)) {
			model.addDefaultValue(value);
		}

		return model;

	}


	private <VT> CustomFieldModel<VT> populateCustomFieldModel(CustomFieldModel<VT> model, CustomField field) {

		InputTypeModel typeModel = createInputTypeModel(field.getInputType());

		model.setId(field.getId());
		model.setName(field.getName());
		model.setLabel(field.getLabel());
		model.setOptional(field.isOptional());
		model.setInputType(typeModel);
		model.setFriendlyOptional(field.isOptional() ? getMessage("label.Yes") : getMessage("label.No"));
		model.setCode(field.getCode());

		return model;
	}


	// ********* methods for the (dead) DenormalizedFieldValue *************** */

	private CustomFieldModel<?> createCustomField(DenormalizedFieldValue customField) {

		CustomFieldModel<String> model = new SingleValuedCustomFieldModel();

		populateCustomFieldModel(model, customField);

		return model;

	}

	private CustomFieldModel<?> createSingleSelectFieldModel(DenormalizedSingleSelectField customField) {

		SingleSelectFieldModel model = new SingleSelectFieldModel();

		populateCustomFieldModel(model, customField);

		for (CustomFieldOption option : customField.getOptions()) {
			CustomFieldOptionModel newOption = new CustomFieldOptionModel();
			newOption.setLabel(option.getLabel());
			newOption.setCode(option.getCode());
			model.addOption(newOption);
		}

		return model;
	}

	private CustomFieldModel<?> createDatePickerFieldModel(DenormalizedFieldValue field) {

		Locale locale = LocaleContextHolder.getLocale();

		DatePickerFieldModel model = new DatePickerFieldModel();

		populateCustomFieldModel(model, field);

		model.setFormat(getMessage("squashtm.dateformatShort.datepicker"));
		model.setLocale(locale.toString());

		return model;

	}

	private CustomFieldModel<?> createMultiSelectFieldModel(DenormalizedMultiSelectField field) {

		MultiSelectFieldModel model = new MultiSelectFieldModel();
		populateCustomFieldModel(model, field);

		for (CustomFieldOption option : field.getOptions()){
			CustomFieldOptionModel newOption = new CustomFieldOptionModel();
			newOption.setLabel(option.getLabel());
			newOption.setCode(option.getCode());
			model.addOption(newOption);
		}

		return model;
	}

	private <VT> CustomFieldModel<VT> populateCustomFieldModel(CustomFieldModel<VT> customFieldModel, DenormalizedFieldValue value) {

		InputTypeModel inputTypeModel = new InputTypeModel();
		inputTypeModel.setEnumName(value.getInputType().name());
		inputTypeModel.setFriendlyName(value.getInputType().name());

		customFieldModel.setCode(value.getCode());
		customFieldModel.setId(value.getId());
		customFieldModel.setInputType(inputTypeModel);
		customFieldModel.setLabel(value.getLabel());
		customFieldModel.setOptional(true);
		customFieldModel.setDenormalized(true);

		return customFieldModel;
	}



	/* **************** misc ****************************** */

	private String getMessage(String key) {
		Locale locale = LocaleContextHolder.getLocale();
		return messageSource.getMessage(key, null, locale);
	}



	/* ******************* various implementations ******************* */

	public static class SingleValuedCustomFieldModel extends CustomFieldModel<String> {

		private String defaultValue;

		@Override
		public String getDefaultValue() {
			return defaultValue;
		}

		@Override
		public void setDefaultValue(String defaultValue) {
			this.defaultValue = defaultValue;
		}

	}

	public static class DatePickerFieldModel extends SingleValuedCustomFieldModel {

		private String format;

		private String locale;

		public String getFormat() {
			return format;
		}

		public void setFormat(String format) {
			this.format = format;
		}

		public String getLocale() {
			return locale;
		}

		public void setLocale(String locale) {
			this.locale = locale;
		}

	}

	public static class SingleSelectFieldModel extends SingleValuedCustomFieldModel {

		private List<CustomFieldOptionModel> options = new LinkedList<>();

		public List<CustomFieldOptionModel> getOptions() {
			return options;
		}

		public void setOptions(List<CustomFieldOptionModel> options) {
			this.options = options;
		}

		public void addOption(CustomFieldOptionModel option) {
			options.add(option);
		}

	}

	public static class MultiSelectFieldModel extends CustomFieldModel<String[]> {

		private List<String> defaultValue = new LinkedList<>();
		private Set<CustomFieldOptionModel> options = new HashSet<>();

		@Override
		public String[] getDefaultValue() {
			return defaultValue.toArray(new String[defaultValue.size()]);
		}

		@Override
		public void setDefaultValue(String[] defaultValue) {
			this.defaultValue = new ArrayList<>(Arrays.asList(defaultValue));
		}

		public void addDefaultValue(String newValue) {
			this.defaultValue.add(newValue);
		}

		public Set<CustomFieldOptionModel> getOptions() {
			return options;
		}

		public void setOptions(Set<CustomFieldOptionModel> options) {
			this.options = options;
		}

		public void addOption(CustomFieldOptionModel option) {
			options.add(option);
		}

	}

	public static class CustomFieldOptionModel {

		private String label;

		private String code;

		public CustomFieldOptionModel(){
			super();
		}

		public CustomFieldOptionModel(String label, String code){
			super();
			this.label = label;
			this.code = code;
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
		}
	}


}
