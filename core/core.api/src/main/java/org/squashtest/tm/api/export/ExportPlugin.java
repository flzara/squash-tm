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
package org.squashtest.tm.api.export;

import org.squashtest.tm.api.workspace.WorkspaceType;

public interface ExportPlugin {
	
	/**
	 * @return the name of the plugin as displayed in the interface
	 */
	String getName();
	
	/**
	 * @return in which workspace this export plugin should be enabled
	 */
	WorkspaceType getDisplayWorkspace();
	
	/**
	 * @return the name of the javascript module that will handle the behavior of the menu item in the export menu. This module 
	 * must comply with the following : 
	 * <ol>
	 * 	<li>respect the require (AMD) format,</li>
	 * 	<li>the module object publishes a method init(item), which parameter will be the menu item DOM object.</li>
	 * </ol>
	 * 
	 * @see http://requirejs.org/docs/api.html
	 */
	String getJavascriptModuleName();
}
