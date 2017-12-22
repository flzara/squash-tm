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
package org.squashtest.tm.domain.customfield

import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.CallTestStep;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestStep;

import spock.lang.Specification
import spock.lang.Unroll;

/**
 * @author Gregory Fouquet
 *
 */
class BindableEntityTest extends Specification {
	@Unroll
	def "Should coerce #type to #bindable"() {
		expect:
		bindable == BindableEntity.coerceToBindableEntity(type)

		where:
		type               | bindable
		TestCase           | BindableEntity.TEST_CASE
		ActionTestStep     | BindableEntity.TEST_STEP
		RequirementVersion | BindableEntity.REQUIREMENT_VERSION
		Iteration          | BindableEntity.ITERATION
	}
	
	@Unroll
	def "Should not coerce #type to any BindableEntity"() {
		when:
		BindableEntity.coerceToBindableEntity(type)

		then:
		thrown(IllegalArgumentException)

		where:
		type << [ TestStep, CallTestStep ]
	}
}
