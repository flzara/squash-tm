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
package org.squashtest.tm.domain.search

import org.squashtest.tm.domain.customfield.CustomFieldValue;

import spock.lang.Specification
import spock.lang.Unroll;

/**
 * @author Gregory Fouquet
 *
 */
class CUFBridgeTest extends Specification {
	CUFBridge bridge = new CUFBridge()

	@Unroll
	def "should format #fieldValue date as #formatted"() {
		given:
		CustomFieldValue value = Mock()
		value.value >> fieldValue

		expect:
		formatted == bridge.coerceToDate(value);

		where:
		fieldValue   | formatted
		null	     | null
		""	         | null
		"   "        | null
		"abc"	     | null
		"2013-04-01" | { def cal = Calendar.getInstance(); cal.clear(); cal.set(2013, 3, 1); return cal.time; }.call()
	}
}
