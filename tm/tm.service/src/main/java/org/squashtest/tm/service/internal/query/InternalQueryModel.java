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

import org.springframework.data.domain.Pageable;
import org.squashtest.tm.domain.EntityReference;
import org.squashtest.tm.domain.query.NaturalJoinStyle;
import org.squashtest.tm.domain.query.QueryColumnPrototypeInstance;
import org.squashtest.tm.domain.query.ColumnType;
import org.squashtest.tm.domain.query.QueryAggregationColumn;
import org.squashtest.tm.domain.query.QueryColumnPrototype;
import org.squashtest.tm.domain.query.QueryFilterColumn;
import org.squashtest.tm.domain.query.QueryModel;
import org.squashtest.tm.domain.query.QueryOrderingColumn;
import org.squashtest.tm.domain.query.QueryProjectionColumn;
import org.squashtest.tm.domain.query.QueryStrategy;
import org.squashtest.tm.domain.query.SpecializedEntityType;
import org.squashtest.tm.service.query.ConfiguredQuery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


/**
 * <p>
 * An InternalQueryModel knows more about the context of use, specifically if that
 * query is used as a MAIN or as a SUBQUERY, and in the latter case whether it will be
 * a subselect or a sub where clause. It has an impact on which columns will be really
 * used for aggregation, projections etc.
 * </p>
 *
 * <p>
 *     Building an InternalQueryModel is a two part process. First you need to
 *     call the constructor, as usual, and optionally configure the queryprofile and
 *     the rootEntity (see methods withRootEntity and withProfile). Then, you
 *     must invoke #configure() to finalize the creation of your instance.
 * </p>
 *
 *
 *
 * see javadoc on {@link QueryProcessingServiceImpl}
 * see javadoc on {@link QueryBuilder}
 *
 *
 * @author bsiri
 *
 */
class InternalQueryModel {

	private ConfiguredQuery parent;

	private List<InternalEntityType> targetEntities;

	private InternalEntityType rootEntity;

	private QueryProfile queryProfile = QueryProfile.MAIN_QUERY;

	// the following properties are deduced from the others once #configure() is
	// invoked. An NPE when calling them is a sign that #configure() has not been called.
	private List<QueryProjectionColumn> projections;
	private List<QueryAggregationColumn> aggregations;
	private List<QueryFilterColumn> filters;
	private List<QueryOrderingColumn> ordering;

	// ******************* factories ***********************************

	static InternalQueryModel createFor(QueryModel queryModel){
		ConfiguredQuery confQuery = new ConfiguredQuery(queryModel);
		return new InternalQueryModel(confQuery);
	}

	static InternalQueryModel createFor(QueryColumnPrototypeInstance columnInstance){
		QueryColumnPrototype proto = columnInstance.getColumn();
		if (proto.getColumnType() != ColumnType.CALCULATED){
			throw new RuntimeException("Attempted to create a subquery for a column that has no subquery");
		}
		return createFor(proto.getSubQuery());
	}

	// *************** constructors ************************************


	// for testing purposes - do not use
	InternalQueryModel(){
		super();
	}


	InternalQueryModel(ConfiguredQuery parent){

		this.parent = parent;
	}

	/**
	 * Forces the rootEntity to the given entity. This is used for subqueries, which need
	 * to define which entity needs to be joined on with the main query.
	 *
	 * @param rootEntity
	 * @return
	 */
	InternalQueryModel withRootEntity(InternalEntityType rootEntity){
		this.rootEntity = rootEntity;
		return this;
	}

	InternalQueryModel withProfile(QueryProfile profile){
		this.queryProfile = profile;
		return this;
	}

	InternalQueryModel configure(){

		// init the root entity if the method withRootEntity wasn't invoked.
		assignRootEntityIfUnspecified();

		// find all the target entities
		computeTargetEntities();

		// finally initialize the columns
		computeColumns();

		return this;

	}
	// ********* all getters **********************

	List<QueryProjectionColumn> getProjectionColumns(){
		return projections;
	}

	List<QueryAggregationColumn> getAggregationColumns(){
		return aggregations;
	}

	List<QueryFilterColumn> getFilterColumns(){
		return filters;
	}

	List<QueryOrderingColumn> getOrderingColumns(){
		return ordering;
	}

	NaturalJoinStyle getJoinStyle(){
		return parent.getQueryModel().getJoinStyle();
	}

	QueryStrategy getStrategy(){
		return parent.getQueryModel().getStrategy();
	}

	Collection<EntityReference> getScope(){
		return parent.getScope();
	}

	InternalEntityType getRootEntity() {
		return rootEntity;
	}


	List<InternalEntityType> getTargetEntities() {
		return targetEntities;
	}

	Pageable getPaging(){
		return parent.getPaging();
	}

	QueryProfile getQueryProfile(){
		return queryProfile;
	}

	Collection<? extends QueryColumnPrototypeInstance> getInlinedColumns(){
		return findSubqueriesForStrategy(QueryStrategy.INLINED);

	}

	Collection<? extends QueryColumnPrototypeInstance> getSubqueryColumns(){
		return findSubqueriesForStrategy(QueryStrategy.SUBQUERY);

	}


	// ********************** init code ********************************
	/*
	 * Assigns a rootEntity if it wasn't specified yet.
	 *
	 * Note : for now this work for subqueries (those really need a root entity) because computeTargetEntities
	 * will put the aggregation column first. Thus the entity of the first aggregation column will be selected,
	 * and that behavior is exactly what we had in the earlier model (ie the first ChartAxis designated the
	 * root entity).
	 *
	 * Still it will be a better practice to specify it using 'withRootEntity' when that information is available.
	 */
	private void assignRootEntityIfUnspecified(){

		if (rootEntity == null) {
			QueryColumnPrototypeInstance firstProto = collectAllColumns().get(0);
			SpecializedEntityType firstSpecialType = firstProto.getColumn().getSpecializedType();
			InternalEntityType firstType = InternalEntityType.fromSpecializedType(firstSpecialType);

			rootEntity = firstType;
		}
	}



	/**
	 * The target entities are all the entities involved in the query, including the rootEntity (it
	 * may have been forced externally in case of a subquery).
	 */
	protected final void computeTargetEntities(){

		List<QueryColumnPrototypeInstance> allColumns = collectAllColumns();

		List<InternalEntityType> allTypes = allColumns.stream()
											  .map(col -> col.getColumn().getSpecializedType())
											  .map(InternalEntityType::fromSpecializedType)
											  .collect(Collectors.toList());

		allTypes.add(0, rootEntity);

		targetEntities = allTypes.stream().distinct().collect(Collectors.toList());

	}

	/**
	 * Initialize the projections, aggregation, filtering and ordering columns. Their content largely depends on the
	 * QueryProfile.
	 *
	 *
	 */
	private void computeColumns(){
		QueryModel model = parent.getQueryModel();

		projections = new ArrayList<>();
		aggregations = new ArrayList<>();
		filters = new ArrayList<>();
		ordering = new ArrayList<>();

		switch(queryProfile){
			// the main query is the simplest of all, just copy the columns as-is
			case MAIN_QUERY:
				projections.addAll(model.getProjectionColumns());
				aggregations.addAll(model.getAggregationColumns());
				filters.addAll(model.getFilterColumns());
				ordering.addAll(model.getOrderingColumns());
				break;

			/*
			In the case of a subselect query, there is no group-by nor order-by. No grouping is necessary because
			they will be correlated subqueries, and no sorting because it's irrelevant : only the main query needs
			to be ordered.
			 */
			case SUBSELECT_QUERY:
				projections.addAll(model.getProjectionColumns());
				filters.addAll(model.getFilterColumns());
				// the aggregations and ordering are left empty
				break;

			/*
				In the case of a subwhere query, there is no ordering for the same reasons as the subselect.
				However, although there are projections and aggregations, the query will still be handled specially
				by he ProjectionPlanner and the SubQueryBuilder, in order to achieve the desired form
				described in the documentation of QueryBuilder.
			 */
			case SUBWHERE_QUERY:
				projections.addAll(model.getProjectionColumns());
				filters.addAll(model.getFilterColumns());
				aggregations.addAll(model.getAggregationColumns());
				break;

			default : throw new RuntimeException("QueryProfile '"+queryProfile+"' is not yet supported.");

		}

	}


	// ************** support code ***************************

	private Collection<QueryColumnPrototypeInstance> findSubqueriesForStrategy(QueryStrategy strategy){
		Collection<QueryColumnPrototypeInstance> found = collectAllColumns().stream()
													.filter( col -> {
														QueryColumnPrototype proto = col.getColumn();
														return proto.getColumnType() == ColumnType.CALCULATED &&
																   proto.getSubQuery().getStrategy() == strategy;
													}).collect(Collectors.toSet());

		return found;
	}


	/**
	 * Returns all the columns, as QueryColumnPrototypeInstances.
	 * @return
	 */
	List<QueryColumnPrototypeInstance> collectAllColumns(){
		QueryModel query = parent.getQueryModel();

		List<QueryColumnPrototypeInstance> allColumns = new ArrayList<>();

		/*
		 * Note : the order in which those are listed is important, see
		 * note on #assignRootEntityIfUnspecified().
		 */
		allColumns.addAll(query.getAggregationColumns());
		allColumns.addAll(query.getProjectionColumns());
		allColumns.addAll(query.getOrderingColumns());
		allColumns.addAll(query.getFilterColumns());

		return allColumns;
	}
}
