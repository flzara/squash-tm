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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.squashtest.tm.domain.query.QueryColumnPrototypeInstance;
import org.squashtest.tm.domain.query.ColumnRole;
import org.squashtest.tm.domain.query.ColumnType;
import org.squashtest.tm.domain.query.IQueryModel;
import org.squashtest.tm.domain.query.QueryAggregationColumn;
import org.squashtest.tm.domain.query.QueryColumnPrototype;
import org.squashtest.tm.domain.query.QueryFilterColumn;
import org.squashtest.tm.domain.query.QueryModel;
import org.squashtest.tm.domain.query.QueryOrderingColumn;
import org.squashtest.tm.domain.query.QueryProjectionColumn;
import org.squashtest.tm.domain.query.QueryStrategy;
import org.squashtest.tm.domain.query.SpecializedEntityType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * see javadoc on {@link ChartDataFinder}
 *
 * @author bsiri
 *
 */
class DetailedChartQuery extends QueryModel {

	private InternalEntityType rootEntity;

	// used when DomainGraph#reverse is true, see documentation on this property for details
	private InternalEntityType measuredEntity;

	private Set<InternalEntityType> targetEntities;


	// for testing purposes - do not use
	DetailedChartQuery(){
		super();
	}


	/**
	 * Constructor for a given chartquery
	 *
	 * @param parent
	 */
	DetailedChartQuery(IQueryModel parent){

		super();

		getAggregationColumns().addAll(parent.getAggregationColumns());

		getFilterColumns().addAll(parent.getFilterColumns());

		getProjectionColumns().addAll(parent.getProjectionColumns());

		getOrderingColumns().addAll(parent.getOrderingColumns());

		setJoinStyle(parent.getJoinStyle());

		setStrategy(parent.getStrategy());

		// TODO : this is no longer valid (there may be no aggregation for instance)
		// find the root entity
		rootEntity = InternalEntityType.fromSpecializedType(parent.getAggregationColumns().get(0).getColumn().getSpecializedType());

		// find the measured Entity
		measuredEntity = InternalEntityType.fromSpecializedType(parent.getProjectionColumns().get(0).getSpecializedType());

		// find all the target entities
		computeTargetEntities();

	}

	/**
	 * Constructor that will build a DetailedChartQuery for the subquery of the given column
	 *
	 * @param column
	 */
	DetailedChartQuery(QueryColumnPrototypeInstance column){
		this(column.getColumn().getSubQuery());
	}


	Collection<? extends QueryColumnPrototypeInstance> getInlinedColumns(){
		return findSubqueriesForStrategy(new PerStrategyColumnFinder(QueryStrategy.INLINED));

	}

	Collection<? extends QueryColumnPrototypeInstance> getSubqueryColumns(){
		return findSubqueriesForStrategy(new PerStrategyColumnFinder(QueryStrategy.SUBQUERY));

	}

	protected final void computeTargetEntities(){
		Map<ColumnRole, Set<SpecializedEntityType>> entitiesByRole = getInvolvedEntities();

		targetEntities = new HashSet<>();

		for (Set<SpecializedEntityType> types : entitiesByRole.values()){
			for (SpecializedEntityType type : types){
				targetEntities.add(InternalEntityType.fromSpecializedType(type));
			}
		}

	}

	protected InternalEntityType getRootEntity() {
		return rootEntity;
	}

	protected InternalEntityType getMeasuredEntity(){
		return measuredEntity;
	}

	protected void setRootEntity(InternalEntityType rootEntity) {
		this.rootEntity = rootEntity;
	}


	protected Set<InternalEntityType> getTargetEntities() {
		return targetEntities;
	}


	protected void setTargetEntities(Set<InternalEntityType> targetEntities) {
		this.targetEntities = targetEntities;
	}


	@Override
	public void setAggregationColumns(List<QueryAggregationColumn> aggregationColumns) {
		getAggregationColumns().addAll(aggregationColumns);
	}

	@Override
	public void setFilterColumns(List<QueryFilterColumn> filterColumns) {
		getFilterColumns().addAll(filterColumns);
	}

	@Override
	public void setProjectionColumns(List<QueryProjectionColumn> projectionColumns) {
		getProjectionColumns().addAll(projectionColumns);
	}

	@Override
	public void setOrderingColumns(List<QueryOrderingColumn> orderingColumns) {
		getOrderingColumns().addAll(orderingColumns);
	}

	private Collection<QueryColumnPrototypeInstance> findSubqueriesForStrategy(PerStrategyColumnFinder finder){
		Collection<QueryColumnPrototypeInstance> found = new ArrayList<>();

		Collection<? extends QueryColumnPrototypeInstance> projection = new ArrayList<>(getProjectionColumns());
		CollectionUtils.filter(projection, finder);
		found.addAll(projection);

		Collection<? extends QueryColumnPrototypeInstance> aggregation = new ArrayList<>(getAggregationColumns());
		CollectionUtils.filter(aggregation, finder);
		found.addAll(aggregation);

		Collection<? extends QueryColumnPrototypeInstance> filters = new ArrayList<>(getFilterColumns());
		CollectionUtils.filter(filters, finder);
		found.addAll(filters);

		Collection<? extends QueryColumnPrototypeInstance> ordering = new ArrayList<>(getOrderingColumns());
		CollectionUtils.filter(ordering, finder);
		found.addAll(ordering);

		return found;
	}

	private static final class PerStrategyColumnFinder implements Predicate{
		private QueryStrategy strategy;

		private PerStrategyColumnFinder(QueryStrategy strategy){
			this.strategy = strategy;
		}

		@Override
		public boolean evaluate(Object col) {
			QueryColumnPrototype proto = ((QueryColumnPrototypeInstance)col).getColumn();
			return proto.getColumnType() == ColumnType.CALCULATED &&
            proto.getSubQuery().getStrategy() == strategy;
		}

	}

}
