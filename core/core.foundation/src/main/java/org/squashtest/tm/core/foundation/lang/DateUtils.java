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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DateUtils {

	private static final String ISO_DATE = "yyyy-MM-dd";
	private static final String ISO_DATETIME = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
	public static final String DD_MM_YYYY_DATE = "dd/MM/yyyy";

	private static final Pattern ISO_DATE_PATTERN = Pattern.compile("^([\\d]{4})-([\\d]{2})-([\\d]{2})$");

	private DateUtils() {
		super();
	}

	/**
	 * Formats a date into a an ISO 8601 string. <strong>The date will be formatted using the jvm default
	 * timezone</strong>
	 *
	 * @param date date
	 * @return returns that date formatted according to the ISO 8601 Date (no time info)
	 */
	public static String formatIso8601Date(Date date) {
		if (date == null) {
			return null;
		} else {
			return formatDate(date, ISO_DATE);
		}
	}

	private static String formatDate(Date date, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(date);
	}

	/**
	 * Formats a timestamp into a an ISO 8601 string. <strong>The date will be formatted using the jvm default
	 * timezone</strong>
	 *
	 * @param date date
	 * @return returns that date formatted according to the ISO 8601 DateTime (with time and timezone info)
	 */
	public static String formatIso8601DateTime(Date date) {
		if (date == null) {
			return null;
		} else {
			return formatDate(date, ISO_DATETIME);
		}
	}

	/**
	 * Checks that the string parses as a four-digit year dash two-digit month dash two-digits day,
	 * that the month is between 1 and 12 and the day between 0 and 31.
	 * It won't check leap years etc. Potentially faster than #strongCheckIso8601Date but
	 * is less secure.
	 *
	 * @param date date
	 * @return boolean
	 */
	public static boolean weakCheckIso8601Date(String date) {
		boolean success;

		if (date == null) {
			success = false;
		} else {
			Matcher matcher = ISO_DATE_PATTERN.matcher(date);
			if (matcher.matches()) {
				int month = Integer.parseInt(matcher.group(2));
				int day = Integer.parseInt(matcher.group(3));
				success = month > 0 && month < 13 && day > 0 && day < 32;
			} else {
				success = false;
			}
		}

		return success;
	}

	/**
	 * full check of whether the date is a valid iso 8601 date or not. Potentially slower than
	 * #weakCheckIso8601Date but safer.
	 * @param date date
	 * @return boolean
	 *
	 */
	public static boolean strongCheckIso8601Date(String date) {
		if (date == null) {
			return false;
		} else {
			try {
				parseIso8601Date(date);
				return true;
			} catch (ParseException e) {
				return false;
			}
		}
	}

	/**
	 * @param strDate strDate
	 * @return the Date obtained when parsing the argument against pattern yyyy-MM-dd
	 */
	public static Date parseIso8601Date(String strDate) throws ParseException {
		if (strDate == null) {
			return null;
		} else {
			return parseDate(strDate, ISO_DATE);
		}
	}

	/**
	 * @param strDatetime strDatetime
	 * @return the Date obtained when parsing the argument against pattern yyyy-MM-dd'T'HH:mm:ssZ
	 */
	public static Date parseIso8601DateTime(String strDatetime) throws ParseException {
		if (strDatetime == null) {
			return null;
		} else {
			return parseDate(strDatetime, ISO_DATETIME);
		}
	}

	private static Date parseDate(String strDatetime, String format) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		sdf.setLenient(false);
		return sdf.parse(strDatetime);
	}

	/**
	 *
	 * @param milliseconds milliseconds
	 * @return <code>null</code> if the string is empty, or a date otherwise. No check regarding the actual content of
	 *         strDate.
	 * @deprecated when you feel the urge to marshall a date into ms, consider using atom / iso instead
	 */
	@Deprecated
	public static Date millisecondsToDate(String milliseconds) {
		Date newDate = null;

		if (!milliseconds.isEmpty()) {
			Long millisecs = Long.valueOf(milliseconds);
			newDate = new Date(millisecs);
		}

		return newDate;
	}

	/**
	 *
	 * @param date date
	 * @return String
	 * @deprecated when you feel the urge to marshall a date into ms, consider using atom / iso instead
	 */
	@Deprecated
	public static String dateToMillisecondsAsString(Date date) {
		if (date != null) {
			return Long.valueOf(date.getTime()).toString();
		} else {
			return "";
		}
	}

	public static Date parseDdMmYyyyDate(String date) throws ParseException {
		return parseDate(date, DD_MM_YYYY_DATE);
	}

	public static Date nextDay(Date day) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(day);
		cal.add(Calendar.DAY_OF_YEAR, 1);
		return cal.getTime();
	}
}
