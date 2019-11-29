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
package org.squashtest.tm.service.internal.testcase.scripted.robot

import org.squashtest.tm.domain.execution.Execution
import org.squashtest.tm.domain.infolist.InfoListItem
import org.squashtest.tm.domain.testcase.ScriptedTestCaseExtender
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.testcase.TestCaseImportance
import org.squashtest.tm.domain.testcase.TestCaseStatus
import spock.lang.Specification

class RobotTestCaseParserTest extends Specification {

	RobotTestCaseParser parser = new RobotTestCaseParser()

	def "#populateExecution(Execution) - Should create one unique execution step containing the script"() {
		given:
		def scriptedTestCaseExtender = Mock(ScriptedTestCaseExtender)
		scriptedTestCaseExtender.getScript() >> " This is the script content !"

		def testCase = Mock(TestCase)
		testCase.getScriptedTestCaseExtender() >> scriptedTestCaseExtender
		testCase.getImportance() >> TestCaseImportance.MEDIUM
		testCase.getStatus() >> TestCaseStatus.UNDER_REVIEW
		testCase.getNature() >> Mock(InfoListItem)
		testCase.getType() >> Mock(InfoListItem)

		Execution exec = new Execution()
		exec.setReferencedTestCase(testCase)

		when:
		parser.populateExecution(exec)

		then:
		exec.getSteps().size() == 1
		def uniqueStep = exec.getSteps().get(0)
		uniqueStep != null
		uniqueStep.getAction() == " This is the script content !"

	}
}
