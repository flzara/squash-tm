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
package org.squashtest.tm.service.internal.repository.hibernate

import com.querydsl.jpa.impl.JPAQueryFactory
import org.squashtest.tm.domain.attachment.QAttachmentList

import javax.inject.Inject
import javax.persistence.Query

import org.spockframework.util.NotThreadSafe
import org.springframework.transaction.annotation.Transactional
import org.squashtest.tm.tools.unittest.assertions.CollectionAssertions
import org.squashtest.tm.tools.unittest.assertions.ListAssertions
import org.squashtest.it.basespecs.DbunitDaoSpecification
import org.squashtest.tm.service.internal.repository.AttachmentListDao
import org.squashtest.tm.service.internal.repository.RequirementDao
import org.squashtest.tm.service.internal.repository.RequirementDeletionDao
import org.squashtest.tm.service.internal.repository.RequirementVersionDao
import org.unitils.dbunit.annotation.DataSet

import spock.unitils.UnitilsSupport

@NotThreadSafe
@UnitilsSupport
@Transactional
class HibernateRequirementDeletionDaoIT extends DbunitDaoSpecification {
	@Inject
	RequirementVersionDao versionDao
	@Inject
	RequirementDao requirementDao
	@Inject
	RequirementDeletionDao deletionDao
	@Inject
	AttachmentListDao attachmentListDao

	def setup() {
		CollectionAssertions.declareContainsExactlyIds()
		CollectionAssertions.declareContainsExactly()
		ListAssertions.declareIdsEqual();
	}

	@DataSet("HibernateRequirementVersionDaoIT.should delete all requirements versions by requirement id.xml")
	def "should delete all requirements versions by requirement id"() {
		given:
		//we associate the current version to the requirements
		String sql = "update REQUIREMENT set current_version_id = :ver_id where rln_id = :id";
		Query query = em.createNativeQuery(sql);
		query.setParameter("id", 10);
		query.setParameter("ver_id", 15);
		query.executeUpdate();
		query.setParameter("id", 20);
		query.setParameter("ver_id", 25);
		query.executeUpdate();
		query.setParameter("id", 30);
		query.setParameter("ver_id", 30);
		query.executeUpdate();
		query.setParameter("id", 40);
		query.setParameter("ver_id", 40);
		query.executeUpdate();
		em.flush()

		when:
		//remove
		deletionDao.findRequirementAttachmentListIds([-10L, -30L]);

		deletionDao.removeFromVerifiedRequirementLists([-10L, -30L])

		deletionDao.deleteRequirementAuditEvents([-10L, -30L])

		deletionDao.removeEntities([-10L, -30L])
		em.flush()

		//then find all
		def resReqVers = versionDao.findAll([-10L, -11L, -12L, -13L, -14L, -15L, -20L, -21L, -22L, -23L, -24L, -25L, -30L, -40L])
		def resReq = requirementDao.findAllByIds([-10L, -20L, -30L, -40L])

		String sql_select_resource = "select res_id from RESOURCE where res_id in (-10, 11, -12, -13, -14, -15, -20, -21, -22, -23, -24, -25, -30, -40)";
		Query query_select_resource = em.createNativeQuery(sql_select_resource);
		def resources = query_select_resource.getResultList()

		def resAttachList = new JPAQueryFactory(em)
			.selectFrom(QAttachmentList.attachmentList)
			.where(QAttachmentList.attachmentList.id.in([-10L, -11L, -12L, -13L, -14L, -15L, -20L, -21L, -22L, -23L, -24L, -25L, -30L, -40L]))
			.fetch()

		String sql_select_librairy_node = "select rln_id from REQUIREMENT_LIBRARY_NODE where rln_id in (-10, -20, -30, -40)";
		Query query_select_librairy_node = em.createNativeQuery(sql_select_librairy_node);
		def resLibrairy_node = query_select_librairy_node.getResultList()

		String sql_select_test_case_verified_req_vers = "select verified_req_version_id from REQUIREMENT_VERSION_COVERAGE where verified_req_version_id in (-15, -25, -30, -40)";
		Query query_select_test_case_verified_req_vers = em.createNativeQuery(sql_select_test_case_verified_req_vers);
		def resTestCaseVerifiedReqVers = query_select_test_case_verified_req_vers.getResultList()

		String sql_select_requirement_audit_event = "select event_id from REQUIREMENT_AUDIT_EVENT where event_id in (-10, -11, -12, -13, -14, -15, -20, -21, -22, -23, -24, -25, -30, -40)";
		Query query_select_requirement_audit_event = em.createNativeQuery(sql_select_requirement_audit_event);
		def resRequirementAuditEvent = query_select_requirement_audit_event.getResultList()

		String sql_select_requirement_creation = "select event_id from REQUIREMENT_CREATION where event_id in (-10, -20, -30, -40)";
		Query query_select_requirement_creation = em.createNativeQuery(sql_select_requirement_creation);
		def resRequirementCreation = query_select_requirement_creation.getResultList()

		String sql_select_requirement_property_change = "select event_id from REQUIREMENT_PROPERTY_CHANGE where event_id in (-12, -13, -21, -22, -23)";
		Query query_select_requirement_property_change = em.createNativeQuery(sql_select_requirement_property_change);
		def resRequirementPropertyChange = query_select_requirement_property_change.getResultList()

		String sql_select_requirement_large_property_change = "select event_id from REQUIREMENT_LARGE_PROPERTY_CHANGE where event_id in (-11, -14, -15, -24, -25)";
		Query query_select_requirement_large_property_change = em.createNativeQuery(sql_select_requirement_large_property_change);
		def resRequirementLargePropertyChange = query_select_requirement_large_property_change.getResultList()

		//should remain only the elements not linked to the removed requirements
		then:
		resReqVers.containsExactlyIds([-20L, -21L, -22L, -23L, -24L, -25L, -40L])
		resReq.containsExactlyIds([-20L, -40L])
		resources.containsExactly([-20G, -21G, -22G, -23G, -24G, -25G, -40G])
		resAttachList.containsExactlyIds([-20L, -21L, -22L, -23L, -24L, -25L, -40L])
		resLibrairy_node.containsExactly([-20G, -40G])
		resTestCaseVerifiedReqVers.containsExactly([-25G, -40G])
		resRequirementAuditEvent.containsExactly([-20G, -21G, -22G, -23G, -24G, -25G, -40G])
		resRequirementCreation.containsExactly([-20G, -40G])
		resRequirementPropertyChange.containsExactly([-21G, -22G, -23G])
		resRequirementLargePropertyChange.containsExactly([-24G, -25G])
	}

}
