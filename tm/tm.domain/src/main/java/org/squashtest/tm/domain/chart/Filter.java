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
import java.util.List;

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
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.squashtest.tm.domain.EntityType;

@Entity
@Table(name = "CHART_FILTER")
public class Filter implements ColumnPrototypeInstance {

	@Id
	@Column(name = "FILTER_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "chart_filter_filter_id_seq")
	@SequenceGenerator(name = "chart_filter_filter_id_seq", sequenceName = "chart_filter_filter_id_seq", allocationSize = 1)
	private long Id;

	@JoinColumn(name = "CHART_COLUMN_ID")
	@ManyToOne
	private ColumnPrototype column;

	@Enumerated(EnumType.STRING)
	@Column(name = "FILTER_OPERATION")
	private Operation operation;

	@ElementCollection
	@CollectionTable(name = "CHART_FILTER_VALUES", joinColumns = @JoinColumn(name = "FILTER_ID") )
	@Column(name="FILTER_VALUE")
	private List<String> values = new ArrayList<>();

	private Long cufId;

	@Override
	public Operation getOperation() {
		return operation;
	}

	public void setOperation(Operation operation) {
		this.operation = operation;
	}

	@Override
	public ColumnPrototype getColumn() {
		return column;
	}

	public void setColumn(ColumnPrototype column) {
		this.column = column;
	}

	public List<String> getValues() {
		return values;
	}

	public void setValues(List<String> values) {
		this.values = values;
	}

	@Override
	public EntityType getEntityType() {
		return column.getEntityType();
	}

	@Override
	public SpecializedEntityType getSpecializedType(){
		return column.getSpecializedType();
	}

	@Override
	public DataType getDataType() {
		return getColumn().getDataType();
	}

	@Override
	public Long getCufId() {
		return cufId;
	}

	public void setCufId(Long cufId) {
		this.cufId = cufId;
	}


	public Filter createCopy(){
		Filter copy = new Filter();
		copy.setColumn(this.getColumn());
		copy.setOperation(this.getOperation());
		copy.getValues().addAll(this.getValues());
		copy.setCufId(this.getCufId());
		return copy;
	}
}
