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
package org.squashtest.tm.web.internal.plugins.manager.automationworkflow;

import org.apache.commons.lang.WordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.squashtest.tm.core.automationworkflow.AutomationWorkflow;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

@Component
public class AutomationWorkflowPluginManagerImpl implements AutomationWorkflowPluginManager {

	private static final String NONE = "None";
	private static final String NATIVE = "Native";

	@Autowired(required = false)
	Collection<AutomationWorkflow> plugins = Collections.EMPTY_LIST;

	@Override
	public Collection<String> findAllNames() {
		ArrayList<String> result = new ArrayList<>();
		result.add(NONE);
		result.add(NATIVE);
		for(AutomationWorkflow workflow : plugins) {
			result.add(WordUtils.capitalize(workflow.getId()));
		}
		return result;
	}
}
