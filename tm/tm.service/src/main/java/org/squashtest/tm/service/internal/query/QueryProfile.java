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
package org.squashtest.tm.service.internal.query;


/**
 * <p>
 * The enum QueryProfile provides more information about how an InternalQueryModel will be used.
 * Depending on that context, the query may alter the content of the projection, grouping etc when it
 * is run as a subquery. Depending on whether it is main query, a subselect or a subwhere has subtle
 * implications on the final shape of the query.
 * </p>
 *
 * <p>
 *     More on this in {@link InternalQueryModel} and in {@link SubQueryBuilder}.
 * </p>
 *
 *
 */
enum QueryProfile{
	REGULAR_QUERY,		// this query is to be treated as is, it requires not particular transformation
	SUBSELECT_QUERY,	// this query is a subquery within the select clause of the outer query. It requires special transformations, that will be undergone by the SubQueryBuilder and QueryPlanner
	SUBWHERE_QUERY;		// this query is a subquery within the where clause of the outer query. It also requires special transformations, albeit different.
}
