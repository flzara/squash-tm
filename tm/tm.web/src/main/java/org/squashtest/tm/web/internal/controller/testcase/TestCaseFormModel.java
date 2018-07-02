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
package org.squashtest.tm.web.internal.controller.testcase;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.squashtest.tm.domain.customfield.RawValue;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.internal.dto.RawValueModel;
import org.squashtest.tm.service.internal.dto.RawValueModel.RawValueModelMap;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

// XSS OK
public class TestCaseFormModel {
	/**
	 * Note : the following validation annotations are never called, a custom validator will be invoked for this.
	 *
	 */

	private static final String MESSAGE_NOT_BLANK = "message.notBlank";

	/*@NotBlank
	@NotNull*/
	private String name;

	private String reference;
	private String description;

	private String scriptLanguage;


	/*@NotNull
	@NotEmpty*/
	private RawValueModelMap customFields = new RawValueModelMap();


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public RawValueModelMap getCustomFields() {
		return customFields;
	}

	public String getScriptLanguage() {
		return scriptLanguage;
	}

	public void setScriptLanguage(String scriptLanguage) {
		this.scriptLanguage = scriptLanguage;
	}

	public void setCustomFields(RawValueModelMap customFields) {
		this.customFields = customFields;
	}


	public TestCase getTestCase() {
		TestCase newTC = new TestCase();
		newTC.setName(name);
		newTC.setDescription(description);
		newTC.setReference(reference);
		if (StringUtils.isNotBlank(scriptLanguage)) {
			String locale = LocaleContextHolder.getLocale().getLanguage();
			newTC.extendWithScript(scriptLanguage, locale);
		}
		return newTC;
	}

	@JsonIgnore
	public Map<Long, RawValue> getCufs() {
		Map<Long, RawValue> cufs = new HashMap<>(customFields.size());
		for (Entry<Long, RawValueModel> entry : customFields.entrySet()) {
			cufs.put(entry.getKey(), entry.getValue().toRawValue());
		}
		return cufs;
	}


	public static class TestCaseFormModelValidator implements Validator {

		private MessageSource messageSource;

		public TestCaseFormModelValidator(MessageSource messageSource) {
			this.messageSource = messageSource;
		}

		public void setMessageSource(MessageSource messageSource) {
			this.messageSource = messageSource;
		}

		@Override
		public boolean supports(Class<?> clazz) {
			return clazz.equals(TestCaseFormModel.class);
		}

		@Override
		public void validate(Object target, Errors errors) {

			Locale locale = LocaleContextHolder.getLocale();
			String notBlank = messageSource.getMessage(MESSAGE_NOT_BLANK, null, locale);
			String lengthMax = messageSource.getMessage("message.lengthMax", new Object[]{"50"}, locale);

			TestCaseFormModel model = (TestCaseFormModel) target;

			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", MESSAGE_NOT_BLANK, notBlank);

			if (model.reference != null && model.reference.length() > 50) {
				errors.rejectValue("reference", "message.lengthMax", lengthMax);
			}

			for (Entry<Long, RawValueModel> entry : model.getCustomFields().entrySet()) {
				RawValueModel value = entry.getValue();
				if (value.isEmpty()) {
					errors.rejectValue("customFields[" + entry.getKey() + "]", MESSAGE_NOT_BLANK, notBlank);
				}
			}


		}

	}


}
