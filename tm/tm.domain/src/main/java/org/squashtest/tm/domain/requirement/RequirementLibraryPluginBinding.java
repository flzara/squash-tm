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
package org.squashtest.tm.domain.requirement;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.squashtest.tm.domain.project.LibraryPluginBinding;

import static org.squashtest.tm.domain.requirement.RequirementLibraryPluginBinding.RL_TYPE;

@Entity
@DiscriminatorValue(RL_TYPE)
public class RequirementLibraryPluginBinding extends LibraryPluginBinding {

	public static final String RL_TYPE = "R";

	public RequirementLibraryPluginBinding() {
		super();
	}

	public RequirementLibraryPluginBinding(String pluginId) {
		super(pluginId);
	}

}
