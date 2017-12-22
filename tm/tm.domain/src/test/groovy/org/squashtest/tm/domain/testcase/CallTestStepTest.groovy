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
package org.squashtest.tm.domain.testcase

import org.squashtest.tm.tools.unittest.reflection.ReflectionCategory
import spock.lang.Specification;

class CallTestStepTest extends Specification {
	def "should create a copy of step"() {
		given:
		TestCase callee = new TestCase(name: "callee")

		and:
		CallTestStep caller = new CallTestStep(calledTestCase: callee)
		use (ReflectionCategory) {
			TestStep.set field: "id", of: caller, to: 10L
		}

		when:
		def callerCopy = caller.createCopy()

		then:
		callerCopy.id == null // transient new entity
		callerCopy.calledTestCase == callee
	}
}
