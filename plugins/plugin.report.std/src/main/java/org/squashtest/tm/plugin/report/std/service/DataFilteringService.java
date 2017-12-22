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
package org.squashtest.tm.plugin.report.std.service;

import org.squashtest.tm.domain.project.ProjectResource;


/**
 * 
 * Internal user only. Should not be exposed through OSGI.
 * 
 * Such service provide a convenient way to apply security-imposed and user-defined filters on various data.
 * 
 * 
 * @author bsiri
 *
 */
public interface DataFilteringService {

	/**
	 * if that interface is modified in the future because of more filtering options, at least you can rely
	 * on that one to be always present.
	 * 
	 * It will basically call all checks and return true if none of them failed.
	 * 
	 * 
	 * @param object to check
	 * @return true if all system and user checks are successful.
	 */
	boolean isFullyAllowed(Object object);
	
	/**
	 * That method will tell if the security system allows such object to be shipped to the end user.
	 * 
	 * @param object the object to check.
	 * @return true if the used is allowed to read that object.
	 */
	boolean hasReadPermissions(Object object);
	
	
	/**
	 * That method will tell if the user actually wants that data, or if he discards it.
	 * 
	 * 
	 * @param object the object to check.
	 * @return true if the user wants the data, false if he filtered it out.
	 */
	boolean isAllowedByUser(ProjectResource object);

}
