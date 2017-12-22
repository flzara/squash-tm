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
package org.squashtest.tm.domain;

import java.util.Comparator;

/**
 * compares {@link Identified} objects using their id.
 *
 * @author Gregory Fouquet
 *
 */
public class IdentifiedComparator implements Comparator<Identified> {
	private static final IdentifiedComparator INSTANCE = new IdentifiedComparator();

	public static IdentifiedComparator getInstance() {
		return INSTANCE;
	}

	/**
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(Identified o1, Identified o2) {
		if (o1.getId() == null) {
			return o2.getId() == null ? 0: -1;
		}
		if (o2.getId() == null) {
			return 1;
		}
		return o1.getId().compareTo(o2.getId());
	}

}
