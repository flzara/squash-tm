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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.NotBlank;
import org.squashtest.tm.domain.Sizes;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.tree.TreeEntity;
import org.squashtest.tm.security.annotation.AclConstrainedObject;

@Entity
public class CustomReportFolder implements TreeEntity {

	@Id
	@Column(name = "CRF_ID")
	@GeneratedValue(strategy=GenerationType.AUTO, generator="custom_report_folder_crf_id_seq")
	@SequenceGenerator(name="custom_report_folder_crf_id_seq", sequenceName="custom_report_folder_crf_id_seq", allocationSize = 1)
	private Long id;

	@NotBlank
	@Size(max = Sizes.NAME_MAX)
	@Column
	private String name;

	@Column
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	private String description;

	@OneToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="PROJECT_ID")
	private Project project;

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}


	public void setId(Long id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public void accept(TreeEntityVisitor visitor) {
		visitor.visit(this);
	}

	@AclConstrainedObject
	public CustomReportLibrary getCustomReportLibrary() {
		return project.getCustomReportLibrary();
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
	public TreeEntity createCopy() {
		CustomReportFolder copy = new CustomReportFolder();
		copy.setName(this.getName());
		copy.setDescription(this.getDescription());
		copy.setProject(this.getProject());
		return copy;
	}


}
