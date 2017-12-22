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
package org.squashtest.tm.service.internal.deletion;

import javax.inject.Inject

import org.springframework.transaction.annotation.Transactional
import org.squashtest.tm.domain.campaign.CampaignLibrary
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.domain.requirement.RequirementLibrary
import org.squashtest.tm.domain.testcase.TestCaseLibrary
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.squashtest.tm.service.internal.repository.ProjectDao
import org.squashtest.tm.service.security.ObjectIdentityService
import org.unitils.dbunit.annotation.DataSet

import spock.unitils.UnitilsSupport

@UnitilsSupport
@Transactional
public class ProjectDeletionHandlerIT extends DbunitServiceSpecification {

	@Inject
	private ProjectDeletionHandlerImpl deletionHandler

	private ObjectIdentityService objectIdentityService = Mock()

	def setup(){
		deletionHandler.objectIdentityService = objectIdentityService;
	}
	@Inject
	private ProjectDao projectDao

	@DataSet("ProjectDeletionHandlerTest.should delete project and libraries.xml")
	def "should delete project and libraries"(){

		when :
		def result = deletionHandler.deleteProject(-1)

		then :
		!found(Project.class, -1L)
		allDeleted ("CustomReportLibrary", [-11L])
		allDeleted ("RequirementLibrary", [-12L])
		allDeleted ("TestCaseLibrary", [-13L])
		allDeleted ("CampaignLibrary", [-14L])
	}

	@DataSet("ProjectDeletionHandlerTest.should delete project and libraries.xml")
	def "should delete project acls"(){

		when :
		def result = deletionHandler.deleteProject(-1)
		getSession().flush();
		then :
		! found(Project.class, -1L)

		//		In integration test context ObjectIdentityService is as stub
		//		this is why i use a mock here
		1*objectIdentityService.removeObjectIdentity(-12L,RequirementLibrary.class)
		1*objectIdentityService.removeObjectIdentity(-13L,TestCaseLibrary.class)
		1*objectIdentityService.removeObjectIdentity(-14L,CampaignLibrary.class)
		1*objectIdentityService.removeObjectIdentity(-1L,Project.class)
	}

	@DataSet("ProjectDeletionHandlerTest.should delete project and libraries.xml")
	def "should delete project and plugins"(){

		when :
		newSQLQuery("select * from LIBRARY_PLUGIN_BINDING_PROPERTY").list().size() == 3
		def result = deletionHandler.deleteProject(-1)

		then :
		allDeleted ("RequirementLibraryPluginBinding", [-12L])
		allDeleted ("TestCaseLibraryPluginBinding", [-11L, -12L])
		allDeleted ("CampaignLibraryPluginBinding", [-31L])

		newSQLQuery("select * from LIBRARY_PLUGIN_BINDING_PROPERTY").list().size() == 0
	}
}
