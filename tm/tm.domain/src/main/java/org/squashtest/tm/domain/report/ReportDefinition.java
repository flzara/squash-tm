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
import org.squashtest.tm.domain.audit.Auditable;
import org.squashtest.tm.domain.customreport.CustomReportLibrary;
import org.squashtest.tm.domain.customreport.CustomReportReportBinding;
import org.squashtest.tm.domain.customreport.CustomReportTreeEntity;
import org.squashtest.tm.domain.customreport.CustomReportTreeEntityVisitor;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.security.annotation.AclConstrainedObject;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "REPORT_DEFINITION")
@Auditable
public class ReportDefinition implements CustomReportTreeEntity {

	@Id
	@Column(name = "REPORT_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "report_definition_report_id_seq")
	@SequenceGenerator(name = "report_definition_report_id_seq", sequenceName = "report_definition_report_id_seq", allocationSize = 1)
	private Long id;

	@NotBlank
	@Size(max = 255)
	@Column
	private String name;

	@Lob
	@Type(type = "org.hibernate.type.TextType")
	@Basic(optional = false)
	private String description;

	@NotNull
	@Type(type = "org.hibernate.type.TextType")
	@Size(max = 255)
	private String summary = "";

	@NotBlank
	@Size(max = 255)
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

	@NotNull
	@OneToMany(fetch=FetchType.LAZY,mappedBy="report", cascade = { CascadeType.REMOVE, CascadeType.REFRESH, CascadeType.MERGE, CascadeType.DETACH})
	private Set<CustomReportReportBinding> reportBindings = new HashSet<>();

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

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
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
	public void accept(CustomReportTreeEntityVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public CustomReportTreeEntity createCopy() {
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
