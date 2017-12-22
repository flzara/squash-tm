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
package org.squashtest.tm.service.workspace

import org.apache.commons.collections.MultiMap
import org.apache.commons.collections.map.MultiValueMap
import org.spockframework.util.NotThreadSafe
import org.springframework.transaction.annotation.Transactional
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.squashtest.tm.service.internal.dto.PermissionWithMask
import org.squashtest.tm.service.internal.dto.UserDto
import org.squashtest.tm.service.internal.dto.json.JsTreeNode
import org.squashtest.tm.service.internal.requirement.RequirementWorkspaceDisplayService
import org.unitils.dbunit.annotation.DataSet
import spock.unitils.UnitilsSupport

import javax.inject.Inject

@UnitilsSupport
@Transactional
@NotThreadSafe
class RequirementWorkspaceDisplayServiceIT extends DbunitServiceSpecification {

	@Inject
	RequirementWorkspaceDisplayService requirementWorkspaceDisplayService

	private HashMap<Long, JsTreeNode> initEmptyJsTreeNodes() {
		Map<Long, JsTreeNode> jsTreeNodes = new HashMap<>()
		jsTreeNodes.put(-14L, new JsTreeNode())
		jsTreeNodes.put(-15L, new JsTreeNode())
		jsTreeNodes.put(-16L, new JsTreeNode())
		jsTreeNodes.put(-19L, new JsTreeNode())
		jsTreeNodes
	}

	private HashMap<Long, JsTreeNode> initNoWizardJsTreeNodes() {
		Map<Long, JsTreeNode> jsTreeNodes = initEmptyJsTreeNodes()
		jsTreeNodes.values().each { it.addAttr("wizards", [] as Set) }
		jsTreeNodes
	}


	@DataSet("RequirementWorkspaceDisplayService.sandbox.no.filter.xml")
	def "should find Requirement Libraries as JsTreeNode"() {
		given:
		UserDto user = new UserDto("robert", -2L, [-100L, -300L], false)

		when:
		def jsTreeNodes = requirementWorkspaceDisplayService.doFindLibraries(readableProjectIds, user)

		then:
		jsTreeNodes.values().collect { it -> it.getAttr().get("resId") }.sort() as Set == expectedLibrariesIds.sort() as Set
		jsTreeNodes.values().collect { it -> it.getTitle() }.sort() as Set == expectedProjectsNames.sort() as Set

		where:
		readableProjectIds             || expectedLibrariesIds | expectedProjectsNames | expectedLibraryFullId
		[]                             || [] | [] | []
		[-14L, -15L, -16L, -19L, -21L] || [-14L, -15L, -16L, -19L] | ["Test Project-1", "Projet 1", "Projet 2", "Projet 5"] | ["RequirementLibrary-14", "RequirementLibrary-15", "RequirementLibrary-16", "RequirementLibrary-19"]
	}

	@DataSet("RequirementWorkspaceDisplayService.sandbox.xml")
	def "should find Requirement Libraries as JsTreeNode with filter"() {
		given:
		UserDto user = new UserDto("robert", -2L, [-100L, -300L], false)

		when:
		def jsTreeNodes = requirementWorkspaceDisplayService.doFindLibraries(readableProjectIds, user)

		then:
		jsTreeNodes.values().collect { it -> it.getAttr().get("resId") }.sort() as Set == expectedLibrariesIds.sort() as Set
		jsTreeNodes.values().collect { it -> it.getTitle() }.sort() as Set == expectedProjectsNames.sort() as Set

		where:
		readableProjectIds             || expectedLibrariesIds | expectedProjectsNames | expectedLibraryFullId
		[]                             || [] | [] | []
		[-14L, -15L, -16L, -19L, -21L] || [-14L, -15L] | ["Test Project-1", "Projet 1"] | ["RequirementLibrary-14", "RequirementLibrary-15"]
	}

	@DataSet("RequirementWorkspaceDisplayService.sandbox.no.filter.xml")
	def "should find Requirement Libraries as JsTreeNode with all perm for admin"() {
		given:
		UserDto user = new UserDto("robert", -2L, [], true)

		and:
		def readableProjectIds = [-14L, -15L, -16L, -19L, -21L]

		when:
		def jsTreeNodes = requirementWorkspaceDisplayService.doFindLibraries(readableProjectIds, user)

		then:
		jsTreeNodes.values().collect { it -> it.getAttr().get("resId") }.sort() as Set == [-14L, -15L, -16L, -19L].sort() as Set
		jsTreeNodes.values().collect { it -> it.getAttr().get(PermissionWithMask.READ.getQuality()) == String.valueOf(true) }
		jsTreeNodes.values().collect { it -> it.getAttr().get(PermissionWithMask.WRITE.getQuality()) == String.valueOf(true) }
		jsTreeNodes.values().collect { it -> it.getAttr().get(PermissionWithMask.CREATE.getQuality()) == String.valueOf(true) }
		jsTreeNodes.values().collect { it -> it.getAttr().get(PermissionWithMask.DELETE.getQuality()) == String.valueOf(true) }
		jsTreeNodes.values().collect { it -> it.getAttr().get(PermissionWithMask.IMPORT.getQuality()) == String.valueOf(true) }
		jsTreeNodes.values().collect { it -> it.getAttr().get(PermissionWithMask.EXECUTE.getQuality()) == null } //execute is only for campaign
		jsTreeNodes.values().collect { it -> it.getAttr().get(PermissionWithMask.IMPORT.getQuality()) == String.valueOf(true) }
		jsTreeNodes.values().collect { it -> it.getAttr().get(PermissionWithMask.EXPORT.getQuality()) == String.valueOf(true) }
		jsTreeNodes.values().collect { it -> it.getAttr().get(PermissionWithMask.LINK.getQuality()) == String.valueOf(true) }
		jsTreeNodes.values().collect { it -> it.getAttr().get(PermissionWithMask.ATTACH.getQuality()) == String.valueOf(true) }
		jsTreeNodes.values().collect { it -> it.getAttr().get(PermissionWithMask.MANAGEMENT.getQuality()) == null } //management is only for projects
	}

	@DataSet("RequirementWorkspaceDisplayService.sandbox.xml")
	def "should find permission masks for standard user"() {
		given:
		UserDto user = new UserDto("robert", -2L, [-100L, -300L], false)
		HashMap<Long, JsTreeNode> jsTreeNodes = initEmptyJsTreeNodes()

		when:
		requirementWorkspaceDisplayService.findPermissionMap(user, jsTreeNodes)

		then:
		jsTreeNodes.keySet().sort() == [-14L, -15L, -16L, -19L].sort()

		def lib15Attr = jsTreeNodes.get(-15L).getAttr()
		lib15Attr.get(PermissionWithMask.READ.getQuality()) == String.valueOf(true)
		lib15Attr.get(PermissionWithMask.WRITE.getQuality()) == null
		lib15Attr.get(PermissionWithMask.CREATE.getQuality()) == null
		lib15Attr.get(PermissionWithMask.DELETE.getQuality()) == null
		lib15Attr.get(PermissionWithMask.IMPORT.getQuality()) == null
		lib15Attr.get(PermissionWithMask.EXECUTE.getQuality()) == null
		lib15Attr.get(PermissionWithMask.EXPORT.getQuality()) == null
		lib15Attr.get(PermissionWithMask.LINK.getQuality()) == null
		lib15Attr.get(PermissionWithMask.ATTACH.getQuality()) == null
		lib15Attr.get(PermissionWithMask.MANAGEMENT.getQuality()) == null

		def lib14Attr = jsTreeNodes.get(-14L).getAttr()
		lib14Attr.get(PermissionWithMask.READ.getQuality()) == String.valueOf(true)
		lib14Attr.get(PermissionWithMask.WRITE.getQuality()) == String.valueOf(true)
		lib14Attr.get(PermissionWithMask.CREATE.getQuality()) == String.valueOf(true)
		lib14Attr.get(PermissionWithMask.DELETE.getQuality()) == String.valueOf(true)
		lib14Attr.get(PermissionWithMask.IMPORT.getQuality()) == String.valueOf(true)
		lib14Attr.get(PermissionWithMask.EXECUTE.getQuality()) == null //execute is for campaign workspace
		lib14Attr.get(PermissionWithMask.EXPORT.getQuality()) == String.valueOf(true)
		lib14Attr.get(PermissionWithMask.LINK.getQuality()) == String.valueOf(true)
		lib14Attr.get(PermissionWithMask.ATTACH.getQuality()) == String.valueOf(true)
		lib14Attr.get(PermissionWithMask.MANAGEMENT.getQuality()) == null //we can't manager libraries, we manage projects...

		def lib16Attr = jsTreeNodes.get(-16L).getAttr()
		lib16Attr.get(PermissionWithMask.READ.getQuality()) == String.valueOf(true)
		lib16Attr.get(PermissionWithMask.WRITE.getQuality()) == null
		lib16Attr.get(PermissionWithMask.CREATE.getQuality()) == null
		lib16Attr.get(PermissionWithMask.DELETE.getQuality()) == null
		lib16Attr.get(PermissionWithMask.IMPORT.getQuality()) == null
		lib16Attr.get(PermissionWithMask.EXECUTE.getQuality()) == null
		lib16Attr.get(PermissionWithMask.EXPORT.getQuality()) == String.valueOf(true)
		lib16Attr.get(PermissionWithMask.LINK.getQuality()) == String.valueOf(true)
		lib16Attr.get(PermissionWithMask.ATTACH.getQuality()) == null
		lib16Attr.get(PermissionWithMask.MANAGEMENT.getQuality()) == null

		def lib19Attr = jsTreeNodes.get(-19L).getAttr()
		lib19Attr.get(PermissionWithMask.READ.getQuality()) == String.valueOf(true)
		lib19Attr.get(PermissionWithMask.WRITE.getQuality()) == String.valueOf(true)
		lib19Attr.get(PermissionWithMask.CREATE.getQuality()) == String.valueOf(true)
		lib19Attr.get(PermissionWithMask.DELETE.getQuality()) == String.valueOf(true)
		lib19Attr.get(PermissionWithMask.IMPORT.getQuality()) == String.valueOf(true)
		lib19Attr.get(PermissionWithMask.EXECUTE.getQuality()) == null
		lib19Attr.get(PermissionWithMask.EXPORT.getQuality()) == String.valueOf(true)
		lib19Attr.get(PermissionWithMask.LINK.getQuality()) == String.valueOf(true)
		lib19Attr.get(PermissionWithMask.ATTACH.getQuality()) == String.valueOf(true)
		lib19Attr.get(PermissionWithMask.MANAGEMENT.getQuality()) == null
	}

	@DataSet("RequirementWorkspaceDisplayService.sandbox.xml")
	def "should find wizards for requirement library"() {
		given:
		def jsTreeNodes = initNoWizardJsTreeNodes()

		when:
		requirementWorkspaceDisplayService.findWizards([-14L, -15L, -16L, -19L, -21L], jsTreeNodes)

		then:
		jsTreeNodes.size() == 4
		jsTreeNodes.get(-14L).getAttr().get("wizards") == ["RedmineReq"] as Set
		jsTreeNodes.get(-15L).getAttr().get("wizards") == ["RedmineReq", "JiraAgile", "JiraForSquash"] as Set
		jsTreeNodes.get(-16L).getAttr().get("wizards") == ["JiraReq", "JiraAgile"] as Set
		jsTreeNodes.get(-19L).getAttr().get("wizards") == [] as Set
	}

	@DataSet("RequirementWorkspaceDisplayService.sandbox.xml")
	def "should find projects models"() {
		given:
		UserDto user = new UserDto("robert", -2L, [-100L, -300L], false)

		when:
		def jsonProjects = requirementWorkspaceDisplayService.findAllProjects([-14L, -15L, -16L, -19L, -21L], user)

		then:
		jsonProjects.size() == 4
		jsonProjects.collect { it.name }.sort() == ["Projet 1", "Projet 2", "Projet 5", "Test Project-1"]

		def jsonProject15 = jsonProjects.getAt(2)
		jsonProject15.getId() == -15L
		jsonProject15.getName().equals("Projet 1")
		jsonProject15.getRequirementCategories().id == -1L
		jsonProject15.getTestCaseNatures().id == -2L
		jsonProject15.getTestCaseTypes().id == -3L

		def customFieldBindings = jsonProject15.getCustomFieldBindings()
		customFieldBindings.size() == 8
		def customFieldBindingModels = customFieldBindings.get("REQUIREMENT_VERSION")
		customFieldBindingModels.size() == 4
		customFieldBindingModels.collect { it.id }.sort() == [-4L, -3L, -2L, -1L]
		customFieldBindingModels.collect { it.customField.id }.sort() == [-4L, -3L, -2L, -1L]
		customFieldBindingModels.collect { it.customField.name }.sort() == ["Liste", "Liste 2", "Lot", "Rich"]

		def jsonMilestones = jsonProject15.getMilestones()
		jsonMilestones.size() == 2
		jsonMilestones.collect { it.label }.sort() == ["Jalon 1", "Jalon 2"]
	}

	@DataSet("RequirementWorkspaceDisplayService.sandbox.xml")
	def "should build requirement libraries with all their children"() {

		given:

		UserDto currentUser = new UserDto("robert", -2L, [-100L, -300L], false)

		MultiMap expansionCandidates = new MultiValueMap();
		expansionCandidates.put("RequirementLibrary", -15L);
		expansionCandidates.put("RequirementFolder", -256L);
		expansionCandidates.put("Requirement", -270L);

		Set<Long> childrenIds = new HashSet<>();

		def readableProjectIds = [-14L, -15L, -16L, -19L, -21L]

		when:

		def libraryFatherChildrenMultiMap = requirementWorkspaceDisplayService.getLibraryFatherChildrenMultiMap(expansionCandidates, childrenIds, new HashSet<Long>(), -9000L)
		def libraryNodeFatherChildrenMultiMap = requirementWorkspaceDisplayService.getLibraryNodeFatherChildrenMultiMap(expansionCandidates, childrenIds, new HashSet<Long>(), -9000L)
		def libraryChildrenMap = requirementWorkspaceDisplayService.getLibraryChildrenMap(childrenIds, expansionCandidates, currentUser, new HashMap<Long, List<Long>>(), new ArrayList<Long>(), -9000L)
		def jsTreeNodes = requirementWorkspaceDisplayService.doFindLibraries(readableProjectIds, currentUser)
		requirementWorkspaceDisplayService.buildHierarchy(jsTreeNodes, libraryFatherChildrenMultiMap, libraryNodeFatherChildrenMultiMap, libraryChildrenMap, -9000L)

		then:

		libraryFatherChildrenMultiMap.size() == 1
		libraryFatherChildrenMultiMap.keySet() == [-15L] as Set
		libraryFatherChildrenMultiMap.get(-15L).sort() == [-276L, -270L, -269L, -257L, -256L]

		libraryNodeFatherChildrenMultiMap.size() == 2
		libraryNodeFatherChildrenMultiMap.keySet().sort() == [-270L, -256L].sort()
		libraryNodeFatherChildrenMultiMap.get(-256L).sort() == [-259L, -258L]

		childrenIds.size() == 8
		childrenIds == [-276L, -271L, -270L, -269L, -259L, -258L, -257L, -256L] as Set

		libraryChildrenMap.keySet() == childrenIds as Set

		jsTreeNodes.size() == 2;
		jsTreeNodes.values().collect { it.getAttr().get("resId") }.sort() == [-15L, -14L]
		jsTreeNodes.values().collect { it.getTitle() }.sort() == ["Projet 1", "Test Project-1"]
		jsTreeNodes.values().collect { it.getState() }.sort() == ["closed", "open"]

		def List<JsTreeNode> libraryChildren = jsTreeNodes.get(-15L).getChildren();  //id -15 : Projet 1

		libraryChildren.size() == 5
		libraryChildren.collect { it.getAttr().get("resId") }.sort() == [-276L, -270L, -269L, -257L, -256L]
		libraryChildren.collect {
			it.getTitle()
		}.sort() == ["Dossier A", "Dossier B", "Exigence", "Exigence 0", "Exigence 10"]
		libraryChildren.collect { it.getState() }.sort() == ["closed", "leaf", "leaf", "open", "open"]

		def List<JsTreeNode> folderChildren = libraryChildren.get(0).getChildren();  //id -256 : Dossier A

		folderChildren.size() == 2
		folderChildren.collect { it.getAttr().get("resId") }.sort() == [-259L, -258L]
		folderChildren.collect { it.getTitle() }.sort() == ["Dossier A2", "Dossier AA"]
		folderChildren.collect { it.getState() }.sort() == ["closed", "leaf"]

		def List<JsTreeNode> requirementChildren = libraryChildren.get(3).getChildren();  //id -270 : Exigence 10

		requirementChildren.size() == 1
		requirementChildren.collect { it.getAttr().get("resId") }.sort() == [-271L]
		requirementChildren.collect { it.getTitle() }.sort() == ["Exigence 11"]
		requirementChildren.collect { it.getState() }.sort() == ["closed"]
	}

	@DataSet("RequirementWorkspaceDisplayService.sandbox.xml")
	def "should get a requirement library children"() {

		given:

		UserDto currentUser = new UserDto("robert", -2L, [-100L, -300L], false)


		when:

		def nodes = requirementWorkspaceDisplayService.getNodeContent(-15L, currentUser, "library", -9000L)

		then:

		nodes.size() == 5
		nodes.collect { it.getAttr().get("resId") }.sort() == [-276L, -270L, -269L, -257L, -256L]
		nodes.collect {
			it.getTitle()
		}.sort() == ["Dossier A", "Dossier B", "Exigence", "Exigence 0", "Exigence 10"]
		nodes.collect { it.getState() }.sort() == ["closed", "closed", "closed", "leaf", "leaf"]
	}

	@DataSet("RequirementWorkspaceDisplayService.sandbox.xml")
	def "should get a requirement folder children"() {

		given:

		UserDto currentUser = new UserDto("robert", -2L, [-100L, -300L], false)


		when:

		def nodes = requirementWorkspaceDisplayService.getNodeContent(-256L, currentUser, "folder", -9000L)

		then:

		nodes.size() == 2
		nodes.collect { it.getAttr().get("resId") }.sort() == [-259L, -258L]
		nodes.collect { it.getTitle() }.sort() == ["Dossier A2", "Dossier AA"]
		nodes.collect { it.getState() }.sort() == ["closed", "leaf"]
	}

	@DataSet("RequirementWorkspaceDisplayService.sandbox.xml")
	def "should get requirement children"() {

		given:

		UserDto currentUser = new UserDto("robert", -2L, [-100L, -300L], false)


		when:

		def nodes = requirementWorkspaceDisplayService.getNodeContent(-270L, currentUser, "Requirement", -9000L)

		then:

		nodes.size() == 1
		nodes.collect { it.getAttr().get("resId") }.sort() == [-271L]
		nodes.collect { it.getTitle() }.sort() == ["Exigence 11"]
		nodes.collect { it.getState() }.sort() == ["closed"]
	}
}
