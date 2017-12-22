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
package org.squashtest.tm.service.workspace

import org.spockframework.util.NotThreadSafe
import org.springframework.transaction.annotation.Transactional
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.squashtest.tm.service.internal.dto.UserDto
import org.unitils.dbunit.annotation.DataSet
import spock.unitils.UnitilsSupport

import javax.inject.Inject

@UnitilsSupport
@Transactional
@NotThreadSafe
class WorkspaceHelperServiceIT extends DbunitServiceSpecification {

	@Inject
	private WorkspaceHelperService workspaceHelperService


	@DataSet("WorkspaceDisplayService.sandbox.xml")
	def "should find projects filter models"() {
		given:
		UserDto user = new UserDto("robert", -2L, [-100L, -300L], false)

		when:
		def filterModel = workspaceHelperService.findFilterModel(user, [-1L, -2L, -3L])

		then:
		filterModel.id == -1
		filterModel.enabled
		filterModel.projectData.length == 3

		Object[] pData1 = filterModel.projectData[0]
		pData1[0] == -2L
		pData1[1] == "bar"
		pData1[2] == true
		pData1[3] == null

		Object[] pData = filterModel.projectData[1]
		pData[0] == -3L
		pData[1] == "baz"
		pData[2] == false
		pData[3] == null

		Object[] pData2 = filterModel.projectData[2]
		pData2[0] == -1L
		pData2[1] == "foo"
		pData2[2] == true
		pData2[3] == "foo label"

	}

	@DataSet("WorkspaceDisplayService.sandbox.xml")
	def "should find default filter modef for user without filter"() {
		given:
		UserDto user = new UserDto("bob", -1L, [-100L, -200L, -300L], false)

		when:
		def filterModel = workspaceHelperService.findFilterModel(user, [-1L, -2L, -3L])

		then:
		filterModel.id == null
		!filterModel.enabled
		filterModel.projectData.length == 3

		Object[] pData1 = filterModel.projectData[0]
		pData1[0] == -2L
		pData1[1] == "bar"
		pData1[2] == true
		pData1[3] == null

		Object[] pData = filterModel.projectData[1]
		pData[0] == -3L
		pData[1] == "baz"
		pData[2] == true
		pData[3] == null

		Object[] pData2 = filterModel.projectData[2]
		pData2[0] == -1L
		pData2[1] == "foo"
		pData2[2] == true
		pData2[3] == "foo label"

	}

}
