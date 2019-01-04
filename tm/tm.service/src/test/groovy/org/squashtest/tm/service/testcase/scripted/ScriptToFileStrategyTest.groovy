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
package org.squashtest.tm.service.testcase.scripted


import static org.squashtest.tm.domain.testcase.TestCaseKind.GHERKIN
import static org.squashtest.tm.service.testcase.scripted.ScriptToFileStrategy.GHERKIN_STRATEGY
import org.squashtest.tm.domain.testcase.TestCase
import spock.lang.Specification
import spock.lang.Unroll

class ScriptToFileStrategyTest extends Specification{


	@Unroll("should select strategy '#strategy' for test case kind '#kind'")
	def "should select strategy"(){
		expect :
			ScriptToFileStrategy.strategyFor(kind) == strategy

		where :
		kind		|	strategy
		GHERKIN		|	GHERKIN_STRATEGY
	}

	@Unroll("should turn '#id:#name' into '#result'")
	def "should create a gherkin filename for a test case"(){

		expect :
			GHERKIN_STRATEGY.createFilenameFor(testcase) == result

		where :
		id 		| name										|	result									| testcase
		815		| "fetch my data"							|	"815_fetch_my_data.feature"				| tc(id, name)
		815		| "r\u00E9cup\u00E8re mes donn\u00E9es"		|	"815_recupere_mes_donnees.feature"		| tc(id, name)
		815		| "r\u00FCckgewinnung der Daten"			|	"815_ruckgewinnung_der_Daten.feature"	| tc(id, name)

	}


	def "should create a gherkin filename for a quite long test case name"(){

		given :
		def testcase = tc(815, "Oh my God this test case has such a long name that I don't think it will fit under the filename length limit")

		when :
		def filename = GHERKIN_STRATEGY.createFilenameFor(testcase)

		then :
		filename.length() == ScriptToFileStrategy.FILENAME_MAX_SIZE
		filename == "815_Oh_my_God_this_test_case_has_such_a_long_name_that_I_don_t_think_it_will_fit_under_the_f.feature"

	}


	def "should create the shortest gherkin filename possible"(){

		given :
		def testcase = tc(815, 256.times {"A"}+"RRGHH!")

		when :
		def filename = GHERKIN_STRATEGY.backupFilenameFor(testcase)

		then:
		filename == "815.feature"
	}


	def "should build a pattern that will locate the filename that corresponds to a scripted test case"(){

		expect :
		GHERKIN_STRATEGY.buildFilenameMatchPattern(tc(815, "name irrelevant")) == "815(_.*)?\\.feature"

	}



	def tc(id, name){
		Mock(TestCase){
			getId() >> id
			getName() >> name
		}
	}


}
