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
package org.squashtest.tm.service.internal.foundation.collection;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.squashtest.tm.core.foundation.collection.Paging;

/**
 * Utility clas for paging queries.
 * 
 * @author Gregory Fouquet
 * 
 */
public final class PagingUtils {
	private PagingUtils() {
		super();
	}

	public static void addPaging(Query query, Paging paging) {
		if (! paging.shouldDisplayAll()){
			query.setMaxResults(paging.getPageSize());
			query.setFirstResult(paging.getFirstItemIndex());
		}
	}

	public static void addPaging(Criteria criteria, Paging paging) {
		if (! paging.shouldDisplayAll()){
			criteria.setMaxResults(paging.getPageSize());
			criteria.setFirstResult(paging.getFirstItemIndex());
		}
	}
}
