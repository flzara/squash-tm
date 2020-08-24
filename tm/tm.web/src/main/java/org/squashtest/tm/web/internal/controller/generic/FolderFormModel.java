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
package org.squashtest.tm.web.internal.controller.generic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.squashtest.tm.domain.customfield.RawValue;
import org.squashtest.tm.domain.library.NewFolderDto;
import org.squashtest.tm.service.internal.dto.RawValueModel;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FolderFormModel {

	/*
	 * @NotBlank
	 * @NotNull
	 */
	private String name;
	private String description;
	private RawValueModel.RawValueModelMap customFields;


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

	public RawValueModel.RawValueModelMap getCustomFields() {
		return customFields;
	}

	public void setCustomFields(RawValueModel.RawValueModelMap customFields) {
		this.customFields = customFields;
	}

	@JsonIgnore
	public NewFolderDto toDTO() {
		NewFolderDto dto = new NewFolderDto();

		dto.setName(name);
		dto.setDescription(description);

		Map<Long, RawValue> cufs = new HashMap<>(customFields.size());
		for (Map.Entry<Long, RawValueModel> entry : customFields.entrySet()) {
			cufs.put(entry.getKey(), entry.getValue().toRawValue());
		}
		dto.setCustomFields(cufs);

		return dto;
	}

	public static class FolderFormModelValidator implements Validator {

		private static final String MESSAGE_NOT_BLANK = "message.notBlank";

		private MessageSource messageSource;

		public FolderFormModelValidator(MessageSource messageSource) {
			super();
			this.messageSource = messageSource;
		}

		public void setMessageSource(MessageSource messageSource) {
			this.messageSource = messageSource;
		}

		@Override
		public boolean supports(Class<?> clazz) {
			return clazz.equals(NewFolderDto.class);
		}

		@Override
		public void validate(Object target, Errors errors) {
			Locale locale = LocaleContextHolder.getLocale();
			String notBlank = messageSource.getMessage(MESSAGE_NOT_BLANK, null, locale);

			FolderFormModel model = (FolderFormModel) target;

			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", MESSAGE_NOT_BLANK, notBlank);

			for (Map.Entry<Long, RawValueModel> entry : model.getCustomFields().entrySet()) {
				RawValueModel value = entry.getValue();
				if (value.isEmpty()) {
					errors.rejectValue("customFields[" + entry.getKey() + "]", MESSAGE_NOT_BLANK, notBlank);
				}
			}
		}

	}
}
