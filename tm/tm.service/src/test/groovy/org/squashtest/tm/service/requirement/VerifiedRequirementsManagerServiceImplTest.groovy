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
package org.squashtest.tm.service.requirement

import java.util.Optional
import org.squashtest.tm.tools.unittest.assertions.CollectionAssertions
import org.squashtest.tm.tools.unittest.reflection.ReflectionCategory
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder
import org.squashtest.tm.core.foundation.collection.PagingAndSorting
import org.squashtest.tm.domain.requirement.Requirement
import org.squashtest.tm.domain.requirement.RequirementLibrary
import org.squashtest.tm.domain.requirement.RequirementLibraryNode
import org.squashtest.tm.domain.requirement.RequirementStatus
import org.squashtest.tm.domain.requirement.RequirementVersion
import org.squashtest.tm.domain.resource.Resource
import org.squashtest.tm.domain.testcase.ActionTestStep
import org.squashtest.tm.domain.testcase.RequirementVersionCoverage
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode
import org.squashtest.tm.domain.testcase.TestStep
import org.squashtest.tm.service.advancedsearch.IndexationService
import org.squashtest.tm.service.internal.library.LibrarySelectionStrategy
import org.squashtest.tm.service.internal.project.ProjectFilterModificationServiceImpl
import org.squashtest.tm.service.internal.repository.LibraryNodeDao
import org.squashtest.tm.service.internal.repository.RequirementLibraryDao
import org.squashtest.tm.service.internal.repository.RequirementVersionCoverageDao
import org.squashtest.tm.service.internal.repository.RequirementVersionDao
import org.squashtest.tm.service.internal.repository.TestCaseDao
import org.squashtest.tm.service.internal.repository.TestStepDao
import org.squashtest.tm.service.internal.requirement.VerifiedRequirementsManagerServiceImpl
import org.squashtest.tm.service.internal.testcase.TestCaseCallTreeFinder
import org.squashtest.tm.service.internal.testcase.TestCaseImportanceManagerServiceImpl
import org.squashtest.tm.service.milestone.ActiveMilestoneHolder
import org.squashtest.tm.service.security.PermissionEvaluationService

import spock.lang.Specification

class VerifiedRequirementsManagerServiceImplTest extends Specification {
	VerifiedRequirementsManagerServiceImpl service = new VerifiedRequirementsManagerServiceImpl()
	TestCaseDao testCaseDao = Mock()
	TestStepDao testStepDao = Mock()
	RequirementLibraryDao requirementLibraryDao = Mock()
	RequirementVersionDao requirementVersionDao = Mock()
	LibraryNodeDao<RequirementLibraryNode> nodeDao = Mock()
	TestCaseCallTreeFinder callTreeFinder = Mock()
	RequirementVersionCoverageDao requirementVersionCoverageDao = Mock()
	TestCaseImportanceManagerServiceImpl testCaseImportanceManagerService = Mock()
	ProjectFilterModificationServiceImpl projectFilterModificationService = Mock()
	LibrarySelectionStrategy<RequirementLibrary, RequirementLibraryNode> libraryStrategy = Mock()
	PermissionEvaluationService permissionService = Mock()
	IndexationService indexationService = Mock()
	ActiveMilestoneHolder activeMilestoneHolder = Mock()

	def setup() {
		CollectionAssertions.declareContainsExactly()

		service.testCaseDao = testCaseDao
		service.testStepDao = testStepDao
		service.requirementVersionDao = requirementVersionDao
		service.requirementLibraryNodeDao = nodeDao
		service.testCaseImportanceManagerService = testCaseImportanceManagerService
		service.requirementVersionCoverageDao = requirementVersionCoverageDao
		service.callTreeFinder = callTreeFinder
		service.permissionService = permissionService
		service.indexationService = indexationService
		service.activeMilestoneHolder = activeMilestoneHolder
		activeMilestoneHolder.getActiveMilestone() >> Optional.empty()
		permissionService.hasRoleOrPermissionOnObject(_, _, _) >> true
	}


	def "should add requirements to test case's verified requirements"() {
		given:
		TestCase testCase = new TestCase()
		testCaseDao.findById(10) >> testCase

		and:
		RequirementVersion rv5 = new RequirementVersion()
		RequirementVersion rv15 = new RequirementVersion()


		use (ReflectionCategory) {
			Resource.set field: "id", of: rv5, to: 5L
			Resource.set field: "id", of: rv15, to: 15L
		}

		requirementVersionDao.findAll([5, 15]) >> [rv5, rv15]

		and:
		Requirement req5 = new Requirement(rv5)
		Requirement req15 = new Requirement(rv15)
		nodeDao.findAllByIds([5, 15]) >> [req5, req15]

		when:
		service.addVerifiedRequirementsToTestCase([5, 15], 10)

		then:
		testCase.verifiedRequirementVersions.containsExactly([rv5, rv15])
	}

	def "should add requirements to test step's verified requirements"() {
		given:"a testCase having a testStep of id=10L"
		ActionTestStep testStep = new ActionTestStep()
		TestCase testCase = new TestCase()
		use (ReflectionCategory){
			TestStep.set field:"id", of:testStep, to:10L
			TestCaseLibraryNode.set field:"id", of:testCase, to:16L
		}
		testStepDao.findActionTestStepById(10L)>>testStep
		testCase.addStep(testStep)

		and:"2 requirement versions of id = 5L and 15L"
		RequirementVersion rv5 = new RequirementVersion()
		RequirementVersion rv15 = new RequirementVersion()


		use (ReflectionCategory) {
			Resource.set field: "id", of: rv5, to: 5L
			Resource.set field: "id", of: rv15, to: 15L
		}

		requirementVersionDao.findAll([5, 15]) >> [rv5, rv15]

		and:"2 requirements having same ids as their current versions (5L and 15L)"
		Requirement req5 = new Requirement(rv5)
		Requirement req15 = new Requirement(rv15)
		nodeDao.findAllByIds([5, 15]) >> [req5, req15]

		and:"the test case covers the requirement version 5L"
		RequirementVersionCoverage coverage = new RequirementVersionCoverage(rv5, testCase)
		requirementVersionCoverageDao.byRequirementVersionAndTestCase(5, 16L)>>coverage
		requirementVersionCoverageDao.byRequirementVersionAndTestCase(15, 16L)>>null

		when:
		service.addVerifiedRequirementsToTestStep([5, 15], 10)

		then:
		testStep.verifiedRequirementVersions.containsExactly([rv5, rv15])
		1*requirementVersionCoverageDao.persist(_);
	}

	def "should not add requirements with no verifiable version to test case's verified requirements"() {
		given:
		TestCase testCase = new TestCase()
		testCaseDao.findById(10) >> testCase

		and:
		RequirementVersion rv5 = new RequirementVersion()
		RequirementVersion rv15 = new RequirementVersion()


		use (ReflectionCategory) {
			Resource.set field: "id", of: rv5, to: 5L
			RequirementVersion.set field: "status", of: rv5, to: RequirementStatus.OBSOLETE
			Resource.set field: "id", of: rv15, to: 15L
		}

		requirementVersionDao.findAll([5, 15]) >> [rv5, rv15]

		and:
		Requirement req5 = new Requirement(rv5)
		Requirement req15 = new Requirement(rv15)
		nodeDao.findAllByIds([5, 15]) >> [req5, req15]

		when:
		service.addVerifiedRequirementsToTestCase([5, 15], 10)

		then:
		testCase.verifiedRequirementVersions.containsExactly([rv15])
	}

	def "should remove requirements from test case's verified requirements"() {
		given: "some requirements"
		RequirementVersion req5 = new RequirementVersion()
		new Requirement(req5)
		RequirementVersion req15 = new RequirementVersion()
		new Requirement(req15)

		use (ReflectionCategory) {
			Resource.set field: "id", of: req5, to: 5L
			Resource.set field: "id", of: req15, to: 15L
		}
		requirementVersionDao.findAll([15L])>> [req15]
		and: "a test case which verifies these requirements"
		TestCase testCase = new TestCase()
		RequirementVersionCoverage rvc5 = new RequirementVersionCoverage(req5, testCase)
		RequirementVersionCoverage rvc15 = new RequirementVersionCoverage(req15, testCase)
		testCaseDao.findById(10L) >> testCase
		requirementVersionCoverageDao.byTestCaseAndRequirementVersions([15L], 10L)>>[rvc15]
		when:
		service.removeVerifiedRequirementVersionsFromTestCase([15L], 10L)

		then:
		1*requirementVersionCoverageDao.delete(_)
	}

	def "should remove single requirement from test case's verified requirements"() {
		given: "a requirement"
		RequirementVersion req = new RequirementVersion()
		use (ReflectionCategory) {
			Resource.set field: "id", of: req, to: 5L
		}
		requirementVersionDao.findOne(5) >> req


		and: " a test case which verifies this requirements"
		TestCase testCase = new TestCase()
		RequirementVersionCoverage rvc5 =  new RequirementVersionCoverage(req, testCase)
		testCaseDao.findById(10L) >> testCase

		requirementVersionCoverageDao.byRequirementVersionAndTestCase(5L, 10L)>> rvc5

		when:
		service.removeVerifiedRequirementVersionFromTestCase(5L, 10L)

		then:
		1*requirementVersionCoverageDao.delete(_)
	}

	def "should return the first 2 verified requirements"() {
		given:
		PagingAndSorting filter = Mock()
		filter.getFirstItemIndex() >> 0
		filter.getPageSize() >> 2

		and :
		RequirementVersionCoverage rvc1 = Mock(RequirementVersionCoverage)
		RequirementVersionCoverage rvc2 = Mock(RequirementVersionCoverage)
		TestCase tc = Mock(TestCase)
		rvc1.getVerifyingTestCase() >> tc
		rvc2.getVerifyingTestCase() >> tc
		rvc1.getVerifyingSteps() >> []
		rvc2.getVerifyingSteps() >> []

		and :

		RequirementVersion rc1 = Mock(RequirementVersion)
		RequirementVersion rc2 = Mock(RequirementVersion)

		rvc1.getVerifiedRequirementVersion() >> rc1
		rvc2.getVerifiedRequirementVersion() >> rc2

		rc1.getRequirementVersionCoverageOrNullFor(tc) >> rvc1
		rc2.getRequirementVersionCoverageOrNullFor(tc) >> rvc2

		and:
		requirementVersionCoverageDao.findAllByTestCaseId(10, filter) >> [rvc1, rvc2]

		when:
		def res = service.findAllDirectlyVerifiedRequirementsByTestCaseId(10, filter)

		then:
		res.pagedItems.size() == 2
	}

	def "should tell that unfiltered result size is 5"() {
		given:
		PagingAndSorting filter = Mock()
		filter.getFirstItemIndex() >> 0
		filter.getPageSize() >> 2

		and :

		RequirementVersionCoverage rvc1 = Mock(RequirementVersionCoverage)
		RequirementVersionCoverage rvc2 = Mock(RequirementVersionCoverage)
		TestCase tc = Mock(TestCase)
		rvc1.getVerifyingTestCase() >> tc
		rvc2.getVerifyingTestCase() >> tc
		rvc1.getVerifyingSteps() >> []
		rvc2.getVerifyingSteps() >> []

		and :

		RequirementVersion rc1 = Mock(RequirementVersion)
		RequirementVersion rc2 = Mock(RequirementVersion)

		rvc1.getVerifiedRequirementVersion() >> rc1
		rvc2.getVerifiedRequirementVersion() >> rc2

		rc1.getRequirementVersionCoverageOrNullFor(tc) >> rvc1
		rc2.getRequirementVersionCoverageOrNullFor(tc) >> rvc2


		requirementVersionCoverageDao.findAllByTestCaseId(10, filter)>> [rvc1, rvc2]
		and:
		requirementVersionCoverageDao.numberByTestCase(10) >> 5

		when:
		PagedCollectionHolder res = service.findAllDirectlyVerifiedRequirementsByTestCaseId(10, filter)

		then:
		res.totalNumberOfItems == 5
	}

	def "should find directly when search all verified requiremnts for test case"() {
		given: "sorting directives"
		PagingAndSorting sorting = Mock()

		and: "the looked up test case with 1 verified requirement"
		TestCase testCase = Mock()
		testCaseDao.findById(10L) >> testCase

		RequirementVersion directlyVerified = Mock()
		directlyVerified.id >> 100L

		RequirementVersionCoverage coverage = Mock()
		coverage.verifiedRequirementVersion >> directlyVerified


		testCase.verifies(directlyVerified)>> true

		testCase.getRequirementVersionCoverages() >> [coverage]


		and:
		requirementVersionCoverageDao.findDistinctRequirementVersionsByTestCases({ [10L].containsAll(it) }, _) >> [directlyVerified]


		and : "the looked up test case calls no test case"
		callTreeFinder.getTestCaseCallTree(_) >> []


		when:
		PagedCollectionHolder verifieds = service.findAllVerifiedRequirementsByTestCaseId(10L, sorting)

		then:
		verifieds.pagedItems.collect {it.id} == [100L]
		verifieds.pagedItems.collect { it.directVerification } == [true]
	}

	def "should find directly verified requiremnts for test step"() {
		given: "sorting directives"
		PagingAndSorting sorting = Mock()

		and: "the looked up test case with 1 verified requirement"
		TestStep testStep = Mock()
		testStepDao.findById(11L)>> testStep
		TestCase testCase = Mock()
		testCase.id >> 10L
		testStep.getTestCase()>> testCase
		testCaseDao.findById(10L) >> testCase

		RequirementVersion directlyVerified = Mock()
		directlyVerified.id >> 100L

		RequirementVersionCoverage coverage = Mock()
		coverage.verifiedRequirementVersion >> directlyVerified


		and:
		requirementVersionCoverageDao.findAllByTestCaseId(10L, sorting)>>[coverage];



		when:
		PagedCollectionHolder verifieds = service.findAllDirectlyVerifiedRequirementsByTestStepId(11L, sorting)

		then:
		verifieds.pagedItems.collect {it.id} == [100L]
		verifieds.pagedItems.collect { it.directVerification } == [true]
	}

	def "should find directly verified requiremnts for test case"() {
		given: "sorting directives"
		PagingAndSorting sorting = Mock()

		and: "the looked up test case with 1 verified requirement"
		TestCase testCase = Mock()
		testCase.id >> 10L
		testCaseDao.findById(10L) >> testCase

		RequirementVersion directlyVerified = Mock()
		directlyVerified.id >> 100L

		RequirementVersionCoverage coverage = Mock()
		coverage.verifiedRequirementVersion >> directlyVerified


		and:
		requirementVersionCoverageDao.findAllByTestCaseId(10L, sorting)>>[coverage];



		when:
		PagedCollectionHolder verifieds = service.findAllDirectlyVerifiedRequirementsByTestCaseId(10L, sorting)

		then:
		verifieds.pagedItems.collect {it.id} == [100L]
		verifieds.pagedItems.collect { it.directVerification } == [true]
	}

	def "should find 1st level indirectly verified requiremnts in verified list"() {
		given: "sorting directives"
		PagingAndSorting sorting = Mock()

		and: "the looked up test case with no verified requirement"
		TestCase testCase = Mock()
		testCaseDao.findById(10L) >> testCase

		testCase.getRequirementVersionCoverages() >> []


		and : "the looked up test case calls a test case"
		long callee = 20L
		callTreeFinder.getTestCaseCallTree(_) >> [callee]


		and: "the callee verifies a requiremnt"
		RequirementVersionCoverage rvc = Mock()
		RequirementVersion verified = Mock()
		rvc.verifiedRequirementVersion >> verified
		verified.id >> 100L
		requirementVersionCoverageDao.findDistinctRequirementVersionsByTestCases({[10L, 20L].containsAll(it) }, _) >> [verified]



		when:
		PagedCollectionHolder verifieds = service.findAllVerifiedRequirementsByTestCaseId(10, sorting)

		then:
		verifieds.pagedItems.collect{it.id} == [100L]
		verifieds.pagedItems.collect { it.directVerification } == [false]
	}



	def "should find 2nd level indirectly verified requiremnts in verified list"() {
		given: "sorting directives"
		PagingAndSorting sorting = Mock()

		and: "the looked up test case with no verified requirement"
		TestCase testCase = Mock()
		testCaseDao.findById(10L) >> testCase

		testCase.getRequirementVersionCoverages() >> []



		and : "the looked up test case calls a test case that calls a test case (L2)"
		long firstLevelCallee = 20L
		long secondLevelCallee = 30L
		callTreeFinder.getTestCaseCallTree(_) >> [
			firstLevelCallee,
			secondLevelCallee
		]



		and: "the L2 callee verifies a requiremnt"
		RequirementVersionCoverage rvc = Mock()
		RequirementVersion verified = Mock()
		rvc.verifiedRequirementVersion >> verified
		verified.id >> 100L
		requirementVersionCoverageDao.findDistinctRequirementVersionsByTestCases({[10L, 20L, 30L].containsAll(it) }, _) >> [verified]

		when:
		PagedCollectionHolder verifieds = service.findAllVerifiedRequirementsByTestCaseId(10, sorting)

		then:
		verifieds.pagedItems.collect{it.id}==[100L]
		verifieds.pagedItems.collect { it.directVerification } == [false]
	}



	def "should count verified requiremnts in verified list"() {
		given: "sorting directives"
		PagingAndSorting sorting = Mock()

		and: "the looked up test case"
		TestCase testCase = Mock()
		testCaseDao.findById(10L) >> testCase

		testCase.getRequirementVersionCoverages() >> []

		and: "the looked up test case calls no test case"
		callTreeFinder.getTestCaseCallTree(10L) >> []

		and:
		requirementVersionCoverageDao.findDistinctRequirementVersionsByTestCases({ [10L].containsAll(it) }, _) >> []

		and:
		requirementVersionCoverageDao.numberDistinctVerifiedByTestCases({ [10L].containsAll(it) }) >> 666
		when:
		PagedCollectionHolder verifieds = service.findAllVerifiedRequirementsByTestCaseId(10, sorting)

		then:
		verifieds.totalNumberOfItems == 666
	}
}
