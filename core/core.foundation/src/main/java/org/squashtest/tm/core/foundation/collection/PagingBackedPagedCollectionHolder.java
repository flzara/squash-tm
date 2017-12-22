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

import java.util.Collection;

/**
 * Implementation of a {@link PagedCollectionHolder} backed by a {@link Paging} object.
 *
 * @author Gregory Fouquet
 *
 */
public class PagingBackedPagedCollectionHolder<COLLECTION extends Collection<?>> implements PagedCollectionHolder<COLLECTION> {
	private final Paging paging;
	private final long totalNumberOfItems;
	private final COLLECTION items;

	/**
	 * @param paging paging
	 * @param totalNumberOfItems totalNumberOfItems
	 * @param items items
	 */
	public PagingBackedPagedCollectionHolder(Paging paging, long totalNumberOfItems, COLLECTION items) {
		super();
		this.paging = paging;
		this.totalNumberOfItems = totalNumberOfItems;
		this.items = items;
	}

	/**
	 * @see org.squashtest.tm.core.foundation.collection.PagedCollectionHolder#getFirstItemIndex()
	 */
	@Override
	public long getFirstItemIndex() {
		return paging.getFirstItemIndex();
	}

	/**
	 * @see org.squashtest.tm.core.foundation.collection.PagedCollectionHolder#getTotalNumberOfItems()
	 */
	@Override
	public long getTotalNumberOfItems() {
		return totalNumberOfItems;
	}

	/**
	 * @see org.squashtest.tm.core.foundation.collection.PagedCollectionHolder#getPagedItems()
	 */
	@Override
	public COLLECTION getPagedItems() {
		return items;
	}
}
