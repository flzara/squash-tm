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

import org.squashtest.tm.domain.EntityType;

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
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "QUERY_FILTER_COLUMN")
public class QueryFilterColumn implements QueryColumnPrototypeInstance {

	@Id
	@Column(name = "QUERY_FILTER_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "query_filter_column_query_filter_id")
	@SequenceGenerator(name = "query_filter_column_query_filter_id", sequenceName = "query_filter_column_query_filter_id")
	private Long id;

	@ManyToOne
	@JoinColumn(name = "QUERY_COLUMN_ID", nullable = false)
	private QueryColumnPrototype column;

	@Enumerated(EnumType.STRING)
	@Column(name = "FILTER_OPERATION")
	private Operation operation;

	private Long cufId;

	@ElementCollection
	@CollectionTable(name = "QUERY_FILTER_VALUES", joinColumns = @JoinColumn(name = "QUERY_FILTER_ID"))
	@Column(name = "FILTER_VALUE")
	private List<String> values = new ArrayList<>();

	@Override
	public QueryColumnPrototype getColumn() {
		return column;
	}

	@Override
	public EntityType getEntityType() {
		return column.getEntityType();
	}

	@Override
	public SpecializedEntityType getSpecializedType() {
		return column.getSpecializedType();
	}

	@Override
	public DataType getDataType() {
		return column.getDataType();
	}

	@Override
	public Operation getOperation() {
		return operation;
	}

	@Override
	public Long getCufId() {
		return cufId;
	}

	public void setColumn(QueryColumnPrototype column) {
		this.column = column;
	}

	public void setOperation(Operation operation) {
		this.operation = operation;
	}

	public void setCufId(Long cufId) {
		this.cufId = cufId;
	}

	public List<String> getValues() {
		return values;
	}

	public void addValues(List<String> values){
		this.values.addAll(values);
	}

	public void removeValues(List<String> values){
		this.values.removeAll(values);
	}

}
