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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.hibernate.sql.Alias;
import org.squashtest.tm.domain.jpql.ExtendedHibernateQuery;
import org.squashtest.tm.domain.query.QueryAggregationColumn;
import org.squashtest.tm.domain.query.QueryColumnPrototypeInstance;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import org.squashtest.tm.domain.query.QueryOrderingColumn;
import org.squashtest.tm.domain.query.QueryProjectionColumn;

/**
 * <p>
 * 	This class is responsible for adding the "select" and "group by" clauses. See main documentation on
 * 	{@link QueryProcessingServiceImpl} for more details on how it is done.
 * </p>
 *
 * <p>
 * 	Depending on the {@link QueryProfile} of the query, the projection will be :
 * 	<ul>
 * 		<li>REGULAR_QUERY and SUBSELECT_QUERY : projection are applied normally</li>
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
 * 	comment on {@link ColumnAliasing} on that matter.
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
	
	
	// internal state properties
	private ColumnAliasing columnAliasing;

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


	private void addProjections(){

		QueryProfile profile = internalQueryModel.getQueryProfile();

		List<Expression<?>> projections;

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
			// convert all our columns into selectable expressions. The column is rendered and given an alias, 
			// the alias can then be refered to in the groupBy and sortBy clauses.
			projections =
				columnAliasing.getProjectedColumns()
					.stream()
					.map(AliasedColumn::renderAsAliasedSelect)
					.collect(Collectors.toList());

			break;
		}

		// now stuff the query
		query.select(Projections.tuple(toArray(projections))).distinct();

	}


	private void addGroupBy(){
		List<Expression<?>> groupBy =
			columnAliasing.getGroupedColumns().stream().map(AliasedColumn::renderAsAliasElseAsColumn).collect(Collectors.toList());


		query.groupBy(groupBy.toArray(new Expression[]{}));
	}


	private void addSortBy(){

		Iterator<AliasedColumn> aliasIterator = columnAliasing.getSortedColumns().iterator();

		List<OrderSpecifier> orders =
			internalQueryModel.getOrderingColumns()
				.stream()
				.map(column -> {
					AliasedColumn aliasedColumn = aliasIterator.next();
					Expression<?> expression = aliasedColumn.renderAsAliasElseAsColumn();
					return new OrderSpecifier(column.getOrder(), expression);
				})
				.collect(Collectors.toList());

		query.orderBy(orders.toArray(new OrderSpecifier[]{}));

	}

	// *********************************** other utils ******************************************************

	private final Expression[] toArray(List<Expression<?>> expressions){
		return expressions.toArray(new Expression[]{});
	}


	// *********************************** internal classes *************************************************


	/**
	 * <p>
	 * The purpose of this class is to address a problem with select distinct that is 
	 * sorted on columns with datatable LEVEL_ENUM (see below), by detecting the potentially
	 * problematic situation and resolving the actual projections, aggregation and sorting 
	 * required for this query. ProjectionPlanner consumes the result of this pre-processing
	 * in order to generate the QueryDsl clauses.   
	 * </p>
	 * 
	 <p>
		In the context of a generated query the expression is fetched with .distinct(). 
		It has the desirable effect of eliminating possible duplicates, which can occur 
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
	private static final class ColumnAliasing{


		// see comment on that hack at the top of the class
		private QuerydslToolbox utils;

		// the template is of the form : <hibernate alias template><optional subquery discriminator slug
		private static final String HIBERNATE_STYLE_ALIAS_TEMPLATE = "col_%d_0_%s";
		private int counter = 0;

		private Map<String, String> aliasByColumnLabel = new HashMap<>();
		private Map<String, String> extraAliasByColumnLabel = new HashMap<>();


		// output properties
		private List<AliasedColumn> projectedColumns;

		private List<AliasedColumn> groupedColumns;

		private List<AliasedColumn> sortedColumns;



		private ColumnAliasing(QuerydslToolbox utils, InternalQueryModel queryModel) {

			this.utils = utils;

			// plan the projections. Takes into account the level_enum columns in the groupBy or sortBy clauses
			planProjections(queryModel);

			// plan the aliases for the group by
			planGroupBy(queryModel);

			// plan the aliases for the sort by
			planSortBy(queryModel);

		}

		// empty constructor for testing purposes
		private ColumnAliasing(){
			super();
		}


		// plan all the projections.
		// also, if any level_enum column exists in the groupby/sortby clause,
		// adds it again as a sortable case-when
		private final void planProjections(InternalQueryModel queryModel) {


			projectedColumns = new ArrayList<>();

			// first, add the regular projection columns
			for (QueryProjectionColumn column : queryModel.getProjectionColumns()){
				String alias = registerNewRegularAlias(column);
				AliasedColumn projected =  new AliasedColumn(column, alias, utils::createAsSelect);
				projectedColumns.add(projected);
			}

			// also, plan any level_enum column in sort clause
			List<QueryColumnPrototypeInstance> otherLevelEnumColumns = findSortByLevelEnum(queryModel);

			for (QueryColumnPrototypeInstance column : otherLevelEnumColumns){
				String extraAlias = registerNewExtraProjectionAlias(column);
				AliasedColumn projected = new AliasedColumn(column, extraAlias, utils::createAsCaseWhen);
				projectedColumns.add(projected);
			}

		}

		// Here we want to include the aliases of all the groupby columns.
		// If a column needs to be grouped on but does not appear in the select clause (this is legal in "select 1" subqueries),
		// the column will be grouped on anyway by using the column expression without alias.
		// If a column has a level enum, we also want to check if it has a corresponding extra column
		// we should group on too.
		private final void planGroupBy(InternalQueryModel queryModel) {

			groupedColumns = new ArrayList<>();

			for (QueryAggregationColumn column : queryModel.getAggregationColumns()){

				// regular columns first.
				String alias = retrieveRegularAlias(column);
				groupedColumns.add(new AliasedColumn(column, alias, utils::createAsGroupBy));

				// handle the possible extra column that goes with it
				String extraAlias = retrieveExtraProjectionAlias(column);

				if (isLevelEnum(column) && extraAlias != null) {
					groupedColumns.add(new AliasedColumn(column, extraAlias, utils::createAsGroupBy));
				}
			}
		}


		// Here we collect the aliases of the columns we must sort on.
		// Contrary on the columns in the groupby clause, a column of the sortby clause must be part of the projection too, so they always have an alias
		// If a level_enum column is present, we must use the alias of the corresponding extra column instead.
		private final void planSortBy(InternalQueryModel queryModel) {
			sortedColumns = new ArrayList<>();

			for (QueryOrderingColumn column : queryModel.getOrderingColumns()){

				String effectiveAlias = null;

				// if level enum, sort on the extra column instead
				if (isLevelEnum(column)){
					effectiveAlias = retrieveExtraProjectionAlias(column);
				}
				// else use the regular column alias instead
				else{
					effectiveAlias = retrieveRegularAlias(column);
				}

				sortedColumns.add(new AliasedColumn(column, effectiveAlias, utils::createAsSortBy));
			}

		}


		// ************** utils **************************



		private List<QueryColumnPrototypeInstance> findSortByLevelEnum(InternalQueryModel queryModel){
			return queryModel.getOrderingColumns()
					.stream()
					.filter(this::isLevelEnum)
					.collect(Collectors.toList());
		}


		private boolean isLevelEnum(QueryColumnPrototypeInstance column){
			return column.getDataType().isAssignableToLevelEnum();
		}

		public List<AliasedColumn> getProjectedColumns() {
			return projectedColumns;
		}

		public List<AliasedColumn> getGroupedColumns() {
			return groupedColumns;
		}

		public List<AliasedColumn> getSortedColumns() {
			return sortedColumns;
		}

		// *************** Alias management **************************

		// XXX : in case a column was already registered, it won't be registered twice
		// the engine doesn't support columns appearing twice in a clause (in particular
		// the projection clause)
		// By design it's not supposed to happen, with one exception : a PieChart may
		// select an id and count on it too. It means nothing but spec is the spec.
		// In that situation, we return null : the column has no alias and it's fine.
		// If the projection columns are iterated on in the correct order we should be fine.

		// XXX2 : another way to "fix" it would be to include the operation in the key,
		// in fact this would be more powerful
		private String registerNewRegularAlias(QueryColumnPrototypeInstance instance){
			String alias = null;
			String label = instance.getColumn().getLabel();
			if (! aliasByColumnLabel.containsKey(label)) {
				alias = generate();
				aliasByColumnLabel.put(label, alias);
			}
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
			int cnt = counter++;
			String ctxt = utils.getSubContext();
			// check for nullity
			ctxt = (ctxt != null) ? ctxt : "";

			String alias = String.format(HIBERNATE_STYLE_ALIAS_TEMPLATE, cnt, ctxt);

			return alias;
		}


	}


	private static final class AliasedColumn{

		private QueryColumnPrototypeInstance columnInstance;

		private String alias;

		private Function<QueryColumnPrototypeInstance, Expression<?>> renderingFunction;


		public AliasedColumn(QueryColumnPrototypeInstance columnInstance, String alias, Function<QueryColumnPrototypeInstance, Expression<?>> renderingFunction) {
			this.columnInstance = columnInstance;
			this.alias = alias;
			this.renderingFunction = renderingFunction;
		}


		// about the nullcheck : see comment on #registerNewRegularAlias
		public Expression<?> renderAsAliasedSelect(){
			Expression<?> rendered = renderingFunction.apply(columnInstance);
			if (alias != null) {
				rendered = Expressions.as(rendered, alias);
			}
			return rendered;
		}

		public Expression<?> renderAsAliasElseAsColumn(){
			if (alias != null){
				return Expressions.stringPath(alias);
			}
			else{
				return renderingFunction.apply(columnInstance);
			}
		}

	}

}
