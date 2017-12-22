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

import org.squashtest.csp.core.bugtracker.domain.BugTracker
import org.squashtest.tm.domain.bugtracker.BugTrackerBinding
import org.squashtest.tm.domain.infolist.InfoList;
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.domain.project.ProjectTemplate
import org.squashtest.tm.domain.testautomation.TestAutomationProject
import org.squashtest.tm.exception.NameAlreadyInUseException;
import org.squashtest.tm.service.customfield.CustomFieldBindingModificationService
import org.squashtest.tm.service.internal.project.CustomProjectModificationServiceImpl
import org.squashtest.tm.service.internal.repository.ProjectTemplateDao
import org.squashtest.tm.service.project.GenericProjectManagerService
import org.squashtest.tm.service.project.ProjectsPermissionManagementService;
import org.squashtest.tm.service.project.GenericProjectCopyParameter;

import spock.lang.Specification

class CustomProjectModificationServiceImplTest extends Specification {
	CustomProjectModificationServiceImpl service = new CustomProjectModificationServiceImpl()
	ProjectTemplateDao projectTemplateDao = Mock()
	GenericProjectManagerService genericProjectManagerService = Mock()

	def setup()
	{
		service.projectTemplateDao = projectTemplateDao
		service.genericProjectManager = genericProjectManagerService
	}

	def "should add projet and copy all settings from template"(){
		given: "a template project"
		ProjectTemplate template = Mock()
		template.isTestAutomationEnabled() >> Boolean.TRUE
		projectTemplateDao.findOne(1L) >> template

		TestAutomationProject automationProject = Mock()
		TestAutomationProject automationCopy = Mock()
		automationProject.createCopy() >> automationCopy
		template.getTestAutomationProjects() >> [automationProject]

		template.isBugtrackerConnected() >> true
		BugTrackerBinding binding = Mock()
		template.getBugtrackerBinding() >> binding
		BugTracker bugtracker = Mock()
		binding.getBugtracker() >> bugtracker

		and: "a project"
		Project project = Mock()
		project.getId()>> 2L
		project.getClass()>> Project.class

		and:"a conf object"
		GenericProjectCopyParameter params = new GenericProjectCopyParameter()
		params.setCopyPermissions(true)
		params.setCopyCUF(true)
		params.setCopyBugtrackerBinding(true)
		params.setCopyAutomatedProjects(true)
		params.setCopyInfolists(false)
		params.setCopyMilestone(false)

		when:
		service.addProjectFromtemplate(project, 1L, params)

		then:
		1* genericProjectManagerService.synchronizeGenericProject(project, template, params);
	}

}
