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
package org.squashtest.tm.core.dynamicmanager.internal.handler;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.squashtest.tm.core.foundation.collection.Sorting;

/**
 * Utility class to apply sorting to a Hibernate query.
 * 
 * @author Gregory Fouquet
 * 
 */
final class SortingUtils {
	private SortingUtils() {
		super();
	}

	/**
	 * Adds sorting to a Criteria query.
	 * 
	 * @param criteria
	 * @param sorting
	 */
	public static void addOrder(Criteria criteria, Sorting sorting) {
		switch (sorting.getSortOrder()) {
		case ASCENDING:
			criteria.addOrder(Order.asc(sorting.getSortedAttribute()));
			break;
		case DESCENDING:
			criteria.addOrder(Order.desc(sorting.getSortedAttribute()));
			break;
		}
	}

	/**
	 * Adds "order by" clause to a hql buffer.
	 * 
	 * @param hql
	 * @param sorting
	 */
	public static void addOrder(StringBuilder hql, Sorting sorting) {
		if (StringUtils.isBlank(sorting.getSortedAttribute())) {
			return;
		}
		
		hql.append(" order by ").append(sorting.getSortedAttribute());

		switch (sorting.getSortOrder()) {
		case ASCENDING:
			hql.append(" asc");
			break;
		case DESCENDING:
			hql.append(" desc");
			break;
		}
	}
}
