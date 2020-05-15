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

import org.hibernate.Session
import org.squashtest.csp.core.bugtracker.domain.BugTracker
import org.squashtest.tm.domain.actionword.ActionWordLibrary
import org.squashtest.tm.domain.actionword.ActionWordLibraryNode
import org.squashtest.tm.domain.bugtracker.BugTrackerBinding
import org.squashtest.tm.domain.campaign.CampaignLibrary
import org.squashtest.tm.domain.customreport.CustomReportLibrary
import org.squashtest.tm.domain.customreport.CustomReportLibraryNode
import org.squashtest.tm.domain.execution.ExecutionStatus
import org.squashtest.tm.domain.infolist.InfoList
import org.squashtest.tm.domain.project.AutomationWorkflowType
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.domain.project.ProjectTemplate
import org.squashtest.tm.domain.requirement.RequirementLibrary
import org.squashtest.tm.domain.requirement.RequirementLibraryPluginBinding
import org.squashtest.tm.domain.testautomation.TestAutomationProject
import org.squashtest.tm.domain.testautomation.TestAutomationServer
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.testcase.TestCaseLibrary
import org.squashtest.tm.domain.tf.automationrequest.AutomationRequestStatus
import org.squashtest.tm.exception.NameAlreadyInUseException
import org.squashtest.tm.exception.project.LockedParameterException
import org.squashtest.tm.service.customfield.CustomFieldBindingModificationService
import org.squashtest.tm.service.infolist.InfoListFinderService
import org.squashtest.tm.service.internal.repository.ActionWordLibraryNodeDao
import org.squashtest.tm.service.internal.repository.CustomReportLibraryNodeDao
import org.squashtest.tm.service.internal.repository.GenericProjectDao
import org.squashtest.tm.service.internal.repository.ProjectDao
import org.squashtest.tm.service.internal.repository.ProjectTemplateDao
import org.squashtest.tm.service.internal.repository.TestCaseDao
import org.squashtest.tm.service.project.GenericProjectCopyParameter
import org.squashtest.tm.service.project.ProjectsPermissionManagementService
import org.squashtest.tm.service.security.ObjectIdentityService
import org.squashtest.tm.service.security.PermissionEvaluationService
import org.squashtest.tm.service.testautomation.TestAutomationProjectManagerService
import org.squashtest.tm.service.testcase.CustomTestCaseModificationService
import spock.lang.Ignore
import spock.lang.Specification

import javax.persistence.EntityManager

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
	ActionWordLibraryNodeDao actionWordLibraryNodeDao = Mock()
	TestCaseDao testCaseDao = Mock()

	ObjectIdentityService objectIdentityService = Mock()
	InfoListFinderService infoListService = Mock()
	ProjectsPermissionManagementService permissionsManager = Mock()
	CustomFieldBindingModificationService customFieldBindingModificationService = Mock()
	PermissionEvaluationService permissionEvaluationService = Mock()
	TestAutomationProjectManagerService taProjectService = Mock()
	CustomTestCaseModificationService customTestCaseModificationService = Mock()

	def setup() {
		manager.em = em
		em.unwrap(_) >> session

		manager.genericProjectDao = genericProjectDao
		manager.customReportLibraryNodeDao = customReportLibraryNodeDao
		manager.actionWordLibraryNodeDao = actionWordLibraryNodeDao
		manager.templateDao = templateDao
		manager.projectDao = projectDao
		manager.testCaseDao = testCaseDao

		manager.objectIdentityService = Mock(ObjectIdentityService)
		manager.infoListService = infoListService
		manager.permissionsManager = permissionsManager
		manager.customFieldBindingModificationService = customFieldBindingModificationService
		manager.permissionEvaluationService = permissionEvaluationService
		manager.taProjectService = taProjectService
		manager.customTestCaseModificationService = customTestCaseModificationService
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
		genericProjectDao.getOne(10L) >> project

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
		genericProjectDao.getOne(10L) >> project

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
		genericProjectDao.getOne(10L) >> project
		and:
		CustomReportLibrary crl = new CustomReportLibrary()
		project.getCustomReportLibrary() >> crl
		customReportLibraryNodeDao.findNodeFromEntity(_) >> new CustomReportLibraryNode()
		and:
		ActionWordLibrary awl = new ActionWordLibrary()
		project.getActionWordLibrary() >> awl
		actionWordLibraryNodeDao.findNodeFromEntity(_) >> new ActionWordLibraryNode()
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

		genericProjectDao.getOne(1L) >> template
		genericProjectDao.getOne(2L) >> project
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
		genericProjectDao.getOne(1L) >> template
		genericProjectDao.getOne(2L) >> project
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
		genericProjectDao.getOne(1L) >> template
		genericProjectDao.getOne(2L) >> project
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

		genericProjectDao.getOne(_) >> project

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

		RequirementLibrary templateReqLibrary = new RequirementLibrary()
		Set<RequirementLibraryPluginBinding> templatePlugins = new HashSet<>()
		Set<RequirementLibraryPluginBinding> projectPlugins = new HashSet<>()
		RequirementLibraryPluginBinding plugin1 = new RequirementLibraryPluginBinding()
		plugin1.pluginId = "plugin1"
		templatePlugins.add(plugin1)
		RequirementLibraryPluginBinding plugin2 = new RequirementLibraryPluginBinding()
		plugin2.pluginId = "plugin2"
		projectPlugins.add(plugin2)
		templateReqLibrary.enabledPlugins = templatePlugins
		RequirementLibrary projectReqLibrary = new RequirementLibrary()
		projectReqLibrary.enabledPlugins = projectPlugins
		TestCaseLibrary templateTCLibrary = new TestCaseLibrary()

		TestCaseLibrary projectTCLibrary = new TestCaseLibrary()

		List<TestAutomationProject> taProjects = new ArrayList<>()

		template.getCampaignLibrary() >> templateLibrary
		template.getRequirementLibrary() >> templateReqLibrary
		template.getTestCaseLibrary() >> templateTCLibrary
		template.getTestAutomationProjects() >> taProjects
		project.getCampaignLibrary() >> projectLibrary
		project.getRequirementLibrary() >> projectReqLibrary
		project.getTestCaseLibrary() >> projectTCLibrary
		project.testAutomationProjects >> taProjects

		and:

		projectDao.getOne(_) >> project
		templateDao.getOne(_) >> template
		genericProjectDao.getOne(1L) >> project
		genericProjectDao.getOne(2L) >> template

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

		project.getRequirementLibrary().getEnabledPlugins().size() == 2
	}

	def "#enableExecutionStatus - Should enable an ExecutionStatus in a Project"() {

		given:

		Project project = Mock()
		project.getId() >> 44L
		CampaignLibrary library = new CampaignLibrary()
		Set<ExecutionStatus> disabledStatuses = [ExecutionStatus.UNTESTABLE]
		library.setDisabledStatuses(disabledStatuses)
		project.getCampaignLibrary() >> library

		and:

		genericProjectDao.getOne(44L) >> project
		genericProjectDao.isProjectTemplate(44L) >> false

		when:

		manager.enableExecutionStatus(44L, ExecutionStatus.UNTESTABLE)

		then:

		library.allowsStatus(ExecutionStatus.UNTESTABLE)
	}

	def "#enableExecutionStatus - Should not enable an ExecutionStatus because the Project is bound to a Template"() {

		given:

		ProjectTemplate template = new ProjectTemplate()

		Project project = new Project()
		project.setTemplate(template)
		CampaignLibrary library = new CampaignLibrary()
		Set<ExecutionStatus> disabledStatuses = [ExecutionStatus.UNTESTABLE]
		library.setDisabledStatuses(disabledStatuses)
		project.setCampaignLibrary(library)

		and:

		genericProjectDao.getOne(44L) >> project

		when:

		manager.enableExecutionStatus(44L, ExecutionStatus.UNTESTABLE)

		then:

		thrown LockedParameterException
	}

	def "#enableExecutionStatus - Should enable an ExecutionStatus in a Template and propagate it to its bound Project"() {

		given:

		ProjectTemplate template = Mock(ProjectTemplate)
		template.getId() >> 404L
		CampaignLibrary templateLibrary = new CampaignLibrary()
		Set<ExecutionStatus> disabledTemplateStatuses = [ExecutionStatus.UNTESTABLE]
		templateLibrary.setDisabledStatuses(disabledTemplateStatuses)
		template.getCampaignLibrary() >> templateLibrary

		Project project = new Project()
		CampaignLibrary projectLibrary = new CampaignLibrary()
		Set<ExecutionStatus> disabledProjectStatuses = [ExecutionStatus.UNTESTABLE]
		projectLibrary.setDisabledStatuses(disabledProjectStatuses)
		project.setCampaignLibrary(projectLibrary)

		and:

		genericProjectDao.getOne(404L) >> template
		genericProjectDao.isProjectTemplate(404L) >> true
		projectDao.findAllBoundToTemplate(404L) >> [project]

		when:

		manager.enableExecutionStatus(404L, ExecutionStatus.UNTESTABLE)

		then:

		templateLibrary.allowsStatus(ExecutionStatus.UNTESTABLE)
		projectLibrary.allowsStatus(ExecutionStatus.UNTESTABLE)

	}

	def "#disableExecutionStatus - Should disable an ExecutionStatus in a Project"() {

		given:

		Project project = Mock()
		project.getId() >> 404L
		CampaignLibrary library = new CampaignLibrary()
		Set<ExecutionStatus> disabledStatuses = []
		library.setDisabledStatuses(disabledStatuses)
		project.getCampaignLibrary() >> library

		and:

		genericProjectDao.getOne(404L) >> project
		genericProjectDao.isProjectTemplate(404L) >> false

		when:

		manager.disableExecutionStatus(404L, ExecutionStatus.UNTESTABLE)

		then:

		!library.allowsStatus(ExecutionStatus.UNTESTABLE)
	}

	def "#disableExecutionStatus - Should not disable an ExecutionStatus because the Project is bound to a Template"() {

		given:

		ProjectTemplate template = new ProjectTemplate()

		Project project = new Project()
		project.setTemplate(template)
		CampaignLibrary library = new CampaignLibrary()
		Set<ExecutionStatus> disabledStatuses = []
		library.setDisabledStatuses(disabledStatuses)
		project.setCampaignLibrary(library)

		and:

		genericProjectDao.getOne(404L) >> project

		when:

		manager.disableExecutionStatus(404L, ExecutionStatus.UNTESTABLE)

		then:

		thrown LockedParameterException
	}

	def "#disableExecutionStatus - Should disable an ExecutionStatus in a Template and propagate it to its bound Project"() {

		given:

		ProjectTemplate template = Mock()
		template.getId() >> 404L
		CampaignLibrary templateLibrary = new CampaignLibrary()
		Set<ExecutionStatus> disabledTemplateStatuses = []
		templateLibrary.setDisabledStatuses(disabledTemplateStatuses)
		template.getCampaignLibrary() >> templateLibrary

		Project project = new Project()
		CampaignLibrary projectLibrary = new CampaignLibrary()
		Set<ExecutionStatus> disabledProjectStatuses = []
		projectLibrary.setDisabledStatuses(disabledProjectStatuses)
		project.setCampaignLibrary(projectLibrary)

		and:

		genericProjectDao.getOne(404L) >> template
		genericProjectDao.isProjectTemplate(404L) >> true
		projectDao.findAllBoundToTemplate(404L) >> [project]

		when:

		manager.disableExecutionStatus(404L, ExecutionStatus.UNTESTABLE)

		then:

		!templateLibrary.allowsStatus(ExecutionStatus.UNTESTABLE)
		!projectLibrary.allowsStatus(ExecutionStatus.UNTESTABLE)

	}

	def "#changeAllowTcModifDuringExec - Should change TC-Modif-During-Exec parameter in a Project"() {

		given:

		Project project = new Project()
		project.setAllowTcModifDuringExec(false)

		and:

		genericProjectDao.getOne(404L) >> project

		when:

		manager.changeAllowTcModifDuringExec(404L, true)

		then:

		project.allowTcModifDuringExec()

	}

	def "#changeAllowTcModifDuringExec - Should not change TC-Modif-During-Exec parameter because the Project is bound to a Template"() {

		given:

		ProjectTemplate template = new ProjectTemplate()

		Project project = new Project()
		project.setAllowTcModifDuringExec(false)
		project.setTemplate(template)

		and:

		genericProjectDao.getOne(404L) >> project

		when:

		manager.changeAllowTcModifDuringExec(404L, true)

		then:

		thrown LockedParameterException

	}

	def "#changeAllowTcModifDuringExec - Should change TC-Modif-During-Exec parameter in a Template and propagate it to its bound Projects"() {

		given:

		ProjectTemplate template = new ProjectTemplate()
		template.setAllowTcModifDuringExec(false)

		Project project1 = new Project()
		Project project2 = new Project()
		project1.setAllowTcModifDuringExec(false)
		project2.setAllowTcModifDuringExec(false)

		and:

		genericProjectDao.getOne(404L) >> template
		templateDao.propagateAllowTcModifDuringExec(404L, true) >> {
			args ->
				if(args[1]) {
					project1.setAllowTcModifDuringExec(true)
					project2.setAllowTcModifDuringExec(true)
				}
		}

		when:

		manager.changeAllowTcModifDuringExec(404L, true)

		then:

		template.allowTcModifDuringExec()
		project1.allowTcModifDuringExec()
		project2.allowTcModifDuringExec()


	}

	def "#bindScmRepository(long, long) - Should bind the ScmRepository to the Project"() {
		given: "Mock data"
			long projectId = 12
			long repositoryId = 29
		when:
			manager.bindScmRepository(projectId, repositoryId)
		then:
			1 * genericProjectDao.bindScmRepository(projectId, repositoryId)
	}

	def "#unbindScmRepository(long) - Should unbind the ScmRepository from the Project"() {
		given: "Mock data"
			long projectId = 12
		when:
			manager.unbindScmRepository(projectId)
		then:
			1 * genericProjectDao.unbindScmRepository(projectId)
	}

	def "#changeUseTreeStructureInScmRepo(long, boolean) - Should change useTreeStructureInScmRepo parameter in a Project"() {

		given:
			Project project = new Project()
			project.setUseTreeStructureInScmRepo(true)
		and:
			genericProjectDao.getOne(87L) >> project
		when:
			manager.changeUseTreeStructureInScmRepo(87L, false)
		then:
			!project.isUseTreeStructureInScmRepo()
	}

	@Ignore(value = "As long as attribute GenericProject#allowAutomationWorkflow exists, we have to call #changeAutomationWorkflow(long, boolean) as well and it is not testable.")
	def "#changeAutomationWorkflow(long, String) - Should change the project automation workflow type"() {
		given:
			Project project = Mock()
			project.getAutomationWorkflowType() >> AutomationWorkflowType.NATIVE
			project.isBoundToTemplate() >> false
			project.accept(_) >> true
			genericProjectDao.getOne(22L) >> project
			testCaseDao.findAllTestCaseAssociatedToTAScriptByProject(22L) >> [5L]
		and:
			TestCase testCase = Mock()
			testCaseDao.findById(5L) >> testCase

		when:
			manager.changeAutomationWorkflow(22L, "Jira")
		then:
			project.getAutomationWorkflowType() == "Jira"
			1 * customTestCaseModificationService.createRequestForTestCase(5L, AutomationRequestStatus.AUTOMATED)
	}

	def "#isProjectUsingWorkflow(long, String) - Should return true since the project uses the workflow if plugin exist"() {
		given:
			Project p = Mock()
			p.getAutomationWorkflowType() >> AutomationWorkflowType.REMOTE_WORKFLOW
			genericProjectDao.getOne(9L) >> p
			p.getAutomationWorkflowType().getI18nKey()>>"REMOTE_WORKFLOW"
		when:
			def result = manager.isProjectUsingWorkflow(9L)
		then:
			result == false
	}

	def "#isProjectUsingWorkflow(long, String) - Should return false since the project uses another workflow"() {
		given:
			Project p = Mock()
			p.getAutomationWorkflowType() >> AutomationWorkflowType.NATIVE
			genericProjectDao.getOne(9L) >> p
			p.getAutomationWorkflowType().getI18nKey()>>"NATIVE"
		when:
		def result = manager.isProjectUsingWorkflow(9L)
		then:
		result == false
	}
}
