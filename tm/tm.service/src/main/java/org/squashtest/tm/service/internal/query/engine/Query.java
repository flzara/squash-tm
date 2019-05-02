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
package org.squashtest.tm.service.internal.query.engine;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.squashtest.tm.domain.chart.ColumnPrototype;
import org.squashtest.tm.domain.chart.ColumnPrototypeInstance;
import org.squashtest.tm.domain.chart.ColumnType;
import org.squashtest.tm.domain.chart.NaturalJoinStyle;
import org.squashtest.tm.domain.chart.QueryStrategy;
import org.squashtest.tm.domain.query.Aggregate;
import org.squashtest.tm.domain.query.Filter;
import org.squashtest.tm.domain.query.Order;
import org.squashtest.tm.domain.query.Projection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class Query {

	private QueryStrategy strategy = QueryStrategy.MAIN;

	private NaturalJoinStyle joinStyle = NaturalJoinStyle.INNER_JOIN;

	private InternalEntityType rootEntity;

	private InternalEntityType targetEntity;

	private Set<InternalEntityType> targetEntities;

	private List<Projection> projectionQueries = new ArrayList<>();

	private List<Filter> filterQueries = new ArrayList<>();

	private List<Aggregate> aggregateQueries = new ArrayList<>();

	private List<Order> orderQueries = new ArrayList<>();

	public Query() {
	}

	public Query(ColumnPrototypeInstance columnPrototypeInstance) {
		Query query = new ChartQueryToQueryTransformer(columnPrototypeInstance.getColumn().getSubQuery()).transformToQuery();
		this.rootEntity = query.getRootEntity();
		this.targetEntity = query.getTargetEntity();
		this.targetEntities = query.getTargetEntities();
		this.projectionQueries = query.getProjectionQueries();
		this.filterQueries = query.getFilterQueries();
		this.aggregateQueries = query.getAggregateQueries();
		this.orderQueries = query.getOrderQueries();
		this.strategy = query.getStrategy();
		this.joinStyle = query.getJoinStyle();
	}

	public Collection<? extends ColumnPrototypeInstance> getInlinedColumns(){
		return findSubqueriesForStrategy(new PerStrategyColumnFinder(QueryStrategy.INLINED));

	}

	public Collection<? extends ColumnPrototypeInstance> getSubqueryColumns(){
		return findSubqueriesForStrategy(new PerStrategyColumnFinder(QueryStrategy.SUBQUERY));

	}

	public List<Projection> getProjectionQueries() {
		return projectionQueries;
	}

	public void setProjectionQueries(List<Projection> projectionQueries) {
		this.projectionQueries = projectionQueries;
	}

	public List<Filter> getFilterQueries() {
		return filterQueries;
	}

	public void setFilterQueries(List<Filter> filterQueries) {
		this.filterQueries = filterQueries;
	}

	public List<Aggregate> getAggregateQueries() {
		return aggregateQueries;
	}

	public void setAggregateQueries(List<Aggregate> aggregateQueries) {
		this.aggregateQueries = aggregateQueries;
	}

	public List<Order> getOrderQueries() {
		return orderQueries;
	}

	public void setOrderQueries(List<Order> orderQueries) {
		this.orderQueries = orderQueries;
	}

	public InternalEntityType getRootEntity() {
		return rootEntity;
	}

	public void setRootEntity(InternalEntityType rootEntity) {
		this.rootEntity = rootEntity;
	}

	public InternalEntityType getTargetEntity() {
		return targetEntity;
	}

	public void setTargetEntity(InternalEntityType targetEntity) {
		this.targetEntity = targetEntity;
	}

	public Set<InternalEntityType> getTargetEntities() {
		return targetEntities;
	}

	public void setTargetEntities(Set<InternalEntityType> targetEntities) {
		this.targetEntities = targetEntities;
	}

	public QueryStrategy getStrategy() {
		return strategy;
	}

	public void setStrategy(QueryStrategy strategy) {
		this.strategy = strategy;
	}

	public NaturalJoinStyle getJoinStyle() {
		return joinStyle;
	}

	public void setJoinStyle(NaturalJoinStyle joinStyle) {
		this.joinStyle = joinStyle;
	}

	private Collection<ColumnPrototypeInstance> findSubqueriesForStrategy(PerStrategyColumnFinder finder){
		Collection<ColumnPrototypeInstance> found = new ArrayList<>();

		Collection<? extends ColumnPrototypeInstance> projections = new ArrayList<>(getProjectionQueries());
		CollectionUtils.filter(projections, finder);
		found.addAll(projections);

		Collection<? extends ColumnPrototypeInstance> aggregates = new ArrayList<>(getAggregateQueries());
		CollectionUtils.filter(aggregates, finder);
		found.addAll(aggregates);

		Collection<? extends ColumnPrototypeInstance> filters = new ArrayList<>(getFilterQueries());
		CollectionUtils.filter(filters, finder);
		found.addAll(filters);

		return found;
	}

	private static final class PerStrategyColumnFinder implements Predicate {
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
