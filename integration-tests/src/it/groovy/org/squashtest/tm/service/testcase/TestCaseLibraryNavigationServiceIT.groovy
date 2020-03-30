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
package org.squashtest.tm.service.testcase

import org.hibernate.Query
import org.hibernate.Session
import org.junit.runner.RunWith
import org.spockframework.runtime.Sputnik
import org.springframework.transaction.annotation.Transactional
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.squashtest.tm.domain.bdd.Keyword
import org.squashtest.tm.domain.customfield.BindableEntity
import org.squashtest.tm.domain.customfield.CustomFieldValue
import org.squashtest.tm.domain.testcase.Dataset
import org.squashtest.tm.domain.testcase.KeywordTestCase
import org.squashtest.tm.domain.testcase.Parameter
import org.squashtest.tm.domain.testcase.ScriptedTestCase
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.testcase.TestCaseFolder
import org.squashtest.tm.domain.testcase.TestCaseLibrary
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode
import org.squashtest.tm.domain.testcase.TestStep
import org.squashtest.tm.exception.library.CannotMoveInHimselfException
import org.squashtest.tm.service.internal.repository.RequirementDao
import org.squashtest.tm.service.internal.repository.RequirementVersionDao
import org.squashtest.tm.service.internal.repository.TestCaseFolderDao
import org.squashtest.tm.service.testcase.fromreq.ReqToTestCaseConfiguration
import org.unitils.dbunit.annotation.DataSet
import org.unitils.dbunit.annotation.ExpectedDataSet
import spock.lang.Ignore
import spock.lang.Unroll
import spock.unitils.UnitilsSupport

import javax.inject.Inject
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

@UnitilsSupport
@Transactional
@RunWith(Sputnik)
class TestCaseLibraryNavigationServiceIT extends DbunitServiceSpecification {

	@PersistenceContext
	private EntityManager em

	@Inject
	private RequirementDao requirementDao

	@Inject
	private RequirementVersionDao requirementVersionDao

    @Inject
    private TestCaseLibraryNavigationService navService

    @Inject
    private TestCaseFolderDao libraryDao

    @DataSet("TestCaseLibraryNavigationServiceIT.should copy paste folder with test-cases.xml")
    def "should copy paste folder with test-cases"() {
        given:
        Long[] sourceIds = [-1L]
        Long destinationId = -2L

        when:
        List<TestCaseLibraryNode> nodes = navService.copyNodesToFolder(destinationId, sourceIds)

        then: "test-case folder has 2 test-cases"
        nodes.get(0) instanceof TestCaseFolder
        TestCaseFolder folderCopy = (TestCaseFolder) nodes.get(0)
        folderCopy.content.size() == 3
        folderCopy.content.find { it.name == "test-case10" } != null
        folderCopy.content.find { it.name == "test-case11" } != null
		folderCopy.content.find { it.name == "keyword-test-case12" } != null
    }

    @DataSet("TestCaseLibraryNavigationServiceIT.should move to same project at right position.xml")
    def "should move folder with test-cases to the right position - first"() {
        given:
        Long[] sourceIds = [-1L]
        Long destinationId = -2L

        when:
        navService.moveNodesToFolder(destinationId, sourceIds, 0)

        then:
        TestCaseFolder parentFolder = (TestCaseFolder) libraryDao.findById(-2L)
        parentFolder.content*.id.containsAll([-1L, -20L, -21L])
    }

    @DataSet("TestCaseLibraryNavigationServiceIT.should move to same project at right position.xml")
    def "should move folder with test-cases to the right position - middle"() {
        given:
        Long[] sourceIds = [-1L]
        Long destinationId = -2L

        when:
        navService.moveNodesToFolder(destinationId, sourceIds, 1)

        then:
        TestCaseFolder parentFolder = (TestCaseFolder) libraryDao.findById(-2L)
        parentFolder.content*.id.containsAll([-20L, -1L, -21L])
    }

    @DataSet("TestCaseLibraryNavigationServiceIT.countsiblings.xml")
    @Unroll("should know that node #id has #num siblings in its parent #container")
    def "should return how many siblings the given element has (including itself)"() {

        expect:
        navService.countSiblingsOfNode(id) == num

        where:
        id   | num | container
        -1L  | 2   | "library"
        -2L  | 2   | "library"
        -10L | 1   | "folder"
        -20L | 3   | "folder"
        -21L | 3   | "folder"
        -22L | 3   | "folder"


    }

    @DataSet("TestCaseLibraryNavigationServiceIT.should move to same project at right position.xml")
    def "should move folder with test-cases to the right position - last"() {
        given:
        Long[] sourceIds = [-1L]
        Long destinationId = -2L

        when:
        navService.moveNodesToFolder(destinationId, sourceIds, 2)

        then:
        TestCaseFolder parentFolder = (TestCaseFolder) libraryDao.findById(-2L)
        parentFolder.content*.id.containsAll([-20L, -21L, -1L])
    }


    @DataSet("TestCaseLibraryNavigationServiceIT.should copy paste tc with parameters and datasets.xml")
    def "should copy paste tc with parameters and datasets"() {
        given: "a test case with parameters and dataset"
        Long[] sourceIds = [-11L]
        Long destinationId = -2L

        when: "this test case is copied into another folder"
        List<TestCaseLibraryNode> nodes = navService.copyNodesToFolder(destinationId, sourceIds)

        then: "the test case is copied"
        nodes.get(0) instanceof TestCase
        TestCase testCaseCopy = (TestCase) nodes.get(0)
        and: "it has copies of parameters"
        testCaseCopy.parameters.size() == 2
        Parameter param1copy = testCaseCopy.parameters.find { it.name == "parameter_1" }
        param1copy != null
        param1copy.id != -1L
        Parameter param2copy = testCaseCopy.parameters.find { it.name == "parameter_2" }
        param2copy != null
        param2copy.id != -2L
        and: "it has copies of datasets"
        testCaseCopy.datasets.size() == 2
        Dataset dataset1copy = testCaseCopy.datasets.find { it.name == "dataset_1" }
        dataset1copy != null
        dataset1copy.id != -1L
        Dataset dataset2copy = testCaseCopy.datasets.find { it.name == "dataset_2" }
        dataset2copy != null
        dataset2copy.id != -2L
        and: "it has copies of datasets-param-values"
        dataset1copy.parameterValues.size() == 2
        dataset1copy.parameterValues.collect { it.parameter }.containsAll([param1copy, param2copy])
        dataset1copy.parameterValues.collect { it.paramValue }.containsAll(["val", "val"])
        dataset2copy.parameterValues.size() == 2
        dataset2copy.parameterValues.collect { it.parameter }.containsAll([param1copy, param2copy])
        dataset2copy.parameterValues.collect { it.paramValue }.containsAll(["val", "val"])
    }

	@DataSet("TestCaseLibraryNavigationServiceIT.should copy paste tc with keyword test steps.xml")
	def "should copy paste tc with keyword test steps"() {
		given: "a test case with parameters and dataset"
		Long[] sourceIds = [-12L]
		Long destinationId = -2L

		when: "this test case is copied into another folder"
		List<TestCaseLibraryNode> nodes = navService.copyNodesToFolder(destinationId, sourceIds)

		then: "the test case is copied"
		nodes.get(0) instanceof TestCase
		TestCase testCaseCopy = (TestCase) nodes.get(0)
		and: "it has copies of steps"
		KeywordTestCase.class.isAssignableFrom(testCaseCopy.class)
		def testSteps = testCaseCopy.getSteps()
		testSteps.size() == 3
		and: "the first test step is Harry"
		def firstStep = testSteps.get(0)
		firstStep.getKeyword() == Keyword.GIVEN
		def firstStepActionWord = firstStep.getActionWord()
		firstStepActionWord.getId() == -1L
		firstStepActionWord.getWord() == "HARRY"
		and: "the second test step is Ron"
		def secondStep = testSteps.get(1)
		secondStep.getKeyword() == Keyword.AND
		def secondStepActionWord = secondStep.getActionWord()
		secondStepActionWord.getId() == -2L
		secondStepActionWord.getWord() == "RON"
		and: "the third test step is Hermione"
		def thirdStep = testSteps.get(2)
		thirdStep.getKeyword() == Keyword.THEN
		def thirdStepActionWord = thirdStep.getActionWord()
		thirdStepActionWord.getId() == -3L
		thirdStepActionWord.getWord() == "HERMIONE"
	}

	@DataSet("TestCaseLibraryNavigationServiceIT.should copy paste scripted tc with its script.xml")
	def "should copy paste scripted tc with its script in a folder"() {
		given: "a test case with parameters and dataset"
		Long[] sourceIds = [-12L]
		Long destinationId = -2L

		when: "this test case is copied into another folder"
		List<TestCaseLibraryNode> nodes = navService.copyNodesToFolder(destinationId, sourceIds)

		then: "the test case is copied"
		nodes.get(0) instanceof ScriptedTestCase
		ScriptedTestCase testCaseCopy = (ScriptedTestCase) nodes.get(0)
		and: "it has copies of parameters"
		def testSteps = testCaseCopy.getSteps()
		testSteps.size() == 0
		and: "the script has been copied"
		testCaseCopy.getScript() == "The script of the scripted test case."
	}

	@DataSet("TestCaseLibraryNavigationServiceIT.should copy paste scripted tc with its script.xml")
	def "should copy paste scripted tc with its script in a library"() {
		given: "a test case with parameters and dataset"
		Long[] sourceIds = [-12L]
		Long destinationId = -1L

		when: "this test case is copied into a library"
		List<TestCaseLibraryNode> nodes = navService.copyNodesToLibrary(destinationId, sourceIds)

		then: "the test case is copied"
		nodes.get(0) instanceof ScriptedTestCase
		ScriptedTestCase testCaseCopy = (ScriptedTestCase) nodes.get(0)
		and: "it has copies of parameters"
		def testSteps = testCaseCopy.getSteps()
		testSteps.size() == 0
		and: "the script has been copied"
		testCaseCopy.getScript() == "The script of the scripted test case."
	}

    @DataSet("TestCaseLibraryNavigationServiceIT.should copy tc with datasetParamValues of called step.xml")
    def "should copy tc with datasetParamValues of called step"() {
        given: "a test case with parameters and dataset"
        Long[] sourceIds = [-11L]
        Long destinationId = -2L

        when: "this test case is copied into another folder"
        List<TestCaseLibraryNode> nodes = navService.copyNodesToFolder(destinationId, sourceIds)

        then: "the test case is copied"
        nodes.get(0) instanceof TestCase
        TestCase testCaseCopy = (TestCase) nodes.get(0)
        and: "it has copies of datset and parameters"
        Dataset dataset1copy = testCaseCopy.datasets.find { it.name == "dataset_1" }
        testCaseCopy.parameters.size() == 1
        Parameter param1copy = testCaseCopy.parameters.find { it.name == "parameter_1" }
        and: "it has copies of datasetParamValues even for called param"
        Parameter calledParam = session.get(Parameter.class, -2L)
        dataset1copy.parameterValues.size() == 2
        dataset1copy.parameterValues.collect { it.parameter }.containsAll([param1copy, calledParam])

    }

    @Unroll
    @DataSet("TestCaseLibraryNavigationServiceID.should copy tc with steps and cufs.xml")
    def "should copy cufs of tc steps #copiedStepId"() {
        given: "a test case with steps and 4 cufs bound to the steps"
		Long[] sourceIds = [-11L]
		Long destinationId = -2L

        when: "this test case is copied into another folder"
        List<TestCaseLibraryNode> nodes = navService.copyNodesToFolder(destinationId, sourceIds)

        then: "the test case is copied"
        nodes.get(0) instanceof TestCase
        TestCase testCaseCopy = (TestCase) nodes.get(0)
        and: "the steps are copied"
        List<TestStep> copiedSteps = testCaseCopy.getSteps()
        copiedSteps.size() == 5
        and: "the step's cufs are copied"
        TestStep secondCopiedStep = copiedSteps.get(1)
        List<CustomFieldValue> cufValues = findCufValuesForEntity(BindableEntity.TEST_STEP, copiedStepId)
        cufValues.collect { it.value }.containsAll(copiedValues)

        where:
        copiedStepId | copiedValues
		-1 | [] // is a call step
		-2 | ["step2-value1", "step2-value2", "step2-value3", "step2-value4"]
		-3 | ["step3-value1", "step3-value2", "step3-value3", "step3-value4"]
		-4 | ["step4-value1", "step4-value2", "step4-value3", "step4-value4"]
		-5 | ["step5-value1", "step5-value2", "step5-value3", "step5-value4"]
    }

    @DataSet("TestCaseLibraryNavigationServiceIT.should copy to other project.xml")
    def "should copy paste to other project"() {
        given:
        Long[] sourceIds = [-1L]
        Long destinationId = -2L

        when:
        List<TestCaseLibraryNode> nodes = navService.copyNodesToFolder(destinationId, sourceIds)

        then: "scripts are kept or removed"
        nodes.get(0) instanceof TestCaseFolder
        TestCaseFolder folderCopy = (TestCaseFolder) nodes.get(0)
        folderCopy.content.size() == 2
        def tc10 = folderCopy.content.find({ it.name == "test-case10" })
        tc10.isAutomated()
        def tc11 = folderCopy.content.find { it.name == "test-case11" }
        !tc11.isAutomated()
        and: "cufs are updated to match destination project's config"
        def values10 = findCufValuesForEntity(BindableEntity.TEST_CASE, tc10.id)
        values10.find { it.getBinding().id == -2L }.value == "tc-10-cuf1"
        values10.find { it.getBinding().id == -4L }.value == "default"
        def values11 = findCufValuesForEntity(BindableEntity.TEST_CASE, tc11.id)
        values11.find { it.getBinding().id == -2L }.value == "tc-11-cuf1"
        values11.find { it.getBinding().id == -4L }.value == "default"
    }

	def findCufValuesForEntity(BindableEntity tctype, long tcId) {
		Query query = session.createQuery("from CustomFieldValue cv where cv.boundEntityType = :type and cv.boundEntityId = :id")
		query.setParameter("id", tcId)
		query.setParameter("type", tctype)
		return query.list()
	}

    @DataSet("TestCaseLibraryNavigationServiceIT.should move to same project.xml")
    @ExpectedDataSet("TestCaseLibraryNavigationServiceIT.should move to same project-result.xml")
    def "should move to same project"() {
        given:
        Long[] sourceIds = [-1L]
        Long destinationId = -2L

        when:
        navService.moveNodesToFolder(destinationId, sourceIds)

        then: "expected dataset is verified"
        session.flush()
    }

    @DataSet("TestCaseLibraryNavigationServiceIT.should not move in himself.xml")
    def "should not move in himself"() {
        given:
        Long[] sourceIds = [-1L]
        Long destinationId = -1L

        when:
        navService.moveNodesToFolder(destinationId, sourceIds)

        then:
        thrown(CannotMoveInHimselfException)
    }

    @Unroll("by attempting to create it, should find that the id of folder #path is #id ")
    @DataSet("TestCaseLibraryNavigationServiceIT.create by path.xml")
    def "should note create a folder because it already exists"() {

        expect:
        navService.mkdirs(path) == id

        where:
        path                                | id
        "/some project/super folder"        | -13L
        "/some project/another folder/"     | -2L
        "/some project/super folder/robert" | -1L
    }


    @DataSet("TestCaseLibraryNavigationServiceIT.create by path.xml")
    def "should create one folder at the root of a library"() {

        given:
        def path = "/some project/big"

        when:
        def id = navService.mkdirs(path)

        then:
        navService.getPathAsString(id) == path
        navService.findNodeIdByPath(path) == id

    }


    @DataSet("TestCaseLibraryNavigationServiceIT.create by path.xml")
    def "should create bunch of folders out of nowhere"() {

        given:
        def path = "/some project/big/normal/low"

        when:
        def id = navService.mkdirs(path)
        getSession().flush()

        then:
        navService.findNodeIdByPath("/some project/big") != null
        navService.findNodeIdByPath("/some project/big/normal") != null
        navService.findNodeIdByPath("/some project/big/normal/low") == id

    }

    @DataSet("TestCaseLibraryNavigationServiceIT.create by path.xml")
    def "should create bunch of folders in existing folders"() {

        given:
        def path = "/some project/another folder/bob/mike"

        when:
        def id = navService.mkdirs(path)
        getSession().flush()

        then:
        navService.findNodeIdByPath("/some project/another folder/bob") != null
        navService.findNodeIdByPath("/some project/another folder/bob/mike") == id

    }


    @DataSet("TestCaseLibraryNavigationServiceIT.should not move in himself.xml")
    def "should not move in his hierarchy"() {
        given:
        Long[] sourceIds = [-13L]
        Long destinationId = -1L

        when:
        navService.moveNodesToFolder(destinationId, sourceIds)

        then:
        thrown(CannotMoveInHimselfException)
    }

    @DataSet("TestCaseLibraryNavigationServiceIT.should move to other project.xml")
    @ExpectedDataSet("TestCaseLibraryNavigationServiceIT.should move to other project-result.xml")
    def "should move to another project"() {
        given:
        Long[] sourceIds = [-1L]
        Long destinationId = -2L

        when:
        navService.moveNodesToFolder(destinationId, sourceIds)

        then: "expected dataset is verified"
        session.flush()
    }

    @DataSet("TestCaseLibraryNavigationServiceIT.should move and update cufs.xml")
    @ExpectedDataSet("TestCaseLibraryNavigationServiceIT.should move and update cufs-result.xml")
    def "should move and update cufs"() {
        given:
        Long[] sourceIds = [-1L]
        Long destinationId = -2L

        when:
        navService.moveNodesToFolder(destinationId, sourceIds)

        then: "expected dataset is verified"
        session.flush()
    }

    @DataSet("TestCaseLibraryNavigationServiceIT.should move and remove or keep scripts.xml")
    def "should move and remove or keep scripts"() {
        given: "project with diffrent associations to automated projects"
        Long[] sourceIds = [-1L]
        Long destinationId = -2L

        when:
        navService.moveNodesToFolder(destinationId, sourceIds)

        then: "expected dataset is verified"
        TestCase keep = findEntity(TestCase.class, -10L)
        keep.isAutomated()
        TestCase remove = findEntity(TestCase.class, -11L)
        !remove.isAutomated()

    }

    @DataSet("TestCaseLibraryNavigationServiceIT.should move from folder to library.xml")
    @ExpectedDataSet("TestCaseLibraryNavigationServiceIT.should move from folder to library-result.xml")
    def "should move from folder to library"() {
        given: "dataset"
        Long[] sourceIds = [-1L]
        Long destinationId = -1L

        when:
        navService.moveNodesToLibrary(destinationId, sourceIds)

        then: "expected dataset is verified"
        session.flush()
        TestCaseFolder tcf = findEntity(TestCaseFolder.class, -13L)
        tcf.content.isEmpty()
        TestCaseLibrary tcl = findEntity(TestCaseLibrary.class, -1L)
        tcl.content.find({ it.id == -1L })
    }

    @DataSet("TestCaseLibraryNavigationServiceIT.test-case-gathering.xml")
    @Unroll("should retrieve a total of #expectedNumber test case ids for the given selection")
    def "should retrieve test case ids from a hierarchy"() {

        expect:
        navService.findTestCaseIdsFromSelection(libIds, nodeIds).size() == expectedNumber

        where:
        libIds       | nodeIds               | expectedNumber
        [-14L, -15L] | []                    | 12
        []           | [-252L, -258L, -237L] | 8
        []           | [-252L, -239L]        | 3
        [-15L]       | [-237L, -249L]        | 9

    }

	@Unroll
	@DataSet("TestCaseLibraryNavigationServiceIT.copy-testcase-from-requirement.xml")
	def "copyReqToTestCasesToFolder(long, Long[], Config) - Should copy some #tcKind test cases from requirements into a folder"() {
		given:
			[-2L, -3L, -4L].each {
				setBidirectionalReqReqVersion(it,it)
			}
		and:
			def config = new ReqToTestCaseConfiguration(tcKind)
		when:
			navService.copyReqToTestCasesToFolder(/*TCFolder*/-1L, /*Requirements*/sourceReqIds as Long[], config)
		then: "Check test cases"
			def testCaseList = em.createQuery("select tc from TestCase tc").getResultList()
			testCaseList.size() == reqNames.size()
			testCaseList.collect { it.name } containsAll(reqNames)
			testCaseList.each {
				expectedTcClass.isAssignableFrom(it.class)
				if(expectedTcClass == TestCase.class) {
					! ScriptedTestCase.class.isAssignableFrom(it.class)
					! KeywordTestCase.class.isAssignableFrom(it.class)
				}
			}
		where:
			sourceReqIds 	| reqNames 												| tcKind 		| expectedTcClass
			[-2L]			| ["Requirement_2"]										| "STANDARD" 	| TestCase.class
			[-2L, -3l, -4L]	| ["Requirement_2","Requirement_3","Requirement_4"]	| "STANDARD"	| TestCase.class
			[-2L]			| ["Requirement_2"]										| "GHERKIN" 	| ScriptedTestCase.class
			[-2L, -3l, -4L]	| ["Requirement_2","Requirement_3","Requirement_4"]	| "GHERKIN"		| ScriptedTestCase.class
			[-2L]			| ["Requirement_2"]										| "KEYWORD" 	| KeywordTestCase.class
			[-2L, -3l, -4L]	| ["Requirement_2","Requirement_3","Requirement_4"]	| "KEYWORD"		| KeywordTestCase.class
	}

	@Ignore("Bug in milestones fetching?")
	@Unroll
	@DataSet("TestCaseLibraryNavigationServiceIT.copy-testcase-from-requirement.xml")
	def "copyReqToTestCasesToFolder(long, Long[], Config) - Should copy a #tcKind test case folder from requirements into a folder"() {
		given:
			[-2L, -3L, -4L].each {
				setBidirectionalReqReqVersion(it,it)
			}
		and:
			def config = new ReqToTestCaseConfiguration(tcKind)
		when:
			navService.copyReqToTestCasesToFolder(/*TCFolder*/-1L, /*Requirements*/sourceReqIds as Long[], config)
		then: "Check test cases"
			def testCaseList = em.createQuery("select tc from TestCase tc").getResultList()
			testCaseList.size() == reqNames.size()
			testCaseList.collect { it.name }.containsAll(reqNames)
			testCaseList.each {
				expectedTcClass.isAssignableFrom(it.class)
				if(expectedTcClass == TestCase.class) {
					! ScriptedTestCase.class.isAssignableFrom(it.class)
					! KeywordTestCase.class.isAssignableFrom(it.class)
				}
			}
		and: "Check test case folders"
			def testCaseFolderList = em.createQuery("select folder from TestCaseFolder folder").getResultList()
			testCaseFolderList.size() == 2
			testCaseFolderList.collect { it.name }.contains("Tc_Folder_1")
		where:
			sourceReqIds 	| reqNames 												| tcKind 		| expectedTcClass
			[-1L] 			| ["Requirement_2","Requirement_3","Requirement_4"] 	| "STANDARD" 	| TestCase.class
			/*
			* Copy the Folder into the Folder
			* Copy the empty Folder into the Folder
			* */
	}

	def setBidirectionalReqReqVersion(Long reqVersionId, Long reqId) {
		def reqVer = requirementVersionDao.getOne(reqVersionId)
		def req = requirementDao.findById(reqId)
		reqVer.setRequirement(req)
	}
}
