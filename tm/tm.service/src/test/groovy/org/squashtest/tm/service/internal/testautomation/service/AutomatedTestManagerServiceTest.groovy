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
package org.squashtest.tm.service.internal.testautomation.service

import javax.inject.Provider

import org.squashtest.tm.core.foundation.lang.Couple
import org.squashtest.tm.domain.campaign.Campaign
import org.squashtest.tm.domain.campaign.Iteration
import org.squashtest.tm.domain.customfield.CustomField
import org.squashtest.tm.domain.customfield.CustomFieldBinding
import org.squashtest.tm.domain.customfield.CustomFieldValue
import org.squashtest.tm.domain.execution.Execution
import org.squashtest.tm.domain.execution.ExecutionStatus
import org.squashtest.tm.domain.testautomation.AutomatedExecutionExtender
import org.squashtest.tm.domain.testautomation.AutomatedSuite
import org.squashtest.tm.domain.testautomation.AutomatedTest
import org.squashtest.tm.domain.testautomation.TestAutomationProject
import org.squashtest.tm.domain.testautomation.TestAutomationServer
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.service.customfield.CustomFieldValueFinderService
import org.squashtest.tm.service.internal.testautomation.AutomatedTestManagerServiceImpl
import org.squashtest.tm.service.internal.testautomation.FetchTestListFuture
import org.squashtest.tm.service.internal.testautomation.FetchTestListTask
import org.squashtest.tm.service.internal.testautomation.TaParametersBuilder
import org.squashtest.tm.service.internal.testautomation.TestAutomationConnectorRegistry
import org.squashtest.tm.service.internal.testautomation.TestAutomationTaskExecutor
import org.squashtest.tm.service.testautomation.model.TestAutomationProjectContent
import org.squashtest.tm.service.testautomation.spi.TestAutomationConnector
import org.squashtest.tm.service.testautomation.spi.UnknownConnectorKind

import spock.lang.Specification

class AutomatedTestManagerServiceTest extends Specification {


	TestAutomationConnectorRegistry connectorRegistry;
	AutomatedTestManagerServiceImpl service;


	TestAutomationTaskExecutor executor;
	CustomFieldValueFinderService finder = Mock()
	Provider builderProvider = Mock()

	def setup(){
		connectorRegistry = Mock()
		executor = Mock()
		service = new AutomatedTestManagerServiceImpl()
		service.connectorRegistry = connectorRegistry
		service.executor = executor;

	}




	def "should build a bunch of tasks to fetch the test lists"(){

		given :
		List<TestAutomationProject> projects = [
			Mock(TestAutomationProject),
			Mock(TestAutomationProject),
			Mock(TestAutomationProject)
		]

		when :
		def res = service.prepareAllFetchTestListTasks(projects)

		then :
		res.collect {
			[
				it.project,
				it.connectorRegistry
			]
		} == [
			[
				projects[0],
				connectorRegistry
			],
			[
				projects[1],
				connectorRegistry
			],
			[
				projects[2],
				connectorRegistry]
		]
	}

	def "should submit a bunch of tasks"(){

		given :
		List<FetchTestListTask> tasks = [
			Mock(FetchTestListTask),
			Mock(FetchTestListTask),
			Mock(FetchTestListTask)
		]

		and :
		List<FetchTestListFuture> futures = [
			Mock(FetchTestListFuture),
			Mock(FetchTestListFuture),
			Mock(FetchTestListFuture)
		]

		and :
		executor.sumbitFetchTestListTask(tasks[0]) >> futures[0]
		executor.sumbitFetchTestListTask(tasks[1]) >> futures[1]
		executor.sumbitFetchTestListTask(tasks[2]) >> futures[2]

		when :
		def res = service.submitAllFetchTestListTasks(tasks)

		then :
		res == futures
	}

	def "should collect test list results"(){

		given :
		TestAutomationProjectContent content1 = Mock()
		TestAutomationProjectContent content2 = Mock()

		and :
		FetchTestListFuture fut1 =  Mock()
		fut1.get(_,_) >> content1

		FetchTestListTask task2 = Mock()
		task2.buildFailedResult(_) >> content2
		FetchTestListFuture fut2 = Mock()

		fut2.getTask() >> task2
		fut2.get(_,_) >> { throw new Exception() }

		when :
		def res = service.collectAllTestLists([fut1, fut2])

		then :
		res == [content1, content2]
	}

}
