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
import org.squashtest.tm.domain.bdd.ActionWordParameterValue;
import org.squashtest.tm.exception.actionword.InvalidActionWordInputException;
import org.squashtest.tm.exception.actionword.InvalidActionWordParameterValueException;
import org.squashtest.tm.exception.actionword.InvalidActionWordTextException;
import org.squashtest.tm.exception.testcase.InvalidParameterNameException;

import static org.squashtest.tm.domain.bdd.ActionWord.ACTION_WORD_CLOSE_GUILLEMET;
import static org.squashtest.tm.domain.bdd.ActionWord.ACTION_WORD_DOUBLE_QUOTE;
import static org.squashtest.tm.domain.bdd.ActionWord.ACTION_WORD_OPEN_GUILLEMET;
import static org.squashtest.tm.domain.bdd.ActionWord.ACTION_WORD_UNDERSCORE;
import static org.squashtest.tm.domain.bdd.util.ActionWordUtil.replaceExtraSpacesInText;

/**
 * @author qtran - created on 24/04/2020
 */
public class KeywordTestStepActionWordParser extends ActionWordParser{
	private StringBuilder actionWordFreeValueParamValueBuilder = new StringBuilder();
	private StringBuilder actionWordTestCaseParamValueBuilder = new StringBuilder();

	@Override
	protected void addParamNameIntoFragmentsInLibrary(String paramNameInput) {
		throw new UnsupportedOperationException("This method is only for LibraryActionWordParser.");
	}

	@Override
	protected void addParamValueIntoFragmentsInKeywordTestStep(String paramValueInput) {
		++paramIndex;
		ActionWordParameterValue paramValue = new ActionWordParameterValue(paramValueInput);
		String paramName = ACTION_WORD_PARAM_NAME_PREFIX + paramIndex;
		ActionWordParameter param = new ActionWordParameter(paramName, paramValueInput);
		parameterValues.add(paramValue);
		fragmentList.add(param);
		actionWordFreeValueParamValueBuilder.setLength(0);
	}

	public ActionWord createActionWordFromKeywordTestStep(String trimmedInput) {
		checkIfInputNullOrEmpty(trimmedInput);
		checkIfInputExceed255Char(trimmedInput);

		//If the input word contains any double quote or <, do the fragmentation
		if (trimmedInput.contains(ACTION_WORD_DOUBLE_QUOTE) || trimmedInput.contains(ACTION_WORD_OPEN_GUILLEMET)) {
			createFragmentsFromInput(trimmedInput);
		}
		//otherwise  --> action word has only text with or without number
		else {
			addTextContainingNumberIntoFragments(trimmedInput, false);
		}

		checkIfActionWordHasText();
		return new ActionWord(fragmentList);
	}

	private void createFragmentsFromInput(String trimmedInput) {
		CharState charState = CharState.TEXT;

		for (int i = 0; i < trimmedInput.length(); ++i) {
			String currentChar = String.valueOf(trimmedInput.charAt(i));
			charState = treatCurrentChar(currentChar, charState);
		}

		if (charState == CharState.TEXT) {
			addTextContainingNumberIntoFragments(actionWordTextBuilder.toString(), false);
		} else if (charState == CharState.FREE_VALUE) {
			addParamValueIntoFragmentsInKeywordTestStep(actionWordFreeValueParamValueBuilder.toString());
		} else {
			addTestCaseParamValueIntoFragments(actionWordTestCaseParamValueBuilder.toString());
		}
	}

	private CharState treatCurrentChar(String currentChar, CharState charState) {
		switch (charState) {
			case TEXT:
				return treatInputInTextState(currentChar);
			case FREE_VALUE:
				return treatInputInFreeValueState(currentChar);
			case TC_PARAM_VALUE:
				return treatInputInTestCaseParamValueState(currentChar);
			default:
				throw new InvalidActionWordInputException("Invalid action word input");
		}
	}

	private CharState treatInputInTextState(String currentChar) {
		switch (currentChar) {
			case ACTION_WORD_CLOSE_GUILLEMET:
				throw new InvalidActionWordTextException("Action word text cannot contain '>' symbol.");
			case ACTION_WORD_OPEN_GUILLEMET:
				actionWordTestCaseParamValueBuilder.append(ACTION_WORD_OPEN_GUILLEMET);
				addTextContainingNumberIntoFragments(actionWordTextBuilder.toString(), false);
				return CharState.TC_PARAM_VALUE;
			case ACTION_WORD_DOUBLE_QUOTE:
				addTextContainingNumberIntoFragments(actionWordTextBuilder.toString(), false);
				return CharState.FREE_VALUE;
			default:
				actionWordTextBuilder.append(currentChar);
				return CharState.TEXT;
		}
	}

	private CharState treatInputInFreeValueState(String currentChar) {
		switch (currentChar) {
			case ACTION_WORD_CLOSE_GUILLEMET:
				throw new InvalidActionWordParameterValueException("Action word parameter value cannot contain '>' symbol.");
			case ACTION_WORD_OPEN_GUILLEMET:
				actionWordTestCaseParamValueBuilder.append(ACTION_WORD_OPEN_GUILLEMET);
				addParamValueIntoFragmentsInKeywordTestStep(actionWordFreeValueParamValueBuilder.toString());
				return CharState.TC_PARAM_VALUE;
			case ACTION_WORD_DOUBLE_QUOTE:
				addParamValueIntoFragmentsInKeywordTestStep(actionWordFreeValueParamValueBuilder.toString());
				return CharState.TEXT;
			default:
				actionWordFreeValueParamValueBuilder.append(currentChar);
				return CharState.FREE_VALUE;
		}
	}

	private CharState treatInputInTestCaseParamValueState(String currentChar) {
		switch (currentChar) {
			case ACTION_WORD_CLOSE_GUILLEMET:
				actionWordTestCaseParamValueBuilder.append(ACTION_WORD_CLOSE_GUILLEMET);
				addTestCaseParamValueIntoFragments(actionWordTestCaseParamValueBuilder.toString());
				return CharState.TEXT;
			case ACTION_WORD_OPEN_GUILLEMET:
			case ACTION_WORD_DOUBLE_QUOTE:
				throw new InvalidParameterNameException("Test case parameter must be between < and >.");
			default:
				actionWordTestCaseParamValueBuilder.append(currentChar);
				return CharState.TC_PARAM_VALUE;
		}
	}

	private void addTestCaseParamValueIntoFragments(String tcParamValueInput) {
		if (!tcParamValueInput.startsWith(ACTION_WORD_OPEN_GUILLEMET) || !tcParamValueInput.endsWith(ACTION_WORD_CLOSE_GUILLEMET)){
			throw new InvalidParameterNameException("Test case parameter must be between < and >.");
		}
		String removedGuillemetStr = tcParamValueInput.substring(1, tcParamValueInput.length()-1);
		String trimmedWord = removedGuillemetStr.trim();
		if (trimmedWord.isEmpty()) {
			throw new InvalidParameterNameException("Test case parameter name cannot be empty.");
		}
		if (!trimmedWord.matches("[\\w-\\s]+")) {
			throw new InvalidParameterNameException("Test case parameter name can contain only alphanumeric, - and _ characters.");
		}
		++paramIndex;
		String actionWordParamValue = createParamValueFromTestCaseParamValueInput(trimmedWord);
		ActionWordParameterValue paramValue = new ActionWordParameterValue(actionWordParamValue);
		String paramName = ACTION_WORD_PARAM_NAME_PREFIX + paramIndex;
		ActionWordParameter param = new ActionWordParameter(paramName, ACTION_WORD_PARSER_EMPTY_CHAR);
		parameterValues.add(paramValue);
		fragmentList.add(param);
		actionWordTestCaseParamValueBuilder.setLength(0);
	}

	private String createParamValueFromTestCaseParamValueInput(String trimmedWord) {
		String removedExtraSpaces = replaceExtraSpacesInText(trimmedWord);
		String replacedInvalidCharsWithUnderscores = removedExtraSpaces.replaceAll("[\\s]", ACTION_WORD_UNDERSCORE);
		return ACTION_WORD_OPEN_GUILLEMET + replacedInvalidCharsWithUnderscores + ACTION_WORD_CLOSE_GUILLEMET;
	}
}


