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
package org.squashtest.tm.domain.bdd.util;

import javax.validation.constraints.NotNull;

import static org.squashtest.tm.domain.bdd.ActionWord.ACTION_WORD_DOUBLE_QUOTE;

/**
 * @author qtran - created on 28/04/2020
 */
public final class ActionWordUtil {
	private ActionWordUtil() {
		throw new UnsupportedOperationException("This is a utility class and cannot be instantiated.");
	}

	/**This method is to replace all extra-spaces by a single space, for ex:'this is    a    text' --> 'this is a text'
	 *
	 * @param text input text with extra spaces
	 * @return text removed extra spaces
	 */
	public static String replaceExtraSpacesInText(@NotNull String text) {
		return text.replaceAll("[\\s]+", " ");
	}

	/**
	 * This method is to check if the given input text contains some words as number (integer or float)
	 * @param inputText source string
	 * @return true if containing at least 1 number
	 */
    public static boolean hasNumber(@NotNull String inputText) {
    	String formattedInput = replaceExtraSpacesInText(inputText);
    	String[] strArrays = formattedInput.split("\\s");
		for (String word : strArrays) {
			if (isNumber(word)){
				return true;
			}
		}
		return false;
    }

	/**
	 * This method is to check if the given input is a number (integer or float)
	 * @param inputWord given word
	 * @return true if is number
	 */
	public static boolean isNumber(String inputWord) {
		return inputWord.matches("-?\\d+(([.,])\\d+)?");
	}

	/**
	 * This method is to wrap two double quotes ("...") over a string
	 * @param stringInput given input string
	 * @return input string wrapped with two double quotes
	 */
	public static String updateNumberValue(String stringInput) {
		if (isNumber(stringInput)){
			return stringInput;
		}
		return ACTION_WORD_DOUBLE_QUOTE + stringInput + ACTION_WORD_DOUBLE_QUOTE;
	}
}
