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

import org.spockframework.util.NotThreadSafe
import org.springframework.transaction.annotation.Transactional
import org.squashtest.tm.tools.unittest.assertions.CollectionAssertions
import org.squashtest.it.basespecs.DbunitDaoSpecification;
import org.squashtest.tm.domain.campaign.Campaign
import org.squashtest.tm.domain.campaign.Iteration
import org.squashtest.tm.domain.campaign.IterationTestPlanItem
import org.squashtest.tm.domain.execution.Execution
import org.squashtest.tm.domain.execution.ExecutionStep
import org.squashtest.tm.domain.requirement.Requirement
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.testcase.TestCaseFolder
import org.squashtest.tm.domain.testcase.TestCaseLibrary
import org.squashtest.tm.service.internal.repository.TestCaseDeletionDao
import org.unitils.dbunit.annotation.DataSet
import org.unitils.dbunit.datasetloadstrategy.impl.CleanInsertLoadStrategy
import spock.unitils.UnitilsSupport

import javax.inject.Inject

@NotThreadSafe
@UnitilsSupport
@Transactional
class HibernateTestCaseDeletionDaoIT extends DbunitDaoSpecification{

	@Inject
	private TestCaseDeletionDao deletionDao;

	def setup() {
		CollectionAssertions.declareContainsExactlyIds()
	}



	@DataSet("NodeDeletionDaoTest.should cascade delete on attachments.xml")
	def "should cascade-remove an attachment list"(){

		when :
		deletionDao.removeAttachmentsLists([-1L, -4L]);

		then :

		found("ATTACHMENT_CONTENT", "attachment_content_id", -121L)
		found("ATTACHMENT_CONTENT", "attachment_content_id", -1111L)
		!found("ATTACHMENT_CONTENT", "attachment_content_id", -111L)
		!found("ATTACHMENT_CONTENT", "attachment_content_id", -1211L)
		!found("ATTACHMENT_CONTENT", "attachment_content_id", -1212L)

		found("ATTACHMENT", "attachment_id", -121L)
		found("ATTACHMENT", "attachment_id", -1111L)
		!found("ATTACHMENT", "attachment_id", -111L)
		!found("ATTACHMENT", "attachment_id", -1211L)
		!found("ATTACHMENT", "attachment_id", -1212L)

		found("ATTACHMENT_LIST", "attachment_list_id", -2L)
		found("ATTACHMENT_LIST", "attachment_list_id", -3L)
		!found("ATTACHMENT_LIST", "attachment_list_id", -1L)
		!found("ATTACHMENT_LIST", "attachment_list_id", -4L)
	}



	@DataSet("NodeDeletionDaoTest.should remove a test case from its folder.xml")
	def "should remove a test case from its folder"(){

		when :
		deletionDao.removeEntities([-11L])
		getSession().flush()
		getSession().clear()

		def folder = findEntity(TestCaseFolder.class, -1L)

		then :
		found ("TEST_CASE", "tcln_id", -12L)
		!found("TEST_CASE", "tcln_id", -11L)


		folder.content.containsExactlyIds([-12L])
	}


	@DataSet("NodeDeletionDaoTest.should remove a folder from the root content.xml")
	def "should remove a folder from the root content"(){
		when :

		deletionDao.removeEntities([-13L])

		getSession().flush()
		getSession().clear()

		def library = findEntity(TestCaseLibrary.class, -1L)

		then :
		library.rootContent.containsExactlyIds([-11L])
	}


	@DataSet("NodeDeletionDaoTest.should have two steps.xml")
	def "should have two steps"(){

		when :
		def tc = findEntity(TestCase.class, -11L)
		then:
		tc.steps.containsExactlyIds([-111L, -112L])
	}



	@DataSet(value=[
		"NodeDeletionDaoTest.removal of test steps should disassociate them from their parents.xml"
	], loadStrategy=CleanInsertLoadStrategy.class)
	def "removal of test steps should disassociate them from their parents"(){



		when :
		deletionDao.removeAllSteps([-111L, -112L])

		getSession().flush()
		getSession().clear()

		def testCaseAfter = findEntity(TestCase.class, -11L)

		then :
		! found("TEST_STEP", "TEST_STEP_id", -111L)
		! found("TEST_STEP", "TEST_STEP_id", -112L)

		! found("ACTION_TEST_STEP", "TEST_STEP_id", -111L)
		! found("CALL_TEST_STEP", "TEST_STEP_id", -112L)

		! found("TEST_CASE_STEPS", "STEP_ID", -111L)
		! found("TEST_CASE_STEPS", "STEP_ID", -112L)

		testCaseAfter.steps.size()==0
	}



	@DataSet("NodeDeletionDaoTest.paired testcase requirement.xml")
	def "should cascade-disassociate a pair of testcase and requirement"(){

		when :

		deletionDao.removeFromVerifyingTestCaseLists([-11L])

		getSession().flush()
		getSession().clear()

		def requirement=findEntity( Requirement.class, -21L)

		then :

		found ("TEST_CASE", "tcln_id", -12L)
		requirement.currentVersion.verifyingTestCases.containsExactlyIds([-12L])
	}


	@DataSet("NodeDeletionDaoTest.should disassociate a test case from iteration test plan and execution.xml")
	def "should disassociate a test case from iteration test plan and execution"(){
		given :
		def itemTestPlan_1 = findEntity(IterationTestPlanItem.class, -51L)
		def execution_1 = findEntity(Execution.class, -61L)

		// force initialization their referenced test case because of session.clear below,
		// that might trigger a lazy initialization in the then: block
		def itpReferId = itemTestPlan_1.referencedTestCase.id
		def execReferId = execution_1.referencedTestCase.id

		when :

		deletionDao.removeOrSetIterationTestPlanInboundReferencesToNull([-11L])
		deletionDao.setExecutionInboundReferencesToNull([-11L])

		getSession().flush()
		getSession().clear()

		def itemTestPlan_2 = findEntity(IterationTestPlanItem.class, -51L)
		def execution_2 = findEntity(Execution.class, -61L)

		then :

		itpReferId == -11
		execReferId == -11

		itemTestPlan_2.referencedTestCase == null
		execution_2.referencedTestCase == null
	}


	@DataSet("NodeDeletionHandlerTest.should disassociate from two item test plan and remove two.xml")
	def "should disassociate from two item test plan having executions and remove two other having no executions, for two iterations "(){
		when :
		deletionDao.removeOrSetIterationTestPlanInboundReferencesToNull([-2L, -3L]);

		then :

		found("ITERATION_TEST_PLAN_ITEM", "item_test_plan_id", -11L)
		found("ITERATION_TEST_PLAN_ITEM", "item_test_plan_id", -14L)
		found("ITERATION_TEST_PLAN_ITEM", "item_test_plan_id", -21L)
		found("ITERATION_TEST_PLAN_ITEM", "item_test_plan_id", -22L)
		found("ITERATION_TEST_PLAN_ITEM", "item_test_plan_id", -24L)

		!found("ITERATION_TEST_PLAN_ITEM", "item_test_plan_id", -12L)
		!found("ITERATION_TEST_PLAN_ITEM", "item_test_plan_id", -13L)
		!found("ITERATION_TEST_PLAN_ITEM", "item_test_plan_id", -23L)

		def it1 = findEntity(Iteration.class, -1L)
		def it2 = findEntity(Iteration.class, -2L)

		it1.testPlans.size() == 2
		it2.testPlans.size() == 3

		it1.testPlans.containsExactlyIds([-14L, -11L])
		it2.testPlans.containsExactlyIds([-21L, -22L, -24L])

		def randomItp = findEntity(IterationTestPlanItem.class, -11L)
		randomItp.referencedTestCase.id == -1L

		def itp2 = findEntity(IterationTestPlanItem.class, -22L )
		itp2.referencedTestCase == null
	}



	@DataSet("NodeDeletionDaoTest.should disassociate exec steps.xml")
	def "should disassociate a test step from calling exec steps"(){

		when :
		deletionDao.setExecStepInboundReferencesToNull([-11L, -14L])


		then :
		findEntity(ExecutionStep.class, -11L).referencedTestStep == null
		findEntity(ExecutionStep.class, -12L).referencedTestStep.id == -12L
		findEntity(ExecutionStep.class, -13L).referencedTestStep.id == -13L
		findEntity(ExecutionStep.class, -14L).referencedTestStep == null
		findEntity(ExecutionStep.class, -21L).referencedTestStep == null
		findEntity(ExecutionStep.class, -22L).referencedTestStep.id == -12L
		findEntity(ExecutionStep.class, -23L).referencedTestStep.id == -13L
		findEntity(ExecutionStep.class, -24L).referencedTestStep == null
	}


	@DataSet("NodeDeletionDaoTest.shouldDeleteAndReorderCTPI.xml")
	def "should delete and reorder campaign item test plans that were calling deleted test cases"(){

		when :
		deletionDao.removeCampaignTestPlanInboundReferences([-2L, -3L])

		then :
		found("CAMPAIGN_TEST_PLAN_ITEM", "ctpi_id", -11L)
		found("CAMPAIGN_TEST_PLAN_ITEM", "ctpi_id", -14L)
		found("CAMPAIGN_TEST_PLAN_ITEM", "ctpi_id", -21L)
		found("CAMPAIGN_TEST_PLAN_ITEM", "ctpi_id", -24L)
		found("CAMPAIGN_TEST_PLAN_ITEM", "ctpi_id", -31L)
		found("CAMPAIGN_TEST_PLAN_ITEM", "ctpi_id", -34L)

		!found("CAMPAIGN_TEST_PLAN_ITEM", "ctpi_id", -12L)
		!found("CAMPAIGN_TEST_PLAN_ITEM", "ctpi_id", -13L)
		!found("CAMPAIGN_TEST_PLAN_ITEM", "ctpi_id", -22L)
		!found("CAMPAIGN_TEST_PLAN_ITEM", "ctpi_id", -23L)
		!found("CAMPAIGN_TEST_PLAN_ITEM", "ctpi_id", -32L)
		!found("CAMPAIGN_TEST_PLAN_ITEM", "ctpi_id", -33L)

		def c1 = findEntity(Campaign.class, -1L)
		def c2 = findEntity(Campaign.class, -2L)
		def c3 = findEntity(Campaign.class, -3L)

		c1.testPlan.size() ==2
		c2.testPlan.size() ==2
		c3.testPlan.size() ==2

		c1.testPlan.containsExactlyIds([-11L, -14L])
		c2.testPlan.containsExactlyIds([-24L, -21L])
		c3.testPlan.containsExactlyIds([-31L, -34L])
	}






}

