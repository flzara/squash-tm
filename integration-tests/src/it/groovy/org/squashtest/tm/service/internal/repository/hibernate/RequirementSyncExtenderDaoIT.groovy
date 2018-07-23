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
package org.squashtest.tm.service.internal.repository.hibernate

import org.squashtest.it.basespecs.DbunitDaoSpecification
import org.squashtest.tm.service.internal.repository.RequirementSyncExtenderDao
import org.unitils.dbunit.annotation.DataSet
import spock.unitils.UnitilsSupport

import javax.inject.Inject

/**
 * @author aguilhem
 */
@UnitilsSupport
class RequirementSyncExtenderDaoIT extends DbunitDaoSpecification {

	@Inject
	RequirementSyncExtenderDao requirementSyncExtenderDao

	@DataSet("RequirementSyncExtenderDaoIT.should find remote req ids.xml")
	def "should find remote requirement ids"(){
		when:
		def result = requirementSyncExtenderDao.findAllRemoteReqIdVerifiedByATestCaseByServerUrl("http://jira.fr/", -1L)

		then:
		result.size() == 1
		result.contains("REQ-24")
	}
}
