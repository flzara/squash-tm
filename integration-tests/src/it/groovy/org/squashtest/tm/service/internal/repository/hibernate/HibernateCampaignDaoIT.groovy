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

import org.squashtest.it.basespecs.DbunitDaoSpecification;
import org.squashtest.tm.core.foundation.collection.ColumnFiltering
import org.squashtest.tm.core.foundation.collection.PagingAndMultiSorting
import org.squashtest.tm.domain.campaign.Campaign
import org.squashtest.tm.domain.campaign.TestPlanStatistics
import org.squashtest.tm.domain.campaign.TestPlanStatus
import org.squashtest.tm.domain.testcase.TestCaseExecutionMode
import org.squashtest.tm.service.internal.repository.CampaignDao
import org.unitils.dbunit.annotation.DataSet

import spock.lang.Unroll
import spock.unitils.UnitilsSupport

@UnitilsSupport
class HibernateCampaignDaoIT extends DbunitDaoSpecification {
	@Inject CampaignDao campaignDao

	@DataSet("HibernateCampaignDaoIT.should return list of copies in folder.xml")
	def "should return list of copies in folder"() {
		when:
		def res = campaignDao.findNamesInFolderStartingWith(-1, "foo-Copie")
		then:
		res.containsAll("foo-Copie1", "foo-Copie10")
	}

	@DataSet("HibernateCampaignDaoIT.should return list of executions.xml")
	def "should return list of executions"(){
		when:
		def result = campaignDao.findAllExecutionsByCampaignId(-1L)

		then:
		result.size() == 5
		result.each {it.name == "campaign1-execution"}
	}

	@DataSet("HibernateCampaignDaoIT.should find campaign statistics.xml")
	def "should find campaign statistics READY"(){
		when:
		TestPlanStatistics result = campaignDao.findCampaignStatistics(-1L)

		then:
		result != null
		result.nbBlocked == 0
		result.nbSuccess == 0
		result.nbReady == 3
		result.nbDone == 0
		result.nbRunning == 0
		result.nbTestCases == 3
		result.nbUntestable == 0
		result.progression == 0
		result.nbFailure == 0
		result.status == TestPlanStatus.READY
	}

	@Unroll
	@DataSet("HibernateCampaignDaoIT.campaign with test plan.xml")
	def "should find test plan filtered by auto-mode: #autoMode"() {
		given:
		PagingAndMultiSorting sort = Mock()
		sort.sortings >> []

		and:
		ColumnFiltering filt = Mock()
		filt.hasFilter(HibernateCampaignDao.MODE_DATA) >> active
		filt.getFilter(HibernateCampaignDao.MODE_DATA) >> filter

		when:
		def res = campaignDao.findFilteredIndexedTestPlan(-10L, sort, filt).collect { it.item.id }

		then:
		res.containsAll(expectedId)
		res.size() == expectedId.size()

		where:
		active   | filter                          | expectedId
		true	 | TestCaseExecutionMode.AUTOMATED | [-1010L]
		true	 | TestCaseExecutionMode.MANUAL    | [-1020L]
		false	 | null                            | [-1020L, -1010L]
	}
}




