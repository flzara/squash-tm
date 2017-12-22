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
package org.squashtest.tm.web.internal.report.criteria;

import java.text.ParseException;
import java.util.Arrays;

/**
 * Thrown when date received from a DateInput does not match expected (parseable) format.
 * 
 * @author Gregory Fouquet
 * 
 */
public class InconsistentDateFormatException extends RuntimeException {

	/**
	 * @param rawDate
	 * @param e
	 */
	public InconsistentDateFormatException(String rawDate, ParseException e) {
		super(message(rawDate), e);
	}

	/**
	 * @param rawDate
	 * @param expectedFormats
	 * @return
	 */
	private static String message(String rawDate) {
		return "Format of date '" + rawDate + "' does not match expected formats : " + Arrays.toString(FormEntryConstants.DATE_ENTRY_FORMATS);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 7789123433803143273L;

}
