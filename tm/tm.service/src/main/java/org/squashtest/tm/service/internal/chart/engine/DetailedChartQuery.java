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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.squashtest.tm.domain.chart.*;


/**
 * see javadoc on {@link ChartDataFinder}
 *
 * @author bsiri
 *
 */
class DetailedChartQuery extends ChartQuery{

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
	DetailedChartQuery(IChartQuery parent){

		super();

		getAxis().addAll(parent.getAxis());

		getFilters().addAll(parent.getFilters());

		getMeasures().addAll(parent.getMeasures());

		setJoinStyle(parent.getJoinStyle());

		setStrategy(parent.getStrategy());

		// find the root entity
		rootEntity = InternalEntityType.fromSpecializedType(parent.getAxis().get(0).getSpecializedType());

		// find the measured Entity
		measuredEntity = InternalEntityType.fromSpecializedType(parent.getMeasures().get(0).getSpecializedType());

		// find all the target entities
		computeTargetEntities();

	}

	/**
	 * Constructor that will build a DetailedChartQuery for the subquery of the given column
	 *
	 * @param column
	 */
	DetailedChartQuery(ColumnPrototypeInstance column){
		this(column.getColumn().getSubQuery());
	}


	Collection<? extends ColumnPrototypeInstance> getInlinedColumns(){
		return findSubqueriesForStrategy(new PerStrategyColumnFinder(QueryStrategy.INLINED));

	}

	Collection<? extends ColumnPrototypeInstance> getSubqueryColumns(){
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
	public void setMeasures(List<MeasureColumn> measures){
		getMeasures().addAll(measures);
	}

	@Override
	public void setAxis(List<AxisColumn> axes){
		getAxis().addAll(axes);
	}

	@Override
	public void setFilters(List<Filter> filters){
		getFilters().addAll(filters);
	}


	private Collection<ColumnPrototypeInstance> findSubqueriesForStrategy(PerStrategyColumnFinder finder){
		Collection<ColumnPrototypeInstance> found = new ArrayList<>();

		Collection<? extends ColumnPrototypeInstance> measures = new ArrayList<>(getMeasures());
		CollectionUtils.filter(measures, finder);
		found.addAll(measures);

		Collection<? extends ColumnPrototypeInstance> axes = new ArrayList<>(getAxis());
		CollectionUtils.filter(axes, finder);
		found.addAll(axes);

		Collection<? extends ColumnPrototypeInstance> filters = new ArrayList<>(getFilters());
		CollectionUtils.filter(filters, finder);
		found.addAll(filters);

		return found;
	}

	private static final class PerStrategyColumnFinder implements Predicate{
		private QueryStrategy strategy;

		private PerStrategyColumnFinder(QueryStrategy strategy){
			this.strategy = strategy;
		}

		@Override
		public boolean evaluate(Object col) {
			ColumnPrototype proto = ((ColumnPrototypeInstance)col).getColumn();
			return proto.getColumnType() == ColumnType.CALCULATED &&
            proto.getSubQuery().getStrategy() == strategy;
		}

	}

}
