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
package org.squashtest.tm.web.internal.i18n;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.squashtest.tm.core.foundation.i18n.Abbreviated;
import org.squashtest.tm.core.foundation.i18n.Internationalizable;

/**
 * Helper class which decorates {@link MessageSource} to get internationalized messages and dates.
 *
 * Note : the decorator pattern should ease migration from {@link MessageSource} to {@link InternationalizationHelper}
 * thanks to full backward compatibility.
 *
 * @author Gregory Fouquet
 *
 */
@Component
@Primary
public class InternationalizationHelper implements MessageSource {
	private final MessageSource messageSource;

	@Inject
	public InternationalizationHelper(@NotNull MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	/**
	 * Returns the internationalized message matching the given key.
	 *
	 * @param i18nKey
	 * @param locale
	 * @return
	 */
	public String internationalize(String i18nKey, Locale locale) {
		return messageSource.getMessage(i18nKey, null, locale);
	}

	/**
	 * Returns the internationalized message for the given {@link Internationalizable}
	 *
	 * @param i18nKey
	 * @param locale
	 * @return
	 */
	public String internationalize(Internationalizable internationalizable, Locale locale) {
		return internationalize(internationalizable.getI18nKey(), locale);
	}

	public String internationalizeAbbreviation(Abbreviated abridged, Locale locale){
		return internationalize(abridged.getAbbreviatedI18nKey(), locale);
	}

	/**
	 * Formats a date using the built-in format for the given locale. If no date is given, returns a built-in "no data"
	 * label.
	 *
	 * @param date
	 * @param locale
	 * @return
	 */
	public String localizeDate(Date date, Locale locale) {
		String formatKey = "squashtm.dateformat";
		return localizeDate(date, locale, formatKey);
	}

	/**
	 * Formats a date using the built-in short date format for the given locale. If no date is given, returns a built-in
	 * "no data" label.
	 *
	 * @param date
	 * @param locale
	 * @return
	 */
	public String localizeShortDate(Date date, Locale locale) {
		String formatKey = "squashtm.dateformatShort";
		return localizeDate(date, locale, formatKey);
	}

	private String localizeDate(Date date, Locale locale, String formatKey) {
		if (date == null) {
			return noData(locale);
		}
		String format = messageSource.getMessage(formatKey, null, locale);
		// TODO formatter could be cached ?
		return new SimpleDateFormat(format).format(date);
	}

	/**
	 *
	 * @see org.springframework.context.MessageSource#getMessage(java.lang.String, java.lang.Object[], java.lang.String,
	 *      java.util.Locale)
	 */
	@Override
	public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
		return messageSource.getMessage(code, args, defaultMessage, locale);
	}

	/**
	 *
	 * @see org.springframework.context.MessageSource#getMessage(java.lang.String, java.lang.Object[], java.util.Locale)
	 * @deprecated if args array is empty or null, condider {@link #internationalize(String, Locale)}
	 */
	@Deprecated
	@Override
	public String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException {
		return messageSource.getMessage(code, args, locale);
	}

	@Override
	public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
		return messageSource.getMessage(resolvable, locale);
	}

	public void resolve(MessageObject object, Locale locale) {
		processAsMap(object, locale);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void processAsMap(Map<String, Object> object, Locale locale) {

		for (Entry<String, Object> entry : object.entrySet()) {
			Object value = entry.getValue();

			if (value instanceof String) {
				String translation = processAsString((String) value, locale);
				entry.setValue(translation);
			} else if (value instanceof Map) {
				processAsMap((Map) value, locale);
			} else {
				throw new IllegalArgumentException(
						"InternationalizationHelper : supplied MessageObject contained data that where neither "
								+ "String nor Map. Got : '" + value.getClass() + "'");
			}
		}
	}

	private String processAsString(String key, Locale locale) {
		return messageSource.getMessage(key, null, locale);
	}

	/**
	 * Translates true / false into an i18n'd yes / no
	 *
	 * @param yesOrNo
	 * @param locale
	 * @return
	 */
	public String internationalizeYesNo(boolean yesOrNo, Locale locale) {
		return internationalize("squashtm.yesno." + yesOrNo, locale);
	}

	/**
	 *
	 * @param locale
	 * @return the i18n'd "nodata" message
	 */
	public String noData(Locale locale) {
		return internationalize("squashtm.nodata", locale);
	}

	/**
	 *
	 * @param locale
	 * @return the i18n'd "deleted" message
	 */
	public String itemDeleted(Locale locale) {
		return internationalize("squashtm.itemdeleted", locale);
	}

	/**
	 * Returns the message or "no data" when message is null.
	 *
	 * @param message
	 *            the motentially null message
	 * @param locale
	 * @return either the given message or the "no data" message.
	 */
	public String messageOrNoData(String message, Locale locale) {
		return message != null ? message : noData(locale);
	}

	/**
	 * as {@Link localizeDate} but will return the word "never" localized if date is null
	 * @param date the date to format
	 * @param locale the user locale
	 * @return the formatted date or the word "never" localized
	 */
	public String localizeDateWithDefaultToNever(Date date, Locale locale) {
		String strDate;
		if(date != null){
			strDate = localizeDate(date, locale);
		} else {
			strDate = internationalize("label.lower.Never", locale);
		}
		return strDate;
	}
}
