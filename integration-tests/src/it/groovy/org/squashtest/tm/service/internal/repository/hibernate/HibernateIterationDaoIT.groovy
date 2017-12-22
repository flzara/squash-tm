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

import org.hibernate.HibernateException
import org.squashtest.it.basespecs.DbunitDaoSpecification;
import org.squashtest.tm.core.foundation.collection.ColumnFiltering
import org.squashtest.tm.core.foundation.collection.DefaultColumnFiltering
import org.squashtest.tm.core.foundation.collection.Filtering
import org.squashtest.tm.core.foundation.collection.PagingAndMultiSorting
import org.squashtest.tm.domain.campaign.TestPlanStatistics
import org.squashtest.tm.domain.campaign.TestPlanStatus
import org.squashtest.tm.service.internal.repository.IterationDao
import org.unitils.dbunit.annotation.DataSet
import spock.lang.Unroll
import spock.unitils.UnitilsSupport

import javax.inject.Inject

@UnitilsSupport
class HibernateIterationDaoIT extends DbunitDaoSpecification {
	@Inject IterationDao iterationDao

	@DataSet("HibernateIterationDaoIT.should return list of executions.xml")
	def "should return list of executions"(){
		when:
		def result = iterationDao.findAllExecutionByIterationId (-2L)

		then:
		result.size() == 3
		result.each {it.name == "iteration2-execution"}
	}

	@DataSet("HibernateIterationDaoIT.should find iteration statistics.xml")
	def "should find test suite statistics READY"(){
		when:
		TestPlanStatistics result = iterationDao.getIterationStatistics(-1L)

		then:
		result != null
		result.nbBlocked == 0
		result.nbSuccess == 0
		result.nbReady == 3
		result.nbDone == 0
		result.nbRunning == 0
		result.nbTestCases == 3
		result.nbUntestable == 0
		result.progression == 0
		result.nbFailure == 0
		result.status == TestPlanStatus.READY
	}

	@Unroll
	@DataSet("HibernateIterationDaoIT.should find iteration statistics.xml")
	def "[Issue 2828] should not break when looking up indexed test plan with filtering : #hasFiltering"() {
		given:
		PagingAndMultiSorting sorting = Mock()
		ColumnFiltering columnFiltering = DefaultColumnFiltering.NO_FILTERING
		sorting.getSortings() >> []
		sorting.getFirstItemIndex() >> 0
		sorting.getPageSize() >> 500
		
		and:
		Filtering filtering = Mock()
		filtering.isDefined() >> hasFiltering
		 
		when:
		def res = iterationDao.findIndexedTestPlan(-1L, sorting, filtering, columnFiltering)
		
		then:
		notThrown(HibernateException)
		
		where: 
		hasFiltering << [true, false]
	}

	@Unroll
	@DataSet("HibernateIterationDaoIT.should find iteration statistics.xml")
	def "[Issue 2828] should not break when looking up test plan with filtering : #hasFiltering"() {
		given:
		PagingAndMultiSorting sorting = Mock()
		ColumnFiltering columnFiltering = DefaultColumnFiltering.NO_FILTERING
		sorting.getSortings() >> []
		sorting.getFirstItemIndex() >> 0
		sorting.getPageSize() >> 500
		
		and:
		Filtering filtering = Mock()
		filtering.isDefined() >> hasFiltering
		 
		when:
		def res = iterationDao.findTestPlan(-1L, sorting, filtering, columnFiltering)
		
		then:
		notThrown(HibernateException)
		
		where: 
		hasFiltering << [true, false]
	}
}
