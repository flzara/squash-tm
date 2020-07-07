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

import org.squashtest.tm.exception.actionword.InvalidActionWordParameterValueException
import spock.lang.Specification
import spock.lang.Unroll

class ActionWordParamValueTest extends Specification {

	def "should throw exception when action word parameter value is null"() {
		when:
		new ActionWordParameterValue(null)

		then:
		InvalidActionWordParameterValueException ex = thrown()
		ex.message == "Action word parameter value cannot be null."
	}

	@Unroll
	def "should create an ActionWordParamValue"() {
		when:
		ActionWordParameterValue parameterValue = new ActionWordParameterValue(value)

		then:
		parameterValue.getValue() == expectedValue

		where:
		value                                 || expectedValue
		""                                    || ""
		"    "                                || ""
		"hello tod@y is Monday 27/04/2020 ^^" || "hello tod@y is Monday 27/04/2020 ^^"
		"     hello        adf    df        " || "hello adf df"
	}

	@Unroll
	def "should reject invalid ActionWordParamValue"() {
		when:
		new ActionWordParameterValue(value)

		then:
		InvalidActionWordParameterValueException ex = thrown()
		ex.message == msg

		where:
		msg																		|	value
		"Action word parameter value cannot contain \", < or >."				|	"a b\"c1 24"
		"Action word parameter value length cannot exceed 255 characters."		|	"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
	}
}
