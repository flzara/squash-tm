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
package org.squashtest.tm.service.internal.testcase.scripted


import org.squashtest.tm.domain.testcase.ScriptedTestCase
import org.squashtest.tm.exception.testcase.ScriptParsingException
import org.squashtest.tm.service.internal.repository.ScriptedTestCaseDao
import spock.lang.Specification

class ScriptedTestCaseServiceImplTest extends Specification {

	ScriptedTestCaseServiceImpl scriptedTestCaseService = new ScriptedTestCaseServiceImpl()

	def scriptedTestCaseDao = Mock(ScriptedTestCaseDao)

	def setup() {
		scriptedTestCaseService.scriptedTestCaseDao = scriptedTestCaseDao
	}

	def "#updateTcScript(Long, String) - Should update the script of a Scripted Test Case"() {
		given: "Scripted Test Case"
		ScriptedTestCase scriptedTestCase = new ScriptedTestCase()
		scriptedTestCase.setScript("I am the former script.")
		scriptedTestCaseDao.getOne(7L) >> scriptedTestCase
		when:
		def newScript = "I am the new script."
		scriptedTestCaseService.updateTcScript(7L, newScript)
		then:
		scriptedTestCase.getScript() == newScript
	}

	def "#validateScript(String) - Should validate a script"() {
		given: "Script to validate"
		def script = """
# language: en
Feature: The feature!
	Scenario: The Scenario!
		Given Hello
		When Goodbye
		Then It is gone"""
		when:
		scriptedTestCaseService.validateScript(script)
		then:
		noExceptionThrown()
	}

	def "#validateScript(String) - Should not validate a wrong script"() {
		given: "Script to validate"
		def script = "I am not a valid script"
		when:
		scriptedTestCaseService.validateScript(script)
		then:
		thrown(ScriptParsingException)
	}

}
