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
import org.squashtest.tm.domain.campaign.CampaignFolder
import org.squashtest.tm.domain.campaign.CampaignLibrary
import org.squashtest.tm.domain.campaign.CampaignLibraryNode
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.exception.DuplicateNameException
import org.squashtest.tm.service.campaign.IterationModificationService
import org.squashtest.tm.service.internal.customfield.PrivateCustomFieldValueService
import org.squashtest.tm.service.internal.library.AbstractLibraryNavigationService
import org.squashtest.tm.service.internal.repository.CampaignDao
import org.squashtest.tm.service.internal.repository.CampaignFolderDao
import org.squashtest.tm.service.internal.repository.CampaignLibraryDao
import org.squashtest.tm.service.internal.repository.IterationDao
import org.squashtest.tm.service.security.PermissionEvaluationService
import spock.lang.Specification

class CampaignLibraryNavigationServiceImplTest extends Specification {

	CampaignLibraryNavigationServiceImpl service = new CampaignLibraryNavigationServiceImpl();

	CampaignLibraryDao campaignLibraryDao = Mock()
	CampaignFolderDao campaignFolderDao = Mock()
	CampaignDao campaignDao = Mock()
	PermissionEvaluationService permissionService = Mock()
	IterationModificationService iterationModificationService = Mock()
	IterationDao iterationDao = Mock()
	PrivateCustomFieldValueService customFieldService = Mock()

	def setup() {
		service.campaignLibraryDao = campaignLibraryDao
		service.campaignFolderDao = campaignFolderDao
		service.campaignDao = campaignDao
		permissionService.hasRoleOrPermissionOnObject(_, _, _) >> true
		service.iterationModificationService = iterationModificationService
		service.iterationDao = iterationDao

		use (ReflectionCategory) {
			AbstractLibraryNavigationService.set(field: "permissionService", of: service, to: permissionService)
			AbstractLibraryNavigationService.set(field: "customFieldValuesService", of: service, to: customFieldService)
		}

		customFieldService.findAllCustomFieldValues(_) >> []
		customFieldService.findAllCustomFieldValues(_, _) >> []
	}


	def "should add folder to library"(){
		given:
		CampaignFolder newFolder = Mock()
		and:
		CampaignLibrary container = Mock()
		container.isContentNameAvailable(_) >> true
		campaignLibraryDao.findById(10) >> container

		when:
		service.addFolderToLibrary(10, newFolder)

		then:
		1 * container.addContent(newFolder)
		1 * campaignFolderDao.persist(newFolder)
	}


	def "should find folder"() {
		given:
		CampaignFolder f = Mock()
		campaignFolderDao.findById(10) >> f

		when:
		def found = service.findFolder(10)

		then:
		found == f
	}

	def "should add folder to folder"() {
		given:
		CampaignFolder newFolder = Mock()
		and:
		CampaignFolder container = Mock()
		container.isContentNameAvailable(_) >> true
		campaignFolderDao.findById(10) >> container

		when:
		service.addFolderToFolder(10, newFolder)

		then:
		container.addContent newFolder
		1 * campaignFolderDao.persist(newFolder)
	}



	def "should find root content of library"() {
		given:
		def rootContent = [
			Mock(CampaignLibraryNode),
			Mock(CampaignLibraryNode)
		]
		campaignLibraryDao.findAllRootContentById(10) >> rootContent


		when:
		def found = service.findLibraryRootContent(10)

		then:
		found == rootContent
	}

	def "should find content of folder"() {
		given:
		def content = [
			Mock(CampaignLibraryNode),
			Mock(CampaignLibraryNode)
		]
		campaignFolderDao.findAllContentById(10) >> content


		when:
		def found = service.findFolderContent(10)

		then:
		found == content
	}


	def "should add campaign to campaign folder"(){
		given:
		Project project = Mock()
		project.getId() >> 1L;
		Campaign campaign = Mock()
		campaign.getProject() >> project;
		and:
		CampaignFolder container = Mock()
		container.isContentNameAvailable(_) >> true
		campaignFolderDao.findById(10) >> container

		when:
		service.addCampaignToCampaignFolder(10, campaign)

		then:
		container.addContent campaign
		1 * campaignDao.persist(campaign)
	}

	def "should not add campaign to campaign folder"(){
		given:
		Campaign campaign = Mock()
		and:
		CampaignFolder container = Mock()
		campaignFolderDao.findById(10) >> container
		container.isContentNameAvailable(_) >> false

		when:
		service.addCampaignToCampaignFolder(10, campaign)

		then:
		thrown(DuplicateNameException)
	}


	def "sould add campaign to campaign library"(){
		given:
		Project project = Mock()
		project.getId() >> 1L;
		Campaign campaign = Mock()
		campaign.getProject() >> project;
		and:
		CampaignLibrary container = Mock()
		container.isContentNameAvailable(_) >> true
		campaignLibraryDao.findById(10) >> container

		when:
		service.addCampaignToCampaignLibrary(10, campaign)

		then:
		container.addContent campaign
		1 * campaignDao.persist(campaign)
	}


	def "should not add campaign to campaign library"(){
		given:
		Campaign campaign = Mock()
		and:
		CampaignLibrary container = Mock()
		container.isContentNameAvailable(_) >> false
		campaignLibraryDao.findById(10) >> container

		when:
		service.addCampaignToCampaignLibrary(10, campaign)

		then:
		thrown (DuplicateNameException)
	}

	def "should find library"() {
		given:
		CampaignLibrary l = Mock()
		campaignLibraryDao.findById(10) >> l

		when:
		def found = service.findLibrary(10)

		then:
		found == l
	}
}
