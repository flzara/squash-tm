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
package org.squashtest.tm.service.connectionhistory

import org.springframework.transaction.annotation.Transactional
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder
import org.squashtest.tm.core.foundation.collection.PagingAndSorting
import org.squashtest.tm.core.foundation.collection.ColumnFiltering;
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.squashtest.tm.service.connectionhistory.ConnectionLogFinderService;
import org.squashtest.tm.domain.users.ConnectionLog;
import org.unitils.dbunit.annotation.DataSet
import spock.lang.Unroll
import spock.unitils.UnitilsSupport

import javax.inject.Inject

import static org.squashtest.tm.core.foundation.collection.SortOrder.ASCENDING
import static org.squashtest.tm.core.foundation.collection.SortOrder.DESCENDING

/**
 * @author aguilhem
 *
 */
@UnitilsSupport
@Transactional
class ConnectionLogFinderServiceIT extends DbunitServiceSpecification {

	@Inject
	ConnectionLogFinderService service

	@DataSet("ConnectionLogFinderServiceIT.should find sorted connection logs.xml")
	@Unroll
	def "should find sorted connection logs"(){
		given : "the dataset , a paging"
		PagingAndSorting paging = Mock()
		paging.firstItemIndex >> start
		paging.pageSize >> pageSize
		paging.sortedAttribute >> sortAttr
		paging.sortOrder >> sortOrder

		and :"a filtering"
		ColumnFiltering filtering = Mock()
		filtering.getFilter("login") >> loginFilter
		filtering.getFilter("connection-date") >> dateFilter
		filtering.isDefined()>> filterDefined

		when :
		PagedCollectionHolder<List<ConnectionLog>> result = service.findAllFiltered(paging, filtering)

		then :
		result.items.size() == expected.size()
		result.items*.login.containsAll(expected)

		where :
		start | pageSize | sortAttr          | sortOrder  | filterDefined | loginFilter | dateFilter   | expected
		0     | 5        | "login"           | ASCENDING  | false         | ""          | ""           | ["FIVE", "FOUR", "ONE", "THREE", "TWO"]
		0     | 5        | "connection_date" | DESCENDING | false         | ""          | ""           | ["FIVE", "FOUR", "THREE", "TWO", "ONE"]
		0     | 2        | "login"           | ASCENDING  | false         | ""          | ""           | ["FIVE", "FOUR"]
		0     | 2        | "login"           | ASCENDING  | true          | "T"         | ""           | ["THREE", "TWO"]
		0     | 1        | "login"           | ASCENDING  | true          | ""          | "25/12/2017" | ["ONE"]
		2     | 3        | "id"              | ASCENDING  | false         | ""          | ""           | ["THREE", "FOUR", "FIVE"]
	}

}
