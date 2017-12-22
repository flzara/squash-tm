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

import org.squashtest.tm.core.foundation.exception.NullArgumentException;
import org.squashtest.tm.domain.testcase.Parameter;
import org.squashtest.tm.domain.testcase.TestCase

import spock.lang.Specification;

class ParameterTest  extends Specification {

	TestCase testCase = new TestCase();
	Parameter parameter = new Parameter("Parame", testCase)


	def "should not set description to null"() {
		when:
		parameter.setDescription(null)

		then:
		thrown(NullArgumentException)
	}
	
	def "when creating a new parameter, tc contains the new parameter"() {
		given:
		TestCase tc = new TestCase()
	
		when:
		Parameter parameter = new Parameter("param", tc)
	
		then:
		tc.parameters.contains parameter
	}
}
