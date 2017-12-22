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
 * Interface for column-filtering instructions.
 * Because filtering requires significantly more processing, services and dao using it should first check {@link #isDefined()} first before triggering the additional
 * filtering mechanisms.
 *
 *
 * @author flaurens
 *
 */
public interface ColumnFiltering {

	/**
	 * @return true if any filtering is required.
	 */
	boolean isDefined();

	/**
	 *
	 * @param index index
	 *
	 * @return the filter String for a column given its index
	 */
	String getFilter(Integer index);

	String getFilter(String mDataProp);

	boolean hasFilter(String mDataProp);

	boolean hasFilter(Integer index);

	@Deprecated
	String getFilter(String mDataProp, int offset);
}

