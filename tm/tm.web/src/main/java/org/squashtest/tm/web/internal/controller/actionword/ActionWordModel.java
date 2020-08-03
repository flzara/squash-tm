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
package org.squashtest.tm.web.internal.controller.actionword;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Locale;

/**
 * @author qtran - created on 28/07/2020
 */
public class ActionWordModel {
	private String name;
	private String description;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	//////////// VALIDATOR ////////////

	public static class ActionWordModelValidator implements Validator {
		private MessageSource messageSource;

		public ActionWordModelValidator(MessageSource messageSource) {
			super();
			this.messageSource = messageSource;
		}

		public MessageSource getMessageSource() {
			return messageSource;
		}

		public void setMessageSource(MessageSource messageSource) {
			this.messageSource = messageSource;
		}

		@Override
		public boolean supports(Class<?> aClass) {
			return aClass.equals(ActionWordModel.class);
		}

		@Override
		public void validate(Object target, Errors errors) {
			Locale locale = LocaleContextHolder.getLocale();
			String notBlank = messageSource.getMessage("message.actionword.empty", null, locale);
			String notNull = messageSource.getMessage("message.actionword.null", null, locale);

			ActionWordModel model = (ActionWordModel) target;
			String modelName = model.getName();

			if (modelName == null) {
				errors.rejectValue("name", "message.notNull", notNull);
			} else if (StringUtils.isBlank(modelName)) {
				errors.rejectValue("name", "message.notBlank", notBlank);
			}
		}
	}
}
