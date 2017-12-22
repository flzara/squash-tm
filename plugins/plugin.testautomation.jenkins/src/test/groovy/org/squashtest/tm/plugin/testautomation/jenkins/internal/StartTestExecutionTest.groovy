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

import org.squashtest.tm.core.foundation.lang.Couple
import org.squashtest.tm.domain.testautomation.AutomatedExecutionExtender
import org.squashtest.tm.domain.testautomation.AutomatedTest
import org.squashtest.tm.domain.testautomation.TestAutomationProject
import org.squashtest.tm.plugin.testautomation.jenkins.internal.net.HttpClientProvider
import org.springframework.http.client.ClientHttpRequestFactory

import spock.lang.Specification

/**
 * @author Gregory Fouquet
 *
 */
class StartTestExecutionTest extends Specification {

	BuildDef buildDef = Mock()
	TestAutomationProject project = Mock()
	HttpClientProvider clientProvider = Mock()
        
	StartTestExecution ste


	def setup() {

                clientProvider.getRequestFactoryFor(_) >> Mock(ClientHttpRequestFactory)
        
		buildDef.project >> project

		ste = new StartTestExecution(buildDef, clientProvider, "EXTERNAL-ID")
	}


	def "should marshall model into a file"() {
		given:
		AutomatedExecutionExtender exec = Mock()
		AutomatedTest test = Mock()
		test.name >> "to/the/batcave"

		exec.automatedTest >> test
		exec.id >> 12

		Map params = ["batman": "leatherpants"]

		buildDef.parameterizedExecutions >> [new Couple(exec, params)]


		when:
		File f = ste.createJsonSuite(buildDef)


		then:
		f.text.size() == 82
		f.text.startsWith("""{"test":[{""")
		f.text.endsWith("""}]}""")
		f.text.contains('"id":"12"')
		f.text.contains('"script":"to/the/batcave"')
		f.text.contains('"param":{"batman":"leatherpants"}')
		f.text.count(',') == 2

	}
}