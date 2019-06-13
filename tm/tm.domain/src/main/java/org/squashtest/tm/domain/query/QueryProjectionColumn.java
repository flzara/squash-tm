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

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Embeddable
@Table(name = "QUERY_PROJECTION_COLUMN")
public class QueryProjectionColumn implements QueryColumnPrototypeInstance {


	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "QUERY_COLUMN_ID", nullable = false)
	private QueryColumnPrototype columnPrototype;

	@Column(name = "LABEL")
	private String label;

	@Enumerated(EnumType.STRING)
	@Column(name = "PROJECTION_OPERATION")
	private Operation operation;

	@Column(name = "CUF_ID")
	private Long cufId;


	@Override
	public QueryColumnPrototype getColumn() {
		return columnPrototype;
	}

	@Override
	public EntityType getEntityType() {
		return columnPrototype.getEntityType();
	}

	@Override
	public SpecializedEntityType getSpecializedType() {
		return columnPrototype.getSpecializedType();
	}

	@Override
	public DataType getDataType() {
		return columnPrototype.getDataType();
	}

	@Override
	public Operation getOperation() {
		return operation;
	}

	@Override
	public Long getCufId() {
		return cufId;
	}

	public void setColumnPrototype(QueryColumnPrototype columnPrototype) {
		this.columnPrototype = columnPrototype;
	}

	public QueryColumnPrototype getColumnPrototype() {
		return columnPrototype;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setOperation(Operation operation) {
		this.operation = operation;
	}

	public void setCufId(Long cufId) {
		this.cufId = cufId;
	}

}
