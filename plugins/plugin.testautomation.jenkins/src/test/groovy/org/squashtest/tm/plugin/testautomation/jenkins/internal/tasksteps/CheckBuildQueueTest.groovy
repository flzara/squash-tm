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
package org.squashtest.tm.plugin.testautomation.jenkins.internal.tasksteps

import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.impl.client.CloseableHttpClient
import org.squashtest.tm.plugin.testautomation.jenkins.internal.JenkinsConnectorSpec;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.JsonParser
import org.squashtest.tm.plugin.testautomation.jenkins.internal.net.RequestExecutor;

import spock.lang.Specification

class CheckBuildQueueTest extends JenkinsConnectorSpec {

	CheckBuildQueue checkQueue;

	def setup(){

		checkQueue = new CheckBuildQueue()
		checkQueue.client = client
		checkQueue.method = method
		checkQueue.parser = parser;

		checkQueue.absoluteId = new BuildAbsoluteId("CorrectJob", "CorrectExternalID")
	}


	def "should check that a given build is not queued"(){

		given :
			def json = makeQueueWithoutThatBuild()
		RequestExecutor.INSTANCE.execute(_, _)>> json

		when :
			checkQueue.perform()

		then :
			checkQueue.buildIsQueued == false
			checkQueue.needsRescheduling() == false
	}

	def "should check that the given build is queued and the step needs rescheduling while it is"(){

		given :
			def json = makeQueueWithThatBuild()
		RequestExecutor.INSTANCE.execute(_, _)>> json

		when :
			checkQueue.perform()

		then :
			checkQueue.buildIsQueued == true
			checkQueue.needsRescheduling() == true
	}

	def "should check that the given build is not queued because the queue is empty"(){

		given :
		RequestExecutor.INSTANCE.execute(_, _)>>  '{"items":[]}'

		when :
			checkQueue.perform()

		then :
			checkQueue.buildIsQueued == false
			checkQueue.needsRescheduling() == false
	}


	def makeQueueWithoutThatBuild(){

		return '{"items":[{"actions":[{"parameters":[{"name":"operation","value":"test-list"},'+
		       '{"name":"externalJobId","value":"WrongExternalID"},{"name":"callerId","value":"anonymous@example.com"},'+
			   '{"name":"notificationURL","value":"file://dev/null"},{"name":"testList","value":"**/*"}]},{}],'+
			   '"id":4,"task":{"name":"CorrectJob"}},{"actions":[{"parameters":[{"name":"operation","value":"test-list"},'+
			   '{"name":"externalJobId","value":"CorrectExternalID"},{"name":"callerId","value":"anonymous@example.com"},'+
			   '{"name":"notificationURL","value":"file://dev/null"},{"name":"testList","value":"**/*"}]},{}],"id":13,'+
			   '"task":{"name":"WrongJob"}}]}'
	}


	def makeQueueWithThatBuild(){

		return '{"items":[{"actions":[{"parameters":[{"name":"operation","value":"test-list"},'+
			   '{"name":"externalJobId","value":"WrongExternalID"},{"name":"callerId","value":"anonymous@example.com"},'+
			   '{"name":"notificationURL","value":"file://dev/null"},{"name":"testList","value":"**/*"}]},{}],'+
			   '"id":4,"task":{"name":"CorrectJob"}},{"actions":[{"parameters":[{"name":"operation","value":"test-list"},'+
			   '{"name":"externalJobId","value":"CorrectExternalID"},{"name":"callerId","value":"anonymous@example.com"},'+
			   '{"name":"notificationURL","value":"file://dev/null"},{"name":"testList","value":"**/*"}]},{}],"id":13,'+
			   '"task":{"name":"CorrectJob"}}]}'
	}
}


