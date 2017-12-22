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
package org.squashtest.tm.domain.report;

import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.NotBlank;
import org.squashtest.tm.domain.Sizes;
import org.squashtest.tm.domain.audit.Auditable;
import org.squashtest.tm.domain.customreport.CustomReportLibrary;
import org.squashtest.tm.domain.customreport.TreeEntityVisitor;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.tree.TreeEntity;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.security.annotation.AclConstrainedObject;

import javax.persistence.*;
import javax.validation.constraints.Size;

@Entity
@Table(name = "REPORT_DEFINITION")
@Auditable
public class ReportDefinition implements TreeEntity{

	@Id
	@Column(name = "REPORT_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "report_definition_report_id_seq")
	@SequenceGenerator(name = "report_definition_report_id_seq", sequenceName = "report_definition_report_id_seq", allocationSize = 1)
	private Long id;

	@NotBlank
	@Size(max = Sizes.NAME_MAX)
	@Column
	private String name;

	@Lob
	@Type(type = "org.hibernate.type.TextType")
	@Basic(optional = false)
	private String description;

	@NotBlank
	@Size(max = Sizes.NAME_MAX)
	@Column
	private String pluginNamespace;

	@Lob
	@Type(type = "org.hibernate.type.TextType")
	@Basic(optional = false)
	private String parameters;

	@JoinColumn(name = "USER_ID")
	@ManyToOne
	private User owner;

	@OneToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="PROJECT_ID")
	private Project project;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getPluginNamespace() {
		return pluginNamespace;
	}

	public void setPluginNamespace(String pluginNamespace) {
		this.pluginNamespace = pluginNamespace;
	}

	public String getParameters() {
		return parameters;
	}

	public void setParameters(String parameters) {
		this.parameters = parameters;
	}

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
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
	public void accept(TreeEntityVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public TreeEntity createCopy() {
		ReportDefinition copy = new ReportDefinition();
		copy.setName(this.getName());
		copy.setDescription(this.getDescription());
		copy.setOwner(this.getOwner());
		copy.setProject(this.getProject());
		copy.setPluginNamespace(this.getPluginNamespace());
		copy.setParameters(this.getParameters());
		return copy;
	}

	@AclConstrainedObject
	public CustomReportLibrary getCustomReportLibrary(){
		return getProject().getCustomReportLibrary();
	}
}
