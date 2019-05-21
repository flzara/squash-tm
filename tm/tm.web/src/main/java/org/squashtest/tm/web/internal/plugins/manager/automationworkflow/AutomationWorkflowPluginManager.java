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

import java.util.Collection;
import java.util.Locale;
import java.util.Map;

public interface AutomationWorkflowPluginManager {

	/**
	 * Get the Map of the existing automation workflows filtered by a Collection of plugin Ids.
	 * @param activePluginsIds The Collection of plugin Ids to filter the result Map
	 * @param locale The Locale to which the plugin names are to translate.
	 * @return A Map of available automation workflows filtered by the given Ids Collection.
	 * Keys are the ids of the workflows and values are their names.
	 * This method is used to get all the activated AutomationWorkflows by giving the list of active plugins for a
	 * given Project as parameter.
	 */
	Map<String, String> getAutomationWorkflowsMapFilteredByIds(Collection<String> activePluginsIds, Locale locale);

	/**
	 * Get the Collection the existing automation workflows represented by their ids.
	 * @return A Collection of ids representing all the available automation workflows.
	 */
	Collection<String> getAutomationWorkflowsIds();

}
