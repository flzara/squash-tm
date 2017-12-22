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
package org.squashtest.tm.core.foundation.i18n;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * Superclass for Spring beans which can resolve internationalized properties using the {@link Locale} set by the Spring
 * Framework. IT uses the {@link Locale} returned by {@link LocaleContextHolder} and defaults to platform default when
 * necessary.
 *
 * @author Gregory Fouquet
 *
 */
public class ContextBasedInternationalized implements MessageSourceAware {
	private static final Logger LOGGER = LoggerFactory.getLogger(ContextBasedInternationalized.class);

	private MessageSource messageSource;

	/**
	 * (non-Javadoc)
	 *
	 * When the message source is set, this method sends a notification using
	 * {@link #initializeMessageSource(MessageSource)}
	 *
	 * @see org.springframework.context.MessageSourceAware#setMessageSource(org.springframework.context.MessageSource)
	 */
	@Override
	public final void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
		initializeMessageSource(messageSource);
	}

	/**
	 * This method is to be overriden by subclasses which need to do further initialization using the message source.
	 *
	 * @param messageSource messageSource
	 */
	protected void initializeMessageSource(MessageSource messageSource) {
		// NOOP
	}

	protected final String getMessage(String code) throws NoSuchMessageException {
		return messageSource.getMessage(code, null, code, currentLocale());
	}

	protected final String getMessage(String code, Object[] args) throws NoSuchMessageException {
		return messageSource.getMessage(code, args, code, currentLocale());
	}

	/**
	 * @return the Locale set by the Spring framework or platform default.
	 */
	private Locale currentLocale() {
		Locale current = LocaleContextHolder.getLocale();

		if (current == null) {
			current = Locale.getDefault();
			LOGGER.warn("No locale available from LocaleContextHolder, platform default will be used instead");
		}

		return current;
	}
}
