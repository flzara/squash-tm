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

import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.impl.client.CloseableHttpClient
import org.squashtest.tm.plugin.testautomation.jenkins.internal.JenkinsConnectorSpec;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.JsonParser
import org.squashtest.tm.plugin.testautomation.jenkins.internal.net.RequestExecutor;

import spock.lang.Specification

class CheckBuildRunningTest extends JenkinsConnectorSpec {

	CheckBuildRunning checkRun;
	CloseableHttpClient client;
	
	def setup(){
		checkRun = new CheckBuildRunning()
		checkRun.client = client
		checkRun.method = method
		checkRun.parser = parser;
		
	}
	
	def "should say that the given build is still running, and need to be checked again"(){
		
		given :
			def json = makeBuildingJson()
			RequestExecutor.INSTANCE.execute(_,_) >> json
			
		when :
			checkRun.perform()
		
		then :
			checkRun.stillBuilding == true
			checkRun.needsRescheduling() == true
		
	}
	
	def "should say that the given build is over"(){
		
		given :
			def json = makeFinishedJson()
			RequestExecutor.INSTANCE.execute(_,_) >> json
			
		when :
			checkRun.perform()
		
		then :
			checkRun.stillBuilding == false
			checkRun.needsRescheduling() == false
		
	}
	
	def makeBuildingJson(){
		return '{"actions":[{"parameters":[{"name":"operation","value":"test-list"},'+
		'{"name":"externalJobId","value":"CorrectExternalID"},{"name":"callerId",'+
		'"value":"anonymous@example.com"},{"name":"notificationURL","value":"file://dev/null"},{"name":"testList","value":"**/*"}]},{},{}],'+
		'"building":true,"number":10}'
	}
	
	def makeFinishedJson(){
		return '{"actions":[{"parameters":[{"name":"operation","value":"test-list"},'+
		'{"name":"externalJobId","value":"CorrectExternalID"},{"name":"callerId",'+
		'"value":"anonymous@example.com"},{"name":"notificationURL","value":"file://dev/null"},{"name":"testList","value":"**/*"}]},{},{}],'+
		'"building":false,"number":10}'
	}
	
}
