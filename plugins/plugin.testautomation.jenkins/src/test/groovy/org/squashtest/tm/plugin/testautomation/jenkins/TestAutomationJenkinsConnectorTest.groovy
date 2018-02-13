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
package org.squashtest.tm.plugin.testautomation.jenkins

import org.squashtest.tm.domain.testautomation.TestAutomationProject
import org.squashtest.tm.domain.testautomation.TestAutomationServer
import spock.lang.Specification
import spock.lang.Unroll

class TestAutomationJenkinsConnectorTest extends Specification {

	@Unroll
	def "should create a correct job path"(){
		given:
		def connector = new TestAutomationJenkinsConnector()

		and:
		TestAutomationServer server = new TestAutomationServer();
		server.setBaseURL(new URL(baseUrl))
		TestAutomationProject testAutomationProject = new TestAutomationProject(jobName,"label", server);

		when:
		def path = connector.getJobPath(testAutomationProject);

		then:
		path == expectedPath

		where:
		baseUrl 					| jobName 						|| expectedPath
		"http://localhost:8080"		| "heroquest"		  			||"http://localhost:8080/job/heroquest"
		"http://forge.net"			| "dossier/heroquest"		  	||"http://forge.net/job/dossier/job/heroquest"
		"http://forge.net"			| "dossier/toto/titi/heroquest"	||"http://forge.net/job/dossier/job/toto/job/titi/job/heroquest"
	}
}
