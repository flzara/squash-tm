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
import spock.lang.Unroll

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

	@Unroll("should turn '#id:#name' into '#result'")
	def "should create a gherkin filename for a test case"(){
		expect :
		def testcase = new MockTC(id, name)
		testcase.createFilename() == result

		where :
		id 		| name										|	result
		815		| "fetch my data"							|	"815_fetch_my_data.feature"
		815		| "r\u00E9cup\u00E8re mes donn\u00E9es"		|	"815_recupere_mes_donnees.feature"
		815		| "r\u00FCckgewinnung der Daten"			|	"815_ruckgewinnung_der_Daten.feature"
	}

	@Unroll("#backupFilenameFor - should turn '#id:#name' into '#result'")
	def "should create a gherkin backup filename for a test case"(){
		expect :
		new MockTC(47L).createBackupFileName() == "47.feature"
	}

	def "should build filename match pattern"() {
		expect:
		new MockTC(44L, "holaTc").buildFilenameMatchPattern() == "44(_.*)?\\.feature"
	}

	class MockTC extends ScriptedTestCase{
		Long overId

		MockTC(Long id) {
			overId = id
		}

		MockTC(Long id, String name) {
			overId = id
			this.name=name
		}

		public Long getId() {
			return overId
		}

		public void setId(Long newId) {
			overId=newId
		}

	}
}
