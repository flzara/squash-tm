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
package org.squashtest.tm.web.internal.controller.testcase.steps;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.squashtest.tm.domain.customfield.RawValue;
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.service.internal.dto.RawValueModel;
import org.squashtest.tm.service.internal.dto.RawValueModel.RawValueModelMap;

import com.fasterxml.jackson.annotation.JsonIgnore;


public class ActionStepFormModel {
	private String action="";

	private String expectedResult="";

	private RawValueModelMap customFields = new RawValueModelMap();

	private int index;

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getExpectedResult() {
		return expectedResult;
	}

	public void setExpectedResult(String expectedResult) {
		this.expectedResult = expectedResult;
	}

	public RawValueModelMap getCustomFields() {
		return customFields;
	}

	public void setCustomFields(RawValueModelMap customFields) {
		this.customFields = customFields;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	@JsonIgnore
	public Map<Long, RawValue> getCufs(){
		Map<Long, RawValue> cufs = new HashMap<>(customFields.size());
		for (Entry<Long, RawValueModel> entry : customFields.entrySet()){
			cufs.put(entry.getKey(), entry.getValue().toRawValue());
		}
		return cufs;
	}

	public ActionTestStep getActionTestStep(){
		ActionTestStep newStep = new ActionTestStep();
		newStep.setAction(action);
		newStep.setExpectedResult(expectedResult);
		return newStep;
	}

	public static class ActionStepFormModelValidator implements Validator{

		private MessageSource messageSource;

		public void setMessageSource(MessageSource messageSource) {
			this.messageSource = messageSource;
		}

		@Override
		public boolean supports(Class<?> clazz) {
			return clazz.equals(ActionStepFormModel.class);
		}


		public ActionStepFormModelValidator(MessageSource messageSource) {
			super();
			this.messageSource = messageSource;
		}

		@Override
		public void validate(Object target, Errors errors) {
			Locale locale = LocaleContextHolder.getLocale();
			String notBlank = messageSource.getMessage("message.notBlank",null, locale);

			ActionStepFormModel model = (ActionStepFormModel) target;

			for (Entry<Long, RawValueModel> entry : model.getCustomFields().entrySet()){
				RawValueModel value = entry.getValue();
				if (value.isEmpty()){
					errors.rejectValue("customFields["+entry.getKey()+"]", "message.notBlank", notBlank);
				}
			}

		}


	}

}
