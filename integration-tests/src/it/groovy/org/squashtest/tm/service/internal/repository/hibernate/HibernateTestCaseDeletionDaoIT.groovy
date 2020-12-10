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
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.transaction.annotation.Transactional
import org.squashtest.it.basespecs.DbunitDaoSpecification
import org.squashtest.tm.domain.campaign.Campaign
import org.squashtest.tm.domain.campaign.Iteration
import org.squashtest.tm.domain.campaign.IterationTestPlanItem
import org.squashtest.tm.domain.campaign.TestSuite
import org.squashtest.tm.domain.execution.Execution
import org.squashtest.tm.domain.execution.ExecutionStep
import org.squashtest.tm.domain.requirement.Requirement
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.testcase.TestCaseFolder
import org.squashtest.tm.domain.testcase.TestCaseLibrary
import org.squashtest.tm.service.internal.repository.TestCaseDeletionDao
import org.squashtest.tm.tools.unittest.assertions.CollectionAssertions
import org.unitils.dbunit.annotation.DataSet
import org.unitils.dbunit.datasetloadstrategy.impl.CleanInsertLoadStrategy
import spock.unitils.UnitilsSupport

import javax.inject.Inject
import javax.sql.DataSource

@NotThreadSafe
@UnitilsSupport
@Transactional
class HibernateTestCaseDeletionDaoIT extends DbunitDaoSpecification{

	@Inject
	private TestCaseDeletionDao deletionDao;

	@Inject
	DataSource dataSource;

	def setup() {
		CollectionAssertions.declareContainsExactlyIds()
		addDataSource();
	}


	//since [TM-362]: no more cascade remove on Attachment => an  AttachmenContent may be linked to many Attachments
	//ManyToOne instead OneToOne.
	// "deletionDao.removeAttachmentsLists" is no more called. An exception is raised on use.
	// For IT test, see now AttachmentManagerServiceImpliIT
//	@DataSet("NodeDeletionDaoTest.should cascade delete on attachments.xml")
//	def "should cascade-remove an attachment list"(){
//
//		when :
//		deletionDao.removeAttachmentsLists([-1L, -4L]);
//
//		then :
//
//		found("ATTACHMENT_CONTENT", "attachment_content_id", -121L)
//		found("ATTACHMENT_CONTENT", "attachment_content_id", -1111L)
//		!found("ATTACHMENT_CONTENT", "attachment_content_id", -111L)
//		!found("ATTACHMENT_CONTENT", "attachment_content_id", -1211L)
//		!found("ATTACHMENT_CONTENT", "attachment_content_id", -1212L)
//
//		found("ATTACHMENT", "attachment_id", -121L)
//		found("ATTACHMENT", "attachment_id", -1111L)
//		!found("ATTACHMENT", "attachment_id", -111L)
//		!found("ATTACHMENT", "attachment_id", -1211L)
//		!found("ATTACHMENT", "attachment_id", -1212L)
//
//		found("ATTACHMENT_LIST", "attachment_list_id", -2L)
//		found("ATTACHMENT_LIST", "attachment_list_id", -3L)
//		!found("ATTACHMENT_LIST", "attachment_list_id", -1L)
//		!found("ATTACHMENT_LIST", "attachment_list_id", -4L)
//	}

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

	@DataSet("NodeDeletionDaoTest.should remove a keyword test case from its folder.xml")
	def "should remove a keyword test case list, one at root, other from its folder"(){

		when :
		deletionDao.removeEntities([-11L, -15L])
		getSession().flush()
		getSession().clear()

		def folder = findEntity(TestCaseFolder.class, -1L)

		then :
		found ("TEST_CASE", "tcln_id", -12L)
		!found("TEST_CASE", "tcln_id", -11L)
		!found("TEST_CASE", "tcln_id", -15L)

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
		deletionDao.removeAllSteps([-111L, -112L, -113L, -114L])

		getSession().flush()
		getSession().clear()

		def testCaseAfter = findEntity(TestCase.class, -11L)

		then :
		! found("TEST_STEP", "TEST_STEP_ID", -111L)
		! found("TEST_STEP", "TEST_STEP_ID", -112L)
		! found("TEST_STEP", "TEST_STEP_ID", -113L)
		! found("TEST_STEP", "TEST_STEP_ID", -114L)

		! found("ACTION_TEST_STEP", "TEST_STEP_ID", -111L)
		! found("CALL_TEST_STEP", "TEST_STEP_ID", -112L)
		! found("KEYWORD_TEST_STEP", "TEST_STEP_ID", -113L)
		! found("KEYWORD_TEST_STEP", "TEST_STEP_ID", -114L)

		! found("ACTION_WORD_PARAMETER_VALUE", "ACTION_WORD_PARAMETER_VALUE_ID", -1L)
		! found("ACTION_WORD_PARAMETER_VALUE", "ACTION_WORD_PARAMETER_VALUE_ID", -2L)

		! found("TEST_CASE_STEPS", "STEP_ID", -111L)
		! found("TEST_CASE_STEPS", "STEP_ID", -112L)
		! found("TEST_CASE_STEPS", "STEP_ID", -113L)
		! found("TEST_CASE_STEPS", "STEP_ID", -114L)

		testCaseAfter.steps.size() == 0
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

		def it1 = findEntity(Iteration.class, -153L)
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

	/*
	 * This test set corresponds to a bug where TC deletion fails because of reordered ITPIs on Postgres 9.6 (and possibly
	 * other versions).
	 */
	@DataSet("NodeDeletionHandlerTest.should update iteration test plan after TC deletion.xml")
	def "should update iteration test plan after TC deletion"(){
		when :
		deletionDao.removeOrSetIterationTestPlanInboundReferencesToNull([-1L, -3L])

		then :
		found("ITERATION_TEST_PLAN_ITEM", "item_test_plan_id", -12L)
		found("ITERATION_TEST_PLAN_ITEM", "item_test_plan_id", -14L)
		found("ITERATION_TEST_PLAN_ITEM", "item_test_plan_id", -22L)
		found("ITERATION_TEST_PLAN_ITEM", "item_test_plan_id", -24L)

		// ITPIs were kept because they have executions
		found("ITERATION_TEST_PLAN_ITEM", "item_test_plan_id", -11L)
		found("ITERATION_TEST_PLAN_ITEM", "item_test_plan_id", -21L)

		!found("ITERATION_TEST_PLAN_ITEM", "item_test_plan_id", -13L)
		!found("ITERATION_TEST_PLAN_ITEM", "item_test_plan_id", -23L)

		def itpi1 = findEntity(IterationTestPlanItem.class, -11L)
		itpi1.referencedTestCase == null

		def itpi2 = findEntity(IterationTestPlanItem.class, -14L)
		itpi2.referencedTestCase != null

		def it1 = findEntity(Iteration.class, -153L)
		it1.testPlans.size() == 3
		it1.testPlans[0].id == -11L
		it1.testPlans[1].id == -14L
		it1.testPlans[2].id == -12L

		def it2 = findEntity(Iteration.class, -2L)
		it2.testPlans.size() == 3
		it2.testPlans[0].id == -22L
		it2.testPlans[1].id == -24L
		it2.testPlans[2].id == -21L

		it2.testSuites.size() == 2

		def ts1 = findEntity(TestSuite.class, -1L)
		ts1.testPlan.size() == 2
		ts1.testPlan[0].id == -24L
		ts1.testPlan[1].id == -22L

		def ts2 = findEntity(TestSuite.class, -2L)
		ts2.testPlan.size() == 2
		ts2.testPlan[0].id == -21L
		ts2.testPlan[1].id == -24L

		def it3 = findEntity(Iteration.class, -3L)
		it3.testPlans.size() == 4
		it3.testPlans[0].id == -32L
		it3.testPlans[1].id == -33L
		it3.testPlans[2].id == -34L
		it3.testPlans[3].id == -31L
	}

	@DataSet("NodeDeletionHandlerTest.should update iteration test plan after TC deletion.xml")
	def "should remove test case reference without reordering test plans for ITPI with execution"(){
		when :
		deletionDao.removeOrSetIterationTestPlanInboundReferencesToNull([-4L])

		then :
		def itpi = findEntity(IterationTestPlanItem.class, -14L)
		itpi != null
		itpi.referencedTestCase == null

		! found("ITERATION_TEST_PLAN_ITEM", "item_test_plan_id", -34L)
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

		c1.testPlan.size() == 2
		c2.testPlan.size() == 2
		c3.testPlan.size() == 2

		c1.testPlan.containsExactlyIds([-11L, -14L])
		c2.testPlan.containsExactlyIds([-24L, -21L])
		c3.testPlan.containsExactlyIds([-31L, -34L])
	}

	/*
	 * This test set corresponds to a bug where TC deletion fails because of reordered CTPIs on Postgres 9.6 (and possibly
	 * other versions).
	 */
	@DataSet("NodeDeletionDaoTest.should update campaign test plan after TC deletion.xml")
	def "should update campaign test plan after TC deletion"(){

		when :
		deletionDao.removeCampaignTestPlanInboundReferences([-2L])

		then :
		found("CAMPAIGN_TEST_PLAN_ITEM", "ctpi_id", -12L)
		found("CAMPAIGN_TEST_PLAN_ITEM", "ctpi_id", -13L)
		found("CAMPAIGN_TEST_PLAN_ITEM", "ctpi_id", -14L)

		!found("CAMPAIGN_TEST_PLAN_ITEM", "ctpi_id", -11L)

		def c1 = findEntity(Campaign.class, -1L)
		c1.testPlan.size() == 3
		c1.testPlan[0].id == -13L
		c1.testPlan[1].id == -12L
		c1.testPlan[2].id == -14L

		def c2 = findEntity(Campaign.class, -2L)
		c2.testPlan.size() == 3
		c2.testPlan[0].id == -24L
		c2.testPlan[1].id == -23L
		c2.testPlan[2].id == -21L

		def c3 = findEntity(Campaign.class, -3L)
		c3.testPlan.size() == 3
		c3.testPlan[0].id == -31L
		c3.testPlan[1].id == -33L
		c3.testPlan[2].id == -34L
	}

	@DataSet("NodeDeletionDaoTest.should update campaign test plan after TC deletion.xml")
	def "should delete all campaign item test plans"(){

		when :
		deletionDao.removeCampaignTestPlanInboundReferences([-1, -2L, -3L, -4L])

		then :
		def c1 = findEntity(Campaign.class, -1L)
		def c2 = findEntity(Campaign.class, -2L)
		def c3 = findEntity(Campaign.class, -3L)

		c1.testPlan.isEmpty()
		c2.testPlan.isEmpty()
		c3.testPlan.isEmpty()
	}


	// ********************* private stuffs **********************

	def addDataSource() {
		String url = dataSource.getConnection().getMetaData().getURL();
		DataSourceProperties ds = Mock();
		ds.getUrl() >> url;
		deletionDao.dataSourceProperties = ds;
	}


}

