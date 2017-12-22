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
import org.squashtest.tm.domain.testautomation.TestAutomationProject
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.squashtest.tm.service.project.GenericProjectManagerService
import org.unitils.dbunit.annotation.DataSet

import spock.unitils.UnitilsSupport

@UnitilsSupport
@Transactional
class GenericProjectManagerServiceIT extends DbunitServiceSpecification {

	@Inject
	GenericProjectManagerService modService


	@DataSet("ProjectModificationServiceIT.xml")
	def "should delete bugtrackerProject" () {
		given :
		Project project = findEntity(Project.class, -1L)
		when:
		modService.removeBugTracker(-1L)

		then:
		!project.isBugtrackerConnected()
	}

	@DataSet("ProjectModificationServiceIT.xml")
	def "should change bugtrackerProjectName" () {
		given :
		Project project = findEntity(Project.class, -1L)
		when:
		modService.changeBugTrackerProjectName(-1L, ["this", "that"])

		then:
		project.getBugtrackerBinding().getProjectNames() == ["this", "that"]
	}

	@DataSet("ProjectModificationServiceIT.xml")
	def "should change bugtracker" () {
		given :
		Project project = findEntity(Project.class, -1L)
		when:
		modService.changeBugTracker(-1L, -2L)

		then:
		project.getBugtrackerBinding().getBugtracker().getId() == -2L
	}

	@DataSet("/org/squashtest/tm/service/testautomation/TestAutomationService.sandbox.xml")
	def "should bind a bunch of test automation project"(){

		given :
		def taprojects = [
			new TestAutomationProject("job1", "New Project 1"),
			new TestAutomationProject("job25", "New Project 25")

		] as Collection

		when :
		modService.bindTestAutomationProjects(-1L, taprojects)

		then :
		def proj = modService.findById(-1L)
		proj.testAutomationProjects.collect { it.jobName} as Set == ["job1", "job25", "roberto1", "roberto2", "roberto3"] as Set
		proj.testAutomationProjects.collect { it.server.id }.unique() == [-1L] as Collection

	}


}
