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
/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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
package org.squashtest.tm.service.internal.repository.hibernate

import org.springframework.transaction.annotation.Transactional
import org.squashtest.tm.core.foundation.collection.*
import org.unitils.dbunit.annotation.DataSet
import spock.unitils.UnitilsSupport
import org.squashtest.tm.core.foundation.collection.PagingAndSorting
import org.squashtest.it.basespecs.DbunitDaoSpecification;

import javax.inject.Inject

@UnitilsSupport
@Transactional
class HibernateTeamDaoIT extends DbunitDaoSpecification {

	@Inject
	TeamDaoImpl dao


	@DataSet("HibernateTeamDaoIT.setup.xml")
	def "should sort the teams by name desc"() {

		given:
		PagingAndSorting paging = new DefaultPagingAndSorting(sortedAttribute: "Team.name", order: SortOrder.DESCENDING)

		and:
		Filtering filter = DefaultFiltering.NO_FILTERING

		when:
		def res = dao.findSortedTeams(paging, filter)

		then:
		res*.name == ["triple team", "simple team", "double team"]


	}

	@DataSet("HibernateTeamDaoIT.setup.xml")
	def "should sort the teams by size desc"() {

		given:
		PagingAndSorting paging = new DefaultPagingAndSorting(sortedAttribute: "Team.members.size", order: SortOrder.DESCENDING)

		and:
		Filtering filter = DefaultFiltering.NO_FILTERING

		when:
		def res = dao.findSortedTeams(paging, filter)

		then:
		res*.name == ["triple team", "double team", "simple team"]


	}

	@DataSet("HibernateTeamDaoIT.setup.xml")
	def "should sort the teams by size asc and looking only for those having 'ple' in their name"() {

		given:
		PagingAndSorting paging = new DefaultPagingAndSorting(sortedAttribute: "Team.members.size", order: SortOrder.ASCENDING)

		and:
		Filtering filter = new DefaultFiltering(null, "ple")

		when:
		def res = dao.findSortedTeams(paging, filter)

		then:
		res*.name == ["simple team", "triple team"]


	}

	@DataSet("HibernateTeamDaoIT.setup.xml")
	def "should retrieve teams id"(){

		when:
		def teamIds = dao.findTeamIds(userID)

		then:
		teamIds.sort().equals(expectedTeamIds)

		where:
		userID 	|| expectedTeamIds
		-1L 	|| [-300L,-200L,-100L]
		-2L 	|| [-300L,-100L]
		-3L 	|| [-300L]
		-4L 	|| []


	}

}
