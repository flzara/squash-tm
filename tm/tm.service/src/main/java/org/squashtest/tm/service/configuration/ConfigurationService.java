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
package org.squashtest.tm.service.configuration;

public interface ConfigurationService {
	/**
	 * Names of properties configurable through this service.
	 */
	interface Properties {
		String MILESTONE_FEATURE_ENABLED = "feature.milestone.enabled";
		String CASE_INSENSITIVE_LOGIN_FEATURE_ENABLED = "feature.caseInsensitiveLogin.enabled";
		String UPLOAD_SIZE_LIMIT = "uploadfilter.upload.sizeLimitInBytes";
		String UPLOAD_EXTENSIONS_WHITELIST = "uploadfilter.fileExtensions.whitelist";
		String IMPORT_SIZE_LIMIT = "uploadfilter.upload.import.sizeLimitInBytes";
	}

	void createNewConfiguration(String key, String value);

	void updateConfiguration(String key, String value);

	String findConfiguration(String key);

	/**
	 * Finds the given config property. Property is coerced to boolean, defaulting to false when it could not be found
	 * or it has a gibberish value.
	 *
	 * @param key
	 * @return
	 */
	boolean getBoolean(String key);

	/**
	 * Sets the given boolean config property. Creates it when required.
	 *
	 * @param key
	 * @param value
	 */
	void set(String key, boolean value);

	/**
	 * Sets the given string config property. Creates it when required.
	 *
	 * @param key
	 * @param value
	 */
	void set(String key, String value);
}
