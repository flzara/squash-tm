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
package org.squashtest.tm.service.internal.helper;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

/**
 * Exposes hyphened string manipulation methods for use as el functions. An hyphened string is a string which words are
 * separated by a '-'.
 *
 * @author Gregory Fouquet
 *
 */
public final class HyphenedStringHelper {
	private static final char[] HYPHEN_ARRAY = { '-' };

	private HyphenedStringHelper() {
		super();
	}

	/**
	 * replaces hyphens in a string by underscores
	 *
	 * @param hyphened
	 * @return
	 */
	public static String hyphenedToUnderscored(String hyphened) {
		return hyphened.replace('-', '_');
	}

	/**
	 * removes hyphens and camel cases the string
	 *
	 * @param hyphened
	 * @return
	 */
	public static String hyphenedToCamelCase(String hyphened) {
		return StringUtils.remove(WordUtils.capitalize(hyphened, HYPHEN_ARRAY), '-');
	}

	/**
	 * Convert a CamelCaseString to a hyphened-string
	 * @param camelCase
	 * @return
	 */
	public static String camelCaseToHyphened(String camelCase) {
		String[] tokens = StringUtils.splitByCharacterTypeCamelCase(camelCase);
		return WordUtils.uncapitalize(StringUtils.join(tokens, '-'), HYPHEN_ARRAY);
	}
}
