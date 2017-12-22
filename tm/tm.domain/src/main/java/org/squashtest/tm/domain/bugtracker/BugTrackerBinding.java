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
package org.squashtest.tm.domain.bugtracker;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.ForeignKey;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.tm.domain.project.GenericProject;

/**
 * The purpose of this entity is to store informations about A Project's connection to a BugTracker. <br>
 * 
 * @author mpagnon
 *
 */
@Entity
@Table(name = "BUGTRACKER_BINDING")
public class BugTrackerBinding {
	@Id
	@Column(name = "BUGTRACKER_BINDING_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "bugtracker_binding_bugtracker_binding_id_seq")
	@SequenceGenerator(name = "bugtracker_binding_bugtracker_binding_id_seq", sequenceName = "bugtracker_binding_bugtracker_binding_id_seq", allocationSize = 1)
	private Long id;

	/*@NotNull
	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH })
	@OrderColumn(name = "BUGTRACKER_PROJECT_ORDER")
	@JoinTable(name = "BUGTRACKER_PROJECT", joinColumns = @JoinColumn(name = "BUGTRACKER_BINDING_ID"), inverseJoinColumns = @JoinColumn(name = "BUGTRACKER_PROJECT_ID"))
	*/
	@ElementCollection
	@CollectionTable(name = "BUGTRACKER_PROJECT", joinColumns = @JoinColumn(name = "BUGTRACKER_BINDING_ID"))
	@OrderColumn(name = "BUGTRACKER_PROJECT_ORDER")
	private List<String> bugtrackerProjectName = new ArrayList<>();

	@OneToOne(optional = false)
	@ForeignKey(name="FK_BugtrackerBinding_Bugtracker")
	@JoinColumn(name="BUGTRACKER_ID")
	private BugTracker bugtracker;

	@OneToOne(optional = false)
	@JoinColumn(name="PROJECT_ID")
	private GenericProject project;

	public BugTrackerBinding(){

	}

	public BugTrackerBinding(BugTracker newBugtracker, GenericProject project) {
		super();
		this.bugtracker = newBugtracker;
		this.project = project;
	}

	/**
	 * 
	 * @return the name of a project in the bugtracker ({@link BugTrackerBinding#getBugtracker()})
	 */
	public List<String> getProjectNames() {
		return bugtrackerProjectName;
	}
	
	public void setProjectNames(List<String> projectNames){
		this.bugtrackerProjectName = projectNames;
	}

	public void addProjectName(String projectName){
		bugtrackerProjectName.add(projectName);
	}

	public BugTracker getBugtracker() {
		return bugtracker;
	}

	public void setBugtracker(BugTracker bugtracker) {
		this.bugtracker = bugtracker;
	}

	public Long getId() {
		return id;
	}

	public GenericProject getProject() {
		return project;
	}

	public void setProject(GenericProject project) {
		this.project = project;
	}





}
