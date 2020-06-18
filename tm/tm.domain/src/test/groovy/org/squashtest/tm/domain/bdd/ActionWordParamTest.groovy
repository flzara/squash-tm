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
package org.squashtest.tm.domain.bdd

import org.squashtest.tm.exception.actionword.InvalidActionWordParameterNameException
import spock.lang.Specification
import spock.lang.Unroll

class ActionWordParamTest extends Specification {

	@Unroll
	def "should create an ActionWordParam"() {
		when:
		ActionWordParameter parameter = new ActionWordParameter(name, defaultValue)

		then:
		parameter.getName() == expectedName
		parameter.getDefaultValue() == expectedValue

		where:
		name              || expectedName || defaultValue           || expectedValue
		"name"            || "name"       || "defaultValue"         || "defaultValue"
		"  a-nAme_123   " || "a-nAme_123" || "defaultValue"         || "defaultValue"
		"name"            || "name"       || "  defaultValue  "     || "defaultValue"
		"name"            || "name"       || "  def@ult    Value  " || "def@ult Value"
	}

	@Unroll
	def "should reject ActionWordParamValue with invalid name"() {
		when:
		new ActionWordParameter(name, "defaultValue")

		then:
		thrown InvalidActionWordParameterNameException

		where:
		name << [null, "", "   ", "a b\"c1 24",
				 "a-n@me_123",
				 "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"]
	}

	@Unroll
	def "should reject ActionWordParamValue with invalid defaultValue"() {
		when:
		new ActionWordParameter("a-nAme_123", defaultValue)

		then:
		thrown InvalidActionWordParameterNameException

		where:
		defaultValue << ["a b\"c1 24",
						 "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"]
	}
}
