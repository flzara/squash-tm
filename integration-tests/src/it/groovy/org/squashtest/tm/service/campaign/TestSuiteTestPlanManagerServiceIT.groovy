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
import org.squashtest.tm.domain.campaign.Iteration
import org.squashtest.tm.domain.campaign.TestSuite
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.squashtest.tm.service.internal.repository.IterationDao
import org.squashtest.tm.service.internal.repository.TestSuiteDao
import org.unitils.dbunit.annotation.DataSet
import spock.unitils.UnitilsSupport

import javax.inject.Inject

@NotThreadSafe
@UnitilsSupport
@Transactional
	class TestSuiteTestPlanManagerServiceIT extends DbunitServiceSpecification {

	@Inject
	private TestSuiteTestPlanManagerService service

	@Inject
	private TestSuiteDao testSuiteDao;

	@Inject
	private IterationDao iterationDao

	def findTestPlan(testSuite) {
		getSession().createQuery("from IterationTestPlanItem it  join it.testSuites ts where ts = :suite")
			.setParameter("suite", testSuite)
			.list()
	}

	@DataSet("TestSuiteTestPlanManager.should link test plan to test Suite.xml")
	def "should add the test plan items to the iteration as they are bound to the test suite"() {

		given:
		long testSuiteId = -1L

		when:
		service.addTestCasesToIterationAndTestSuite([-1L, -2L, -3L, -4L], testSuiteId);
		TestSuite ts = testSuiteDao.findOne(-1L)
		Iteration iter = ts.getIteration()

		then:
		findTestPlan(ts).size() == 4
		iter.getTestPlans().size() == 4
	}

	@DataSet("TestSuiteTestPlanManager.should keep test plan on iteration.xml")
	def "should keep test plan on iteration"() {

		given:
		long testSuiteId = -1L

		when:
		service.detachTestPlanFromTestSuite([-1L, -2L], testSuiteId)
		TestSuite ts = testSuiteDao.findOne(-1L)
		Iteration iter = ts.getIteration()

		then:
		findTestPlan(ts).size() == 2
		iter.getTestPlans().size() == 4
	}

	@DataSet("TestSuiteTestPlanManager.should keep test plan on iteration.xml")
	def "should take away test plan from iteration as well as test suite"() {

		given:
		long testSuiteId = -1L

		when:
		service.detachTestPlanFromTestSuiteAndRemoveFromIteration([-1L, -2L], testSuiteId)
		TestSuite ts = testSuiteDao.findOne(-1L)
		Iteration iter = ts.getIteration()

		then:
		findTestPlan(ts).size() == 2
		iter.getTestPlans().size() == 2
	}


	@DataSet("TestSuiteTestPlanManager.should add one test plan item to two test suites.xml")
	def "should add one test plan item to two test suites"() {

		given:

		long testSuiteId1 = -1L
		long testSuiteId2 = -2L
		long itemId = -1L

		when:
		List<Long> testSuiteIds = new ArrayList<Long>();
		testSuiteIds.add(testSuiteId1);
		testSuiteIds.add(testSuiteId2);

		List<Long> itemIds = new ArrayList<Long>();
		itemIds.add(itemId);

		service.bindTestPlanToMultipleSuites(testSuiteIds, itemIds);

		then:

		TestSuite suite1 = testSuiteDao.findOne(-1L);
		suite1.getTestPlan().size() == 1;

		TestSuite suite2 = testSuiteDao.findOne(-2L);
		suite2.getTestPlan().size() == 1;
	}

	@DataSet("TestSuiteTestPlanManager.should add two test plan items to two test suites.xml")
	def "should add two test plan item to two test suites"() {

		given:

		long testSuiteId1 = -1L
		long testSuiteId2 = -2L
		long itemId1 = -1L
		long itemId2 = -2L

		when:

		List<Long> testSuiteIds = new ArrayList<Long>();
		testSuiteIds.add(testSuiteId1);
		testSuiteIds.add(testSuiteId2);

		List<Long> itemIds = new ArrayList<Long>();
		itemIds.add(itemId1);
		itemIds.add(itemId2);

		service.bindTestPlanToMultipleSuites(testSuiteIds, itemIds);

		then:

		TestSuite suite1 = testSuiteDao.findOne(-1L);
		suite1.getTestPlan().size() == 2;

		TestSuite suite2 = testSuiteDao.findOne(-2L);
		suite2.getTestPlan().size() == 2;
	}

	@DataSet("TestSuiteTestPlanManager.should add two test plan items to two test suites with test plan items.xml")
	def "should add two test plan item to two test suites with test plan items"() {

		given:

		long testSuiteId1 = -1L
		long testSuiteId2 = -2L
		long itemId1 = -1L
		long itemId2 = -2L

		when:

		List<Long> testSuiteIds = new ArrayList<Long>();
		testSuiteIds.add(testSuiteId1);
		testSuiteIds.add(testSuiteId2);

		List<Long> itemIds = new ArrayList<Long>();
		itemIds.add(itemId1);
		itemIds.add(itemId2);

		service.bindTestPlanToMultipleSuites(testSuiteIds, itemIds);

		then:

		TestSuite suite1 = testSuiteDao.findOne(-1L);
		suite1.getTestPlan().size() == 3;

		TestSuite suite2 = testSuiteDao.findOne(-2L);
		suite2.getTestPlan().size() == 3;

		Iteration iteration = iterationDao.findById(-1L);
		iteration.getTestPlans().size() == 4;
		iteration.getTestSuites().size() == 2;
	}
}
