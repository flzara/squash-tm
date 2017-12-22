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
package org.squashtest.tm.service.internal.repository.hibernate

import java.util.List;

import javax.inject.Inject

import org.springframework.transaction.annotation.Transactional
import org.hibernate.Query
import org.squashtest.tm.core.dynamicmanager.annotation.QueryParam;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting
import org.squashtest.tm.core.foundation.collection.SortOrder
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.RequirementVersionCoverage;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.internal.repository.RequirementVersionCoverageDao
import org.squashtest.tm.tools.unittest.assertions.CollectionAssertions
import org.squashtest.tm.tools.unittest.assertions.ListAssertions
import org.squashtest.it.basespecs.DbunitDaoSpecification;
import org.unitils.dbunit.annotation.DataSet
import static org.squashtest.tm.core.foundation.collection.SortOrder.*;
import spock.lang.Unroll;
import spock.unitils.UnitilsSupport

@UnitilsSupport
class HibernateRequirementVersionCoverageDaoIT extends DbunitDaoSpecification {
	@Inject
	RequirementVersionCoverageDao dao

	def setup() {
		CollectionAssertions.declareContainsExactlyIds()
		ListAssertions.declareIdsEqual()
	}

	@DataSet("HibernateRequirementVersionDaoIT.should count requirements verified by list of test cases.xml")
	def "should find all requirements verified by list of test cases sorted by id"() {
		PagingAndSorting sorting = Mock()
		sorting.firstItemIndex >> 0
		sorting.pageSize >> 10
		sorting.sortedAttribute >> "RequirementVersion.id"
		sorting.sortOrder >> SortOrder.ASCENDING

		when:
		def reqs = dao.findDistinctRequirementVersionsByTestCases([-100L, -200L], sorting)

		then:
		reqs*.id.containsAll([-10L, -20L, -30L])
		reqs.size() == 3
	}
	@DataSet("HibernateRequirementVersionDaoIT.should count requirements verified by list of test cases.xml")
	def "should find all requirements verified by list of test cases sorted by desc name"() {
		PagingAndSorting sorting = Mock()
		sorting.firstItemIndex >> 0
		sorting.pageSize >> 10
		sorting.sortedAttribute >> "RequirementVersion.name"
		sorting.sortOrder >> SortOrder.DESCENDING

		when:
		def reqs = dao.findDistinctRequirementVersionsByTestCases([-100L, -200L], sorting)

		then:
		reqs*.name == ["vingt", "30", "10"]
	}

	@DataSet("HibernateRequirementVersionDaoIT.should count requirements verified by list of test cases.xml")
	def "should find paged list of requirements verified by list of test cases"() {
		PagingAndSorting sorting = Mock()
		sorting.firstItemIndex >> 1
		sorting.pageSize >> 1
		sorting.sortedAttribute >> "RequirementVersion.id"
		sorting.sortOrder >> SortOrder.ASCENDING

		when:
		def reqs = dao.findDistinctRequirementVersionsByTestCases([-100L, -200L], sorting)

		then:
		reqs*.id == [-20L]
	}

	@Unroll("should find paged list for test-step sorted by #sortAttr")
	@DataSet("HibernateRequirementVersionCoverageDaoIT.should find paged list for test-step.xml")
	def "should find #expected steps for page[#start, #pageSize] and sorting[#sortAttr, #sortOrder]"() {
		PagingAndSorting paging = Mock()
		paging.firstItemIndex >> start
		paging.pageSize >> pageSize
		paging.sortedAttribute >> sortAttr
		paging.sortOrder >> sortOrder

		when:
		List<RequirementVersionCoverage> list = dao.findAllByTestCaseId(-1L,  paging)

		then:
		list*.id == expected

		where:
		start | pageSize | sortAttr                           | sortOrder  | expected
		0     | 3        | "RequirementVersion.id"            | ASCENDING  | [-5L, -4L, -3L]
		0     | 3        | "RequirementVersion.name"          | ASCENDING  | [-5L, -2L, -4L]
		//		0     | 3        | "Project.name"                     | ASCENDING  | [-1L, -2L, -3L] // dont work anymore since h2 upgrade
		0     | 3        | "RequirementVersion.reference"     | ASCENDING  | [-3L, -5L, -4L]
		0     | 3        | "RequirementVersion.versionNumber" | ASCENDING  | [-2L, -1L, -3L]
		0     | 3        | "RequirementVersion.criticality"   | ASCENDING  | [-4L, -2L, -3L]
		0     | 3        | "RequirementCategory.code" 		  | ASCENDING  | [-1L, -5L, -3L]
		//		0     | 3        | "ActionTestStep.id"                | DESCENDING | [-1L, -3L, -2L]
		//		0     | 50       | "ActionTestStep.id"                | DESCENDING | [-1L, -3L, -2L, -4L, -5L]



	}

	@DataSet("HibernateRequirementVersionDaoIT.should count requirements verified by list of test cases.xml")
	def "should count distinct requirements verified by list of test cases"() {
		when:
		def count = dao.numberDistinctVerifiedByTestCases([-100L, -200L])

		then:
		count == 3
	}

	@DataSet("HibernateRequirementVersionDaoIT.should count requirements verified by list of test cases.xml")
	def "should count coverages verified by list of test cases"() {
		when:
		def count = dao.numberByTestCases([-100L, -200L])

		then:
		count == 4
	}



	@DataSet("HibernateTestCaseDaoIT.should find requirement versions directly verified by a test case sorted by name.xml")
	def "should find requirement versions directly verified by a test case sorted by name"() {
		given:
		PagingAndSorting pas = Mock()
		pas.firstItemIndex >> 0
		pas.pageSize >> 10
		pas.sortedAttribute >> "RequirementVersion.name"
		pas.sortOrder >> SortOrder.ASCENDING

		when:
		def res = dao.findAllByTestCaseId(-100, pas)

		then:
		res*.verifiedRequirementVersion*.id == [-20L, -10L]
	}

	@DataSet("HibernateTestCaseDaoIT.should find requirement versions directly verified by a test case sorted by name.xml")
	def "should count coverage verified by a test case"() {
		when:
		def count = dao.numberByTestCase(-100)

		then:
		count == 2
	}

	@DataSet("HibernateRequirementVersionCoverageDaoIT.should find by.xml")
	def"should find by version and test-case"(){
		given :
		def verifiedRequirementVersionId = -10L
		def verifyingTestCaseId = -100L
		when :
		def result = dao.byRequirementVersionAndTestCase( verifiedRequirementVersionId, verifyingTestCaseId);
		then :
		result*.id == [-1L]
	}

	@DataSet("HibernateRequirementVersionCoverageDaoIT.should find by.xml")
	def"should find by version and test-cases"(){
		given :
		def verifiedRequirementVersionId = -10L
		def verifyingTestCasesIds = [-100L,-101L, -200L]
		when :
		def result = dao.byRequirementVersionAndTestCases(verifyingTestCasesIds,verifiedRequirementVersionId);
		then:
		result*.id.containsAll([-1L, -4L])
		result.size() == 2
	}

	@DataSet("HibernateRequirementVersionCoverageDaoIT.should find by.xml")
	def"should find by versions and test-case"(){
		given :
		def verifiedRequirementVersionsIds =  [-10L, -20L]
		def verifyingTestCaseId = -100L
		when :
		def result = dao.byTestCaseAndRequirementVersions(verifiedRequirementVersionsIds, verifyingTestCaseId);
		then:
		result*.id.containsAll([-1L, -2L])
		result.size() == 2
	}

	@DataSet("HibernateRequirementVersionCoverageDaoIT.should find by.xml")
	def"should find by versions and test-step"(){
		given :
		def verifiedRequirementVersionsIds =  [-10L, -20L, -30L]
		def testStepId = -200L
		when :
		def result = dao.byRequirementVersionsAndTestStep(verifiedRequirementVersionsIds,testStepId);
		then:
		result*.id.containsAll([-2L, -3L])
		result.size() == 2
	}
}
