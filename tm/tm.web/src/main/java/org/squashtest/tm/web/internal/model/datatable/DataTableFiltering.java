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

import org.apache.commons.lang3.StringUtils;
import org.squashtest.tm.core.foundation.collection.Filtering;

public class DataTableFiltering implements Filtering{

	private final DataTableDrawParameters params;
	
	
	public DataTableFiltering(DataTableDrawParameters params) {
		super();
		this.params = params;
	}

	@Override
	public boolean isDefined() {
		return ! StringUtils.isBlank(params.getsSearch());
	}

	@Override
	public String getFilter() {
		return params.getsSearch();
	}


	// for now we do not filter for specific attributes, we filter for all of them
	@Override
	public String getFilteredAttribute() {
		return null;
	}

	
	
}
