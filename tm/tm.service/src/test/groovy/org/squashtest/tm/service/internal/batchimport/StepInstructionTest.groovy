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
package org.squashtest.tm.service.internal.batchimport


import org.squashtest.tm.domain.testcase.ActionTestStep
import org.squashtest.tm.service.importer.ImportMode
import org.squashtest.tm.service.importer.ImportStatus
import org.squashtest.tm.service.internal.batchimport.*
import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Gregory Fouquet
 *
 */
class StepInstructionTest extends Specification {
	TestStepTarget target = Mock()
	ActionTestStep step = Mock()
	TestCaseTarget tct = Mock()
	CallStepParamsInfo paraminfo = new CallStepParamsInfo()
	ActionStepInstruction instruction = new ActionStepInstruction(target, step)
	CallStepInstruction callInstruction = new CallStepInstruction(target, tct, step, paraminfo)
	Facility f = Mock()

	def setup() {
		f._ >> new LogTrain() // prevents NPE on wrong mock call
	}

	def "should create action step"() {
		given:
		instruction.mode = ImportMode.CREATE

		when:
		def lt = instruction.execute(f)

		then:
		1 * f.addActionStep(target, step, _) >> new LogTrain()
	}

	@Unroll
	def "should update action step using mode #mode"() {
		given:
		instruction.mode = mode

		when:
		def lt = instruction.execute(f)

		then:
		1 * f.updateActionStep(target, step, _) >> new LogTrain()

		where:
		mode << [ImportMode.UPDATE, null]
	}

	def "should delete test step"() {
		given:
		instruction.mode = ImportMode.DELETE

		when:
		def lt = instruction.execute(f)

		then:
		1 * f.deleteTestStep(target) >> new LogTrain()
	}

	def "should not execute"() {
		given:
		instruction.addLogEntry(ImportStatus.FAILURE, "", null)

		when:
		def lt = instruction.execute(f)

		then:
		0 * f._

	}

	def "should create call step"() {
		given:
		callInstruction.mode = ImportMode.CREATE

		when:
		def lt = callInstruction.execute(f)

		then:
		1 * f.addCallStep(target, null, tct, paraminfo, step) >> new LogTrain()
	}

	@Unroll
	def "should update call step using mode #mode"() {
		given:
		callInstruction.mode = mode

		when:
		def lt = callInstruction.execute(f)

		then:
		1 * f.updateCallStep(target, null, tct, paraminfo, step) >> new LogTrain()

		where:
		mode << [ImportMode.UPDATE, null]
	}

}
