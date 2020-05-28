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

import org.squashtest.it.basespecs.DbunitDaoSpecification
import org.squashtest.tm.core.foundation.collection.ColumnFiltering
import org.squashtest.tm.core.foundation.collection.PagingAndMultiSorting
import org.squashtest.tm.core.foundation.collection.Sorting
import org.squashtest.tm.domain.testautomation.AutomatedSuite
import org.squashtest.tm.service.internal.repository.AutomatedSuiteDao
import org.unitils.dbunit.annotation.DataSet
import spock.unitils.UnitilsSupport

import javax.inject.Inject

import static org.squashtest.tm.domain.execution.ExecutionStatus.FAILURE
import static org.squashtest.tm.domain.execution.ExecutionStatus.RUNNING;


@UnitilsSupport
@DataSet("HibernateAutomatedSuiteDaoIT.sandbox.xml")
public class HibernateAutomatedSuiteDaoIT extends DbunitDaoSpecification {

	@Inject
	AutomatedSuiteDao suiteDao;

	def suiteid = "-12345"
	def iterationId = -1L
	def testSuiteId = -11L


	def "should create a new suite"(){

		expect :
		def suite = suiteDao.createNewSuite()
		suite.id != null

	}

	def "should find all the extenders associated to that suite"(){

		when :
		def extenders = suiteDao.findAllExtenders(suiteid)

		then :
		extenders*.id as Set == [-110L, -120L, -130L, -210L, -220L] as Set

	}

	def "should find all the extenders of executions waiting to be run"(){

		expect :
		suiteDao.findAllWaitingExtenders(suiteid)*.id == [ -210L ]

	}

	def "should find all the extenders of executions currently running"(){

		expect :
		suiteDao.findAllRunningExtenders(suiteid)*.id == [ -130L ]

	}

	def "should find all completed executions"(){

		expect :
		suiteDao.findAllCompletedExtenders(suiteid)*.id as Set == [ -110L, -120L, -220L] as Set

	}

	def "should find all extenders by statys"(){

		expect :
		suiteDao.findAllExtendersByStatus(suiteid, [FAILURE, RUNNING])*.id as Set == [-220L, -130L] as Set

	}

	def "should find all suites linked to an iteration"(){

		given:
		ColumnFiltering filter = ColumnFiltering.UNFILTERED
		PagingAndMultiSorting paging = new TestPagingMultiSorting()

		when:

		List<AutomatedSuite> result = suiteDao.findAutomatedSuitesByIterationID(iterationId, paging, filter)

		then :

		result*.id as Set == ["-12345", "-6789"] as Set

	}

	def "should count all suites linked to an iteration"(){

		given:
		ColumnFiltering filter = ColumnFiltering.UNFILTERED

		expect :

		suiteDao.countSuitesByIterationId(iterationId, filter) == 2

	}

	def "should find all suites linked to a test suite"(){

		given:
		ColumnFiltering filter = ColumnFiltering.UNFILTERED
		PagingAndMultiSorting paging = new TestPagingMultiSorting()

		when:

		List<AutomatedSuite> result = suiteDao.findAutomatedSuitesByTestSuiteID(testSuiteId, paging, filter)

		then :

		result*.id as List == ["-12345"]

	}

	def "should count all suites linked to a test suite"(){

		given:
		ColumnFiltering filter = ColumnFiltering.UNFILTERED

		expect :

		suiteDao.countSuitesByTestSuiteId(testSuiteId, filter) == 1

	}

}

class TestPagingMultiSorting implements PagingAndMultiSorting{

	@Override
	int getFirstItemIndex() {
		return 0
	}

	@Override
	int getPageSize() {
		return 50
	}

	@Override
	boolean shouldDisplayAll() {
		return false
	}

	@Override
	List<Sorting> getSortings() {
		return Collections.emptyList()
	}

}
