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

import org.squashtest.tm.domain.EntityType;
import org.squashtest.tm.domain.query.ColumnPrototypeInstance;
import org.squashtest.tm.domain.query.DataType;
import org.squashtest.tm.domain.query.Operation;
import org.squashtest.tm.domain.query.QueryColumnPrototype;
import org.squashtest.tm.domain.query.SpecializedEntityType;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Embeddable
@Table(name = "CHART_MEASURE_COLUMN")
public class MeasureColumn implements ColumnPrototypeInstance {


	@JoinColumn(name = "CHART_COLUMN_ID")
	@ManyToOne
	private QueryColumnPrototype column;

	@NotBlank
	@Size(min = 0, max = 30)
	private String label;

	@Enumerated(EnumType.STRING)
	@Column(name = "MEASURE_OPERATION")
	private Operation operation;

	private Long cufId;

	@ManyToOne
	@JoinColumn(name = "CHART_DEFINITION_ID")
	private ChartDefinition chartDefinition;

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}



	@Override
	public EntityType getEntityType() {
		return column.getEntityType();
	}



	@Override
	public Long getCufId() {
		return cufId;
	}

	public void setCufId(Long cufId) {
		this.cufId = cufId;
	}

	@Override
	public QueryColumnPrototype getColumn() {
		return column;
	}

	public void setColumn(QueryColumnPrototype column) {
		this.column = column;
	}

	@Override
	public Operation getOperation() {
		return operation;
	}

	public void setOperation(Operation operation) {
		this.operation = operation;
	}

	public ChartDefinition getChartDefinition() {
		return chartDefinition;
	}

	public void setChartDefinition(ChartDefinition chartDefinition) {
		this.chartDefinition = chartDefinition;
	}

	@Override
	public SpecializedEntityType getSpecializedType() {
		return column.getSpecializedType();
	}

	@Override
	public DataType getDataType() {
		return column.getDataType();
	}

	public MeasureColumn createCopy(){
		MeasureColumn copy = new MeasureColumn();
		copy.setColumn(this.getColumn());
		copy.setOperation(this.getOperation());
		copy.setCufId(this.getCufId());
		copy.setLabel(this.getLabel());
		copy.setChartDefinition(this.getChartDefinition());
		return copy;
	}
}
