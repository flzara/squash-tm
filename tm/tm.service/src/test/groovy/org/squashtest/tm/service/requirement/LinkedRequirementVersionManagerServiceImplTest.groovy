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
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder
import org.squashtest.tm.core.foundation.collection.PagingAndSorting
import org.squashtest.tm.domain.requirement.*
import org.squashtest.tm.exception.requirement.link.AlreadyLinkedRequirementVersionException
import org.squashtest.tm.exception.requirement.link.LinkedRequirementVersionException
import org.squashtest.tm.exception.requirement.link.SameRequirementLinkedRequirementVersionException
import org.squashtest.tm.exception.requirement.link.UnlinkableLinkedRequirementVersionException
import org.squashtest.tm.service.internal.milestone.ActiveMilestoneHolderImpl
import org.squashtest.tm.service.internal.repository.LibraryNodeDao
import org.squashtest.tm.service.internal.repository.RequirementVersionDao
import org.squashtest.tm.service.internal.repository.RequirementVersionLinkDao
import org.squashtest.tm.service.internal.repository.RequirementVersionLinkTypeDao
import org.squashtest.tm.service.internal.requirement.LinkedRequirementVersionManagerServiceImpl
import org.squashtest.tm.service.internal.requirement.RequirementNodeWalker
import org.squashtest.tm.service.milestone.ActiveMilestoneHolder
import spock.lang.Specification

/**
 * Created by jlor on 07/11/2017.
 */
class LinkedRequirementVersionManagerServiceImplTest extends Specification {

	LinkedRequirementVersionManagerService service =
		new LinkedRequirementVersionManagerServiceImpl()

	RequirementVersionLinkDao reqVersionLinkDao = Mock()

	LibraryNodeDao<RequirementLibraryNode> requirementLibraryNodeDao = Mock()

	ActiveMilestoneHolder activeMilestoneHolder = Mock()

	RequirementVersionDao reqVersionDao = Mock()

	RequirementVersionLinkTypeDao reqVersionLinkTypeDao = Mock()

	def setup() {
		service.reqVersionLinkDao = reqVersionLinkDao
		service.requirementLibraryNodeDao = requirementLibraryNodeDao
		service.activeMilestoneHolder = activeMilestoneHolder
		service.reqVersionDao = reqVersionDao
		service.reqVersionLinkTypeDao = reqVersionLinkTypeDao
	}

	def "#findAllByRequirementVersion"() {

		given: "Input parameters"
			long requirementId = 432L
			PagingAndSorting pas = Mock()

		and: "Mock dao data"
			RequirementVersionLink link1 = Mock()
			LinkedRequirementVersion lrv1 = Mock()
			link1.getRelatedLinkedRequirementVersion() >> lrv1

			RequirementVersionLink link2 = Mock()
			LinkedRequirementVersion lrv2 = Mock()
			link2.getRelatedLinkedRequirementVersion() >> lrv2

			RequirementVersionLink link3 = Mock()
			LinkedRequirementVersion lrv3 = Mock()
			link3.getRelatedLinkedRequirementVersion() >> lrv3

			List<RequirementVersionLink> linksList = [link1, link2, link3] as List

		and: "Mock dao method"
			reqVersionLinkDao.findAllByReqVersionId(requirementId, pas) >> linksList

		and: "Expected result data"
			List<LinkedRequirementVersion> expectedList = [lrv1, lrv2, lrv3] as List

		when:
			PagedCollectionHolder result = service.findAllByRequirementVersion(432, pas)

		then:
			result.getTotalNumberOfItems() == expectedList.size()
			result.getPagedItems().containsAll(expectedList)
	}

	def "#removeLinkedRequirementVersionsFromRequirementVersion"() {

		given: "Input data"
			long reqVerId = 432L
			List<Long> reqVerIdsToUnlink = [987L, 654L, 321] as List

		when:
			service.removeLinkedRequirementVersionsFromRequirementVersion(reqVerId, reqVerIdsToUnlink)

		then:
			1*reqVersionLinkDao.deleteAllLinks(reqVerId, reqVerIdsToUnlink)

	}

	def "#addLinkedReqVersionsToReqVersion: Should throw 3 exceptions and add 1 link."() {

		given: "Input data"
			long mainRvId = 321L
			long mainReqId = 1L

			long rv1Id = 123L
			long req1Id = 2L

			long rv2Id = 456L
			long req2Id = 3L

			long rv3Id = 789L
			long req3Id = mainReqId

			long rv4Id = 951L
			long req4Id = 5L

			List<Long> reqVerIdsToLink = [rv1Id, rv2Id, rv3Id, rv4Id]

		and: "Mock dao data"

			RequirementVersion mainRv = Mock(RequirementVersion.class)
			mainRv.getId() >> mainRvId
			Requirement mainReq = Mock(Requirement.class)
			mainReq.getId() >> mainReqId
			mainRv.isLinkable() >> true
			mainRv.getRequirement() >> mainReq

			RequirementVersion rv1 = Mock(RequirementVersion.class)
			rv1.getId() >> rv1Id
			Requirement req1 = Mock(Requirement.class)
			req1.getId() > req1Id
			req1.getResource() >> rv1
			rv1.isLinkable() >> true
			rv1.getRequirement() >> req1
			req1.accept(_) >> { RequirementNodeWalker visitor ->
				visitor.visit(req1)
			}

			RequirementVersion rv2 = Mock(RequirementVersion.class)
			rv2.getId() >> rv2Id
			Requirement req2 = Mock(Requirement.class)
			req2.getId() >> req2Id
			req2.getResource() >> rv2
			rv2.isLinkable() >> true
			rv2.getRequirement() >> req2
			req2.accept(_) >> { RequirementNodeWalker visitor ->
				visitor.visit(req2)
			}

			RequirementVersion rv3 = Mock(RequirementVersion.class)
			rv3.getId() >> rv3Id
			Requirement req3 = Mock(Requirement.class)
			req3.getId() >> req3Id
			req3.getResource() >> rv3
			rv3.isLinkable() >> true
			rv3.getRequirement() >> req3
			req3.accept(_) >> { RequirementNodeWalker visitor ->
				visitor.visit(req3)
			}

			RequirementVersion rv4 = Mock(RequirementVersion.class)
			rv4.getId() >> rv4Id
			Requirement req4 = Mock(Requirement.class)
			req4.getId() >> req4Id
			req4.getResource() >> rv4
			rv4.isLinkable() >> false
			rv4.getRequirement() >> req4
			req4.accept(_) >> { RequirementNodeWalker visitor ->
				visitor.visit(req4)
			}

			List<LibraryNodeDao> reqList = [req1, req2, req3, req4] as List

			RequirementVersionLinkType defaultType = Mock()

		and: "Mock dao methods"
			requirementLibraryNodeDao.findAllByIds(reqVerIdsToLink) >> reqList
			reqVersionDao.findOne(_) >> mainRv
			reqVersionLinkDao.linkAlreadyExists(mainRvId, _) >> { args ->
				if(args[1] == rv1Id) true
				else false
			}
			reqVersionLinkTypeDao.getDefaultRequirementVersionLinkType() >> defaultType

		and: "Mock service method"
			activeMilestoneHolder.getActiveMilestone() >> Optional.ofNullable(null)

		when:
			def result = service.addLinkedReqVersionsToReqVersion(mainRvId, reqVerIdsToLink)

		then:
			1*reqVersionLinkDao.addLink(_)
			result.size() == 3
	}

}
