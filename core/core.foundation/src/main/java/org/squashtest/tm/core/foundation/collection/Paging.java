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
 * Defines a paging to apply when querying for a collection.
 * @author Gregory Fouquet
 *
 */
public interface Paging {

	/**
	 * The 0-based index of the first returned item.
	 *
	 * @return int
	 */
	int getFirstItemIndex();

	/**
	 * The size of a page ie the max number of items in the returned collection.
	 *
	 * @return int
	 */
	int getPageSize();


	/**
	 *
	 * @return true if the query should return all the valid rows (ie, the paging should not be applied)
	 */
	boolean shouldDisplayAll();

}
