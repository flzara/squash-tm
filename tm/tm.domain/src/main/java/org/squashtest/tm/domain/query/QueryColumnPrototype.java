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
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import java.util.Set;

@Entity
@Table(name = "QUERY_COLUMN_PROTOTYPE")
public class QueryColumnPrototype {

	@Id
	@Column(name = "QUERY_COLUMN_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "query_column_prototype_query_column_id_seq")
	@SequenceGenerator(name = "query_column_prototype_query_column_id_seq", sequenceName = "query_column_prototype_query_column_id_seq", allocationSize = 1)
	private Long id;

	@Enumerated(EnumType.STRING)
	private ColumnType columnType = ColumnType.ATTRIBUTE;

	@OneToOne
	@JoinColumn(name = "SUBQUERY_ID", insertable = false)
	private QueryModel subQuery = new QueryModel();

	@NotBlank
	private String label;

	@Embedded
	private SpecializedEntityType specializedType;

	@CollectionTable(name = "CHART_COLUMN_ROLE", joinColumns = @JoinColumn(name = "CHART_COLUMN_ID") )
	@ElementCollection
	@Enumerated(EnumType.STRING)
	@Column(name="ROLE")
	private Set<ColumnRole> role;

	@Enumerated(EnumType.STRING)
	private DataType dataType;

	/**
	 * The name of the attribute referenced by the column. In some instance it may be null, 
	 * in which case the column represents the entity itself (also the columnType is {@link ColumnType#ENTITY},
	 *  the DataType is {@link DataType#ENTITY} and business is false).
	 * 
	 */
	private String attributeName;

	/**
	 * Says whether This is open for public use or if it is for internal purposes only. A column
	 * open to public use will be listed in user interfaces (eg the chart wizard) for user-defined 
	 * query creation. A column for internal purposes is meant to be manipulated by Squash-TM and 
	 * the query engine only.  
	 * 
	 */
	private boolean business = true;

	public QueryColumnPrototype() {
	}

	public QueryColumnPrototype(ColumnType columnType, QueryModel subQuery, @NotBlank String label,
								SpecializedEntityType specializedType, String attributeName, Set<ColumnRole> role,
								DataType dataType, boolean business) {
		this.columnType = columnType;
		this.subQuery = subQuery;
		this.label = label;
		this.specializedType = specializedType;
		this.attributeName = attributeName;
		this.role = role;
		this.dataType = dataType;
		this.business = business;
	}

	public Long getId() {
		return id;
	}

	public ColumnType getColumnType() {
		return columnType;
	}

	public QueryModel getSubQuery() {
		return subQuery;
	}

	public String getLabel() {
		return label;
	}

	public SpecializedEntityType getSpecializedType() {
		return specializedType;
	}

	public String getAttributeName() {
		return attributeName;
	}

	public Set<ColumnRole> getRole() {
		return role;
	}

	public DataType getDataType() {
		return dataType;
	}

	public boolean isBusiness() {
		return business;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

	public EntityType getEntityType() {
		return specializedType.getEntityType();
	}
	
	public boolean representsEntityItself(){
		return columnType == ColumnType.ENTITY;
	}
}
