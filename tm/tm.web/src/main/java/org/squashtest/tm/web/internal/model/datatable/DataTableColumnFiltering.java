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
package org.squashtest.tm.web.internal.model.datatable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.squashtest.tm.core.foundation.collection.ColumnFiltering;

public class DataTableColumnFiltering implements ColumnFiltering {

	private final DataTableDrawParameters params;
	private Map<Object, Integer> dataProps = new HashMap<>();

	public DataTableColumnFiltering(DataTableDrawParameters params) {
		super();
		this.params = params;
		createDataProps();
	}

	private void createDataProps() {
		Map<Integer, Object> mDataProp = params.getmDataProp();
		for (Entry<Integer, Object> entry : mDataProp.entrySet()) {
			dataProps.put(entry.getValue(), entry.getKey());
		}
	}

	@Override
	public boolean isDefined() {
		Collection<String> values = params.getsSearches().values();
		for (String value : values) {
			if (!StringUtils.isBlank(value)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getFilter(Integer index) {
		return params.getsSearches(index);
	}

	@Override
	public boolean hasFilter(Integer index) {
		return !StringUtils.isBlank(getFilter(index));
	}

	@Override
	public String getFilter(String mDataProp) {
		return getFilter(this.dataProps.get(mDataProp));
	}

	@Override
	public boolean hasFilter(String mDataProp) {
		if (this.dataProps.containsKey(mDataProp)) {
			return hasFilter(this.dataProps.get(mDataProp));
		} else {
			return false;
		}
	}

	@Override
	@Deprecated
	/**
	 * @deprecated does not seem to be used any longer
	 */
	public String getFilter(String mDataProp, int offset) {
		return getFilter(this.dataProps.get(mDataProp) + offset);
	}
}
