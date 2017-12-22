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
package org.squashtest.tm.service.project


import javax.inject.Inject

import org.springframework.transaction.annotation.Transactional
import org.squashtest.tm.domain.project.Project
import org.squashtest.it.basespecs.DbunitServiceSpecification;
import org.squashtest.tm.service.project.ProjectManagerService
import org.unitils.dbunit.annotation.DataSet

import spock.unitils.UnitilsSupport

@UnitilsSupport
@Transactional
class ProjectManagerServiceIT extends DbunitServiceSpecification {

	@Inject
	ProjectManagerService service

	@DataSet("ProjectManagerServiceIT.should copy template settings.xml")
	def"should copy template settings"(){
		given : "a new project to persit"
		Project project = new Project();
		project.setName("name");
		
		and:"a conf object"
		GenericProjectCopyParameter params = new GenericProjectCopyParameter()
		params.setCopyPermissions(true)
		params.setCopyCUF(true)
		params.setCopyBugtrackerBinding(true)
		params.setCopyAutomatedProjects(true)
		params.setCopyInfolists(true)
		params.setCopyMilestone(false)

		when :
		service.addProjectFromtemplate(project, -1000L, params)

		then:
		project.isTestAutomationEnabled() == true
		project.isBugtrackerConnected() == true
		project.getBugtrackerBinding().getId() != -11L
		project.getBugtrackerBinding().getBugtracker().getId() == -10L
		project.getTestAutomationProjects().size() == 1

		project.getTestCaseNatures().getCode() == "DEF_TC_NAT"
		project.getTestCaseTypes().getCode() == "DEF_TC_TYP"
		project.getRequirementCategories().getCode() == "DEF_REQ_CAT"

	}


}
