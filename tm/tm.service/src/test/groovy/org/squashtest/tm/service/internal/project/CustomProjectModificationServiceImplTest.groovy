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
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.domain.project.ProjectTemplate
import org.squashtest.tm.domain.testautomation.TestAutomationProject
import org.squashtest.tm.service.internal.repository.ProjectTemplateDao
import org.squashtest.tm.service.project.GenericProjectManagerService
import org.squashtest.tm.service.project.GenericProjectCopyParameter;

import spock.lang.Specification

class CustomProjectModificationServiceImplTest extends Specification {

	CustomProjectModificationServiceImpl service = new CustomProjectModificationServiceImpl()

	ProjectTemplateDao projectTemplateDao = Mock()
	GenericProjectManagerService genericProjectManagerService = Mock()

	def setup() {
		service.projectTemplateDao = projectTemplateDao
		service.genericProjectManager = genericProjectManagerService
	}

	def "Should add a Projet, bind it to the Template and copy all the settings from it"() {

		given: "The Template"

		ProjectTemplate template = new ProjectTemplate()

		and: "The Project"

		Project project = new Project()

		and:"The Conf Object"

		GenericProjectCopyParameter params = new GenericProjectCopyParameter()

		params.setKeepTemplateBinding(true)

		params.setCopyCUF(false)
		params.setCopyInfolists(false)
		params.setCopyAllowTcModifFromExec(false)
		params.setCopyOptionalExecStatuses(false)

		and:

		projectTemplateDao.getOne(1L) >> template

		when:

		service.addProjectFromTemplate(project, 1L, params)

		then:

		1 * genericProjectManagerService.persist(project)
		1 * genericProjectManagerService.synchronizeGenericProject(project, template, params);

		project.getTemplate() == template

		params.isCopyCUF() == true
		params.isCopyInfolists() == true
		params.isCopyAllowTcModifFromExec() == true
		params.isCopyOptionalExecStatuses() == true
	}

	def "Should add a Projet and copy all the settings from the Template"() {

		given: "The Template"

		ProjectTemplate template = new ProjectTemplate()

		and: "The Project"

		Project project = new Project()

		and:"The Conf Object"

		GenericProjectCopyParameter params = new GenericProjectCopyParameter()

		params.setKeepTemplateBinding(false)

		params.setCopyCUF(false)
		params.setCopyInfolists(false)
		params.setCopyAllowTcModifFromExec(false)
		params.setCopyOptionalExecStatuses(false)

		and:

		projectTemplateDao.getOne(1L) >> template

		when:

		service.addProjectFromTemplate(project, 1L, params)

		then:

		1 * genericProjectManagerService.persist(project)
		1 * genericProjectManagerService.synchronizeGenericProject(project, template, params);

		project.getTemplate() == null

		params.isCopyCUF() == false
		params.isCopyInfolists() == false
		params.isCopyAllowTcModifFromExec() == false
		params.isCopyOptionalExecStatuses() == false
	}

}
