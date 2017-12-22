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
package org.squashtest.tm.service.internal.dto;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.i18n.LocaleContextHolder;

import java.text.DecimalFormat;
import java.util.Locale;

/**
 * Created by jthebault on 25/07/2016.
 */
public class NumericCufHelper {

	/**
	 * Utility class with only static methods...
	 */
	private NumericCufHelper() {
	}

	/**
	 * Method to format a String reprsenting a number to the same string with good locale decimal separator.
	 * For example : Use it after a .toString() on a BigDecimal.
	 * @param unformatedValue
	 * @return
     */
	public static String formatOutputNumericCufValue(String unformatedValue) {
		if(StringUtils.isBlank(unformatedValue)){
			return "";
		}
		Locale locale = LocaleContextHolder.getLocale();
		DecimalFormat formatter = (DecimalFormat) DecimalFormat.getInstance(locale);
		char decimalSeparator = formatter.getDecimalFormatSymbols().getDecimalSeparator();
		return unformatedValue.replace('.',decimalSeparator);
	}

	/**
	 * Replace the decimal separator , by a .
	 * @param unformatedValue
	 * @return
     */
	public static String formatInputNumericCufValue(String unformatedValue) {
		return unformatedValue.replace(",",".");
	}
}
