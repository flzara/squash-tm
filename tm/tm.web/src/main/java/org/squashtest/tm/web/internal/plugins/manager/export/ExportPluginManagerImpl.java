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
package org.squashtest.tm.web.internal.plugins.manager.export;

import org.apache.commons.collections.map.MultiValueMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.squashtest.tm.annotation.WebComponent;
import org.squashtest.tm.api.export.ExportPlugin;
import org.squashtest.tm.api.workspace.WorkspaceType;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.Collections;

@WebComponent
public class ExportPluginManagerImpl implements ExportPluginManager {
	@Autowired(required = false)
	private Collection<ExportPlugin> plugins = Collections.emptyList();
	private final MultiValueMap pluginsByWorkspace = new MultiValueMap();

	@PostConstruct
	public void registerPlugins() {
		for (ExportPlugin plugin : plugins) {
			pluginsByWorkspace.put(plugin.getDisplayWorkspace(), plugin);
		}
	}


	@Override
	@SuppressWarnings("unchecked")
	public Collection<ExportPlugin> findAllByWorkspace(WorkspaceType workspace) {
		Collection<ExportPlugin> plugins = pluginsByWorkspace.getCollection(workspace);
		if (plugins == null) {
			plugins = Collections.emptySet();
		}
		return Collections.unmodifiableCollection(plugins);
	}
}
