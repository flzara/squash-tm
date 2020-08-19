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

import org.hibernate.annotations.Type;
import org.squashtest.tm.domain.EntityReference;
import org.squashtest.tm.domain.audit.Auditable;
import org.squashtest.tm.domain.customreport.CustomReportChartBinding;
import org.squashtest.tm.domain.customreport.CustomReportLibrary;
import org.squashtest.tm.domain.customreport.CustomReportTreeEntity;
import org.squashtest.tm.domain.customreport.CustomReportTreeEntityVisitor;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.query.ColumnRole;
import org.squashtest.tm.domain.query.QueryColumnPrototypeInstance;
import org.squashtest.tm.domain.query.SpecializedEntityType;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.security.annotation.AclConstrainedObject;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "CHART_DEFINITION")
@Auditable
public class ChartDefinition implements CustomReportTreeEntity {

	@Id
	@Column(name = "CHART_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "chart_definition_chart_id_seq")
	@SequenceGenerator(name = "chart_definition_chart_id_seq", sequenceName = "chart_definition_chart_id_seq", allocationSize = 1)
	private Long id;

	@NotBlank
	//TODO Changing the database script to ensure that the limitation is TreeEntity.MAX_NAME_SIZE (ie 255).
	//Presently, the column is declared as a VARCHAR(50)
	//We can't do that in 1.13.3 because short delays, and we don't want to do a migration script just for that
	@Size(min = 0, max = 50)
	//if you change this name, don't forget to update the NODE name.
	//name is denormalized, to avoid complex request each time we need the node name.
	private String name;

	@JoinColumn(name = "USER_ID")
	@ManyToOne
	private User owner;

	@Enumerated(EnumType.STRING)
	private Visibility visibility;

	@Enumerated(EnumType.STRING)
	@Column(name = "CHART_TYPE")
	private ChartType type;

	@Lob
	@Type(type = "org.hibernate.type.TextType")
	private String description;

	@OneToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="PROJECT_ID")
	private Project project;

	// --------------- scope ----------------------

	/**
	 * The ScopeType is the coarse-grained hint about whether, and how, the attribute 'scope' should be considered.
	 * The doc in the enum type will tell you more about.
	 */
	@Enumerated(EnumType.STRING)
	private ScopeType scopeType;

	/**
	 * The scope forms the boundaries of the data that should be retrieved. It consists of a set of domain object
	 * instances that the data processed in the chart will be restricted to. They can be either specific
	 * instances, or folder/projects.
	 */
	@ElementCollection
	@CollectionTable(name = "CHART_SCOPE", joinColumns = @JoinColumn(name = "CHART_ID") )
	@AttributeOverrides({
		@AttributeOverride(name = "type", column = @Column(name = "ENTITY_REFERENCE_TYPE") ),
		@AttributeOverride(name = "id", column = @Column(name = "ENTITY_REFERENCE_ID") )
	})
	private List<EntityReference> scope = new ArrayList<>();

	// -------------- /scope --------------------

	// ---------------- project scope --------------

	/**
	 * The project scope has unfortunately a bad name. It has nothing to do with the Chart Scope,
	 * ie the set of entities that forms the boundaries of the expected results.
	 *
	 * What the projectScope really is, is an indicator for the GUI (the chart creation wizard) so that it knows
	 * which custom fields should be proposed in the interface. It has no other purpose and is ignored when
	 * the query is processed.
	 */
	@ElementCollection
	@CollectionTable(name = "CHART_PROJECT_SCOPE", joinColumns = @JoinColumn(name = "CHART_ID") )
	private List<String> projectScope = new ArrayList<>();


	// --------------- /project scope --------------

	@NotNull
	@OneToMany(fetch=FetchType.LAZY,mappedBy="chart", cascade = { CascadeType.REMOVE, CascadeType.REFRESH, CascadeType.MERGE, CascadeType.DETACH})
	private Set<CustomReportChartBinding> chartBindings = new HashSet<>();

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "CHART_DEFINITION_ID")
	private List<Filter> filters = new ArrayList<>();

	@ElementCollection
	@CollectionTable(name = "CHART_AXIS_COLUMN", joinColumns = @JoinColumn(name = "CHART_DEFINITION_ID") )
	@OrderColumn(name = "AXIS_RANK")
	private List<AxisColumn> axis = new ArrayList<>();

	@ElementCollection
	@CollectionTable(name = "CHART_MEASURE_COLUMN", joinColumns = @JoinColumn(name = "CHART_DEFINITION_ID") )
	@OrderColumn(name = "MEASURE_RANK")
	private List<MeasureColumn> measures = new ArrayList<>();

	public User getOwner() {
		return owner;
	}

	public Visibility getVisibility() {
		return visibility;
	}

	public ChartType getType() {
		return type;
	}

	public String getDescription() {
		return description;
	}

	public List<EntityReference> getScope() {
		return scope;
	}

	public List<Filter> getFilters() {
		return filters;
	}

	public List<AxisColumn> getAxis() {
		return axis;
	}

	public List<MeasureColumn> getMeasures() {
		return measures;
	}

	public ScopeType getScopeType() {
		return scopeType;
	}

	public void setScopeType(ScopeType scopeType) {
		this.scopeType = scopeType;
	}

	/**
	 * Returns which entities are covered by this chart, sorted by roles
	 *
	 * @return
	 */
	public Map<ColumnRole, Set<SpecializedEntityType>> getInvolvedEntities(){
		Map<ColumnRole, Set<SpecializedEntityType>> result = new HashMap<>(3);

		Collection<? extends QueryColumnPrototypeInstance> columns;

		columns = getFilters();
		if (! columns.isEmpty()){
			Set<SpecializedEntityType> filterTypes = collectTypes(columns);
			result.put(ColumnRole.FILTER, filterTypes);
		}

		columns = getAxis();
		if (! columns.isEmpty()){
			Set<SpecializedEntityType> axisTypes = collectTypes(columns);
			result.put(ColumnRole.AXIS, axisTypes);
		}

		columns = getMeasures();
		if (! columns.isEmpty()){
			Set<SpecializedEntityType> measureTypes = collectTypes(columns);
			result.put(ColumnRole.MEASURE, measureTypes);
		}

		return result;
	}


	@Override
	public Long getId() {
		return id;
	}
	@Override
	public String getName() {
		return name;
	}
	@Override
	/*
	 * (non-Javadoc)
	 * @see org.squashtest.tm.domain.tree.TreeEntity#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void accept(CustomReportTreeEntityVisitor visitor) {
		visitor.visit(this);
	}

	public void setOwner(User user) {
		this.owner = user;
	}

	@Override
	public Project getProject() {
		return project;
	}

	@Override
	public void setProject(Project project) {
		this.project = project;
	}

	public void setVisibility(Visibility visibility) {
		this.visibility = visibility;
	}

	public void setType(ChartType type) {
		this.type = type;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setScope(List<EntityReference> scope) {
		this.scope = scope;
	}


	@Override
	public CustomReportTreeEntity createCopy() {
		ChartDefinition copy = new ChartDefinition();
		copy.setName(this.getName());
		copy.setOwner(this.getOwner());
		copy.setProject(this.getProject());
		copy.setProjectScope(copyProjectScope());
		copy.setDescription(this.getDescription());

		copy.setType(this.getType());
		copy.setScope(this.copyScope());
		copy.setVisibility(this.getVisibility());
		copy.setScopeType(this.getScopeType());

		copy.setAxis(copyAxis());
		copy.setFilters(copyFilters());
		copy.setMeasures(copyMeasures());

		return copy;
	}


	private List<EntityReference> copyScope() {
		List<EntityReference> copy = new ArrayList<>();
		copy.addAll(this.getScope());
		return  copy;
	}

	private List<String> copyProjectScope() {
		List<String> copy = new ArrayList<>();
		copy.addAll(this.getProjectScope());
		return copy;
	}

	@AclConstrainedObject
	public CustomReportLibrary getCustomReportLibrary(){
		return getProject().getCustomReportLibrary();
	}

	public List<String> getProjectScope() {
		return projectScope;
	}

	public void setProjectScope(List<String> projectScope) {
		this.projectScope = projectScope;
	}

	public void setFilters(List<Filter> filters) {
		/*this.filters = new HashSet<>(filters);*/
		this.filters = filters;
	}

	public void setAxis(List<AxisColumn> axis) {
		this.axis = axis;
	}

	public void setMeasures(List<MeasureColumn> measures) {
		this.measures = measures;
	}

	private Set<SpecializedEntityType> collectTypes(Collection<? extends QueryColumnPrototypeInstance> columns){
		Set<SpecializedEntityType> types = new HashSet<>();
		for (QueryColumnPrototypeInstance col : columns){
			types.add(col.getSpecializedType());
		}
		return types;
	}

	private List<AxisColumn> copyAxis(){
		return axis.stream().map(AxisColumn::createCopy).collect(Collectors.toList());
	}

	private List<MeasureColumn> copyMeasures(){
		return measures.stream().map(MeasureColumn::createCopy).collect(Collectors.toList());
	}

	private List<Filter> copyFilters(){
		return filters.stream().map(Filter::createCopy).collect(Collectors.toList());
	}
}
