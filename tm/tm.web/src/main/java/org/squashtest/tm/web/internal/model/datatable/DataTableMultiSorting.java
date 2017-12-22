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

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.squashtest.tm.core.foundation.collection.DefaultSorting;
import org.squashtest.tm.core.foundation.collection.PagingAndMultiSorting;
import org.squashtest.tm.core.foundation.collection.SortOrder;
import org.squashtest.tm.core.foundation.collection.Sorting;
import org.squashtest.tm.web.internal.model.viewmapper.DatatableMapper;

public class DataTableMultiSorting extends DataTablePaging implements PagingAndMultiSorting {
	
	protected final DatatableMapper mapper;

	public DataTableMultiSorting(@NotNull DataTableDrawParameters params, @NotNull DatatableMapper mapper){
		super(params);
		this.mapper=mapper;
	}

	@Override
	public List<Sorting> getSortings() {
		
		List<Sorting> sortings = new ArrayList<>(params.getiSortingCols());
		
		int sortedcol;
		String sorteddir;
		Object mappingKey;
		String attribute;
		
		for (int i=0; i<params.getiSortingCols();i++){
			sorteddir = params.getsSortDir(i);
			sortedcol = params.getiSortCol(i);
					
			mappingKey = params.getmDataProp(sortedcol);
			attribute = mapper.getMapping(mappingKey);
			
			sortings.add(new DefaultSorting(attribute, SortOrder.coerceFromCode(sorteddir)));
		}
		
		return sortings;
	}

}
