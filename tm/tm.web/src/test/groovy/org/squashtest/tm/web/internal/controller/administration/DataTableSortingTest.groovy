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
package org.squashtest.tm.web.internal.controller.administration

import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableSorting;
import org.squashtest.tm.web.internal.model.viewmapper.DatatableMapper;
import org.squashtest.tm.web.internal.model.viewmapper.IndexBasedMapper;

import spock.lang.Specification

/**
 * 
 * @author Gregory Fouquet
 *
 */
class DataTableSortingTest extends Specification {
	def "BugTrackerAdministationController.getBugtrackerTableModel(..) should not throw ClassCastException as per changeset 51e016135748"() {
		given:
		DatatableMapper<Integer> mapper=new IndexBasedMapper(6)
				.mapAttribute(2, "name", BugTracker.class)
				.mapAttribute(3, "kind", BugTracker.class)
				.mapAttribute(4, "url", BugTracker.class)
				.mapAttribute(5, "iframeFriendly", BugTracker.class)

		and:
		DataTableDrawParameters params = new DataTableDrawParameters()
		params.setiSortCol_0(2)
		params.setsSortDir_0("asc")

		and:
		PagingAndSorting pas =  new DataTableSorting(params, mapper)

		when:
		pas.getSortedAttribute()

		then:
		notThrown ClassCastException
	}
}
