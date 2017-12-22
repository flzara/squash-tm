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
package org.squashtest.tm.service.internal.testautomation.thread

import java.util.concurrent.Future

import org.springframework.core.task.AsyncTaskExecutor
import org.squashtest.tm.service.internal.testautomation.FetchTestListTask
import org.squashtest.tm.service.internal.testautomation.FetchTestListFuture
import org.squashtest.tm.service.internal.testautomation.TestAutomationTaskExecutor
import org.squashtest.tm.service.testautomation.model.TestAutomationProjectContent

import spock.lang.Specification
class TestAutomationTaskExecutorTest extends Specification {

	AsyncTaskExecutor wrapped
	TestAutomationTaskExecutor executor;
	
	def setup(){
		wrapped = Mock()
		executor = new TestAutomationTaskExecutor(wrapped)
	}
	
	def "should return a task aware future for fetch test list task"(){
		
		given :
			FetchTestListTask task = Mock()
			Future<TestAutomationProjectContent> future = Mock()
			wrapped.submit(task) >> future
			
		when :
			def res = executor.sumbitFetchTestListTask(task)
			
		then :
			res instanceof FetchTestListFuture 
			res.task == task		
		
	}
	
}
