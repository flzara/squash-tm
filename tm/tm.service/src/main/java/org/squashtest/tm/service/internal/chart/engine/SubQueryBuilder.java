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

import java.util.Arrays;
import java.util.List;

import org.squashtest.tm.domain.chart.AxisColumn;
import org.squashtest.tm.domain.chart.Filter;
import org.squashtest.tm.domain.chart.MeasureColumn;
import org.squashtest.tm.domain.chart.Operation;
import org.squashtest.tm.domain.jpql.ExtendedHibernateQuery;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;

/**
 * This class creates a subquery, it is used for ChartQueries having a QueryStrategy = SUBQUERY.
 *
 * @author bsiri
 *
 */
class SubQueryBuilder extends QueryBuilder {


	// used for subselect subqueries
	private List<Expression<?>> subselectProfileJoinExpression;

	// used for subwhere subqueries
	private Filter subwhereProfileFilterExpression;



	SubQueryBuilder(DetailedChartQuery queryDefinition) {
		super(queryDefinition);
	}


	// ====================== configuration section =============

	SubQueryBuilder asMainQuery(){
		profile = QueryProfile.MAIN_QUERY;
		utils.setSubContext(null);
		return this;
	}

	SubQueryBuilder asSubselectQuery(){
		profile = QueryProfile.SUBSELECT_QUERY;
		utils.setSubContext(generateContextName());
		return this;
	}

	SubQueryBuilder asSubwhereQuery(){
		profile = QueryProfile.SUBWHERE_QUERY;
		utils.setSubContext(generateContextName());
		return this;
	}

	SubQueryBuilder joinAxesOn(List<Expression<?>> axes){
		this.subselectProfileJoinExpression = axes;
		return this;
	}

	SubQueryBuilder joinAxesOn(Expression<?>... axes) {
		this.subselectProfileJoinExpression = Arrays.asList(axes);
		return this;
	}

	SubQueryBuilder filterMeasureOn(Filter filter){
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

	// we must join on the axes with those of the outer query
	private void joinWithOuterquery(){
		BooleanBuilder joinWhere = new BooleanBuilder();

		List<AxisColumn> axes = queryDefinition.getAxis();

		for (AxisColumn axe : axes) {

			Expression<?> outerAxis = subselectProfileJoinExpression.get(0);
			Expression<?> subAxis = utils.getQBean(axe);

			joinWhere.and(Expressions.predicate(Ops.EQ, outerAxis, subAxis));
		}

		detachedQuery.where(joinWhere);
	}



	// we must filter on the measure. Take care that if the measure has an aggregate operation, the
	// additional filter will take the form of a having clause.
	private void addSubwhereSpecifics(){

		MeasureColumn measure = queryDefinition.getMeasures().get(0);

		Expression<?> measureExpr = utils.createAsSelect(measure);
		Operation operation = subwhereProfileFilterExpression.getOperation();
		List<Expression<?>> operands = utils.createOperands(subwhereProfileFilterExpression, operation);

		BooleanExpression predicate = utils.createPredicate(operation, measureExpr, measure.getDataType(),
				operands.toArray(new Expression[] {}));

		if (utils.isAggregate(measure.getOperation())){
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
			throw new IllegalArgumentException("subselect queries must always provide a join with the outer query, please use joinAxesOn()");
		}

		if (subselectProfileJoinExpression.size() != queryDefinition.getAxis().size()){
			throw new IllegalArgumentException("subselect queries joined entities must match (in number and type) the axis entities of the subquery");
		}
	}

	private void checkSubwhereConfiguration(){
		if (subselectProfileJoinExpression == null){
			throw new IllegalArgumentException("subwhere queries must always provide a join with the outer query, please use joinAxesOn()");
		}

		if (subselectProfileJoinExpression.size() != queryDefinition.getAxis().size()){
			throw new IllegalArgumentException("subwhere queries joined entities must match (in number and type) the axis entities of the subquery");
		}
		if (subwhereProfileFilterExpression == null){
			throw new IllegalArgumentException("subwhere queries must always provide a filter on the measure, please use filterMeasureOn()");
		}
	}

	private String generateContextName(){
		return Double.valueOf(Math.random()).toString().substring(2,5);
	}

}
