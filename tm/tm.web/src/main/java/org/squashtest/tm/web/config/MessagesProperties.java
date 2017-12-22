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
package org.squashtest.tm.web.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * This class receives properties from the "spring.messages" namespace. They are used to configure Squash TM custom
 * message sources
 *
 * @author Gregory Fouquet
 * @since 1.13.0
 */
@ConfigurationProperties(prefix = "spring.messages")
public class MessagesProperties {
	/**
	 * Comma-separated list of basenames, each following the ResourceBundle convention.
	 * Essentially a fully-qualified classpath location. If it doesn't contain a package
	 * qualifier (such as "org.mypackage"), it will be resolved from the classpath root.
	 */
	private String basename = "messages";

	/**
	 * Message bundles encoding.
	 */
	private String encoding = "utf-8";

	/**
	 * Loaded resource bundle files cache expiration, in seconds. When set to -1, bundles
	 * are cached forever.
	 */
	private int cacheSeconds = -1;

	public String getBasename() {
		return basename;
	}

	public void setBasename(String basename) {
		this.basename = basename;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public int getCacheSeconds() {
		return cacheSeconds;
	}

	public void setCacheSeconds(int cacheSeconds) {
		this.cacheSeconds = cacheSeconds;
	}
}
