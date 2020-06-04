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

import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotNull;

/**
 * @author qtran - created on 28/04/2020
 */
public final class ActionWordUtil {
	private ActionWordUtil() {
		throw new UnsupportedOperationException("This is a utility class and cannot be instantiated.");
	}

	//this method is to replace all extra-spaces by a single space, for ex:' this is a    text    '-->' this is a text '
	public static String formatText(@NotNull String text) {
		return text.replaceAll("[\\s]+"," ");
	}

	/**
	 * This method is to add a double quote at the end of the input word if the current number of double quote is odd
	 *
	 * @param word the input action word word
	 * @return word with inserted double quotes at the end if missing
	 */
	public static String addMissingDoubleQuoteIfAny(String word) {
		int count = StringUtils.countMatches(word, "\"");
		if (count % 2 == 1) {
			word += "\"";
		}
		return word;
	}
}
