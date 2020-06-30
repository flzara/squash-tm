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
import org.squashtest.tm.domain.bdd.ActionWordFragment;
import org.squashtest.tm.domain.bdd.ActionWordParameter;
import org.squashtest.tm.domain.bdd.ActionWordParameterValue;
import org.squashtest.tm.domain.bdd.ActionWordText;
import org.squashtest.tm.exception.actionword.InvalidActionWordInputException;
import org.squashtest.tm.exception.actionword.InvalidActionWordParameterValueException;
import org.squashtest.tm.exception.actionword.InvalidActionWordTextException;
import org.squashtest.tm.exception.testcase.InvalidParameterNameException;

import java.util.ArrayList;
import java.util.List;

import static org.squashtest.tm.domain.bdd.ActionWord.ACTION_WORD_CLOSE_GUILLEMET;
import static org.squashtest.tm.domain.bdd.ActionWord.ACTION_WORD_DOUBLE_QUOTE;
import static org.squashtest.tm.domain.bdd.ActionWord.ACTION_WORD_OPEN_GUILLEMET;
import static org.squashtest.tm.domain.bdd.ActionWord.ACTION_WORD_UNDERSCORE;
import static org.squashtest.tm.domain.bdd.ActionWordParameter.ACTION_WORD_PARAM_DEFAULT_VALUE;
import static org.squashtest.tm.domain.bdd.util.ActionWordUtil.hasNumber;
import static org.squashtest.tm.domain.bdd.util.ActionWordUtil.isNumber;
import static org.squashtest.tm.domain.bdd.util.ActionWordUtil.replaceExtraSpacesInText;

/**
 * @author qtran - created on 24/04/2020
 */
public class ActionWordParser {

	private static final String ACTION_WORD_PARAM_NAME_PREFIX = "param";
	private static final String ACTION_WORD_PARSER_SPACE_CHAR = " ";

	private boolean actionWordHasText = false;

	private List<ActionWordFragment> fragmentList = new ArrayList<>();

	private List<ActionWordParameterValue> parameterValues = new ArrayList<>();

	private StringBuilder actionWordTextBuilder = new StringBuilder();

	private StringBuilder actionWordFreeValueParamValueBuilder = new StringBuilder();

	private StringBuilder actionWordTestCaseParamValueBuilder = new StringBuilder();

	private int paramIndex = 0;

	public boolean doesActionWordHaveText() {
		return actionWordHasText;
	}

	public List<ActionWordParameterValue> getParameterValues() {
		return parameterValues;
	}

	public ActionWord createActionWordFromKeywordTestStep(String trimmedInput) {
		if (trimmedInput.length() > ActionWord.ACTION_WORD_MAX_LENGTH) {
			throw new InvalidActionWordInputException("Action word cannot exceed 255 characters.");
		}
		if (trimmedInput.isEmpty()) {
			throw new InvalidActionWordInputException("Action word cannot be empty.");
		}

		//If the input word contains any double quote or <, do the fragmentation
		if (trimmedInput.contains(ACTION_WORD_DOUBLE_QUOTE) || trimmedInput.contains(ACTION_WORD_OPEN_GUILLEMET)) {
			createFragmentsFromInput(trimmedInput);
			if (!actionWordHasText) {
				throw new InvalidActionWordInputException("Action word must contain at least some texts.");
			}
		}
		//otherwise  --> action word has no "..." or <...>
		else {
			addTextContainingNumberIntoFragments(trimmedInput);
		}
		return new ActionWord(fragmentList);
	}

	private void createFragmentsFromInput(String trimmedInput) {
		CharState charState = CharState.TEXT;

		for (int i = 0; i < trimmedInput.length(); ++i) {
			String currentChar = String.valueOf(trimmedInput.charAt(i));
			charState = treatCurrentChar(currentChar, charState);
		}

		if (charState == CharState.TEXT) {
			addTextContainingNumberIntoFragments(actionWordTextBuilder.toString());
		} else if (charState == CharState.FREE_VALUE) {
			addFreeValueParamValueIntoFragments(actionWordFreeValueParamValueBuilder.toString());
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

	private CharState treatInputInTestCaseParamValueState(String currentChar) {
		switch (currentChar) {
			case ACTION_WORD_CLOSE_GUILLEMET:
				addTestCaseParamValueIntoFragments(actionWordTestCaseParamValueBuilder.toString());
				return CharState.TEXT;
			case ACTION_WORD_OPEN_GUILLEMET:
				addTestCaseParamValueIntoFragments(actionWordTestCaseParamValueBuilder.toString());
				return CharState.TC_PARAM_VALUE;
			case ACTION_WORD_DOUBLE_QUOTE:
				addTestCaseParamValueIntoFragments(actionWordTestCaseParamValueBuilder.toString());
				return CharState.FREE_VALUE;
			default:
				actionWordTestCaseParamValueBuilder.append(currentChar);
				return CharState.TC_PARAM_VALUE;
		}
	}

	private void addTestCaseParamValueIntoFragments(String tcParamValueInput) {
		String trimmedWord = tcParamValueInput.trim();
		if (trimmedWord.isEmpty()) {
			throw new InvalidParameterNameException("Test case parameter name cannot be empty.");
		}
		++paramIndex;
		String actionWordParamValue = createParamValueFromTestCaseParamValueInput(trimmedWord);
		ActionWordParameter param = initiateActionWordParameter(paramIndex, actionWordParamValue);
		fragmentList.add(param);
		actionWordTestCaseParamValueBuilder.setLength(0);
	}

	private void raiseHasTextFlag() {
		if (!actionWordHasText) {
			actionWordHasText = true;
		}
	}

	private ActionWordParameter initiateActionWordParameter(int paramIndex, String actionWordParamValue) {
		ActionWordParameterValue paramValue = new ActionWordParameterValue(actionWordParamValue);
		String paramName = ACTION_WORD_PARAM_NAME_PREFIX + paramIndex;
		ActionWordParameter param = new ActionWordParameter(paramName, ACTION_WORD_PARAM_DEFAULT_VALUE);
		parameterValues.add(paramValue);
		return param;
	}

	private String createParamValueFromTestCaseParamValueInput(String trimmedWord) {
		String removedExtraSpaces = replaceExtraSpacesInText(trimmedWord);
		String replacedInvalidCharsWithUnderscores = removedExtraSpaces.replaceAll("[^\\w-]", ACTION_WORD_UNDERSCORE);
		return ACTION_WORD_OPEN_GUILLEMET + replacedInvalidCharsWithUnderscores + ACTION_WORD_CLOSE_GUILLEMET;
	}

	private CharState treatInputInFreeValueState(String currentChar) {
		switch (currentChar) {
			case ACTION_WORD_CLOSE_GUILLEMET:
				throw new InvalidActionWordParameterValueException("Action word parameter value cannot contain '>' symbol.");
			case ACTION_WORD_OPEN_GUILLEMET:
				addFreeValueParamValueIntoFragments(actionWordFreeValueParamValueBuilder.toString());
				return CharState.TC_PARAM_VALUE;
			case ACTION_WORD_DOUBLE_QUOTE:
				addFreeValueParamValueIntoFragments(actionWordFreeValueParamValueBuilder.toString());
				return CharState.TEXT;
			default:
				actionWordFreeValueParamValueBuilder.append(currentChar);
				return CharState.FREE_VALUE;
		}
	}

	private void addFreeValueParamValueIntoFragments(String paramValueInput) {
		++paramIndex;
		ActionWordParameter param = initiateActionWordParameter(paramIndex, paramValueInput);
		fragmentList.add(param);
		actionWordFreeValueParamValueBuilder.setLength(0);
	}

	private CharState treatInputInTextState(String currentChar) {
		switch (currentChar) {
			case ACTION_WORD_CLOSE_GUILLEMET:
				throw new InvalidActionWordTextException("Action word text cannot contain '>' symbol.");
			case ACTION_WORD_OPEN_GUILLEMET:
				addTextContainingNumberIntoFragments(actionWordTextBuilder.toString());
				return CharState.TC_PARAM_VALUE;
			case ACTION_WORD_DOUBLE_QUOTE:
				addTextContainingNumberIntoFragments(actionWordTextBuilder.toString());
				return CharState.FREE_VALUE;
			default:
				actionWordTextBuilder.append(currentChar);
				return CharState.TEXT;
		}
	}

	private void addTextContainingNumberIntoFragments(String inputText) {
		if (!inputText.isEmpty()) {
			if (hasNumber(inputText)) {
				fragmentTextContainingNumbers(inputText);
			} else {
				addTextIntoFragments(inputText);
			}
		}
	}

	private void addTextIntoFragments(String inputText) {
		ActionWordText text = new ActionWordText(inputText);
		fragmentList.add(text);
		raiseHasTextFlag();
		actionWordTextBuilder.setLength(0);
	}

	private void fragmentTextContainingNumbers(String inputText) {
		String formattedInput = replaceExtraSpacesInText(inputText);
		String firstAndLastSpaceControlledStr = addSymbolIfHasSpaceAtBeginningOrEndWord(formattedInput);
		String[] strArrays = firstAndLastSpaceControlledStr.split(ACTION_WORD_PARSER_SPACE_CHAR);
		StringBuilder builder = new StringBuilder();
		boolean firstWordInStr = true;
		for (String word : strArrays) {
			firstWordInStr = separateTextAndNumberInWordAndAddThemToFragments(builder, firstWordInStr, word);
		}
		//add the last text
		addWordWithoutNumberIntoFragments(builder);
	}

	private boolean separateTextAndNumberInWordAndAddThemToFragments(StringBuilder builder, boolean firstWordInStr, String word) {
		if (isNumber(word)) {
			treatNumberText(builder, word);
		} else {
			treatNonNumberText(builder, firstWordInStr, word);
		}
		return false;
	}

	private void treatNonNumberText(StringBuilder builder, boolean firstWordInStr, String word) {
		if (firstWordInStr) {
			treatFirstWord(builder, word);
		} else {
			treatNonFirstWord(builder, word);
		}
	}

	private void treatNonFirstWord(StringBuilder builder, String word) {
		builder.append(ACTION_WORD_PARSER_SPACE_CHAR);
		if (!ACTION_WORD_DOUBLE_QUOTE.equals(word)) {
			builder.append(word);
		}
	}

	private void treatFirstWord(StringBuilder builder, String word) {
		if (ACTION_WORD_DOUBLE_QUOTE.equals(word)) {
			builder.append(ACTION_WORD_PARSER_SPACE_CHAR);
		} else {
			builder.append(word);
		}
	}

	private void treatNumberText(StringBuilder builder, String word) {
		if (builder.length() != 0) {
			builder.append(ACTION_WORD_PARSER_SPACE_CHAR);
		}
		addWordWithoutNumberIntoFragments(builder);
		addFreeValueParamValueIntoFragments(word);
	}

	private void addWordWithoutNumberIntoFragments(StringBuilder builder) {
		if (builder.length() != 0) {
			addTextIntoFragments(builder.toString());
			builder.setLength(0);
		}
	}

	private String addSymbolIfHasSpaceAtBeginningOrEndWord(String inputText) {
		StringBuilder builder = new StringBuilder(inputText);
		if (inputText.startsWith(ACTION_WORD_PARSER_SPACE_CHAR)) {
			builder.insert(0, ACTION_WORD_DOUBLE_QUOTE);
		}
		if (inputText.endsWith(ACTION_WORD_PARSER_SPACE_CHAR)) {
			builder.append(ACTION_WORD_DOUBLE_QUOTE);
		}
		return builder.toString();
	}

	enum CharState {
		TEXT,
		FREE_VALUE,
		TC_PARAM_VALUE
	}

}


