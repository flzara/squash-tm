package org.squashtest.tm.service.internal.testcase.bdd;

import org.squashtest.tm.domain.bdd.ActionWord;
import org.squashtest.tm.domain.bdd.ActionWordFragment;
import org.squashtest.tm.domain.bdd.ActionWordParameterValue;
import org.squashtest.tm.domain.bdd.ActionWordText;
import org.squashtest.tm.exception.actionword.InvalidActionWordInputException;

import java.util.ArrayList;
import java.util.List;

import static org.squashtest.tm.domain.bdd.ActionWord.ACTION_WORD_DOUBLE_QUOTE;
import static org.squashtest.tm.domain.bdd.util.ActionWordUtil.hasNumber;
import static org.squashtest.tm.domain.bdd.util.ActionWordUtil.isNumber;
import static org.squashtest.tm.domain.bdd.util.ActionWordUtil.replaceExtraSpacesInText;

/**
 * @author qtran - created on 27/07/2020
 */
public abstract class ActionWordParser {
	protected static final String ACTION_WORD_PARAM_NAME_PREFIX = "param";
	protected static final String ACTION_WORD_PARSER_SPACE_CHAR = " ";
	protected static final String ACTION_WORD_PARSER_EMPTY_CHAR = "";

	protected boolean actionWordHasText = false;

	protected List<ActionWordFragment> fragmentList = new ArrayList<>();

	protected List<ActionWordParameterValue> parameterValues = new ArrayList<>();

	protected StringBuilder actionWordTextBuilder = new StringBuilder();

	protected int paramIndex = 0;

	public boolean doesActionWordHaveText() {
		return actionWordHasText;
	}

	public List<ActionWordParameterValue> getParameterValues() {
		return parameterValues;
	}

	enum CharState {
		TEXT,
		FREE_VALUE,
		TC_PARAM_VALUE
	}

	protected abstract void addParamNameIntoFragmentsInLibrary(String paramNameInput);
	protected abstract void addParamValueIntoFragmentsInKeywordTestStep(String paramNameInput);

	protected void checkIfInputNullOrEmpty(String trimmedInput) {
		if (trimmedInput == null || trimmedInput.isEmpty()) {
			throw new InvalidActionWordInputException("Action word cannot be empty.");
		}
	}

	protected void checkIfInputExceed255Char(String trimmedInput) {
		if (trimmedInput.length() > ActionWord.ACTION_WORD_MAX_LENGTH) {
			throw new InvalidActionWordInputException("Action word cannot exceed 255 characters.");
		}
	}

	protected void checkIfActionWordHasText() {
		if (!actionWordHasText) {
			throw new InvalidActionWordInputException("Action word must contain at least some texts.");
		}
	}

	protected void addTextContainingNumberIntoFragments(String inputText, boolean isInLibrary) {
		if (!inputText.isEmpty()) {
			if (hasNumber(inputText)) {
				fragmentTextContainingNumbers(inputText, isInLibrary);
			} else {
				addTextIntoFragments(inputText);
			}
		}
	}

	protected void fragmentTextContainingNumbers(String inputText, boolean isInLibrary) {
		String replacedExtraSpacesText = replaceExtraSpacesInText(inputText);
		String firstAndLastSpaceControlledStr = addSymbolIfHasSpaceAtBeginningOrEndWord(replacedExtraSpacesText);
		String[] strArrays = firstAndLastSpaceControlledStr.split(ACTION_WORD_PARSER_SPACE_CHAR);
		StringBuilder builder = new StringBuilder();
		boolean firstWordInStr = true;
		for (String word : strArrays) {
			firstWordInStr = separateTextAndNumberInWordAndAddThemToFragments(builder, firstWordInStr, word, isInLibrary);
		}
		//add the last text
		addWordWithoutNumberIntoFragments(builder);
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

	private boolean separateTextAndNumberInWordAndAddThemToFragments(StringBuilder builder, boolean firstWordInStr, String word, boolean isInLibrary) {
		if (isNumber(word)) {
			treatNumberText(builder, word, isInLibrary);
		} else {
			treatNonNumberText(builder, firstWordInStr, word);
		}
		return false;
	}

	private void treatNumberText(StringBuilder builder, String word, boolean isInLibrary) {
		if (builder.length() != 0) {
			builder.append(ACTION_WORD_PARSER_SPACE_CHAR);
		}
		addWordWithoutNumberIntoFragments(builder);
		if (isInLibrary) {
			addParamNameIntoFragmentsInLibrary(word);
		} else {
			addParamValueIntoFragmentsInKeywordTestStep(word);
		}
	}

	private void addWordWithoutNumberIntoFragments(StringBuilder builder) {
		if (builder.length() != 0) {
			addTextIntoFragments(builder.toString());
			builder.setLength(0);
		}
	}

	private void addTextIntoFragments(String inputText) {
		ActionWordText text = new ActionWordText(inputText);
		fragmentList.add(text);
		raiseHasTextFlag();
		actionWordTextBuilder.setLength(0);
	}

	private void raiseHasTextFlag() {
		if (!actionWordHasText) {
			actionWordHasText = true;
		}
	}

	private void treatNonNumberText(StringBuilder builder, boolean firstWordInStr, String word) {
		if (firstWordInStr) {
			treatFirstWord(builder, word);
		} else {
			treatNonFirstWord(builder, word);
		}
	}

	private void treatFirstWord(StringBuilder builder, String word) {
		if (ACTION_WORD_DOUBLE_QUOTE.equals(word)) {
			builder.append(ACTION_WORD_PARSER_SPACE_CHAR);
		} else {
			builder.append(word);
		}
	}

	private void treatNonFirstWord(StringBuilder builder, String word) {
		builder.append(ACTION_WORD_PARSER_SPACE_CHAR);
		if (!ACTION_WORD_DOUBLE_QUOTE.equals(word)) {
			builder.append(word);
		}
	}
}
