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
package org.squashtest.tm.domain.milestone;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.SortableField;
import org.springframework.format.annotation.DateTimeFormat;
import org.squashtest.tm.domain.Identified;
import org.squashtest.tm.domain.audit.Auditable;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.project.ProjectTemplate;
import org.squashtest.tm.domain.project.ProjectVisitor;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.search.LevelEnumBridge;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.users.User;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Auditable
@Entity
@Table(name = "MILESTONE")
@Indexed
public class Milestone implements Identified {

	private static final String MILESTONE_ID = "MILESTONE_ID";

	@Id
	@Column(name = MILESTONE_ID)
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "milestone_milestone_id_seq")
	@SequenceGenerator(name = "milestone_milestone_id_seq", sequenceName = "milestone_milestone_id_seq", allocationSize = 1)
	@SortableField
	private Long id;

	@Lob
	@Type(type = "org.hibernate.type.TextType")
	private String description;

	@NotBlank
	@Pattern(regexp="[^|]*")
	@Size(min = 0, max = 30)
	private String label;

	@Enumerated(EnumType.STRING)
	@FieldBridge(impl = LevelEnumBridge.class)
	private MilestoneStatus status;

	@Enumerated(EnumType.STRING)
	@Column(name = "M_RANGE")
	@FieldBridge(impl = LevelEnumBridge.class)
	private MilestoneRange range;

	@NotNull
	@DateTimeFormat(pattern = "yy-MM-dd")
	private Date endDate;

	@ManyToMany(cascade=CascadeType.DETACH)
	@JoinTable(name = "MILESTONE_BINDING", joinColumns = @JoinColumn(name = MILESTONE_ID), inverseJoinColumns = @JoinColumn(name = "PROJECT_ID"))
	private Set<GenericProject> projects = new HashSet<>();

	@ManyToMany(cascade=CascadeType.DETACH)
	@JoinTable(name = "MILESTONE_BINDING_PERIMETER", joinColumns = @JoinColumn(name = MILESTONE_ID), inverseJoinColumns = @JoinColumn(name = "PROJECT_ID"))
	private Set<GenericProject> perimeter = new HashSet<>();

	@JoinColumn(name = "USER_ID")
	@ManyToOne(cascade=CascadeType.DETACH)
	private User owner;

	/**
	 * @deprecated does not seem to be used any longer
	 */
	@Deprecated
	@ManyToMany(fetch = FetchType.LAZY, cascade=CascadeType.DETACH)
	@JoinTable(name = "MILESTONE_TEST_CASE", joinColumns = @JoinColumn(name = MILESTONE_ID), inverseJoinColumns = @JoinColumn(name = "TEST_CASE_ID"))
	private Set<TestCase> testCases = new HashSet<>();

	/**
	 * @deprecated does not seem to be used any longer
	 */
	@Deprecated
	@ManyToMany(fetch = FetchType.LAZY, cascade=CascadeType.DETACH)
	@JoinTable(name = "MILESTONE_REQ_VERSION", joinColumns = @JoinColumn(name = MILESTONE_ID), inverseJoinColumns = @JoinColumn(name = "REQ_VERSION_ID"))
	private Set<RequirementVersion> requirementVersions = new HashSet<>();

	/**
	 * @deprecated does not seem to be used any longer
	 */
	@Deprecated
	@ManyToMany(fetch = FetchType.LAZY, cascade=CascadeType.DETACH)
	@JoinTable(name = "MILESTONE_CAMPAIGN", joinColumns = @JoinColumn(name = MILESTONE_ID), inverseJoinColumns = @JoinColumn(name = "CAMPAIGN_ID"))
	private Set<Campaign> campaigns = new HashSet<>();

	public List<GenericProject> getPerimeter() {
		return new ArrayList<>(perimeter);
	}

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public int getNbOfBindedProject() {
		return projects.size();
	}

	public List<GenericProject> getProjects() {
		return new ArrayList<>(projects);
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public MilestoneStatus getStatus() {
		return status;
	}

	public void setStatus(MilestoneStatus status) {
		this.status = status;
	}

	public MilestoneRange getRange() {
		return range;
	}

	public void setRange(MilestoneRange range) {
		this.range = range;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	@Override
	public Long getId() {
		return id;
	}

	public void unbindProject(GenericProject genericProject) {
		removeProject(genericProject);
		genericProject.removeMilestone(this);
	}

	/**
	 * CONSIDER THIS PRIVATE ! It should only be called by project.unbindMilestone or milestone.unbindProject
	 * TODO find a better design
	 * @param project
	 */
	public void removeProject(GenericProject project) {
		Iterator<GenericProject> iter = projects.iterator();
		while (iter.hasNext()) {
			GenericProject proj = iter.next();
			if (proj.getId().equals(project.getId())) {
				iter.remove();
				break;
			}
		}
	}

	public void unbindProjects(List<GenericProject> projects) {
		for (GenericProject project : projects) {
			unbindProject(project);
		}
	}

	public void bindProject(GenericProject project) {
		projects.add(project);
		project.addMilestone(this);
	}

	public void addProject(GenericProject genericProject) {
		projects.add(genericProject);
	}

	public void bindProjects(List<GenericProject> projects) {
		for (GenericProject project : projects) {
			bindProject(project);
		}
	}

	public void addProjectToPerimeter(GenericProject genericProject) {

		if (!isInPerimeter(genericProject)) {
			perimeter.add(genericProject);
		}

	}

	public boolean isInPerimeter(GenericProject genericProject) {
		for (GenericProject project : perimeter) {
			if (project.getName().equals(genericProject.getName())) {
				return true;
			}
		}
		return false;
	}

	public void addProjectsToPerimeter(List<GenericProject> projects) {
		perimeter.addAll(projects);
	}

	public void removeProjectsFromPerimeter(List<GenericProject> projects) {
		for (GenericProject project : projects) {
			removeProjectFromPerimeter(project);
		}

	}

	public void removeProjectFromPerimeter(GenericProject project) {
		Iterator<GenericProject> iter = perimeter.iterator();
		while (iter.hasNext()) {
			GenericProject proj = iter.next();
			if (proj.getId().equals(project.getId())) {
				iter.remove();
				break;
			}
		}
	}

	/**
	 * @deprecated does not seem to be used any longer
	 */
	@Deprecated
	// XXX omg potentially VERY EXPENSIVE
	public Set<TestCase> getTestCases() {
		return testCases;
	}

	/**
	 * @deprecated does not seem to be used any longer
	 */
	@Deprecated
	// XXX omg potentially VERY EXPENSIVE
	public Set<RequirementVersion> getRequirementVersions() {
		return requirementVersions;
	}

	/**
	 * @deprecated does not seem to be used any longer
	 */
	@Deprecated
	// XXX omg potentially VERY EXPENSIVE
	public Set<Campaign> getCampaigns() {
		return campaigns;
	}

	/**
	 * @deprecated does not seem to be used any longer
	 */
	@Deprecated
	// XXX omg potentially VERY EXPENSIVE
	public void bindTestCase(TestCase testCase) {
		testCases.add(testCase);
	}

	/**
	 * @deprecated does not seem to be used any longer
	 */
	@Deprecated
	// XXX omg potentially VERY EXPENSIVE
	public void bindRequirementVersion(RequirementVersion version) {

		// we need to exit early because this case is legit
		// but would fail the test below
		if (requirementVersions.contains(version)) {
			return;
		}

		if (isOneVersionAlreadyBound(version)) {
			throw new IllegalArgumentException("Another version of this requirement is already bound to this milestone");
		}

		requirementVersions.add(version);

	}

	public boolean isOneVersionAlreadyBound(RequirementVersion version) {


		// check that no other version of this requirement is bound already to this milestone
		Collection<RequirementVersion> allVersions = new ArrayList<>(version.getRequirement().getRequirementVersions());
		CollectionUtils.filter(allVersions, new Predicate() {

			@Override
			public boolean evaluate(Object reqV) {
				return ((RequirementVersion) reqV).getMilestones().contains(Milestone.this);
			}
		});

		return CollectionUtils.containsAny(requirementVersions, allVersions);

	}


	public boolean isBoundToATemplate() {

		final boolean[] boundToTemplate = { false };
		for (GenericProject proj : projects) {
			proj.accept(new ProjectVisitor() {

				@Override
				public void visit(ProjectTemplate projectTemplate) {
					boundToTemplate[0] = true;
				}

				@Override
				public void visit(Project project) {
					//do nothing
				}
			});

			if (boundToTemplate[0]) {
				return true;
			}
		}

		return false;

	}

	public void removeTemplates() {
		removeTemplates(perimeter);
		removeTemplates(projects);
	}

	private void removeTemplates(Collection<GenericProject> col) {
		final Iterator<GenericProject> iter = col.iterator();
		while (iter.hasNext()) {
			GenericProject proj = iter.next();

			proj.accept(new ProjectVisitor() {

				@Override
				public void visit(ProjectTemplate projectTemplate) {
					iter.remove();
				}

				@Override
				public void visit(Project project) {
					//do nothing, keep the projects !
				}
			});
		}
	}

	public static Boolean allowsEdition(Collection<Milestone> milestones){
		Boolean allowed = Boolean.TRUE;
		for (Milestone m : milestones){
			if (! m.getStatus().isAllowObjectModification()){
				allowed =Boolean.FALSE;
				break;
			}
		}
		return allowed;
	}


	public static Boolean allowsCreationOrDeletion(Collection<Milestone> milestones){
		Boolean allowed = Boolean.TRUE;
		for (Milestone m : milestones){
			if (! m.getStatus().isAllowObjectCreateAndDelete()){
				allowed =Boolean.FALSE;
				break;
			}
		}
		return allowed;
	}


	public boolean isLocked(){
		return MilestoneStatus.LOCKED == status;
	}

	public void unbindAllProjects() {
		projects.clear();
	}

	public void clearPerimeter() {
		perimeter.clear();
	}

	public void clearObjects() {
		campaigns.clear();
		testCases.clear();
		requirementVersions.clear();
	}

}
