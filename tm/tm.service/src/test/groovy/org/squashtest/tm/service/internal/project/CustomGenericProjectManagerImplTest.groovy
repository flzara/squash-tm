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
package org.squashtest.tm.service.internal.project

import org.squashtest.tm.domain.campaign.CampaignLibrary
import org.squashtest.tm.domain.execution.ExecutionStatus
import org.squashtest.tm.domain.infolist.InfoList
import org.squashtest.tm.domain.project.GenericProject
import org.squashtest.tm.service.internal.repository.ProjectDao
import org.squashtest.tm.service.internal.repository.ProjectTemplateDao;

import javax.persistence.EntityManager;

import org.hibernate.Session
import org.hibernate.SessionFactory
import org.squashtest.csp.core.bugtracker.domain.BugTracker
import org.squashtest.tm.domain.bugtracker.BugTrackerBinding
import org.squashtest.tm.domain.customreport.CustomReportLibrary
import org.squashtest.tm.domain.customreport.CustomReportLibraryNode
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.domain.project.ProjectTemplate
import org.squashtest.tm.domain.testautomation.TestAutomationProject
import org.squashtest.tm.domain.testautomation.TestAutomationServer
import org.squashtest.tm.exception.NameAlreadyInUseException
import org.squashtest.tm.service.customfield.CustomFieldBindingModificationService
import org.squashtest.tm.service.infolist.InfoListFinderService
import org.squashtest.tm.service.internal.repository.CustomReportLibraryNodeDao
import org.squashtest.tm.service.internal.repository.GenericProjectDao
import org.squashtest.tm.service.project.GenericProjectCopyParameter
import org.squashtest.tm.service.project.ProjectsPermissionManagementService
import org.squashtest.tm.service.security.ObjectIdentityService
import org.squashtest.tm.service.security.PermissionEvaluationService
import org.squashtest.tm.service.testautomation.TestAutomationProjectManagerService

import spock.lang.Specification

/**
 * @author Gregory Fouquet
 *
 */
class CustomGenericProjectManagerImplTest extends Specification {

	CustomGenericProjectManagerImpl manager = new CustomGenericProjectManagerImpl()

	EntityManager em = Mock()
	Session session = Mock()

	GenericProjectDao genericProjectDao = Mock()
	ProjectDao projectDao = Mock()
	ProjectTemplateDao templateDao = Mock()
	CustomReportLibraryNodeDao customReportLibraryNodeDao = Mock()

	ObjectIdentityService objectIdentityService = Mock()
	InfoListFinderService infoListService = Mock()
	ProjectsPermissionManagementService permissionsManager = Mock()
	CustomFieldBindingModificationService customFieldBindingModificationService = Mock()
	PermissionEvaluationService permissionEvaluationService = Mock()
	TestAutomationProjectManagerService taProjectService = Mock()

	def setup() {
		manager.em = em
		em.unwrap(_) >> session

		manager.genericProjectDao = genericProjectDao
		manager.customReportLibraryNodeDao = customReportLibraryNodeDao
		manager.templateDao = templateDao
		manager.projectDao = projectDao

		manager.objectIdentityService = Mock(ObjectIdentityService)
		manager.infoListService = infoListService
		manager.permissionsManager = permissionsManager
		manager.customFieldBindingModificationService = customFieldBindingModificationService
		manager.permissionEvaluationService = permissionEvaluationService
		manager.taProjectService = taProjectService

	}

	def "should not persist project with name in use"() {
		given:
		Project candidate = new Project(name: "HASHTAG NAME CLASH")

		and:
		genericProjectDao.countByName("HASHTAG NAME CLASH") >> 1L

		when:
		manager.persist(candidate)

		then:
		thrown NameAlreadyInUseException
	}

	def "should persist project with free name "() {
		given:
		Project candidate = new Project(name: "HASHTAG NAME AVAILABLE")

		and:
		genericProjectDao.countByName("HASHTAG NAME AVAILABLE") >> 0L

		when:
		manager.persist(candidate)

		then:
		1 * em.persist(candidate)
		// missing ids of entities will throw a NPE after the persist(). we retort to this workaround which is simpler than trying to set an id on unknown objects
		thrown NullPointerException
	}

	def "should not change project's name to name in use"() {
		given:
		Project project = new Project()
		genericProjectDao.findOne(10L) >> project

		and:
		genericProjectDao.countByName("HASHTAG NAME CLASH") >> 1L

		when:
		manager.changeName(10L, "HASHTAG NAME CLASH")

		then:
		thrown NameAlreadyInUseException
	}

	def "should change a project's name to its own name"() {
		given:
		Project project = new Project(name: "HASHTAG NO NAME CLASH")
		genericProjectDao.findOne(10L) >> project

		and:
		genericProjectDao.countByName("HASHTAG NO NAME CLASH") >> 1L

		when:
		manager.changeName(10L, "HASHTAG NO NAME CLASH")

		then:
		notThrown NameAlreadyInUseException
	}

	def "should change a project's name to a free name"() {
		given:
		Project project = new Project()
		CustomReportLibrary crl = new CustomReportLibrary()
		genericProjectDao.findOne(10L) >> project
		project.getCustomReportLibrary() >> crl
		customReportLibraryNodeDao.findNodeFromEntity(_) >> new CustomReportLibraryNode()

		and:
		genericProjectDao.countByName("use your freedom a'choice") >> 0L

		when:
		manager.changeName(10L, "use your freedom a'choice")

		then:
		notThrown NameAlreadyInUseException
	}

	def "should add projet and copy all settings from template except milestones"() {

		given: "a template project"

		ProjectTemplate template = Mock()
		Project project = Mock()

		template.isTestAutomationEnabled() >> Boolean.TRUE

		genericProjectDao.findOne(1L) >> template
		genericProjectDao.findOne(2L) >> project
		permissionEvaluationService.hasRoleOrPermissionOnObject(_,_,_) >> true

		TestAutomationProject automationProject = Mock()
		TestAutomationProject automationCopy = Mock()
		TestAutomationServer testAutomationServer = Mock()
		automationProject.createCopy() >> automationCopy
		template.getTestAutomationProjects() >> [automationProject]
		template.getTestAutomationServer() >> testAutomationServer

		template.isBugtrackerConnected() >> true
		BugTrackerBinding binding = Mock()
		template.getBugtrackerBinding() >> binding
		BugTracker bugtracker = Mock()
		binding.getBugtracker() >> bugtracker

		and: "a project"

		project.getId()>> 2L
		project.getClass()>> Project.class
		project.getTestAutomationServer() >> testAutomationServer


		and:"a conf object"

		GenericProjectCopyParameter params = new GenericProjectCopyParameter()
		params.setCopyPermissions(true)
		params.setCopyCUF(true)
		params.setCopyBugtrackerBinding(true)
		params.setCopyAutomatedProjects(true)
		params.setCopyInfolists(true)
		params.setCopyMilestone(false)
		params.setCopyAllowTcModifFromExec(true)
		params.setCopyOptionalExecStatuses(false)
		params.setCopyPlugins(false)

		when:

		manager.synchronizeGenericProject(project, template, params)

		then:

		1* permissionsManager.copyAssignedUsers(project,template)
		1* project.setBugtrackerBinding(_)
		1* customFieldBindingModificationService.copyCustomFieldsSettingsFromTemplate(project, template)
		1* taProjectService.persist(_)
		1* project.setRequirementCategories(_);
		1* project.setTestCaseNatures(_);
		1* project.setTestCaseTypes(_);
		1* project.setAllowTcModifDuringExec(_)
	}

	def "should add projet and copy all settings but custom fields binding from template"(){
		given: "a template project"
		ProjectTemplate template = Mock()
		Project project = Mock()
		template.isTestAutomationEnabled() >> Boolean.TRUE
		genericProjectDao.findOne(1L) >> template
		genericProjectDao.findOne(2L) >> project
		permissionEvaluationService.hasRoleOrPermissionOnObject(_,_,_) >> true

		TestAutomationProject automationProject = Mock()
		TestAutomationProject automationCopy = Mock()
		TestAutomationServer testAutomationServer = Mock()
		automationProject.createCopy() >> automationCopy
		template.getTestAutomationProjects() >> [automationProject]
		template.getTestAutomationServer() >> testAutomationServer

		template.isBugtrackerConnected() >> true
		BugTrackerBinding binding = Mock()
		template.getBugtrackerBinding() >> binding
		BugTracker bugtracker = Mock()
		binding.getBugtracker() >> bugtracker

		and: "a project"
		project.getId()>> 2L
		project.getClass()>> Project.class
		project.getTestAutomationServer() >> testAutomationServer


		and:"a conf object"
		GenericProjectCopyParameter params = new GenericProjectCopyParameter()
		params.setCopyPermissions(true)
		params.setCopyCUF(false)
		params.setCopyBugtrackerBinding(true)
		params.setCopyAutomatedProjects(true)
		params.setCopyInfolists(true)
		params.setCopyMilestone(false)
		params.setCopyAllowTcModifFromExec(true)
		params.setCopyOptionalExecStatuses(false)
		params.setCopyPlugins(false)

		when:
		manager.synchronizeGenericProject(project, template, params)

		then:
		1* permissionsManager.copyAssignedUsers(project,template)
		1* project.setBugtrackerBinding(_)
		0* customFieldBindingModificationService.copyCustomFieldsSettingsFromTemplate(project, template)
		1* taProjectService.persist(_)
		1* project.setRequirementCategories(_);
		1* project.setTestCaseNatures(_);
		1* project.setTestCaseTypes(_);
		1* project.setAllowTcModifDuringExec(_)
	}

	def "should add projet and copy only infolists"(){
		given: "a template project"
		ProjectTemplate template = Mock()
		Project project = Mock()
		template.isTestAutomationEnabled() >> Boolean.TRUE
		genericProjectDao.findOne(1L) >> template
		genericProjectDao.findOne(2L) >> project
		permissionEvaluationService.hasRoleOrPermissionOnObject(_,_,_) >> true

		and: "a project"
		project.getId()>> 2L
		project.getClass()>> Project.class


		and:"a conf object"
		GenericProjectCopyParameter params = new GenericProjectCopyParameter()
		params.setCopyPermissions(false)
		params.setCopyCUF(false)
		params.setCopyBugtrackerBinding(false)
		params.setCopyAutomatedProjects(false)
		params.setCopyInfolists(true)
		params.setCopyMilestone(false)
		params.setCopyAllowTcModifFromExec(false)
		params.setCopyOptionalExecStatuses(false)
		params.setCopyPlugins(false)

		when:
		manager.synchronizeGenericProject(project, template, params)

		then:
		0* permissionsManager.copyAssignedUsers(project,template)
		0* project.setBugtrackerBinding(_)
		0* customFieldBindingModificationService.copyCustomFieldsSettingsFromTemplate(project, template)
		0* taProjectService.persist(_)
		1* project.setRequirementCategories(_);
		1* project.setTestCaseNatures(_);
		1* project.setTestCaseTypes(_);
		0* project.setAllowTcModifDuringExec(_)
	}

	def "#disassociateFromTemplate - Should disassociate a Project from a Template"() {

		given:

		Project project = new Project()
		ProjectTemplate template = new ProjectTemplate()
		project.setTemplate(template)

		and:

		genericProjectDao.findOne(_) >> project

		when:

		manager.disassociateFromTemplate(1L)

		then:

		project.getTemplate() == null
	}

	def "#associateToTemplate - Should associate a persisted Project to a Template"() {

		given:

		Project project = Mock(Project)
		project.getId() >> 1L
		ProjectTemplate template = Mock(ProjectTemplate);
		template.getId() >> 2L

		InfoList categoryList = Mock()
		InfoList natureList = Mock()
		InfoList typeList = Mock()

		template.getRequirementCategories() >> categoryList
		template.getTestCaseNatures() >> natureList
		template.getTestCaseTypes() >> typeList

		template.allowTcModifDuringExec() >> true

		CampaignLibrary templateLibrary = new CampaignLibrary()
		templateLibrary.enableStatus(ExecutionStatus.SETTLED)
		templateLibrary.disableStatus(ExecutionStatus.UNTESTABLE)

		CampaignLibrary projectLibrary = new CampaignLibrary()
		projectLibrary.disableStatus(ExecutionStatus.SETTLED)
		projectLibrary.enableStatus(ExecutionStatus.UNTESTABLE)

		template.getCampaignLibrary() >> templateLibrary
		project.getCampaignLibrary() >> projectLibrary

		and:

		projectDao.findOne(_) >> project
		templateDao.findOne(_) >> template
		genericProjectDao.findOne(1L) >> project
		genericProjectDao.findOne(2L) >> template

		when:

		manager.associateToTemplate(1L, 2L)

		then:

		1 * project.setTemplate(template)

		1 * customFieldBindingModificationService.copyCustomFieldsSettingsFromTemplate(project, template)

		1 * project.setRequirementCategories(categoryList)
		1 * project.setTestCaseNatures(natureList)
		1 * project.setTestCaseTypes(typeList)

		1 * project.setAllowTcModifDuringExec(true)

		project.getCampaignLibrary().allowsStatus(ExecutionStatus.SETTLED)
		!project.getCampaignLibrary().allowsStatus(ExecutionStatus.UNTESTABLE)
	}

	def "#enableExecutionStatus - Should enable an ExecutionStatus in a Project"() {

		given:

		Project project = new Project()
		CampaignLibrary library = new CampaignLibrary()
		Set<ExecutionStatus> disabledStatuses = [ExecutionStatus.UNTESTABLE]
		library.setDisabledStatuses(disabledStatuses)
		project.setCampaignLibrary(library)

		and:

		genericProjectDao.findOne(44L) >> project

		when:

		manager.enableExecutionStatus(44L, ExecutionStatus.UNTESTABLE)

		then:

		library.allowsStatus(ExecutionStatus.UNTESTABLE)
	}

	def "#enableExecutionStatus - Should enable an ExecutionStatus in a Template and propagate it to its bound Project"() {

		given:

		ProjectTemplate template = new ProjectTemplate()
		CampaignLibrary templateLibrary = new CampaignLibrary()
		Set<ExecutionStatus> disabledTemplateStatuses = [ExecutionStatus.UNTESTABLE]
		templateLibrary.setDisabledStatuses(disabledTemplateStatuses)
		template.setCampaignLibrary(templateLibrary)

		Project project = new Project()
		CampaignLibrary projectLibrary = new CampaignLibrary()
		Set<ExecutionStatus> disabledProjectStatuses = [ExecutionStatus.UNTESTABLE]
		projectLibrary.setDisabledStatuses(disabledProjectStatuses)
		project.setCampaignLibrary(projectLibrary)

		and:

		genericProjectDao.findOne(404L) >> template
		projectDao.findAllBoundToTemplate(404L) >> [project]

		when:

		manager.enableExecutionStatus(404L, ExecutionStatus.UNTESTABLE)

		then:

		templateLibrary.allowsStatus(ExecutionStatus.UNTESTABLE)
		projectLibrary.allowsStatus(ExecutionStatus.UNTESTABLE)

	}

	def "#disableExecutionStatus - Should disable an ExecutionStatus in a Project"() {

		given:

		Project project = new Project()
		CampaignLibrary library = new CampaignLibrary()
		Set<ExecutionStatus> disabledStatuses = []
		library.setDisabledStatuses(disabledStatuses)
		project.setCampaignLibrary(library)

		and:

		genericProjectDao.findOne(404L) >> project

		when:

		manager.disableExecutionStatus(404L, ExecutionStatus.UNTESTABLE)

		then:

		!library.allowsStatus(ExecutionStatus.UNTESTABLE)
	}

	def "#disableExecutionStatus - Should disable an ExecutionStatus in a Template and propagate it to its bound Project"() {

		given:

		ProjectTemplate template = new ProjectTemplate()
		CampaignLibrary templateLibrary = new CampaignLibrary()
		Set<ExecutionStatus> disabledTemplateStatuses = []
		templateLibrary.setDisabledStatuses(disabledTemplateStatuses)
		template.setCampaignLibrary(templateLibrary)

		Project project = new Project()
		CampaignLibrary projectLibrary = new CampaignLibrary()
		Set<ExecutionStatus> disabledProjectStatuses = []
		projectLibrary.setDisabledStatuses(disabledProjectStatuses)
		project.setCampaignLibrary(projectLibrary)

		and:

		genericProjectDao.findOne(404L) >> template
		projectDao.findAllBoundToTemplate(404L) >> [project]

		when:

		manager.disableExecutionStatus(404L, ExecutionStatus.UNTESTABLE)

		then:

		!templateLibrary.allowsStatus(ExecutionStatus.UNTESTABLE)
		!projectLibrary.allowsStatus(ExecutionStatus.UNTESTABLE)

	}
}
