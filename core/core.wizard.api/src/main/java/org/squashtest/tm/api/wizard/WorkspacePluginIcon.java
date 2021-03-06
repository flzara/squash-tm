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
package org.squashtest.tm.api.wizard;

public interface WorkspacePluginIcon {
	/**
	 * Get this Icon's workspace name.
	 * Will be used for icon class and highlight, also defined in html file for navbar
	 * @return This Icon's workspace name.
	 */
	String getWorkspaceName();
	/**
	 * Get this Icon's icon file path.
	 * @return This Icon's icon file path.
	 */
	String getIconFilePath();
	/**
	 * Get this Icon's icon hover file path.
	 * @return This Icon's icon hover file path.
	 */
	String getIconHoverFilePath();
	/**
	 * Get this Icon's tooltip.
	 * @return This Icon's tooltip.
	 */
	String getTooltip();
	/**
	 * Get the context-relative Url bound to this Icon.
	 * @return The context-relative Url bound to this Icon.
	 */
	String getUrl();
}
