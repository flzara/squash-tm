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
package org.squashtest.tm.service.internal.workspace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.squashtest.tm.api.wizard.WorkspacePlugin;
import org.squashtest.tm.service.workspace.WorkspacePluginManager;

import java.util.Collection;
import java.util.Collections;

@Service
public class WorkspacePluginManagerImpl implements WorkspacePluginManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(WorkspacePluginManagerImpl.class);

	/**
	 * The Collection of collected WorkspacePlugins from the classpath.
	 */
	@Autowired(required = false)
	private Collection<WorkspacePlugin> collectedWorkspacePlugins = Collections.EMPTY_LIST;

	@Override
	public Collection<WorkspacePlugin> findAll() {
		return Collections.unmodifiableCollection(collectedWorkspacePlugins);
	}
}
