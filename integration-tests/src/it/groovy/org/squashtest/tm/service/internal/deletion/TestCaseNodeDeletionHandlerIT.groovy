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
package org.squashtest.tm.service.internal.deletion;

import javax.inject.Inject

import org.springframework.transaction.annotation.Transactional
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.squashtest.tm.domain.testcase.CallTestStep
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.testcase.TestCaseFolder
import org.squashtest.tm.domain.testcase.TestCaseLibrary
import org.squashtest.tm.domain.testcase.TestStep
import org.squashtest.tm.domain.testcase.Dataset
import org.squashtest.tm.domain.testcase.Parameter
import org.squashtest.tm.domain.testcase.DatasetParamValue
import org.squashtest.tm.service.internal.repository.TestCaseDao
import org.squashtest.tm.service.internal.testcase.TestCaseNodeDeletionHandler;
import org.squashtest.tm.service.testcase.TestCaseLibraryNavigationService
import org.unitils.dbunit.annotation.DataSet

import spock.unitils.UnitilsSupport

@UnitilsSupport
@Transactional
public class TestCaseNodeDeletionHandlerIT extends DbunitServiceSpecification {

	@Inject
	private TestCaseNodeDeletionHandler deletionHandler;

	@Inject
	private TestCaseLibraryNavigationService tcNavService;

	@Inject
	private TestCaseDao tcDao;



	@DataSet("NodeDeletionHandlerTest.should not delete the test case because of a step call.xml")
	def "should not delete the test case because of a step call"(){

		when :
		def result = deletionHandler.deleteNodes([-12L])

		then :
		result.removed  == []
		found(TestCase.class, -12L)

	}

	@DataSet("NodeDeletionHandlerTest.delete_tc_cascade_steps.xml")
	def "should delete the test case and cascade to its steps"(){

		when :
		def result = deletionHandler.deleteNodes([-11L]);

		then :
		result.removed*.resid.containsAll([-11L])

		! found(TestCase.class, -11L)
		! found(TestStep.class, -111L)
		! found(TestStep.class, -112L)
		! found(CallTestStep.class, -112L)
		! found(Dataset.class, -112L)
		! found(Parameter.class, -112L)
		! found(DatasetParamValue.class, -112L)
		found (TestCase.class, -12L)

		allDeleted("CustomFieldValue", [-11L, -12L])
		allNotDeleted("CustomFieldValue", [-21L, -22L])
	}


	@DataSet("NodeDeletionHandlerTest.external caller test case.xml")
	def "should not delete a folder because one child is called by a non-deleted test case, the other test case is removed normally"(){

		when :
		def result = deletionHandler.deleteNodes([-1L]);

		then :
		result.removed*.resid.containsAll([-11L])
		found (TestCaseFolder.class, -1L)
		found (TestCase.class, -12L)			//that one is the test case called by the external caller test case
		! found (TestCase.class, -11L)
	}


	@DataSet("NodeDeletionHandlerTest.tc_hierarchy_cascade_delete.xml")
	def "should delete a folder and all its dependencies, Called tc are removed successfully because the caller is removed along it, so are the custom field values"(){

		when :
		def result = deletionHandler.deleteNodes([-1L]);

		then :
		result.removed.collect{it.resid}.containsAll([-1L, -11L, -12L])

		allDeleted("TestCase", [-11L, -12L])
		allDeleted("TestStep", [-111L, -112L, -121L])
		allDeleted("TestCaseFolder", [-1L])

		allDeleted("Attachment", [
			-111L,
			-121L,
			-1111L,
			-1211L,
			-1212L
		])
		allDeleted("AttachmentContent", [
			-111L,
			-121L,
			-1111L,
			-1211L,
			-1212L
		])
		allDeleted("AttachmentList", [-11L, -12L, -111L, -121L, -123L])	//issue 2899 : now checks that the attachment lists for folders are also deleted

		allDeleted("CustomFieldValue", [-11L, -12L, -21L, -22L])

		def lib = findEntity(TestCaseLibrary.class, -1L)
		lib.rootContent.size() == 0
	}

	@DataSet("TestCaseNodeDeletionHandlerIT.should delete a test-step along with its attachments.xml")
	def "should delete a test-step along with its attachments"(){
		given:
		TestCase owner = findEntity(TestCase.class, -11L);
		TestStep tStep = findEntity (TestStep.class, -111L);


		when :
		deletionHandler.deleteStep (owner, tStep);

		then :
		allDeleted("TestStep", [-111L])
		allDeleted("AttachmentList", [-111L])
		allDeleted("Attachment", [-1111L])
		allDeleted("AttachmentContent", [-1111L])

	}

	@DataSet("TestCaseNodeDeletionHandlerIT.should delete a test-step along with its attachments.xml")
	def "should delete a call step "(){
		given:
		TestCase owner = findEntity(TestCase.class, -11L)
		TestStep tStep = findEntity (TestStep.class, -112L)


		when :
		deletionHandler.deleteStep (owner, tStep)

		then :
		allDeleted("TestStep", [-112L])

	}

	@DataSet("NodeDeletionHandlerTest.should delete testSuites.xml")
	def "should delete a test case and reorder the (non executed) test plans that includes it"(){

		when :
		deletionHandler.deleteNodes([-100L])
		session.flush();

		def tsMaxOrder = session.createSQLQuery("select max(test_plan_order) from TEST_SUITE_TEST_PLAN_ITEM where suite_id = -1").uniqueResult()
		def itMaxOrder = session.createSQLQuery("select max(item_test_plan_order) from ITEM_TEST_PLAN_LIST where iteration_id=-11").uniqueResult()
		then :
		tsMaxOrder == 0	//only one element, max index 0
		itMaxOrder == 0 //only one element too because this test case was included twice
	}

}
