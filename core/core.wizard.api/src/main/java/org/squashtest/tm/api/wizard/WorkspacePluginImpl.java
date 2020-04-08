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

import org.squashtest.tm.api.plugin.EntityReference;
import org.squashtest.tm.api.plugin.PluginType;
import org.squashtest.tm.api.plugin.PluginValidationException;

import java.util.Map;

/**
 * Implementation of {@linkplain WorkspacePlugin}
 * avoiding overriding all unused methods for further implementations.
 */
public class WorkspacePluginImpl implements WorkspacePlugin {

	private String id;
	private String[] accessRoles;
	private WorkspacePluginIcon workspaceIcon;

	@Override
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String[] getAccessRoles() {
		return accessRoles;
	}
	public void setAccessRoles(String[] accessRoles) {
		this.accessRoles = accessRoles;
	}

	@Override
	public WorkspacePluginIcon getWorkspaceIcon() {
		return workspaceIcon;
	}
	public void setWorkspaceIcon(WorkspacePluginIcon workspaceIcon) {
		this.workspaceIcon = workspaceIcon;
	}

	@Override
	public String getVersion() {
		throw new UnsupportedOperationException();
	}
	@Override
	public String getFilename() {
		throw new UnsupportedOperationException();
	}
	@Override
	public String getType() {
		throw new UnsupportedOperationException();
	}
	@Override
	public PluginType getPluginType() {
		throw new UnsupportedOperationException();
	}
	@Override
	public Map<String, String> getProperties() {
		throw new UnsupportedOperationException();
	}
	@Override
	public String getConfigurationPath(EntityReference context) {
		throw new UnsupportedOperationException();
	}
	@Override
	public void validate(EntityReference reference, Map<String, String> configuration) throws PluginValidationException {
		throw new UnsupportedOperationException();
	}
	@Override
	public void validate(EntityReference reference) throws PluginValidationException {
		throw new UnsupportedOperationException();
	}



}
