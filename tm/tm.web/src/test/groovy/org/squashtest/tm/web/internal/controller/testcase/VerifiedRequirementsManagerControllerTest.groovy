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
package org.squashtest.tm.web.internal.controller.testcase

import java.util.Optional
import org.springframework.ui.ExtendedModelMap
import org.springframework.ui.Model
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.domain.requirement.Requirement
import org.squashtest.tm.domain.requirement.RequirementLibrary
import org.squashtest.tm.domain.testcase.ActionTestStep
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.testcase.TestStep
import org.squashtest.tm.exception.NoVerifiableRequirementVersionException
import org.squashtest.tm.service.internal.requirement.RequirementWorkspaceDisplayService
import org.squashtest.tm.service.milestone.ActiveMilestoneHolder
import org.squashtest.tm.service.requirement.RequirementLibraryFinderService
import org.squashtest.tm.service.requirement.VerifiedRequirementsManagerService
import org.squashtest.tm.service.security.PermissionEvaluationService
import org.squashtest.tm.service.testcase.TestCaseModificationService
import org.squashtest.tm.service.testcase.TestStepModificationService
import org.squashtest.tm.service.user.UserAccountService
import org.squashtest.tm.web.internal.controller.generic.NodeBuildingSpecification
import org.squashtest.tm.web.internal.controller.milestone.MilestoneFeatureConfiguration
import org.squashtest.tm.web.internal.controller.milestone.MilestoneUIConfigurationService
import org.squashtest.tm.web.internal.controller.testcase.requirement.VerifiedRequirementsManagerController
import org.squashtest.tm.web.internal.model.builder.DriveNodeBuilder
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters

import javax.inject.Provider

class VerifiedRequirementsManagerControllerTest extends NodeBuildingSpecification {
	VerifiedRequirementsManagerController controller = new VerifiedRequirementsManagerController()
	VerifiedRequirementsManagerService verifiedRequirementsManagerService = Mock()
	Provider driveNodeBuilder = Mock()
	TestCaseModificationService testCaseFinder = Mock()
	RequirementLibraryFinderService requirementLibraryFinder = Mock()
	TestStepModificationService testStepService = Mock()
	MilestoneUIConfigurationService milestoneConfService = Mock()
	PermissionEvaluationService permissionService = permissionEvaluator()
	ActiveMilestoneHolder activeMilestoneHolder = Mock()
	UserAccountService userAccountService = Mock()
	RequirementWorkspaceDisplayService requirementWorkspaceDisplayService = Mock()

	def setup() {
		controller.verifiedRequirementsManagerService = verifiedRequirementsManagerService
		controller.driveNodeBuilder = driveNodeBuilder
		controller.testCaseModificationService = testCaseFinder
		controller.requirementLibraryFinder = requirementLibraryFinder
		controller.testStepService = testStepService;
		controller.milestoneConfService = milestoneConfService
		controller.userAccountService = userAccountService
		controller.requirementWorkspaceDisplayService = requirementWorkspaceDisplayService
		milestoneConfService.configure(_, _) >> new MilestoneFeatureConfiguration()
		milestoneConfService.configure(_) >> new MilestoneFeatureConfiguration()

		controller.activeMilestoneHolder = activeMilestoneHolder
		activeMilestoneHolder.getActiveMilestone() >> Optional.empty()
		activeMilestoneHolder.getActiveMilestoneId() >> Optional.of(-9000L)

		driveNodeBuilder.get() >> new DriveNodeBuilder(permissionEvaluator(), null)
		controller.permissionService = permissionService;
		permissionService.hasRoleOrPermissionOnObject(_, _, _) >> true
	}

	def "should show test case manager page"() {
		given:
		requirementLibraryFinder.findLinkableRequirementLibraries() >> []

		when:
		def res = controller.showTestCaseManager(20L, Mock(Model), [] as String[])

		then:
		res == "page/test-case-workspace/show-verified-requirements-manager"
	}

	def "should show test step manager page"() {
		given:
		requirementLibraryFinder.findLinkableRequirementLibraries() >> []

		and:
		testStepService.findById(_) >> Mock(ActionTestStep)


		when:
		def res = controller.showTestStepManager(20L, Mock(Model), [] as String[])

		then:
		res == "page/test-case-workspace/show-step-verified-requirements-manager"
	}

	def "should populate manager page with test case and requirement libraries model"() {
		given:
		TestCase testCase = Mock()
		testCaseFinder.findById(20L) >> testCase

		and:
		RequirementLibrary lib = Mock()
		lib.getClassSimpleName() >> "RequirementLibrary"
		Project project = Mock()
		project.getId() >> 10l
		lib.project >> project
		lib.getId() >> 101L
		requirementLibraryFinder.findLinkableRequirementLibraries() >> [lib]

		and:
		def model = new ExtendedModelMap()

		and:

		requirementWorkspaceDisplayService.findAllLibraries(_, _, _, _) >> []

		when:
		def res = controller.showTestCaseManager(20L, model, [] as String[])

		then:
		model['testCase'] == testCase
		model['linkableLibrariesModel'] != null
	}

	def "should populate manager page with test step and requirement libraries model"() {
		given:
		TestStep testStep = Mock()
		testStepService.findById(20L) >> testStep

		and:
		RequirementLibrary lib = Mock()
		lib.getClassSimpleName() >> "RequirementLibrary"
		Project project = Mock()
		project.getId() >> 10l
		lib.project >> project
		requirementLibraryFinder.findLinkableRequirementLibraries() >> [lib]

		and:
		def model = new ExtendedModelMap()

		when:
		def res = controller.showTestStepManager(20L, model, [] as String[])

		then:
		model['testStep'] == testStep
		model['linkableLibrariesModel'] != null
	}

	def "should add requirements to verified requirements to test case"() {
		when:
		controller.addVerifiedRequirementsToTestCase([5, 15], 10)

		then:
		1 * verifiedRequirementsManagerService.addVerifiedRequirementsToTestCase([5, 15], 10) >> []
	}

	def "should add requirements to verified requirements of test step"() {
		when:
		controller.addVerifiedRequirementsToTestStep([5, 15], 10)

		then:
		1 * verifiedRequirementsManagerService.addVerifiedRequirementsToTestStep([5, 15], 10) >> []
	}

	def "should add requirements to verified requirement of test step"() {
		when:
		controller.addVerifiedRequirementsToTestStep([5L], 10L)

		then:
		1 * verifiedRequirementsManagerService.addVerifiedRequirementsToTestStep([5L], 10L) >> []
	}

	def "should remove requirements from verified requirements of test case"() {
		when:
		controller.removeVerifiedRequirementVersionsFromTestCase([5, 15], 10)

		then:
		1 * verifiedRequirementsManagerService.removeVerifiedRequirementVersionsFromTestCase([5, 15], 10)
	}

	def "should remove requirements from verified requirements of test step"() {
		when:
		controller.removeVerifiedRequirementVersionsFromTestStep([5, 15], 10)

		then:
		1 * verifiedRequirementsManagerService.removeVerifiedRequirementVersionsFromTestStep([5, 15], 10)
	}

	def "should return rapport of requirements which could not be added"() {
		given:
		Requirement req = Mock()
		NoVerifiableRequirementVersionException ex = new NoVerifiableRequirementVersionException(req)
		verifiedRequirementsManagerService.addVerifiedRequirementsToTestCase([5, 15], 10) >> [ex]

		when:
		def res = controller.addVerifiedRequirementsToTestCase([5, 15], 10)

		then:
		res.noVerifiableVersionRejections
	}

	def "should build table model for verified requirements"() {
		given:
		DataTableDrawParameters request = new DataTableDrawParameters(sEcho: "echo", iDisplayStart: 0, iDisplayLength: 100)

		and:
		PagedCollectionHolder holder = Mock()
		holder.pagedItems >> []
		verifiedRequirementsManagerService.findAllVerifiedRequirementsByTestCaseId(10, _) >> holder

		when:
		def res = controller.getTestCaseWithCallStepsVerifiedRequirementsTableModel(10, request, Locale.getDefault())

		then:
		res.sEcho == "echo"
		res.iTotalDisplayRecords == 0
		res.iTotalRecords == 0
	}
}
