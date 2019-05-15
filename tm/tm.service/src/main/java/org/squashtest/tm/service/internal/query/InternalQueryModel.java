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
import org.squashtest.tm.domain.attachment.QAttachmentContent;
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
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


/**
 *
 * <p>
 *     An InternalQueryModel is yet another layer of refinement of a QueryModel, in the sense that it also knows the context
 *     of use : as  a regular query, or as a subquery within a particular context. This information is driven by the
 *     attribute {@link #queryProfile}. Depending on the profile, the actual columns for projections, aggregation etc
 *     will change to adapt for the context - see documentation on {@link QueryBuilder} for a better understanding
 *     of what is happening.
 * </p>
 *
 * <p>
 *     It also define the rootEntity of the query. The rootEntity only really means something in the context of a subquery,
 *     because it indirectly defines which columns (typically the entity.id) will be used for joining with the outerquery.
 *     It can be either deduced from the columns of the query - by picking the first entity listed among the aggregation columns,
 *     and if not found among the projections columns. Best is to specify it explicitly, either using the methods {@link #withRootEntity(InternalEntityType)}.
 *     The factory method {@link #createFor(QueryColumnPrototypeInstance)} will also deduce the correct rootEntity from the column in argument.
 * </p>
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

	private Set<InternalEntityType> targetEntities;

	private InternalEntityType rootEntity;

	private QueryProfile queryProfile = QueryProfile.REGULAR_QUERY;

	private PerProfileColumnExposer columnExposer = new RegularQueryColumnExposer(new QueryModel());


	// ******************* factories ***********************************

	/**
	 * Creates an InternalQueryModel for the given query. The QueryProfile and rootEntity are left to their default.
	 *
	 * @param queryModel
	 * @return
	 */
	static InternalQueryModel createFor(QueryModel queryModel){
		ConfiguredQuery confQuery = new ConfiguredQuery(queryModel);
		return new InternalQueryModel(confQuery);
	}


	/**
	 * That factory method creates an InternalQueryModel that corresponds to the subquery represented by the
	 * columnInstance in argument. Because it is expected to be a calculated column, the method will throw
	 * an exception if the column is not calculated.
	 *
	 * The returned instance will use as rootEntity the entity type that owns that column (which is usually
	 * what we want).
	 *
	 * @param columnInstance
	 * @return
	 */
	static InternalQueryModel createFor(QueryColumnPrototypeInstance columnInstance){
		QueryColumnPrototype proto = columnInstance.getColumn();

		// check that the column is indeed a calculated column, which means it has a subquery
		if (proto.getColumnType() != ColumnType.CALCULATED){
			throw new RuntimeException("Attempted to create a subquery for a column that has no subquery");
		}

		// create a configured query from the subquery
		ConfiguredQuery configured = new ConfiguredQuery(proto.getSubQuery());

		// define which rootEntity for that column.
		SpecializedEntityType specType = columnInstance.getSpecializedType();
		InternalEntityType internalType = InternalEntityType.fromSpecializedType(specType);

		return new InternalQueryModel(configured).withRootEntity(internalType);
	}

	// *************** constructors ************************************


	// for testing purposes - do not use
	InternalQueryModel(){
		super();
	}


	InternalQueryModel(ConfiguredQuery parent){
		this.parent = parent;
		assignColumnExposer();
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
		this.assignColumnExposer();
		return this;
	}

	// ********* all getters **********************

	List<QueryProjectionColumn> getProjectionColumns(){
		return columnExposer.getProjections();
	}

	List<QueryAggregationColumn> getAggregationColumns(){
		return columnExposer.getAggregations();
	}

	List<QueryFilterColumn> getFilterColumns(){
		return columnExposer.getFilters();
	}

	List<QueryOrderingColumn> getOrderingColumns(){
		return columnExposer.getOrdering();
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


	/**
	 * Returns the rootEntity of that InternalQueryModel. If none was set using withRootEntity, a default
	 * value will be assigned after inspection of available columns.
	 *
	 * @return
	 */
	InternalEntityType getRootEntity() {
		assignRootEntityIfUnspecified();
		return rootEntity;
	}


	Set<InternalEntityType> getTargetEntities() {
		computeTargetEntitiesIfUnspecified();
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
	 * Note : for now this work for subqueries (those really need a root entity) because computeTargetEntitiesIfUnspecified
	 * will put the aggregation column first. Thus the entity of the first aggregation column will be selected,
	 * and that behavior is exactly what we had in the earlier model (ie the first ChartAxis designated the
	 * root entity).
	 *
	 * Still it will be a better practice to specify it using 'withRootEntity' when that information is available.
	 */
	private void assignRootEntityIfUnspecified(){

		if (rootEntity == null) {
			QueryColumnPrototypeInstance firstProto = collectAllColumns().get(0);
			SpecializedEntityType firstSpecialType = firstProto.getSpecializedType();
			InternalEntityType firstType = InternalEntityType.fromSpecializedType(firstSpecialType);

			rootEntity = firstType;
		}
	}



	/**
	 * The target entities are all the entities involved in the query, including the rootEntity (it
	 * may have been forced externally in case of a subquery).
	 */
	protected final void computeTargetEntitiesIfUnspecified(){

		if (targetEntities == null || targetEntities.isEmpty()){

			// the type LinkedHashSet is used because it ensure
			// that elements will be iterated over as we need it (ie, the rootEntity first)
			Set<InternalEntityType> allTypes = new LinkedHashSet<>();

			// since the root entity belongs to the target entities, ensure we have one set.
			assignRootEntityIfUnspecified();

			allTypes.add(rootEntity);

			// the other columns now
			List<QueryColumnPrototypeInstance> allColumns = collectAllColumns();

			Set<InternalEntityType> colTypes = allColumns.stream()
													.map(col -> col.getSpecializedType())
													.map(InternalEntityType::fromSpecializedType)
													.collect(Collectors.toSet());

			// merge the sets
			allTypes.addAll(colTypes);

			targetEntities = allTypes;
		}
	}

	private void assignColumnExposer(){
		switch(queryProfile){
			case REGULAR_QUERY:
				columnExposer = new RegularQueryColumnExposer(parent.getQueryModel()); break;
			case SUBSELECT_QUERY:
				columnExposer = new SubselectQueryColumnExposer(parent.getQueryModel()); break;
			case SUBWHERE_QUERY:
				columnExposer = new SubwehreQueryColumnExposer(parent.getQueryModel()); break;
			default :
				throw new RuntimeException("unsupported QueryProfile : "+queryProfile);
		}
	}


	// ************** support code ***************************

	private Collection<QueryColumnPrototypeInstance> findSubqueriesForStrategy(QueryStrategy strategy){
		Collection<QueryColumnPrototypeInstance> found = collectAllColumns().stream()
													.filter( col -> {
														QueryColumnPrototype proto = col.getColumn();
														return proto.getColumnType() == ColumnType.CALCULATED &&
																   proto.getSubQuery().getStrategy() == strategy;
													}).collect(Collectors.toList());

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






	// ************* PerProfileColumnExposer strategies ****************************

	/**
	 * Will expose all or part of the columns of a QueryModel, depending
	 * on the query profile.
	 */
	private static abstract class PerProfileColumnExposer{
		protected List<QueryProjectionColumn> projections;
		protected List<QueryAggregationColumn> aggregations;
		protected List<QueryFilterColumn> filters;
		protected List<QueryOrderingColumn> ordering;

		public List<QueryProjectionColumn> getProjections() {
			return projections;
		}
		public List<QueryAggregationColumn> getAggregations() {
			return aggregations;
		}
		public List<QueryFilterColumn> getFilters() {
			return filters;
		}
		public List<QueryOrderingColumn> getOrdering() {
			return ordering;
		}
	}


	/**
	 * The RegularQueryColumnExporter exposes the query as is.
	 */
	private static final class RegularQueryColumnExposer extends PerProfileColumnExposer{
		RegularQueryColumnExposer(QueryModel model) {
			projections = new ArrayList<>(model.getProjectionColumns());
			aggregations = new ArrayList<>(model.getAggregationColumns());
			filters = new ArrayList<>(model.getFilterColumns());
			ordering = new ArrayList<>(model.getOrderingColumns());
		}
	}

	/**
	 * When a query is a subselect, it will be joined with the outer query. Thus there is
	 * no need for aggregation. Also ordering is irrelevant, because what matters is the
	 * ordering of the outer query.
	 */
	private static final class SubselectQueryColumnExposer extends PerProfileColumnExposer{
		SubselectQueryColumnExposer(QueryModel model) {
			projections = new ArrayList<>(model.getProjectionColumns());
			filters = new ArrayList<>(model.getFilterColumns());
			aggregations = Collections.emptyList();
			ordering = Collections.emptyList();
		}
	}


	/**
	 * The subwhere query is a query used in the "where" clause of the outer query. As for the
	 * subselect there is no ordering because there is no point to order within a subquery.
	 *
	 * Also, although the projection columns are exposed, in that context they will be used
	 * in a particular way (see SubQueryBuilder).
	 */
	private static final class SubwehreQueryColumnExposer extends PerProfileColumnExposer{
		SubwehreQueryColumnExposer(QueryModel model) {
			projections = new ArrayList<>(model.getProjectionColumns());
			filters = new ArrayList<>(model.getFilterColumns());
			aggregations = new ArrayList<>(model.getAggregationColumns());
			ordering = Collections.emptyList();
		}
	}

}
