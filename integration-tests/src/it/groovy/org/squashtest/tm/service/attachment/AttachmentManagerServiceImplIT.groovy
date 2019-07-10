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
package org.squashtest.tm.service.attachment

import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.hibernate.Query
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.core.io.ClassPathResource
import org.springframework.transaction.annotation.Transactional
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.squashtest.tm.domain.attachment.Attachment
import org.squashtest.tm.domain.attachment.AttachmentContent
import org.squashtest.tm.domain.execution.Execution
import org.squashtest.tm.domain.requirement.Requirement
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode
import org.squashtest.tm.service.deletion.OperationReport
import org.squashtest.tm.service.execution.ExecutionModificationService
import org.squashtest.tm.service.requirement.RequirementLibraryNavigationService
import org.squashtest.tm.service.requirement.RequirementVersionManagerService
import org.squashtest.tm.service.testcase.TestCaseLibraryNavigationService
import org.squashtest.tm.service.testcase.TestCaseModificationService
import org.unitils.dbunit.annotation.DataSet
import spock.unitils.UnitilsSupport

import javax.inject.Inject
import javax.sql.DataSource

@UnitilsSupport
@Transactional
@DataSet("AttachmentManagerServiceImplIT.xml")
class AttachmentManagerServiceImplIT extends DbunitServiceSpecification {

	@Inject
	private DataSource dataSource

	@Inject
	TestCaseModificationService tcModService

	@Inject
	TestCaseLibraryNavigationService tcNavService

	@Inject
	RequirementLibraryNavigationService reqNavService

	@Inject
	RequirementVersionManagerService reqVersionService

	@Inject
	AttachmentManagerService attachService

	@Inject
	ExecutionModificationService executionModService

	// IDs : for legacy ITs. see dataset
	int testCaseId = -240 //Test-CAse 1 Copie 2 //duplicate test case with Attachmentcontent
	int testCaseIdWithoutAttachment = -245 //TC 2 AttachmentList 919
	int attachListId = -898 //of test folder a, no attachment

	def "should create an AttachmentList along with a TestCase"() {
		given:

		when:
		def attachListId = tcModService.findById(testCaseId).attachmentList.id
		def attachList = tcModService.findById(testCaseId).getAttachmentList()

		then:
		attachList != null
		attachList.id == attachListId;
	}


	def "should add a new attachment and retrieve it"() {
		given:
		ClassPathResource res = new ClassPathResource("/org/squashtest/tm/service/attachment/attachment.jpg")
		File source = res.getFile()
		FileInputStream fis = new FileInputStream(source)

		RawAttachment raw = new RawAttachment() {
			String getName() {
				"attachment.jpg"
			}

			InputStream getStream() {
				fis
			}

			long getSizeInBytes() {
				source.length()
			}
		}


		when:
		Long id = attachService.addAttachment(attachListId, raw)
		session.flush()

		Attachment attach = session.load(Attachment, id)

		then: "attachment correctly created"
		attach.name == "attachment.jpg"
		attach.type == "jpg"

		and:
		session.clear()
		File stored = File.createTempFile("yuno", "storeblobs")
		OutputStream os = new FileOutputStream(stored)

		when:
		attachService.writeContent(id, os)
		IOUtils.closeQuietly(os);

		then: "attachment content is same as source file"
		FileUtils.contentEquals(source, stored)
	}

	RawAttachment rawAttachment(file, name) {
		new RawAttachment() {
			FileInputStream fis = new FileInputStream(file)

			String getName() {
				name
			}

			InputStream getStream() {
				fis
			}

			long getSizeInBytes() {
				file.length()
			}
		}
	}

	def "should add and retrieve a lot of attachment headers"() {
		given:
		File source = sourceFile()

		def raws = []
		raws << rawAttachment(source, "att1.jpg")
		raws << rawAttachment(source, "att2.jpg")
		raws << rawAttachment(source, "att3.jpg")

		when:
		List<Long> ids = []

		raws.each {
			ids << attachService.addAttachment(attachListId, it)
		}

		session.flush()
		session.clear()

		Set<Attachment> attached = attachService.findAttachments(attachListId)

		then:
		attached*.id.containsAll(ids);
		attached*.name.containsAll([
			"att1.jpg",
			"att2.jpg",
			"att3.jpg"
		])
		attached*.type.containsAll(["jpg", "jpg", "jpg"])
	}

	private File sourceFile() {
		ClassPathResource res = new ClassPathResource("/org/squashtest/tm/service/attachment/attachment.jpg")
		File source = res.getFile()
		return source
	}


	byte[] randomBytes(int howMany) {
		byte[] result = new byte[howMany];
		for (int i = 0; i < howMany; i++) {
			result[i] = Math.round(Math.random() * 255);
		}
		return result;
	}

	def "should remove an attachment"() {

		given:
		File source = sourceFile()
		RawAttachment raw = rawAttachment(source, "image.jpg")
		Long id = attachService.addAttachment(attachListId, raw)

		// force the insertion of the attachment in the DB
		session.flush()

		// Hibernate doesn't manage the state of the managed Attachment entity we just created : it
		// wouldn't flush-before-select for unknown reasons. The solution accepted here is to clear the
		// session and force Hibernate to reset its book keeping.
		session.clear()

		when:
		attachService.removeAttachmentFromList(attachListId, id)
		em.flush()


		Set<Attachment> attached = attachService.findAttachments(attachListId)
		then:
		attached.size() == 0

	}

	def "should correctly tell if a test case have attachments or not"() {
		when:
		TestCase testCase = tcModService.findById(testCaseIdWithoutAttachment);

		then:
		!testCase.attachmentList.hasAttachments()

		and:
		File source = sourceFile()
		RawAttachment raw = rawAttachment(source, "image.jpg")

		when:
		attachService.addAttachment(attachListId, raw)
		session.flush()
		TestCase testCase2 = tcModService.findById(testCaseId)

		then:
		testCase2.attachmentList.hasAttachments()
	}

	//**********************************************************************************************************************************************
	//  TESTS FOR [TM-362] =>   shallowCopy of AttachmentContent instead of hardCopy on copy/past TM's item (TestCase, Requirement, execution and so)
	//**********************************************************************************************************************************************
	//@DataSet(value = "AttachmentManagerServiceImplRequirementIT.xml", loadstrategy = org.unitils.dbunit.datasetloadstrategy.impl.UpdateLoadStrategy.class)
//	@DataSet(loadStrategy=UpdateLoadStrategy.class, value = "AttachmentManagerServiceImplRequirementIT.xml")
	def "attachments shallowCopy: copy a test case and delete the source"() {
	//add a new PJ on a TestCase with 3 steps with a PJ on step1 and then copy it
		//"TC 3 classique" : ID=-249 (AttList = -929) , 3 steps -179,-180,-181 -  1PJ on step -179 List = -930
		given:
		def testCaseId = -249L
		def testCaseAttachListId = -929L
		def step1Id = -179L
		def step2Id = -180L
		def step3Id = -181L
		def step1AttchListId = -930L
		def step2AttchListId = -931L
		def step3AttchListId = -932L
		//existing PJ on step
		def attachIdStep1Id = -18L
		def contentIdStep1Id = -10L

		//1°) checking Dataset
		when:
		def tcAttachList = executeSelectSQLQuery(
			"select ATTACHMENT_LIST_ID from TEST_CASE_LIBRARY_NODE WHERE TCLN_ID = " + testCaseId)

		//steps
		List<long[]> result = executeSelectSQLQuery(
			"select TEST_STEP_ID, ATTACHMENT_LIST_ID from action_test_step inner join test_case_steps ON TEST_CASE_STEPS.STEP_ID = ACTION_TEST_STEP.TEST_STEP_ID where TEST_CASE_ID = "
				+ testCaseId + "ORDER BY 1 DESC") //negative numbers

		then:
		areOrhanContents() == false
		tcAttachList.size == 1
		tcAttachList[0] == testCaseAttachListId
		//Dataset expected  3 steps
		result.size() == 3
		result[0][0] == step1Id
		result[1][0] == step2Id
		result[2][0] == step3Id

		result[0][1] == step1AttchListId
		result[1][1] == step2AttchListId
		result[2][1] == step3AttchListId

		when:
		List<Long> attchLists = new ArrayList<>()
		attchLists.add(testCaseAttachListId)
		attchLists.add(step1AttchListId)
		attchLists.add(step2AttchListId)
		attchLists.add(step3AttchListId)
		List<Attachment> attachments = getAttachmentsFromLists(attchLists)

		then:
		//expected only 1 PJ on step1
		attachments.size() == 1
		attachments.get(0).attachmentList.id == step1AttchListId
		attachments.get(0).attachmentList.size() == 1
		attachments.get(0).getId() == attachIdStep1Id
		checkIdAndNameOfAttachmentContent(contentIdStep1Id,"new6.txt", attachments.get(0)) == true
		attachments.get(0).name.equals("new6.txt") == true
		attachments.get(0).content.id  == contentIdStep1Id

		//2°) add a PJ on testCase
		when:
		def newStContent = "It is OK"
		def newContentName = "PJofTC.txt"
		InputStream newContentStream = IOUtils.toInputStream(newStContent)

		//org.squashtest.tm.web.internal.controller.attachment.UploadedData not seen ...
		RawAttachment rawAttachment = setRawAttachment(newContentStream, newContentName)

		def newTCAttachId = attachService.addAttachment(testCaseAttachListId, rawAttachment)
		def attachmentsTC = attachService.findAttachments(testCaseAttachListId)
		Long newTCContentId = attachmentsTC.getAt(0).content.id //save ID for 3°)

		then:
		attachmentsTC.size() == 1
		attachmentsTC.getAt(0).attachmentList.size() == 1
		attachmentsTC.getAt(0).getId() == newTCAttachId
		attachmentsTC.getAt(0).name.equals(newContentName) == true
		attachmentsTC.getAt(0).content.id  == newTCContentId
		attachmentsTC.getAt(0).content.stream.text.equals(newStContent) == true

		//3°) copying the testCase with a newPJ and 3 steps, the first one with PJ
		//expected: AttachmentContent should not be duplicate
		when:
		Long[] nodes = [testCaseId]
		def copiedNodes = tcNavService.copyNodesToLibrary(-14L,nodes)

		then:
		copiedNodes.size() == 1
		TestCaseLibraryNode copyCase = copiedNodes.get(0)
		Long testCopyCaseAttachListId = copyCase.attachmentList.id
		def steps = copyCase.getActionSteps()
		steps.size() == 3
		//PJ on step 1
		steps.get(0).attachmentList.attachments.size() == 1
		//same PJ that step1 of source TEstCase
		def newStep1AttachList = steps.get(0).attachmentList.id //for compare later
		steps.get(0).attachmentList.attachments.getAt(0).name.equals("new6.txt") == true
		steps.get(0).attachmentList.attachments.getAt(0).content.id  == contentIdStep1Id
		//2 attachments for the same Content
		countAttachemntsForAttachmentContent(contentIdStep1Id) == 2
		// no PJ on other steps
		steps.get(1).attachmentList.attachments.size() == 0
		steps.get(2).attachmentList.attachments.size() == 0
		//checking PJ on Testcase itself
		copyCase.attachmentList.size() == 1
		//same AttachmentContent on TestCase and copied TestCase, the PJ add in 2°)
		copyCase.attachmentList.attachments.getAt(0).name.equals(newContentName) == true
		copyCase.attachmentList.attachments.getAt(0).content.id  == newTCContentId
		copyCase.attachmentList.attachments.getAt(0).content.stream.text.equals(newStContent) == true
		countAttachemntsForAttachmentContent(newTCContentId) == 2 //source and target

		//4°) Delete the original TestCase and check if PJs on Copied TestCAse are always available
		when:
		mockDataSourceUrl()
		OperationReport report = tcNavService.deleteNodes([nodes[0]])

		then:
		report.removed.size() == 1
		report.removed.getAt(0).resid == testCaseId
		areOrhanContents() == false
		//attachmentContent of TestCase
		List<Attachment>  attchmentsForContent = getAttachmentsForAttachmentContent(newTCContentId)
		attchmentsForContent.size() == 1
		attchmentsForContent.getAt(0).attachmentList.id == testCopyCaseAttachListId //on testCaseCopy
		attchmentsForContent.getAt(0).name == newContentName
		attchmentsForContent.getAt(0).content.stream.text == newStContent

		when:
		//attachmentContent of step 1
		attchmentsForContent.clear()
		attchmentsForContent = getAttachmentsForAttachmentContent(contentIdStep1Id)
		then:
		attchmentsForContent.size() == 1 // only copied step1
		attchmentsForContent.getAt(0).attachmentList.id ==  newStep1AttachList//on step 1 of copied testcase
		attchmentsForContent.getAt(0).name.equals("new6.txt") == true
	}

	def "attachments shallowCopy: delete one by one the 2 PJs on a duplicate object and its source"() {
		//expected: the PJ (content) must still exists or not in db after attachment deletion depending on other objects  have a reference on content
		//"TC3 Gherkin" : ID=-247 (AttList = -927) 2 PJs ContentId=-9,-8) and its duplicate TestCase "TC3 Gherkin Copie1"  ID=-248 (AttList = -928)
		given:
		def testCaseAttachListId = -927L
		def duplicateTestCaseAttachListId = -928L
		def testCaseAttach_1_Id = -14L
		def duplicateTestCaseAttach_1_Id = -17L
		def testCaseAttach_2_Id = -15L
		def duplicateTestCaseAttach_2_Id = -16L
		def testCaseUniqueContent_1_Id = -8L
		def testCaseUniqueContent_2_Id = -9L

		def content_1_name
		def content_2_name

		//1°) checking Dataset
		when:
		List<Attachment> attachments = getAttachmentsForAttachmentContent(testCaseUniqueContent_1_Id)

		then:
		areOrhanContents() == false
		attachments.size() == 2 //ordered list
		attachments.get(0).id  ==  duplicateTestCaseAttach_1_Id
		attachments.get(0).attachmentList.id ==  duplicateTestCaseAttachListId
		when:  //save values
		content_1_name = attachments.get(0).name
		then:
		attachments.get(1).id ==  testCaseAttach_1_Id
		attachments.get(1).attachmentList.id ==  testCaseAttachListId
		attachments.get(1).name.equals(content_1_name) == true

		when:
		attachments.clear()
		attachments = getAttachmentsForAttachmentContent(testCaseUniqueContent_2_Id)

		then:
		attachments.size() == 2 //ordered list
		attachments.get(0).id  ==  duplicateTestCaseAttach_2_Id
		attachments.get(0).attachmentList.id ==  duplicateTestCaseAttachListId
		when:
		content_2_name = attachments.get(0).name
		then:
		attachments.get(1).id ==  testCaseAttach_2_Id
		attachments.get(1).attachmentList.id ==  testCaseAttachListId
		attachments.get(1).name.equals(content_2_name) == true

		//2°) remove Attachment_1 on testCase
		when:
		attachService.removeListOfAttachments(testCaseAttachListId, [testCaseAttach_1_Id])
		then:
		areOrhanContents() == false
		//expected: Content_1 always available from duplicate TestCase
		when:
		attachments.clear()
		attachments = getAttachmentsForAttachmentContent(testCaseUniqueContent_1_Id)
		then:
		attachments.size() == 1
		attachments.get(0).id ==  duplicateTestCaseAttach_1_Id
		attachments.get(0).attachmentList.id ==  duplicateTestCaseAttachListId
		attachments.get(0).name.equals(content_1_name) == true

		when:
		//no change for content_2
		attachments.clear()
		attachments = getAttachmentsForAttachmentContent(testCaseUniqueContent_2_Id)
		then:
		attachments.size() == 2
		attachments.get(0).id  ==  duplicateTestCaseAttach_2_Id
		attachments.get(0).attachmentList.id ==  duplicateTestCaseAttachListId
		attachments.get(0).name.equals(content_2_name) == true
		attachments.get(1).id ==  testCaseAttach_2_Id
		attachments.get(1).attachmentList.id ==  testCaseAttachListId
		attachments.get(1).name.equals(content_2_name) == true

		//3°) remove Attachment_2 on duplicateTestCase
		when:
		attachService.removeListOfAttachments(duplicateTestCaseAttachListId, [duplicateTestCaseAttach_2_Id])
		then:
		areOrhanContents() == false
		//no change Content_1
		when:
		attachments.clear()
		attachments = getAttachmentsForAttachmentContent(testCaseUniqueContent_1_Id)
		then:
		attachments.size() == 1
		attachments.get(0).id ==  duplicateTestCaseAttach_1_Id
		attachments.get(0).attachmentList.id ==  duplicateTestCaseAttachListId
		attachments.get(0).name.equals(content_1_name) == true

		when:
		//content_2 only linked to testCase
		attachments.clear()
		attachments = getAttachmentsForAttachmentContent(testCaseUniqueContent_2_Id)
		then:
		attachments.size() == 1
		attachments.get(0).id ==  testCaseAttach_2_Id
		attachments.get(0).attachmentList.id ==  testCaseAttachListId
		attachments.get(0).name.equals(content_2_name) == true

		//4°) remove Attachment_1 on duplicateTEstCase
		when:
		attachService.removeListOfAttachments(duplicateTestCaseAttachListId, [duplicateTestCaseAttach_1_Id])
		then:
		areOrhanContents() == false
		//no more Attachment, no more Content_1
		when:
		attachments.clear()
		attachments = getAttachmentsForAttachmentContent(testCaseUniqueContent_1_Id)
		attachments.size() == 0
		then:
		List<AttachmentContent> contents = getAttachemntContentsById(testCaseUniqueContent_1_Id)
		then:
		contents.size() == 0

		when:
		//no change for Content_2
		attachments.clear()
		attachments = getAttachmentsForAttachmentContent(testCaseUniqueContent_2_Id)
		then:
		attachments.size() == 1
		attachments.get(0).id ==  testCaseAttach_2_Id
		attachments.get(0).attachmentList.id ==  testCaseAttachListId
		attachments.get(0).name.equals(content_2_name) == true

		//4°) remove Attachment_2 on TEstCase
		when:
		attachService.removeListOfAttachments(testCaseAttachListId, [testCaseAttach_2_Id])
		then:
		areOrhanContents() == false
		//no more Content_1
		when:
		attachments.clear()
		attachments = getAttachmentsForAttachmentContent(testCaseUniqueContent_2_Id)
		then:
		attachments.size() == 0
		when:
		contents.clear()
		contents = getAttachemntContentsById(testCaseUniqueContent_2_Id)
		then:
		contents.size() == 0

		when:
		//no more Content_2
		attachments.clear()
		attachments = getAttachmentsForAttachmentContent(testCaseUniqueContent_2_Id)
		then:
		attachments.size() == 0
		when:
		contents.clear()
		contents = getAttachemntContentsById(testCaseUniqueContent_2_Id)
		then:
		contents.size() == 0
	}


	def "attachments shallowCopy: copy a tree with folder, testCase, duplicate TestCase and delete it  "() {
		//expected: no AttachmentContent created on copy, no PJs lose on delete

		given :
		def srcFolderId = -243L //folder c
		                  /*******************
		                  *  "folder c" (PJ new6]
						  *    |
						  *    "folder Test Case 2" (PJ new5.txt)
						  *        |
						  *         "tc 2" (3 steps =>PJ new6.txt on step 2)
						  *   |
						  *    "tc 2 copie"  (3 steps =>PJ new6.txt on step 2)
						  *********************/
		def targetFolderId = -237L //folder a
		def attchmentContentsNbInTree = 3
		def attchmentsNbInTree = 4 //1 "folder c" + 1 "folder testcase 2" + 1 "step 2 tc 2" + 1 "step2 of tc 2 copie"
		def attchmentListNbInTree = 10

		//1°) copy the tree "folder c"
		when:
		def initialAttachmentContentNb =  countTotalAttachmentContent() // total in database => not only contained in srcFolder
		def initialAttachmentNb = countTotalAttachment() //idem
		def initialAttachmentListNb = countTotalAttachmentList()
		def nodeList = tcNavService.copyNodesToFolder(targetFolderId, srcFolderId);

		then:
		nodeList.size() == 1
		//same content ...
		nodeList.getAt(0).content.size() == 2 // 2 elements at first sublevel ("folder testcase 2 " and " tc2 copie"
		nodeList.getAt(0).content.getAt(0).name == "folder Test Case 2"
		nodeList.getAt(0).content.getAt(0).content.size() == 1 //tc2

		//expected: no duplicate AttachmentContent in Database
		countTotalAttachmentContent() == initialAttachmentContentNb
		// see above
		countTotalAttachment() == (initialAttachmentNb + attchmentsNbInTree)
		countTotalAttachmentList() == (initialAttachmentListNb + attchmentListNbInTree)

		//2°) remove the copy of "folder c"
		when:
			mockDataSourceUrl()
			tcNavService.deleteNodes([nodeList.getAt(0).id])
		then:
		countTotalAttachmentContent() == initialAttachmentContentNb
		countTotalAttachment() == initialAttachmentNb
		countTotalAttachmentList() == initialAttachmentListNb

		//3°) remove the "folder c"
		when:
		tcNavService.deleteNodes([srcFolderId])
		then:
		// remove the AttachmentContent no more use
		countTotalAttachmentContent() == (initialAttachmentContentNb - attchmentContentsNbInTree)
		countTotalAttachment() == (initialAttachmentNb - attchmentsNbInTree)
		countTotalAttachmentList() == (initialAttachmentListNb - attchmentListNbInTree)

	}

	 /**************
	 *       workspace requirement
	 *       Test Folder 1  (ID=-254, atachment_list_id = -900 , no PJ)
	 *                    |
	 *                     TF1-R1 Test Requirement 1 (ID=-255, atachment_list_id = -888 , no PJ)
	 *                                              |
	 *                                              Sub ex Ex (ID=-256, atachment_list_id = - 938, PJ-> requirement.txt, Content_id = -12, Attachment_id = -21 )
	 *                                                                                |
	 *                                                                                 ex 1(ID=-257, atachment_list_id = - 939, PJ-> new7.txt Content_id = -13, Attachment_id = -22)
	 ***************/

	def "attachments shallowCopy-workspace requirement-should not duplicate PJ when creating a new requirement version"() {
		given:
		def requirementId = -256L
		def attachmentId = -21L
		def pjName = "requirement.txt"
		def contentID = -12L
		//update circular dependencies ...
		updateRequirementVersion(-255L, -255L)
		updateRequirementVersion(-256L, -256L)
		updateRequirementVersion(-257L, -257L)

		//1°)  check dataset: expected only 1 Attachment for ContentId= -12L
		when:
		List<Attachment> attachments = getAttachmentsForAttachmentContent(contentID)
		Long lastVersionId = executeSelectSingleResultSQLQuery("SELECT current_version_id FROM requirement WHERE rln_id = " + requirementId)

		then:
		attachments.size() == 1
		lastVersionId == requirementId
		when:
		Attachment attachment = attachments.getAt(0)
		then:
		attachment.name.equals(pjName) == true
		attachment.id == attachmentId

		//2°) create a new version for the used requirement
		when:
			reqVersionService.createNewVersion(requirementId, false, false)
			attachments.clear()
			attachments = getAttachmentsForAttachmentContent(contentID)
			Long newVersionId = executeSelectSingleResultSQLQuery("SELECT current_version_id FROM requirement WHERE rln_id = " + requirementId)

		then:
			newVersionId != lastVersionId
		    // 2 Attachments for the same AttachmentContent
			attachments.size() == 2
		when:
			//old version of requirement
			attachment = attachments.getAt(0)
		then:

			attachment.name.equals(pjName) == true
			attachment.id == attachmentId
		when:
			//new version of requirement
			attachment = attachments.getAt(1)
		then:
			attachment.name.equals(pjName) == true
			attachment.id != attachmentId
			attachment.attachmentToCopyId == attachmentId

		//3°) Remove the requirement (and this newly created version): AttachmentContent must be delete
		when:
			em.flush()
			em.clear()
			OperationReport report = reqNavService.deleteNodes(Collections.singletonList(requirementId))

		then:
			// the requirement and its new version
			report.removed.size() == 1
	    	report.removed.getAt(0).resid == requirementId
			executeSelectSingleResultSQLQuery("SELECT count(rln_id) FROM requirement WHERE rln_id = " + requirementId) == 0
		    executeSelectSingleResultSQLQuery("SELECT count(current_version_id) FROM requirement WHERE current_version_id = " + newVersionId) == 0
			executeSelectSingleResultSQLQuery("SELECT count(res_id) FROM requirement_version WHERE res_id = " + requirementId) == 0
			executeSelectSingleResultSQLQuery("SELECT count(res_id) FROM requirement_version WHERE res_id = " + newVersionId) == 0
		    // the PJ
			areOrhanContents() == false
			executeSelectSingleResultSQLQuery("SELECT count(attachment_id) FROM attachment WHERE  content_id = " + contentID) == 0
	}

	def "attachments shallowCopy-workspace requirement-copy/delete tree"() {
		def reqFolderId = -254L
		def initialAttachmentContentNb =  countTotalAttachmentContent() // total in database
		def initialAttachmentNb = countTotalAttachment()
		def initialAttachmentListNb = countTotalAttachmentList()

		def rootLibraryRequirementId = -14L

		//check dataset
		when:
			List<Requirement> results = executeSelectQuery("SELECT requirement from RequirementLibraryNode requirement where requirement.id = :id", "id", reqFolderId)
		then:
			results.size() == 1
			//check expected PJs existing in tree
			results.get(0).attachmentList.size() == 0 // no PJ on folder
			results.get(0).content.size() == 1 // 1 subElement (-255)
			results.get(0).content.getAt(0).resource.attachmentList.size() == 0 //(no PJ)
			results.get(0).content.getAt(0).content.size() ==1 //sublevel 2 (-256)
			results.get(0).content.getAt(0).content.getAt(0).resource.attachmentList.size() == 1 // 1 PG
			results.get(0).content.getAt(0).content.getAt(0).content.size() == 1 //sublevel 3
			results.get(0).content.getAt(0).content.getAt(0).content.getAt(0).resource.attachmentList.size()  == 1

		when:
			//saving read data
			def folderAttachListId = results.get(0).attachmentList.id
			def level2ContentId = results.get(0).content.getAt(0).content.getAt(0).resource.attachmentList.allAttachments.getAt(0).content.id
			def level3ContentId = results.get(0).content.getAt(0).content.getAt(0).content.getAt(0).resource.attachmentList.allAttachments.getAt(0).content.id
			def nbContentAttachmentInTree = 2
			//add a PJ on folder
			def newStContent = "Content on req Folder"
			def newContentName = "PJ of ReqFolder.txt"
			InputStream newContentStream = IOUtils.toInputStream(newStContent)
			RawAttachment rawAttachment = setRawAttachment(newContentStream, newContentName)
			attachService.addAttachment(folderAttachListId, rawAttachment)
			def attachmentsTC = attachService.findAttachments(folderAttachListId)
		then: //check that the PJ on folder exist and is readable
			attachmentsTC.size() == 1
			attachmentsTC.getAt(0).attachmentList.size() == 1
			attachmentsTC.getAt(0).name.equals(newContentName) == true

			attachmentsTC.getAt(0).content.stream.text.equals(newStContent) == true

		when: // copy all the folder
			def pjOnFolderContentId = attachmentsTC.getAt(0).content.id // save
			Long[] nodes = [reqFolderId]
			reqNavService.copyNodesToLibrary(rootLibraryRequirementId,nodes)
		then:
			false == false
//		List<Requirement> results = executeSelectQuery("SELECT requirement from RequirementLibraryNode requirement where requirement.id = :id", "id", reqFolderId)
//		reqFolder = results.get(0)
//		reqNavService.
		//xxxNavService.copyNodesToFolder(rootLibraryRequirement, reqFolderId);
//		nbOfAttahcmentsIntree =

	}

	/**************
	 *       workspace campaign
	 *
	 *
	 *
	 *       Test Folder 1  (ID=-254, atachment_list_id = -900 , no PJ)
	 *                    |
	 *                     TF1-R1 Test Requirement 1 (ID=-255, atachment_list_id = -888 , no PJ)
	 *                                              |
	 *                                              Sub ex Ex (ID=-256, atachment_list_id = - 938, PJ-> requirement.txt, Content_id = -12, Attachment_id = -21 )
	 *                                                                                |
	 *                                                                                 ex 1(ID=-257, atachment_list_id = - 939, PJ-> new7.txt Content_id = -13, Attachment_id = -22)
	 ***************/
//      TODO: to run the test, we must add campaign, item_plan_test dependencies in dataset ...
//	def "attachments shallowCopy-workspace campaign-delete an execution with added PJ linked to a TestCase with a PJ"() {
//		//1 PJ on testCase, and an added PJ on Execution "-84" of this TestCase
//		def linkedTestCaseId = -240L  //1PJ
//		def linkedTestCaseAttachmentId = -910L
//		def executionId = -84L
//		def executionAttachmentListId = -952L
//		def commonContentId = -1L
//		def addedToExecutionContentId = -14L
//
//		when:
//			//checking dataset
//			List<Execution> executions = executeSelectQuery("SELECT execution from Execution execution where execution.id = :id", "id", executionId)
//			List<Long> contentIdsOnExecution = executeSelectQuery("SELECT attachment.content.id from Attachment attachment where attachment.attachmentList.id = :id order by attachment.content.id ASC", "id", executionAttachmentListId)
//			List<Long> contentIdsOnTestCase = executeSelectQuery("SELECT attachment.content.id from Attachment attachment where attachment.attachmentList.id = :id order by attachment.content.id ASC", "id", linkedTestCaseAttachmentId)
//
//		then:
//			executions.size() == 1
//			executions.getAt(0).attachmentList.id == executionAttachmentListId
//			executions.getAt(0).attachmentList.getAllAttachments().size() == 2
//			executions.getAt(0).referencedTestCase.id == linkedTestCaseId
//			executions.getAt(0).referencedTestCase.attachmentList.id == linkedTestCaseAttachmentId
//			executions.getAt(0).referencedTestCase.attachmentList.getAllAttachments().size() == 1
//			contentIdsOnExecution.size() == 2
//			contentIdsOnExecution.getAt(0) == addedToExecutionContentId
//			contentIdsOnExecution.getAt(1) == commonContentId
//		    contentIdsOnTestCase.size() == 1
//	  		contentIdsOnTestCase.getAt(0) == commonContentId
//
//		//deleting the execution
//		when:
//			Execution execution = executions.getAt(0)
//			executionModService.deleteExecution(execution)
//		then:
//		//expected: commonContentId should always be available for testcase but addedToExecutionContentId must not exist in db
//		areOrhanContents() == false
//		executeSelectSingleResultSQLQuery("SELECT count(execution_id) from Execution where execution_id = " + execution.id) == 0
//		executeSelectSingleResultSQLQuery("SELECT count(attachment_content_id) from attachment_content where attachment_content_id = " + addedToExecutionContentId) == 0
//		executeSelectQuery("SELECT attachment.content.id from Attachment attachment where attachment.attachmentList.id = :id order by attachment.content.id ASC",
//			"id", linkedTestCaseAttachmentId).equals(contentIdsOnTestCase) == true // no change
//	}

	/** *************************************************************
	                 UTILS
	************************************************************** */

	RawAttachment setRawAttachment(inputStream, name) {
		new RawAttachment() {
			@Override
			InputStream getStream() {
				return inputStream
			}

			@Override
			String getName() {
				return name
			}

			@Override
			long getSizeInBytes() {
				return inputStream.bytes.size()
			}
		}
	}


	protected <R> List<R> executeSelectSQLQuery(String queryString) {
		Query query = getSession().createSQLQuery(queryString)
		return query.getResultList()
	}

	protected <R> Object executeSelectSingleResultSQLQuery(String queryString) {
		Query query = getSession().createSQLQuery(queryString)
		return query.getSingleResult()
	}

	protected boolean areOrhanContents() {
		def result = executeSelectSQLQuery(
			"SELECT attachment.attachment_id, attachment_content.ATTACHMENT_CONTENT_ID " +
				"from attachment_content left join attachment on attachment_content.ATTACHMENT_CONTENT_ID = attachment.CONTENT_ID where attachment.attachment_id is null")
		return !result.isEmpty()
	}

	protected <R> List<R> executeSelectQuery(String stQuery, String paramName, Collection<Long> ids) {
		if (!ids.isEmpty()) {
			Query query = getSession().createQuery(stQuery)
			query.setParameterList(paramName, ids)
			return query.list()
		} else {
			return Collections.emptyList()
		}
	}

	protected <R> List<R> executeSelectQuery(String stQuery, String paramName, Long id) {
			Query query = getSession().createQuery(stQuery)
			query.setParameterList(paramName,id)
			return query.getResultList()
	}

	protected List<Attachment> getAttachmentsFromLists(Collection<Long> attchLists) {
		return executeSelectQuery("select attach from Attachment attach where attach.attachmentList.id in (:ids)", "ids", attchLists)
	}


	protected List<Attachment> getAttachmentsForAttachmentContent(Long contentID) {
		return executeSelectQuery("SELECT attachment from Attachment attachment where attachment.content.id = :id order by attachment.id ASC", "id", contentID)
	}

	protected List<AttachmentContent> getAttachemntContentsById(Long contentID) {
		return executeSelectQuery("SELECT content from AttachmentContent content where content.id = :id order by content.id ASC", "id", contentID)
	}

	protected Long countAttachemntsForAttachmentContent(Long contentID) {
		return getAttachmentsForAttachmentContent(contentID).size()
	}

	protected Long countTotalAttachmentContent() {
		return executeSelectSingleResultSQLQuery("SELECT count(*) FROM attachment_content")
	}

	protected Long countTotalAttachment() {
		return executeSelectSingleResultSQLQuery("SELECT count(*) FROM attachment")
	}

	protected Long countTotalAttachmentList() {
		return executeSelectSingleResultSQLQuery("SELECT count(*) FROM attachment_list")
	}

	protected  boolean checkIdAndNameOfAttachmentContent(Long idRef, String attName, Attachment underTest) {
		boolean result = attName.equals(underTest.name)
		if (result) {
			result = (idRef == underTest.content.id)
		}
		return result
	}

	private mockDataSourceUrl() {
		String url = dataSource.getConnection().getMetaData().getURL()
		DataSourceProperties ds = Mock()
		ds.getUrl() >> url
		tcNavService.deletionHandler.deletionDao.dataSourceProperties  = ds
	}

	protected void updateRequirementVersion(Long res_id, Long requirement_id) {
		Query query = getSession().createSQLQuery("UPDATE requirement_version set requirement_id = " + requirement_id + " where res_id = " + res_id)
		query.executeUpdate()
	}
}
