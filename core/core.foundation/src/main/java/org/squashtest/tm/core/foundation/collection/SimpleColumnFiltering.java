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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleColumnFiltering implements ColumnFiltering {

	private Map<String, String> filters = new HashMap<>();

	@Override
	public boolean isDefined() {
		return ! filters.keySet().isEmpty();
	}

	@Override
	public List<String> getFilteredAttributes() {
		return new ArrayList<>(filters.keySet());
	}

	public SimpleColumnFiltering addFilter(String property, String value){
		filters.put(property, value);
		return this;
	}


	@Override
	public String getFilter(String mDataProp) {
		return filters.get(mDataProp);
	}

	@Override
	public boolean hasFilter(String mDataProp) {
		return filters.get(mDataProp) != null;
	}

}
