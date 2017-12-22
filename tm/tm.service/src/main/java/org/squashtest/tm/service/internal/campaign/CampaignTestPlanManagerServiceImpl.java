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
package org.squashtest.tm.service.internal.campaign;

import static org.squashtest.tm.service.security.Authorizations.OR_HAS_ROLE_ADMIN;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.ObjectIdentityRetrievalStrategy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.foundation.collection.ColumnFiltering;
import org.squashtest.tm.core.foundation.collection.DelegatePagingAndMultiSorting;
import org.squashtest.tm.core.foundation.collection.MultiSorting;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.Paging;
import org.squashtest.tm.core.foundation.collection.PagingAndMultiSorting;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.PagingBackedPagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.Pagings;
import org.squashtest.tm.domain.IdentifiersOrderComparator;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.CampaignTestPlanItem;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.projectfilter.ProjectFilter;
import org.squashtest.tm.domain.testcase.Dataset;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.service.campaign.CampaignTestPlanManagerService;
import org.squashtest.tm.service.campaign.IndexedCampaignTestPlanItem;
import org.squashtest.tm.service.internal.library.LibrarySelectionStrategy;
import org.squashtest.tm.service.internal.repository.CampaignDao;
import org.squashtest.tm.service.internal.repository.CampaignTestPlanItemDao;
import org.squashtest.tm.service.internal.repository.DatasetDao;
import org.squashtest.tm.service.internal.repository.LibraryNodeDao;
import org.squashtest.tm.service.internal.repository.TestCaseDao;
import org.squashtest.tm.service.internal.repository.TestCaseLibraryDao;
import org.squashtest.tm.service.internal.repository.UserDao;
import org.squashtest.tm.service.internal.testcase.TestCaseNodeWalker;
import org.squashtest.tm.service.milestone.ActiveMilestoneHolder;
import org.squashtest.tm.service.project.ProjectFilterModificationService;
import org.squashtest.tm.service.security.acls.model.ObjectAclService;

import java.util.Optional;

@Service("squashtest.tm.service.CampaignTestPlanManagerService")
@Transactional
public class CampaignTestPlanManagerServiceImpl implements CampaignTestPlanManagerService {
	/**
	 * Permission string for reading returned object.
	 */
	private static final String CAN_READ_RETURNED_OBJECT = "hasPermission(returnObject, 'READ')" + OR_HAS_ROLE_ADMIN;

	/**
	 * Permission string for linking campaigns to TP / Users based on campaignId param.
	 */
	private static final String CAN_LINK_CAMPAIGN_BY_ID = "hasPermission(#campaignId, 'org.squashtest.tm.domain.campaign.Campaign', 'LINK')" + OR_HAS_ROLE_ADMIN;

	private static final String CAN_REORDER_TEST_PLAN	= "hasPermission(#campaignId, 'org.squashtest.tm.domain.campaign.Campaign', 'LINK')" + OR_HAS_ROLE_ADMIN;

	private static final String CAN_READ_TEST_PLAN	=	"hasPermission(#campaignId, 'org.squashtest.tm.domain.campaign.Campaign' ,'READ')" + OR_HAS_ROLE_ADMIN;

	@Inject
	private TestCaseLibraryDao testCaseLibraryDao;

	@Inject
	private CampaignDao campaignDao;

	@Inject
	private ProjectFilterModificationService projectFilterModificationService;

	@Inject
	@Qualifier("squashtest.tm.repository.TestCaseLibraryNodeDao")
	private LibraryNodeDao<TestCaseLibraryNode> testCaseLibraryNodeDao;

	@Inject
	private ObjectAclService aclService;

	@Inject
	private CampaignTestPlanItemDao campaignTestPlanItemDao;

	@Inject
	private UserDao userDao;

	@Inject
	private TestCaseDao testCaseDao;

	@Inject
	private DatasetDao datasetDao;


	@Inject
	@Qualifier("squashtest.core.security.ObjectIdentityRetrievalStrategy")
	private ObjectIdentityRetrievalStrategy objIdRetrievalStrategy;

	@Inject
	@Qualifier("squashtest.tm.service.TestCaseLibrarySelectionStrategy")
	private LibrarySelectionStrategy<TestCaseLibrary, TestCaseLibraryNode> libraryStrategy;

	@Inject
	private ActiveMilestoneHolder activeMilestoneHolder;


	public void setObjectIdentityRetrievalStrategy(ObjectIdentityRetrievalStrategy objectIdentityRetrievalStrategy) {
		this.objIdRetrievalStrategy = objectIdentityRetrievalStrategy;
	}

	@Override
	@Transactional(readOnly = true)
	@PostAuthorize(CAN_READ_RETURNED_OBJECT)
	public Campaign findCampaign(long campaignId) {
		return campaignDao.findById(campaignId);
	}

	@Override
	@Transactional(readOnly = true)
	@PostFilter("hasPermission(filterObject, 'READ')" + OR_HAS_ROLE_ADMIN)
	public List<TestCaseLibrary> findLinkableTestCaseLibraries() {
		ProjectFilter pf = projectFilterModificationService.findProjectFilterByUserLogin();
		return pf.getActivated() ? libraryStrategy.getSpecificLibraries(pf.getProjects()) : testCaseLibraryDao
				.findAll();

	}


	@Override
	@PreAuthorize(CAN_READ_TEST_PLAN)
	public PagedCollectionHolder<List<CampaignTestPlanItem>> findTestPlanByCampaignId(long campaignId,	PagingAndSorting filter) {
		List<CampaignTestPlanItem> tcs = campaignDao.findAllTestPlanByIdFiltered(campaignId, filter);
		long count = campaignDao.countTestPlanById(campaignId);
		return new PagingBackedPagedCollectionHolder<>(filter, count, tcs);
	}

	@Override
	@PreAuthorize(CAN_READ_TEST_PLAN)
	public PagedCollectionHolder<List<IndexedCampaignTestPlanItem>> findTestPlan(long campaignId, PagingAndMultiSorting sorting, ColumnFiltering filtering) {

		List<IndexedCampaignTestPlanItem> indexedItems = campaignDao.findFilteredIndexedTestPlan(campaignId, sorting, filtering);
		long testPlanSize = campaignDao.countFilteredTestPlanById(campaignId, filtering);

		return new PagingBackedPagedCollectionHolder<>(sorting, testPlanSize, indexedItems);
	}


	@Override
	@PreAuthorize(CAN_LINK_CAMPAIGN_BY_ID)
	public void addTestCasesToCampaignTestPlan(final List<Long> testCasesIds, long campaignId) {
		// nodes are returned unsorted
		List<TestCaseLibraryNode> nodes = testCaseLibraryNodeDao.findAllByIds(testCasesIds);

		// now we resort them according to the order in which the testcaseids were given
		IdentifiersOrderComparator comparator = new IdentifiersOrderComparator(testCasesIds);
		Collections.sort(nodes, comparator);

		List<TestCase> testCases = new TestCaseNodeWalker().walk(nodes);

		final Optional<Milestone> activeMilestone = activeMilestoneHolder.getActiveMilestone();

		if (activeMilestone.isPresent()) {
		CollectionUtils.filter(testCases, new Predicate() {

			@Override
			public boolean evaluate(Object tc) {
				return ((TestCase) tc).getAllMilestones().contains(activeMilestone.get());
			}
		});
		}

		Campaign campaign = campaignDao.findById(campaignId);

		/*
		 * Feat 3700 campaign test plans are now populated the same way than iteration
		 * are
		 */
		for (TestCase testCase : testCases) {

			Set<Dataset> datasets = testCase.getDatasets();

			if (datasets.isEmpty()){
				CampaignTestPlanItem itp = new CampaignTestPlanItem(testCase);
				campaignTestPlanItemDao.persist(itp);
				campaign.addToTestPlan(itp);
			} else {
				for (Dataset ds : datasets){
					CampaignTestPlanItem itp = new CampaignTestPlanItem(testCase, ds);
					campaignTestPlanItemDao.persist(itp);
					campaign.addToTestPlan(itp);
				}
			}
		}
	}


	@Override
	@PreAuthorize(CAN_LINK_CAMPAIGN_BY_ID)
	public void addTestCaseToCampaignTestPlan(Long testCaseId, Long datasetId, long campaignId) {
		Campaign campaign = campaignDao.findById(campaignId);

		TestCase testCase = testCaseDao.findById(testCaseId);

		Dataset ds = datasetId!=null ? datasetDao.findById(datasetId) : null;

		CampaignTestPlanItem itp = new CampaignTestPlanItem(testCase, ds);
		campaignTestPlanItemDao.persist(itp);
		campaign.addToTestPlan(itp);
	}


	@Override
	@Transactional(readOnly = true)
	public List<User> findAssignableUserForTestPlan(long campaignId) {

		Campaign campaign = campaignDao.findById(campaignId);

		List<ObjectIdentity> entityRefs = new ArrayList<>();
		ObjectIdentity oid = objIdRetrievalStrategy.getObjectIdentity(campaign);
		entityRefs.add(oid);

		List<String> loginList = aclService.findUsersWithExecutePermission(entityRefs);
		return userDao.findUsersByLoginList(loginList);
	}

	/**
	 * @see CampaignTestPlanManagerService#assignUserToTestPlanItem(Long, long, Long)
	 * @param campaignId
	 *            not necessary but actually used for security check
	 */
	@Override
	@PreAuthorize(CAN_LINK_CAMPAIGN_BY_ID)
	public void assignUserToTestPlanItem(long itemId, long campaignId, long userId) {
		User assignee = null;
		if (userId != 0) {
			assignee = userDao.findOne(userId);
		}

		CampaignTestPlanItem item = campaignTestPlanItemDao.findById(itemId);
		item.setUser(assignee);
	}

	/**
	 * @see CampaignTestPlanManagerService#assignUserToTestPlanItem(Long, long, Long)
	 * @param campaignId
	 *            not necessary but actually used for security check
	 */
	@Override
	@PreAuthorize(CAN_LINK_CAMPAIGN_BY_ID)
	public void assignUserToTestPlanItems(@NotNull List<Long> itemsIds, long campaignId, long userId) {
		User assignee = null;
		if (userId != 0) {
			assignee = userDao.findOne(userId);
		}

		List<CampaignTestPlanItem> items = campaignTestPlanItemDao.findAllByIds(itemsIds);

		for (CampaignTestPlanItem item : items) {
			item.setUser(assignee);
		}
	}

	/**
	 * @see org.squashtest.tm.service.campaign.CampaignTestPlanManagerService#moveTestPlanItems(long, int, java.util.List)
	 */
	@Override
	@PreAuthorize(CAN_LINK_CAMPAIGN_BY_ID)
	public void moveTestPlanItems(long campaignId, int targetIndex, List<Long> itemIds) {
		Campaign campaign = campaignDao.findById(campaignId);
		campaign.moveTestPlanItems(targetIndex, itemIds);
	}

	@Override
	@PreAuthorize(CAN_REORDER_TEST_PLAN)
	public void reorderTestPlan(long campaignId, MultiSorting newSorting) {
		Paging noPaging = Pagings.NO_PAGING;
		PagingAndMultiSorting sorting = new DelegatePagingAndMultiSorting(noPaging, newSorting);

		List<CampaignTestPlanItem> items = campaignDao.findTestPlan(campaignId, sorting);

		Campaign campaign = campaignDao.findById(campaignId);

		campaign.getTestPlan().clear();
		campaign.getTestPlan().addAll(items);
	}


	/**
	 * @see org.squashtest.tm.service.campaign.CampaignTestPlanManagerService#removeTestPlanItem(long, long)
	 */
	@Override
	@PreAuthorize(CAN_LINK_CAMPAIGN_BY_ID)
	public void removeTestPlanItem(long campaignId, long itemId) {
		Campaign campaign = campaignDao.findById(campaignId);
		campaign.removeTestPlanItem(itemId);
	}

	/**
	 * @see org.squashtest.tm.service.campaign.CampaignTestPlanManagerService#removeTestPlanItems(long, java.util.List)
	 */
	@Override
	@PreAuthorize(CAN_LINK_CAMPAIGN_BY_ID)
	public void removeTestPlanItems(long campaignId, List<Long> itemIds) {
		Campaign campaign = campaignDao.findById(campaignId);
		campaign.removeTestPlanItems(itemIds);
	}

	/**
	 * @see org.squashtest.tm.service.campaign.CampaignTestPlanManagerService#findById(long)
	 */
	@Override
	@Transactional(readOnly = true)
	@PostAuthorize(CAN_READ_RETURNED_OBJECT)
	public CampaignTestPlanItem findById(long itemId) {
		return campaignTestPlanItemDao.findById(itemId);
	}

	@Override
	@PreAuthorize(CAN_READ_TEST_PLAN)
	@Transactional(readOnly = true)
	public List<Long> findPlannedTestCasesIds(Long campaignId) {
		return campaignTestPlanItemDao.findPlannedTestCasesIdsByCampaignId(campaignId);
	}


	@Override
	@PreAuthorize("hasPermission(#itemId, 'org.squashtest.tm.domain.campaign.CampaignTestPlanItem', 'WRITE')" + OR_HAS_ROLE_ADMIN)
	public void changeDataset(long itemId, Long datasetId) {
		CampaignTestPlanItem item = campaignTestPlanItemDao.findById(itemId);

		if (datasetId == null){
			item.setReferencedDataset(null);
		}
		else{
			TestCase tc = item.getReferencedTestCase();
			Dataset ds = datasetDao.findById(datasetId);
			if (! ds.getTestCase().equals(tc)){
				throw new IllegalArgumentException("dataset [id:'"+ds.getId()+"', name:'"+ds.getName()+
						"'] doesn't belong to test case [id:'"+tc.getId()+"', name:'"+tc.getName()+"']");
			}
			item.setReferencedDataset(ds);
		}
	}


}
