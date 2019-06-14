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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.squashtest.tm.domain.EntityType;
import org.squashtest.tm.domain.jpql.ExtendedHibernateQuery;
import org.squashtest.tm.domain.query.DataType;
import org.squashtest.tm.domain.query.Operation;
import org.squashtest.tm.domain.query.QueryAggregationColumn;
import org.squashtest.tm.domain.query.QueryColumnPrototype;
import org.squashtest.tm.domain.query.QueryColumnPrototypeInstance;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import org.squashtest.tm.domain.query.QueryOrderingColumn;
import org.squashtest.tm.domain.query.QueryProjectionColumn;
import org.squashtest.tm.domain.query.SpecializedEntityType;

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
	//private AliasIndex aliasIndex = new AliasIndex();

	private ColumnAliasing columnAliasing;


	/*
	 * The documentation for that enum will be given in the context of use, see #populateClauses()
	 */
	/*
	private enum AliasStrategy {
		NONE,
		APPEND_ALIAS,
		REPLACE_BY_ALIAS_IF_POSSIBLE;
	}
	*/

	ProjectionPlanner(InternalQueryModel internalQueryModel, ExtendedHibernateQuery<?> query){
		super();
		this.internalQueryModel = internalQueryModel;
		this.query = query;
		this.utils = new QuerydslToolbox();
		this.columnAliasing = new ColumnAliasing(this.utils, this.internalQueryModel);
	}

	ProjectionPlanner(InternalQueryModel definition, ExtendedHibernateQuery<?> query, QuerydslToolbox utils){
		super();
		this.internalQueryModel = definition;
		this.query = query;
		this.utils = utils;
		this.columnAliasing = new ColumnAliasing(this.utils, this.internalQueryModel);
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
				columnAliasing.getProjectedColumns()
					.stream()
					.map(ProjectedColumn::renderAsSelect)
					.collect(Collectors.toList());





			break;
		}

		// now stuff the query
		query.select(Projections.tuple(toArray(projections))).distinct();

	}


	private void addGroupBy(){
		List<Expression<?>> groupBy =
			columnAliasing.getGroupByAliases().stream().map(Expressions::stringPath).collect(Collectors.toList());


		query.groupBy(groupBy.toArray(new Expression[]{}));
	}


	private void addSortBy(){

		Iterator<String> aliasIterator = columnAliasing.getSortByAliases().iterator();

		List<OrderSpecifier<?>> orders =
			internalQueryModel.getOrderingColumns()
				.stream()
				.map(column -> {
					String alias = aliasIterator.next();
					Expression<?> expression = Expressions.stringPath(alias);
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
	 *
	 */
	/*
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
*/
	// *********************************** other utils ******************************************************

	private final Expression[] toArray(List<Expression<?>> expressions){
		return expressions.toArray(new Expression[]{});
	}


	// *********************************** internal classes *************************************************
	
	/*
	 * Aliases management. It helps us keeping aliases consistent across select, groupBy and sortBy clauses. 
	 */
	/*
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
*/

	/**
	 * This internal class accounts for the possible mess occurring
	 * when a level_enum column appears in the group by or sort by clause
	 * (see {@link #addProjections()})
	 *
	 */
	private static final class ColumnAliasing{


		// see comment on that hack at the top of the class
		private static final String HIBERNATE_ALIAS_PREFIX = "col_x_0_";
		private static final String SUFFIX = "_sortkey";
		private int counter = 0;

		private Map<String, String> aliasByColumnLabel = new HashMap<>();
		private Map<String, String> extraAliasByColumnLabel = new HashMap();


		// output properties
		private List<ProjectedColumn> projectedColumns;

		private List<String> groupByAliases;

		private List<String> sortByAliases;



		private ColumnAliasing(QuerydslToolbox utils, InternalQueryModel queryModel) {

			// plan the projections. Takes into account the level_enum columns in the groupBy or sortBy clauses
			planProjections(utils, queryModel);

			// plan the aliases for the group by
			planGroupBy(utils, queryModel);

			// plan the aliases for the sort by
			planSortBy(utils, queryModel);

		}


		// plan all the projections.
		// also, if any level_enum column exists in the groupby/sortby clause,
		// adds it again as a sortable case-when
		private final void planProjections(QuerydslToolbox utils, InternalQueryModel queryModel) {


			projectedColumns = new ArrayList<>();

			// first, add the regular projection columns
			for (QueryProjectionColumn column : queryModel.getProjectionColumns()){
				String alias = registerNewRegularAlias(column);
				ProjectedColumn projected =  new ProjectedColumn(column, alias, utils::createAsSelect);
				projectedColumns.add(projected);
			}

			// also, plan any level_enum column in sort clause
			List<QueryColumnPrototypeInstance> otherLevelEnumColumns = findSortByLevelEnum(queryModel);

			for (QueryColumnPrototypeInstance column : otherLevelEnumColumns){
				String extraAlias = registerNewExtraProjectionAlias(column);
				ProjectedColumn projected = new ProjectedColumn(column, extraAlias, utils::createAsCaseWhen);
				projectedColumns.add(projected);
			}

		}

		// here we want to include the aliases of all the groupby columns.
		// if a column has a level enum, we also want to check if it has a corresponding extra column
		// we should group on too.
		private final void planGroupBy(QuerydslToolbox utils, InternalQueryModel queryModel) {

			groupByAliases = new ArrayList<>();

			for (QueryAggregationColumn column : queryModel.getAggregationColumns()){

				String alias = retrieveRegularAlias(column);
				String extraAlias = retrieveExtraProjectionAlias(column);

				groupByAliases.add(alias);

				if (isLevelEnum(column) && extraAlias != null) {
					groupByAliases.add(extraAlias);
				}
			}
		}


		// here we collect the aliases of the columns we must sort on.
		// if a level_enum column is present, we must use the alias of the corresponding extra column instead.
		private final void planSortBy(QuerydslToolbox utils, InternalQueryModel queryModel) {
			sortByAliases = new ArrayList<>();

			for (QueryOrderingColumn column : queryModel.getOrderingColumns()){

				// if level enum, sort on the extra column instead
				if (isLevelEnum(column)){
					String extraAlias = retrieveExtraProjectionAlias(column);
					sortByAliases.add(extraAlias);
				}
				// else use the regular alias
				else{
					String regularAlias = retrieveRegularAlias(column);
					sortByAliases.add(regularAlias);
				}
			}

		}


		// ************** utils **************************


		// find all columns of type level_enum in the groupby or sortby clauses
		/*
		private List<QueryColumnPrototypeInstance> findGroupByOrOrderBylevelEnumColumns(InternalQueryModel queryModel){

			List<QueryColumnPrototypeInstance> extraInstances = findExtraProjections(queryModel.getAggregationColumns());
			extraInstances.addAll(findExtraProjections(queryModel.getOrderingColumns()));

			// remove the duplicates, by book-keeping the column
			Set<Long> uniqueIds = new HashSet<>();

			return extraInstances.stream().filter( column -> {
				Long colId = column.getColumn().getId();
				if (uniqueIds.contains(colId)){
					return false;
				}
				else{
					uniqueIds.add(colId);
					return true;
				}
			}).collect(Collectors.toList());
		}*/


		private List<QueryColumnPrototypeInstance> findSortByLevelEnum(InternalQueryModel queryModel){
			return queryModel.getOrderingColumns()
					.stream()
					.filter(this::isLevelEnum)
					.collect(Collectors.toList());
		}

		private List<QueryColumnPrototypeInstance> findExtraProjections(List<? extends QueryColumnPrototypeInstance> instances){
			return instances.stream().filter(this::isLevelEnum).collect(Collectors.toList());
		}

		private boolean isLevelEnum(QueryColumnPrototypeInstance column){
			return column.getDataType() == DataType.LEVEL_ENUM;
		}

		public List<ProjectedColumn> getProjectedColumns() {
			return projectedColumns;
		}

		public List<String> getGroupByAliases() {
			return groupByAliases;
		}

		public List<String> getSortByAliases() {
			return sortByAliases;
		}

		// *************** Alias management **************************

		private String registerNewRegularAlias(QueryColumnPrototypeInstance instance){
			String label = instance.getColumn().getLabel();
			String alias = generate();

			aliasByColumnLabel.put(label, alias);
			return alias;
		}

		private String registerNewExtraProjectionAlias(QueryColumnPrototypeInstance instance){
			String label = instance.getColumn().getLabel();
			String extraAlias = generate();

			extraAliasByColumnLabel.put(label, extraAlias);

			return extraAlias;
		}

		private String retrieveRegularAlias(QueryColumnPrototypeInstance instance){
			String label = instance.getColumn().getLabel();
			return aliasByColumnLabel.get(label);
		}

		private String retrieveExtraProjectionAlias(QueryColumnPrototypeInstance instance){
			String label = instance.getColumn().getLabel();
			return extraAliasByColumnLabel.get(label);
		}



		// internal usage only
		private String generate(){
			return HIBERNATE_ALIAS_PREFIX.replace("x", String.valueOf(counter++));
		}





	}


	private static final class ProjectedColumn{

		private QueryColumnPrototypeInstance columnInstance;

		private String alias;

		private Function<QueryColumnPrototypeInstance, Expression<?>> renderingFunction;


		public ProjectedColumn(QueryColumnPrototypeInstance columnInstance, String alias, Function<QueryColumnPrototypeInstance, Expression<?>> renderingFunction) {
			this.columnInstance = columnInstance;
			this.alias = alias;
			this.renderingFunction = renderingFunction;
		}


		public Expression<?> renderAsSelect(){
			Expression<?> rendered = renderingFunction.apply(columnInstance);
			Expression<?> renderedAliased = Expressions.as(rendered, alias);
			return renderedAliased;
		}

		public QueryColumnPrototypeInstance getColumnInstance() {
			return columnInstance;
		}

		public String getAlias() {
			return alias;
		}
	}

}
