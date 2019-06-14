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
import java.util.function.Function;
import java.util.stream.Collectors;

import org.squashtest.tm.domain.jpql.ExtendedHibernateQuery;
import org.squashtest.tm.domain.query.QueryColumnPrototypeInstance;

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
 * 		<li>REGULAR_QUERY and SUBSELCT_QUERY : projection are applied normally</li>
 * 		<li>SUBWHERE_QUERY : the select clause will always be 'select 1' - the rest of the inner query will define whether the result is null or not,
 * 			the outer  query can then test with 'exists (subquery)'  </li>
 * 	</ul>
 * </p>
 *
 * <h3>Hacked section, pay attention</h3>
 * <p>
 * 	Last detail, about grouping on subqueries (which happens when a subquery is defined as a projection column). The Hibernate HQL parser wont allow
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
 * 	<b>Gotcha #3 : </b> Because the use of a .distinct() in the select, we have problems when we also need to sort on level_enums. See
 * 	comment on {@link #addProjections()} on that matter.
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
	private enum AliasStrategy {
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



	/**
	 * <p>
		Convert the projection columns to expressions and add them to the select clause.
		</p>
		 <p>
			The expression is fetched with .distinct(), which eliminates possible duplicates,
			eg when the filter's where clauses cause the same projection columns to be found
			multiple times.
		 </p>
	 <p>
		There is however a corner case when sorting on a level enum column when using
		distinct() (when there is no .distinct() the problem wouldn't arise) :
		 <ol>
			<li>because it is a sorted column it must appear in the projection,</li>
			<li>because it is a level enum we need to use a case-when construct to sort it.</li>
		 </ol>
	 </p>
	 <p>
		The second point would turn the actual enum data into the level we used to sort it,
		which make the result set incorrect. To reconcile both requirements we have to
		include such columns twice : once with the actual data and once again as the case-when
		construct.
	 </p>

	 <p>
	 	Remember that, when that situation happens :
	 	<ul>
	 		<li>if level enum columns appear in the group by clause, the case-when form of that column must also be grouped on</li>
	 		<li>if a level enum column appear in the sort by clause (the very reason why we have that problem), only the case-when column should be sorted.</li>
	 	</ul>
	 </p>
	 */
	private void addProjections(){

		QueryProfile profile = internalQueryModel.getQueryProfile();

		List<Expression<?>> projections;
		List<Expression<?>> levelEnumSortableProjections;

		switch(profile){

		// The case of the SubWhere query is special
		case SUBWHERE_QUERY :
			// that one is special : it's always 'select 1'
			Expression<?> select1 = Expressions.constant(1);
			projections =  new ArrayList<>(1);
			projections.add(select1);
			break;

		// for the rest no problem
		default:
			// convert all our columns into selectable expressions
			projections =
				internalQueryModel.getProjectionColumns()
					.stream()
					.map(column -> convertToExpression(column, AliasStrategy.APPEND_ALIAS, utils::createAsSelect))
					.collect(Collectors.toList());


			// additional mission, if any column with datatype LEVEL_ENUM is present,
			// include it again using the case-when form, and alias it.
			levelEnumSortableProjections =
				internalQueryModel.getOrderingColumns()
				.stream()
				.map(column -> convertToExpression(column, AliasStrategy.APPEND_ALIAS, utils::createAsCaseWhen))
				.collect(Collectors.toList());

			projections.addAll(levelEnumSortableProjections);

			break;
		}


		// now stuff the query
		query.select(Projections.tuple(toArray(projections))).distinct();

	}


	private void addGroupBy(){
		List<Expression<?>> groupBy =
			internalQueryModel.getAggregationColumns()
				.stream()
				.map(column -> convertToExpression(column, AliasStrategy.REPLACE_BY_ALIAS_IF_POSSIBLE, utils::createAsGroupBy))
				.collect(Collectors.toList());



		query.groupBy(groupBy.toArray(new Expression[]{}));
	}


	private void addSortBy(){

		List<OrderSpecifier<?>> orders =
			internalQueryModel.getOrderingColumns()
				.stream()
				.map(column -> {
					Expression<?> expression = convertToExpression(column, AliasStrategy.REPLACE_BY_ALIAS_IF_POSSIBLE, utils::createAsSortBy);
					return new OrderSpecifier(column.getOrder(), expression);
				})
				.collect(Collectors.toList());

		query.orderBy(orders.toArray(new OrderSpecifier[]{}));

	}



	// ******************** Column to Expression conversion *********************************************

	/**
	 * <p>
	 * 		Turns a column into its querydsl expression counterpart and returns it.
	 *
	 * </p>
	 *
	 * <p>
	 *     	The expression is created by the function parameter columnToExpressionConverter in most cases. However the
	 *     	final result depends on the AliasStrategy :
	 * </p>
	 *
	 * <ul>
	 *     <li>
	 *         	APPEND_ALIAS : we always want an alias. Either retrieve it or generate a new one.
	 * 			The column is converted to an expression and aliased.
	 * 		</li>
	 *     <li>
	 *         	NONE : we never want an alias. The column converted to an Expression but no alias is given.
	 *     </li>
	 *     <li>
	 *			REPLACE_BY_ALIAS_IF_POSSIBLE : if an alias exist for that column reuse it (replace
	 * 			the column expression by the alias), otherwise the column expression is returned as is.
	 *		</li>
	 * </ul>
	 *
	 * @param column
	 * @param aliasStrategy
	 * @param columnToExpressionConverter
	 * @param <C>
	 */
	private <C extends QueryColumnPrototypeInstance>
	Expression<?> convertToExpression(C column,
									  AliasStrategy aliasStrategy,
									  Function<QueryColumnPrototypeInstance, Expression> columnToExpressionConverter){

		Expression<?> expr = null;

		// regular column
		switch(aliasStrategy){

			case APPEND_ALIAS :
				String alias = aliasIndex.generateAlias(column);

				expr = columnToExpressionConverter.apply(column);
				expr = Expressions.as(expr, alias);

				break;


			case NONE :
				expr = columnToExpressionConverter.apply(column);
				break;


			case REPLACE_BY_ALIAS_IF_POSSIBLE :

				Optional<String> maybeAlias =  aliasIndex.getMaybeAlias(column);

				if (maybeAlias.isPresent()){
					expr = Expressions.stringPath(maybeAlias.get());
				}
				else{
					expr = columnToExpressionConverter.apply(column);
				}


				break;


			default:
				break;
		}


		return expr;

	}

	// *********************************** other utils ******************************************************

	private final Expression[] toArray(List<Expression<?>> expressions){
		return expressions.toArray(new Expression[]{});
	}


	// *********************************** internal classes *************************************************
	
	/*
	 * Aliases management. It helps us keeping aliases consistent across select, groupBy and sortBy clauses. 
	 */
	private static final class AliasIndex{
		// see comment on that hack at the top of the class
		private static final String HIBERNATE_ALIAS_PREFIX = "col_x_0_";
		
		private Map<Long, String> aliasByPrototypeId = new HashMap<>();
		private int counter = 0;
		
		// generates an alias and registers it. If the column already had an
		// alias, it is overridden with the new one.
		private String generateAlias(QueryColumnPrototypeInstance instance){
			
			Long protoId = instance.getColumn().getId();
			String value = generate();
			aliasByPrototypeId.put(protoId, value);
			return value;
			
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
