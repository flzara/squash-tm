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

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import org.squashtest.tm.domain.query.QueryColumnPrototypeInstance;
import org.squashtest.tm.domain.jpql.ExtendedHibernateQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 	This class is responsible for adding the "select" and "group by" clauses. See main documentation on
 * 	{@link QueryProcessingServiceImpl} for more details on how it is done.
 * </p>
 *
 * <p>
 * 	Depending on the chosen profile, the projection will be :
 * 	<ul>
 * 		<li>MAIN_QUERY : the full projection will be applied (that is, axis then measures)</li>
 * 		<li>SUBSELECT_QUERY : only the measures will be projected - the axis only value is implicit because the outer query will drive it</li>
 * 		<li>SUBWHERE_QUERY : the select clause will always be 'select 1' - the rest of the inner query will define whether the result is null or not,
 * 			the outer  query can then test with 'exists (subquery)'  </li>
 * 	</ul>
 * </p>
 *
 * <h3>Hacked section, pay attention</h3>
 * <p>
 * 	Last detail, about grouping on subqueries (which happens when a subquery is defined as axis). The Hibernate HQL parser wont allow
 * 	group by/ order by on subqueries because it doesn't allow AST nodes of type query at that position. However it will allow column aliases.
 * 	So when this situation arise we have to give :
 * 		<ul>
 * 			<li>an alias to the subquery in the 'from' clause, </li>
 * 			<li>use that alias in the group by/order by</li>
 * 		</ul>
 *
 * 	<b>Gotcha #1 : </b> such notation is not supported by all databases, fortunately mysql and postgres do <br/>
 * 	<b>Gotcha #2 : </b> we cannot specify our own aliases because the aliases that Hibernate generates in the 'from' clause and
 * the 'group by' clause are inconsistent. A query is not supposed to be expressed that way in the first place. So we have to guess what
 * the alias will be, and follow the pattern col_0_0, col_0_1. This is highly dependent on Hibernate implementation and might break
 * any day.
 * 	<b>Gotcha #3 : </b> it's safer to specify aliases for subqueries only because hibernate would generate more meaningful aliases when
 * the aliases column is a regular column, thoses aliases wouldn't follow the generic pattern col_x_y.
 * </p>
 *
 *
 * @author bsiri
 *
 */
class ProjectionPlanner {

	private InternalQueryModel internalQueryModel;

	private ExtendedHibernateQuery<?> query;

	private QuerydslToolbox utils;

	// see comment on that hack above
	private static final String HIBERNATE_ALIAS_PREFIX = "col_x_0_";

	private enum SubqueryAliasStrategy{
		NONE,
		APPEND_ALIAS,
		REPLACE_BY_ALIAS;
	}

	ProjectionPlanner(InternalQueryModel internalQueryModel, ExtendedHibernateQuery<?> query){
		super();
		this.internalQueryModel = internalQueryModel;
		this.query = query;
		this.utils = new QuerydslToolbox();
	}

	ProjectionPlanner(InternalQueryModel definition, ExtendedHibernateQuery<?> query, QuerydslToolbox utils){
		super();
		this.internalQueryModel = definition;
		this.query = query;
		this.utils = utils;
	}


	void modifyQuery(){
		addProjections();
		addGroupBy();
		addSortBy();
	}

	private void addProjections(){

		QueryProfile profile = internalQueryModel.getQueryProfile();

		List<Expression<?>> selection = new ArrayList<>();

		switch(profile){

		// The case of the SubWhere query is special
		case SUBWHERE_QUERY :
			// that one is special : it's always 'select 1'
			Expression<?> select1 = Expressions.constant(1);
			selection.add(select1);
			break;

		// for the rest no problem
		default:
			populateClauses(selection, internalQueryModel.getProjectionColumns(), SubqueryAliasStrategy.APPEND_ALIAS);
			break;
		}


		// now stuff the query
		query.select(Projections.tuple(selection.toArray(new Expression[]{}))).distinct();

	}



	private void addGroupBy(){
		List<Expression<?>> groupBy = new ArrayList<>();

		populateClauses(groupBy, internalQueryModel.getAggregationColumns(), SubqueryAliasStrategy.REPLACE_BY_ALIAS);

		query.groupBy(groupBy.toArray(new Expression[]{}));
	}


	private void addSortBy(){

		List<Expression<?>> expressions = new ArrayList<>();

		populateClauses(expressions, internalQueryModel.getAggregationColumns(), SubqueryAliasStrategy.REPLACE_BY_ALIAS);

		List<OrderSpecifier> orders = new ArrayList<>();
		populateOrders(orders, expressions);

		query.orderBy(orders.toArray(new OrderSpecifier[]{}));

	}


	private void populateClauses(List<Expression<?>> toPopulate, List<? extends QueryColumnPrototypeInstance> columns, SubqueryAliasStrategy aliasStrategy){

		int count = 0;
		for (QueryColumnPrototypeInstance col : columns){

			Expression<?> expr = null;

			// regular column
			if (! utils.isSubquery(col)){
				expr = utils.createAsSelect(col);
			}
			else{
				String alias = genAlias(count);
				switch(aliasStrategy){
				case APPEND_ALIAS :
					expr = utils.createAsSelect(col);
					expr = Expressions.as(expr, alias);
					break;
				case REPLACE_BY_ALIAS :
					expr = Expressions.stringPath(alias);
					break;
				case NONE :
					expr = utils.createAsSelect(col);
					break;
				default:
					break;
				}
			}

			toPopulate.add(expr);
			count++;
		}

	}

	private void populateOrders(List<OrderSpecifier> orders, List<Expression<?>> expressions){
		for (Expression e : expressions){
			OrderSpecifier spec = new OrderSpecifier(Order.ASC, e);
			orders.add(spec);
		}
	}

	private String genAlias(int count){
		return HIBERNATE_ALIAS_PREFIX.replace("x", String.valueOf(count));
	}

}
