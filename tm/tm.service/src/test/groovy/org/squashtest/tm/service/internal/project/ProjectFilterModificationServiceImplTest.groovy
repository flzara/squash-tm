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

import org.squashtest.tm.domain.projectfilter.ProjectFilter
import org.squashtest.tm.service.internal.repository.ProjectDao
import org.squashtest.tm.service.internal.repository.ProjectFilterDao
import org.squashtest.tm.service.security.UserContextService

import spock.lang.Specification

class ProjectFilterModificationServiceImplTest extends Specification {
	ProjectFilterModificationServiceImpl service = new ProjectFilterModificationServiceImpl()
	ProjectFilterDao projectFilterDao = Mock()
	UserContextService userContextService = Mock()
	ProjectDao projectDao = Mock()
	def user = "current_user"

	def setup()
	{
		service.projectDao = projectDao
		service.projectFilterDao = projectFilterDao
		service.userContextService = userContextService
	}

	def "should update projectFilterStatus"(){
		given:
		ProjectFilter projectFilter = new ProjectFilter()
		projectFilter.setUserLogin(user)
		//set the status to false, should be changed with the test
		projectFilter.setActivated(false)
		projectFilterDao.findByUserLogin(user) >> projectFilter
		userContextService.getUsername() >> "current_user"

		when:
		service.updateProjectFilterStatus(true)

		then:
		projectFilter.getActivated() == true

	}

}
