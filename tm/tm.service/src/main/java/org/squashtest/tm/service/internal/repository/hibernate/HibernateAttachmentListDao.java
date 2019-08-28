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
import org.squashtest.tm.domain.EntityType;
import org.squashtest.tm.domain.attachment.AttachmentList;
import org.squashtest.tm.domain.audit.AuditableMixin;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.domain.requirement.QRequirementVersion;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.QTestCase;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.jooq.domain.tables.ActionTestStep;
import org.squashtest.tm.jooq.domain.tables.CampaignLibraryNode;
import org.squashtest.tm.jooq.domain.tables.Execution;
import org.squashtest.tm.jooq.domain.tables.Iteration;
import org.squashtest.tm.jooq.domain.tables.Project;
import org.squashtest.tm.jooq.domain.tables.Resource;
import org.squashtest.tm.jooq.domain.tables.TestCaseLibraryNode;
import org.squashtest.tm.jooq.domain.tables.TestCaseSteps;
import org.squashtest.tm.jooq.domain.tables.TestSuite;
import org.squashtest.tm.service.internal.repository.AttachmentListDao;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.util.HashMap;
import java.util.Map;

import static org.jooq.impl.DSL.inline;


@Repository
public class HibernateAttachmentListDao implements AttachmentListDao {

	private static final Map<String, Class> ENTITY_CLASS_MAP;
	static {
		ENTITY_CLASS_MAP = new HashMap<>(7);
		ENTITY_CLASS_MAP.put(EntityType.PROJECT.toString(), GenericProject.class);
		ENTITY_CLASS_MAP.put(EntityType.REQUIREMENT_VERSION.toString(), RequirementVersion.class);
		ENTITY_CLASS_MAP.put(EntityType.TEST_CASE.toString(), TestCase.class);
		ENTITY_CLASS_MAP.put(EntityType.CAMPAIGN.toString(), Campaign.class);
		ENTITY_CLASS_MAP.put(EntityType.ITERATION.toString(), org.squashtest.tm.domain.campaign.Iteration.class);
		ENTITY_CLASS_MAP.put(EntityType.TEST_SUITE.toString(), org.squashtest.tm.domain.campaign.TestSuite.class);
		ENTITY_CLASS_MAP.put(EntityType.EXECUTION.toString(), org.squashtest.tm.domain.execution.Execution.class);
	}

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
	public AuditableMixin findAuditableAssociatedEntityIfExists(Long attachmentListId) {
		AuditableMixin auditable = null;

		Record2<String, Long> jooqRecord = getAssociatedEntityTypeAndId(attachmentListId);

		if(jooqRecord != null){
			String entityType = jooqRecord.get("entity_type", String.class);
			long entityId = jooqRecord.get("entity_id", Long.class);

			Class<?> entityClass = ENTITY_CLASS_MAP.get(entityType);
			if(entityClass != null){
				auditable = (AuditableMixin) entityManager.find(entityClass, entityId);
			}
		}
		return auditable;
	}

	private Record2<String, Long> getAssociatedEntityTypeAndId(Long attachmentListId){
		return DSL.select(inline(EntityType.TEST_CASE.toString()).as("entity_type"), TestCaseLibraryNode.TEST_CASE_LIBRARY_NODE.TCLN_ID.as("entity_id"))
			.from(TestCaseLibraryNode.TEST_CASE_LIBRARY_NODE)
			.where(TestCaseLibraryNode.TEST_CASE_LIBRARY_NODE.ATTACHMENT_LIST_ID.eq(attachmentListId))
			.union(
				DSL.select(inline(EntityType.TEST_CASE.toString()).as("entity_type"), TestCaseLibraryNode.TEST_CASE_LIBRARY_NODE.TCLN_ID.as("entity_id"))
					.from(TestCaseLibraryNode.TEST_CASE_LIBRARY_NODE)
					.innerJoin(TestCaseSteps.TEST_CASE_STEPS).on(TestCaseSteps.TEST_CASE_STEPS.TEST_CASE_ID.eq(TestCaseLibraryNode.TEST_CASE_LIBRARY_NODE.TCLN_ID))
					.innerJoin(ActionTestStep.ACTION_TEST_STEP).on(ActionTestStep.ACTION_TEST_STEP.TEST_STEP_ID.eq(TestCaseSteps.TEST_CASE_STEPS.STEP_ID))
					.where(ActionTestStep.ACTION_TEST_STEP.ATTACHMENT_LIST_ID.eq(attachmentListId))
			)
			.union(
				DSL.select(inline(EntityType.CAMPAIGN.toString()).as("entity_type"), CampaignLibraryNode.CAMPAIGN_LIBRARY_NODE.CLN_ID.as("entity_id"))
					.from(CampaignLibraryNode.CAMPAIGN_LIBRARY_NODE)
					.where(CampaignLibraryNode.CAMPAIGN_LIBRARY_NODE.ATTACHMENT_LIST_ID.eq(attachmentListId))
			)
			.union(
				DSL.select(inline(EntityType.REQUIREMENT_VERSION.toString()).as("entity_type"), Resource.RESOURCE.RES_ID.as("entity_id"))
					.from(Resource.RESOURCE)
					.where(Resource.RESOURCE.ATTACHMENT_LIST_ID.eq(attachmentListId))
			)
			.union(
				DSL.select(inline(EntityType.ITERATION.toString()).as("entity_type"), Iteration.ITERATION.ITERATION_ID.as("entity_id"))
					.from(Iteration.ITERATION)
					.where(Iteration.ITERATION.ATTACHMENT_LIST_ID.eq(attachmentListId))
			)
			.union(
				DSL.select(inline(EntityType.TEST_SUITE.toString()).as("entity_type"), TestSuite.TEST_SUITE.ID.as("entity_id"))
					.from(TestSuite.TEST_SUITE)
					.where(TestSuite.TEST_SUITE.ATTACHMENT_LIST_ID.eq(attachmentListId))
			)
			.union(
				DSL.select(inline(EntityType.PROJECT.toString()).as("entity_type"), Project.PROJECT.PROJECT_ID.as("entity_id"))
					.from(Project.PROJECT)
					.where(Project.PROJECT.ATTACHMENT_LIST_ID.eq(attachmentListId))
			)
			.union(
				DSL.select(inline(EntityType.EXECUTION.toString()).as("entity_type"), Execution.EXECUTION.EXECUTION_ID.as("entity_id"))
					.from(Execution.EXECUTION)
					.where(Execution.EXECUTION.ATTACHMENT_LIST_ID.eq(attachmentListId))
			).fetchOne();
	}

}
