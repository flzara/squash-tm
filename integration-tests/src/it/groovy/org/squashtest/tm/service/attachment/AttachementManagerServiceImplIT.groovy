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

//import org.squashtest.tm.web.internal.controller.attachment.UploadedData

import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode
import org.squashtest.tm.service.deletion.OperationReport
import org.squashtest.tm.service.project.GenericProjectManagerService
import org.squashtest.tm.service.requirement.RequirementLibraryNavigationService
import org.squashtest.tm.service.requirement.RequirementVersionManagerService
import org.squashtest.tm.service.testcase.TestCaseLibraryNavigationService
import org.squashtest.tm.service.testcase.TestCaseModificationService
import org.unitils.dbunit.annotation.DataSet
import spock.unitils.UnitilsSupport

import javax.inject.Inject

@UnitilsSupport
@Transactional
@DataSet
class AttachmentManagerServiceImplIT extends DbunitServiceSpecification {

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
	GenericProjectManagerService genericProjectManager

	// IDs : see dataset
	int folderId = -241; //folder b 1 PJ not duplicate
	int testCaseId = -240; //Test-CAse 1 Copie 2 //duplicate test case with Attachmentcontent
	int testCaseIdWithoutAttachment = -245 //TC 2 AttachmentList 919
	int attachListId = -898; //of test folder a, no attachment


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
		def contentStreamStep1 = "new6"

		//1°) checking Dataset
		when:
		def tcAttachList = executeSelectSQLQuery(
			"select ATTACHMENT_LIST_ID from TEST_CASE_LIBRARY_NODE WHERE TCLN_ID = " + testCaseId)

		//steps
		List<long[]> result = executeSelectSQLQuery(
			"select TEST_STEP_ID, ATTACHMENT_LIST_ID from action_test_step inner join test_case_steps ON TEST_CASE_STEPS.STEP_ID = ACTION_TEST_STEP.TEST_STEP_ID where TEST_CASE_ID = "
				+ testCaseId + "ORDER BY 1 DESC") //negative numbers

		then:
		areOrheanContents() == false
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
		//checkIdAndNameAndStreamOfAttachment(contentIdStep1Id,"new6.txt",contentStreamStep1, attachments.get(0)) == true
		attachments.get(0).name.equals("new6.txt") == true
		attachments.get(0).content.id  == contentIdStep1Id
		attachments.get(0).content.stream.text.equals(contentStreamStep1) == true

		//2°) add a PJ on testCase
		when:
		def newStContent = "It is OK"
		def newContenName = "PJ of TC"
		InputStream newContentStream = IOUtils.toInputStream(newStContent)
		def newContenByteSize = newContentStream.bytes.size()

		//org.squashtest.tm.web.internal.controller.attachment.UploadedData not seen ...
		//RawAttachment rawAttachment = new UploadedData(newContentStream, newContenName, newContenByteSize)
		RawAttachment rawAttachment = new RawAttachment() {

			@Override
			InputStream getStream() {
				return newContentStream
			}

			@Override
			String getName() {
				return newContenName
			}

			@Override
			long getSizeInBytes() {
				return newContenByteSize
			}
		}

		def newTCAttachId = attachService.addAttachment(testCaseAttachListId, rawAttachment)
		def attachmentsTC = attachService.findAttachments(testCaseAttachListId)
		Long newTCContentId = attachmentsTC.getAt(0).content.id //save ID for 3°)

		then:
		attachmentsTC.size() == 1
		attachmentsTC.getAt(0).attachmentList.size() == 1
		attachmentsTC.getAt(0).getId() == newTCAttachId
		attachmentsTC.getAt(0).name.equals(newContenName) == true
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
		steps.get(0).attachmentList.attachments.getAt(0).content.stream.text.equals(contentStreamStep1) == true
		//2 attachments for the same Content
		countAttachemntsForAttachmentContent(contentIdStep1Id) == 2
		// no PJ on other steps
		steps.get(1).attachmentList.attachments.size() == 0
		steps.get(2).attachmentList.attachments.size() == 0
		//checking PJ on Testcase itself
		copyCase.attachmentList.size() == 1
		//same AttachmentContent on TestCase and copied TestCase, the PJ add in 2°)
		copyCase.attachmentList.attachments.getAt(0).name.equals(newContenName) == true
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
		areOrheanContents() == false
		//attachmentContent of TestCase
		List<Attachment>  attchmentsForContent = getAttachemntsForAttachmentContent(newTCContentId)
		attchmentsForContent.size() == 1
		attchmentsForContent.getAt(0).attachmentList.id == testCopyCaseAttachListId //on testCaseCopy
		attchmentsForContent.getAt(0).name == newContenName
		attchmentsForContent.getAt(0).content.stream.text == newStContent

		when:
		//attachmentContent of step 1
		attchmentsForContent.clear()
		attchmentsForContent = getAttachemntsForAttachmentContent(contentIdStep1Id)
		then:
		attchmentsForContent.size() == 1 // only copied step1
		attchmentsForContent.getAt(0).attachmentList.id ==  newStep1AttachList//on step 1 of copied testcase
		attchmentsForContent.getAt(0).name.equals("new6.txt") == true
		attchmentsForContent.getAt(0).content.stream.text == contentStreamStep1
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
		def content_1_streamTxt
		def content_2_streamTxt

		//1°) checking Dataset
		when:
		List<Attachment> attachments = getAttachemntsForAttachmentContent(testCaseUniqueContent_1_Id)

		then:
		areOrheanContents() == false
		attachments.size() == 2 //ordered list
		attachments.get(0).id  ==  duplicateTestCaseAttach_1_Id
		attachments.get(0).attachmentList.id ==  duplicateTestCaseAttachListId
		when:  //save values
		content_1_streamTxt = attachments.get(0).content.stream.text
		content_1_name = attachments.get(0).name
		then:
		attachments.get(1).id ==  testCaseAttach_1_Id
		attachments.get(1).attachmentList.id ==  testCaseAttachListId
		attachments.get(1).content.stream.text.equals(content_1_streamTxt) == true
		attachments.get(1).name.equals(content_1_name) == true

		when:
		attachments.clear()
		attachments = getAttachemntsForAttachmentContent(testCaseUniqueContent_2_Id)

		then:
		attachments.size() == 2 //ordered list
		attachments.get(0).id  ==  duplicateTestCaseAttach_2_Id
		attachments.get(0).attachmentList.id ==  duplicateTestCaseAttachListId
		when:
		content_2_streamTxt = attachments.get(0).content.stream.text
		content_2_name = attachments.get(0).name
		then:
		attachments.get(1).id ==  testCaseAttach_2_Id
		attachments.get(1).attachmentList.id ==  testCaseAttachListId
		attachments.get(1).content.stream.text.equals(content_2_streamTxt) == true
		attachments.get(1).name.equals(content_2_name) == true

		//2°) remove Attachment_1 on testCase
		when:
		attachService.removeListOfAttachments(testCaseAttachListId, [testCaseAttach_1_Id])
		then:
		areOrheanContents() == false
		//expected: Content_1 always available from duplicate TestCase
		when:
		attachments.clear()
		attachments = getAttachemntsForAttachmentContent(testCaseUniqueContent_1_Id)
		then:
		attachments.size() == 1
		attachments.get(0).id ==  duplicateTestCaseAttach_1_Id
		attachments.get(0).attachmentList.id ==  duplicateTestCaseAttachListId
		attachments.get(0).content.stream.text.equals(content_1_streamTxt) == true
		attachments.get(0).name.equals(content_1_name) == true

		when:
		//no change for content_2
		attachments.clear()
		attachments = getAttachemntsForAttachmentContent(testCaseUniqueContent_2_Id)
		then:
		attachments.size() == 2
		attachments.get(0).id  ==  duplicateTestCaseAttach_2_Id
		attachments.get(0).attachmentList.id ==  duplicateTestCaseAttachListId
		attachments.get(0).content.stream.text.equals(content_2_streamTxt) == true
		attachments.get(0).name.equals(content_2_name) == true
		attachments.get(1).id ==  testCaseAttach_2_Id
		attachments.get(1).attachmentList.id ==  testCaseAttachListId
		attachments.get(1).content.stream.text.equals(content_2_streamTxt) == true
		attachments.get(1).name.equals(content_2_name) == true


		//3°) remove Attachment_2 on duplicateTestCase
		when:
		attachService.removeListOfAttachments(duplicateTestCaseAttachListId, [duplicateTestCaseAttach_2_Id])
		then:
		areOrheanContents() == false
		//no change Content_1
		when:
		attachments.clear()
		attachments = getAttachemntsForAttachmentContent(testCaseUniqueContent_1_Id)
		then:
		attachments.size() == 1
		attachments.get(0).id ==  duplicateTestCaseAttach_1_Id
		attachments.get(0).attachmentList.id ==  duplicateTestCaseAttachListId
		attachments.get(0).content.stream.text.equals(content_1_streamTxt) == true
		attachments.get(0).name.equals(content_1_name) == true

		when:
		//content_2 only linked to testCase
		attachments.clear()
		attachments = getAttachemntsForAttachmentContent(testCaseUniqueContent_2_Id)
		then:
		attachments.size() == 1
		attachments.get(0).id ==  testCaseAttach_2_Id
		attachments.get(0).attachmentList.id ==  testCaseAttachListId
		attachments.get(0).content.stream.text.equals(content_2_streamTxt) == true
		attachments.get(0).name.equals(content_2_name) == true

		//4°) remove Attachment_1 on duplicateTEstCase
		when:
		attachService.removeListOfAttachments(duplicateTestCaseAttachListId, [duplicateTestCaseAttach_1_Id])
		then:
		areOrheanContents() == false
		//no more Attachment, no more Content_1
		when:
		attachments.clear()
		attachments = getAttachemntsForAttachmentContent(testCaseUniqueContent_1_Id)
		attachments.size() == 0
		then:
		List<AttachmentContent> contents = getAttachemntContentsById(testCaseUniqueContent_1_Id)
		then:
		contents.size() == 0

		when:
		//no change for Content_2
		attachments.clear()
		attachments = getAttachemntsForAttachmentContent(testCaseUniqueContent_2_Id)
		then:
		attachments.size() == 1
		attachments.get(0).id ==  testCaseAttach_2_Id
		attachments.get(0).attachmentList.id ==  testCaseAttachListId
		attachments.get(0).content.stream.text.equals(content_2_streamTxt) == true
		attachments.get(0).name.equals(content_2_name) == true

		//4°) remove Attachment_2 on TEstCase
		when:
		attachService.removeListOfAttachments(testCaseAttachListId, [testCaseAttach_2_Id])
		then:
		areOrheanContents() == false
		//no more Content_1
		when:
		attachments.clear()
		attachments = getAttachemntsForAttachmentContent(testCaseUniqueContent_2_Id)
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
		attachments = getAttachemntsForAttachmentContent(testCaseUniqueContent_2_Id)
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
//		                  /*******************
//		                  *  "folder c" (PJ new6]
//						  *    |
//						  *    "folder Test Case 2" (PJ new5.txt)
//						  *        |
//						  *         "tc 2" (3 steps =>PJ new6.txt on step 2)
//						  *   |
//						  *    "tc 2 copie"  (3 steps =>PJ new6.txt on step 2)
//						  *********************/
		def targetFolderId = -237L //folder a
		def srcFolderAttachListId = -917L
		def targetFolderAttachListId = -898L
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
//	def "attachments shallowCopy: workspace requirement: copy tree folder to library"() {
//		given:
//		def attachmentListFolderId = -254L
//
//
//		when:
//			reqVersionService.createNewVersion(collection long, false, false)
//			reqNavService.copyNodesToLibrary()
//		then:
//
//	}

	/** *************************************************************
	                 UTILS
	************************************************************** */

	protected <R> List<R> executeSelectSQLQuery(String queryString) {
		Query query = getSession().createSQLQuery(queryString)
		return query.getResultList()
	}

	protected Long executeCountSelectSQLQuery(String queryString) {
		Query query = getSession().createSQLQuery(queryString)
		return query.getSingleResult()
	}


	protected boolean areOrheanContents() {
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


	protected List<Attachment> getAttachemntsForAttachmentContent(Long contentID) {
		return executeSelectQuery("SELECT attachment from Attachment attachment where attachment.content.id = :id order by attachment.id ASC", "id", contentID)
	}

	protected List<AttachmentContent> getAttachemntContentsById(Long contentID) {
		return executeSelectQuery("SELECT content from AttachmentContent content where content.id = :id order by content.id ASC", "id", contentID)
	}

	protected Long countAttachemntsForAttachmentContent(Long contentID) {
		return getAttachemntsForAttachmentContent(contentID).size()
	}

	protected Long countTotalAttachmentContent() {
		return executeCountSelectSQLQuery("SELECT count(*) FROM attachment_content")
	}

	protected Long countTotalAttachment() {
		return executeCountSelectSQLQuery("SELECT count(*) FROM attachment")
	}

	protected Long countTotalAttachmentList() {
		return executeCountSelectSQLQuery("SELECT count(*) FROM attachment_list")
	}

	protected boolean checkIdAndStreamOfContent(Long idRef, String streamToTxtRef, AttachmentContent underTest) {
		boolean result = (idRef == underTest.id)
		if (result) {
			result = streamToTxtRef.equals(underTest.getStream().text)
		}
		return result
	}

	protected  boolean checkIdAndNameAndStreamOfAttachment(Long idRef,String attName,  String streamToTxtRef, Attachment underTest) {
		boolean result
		result = attName.equals(underTest.name)
		if (result) {
			result = checkIdAndStreamOfContent(idRef, streamToTxtRef, underTest.content)
		}
		return result
	}

	private mockDataSourceUrl() {
		DataSourceProperties ds = Mock()
		ds.getUrl() >> "NotNullPointer"
		tcNavService.deletionHandler.deletionDao.dataSourceProperties  = ds
	}
}
