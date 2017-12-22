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
import org.hibernate.*;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.milestone.MilestoneHolder;
import org.squashtest.tm.domain.milestone.MilestoneStatus;
import org.squashtest.tm.domain.milestone.QMilestone;
import org.squashtest.tm.domain.requirement.QRequirementVersion;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.QTestCase;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.internal.repository.CustomMilestoneDao;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import static org.squashtest.tm.jooq.domain.Tables.ACL_CLASS;
import static org.squashtest.tm.jooq.domain.Tables.ACL_OBJECT_IDENTITY;
import static org.squashtest.tm.jooq.domain.Tables.ACL_RESPONSIBILITY_SCOPE_ENTRY;
import static org.squashtest.tm.jooq.domain.Tables.MILESTONE;
import static org.squashtest.tm.jooq.domain.Tables.MILESTONE_BINDING;

import java.util.*;

public class MilestoneDaoImpl implements CustomMilestoneDao {
	private static final String CAMPAIGN_ID = "campaignId";
	private static final String VERSION_ID = "versionId";
	private static final String VALID_STATUS = "validStatus";
	private static final String MILESTONE_IDS = "milestoneIds";
	private static final String MILESTONE_ID = "milestoneId";
	private static final String PROJECT_IDS = "projectIds";
	private static final String PROJECT_ID = "projectId";
	private static final Logger LOGGER = LoggerFactory.getLogger(MilestoneDaoImpl.class);
	private static final int BATCH_UPDATE_SIZE = 50;

	@PersistenceContext
	private EntityManager entityManager;
	
	@Inject
	private DSLContext DSL;
	

	@SuppressWarnings("unchecked")
	@Override
	public Collection<Milestone> findAssociableMilestonesForTestCase(long testCaseId) {
		Query query = entityManager.createNamedQuery("milestone.findAssociableMilestonesForTestCase");
		query.setParameter("testCaseId", testCaseId);
		query.setParameter(VALID_STATUS, MilestoneStatus.getAllStatusAllowingObjectBind());
		return query.getResultList();
	}
	
	

	@Override
	public List<Long> findAllMilestoneIds() {
		return DSL.selectDistinct(MILESTONE.MILESTONE_ID)
				.from(MILESTONE)
				.fetch(MILESTONE.MILESTONE_ID, Long.class);
	}



	@Override
	public List<Long> findMilestoneIdsForUsers(Collection<Long> partyIds) {
		
		
		return DSL.selectDistinct(MILESTONE_BINDING.MILESTONE_ID)
				.from(ACL_RESPONSIBILITY_SCOPE_ENTRY)
					.join(ACL_OBJECT_IDENTITY).on(ACL_OBJECT_IDENTITY.ID.eq(ACL_RESPONSIBILITY_SCOPE_ENTRY.OBJECT_IDENTITY_ID))
					.join(ACL_CLASS).on(ACL_CLASS.ID.eq(ACL_OBJECT_IDENTITY.CLASS_ID))
					.join(MILESTONE_BINDING).on(ACL_OBJECT_IDENTITY.IDENTITY.eq(MILESTONE_BINDING.PROJECT_ID))
				.where(ACL_RESPONSIBILITY_SCOPE_ENTRY.PARTY_ID.in(partyIds)
					.and(ACL_CLASS.CLASSNAME.eq("org.squashtest.tm.domain.project.Project")))
				.fetch(MILESTONE_BINDING.MILESTONE_ID, Long.class);
	}



	@SuppressWarnings("unchecked")
	@Override
	public Collection<Milestone> findAllMilestonesForTestCase(long testCaseId) {

		Set<Milestone> allMilestones = new HashSet<>();

		Query query1 = entityManager.createNamedQuery("milestone.findTestCaseMilestones");
		query1.setParameter("testCaseId", testCaseId);
		List<Milestone> ownMilestones = query1.getResultList();

		Query query2 = entityManager.createNamedQuery("milestone.findIndirectTestCaseMilestones");
		query2.setParameter("testCaseId", testCaseId);
		List<Milestone> indirectMilestones = query2.getResultList();

		allMilestones.addAll(ownMilestones);
		allMilestones.addAll(indirectMilestones);

		return allMilestones;

	}

	/*
	 * Note : for now the implementation for isTestCaseMilestoneDeletable and isTestCaseMilestoneModifiable is the same.
	 * That might change in the future. (non-Javadoc)
	 *
	 * @see org.squashtest.tm.service.internal.repository.MilestoneDao#isTestCaseMilestoneDeletable(long)
	 */
	@Override
	public boolean isTestCaseMilestoneDeletable(long testCaseId) {
		return doesTestCaseBelongToMilestonesWithStatus(testCaseId, MilestoneStatus.PLANNED, MilestoneStatus.LOCKED);
	}

	@Override
	public boolean isTestCaseMilestoneModifiable(long testCaseId) {
		return doesTestCaseBelongToMilestonesWithStatus(testCaseId, MilestoneStatus.PLANNED, MilestoneStatus.LOCKED);
	}

	private boolean doesTestCaseBelongToMilestonesWithStatus(long testCaseId, MilestoneStatus... statuses) {
		Query query = entityManager.createNamedQuery("testCase.findTestCasesWithMilestonesHavingStatuses");
		query.setParameter("testCaseIds", Collections.singletonList(testCaseId));
		query.setParameter("statuses", Arrays.asList(statuses));
		List<Long> ids = query.getResultList();
		return ids.contains(testCaseId);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<Milestone> findAssociableMilestonesForRequirementVersion(long versionId) {
		Query q = entityManager.createNamedQuery("milestone.findAssociableMilestonesForRequirementVersion");
		q.setParameter(VERSION_ID, versionId);
		q.setParameter(VALID_STATUS, MilestoneStatus.getAllStatusAllowingObjectBind());
		return q.getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<Milestone> findAssociableMilestonesForCampaign(long campaignId) {
		Query q = entityManager.createNamedQuery("milestone.findAssociableMilestonesForCampaign");
		q.setParameter(VALID_STATUS, MilestoneStatus.getAllStatusAllowingObjectBind());
		q.setParameter(CAMPAIGN_ID, campaignId);
		return q.getResultList();
	}

	// TODO : use HQL update
	@Override
	public void bindMilestoneToProjectTestCases(long projectId, long milestoneId) {
		Session session = entityManager.unwrap(Session.class);
		org.hibernate.Query query = session.getNamedQuery("BoundEntityDao.findAllTestCasesForProject");
		query.setParameter(PROJECT_ID, projectId);
		ScrollableResults tcs = scrollableResults(query);

		bindTestCases(milestoneId, tcs);
	}

	// TODO : use HQL update
	@Override
	public void bindMilestoneToProjectRequirementVersions(long projectId, long milestoneId) {
		Session session = entityManager.unwrap(Session.class);
		org.hibernate.Query query = session.getNamedQuery("milestone.findLastNonObsoleteReqVersionsForProject");
		query.setParameter(PROJECT_ID, projectId);
		ScrollableResults reqVersions = scrollableResults(query);

		bindRequirementVersions(milestoneId, reqVersions);

	}

	private void bindRequirementVersions(long milestoneId, ScrollableResults reqVersions) {
		Milestone milestone = entityManager.find(Milestone.class, milestoneId);
		int count = 0;
		while (reqVersions.next()) {
			RequirementVersion reqV = (RequirementVersion) reqVersions.get(0);
			milestone.bindRequirementVersion(reqV);
			if (++count % BATCH_UPDATE_SIZE == 0) {
				// flush a batch of updates and release memory:
				entityManager.flush();
				entityManager.clear();
				milestone = entityManager.find(Milestone.class, milestoneId);
			}
		}
	}

	@Override
	public void synchronizeRequirementVersions(long source, long target, List<Long> projectIds) {
		Session session = entityManager.unwrap(Session.class);
		org.hibernate.Query query = session.getNamedQuery("milestone.findAllRequirementVersionsForProjectAndMilestone");
		query.setParameterList(PROJECT_IDS, projectIds);
		query.setParameter(MILESTONE_ID, source);
		ScrollableResults reqVersions = scrollableResults(query);

		bindRequirementVersions(target, reqVersions);

	}

	@Override
	public void synchronizeTestCases(long source, long target, List<Long> projectIds) {
		Session session = entityManager.unwrap(Session.class);
		org.hibernate.Query query = session.getNamedQuery("milestone.findAllTestCasesForProjectAndMilestone");
		query.setParameterList(PROJECT_IDS, projectIds);
		query.setParameter(MILESTONE_ID, source);
		ScrollableResults tcs = scrollableResults(query);

		bindTestCases(target, tcs);
	}

	private void bindTestCases(long targetMilestone, ScrollableResults testCases) {
		Milestone milestone = entityManager.find(Milestone.class, targetMilestone);
		int count = 0;
		while (testCases.next()) {
			TestCase tc = (TestCase) testCases.get(0);
			milestone.bindTestCase(tc);
			if (++count % BATCH_UPDATE_SIZE == 0) {
				// flush a batch of updates and release memory:
				entityManager.flush();
				entityManager.clear();
				milestone = entityManager.find(Milestone.class, targetMilestone);
			}
		}
	}

	private ScrollableResults scrollableResults(org.hibernate.Query query) throws HibernateException {
		return query.setCacheMode(CacheMode.IGNORE).scroll(ScrollMode.FORWARD_ONLY);
	}

	/**
	 * @see org.squashtest.tm.service.internal.repository.MilestoneDao#performBatchUpdate(org.squashtest.tm.service.internal.repository.MilestoneDao.HolderConsumer)
	 */
	@Override
	public void performBatchUpdate(HolderConsumer consumer) {
		LOGGER.info("About to perform a Milestone Holder batch update");
		final String[] entities = {"TestCase", "RequirementVersion", "Campaign"};

		Session session = entityManager.unwrap(Session.class);

		for (String entity : entities) {
			LOGGER.info("About to fetch entities {}", entity);

			String namedQuery = entity + ".findAllWithMilestones";
			LOGGER.debug("Fetching bound entities with query named {}", namedQuery);

			ScrollableResults holders = scrollableResults(session.getNamedQuery(namedQuery));

			int count = 0;

			while (holders.next()) {
				MilestoneHolder holder = (MilestoneHolder) holders.get(0);
				consumer.consume(holder);
				if (++count % BATCH_UPDATE_SIZE == 0) {
					// flush a batch of updates and release memory:
					session.flush();
					session.clear();
				}
			}
		}

		LOGGER.info("Done with Milestone Holder batch update");
	}

	@Override
	public boolean isBoundToAtleastOneObject(long milestoneId) {
		Query query = entityManager.createNamedQuery("milestone.countBoundObject");
		query.setParameter(MILESTONE_ID, milestoneId);
		int count = (int) query.getSingleResult();
		return count != 0;
	}

	@Override
	public void unbindAllObjectsForProject(Long milestoneId, Long projectId) {
		List<Long> projectIds = new ArrayList<>();
		projectIds.add(projectId);
		unbindAllObjectsForProjects(milestoneId, projectIds);
	}

	@Override
	public void unbindAllObjectsForProjects(Long milestoneId, List<Long> projectIds) {
		final String[] entities = {"TestCases", "RequirementVersions", "Campaigns"};

		Session session = entityManager.unwrap(Session.class);

		for (String entity : entities) {
			LOGGER.info("About to fetch entities {}", entity);

			String namedQuery = "milestone.findAll" + entity + "ForProjectAndMilestone";
			LOGGER.debug("Fetching bound entities with query named {}", namedQuery);
			org.hibernate.Query query = session.getNamedQuery(namedQuery);
			query.setParameter(MILESTONE_ID, milestoneId);
			query.setParameterList(PROJECT_IDS, projectIds);

			ScrollableResults holders = scrollableResults(query);

			unbindFromMilestone(milestoneId, session, holders);
		}
	}

	private void unbindFromMilestone(Long milestoneId, Session session, ScrollableResults holders) {
		int count = 0;
		FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);

		while (holders.next()) {
			MilestoneHolder holder = (MilestoneHolder) holders.get(0);
			holder.unbindMilestone(milestoneId);
			if (++count % BATCH_UPDATE_SIZE == 0) {
				// flush a batch of updates and release memory:
				flushAndClearSession(session, fullTextEntityManager);
			}
		}
		// flush remaining items
		flushAndClearSession(session, fullTextEntityManager);
	}
	
	private void flushAndClearSession(Session session, FullTextEntityManager fullTextEntityManager) {
		session.flush();
		fullTextEntityManager.flushToIndexes();
		fullTextEntityManager.clear();
		
	}

	@Override
	public void unbindAllObjects(long milestoneId) {

		final String[] entities = {"TestCase", "RequirementVersion", "Campaign"};

		Session session = entityManager.unwrap(Session.class);

		for (String entity : entities) {
			LOGGER.info("About to fetch entities {}", entity);

			String namedQuery = entity + ".findAllBoundToMilestone";
			LOGGER.debug("Fetching bound entities with query named {}", namedQuery);
			org.hibernate.Query query = session.getNamedQuery(namedQuery);
			query.setParameter(MILESTONE_ID, milestoneId);
			ScrollableResults holders = scrollableResults(query);

			unbindFromMilestone(milestoneId, session, holders);
		}
	}

	@Override
	public boolean isMilestoneBoundToACampainInProjects(Long milestoneId, List<Long> projectIds) {
		Query query = entityManager.createNamedQuery("milestone.countCampaignsForProjectAndMilestone");
		query.setParameter(PROJECT_IDS, projectIds);
		query.setParameter(MILESTONE_ID, milestoneId);
		return (long) query.getSingleResult() > 0;
	}

	@Override
	public boolean isMilestoneBoundToOneObjectOfProject(Long milestoneId, Long projectId) {

		List<Long> projectIds = new ArrayList<>();
		projectIds.add(projectId);

		Query queryTc = entityManager.createNamedQuery("milestone.findAllTestCasesForProjectAndMilestone");
		queryTc.setParameter(PROJECT_IDS, projectIds);
		queryTc.setParameter(MILESTONE_ID, milestoneId);

		if (!queryTc.getResultList().isEmpty()) {
			return true; // return now so we don't do useless request
		}

		Query queryCamp = entityManager.createNamedQuery("milestone.findAllCampaignsForProjectAndMilestone");
		queryCamp.setParameter(PROJECT_IDS, projectIds);
		queryCamp.setParameter(MILESTONE_ID, milestoneId);
		if (!queryCamp.getResultList().isEmpty()) {
			return true;// return now so we don't do useless request
		}
		Query queryReq = entityManager.createNamedQuery("milestone.findAllRequirementVersionsForProjectAndMilestone");
		queryReq.setParameter(PROJECT_IDS, projectIds);
		queryReq.setParameter(MILESTONE_ID, milestoneId);
		return !queryReq.getResultList().isEmpty();

	}

	@Override
	public boolean isOneMilestoneAlreadyBindToAnotherRequirementVersion(List<Long> reqVIds, List<Long> milestoneIds) {
		if (reqVIds.isEmpty() || milestoneIds.isEmpty()) {
			return false;
		} else {
			Query query = entityManager.createNamedQuery("milestone.otherRequirementVersionBindToOneMilestone");
			query.setParameter("reqVIds", reqVIds);
			query.setParameter(MILESTONE_IDS, milestoneIds);
			return !query.getResultList().isEmpty();
		}
	}

	@Override
	public Collection<Long> findTestCaseIdsBoundToMilestones(Collection<Long> milestoneIds) {
		QTestCase tc = QTestCase.testCase;
		QMilestone ms = QMilestone.milestone;

		return new JPAQueryFactory(entityManager)
			.select(tc.id)
			.from(tc)
			.innerJoin(tc.milestones, ms)
			.where(ms.id.in(milestoneIds))
			.fetch();
	}

	@Override
	public Collection<Long> findRequirementVersionIdsBoundToMilestones(Collection<Long> milestoneIds) {
		QRequirementVersion v = QRequirementVersion.requirementVersion;
		QMilestone ms = QMilestone.milestone;

		return new JPAQueryFactory(entityManager)
			.select(v.id)
			.from(v)
			.innerJoin(v.milestones, ms)
			.where(ms.id.in(milestoneIds))
			.fetch();
	}

}
