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

import javax.inject.Inject

import org.hibernate.SessionFactory
import org.hibernate.Query
import org.junit.runner.RunWith
import org.spockframework.runtime.Sputnik
import org.springframework.transaction.annotation.Transactional
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.campaign.TestSuite
import org.squashtest.tm.domain.execution.Execution
import org.squashtest.tm.domain.execution.ExecutionStatus
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.squashtest.tm.service.campaign.IterationModificationService
import org.squashtest.tm.domain.denormalizedfield.DenormalizedFieldHolderType
import org.squashtest.tm.domain.denormalizedfield.DenormalizedFieldValue
import org.unitils.dbunit.annotation.DataSet
import org.unitils.dbunit.annotation.ExpectedDataSet

import spock.unitils.UnitilsSupport

@UnitilsSupport
@Transactional
@RunWith(Sputnik)
class IterationFinderServiceIT extends DbunitServiceSpecification {

	@Inject
	IterationModificationService iterService



	@DataSet("IterationFinderServiceIT.3executions.xml")
	def "should find test plan executions"(){
		given :
		def iterationId = -1L
		def testPlanItemId = -2L

		when :
		List<Execution> execList = iterService.findExecutionsByTestPlan(iterationId, testPlanItemId)

		def exec1 = execList.get(0)
		def exec2 = execList.get(1)
		def exec3 = execList.get(2)

		then:
		exec1.name == "execution1"
		exec2.name == "execution2"
		exec3.name == "execution3"


	}


	@DataSet("IterationFinderServiceIT.3itps.3executions.xml")
	def "should fetch executions in the correct order"(){

		given :
		def iterationId = -1L

		when :
		List<Execution> executions = iterService.findAllExecutions(iterationId)

		then :
		executions*.name.containsAll([
			"execution",
			"execution2",
			"execution3"
		])
		executions.size() == 3
	}

	/** TODO FIXME see {@linkplain Iteration#getPlannedTestCase()} */
	//	@DataSet("IterationFinderServiceIT.execution.xml")
	//	def "should get the list of planned test cases of an iteration"(){
	//
	//		given :
	//		iterationId = -1L
	//TODO change dataset and do not use services in the given block
	//		TestCase tc1 = new TestCase(name:"tc1");
	//		TestCase tc2 = new TestCase(name:"tc2");
	//
	//		tcNavService.addTestCaseToLibrary(libtcId, tc1)
	//		tcNavService.addTestCaseToLibrary(libtcId, tc2)
	//
	//		and :
	//
	//		tpManagerService.addTestCasesToIteration([tc1.id, tc2.id], iterationId);
	//
	//		def tp1 = tpManagerService.findTestPlanItemByTestCaseId(iterationId, tc1.id)
	//		def tp2 = tpManagerService.findTestPlanItemByTestCaseId(iterationId, tc2.id)
	//
	//		iterService.addExecution(tp1.id)
	//		iterService.addExecution(tp2.id)
	//
	//		when :
	//		List<TestCase> list = iterService.findPlannedTestCases(iterationId);
	//
	//		then :
	//		list.size()==3
	//		list.collect {it.name} == [
	//			"exec IT test case",
	//			"tc1",
	//			"tc2"
	//		]
	//	}



}
