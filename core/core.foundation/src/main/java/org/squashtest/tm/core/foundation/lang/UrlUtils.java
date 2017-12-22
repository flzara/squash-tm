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

import java.net.MalformedURLException;
import java.net.URL;

import org.squashtest.tm.core.foundation.exception.InvalidUrlException;

/**
 * @author Gregory Fouquet
 *
 */
public final class UrlUtils {

	private UrlUtils() {
		super();
	}

	/**
	 * Coerces the given string into a URL. When it's not coercible, throws a {@link InvalidUrlException}
	 *
	 * @param url url
	 * @return URL
	 * @throws InvalidUrlException InvalidUrlException
	 */
	public static URL toUrl(String url) throws InvalidUrlException {
		try {
			return new URL(url);
		} catch (MalformedURLException e) {
			throw new InvalidUrlException(e);
		}
	}
}
