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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.validation.constraints.NotNull;

/**
 * This class compares {@link Identified} objects. It uses an ordered collection (ie a list) of ids. The order of 2
 * Identified objects is the order of their ids in this list.
 * 
 * @author Gregory Fouquet
 * 
 */
public class IdentifiersOrderComparator implements Comparator<Identified> {
	private final List<Long> orderedIds;

	/**
	 * @param orderedIds
	 */
	public IdentifiersOrderComparator(@NotNull List<Long> orderedIds) {
		super();
		this.orderedIds = Collections.unmodifiableList(orderedIds);
	}

	/**
	 * 
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(Identified o1, Identified o2) {
		long id1 = orderedIds.indexOf(o1.getId());
		long id2 = orderedIds.indexOf(o2.getId());
		return (int) (id1 - id2);
	}
}
