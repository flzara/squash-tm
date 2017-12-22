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

class GatherTestListTest extends JenkinsConnectorSpec {

	GatherTestList gatherList;
	
	def setup(){
		
		gatherList = new GatherTestList()
		gatherList.client = client
		gatherList.method = method
		gatherList.parser = parser;
		
	}
	
	def "should find a list of tests"(){
		
		given :
			def json = makeJson()
			RequestExecutor.INSTANCE.execute(_,_) >> json
		
		and :
			def expected = [
						  "tests/autrestests/othertest1.txt", 
						  "tests/database-tests/dbtest-1.txt", 
						  "tests/database-tests/dbtest-2.txt",
						  "tests/vcs.txt"
						  ]
		
		when :
			gatherList.perform()
		
		then :
			gatherList.testNames == expected
		
	}
	
	
	def makeJson(){
		return '{"name":"tests",'+
				'"contents":[{"name":"autrestests","contents":[{"name":"othertest1.txt","contents":null}]},'+
				'{"name":"database-tests","contents":[{"name":"dbtest-1.txt","contents":null},'+
				'{"name":"dbtest-2.txt","contents":null}]},{"name" : "vcs.txt", "contents" : null}]}'
	}
}
