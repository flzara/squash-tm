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

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.campaign.CampaignLibrary;
import org.squashtest.tm.domain.campaign.CampaignLibraryNode;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.service.internal.library.LibrarySelectionStrategy;

@Component("squashtest.tm.service.CampaignLibrarySelectionStrategy")
public class CampaignLibrarySelectionStrategyImpl implements LibrarySelectionStrategy<CampaignLibrary, CampaignLibraryNode> {

	@Override
	public List<CampaignLibrary> getSpecificLibraries(List<Project> givenProjectList) {
		List<CampaignLibrary> toReturn = new ArrayList<>();
		for (Project project : givenProjectList) {
			toReturn.add(project.getCampaignLibrary());
		}
		return toReturn;
	}

}
