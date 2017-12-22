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

class GetBuildIDTest extends JenkinsConnectorSpec {

	def "should get the id of a build"(){
		
		given :
			def json = makeBuildListForProject()
			RequestExecutor.INSTANCE.execute(_, _)>> json
			
		when :
			getID.perform()
		
		then :
			getID.absoluteId.buildId == 8
			
	}
	
	
	def makeBuildListForProject(){
		return '{"builds":[{"actions":[{"parameters":[{"name":"operation","value":"test-list"},'+
				'{"name":"externalJobId","value":"300820121028"},{"name":"callerId","value":"anonymous@example.com"},'+
				'{"name":"notificationURL","value":"file://dev/null"},{"name":"testList","value":"**/*"}]},{},{},{}],'+
				'"building":false,"number":11},{"actions":[{"parameters":[{"name":"operation","value":"test-list"},'+
				'{"name":"externalJobId","value":"300820121035"},{"name":"callerId","value":"anonymous@example.com"},'+
				'{"name":"notificationURL","value":"file://dev/null"},{"name":"testList","value":"**/*"}]},{},{}],'+
				'"building":false,"number":10},{"actions":[{"parameters":[{"name":"operation","value":"test-list"},'+
				'{"name":"externalJobId","value":"300820121025"},{"name":"callerId","value":"anonymous@example.com"},'+
				'{"name":"notificationURL","value":"file://dev/null"},{"name":"testList","value":"**/*"}]},{},{}],"building":false,"number":9},'+
				'{"actions":[{"parameters":[{"name":"operation","value":"test-list"},{"name":"externalJobId","value":"CorrectExternalID"},'+
				'{"name":"callerId","value":"anonymous@example.com"},{"name":"notificationURL","value":"file://dev/null"},'+
				'{"name":"testList","value":"**/*"}]},{},{}],"building":false,"number":8},'+
				'{"actions":[{"parameters":[{"name":"operation","value":"test-list"},{"name":"externalJobId","value":"240820121832"},'+
				'{"name":"callerId","value":"anonymous@example.com"},{"name":"notificationURL","value":"file://dev/null"},'+
				'{"name":"testList","value":"**/*"}]},{},{}],"building":false,"number":7}]}'
	}

}
