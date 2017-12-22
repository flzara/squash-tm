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

import javax.validation.constraints.NotNull;

import org.springframework.data.domain.Pageable;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.SortOrder;
import org.squashtest.tm.core.foundation.collection.SpringPaginationUtils;
import org.squashtest.tm.web.internal.model.viewmapper.DatatableMapper;

public class DataTableSorting extends DataTablePaging implements PagingAndSorting {
	
	// FIXME DatatableMapper<String>
	private final DatatableMapper mapper;

	
	// FIXME DatatableMapper<String>
	public DataTableSorting(@NotNull DataTableDrawParameters params, @NotNull DatatableMapper mapper){
		super(params);
		this.mapper=mapper;
	}
	
	@Override
	public String getSortedAttribute() {
		return mapper.getMapping(params.getsSortedAttribute_0());
	}


	@Override
	public SortOrder getSortOrder() {
		return SortOrder.coerceFromCode(params.getsSortDir_0());
	}
	
	@Override
	public Pageable toPageable() {
		return SpringPaginationUtils.toPageable(this);
	}
	

}
