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

import org.squashtest.tm.domain.attachment.Attachment
import org.squashtest.tm.domain.campaign.Campaign
import org.squashtest.tm.domain.campaign.CampaignTestPlanItem
import org.squashtest.tm.domain.campaign.Iteration
import org.squashtest.tm.domain.campaign.IterationTestPlanItem
import org.squashtest.tm.domain.execution.Execution
import org.squashtest.tm.domain.infolist.InfoList
import org.squashtest.tm.domain.infolist.UserListItem
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.domain.testcase.*
import org.squashtest.tm.domain.users.User
import org.squashtest.tm.service.advancedsearch.IndexationService
import org.squashtest.tm.service.campaign.IterationTestPlanManagerService
import org.squashtest.tm.service.execution.ExecutionModificationService
import org.squashtest.tm.service.internal.customfield.PrivateCustomFieldValueService
import org.squashtest.tm.service.internal.denormalizedField.PrivateDenormalizedFieldValueService
import org.squashtest.tm.service.internal.repository.*
import org.squashtest.tm.service.testcase.TestCaseCyclicCallChecker
import spock.lang.Specification
import spock.lang.Unroll

class CustomIterationModificationServiceImplTest extends Specification {
	CustomIterationModificationServiceImpl service = new CustomIterationModificationServiceImpl()
	ExecutionDao execDao = Mock()

	IterationTestPlanDao testPlanDao = Mock()
	CampaignDao campaignDao = Mock()
	IterationDao iterationDao = Mock()
	TestCaseDao testCaseDao = Mock()

	TestCaseCyclicCallChecker cyclicCallChecker = Mock()

	PrivateCustomFieldValueService customFieldService = Mock()
	PrivateDenormalizedFieldValueService denormalizedFieldValueService = Mock();

	IterationTestPlanManagerService iterationTestPlanManager = Mock()
	IndexationService indexationService = Mock()

	ExecutionModificationService executionModificationService = Mock();

	def setup() {
		service.executionDao = execDao
		service.campaignDao = campaignDao
		service.testPlanDao = testPlanDao
		service.iterationDao = iterationDao
		service.testCaseCyclicCallChecker = cyclicCallChecker
		service.customFieldValueService = customFieldService
		service.denormalizedFieldValueService = denormalizedFieldValueService
		service.indexationService = indexationService
		service.executionModificationService = executionModificationService
	}

	def "should add unparameterized iteration to campaign with test plan"() {
		given:
		Iteration iteration = new Iteration()
		TestCase tc1 = Mock()
		tc1.getId() >> 1
		TestCase tc2 = Mock()
		tc2.getId() >> 2

		and:
		User user = Mock()
		Campaign campaign = new Campaign()
		CampaignTestPlanItem itp1 = new CampaignTestPlanItem(tc1)
		itp1.setUser(user)
		CampaignTestPlanItem itp2 = new CampaignTestPlanItem(tc2)
		itp2.setUser(user)
		campaign.addToTestPlan(itp1)
		campaign.addToTestPlan(itp2)
		campaignDao.findById(10) >> campaign

		and:
		def frag1 = IterationTestPlanItem.createTestPlanItems(tc1, null)
		iterationTestPlanManager.createTestPlanFragment(tc1, user) >> frag1

		and:
		def frag2 = IterationTestPlanItem.createTestPlanItems(tc2, null)
		iterationTestPlanManager.createTestPlanFragment(tc2, user) >> frag2

		when:
		service.addIterationToCampaign(iteration, 10, true)

		then:
		campaign.iterations.contains(iteration)
		1 * iterationDao.persistIterationAndTestPlan(iteration)
		iteration.testPlans*.referencedTestCase == [tc1, tc2]
	}

	def "should build an iteration test plan based on campaign test plan"() {
		given:
		Iteration iteration = new Iteration()
		TestCase tc1 = Mock()
		TestCase tc2 = Mock()

		and:
		User user = Mock()
		Campaign campaign = new Campaign()
		CampaignTestPlanItem ctp1 = new CampaignTestPlanItem(tc1)    // no dataset for this one
		ctp1.setUser(user)
		campaign.addToTestPlan(ctp1)

		CampaignTestPlanItem ctp21 = new CampaignTestPlanItem(tc2, Mock(Dataset))
		ctp21.setUser(user)
		campaign.addToTestPlan(ctp21)

		CampaignTestPlanItem ctp22 = new CampaignTestPlanItem(tc2, Mock(Dataset))
		ctp22.setUser(user)
		campaign.addToTestPlan(ctp22)

		campaignDao.findById(10) >> campaign


		when:
		service.addIterationToCampaign(iteration, 10, true)

		then:
		campaign.iterations.contains(iteration)
		1 * iterationDao.persistIterationAndTestPlan(iteration)
		iteration.plannedTestCase == [tc1, tc2, tc2]
		iteration.testPlans.collect { it.referencedTestCase } == [tc1, tc2, tc2]
		iteration.testPlans.collect { it.referencedDataset != null } == [false, true, true]
	}

	def "should add iteration to campaign without test plan"() {
		given:
		Iteration iteration = new Iteration()

		and:
		User user = Mock()
		Campaign campaign = new Campaign()
		TestCase tc1 = Mock()
		CampaignTestPlanItem itp1 = new CampaignTestPlanItem(tc1)
		itp1.setUser(user)
		campaign.addToTestPlan(itp1)
		campaignDao.findById(10) >> campaign

		when:
		service.addIterationToCampaign(iteration, 10, false)

		then:
		campaign.iterations.contains(iteration)
		1 * iterationDao.persistIterationAndTestPlan(iteration)
		iteration.getPlannedTestCase() == []
		iteration.getTestPlans().size() == 0
	}

	def "should return indice of added iteration"() {
		given:
		Iteration iteration = Mock()
		iteration.getName() >> "iteration"
		and:
		Iteration alreadyInCampaign = Mock();
		alreadyInCampaign.getName() >> "alreadyInCampaign"
		and:
		Campaign campaign = new Campaign()
		campaign.iterations << alreadyInCampaign
		campaignDao.findById(10) >> campaign

		when:
		def index = service.addIterationToCampaign(iteration, 10, true)

		then:
		index == 1
	}

	@Unroll("for id #id should get executions ids  #execsIds")
	def "should replace execution with new exec with correct order"() {

		given:
		def iteration = new MockIteration()
		TestCase testCase = Mock()
		testCase.getId() >> 1
		testCase.getSteps() >> []
		testCase.getExecutionMode() >> TestCaseExecutionMode.AUTOMATED
		testCase.getName() >> "test case"
		testCase.getAllAttachments() >> new HashSet<Attachment>()
		testCase.getPrerequisite() >> "prerequisite"
		testCase.getImportance() >> TestCaseImportance.LOW
		testCase.getNature() >> new UserListItem(code: "SOME_NATURE", infoList: Mock(InfoList))
		testCase.getType() >> new UserListItem(code: "SOME_TYPE", infoList: Mock(InfoList))
		testCase.getStatus() >> TestCaseStatus.WORK_IN_PROGRESS
		testCase.getDatasets() >> []
		IterationTestPlanItem testPlan = new IterationTestPlanItem(id: 1L, iteration: iteration)

		def execs = (1..4).collect {
			def exec = Mock(Execution)
			exec.getProject() >> new Project()
			exec.getExecutionOrder() >> it - 1
			exec.getId() >> it
			exec.getTestPlan() >> testPlan
			execDao.findOne(it) >> exec
			testPlan.addExecution exec
			return exec
		}

		executionModificationService.deleteExecution(_) >> { Execution execution ->
			testPlan.removeExecution(execution);
		}

		testPlan.setReferencedTestCase(testCase)
		iteration.addTestPlan testPlan


		when:
		Execution res = service.updateExecutionFromTc(id)

		then:
		res.testPlan.executions*.id == execsIds

		where:
		id || execsIds
		1L || [null, 2, 3, 4]
		2L || [1, null, 3, 4]
		3L || [1, 2, null, 4]
		4L || [1, 2, 3, null]
	}


	class MockIteration extends Iteration {

		MockIteration() {

		}

		public Project getProject() {
			Project project = new Project();
			return project;
		}
	}
}
