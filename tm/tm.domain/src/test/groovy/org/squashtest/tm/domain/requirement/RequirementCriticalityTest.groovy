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
package org.squashtest.tm.domain.requirement

import static RequirementCriticality.*

import org.squashtest.tm.domain.requirement.RequirementCriticality

import spock.lang.Specification

class RequirementCriticalityTest extends Specification {
	def "should convert level into criticality"() {
		when:
		def crit = RequirementCriticality.valueOf(2)

		then:
		crit == MINOR
	}
	def "should throw exception on unknown levels"() {
		when:
		def crit = RequirementCriticality.valueOf(-1)

		then:
		thrown(IllegalArgumentException)
	}
	def "should find strongest criticality"(){
		List<RequirementCriticality> requirementCriticalities = Arrays.asList (MAJOR, MINOR, CRITICAL, UNDEFINED, MAJOR)
		when:
		RequirementCriticality stongest = RequirementCriticality.findStrongestCriticality (requirementCriticalities)
		then:
		stongest == CRITICAL
	}
}