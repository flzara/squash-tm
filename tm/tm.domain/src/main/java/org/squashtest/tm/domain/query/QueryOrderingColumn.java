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

import com.querydsl.core.types.Order;
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
@Table(name = "QUERY_ORDERING_COLUMN")
public class QueryOrderingColumn implements ColumnPrototypeInstance{

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "QUERY_PROJECTION_ID", nullable = false)
	private QueryProjectionColumn queryProjectionColumn;

	@Enumerated(EnumType.STRING)
	@Column(name = "ORDER_DIR")
	private Order order = Order.ASC;

	public QueryProjectionColumn getQueryProjectionColumn() {
		return queryProjectionColumn;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public Order getOrder() {
		return order;
	}

	@Override
	public QueryColumnPrototype getColumn() {
		return queryProjectionColumn.getColumn();
	}

	@Override
	public EntityType getEntityType() {
		return queryProjectionColumn.getColumn().getEntityType();
	}

	@Override
	public SpecializedEntityType getSpecializedType() {
		return queryProjectionColumn.getColumn().getSpecializedType();
	}

	@Override
	public DataType getDataType() {
		return queryProjectionColumn.getColumn().getDataType();
	}

	@Override
	public Operation getOperation() {
		return queryProjectionColumn.getOperation();
	}

	@Override
	public Long getCufId() {
		return queryProjectionColumn.getCufId();
	}
}
