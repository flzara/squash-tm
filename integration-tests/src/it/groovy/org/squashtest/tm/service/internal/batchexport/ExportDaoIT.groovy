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
package org.squashtest.tm.service.internal.batchexport

import org.junit.runner.RunWith
import org.spockframework.runtime.Sputnik
import org.springframework.transaction.annotation.Transactional
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.squashtest.tm.core.foundation.lang.DateUtils
import org.squashtest.tm.domain.testcase.TestCaseAutomatable
import org.squashtest.tm.domain.testcase.TestCaseImportance
import org.squashtest.tm.domain.testcase.TestCaseKind
import org.squashtest.tm.domain.testcase.TestCaseStatus
import org.squashtest.tm.service.internal.batchexport.RequirementExportModel.RequirementLinkModel
import org.unitils.dbunit.annotation.DataSet
import spock.unitils.UnitilsSupport

import javax.inject.Inject

import static org.squashtest.tm.domain.testcase.TestCaseKind.STANDARD
import static org.squashtest.tm.service.internal.batchexport.ExportModel.TestCaseModel

@UnitilsSupport
@Transactional
@RunWith(Sputnik)
class ExportDaoIT extends DbunitServiceSpecification{

	@Inject
	private ExportDao exporter


	@DataSet("ExportDaoIT.should create models.xml")
	def "should create models"(){
		given :
		def testCaseIds = [-10L, -11L]

		when :
		ExportModel result = exporter.findModel(testCaseIds)

		then :
		result.testCases.size() == 2
		result.datasets.size() == 1
		result.parameters.size() == 1
		result.testSteps.size() == 2

	}

	@DataSet("ExportDaoIT.should create scripted test case model.xml")
	def "should create scripted test case models"(){
		given :
		def testCaseIds = [-10L, -11L]

		when :
		ExportModel result = exporter.findModel(testCaseIds)
		List<ExportModel.TestCaseModel> testCases = result.testCases
		ExportModel.TestCaseModel standardTestCaseModel = testCases.find{it.id == -10L}
		ExportModel.TestCaseModel scriptedTestCaseModel = testCases.find{it.id == -11L}


		then :
		testCases.size() == 2
		standardTestCaseModel.getTestCaseKind() == STANDARD

		scriptedTestCaseModel.getTestCaseKind() == org.squashtest.tm.domain.testcase.TestCaseKind.GHERKIN
		scriptedTestCaseModel.getTcScript() == "Feature: three cucumbers and two tomatoes"
	}


	/*
	 * See dataset description in the dataset file
	 */
	@DataSet("export-req-links.xml")
	def "should collect the requirement links model"(){

		given :
			// setup : must fix the reciprocal references from requirement to requirement version
			// (it had to be stripped from the dataset because it wouldn't be inserted otherwise)
			// the following maps Requirement ids to RequirementVersion ids
			[
				(-255) : -255,
				(-256) : -256,
				(-257) : -259,
				(-258) : -260,
				(-259) : -261
			]
			.each { k,v -> executeSQL("update REQUIREMENT set current_version_id = $v where rln_id = $k") }


		and :
			def ids = -259l..-255l

		when :
			List<RequirementLinkModel> linkModels = exporter.findRequirementLinksModel(ids)

		then :
			linkModels.size() == 4

			// first, test what should be contained
			def req1v1_req3v1_related = model([reqPath : "/Test Project-1/Test Folder 1/Test Requirement 1", reqVersion : 1, relReqPath : "/Test Project-1/Test Folder 1/Test Requirement 3", relReqVersion : 1, relatedReqRole : "RELATED"])
			def req1v1_req3v3_duplicate = model([reqPath : "/Test Project-1/Test Folder 1/Test Requirement 1", reqVersion : 1, relReqPath : "/Test Project-1/Test Folder 1/Test Requirement 3", relReqVersion : 3, relatedReqRole : "DUPLICATE"])
			def req2v1_req1v1_child = model([reqPath : "/Test Project-1/Test Folder 1/Test Requirement 2", reqVersion : 1, relReqPath : "/Test Project-1/Test Folder 1/Test Requirement 1", relReqVersion : 1, relatedReqRole : "CHILD"])
			def req3v3_otherv1_child = model([reqPath : "/Test Project-1/Test Folder 1/Test Requirement 3", reqVersion : 3, relReqPath : "/Test Project-1/related to ex3", relReqVersion : 1, relatedReqRole : "CHILD"])

			linkModels.find { match(it, req1v1_req3v1_related) } != null
			linkModels.find { match(it, req1v1_req3v3_duplicate) } != null
			linkModels.find { match(it, req2v1_req1v1_child) } != null
			linkModels.find { match(it, req3v3_otherv1_child) } != null

			// now those that should not be found
			def unrelated = model([reqPath : "/Test Project-1/unrelated", reqVersion : 1, relReqPath : "/Test Project-1/related to ex3", relReqVersion : 1, relatedReqRole : "DUPLICATE"])

			linkModels.find { match(it, unrelated) } == null

			true

	}

	@DataSet("ExportDaoIT.should create models from library and folders.xml")
	def "should create models from folders"(){
		given :
		def testCaseIds = [-10L, -11L, -12L, -13L]

		when:
		def res = exporter.loadTestCaseModelsFromFolderWithJOOQ(testCaseIds)

		then:
		res != null
		res.size() == 3
		res.collect{ it.getId()}.containsAll([-10L, -11L, -12L])
		res.sort{it.getId()}
		def tcModel = res.get(2) //-10L

			tcModel.getUuid() != null
			tcModel.getLastModifiedBy() == "admin"
			DateUtils.formatIso8601Date(tcModel.getLastModifiedOn()) =="2020-02-24"
			tcModel.getCreatedBy() == "dbuni"
			DateUtils.formatIso8601Date(tcModel.getCreatedOn()) == "2012-11-13"

			tcModel.getReference() == "ref"
			tcModel.getWeight() == TestCaseImportance.HIGH
			tcModel.getWeightAuto() == 1
			tcModel.getAutomatable() == TestCaseAutomatable.Y
			tcModel.getPrerequisite() == "batman"
			tcModel.getStatus() == TestCaseStatus.TO_BE_UPDATED
			tcModel.getProjectId() == -1L
			tcModel.getProjectName() == "Project_1"
			tcModel.getMilestone() == "Milestone_1|Milestone_2|Milestone_3|Milestone_4"
			def tcNature = tcModel.getNature()
			tcNature.getLabel() == "nature"
			tcNature.getCode() == "007"
			def tcType = tcModel.getType()
			tcType.getLabel() == "type"
			tcType.getCode() == "008"
			tcModel.getName() == "test-case10"
			tcModel.getDescription() == "this is a test case"
			tcModel.getOrder() == 1
			tcModel.getNbAttachments() == 1
			tcModel.getNbReq() == 2
			tcModel.getNbIterations() == 1
			tcModel.getNbCaller() == 2
			tcModel.getTcScript() == null
			tcModel.getTestCaseKind() == TestCaseKind.STANDARD


		def tcModel2 = res.get(1) //-11L
			tcModel2.getUuid() != null
			tcModel2.getLastModifiedBy() == "admin"
			DateUtils.formatIso8601Date(tcModel2.getLastModifiedOn()) =="2020-02-25"
			tcModel2.getCreatedBy() == "dbuni"
			DateUtils.formatIso8601Date(tcModel2.getCreatedOn()) == "2012-11-13"

			tcModel2.getReference() == "ref"
			tcModel2.getWeight() == TestCaseImportance.MEDIUM
			tcModel2.getWeightAuto() == 0
			tcModel2.getAutomatable() == TestCaseAutomatable.Y
			tcModel2.getPrerequisite() == "robin"
			tcModel2.getStatus() == TestCaseStatus.APPROVED
			tcModel2.getProjectId() == -1L
			tcModel2.getProjectName() == "Project_1"
			tcModel2.getMilestone() == null
			def tcNature2 = tcModel2.getNature()
			tcNature2.getLabel() == "nature"
			tcNature2.getCode() == "007"
			def tcType2 = tcModel2.getType()
			tcType2.getLabel() == "type"
			tcType2.getCode() == "008"
			tcModel2.getName() == "scripted-test-case11"
			tcModel2.getDescription() == "this is a scripted test case"
			tcModel2.getOrder() == 2
			tcModel2.getNbAttachments() == 1
			tcModel2.getNbReq() == 0
			tcModel2.getNbIterations() == 0
			tcModel2.getNbCaller() == 0
			tcModel2.getTcScript() == "This is Gherkin script."
			tcModel2.getTestCaseKind() == TestCaseKind.GHERKIN


		def tcModel3 = res.get(0) //-12L
		tcModel3.getUuid() != null
		tcModel3.getLastModifiedBy() == "admin"
		DateUtils.formatIso8601Date(tcModel3.getLastModifiedOn()) =="2020-02-26"
		tcModel3.getCreatedBy() == "dbuni"
		DateUtils.formatIso8601Date(tcModel3.getCreatedOn()) == "2012-11-13"

		tcModel3.getReference() == "ref"
		tcModel3.getWeight() == TestCaseImportance.VERY_HIGH
		tcModel3.getWeightAuto() == 1
		tcModel3.getAutomatable() == TestCaseAutomatable.Y
		tcModel3.getPrerequisite() == "joker"
		tcModel3.getStatus() == TestCaseStatus.WORK_IN_PROGRESS
		tcModel3.getProjectId() == -1L
		tcModel3.getProjectName() == "Project_1"
		tcModel3.getMilestone() == null
		def tcNature3 = tcModel3.getNature()
		tcNature3.getLabel() == "nature"
		tcNature3.getCode() == "007"
		def tcType3 = tcModel2.getType()
		tcType3.getLabel() == "type"
		tcType3.getCode() == "008"
		tcModel3.getName() == "keyword-test-case12"
		tcModel3.getDescription() == "this is a keyword test case"
		tcModel3.getOrder() == 1
		tcModel3.getNbAttachments() == 1
		tcModel3.getNbReq() == 0
		tcModel3.getNbIterations() == 0
		tcModel3.getNbCaller() == 0
		tcModel3.getTcScript() == null
		tcModel3.getTestCaseKind() == TestCaseKind.KEYWORD
	}

	@DataSet("ExportDaoIT.should create models from library and folders.xml")
	def "should create models from library"(){
		given :
		def testCaseIds = [-10L, -11L, -12L, -13L]

		when:
		def res = exporter.loadTestCaseModelsFromLibraryWithJOOQ(testCaseIds)

		then:
		res != null
		res.size() == 1
		res.collect{ it.getId()}.contains(-13L)
		def tcModel = res.get(0) //-13L

		tcModel.getUuid() != null
		tcModel.getLastModifiedBy() == "admin"
		DateUtils.formatIso8601Date(tcModel.getLastModifiedOn()) =="2020-02-26"
		tcModel.getCreatedBy() == "dbuni"
		DateUtils.formatIso8601Date(tcModel.getCreatedOn()) == "2012-11-13"

		tcModel.getReference() == "ref"
		tcModel.getWeight() == TestCaseImportance.LOW
		tcModel.getWeightAuto() == 0
		tcModel.getAutomatable() == TestCaseAutomatable.Y
		tcModel.getPrerequisite() == "superman"
		tcModel.getStatus() == TestCaseStatus.OBSOLETE
		tcModel.getProjectId() == -1L
		tcModel.getProjectName() == "Project_1"
		tcModel.getMilestone() == null
		def tcNature = tcModel.getNature()
		tcNature.getLabel() == "nature"
		tcNature.getCode() == "007"
		def tcType = tcModel.getType()
		tcType.getLabel() == "type"
		tcType.getCode() == "008"
		tcModel.getName() == "root-test-case13"
		tcModel.getDescription() == "this is a test case at root"
		tcModel.getOrder() == 2
		tcModel.getNbAttachments() == 1
		tcModel.getNbReq() == 0
		tcModel.getNbIterations() == 0
		tcModel.getNbCaller() == 0
		tcModel.getTcScript() == null
		tcModel.getTestCaseKind() == TestCaseKind.KEYWORD
	}

	private boolean match(RequirementLinkModel model1, RequirementLinkModel model2){
		return model1.reqPath.equals(model2.reqPath) &&
				(model1.reqVersion == model2.reqVersion) &&
				model1.relReqPath.equals(model2.relReqPath) &&
				(model1.relReqVersion == model2.relReqVersion) &&
				model1.relatedReqRole.equals(model2.relatedReqRole)
	}

	private RequirementLinkModel model(Map ppts){
		return RequirementLinkModel.create(ppts["reqVersion"], ppts["relReqVersion"], ppts["relatedReqRole"], ppts["reqPath"], ppts["relReqPath"])
	}
}
