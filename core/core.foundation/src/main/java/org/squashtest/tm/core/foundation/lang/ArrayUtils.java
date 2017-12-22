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
package org.squashtest.tm.core.foundation.lang;

import org.apache.commons.lang3.StringUtils;

/**
 * Utility methods for arrays not found in commons-lang
 *
 * @author Gregory Fouquet
 *
 */
public final class ArrayUtils {

	/**
	 *
	 */
	private ArrayUtils() {
		super();
	}

	/**
	 * Returns true if the array is not null and contains at least one non blank string.
	 *
	 * @param array array
	 * @return isNotBlankStringsArray
	 */
	public static boolean isNotBlankStringsArray(String[] array) {
		return !isBlankStringsArray(array);
	}
	public static boolean isBlankStringsArray(String[] array) {
		if (array == null) {
			return true;
		}
		for (String elem : array) {
			if (StringUtils.isNotBlank(elem)) {
				return false;
			}
		}

		return true;
	}
}
