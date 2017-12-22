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
package org.squashtest.tm.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Gregory Fouquet
 * @since 1.13.0
 */
@ConfigurationProperties(prefix = "squash.path")
public class SquashPathProperties {
	/**
	 * Root path for languages redefinition packages
	 */
	private String languagesPath;
	/**
	 * Root path for plugins
	 */
	private String pluginsPath;
	/**
	 * Root path for bundles
	 */
	private String bundlesPath;

	public String getBundlesPath() {
		return bundlesPath;
	}

	public void setBundlesPath(String bundlesPath) {
		this.bundlesPath = bundlesPath;
	}

	public String getLanguagesPath() {
		return languagesPath;
	}

	public void setLanguagesPath(String languagesPath) {
		this.languagesPath = languagesPath;
	}

	public String getPluginsPath() {
		return pluginsPath;
	}

	public void setPluginsPath(String pluginsPath) {
		this.pluginsPath = pluginsPath;
	}

}
