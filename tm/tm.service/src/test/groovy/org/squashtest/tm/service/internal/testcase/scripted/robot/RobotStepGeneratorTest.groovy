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
import org.squashtest.tm.domain.testcase.ScriptedTestCaseExtender
import spock.lang.Specification

class RobotStepGeneratorTest extends Specification {

	RobotStepGenerator stepGenerator = new RobotStepGenerator()

	def "#populateExecution(Execution, ScriptedTestCaseExtender) - Should populate the Execution with one Step containing the script"() {
		given:
			Execution exec = new Execution()
		and:
			ScriptedTestCaseExtender scriptExtender = Mock()
			scriptExtender.getScript() >>
				"*** Settings ***\r\n" +
				"Library           SeleniumLibrary\r\n" +
				"\r\n" +
				"*** Variables ***\r\n" +
				"\${BROWSER}        Firefox\r\n" +
				"\r\n" +
				"*** Keywords ***\r\n" +
				"Login Page Should Be Open\r\n" +
				"    Title Should Be    Login Page"
		when:
			stepGenerator.populateExecution(exec, scriptExtender)
		then:
			exec.getSteps().size() == 1
			def uniqueStep = exec.getSteps().get(0)
			uniqueStep != null
			uniqueStep.getAction() == "*** Settings ***<br/>" +
				"Library           SeleniumLibrary<br/>" +
				"<br/>" +
				"*** Variables ***<br/>" +
				"\${BROWSER}        Firefox<br/>" +
				"<br/>" +
				"*** Keywords ***<br/>" +
				"Login Page Should Be Open<br/>" +
				"    Title Should Be    Login Page"
	}
}
