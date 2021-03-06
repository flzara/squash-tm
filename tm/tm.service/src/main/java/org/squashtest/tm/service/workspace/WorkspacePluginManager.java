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
package org.squashtest.tm.service.workspace;

import org.squashtest.tm.api.wizard.WorkspacePlugin;

import java.util.Collection;

public interface WorkspacePluginManager {
	/**
	 * Get all {@linkplain WorkspacePlugin} collected from the classpath and authorized for the current User.
	 * @return A Collection of all WorkspacePlugins collected from the classpath and authorized for the current User .
	 */
	Collection<WorkspacePlugin> getAllAuthorized();
}
