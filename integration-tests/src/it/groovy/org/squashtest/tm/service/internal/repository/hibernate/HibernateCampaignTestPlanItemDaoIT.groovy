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
package org.squashtest.tm.service.internal.repository.hibernate

import javax.inject.Inject

import org.hibernate.Query
import org.springframework.transaction.annotation.Transactional
import org.squashtest.it.basespecs.DbunitDaoSpecification;
import org.squashtest.tm.core.foundation.collection.Paging
import org.squashtest.tm.domain.campaign.IterationTestPlanItem
import org.squashtest.tm.domain.campaign.TestPlanStatistics
import org.squashtest.tm.domain.campaign.TestPlanStatus
import org.squashtest.tm.service.internal.repository.CampaignTestPlanItemDao
import org.unitils.dbunit.annotation.DataSet

import spock.unitils.UnitilsSupport

@UnitilsSupport
@Transactional
class HibernateCampaignTestPlanItemDaoIT extends DbunitDaoSpecification {
	@Inject CampaignTestPlanItemDao campaignTestPlanItemDao
	
	@DataSet("HibernateCampaignTestPlanItemDaoIT.should find test case ids.xml")
	def "should find test cases ids"(){
		given :
		def campaignId = -1L
		
		when : 
		def result = campaignTestPlanItemDao.findPlannedTestCasesIdsByCampaignId(campaignId)
		
		then : 
		result.size() == 2;
		result.containsAll([-1L, -2L]);
	}
	
}
