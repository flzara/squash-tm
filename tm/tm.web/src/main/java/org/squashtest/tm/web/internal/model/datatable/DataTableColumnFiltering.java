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

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.squashtest.tm.core.foundation.collection.ColumnFiltering;
import org.squashtest.tm.web.internal.model.viewmapper.DatatableMapper;

public class DataTableColumnFiltering implements ColumnFiltering {

	private final DataTableDrawParameters params;
	private Map<Object, Integer> dataProps = new HashMap<>();
	private DatatableMapper mapper;

	public DataTableColumnFiltering(DataTableDrawParameters params) {
		super();
		this.params = params;
		createDataProps();
	}

	public DataTableColumnFiltering(DataTableDrawParameters params, DatatableMapper mapper) {
		super();
		this.params = params;
		this.mapper = mapper;
		createDataPropsAutomation();
	}

	private void createDataProps() {
		Map<Integer, Object> mDataProp = params.getmDataProp();
		for (Entry<Integer, Object> entry : mDataProp.entrySet()) {
			dataProps.put(entry.getValue(), entry.getKey());
		}
	}

	private void createDataPropsAutomation() {
		Set<Entry<Integer, Object>> entries = params.getmDataProp().entrySet();
		for (Entry<Integer, Object> entry :entries) {
			if(!"tc-id".equals(entry.getValue()) && !"checkbox".equals(entry.getValue())) {
				dataProps.put(mapper.getMapping(entry.getValue()), entry.getKey());
			}
		}
	}

	@Override
	public List<String> getFilteredAttributes() {


		List<String> attr = new ArrayList<>();
		Set<Entry<Integer, String>> entries = params.getsSearches().entrySet();
		Object mDataIndex;
		for(int x=0; x<entries.size(); x++) {
			if(!StringUtils.isBlank(params.getsSearches(x))) {
				mDataIndex = params.getmDataProp(x);
				attr.add(mapper.getMapping(mDataIndex));
			}
		}

		return attr;
	}


	@Override
	public boolean isDefined() {
		return ! getFilteredAttributes().isEmpty();
	}


	@Override
	public String getFilter(String mDataProp) {
		Integer index = indexOf(mDataProp);
		return params.getsSearches(index);
	}


	@Override
	public boolean hasFilter(String mDataProp) {
		if (this.dataProps.containsKey(mDataProp)) {
			String filter = getFilter(mDataProp);
			return ! StringUtils.isBlank(filter);
		} else {
			return false;
		}
	}

	private Integer indexOf(String prop){
		return dataProps.get(prop);
	}

}
