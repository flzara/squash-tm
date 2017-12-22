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
package org.squashtest.tm.service.internal.batchimport

import org.springframework.security.access.AccessDeniedException
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.domain.requirement.Requirement
import org.squashtest.tm.domain.requirement.RequirementLibrary
import org.squashtest.tm.domain.requirement.RequirementStatus
import org.squashtest.tm.domain.requirement.RequirementVersion
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.testcase.TestCaseLibrary
import org.squashtest.tm.domain.users.User
import org.squashtest.tm.service.importer.Target
import org.squashtest.tm.service.internal.batchimport.testcase.excel.CoverageTarget
import org.squashtest.tm.service.internal.repository.ProjectDao
import org.squashtest.tm.service.internal.repository.UserDao
import org.squashtest.tm.service.requirement.RequirementLibraryNavigationService
import org.squashtest.tm.service.security.PermissionEvaluationService
import org.squashtest.tm.service.security.UserContextService
import org.squashtest.tm.service.user.UserAccountService
import spock.lang.Issue
import spock.lang.Specification
import spock.lang.Unroll

import static org.squashtest.tm.service.importer.ImportStatus.FAILURE

/**
 * @author Gregory Fouquet
 *
 */
class ValidationFacilityTest extends Specification {
	ValidationFacility facility = new ValidationFacility()
	EntityValidator entityValidator = Mock()
	Model model = Mock()
	UserAccountService userAccount = Mock()
	UserContextService userContextService = Mock();
	PermissionEvaluationService permissionService = Mock()
	UserDao userDao = Mock()
	TargetStatus status = Mock()

	RequirementLibraryNavigationService requirementFinder = Mock()
	ProjectDao projectDao = Mock()

	def setup() {
		facility.model = model
		facility.userAccountService = userAccount
		facility.permissionService = permissionService
		facility.userDao = userDao
		facility.entityValidator = entityValidator

		facility.reqLibNavigationService = requirementFinder
		facility.reqFinderService = requirementFinder

		facility.projectDao = projectDao
		facility.userContextService = userContextService

		model.getTestCaseCufs(_) >> Collections.emptyList()
		model.getProjectStatus(_) >> Mock(ProjectTargetStatus)

		userAccount.findCurrentUser() >> Mock(User)
		userContextService.getUsername() >> ""
	}

	def mockAnyStatus() {
		model.getStatus(_) >> status
	}

	@Unroll
	def "should validate new test case with inconsistent path #path and name #name"() {
		given:
		LogTrain logTrain = new LogTrain()
		entityValidator.createTestCaseChecks(_, _) >> logTrain

		and:
		TestCaseTarget target = Mock()
		target.path >> path

		and:
		TestCase testCase = Mock()
		testCase.name >> name

		and:
		TestCaseInstruction instr = new TestCaseInstruction(target, testCase);

		and:
		mockAnyStatus()
		status.status >> Existence.NOT_EXISTS

		when:
		LogTrain createLog = facility.createTestCase(instr)

		then:
		!createLog.criticalErrors

		where:
		path					| name
		"/the/path/is/straight"	| "deviant"
		"/the/path/is/straight"	| ""
	}

	def "should not validate old test case with inconsistent path and name"() {
		given:
		LogTrain logTrain = new LogTrain()
		entityValidator.updateTestCaseChecks(_, _) >> logTrain

		and:
		TestCaseTarget target = Mock()
		target.path >> "/the/path/is/straight"

		and:
		TestCase testCase = Mock()
		testCase.name >> "deviant"

		and:
		TestCaseInstruction instr = new TestCaseInstruction(target, testCase);

		and:
		mockAnyStatus()
		status.status >> Existence.EXISTS

		when:
		LogTrain createLog = facility.updateTestCase(instr)

		then:
		createLog.criticalErrors
	}

	@Unroll
	def "should validate old test case with without name '#name'"() {
		given:
		LogTrain logTrain = new LogTrain()
		entityValidator.updateTestCaseChecks(_, _) >> logTrain

		and:
		TestCaseTarget target = Mock()
		target.path >> path

		and:
		TestCase testCase = Mock()
		testCase.name >> name

		and:
		TestCaseInstruction instr = new TestCaseInstruction(target, testCase);

		and:
		mockAnyStatus()
		status.status >> Existence.EXISTS

		when:
		LogTrain createLog = facility.updateTestCase(instr)

		then:
		createLog.criticalErrors == fails

		where:
		path					| name		| fails
		"/the/path/is/straight"	| ""		| false
		"/the/path/is/straight"	| null		| false
	}

	def "should validate reqver for coverage"() {
		given:
		CoverageTarget target = wellFormedCoverageTarget()

		and:
		LogTrain train = Mock()

		and:
		Requirement requirement = requirementWithOneVersion()

		requirementFinder.findNodeIdByPath(_) >> 10L
		requirementFinder.findRequirement(10L) >> requirement

		permissionService.hasRoleOrPermissionOnObject(_, _, _, _) >> true

		and:
		status.status >> Existence.EXISTS
		// if you dont understand, think in javascript vvvvvvvvvvvvvvvv
		model.getStatus({ it instanceof RequirementTarget ? (it.id = 10L) || true : false }) >> status
		model.getStatus(_) >> status

		expect:
		facility.checkRequirementVersionForCoverage(target, train)
}

	@Issue("#6257")
	def "should not validate unreadble reqver for coverage"() {
		given:
		CoverageTarget target = wellFormedCoverageTarget()

		and:
		LogTrain train = Mock()

		and:
		Requirement requirement = requirementWithOneVersion()

		requirementFinder.findNodeIdByPath(_) >> 10L
		// this simulates no READ acl on requirement
		requirementFinder.findRequirement(10L) >> { throw new AccessDeniedException("This is thrown in #6257") }

		when:
		def res = facility.checkRequirementVersionForCoverage(target, train)

		then:
		res == null
		notThrown(AccessDeniedException) // in #6257 we get this exception
	}

	def wellFormedCoverageTarget() {
		CoverageTarget target = Mock()
		target.reqPathWellFormed >> true
		target.reqVersion >> 1L

		return new CoverageTarget(reqPath: "/ouate/de/phoque", tcPath: "/sweet/fanny/adams", reqVersion: 1);
	}

	def requirementWithOneVersion() {
		Requirement requirement = Mock()
		requirement.id >> 10L
		requirement.status >> RequirementStatus.WORK_IN_PROGRESS

		RequirementVersion requirementVersion = Mock()
		requirementVersion.getId() >> 100L
		requirement.findRequirementVersion(1L) >> requirementVersion

		return requirement;
	}

	@Issue("#6255")
	@Unroll
	def "should check project with authz tc: #testCaseLibAuthorized, req: #requirementLibAuthorized"() {
		given:
		CoverageTarget coverageTarget = wellFormedCoverageTarget()

		and:
		Target checkedTarget = Mock()

		and:
		Project project = Mock()
		TestCaseLibrary testCaseLibrary = Mock()
		testCaseLibrary.id >> 100L
		RequirementLibrary requirementLibrary = Mock()
		requirementLibrary.id >> 200L
		project.testCaseLibrary >> testCaseLibrary
		project.requirementLibrary >> requirementLibrary

		projectDao.findByName(_) >> project

		and:
		permissionService.hasRoleOrPermissionOnObject("ROLE_ADMIN", "IMPORT", 100L, "org.squashtest.tm.domain.testcase.TestCaseLibrary") >> testCaseLibAuthorized
		permissionService.hasRoleOrPermissionOnObject("ROLE_ADMIN", "IMPORT", 200L, "org.squashtest.tm.domain.requirement.RequirementLibrary") >> requirementLibAuthorized

		when:
		def res = facility.checkPermissionOnProject("IMPORT", coverageTarget, checkedTarget)

		then:
		notThrown(UnsupportedOperationException) // as thrown in ish #6255
		check(res) || true
		//         ^^^^^^ in case 'check' is falsy

		where:
		testCaseLibAuthorized 	| requirementLibAuthorized	| check
		true 					| true						| { assert it == null }
		false					| false						| { assert it.status == FAILURE }
		false 					| true						| { assert it.status == FAILURE }
		true 					| false						| { assert it.status == FAILURE }
	}

}
