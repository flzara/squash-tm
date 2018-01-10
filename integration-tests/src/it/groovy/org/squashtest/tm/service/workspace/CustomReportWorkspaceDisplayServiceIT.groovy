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
import org.squashtest.tm.service.internal.customreport.CustomReportWorkspaceDisplayService
import org.squashtest.tm.service.internal.dto.UserDto
import org.squashtest.tm.service.internal.dto.json.JsTreeNode
import org.unitils.dbunit.annotation.DataSet
import spock.unitils.UnitilsSupport

import javax.inject.Inject

@UnitilsSupport
@Transactional
@NotThreadSafe
class CustomReportWorkspaceDisplayServiceIT extends DbunitServiceSpecification {

	@Inject
	CustomReportWorkspaceDisplayService customReportWorkspaceDisplayService = new CustomReportWorkspaceDisplayService()

	@DataSet("CustomReportWorkspaceDisplayService.sandbox.no.filter.xml")
	def "should find Custom Report Libraries as JsTreeNode"() {
		given:
		UserDto user = new UserDto("robert", -2L, [-100L, -300L], false)

		when:
		def jsTreeNodes = customReportWorkspaceDisplayService.doFindLibraries(readableProjectIds, user)

		then:
		jsTreeNodes.values().collect { it -> it.getAttr().get("resId") }.sort() as Set == expectedLibrariesIds.sort() as Set
		jsTreeNodes.values().collect { it -> it.getTitle() }.sort() as Set == expectedProjectsNames.sort() as Set

		where:
		readableProjectIds || expectedLibrariesIds | expectedProjectsNames      | expectedLibraryFullId
		[]                 || []                   | []                         | []
		[-1L, -2L]         || [-1L, -6L]           | ["project-1", "project-2"] | ["CustomReportLibrary--1", "CustomReportLibrary--6"]
	}

	@DataSet("CustomReportWorkspaceDisplayService.sandbox.xml")
	def "should find Campaign Libraries as JsTreeNode with filter"() {
		given:
		UserDto user = new UserDto("robert", -2L, [-100L, -300L], false)

		when:
		def jsTreeNodes = customReportWorkspaceDisplayService.doFindLibraries(readableProjectIds, user)

		then:
		jsTreeNodes.values().collect { it -> it.getAttr().get("resId") }.sort() as Set == expectedLibrariesIds.sort() as Set
		jsTreeNodes.values().collect { it -> it.getTitle() }.sort() as Set == expectedProjectsNames.sort() as Set

		where:
		readableProjectIds || expectedLibrariesIds | expectedProjectsNames | expectedLibraryFullId
		[]                 || []                   | []                    | []
		[-1L, -2L]         || [-1L]                | ["project-1"]         | ["CustomReportLibrary--1"]
	}

	@DataSet("CustomReportWorkspaceDisplayService.sandbox.xml")
	def "should build a custom report library with all its children"() {

		given:

		UserDto currentUser = new UserDto("robert", -2L, [-100L, -300L], false)

		MultiMap libraryFatherChildrenMultiMap = new MultiValueMap()
		MultiMap libraryNodeFatherChildrenMultiMap = new MultiValueMap()
		MultiMap expansionCandidates = new MultiValueMap()
		expansionCandidates.put("CustomReportLibrary", -1L)
		expansionCandidates.put("CustomReportFolder", -7L)
		Set<Long> childrenIds = new HashSet<>()

		def readableProjectIds = [-1L, -2L]

		when:

		customReportWorkspaceDisplayService.getFatherChildrenMultiMaps(expansionCandidates, childrenIds, libraryFatherChildrenMultiMap, libraryNodeFatherChildrenMultiMap)
		def libraryChildrenMap = customReportWorkspaceDisplayService.getLibraryChildrenMap(childrenIds)
		def jsTreeNodes = customReportWorkspaceDisplayService.doFindLibraries(readableProjectIds, currentUser)
		customReportWorkspaceDisplayService.buildHierarchy(jsTreeNodes, libraryFatherChildrenMultiMap, libraryNodeFatherChildrenMultiMap, libraryChildrenMap)

		then:

		libraryFatherChildrenMultiMap.size() == 1
		libraryFatherChildrenMultiMap.keySet() == [-1L] as Set
		libraryFatherChildrenMultiMap.get(-1L) == [-2L, -7L]

		libraryNodeFatherChildrenMultiMap.size() == 1
		libraryNodeFatherChildrenMultiMap.keySet() == [-7L] as Set
		libraryNodeFatherChildrenMultiMap.get(-7L) == [-8L, -9L]

		childrenIds.size() == 4
		childrenIds == [-2L, -7L, -8L, -9L] as Set

		libraryChildrenMap.keySet() == childrenIds as Set

		jsTreeNodes.size() == 1
		jsTreeNodes.values().collect { it.getAttr().get("resId") }.sort() == [-1L]
		jsTreeNodes.values().collect { it.getTitle() }.sort() == ["project-1"]
		jsTreeNodes.values().collect { it.getState() }.sort() == ["open"]

		def List<JsTreeNode> libraryChildren = jsTreeNodes.get(-1L).getChildren();  //id -1 : project-1

		libraryChildren.size() == 2
		libraryChildren.collect { it.getAttr().get("resId") } == [-2L, -7L]
		libraryChildren.collect { it.getTitle() }.sort() == ["Dash1", "Folder1"]
		libraryChildren.collect { it.getState() }.sort() == ["leaf", "open"]

		def List<JsTreeNode> folderChildren = libraryChildren.get(1).getChildren();  //id -7 : Folder1

		folderChildren.size() == 2
		folderChildren.collect { it.getAttr().get("resId") } == [-8L, -9L]
		folderChildren.collect { it.getTitle() }.sort() == ["Chart4", "report1"]
		folderChildren.collect { it.getState() }.sort() == ["leaf", "leaf"]
	}

	@DataSet("CustomReportWorkspaceDisplayService.sandbox.xml")
	def "should get a custom report library children"() {

		when:

		def nodes = customReportWorkspaceDisplayService.getNodeContent(-1L)

		then:

		nodes.size() == 2
		nodes.collect { it.getAttr().get("resId") } == [-2L, -7L]
		nodes.collect { it.getTitle() }.sort() == ["Dash1", "Folder1"]
		nodes.collect { it.getState() }.sort() == ["closed", "leaf"]
	}

}
