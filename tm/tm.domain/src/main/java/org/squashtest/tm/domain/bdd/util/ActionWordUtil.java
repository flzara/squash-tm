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

/**
 * @author qtran - created on 28/04/2020
 */
public final class ActionWordUtil {
	private ActionWordUtil() {
		throw new UnsupportedOperationException("This is a utility class and cannot be instantiated.");
	}

	/**tThis method is to replace all extra-spaces by a single space, for ex:'this is    a    text' --> 'this is a text'
	 *
	 * @param text input text with extra spaces
	 * @return text removed extra spaces
	 */
	public static String formatText(@NotNull String text) {
		return text.replaceAll("[\\s]+", " ");
	}

}
