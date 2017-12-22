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
import org.squashtest.tm.core.foundation.exception.NullArgumentException;

/**
 * Assertion utilities to validate object state.
 *
 * @author Gregory Fouquet
 *
 */
public final class Assert {

	/**
	 *
	 */
	private Assert() {
		super();
	}

	/**
	 * Checks that a given object property is not null.
	 *
	 * @param property property
	 * @param message
	 *            The exceptin message if assertion fails
	 * @throws IllegalStateException
	 *             if property is null
	 */
	public static void propertyNotNull(Object property, String message) throws IllegalStateException {
		if (property == null) {
			throw new IllegalStateException(message);
		}
	}

	/**
	 * Checks that a given object property is not null.
	 *
	 * @param property property
	 * @throws IllegalStateException
	 *             if property is null.
	 */
	public static void propertyNotNull(Object property) throws IllegalStateException {
		propertyNotNull(property, "Assertion error : property should not be null");
	}

	/**
	 * Checks that a String property is not blank
	 *
	 * @param property property
	 * @param message message
	 */
	public static void propertyNotBlank(String property, String message) {
		if (StringUtils.isBlank(property)) {
			throw new IllegalStateException(message);
		}

	}

	/**
	 * @param parameter parameter
	 * @param parameterName parameterName
	 */
	public static void parameterNotNull(Object parameter, String parameterName) {
		if (parameter == null) {
			throw new NullArgumentException(parameterName);
		}
	}
}
