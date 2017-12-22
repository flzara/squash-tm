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

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;
import org.squashtest.tm.domain.Sizes;
import org.squashtest.tm.domain.audit.Auditable;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.tree.TreeEntity;
import org.squashtest.tm.security.annotation.AclConstrainedObject;

@Entity
@Auditable
public class CustomReportDashboard implements TreeEntity {

	@Id
	@Column(name = "CRD_ID")
	@GeneratedValue(strategy=GenerationType.AUTO, generator="custom_report_dashboard_crd_id_seq")
	@SequenceGenerator(name="custom_report_dashboard_crd_id_seq", sequenceName="custom_report_dashboard_crd_id_seq", allocationSize = 1)
	private Long id;

	@NotBlank
	@Size(max = Sizes.NAME_MAX)
	@Column
	private String name;

	@OneToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="PROJECT_ID")
	private Project project;

	@NotNull
	@OneToMany(fetch=FetchType.LAZY,mappedBy="dashboard", cascade = { CascadeType.ALL})
	private Set<CustomReportChartBinding> chartBindings = new HashSet<>();

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
		this.name=name;
	}

	@Override
	public void accept(TreeEntityVisitor visitor) {
		visitor.visit(this);
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
		CustomReportDashboard copy = new CustomReportDashboard();
		copy.setProject(this.getProject());
		copy.setName(this.getName());
		copy.getChartBindings().addAll(this.copyBindingsToAnotherDashboard(copy));
		return copy;
	}

	private Set<CustomReportChartBinding> copyBindingsToAnotherDashboard(CustomReportDashboard target) {
		Set<CustomReportChartBinding> copy = new HashSet<>();
		for (CustomReportChartBinding chartBinding : this.chartBindings) {
			CustomReportChartBinding chartBindingCopy = chartBinding.createCopy();
			chartBindingCopy.setDashboard(target);
			copy.add(chartBindingCopy);
		}
		return copy;
	}

	@AclConstrainedObject
	public CustomReportLibrary getCustomReportLibrary(){
		return getProject().getCustomReportLibrary();
	}

	public Set<CustomReportChartBinding> getChartBindings() {
		return chartBindings;
	}

	public void setChartBindings(Set<CustomReportChartBinding> chartBindings) {
		this.chartBindings = chartBindings;
	}
}
