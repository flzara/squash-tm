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
package org.squashtest.tm.web.internal.controller.requirement

import java.util.Optional
import org.springframework.ui.ExtendedModelMap
import org.springframework.ui.Model
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder
import org.squashtest.tm.domain.requirement.RequirementVersion
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.exception.requirement.RequirementAlreadyVerifiedException
import org.squashtest.tm.service.internal.testcase.TestCaseWorkspaceDisplayService
import org.squashtest.tm.service.milestone.ActiveMilestoneHolder
import org.squashtest.tm.service.requirement.RequirementVersionManagerService
import org.squashtest.tm.service.security.PermissionEvaluationService
import org.squashtest.tm.service.testcase.VerifyingTestCaseManagerService
import org.squashtest.tm.service.user.UserAccountService
import org.squashtest.tm.web.internal.controller.milestone.MilestoneFeatureConfiguration
import org.squashtest.tm.web.internal.controller.milestone.MilestoneUIConfigurationService
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper
import org.squashtest.tm.web.internal.model.builder.DriveNodeBuilder
import spock.lang.Specification

import javax.inject.Provider

class VerifyingTestCaseManagerControllerTest extends Specification {
	VerifyingTestCaseManagerController controller = new VerifyingTestCaseManagerController()
	InternationalizationHelper i18nHelper = Mock()
	Provider<DriveNodeBuilder> driveNodeBuilder = driveNodeBuilderProvider()
	VerifyingTestCaseManagerService verifyingTestCaseManager = Mock()
	RequirementVersionManagerService requirementVersionManager = Mock()
	MilestoneUIConfigurationService milestoneConfService = Mock()
	ActiveMilestoneHolder activeMilestoneHolder = Mock()
	UserAccountService userAccountService = Mock()
	TestCaseWorkspaceDisplayService testCaseWorkspaceDisplayService = Mock()

	def setup() {
		controller.i18nHelper = i18nHelper
		controller.driveNodeBuilder = driveNodeBuilder
		controller.verifyingTestCaseManager = verifyingTestCaseManager
		controller.requirementVersionFinder = requirementVersionManager
		controller.milestoneConfService = milestoneConfService
		controller.activeMilestoneHolder = activeMilestoneHolder
		controller.userAccountService = userAccountService
		controller.testCaseWorkspaceDisplayService = testCaseWorkspaceDisplayService

		activeMilestoneHolder.getActiveMilestone() >> Optional.empty()
		activeMilestoneHolder.getActiveMilestoneId() >> Optional.of(-9000L)

		milestoneConfService.configure(_, _) >> new MilestoneFeatureConfiguration()
		milestoneConfService.configure(_) >> new MilestoneFeatureConfiguration()
	}

	def driveNodeBuilderProvider() {
		def provider = Mock(Provider)
		PermissionEvaluationService permissionEvaluationService = Mock()
		provider.get() >> new DriveNodeBuilder(permissionEvaluationService, null)
		return provider
	}

	def "should init model to show manager"() {
		given:
		Model model = new ExtendedModelMap()
		mockVerifyingTestCaseService()

		and:
		RequirementVersion requirementVersion = Mock()
		requirementVersionManager.findById(10L) >> requirementVersion

		and:
		verifyingTestCaseManager.findLinkableTestCaseLibraries() >> []

		and:
		testCaseWorkspaceDisplayService.findAllLibraries(_, _, _, _) >> []

		when:
		controller.showManager(10L, model, [] as String[])

		then:
		model.asMap()['requirementVersion'] == requirementVersion
		model.asMap()['linkableLibrariesModel'] == []
	}

	def "should return manager view name"() {
		given:
		requirementVersionManager.findById(10L) >> Mock(RequirementVersion)
		verifyingTestCaseManager.findLinkableTestCaseLibraries() >> []
		mockVerifyingTestCaseService()

		when:
		def view = controller.showManager(10L, Mock(Model), [] as String[])

		then:
		view == "page/requirement-workspace/show-verifying-testcase-manager"
	}

	def "should return rapport of test cases which could not be added"() {
		given:
		TestCase tc = Mock()
		tc.id >> 2
		RequirementVersion req = Mock()
		RequirementAlreadyVerifiedException ex = new RequirementAlreadyVerifiedException(req, tc)
		Map<String, Collection<?>> rejectionsAndIds = Mock();
		rejectionsAndIds.get(VerifyingTestCaseManagerService.REJECTION_KEY) >> [ex]
		verifyingTestCaseManager.addVerifyingTestCasesToRequirementVersion([1, 2], 10) >> rejectionsAndIds
		mockVerifyingTestCaseService()

		when:
		def res = controller.addVerifyingTestCasesToRequirement([1, 2], 10)

		then:
		res.alreadyVerifiedRejections
	}

	def mockVerifyingTestCaseService() {

		/*Requirement r = Mock()
		 RequirementVersion v = Mock()
		 r.getCurrentVersion() >> v
		 v.getId() >> 0*/

		PagedCollectionHolder<?> ch = Mock()
		ch.getFirstItemIndex() >> 0
		ch.getPagedItems() >> []

		verifyingTestCaseManager.findAllByRequirementVersion(_, _) >> ch

	}
}
