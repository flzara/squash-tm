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
package org.squashtest.tm.service.internal.foundation.collection

import org.squashtest.tm.core.foundation.collection.MultiSorting
import org.squashtest.tm.core.foundation.collection.SortOrder
import org.squashtest.tm.core.foundation.collection.Sorting

import spock.lang.Specification

/**
 * @author Gregory Fouquet
 *
 */
public class SortingUtilsTest extends Specification {

	def "should add multiple order by clause to query" () {
		given:
		String query = "from Bar"

		and:
		MultiSorting ms = Mock()
		Sorting s = Mock()
		s.sortedAttribute >> "Bar.foo"
		s.sortOrder >> SortOrder.ASCENDING
		Sorting s2 = Mock()
		s2.sortedAttribute >> "Bar.bar"
		s2.sortOrder >> SortOrder.DESCENDING
		ms.getSortings() >> [s, s2]

		when:
		def res = SortingUtils.addOrder(query, ms)

		then:
		res == "from Bar order by Bar.foo asc, Bar.bar desc"
	}

	def "should add single order by clause to query" () {
		given:
		String query = "from Bar"

		and:
		Sorting s = Mock()
		s.sortedAttribute >> "Bar.foo"
		s.sortOrder >> SortOrder.ASCENDING

		when:
		def res = SortingUtils.addOrder(query, s)

		then:
		res == "from Bar order by Bar.foo asc nulls first"
	}
}
