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

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.squashtest.tm.service.internal.testcase.bdd.KeywordTestStepActionWordParser;

import java.util.Locale;

public class KeywordTestStepModel {

	private String keyword;

	private String actionWord;

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public String getActionWord() {
		return actionWord;
	}

	public void setActionWord(String actionWord) {
		this.actionWord = actionWord;
	}

	public static class KeywordTestStepModelValidator implements Validator {

		private static final String MESSAGE_NOT_BLANK = "message.notBlank";
		
		private MessageSource messageSource;

		public KeywordTestStepModelValidator(MessageSource messageSource) {
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
			return aClass.equals(KeywordTestStepModel.class);
		}

		@Override
		public void validate(Object target, Errors errors) {
			Locale locale = LocaleContextHolder.getLocale();
			String notBlank = messageSource.getMessage(MESSAGE_NOT_BLANK, null, locale);

			KeywordTestStepModel model = (KeywordTestStepModel) target;
			String keyword = model.getKeyword();
			String actionWord = model.getActionWord();

			if (keyword.isEmpty()) {
				errors.rejectValue("Keyword in Keyword Test case", MESSAGE_NOT_BLANK, notBlank);
			}

			if (actionWord.isEmpty()) {
				errors.rejectValue("Action word in Keyword Test case", MESSAGE_NOT_BLANK, notBlank);
			}

			String noText = messageSource.getMessage("message.noText", null, locale);
			if (!validateTextExistence(actionWord)) {
				errors.rejectValue("Action word in Keyword Test case", "message.noText", noText);
			}

		}

		private boolean validateTextExistence(String actionWord) {
			if (actionWord.contains("\"")) {
				return hasTextOutsideParameters(actionWord);
			}
			return true;
		}

		private boolean hasTextOutsideParameters(String actionWord) {
			KeywordTestStepActionWordParser parser = new KeywordTestStepActionWordParser();
			parser.createActionWordFromKeywordTestStep(actionWord.trim());
			return parser.doesActionWordHaveText();
		}
	}
}
