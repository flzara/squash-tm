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
package org.squashtest.tm.domain.chart;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

/**
 * <p>This class represents a query. It is composed of  AxisColumn, Filter and MeasureColumns.</p>
 *
 * <p>
 * 	When a query has a strategy = MAIN, it usually represents a ChartDefinition (ie a ChartDefinition points to them). However in other cases
 *  they implement the business of a {@link ColumnPrototype} which has a {@link ColumnType}=CALCULATED. The query is thus a dependency of
 *  a main query. They way it is plugged into the main query depends on a strategy :
 *  </p>
 *
 *  <ul>
 *     	<li>SUBQUERY : this query is indeed a subquery : a subcontext is created then joined with the relevant entity of the main query</li>
 *     	<li>INLINED : the extra tables will be joined within the main query.</li>
 *  </ul>
 *
 * <p>
 * 	A main query always has a strategy = MAIN and a NaturalJoinStyle = INNER. For the other, for now these attributes are hardcoded in the
 * database and cannot be set yet.
 * </p>
 *
 * @author bsiri
 *
 */
@Entity
@Table(name = "CHART_QUERY")
public class ChartQuery implements IChartQuery {

	@Id
	@Column(name = "CHART_QUERY_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "chart_query_chart_query_id_seq")
	@SequenceGenerator(name = "chart_query_chart_query_id_seq", sequenceName = "chart_query_chart_query_id_seq", allocationSize = 1)
	private long id;

	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "QUERY_ID", nullable = false)
	private List<Filter> filters = new ArrayList<>();

	@ElementCollection
	@CollectionTable(name = "CHART_AXIS_COLUMN", joinColumns = @JoinColumn(name = "QUERY_ID") )
	@OrderColumn(name = "AXIS_RANK")
	private List<AxisColumn> axis = new ArrayList<>();

	@ElementCollection
	@CollectionTable(name = "CHART_MEASURE_COLUMN", joinColumns = @JoinColumn(name = "QUERY_ID") )
	@OrderColumn(name = "MEASURE_RANK")
	private List<MeasureColumn> measures = new ArrayList<>();

	@Enumerated(EnumType.STRING)
	private QueryStrategy strategy = QueryStrategy.MAIN;

	@Enumerated(EnumType.STRING)
	private NaturalJoinStyle joinStyle = NaturalJoinStyle.INNER_JOIN;


	@Override
	public List<Filter> getFilters() {
		return filters;
	}

	@Override
	public List<AxisColumn> getAxis() {
		return axis;
	}

	@Override
	public List<MeasureColumn> getMeasures() {
		return measures;
	}

	public void setFilters(List<Filter> filters) {
		this.filters = filters;
	}

	public void setAxis(List<AxisColumn> axis) {
		this.axis = axis;
	}

	public void setMeasures(List<MeasureColumn> measures) {
		this.measures = measures;
	}

	@Override
	public QueryStrategy getStrategy() {
		return strategy;
	}

	@Override
	public NaturalJoinStyle getJoinStyle() {
		return joinStyle;
	}


	public void setStrategy(QueryStrategy strategy) {
		this.strategy = strategy;
	}

	public void setJoinStyle(NaturalJoinStyle joinStyle) {
		this.joinStyle = joinStyle;
	}

	/**
	 * Returns which entities are covered by this chart, sorted by roles.
	 * @return
	 */
	@Override
	public Map<ColumnRole, Set<SpecializedEntityType>> getInvolvedEntities(){

		Map<ColumnRole, Set<SpecializedEntityType>> result = new HashMap<>(3);

		Collection<? extends ColumnPrototypeInstance> columns;

		columns = getFilters();
		if (! columns.isEmpty()){
			Set<SpecializedEntityType> filterTypes = collectTypes(columns);
			result.put(ColumnRole.FILTER, filterTypes);
		}

		columns = getAxis();
		if (! columns.isEmpty()){
			Set<SpecializedEntityType> axisTypes = collectTypes(columns);
			result.put(ColumnRole.AXIS, axisTypes);
		}

		columns = getMeasures();
		if (! columns.isEmpty()){
			Set<SpecializedEntityType> measureTypes = collectTypes(columns);
			result.put(ColumnRole.MEASURE, measureTypes);
		}

		return result;

	}

	public ChartQuery createCopy() {
		ChartQuery copy = new ChartQuery();
		copy.getAxis().addAll(this.copyAxis());
		copy.getMeasures().addAll(this.copyMeasures());
		copy.getFilters().addAll(this.copyFilters());
		copy.setJoinStyle(this.getJoinStyle());
		copy.setStrategy(this.getStrategy());
		return copy;
	}

	private List<Filter> copyFilters() {
		List<Filter> copy = new ArrayList<>();
		for (Filter filter : getFilters()) {
			copy.add(filter.createCopy());
		}
		return copy;
	}

	private List<AxisColumn> copyAxis() {
		List<AxisColumn> copy = new ArrayList<>();
		for (AxisColumn axisColumn : getAxis()) {
			copy.add(axisColumn.createCopy());
		}
		return copy;
	}

	private List<MeasureColumn> copyMeasures() {
		List<MeasureColumn> copy = new ArrayList<>();
		for (MeasureColumn measureColumn : getMeasures()) {
			copy.add(measureColumn.createCopy());
		}
		return copy;
	}

	private Set<SpecializedEntityType> collectTypes(Collection<? extends ColumnPrototypeInstance> columns){
		Set<SpecializedEntityType> types = new HashSet<>();
		for (ColumnPrototypeInstance col : columns){
			types.add(col.getSpecializedType());
		}
		return types;
	}

}
