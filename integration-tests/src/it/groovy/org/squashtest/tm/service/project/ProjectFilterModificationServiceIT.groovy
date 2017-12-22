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

import org.spockframework.util.NotThreadSafe
import org.springframework.transaction.annotation.Transactional
import org.squashtest.tm.domain.project.Project
import org.squashtest.it.basespecs.DbunitServiceSpecification;
import org.squashtest.tm.service.project.ProjectFilterModificationService
import org.unitils.dbunit.annotation.DataSet

import spock.unitils.UnitilsSupport

@NotThreadSafe
@UnitilsSupport
@Transactional
class ProjectFilterModificationServiceIT extends DbunitServiceSpecification {

	@Inject
	ProjectFilterModificationService service;

	def "should create a new project filter"()
	{
		//create an empty list, not important for this test
		List<Long> projectIdList = new ArrayList<Long>()

		when:
		service.saveOrUpdateProjectFilter(projectIdList, true)
		then:
		service.findProjectFilterByUserLogin() != null
	}

	@DataSet("ProjectFilterModificationServiceIT. should update project filter projects list.xml")
	def "should update project filter projects list"()
	{
		def activated = true
		Long projectId1 = -1
		Long projectId2 = -2
		Long projectId3 = -3
		//list with project 1
		List<Long> projectIdList1 = []
		projectIdList1 << projectId1
		//list with project 1&2
		List<Long> projectIdList12 = []
		projectIdList12 << projectId1
		projectIdList12 << projectId2
		//list with project 1,2&3
		List<Long> projectIdList123 = []
		projectIdList123 << projectId1
		projectIdList123 << projectId2
		projectIdList123 << projectId3
		//Create the project filter
		List<Long> projectIdList = []
		service.saveOrUpdateProjectFilter(projectIdList, activated)

		when:
		service.saveOrUpdateProjectFilter(projectIdList1, activated)
		//save the list with only project 1
		List<Project> projectList1 = []
		projectList1.addAll(service.findProjectFilterByUserLogin().getProjects())

		//save the list with only project 1 & 2
		service.saveOrUpdateProjectFilter(projectIdList12, activated)
		List<Project> projectList12 = []
		projectList12.addAll(service.findProjectFilterByUserLogin().getProjects())

		//save the list with only project 1, 2 & 3
		service.saveOrUpdateProjectFilter(projectIdList123, activated)
		List<Project> projectList123 = []
		projectList123.addAll(service.findProjectFilterByUserLogin().getProjects())

		then:
		projectList1.size() == 1
		projectList12.size() == 2
		projectList123.size() == 3
	}

}
