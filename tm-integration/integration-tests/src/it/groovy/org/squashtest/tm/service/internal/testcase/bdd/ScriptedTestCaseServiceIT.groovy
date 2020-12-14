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
package org.squashtest.tm.service.internal.testcase.bdd

import org.springframework.transaction.annotation.Transactional
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.squashtest.tm.domain.testcase.ScriptedTestCase
import org.squashtest.tm.service.internal.repository.ScriptedTestCaseDao
import org.squashtest.tm.service.testcase.scripted.ScriptedTestCaseService
import org.unitils.dbunit.annotation.DataSet
import spock.unitils.UnitilsSupport

import javax.inject.Inject

@DataSet
@Transactional
@UnitilsSupport
class ScriptedTestCaseServiceIT extends DbunitServiceSpecification {

	@Inject
	ScriptedTestCaseService scriptedTestCaseService

	@Inject
	ScriptedTestCaseDao scriptedTestCaseDao

	def "#updateTcScript(Long, String) - Should update the script of a Scripted Test Case"() {
		when:
		def newScript = "I am the new script!"
		scriptedTestCaseService.updateTcScript(-7L, newScript)
		then:
		ScriptedTestCase scriptedTestCase = scriptedTestCaseDao.getOne(-7L)
		scriptedTestCase.script == "I am the new script!"
	}
}
