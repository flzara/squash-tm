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

import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.NullPrecedence;
import org.hibernate.criterion.Order;
import org.squashtest.tm.core.foundation.collection.MultiSorting;
import org.squashtest.tm.core.foundation.collection.Sorting;

/**
 * Utility class to apply sorting to a Hibernate query.
 *
 * @author Gregory Fouquet
 *
 */
public final class SortingUtils {

	private static final Pattern HQL_ORDER_PATTERN = Pattern.compile("order\\s+by\\s+\\S+\\s+(asc|desc)");

	private SortingUtils() {
		super();
	}

	// ********************** for Criterias ************************

	/**
	 * Adds sorting to a Criteria query.
	 *
	 * @param criteria
	 * @param sorting
	 */
	public static void addOrder(Criteria criteria, Sorting sorting) {
		if (!StringUtils.isBlank(sorting.getSortedAttribute())) {
			switch (sorting.getSortOrder()) {
			case ASCENDING:
				criteria.addOrder(Order.asc(sorting.getSortedAttribute()).nulls(NullPrecedence.FIRST));
				break;
			case DESCENDING:
				criteria.addOrder(Order.desc(sorting.getSortedAttribute()).nulls(NullPrecedence.LAST));
				break;
			}
		}
	}

	public static void addOrder(Criteria criteria, MultiSorting sorting) {
		for (Sorting sort : sorting.getSortings()) {
			addOrder(criteria, sort);
		}
	}

	public static void addOrders(Criteria criteria, Collection<Sorting> sortings) {
		for (Sorting next : sortings) {
			addOrder(criteria, next);
		}
	}

	// ***************** for HQL **********************

	public static String addOrders(String hql, Collection<Sorting> sortings){
		StringBuilder builder = new StringBuilder(hql);
		for (Sorting sorting : sortings){
			addOrder(builder, sorting);
		}
		return builder.toString();
	}

	public static void addOrder(StringBuilder hqlbuilder, Sorting sorting) {

		// escapes if nothing needs to be done
		if (StringUtils.isBlank(sorting.getSortedAttribute())) {
			return;
		}

		// handle possible multiple occurences of an order clause
		handlePreviousOrderClauses(hqlbuilder);

		// now add the clause
		hqlbuilder.append(sorting.getSortedAttribute()).append(" ").append(sorting.getSortOrder().getCode()).append(" nulls first");

	}

	public static String addOrder(String hql, Sorting sorting) {
		StringBuilder res = new StringBuilder(hql);
		addOrder(res, sorting);
		return res.toString();
	}


	// for the sake of optimization we won't simply loop over the method right above
	public static void addOrder(StringBuilder hqlbuilder, MultiSorting sortings) {

		// escapes if nothing needs to be done
		if (sortings.getSortings().isEmpty()) {
			return;
		}

		// handle possible multiple occurences of an order clause
		handlePreviousOrderClauses(hqlbuilder);

		// now add the clauses
		for (Iterator<Sorting> iter = sortings.getSortings().iterator(); iter.hasNext();) {
			Sorting sorting = iter.next();

			hqlbuilder.append(sorting.getSortedAttribute()).append(" ").append(sorting.getSortOrder().getCode());

			if (iter.hasNext()) {
				hqlbuilder.append(", ");
			}
		}

	}

	public static String addOrder(String hql, MultiSorting sorting) {
		StringBuilder res = new StringBuilder(hql);
		addOrder(res, sorting);
		return res.toString();
	}

	private static void handlePreviousOrderClauses(StringBuilder hqlbuilder) {
		if (HQL_ORDER_PATTERN.matcher(hqlbuilder.toString()).find()) {
			hqlbuilder.append(", ");
		} else {
			hqlbuilder.append(" order by ");
		}
	}
}
