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

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import org.squashtest.tm.domain.jpql.ExtendedHibernateQuery;
import org.squashtest.tm.domain.query.Operation;
import org.squashtest.tm.domain.query.QueryAggregationColumn;
import org.squashtest.tm.domain.query.QueryColumnPrototypeInstance;
import org.squashtest.tm.domain.query.QueryFilterColumn;
import org.squashtest.tm.domain.query.QueryProjectionColumn;
import org.squashtest.tm.domain.query.SpecializedEntityType;

import java.util.Arrays;
import java.util.List;

/**
 * This class creates a subquery, it is used for ChartQueries having a QueryStrategy = SUBQUERY.
 *
 * @author bsiri
 *
 */
class SubQueryBuilder extends QueryBuilder {


	// used for subselect subqueries
	private Expression<?> subselectProfileJoinExpression;

	// used for subwhere subqueries
	private QueryFilterColumn subwhereProfileFilterExpression;


	SubQueryBuilder(QueryColumnPrototypeInstance columnInstance) {
		super( ExpandedConfiguredQuery.createFor(columnInstance) );
	}


	// ====================== configuration section =============

	SubQueryBuilder withRootEntity(InternalEntityType type){
		expandedQuery.withRootEntity(type);
		return this;
	}

	SubQueryBuilder withRootEntity(SpecializedEntityType type){
		InternalEntityType internalType = InternalEntityType.fromSpecializedType(type);
		expandedQuery.withRootEntity(internalType);
		return this;
	}

	SubQueryBuilder asSubselectQuery(){
		profile = QueryProfile.SUBSELECT_QUERY;
		expandedQuery.withProfile(profile);
		utils.setSubContext(generateContextName());
		return this;
	}

	SubQueryBuilder asSubwhereQuery(){
		profile = QueryProfile.SUBWHERE_QUERY;
		expandedQuery.withProfile(profile);
		utils.setSubContext(generateContextName());
		return this;
	}


	SubQueryBuilder joinRootEntityOn(Expression<?> mainQueryPath) {
		this.subselectProfileJoinExpression = mainQueryPath;
		return this;
	}

	SubQueryBuilder filterOn(QueryFilterColumn filter){
		this.subwhereProfileFilterExpression = filter;
		return this;
	}


	// **************** actual building ***************************

	@Override
	ExtendedHibernateQuery<?> createQuery(){

		checkConfiguration();

		super.createQuery();

		if (profile == QueryProfile.SUBSELECT_QUERY){
			joinWithOuterquery();
		}

		if (profile == QueryProfile.SUBWHERE_QUERY){
			joinWithOuterquery();
			addSubwhereSpecifics();
		}

		return detachedQuery;

	}

	// we must join on the root entity of the subquery with the specified axe
	private void joinWithOuterquery(){

		List<QueryAggregationColumn> aggregationColumns = expandedQuery.getAggregationColumns();

		Expression<?> outerQueryJoinPath = subselectProfileJoinExpression;
		Expression<?> subQueryJoinPath = utils.getQBean(expandedQuery.getRootEntity());

		BooleanExpression joinWhere = Expressions.predicate(Ops.EQ, outerQueryJoinPath, subQueryJoinPath);

		detachedQuery.where(joinWhere);
	}



	// we must filter on the measure. Take care that if the measure has an aggregate operation, the
	// additional filter will take the form of a having clause.
	private void addSubwhereSpecifics(){

		QueryProjectionColumn projectionColumn = expandedQuery.getProjectionColumns().get(0);

		Expression<?> measureExpr = utils.createAsSelect(projectionColumn);
		Operation operation = subwhereProfileFilterExpression.getOperation();
		List<Expression<?>> operands = utils.createOperands(subwhereProfileFilterExpression, operation);

		BooleanExpression predicate = utils.createPredicate(operation, measureExpr, projectionColumn.getDataType(),
				operands.toArray(new Expression[] {}));

		if (utils.isAggregate(projectionColumn.getOperation())){
			detachedQuery.having(predicate);
		}
		else{
			detachedQuery.where(predicate);
		}

	}



	private void checkConfiguration(){
		switch(profile){
		case SUBSELECT_QUERY :
			checkSubselectConfiguration();
			break;
		case SUBWHERE_QUERY :
			checkSubwhereConfiguration();
			break;
		default : break;
		}
	}

	private void checkSubselectConfiguration(){
		if (subselectProfileJoinExpression == null){
			throw new IllegalArgumentException("subselect queries must always provide a join with the outer query, please use joinRootEntityOn()");
		}
	}

	private void checkSubwhereConfiguration(){
		if (subselectProfileJoinExpression == null){
			throw new IllegalArgumentException("subwhere queries must always provide a join with the outer query, please use joinRootEntityOn()");
		}

		if (subwhereProfileFilterExpression == null){
			throw new IllegalArgumentException("subwhere queries must always provide a filter on the measure, please use filterOn()");
		}
	}

	private String generateContextName(){
		return Double.valueOf(Math.random()).toString().substring(2,5);
	}

}
