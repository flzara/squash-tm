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
package org.squashtest.tm.service.internal.campaign

import org.squashtest.tm.tools.unittest.reflection.ReflectionCategory
import org.squashtest.tm.domain.campaign.Iteration
import org.squashtest.tm.domain.campaign.IterationTestPlanItem
import org.squashtest.tm.domain.testcase.Dataset
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.testcase.TestCaseFolder
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode
import org.squashtest.tm.domain.users.User
import org.squashtest.tm.service.advancedsearch.IndexationService
import org.squashtest.tm.service.internal.repository.DatasetDao
import org.squashtest.tm.service.internal.repository.IterationDao
import org.squashtest.tm.service.internal.repository.IterationTestPlanDao
import org.squashtest.tm.service.internal.repository.LibraryNodeDao
import org.squashtest.tm.service.milestone.ActiveMilestoneHolder
import org.squashtest.tm.service.testutils.MockFactory

import spock.lang.Specification
import spock.lang.Unroll

import java.util.Optional

public class IterationTestPlanManagerServiceImplTest extends Specification {


	MockFactory mockFactory = new MockFactory()

	IterationTestPlanManagerServiceImpl service = new IterationTestPlanManagerServiceImpl();

	LibraryNodeDao<TestCaseLibraryNode> nodeDao = Mock()
	IterationDao iterDao = Mock()
	IterationTestPlanDao itemDao = Mock()
	DatasetDao datasetDao = Mock()
	IndexationService indexationService = Mock()
	CampaignNodeDeletionHandler deletionHandler = Mock()
	ActiveMilestoneHolder activeMilestoneHolder = Mock()

	def setup() {
		service.testCaseLibraryNodeDao = nodeDao
		service.iterationDao = iterDao
		service.iterationTestPlanDao = itemDao
		service.datasetDao = datasetDao
		service.indexationService = indexationService
		service.deletionHandler = deletionHandler
		service.activeMilestoneHolder = activeMilestoneHolder
		activeMilestoneHolder.getActiveMilestone() >> Optional.empty()
	}

	def "should reccursively add a list of test cases to an iteration"() {
		given: "a campaign"
		Iteration iteration = new Iteration()
		iterDao.findById(10) >> iteration
		use(ReflectionCategory) {
			Iteration.set field: "id", of: iteration, to: 10L
		}

		and: "a bunch of folders and testcases"
		def folder1 = MockTCF(1L, "f1")
		def folder2 = MockTCF(2L, "f2")
		def tc1 = MockTC(3L, "tc1")
		def tc2 = MockTC(4L, "tc2")
		def tc3 = MockTC(5L, "tc3")

		folder1.addContent(tc1)
		folder1.addContent(folder2)
		folder2.addContent(tc2)

		nodeDao.findAllByIds([1L, 5L]) >> [tc3, folder1] //note that we reversed the order here to test the sorting
		when: "the test cases are added to the campaign"
		service.addTestCasesToIteration([1L, 5L], 10)

		then:
		def collected = iteration.getTestPlans().collect({ it.referencedTestCase })
		/*we'll test here that :
		 the content of collected states that tc3 is positioned last,
		 collected contains tc1 and tc2 in an undefined order in first position (since the content of a folder is a Set)
		 */
		collected[0..1] == [tc1, tc2] || [tc2, tc1]
		collected[2] == tc3
	}


	def "should move a test case"() {
		given:
		TestCase tc1 = Mock()
		TestCase tc2 = Mock()
		TestCase tc3 = Mock()
		IterationTestPlanItem itp1 = Mock()
		IterationTestPlanItem itp2 = Mock()
		IterationTestPlanItem itp3 = Mock()
		itp1.isTestCaseDeleted() >> false
		itp2.isTestCaseDeleted() >> false
		itp3.isTestCaseDeleted() >> false
		itp1.getReferencedTestCase() >> tc1
		itp2.getReferencedTestCase() >> tc2
		itp3.getReferencedTestCase() >> tc3
		Iteration iteration = new Iteration()
		iteration.addTestPlan(itp1)
		iteration.addTestPlan(itp2)
		iteration.addTestPlan(itp3)
		iterDao.findById(_) >> iteration
		itemDao.findAllByIdIn(_) >> [itp3]

		when:
		service.changeTestPlanPosition(5, 0, [600])

		then:
		iteration.getPlannedTestCase() == [tc3, tc1, tc2]
	}

	@Unroll
	def "should create test plan fragment using datasets #datasets"() {
		given:
		TestCase testCase = Mock()
		User user = Mock()

		and:
		datasetDao.findOwnDatasetsByTestCase(_) >> datasets

		and:
		def expectedFragSize = Math.max(datasets.size(), 1) // on empty dataset there should be one item

		when:
		Collection<IterationTestPlanItem> frag = service.createTestPlanFragment(testCase, user)

		then:
		frag.size() == expectedFragSize
		frag*.referencedTestCase.inject(true) { res, it -> res && it == testCase } // reduces collection to true when all items equal testCase
		frag*.user.inject(true) { res, it -> res && it == user } // reduces to true when all assignees equal user
		frag*.referencedDataset.containsAll(datasets)

		where:
		datasets << [[], [Mock(Dataset)], [Mock(Dataset), Mock(Dataset)]]
	}

	def MockTC(def id, def name) {
		TestCase tc = new TestCase(name: name)
		use(ReflectionCategory) {
			TestCaseLibraryNode.set field: "id", of: tc, to: id
		}
		return tc
	}

	def MockTCF(def id, def name) {
		TestCaseFolder f = new TestCaseFolder(name: name)
		use(ReflectionCategory) {
			TestCaseLibraryNode.set field: "id", of: f, to: id
		}

		f.notifyAssociatedWithProject(mockFactory.mockProject())

		return f
	}

	def "should remove test plan item from iteration by calling deletion handler"() {
		given:
		IterationTestPlanItem item = Mock()
		Iteration iteration = Mock()
		item.getIteration() >> iteration
		item.getExecutions() >> Collections.emptyList()
		item.getReferencedTestCase() >> null

		itemDao.findById(1L) >> item
		when:
		service.removeTestPlanFromIteration(1L)

		then:
		1 * deletionHandler.deleteIterationTestPlanItem(item);

	}


}

