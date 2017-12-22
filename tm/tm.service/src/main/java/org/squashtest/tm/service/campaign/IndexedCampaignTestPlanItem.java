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

import org.squashtest.tm.domain.campaign.CampaignTestPlanItem;


/**
 * This class pairs an Iteration with an index. The index depends on the collection the IterationTestPlanItem was 
 * accessed from (the test plan of the iteration it belongs to, or of several test suites).
 * 
 *  Without context the index is meaningless so be sure to keep track of how you acquired instances of theses.
 * 
 * @author bsiri
 *
 */
public class IndexedCampaignTestPlanItem {

	private final Integer index;
	
	private final CampaignTestPlanItem item;

	
	public IndexedCampaignTestPlanItem(Integer index, CampaignTestPlanItem item) {
		super();
		this.index = index;
		this.item = item;
	}

	public Integer getIndex() {
		return index;
	}

	public CampaignTestPlanItem getItem() {
		return item;
	}
	
	
	
}
