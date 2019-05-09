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

import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Embeddable
@Table(name = "QUERY_AGGREGATION_COLUMN")
public class QueryAggregationColumn implements ColumnPrototypeInstance {

	@JoinColumn(name = "PROJECTION_COLUMN_ID")
	@ManyToOne(fetch = FetchType.EAGER)
	private QueryProjectionColumn projectionColumn;

	public QueryProjectionColumn getProjectionColumn() {
		return projectionColumn;
	}

	@Override
	public QueryColumnPrototype getColumn() {
		return projectionColumn.getColumn();
	}

	@Override
	public EntityType getEntityType() {
		return projectionColumn.getColumn().getEntityType();
	}

	@Override
	public SpecializedEntityType getSpecializedType() {
		return projectionColumn.getColumn().getSpecializedType();
	}

	@Override
	public DataType getDataType() {
		return projectionColumn.getColumn().getDataType();
	}

	@Override
	public Operation getOperation() {
		return projectionColumn.getOperation();
	}

	@Override
	public Long getCufId() {
		return projectionColumn.getCufId();
	}
}
