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
package org.squashtest.tm.service.internal.audit

import org.springframework.transaction.annotation.Transactional
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.squashtest.tm.domain.campaign.Campaign
import org.squashtest.tm.domain.requirement.RequirementVersion
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.service.audit.AuditModificationService
import org.unitils.dbunit.annotation.DataSet
import spock.unitils.UnitilsSupport

import javax.inject.Inject
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

@UnitilsSupport
@Transactional
class AuditModificationServiceIT extends DbunitServiceSpecification {
	@Inject
	AuditModificationService auditModificationService

	@PersistenceContext
	EntityManager em

	@DataSet("AuditModificationServiceIT.xml")
	def "should update campaign related to an attachment list"(){

		when:
		auditModificationService.updateRelatedToAttachmentAuditableEntity(-3L)

		then:

		Campaign campaign = em.find(Campaign.class, -10L)
		String lastModifiedBy = campaign.getLastModifiedBy()
		Date lastModifiedOn = campaign.getLastModifiedOn()

		lastModifiedBy != null
		lastModifiedOn != null

	}

	@DataSet("AuditModificationServiceIT.xml")
	def "should update requirement version related to an attachment list"(){
		when:
		auditModificationService.updateRelatedToAttachmentAuditableEntity(-1L)

		then:

		RequirementVersion requirementVersion = em.find(RequirementVersion.class, -1L)
		String lastModifiedBy = requirementVersion.getLastModifiedBy()
		Date lastModifiedOn = requirementVersion.getLastModifiedOn()

		lastModifiedBy != null
		lastModifiedOn != null
	}

	@DataSet("AuditModificationServiceIT.xml")
	def "should update test case related to an attachment list"(){
		when:
		auditModificationService.updateRelatedToAttachmentAuditableEntity(-2L)

		then:

		TestCase testCase = em.find(TestCase.class, -100L)
		String lastModifiedBy = testCase.getLastModifiedBy()
		Date lastModifiedOn = testCase.getLastModifiedOn()

		lastModifiedBy != null
		lastModifiedOn != null
	}

	@DataSet("AuditModificationServiceIT.xml")
	def "should update requirement version related to an RequirementLink"(){
		given:
		RequirementVersion requirementVersion1 = em.find(RequirementVersion.class, -1L)
		RequirementVersion requirementVersion2 = em.find(RequirementVersion.class, -2L)

		when:
		auditModificationService.updateRelatedToRequirementLinkAuditableEntity([requirementVersion1, requirementVersion2])

		then:

		String lastModifiedBy1 = requirementVersion1.getLastModifiedBy()
		Date lastModifiedOn1 = requirementVersion1.getLastModifiedOn()

		lastModifiedBy1 != null
		lastModifiedOn1 != null

		String lastModifiedBy2 = requirementVersion2.getLastModifiedBy()
		Date lastModifiedOn2 = requirementVersion2.getLastModifiedOn()

		lastModifiedBy2 != null
		lastModifiedOn2 != null

	}

	@DataSet("AuditModificationServiceIT.xml")
	def "should update bound entity related to modified custom field"(){
		given:
		TestCase testCase = em.find(TestCase.class, -100L)

		when:
		auditModificationService.updateRelatedToCustomFieldAuditableEntity(testCase)

		then:

		String lastModifiedBy = testCase.getLastModifiedBy()
		Date lastModifiedOn = testCase.getLastModifiedOn()

		lastModifiedBy != null
		lastModifiedOn != null

	}
}
