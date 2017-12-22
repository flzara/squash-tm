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

import org.springframework.data.domain.Pageable
import org.springframework.transaction.annotation.Transactional
import org.squashtest.tm.tools.unittest.assertions.ListAssertions
import org.squashtest.it.basespecs.DbunitDaoSpecification;
import org.squashtest.tm.core.foundation.collection.Paging
import org.squashtest.tm.core.foundation.collection.PagingAndSorting
import org.squashtest.tm.core.foundation.collection.SortOrder
import org.squashtest.tm.core.foundation.collection.SpringPaginationUtils
import org.squashtest.tm.domain.NamedReferencePair
import org.squashtest.tm.domain.testcase.TestCaseImportance
import org.squashtest.tm.service.internal.repository.TestCaseDao
import org.unitils.dbunit.annotation.DataSet
import spock.unitils.UnitilsSupport

import javax.inject.Inject
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

@UnitilsSupport
@Transactional
class HibernateTestCaseDaoIT extends DbunitDaoSpecification {
	@Inject
	TestCaseDao testCaseDao

	def setupSpec() {
		List.metaClass.containsSameIdentifiers << { ids ->
			assert delegate.size() == ids.size()
			assert (delegate*.id).containsAll(ids)
			true
		}

		ListAssertions.declareIdsEqual()
	}


	@DataSet("HibernateTestCaseDaoIT.should find filtered steps by test case id.xml")
	def "should find filtered steps by test case id"() {
		given:
		Paging filter = Mock()
		filter.firstItemIndex >> 1
		filter.pageSize >> 2

		when:
		def steps = testCaseDao.findAllStepsByIdFiltered(-10, filter)

		then:
		steps.size() == 2
		steps*.id.containsAll([-200L, -300L])
	}

	@DataSet("HibernateTestCaseDaoIT.should count calling test steps.xml")
	def "should count calling test steps"() {
		when:
		def callers = testCaseDao.countCallingTestSteps(-10L)

		then:
		callers == 1L
	}

	@DataSet("HibernateTestCaseDaoIT.should count calling test steps.xml")
	def "should count no calling test steps"() {
		when:
		def callers = testCaseDao.countCallingTestSteps(-20L)

		then:
		callers == 0L
	}

	@DataSet("HibernateTestCaseDaoIT.should find called test cases.xml")
	def "should find called test cases"() {
		when:
		def calleds = testCaseDao.findTestCasesHavingCaller([-10L, -30L])

		then:
		calleds == [-10L]
	}

	@DataSet("HibernateTestCaseDaoIT.should find called test cases.xml")
	def "should find no called test cases"() {
		when:
		def calleds = testCaseDao.findTestCasesHavingCaller([-30L])

		then:
		calleds == []
	}

	@DataSet("HibernateTestCaseDaoIT.should find ids of test cases called by several test cases.xml")
	def "should find ids of test cases called by several test cases"() {
		when:
		def callees = testCaseDao.findAllTestCasesIdsCalledByTestCases([-110L, -120L])

		then:
		callees.containsAll([-10L, -20L])
		callees.size() == 2
	}

	@DataSet("HibernateTestCaseDaoIT.should find distinct ids of test cases called by several test cases.xml")
	def "should find distinct ids of test cases called by several test cases"() {
		when:
		def callees = testCaseDao.findAllTestCasesIdsCalledByTestCases([-110L, -120L])

		then:
		callees == [-10L]
	}

	@DataSet("HibernateTestCaseDaoIT.should find the calling test cases.xml")
	def " (*) should find the UNIQUES calling test cases sorted by test case name"() {
		given:
		PagingAndSorting sorting = new PagingAndSorting() {

			@Override
			public int getFirstItemIndex() {
				return 0;
			}

			@Override
			public int getPageSize() {
				return 10;
			}

			@Override
			public SortOrder getSortOrder() {
				return SortOrder.ASCENDING;
			}


			@Override
			public String getSortedAttribute() {
				return "TestCase.name";
			}

			boolean shouldDisplayAll() {
				return false;
			};

			Pageable toPageable() {
				return SpringPaginationUtils.toPageable(this);
			}
		};

		and:
		def resultNames = [
			"first test case",
			"second test case",
			"third test case"
		]
		def resultIds = [-101L, -102L, -103L]


		when:
		def testCaseList = testCaseDao.findAllCallingTestCases(-100L, sorting);

		then:
		testCaseList*.id == resultIds
		testCaseList*.name == resultNames
	}


	private tcrefPair(callerid, callername, calledid, calledname) {
		new NamedReferencePair(callerid, callername, calledid, calledname)
	}

	@DataSet("HibernateTestCaseDaoIT.should find the calling test cases.xml")
	def "should find the callers of a couple of test cases, returning results as pairs of caller/called details"() {

		when:
		def result = testCaseDao.findTestCaseCallsUpstream([-100L, -50L]);
		then:
		result.size == 9

		/*
		 * nodes 101, 102 and 103 call node 100 2 times each
		 * nodes 101, 102 and 103 call node 50 1 time each
		 */


		def call1 = tcrefPair(
			-101L,
			"first test case",
			-50L,
			"other bottom test case"
		);
		def call2 = tcrefPair(
			-102L,
			"second test case",
			-50L,
			"other bottom test case"
		);
		def call3 = tcrefPair(
			-103L,
			"third test case",
			-50L,
			"other bottom test case"
		);
		def call4 = tcrefPair(
			-101L,
			"first test case",
			-100L,
			"bottom test case"
		);
		def call5 = tcrefPair(
			-102L,
			"second test case",
			-100L,
			"bottom test case"
		);
		def call6 = tcrefPair(
			-103L,
			"third test case",
			-100L,
			"bottom test case"
		);


		result.count { it.equals call1 } == 1
		result.count { it.equals call2 } == 1
		result.count { it.equals call3 } == 1
		result.count { it.equals call4 } == 2
		result.count { it.equals call5 } == 2
		result.count { it.equals call6 } == 2

	}


	@DataSet("HibernateTestCaseDaoIT.should find the calling test cases.xml")
	def "should find no callers of a couple of test cases, returning results as pairs of caller/called details with null values"() {

		when:
		def result = testCaseDao.findTestCaseCallsUpstream([-101L, -102L, -103L]);
		then:
		result.size == 3



		def call1 = tcrefPair(
			null,
			null,
			-101L,
			"first test case"
		);
		def call2 = tcrefPair(
			null,
			null,
			-102L,
			"second test case"
		);
		def call3 = tcrefPair(
			null,
			null,
			-103L,
			"third test case"
		);


		result.contains(call1)
		result.contains(call2)
		result.contains(call3)
	}


	@DataSet("HibernateTestCaseDaoIT.should find the calling test cases.xml")
	def "should find the called test cases of a couple of test cases, returning results as pairs of caller/called details"() {

		when:
		def result = testCaseDao.findTestCaseCallsDownstream([-101L, -103L]);
		then:
		result.size == 7

		/*
		 * nodes 101, 102 and 103 call node 100 2 times each
		 * nodes 101, 102 and 103 call node 50 1 time each
		 * node 103 calls node 5 1 time
		 */


		def call1 = tcrefPair(
			-101L,
			"first test case",
			-50L,
			"other bottom test case"
		);

		def call2 = tcrefPair(
			-103L,
			"third test case",
			-50L,
			"other bottom test case"
		);
		def call3 = tcrefPair(
			-101L,
			"first test case",
			-100L,
			"bottom test case"
		);
		def call4 = tcrefPair(
			-103L,
			"third test case",
			-100L,
			"bottom test case"
		);
		def call5 = tcrefPair(
			-103L,
			"third test case",
			-5L,
			"ultra bottom test case"
		);


		result.count { it.equals call1 } == 1
		result.count { it.equals call2 } == 1
		result.count { it.equals call3 } == 2
		result.count { it.equals call4 } == 2
		result.count { it.equals call5 } == 1


	}


	@DataSet("HibernateTestCaseDaoIT.should find the calling test cases.xml")
	def "should find no called test cases of a couple of test cases, returning results as pairs of details with null values"() {


		when:
		def result = testCaseDao.findTestCaseCallsDownstream([-100L, -5L]);
		then:
		result.size == 2



		def call1 = tcrefPair(
			-100L,
			"bottom test case",
			null,
			null
		);
		def call2 = tcrefPair(
			-5L,
			"ultra bottom test case",
			null,
			null
		);


		result.contains(call1)
		result.contains(call2)
	}


	@DataSet("HibernateTestCaseDaoIT.deletiontest.xml")
	def "should delete a test case and cascade-remove some relationships"() {

		when:
		def tc = testCaseDao.findById(-1L)
		testCaseDao.remove(tc)

		def retc = testCaseDao.findById(-1L)

		then:

		tc != null

		retc == null
	}


	@DataSet("HibernateTestCaseDaoIT.should return list of executions.xml")
	def "should return list of executions"() {
		when:
		def id = -10L;
		def result = testCaseDao.findAllExecutionByTestCase(id)

		then:
		result.size() == 3
		result.each { it.name == "testCase1-execution" }
	}

	@DataSet("HibernateTestCaseDaoIT.should find tc imp w impAuto.xml")
	def "should find tc imp w impAuto"() {
		given:
		def testIds = [-1L, -2L, -3L, -4L]
		when:
		Map<Long, TestCaseImportance> result = testCaseDao.findAllTestCaseImportanceWithImportanceAuto(testIds)
		then:
		result.size() == 2
		result.containsKey(-1L)
		result.containsKey(-3L)
		result.get(-1L) == TestCaseImportance.LOW
		result.get(-3L) == TestCaseImportance.MEDIUM
	}

	/*
	 * Dataset :
	 *
	 * test cases : [ id : name ]
	 *
	 * 	[239 : "1 to 10"],
	 * 	[240 : "2 to 10"],
	 * 	[241 : "1 to 5"],
	 * 	[242 : "3 to 7"],
	 * 	[243 : "6 to 10"],
	 * 	[244 : "1 to 8"]
	 *
	 * milestones : [ id : name : endDate ]
	 *
	 * [1 	: "jalon1" 	: 2015/01/01],
	 * [2 	: "jalon2" 	: 2015/02/01],
	 * [3 	: "jalon3" 	: 2015/03/01],
	 * [4 	: "jalon4" 	: 2015/04/01],
	 * [5 	: "jalon5" 	: 2015/05/01],
	 * [6 	: "jalon6" 	: 2015/06/01],
	 * [7 	: "jalon7" 	: 2015/07/01],
	 * [8 	: "jalon8" 	: 2015/08/01],
	 * [9 	: "jalon9" 	: 2015/09/01],
	 * [10 	: "jalon10" : 2015/10/01],
	 *
	 * The names of the test cases tells which milestones it belongs to.
	 *
	 * The purpose of that test is to sort the test cases by :
	 * min(dataset.endDate), then ref, finally the name.
	 *
	 * We want them as verifying test cases for the requirement version 255
	 *
	 */

	@DataSet("HibernateTestCaseDaoIT.verifying TC sorted by milestone.xml")
	def "should find verifying test cases sorted by milestone dates"() {

		given:

		PagingAndSorting pas = Mock(PagingAndSorting)
		pas.getSortedAttribute() >> "endDate"
		pas.getSortOrder() >> SortOrder.ASCENDING
		pas.shouldDisplayAll() >> true

		when:

		def res = testCaseDao.findAllByVerifiedRequirementVersion(255l, pas)

		then:
		res*.id == [239L, 241L, 244L, 240L, 242L, 243L]

	}

	// ************* scaffolding ************

	//cannot make it more groovy because java native code wouldn't mix well with other kinds of proxies
	class SearchCriteriaHandler implements InvocationHandler {

		Map attributes

		SearchCriteriaHandler(attributes) {
			this.attributes = attributes;
		}


		@Override
		public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {

			def mName = method.name

			def matchUses = mName =~ /uses(.*)/

			if (matchUses.find()) {
				return !(attributes[matchUses[0][1]].isEmpty())
			}

			def matchGet = mName =~ /get(.*)Set/

			if (matchGet.find()) {
				return attributes[matchGet[0][1]]
			}

			//others

			if (mName == "getNameFilter") {
				return attributes["NameFilter"]
			}

			if (mName == "includeFoldersInResult") {
				return attributes["ImportanceFilter"].isEmpty() &&
					attributes["NatureFilter"].isEmpty() &&
					attributes["TypeFilter"].isEmpty() &&
					attributes["StatusFilter"].isEmpty()
			}

			if (mName == "isGroupByProject") {
				return attributes["groupByProject"]
			}
		}
	}


}
