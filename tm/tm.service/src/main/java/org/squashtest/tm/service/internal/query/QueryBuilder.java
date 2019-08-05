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

import org.squashtest.tm.domain.jpql.ExtendedHibernateQuery;

/*
 * TODO : about the subqueries
 * 
 * Lets see simple examples that illustrate the mechanism, and how we could optimize this
 * 
 * ===========================================================
 * 
 * -------------------------
 * Current mechanism
 * -------------------------
 * 
 *  1/ select context :
 * 
 * 	Our example is "select requirement id and  their version count for all requirements covered by test case 1"
 * 
 *  Produced implementation is (correlated subquery) :
 * 
 * select requirement.id, (select count(distinct version2) from Requirement r2 join r2.versions versions2 where r2.id = requirement.id)
 * from TestCase testCase join testCase.requirementVersionCoverages cov
 * join cov.verifiedRequirementVersion version
 * join version.requirement requirement
 * group by testCase.id
 * 
 * 
 * ----------------------
 * 
 * 2/ where context
 * 
 *  Our example is "select count requirement covered by test case 1 that have at least 2 versions"
 * 
 * Produced implementation is (using subquery) :
 * 
 * select count(distinct requirement.id)
 * from TestCase testCase join testCase.requirementVersionCoverages cov
 * join cov.verifiedRequirementVersion version
 * join version.requirement requirement
 * where testCase.id =1
 * and exists (select 1 from Requirement req2 join req2.versions version2 where req2.id = requirement.id group by req2.id having count(distinct version2) > 1 )
 * group by testCase.id
 * 
 * 
 */
class QueryBuilder {


	protected QuerydslToolbox utils = new QuerydslToolbox();

	protected InternalQueryModel internalQueryModel;

	// the SubQueryBuilder would use a different strategy.
	// for the QueryBuilder, it is set to REGULAR_QUERY.
	protected QueryProfile profile = QueryProfile.REGULAR_QUERY;

	protected ExtendedHibernateQuery<?> detachedQuery;

	QueryBuilder(InternalQueryModel internalQueryModel){
		super();
		this.internalQueryModel = internalQueryModel;
	}




	// **************** actual building ***************************

	ExtendedHibernateQuery<?> createQuery(){

		QueryPlanner mainPlanner = new QueryPlanner(internalQueryModel, utils);
		detachedQuery = mainPlanner.createQuery();


		ProjectionPlanner projectionPlanner = new ProjectionPlanner(internalQueryModel, detachedQuery, utils);
		projectionPlanner.modifyQuery();


		FilterPlanner filterPlanner = new FilterPlanner(internalQueryModel, detachedQuery, utils);
		filterPlanner.modifyQuery();

		return detachedQuery;
	}



}
