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
package org.squashtest.tm.service.campaign

import org.spockframework.util.NotThreadSafe
import org.springframework.transaction.annotation.Transactional
import org.squashtest.it.utils.CollectionComparisonUtils
import org.squashtest.tm.core.foundation.collection.*
import org.squashtest.tm.domain.campaign.IterationTestPlanItem
import org.squashtest.tm.domain.users.User
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.unitils.dbunit.annotation.DataSet
import spock.lang.Unroll
import spock.unitils.UnitilsSupport

import javax.inject.Inject

@NotThreadSafe
@UnitilsSupport
@Transactional
class IterationTestPlanManagerServiceIT extends DbunitServiceSpecification {

	@Inject
	private IterationTestPlanManagerService service




	@DataSet("IterationTestPlanManagerServiceIT.xml")
	@Unroll("should fetch a test plan sorted by #attributes")
	def "should fetch a test plan sorted by some attributes"(){


		given :
		def pagingsorting = new TestPagingMultiSorting(attributes)
		def columnsorting = DefaultColumnFiltering.NO_FILTERING

		and:



		when :
		List items = service.findAssignedTestPlan(7000084L, pagingsorting, columnsorting).pagedItems
		List itemIds = items.collect { it.item.id }

		println items.collect { it.item.id  + it.item.testSuiteNames }

		then :
		CollectionComparisonUtils.matchesPartialOrder(itemIds , expectedItemIds)


		where :

		attributes																|	expectedItemIds
		//		"TestCase.name asc"														|	[70000280L, 70000277L, 70000276L, 70000275L, 70000274L, 70000279L, 70000278L, 70000269L, 70000268L, 70000267L, 70000266L, 70000273L, 70000272L, 70000271L, 70000270L]
		"suitenames asc"														|	[[70000266L, 70000269L, 70000272L, 70000275L], [70000267L, 70000276L, 70000278L], 70000277L, 70000268L, [70000270L, 70000273L], [70000274L, 70000279L], [70000271L, 70000280L]]
		//		"TestCase.importance asc"												|	[[70000266L, 70000272L, 70000275L, 70000277L], [70000269L, 70000273L, 70000280L], [70000268L, 70000270L, 70000276L, 70000278L], [70000267L, 70000271L, 70000274L, 70000279L]]
		//		"IterationTestPlanItem.executionStatus asc"								|	[70000270L, [70000266L, 70000271L, 70000273L, 70000274L, 70000280L], [70000267L, 70000272L, 70000279L], [70000268L, 70000275L, 70000277L], [70000269L, 70000276L, 70000278L]]
		//		"suitenames desc, TestCase.importance asc"								|	[70000280L, 70000271L, [70000274L, 70000279L], 70000273L, 70000270L, 70000268L, 70000277L, [70000276L, 70000278L], 70000267L, [70000266L, 70000272L, 70000275L], 70000269L]
		//		"TestCase.importance asc, TestCase.name desc"							|	[70000272L, 70000266L, 70000275L, 70000277L, 70000273L, 70000269L, 70000280L, 70000270L, 70000268L, 70000278L, 70000276L, 70000271L, 70000267L, 70000279L, 70000274L]
		//		"TestCase.importance asc, IterationTestPlanItem.executionStatus desc"	|	[[70000275L, 70000277L], 70000272L, 70000266L, 70000269L, [70000273L, 70000280L], [70000276L, 70000278L], 70000268L, 70000270L, [70000267L, 70000279L], [70000271L, 70000274L]]
	}


	class TestPagingMultiSorting implements PagingAndMultiSorting{

		List<Sorting> sortings = new ArrayList<Sorting>()

		public TestPagingMultiSorting(String definition){
			def matcher = definition =~ /([^ ]+) (asc|desc)/

			matcher.each {
				String attr = it[1]
				String strOrder = it[2]
				SortOrder order = strOrder.toUpperCase() + "ENDING"

				DefaultSorting sorting = new DefaultSorting()
				sorting.setSortedAttribute attr
				sorting.setSortOrder order
				sortings << sorting
			}
		}

		@Override
		public int getFirstItemIndex() {
			return 0
		}

		@Override
		public int getPageSize() {
			return 50
		}

		@Override
		public boolean shouldDisplayAll() {
			return false
		}

		@Override
		public List<Sorting> getSortings() {
			return sortings
		}

	}


	@DataSet("IterationTestPlanManagerServiceIT.1execution.xml")
	def "should remove executed Test plan from iteration because has admin rights"(){
		given :
		def iterationId = -1L
		def testPlanItem = -1L
		when :
		service.removeTestPlanFromIteration(testPlanItem)

		then :
		!found(IterationTestPlanItem.class, -1L)
	}

	//		TODO make it work
	//		@DataSet("IterationTestPlanManagerServiceIT.1execution.noEDRight.xml")
	//		def "should remove executed Test plan from iteration because has not EXTENDED_DELETE rights"(){
	//			given :
	//			def iterationId = -1L
	//			def testPlanItem = -1L
	//			when :
	//			service.removeTestPlanFromIteration(testPlanItem)
	//
	//			then :
	//			found(IterationTestPlanItem.class, -1L)
	//		}

	@DataSet("IterationTestPlanManagerServiceIT.0execution.xml")
	def "should remove not executed Test plan from iteration"(){
		given :
		def iterationId = -1L
		def testPlanItem = -1L
		when :
		service.removeTestPlanFromIteration(testPlanItem)

		then :
		!found(IterationTestPlanItem.class, -1L)
	}
}
