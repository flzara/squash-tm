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

import java.util.Collection;
import java.util.List;

import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.CampaignLibrary;
import org.squashtest.tm.service.statistics.campaign.CampaignStatisticsBundle;

public interface CampaignLibraryFinderService {

	/**
	 * Returns the path of a CampaignLibraryNode given its id. The format is standard, beginning with /&lt;project-name&gt;
	 * 
	 * @param entityId the id of the node.
	 * @return the path of that node.
	 */
	String getPathAsString(long entityId);

	/**
	 * Returns the collection of {@link CampaignLibrary}s which Campaigns can be linked by a {@link Campaign} via a
	 * CallTestStep
	 * 
	 * @return
	 */
	List<CampaignLibrary> findLinkableCampaignLibraries();


	/**
	 * Passing the ids of some selected CampaignLibrary and CampaignLibraryNode (in separate collections), will return
	 * the ids of the Campaign encompassed by this selection.
	 * 
	 * The campaign ids that cannot be accessed for security reason will be filtered out.
	 * 
	 * @param libraryIds
	 * @param nodeIds
	 * @return
	 */
	Collection<Long> findCampaignIdsFromSelection(Collection<Long> libraryIds, Collection<Long> nodeIds);


	/**
	 * Returns all dashboard data for a milestone dashboard (ie about all campaigns that belong to the active milestone)
	 * 
	 *
	 * @return
	 */
	CampaignStatisticsBundle gatherCampaignStatisticsBundleByMilestone();

}
