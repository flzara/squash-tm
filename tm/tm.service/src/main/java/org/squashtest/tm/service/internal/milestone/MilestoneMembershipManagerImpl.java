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
package org.squashtest.tm.service.internal.milestone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.squashtest.tm.domain.audit.AuditableMixin;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.audit.AuditModificationService;
import org.squashtest.tm.service.internal.repository.CampaignDao;
import org.squashtest.tm.service.internal.repository.MilestoneDao;
import org.squashtest.tm.service.internal.repository.RequirementVersionDao;
import org.squashtest.tm.service.internal.repository.TestCaseDao;
import org.squashtest.tm.service.milestone.MilestoneMembershipManager;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;

import static org.squashtest.tm.service.security.Authorizations.READ_CAMPAIGN_OR_ROLE_ADMIN;
import static org.squashtest.tm.service.security.Authorizations.READ_ITERATION_OR_ROLE_ADMIN;
import static org.squashtest.tm.service.security.Authorizations.READ_REQVERSION_OR_ROLE_ADMIN;
import static org.squashtest.tm.service.security.Authorizations.READ_TC_OR_ROLE_ADMIN;
import static org.squashtest.tm.service.security.Authorizations.READ_TS_OR_ROLE_ADMIN;
import static org.squashtest.tm.service.security.Authorizations.WRITE_CAMPAIGN_OR_ROLE_ADMIN;
import static org.squashtest.tm.service.security.Authorizations.WRITE_REQVERSION_OR_ROLE_ADMIN;
import static org.squashtest.tm.service.security.Authorizations.WRITE_TC_OR_ROLE_ADMIN;

@Service("squashtest.tm.service.MilestoneMembershipManager")
public class MilestoneMembershipManagerImpl implements MilestoneMembershipManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(MilestoneMembershipManagerImpl.class);

	@Inject
	private TestCaseDao testCaseDao;

	@Inject
	private RequirementVersionDao requirementVersionDao;

	@Inject
	private CampaignDao campaignDao;


	@Inject
	private MilestoneDao milestoneDao;

	@Inject
	private AuditModificationService auditModificationService;

	@Override
	@PreAuthorize(WRITE_TC_OR_ROLE_ADMIN)
	public void bindTestCaseToMilestones(long testCaseId, Collection<Long> milestoneIds) {
		TestCase tc = testCaseDao.findById(testCaseId);
		Collection<Milestone> milestones = milestoneDao.findAllById(milestoneIds);

		for (Milestone m : milestones) {
			tc.bindMilestone(m);
		}

		LOGGER.debug("Milestone binding: updating auditable test case {}", testCaseId);
		auditModificationService.updateAuditable((AuditableMixin)tc);
	}

	@Override
	@PreAuthorize(WRITE_TC_OR_ROLE_ADMIN)
	public void unbindTestCaseFromMilestones(long testCaseId, Collection<Long> milestoneIds) {
		TestCase tc = testCaseDao.findById(testCaseId);
		for (Long milestoneId : milestoneIds) {
			tc.unbindMilestone(milestoneId);
		}

		LOGGER.debug("Milestone unbinding: updating auditable test case {}", testCaseId);
		auditModificationService.updateAuditable((AuditableMixin)tc);
	}

	@Override
	@PreAuthorize(WRITE_REQVERSION_OR_ROLE_ADMIN)
	public void bindRequirementVersionToMilestones(long requirementVersionId, Collection<Long> milestoneIds) {
		RequirementVersion version = requirementVersionDao.getOne(requirementVersionId);
		Collection<Milestone> milestones = milestoneDao.findAllById(milestoneIds);

		for (Milestone m : milestones) {
			if (!m.isOneVersionAlreadyBound(version)) {
				version.bindMilestone(m);
			}
		}

		LOGGER.debug("Milestone binding: updating auditable requirement version {}", requirementVersionId);
		auditModificationService.updateAuditable((AuditableMixin)version);
	}

	@Override
	@PreAuthorize(WRITE_REQVERSION_OR_ROLE_ADMIN)
	public void unbindRequirementVersionFromMilestones(long requirementVersionId, Collection<Long> milestoneIds) {
		RequirementVersion version = requirementVersionDao.getOne(requirementVersionId);
		for (Long milestoneId : milestoneIds) {
			version.unbindMilestone(milestoneId);
		}

		LOGGER.debug("Milestone unbinding: updating auditable requirement version {}", requirementVersionId);
		auditModificationService.updateAuditable((AuditableMixin)version);
	}

	@Override
	@PreAuthorize(WRITE_CAMPAIGN_OR_ROLE_ADMIN)
	public void bindCampaignToMilestone(long campaignId, Long milestoneId) {
		if (milestoneId != null){
			Campaign campaign = campaignDao.findById(campaignId);
			Milestone milestone = milestoneDao.getOne(milestoneId);
			campaign.bindMilestone(milestone);
			LOGGER.debug("Milestone binding: updating auditable campaign {}", campaignId);
			auditModificationService.updateAuditable((AuditableMixin)campaign);
		}
	}

	@Override
	@PreAuthorize(WRITE_CAMPAIGN_OR_ROLE_ADMIN)
	public void unbindCampaignFromMilestones(long campaignId, Collection<Long> milestoneIds) {
		Campaign campaign = campaignDao.findById(campaignId);
		for (Long milestoneId : milestoneIds) {
			campaign.unbindMilestone(milestoneId);
		}
		LOGGER.debug("Milestone unbinding: updating auditable campaign {}", campaignId);
		auditModificationService.updateAuditable((AuditableMixin)campaign);
	}

	@Override
	@PreAuthorize(READ_TC_OR_ROLE_ADMIN)
	public Collection<Milestone> findAllMilestonesForTestCase(long testCaseId) {
		return milestoneDao.findAllMilestonesForTestCase(testCaseId);
	}

	@Override
	@PreAuthorize(READ_TC_OR_ROLE_ADMIN)
	public boolean isTestCaseMilestoneDeletable(long testCaseId) {
		return milestoneDao.isTestCaseMilestoneDeletable(testCaseId);
	}

	@Override
	@PreAuthorize(READ_TC_OR_ROLE_ADMIN)
	public boolean isTestCaseMilestoneModifiable(long testCaseId) {
		return milestoneDao.isTestCaseMilestoneModifiable(testCaseId);
	}

	@Override
	@PreAuthorize(READ_TC_OR_ROLE_ADMIN)
	public Collection<Milestone> findAssociableMilestonesToTestCase(long testCaseId) {
		return milestoneDao.findAssociableMilestonesForTestCase(testCaseId);
	}

	@Override
	public Collection<Milestone> findAllMilestonesForUser(long userId) {
		return milestoneDao.findAssociableMilestonesForUser(userId);
	}

	@Override
	@PreAuthorize(READ_REQVERSION_OR_ROLE_ADMIN)
	public Collection<Milestone> findAssociableMilestonesToRequirementVersion(long requirementVersionId) {
		return milestoneDao.findAssociableMilestonesForRequirementVersion(requirementVersionId);
	}

	@Override
	@PreAuthorize(READ_REQVERSION_OR_ROLE_ADMIN)
	public Collection<Milestone> findMilestonesForRequirementVersion(long requirementVersionId) {
		return milestoneDao.findMilestonesForRequirementVersion(requirementVersionId);
	}

	@Override
	@PreAuthorize(READ_CAMPAIGN_OR_ROLE_ADMIN)
	public Collection<Milestone> findAssociableMilestonesToCampaign(long campaignId) {
		return milestoneDao.findAssociableMilestonesForCampaign(campaignId);
	}

	@Override
	@PreAuthorize(READ_CAMPAIGN_OR_ROLE_ADMIN)
	public Collection<Milestone> findMilestonesForCampaign(long campaignId) {
		return milestoneDao.findMilestonesForCampaign(campaignId);
	}

	@Override
	@PreAuthorize(READ_ITERATION_OR_ROLE_ADMIN)
	public Collection<Milestone> findMilestonesForIteration(long iterationId) {
		return milestoneDao.findMilestonesForIteration(iterationId);
	}

	@Override
	@PreAuthorize(READ_TS_OR_ROLE_ADMIN)
	public Collection<Milestone> findMilestonesForTestSuite(long testSuiteId) {
		return milestoneDao.findMilestonesForTestSuite(testSuiteId);
	}

	@Override
	public Collection<Campaign> findCampaignsByMilestoneId(long milestoneId) {
		return milestoneDao.findCampaignsForMilestone(milestoneId);
	}

	@Override
	public boolean isMilestoneBoundToACampainInProjects(Long milestoneId, List<Long> projectIds) {
		return milestoneDao.isMilestoneBoundToACampainInProjects(milestoneId, projectIds);
	}

}
