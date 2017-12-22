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
package org.squashtest.tm.web.internal.controller.testcase.parameters;

import java.io.Serializable;
import java.util.Comparator;

import org.squashtest.tm.core.foundation.collection.SortOrder;
import org.squashtest.tm.domain.testcase.Parameter;

/**
 * Will compare {@link Parameter} on their name in the given {@link SortOrder}
 *
 * @author mpagnon
 *
 */
@SuppressWarnings("serial")
public final class ParameterNameComparator implements Comparator<Parameter>,
		Serializable {

	private SortOrder sortOrder;

	public ParameterNameComparator(SortOrder sortOrder) {
		this.sortOrder = sortOrder;
	}

	@Override
	public int compare(Parameter o1, Parameter o2) {
		int ascResult = o1.getName().compareTo(o2.getName());
		if (sortOrder == SortOrder.ASCENDING) {
			return ascResult;
		} else {
			return -ascResult;
		}
	}

}
