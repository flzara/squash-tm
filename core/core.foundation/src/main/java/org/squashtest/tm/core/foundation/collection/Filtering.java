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
package org.squashtest.tm.core.foundation.collection;


/**
 * Interface for data-filtering instructions. The string that the user is looking for is returned by {@link #getFilter()}, and the name of the filtered attribute (if any)
 * is returned by {@link #getFilteredAttribute()} (see the method comments for details). 
 * Because filtering requires significantly more processing, services and dao using it should first check {@link #isDefined()} first before triggering the additional 
 * filtering mecanisms.
 * 
 * 
 * @author bsiri
 *
 */
public interface Filtering {

	
	/**
	 * @return true if any filtering is required. 
	 */
	boolean isDefined();
	
	/**
	 * @return the String that the user is searching for
	 */
	String getFilter();
	
	
	/**
	 * @return null if the filter is to be applied to any relevant attribute, a non-null values is the name (qualified or not) of the sorted attribute if the filtering should be applied to only one specific attribute.
	 */
	String getFilteredAttribute(); 
}
