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
package org.squashtest.tm.domain.query;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "QUERY_MODEL")
public class QueryModel implements IQueryModel{

	@Id
	@Column(name = "QUERY_MODEL_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "query_model_query_model_id_seq")
	@SequenceGenerator(name = "query_model_query_model_id_seq", sequenceName = "query_model_query_model_id_seq", allocationSize = 1)
	private Long id;

	private String name;

	@Enumerated(EnumType.STRING)
	private QueryStrategy strategy = QueryStrategy.MAIN;

	@Enumerated(EnumType.STRING)
	private NaturalJoinStyle joinStyle = NaturalJoinStyle.INNER_JOIN;

	@ElementCollection
	@CollectionTable(name = "QUERY_AGGREGATION_COLUMN", joinColumns = @JoinColumn(name = "QUERY_MODEL_ID") )
	@OrderColumn(name = "AGGREGATION_RANK")
	private List<QueryAggregationColumn> aggregationColumns = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "QUERY_MODEL_ID", nullable = false)
	private List<QueryFilterColumn> filterColumns = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "QUERY_MODEL_ID", nullable = false)
	@OrderColumn(name = "PROJECTION_RANK")
	private List<QueryProjectionColumn> projectionColumns = new ArrayList<>();

	@ElementCollection
	@CollectionTable(name = "QUERY_ORDERING_COLUMN", joinColumns = @JoinColumn(name = "QUERY_MODEL_ID") )
	@OrderColumn(name = "ORDER_RANK")
	private List<QueryOrderingColumn> orderingColumns = new ArrayList<>();

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	@Override
	public QueryStrategy getStrategy() {
		return strategy;
	}

	@Override
	public NaturalJoinStyle getJoinStyle() {
		return joinStyle;
	}

	@Override
	public List<QueryAggregationColumn> getAggregationColumns() {
		return aggregationColumns;
	}

	@Override
	public List<QueryFilterColumn> getFilterColumns() {
		return filterColumns;
	}

	@Override
	public List<QueryProjectionColumn> getProjectionColumns() {
		return projectionColumns;
	}

	@Override
	public List<QueryOrderingColumn> getOrderingColumns() {
		return orderingColumns;
	}

	public QueryModel createCopy() {
		QueryModel copy = new QueryModel();
		copy.setName(getName());
		copy.setJoinStyle(getJoinStyle());
		copy.setStrategy(getStrategy());
		return copy;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setStrategy(QueryStrategy strategy) {
		this.strategy = strategy;
	}

	public void setJoinStyle(NaturalJoinStyle joinStyle) {
		this.joinStyle = joinStyle;
	}

	public void setAggregationColumns(List<QueryAggregationColumn> aggregationColumns) {
		this.aggregationColumns = aggregationColumns;
	}

	public void setFilterColumns(List<QueryFilterColumn> filterColumns) {
		this.filterColumns = filterColumns;
	}

	public void setProjectionColumns(List<QueryProjectionColumn> projectionColumns) {
		this.projectionColumns = projectionColumns;
	}

	public void setOrderingColumns(List<QueryOrderingColumn> orderingColumns) {
		this.orderingColumns = orderingColumns;
	}

	@Override
	public Map<ColumnRole, Set<SpecializedEntityType>> getInvolvedEntities() {

		Map<ColumnRole, Set<SpecializedEntityType>> result = new HashMap<>(4);

		Collection<? extends QueryColumnPrototypeInstance> columns;
		columns = getFilterColumns();
		if(!columns.isEmpty()) {
			Set<SpecializedEntityType> filterTypes = collectTypes(columns);
			result.put(ColumnRole.FILTER, filterTypes);
		}
		//TODO continue method
		columns = getAggregationColumns();
		columns = getOrderingColumns();
		columns = getProjectionColumns();





		return result;
	}

	private Set<SpecializedEntityType> collectTypes(Collection<? extends QueryColumnPrototypeInstance> columns){
		Set<SpecializedEntityType> types = new HashSet<>();
		for (QueryColumnPrototypeInstance col : columns){
			types.add(col.getSpecializedType());
		}
		return types;
	}


}
