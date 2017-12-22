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
import org.squashtest.tm.domain.campaign.Campaign
import org.squashtest.tm.domain.campaign.CampaignTestPlanItem
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.domain.projectfilter.ProjectFilter
import org.squashtest.tm.domain.testcase.Dataset
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.testcase.TestCaseFolder
import org.squashtest.tm.domain.testcase.TestCaseLibrary
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode
import org.squashtest.tm.domain.users.User
import org.squashtest.tm.service.internal.library.LibrarySelectionStrategy
import org.squashtest.tm.service.internal.project.ProjectFilterModificationServiceImpl
import org.squashtest.tm.service.internal.repository.CampaignDao
import org.squashtest.tm.service.internal.repository.CampaignTestPlanItemDao
import org.squashtest.tm.service.internal.repository.LibraryNodeDao
import org.squashtest.tm.service.internal.repository.TestCaseLibraryDao
import org.squashtest.tm.service.internal.repository.UserDao
import org.squashtest.tm.service.milestone.ActiveMilestoneHolder
import org.squashtest.tm.service.testutils.MockFactory

import spock.lang.Specification

import java.util.Optional


class CampaignTestPlanManagerServiceImplTest extends Specification {

	CampaignTestPlanManagerServiceImpl service = new CampaignTestPlanManagerServiceImpl()
	TestCaseLibraryDao testCaseLibraryDao = Mock()
	LibraryNodeDao<TestCaseLibraryNode> nodeDao = Mock()
	CampaignDao campaignDao = Mock()
	CampaignTestPlanItemDao itemDao = Mock()
	ProjectFilterModificationServiceImpl projectFilterModificationService = Mock()
	LibrarySelectionStrategy<TestCaseLibrary, TestCaseLibraryNode> libraryStrategy = Mock()
	UserDao userDao = Mock()
	ActiveMilestoneHolder activeMilestoneHolder = Mock()

	MockFactory mockFactory = new MockFactory()

	def setup(){
		service.testCaseLibraryDao = testCaseLibraryDao
		service.campaignDao = campaignDao
		service.projectFilterModificationService = projectFilterModificationService
		service.libraryStrategy = libraryStrategy
		service.campaignTestPlanItemDao = itemDao
		service.testCaseLibraryNodeDao = nodeDao
		service.userDao = userDao
		service.activeMilestoneHolder = activeMilestoneHolder
		activeMilestoneHolder.getActiveMilestone() >> Optional.empty()
	}

	def "should find campaign by id"(){

		given: "a campaign"
		Campaign expectedCamp = Mock()
		campaignDao.findById(10L) >> expectedCamp

		when:
		def actualCamp = service.findCampaign(10L)

		then:
		actualCamp == expectedCamp
	}

	def "should find linkable test case library"() {

		given: "a test case library"
		TestCaseLibrary lib = Mock()
		ProjectFilter pf = new ProjectFilter()
		pf.setActivated(false)
		projectFilterModificationService.findProjectFilterByUserLogin() >> pf
		testCaseLibraryDao.findAll() >> [lib]

		when:
		def res =
				service.findLinkableTestCaseLibraries()

		then:
		res == [lib]
	}


	def "should add a test case only once to a campaign because of no dataset"() {

		given: "a campaign"
		Campaign camp = new Campaign()
		campaignDao.findById(10) >> camp

		and: "some test cases"
		def tc1 = new MockTC(1L)

		and : "the dao"
		nodeDao.findAllByIds([1L]) >> [tc1]

		when: "the test case is added to the campaign test plan"
		service.addTestCasesToCampaignTestPlan([1L], 10)

		then: "the campaign contains one test case, only once"
		camp.testPlan.size() == 1
		tc1 == camp.testPlan[0].referencedTestCase
		null == camp.testPlan[0].referencedDataset
	}

	def "should add a test case three times to a campaign because of it has datasets"() {

		given: "a campaign"
		Campaign camp = new Campaign()
		campaignDao.findById(10) >> camp

		and: "some test cases"
		def tc1 = new MockTC(1L)

		and : "the dao"
		nodeDao.findAllByIds([1L]) >> [tc1]

		and : "the datasets"

		["like this", "like that", "alternative"].each{
			tc1.addDataset(new Dataset(name:it))
		}

		when: "the test case is added to the campaign test plan"
		service.addTestCasesToCampaignTestPlan([1L], 10)

		then: "the campaign contains one test case, only once"
		camp.testPlan.size() == 3
		camp.testPlan.collect { it.referencedTestCase }.unique() == [tc1]
		camp.testPlan.collect { it.referencedDataset.name } as Set == ["like this", "like that", "alternative"] as Set
	}

	def "should add a list of test cases to a campaign"() {

		given: "a campaign"
		Campaign camp = new Campaign()
		campaignDao.findById(10) >> camp

		and: "some test cases"
		def tc1 = new MockTC(1L)
		def tc2 = new MockTC(2l)
		def tc3 = new MockTC(3l)


		and : "the dao"
		nodeDao.findAllByIds([1L, 2L, 3L]) >> [tc1, tc2, tc3]

		and : "the datasets"
		tc2.addDataset new Dataset(name:"when it works")
		["low grade", "medium grade", "high grade"].each{
			tc3.addDataset(new Dataset(name:it))
		}

		when: "the test cases are added to the campaign"
		service.addTestCasesToCampaignTestPlan([1L, 2L, 3L], 10)

		then: "the campaign contains the test cases added"
		camp.testPlan.size() ==5

		camp.testPlan.findAll { it.referencedTestCase == tc1 }.size() == 1
		camp.testPlan.findAll { it.referencedTestCase == tc2 }.size() == 1
		camp.testPlan.findAll { it.referencedTestCase == tc3 }.size() == 3

		camp.testPlan.findAll { it.referencedTestCase == tc1 }.collect { it.referencedDataset }== [null]
		camp.testPlan.findAll { it.referencedTestCase == tc2 }.collect { it.referencedDataset.name } == ["when it works"]
		camp.testPlan.findAll { it.referencedTestCase == tc3 }.collect { it.referencedDataset.name } as Set == ["low grade", "medium grade", "high grade"] as Set
	}


	def "should reccursively add a list of test cases to a campaign"() {
		given: "a campaign"
		Campaign camp = new Campaign()
		campaignDao.findById(10) >> camp

		and : "a bunch of folders and testcases"
		def folder1 = new MockTCF(1L, "f1")
		def folder2 = new MockTCF(2L, "f2")
		def tc1 = new MockTC(3L, "tc1")
		def tc2 = new MockTC(4L, "tc2")
		def tc3 = new MockTC(5L, "tc3")

		folder1.addContent(tc1)
		folder1.addContent(folder2)
		folder2.addContent(tc2)

		nodeDao.findAllByIds([1L, 5L]) >> [tc3, folder1] //note that we reversed the order here to test the sorting
		when: "the test cases are added to the campaign"
		service.addTestCasesToCampaignTestPlan([1L, 5L], 10)

		then :
		def collected = camp.getTestPlan().collect{it.referencedTestCase}
		/*we'll test here that :
		 the content of collected states that tc3 is positioned last,
		 collected contains tc1 and tc2 in an undefined order in first position (since the content of a folder is a Set)
		 */
		collected[0..1] == [tc1, tc2]|| [tc2, tc1]
		collected[2] == tc3

	}

	def "should remove a single test plan item from a campaign"() {
		given:
		TestCase tc = Mock()
		tc.id >> 2

		and:
		Campaign camp = new Campaign()
		campaignDao.findById(10) >> camp

		CampaignTestPlanItem itp1 = new CampaignTestPlanItem(tc)
		CampaignTestPlanItem itp2 = new CampaignTestPlanItem(tc)
		CampaignTestPlanItem itp3 = new CampaignTestPlanItem(tc)

		use (ReflectionCategory) {
			CampaignTestPlanItem.set field: "id", of: itp1, to: 1L
			camp.testPlan << itp1

			CampaignTestPlanItem.set field: "id", of: itp2, to: 2L
			camp.testPlan << itp2

			CampaignTestPlanItem.set field: "id", of: itp3, to: 3L
			camp.testPlan << itp3
		}

		when: "a test case is removed from the campaign"
		service.removeTestPlanItem 10, 2

		then: "the campaign should contain all but the removed test case"
		camp.testPlan == [itp1, itp3]
	}

	def "should remove test plan items from a campaign"() {
		given:
		TestCase tc = Mock()
		tc.id >> 2

		and:
		Campaign camp = new Campaign()
		campaignDao.findById(10) >> camp

		CampaignTestPlanItem itp1 = new CampaignTestPlanItem(tc)
		CampaignTestPlanItem itp2 = new CampaignTestPlanItem(tc)
		CampaignTestPlanItem itp3 = new CampaignTestPlanItem(tc)

		use (ReflectionCategory) {
			CampaignTestPlanItem.set field: "id", of: itp1, to: 1L
			camp.testPlan << itp1

			CampaignTestPlanItem.set field: "id", of: itp2, to: 2L
			camp.testPlan << itp2

			CampaignTestPlanItem.set field: "id", of: itp3, to: 3L
			camp.testPlan << itp3
		}

		when: "a test case is removed from the campaign"
		service.removeTestPlanItems 10, [1L, 3L]

		then: "the campaign should contain all but the removed test case"
		camp.testPlan == [itp2]
	}

	def "should persist new items added to the test plan"() {
		given: "a campaign"
		Campaign camp = new Campaign()
		campaignDao.findById(10) >> camp

		and: "a test case"
		def tc1 = new MockTC(1L)
		nodeDao.findAllByIds([1L]) >> [tc1]

		when: "the test cases are added to the campaign"
		service.addTestCasesToCampaignTestPlan([1L], 10)

		then: "a new test plan item has been persisted"
		1 * itemDao.persist(_)
	}


	def "should persist items already in the test plan but only one call"() {
		given: "a test case"
		def tc1 = new MockTC(1L)
		nodeDao.findAllByIds([1L]) >> [tc1]

		and: "a campaign with the test case in its test plan"
		Campaign camp = new Campaign()
		campaignDao.findById(10) >> camp

		CampaignTestPlanItem tpi = new CampaignTestPlanItem(referencedTestCase: tc1)
		camp.testPlan << tpi


		when:
		service.addTestCasesToCampaignTestPlan([1L], 10)

		then:
		1 * itemDao.persist(_)
	}

	class MockTC extends TestCase{
		Long overId
		MockTC(Long id){
			overId = id
			name="don't care "+id
		}
		MockTC(Long id, String name){
			this(id)
			this.name=name
		}
		public Long getId(){return overId}
		public void setId(Long newId){overId=newId}

	}

	class MockTCF extends TestCaseFolder{
		Long overId
		Project p


		MockTCF(Long id){
			overId = id
			name="don't care"

			this.p = mockFactory.mockProject()
			this.notifyAssociatedWithProject(this.p)

		}
		MockTCF(Long id, String name){
			this(id)
			this.name=name
		}
		public Long getId(){return overId}
		public void setId(Long newId){overId=newId}
	}



	def "should assign user to test plan items"() {
		given:
		User u = Mock()
		userDao.findOne(10L) >> u

		and:
		CampaignTestPlanItem i100 = Mock()
		CampaignTestPlanItem i200 = Mock()
		itemDao.findAllByIds([100L, 200L]) >> [i100, i200]

		when:
		service.assignUserToTestPlanItems([100L, 200L], 10000L, 10L)

		then:
		1 * i100.setUser(u)
		1 * i200.setUser(u)
	}

	def "should assign user to test plan item"() {
		given:
		User u = Mock()
		userDao.findOne(10L) >> u

		and:
		CampaignTestPlanItem item = Mock()
		itemDao.findById(100L) >> item

		when:
		service.assignUserToTestPlanItem(100L, 10000L, 10L)

		then:
		1 * item.setUser(u)
	}

}
