/**
 * This file is part of the Squashtest platform.
 * Copyright (C) Henix, henix.fr
 * <p>
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 * <p>
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * this software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.service.internal.testcase.bdd;

import org.apache.commons.lang3.StringUtils;
import org.squashtest.tm.domain.bdd.ActionWord;
import org.squashtest.tm.domain.bdd.ActionWordFragment;
import org.squashtest.tm.domain.bdd.ActionWordParameter;
import org.squashtest.tm.domain.bdd.ActionWordParameterValue;
import org.squashtest.tm.domain.bdd.ActionWordText;

import java.util.ArrayList;
import java.util.List;

/**
 * @author qtran - created on 24/04/2020
 */
public class ActionWordParser {

	private boolean actionWordHavingText = false;

	private List<ActionWordFragment> fragmentList = new ArrayList<>();

	private List<ActionWordParameterValue> parameterValues = new ArrayList<>();

	public ActionWord generateActionWordFromTextWithParamValue(String trimmedWord) {
		//If the input word contains any double quote, do the fragmentation
		if (trimmedWord.contains("\"")) {
			//add the missing double quote if any
			String updateWord = addMissingDoubleQuoteIfAny(trimmedWord);
			//creating the fragment list
			createFragmentsWithParamValue(updateWord);
			//check if the fragment list contains at least one text element
			if (!actionWordHavingText) {
				throw new IllegalArgumentException("Action word must contain at least some texts.");
			}
			//generate token
			String token = generateToken(fragmentList);
			//initiate the action word
			ActionWord result = new ActionWord(updateWord, token);

			result.setFragments(fragmentList);
			return result;
		} else {
			actionWordHavingText = true;
			//otherwise  --> action word has no parameter
			ActionWord result = new ActionWord(trimmedWord);
			ActionWordText text = new ActionWordText(trimmedWord);
			result.addFragment(text);
			return result;
		}
	}

	public String generateToken(List<ActionWordFragment> fragmentList) {
		StringBuilder builder1 = new StringBuilder();
		StringBuilder builder2 = new StringBuilder("-");
		for (ActionWordFragment fragment : fragmentList) {
			if (ActionWordParameter.class.isAssignableFrom(fragment.getClass())) {
				builder1.append(ActionWord.ACTION_WORD_PARAM_TOKEN);
			} else {
				builder1.append(ActionWord.ACTION_WORD_TEXT_TOKEN);
				ActionWordText text = (ActionWordText) fragment;
				builder2.append(text.getText()).append("-");
			}
		}
		return builder1.append(builder2).toString();
	}

	private void createFragmentsWithParamValue(String word) {
		boolean inDoubleQuotes = false;
		int paramIndex = 0;
		StringBuilder actionWordText = new StringBuilder();
		StringBuilder actionWordParamValue = new StringBuilder();

		for (int i = 0; i < word.length(); ++i) {
			String currentChar = String.valueOf(word.charAt(i));
			if ("\"".equals(currentChar)) {
				//end of the current fragment
				if (inDoubleQuotes) {
					//this is the value of a param fragment
					++paramIndex;
					ActionWordParameter param = initiateActionWordParameter(paramIndex, actionWordParamValue.toString());
					fragmentList.add(param);
					actionWordParamValue.setLength(0);
				} else if (actionWordText.length() > 0) {
					//this is a text fragment if the currentChar is not empty
					fragmentList.add(new ActionWordText(actionWordText.toString()));
					updateHasTextBoolean();
					actionWordText.setLength(0);
				}
				//change the status in/out of the double quotes
				inDoubleQuotes = !inDoubleQuotes;
			} else {
				if (inDoubleQuotes) {
					//continue to charge the current actionWordParamValue
					actionWordParamValue.append(currentChar);
				} else {
					//continue to charge the current actionWordText
					actionWordText.append(currentChar);
				}
			}
		}

		//add the last text
		if (actionWordText.length() > 0) {
			//this is a text fragment if the currentChar is not empty
			fragmentList.add(new ActionWordText(actionWordText.toString()));
			updateHasTextBoolean();
		}
	}

	private void updateHasTextBoolean() {
		if (!actionWordHavingText) {
			actionWordHavingText = true;
		}
	}

	private ActionWordParameter initiateActionWordParameter(int paramIndex, String actionWordParamValue) {
		ActionWordParameterValue paramValue = new ActionWordParameterValue(actionWordParamValue);
		String paramName = "p" + paramIndex;
		ActionWordParameter param = new ActionWordParameter(paramName, "");
		parameterValues.add(paramValue);
		return param;
	}

	/**
	 * This method is to add a double quote at the end of the input word if the current number of double quote is odd
	 *
	 * @param word the input action word word
	 * @return word with inserted double quotes at the end if missing
	 */
	private String addMissingDoubleQuoteIfAny(String word) {
		int count = StringUtils.countMatches(word, "\"");
		if (count % 2 == 1) {
			word += "\"";
		}
		return word;
	}

	public List<ActionWordParameterValue> getParameterValues() {
		return parameterValues;
	}

	public void setParameterValues(List<ActionWordParameterValue> parameterValues) {
		this.parameterValues = parameterValues;
	}
}


