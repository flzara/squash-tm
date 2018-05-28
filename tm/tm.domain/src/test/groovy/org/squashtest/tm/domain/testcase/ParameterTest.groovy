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
import org.squashtest.tm.exception.testcase.InvalidParameterNameException
import spock.lang.Specification
import spock.lang.Unroll;

class ParameterTest extends Specification {

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

	def "should extract param names from string"() {
		when:
		def parameterNames = Parameter.findUsedParameterNamesInString(inputString)

		then:
		parameterNames.sort() == expectedParamNames.sort()

		where:
		inputString                                                                  || expectedParamNames
		null                                                                         || []
		""                                                                           || []
		"an action without param"                                                    || []
		"an action with param {param1}"                                              || []
		"an action with param \${param1}"                                            || ["param1"]
		"{an action with param \${param1}}"                                          || ["param1"]
		"an action with param \${param1} and not {param2}"                           || ["param1"]
		"{an action with param \${param1} and not param2}"                           || ["param1"]
		"an action with params \${param1} and another one \${param2} and \${param3}" || ["param1", "param2", "param3"]

	}

	def "should reject invalid param names from string"() {
		when:
		Parameter.findUsedParameterNamesInString(inputString)

		then:
		thrown InvalidParameterNameException

		where:
		inputString << ["an action with invalid \${param 1}", "an action with invalid \${param^}"]
	}

	def "should detect invalid param names in string"() {
		when:
		def valid = Parameter.hasInvalidParameterNamesInString(inputString)

		then:
		valid == expected

		where:
		inputString                                                                  || expected
		null                                                                         || false
		""                                                                           || false
		"an action without param"                                                    || false
		"an action with param {param1}"                                              || false
		"an action with param {param 1}"                                             || false
		"an action with param \${param1}"                                            || false
		"{an action with param \${param1}}"                                          || false
		"an action with param \${param1} and not {param2}"                           || false
		"{an action with param \${param1} and not param2}"                           || false
		"an action with params \${param1} and another one \${param2} and \${param3}" || false
		"an action with param \${param 1}"                                           || true
		"an action with param \${param1} and \${param 2}"                            || true
	}


}
