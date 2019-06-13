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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.squashtest.tm.domain.jpql.ExtendedHibernateQuery;
import org.squashtest.tm.domain.query.QueryColumnPrototypeInstance;
import org.squashtest.tm.domain.query.QueryOrderingColumn;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;

/**
 * <p>
 * 	This class is responsible for adding the "select" and "group by" clauses. See main documentation on
 * 	{@link QueryProcessingServiceImpl} for more details on how it is done.
 * </p>
 *
 * <p>
 * 	Depending on the chosen profile, the projection will be :
 * 	<ul>
 * 		<li>REGULAR_QUERY : the full projection will be applied (that is, axis then measures)</li>
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

	// configured properties
	private InternalQueryModel internalQueryModel;

	private ExtendedHibernateQuery<?> query;

	private QuerydslToolbox utils;
	
	
	// state properties
	private AliasIndex aliasIndex = new AliasIndex();


	/*
	 * The documentation for that enum will be given in the context of use, see #populateClauses()
	 */
	private enum SubqueryAliasStrategy{
		NONE,
		APPEND_ALIAS,
		REPLACE_BY_ALIAS_IF_POSSIBLE;
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

		populateClauses(groupBy, internalQueryModel.getAggregationColumns(), SubqueryAliasStrategy.REPLACE_BY_ALIAS_IF_POSSIBLE);

		query.groupBy(groupBy.toArray(new Expression[]{}));
	}


	private void addSortBy(){

		List<Expression<?>> expressions = new ArrayList<>();

		populateClauses(expressions, internalQueryModel.getOrderingColumns(), SubqueryAliasStrategy.REPLACE_BY_ALIAS_IF_POSSIBLE);

		List<OrderSpecifier> orders = new ArrayList<>();
		populateOrders(orders, internalQueryModel.getOrderingColumns(), expressions);

		query.orderBy(orders.toArray(new OrderSpecifier[]{}));

	}


	private void populateClauses(List<Expression<?>> toPopulate, List<? extends QueryColumnPrototypeInstance> columns, SubqueryAliasStrategy aliasStrategy){

		for (QueryColumnPrototypeInstance col : columns){

			Expression<?> expr = null;

			// regular column
			if (! utils.isSubquery(col)){
				expr = utils.createAsSelect(col);
			}
			//subquery columns
			else{
				switch(aliasStrategy){
				
				/*
				 * APPEND_ALIAS : we always want an alias. Either retrieve it or generate a new one. 
				 * The column is then appended to the query and aliased.
				 */
				case APPEND_ALIAS :
					String alias = aliasIndex.getOrGenerateAlias(col);
					expr = utils.createAsSelect(col);
					expr = Expressions.as(expr, alias);
					break;
					
				/*
				 * NONE : we never want an alias. The column is appended but no alias is given.
				 */
				case NONE :
					expr = utils.createAsSelect(col);
					break;
				
				/*
				 * REPLACE_BY_ALIAS_IF_POSSIBLE : if an alias exist for that column reuse it (replace 
				 * the column expression by the alias), else append the column without alias.
				 */
				case REPLACE_BY_ALIAS_IF_POSSIBLE :
					
					Optional<String> maybeAlias =  aliasIndex.getMaybeAlias(col);
					
					if (maybeAlias.isPresent()){
						expr = Expressions.stringPath(maybeAlias.get());
					}
					else{
						expr = utils.createAsSelect(col);
					}
					
					
					break;
					
					
				default:
					break;
				}
			}

			toPopulate.add(expr);
		}

	}

	private void populateOrders(List<OrderSpecifier> orders, List<QueryOrderingColumn> queryOrdering, List<Expression<?>> expressions){

		for (int i=0; i < queryOrdering.size(); i++){
			QueryOrderingColumn column = queryOrdering.get(i);
			Expression<?> expr = expressions.get(i);
			OrderSpecifier spec = new OrderSpecifier(column.getOrder(), expr);
			orders.add(spec);
		}

	}



	
	/*
	 * Aliases management. It helps us keeping aliases consistent across select, groupBy and sortBy clauses. 
	 */
	private static final class AliasIndex{
		// see comment on that hack at the top of the class
		private static final String HIBERNATE_ALIAS_PREFIX = "col_x_0_";
		
		private Map<Long, String> aliasByPrototypeId = new HashMap<>();
		private int counter = 0;
		
		// returns the alias if found, generating it if needed
		private String getOrGenerateAlias(QueryColumnPrototypeInstance instance){
			
			Long protoId = instance.getColumn().getId();			
			return aliasByPrototypeId.computeIfAbsent(protoId, (id) -> generate());
			
		}
		
		// return the alias as an Optional, which may be empty if it wasn't defined yet.
		private Optional<String> getMaybeAlias(QueryColumnPrototypeInstance instance){
			Long protoId = instance.getColumn().getId(); 
			return Optional.ofNullable(aliasByPrototypeId.get(protoId));
		}
		
		// internal usage only
		private String generate(){
			return HIBERNATE_ALIAS_PREFIX.replace("x", String.valueOf(counter++));
		}
		
		
		
	}

}
