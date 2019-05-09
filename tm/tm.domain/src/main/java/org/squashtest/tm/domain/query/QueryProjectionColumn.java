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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "QUERY_PROJECTION_COLUMN")
public class QueryProjectionColumn {

	@Id
	@Column(name = "QUERY_PROJECTION_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "query_projection_column_query_projection_id_seq")
	@SequenceGenerator(name = "query_projection_column_query_projection_id_seq", sequenceName = "query_projection_column_query_projection_id_seq")
	private Long id;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "QUERY_COLUMN_ID", nullable = false)
	private QueryColumnPrototype columnPrototype;

	@ManyToOne
	@JoinColumn(name = "QUERY_MODEL_ID", insertable = false, updatable = false, nullable = false)
	private QueryModel queryModel;

	private String label;

	@Enumerated(EnumType.STRING)
	@Column(name = "PROJECTION_OPERATION")
	private Operation operation;

	private Long cufId;

	public Long getId() {
		return id;
	}

	public QueryColumnPrototype getColumnPrototype() {
		return columnPrototype;
	}

	public QueryModel getQueryModel() {
		return queryModel;
	}

	public String getLabel() {
		return label;
	}

	public Operation getOperation() {
		return operation;
	}

	public Long getCufId() {
		return cufId;
	}
}
