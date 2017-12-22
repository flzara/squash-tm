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
package org.squashtest.tm.plugin.testautomation.jenkins.internal

import org.apache.http.StatusLine
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.impl.client.CloseableHttpClient
import org.squashtest.tm.plugin.testautomation.jenkins.internal.net.RequestExecutor
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasksteps.BuildAbsoluteId
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasksteps.GetBuildID

import spock.lang.Specification

abstract class JenkinsConnectorSpec extends Specification {
	GetBuildID getID = new GetBuildID()
	CloseableHttpClient client = Mock()
	HttpUriRequest method = Mock()
	BuildAbsoluteId absoluteId;
	JsonParser parser= new JsonParser()
	CloseableHttpResponse resp = Mock()
	
	def setup(){
		StatusLine status = Mock()
		status.getStatusCode() >> 200
		resp.getStatusLine() >> status

		getID.client = client
		getID.method = method
		getID.parser = parser;
		getID.absoluteId = new BuildAbsoluteId("CorrectJob", "CorrectExternalID")

		client.execute(method) >> resp
		
		RequestExecutor.INSTANCE = Mock(RequestExecutor)
	}

}
