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
class StepResultPropSetterTest extends Specification {
	StepResultPropSetter setter = StepResultPropSetter.INSTANCE

	@Unroll
	def "should set result field of action step to #expected"() {
		given:
		ActionTestStep step = new ActionTestStep()

		when:
		setter.set(result, step)

		then:
		step.expectedResult == expected

		where:
		result 									| expected
		"i wanna see some results out there"	| "i wanna see some results out there"
		null									| ""

	}

	def "should set the result on actionStepBackup"() {
		given:
		TestStepTarget  stepTarget = new TestStepTarget()
		TestCaseTarget calledTestCase = new TestCaseTarget()
		ActionTestStep actionStepBackup = new ActionTestStep()
		CallStepParamsInfo paraminfo = new CallStepParamsInfo()

		CallStepInstruction target = new CallStepInstruction(stepTarget, calledTestCase, actionStepBackup, paraminfo)
		def result = "i wanna see some results out there"
		when:
		setter.set(result, target)

		then:
		actionStepBackup.getExpectedResult() == result


	}
}
