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
package org.squashtest.tm.web.internal.helper;

import java.util.Locale;

import javax.validation.constraints.NotNull;

/**
 * @author Gregory
 * 
 */
public interface LabelFormatter<T> {
	/**
	 * Indicates that, from now on, the given locale should be used.
	 * 
	 * @param locale
	 *            should not be <code>null</code>
	 * @return this object for method chaining purposes.
	 */
	LabelFormatter<T> useLocale(@NotNull Locale locale);

	/**
	 * From now on, produces escaped html.
	 * 
	 * @return
	 */
	LabelFormatter<T> escapeHtml();

	/**
	 * From now on, produces plain (unescaped) text.
	 * 
	 * @return
	 */
	LabelFormatter<T> plainText();

	/**
	 * 
	 * @param toFormat
	 *            object we want to create a formatted label. Should not be null.
	 * @return the formatted label.
	 */
	String formatLabel(@NotNull T toFormat);
}
