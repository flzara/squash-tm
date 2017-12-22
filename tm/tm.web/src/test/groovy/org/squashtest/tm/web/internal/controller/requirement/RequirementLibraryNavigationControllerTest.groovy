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
import org.springframework.context.MessageSource
import org.squashtest.tm.domain.infolist.ListItemReference
import org.squashtest.tm.domain.requirement.*
import org.squashtest.tm.service.internal.dto.json.JsTreeNode
import org.squashtest.tm.service.internal.requirement.RequirementWorkspaceDisplayService
import org.squashtest.tm.service.milestone.ActiveMilestoneHolder
import org.squashtest.tm.service.requirement.RequirementLibraryNavigationService
import org.squashtest.tm.service.security.PermissionEvaluationService
import org.squashtest.tm.service.user.UserAccountService
import org.squashtest.tm.tools.unittest.reflection.ReflectionCategory
import org.squashtest.tm.web.internal.controller.generic.LibraryNavigationController
import org.squashtest.tm.web.internal.controller.generic.NodeBuildingSpecification
import org.squashtest.tm.web.internal.model.builder.DriveNodeBuilder
import org.squashtest.tm.web.internal.model.builder.RequirementLibraryTreeNodeBuilder

import javax.inject.Provider

class RequirementLibraryNavigationControllerTest extends NodeBuildingSpecification {
	RequirementLibraryNavigationController controller = new RequirementLibraryNavigationController()
	RequirementLibraryNavigationService requirementLibraryNavigationService = Mock()
	Provider driveNodeBuilder = Mock();
	Provider requirementLibraryTreeNodeBuilder = Mock();
	ActiveMilestoneHolder activeMilestoneHolder = Mock()
	UserAccountService userAccountService = Mock();
	RequirementWorkspaceDisplayService requirementWorkspaceDisplayService = Mock()

	def setup() {
		controller.requirementLibraryNavigationService = requirementLibraryNavigationService
		controller.driveNodeBuilder = driveNodeBuilder
		controller.requirementLibraryTreeNodeBuilder = requirementLibraryTreeNodeBuilder
		controller.userAccountService = userAccountService
		controller.requirementWorkspaceDisplayService = requirementWorkspaceDisplayService

		use(ReflectionCategory) {
			LibraryNavigationController.set field: "messageSource", of: controller, to: Mock(MessageSource)
		}

		controller.activeMilestoneHolder = activeMilestoneHolder
		activeMilestoneHolder.getActiveMilestone() >> Optional.empty()
		activeMilestoneHolder.getActiveMilestoneId() >> Optional.of(-9000L)
		driveNodeBuilder.get() >> new DriveNodeBuilder(Mock(PermissionEvaluationService), null)
		requirementLibraryTreeNodeBuilder.get() >> new RequirementLibraryTreeNodeBuilder(permissionEvaluator())
	}

	def "should add folder to root of library and return folder node model"() {
		given:
		RequirementFolder folder = new RequirementFolder(name: "new folder") // we need the real thing because of visitor pattern
		use(ReflectionCategory) {
			RequirementLibraryNode.set field: "id", of: folder, to: 100L
		}

		when:
		JsTreeNode res = controller.addNewFolderToLibraryRootContent(10, folder)

		then:
		1 * requirementLibraryNavigationService.addFolderToLibrary(10, folder)
		res.title == "new folder"
		res.attr['resId'] == "100"
		res.attr['rel'] == "folder"
	}

	def "should return root nodes of library"() {
		given:
		RequirementFolder rootFolder = Mock()

		requirementWorkspaceDisplayService.getNodeContent(_,_,_,_) >> [rootFolder]

		when:
		def res = controller.getRootContentTreeModel(10)

		then:
		res.size() == 1
	}

	def "should add requirement to root of library and return requirement node model"() {
		given:
		RequirementFormModel firstVersion = new RequirementFormModel(
			name: "new req",
			criticality: RequirementCriticality.MAJOR,
			category: RequirementCategory.PERFORMANCE,
			customFields: [:])
		Requirement req = new Requirement(new RequirementVersion(name: "new req", category: new ListItemReference("whatever")))
		use(ReflectionCategory) {
			RequirementLibraryNode.set field: "id", of: req, to: 100L
		}

		when:
		JsTreeNode res = controller.addNewRequirementToLibraryRootContent(100, firstVersion)

		then:
		1 * requirementLibraryNavigationService.addRequirementToRequirementLibrary(100, _, []) >> req
		res.title == "new req"
		res.attr['resId'] == "100"
		res.attr['rel'] == "requirement"
	}

	def "should return content nodes of folder"() {
		given:
		RequirementFolder content = Mock()
		content.name >> "content"
		content.id >> 5

		requirementWorkspaceDisplayService.getNodeContent(_,_,_,_) >> [content]

		when:
		def res = controller.getFolderContentTreeModel(10)

		then:
		res.size() == 1
	}

	def "should add folder to folder content and return folder node model"() {
		given:
		RequirementFolder folder = new RequirementFolder(name: "new folder") // we need the real thing because of visitor pattern
		use(ReflectionCategory) {
			RequirementLibraryNode.set field: "id", of: folder, to: 100L
		}

		when:
		JsTreeNode res = controller.addNewFolderToFolderContent(100, folder)

		then:
		1 * requirementLibraryNavigationService.addFolderToFolder(100, folder)
		res.title == "new folder"
		res.attr['resId'] == "100"
		res.attr['rel'] == "folder"
	}


}
