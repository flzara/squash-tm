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
package org.squashtest.tm.service.internal.repository

import org.squashtest.it.basespecs.DbunitDaoSpecification
import org.unitils.dbunit.annotation.DataSet
import spock.unitils.UnitilsSupport

import javax.inject.Inject

/**
 * @author Gregory Fouquet
 *
 */
@UnitilsSupport
class IterationTestPlanDaoIT extends DbunitDaoSpecification {
	@Inject
	IterationTestPlanDao dao

	@DataSet("IterationTestPlanItemDao.test plan without suite.xml")
	def "should fetch test plan items ordered according to iteration"() {
		when:
		def res = dao.findAllByIdsOrderedByIterationTestPlan([-113L, -112L, -111L])

		then:
		res*.id == [-112L, -111L, -113L]
	}
	@DataSet("IterationTestPlanItemDao.test plan with suite.xml")
	def "should fetch test plan items ordered according to test suite"() {
		when:
		def res = dao.findAllByIdsOrderedBySuiteTestPlan([-111L, -112L, -113L], -1101L)

		then:
		res*.id == [-113L, -111L, -112L]
	}

	@DataSet("IterationTestPlanItemDao.items with tc automated and not automated.xml")
	def "should find all items with test case automated by idIteration"(){
		when:
		def res = dao.findAllByIterationIdWithTCAutomated(-110).sort()
		then:
		res*.id.size() == 2
		res*.id.containsAll([ -114L, -111L])
	}
	@DataSet("IterationTestPlanItemDao.items with tc automated and not automated.xml")
	def "should find all items with test case automated by items"(){
		when:
		def res = dao.findAllByItemsIdWithTCAutomated([-111L, -112L, -113L, -114L]).sort()
		then:
		res*.id.size() == 2
		res*.id.containsAll([ -114L, -111L])
	}
	@DataSet("IterationTestPlanItemDao.items with tc automated and not automated.xml")
	def "should find all items with test case automated by testSuites"(){
		when:
		def res = dao.findAllByTestSuiteIdWithTCAutomated(-1101)
		then:
		res*.id == [ -111L]
	}


}
