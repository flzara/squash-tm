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
package org.squashtest.tm.service.internal.repository.hibernate

import javax.inject.Inject

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.transaction.annotation.Transactional
import org.hibernate.Query
import org.squashtest.tm.core.foundation.collection.PagingAndSorting
import org.squashtest.tm.core.foundation.collection.SortOrder
import org.squashtest.tm.service.internal.repository.RequirementVersionDao
import org.squashtest.tm.service.internal.repository.RequirementDeletionDao
import org.squashtest.tm.tools.unittest.assertions.CollectionAssertions
import org.squashtest.tm.tools.unittest.assertions.ListAssertions
import org.squashtest.it.basespecs.DbunitDaoSpecification;
import org.unitils.dbunit.annotation.DataSet

import spock.unitils.UnitilsSupport

@UnitilsSupport
class HibernateRequirementVersionDaoIT extends DbunitDaoSpecification {
	@Inject RequirementVersionDao versionDao
	@Inject RequirementDeletionDao deletionDao

	def setup() {
		CollectionAssertions.declareContainsExactlyIds()
		ListAssertions.declareIdsEqual()
	}


	@DataSet("HibernateTestCaseDaoIT.should find requirement versions directly verified by a test case sorted by name.xml")
	def "should count requirements verified by a test case"() {
		when:
		def count = versionDao.countVerifiedByTestCase(-100)

		then:
		count == 2
	}



	@DataSet("HibernateTestCaseDaoIT.should count versions of requirement.xml")
	def "should find versions of requirement"() {
		given:

		PageRequest pr = new PageRequest(1, 1, Direction.ASC, "versionNumber")

		when:
		def res = versionDao.findAllByRequirementId(-1l, pr)

		then:
		res.content*.id == [-20L]
	}

	@DataSet("HibernateRequirementVersionDaoIT.should find all requirements versions by id.xml")
	def "should find all requirements versions by id"() {
		when:
		def res = versionDao.findAll([-10L, -20L])

		then:
		res.containsExactlyIds([-10L, -20L])
	}
}
