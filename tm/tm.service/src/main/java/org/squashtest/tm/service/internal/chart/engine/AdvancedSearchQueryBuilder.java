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
package org.squashtest.tm.service.internal.chart.engine;

import org.springframework.data.domain.Pageable;
import org.squashtest.tm.domain.jpql.ExtendedHibernateQuery;


public class AdvancedSearchQueryBuilder {

	enum QueryProfile{
		MAIN_QUERY,			// for the main query, tuples returns the full axis + measures data
		SUBSELECT_QUERY,	// generate correlated subqueries, returning the measure only, correlated on axes supplied by the outer query
		SUBWHERE_QUERY;		// the "where" clause is supplied by the outer query and joined with axes from he outer query.
		// Returns 1 or null, ie it's test the existence of elements matching the predicate.
	}

	protected QuerydslToolbox utils = new QuerydslToolbox();

	protected DetailedChartQuery queryDefinition;

	private Pageable pageable;

	// the SubQueryBuilder would use a different strategy.
	// for the QueryBuilder, it is set to MAIN_QUERY.
	protected QueryBuilder.QueryProfile profile = QueryBuilder.QueryProfile.MAIN_QUERY;

	protected ExtendedHibernateQuery<?> detachedQuery;

	AdvancedSearchQueryBuilder(DetailedChartQuery queryDefinition, Pageable pageable){
		super();
		this.queryDefinition = queryDefinition;
		this.pageable = pageable;
	}

	// **************** actual building ***************************

	ExtendedHibernateQuery<?> createQuery(){


		QueryPlanner mainPlanner = new QueryPlanner(queryDefinition, utils);
		detachedQuery = mainPlanner.createQuery();


		ProjectionPlanner projectionPlanner = new ProjectionPlanner(queryDefinition, detachedQuery, utils);
		projectionPlanner.setProfile(profile);
		projectionPlanner.modifySearchQuery();


		FilterPlanner filterPlanner = new FilterPlanner(queryDefinition, detachedQuery, utils);
		filterPlanner.modifyQuery();

		return detachedQuery;
	}

}
