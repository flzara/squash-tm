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
import org.hibernate.FlushMode
import org.hibernate.Query
import org.hibernate.type.LongType
import org.springframework.core.io.ClassPathResource
import org.springframework.transaction.annotation.Transactional
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.squashtest.tm.domain.attachment.Attachment
import org.squashtest.tm.domain.attachment.AttachmentContent
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.service.project.GenericProjectManagerService
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
	TestCaseModificationService service

	@Inject
	TestCaseLibraryNavigationService navService

	@Inject
	AttachmentManagerService attachService;

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
		def attachListId = service.findById(testCaseId).attachmentList.id;
		def attachList = service.findById(testCaseId).getAttachmentList();

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
		TestCase testCase = service.findById(testCaseIdWithoutAttachment);

		then:
		!testCase.attachmentList.hasAttachments()

		and:
		File source = sourceFile()
		RawAttachment raw = rawAttachment(source, "image.jpg")

		when:
		attachService.addAttachment(attachListId, raw)
		session.flush()
		TestCase testCase2 = service.findById(testCaseId)

		then:
		testCase2.attachmentList.hasAttachments()
	}

	//**********************************************************************************************************************************************
	//  TESTS FOR [TM-362] =>   shallowCopy of AttachmentContent instead of hardCopy on copy/past TM's item (TestCase, Requirement, execution and so)
	//**********************************************************************************************************************************************

	def "attachments shallowCopy: add a PJ to a TestCase with 3 steps and copy it "() {

		//"TC 3 classique" : ID=-249 (AttList = -929) , 3 steps 179,180,181 -  1PJ on step 179 List = 930
		given:
		def testCase = -249L
		def testCaseAttachList = -929L
		def step1 = -179L
		def step2 = -180L
		def step3 = -181L
		def step1AttchList = -930L
		def step2AttchList = -931L
		def step3AttchList = -932L
		def attachIdStep1
		//PJ of step
//		def pjNameStep =

		//1°) checking Dataset
		when:
		def tcAttachList = executeSelectSQLQuery(
			"select ATTACHMENT_LIST_ID from TEST_CASE_LIBRARY_NODE WHERE TCLN_ID = " + testCase)

		//steps
		List<long[]> result = executeSelectSQLQuery(
			"select TEST_STEP_ID, ATTACHMENT_LIST_ID from action_test_step inner join test_case_steps ON TEST_CASE_STEPS.STEP_ID = ACTION_TEST_STEP.TEST_STEP_ID where TEST_CASE_ID = "
				+ testCase + "ORDER BY 1 DESC") //negative numbers

		then:
		areOrheanContents() == false
		tcAttachList.size == 1
		tcAttachList[0] == testCaseAttachList
		//Dataset expected  3 steps
		result.size() == 3
		result[0][0] == step1
		result[1][0] == step2
		result[2][0] == step3

		result[0][1] == step1AttchList
		result[1][1] == step2AttchList
		result[2][1] == step3AttchList


		when:
//		def String attLists = testCaseAttachList + "," + step1AttchList + "," + step2AttchList + "," + step3AttchList
//		List<Attachment> attachments = executeSelectSQLQuery(
//			"select * from Attachment where attachment_list_id in ("+attLists +")")

//		List<Long> attchLists = new ArrayList<>()
//		attchLists.add((Long)testCaseAttachList)
//		attchLists.add((Long)step1AttchList)
//		attchLists.add((Long)step2AttchList)
//		attchLists.add((Long)step3AttchList)

		List<Long> attchLists = new ArrayList<>()
		attchLists.add(testCaseAttachList)
		attchLists.add(step1AttchList)
		attchLists.add(step2AttchList)
		attchLists.add(step3AttchList)


//		attchLists.add(-1)
//		attchLists.add(-2)
//		attchLists.add(-3)
		List<Attachment> attachments = getAttachmentsFromLists(attchLists)
//		  executeSelectNamedQuery("SELECT attachment from Attachment where content.id in (:ids)", "ids", attchLists)

		then:
		//expected only 1 PJ on step1
		1 == 2
		attachments.size() == 1
		attachments.get(0).attachmentList == step1AttchList
		attachments.get(0).getId() == -179
		attachments.get(0).name == "new6.txt"


//		attachService.removeAttachmentFromList(attachListId, id)
//		em.flush()
//		attachments.size() == 1
//
//		then :
//		Set<Attachment> attached = attachService.findAttachments(attachListId)
//		attached.size()==1
//		//check content really existattachments
//		Attachment attachment;
//		attachment.getContent().content
//		attachService.writeContentattachmentId, outStream)

	}
//

	//**************************************************************
	//                              UTILS
	//**************************************************************

	protected <R> List<R> executeSelectSQLQuery(String queryString) {
		Query query = getSession().createSQLQuery(queryString)
		return query.list()
	}
//
//	protected Long executeSelectCountSQLQuery(String queryString, String paramName, Collection<Long> ids) {
//			Query query = getSession().createSQLQuery(queryString)
//			query.setParameterList(paramName, ids, LongType.INSTANCE)
//			return query.getSingleResult()
//	}

	protected boolean areOrheanContents() {
		def result = executeSelectSQLQuery(
			"SELECT attachment.attachment_id, attachment_content.ATTACHMENT_CONTENT_ID " +
				"from attachment_content left join attachment on attachment_content.ATTACHMENT_CONTENT_ID = attachment.CONTENT_ID where attachment.attachment_id is null")
		return !result.isEmpty()
	}


	protected <R> List<R> executeSelectQuery(String stQuery, String paramName, Collection<Long> ids) {
		if (!ids.isEmpty()) {
			Query query = getSession().createQuery(stQuery)
//			query.setParameterList(paramName, ids, LongType.INSTANCE)
			query.setParameterList(paramName, ids)
			return query.list()
		} else {
			return Collections.emptyList()
		}
	}

	protected List<Attachment> getAttachmentsFromLists(Collection<Long> attchLists) {
		return executeSelectQuery("select attach from Attachment attach where attach.attachmentList.id in (:ids)", "ids", attchLists)

	}

	//non testé ...
	protected List<Attachment> getAttachemntsFoAttachmentContent(Long contentID) {
		return executeSelectQuery("SELECT attachment from Attachment where attachment.content.id =id", "id", contentID)
	}

}
