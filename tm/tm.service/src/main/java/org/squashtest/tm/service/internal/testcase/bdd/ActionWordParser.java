package org.squashtest.tm.service.internal.testcase.bdd;

import org.apache.commons.lang3.StringUtils;
import org.squashtest.tm.domain.bdd.ActionWord;
import org.squashtest.tm.domain.bdd.ActionWordFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * @author qtran - created on 24/04/2020
 */
public class ActionWordParser {

	private List<ActionWordFragment> fragmentList = new ArrayList<>();

	public ActionWord generateActionWordFromTextWithParamValue(String trimmedWord) {
		//If the input word contains any double quote, do the fragmentation
		if (trimmedWord.contains("\"")) {
			String updateWord = addMissingDoubleQuoteIfAny(trimmedWord);
			createFragments(updateWord);
			String token = generateToken(fragmentList);
			ActionWord result = new ActionWord(updateWord, token);
			result.setFragments(fragmentList);
			return result;
		} else {
			//otherwise  --> action word has no parameter and its token = T
			ActionWord result = new ActionWord(trimmedWord, ActionWord.ACTION_WORD_TEXT_TOKEN);
			result.addFragment(new ActionWordFragment());
			return result;
		}
	}

	public String generateToken(List<ActionWordFragment> fragmentList) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < fragmentList.size(); ++i) {
			builder.append("T");
		}
		return builder.toString();
	}

	private void createFragments(String word) {
		boolean inDoubleQuotes = false;
		int index = 0;
		String actionWordText = "";
		String actionWordParamValue = "";

		for (int i = 0; i < word.length(); ++i) {
			String currentChar = String.valueOf(word.charAt(i));
			if ("\"".equals(currentChar)) {
				//end of the current fragment
				if (inDoubleQuotes) {
					//this is the value of a param fragment
					fragmentList.add(new ActionWordFragment());
					actionWordParamValue = "";
				} else if (!actionWordText.isEmpty()){
					//this is a text fragment if the currentChar is not empty
					fragmentList.add(new ActionWordFragment());
					actionWordText = "";
				}
				//change the status in/out of the double quotes
				inDoubleQuotes = !inDoubleQuotes;
				//move to the new fragment index
				++index;
			} else {
				if (inDoubleQuotes) {
					//continue to charge the current actionWordParamValue
					actionWordParamValue += currentChar;
				} else {
					//continue to charge the current actionWordText
					actionWordText += currentChar;
				}
			}
		}
	}

	/**
	 * This method is to add a double quote at the end of the input word if the current number of double quote is odd
	 *
	 * @param word
	 * @return
	 */
	private String addMissingDoubleQuoteIfAny(String word) {
		int count = StringUtils.countMatches(word, "\"");
		if (count % 2 == 1) {
			word += "\"";
		}
		return word;
	}


}
