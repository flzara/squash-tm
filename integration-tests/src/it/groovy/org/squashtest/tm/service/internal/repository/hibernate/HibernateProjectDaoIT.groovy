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

import org.squashtest.it.basespecs.DbunitDaoSpecification;
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.service.internal.repository.ProjectDao
import org.unitils.dbunit.annotation.DataSet
import spock.lang.IgnoreRest
import spock.unitils.UnitilsSupport

import javax.inject.Inject

@UnitilsSupport
class HibernateProjectDaoIT extends DbunitDaoSpecification {

	@Inject
	ProjectDao projectDao

	@DataSet("HibernateProjectDaoIT.xml")
	def "should return a list of existing project" () {
		when:
		List<Project> list = projectDao.findAll()

		then:
		list.size() == 3
	}

	@DataSet("HibernateProjectDaoIT.should count non folders 1.xml")
	def "should count non folders 1" () {
		when:
		Long count = projectDao.countNonFoldersInProject(-1L)

		then:
		count == 0
	}
	@DataSet("HibernateProjectDaoIT.should count non folders 2.xml")
	def "should count non folders 2" () {
		when:
		Long count = projectDao.countNonFoldersInProject(-1L)

		then:
		count == 3
	}
	@DataSet("HibernateProjectDaoIT.should count non folders 3.xml")
	def "should count non folders 3" () {
		when:
		Long count = projectDao.countNonFoldersInProject(-1L)

		then:
		count == 2
	}

	@DataSet("HibernateProjectDaoIT.sandbox.xml")
	def "should find readable project ids"(){

		when:
		def projectIds = projectDao.findAllProjectIds(partyIds);

		then:
		projectIds.sort() == expectedProjectIds.sort()

		where:
		partyIds 			|| expectedProjectIds
		[-200L,-1L]			|| [-4L,-3L,-2L,-1L]
		[-200L]				|| [-4L,-3L,-2L,-1L]
		[-1L]				|| [-1L]
		[-2L,-100L]			|| [-3L,-2L,-1L]
		[-3L]				|| []
	}
}
