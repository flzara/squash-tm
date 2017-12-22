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

import java.util.List;

import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.CampaignTestPlanItem;
import org.squashtest.tm.domain.project.Project;

@Transactional(readOnly = true)
public interface CampaignFinder {

	@PostAuthorize("hasPermission(returnObject,'READ')"+ OR_HAS_ROLE_ADMIN)
	Campaign findById(long campaignId);

	@PostFilter("hasPermission(filterObject , 'READ')" + OR_HAS_ROLE_ADMIN)
	List<Campaign> findAllByIds(List<Long> campaignIds);

	@PreAuthorize("hasPermission(#arg0, 'org.squashtest.tm.domain.campaign.Campaign' ,'READ') "
			+ OR_HAS_ROLE_ADMIN)
	PagedCollectionHolder<List<CampaignTestPlanItem>> findTestPlanByCampaignId(long campaignId,
			PagingAndSorting filter);

	@PreAuthorize("hasPermission(#arg0, 'org.squashtest.tm.domain.campaign.Campaign' ,'READ') "
			+ OR_HAS_ROLE_ADMIN)
	boolean findCampaignByProjectId(List<Project> projectList);


}
