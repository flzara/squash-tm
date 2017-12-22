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
package org.squashtest.tm.service.internal.library

import org.squashtest.tm.domain.campaign.Campaign
import org.squashtest.tm.domain.projectfilter.ProjectFilter
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.service.internal.library.SearchServiceImpl;
import org.squashtest.tm.service.internal.repository.CampaignDao
import org.squashtest.tm.service.internal.repository.RequirementDao
import org.squashtest.tm.service.internal.repository.TestCaseDao
import org.squashtest.tm.service.project.ProjectFilterModificationService;

import spock.lang.Specification


class SearchServiceImplTest extends Specification {

	SearchServiceImpl service = new SearchServiceImpl();

	CampaignDao campaignDao = Mock();
	ProjectFilterModificationService projService = Mock();

	def setup() {
		service.campaignDao = campaignDao
		service.projectFilterModificationService = projService
	}

	def "should return a Campaign with name matching the given String" () {
		given:
		Campaign campaign1 = Mock();
		Campaign campaign2 = Mock();
		campaignDao.findAllByNameContaining("campaign", false) >> [campaign1, campaign2]

		and :

		ProjectFilter filter = Mock()
		filter.getActivated() >> false
		projService.findProjectFilterByUserLogin() >> filter

		when:
		def found = service.findCampaignByName("campaign", false)

		then:
		found == [campaign1, campaign2]
	}


}
