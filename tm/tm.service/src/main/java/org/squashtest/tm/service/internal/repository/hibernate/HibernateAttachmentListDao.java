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
package org.squashtest.tm.service.internal.repository.hibernate;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.jooq.DSLContext;
import org.jooq.Record2;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.domain.attachment.AttachmentList;
import org.squashtest.tm.domain.requirement.QRequirementVersion;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.QTestCase;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.jooq.domain.tables.CampaignLibraryNode;
import org.squashtest.tm.jooq.domain.tables.Iteration;
import org.squashtest.tm.jooq.domain.tables.Resource;
import org.squashtest.tm.jooq.domain.tables.TestCaseLibraryNode;
import org.squashtest.tm.jooq.domain.tables.TestSuite;
import org.squashtest.tm.service.internal.repository.AttachmentListDao;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.jooq.impl.DSL.inline;


@Repository
public class HibernateAttachmentListDao implements AttachmentListDao {
	@PersistenceContext
	private EntityManager entityManager;

	@Inject
	private DSLContext DSL;

	@Override
	public AttachmentList getOne(Long id) {
		return entityManager.getReference(AttachmentList.class, id);
	}

	@Override
	public TestCase findAssociatedTestCaseIfExists(Long attachmentListId) {
		final QTestCase testCase = QTestCase.testCase;

		return new JPAQueryFactory(entityManager)
			.selectFrom(testCase)
			.where(testCase.attachmentList.id.eq(attachmentListId))
			.fetchOne();
	}

	@Override
	public RequirementVersion findAssociatedRequirementVersionIfExists(Long attachmentListId) {
		final QRequirementVersion req = QRequirementVersion.requirementVersion;

		return new JPAQueryFactory(entityManager)
			.selectFrom(req)
			.where(req.attachmentList.id.eq(attachmentListId))
			.fetchOne();
	}

	@Override
	public Record2<String, Long> findAuditableAssociatedEntityIfExists(Long attachmentListId) {
		Record2<String, Long> result = DSL.select(inline("test_case").as("entity_name"), TestCaseLibraryNode.TEST_CASE_LIBRARY_NODE.TCLN_ID.as("entity_id"))
			.from(TestCaseLibraryNode.TEST_CASE_LIBRARY_NODE)
			.where(TestCaseLibraryNode.TEST_CASE_LIBRARY_NODE.ATTACHMENT_LIST_ID.eq(attachmentListId))
			.union(
				DSL.select(inline("campaign").as("entity_name"), CampaignLibraryNode.CAMPAIGN_LIBRARY_NODE.CLN_ID.as("entity_id"))
					.from(CampaignLibraryNode.CAMPAIGN_LIBRARY_NODE)
					.where(CampaignLibraryNode.CAMPAIGN_LIBRARY_NODE.ATTACHMENT_LIST_ID.eq(attachmentListId))
			)
			.union(
				DSL.select(inline("requirement_version").as("entity_name"), Resource.RESOURCE.RES_ID.as("entity_id"))
					.from(Resource.RESOURCE)
					.where(Resource.RESOURCE.ATTACHMENT_LIST_ID.eq(attachmentListId))
			)
			.union(
				DSL.select(inline("iteration").as("entity_name"), Iteration.ITERATION.ITERATION_ID.as("entity_id"))
					.from(Iteration.ITERATION)
					.where(Iteration.ITERATION.ATTACHMENT_LIST_ID.eq(attachmentListId))
			)
			.union(
				DSL.select(inline("test_suite").as("entity_name"), TestSuite.TEST_SUITE.ID.as("entity_id"))
					.from(TestSuite.TEST_SUITE)
					.where(TestSuite.TEST_SUITE.ATTACHMENT_LIST_ID.eq(attachmentListId))
			).fetchOne();
		return result;
	}

}
