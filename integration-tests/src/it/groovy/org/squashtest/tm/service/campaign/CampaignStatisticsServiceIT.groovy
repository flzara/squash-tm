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
package org.squashtest.tm.service.campaign

import org.squashtest.tm.service.milestone.ActiveMilestoneHolder

import javax.inject.Inject

import org.spockframework.util.NotThreadSafe
import org.springframework.transaction.annotation.Transactional
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.unitils.dbunit.annotation.DataSet

import spock.unitils.UnitilsSupport




@NotThreadSafe
@UnitilsSupport
@Transactional
class CampaignStatisticsServiceIT extends DbunitServiceSpecification {
	@Inject
	private CampaignStatisticsService service

	@Inject
	private ActiveMilestoneHolder activeMilestoneHolder;

	//TODO improve check and dataset
	@DataSet("CampaignStatisticsServiceIT.xml")
	def"should gather campaign progression statistics"(){
		given :
		def campId = [-10L]
		when :
		def result = service.gatherCampaignProgressionStatistics(campId)
		then :
		result.scheduledIterations.size() == 3

	}



	@DataSet("CampaignStatisticsServiceIT.xml")
	def"should gather campaign statistics bundle"(){
		given :
		def campId = [-10L]
		when :
		def result = service.gatherCampaignStatisticsBundle(campId)
		then :
		result.iterationTestInventoryStatisticsList.iterationName == ['iter - 3', 'ref A - iter - tc1', 'ref B - iter - tc1 -2']

	}

	@DataSet("CampaignStatisticsServiceIT.xml")
	def"should gather campaign test case status statistics"(){
		given :
		def campId = [-10L]
		when :
		def result = service.gatherTestCaseStatusStatistics(campId)
		then :
		notThrown(Exception)

	}
	@DataSet("CampaignStatisticsServiceIT.xml")
	def"should gather campaign test case succes rate stat"(){
		given :
		def campId = [-10L]
		when :
		def result = service.gatherTestCaseSuccessRateStatistics(campId)
		then :
		notThrown(Exception)
	}
	@DataSet("CampaignStatisticsServiceIT.xml")
	def"should gather campaign test inventory stat"(){
		given :
		def campId = -10L
		when :
		def result = service.gatherCampaignTestInventoryStatistics(campId)
		then :
		notThrown(Exception)
	}

	@DataSet("CampaignStatisticsServiceIT.xml")
	def"should gather campaign  non executed test case importance stat"(){
		given :
		def campId = [-10L]
		when :
		def result = service.gatherNonExecutedTestCaseImportanceStatistics(campId)
		then :
		notThrown(Exception)
	}

	@DataSet("CampaignStatisticsServiceIT.xml")
	def"should not explode when no campaign are present"(){
		//test for Issue 5267
		given :
		def campId = []
		when :
		service.gatherFolderTestInventoryStatistics(campId)
		service.gatherTestCaseStatusStatistics(campId)

		then :
		notThrown(Exception)
	}




	@DataSet("CampaignStatisticsServiceIT.xml")
	def"should gather folder data"(){
		//test for Issue 5270
		given :
		def campId = [-10L]
		when :
	    service.gatherFolderTestInventoryStatistics(campId)

		then :
		notThrown(Exception)
	}

	@DataSet("CampaignStatisticsServiceIT.xml")
	def"should gather folder data 2"(){
		given :
		def campId = [-10L, -12L, -13L]
		when :
		def result = service.gatherFolderTestInventoryStatistics(campId)

		then :
		result.campaignName == ['bar', 'ref B - bar', 'ref Z - foo']
	}


	@DataSet("CampaignStatisticsServiceIT.xml")
	def"should gather milestone data "(){
		given :
		activeMilestoneHolder.setActiveMilestone(-1L)
		when :
		def result = service.gatherMilestoneStatisticsBundle()

		then :
		result.iterationTestInventoryStatisticsList.iterationName == ['bar / iter', 'ref B - bar / iter', 'ref Z - foo / iter - 3', 'ref Z - foo / ref A - iter - tc1', 'ref Z - foo / ref B - iter - tc1 -2']
		activeMilestoneHolder.clearContext()
	}



}
