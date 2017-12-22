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

import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.EntityReference;
import org.squashtest.tm.domain.EntityType;
import org.squashtest.tm.domain.campaign.*;
import org.squashtest.tm.domain.campaign.export.CampaignExportCSVModel;
import org.squashtest.tm.domain.customfield.RawValue;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.projectfilter.ProjectFilter;
import org.squashtest.tm.exception.DuplicateNameException;
import org.squashtest.tm.service.annotation.*;
import org.squashtest.tm.service.campaign.CampaignLibraryNavigationService;
import org.squashtest.tm.service.campaign.CampaignStatisticsService;
import org.squashtest.tm.service.campaign.IterationModificationService;
import org.squashtest.tm.service.deletion.OperationReport;
import org.squashtest.tm.service.deletion.SuppressionPreviewReport;
import org.squashtest.tm.service.internal.campaign.coercers.*;
import org.squashtest.tm.service.internal.campaign.export.CampaignExportCSVFullModelImpl;
import org.squashtest.tm.service.internal.campaign.export.CampaignExportCSVModelImpl;
import org.squashtest.tm.service.internal.campaign.export.SimpleCampaignExportCSVModelImpl;
import org.squashtest.tm.service.internal.campaign.export.WritableCampaignCSVModel;
import org.squashtest.tm.service.internal.library.AbstractLibraryNavigationService;
import org.squashtest.tm.service.internal.library.LibrarySelectionStrategy;
import org.squashtest.tm.service.internal.library.NodeDeletionHandler;
import org.squashtest.tm.service.internal.library.PasteStrategy;
import org.squashtest.tm.service.internal.repository.*;
import org.squashtest.tm.service.milestone.ActiveMilestoneHolder;
import org.squashtest.tm.service.milestone.MilestoneMembershipManager;
import org.squashtest.tm.service.project.ProjectFilterModificationService;
import org.squashtest.tm.service.security.SecurityCheckableObject;
import org.squashtest.tm.service.statistics.campaign.CampaignStatisticsBundle;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.*;

import static org.squashtest.tm.service.security.Authorizations.OR_HAS_ROLE_ADMIN;

@Service("squashtest.tm.service.CampaignLibraryNavigationService")
@Transactional
public class CampaignLibraryNavigationServiceImpl
	extends AbstractLibraryNavigationService<CampaignLibrary, CampaignFolder, CampaignLibraryNode>
	implements CampaignLibraryNavigationService {

	@Inject
	private CampaignLibraryDao campaignLibraryDao;

	@Inject
	private CampaignFolderDao campaignFolderDao;

	@Inject
	@Qualifier("squashtest.tm.repository.CampaignLibraryNodeDao")
	private LibraryNodeDao<CampaignLibraryNode> campaignLibraryNodeDao;

	@Inject
	private CampaignDao campaignDao;

	@Inject
	private IterationDao iterationDao;

	@Inject
	private TestSuiteDao suiteDao;

	@Inject
	private IterationModificationService iterationModificationService;

	@Inject
	private ProjectFilterModificationService projectFilterModificationService;

	@Inject
	private CampaignNodeDeletionHandler deletionHandler;

	@Inject
	private Provider<SimpleCampaignExportCSVModelImpl> simpleCampaignExportCSVModelProvider;

	@Inject
	private Provider<CampaignExportCSVModelImpl> standardCampaignExportCSVModelProvider;

	@Inject
	private Provider<CampaignExportCSVFullModelImpl> fullCampaignExportCSVModelProvider;

	@Inject
	private CampaignStatisticsService statisticsService;

	@Inject
	@Qualifier("squashtest.tm.service.CampaignLibrarySelectionStrategy")
	private LibrarySelectionStrategy<CampaignLibrary, CampaignLibraryNode> libraryStrategy;

	@Inject
	@Qualifier("squashtest.tm.service.internal.PasteToCampaignFolderStrategy")
	private Provider<PasteStrategy<CampaignFolder, CampaignLibraryNode>> pasteToCampaignFolderStrategyProvider;

	@Inject
	@Qualifier("squashtest.tm.service.internal.PasteToCampaignLibraryStrategy")
	private Provider<PasteStrategy<CampaignLibrary, CampaignLibraryNode>> pasteToCampaignLibraryStrategyProvider;

	@Inject
	@Qualifier("squashtest.tm.service.internal.PasteToCampaignStrategy")
	private Provider<PasteStrategy<Campaign, Iteration>> pasteToCampaignStrategyProvider;

	@Inject
	private MilestoneMembershipManager milestoneManager;

	@Inject
	private ActiveMilestoneHolder activeMilestoneHolder;

	@Override
	protected NodeDeletionHandler<CampaignLibraryNode, CampaignFolder> getDeletionHandler() {
		return deletionHandler;
	}

	@Override
	protected PasteStrategy<CampaignFolder, CampaignLibraryNode> getPasteToFolderStrategy() {
		return pasteToCampaignFolderStrategyProvider.get();
	}

	@Override
	protected PasteStrategy<CampaignLibrary, CampaignLibraryNode> getPasteToLibraryStrategy() {
		return pasteToCampaignLibraryStrategyProvider.get();
	}

	@Override
	@PreAuthorize("(hasPermission(#campaignId, 'org.squashtest.tm.domain.campaign.Campaign', 'CREATE')) "
		+ OR_HAS_ROLE_ADMIN)
	@PreventConcurrent(entityType = CampaignLibraryNode.class)
	public List<Iteration> copyIterationsToCampaign(@Id long campaignId, Long[] iterationsIds) {
		PasteStrategy<Campaign, Iteration> pasteStrategy = pasteToCampaignStrategyProvider.get();
		makeCopierStrategy(pasteStrategy);
		return pasteStrategy.pasteNodes(campaignId, Arrays.asList(iterationsIds));
	}

	@Override
	@PreventConcurrent(entityType = CampaignLibraryNode.class)
	@PreAuthorize("hasPermission(#campaignId, 'org.squashtest.tm.domain.campaign.Campaign', 'CREATE') "
		+ OR_HAS_ROLE_ADMIN)
	public int addIterationToCampaign(Iteration iteration, @Id long campaignId, boolean copyTestPlan) {
		Campaign campaign = campaignDao.findById(campaignId);

		if (!campaign.isContentNameAvailable(iteration.getName())) {
			throw new DuplicateNameException(iteration.getName(), iteration.getName());
		}
		return iterationModificationService.addIterationToCampaign(iteration, campaignId, copyTestPlan);
	}

	@Override
	@PreventConcurrent(entityType = CampaignLibraryNode.class)
	@PreAuthorize("hasPermission(#campaignId, 'org.squashtest.tm.domain.campaign.Campaign', 'CREATE') "
		+ OR_HAS_ROLE_ADMIN)
	public int addIterationToCampaign(Iteration iteration, @Id long campaignId, boolean copyTestPlan,
									  Map<Long, RawValue> customFieldValues) {
		int iterIndex = addIterationToCampaign(iteration, campaignId, copyTestPlan);
		initCustomFieldValues(iteration, customFieldValues);
		return iterIndex;
	}

	@Override
	@PreAuthorize("hasPermission(#destinationId, 'org.squashtest.tm.domain.campaign.Campaign', 'WRITE') "
		+ OR_HAS_ROLE_ADMIN)
	@PreventConcurrent(entityType = CampaignLibraryNode.class, paramName = "destinationId")
	public void moveIterationsWithinCampaign(@Id("destinationId") long destinationId, Long[] nodeIds, int position) {
		/*
		 * because : 1 - iteration is not a campaign library node 2 - an
		 * iteration will move only within the same campaign,
		 *
		 * we can't use the TreeNodeMover and we don't need it anyway.
		 */

		List<Long> iterationIds = Arrays.asList(nodeIds);

		Campaign c = campaignDao.findById(destinationId);
		List<Iteration> iterations = iterationDao.findAllByIds(iterationIds);

		c.moveIterations(position, iterations);

	}

	@Override
	protected final CampaignLibraryDao getLibraryDao() {
		return campaignLibraryDao;
	}

	@Override
	protected final CampaignFolderDao getFolderDao() {
		return campaignFolderDao;
	}

	@Override
	protected final LibraryNodeDao<CampaignLibraryNode> getLibraryNodeDao() {
		return campaignLibraryNodeDao;
	}

	/*
	 * refer to the comment in
	 * org.squashtest.csp.tm.internal.service.TestCaseModificationServiceImpl#
	 * findVerifiedRequirementsByTestCaseId
	 *
	 * (non-Javadoc)
	 *
	 * @see org.squashtest.csp.tm.service.CampaignLibraryNavigationService#
	 * findIterationsByCampaignId(long)
	 */
	@Override
	@PreAuthorize("hasPermission(#campaignId, 'org.squashtest.tm.domain.campaign.Campaign' , 'READ') "
		+ OR_HAS_ROLE_ADMIN)
	public List<Iteration> findIterationsByCampaignId(long campaignId) {
		return iterationModificationService.findIterationsByCampaignId(campaignId);
	}

	@Override
	@PreAuthorize("hasPermission(#libraryId, 'org.squashtest.tm.domain.campaign.CampaignLibrary', 'CREATE')"
		+ OR_HAS_ROLE_ADMIN)
	@PreventConcurrent(entityType = CampaignLibrary.class)
	public void addCampaignToCampaignLibrary(@Id long libraryId, Campaign newCampaign) {
		CampaignLibrary library = campaignLibraryDao.findById(libraryId);

		if (!library.isContentNameAvailable(newCampaign.getName())) {
			throw new DuplicateNameException(newCampaign.getName(), newCampaign.getName());
		} else {
			library.addContent(newCampaign);
			campaignDao.persist(newCampaign);
			createCustomFieldValues(newCampaign);
		}

	}

	@Override
	@PreAuthorize("hasPermission(#libraryId, 'org.squashtest.tm.domain.campaign.CampaignLibrary', 'CREATE')"
		+ OR_HAS_ROLE_ADMIN)
	@PreventConcurrent(entityType = CampaignLibrary.class)
	public void addCampaignToCampaignLibrary(@Id long libraryId, Campaign campaign,
											 Map<Long, RawValue> customFieldValues) {
		addCampaignToCampaignLibrary(libraryId, campaign);
		initCustomFieldValues(campaign, customFieldValues);
		Optional<Milestone> activeMilestone = activeMilestoneHolder.getActiveMilestone();
		if (activeMilestone.isPresent()) {
			milestoneManager.bindCampaignToMilestone(campaign.getId(), activeMilestone.get().getId());
		}
	}

	@Override
	@PreAuthorize("hasPermission(#folderId, 'org.squashtest.tm.domain.campaign.CampaignFolder', 'CREATE')"
		+ OR_HAS_ROLE_ADMIN)
	@PreventConcurrent(entityType = CampaignLibraryNode.class)
	public void addCampaignToCampaignFolder(@Id long folderId, Campaign newCampaign) {
		CampaignFolder folder = campaignFolderDao.findById(folderId);
		if (!folder.isContentNameAvailable(newCampaign.getName())) {
			throw new DuplicateNameException(newCampaign.getName(), newCampaign.getName());
		} else {
			folder.addContent(newCampaign);
			campaignDao.persist(newCampaign);
			createCustomFieldValues(newCampaign);
		}

	}

	@Override
	@PreAuthorize("hasPermission(#folderId, 'org.squashtest.tm.domain.campaign.CampaignFolder', 'CREATE')"
		+ OR_HAS_ROLE_ADMIN)
	@PreventConcurrent(entityType = CampaignLibraryNode.class)
	public void addCampaignToCampaignFolder(@Id long folderId, Campaign campaign,
											Map<Long, RawValue> customFieldValues) {

		addCampaignToCampaignFolder(folderId, campaign);
		initCustomFieldValues(campaign, customFieldValues);

		Optional<Milestone> activeMilestone = activeMilestoneHolder.getActiveMilestone();
		if (activeMilestone.isPresent()) {
			milestoneManager.bindCampaignToMilestone(campaign.getId(), activeMilestone.get().getId());
		}
	}

	@Override
	@PostFilter("hasPermission(filterObject, 'READ') " + OR_HAS_ROLE_ADMIN)
	public List<TestSuite> findIterationContent(long iterationId) {
		return suiteDao.findAllByIterationId(iterationId);
	}

	@Override
	public String getPathAsString(long entityId) {
		// get
		CampaignLibraryNode node = getLibraryNodeDao().findById(entityId);

		// check
		checkPermission(new SecurityCheckableObject(node, "READ"));

		// proceed
		List<String> names = getLibraryNodeDao().getParentsName(entityId);

		return "/" + node.getProject().getName() + "/" + formatPath(names);

	}

	private String formatPath(List<String> names) {
		StringBuilder builder = new StringBuilder();
		for (String name : names) {
			builder.append("/").append(name);
		}
		return builder.toString();
	}

	@Override
	@PostFilter("hasPermission(filterObject, 'READ') " + OR_HAS_ROLE_ADMIN)
	public List<CampaignLibrary> findLinkableCampaignLibraries() {
		ProjectFilter pf = projectFilterModificationService.findProjectFilterByUserLogin();

		return pf.getActivated() ? libraryStrategy.getSpecificLibraries(pf.getProjects())
			: campaignLibraryDao.findAll();
	}

	@Override
	public List<SuppressionPreviewReport> simulateIterationDeletion(List<Long> targetIds) {
		return deletionHandler.simulateIterationDeletion(targetIds);
	}

	@Override
	@BatchPreventConcurrent(entityType = CampaignLibraryNode.class, coercer = IterationToCampaignIdsCoercer.class)
	public OperationReport deleteIterations(@Ids List<Long> targetIds) {
		return deletionHandler.deleteIterations(targetIds);
	}

	@Override
	public List<SuppressionPreviewReport> simulateSuiteDeletion(List<Long> targetIds) {
		return deletionHandler.simulateSuiteDeletion(targetIds);
	}

	@Override
	@PreAuthorize("hasPermission(#campaignId, 'org.squashtest.tm.domain.campaign.Campaign' ,'EXPORT')"
		+ OR_HAS_ROLE_ADMIN)
	public CampaignExportCSVModel exportCampaignToCSV(Long campaignId, String exportType) {

		Campaign campaign = campaignDao.findById(campaignId);

		WritableCampaignCSVModel model;

		if ("L".equals(exportType)) {
			model = simpleCampaignExportCSVModelProvider.get();
		} else if ("F".equals(exportType)) {
			model = fullCampaignExportCSVModelProvider.get();
		} else {
			model = standardCampaignExportCSVModelProvider.get();
		}

		model.setCampaign(campaign);
		model.init();

		return model;
	}

	@Override
	// Only functions for campaigns and campaign folders
	// TODO make it work for iteration and test suites
	public List<String> getParentNodesAsStringList(EntityReference entityReference) {
		CampaignLibraryNode node;
		Long nodeId;

		if (entityReference.getType().equals(EntityType.CAMPAIGN)) {
			nodeId = entityReference.getId();
			node = campaignLibraryNodeDao.findById(nodeId);
		} else {
			nodeId = iterationDao.findById(entityReference.getId()).getCampaign().getId();
			node = campaignLibraryNodeDao.findById(nodeId);
		}

		List<String> parents = new ArrayList<>();

		if (node != null) {

			List<Long> ids = campaignLibraryNodeDao.getParentsIds(nodeId);

			Long librabryId = node.getLibrary().getId();

			parents.add("#CampaignLibrary-" + librabryId);

			if (entityReference.getType().equals(EntityType.ITERATION)) {
				parents.add("Campaign-" + nodeId);
			}

			if (ids.size() > 1) {
				for (int i = 0; i < ids.size() - 1; i++) {
					long currentId = ids.get(i);
					CampaignLibraryNode currentNode = campaignLibraryNodeDao.findById(currentId);
					parents.add(currentNode.getClass().getSimpleName() + "-" + currentId);
				}
			}
		}

		return parents;
	}

	@Override
	public List<Long> findAllCampaignIdsForMilestone(Milestone milestone) {
		return campaignDao.findAllIdsByMilestone(milestone.getId());
	}

	@Override
	public Collection<Long> findCampaignIdsFromSelection(Collection<Long> libraryIds, Collection<Long> nodeIds) {

		/*
		 * first, let's check the permissions on those root nodes By
		 * transitivity, if the user can read them then it will be allowed to
		 * read the campaigns below
		 */
		Collection<Long> readLibIds = securityFilterIds(libraryIds, CampaignLibrary.class.getName(), "READ");
		Collection<Long> readNodeIds = securityFilterIds(nodeIds, CampaignLibraryNode.class.getName(), "READ");

		// now we can collect the campaigns
		Set<Long> cIds = new HashSet<>();

		if (!readLibIds.isEmpty()) {
			cIds.addAll(campaignDao.findAllCampaignIdsByLibraries(readLibIds));
		}
		if (!readNodeIds.isEmpty()) {
			cIds.addAll(campaignDao.findAllCampaignIdsByNodeIds(readNodeIds));
		}

		return cIds;

	}

	@Override
	public CampaignStatisticsBundle gatherCampaignStatisticsBundleByMilestone() {
		return statisticsService.gatherMilestoneStatisticsBundle();
	}

	@Override
	@BatchPreventConcurrent(entityType = Iteration.class, coercer = TestSuiteToIterationCoercerForList.class)
	public OperationReport deleteSuites(@Ids List<Long> suiteIds, boolean removeFromIter) {

		return deletionHandler.deleteSuites(suiteIds, removeFromIter);
	}

	// ####################### PREVENT CONCURRENCY OVERIDES
	// ############################

	@Override
	@PreventConcurrent(entityType = CampaignLibraryNode.class)
	public void addFolderToFolder(@Id long destinationId, CampaignFolder newFolder) {
		super.addFolderToFolder(destinationId, newFolder);
	}

	@Override
	@PreventConcurrent(entityType = CampaignLibrary.class)
	public void addFolderToLibrary(@Id long destinationId, CampaignFolder newFolder) {
		super.addFolderToLibrary(destinationId, newFolder);
	}

	@Override
	@PreventConcurrents(simplesLocks = {
		@PreventConcurrent(entityType = CampaignLibraryNode.class, paramName = "destinationId")}, batchsLocks = {
		@BatchPreventConcurrent(entityType = CampaignLibrary.class, paramName = "sourceNodesIds", coercer = CampaignLibraryIdsCoercerForArray.class),
		@BatchPreventConcurrent(entityType = CampaignLibraryNode.class, paramName = "sourceNodesIds", coercer = CLNAndParentIdsCoercerForArray.class)})
	public List<CampaignLibraryNode> copyNodesToFolder(@Id("destinationId") long destinationId,
													   @Ids("sourceNodesIds") Long[] sourceNodesIds) {
		return super.copyNodesToFolder(destinationId, sourceNodesIds);
	}

	@Override
	@PreventConcurrents(simplesLocks = {
		@PreventConcurrent(entityType = CampaignLibrary.class, paramName = "destinationId")}, batchsLocks = {
		@BatchPreventConcurrent(entityType = CampaignLibrary.class, paramName = "targetId", coercer = CampaignLibraryIdsCoercerForArray.class),
		@BatchPreventConcurrent(entityType = CampaignLibraryNode.class, paramName = "targetId", coercer = CLNAndParentIdsCoercerForArray.class)})
	public List<CampaignLibraryNode> copyNodesToLibrary(@Id("destinationId") long destinationId,
														@Ids("targetId") Long[] targetIds) {
		return super.copyNodesToLibrary(destinationId, targetIds);
	}

	@Override
	@PreventConcurrents(simplesLocks = {
		@PreventConcurrent(entityType = CampaignLibraryNode.class, paramName = "destinationId")}, batchsLocks = {
		@BatchPreventConcurrent(entityType = CampaignLibrary.class, paramName = "targetId", coercer = CampaignLibraryIdsCoercerForArray.class),
		@BatchPreventConcurrent(entityType = CampaignLibraryNode.class, paramName = "targetId", coercer = CLNAndParentIdsCoercerForArray.class)})
	public void moveNodesToFolder(@Id("destinationId") long destinationId, @Ids("targetId") Long[] targetIds) {
		super.moveNodesToFolder(destinationId, targetIds);
	}

	@Override
	@PreventConcurrents(simplesLocks = {
		@PreventConcurrent(entityType = CampaignLibraryNode.class, paramName = "destinationId")}, batchsLocks = {
		@BatchPreventConcurrent(entityType = CampaignLibrary.class, paramName = "targetId", coercer = CampaignLibraryIdsCoercerForArray.class),
		@BatchPreventConcurrent(entityType = CampaignLibraryNode.class, paramName = "targetId", coercer = CLNAndParentIdsCoercerForArray.class)})
	public void moveNodesToFolder(@Id("destinationId") long destinationId, @Ids("targetId") Long[] targetIds,
								  int position) {
		super.moveNodesToFolder(destinationId, targetIds, position);
	}

	@Override
	@PreventConcurrents(simplesLocks = {
		@PreventConcurrent(entityType = CampaignLibrary.class, paramName = "destinationId")}, batchsLocks = {
		@BatchPreventConcurrent(entityType = CampaignLibrary.class, paramName = "targetId", coercer = CampaignLibraryIdsCoercerForArray.class),
		@BatchPreventConcurrent(entityType = CampaignLibraryNode.class, paramName = "targetId", coercer = CLNAndParentIdsCoercerForArray.class)})
	public void moveNodesToLibrary(@Id("destinationId") long destinationId, @Ids("targetId") Long[] targetIds) {
		super.moveNodesToLibrary(destinationId, targetIds);
	}

	@Override
	@PreventConcurrents(simplesLocks = {
		@PreventConcurrent(entityType = CampaignLibrary.class, paramName = "destinationId")}, batchsLocks = {
		@BatchPreventConcurrent(entityType = CampaignLibrary.class, paramName = "targetId", coercer = CampaignLibraryIdsCoercerForArray.class),
		@BatchPreventConcurrent(entityType = CampaignLibraryNode.class, paramName = "targetId", coercer = CLNAndParentIdsCoercerForArray.class)})
	public void moveNodesToLibrary(@Id("destinationId") long destinationId, @Ids("targetId") Long[] targetIds,
								   int position) {
		super.moveNodesToLibrary(destinationId, targetIds, position);
	}

	@Override
	@PreventConcurrents(batchsLocks = {
		@BatchPreventConcurrent(entityType = CampaignLibrary.class, paramName = "targetIds", coercer = CampaignLibraryIdsCoercerForList.class),
		@BatchPreventConcurrent(entityType = CampaignLibraryNode.class, paramName = "targetIds", coercer = CLNAndParentIdsCoercerForList.class)})
	public OperationReport deleteNodes(@Ids("targetIds") List<Long> targetIds) {
		return super.deleteNodes(targetIds);
	}

	// ###################### /PREVENT CONCURRENCY OVERIDES
	// ############################
}
