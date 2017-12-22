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
package org.squashtest.tm.service.internal.batchimport;

import org.junit.Test;
import org.squashtest.tm.domain.testcase.Parameter;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.importer.ImportMode;
import org.squashtest.tm.service.importer.ImportStatus;
import org.squashtest.tm.service.internal.batchimport.Facility;
import org.squashtest.tm.service.internal.batchimport.LogTrain;
import org.squashtest.tm.service.internal.batchimport.ParameterInstruction;
import org.squashtest.tm.service.internal.batchimport.ParameterTarget;
import org.squashtest.tm.service.internal.batchimport.TestCaseInstruction;
import org.squashtest.tm.service.internal.batchimport.TestCaseTarget;
import org.squashtest.tm.service.internal.batchimport.excel.ImportModeCellCoercer;

import spock.lang.Specification;
import spock.lang.Unroll;

/**
 * @author Gregory Fouquet
 *
 */
class ParameterInstructionTest extends Specification {
	ParameterTarget target = Mock()
	Parameter parameter = Mock()
	ParameterInstruction instruction = new ParameterInstruction(target, parameter)
	Facility f = Mock()

	def "should create test case"() {
		given:
		instruction.mode = ImportMode.CREATE

		when:
		def lt = instruction.execute(f)

		then:
		1 * f.createParameter(target, parameter) >> new LogTrain()
	}

	@Unroll
	def "should update test case using mode #mode"() {
		given:
		instruction.mode = mode

		when:
		def lt = instruction.execute(f)

		then:
		1 * f.updateParameter(target, parameter) >> new LogTrain()

		where:
		mode << [ImportMode.UPDATE, null]
	}

	def "should delete test case"() {
		given:
		instruction.mode = ImportMode.DELETE

		when:
		def lt = instruction.execute(f)

		then:
		1 * f.deleteParameter(target) >> new LogTrain()
	}

	def "should not execute"() {
		given:
		instruction.addLogEntry(ImportStatus.FAILURE, "", null);

		when:
		def lt = instruction.execute(f)

		then:
		0 * f._

	}

}
