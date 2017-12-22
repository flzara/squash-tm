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
package org.squashtest.tm.validation.validator

import org.squashtest.tm.validation.validator.UniqueListItemsValidator;

import spock.lang.Specification
import spock.lang.Unroll;

/**
 * @author Gregory Fouquet
 *
 */
class UniqueListItemsValidatorTest extends Specification {
	UniqueListItemsValidator validator = new UniqueListItemsValidator()
	
	@Unroll("List #list should be valid")
	def "list should be valid"() {
		expect:
		validator.isValid list, null
		
		where:
		list << [ null, [], ["a"], ["a", "b"] ]
	}
	
	@Unroll("List #list should not be valid")
	def "list should not be valid"() {
		expect:
		!validator.isValid(list, null)
		
		where:
		list << [ ["a", "a"], ["a", "b", "a"] ]
	}
}
