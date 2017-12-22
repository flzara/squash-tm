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
package org.squashtest.tm.web.internal.controller.requirement;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.squashtest.tm.domain.customfield.RawValue;
import org.squashtest.tm.domain.requirement.NewRequirementVersionDto;
import org.squashtest.tm.domain.requirement.RequirementCriticality;
import org.squashtest.tm.service.internal.dto.RawValueModel;
import org.squashtest.tm.service.internal.dto.RawValueModel.RawValueModelMap;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class RequirementFormModel {

	/*
	 * @NotBlank
	 * @NotNull
	 */
	private String name;

	private String description;

	/*@NotNull*/
	private RequirementCriticality criticality;

	/*@NotNull*/
	private String category;

	private String reference;

	private RawValueModelMap customFields;




	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getDescription() {
		return description;
	}


	public void setDescription(String description) {
		this.description = description;
	}


	public RequirementCriticality getCriticality() {
		return criticality;
	}


	public void setCriticality(RequirementCriticality criticality) {
		this.criticality = criticality;
	}


	public String getCategory() {
		return category;
	}


	public void setCategory(String category) {
		this.category = category;
	}


	public String getReference() {
		return reference;
	}


	public void setReference(String reference) {
		this.reference = reference;
	}


	public RawValueModelMap getCustomFields() {
		return customFields;
	}


	public void setCustomFields(RawValueModelMap customFields) {
		this.customFields = customFields;
	}


	/*
	 * Check out what does NewRequirementVersionDto and laugh
	 */
	@JsonIgnore
	public NewRequirementVersionDto toDTO(){
		NewRequirementVersionDto dto = new NewRequirementVersionDto();

		dto.setName(name);
		dto.setReference(reference);
		dto.setCategory(category);
		dto.setCriticality(criticality);
		dto.setDescription(description);


		Map<Long, RawValue> cufs = new HashMap<>(customFields.size());
		for (Entry<Long, RawValueModel> entry : customFields.entrySet()){
			cufs.put(entry.getKey(), entry.getValue().toRawValue());
		}
		dto.setCustomFields(cufs);

		return dto;
	}


	public static class RequirementFormModelValidator implements Validator{

		/**
		 *
		 */
		private static final String MESSAGE_LENGTH_MAX = "message.lengthMax";
		/**
		 *
		 */
		private static final String MESSAGE_NOT_BLANK = "message.notBlank";

		private MessageSource messageSource;

		public void setMessageSource(MessageSource messageSource) {
			this.messageSource = messageSource;
		}




		public RequirementFormModelValidator(MessageSource messageSource) {
			super();
			this.messageSource = messageSource;
		}




		@Override
		public boolean supports(Class<?> clazz) {
			return clazz.equals(NewRequirementVersionDto.class);
		}

		@Override
		public void validate(Object target, Errors errors) {
			Locale locale = LocaleContextHolder.getLocale();
			String notBlank = messageSource.getMessage(MESSAGE_NOT_BLANK, null, locale);
			String lengthMax = messageSource.getMessage(MESSAGE_LENGTH_MAX, new Object[]{"50"}, locale);

			RequirementFormModel model = (RequirementFormModel) target;

			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", MESSAGE_NOT_BLANK, notBlank);

			if (model.criticality==null){
				errors.rejectValue("criticality", MESSAGE_NOT_BLANK, notBlank);
			}

			if (model.category==null){
				errors.rejectValue("category", MESSAGE_NOT_BLANK, notBlank);
			}

			if (model.reference != null && model.reference.length()>50){
				errors.rejectValue("reference", MESSAGE_LENGTH_MAX, lengthMax);
			}


			for (Entry<Long, RawValueModel> entry : model.getCustomFields().entrySet()){
				RawValueModel value = entry.getValue();
				if (value.isEmpty()){
					errors.rejectValue("customFields["+entry.getKey()+"]", "message.notBlank", notBlank);
				}
			}

		}

	}



}
