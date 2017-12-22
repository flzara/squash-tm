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
package org.squashtest.tm.service.internal.batchimport.testcase.excel


import org.squashtest.tm.domain.testcase.ActionTestStep
import org.squashtest.tm.service.internal.batchimport.CallStepInstruction
import org.squashtest.tm.service.internal.batchimport.CallStepParamsInfo;
import org.squashtest.tm.service.internal.batchimport.TestCaseTarget
import org.squashtest.tm.service.internal.batchimport.TestStepTarget

import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Gregory Fouquet
 *
 */
class StepActionPropSetterTest extends Specification {
	StepActionPropSetter setter = StepActionPropSetter.INSTANCE

	@Unroll
	def "should set action field of action step to #expected"() {
		given:
		ActionTestStep step = new ActionTestStep()

		when:
		setter.set(action, step)

		then:
		step.action == expected

		where:
		action 								| expected
		"i wanna see some action out there"	| "i wanna see some action out there"
		null								| ""

	}

	@Unroll
	def "should set path field of call step to #expected"() {
		given:
		TestStepTarget  stepTarget = new TestStepTarget()
		TestCaseTarget calledTestCase = new TestCaseTarget()
		ActionTestStep actionStepBackup = new ActionTestStep()
		CallStepParamsInfo paramInfo = new CallStepParamsInfo()

		CallStepInstruction target = new CallStepInstruction(stepTarget, calledTestCase, actionStepBackup, paramInfo)

		when:
		setter.set(action, target)

		then:
		target.calledTC.path == expected
		target.actionStepBackup.action == actionBackup

		where:
		actionBackup | action 												| expected
		"the path is straight but the slope is steep"		| "the path is straight but the slope is steep"			| "the path is straight but the slope is steep"
		"CALL the path is straight but the slope is steep"	| "CALL the path is straight but the slope is steep"	| "the path is straight but the slope is steep"
		"CALLthe path is straight but the slope is steep"	| "CALLthe path is straight but the slope is steep"		| "the path is straight but the slope is steep"
		""											 		| null													| null

	}
}
