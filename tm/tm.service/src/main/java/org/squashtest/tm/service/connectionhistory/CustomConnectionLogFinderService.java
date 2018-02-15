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
package org.squashtest.tm.service.connectionhistory;

import org.squashtest.tm.core.foundation.collection.ColumnFiltering;
import org.squashtest.tm.core.foundation.collection.Filtering;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.users.ConnectionLog;

import java.util.List;

/**
 * @author aguilhem
 */
public interface CustomConnectionLogFinderService {
	/**
	 * Find {@link ConnectionLog} based on filtering and paging the result list
	 * @param paging the {@link PagingAndSorting} to apply
	 * @param columnFiltering the {@link ColumnFiltering} to apply
	 * @return a {@link PagedCollectionHolder} of {@link ConnectionLog} with the adequate paging and sorting
	 */
	PagedCollectionHolder<List<ConnectionLog>> findAllFiltered(PagingAndSorting paging, ColumnFiltering columnFiltering);
}
