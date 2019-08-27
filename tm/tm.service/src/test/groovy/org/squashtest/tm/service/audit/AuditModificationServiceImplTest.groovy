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
package org.squashtest.tm.service.audit

import org.springframework.security.core.Authentication
import org.squashtest.tm.domain.audit.AuditableMixin
import org.squashtest.tm.domain.campaign.Campaign
import org.squashtest.tm.domain.customfield.BindableEntity
import org.squashtest.tm.domain.requirement.RequirementVersion
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.security.UserContextHolder
import org.squashtest.tm.service.internal.audit.AuditModificationServiceImpl
import org.squashtest.tm.service.internal.repository.AttachmentListDao
import spock.lang.Specification

/**
 * @author aguilhem
 */
class AuditModificationServiceImplTest extends Specification {

	AuditModificationServiceImpl service = new AuditModificationServiceImpl()

	AttachmentListDao attachmentListDao = Mock()

	Authentication authentication = Mock()

	def setup(){
		service.attachmentListDao = attachmentListDao
		UserContextHolder.context.authentication = authentication
		authentication.name >> "bruce dickinson"
	}

	def "should update an auditable"(){
		given:
		RequirementVersion auditable = Mock()

		when:
		service.updateAuditable((AuditableMixin)auditable)

		then:
		1*auditable.setLastModifiedBy('bruce dickinson')
		1*auditable.setLastModifiedOn(_ as Date)
	}

	def "should update campaign related to an attachment list"(){
		given:
		Campaign campaign = Mock()

		when:
		service.updateRelatedToAttachmentAuditableEntity(1L)

		then:
		1*attachmentListDao.findAuditableAssociatedEntityIfExists(1L) >> campaign
		1*campaign.setLastModifiedBy('bruce dickinson')
		1*campaign.setLastModifiedOn(_ as Date)
	}

	def "should update requirement version related to an attachment list"(){
		given:
		RequirementVersion requirementVersion = Mock()

		when:
		service.updateRelatedToAttachmentAuditableEntity(1L)

		then:
		1*attachmentListDao.findAuditableAssociatedEntityIfExists(1L) >> requirementVersion
		1*requirementVersion.setLastModifiedBy('bruce dickinson')
		1*requirementVersion.setLastModifiedOn(_ as Date)
	}

	def "should update test case related to an attachment list"(){
		given:
		TestCase testCase = Mock()

		when:
		service.updateRelatedToAttachmentAuditableEntity(1L)

		then:
		1*attachmentListDao.findAuditableAssociatedEntityIfExists(1L) >> testCase
		1*testCase.setLastModifiedBy('bruce dickinson')
		1*testCase.setLastModifiedOn(_ as Date)
	}

	def "should update requirement version related to an RequirementLink"(){
		given:
		RequirementVersion requirementVersion = Mock()
		RequirementVersion requirementVersion2 = Mock()

		when:
		service.updateRelatedToRequirementLinkAuditableEntity([requirementVersion, requirementVersion2])

		then:
		1*requirementVersion.setLastModifiedBy('bruce dickinson')
		1*requirementVersion.setLastModifiedOn(_ as Date)
		1*requirementVersion2.setLastModifiedBy('bruce dickinson')
		1*requirementVersion2.setLastModifiedOn(_ as Date)
	}

	def "should update bound entity related to modified custom field"(){
		given:
		RequirementVersion requirementVersion = Mock()

		when:
		service.updateRelatedToCustomFieldAuditableEntity(requirementVersion)

		then:
		1*requirementVersion.getBoundEntityType() >> BindableEntity.REQUIREMENT_VERSION
		1*requirementVersion.setLastModifiedBy('bruce dickinson')
		1*requirementVersion.setLastModifiedOn(_ as Date)
	}
}
