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
package org.squashtest.tm.service.internal.advancedsearch;

import org.hibernate.*;
import org.hibernate.criterion.Restrictions;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.SearchFactory;
import org.hibernate.search.backend.impl.PostTransactionWorkQueueSynchronization;
import org.hibernate.search.backend.impl.PerTransactionWorker;
import org.hibernate.search.backend.impl.WorkQueue;
import org.hibernate.search.batchindexing.MassIndexerProgressMonitor;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.spi.SearchIntegrator;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.library.IndexModel;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.advancedsearch.IndexationService;
import org.squashtest.tm.service.configuration.ConfigurationService;
import org.squashtest.tm.service.internal.library.AdvancedSearchIndexingMonitor;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.PersistenceContext;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

@Service("squashtest.tm.service.IndexationService")
@Transactional
public class IndexationServiceImpl implements IndexationService {

	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(IndexationServiceImpl.class);

	@PersistenceContext
	private EntityManager em;

	@Inject
	private ConfigurationService configurationService;

	public static final String SQUASH_VERSION_KEY = "squashtest.tm.database.version";

	public static final String REQUIREMENT_INDEXING_DATE_KEY = "lastindexing.requirement.date";
	public static final String TESTCASE_INDEXING_DATE_KEY = "lastindexing.testcase.date";
	public static final String CAMPAIGN_INDEXING_DATE_KEY = "lastindexing.campaign.date";

	public static final String REQUIREMENT_INDEXING_VERSION_KEY = "lastindexing.requirement.version";
	public static final String TESTCASE_INDEXING_VERSION_KEY = "lastindexing.testcase.version";
	public static final String CAMPAIGN_INDEXING_VERSION_KEY = "lastindexing.campaign.version";

	private static final int BATCH_SIZE = 20;

	private static final int MASS_INDEX_BATCH_SIZE = 50;

	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");

	@Override
	public IndexModel findIndexModel() {
		IndexModel model = new IndexModel();
		model.setRequirementIndexDate(findIndexDate(REQUIREMENT_INDEXING_DATE_KEY));
		model.setTestCaseIndexDate(findIndexDate(TESTCASE_INDEXING_DATE_KEY));
		model.setCampaignIndexDate(findIndexDate(CAMPAIGN_INDEXING_DATE_KEY));
		model.setRequirementIndexVersion(configurationService.findConfiguration(REQUIREMENT_INDEXING_VERSION_KEY));
		model.setTestcaseIndexVersion(configurationService.findConfiguration(TESTCASE_INDEXING_VERSION_KEY));
		model.setCampaignIndexVersion(configurationService.findConfiguration(CAMPAIGN_INDEXING_VERSION_KEY));
		model.setCurrentSquashVersion(configurationService.findConfiguration(SQUASH_VERSION_KEY));
		return model;
	}

	private Date findIndexDate(String key) {
		String value = configurationService.findConfiguration(key);
		Date date = null;
		if (value != null) {
			try {
				date = dateFormat.parse(value);
			} catch (ParseException e) {

			}
		}
		return date;
	}

	@Override
	public Boolean isIndexedOnPreviousVersion() {
		String currentVersion = configurationService.findConfiguration(SQUASH_VERSION_KEY);
		String testcaseIndexVersion = configurationService.findConfiguration(TESTCASE_INDEXING_VERSION_KEY);
		String requirementIndexVersion = configurationService.findConfiguration(REQUIREMENT_INDEXING_VERSION_KEY);
		String campaignIndexVersion = configurationService.findConfiguration(CAMPAIGN_INDEXING_VERSION_KEY);

		boolean result = currentVersion.equals(requirementIndexVersion) && currentVersion.equals(testcaseIndexVersion)
			&& currentVersion.equals(campaignIndexVersion);

		return !result;
	}

	@Override
	public void reindexRequirementVersion(Long requirementVersionId) {
		reindexEntity(RequirementVersion.class, requirementVersionId);
	}

	@Override
	public void reindexRequirementVersions(List<RequirementVersion> requirementVersionList) {
		for (RequirementVersion requirementVersion : requirementVersionList) {
			reindexRequirementVersion(requirementVersion.getId());
		}
	}

	@Override
	public void reindexRequirementVersionsByIds(List<Long> requirementVersionsIds) {
		for (Long id : requirementVersionsIds) {
			reindexRequirementVersion(id);
		}

	}

	@Override
	public void reindexTestCase(Long testCaseId) {
		reindexEntity(TestCase.class, testCaseId);
	}

	@Override
	public void reindexTestCases(List<TestCase> testCaseList) {
		for (TestCase testcase : testCaseList) {
			reindexTestCase(testcase.getId());
		}
	}

	private void reindexEntity(Class<?> clazz, long id) {
		FullTextEntityManager ftem = Search.getFullTextEntityManager(em);
		ftem.index(ftem.getReference(clazz, id));
	}

	// index used in administration
	@Override
	public void indexAll() {
		indexEntities(TestCase.class, RequirementVersion.class, IterationTestPlanItem.class);
	}

	@Override
	public void indexRequirementVersions() {
		indexEntities(RequirementVersion.class);
	}

	@Override
	public void indexTestCases() {
		indexEntities(TestCase.class);
	}

	@Override
	public void indexIterationTestPlanItem() {
		indexEntities(IterationTestPlanItem.class);
	}

	private void indexEntities(Class<?>... T) {
		FullTextEntityManager ftem = Search.getFullTextEntityManager(em);
		MassIndexerProgressMonitor monitor = new AdvancedSearchIndexingMonitor(Arrays.asList(T),
			this.configurationService);
		ftem.createIndexer(T).purgeAllOnStart(true).threadsToLoadObjects(T.length).typesToIndexInParallel(T.length)
			.batchSizeToLoadObjects(MASS_INDEX_BATCH_SIZE).cacheMode(CacheMode.IGNORE).progressMonitor(monitor)
			.start();

	}

	@Override
	public void batchReindexTc(Collection<Long> tcIdsToIndex) {
		batchReindex(TestCase.class, tcIdsToIndex);
	}

	@Override
	public void batchReindexReqVersion(Collection<Long> reqVersionIdsToIndex) {
		batchReindex(RequirementVersion.class, reqVersionIdsToIndex);

	}

	@Override
	public void batchReindexItpi(Collection<Long> itpisIdsToIndex) {
		batchReindex(IterationTestPlanItem.class, itpisIdsToIndex);
	}

	// Batched versions
	private <T> void batchReindex(Class<T> entity, Collection<Long> ids) {
		if (!ids.isEmpty()) {
			FullTextEntityManager ftem = getFullTextSession();

			ScrollableResults scroll = getScrollableResults(ftem, entity, ids);
			doReindex(ftem, scroll);
		}
	}

	private void doReindex(FullTextEntityManager ftem, ScrollableResults scroll) {
		// update index going through the search results
		int batch = 0;
		while (scroll.next()) {
			ftem.index(scroll.get(0)); // indexing of a single entity

			if (++batch % BATCH_SIZE == 0) { // commit batch
				ftem.flushToIndexes();
				ftem.clear();
			}
		}
		// flush remaining item
		ftem.flushToIndexes();
		ftem.clear();
	}

	private ScrollableResults getScrollableResults(FullTextEntityManager ftem, Class<?> entity, Collection<Long> ids) {
		Criteria query = ftem.unwrap(FullTextSession.class).createCriteria(entity);
		query.add(Restrictions.in("id", ids));
		return query.scroll(ScrollMode.FORWARD_ONLY);
	}

	private FullTextEntityManager getFullTextSession() {
		FullTextEntityManager ftem = Search.getFullTextEntityManager(em);
		
		/*
		  We need to change the flush mode (not sure why, it's historically there) and we'd like to restore it
		  after indexation. So we store it, set to commit mode, then restore to whatever value it was.
		 */

		FlushModeType previous = ftem.getFlushMode();
		ftem.setFlushMode(FlushModeType.COMMIT);

		// Clear the lucene work queue to eliminate lazy init bug for batch processing.
		clearLuceneQueue(ftem);
		
		ftem.setFlushMode(previous);

		return ftem;
	}

	// BEWARE dark magic
	private void clearLuceneQueue(FullTextEntityManager em) {
		SearchFactory searchFactory = em.getSearchFactory();
		SearchIntegrator searchIntegrator = searchFactory.unwrap(SearchIntegrator.class);
		PerTransactionWorker worker = (PerTransactionWorker) searchIntegrator.getWorker();

		try {
			Field synchronizationPerTransactionField = PerTransactionWorker.class
				.getDeclaredField("synchronizationPerTransaction");

			synchronizationPerTransactionField.setAccessible(true);
			@SuppressWarnings("unchecked")
			ConcurrentMap<Object, PostTransactionWorkQueueSynchronization> synchronizationPerTransaction = (ConcurrentMap<Object, PostTransactionWorkQueueSynchronization>) synchronizationPerTransactionField
				.get(worker);

			// NdGRF : entityManager.getTransaction() is illegal + I dow't know which "black magic" we're using so we
			// rely on the same code as before JPA i.e. we get the Tx from the hibernate Session
			Transaction transaction = em.unwrap(Session.class).getTransaction();
			PostTransactionWorkQueueSynchronization txSync = synchronizationPerTransaction
				.get(transaction);

			Field queueField = PostTransactionWorkQueueSynchronization.class.getDeclaredField("queue");
			queueField.setAccessible(true);

			if (txSync != null) {
				WorkQueue queue = (WorkQueue) queueField.get(txSync);
				queue.clear();
			}

		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			LOGGER.debug("Error during indexing", e);
		}

	}

}
