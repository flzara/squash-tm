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
import org.squashtest.tm.domain.campaign.Campaign
import org.squashtest.tm.domain.campaign.CampaignFolder
import org.squashtest.tm.domain.campaign.CampaignLibrary
import org.squashtest.tm.domain.library.Library
import org.squashtest.tm.domain.campaign.Iteration
import org.squashtest.tm.service.internal.campaign.CampaignWorkspaceDisplayService
import org.squashtest.tm.service.internal.dto.PermissionWithMask
import org.squashtest.tm.service.internal.dto.UserDto
import org.squashtest.tm.service.internal.dto.json.JsTreeNode
import org.squashtest.tm.service.internal.repository.hibernate.HibernateCampaignDao
import org.squashtest.tm.service.internal.repository.hibernate.HibernateCampaignFolderDao
import org.squashtest.tm.service.internal.repository.hibernate.HibernateIterationDao
import org.unitils.dbunit.annotation.DataSet
import spock.unitils.UnitilsSupport

import javax.inject.Inject

@UnitilsSupport
@Transactional
@NotThreadSafe
class CampaignWorkspaceDisplayServiceIT extends DbunitServiceSpecification {

	@Inject
	CampaignWorkspaceDisplayService campaignWorkspaceDisplayService

	private HibernateCampaignFolderDao hibernateCampaignFolderDao

	private HibernateCampaignDao hibernateCampaignDao

	private HibernateIterationDao hibernateIterationDao


	def setup() {
		hibernateCampaignFolderDao = Mock()
		hibernateCampaignDao = Mock()
		hibernateIterationDao = Mock()

		campaignWorkspaceDisplayService.hibernateCampaignFolderDao = hibernateCampaignFolderDao
		campaignWorkspaceDisplayService.hibernateCampaignDao = hibernateCampaignDao
		campaignWorkspaceDisplayService.hibernateIterationDao = hibernateIterationDao
	}

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


	@DataSet("CampaignWorkspaceDisplayService.sandbox.no.filter.xml")
	def "should find Campaign Libraries as JsTreeNode"() {
		given:
		UserDto user = new UserDto("robert", -2L, [-100L, -300L], false)

		when:
		def jsTreeNodes = campaignWorkspaceDisplayService.doFindLibraries(readableProjectIds, user)

		then:
		jsTreeNodes.values().collect { it -> it.getAttr().get("resId") }.sort() as Set == expectedLibrariesIds.sort() as Set
		jsTreeNodes.values().collect { it -> it.getTitle() }.sort() as Set == expectedProjectsNames.sort() as Set

		where:
		readableProjectIds             || expectedLibrariesIds | expectedProjectsNames | expectedLibraryFullId
		[]                             || [] | [] | []
		[-14L, -15L, -16L, -19L, -21L] || [-14L, -15L, -16L, -19L] | ["Test Project-1", "Projet 1", "Projet 2", "Projet 5"] | ["RequirementLibrary-14", "RequirementLibrary-15", "RequirementLibrary-16", "RequirementLibrary-19"]
	}

	@DataSet("CampaignWorkspaceDisplayService.sandbox.xml")
	def "should find Campaign Libraries as JsTreeNode with filter"() {
		given:
		UserDto user = new UserDto("robert", -2L, [-100L, -300L], false)

		when:
		def jsTreeNodes = campaignWorkspaceDisplayService.doFindLibraries(readableProjectIds, user)

		then:
		jsTreeNodes.values().collect { it -> it.getAttr().get("resId") }.sort() as Set == expectedLibrariesIds.sort() as Set
		jsTreeNodes.values().collect { it -> it.getTitle() }.sort() as Set == expectedProjectsNames.sort() as Set

		where:
		readableProjectIds             || expectedLibrariesIds | expectedProjectsNames | expectedLibraryFullId
		[]                             || [] | [] | []
		[-14L, -15L, -16L, -19L, -21L] || [-14L, -15L] | ["Test Project-1", "Projet 1"] | ["RequirementLibrary-14", "RequirementLibrary-15"]
	}

	@DataSet("CampaignWorkspaceDisplayService.sandbox.no.filter.xml")
	def "should find Campaign Libraries as JsTreeNode with all perm for admin"() {
		given:
		UserDto user = new UserDto("robert", -2L, [], true)

		and:
		def readableProjectIds = [-14L, -15L, -16L, -19L, -21L]

		when:
		def jsTreeNodes = campaignWorkspaceDisplayService.doFindLibraries(readableProjectIds, user)

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

	@DataSet("CampaignWorkspaceDisplayService.sandbox.xml")
	def "should find permission masks for standard user"() {
		given:
		UserDto user = new UserDto("robert", -2L, [-100L, -300L], false)
		HashMap<Long, JsTreeNode> jsTreeNodes = initEmptyJsTreeNodes()

		when:
		campaignWorkspaceDisplayService.findPermissionMap(user, jsTreeNodes)

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
		lib14Attr.get(PermissionWithMask.IMPORT.getQuality()) == null
		lib14Attr.get(PermissionWithMask.EXECUTE.getQuality()) == String.valueOf(true)
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
		lib16Attr.get(PermissionWithMask.EXECUTE.getQuality()) == String.valueOf(true)
		lib16Attr.get(PermissionWithMask.EXPORT.getQuality()) == String.valueOf(true)
		lib16Attr.get(PermissionWithMask.LINK.getQuality()) == null
		lib16Attr.get(PermissionWithMask.ATTACH.getQuality()) == String.valueOf(true)
		lib16Attr.get(PermissionWithMask.MANAGEMENT.getQuality()) == null

		def lib19Attr = jsTreeNodes.get(-19L).getAttr()
		lib19Attr.get(PermissionWithMask.READ.getQuality()) == String.valueOf(true)
		lib19Attr.get(PermissionWithMask.WRITE.getQuality()) == String.valueOf(true)
		lib19Attr.get(PermissionWithMask.CREATE.getQuality()) == String.valueOf(true)
		lib19Attr.get(PermissionWithMask.DELETE.getQuality()) == String.valueOf(true)
		lib19Attr.get(PermissionWithMask.IMPORT.getQuality()) == null
		lib19Attr.get(PermissionWithMask.EXECUTE.getQuality()) == String.valueOf(true)
		lib19Attr.get(PermissionWithMask.EXPORT.getQuality()) == String.valueOf(true)
		lib19Attr.get(PermissionWithMask.LINK.getQuality()) == String.valueOf(true)
		lib19Attr.get(PermissionWithMask.ATTACH.getQuality()) == String.valueOf(true)
		lib19Attr.get(PermissionWithMask.MANAGEMENT.getQuality()) == null
	}

	@DataSet("CampaignWorkspaceDisplayService.sandbox.xml")
	def "should find wizards for campaign library"() {
		given:
		def jsTreeNodes = initNoWizardJsTreeNodes()

		when:
		campaignWorkspaceDisplayService.findWizards([-14L, -15L, -16L, -19L, -21L], jsTreeNodes)

		then:
		jsTreeNodes.size() == 4
		jsTreeNodes.get(-14L).getAttr().get("wizards") == ["RedmineReq"] as Set
		jsTreeNodes.get(-15L).getAttr().get("wizards") == ["RedmineReq", "JiraForSquash"] as Set
		jsTreeNodes.get(-16L).getAttr().get("wizards") == ["JiraReq"] as Set
		jsTreeNodes.get(-19L).getAttr().get("wizards") == [] as Set
	}

	@DataSet("CampaignWorkspaceDisplayService.sandbox.xml")
	def "should find projects models"() {
		given:
		UserDto user = new UserDto("robert", -2L, [-100L, -300L], false)

		when:
		def jsonProjects = campaignWorkspaceDisplayService.findAllProjects([-14L, -15L, -16L, -19L, -21L], user)

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
		def customFieldBindingModels = customFieldBindings.get("CAMPAIGN")
		customFieldBindingModels.size() == 3
		customFieldBindingModels.collect { it.id }.sort() == [-3L, -2L, -1L]
		customFieldBindingModels.collect { it.customField.id }.sort() == [-3L, -2L, -1L]
		customFieldBindingModels.collect { it.customField.name }.sort() == ["Liste", "Liste 2", "Lot"]

		def jsonMilestones = jsonProject15.getMilestones()
		jsonMilestones.size() == 2
		jsonMilestones.collect { it.label }.sort() == ["Jalon 1", "Jalon 2"]
	}

	@DataSet("CampaignWorkspaceDisplayService.sandbox.xml")
	def "should build requirement libraries with all their children"() {

		given:

		UserDto currentUser = new UserDto("robert", -2L, [-100L, -300L], false)

		MultiMap expansionCandidates = new MultiValueMap();
		expansionCandidates.put("CampaignLibrary", -14L);
		expansionCandidates.put("CampaignFolder", -104L);
		expansionCandidates.put("Campaign", -105L);
		expansionCandidates.put("Iteration", -83);

		Set<Long> childrenIds = new HashSet<>();

		def readableProjectIds = [-14L, -15L, -16L, -19L, -21L]

		when:

		def libraryFatherChildrenMultiMap = campaignWorkspaceDisplayService.getLibraryFatherChildrenMultiMap(expansionCandidates, childrenIds, new HashSet<Long>(), -9000L)
		def libraryNodeFatherChildrenMultiMap = campaignWorkspaceDisplayService.getLibraryNodeFatherChildrenMultiMap(expansionCandidates, childrenIds, new HashSet<Long>(), -9000L)
		def libraryChildrenMap = campaignWorkspaceDisplayService.getLibraryChildrenMap(childrenIds, expansionCandidates, currentUser, new HashMap<Long, List<Long>>(), new ArrayList<Long>(), -9000L)
		def jsTreeNodes = campaignWorkspaceDisplayService.doFindLibraries(readableProjectIds, currentUser)
		campaignWorkspaceDisplayService.buildHierarchy(jsTreeNodes, libraryFatherChildrenMultiMap, libraryNodeFatherChildrenMultiMap, libraryChildrenMap, -9000L)

		then:

		libraryFatherChildrenMultiMap.size() == 1
		libraryFatherChildrenMultiMap.keySet() == [-14L] as Set
		libraryFatherChildrenMultiMap.get(-14L) == [-104L]

		libraryNodeFatherChildrenMultiMap.size() == 1
		libraryNodeFatherChildrenMultiMap.keySet() == [-104L] as Set
		libraryNodeFatherChildrenMultiMap.get(-104L) == [-105L]

		childrenIds.size() == 2
		childrenIds == [-104L, -105L] as Set

		libraryChildrenMap.keySet() == childrenIds as Set

		jsTreeNodes.size() == 2;
		jsTreeNodes.values().collect { it.getAttr().get("resId") }.sort() == [-15L, -14L]
		jsTreeNodes.values().collect { it.getTitle() }.sort() == ["Projet 1", "Test Project-1"]
		jsTreeNodes.values().collect { it.getState() }.sort() == ["closed", "open"]

		def List<JsTreeNode> libraryChildren = jsTreeNodes.get(-14L).getChildren();  //id -14 : Test Project11

		libraryChildren.size() == 1
		libraryChildren.collect { it.getAttr().get("resId") } == [-104L]
		libraryChildren.collect { it.getTitle() }.sort() == ["Folder Test 1"]
		libraryChildren.collect { it.getState() }.sort() == ["open"]

		def List<JsTreeNode> folderChildren = libraryChildren.get(0).getChildren();  //id -104 : Folder Test 1

		folderChildren.size() == 1
		folderChildren.collect { it.getAttr().get("resId") }.sort() == [-105L]
		folderChildren.collect { it.getTitle() }.sort() == ["Campaign Test 1"]
		folderChildren.collect { it.getState() }.sort() == ["open"]

		def List<JsTreeNode> campaignChildren = folderChildren.get(0).getChildren();  //id -105 : Campaign Test 1

		campaignChildren.size() == 1
		campaignChildren.collect { it.getAttr().get("resId") }.sort() == [-83L]
		campaignChildren.collect { it.getTitle() }.sort() == ["Iteration - 1"]
		campaignChildren.collect { it.getState() }.sort() == ["open"]

		def List<JsTreeNode> iterationChildren = campaignChildren.get(0).getChildren();  //id -83 : Iteration - 1

		iterationChildren.size() == 1
		iterationChildren.collect { it.getAttr().get("resId") }.sort() == [-2L]
		iterationChildren.collect { it.getTitle() }.sort() == ["Test Suite 1"]
		iterationChildren.collect { it.getState() }.sort() == ["leaf"]
	}

	@DataSet("CampaignWorkspaceDisplayService.sandbox.xml")
	def "should get a campaign library children"() {

		given:

		UserDto currentUser = new UserDto("robert", -2L, [-100L, -300L], false)


		when:

		def nodes = campaignWorkspaceDisplayService.getNodeContent(-14L, currentUser, "library", -9000L)

		then:

		nodes.size() == 1
		nodes.collect { it.getAttr().get("resId") } == [-104L]
		nodes.collect { it.getTitle() }.sort() == ["Folder Test 1"]
		nodes.collect { it.getState() }.sort() == ["closed"]
	}

	@DataSet("CampaignWorkspaceDisplayService.sandbox.xml")
	def "should get a campaign folder children"() {

		given:

		UserDto currentUser = new UserDto("robert", -2L, [-100L, -300L], false)
		CampaignFolder camp = Mock()
		Library lib = Mock()
		lib.id >> -15L
		camp.library >> lib
		campaignWorkspaceDisplayService.hibernateCampaignFolderDao.findById(-104L) >> camp

		when:

		def nodes = campaignWorkspaceDisplayService.getNodeContent(-104L, currentUser, "folder", -9000L)

		then:

		nodes.size() == 1
		nodes.collect { it.getAttr().get("resId") }.sort() == [-105L]
		nodes.collect { it.getTitle() }.sort() == ["Campaign Test 1"]
		nodes.collect { it.getState() }.sort() == ["closed"]
	}

	@DataSet("CampaignWorkspaceDisplayService.sandbox.xml")
	def "should get campaign children"() {

		given:

		UserDto currentUser = new UserDto("robert", -2L, [-100L, -300L], false)
		Campaign camp = Mock()
		Library lib = Mock()
		lib.id >> -15L
		camp.library >> lib
		campaignWorkspaceDisplayService.hibernateCampaignDao.findById(-105L) >> camp

		when:

		def nodes = campaignWorkspaceDisplayService.getCampaignNodeContent(-105L, currentUser, "Campaign")

		then:

		nodes.size() == 1
		nodes.collect { it.getAttr().get("resId") }.sort() == [-83L]
		nodes.collect { it.getTitle() }.sort() == ["Iteration - 1"]
		nodes.collect { it.getState() }.sort() == ["closed"]
	}

	@DataSet("CampaignWorkspaceDisplayService.sandbox.xml")
	def "should get iteration children"() {

		given:

		UserDto currentUser = new UserDto("robert", -2L, [-100L, -300L], false)
		Iteration iter = Mock()
		CampaignLibrary lib = Mock()
		lib.id >> -15L
		iter.campaignLibrary >> lib
		campaignWorkspaceDisplayService.hibernateIterationDao.findById(-83L) >> iter

		when:

		def nodes = campaignWorkspaceDisplayService.getCampaignNodeContent(-83L, currentUser, "Iteration")

		then:

		nodes.size() == 1
		nodes.collect { it.getAttr().get("resId") }.sort() == [-2L]
		nodes.collect { it.getTitle() }.sort() == ["Test Suite 1"]
		nodes.collect { it.getState() }.sort() == ["leaf"]
	}

}
