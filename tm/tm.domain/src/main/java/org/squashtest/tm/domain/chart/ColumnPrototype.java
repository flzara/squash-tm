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

import java.util.EnumSet;
import java.util.Set;

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
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;
import org.squashtest.tm.domain.EntityType;
import org.squashtest.tm.domain.Sizes;
import org.squashtest.tm.domain.chart.SpecializedEntityType.EntityRole;

/**
 * <p>Represents the concept of "attribute of an entity" :
 * 	<ul>
 * 		<li>it is pretty much like a column in a relational model (the "Column" part)</li>
 * 		<li>it is a referential data that will "instanciated" in a {@link ChartDefinition} (the "Prototype" part)</li>
 * 	</ul>
 * </p>
 *
 * <p>
 * 	<b>Taxonomy :</b> ColumnPrototypes comes in three varieties :
 * 	<ul>
 * 		<li>Attribute columns : these columns refer to a proper attribute of the ENTITY_TYPE.</li>
 * 		<li>Calculated columns : these columns don't physically exist and must be calculated.</li>
 * 		<li>Customfield columns : these columns represent custom fields of this entity (detached and configurable attributes)</li>
 * 	</ul>
 * </p>
 *
 * <p>Usage : A ColumnPrototype may be specialized to assume on of the three {@link ColumnRole}s : it can be filtered on, or it can hold the
 * observable value displayed in the chart, or be an axis of this chart. See {@link Filter}, {@link AxisColumn}, {@link MeasureColumn}</p>
 *
 * <p>No user shall create them: only the system can do that. Typically prototypes will be inserted or removed when custom fields
 * are bound to/ unbound from entities.</p>
 *
 * @author bsiri
 *
 */
@Entity
@Table(name = "CHART_COLUMN_PROTOTYPE")
public class ColumnPrototype {

	@Id
	@javax.persistence.Column(name = "CHART_COLUMN_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "chart_column_prototype_chart_column_id_seq")
	@SequenceGenerator(name = "chart_column_prototype_chart_column_id_seq", sequenceName = "chart_column_prototype_chart_column_id_seq", allocationSize = 1)
	private Long id;

	@NotBlank
	@Size(max = Sizes.LABEL_MAX)
	private String label;

	@Embedded
	private SpecializedEntityType specializedType;

	@Enumerated(EnumType.STRING)
	private DataType dataType;

	@Enumerated(EnumType.STRING)
	private ColumnType columnType = ColumnType.ATTRIBUTE;

	@OneToOne
	@JoinColumn(name = "SUBQUERY_ID", insertable = false)
	private ChartQuery subQuery = new ChartQuery();

	private String attributeName;

	private boolean business = true;

	public Long getId() {
		return id;
	}

	public ColumnType getColumnType() {
		return columnType;
	}

	public String getAttributeName() {
		return attributeName;
	}

	@CollectionTable(name = "CHART_COLUMN_ROLE", joinColumns = @JoinColumn(name = "CHART_COLUMN_ID") )
	@ElementCollection
	@Enumerated(EnumType.STRING)
	@Column(name="ROLE")
	private Set<ColumnRole> role;


	public String getLabel() {
		return label;
	}

	public EntityType getEntityType() {
		return specializedType.getEntityType();
	}

	public SpecializedEntityType getSpecializedType(){
		return specializedType;
	}

	public EntityRole getEntityRole(){
		return specializedType.getEntityRole();
	}

	public DataType getDataType() {
		return dataType;
	}

	public EnumSet<ColumnRole> getRole() {
		return role.isEmpty() ? EnumSet.noneOf(ColumnRole.class) : EnumSet.copyOf(role);
	}

	/**
	 * May be null : ColumnPrototypes having {@link ColumnType#ATTRIBUTE} have no subqueries for instance.
	 *
	 * @return
	 */
	public ChartQuery getSubQuery(){
		return subQuery;
	}

	public boolean isBusiness() {
		return business;
	}

	protected ColumnPrototype() {

	}

	public ColumnPrototype(String label, SpecializedEntityType specializedType, DataType dataType,
			ColumnType columnType, ChartQuery subQuery, String attributeName, boolean business, Set<ColumnRole> role) {
		super();
		this.label = label;
		this.specializedType = specializedType;
		this.dataType = dataType;
		this.columnType = columnType;
		this.subQuery = subQuery;
		this.attributeName = attributeName;
		this.business = business;
		this.role = role;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

}
