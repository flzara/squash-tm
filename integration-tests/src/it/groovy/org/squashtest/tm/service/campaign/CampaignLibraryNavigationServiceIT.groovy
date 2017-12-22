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
package org.squashtest.tm.service.campaign

import org.junit.runner.RunWith
import org.spockframework.runtime.Sputnik
import org.springframework.transaction.annotation.Transactional
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.squashtest.tm.domain.campaign.*
import org.squashtest.tm.domain.customfield.CustomFieldValue
import org.squashtest.tm.exception.DuplicateNameException
import org.squashtest.tm.exception.library.CannotMoveInHimselfException
import org.squashtest.tm.service.customfield.CustomFieldValueFinderService
import org.squashtest.tm.service.internal.repository.CampaignFolderDao
import org.squashtest.tm.service.project.GenericProjectManagerService
import org.unitils.dbunit.annotation.DataSet
import org.unitils.dbunit.annotation.ExpectedDataSet
import spock.unitils.UnitilsSupport

import javax.inject.Inject

@UnitilsSupport
@Transactional
@RunWith(Sputnik)
class CampaignLibraryNavigationServiceIT extends DbunitServiceSpecification {


	@Inject
	private CampaignLibraryNavigationService navService

	@Inject
	private CampaignFolderDao folderDao

	@Inject
	GenericProjectManagerService genericProjectManager

	@Inject
	CustomFieldValueFinderService customFieldValueService

	private Long libId = -1
	private Long campId = -10
	private Long folderId = -1


	@DataSet("CampaignLibraryNavigationServiceIT.xml")
	def "should not persist a nameless campaign"() {
		given:
		Campaign camp = new Campaign()
		when:
		navService.addCampaignToCampaignLibrary(libId, camp)
		session.flush()
		then:

		thrown RuntimeException
	}

	@DataSet("CampaignLibraryNavigationServiceIT.xml")
	def "should not persist a nameless iteration"() {
		given:
		Campaign camp = new Campaign(name: "cp")
		navService.addCampaignToCampaignLibrary(libId, camp)
		Iteration iter = new Iteration()
		when:
		navService.addIterationToCampaign(iter, camp.id)
		then:
		thrown(RuntimeException)
	}


	@DataSet("CampaignLibraryNavigationServiceIT.xml")
	def "should add folder to library and fetch it back"() {
		given:
		def folder = new CampaignFolder(name: "folder 2")
		navService.addFolderToLibrary(libId, folder)
		when:
		def obj = navService.findFolder(folder.id)

		then:
		obj != null
		obj.id != null
		obj.name == folder.name
	}

	@DataSet("CampaignLibraryNavigationServiceIT.xml")
	def "should not add a folder to library"() {
		given:
		def folder = new CampaignFolder(name: "a folder")    //same as the one in the dataset

		when:
		navService.addFolderToLibrary(libId, folder)

		then:
		thrown(DuplicateNameException)

	}


	@DataSet("CampaignLibraryNavigationServiceIT.xml")
	def "should add folder to folder and fetch it back"() {
		given:
		def folder = new CampaignFolder(name: "folder 2")
		navService.addFolderToFolder(folderId, folder)


		when:
		def obj = navService.findFolder(folder.id)

		then:
		obj != null
		obj.id != null
		obj.name == folder.name
	}


	@DataSet("CampaignLibraryNavigationServiceIT.xml")
	def "should find root content of library"() {
		given:
		def folder2 = new CampaignFolder(name: "folder 2")
		def folder3 = new CampaignFolder(name: "folder 3")
		def campaign = new Campaign(name: "campaign 1")

		navService.addFolderToLibrary(libId, folder2)
		navService.addFolderToLibrary(libId, folder3)
		navService.addCampaignToCampaignLibrary(libId, campaign)

		when:
		List<CampaignLibraryNode> rootContent = navService.findLibraryRootContent(libId)


		then:
		rootContent.size() == 4
		rootContent.collect { it.id }.containsAll([
			folderId,
			folder2.id,
			folder3.id,
			campaign.id
		])
	}


	@DataSet("CampaignLibraryNavigationServiceIT.xml")
	def "should find content of folder"() {
		given:
		def folder2 = new CampaignFolder(name: "folder 2")
		def folder3 = new CampaignFolder(name: "folder 3")
		def campaign = new Campaign(name: "campaign 2")

		navService.addFolderToFolder(folderId, folder2)
		navService.addFolderToFolder(folderId, folder3)
		navService.addCampaignToCampaignFolder(folderId, campaign)

		when:
		List<CampaignLibraryNode> folderContent = navService.findFolderContent(folderId)


		then:
		folderContent.size() == 4
		folderContent.collect { it.id }.containsAll([
			campId,
			folder2.id,
			folder3.id,
			campaign.id
		])
	}

	@DataSet("CampaignLibraryNavigationServiceIT.xml")
	def "should add campaign to campaign folder and fetch it back"() {
		given:
		def campaign = new Campaign(name: "new campaign", description: "test campaign")

		when:
		navService.addCampaignToCampaignFolder(folderId, campaign)
		def obj = findCampaign(campaign.id)
		then:
		obj != null
		obj.name == "new campaign"
		obj.description == "test campaign"
	}


	@DataSet("CampaignLibraryNavigationServiceIT.xml")
	def "should not add campaign to campaign folder"() {
		given:
		def campaign = new Campaign(name: "campaign 1", description: "test campaign") //same as in the setup() clause

		when:
		navService.addCampaignToCampaignFolder(folderId, campaign)

		then:
		thrown(DuplicateNameException)
	}


	@DataSet("CampaignLibraryNavigationServiceIT.xml")
	def "sould add campaign to campaign library and fetch it back"() {
		given:
		def campaign = new Campaign(name: "test campaign", description: "test campaign")
		when:
		navService.addCampaignToCampaignLibrary(libId, campaign)
		def obj = findCampaign(campaign.id)
		then:
		obj != null
		obj.name == "test campaign"
		obj.description == "test campaign"
	}


	@DataSet("CampaignLibraryNavigationServiceIT.xml")
	def "should not add campaign to campaign library"() {
		given:
		def campaign1 = new Campaign(name: "test campaign 1", description: "test campaign")
		navService.addCampaignToCampaignLibrary(libId, campaign1)
		when:
		def campaign2 = new Campaign(name: "test campaign 1", description: "test campaign")
		navService.addCampaignToCampaignLibrary libId, campaign2
		then:
		thrown(DuplicateNameException)
	}


	@DataSet("CampaignLibraryNavigationServiceIT.xml")
	def "should find test campaign"() {
		given:
		true

		when:
		def obj = findCampaign(campId)

		then:
		obj != null
		obj.name == "campaign 1"
		obj.description == "the first campaign"
	}

	@DataSet("CampaignLibraryNavigationServiceIT.should copy paste iterations to campaign.xml")
	def "should copy paste iterations to campaign"() {
		given:
		Long[] iterationList = [-1L, -2L]
		Long targetCampaignId = -11L

		when:

		List<Iteration> iterations = navService.copyIterationsToCampaign(targetCampaignId, iterationList)

		then:
		iterations.size() == 2
		iterations.get(0).name == "iter - tc1"
		iterations.get(1).name == "iter - tc1 -2"

	}

	@DataSet("CampaignLibraryNavigationServiceIT.should copy paste iterations with testSuites.xml")
	def "should copy paste iterations with testSuites"() {
		given:
		Long[] iterationList = [-10012L, -2L]
		Long targetCampaignId = -11L

		when:
		List<Iteration> iterations = navService.copyIterationsToCampaign(targetCampaignId, iterationList)

		then: "2 iterations are copied"
		iterations.size() == 2
		and: "the copy 'iter-tc1' has 2 test-suites"
		iterations.find { it.getName() == "iter - tc1" } != null
		Iteration iteration1 = iterations.find { it.getName() == "iter - tc1" }
		iteration1.getTestSuites().size() == 2
		and: "the 'test-suite1' has been bound to the item-test-plan already copied with the iteration"
		iteration1.getTestSuites().find { it.getName() == "testSuite1" } != null
		TestSuite testsSuite1 = iteration1.getTestSuites().find { it.getName() == "testSuite1" }
		testsSuite1.getTestPlan().size() == 1
		iteration1.getTestPlans().size() == 1
		and: "the 'test-suite2' is found and has no test-plan-item"
		iteration1.getTestSuites().find { it.getName() == "testSuite2" } != null
		TestSuite testsSuite2 = iteration1.getTestSuites().find { it.getName() == "testSuite2" }
		testsSuite2.getTestPlan().size() == 0
	}

	@DataSet("CampaignLibraryNavigationServiceIT.should copy paste iterations with testSuites containing CUF.xml")
	def "should copy paste iterations with testSuites containing CUF"() {
		given:
		Long iteration = -10012L
		Long targetCampaignId = -11L
		Long customFieldValueId = -42L

		when:
		List<Iteration> iterations = navService.copyIterationsToCampaign(targetCampaignId, iteration)
		Iteration iteration1 = iterations.find { it.name == "iter - tc1" }
		TestSuite copiedTestSuite = iteration1.getTestSuites().get(0)
		List<CustomFieldValue> customFieldValues = customFieldValueService.findAllCustomFieldValues(copiedTestSuite)
		CustomFieldValue updatedCustomField = customFieldValues.get(0)

		then: "an iterations is copied"
		iterations.size() == 1
		and: "the copy 'iter-tc1' has 1 test-suite"
		iterations.find { it.getName() == "iter - tc1" } != null
		iteration1.getTestSuites().size() == 1
		and: "the test-suite has a CUF (plain text) with an updated value"
		updatedCustomField.getValue().equals("updated value")

	}

	@DataSet("CampaignLibraryNavigationServiceIT.should copy paste campaigns with iterations.xml")
	def "should copy paste campaigns with iterations"() {
		given:
		Long[] sourceIds = [-10L]
		Long destinationId = -1L

		when:
		List<Campaign> campaigns = navService.copyNodesToFolder(destinationId, sourceIds)

		then:
		campaigns.get(0).getIterations().size() == 2
		def iterations = campaigns.get(0).getIterations()
		iterations.find { it.getName() == "iter - tc1" } != null
		iterations.find { it.getName() == "iter - tc1 -2" } != null
	}

	@DataSet("CampaignLibraryNavigationServiceIT.should copy paste campaigns with testSuites.xml")
	def "should copy paste campaigns with testSuites"() {
		given:
		Long[] targetIds = [-10L]
		Long destinationId = -1L

		when:
		List<Campaign> campaigns = navService.copyNodesToFolder(destinationId, targetIds)

		then:
		campaigns.get(0).getIterations().size() == 2
		def iterations = campaigns.get(0).getIterations()
		Iteration iteration1 = iterations.find { it.getName() == "iter - tc1" }
		iteration1.getTestSuites().size() == 2
		iteration1.getTestSuites().find { it.getName() == "testSuite1" } != null
		TestSuite testsSuite1 = iteration1.getTestSuites().find { it.getName() == "testSuite1" }
		testsSuite1.getTestPlan().size() == 1
		iteration1.getTestSuites().find { it.getName() == "testSuite2" } != null
		TestSuite testsSuite2 = iteration1.getTestSuites().find { it.getName() == "testSuite2" }
		testsSuite2.getTestPlan().size() == 0
		Iteration iteration2 = iterations.find { it.getName() == "iter - tc1 -2" }
		iteration2.getTestSuites().isEmpty()
	}

	@DataSet("CampaignLibraryNavigationServiceIT.should copy paste folder with campaigns, iterations, suite.xml")
	def "should copy paste folder with campaigns, iterations, suites"() {
		given:
		Long[] sourceIds = [-1L]
		Long destinationId = -2L

		when:
		List<CampaignLibraryNode> campaignNodes = navService.copyNodesToFolder(destinationId, sourceIds)

		then: "campaign folder has 2 campaigns"
		campaignNodes.get(0) instanceof CampaignFolder
		CampaignFolder folderCopy = (CampaignFolder) campaignNodes.get(0)
		folderCopy.content.size() == 2
		folderCopy.content.find { it.name == "campaign10" } != null
		folderCopy.content.find { it.name == "campaign11" } != null

		and: "campaign 1 has 2 iterations"
		Campaign campaign10Copy = folderCopy.content.find { it.name == "campaign10" }
		campaign10Copy.iterations.size() == 2
		campaign10Copy.iterations.find { it.name == "iter - tc1" } != null
		campaign10Copy.iterations.find { it.name == "iter - tc1 -2" } != null

		and: "iteration 1 has 2 test suites"
		Iteration iteration10012 = campaign10Copy.iterations.find { it.name == "iter - tc1" }
		iteration10012.testSuites.size() == 2
		iteration10012.testSuites.find { it.name == "testSuite1" } != null
		iteration10012.testSuites.find { it.name == "testSuite2" } != null

		and: "iteration 2 has no test suites"
		Iteration iteration2 = campaign10Copy.iterations.find { it.name == "iter - tc1 -2" }
		iteration2.testSuites.isEmpty()

		and: "campaign 2 is empty"
		Campaign campaign11Copy = folderCopy.content.find { it.name == "campaign11" }
		campaign11Copy.iterations.isEmpty()
	}

	@DataSet("CampaignLibraryNavigationServiceIT.should move to same project f+c.xml")
	@ExpectedDataSet("CampaignLibraryNavigationServiceIT.should move to same project f+c-result.xml")
	def "should move folder + campaigns to same project"() {
		given:
		Long[] sourceIds = [-1L]
		Long destinationId = -2L

		when:
		navService.moveNodesToFolder(destinationId, sourceIds)

		then: "expected dataset is verified"
		session.flush()
	}

	@DataSet("CampaignLibraryNavigationServiceIT.should not move in himself.xml")
	def "should not move in himself"() {
		given:
		Long[] sourceIds = [-1L]
		Long destinationId = -1L

		when:
		navService.moveNodesToFolder(destinationId, sourceIds)

		then:
		thrown(CannotMoveInHimselfException)
	}

	@DataSet("CampaignLibraryNavigationServiceIT.should not move in himself.xml")
	def "should not move in his decendents"() {
		given:
		Long[] sourceIds = [-13L]
		Long destinationId = -1L

		when:
		navService.moveNodesToFolder(destinationId, sourceIds)

		then:
		thrown(CannotMoveInHimselfException)
	}


	@DataSet("CampaignLibraryNavigationServiceIT.should move to another project f+c.xml")
	@ExpectedDataSet("CampaignLibraryNavigationServiceIT.should move to another project f+c-result.xml")
	def "should move folder + campaigns to another project"() {
		given:
		Long[] sourceIds = [-1L]
		Long destinationId = -2L

		when:
		navService.moveNodesToFolder(destinationId, sourceIds)

		then: "expected dataset is verified"
		session.flush()
	}

	@DataSet("CampaignLibraryNavigationServiceIT.should move to another project f+c + cufs.xml")
	@ExpectedDataSet("CampaignLibraryNavigationServiceIT.should move to another project f+c + cufs-result.xml")
	def "should move folder + campaigns  with cufs to another project"() {
		given:
		Long[] sourceIds = [-1L]
		Long destinationId = -2L

		when:
		navService.moveNodesToFolder(destinationId, sourceIds)

		then: "expected dataset is verified"
		session.flush()
	}

	@DataSet("CampaignLibraryNavigationServiceIT.should move to another project f+c+i+s + cufs + execs.xml")
	@ExpectedDataSet("CampaignLibraryNavigationServiceIT.should move to another project f+c+i+s + cufs + execs-result.xml")
	def "should move folder + campaigns + iterations + suites with cufs and issues to another project"() {
		given:
		Long[] sourceIds = [-1L]
		Long destinationId = -2L

		when:
		navService.moveNodesToFolder(destinationId, sourceIds)

		then: "expected dataset is verified"
		session.flush()
	}


	@DataSet("CampaignLibraryNavigationServiceIT.should move a grap of folders.xml")
	@ExpectedDataSet("CampaignLibraryNavigationServiceIT.should move a grap of folders-result.xml")
	def "should move a grap of folder in a new project"() {
		given:
		Long[] sourceIds = [-1L]
		Long destinationId = -2L

		when:
		navService.moveNodesToFolder(destinationId, sourceIds)

		then: "expected dataset is verified"
		session.flush()
	}

	@DataSet("CampaignLibraryNavigationServiceIT.should move to another project and keep issues.xml")
	@ExpectedDataSet("CampaignLibraryNavigationServiceIT.should move to another project and keep issues-result.xml")
	def "should move to another project and keep issues"() {
		given: "a dataset with 2 projects having the same bugtracker"
		Long[] sourceIds = [-1L]
		Long destinationId = -2L

		when:
		navService.moveNodesToFolder(destinationId, sourceIds)

		then: "issues are kept"
		session.flush()
	}

	@DataSet("CampaignLibraryNavigationServiceIT.should move to another project and remove issues.xml")
	@ExpectedDataSet("CampaignLibraryNavigationServiceIT.should move to another project and remove issues-result.xml")
	def "should move to another project and remove issues"() {
		given: "a dataset with 2 projects having different bugtrackers"
		Long[] sourceIds = [-1L]
		Long destinationId = -2L

		when:
		navService.moveNodesToFolder(destinationId, sourceIds)

		then: "issues are removed"
		session.flush()
	}


	@DataSet("CampaignLibraryNavigationServiceIT.should move to same project at right position.xml")
	def "should move folder with campaigns to the right position - first"() {
		given:
		Long[] sourceIds = [-1L]
		Long destinationId = -2L

		when:
		navService.moveNodesToFolder(destinationId, sourceIds, 0)

		then:
		CampaignFolder parentFolder = (CampaignFolder) folderDao.findById(-2L)
		parentFolder.content*.id.containsAll([-1L, -20L, -21L])
	}

	@DataSet("CampaignLibraryNavigationServiceIT.should move to same project at right position.xml")
	def "should move folder with campaigns to the right position - middle"() {
		given:
		Long[] sourceIds = [-1L]
		Long destinationId = -2L

		when:
		navService.moveNodesToFolder(destinationId, sourceIds, 1)

		then:
		CampaignFolder parentFolder = (CampaignFolder) folderDao.findById(-2L)
		parentFolder.content*.id.containsAll([-20L, -1L, -21L])
	}

	@DataSet("CampaignLibraryNavigationServiceIT.should move to same project at right position.xml")
	def "should move folder with campaigns to the right position - last"() {
		given:
		Long[] sourceIds = [-1L]
		Long destinationId = -2L

		when:
		navService.moveNodesToFolder(destinationId, sourceIds, 2)

		then:
		CampaignFolder parentFolder = (CampaignFolder) folderDao.findById(-2L)
		parentFolder.content*.id.containsAll([-20L, -21L, -1L])
	}


	def findCampaign(id) {
		session.load(Campaign, id)
	}
}
