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
package org.squashtest.tm.plugin.testautomation.jenkins.internal.net

import org.squashtest.tm.domain.testautomation.AutomatedTest
import org.squashtest.tm.domain.testautomation.TestAutomationProject
import org.squashtest.tm.domain.testautomation.TestAutomationServer
import org.squashtest.tm.service.testautomation.model.TestAutomationProjectContent
import spock.lang.Specification

class HttpRequestFactoryTest extends Specification {

	private HttpRequestFactory factory

	def setup(){
		factory = new HttpRequestFactory()
	}

	def "should return a well formatted query"(){

		given :
		TestAutomationServer server = new TestAutomationServer("server", new URL("http://ci.jruby.org/server"), "", "", "jenkins")

		when :
		def method = factory.newGetJobsMethod(server)

		then :
		method.getURI().toString() == "http://ci.jruby.org/server/api/json?tree=jobs%5Bname%2Ccolor%5D"
	}

	def "should create the result path for tests being at the root of the project"(){
		given :
		AutomatedTest test = new AutomatedTest("tests/mon-test.txt", null)

		when :
		def res = factory.toRelativePath(test)

		then :
		res == "(root)/tests/mon_test_txt"
	}

	def "should create the crappy result path for tests being in deeper folders of the project"(){

		given :
		AutomatedTest test = new AutomatedTest("tests/subfolder/re-test.txt", null)

		when:
		def res = factory.toRelativePath(test)

		then :
		res == "tests/subfolder/re_test_txt"
	}
}

