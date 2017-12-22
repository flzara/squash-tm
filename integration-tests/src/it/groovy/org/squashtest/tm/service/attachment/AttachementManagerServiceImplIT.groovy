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

import javax.inject.Inject

import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.springframework.core.io.ClassPathResource
import org.springframework.transaction.annotation.Transactional
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.squashtest.tm.domain.attachment.Attachment
import org.squashtest.tm.domain.project.GenericProject
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.testcase.TestCaseFolder
import org.squashtest.tm.service.project.GenericProjectManagerService
import org.squashtest.tm.service.testcase.TestCaseLibraryNavigationService
import org.squashtest.tm.service.testcase.TestCaseModificationService
import org.unitils.dbunit.annotation.DataSet;

import spock.unitils.UnitilsSupport

@UnitilsSupport
@Transactional
@DataSet
class AttachmentManagerServiceImplIT extends DbunitServiceSpecification {

	@Inject	TestCaseModificationService service

	@Inject TestCaseLibraryNavigationService navService

	@Inject AttachmentManagerService attachService;

	@Inject GenericProjectManagerService genericProjectManager
	
	// IDs : see dataset
	int folderId = -1;
	int testCaseId=-2;
	int attachListId = -3;



	def "should create an AttachmentList along with a TestCase"(){
		given :


		when :
		def attachListId = service.findById(testCaseId).attachmentList.id;
		def attachList = service.findById(testCaseId).getAttachmentList();

		then :
		attachList != null
		attachList.id == attachListId;
	}


	def "should add a new attachment and retrieve it"(){
		given :
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


		when :
		Long id = attachService.addAttachment(attachListId, raw)
		session.flush()

		Attachment attach =  session.load(Attachment, id)

		then : "attachment correctly created"
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

	def "should add and retrieve a lot of attachment headers"(){
		given :
		File source = sourceFile()

		def raws = []
		raws << rawAttachment(source, "att1.jpg")
		raws << rawAttachment(source, "att2.jpg")
		raws << rawAttachment(source, "att3.jpg")

		when :
		List<Long> ids = []

		raws.each {
			ids << attachService.addAttachment(attachListId, it)
		}

		session.flush()
		session.clear()

		Set<Attachment> attached = attachService.findAttachments(attachListId)

		then :
		attached*.id.containsAll (ids);
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



	byte[] randomBytes(int howMany){
		byte [] result = new byte[howMany];
		for (int i=0;i<howMany;i++){
			result[i]=Math.round(Math.random()*255);
		}
		return result;
	}

	def "should remove an attachment"(){
		given :
		File source = sourceFile()
		RawAttachment raw = rawAttachment(source, "image.jpg")
		Long id = attachService.addAttachment(attachListId, raw)
		session.flush()

		when :
		attachService.removeAttachmentFromList(attachListId, id)
		session.flush()
		Set<Attachment> attached = attachService.findAttachments(attachListId)

		then :
		attached.size()==0
	}

	def "should correctly tell if a test case have attachments or not"(){
		when :
		TestCase testCase = service.findById(testCaseId);

		then:
		!testCase.attachmentList.hasAttachments()

		and:
		File source = sourceFile()
		RawAttachment raw = rawAttachment(source, "image.jpg")

		when:
		attachService.addAttachment(attachListId, raw);
		session.flush()
		TestCase testCase2 = service.findById(testCaseId);

		then:
		testCase2.attachmentList.hasAttachments()
	}


}
