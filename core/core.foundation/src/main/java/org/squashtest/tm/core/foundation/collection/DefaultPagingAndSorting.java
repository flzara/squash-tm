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

import org.springframework.data.domain.Pageable;

public class DefaultPagingAndSorting implements PagingAndSorting {

	private String sortedAttribute;
	private Integer pageSize = 50;
	private Integer firstIndex = 0;
	private SortOrder order = SortOrder.ASCENDING;
	private boolean isShouldDisplayAll = false;

	public DefaultPagingAndSorting() {
		super();
	}

	public DefaultPagingAndSorting(String sortedAttribute) {
		super();
		this.sortedAttribute = sortedAttribute;
	}

	public DefaultPagingAndSorting(String sortedAttribute, boolean shouldDisplayAll){
		super();
		this.sortedAttribute = sortedAttribute;
		this.isShouldDisplayAll = shouldDisplayAll;
	}

	public DefaultPagingAndSorting(Integer pageSize) {
		super();
		this.pageSize = pageSize;
	}

	public DefaultPagingAndSorting(String sortedAttribute, Integer pageSize) {
		super();
		this.sortedAttribute = sortedAttribute;
		this.pageSize = pageSize;
	}

	public void setSortedAttribute(String sortedAttribute) {
		this.sortedAttribute = sortedAttribute;
	}

	public void setFirstItemIndex(int index) {
		this.firstIndex = index;
	}

	@Override
	public int getFirstItemIndex() {
		return firstIndex;
	}

	@Override
	public int getPageSize() {
		return pageSize;
	}

	@Override
	public boolean shouldDisplayAll() {
		return isShouldDisplayAll;
	}

	public void setShouldDisplayAll(boolean should) {
		this.isShouldDisplayAll = should;
	}

	@Override
	public String getSortedAttribute() {
		return sortedAttribute;
	}

	public void setSortOrder(SortOrder order) {
		this.order = order;
	}

	@Override
	public SortOrder getSortOrder() {
		return order;
	}

	@Override
	public Pageable toPageable() {
		return SpringPaginationUtils.toPageable(this);
	}

}
