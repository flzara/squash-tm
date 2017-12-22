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

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.CampaignFolder;
import org.squashtest.tm.domain.campaign.CampaignLibraryNode;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.TestPlanStatistics;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.service.campaign.CampaignFinder;
import org.squashtest.tm.service.campaign.CampaignStatisticsService;
import org.squashtest.tm.service.campaign.CustomCampaignModificationService;
import org.squashtest.tm.service.internal.library.NodeManagementService;
import org.squashtest.tm.service.internal.repository.CampaignDao;
import org.squashtest.tm.service.internal.repository.IterationDao;
import org.squashtest.tm.service.milestone.MilestoneMembershipManager;
import org.squashtest.tm.service.statistics.campaign.CampaignStatisticsBundle;
import org.squashtest.tm.service.statistics.campaign.ManyCampaignStatisticsBundle;

@Service("CustomCampaignModificationService")
@Transactional
public class CustomCampaignModificationServiceImpl implements CustomCampaignModificationService {

	private static final String READ_CAMPAIGN_OR_ADMIN = "hasPermission(#campaignId, 'org.squashtest.tm.domain.campaign.Campaign', 'READ')" + OR_HAS_ROLE_ADMIN;

	private static final String READ_FOLDER_OR_ADMIN = "hasPermission(#folderId, 'org.squashtest.tm.domain.campaign.CampaignFolder', 'READ')" + OR_HAS_ROLE_ADMIN;


	private static final String WRITE_CAMPAIGN_OR_ADMIN = "hasPermission(#campaignId, 'org.squashtest.tm.domain.campaign.Campaign' ,'WRITE')" + OR_HAS_ROLE_ADMIN;

	@PersistenceContext
	private EntityManager em;

	@Inject
	private CampaignDao campaignDao;

	@Inject
	private IterationDao iterationDao;

	@Inject
	private CampaignStatisticsService statisticsService;

	@Inject
	private MilestoneMembershipManager milestoneService;

	@Inject
	private CampaignFinder campaignFinder;

	@Inject
	@Named("squashtest.tm.service.internal.CampaignManagementService")
	private NodeManagementService<Campaign, CampaignLibraryNode, CampaignFolder> campaignManagementService;

	public CustomCampaignModificationServiceImpl() {
		super();
	}

	@Override
	@PreAuthorize(WRITE_CAMPAIGN_OR_ADMIN)
	public void rename(long campaignId, String newName) {
		campaignManagementService.renameNode(campaignId, newName);
	}

	@Override
	@PreAuthorize(READ_CAMPAIGN_OR_ADMIN)
	public TestPlanStatistics findCampaignStatistics(long campaignId) {
		return campaignDao.findCampaignStatistics(campaignId);
	}

	@Override
	@PreAuthorize(READ_CAMPAIGN_OR_ADMIN)
	public List<Iteration> findIterationsByCampaignId(long campaignId) {
		return iterationDao.findAllByCampaignId(campaignId);
	}


	@Override
	@PreAuthorize(READ_CAMPAIGN_OR_ADMIN)
	public Integer countIterations(Long campaignId) {
		return campaignDao.countIterations(campaignId);
	}


	@Override
	@PreAuthorize(READ_CAMPAIGN_OR_ADMIN)
	public CampaignStatisticsBundle gatherCampaignStatisticsBundle(
		long campaignId) {
		return statisticsService.gatherCampaignStatisticsBundle(campaignId);
	}


	@Override
	@PreAuthorize(READ_FOLDER_OR_ADMIN)
	public ManyCampaignStatisticsBundle gatherFolderStatisticsBundle(
		Long folderId) {
		return statisticsService.gatherFolderStatisticsBundle(folderId);
	}


	/*
	 *
	 * Milestones sections
	 *
	 */

	@Override
	@PreAuthorize(WRITE_CAMPAIGN_OR_ADMIN)
	public void bindMilestone(long campaignId, long milestoneId) {
		milestoneService.bindCampaignToMilestone(campaignId, milestoneId);
	}


	@Override
	@PreAuthorize(WRITE_CAMPAIGN_OR_ADMIN)
	public void unbindMilestones(long campaignId, Collection<Long> milestoneIds) {
		milestoneService.unbindCampaignFromMilestones(campaignId, milestoneIds);
	}

	@Override
	@PreAuthorize(READ_CAMPAIGN_OR_ADMIN)
	public Collection<Milestone> findAllMilestones(long campaignId) {
		return milestoneService.findMilestonesForCampaign(campaignId);
	}

	@Override
	@PreAuthorize(READ_CAMPAIGN_OR_ADMIN)
	public Collection<Milestone> findAssociableMilestones(long campaignId) {
		return milestoneService.findAssociableMilestonesToCampaign(campaignId);
	}

	@Override
	public Collection<Campaign> findCampaignsByMilestoneId(long milestoneId) {
		return milestoneService.findCampaignsByMilestoneId(milestoneId);
	}

	/**
	 * This method calls the {@link org.squashtest.tm.service.campaign# findById() findById()}
	 * method of {@link CampaignFinder} after checking the existence
	 * of the {@link Campaign} in database. Avoiding an AccessDeniedException
	 * in case the id does not exist in database.
	 */
	@Override
	public Campaign findCampaigWithExistenceCheck(long campaignId) {
		Campaign campaign = em.find(Campaign.class, campaignId);
		if (campaign == null) {
			return null;
		}
		return campaignFinder.findById(campaignId);
	}

}
