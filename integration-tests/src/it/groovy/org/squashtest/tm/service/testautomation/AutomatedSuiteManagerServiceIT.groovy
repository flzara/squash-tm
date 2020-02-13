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
package org.squashtest.tm.service.testautomation

import org.spockframework.util.NotThreadSafe
import org.springframework.transaction.annotation.Transactional
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.squashtest.tm.core.foundation.lang.Couple
import org.squashtest.tm.domain.project.GenericProject
import org.squashtest.tm.domain.testautomation.AutomatedExecutionExtender
import org.squashtest.tm.domain.testautomation.AutomatedSuite
import org.squashtest.tm.domain.testautomation.TestAutomationServer
import org.unitils.dbunit.annotation.DataSet
import spock.unitils.UnitilsSupport
import org.squashtest.tm.service.internal.repository.IterationTestPlanDao

import javax.inject.Inject

@NotThreadSafe
@UnitilsSupport
@Transactional
class AutomatedSuiteManagerServiceIT extends DbunitServiceSpecification {

	@Inject
	AutomatedSuiteManagerService service

        @Inject
        IterationTestPlanDao itpiDao

	@DataSet("TestAutomationService.sandbox.xml")
	def "should return executions associated to an automated test suite given its id"(){
		when:
		def res = service.findExecutionsByAutomatedTestSuiteId("suite1")
		then:
		res[0].id == -40L
		res[1].id == -41L
	}

	def getServer(id){
		return getSession().load(TestAutomationServer.class, id)
	}

	def getProject(id){
		return getSession().load(GenericProject.class, id)
	}

	@DataSet("TestAutomationService.TFtrigger.xml")
    def "should return automated test suite associated to an iteration given a test plan items list"() {
    	given:
        def testItemsList = itpiDao.findAllByIdsOrderedByIterationTestPlan([-201L, -202L, -203L])

        when:
		AutomatedSuite suite = service.createFromIterationTestPlanItems(testItemsList)

        then:
		suite.executionExtenders.size() == 3
		suite.executionExtenders[0].id == 1L
		suite.executionExtenders[0].automatedTest.id == -71L
	}

	@DataSet("TestAutomationService.TFtrigger.xml")
	def "should return collection of tests with params"() {
		given:
		def testItemsList = itpiDao.findAllByIdsOrderedByIterationTestPlan([-201L, -202L, -203L])

		AutomatedSuite suite = service.createFromIterationTestPlanItems(testItemsList)

		when:
		Collection<Couple<AutomatedExecutionExtender, Map<String, Object>>> executionOrder = service.prepareExecutionOrder(suite)

		then:
		executionOrder.size() == 3
		executionOrder[0].a1.execution.referencedTestCase.uuid == "5bb09a58-72fd-4630-95fa-1b4651052c6a"
		executionOrder[0].a1.automatedTest.name == "test 1"

		executionOrder[0].a2.containsKey("TC_UUID")
		executionOrder[0].a2.get("TC_UUID") == "5bb09a58-72fd-4630-95fa-1b4651052c6a"

		executionOrder[0].a2.containsKey("TC_REFERENCE")
		executionOrder[0].a2.get("TC_REFERENCE") == "ref"
	}
}
