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
 * The enum QueryProfile provides more information about how an ExpandedConfiguredQuery will be used.
 * Depending on that context, the query may alter the content of the projection, grouping etc when it
 * is run as a subquery. Depending on whether it is main query, a subselect or a subwhere has subtle
 * implications on the final shape of the query.
 * </p>
 *
 * <p>
 *     More on this in {@link ExpandedConfiguredQuery} and in {@link SubQueryBuilder}.
 * </p>
 *
 *
 */
enum QueryProfile{
	MAIN_QUERY,			// for the main query, tuples returns the full axis + measures data
	SUBSELECT_QUERY,	// generate correlated subqueries, returning the measure only, correlated on axes supplied by the outer query
	SUBWHERE_QUERY;		// the "where" clause is supplied by the outer query and joined with axes from he outer query.
	// Returns 1 or null, ie it's test the existence of elements matching the predicate.
}
