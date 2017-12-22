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
package org.squashtest.tm.service.campaign;

import static org.squashtest.tm.service.security.Authorizations.OR_HAS_ROLE_ADMIN;

import java.util.Date;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.dynamicmanager.annotation.DynamicManager;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.CampaignStatus;
import org.squashtest.tm.domain.execution.ExecutionStatus;

@Transactional
@DynamicManager(name="squashtest.tm.service.CampaignModificationService" , entity=Campaign.class)
public interface CampaignModificationService extends CustomCampaignModificationService, CampaignFinder {
	String WRITE_CAMAIGN_OR_ADMIN = "hasPermission(#arg0, 'org.squashtest.tm.domain.campaign.Campaign' ,'WRITE') "
			+ OR_HAS_ROLE_ADMIN;

	@PreAuthorize(WRITE_CAMAIGN_OR_ADMIN)
	void changeDescription(long campaignId, String newDescription);

	@PreAuthorize(WRITE_CAMAIGN_OR_ADMIN)
	void changeReference(long campaignId, String newReference);

	@PreAuthorize(WRITE_CAMAIGN_OR_ADMIN)
	void changeStatus(long campaignId, CampaignStatus status);

	@PreAuthorize(WRITE_CAMAIGN_OR_ADMIN)
	void changeScheduledStartDate(long campaignId, Date scheduledStart);

	@PreAuthorize(WRITE_CAMAIGN_OR_ADMIN)
	void changeScheduledEndDate(long campaignId, Date scheduledEnd);

	@PreAuthorize(WRITE_CAMAIGN_OR_ADMIN)
	void changeActualStartDate(long campaignId, Date actualStart);

	@PreAuthorize(WRITE_CAMAIGN_OR_ADMIN)
	void changeActualEndDate(long campaignId, Date actualEnd);

	@PreAuthorize(WRITE_CAMAIGN_OR_ADMIN)
	void changeActualStartAuto(long campaignId, boolean isAuto);

	@PreAuthorize(WRITE_CAMAIGN_OR_ADMIN)
	void changeActualEndAuto(long campaignId, boolean isAuto);



}
