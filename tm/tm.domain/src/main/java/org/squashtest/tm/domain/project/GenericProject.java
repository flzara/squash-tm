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
package org.squashtest.tm.domain.project;

import org.hibernate.annotations.Type;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.tm.domain.Identified;
import org.squashtest.tm.domain.Sizes;
import org.squashtest.tm.domain.actionword.ActionWordLibrary;
import org.squashtest.tm.domain.attachment.AttachmentHolder;
import org.squashtest.tm.domain.attachment.AttachmentList;
import org.squashtest.tm.domain.audit.Auditable;
import org.squashtest.tm.domain.bdd.BddImplementationTechnology;
import org.squashtest.tm.domain.bdd.BddScriptLanguage;
import org.squashtest.tm.domain.bugtracker.BugTrackerBinding;
import org.squashtest.tm.domain.campaign.CampaignLibrary;
import org.squashtest.tm.domain.customfield.BoundEntity;
import org.squashtest.tm.domain.customreport.CustomReportLibrary;
import org.squashtest.tm.domain.infolist.InfoList;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.requirement.RequirementLibrary;
import org.squashtest.tm.domain.scm.ScmRepository;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.domain.testautomation.TestAutomationServer;
import org.squashtest.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.tm.domain.tf.automationrequest.AutomationRequestLibrary;
import org.squashtest.tm.exception.NoBugTrackerBindingException;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * GenericProject is the superclass of Project and ProjectTemplate. Even though there is no other structural difference
 * between an project and a template, choosing a specialization through inheritance (instead of a specialization through
 * composition) lets the app rely on polymorphism and reduce the impact upon project templates introduction.
 *
 * @author Gregory Fouquet
 *
 */
@Auditable
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "PROJECT_TYPE", discriminatorType = DiscriminatorType.STRING)
@Entity
@Table(name = "PROJECT")
public abstract class GenericProject implements Identified, AttachmentHolder, BoundEntity {

	@Id
	@Column(name = "PROJECT_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "project_project_id_seq")
	@SequenceGenerator(name = "project_project_id_seq", sequenceName = "project_project_id_seq", allocationSize = 1)
	private Long id;

	@Lob
	@Type(type = "org.hibernate.type.TextType")
	private String description;

	@Size(max = Sizes.LABEL_MAX)
	private String label;

	@NotBlank
	@Size(max = Sizes.NAME_MAX)
	private String name;

	private boolean active = true;

	@JoinColumn(name = "TEMPLATE_ID")
	@ManyToOne(fetch = FetchType.LAZY)
	private ProjectTemplate template;

	@OneToOne(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@JoinColumn(name = "TCL_ID")
	private TestCaseLibrary testCaseLibrary;

	@OneToOne(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@JoinColumn(name = "RL_ID")
	private RequirementLibrary requirementLibrary;

	@OneToOne(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@JoinColumn(name = "CL_ID")
	private CampaignLibrary campaignLibrary;

	@OneToOne(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@JoinColumn(name = "CRL_ID")
	private CustomReportLibrary customReportLibrary;

	@OneToOne(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@JoinColumn(name = "ARL_ID")
	private AutomationRequestLibrary automationRequestLibrary;

	@OneToOne(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@JoinColumn(name = "AWL_ID")
	private ActionWordLibrary actionWordLibrary;

	@OneToOne(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "project")
	private BugTrackerBinding bugtrackerBinding;


	@OneToMany(cascade = { CascadeType.ALL }, mappedBy = "tmProject")
	private Set<TestAutomationProject> testAutomationProjects = new HashSet<>();

	@JoinColumn(name = "TA_SERVER_ID")
	@ManyToOne(fetch = FetchType.LAZY)
	private TestAutomationServer testAutomationServer;

	@JoinColumn(name = "SCM_REPOSITORY_ID")
	@ManyToOne(fetch = FetchType.LAZY)
	private ScmRepository scmRepository;

	@OneToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE }, fetch = FetchType.LAZY)
	@JoinColumn(name = "ATTACHMENT_LIST_ID", updatable = false)
	private final AttachmentList attachmentList = new AttachmentList();

	// the so-called information lists
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="REQ_CATEGORIES_LIST")
	private InfoList requirementCategories;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="TC_NATURES_LIST")
	private InfoList testCaseNatures;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="TC_TYPES_LIST")
	private InfoList testCaseTypes;

	@ManyToMany(mappedBy = "projects")
	private Set<Milestone> milestones = new HashSet<>();

	@NotNull
	@Enumerated(EnumType.STRING)
	private BddImplementationTechnology bddImplementationTechnology = BddImplementationTechnology.CUCUMBER;

	@NotNull
	@Enumerated(EnumType.STRING)
	private BddScriptLanguage bddScriptLanguage = BddScriptLanguage.ENGLISH;

	private boolean allowTcModifDuringExec = false;

	private boolean allowAutomationWorkflow = false;

	@NotNull
	@Enumerated(EnumType.STRING)
	private AutomationWorkflowType automationWorkflowType = AutomationWorkflowType.NONE;

	private boolean useTreeStructureInScmRepo = true;

	private Integer automatedSuitesLifetime;

	public GenericProject() {
		super();
	}


	public List<Milestone> getMilestones() {
		return new ArrayList<>(milestones);
	}


	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public Long getId() {
		return id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@NotBlank
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name.trim();
	}

	public void setActive(boolean isActive) {
		this.active = isActive;
	}

	public boolean isActive() {
		return this.active;
	}

	public boolean isBugtrackerConnected() {
		return bugtrackerBinding != null;
	}

	public TestCaseLibrary getTestCaseLibrary() {
		return testCaseLibrary;
	}

	public void setTestCaseLibrary(TestCaseLibrary testCaseLibrary) {
		this.testCaseLibrary = testCaseLibrary;
		notifyLibraryAssociation(testCaseLibrary);
	}

	public RequirementLibrary getRequirementLibrary() {
		return requirementLibrary;
	}

	public void setRequirementLibrary(RequirementLibrary requirementLibrary) {
		this.requirementLibrary = requirementLibrary;
		notifyLibraryAssociation(requirementLibrary);
	}

	public CampaignLibrary getCampaignLibrary() {
		return campaignLibrary;
	}

	public void setCampaignLibrary(CampaignLibrary campaignLibrary) {
		this.campaignLibrary = campaignLibrary;
		notifyLibraryAssociation(campaignLibrary);
	}

	public AutomationRequestLibrary getAutomationRequestLibrary() {
		return automationRequestLibrary;
	}

	public void setAutomationRequestLibrary(AutomationRequestLibrary automationRequestLibrary) {
		this.automationRequestLibrary = automationRequestLibrary;
		if (automationRequestLibrary != null){
			automationRequestLibrary.notifyAssociatedWithProject(this);
		}
	}

	public CustomReportLibrary getCustomReportLibrary() {
		return customReportLibrary;
	}


	public void setCustomReportLibrary(CustomReportLibrary customReportLibrary) {
		this.customReportLibrary = customReportLibrary;
	}

	public ActionWordLibrary getActionWordLibrary() {
		return actionWordLibrary;
	}

	public void setActionWordLibrary(ActionWordLibrary actionWordLibrary) {
		this.actionWordLibrary = actionWordLibrary;
	}

	public BugTrackerBinding getBugtrackerBinding() {
		return bugtrackerBinding;
	}

	public void setBugtrackerBinding(BugTrackerBinding bugtrackerBinding) {
		this.bugtrackerBinding = bugtrackerBinding;
	}

	/**
	 * Notifies a library it was associated with this project.
	 *
	 * @param library
	 */
	private void notifyLibraryAssociation(GenericLibrary<?> library) {
		if (library != null) {
			library.notifyAssociatedWithProject(this);
		}
	}

	@Override
	public AttachmentList getAttachmentList() {
		return attachmentList;
	}

	/**
	 * will add a TestAutomationProject if it wasn't added already, or won't do anything if it was already bound to
	 * this.
	 *
	 * @param project
	 */
	public void bindTestAutomationProject(TestAutomationProject project) {
		for (TestAutomationProject proj : testAutomationProjects) {
			if (proj.getId().equals(project.getId())) {
				return;
			}
		}
		testAutomationProjects.add(project);
	}

	public void unbindTestAutomationProject(TestAutomationProject project) {
		Iterator<TestAutomationProject> iter = testAutomationProjects.iterator();
		while (iter.hasNext()) {
			TestAutomationProject proj = iter.next();
			if (proj.getId().equals(project.getId())) {
				iter.remove();
				break;
			}
		}
	}

	public void unbindTestAutomationProject(long taProjectId) {
		Iterator<TestAutomationProject> iter = testAutomationProjects.iterator();
		while (iter.hasNext()) {
			TestAutomationProject proj = iter.next();
			if (proj.getId().equals(taProjectId)) {
				iter.remove();
				break;
			}
		}
	}

	/**
	 * Tells whether or not the project is link to a test automation server
	 * @return
	 */
	public boolean isTestAutomationEnabled() {
		return testAutomationServer != null;
	}

	/**
	 * Tells whether or not the project is link to a legacy type test automation server
	 * @return
	 */
	public boolean isLegacyTestAutomationEnabled() {
		return testAutomationServer != null && testAutomationServer.getKind().equals( "jenkins");
	}

	public TestAutomationServer getTestAutomationServer() {
		return testAutomationServer;
	}

	public void setTestAutomationServer(TestAutomationServer server) {
		this.testAutomationServer = server;
	}

	public boolean hasTestAutomationProjects() {
		return !testAutomationProjects.isEmpty();
	}

	public Collection<TestAutomationProject> getTestAutomationProjects() {
		return testAutomationProjects;
	}

	/**
	 * returns true if the given TA project is indeed bound to the TM project
	 *
	 * @param p
	 * @return
	 */
	public boolean isBoundToTestAutomationProject(TestAutomationProject p) {
		return testAutomationProjects.contains(p);
	}

	/**
	 * returns a TestAutomationProject, bound to this TM project, that references the same job than the argument.
	 *
	 * @param p
	 * @return a TestAutomationProject if an equivalent was found or null if not
	 */
	public TestAutomationProject findTestAutomationProjectByJob(TestAutomationProject p) {
		for (TestAutomationProject mine : testAutomationProjects) {
			if (mine.referencesSameJob(p)) {
				return mine;
			}
		}
		return null;
	}

	public void removeBugTrackerBinding() {
		this.bugtrackerBinding = null;
	}

	/**
	 *
	 * @return the BugTracker the Project is bound to
	 * @throws NoBugTrackerBindingException
	 *             if the project is not BugtrackerConnected
	 */
	public BugTracker findBugTracker() {
		if (isBugtrackerConnected()) {
			return getBugtrackerBinding().getBugtracker();
		} else {
			throw new NoBugTrackerBindingException();
		}
	}



	public InfoList getRequirementCategories() {
		return requirementCategories;
	}

	public void setRequirementCategories(InfoList requirementCategories) {
		this.requirementCategories = requirementCategories;
	}

	public InfoList getTestCaseNatures() {
		return testCaseNatures;
	}

	public void setTestCaseNatures(InfoList testCaseNatures) {
		this.testCaseNatures = testCaseNatures;
	}


	public InfoList getTestCaseTypes() {
		return testCaseTypes;
	}

	public void setTestCaseTypes(InfoList testCaseTypes) {
		this.testCaseTypes = testCaseTypes;
	}

	public void setTestAutomationProjects(Set<TestAutomationProject> testAutomationProjects) {
		this.testAutomationProjects = testAutomationProjects;
	}

	public abstract void accept(ProjectVisitor visitor);

	public void unbindMilestone(Milestone milestone) {
		removeMilestone(milestone);
		milestone.removeProject(this);
	}

	/**
	 * CONSIDER THIS PRIVATE ! It should only be called by project.unbindMilestone or milestone.unbindProject
	 * TODO find a better design
	 * @param milestone
	 */
	public void removeMilestone(Milestone milestone) {
		Iterator<Milestone> iter = milestones.iterator();
		while (iter.hasNext()) {
			Milestone mil = iter.next();
			if (mil.getId().equals(milestone.getId())) {
				iter.remove();
				break;
			}
		}
	}

	public void unbindMilestones(List<Milestone> milestones) {
		// TODO arg could be Collection instead of List
		for (Milestone milestone : milestones) {
			unbindMilestone(milestone);
		}

	}

	public void addMilestone(Milestone milestone) {
		milestones.add(milestone);
	}

	public void bindMilestone(Milestone milestone) {
		milestones.add(milestone);
		milestone.addProject(this);
	}

	public void bindMilestones(List<Milestone> milestones) {
		for (Milestone milestone : milestones){
			bindMilestone(milestone);
		}

	}

	public boolean isBoundToMilestone(Milestone milestone) {
		return milestones.contains(milestone);
	}

	public void setAllowTcModifDuringExec(boolean allowTcModifDuringExec) {
		this.allowTcModifDuringExec = allowTcModifDuringExec;
	}

	public boolean allowTcModifDuringExec() {
		return this.allowTcModifDuringExec;
	}

	public ProjectTemplate getTemplate() {
		return template;
	}
	public void setTemplate(ProjectTemplate template) {
		this.template = template;
	}

	public boolean isBoundToTemplate() {
		return template != null;
	}

	public boolean isAllowAutomationWorkflow() {
		return allowAutomationWorkflow;
	}

	public void setAllowAutomationWorkflow(boolean allowAutomationWorkflow) {
		this.allowAutomationWorkflow = allowAutomationWorkflow;
	}

	public ScmRepository getScmRepository() {
		return scmRepository;
	}

	public void setScmRepository(ScmRepository scmRepository) {
		this.scmRepository = scmRepository;
	}

	public boolean isUseTreeStructureInScmRepo() {
		return useTreeStructureInScmRepo;
	}

	public void setUseTreeStructureInScmRepo(boolean useTreeStructureInScmRepo) {
		this.useTreeStructureInScmRepo = useTreeStructureInScmRepo;
	}

	public AutomationWorkflowType getAutomationWorkflowType() {
		return automationWorkflowType;
	}

	public void setAutomationWorkflowType(AutomationWorkflowType automationWorkflowType) {
		this.automationWorkflowType = automationWorkflowType;
	}

	public BddImplementationTechnology getBddImplementationTechnology() {
		return bddImplementationTechnology;
	}
	public void setBddImplementationTechnology(BddImplementationTechnology bddImplementationTechnology) {
		this.bddImplementationTechnology = bddImplementationTechnology;
	}

	public BddScriptLanguage getBddScriptLanguage() {
		return bddScriptLanguage;
	}
	public void setBddScriptLanguage(BddScriptLanguage bddScriptLanguage) {
		this.bddScriptLanguage = bddScriptLanguage;
	}

	public Integer getAutomatedSuitesLifetime() {
		return automatedSuitesLifetime;
	}
	public void setAutomatedSuitesLifetime(Integer automatedSuitesLifetime) {
		this.automatedSuitesLifetime = automatedSuitesLifetime;
	}
}
