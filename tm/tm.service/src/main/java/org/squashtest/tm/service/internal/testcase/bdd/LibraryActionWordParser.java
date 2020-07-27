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
package org.squashtest.tm.service.internal.testcase.bdd;

import org.squashtest.tm.domain.bdd.ActionWord;
import org.squashtest.tm.domain.bdd.ActionWordParameter;
import org.squashtest.tm.domain.bdd.util.ActionWordUtil;
import org.squashtest.tm.exception.actionword.InvalidActionWordInputException;
import org.squashtest.tm.exception.actionword.InvalidActionWordParameterNameException;

import java.util.ArrayList;
import java.util.List;

import static org.squashtest.tm.domain.bdd.ActionWord.ACTION_WORD_CLOSE_GUILLEMET;
import static org.squashtest.tm.domain.bdd.ActionWord.ACTION_WORD_DOUBLE_QUOTE;
import static org.squashtest.tm.domain.bdd.ActionWord.ACTION_WORD_OPEN_GUILLEMET;
import static org.squashtest.tm.domain.bdd.ActionWord.ACTION_WORD_UNDERSCORE;

/**
 * @author qtran - created on 27/07/2020
 */
public class LibraryActionWordParser extends ActionWordParser {
	private List<String> paramNames = new ArrayList<>();
	private StringBuilder actionWordFreeValueParamNameBuilder = new StringBuilder();

	@Override
	protected void addParamNameIntoFragmentsInLibrary(String paramNameInput) {
		String trimmedInput = paramNameInput.trim();
		if (ACTION_WORD_PARSER_EMPTY_CHAR.equals(trimmedInput)){
			throw new InvalidActionWordParameterNameException("Action word parameter name cannot be empty.");
		}
		ActionWordParameter param = new ActionWordParameter();
		String paramName=ACTION_WORD_PARSER_EMPTY_CHAR;
		++paramIndex;
		if (ActionWordUtil.isNumber(trimmedInput)){
			paramName += ACTION_WORD_PARAM_NAME_PREFIX + paramIndex;
			param.setDefaultValue(trimmedInput);
		} else {
			String replacedExtraSpacesInput = ActionWordUtil.replaceExtraSpacesInText(trimmedInput);
			String replacedSpaceByUnderscoreInput = replacedExtraSpacesInput.replaceAll("[\\s]", ACTION_WORD_UNDERSCORE);
			paramName += replacedSpaceByUnderscoreInput;
			param.setDefaultValue(ACTION_WORD_PARSER_EMPTY_CHAR);
		}
		param.setName(paramName);
		if (paramNames.contains(paramName)) {
			throw new InvalidActionWordParameterNameException("Action word parameter name must be unique.");
		}
		paramNames.add(paramName);
		fragmentList.add(param);
		actionWordFreeValueParamNameBuilder.setLength(0);
	}

	@Override
	protected void addParamValueIntoFragmentsInKeywordTestStep(String paramNameInput) {
		throw new UnsupportedOperationException("This method is only for KeywordTestStepActionWordParser.");
	}

	public ActionWord createActionWordInLibrary(String trimmedInput) {
		checkIfInputNullOrEmpty(trimmedInput);
		checkIfInputExceed255Char(trimmedInput);

		checkIfInputContainInvalidChar(trimmedInput, ACTION_WORD_OPEN_GUILLEMET);
		checkIfInputContainInvalidChar(trimmedInput, ACTION_WORD_CLOSE_GUILLEMET);

		//If the input word contains any double quote, do the fragmentation
		if (trimmedInput.contains(ACTION_WORD_DOUBLE_QUOTE)) {
			createFragmentsFromInputInLibrary(trimmedInput);
		}
		//otherwise  --> action word has only text with or without number
		else {
			addTextContainingNumberIntoFragments(trimmedInput, true);
		}

		checkIfActionWordHasText();
		return new ActionWord(fragmentList);
	}

	private void checkIfInputContainInvalidChar(String trimmedInput, String invalidChar) {
		if (trimmedInput.contains(invalidChar)) {
			String exMsg = "Action word cannot contain '"+invalidChar+"' symbol.";
			throw new InvalidActionWordInputException(exMsg);
		}
	}

	private void createFragmentsFromInputInLibrary(String trimmedInput) {
		CharState charState = CharState.TEXT;

		for (int i = 0; i < trimmedInput.length(); ++i) {
			String currentChar = String.valueOf(trimmedInput.charAt(i));
			charState = treatCurrentCharInLibrary(currentChar, charState);
		}

		if (charState == CharState.TEXT) {
			addTextContainingNumberIntoFragments(actionWordTextBuilder.toString(), true);
		} else {
			addParamNameIntoFragmentsInLibrary(actionWordFreeValueParamNameBuilder.toString());
		}
	}

	private CharState treatCurrentCharInLibrary(String currentChar, CharState charState) {
		switch (charState) {
			case TEXT:
				return treatInputInTextStateInLibrary(currentChar);
			case FREE_VALUE:
				return treatInputInFreeValueStateInLibrary(currentChar);
			default:
				throw new InvalidActionWordInputException("Invalid action word input");
		}
	}

	private CharState treatInputInTextStateInLibrary(String currentChar) {
		if (ACTION_WORD_DOUBLE_QUOTE.equals(currentChar)) {
			addTextContainingNumberIntoFragments(actionWordTextBuilder.toString(), true);
			return CharState.FREE_VALUE;
		}
		actionWordTextBuilder.append(currentChar);
		return CharState.TEXT;
	}

	private CharState treatInputInFreeValueStateInLibrary(String currentChar) {
		if (ACTION_WORD_DOUBLE_QUOTE.equals(currentChar)){
			addParamNameIntoFragmentsInLibrary(actionWordFreeValueParamNameBuilder.toString());
			return CharState.TEXT;
		} else if (ActionWordUtil.isAlphaNumericOrDashOrUnderscoreOrPointOrCommaOrSpaceChar(currentChar)){
			actionWordFreeValueParamNameBuilder.append(currentChar);
			return CharState.FREE_VALUE;
		} else {
			throw new InvalidActionWordParameterNameException("Action word parameter name can contain only alphanumeric, - or _ characters.");
		}
	}

}
