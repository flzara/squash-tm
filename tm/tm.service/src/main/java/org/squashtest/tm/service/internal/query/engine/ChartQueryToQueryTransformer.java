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

import org.squashtest.tm.domain.chart.AxisColumn;
import org.squashtest.tm.domain.chart.ChartQuery;
import org.squashtest.tm.domain.chart.ColumnRole;
import org.squashtest.tm.domain.chart.MeasureColumn;
import org.squashtest.tm.domain.chart.SpecializedEntityType;
import org.squashtest.tm.domain.query.Aggregate;
import org.squashtest.tm.domain.query.AggregateQuery;
import org.squashtest.tm.domain.query.Filter;
import org.squashtest.tm.domain.query.FilterQuery;
import org.squashtest.tm.domain.query.Order;
import org.squashtest.tm.domain.query.OrderQuery;
import org.squashtest.tm.domain.query.Projection;
import org.squashtest.tm.domain.query.ProjectionQuery;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ChartQueryToQueryTransformer {

	private ChartQuery chartQuery;

	public ChartQueryToQueryTransformer(ChartQuery chartQuery) {
		this.chartQuery = chartQuery;
	}

	public Query transformToQuery() {

		Query query = new Query();
		query.setRootEntity(InternalEntityType.fromSpecializedType(chartQuery.getAxis().get(0).getSpecializedType()));
		query.setTargetEntity(InternalEntityType.fromSpecializedType(chartQuery.getMeasures().get(0).getSpecializedType()));
		query.setTargetEntities(computeTargetEntities());
		query.setJoinStyle(chartQuery.getJoinStyle());
		query.setStrategy(chartQuery.getStrategy());

		List<Aggregate> aggregates = chartQuery.getAxis().stream().map(this::axisColumnToAggregate).collect(Collectors.toList());
		query.setAggregateQueries(aggregates);

		List<Projection> projections = chartQuery.getMeasures().stream().map(this::measureColumnToProjection).collect(Collectors.toList());
		query.setProjectionQueries(projections);

		List<Filter> filters = chartQuery.getFilters().stream().map(this::filterToFilter).collect(Collectors.toList());
		query.setFilterQueries(filters);

		List<Order> orders = chartQuery.getAxis().stream().map((this::axisColumnToOrder)).collect(Collectors.toList());
		query.setOrderQueries(orders);

		return query;
	}

	private Set<InternalEntityType> computeTargetEntities(){
		Map<ColumnRole, Set<SpecializedEntityType>> entitiesByRole = chartQuery.getInvolvedEntities();

		Set<InternalEntityType> targetEntities = new HashSet<>();

		for (Set<SpecializedEntityType> types : entitiesByRole.values()){
			for (SpecializedEntityType type : types){
				targetEntities.add(InternalEntityType.fromSpecializedType(type));
			}
		}
        return targetEntities;
	}

	private Aggregate axisColumnToAggregate(AxisColumn axisColumn) {
		AggregateQuery aggregate = new AggregateQuery();
		aggregate.setLabel(axisColumn.getLabel());
		aggregate.setColumn(axisColumn.getColumn());
		aggregate.setCufId(axisColumn.getCufId());
		aggregate.setOperation(axisColumn.getOperation());
		return aggregate;

	}

	private Projection measureColumnToProjection(MeasureColumn measureColumn) {
		ProjectionQuery projectionQuery = new ProjectionQuery();
		projectionQuery.setColumn(measureColumn.getColumn());
		projectionQuery.setOperation(measureColumn.getOperation());
		projectionQuery.setLabel(measureColumn.getLabel());
		projectionQuery.setCufId(measureColumn.getCufId());
		return projectionQuery;
	}

	private Filter filterToFilter(org.squashtest.tm.domain.chart.Filter filter) {
		FilterQuery filterQuery = new FilterQuery();
		filterQuery.setColumn(filter.getColumn());
		filterQuery.setCufId(filter.getCufId());
		filterQuery.setOperation(filter.getOperation());
		filterQuery.setValues(filter.getValues());
		return filterQuery;
	}

	private Order axisColumnToOrder(AxisColumn axisColumn) {
		OrderQuery orderQuery = new OrderQuery();
		orderQuery.setColumn(axisColumn.getColumn());
		orderQuery.setCufId(axisColumn.getCufId());
		orderQuery.setOperation(axisColumn.getOperation());
		orderQuery.setOrder(com.querydsl.core.types.Order.ASC);
		orderQuery.setLabel(axisColumn.getLabel());
		return orderQuery;
	}
}
