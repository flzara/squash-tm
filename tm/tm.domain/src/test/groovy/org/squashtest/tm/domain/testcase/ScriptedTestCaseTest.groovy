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
package org.squashtest.tm.domain.testcase


import org.squashtest.tm.domain.testutils.MockFactory
import org.squashtest.tm.tools.unittest.assertions.CollectionAssertions
import spock.lang.Specification

class ScriptedTestCaseTest extends Specification {

	MockFactory mockFactory = new MockFactory()

	def setup() {
		CollectionAssertions.declareContainsExactly()
	}

	def "copy of a scripted test case should have the same script"() {
		given:
		ScriptedTestCase source = new ScriptedTestCase()
		source.setName("source")
		source.notifyAssociatedWithProject(mockFactory.mockProject())
		String script = "I am the script"
		source.setScript(script)

		when:
		def copy = source.createCopy()

		then:
		copy.script == script
	}


}
