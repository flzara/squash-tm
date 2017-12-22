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

import org.squashtest.tm.api.plugin.Plugin;

/**
 * @author Gregory Fouquet
 * 
 */
public interface WizardPlugin extends Plugin {


	/**
	 * This wizard's name, the one which may appear in administation workspace and such. should not be null.
	 * 
	 * @return
	 */
	String getName();

	/**
	 *  returns the name of the javascript module that must be loaded and executed. May return null if none is applicable.
	 */
	String getModule();


}
