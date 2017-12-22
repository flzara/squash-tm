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
package org.squashtest.tm.domain.testcase;

import static TestCaseImportance.*
import static RequirementCriticality.*

import org.squashtest.tm.domain.requirement.RequirementCriticality
import org.squashtest.tm.domain.testcase.TestCaseImportance

import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Gregory Fouquet
 *
 */
class TestCaseWeightTest extends Specification {
	@Unroll("i18n key of #status should be '#key'")
	def "should return i18n key"() {
		when:
		def actualKey = status.i18nKey

		then:
		actualKey == key

		where:
		status    | key
		LOW       | "test-case.importance.LOW"
		MEDIUM    | "test-case.importance.MEDIUM"
		HIGH      | "test-case.importance.HIGH"
		VERY_HIGH | "test-case.importance.VERY_HIGH"
	}

	def "should deduce importance HIGH given criticalities"(){
		given:
		List<RequirementCriticality> requirementsCriticalities = Arrays.asList(MAJOR, MINOR, CRITICAL, UNDEFINED)
		when:
		TestCaseImportance tci = TestCaseImportance.deduceTestCaseImportance (requirementsCriticalities)
		then:
		tci.equals HIGH
	}
	def "should deduce importance MEDIUM given criticalities"(){
		given:
		List<RequirementCriticality> requirementsCriticalities = Arrays.asList(UNDEFINED, MAJOR, MINOR,)
		when:
		TestCaseImportance tci = TestCaseImportance.deduceTestCaseImportance (requirementsCriticalities)
		then:
		tci.equals MEDIUM
	}
	def "should deduce importance LOW given criticalities 1"(){
		given:
		List<RequirementCriticality> requirementsCriticalities = Arrays.asList(UNDEFINED, MINOR)
		when:
		TestCaseImportance tci = TestCaseImportance.deduceTestCaseImportance (requirementsCriticalities)
		then:
		tci.equals LOW
	}
	def "should deduce importance LOW given criticalities 2"(){
		given:
		List<RequirementCriticality> requirementsCriticalities = Arrays.asList(UNDEFINED)
		when:
		TestCaseImportance tci = TestCaseImportance.deduceTestCaseImportance (requirementsCriticalities)
		then:
		tci.equals LOW
	}
	def "should deduce importance LOW given criticalities 3"(){
		given:
		List<RequirementCriticality> requirementsCriticalities = new ArrayList<RequirementCriticality>(0)
		when:
		TestCaseImportance tci = TestCaseImportance.deduceTestCaseImportance (requirementsCriticalities)
		then:
		tci.equals LOW
	}
	def "should say that change of criticality can change importance of TC 1"(){
		given:
		RequirementCriticality oldRequirementCriticality = MINOR
		RequirementCriticality newCriticality = CRITICAL
		TestCaseImportance importance = MEDIUM
		when:
		boolean canChange = importance.changeOfCriticalityCanChangeImportanceAuto (oldRequirementCriticality, newCriticality)
		then:
		canChange == true
	}
	def "should say that change of criticality can change importance of TC 2"(){
		given:
		RequirementCriticality oldRequirementCriticality = CRITICAL
		RequirementCriticality newCriticality = MAJOR
		TestCaseImportance importance = HIGH
		when:
		boolean canChange = importance.changeOfCriticalityCanChangeImportanceAuto (oldRequirementCriticality, newCriticality)
		then:
		canChange == true
	}
	def "should say that change of criticality WON'T change importance of TC 1"(){
		given:
		RequirementCriticality oldRequirementCriticality = UNDEFINED
		RequirementCriticality newCriticality = MAJOR
		TestCaseImportance importance = HIGH
		when:
		boolean canChange = importance.changeOfCriticalityCanChangeImportanceAuto (oldRequirementCriticality, newCriticality)
		then:
		canChange == false
	}
	def "should say that change of criticality WON'T change importance of TC 2"(){
		given:
		RequirementCriticality oldRequirementCriticality = UNDEFINED
		RequirementCriticality newCriticality = CRITICAL
		TestCaseImportance importance = HIGH
		when:
		boolean canChange = importance.changeOfCriticalityCanChangeImportanceAuto (oldRequirementCriticality, newCriticality)
		then:
		canChange == false
	}
	def "should deduce new importance when add criticality"(){
		given:
		RequirementCriticality addedCrit = CRITICAL
		TestCaseImportance importance = MEDIUM
		when:
		TestCaseImportance newImp = importance.deduceNewImporanceWhenAddCriticality (addedCrit)
		then:
		newImp == HIGH
	}
}
