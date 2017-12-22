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

import org.springframework.util.StringUtils;

public class DefaultFiltering implements Filtering {

	public static final DefaultFiltering NO_FILTERING = new DefaultFiltering("", null);

	private String filteredAttribute;
	private String filter;

	public DefaultFiltering() {
		super();
	}

	public DefaultFiltering(String filteredAttribute, String filter) {
		super();
		this.filteredAttribute = filteredAttribute;
		this.filter = filter;
	}

	@Override
	public boolean isDefined() {
		return StringUtils.hasLength(filter);
	}

	@Override
	public String getFilter() {
		return filter;
	}

	@Override
	public String getFilteredAttribute() {
		return filteredAttribute;
	}

	public void setFilteredAttribute(String filteredAttribute) {
		this.filteredAttribute = filteredAttribute;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

}
