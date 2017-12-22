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


import static org.squashtest.tm.domain.testcase.TestCaseImportance.*

import org.squashtest.tm.domain.LevelComparator

import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Gregory Fouquet
 *
 */
class LevelComparatorTest extends Specification {
	LevelComparator comparator = new LevelComparator()
	
	@Unroll("#higher should be lower than #lower")
	def "high priority should be smaller than low priority"() {
		when:
		def res = comparator.compare(higher, lower)
		
		then:
		res < 0
		
		where:
		higher    | lower
		VERY_HIGH | HIGH
		HIGH      | MEDIUM
		MEDIUM    | LOW
	}

	def "null should be smaller than anything"() {
		expect:
		comparator.compare(null, VERY_HIGH) < 0
	}	
	def "anything should be greater than null"() {
		expect:
		comparator.compare(VERY_HIGH, null) > 0
	}	
}
