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
package org.squashtest.tm.service.internal.deletion

import org.spockframework.util.NotThreadSafe
import org.springframework.transaction.annotation.Transactional
import org.squashtest.tm.domain.attachment.AttachmentList
import org.squashtest.tm.domain.bugtracker.IssueList;
import org.squashtest.tm.domain.campaign.*
import org.squashtest.tm.domain.execution.Execution
import org.squashtest.tm.domain.execution.ExecutionStatus
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.squashtest.tm.service.campaign.CampaignLibraryNavigationService
import org.squashtest.tm.service.internal.campaign.CampaignNodeDeletionHandler
import org.unitils.dbunit.annotation.DataSet

import spock.lang.Unroll;
import spock.unitils.UnitilsSupport

import javax.inject.Inject

/*
 *
 * Note : that class wont test yet whether an entity is actually removable or not, since the implementation of the
 * class doesn't care of it yet.
 *
 * 2012-11-09 Note : the comment above still holds true today.
 *
 */

@NotThreadSafe
@UnitilsSupport
@Transactional
class CampaignNodeDeletionHandlerIT  extends DbunitServiceSpecification{

	@Inject
	private CampaignNodeDeletionHandler deletionHandler

	@Inject
	private CampaignLibraryNavigationService cNavService;

	/* ****** test of suppression itself, assume that they're all green for removal ************* */


	@DataSet("NodeDeletionHandlerTest.executionPlusSteps.xml")
	def "should delete an execution, its steps, their attachments and their issues"(){
		given :
		def exec = findEntity(Execution.class, -500L)

		when :
		deletionHandler.deleteExecution(exec)
		em.flush()

		then :		
		
		allDeleted("Attachment", [
			-5001L,
			-5002L,
			-60011L,
			-60012L,
			-60021L,
			-60022L,
			-60031L,
			-60032L
		])
		
		allDeleted("AttachmentList", [-500L, -6001L, -6002L, -6003L]);

		allDeleted("AttachmentContent", [
			-5001L,
			-5002L,
			-60011L,
			-60012L,
			-60021L,
			-60022L,
			-60031L,
			-60032L
		])

		allDeleted("IssueList", [-500L, -6001L, -6002L, -6003L])
		allDeleted("Issue", [
			-5001L,
			-5002L,
			-60011L,
			-60012L,
			-60021L,
			-60022L,
			-60031L,
			-60032L
		])


		allDeleted("Execution", [-500L])
		allDeleted("ExecutionStep", [-500L, -6001L, -6002L, -6003L])
	}
	@DataSet("NodeDeletionHandlerTest.executionPlusDenormalizedFields.xml")
	def "should delete an execution with all denormalized field values"(){
		given :
		def exec = findEntity(Execution.class, -500L)

		when :
		deletionHandler.deleteExecution(exec)

		then :
		allDeleted("DenormalizedFieldValue", [-1L,-2L,-3L,-4L]);
	}

	@DataSet("NodeDeletionHandlerTest.executionPlusCustomFields.xml")
	def "should delete an execution with all custom field values"(){
		given :
		def exec = findEntity(Execution.class, -500L)

		when :
		deletionHandler.deleteExecution(exec)

		then :
		allDeleted("CustomFieldValue", [-1L,-3L,-4L]);
	}



	@DataSet("NodeDeletionHandlerTest.iterationPlusExecutions.xml")
	def "should delete an execution but not the other"(){
		given :
		def exec = findEntity(Execution.class, -1111L)

		when :
		deletionHandler.deleteExecution(exec)

		then :
		
		// Exec -1111L is gone
		! found(AttachmentList, -1111L)
		! found(IssueList, -1111L)
		! found(Execution, -1111L)

		// Exec -1112L still there
		found(AttachmentList, -1112L)
		found(IssueList, -1112L)
		found(Execution, -1112L)

		def tp = findEntity(IterationTestPlanItem.class, -111L )
		tp.executions.size()==1
		tp.executions[0].id==-1112L
	}
	@DataSet("NodeDeletionHandlerTest.iterationPlusExecutionsStatus.xml")
	def "should delete an execution and update status and auto dates"(){
		given :
		def exec = findEntity(Execution.class, -1112L)

		when :
		deletionHandler.deleteExecution(exec)

		then :

		IterationTestPlanItem tp = findEntity(IterationTestPlanItem.class, -111L )
		tp.executionStatus == ExecutionStatus.READY
		tp.lastExecutedBy == null
		tp.lastExecutedOn == null
		Iteration iteration = tp.iteration
		iteration.actualEndDate == null
		Campaign campaign = iteration.campaign
		campaign.actualEndDate.date == 12
		campaign.actualEndDate.month +1  == 8
		campaign.actualEndDate.year +1900  == 2011
	}

	@DataSet("NodeDeletionHandlerTest.iterationPlusExecutionsStatus2.xml")
	def "should delete an execution and update status and auto dates 2"(){
		given :
		def exec = findEntity(Execution.class, -1112L)

		when :
		deletionHandler.deleteExecution(exec)

		then :

		IterationTestPlanItem tp = findEntity(IterationTestPlanItem.class, -111L )
		tp.executionStatus == ExecutionStatus.FAILURE
		tp.lastExecutedBy == "machin"
		tp.lastExecutedOn != null
		tp.lastExecutedOn.date == 18
		tp.lastExecutedOn.month +1 == 8
		tp.lastExecutedOn.year +1900 == 2011
		Iteration iteration = tp.iteration
		iteration.actualEndDate != null
		iteration.actualEndDate.date == 20
		iteration.actualEndDate.month +1 == 8
		iteration.actualEndDate.year +1900 == 2011
		iteration.actualStartDate != null
		iteration.actualStartDate.date == 18
		iteration.actualStartDate.month +1 == 8
		iteration.actualStartDate.year +1900 == 2011
		Campaign campaign = iteration.campaign
		campaign.actualEndDate != null
		campaign.actualEndDate.date == 20
		campaign.actualEndDate.month +1  == 8
		campaign.actualEndDate.year +1900  == 2011
		campaign.actualStartDate != null
		campaign.actualStartDate.date == 18
		campaign.actualStartDate.month +1  == 8
		campaign.actualStartDate.year +1900  == 2011
	}

	@DataSet("NodeDeletionHandlerTest.iterationPlusExecutions.xml")
	def "should remove a pair of iterations and the executions"(){

		when:
		deletionHandler.deleteIterations([-11L, -12L])


		then :

		allDeleted("AttachmentList", [
			-11L,
			-12L,
			-1111L,
			-1112L,
			-1121L,
			-1122L,
			-1211L,
			-1212L,
			-1221L,
			-1222L
		])
		allDeleted("IssueList", [
			-1111L,
			-1112L,
			-1121L,
			-1122L,
			-1211L,
			-1212L,
			-1221L,
			-1222L
		])
		allDeleted("Execution", [
			-1111L,
			-1112L,
			-1121L,
			-1122L,
			-1211L,
			-1212L,
			-1221L,
			-1222L
		])
		allDeleted("IterationTestPlanItem", [-111L, -112L, -121L, -122L])
		allDeleted("Iteration", [-11L, -12L])


		def cpg= findEntity(Campaign.class, -1L)
		cpg.iterations.size()==0
	}





	@DataSet("NodeDeletionHandlerTest.iterationPlusExecutions.xml")
	def "should remove only one iteration and its executions"(){

		when :

		deletionHandler.deleteIterations([-11L])


		then :

		allDeleted("AttachmentList", [
			-11L,
			-1111L,
			-1112L,
			-1121L,
			-1122L
		])
		allDeleted("IssueList", [-1111L, -1112L, -1121L, -1122L])
		allDeleted("Execution", [-1111L, -1112L, -1121L, -1122L])
		allDeleted("IterationTestPlanItem", [-111L, -112L])
		allDeleted("Iteration", [-11L])

		allNotDeleted("AttachmentList", [
			-12L,
			-1211L,
			-1212L,
			-1221L,
			-1222L
		])
		allNotDeleted("IssueList", [-1211L, -1212L, -1221L, -1222L])
		allNotDeleted("Execution", [-1211L, -1212L, -1221L, -1222L])
		allNotDeleted("IterationTestPlanItem", [-121L, -122L])
		allNotDeleted("Iteration", [-12L])

		def cpg= findEntity(Campaign.class, -1L)
		cpg.iterations.size()==1
		cpg.iterations[0].id==-12L
	}

	@DataSet("NodeDeletionHandlerTest.iterationPlusExecutions.xml")
	def "should remove iteration test plan item and its executions"(){
		given :
		IterationTestPlanItem item = findEntity(IterationTestPlanItem.class, -111L)
                Iteration iter = findEntity(Iteration.class, -11)
                
		when :
                iter.removeItemFromTestPlan(item)                
		deletionHandler.deleteIterationTestPlanItem(item)
                em.flush()

		then :
                
		allNotDeleted("Iteration", [-12L, -11L])
                allNotDeleted("AttachmentList", [
                    -11L,-12L,
                    -1211L,
                    -1212L,
                    -1221L,
                    -1222L,
                    -1121L,
                    -1122L
		])
		allNotDeleted("IssueList", [-1211L, -1212L, -1221L, -1222L, -1121L, -1122L])
		allNotDeleted("Execution", [-1211L, -1212L, -1221L, -1222L, -1121L, -1122L])
		allNotDeleted("IterationTestPlanItem", [-121L, -122L, -112L])
                
                and :

		allDeleted("IterationTestPlanItem", [-111L])
		allDeleted("AttachmentList", [-1111L, -1112L])
		allDeleted("IssueList", [-1111L, -1112L])
		allDeleted("Execution", [-1111L, -1112L])

	}


	@DataSet("NodeDeletionHandlerTest.campaignPlusTestplan.xml")
	def "should remove a campaign and its Campaign test plans and iterations, and their custom field values"(){
		given :

		def cpg = findEntity(Campaign.class, 1);

		when:
		deletionHandler.deleteNodes([-1L])

		then :

		allDeleted("Campaign", [-1L])
		allDeleted("AttachmentList", [-1L, -11L, -12L])
		allDeleted("CampaignTestPlanItem", [-50L, -51L])
		allDeleted("Iteration", [-11L, -12L])

		allDeleted("CustomFieldValue", [-101L, -102L, -113L, -123L])
	}



	@DataSet("NodeDeletionHandlerTest.cpgFolderHierarchy.xml")
	def "should remove a hierarchy of campaign folders and campaigns (1)"(){
		when :
		deletionHandler.deleteNodes([-11L])
		then :
		allDeleted("CampaignFolder", [-11L, -21L])
		allDeleted("Campaign", [-22L, -31L, -32L])
		allDeleted("AttachmentList", [-11L, -21L, -22L, -31L, -32L])	//issue 2899 : now checks that the attachment lists for folders are also deleted

		allNotDeleted("Campaign", [-12L])
		allNotDeleted("AttachmentList", [-12L])

		allDeleted("CustomFieldValue", [-221L, -222L, -311L, -312L, -321L, -322L])
		allNotDeleted("CustomFieldValue", [-121L, -122L])

		def lib = findEntity(CampaignLibrary.class, -1L)
		def cpg1 = findEntity(Campaign.class, -12L)

		lib.rootContent.size()==1
		lib.rootContent.contains(cpg1)
	}


	@DataSet("NodeDeletionHandlerTest.cpgFolderHierarchy.xml")
	def "should remove a hierarchy of campaign folders and campaigns (2)"(){
		when :
		deletionHandler.deleteNodes([-21L])
		then :
		allDeleted("CampaignFolder", 	[-21L])
		allDeleted("Campaign", 			[-31L, -32L])
		allDeleted("AttachmentList", 	[-31L, -32L])

		allNotDeleted("Campaign", 		[-12L, -22L])
		allNotDeleted("AttachmentList", [-12L, -22L])
		allNotDeleted("CampaignFolder", [-11L])

		allDeleted("CustomFieldValue", 	[-3131L, -312L, -321L, -322L])
		allNotDeleted("CustomFieldValue",[-121L, -122L, -221L, -222L])

		def lib = findEntity(CampaignLibrary.class, -1L)
		def cpg1 = findEntity(Campaign.class, -12L)
		def fold1 = findEntity(CampaignFolder.class, -11L)

		lib.rootContent.size()==2
		lib.rootContent.containsAll([cpg1, fold1])
	}


	@DataSet("NodeDeletionHandlerTest.cpgFolderHierarchy.xml")
	def "should remove a hierarchy of campaign folders and campaigns (3)"(){
		when :
		deletionHandler.deleteNodes([-11L, -12L])
		then :
		allDeleted("CampaignFolder", [-11L, -21L])
		allDeleted("Campaign", [-12L, -22L, -31L, -32L])
		allDeleted("AttachmentList", [-12L, -22L, -31L, -32L])

		def lib=findEntity(CampaignLibrary.class, -1L)
		lib.rootContent.size()==0

		allDeleted("CustomFieldValue", [-121L, -122L, -221L, -222L, -3131L, -312L, -321L, -322L])

	}


	@DataSet("NodeDeletionHandlerTest.should delete testSuites.xml")
	def"should remove test suites"(){
		when :
		deletionHandler.deleteSuites([-1L, -2L], false)
		then :
		allDeleted("TestSuite", [-1L, -2L])
		allDeleted("AttachmentList", [-12L, -13L])

		IterationTestPlanItem iterationTestPlanItem=findEntity(IterationTestPlanItem.class, -121L)
		iterationTestPlanItem.getTestSuites().size() == 0
		IterationTestPlanItem iterationTestPlanItem2=findEntity(IterationTestPlanItem.class, -122L)
		iterationTestPlanItem2.getTestSuites().size() == 0

		Iteration iteration = findEntity(Iteration.class, -11L)
		iteration.getTestSuites().size() == 0
		Iteration iteration2 = findEntity(Iteration.class, -12L)
		iteration2.getTestSuites().size() == 0


	}

	@Unroll
	@DataSet("NodeDeletionHandlerTest.should delete testSuites and remove itpi from iteration.xml")
	def "should remove a test plan item along with a test suite when it becomes orphan, leave the other not deleted"(){
		when :
		deletionHandler.deleteSuites(suiteId, true)
		then :
		allDeleted("TestSuite", suiteId)
		allDeleted("AttachmentList", attachListId)
		allDeleted("IterationTestPlanItem", itpiId )
		allNotDeleted("IterationTestPlanItem", notdeletedItpi)

		where :
		suiteId    | attachListId | itpiId			| notdeletedItpi
		// dataset 1 : itpi 123 is shared with test suite 3
		[-1L, -2L] | [-12L, -13L] | [-121L, -122L]	| [-123L]	
		// dataset 2 : itpi 123 out of scope and 121 is shared with test suite 2 (which is not deleted)
		[-1L]      | [-12L]       | [-122L]			| [-121L, -123L] 

	}


}
