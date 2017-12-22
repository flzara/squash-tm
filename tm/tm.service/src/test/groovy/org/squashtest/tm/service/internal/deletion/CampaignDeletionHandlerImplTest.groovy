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
package org.squashtest.tm.service.internal.deletion

import org.squashtest.tm.service.campaign.IterationTestPlanManagerService;
import org.squashtest.tm.service.internal.customfield.PrivateCustomFieldValueService
import org.squashtest.tm.service.internal.repository.CampaignDeletionDao;
import org.squashtest.tm.service.internal.repository.TestSuiteDao
import  org.squashtest.tm.domain.campaign.TestSuite
import  org.squashtest.tm.domain.campaign.IterationTestPlanItem 
import spock.lang.Specification;


class CampaignDeletionHandlerImplTest  extends Specification{

	
	CampaignDeletionHandlerImpl handler = new CampaignDeletionHandlerImpl()
	TestSuiteDao suiteDao = Mock()
	PrivateCustomFieldValueService customValueService = Mock()
	CampaignDeletionDao deletionDao = Mock()
	IterationTestPlanManagerService iterationTestPlanManagerService = Mock()
	
	def setup(){
		handler.suiteDao = suiteDao
		handler.customValueService = customValueService
		handler.deletionDao = deletionDao
		handler.iterationTestPlanManagerService = iterationTestPlanManagerService
		
	}
	
	def "should simulate test suite deletion with no error"(){
		
		given :
		def ts = (1..5).collect { createTestSuite(it)}
		def itpi = (1..5).collect{ createItpi(it, ts) }
		ts.each {it.getTestPlan() >> itpi}

		suiteDao.findAll(_) >> ts
		def ids = [1L, 2L, 3L, 4L, 5L]
		when :
		def result = handler.simulateSuiteDeletion(ids)
		then :
		result == []

	}
	
	def "should simulate test suite deletion with errors"(){
		
		given :
		def ts = (1..5).collect { createTestSuite(it)}
		def itpi = (1..5).collect{ createItpi(it, ts) }
		ts.each {it.getTestPlan() >> itpi}

		suiteDao.findAll(_) >> ts
		def ids = [1L, 2L, 5L]
		when :
		def result = handler.simulateSuiteDeletion(ids)
		then :
		result.size == 1

	}
	
	def "should delete test suite "(){
		
		given :
		def ts = (1..5).collect { createTestSuite(it)}
		def itpi = (1..5).collect{ createItpi(it, ts) }
		ts.each {it.getTestPlan() >> itpi}
	
		suiteDao.findAll(_) >> ts
		def ids = [1L, 2L, 5L]
		when :
		def result = handler.deleteSuites(ids, false)
		then :
		result.removed.resid == ids

	}

	
	def "should delete test suite and remove iteration test plan item from iteration "(){
		
		given :
		def ts = (1..5).collect { createTestSuite(it)}
		def itpi = (1..5).collect{ createItpi(it, ts) }
		ts.each {it.getTestPlan() >> itpi}
	
		suiteDao.findAll(_) >> ts
		def ids = [1L, 2L, 3L, 4L, 5L]
		when :
		def result = handler.deleteSuites(ids, true)
		then :
		5 * iterationTestPlanManagerService.removeTestPlanFromIteration(_)

	}
	
	def "should delete test suite and not remove iteration test plan item from iteration if the item is in another not deleted suite"(){
		
		given :
		def ts = (1..5).collect { createTestSuite(it)}
		def itpi = (1..5).collect{ createItpi(it, ts) }
		ts.each {it.getTestPlan() >> itpi}
	
		suiteDao.findAll(_) >> ts
		def ids = [1L, 3L, 4L, 5L]
		when :
		def result = handler.deleteSuites(ids, true)
		then :
		0 * iterationTestPlanManagerService.removeTestPlanFromIteration(_)

	}
	
	
	
	
	
	
	def createTestSuite = {id -> TestSuite ts = Mock(TestSuite)
		ts.getId() >> id
		ts.getAttachmentList() >> []
		ts.getIteration() >> []
		return ts}
	
	def createItpi = {id, testSuites -> IterationTestPlanItem itpi = Mock(IterationTestPlanItem)
		itpi.getId() >> id
		itpi.getTestSuites() >> testSuites.collect{it}
		itpi.getExecutions() >> []
		return itpi}
	
	
}
