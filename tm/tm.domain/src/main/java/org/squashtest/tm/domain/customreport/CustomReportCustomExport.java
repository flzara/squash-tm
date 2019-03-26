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
package org.squashtest.tm.domain.customreport;

import org.squashtest.tm.domain.EntityReference;
import org.squashtest.tm.domain.Sizes;
import org.squashtest.tm.domain.audit.Auditable;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.tree.TreeEntity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Auditable
@Entity
@Table(name = "CUSTOM_REPORT_CUSTOM_EXPORT")
public class CustomReportCustomExport implements TreeEntity {

	@Id
	@Column(name = "CRCE_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "custom_report_custom_export_crce_id_seq")
	@SequenceGenerator(name = "custom_report_custom_export_crce_id_seq", sequenceName = "custom_report_custom_export_crce_id_seq", allocationSize = 1)
	private Long id;

	@Column
	@NotBlank
	@Size(max = Sizes.NAME_MAX)
	private String name;

	@ElementCollection
	@CollectionTable(name = "CUSTOM_EXPORT_SCOPE", joinColumns = @JoinColumn(name = "CUSTOM_EXPORT_ID") )
	@AttributeOverrides({
		@AttributeOverride(name = "type", column = @Column(name = "ENTITY_REFERENCE_TYPE") ),
		@AttributeOverride(name = "id", column = @Column(name = "ENTITY_REFERENCE_ID") )
	})
	private List<EntityReference> scope = new ArrayList<>();

	@ElementCollection
	@CollectionTable(name = "CUSTOM_EXPORT_COLUMN", joinColumns = @JoinColumn(name = "CUSTOM_EXPORT_ID") )
//	@OrderColumn(name = "")
	private List<CustomReportCustomExportColumn> columns = new ArrayList<>();

	@JoinColumn(name = "PROJECT_ID")
	@ManyToOne(fetch = FetchType.LAZY)
	private Project project;

	@Override
	public void accept(TreeEntityVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public TreeEntity createCopy() {
		CustomReportCustomExport copy = new CustomReportCustomExport();
		copy.setName(this.name);
		copy.setProject(this.project);
		return copy;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public Project getProject() {
		return project;
	}
	@Override
	public void setProject(Project project) {
		this.project = project;
	}

	@Override
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public List<EntityReference> getScope() {
		return scope;
	}
	public void setScope(List<EntityReference> scope) {
		this.scope = scope;
	}

	public List<CustomReportCustomExportColumn> getColumns() {
		return columns;
	}
	public void setColumns(List<CustomReportCustomExportColumn> columns) {
		this.columns = columns;
	}
}
