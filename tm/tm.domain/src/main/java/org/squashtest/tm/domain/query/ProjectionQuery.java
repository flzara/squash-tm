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

public class ProjectionQuery implements Projection {

	private ColumnPrototype column;

	private String label;

	private Operation operation;

	private Long cufId;

	@Override
	public ColumnPrototype getColumn() {
		return column;
	}

	public void setColumn(ColumnPrototype column) {
		this.column = column;
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

	public void setOperation(Operation operation) {
		this.operation = operation;
	}

	@Override
	public Long getCufId() {
		return cufId;
	}

	public void setCufId(Long cufId) {
		this.cufId = cufId;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
}
