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
import org.squashtest.tm.service.bugtracker.BugTrackerFinderService
import org.squashtest.tm.service.infolist.InfoListModelService
import org.squashtest.tm.service.internal.dto.json.JsTreeNode
import org.squashtest.tm.service.internal.testcase.TestCaseWorkspaceDisplayService
import org.squashtest.tm.service.milestone.ActiveMilestoneHolder
import org.springframework.ui.Model
import org.squashtest.tm.service.milestone.MilestoneModelService
import org.squashtest.tm.service.user.PartyPreferenceService
import org.squashtest.tm.service.user.UserAccountService
import org.squashtest.tm.service.workspace.WorkspaceHelperService
import org.squashtest.tm.tools.unittest.reflection.ReflectionCategory
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.domain.testcase.TestCaseLibrary
import org.squashtest.tm.service.library.WorkspaceService
import org.squashtest.tm.web.internal.controller.generic.NodeBuildingSpecification
import org.squashtest.tm.service.project.ProjectFinder
import org.squashtest.tm.web.internal.helper.I18nLevelEnumInfolistHelper
import org.squashtest.tm.web.internal.model.builder.DriveNodeBuilder
import org.squashtest.tm.web.internal.model.builder.JsonProjectBuilder

import javax.inject.Inject
import javax.inject.Provider

class TestCasesWorkspaceControllerTest extends NodeBuildingSpecification {
	TestCaseWorkspaceController controller = new TestCaseWorkspaceController()
	WorkspaceService service = Mock()
	TestCaseWorkspaceDisplayService testCaseWorkspaceDisplayService = Mock()
	DriveNodeBuilder driveNodeBuilder = new DriveNodeBuilder(permissionEvaluator(), Mock(Provider))
	ProjectFinder projFinder = Mock()
	JsonProjectBuilder projBuilder = Mock()
	ActiveMilestoneHolder activeMilestoneHolder = Mock()
	Provider provider = Mock()
	PartyPreferenceService partyPreferenceService = Mock();
	I18nLevelEnumInfolistHelper i18nLevelEnumInfolistHelper = Mock();
	UserAccountService userAccountService = Mock()
	MilestoneModelService milestoneModelService = Mock()
	InfoListModelService infoListModelService = Mock()
	WorkspaceHelperService workspaceHelperService = Mock()
	BugTrackerFinderService bugTrackerFinderService = Mock()


	def setup() {
		controller.workspaceService = service
		controller.projectFinder = projFinder
		controller.userAccountService = userAccountService
		controller.jsonProjectBuilder = projBuilder
		controller.activeMilestoneHolder = activeMilestoneHolder
		controller.testCaseWorkspaceDisplayService = testCaseWorkspaceDisplayService
		activeMilestoneHolder.getActiveMilestoneId() >> Optional.of(-9000L)
		provider.get() >> driveNodeBuilder
		controller.partyPreferenceService = partyPreferenceService;
		controller.i18nLevelEnumInfolistHelper = i18nLevelEnumInfolistHelper;
		controller.milestoneModelService = milestoneModelService
		controller.infoListModelService = infoListModelService
		controller.workspaceHelperService = workspaceHelperService
		controller.bugTrackerFinderService = bugTrackerFinderService
		use(ReflectionCategory) {
			TestCaseWorkspaceController.set field: 'driveNodeBuilderProvider', of: controller, to: provider
		}
	}

	def "show should return workspace view with tree root model"() {
		given:
		TestCaseLibrary library = Mock()
		library.getClassSimpleName() >> "TestCaseLibrary"
		Project project = Mock()
		library.project >> project
		testCaseWorkspaceDisplayService.findAllLibraries(_,_) >> [library]
		testCaseWorkspaceDisplayService.findAllProjects(_,_) >> []
		def model = Mock(Model)
		def modelMap = ["rootModel" : new ArrayList<JsTreeNode>()]
		model.asMap() >> modelMap

		when:
		String view = controller.showWorkspace(model, Locale.getDefault(), [] as String[], "" as String)

		then:
		view == "test-case-workspace.html"
		1 * model.addAttribute("rootModel", _)
		1 * model.addAttribute("projects", [])
	}
}
