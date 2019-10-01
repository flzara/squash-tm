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

import org.squashtest.tm.domain.testcase.TestCase
import spock.lang.Specification
import spock.lang.Unroll

import static org.squashtest.tm.domain.testcase.TestCaseKind.GHERKIN
import static org.squashtest.tm.domain.testcase.TestCaseKind.ROBOT
import static org.squashtest.tm.service.testcase.scripted.ScriptToFileStrategy.GHERKIN_STRATEGY
import static org.squashtest.tm.service.testcase.scripted.ScriptToFileStrategy.ROBOT_STRATEGY

class ScriptToFileStrategyTest extends Specification{


	@Unroll("should select strategy '#strategy' for test case kind '#kind'")
	def "should select strategy"(){
		expect :
			ScriptToFileStrategy.strategyFor(kind) == strategy

		where :
		kind		|	strategy
		GHERKIN		|	GHERKIN_STRATEGY
		ROBOT		| 	ROBOT_STRATEGY
	}

	@Unroll("should turn '#id:#name' into '#result' with strategy #strategy")
	def "should create a filename for a test case"(){

		given:
			def strat = strategy

		expect :
			strat.createFilenameFor(testcase) == result

		where :
		strategy 			| id 		| name											| testcase		|	result
		GHERKIN_STRATEGY 	| 815		| "fetch my data"								| tc(id, name)	|	"815_fetch_my_data.feature"
		GHERKIN_STRATEGY 	| 815		| "r\u00E9cup\u00E8re mes donn\u00E9es"			| tc(id, name)	|	"815_recupere_mes_donnees.feature"
		GHERKIN_STRATEGY 	| 815		| "r\u00FCckgewinnung der Daten"				| tc(id, name)	|	"815_ruckgewinnung_der_Daten.feature"

		ROBOT_STRATEGY 		| 815		| "fetch my data"								| tc(id, name)	|	"815_fetch_my_data.robot"
		ROBOT_STRATEGY 		| 815		| "r\u00E9cup\u00E8re mes donn\u00E9es"			| tc(id, name)	|	"815_recupere_mes_donnees.robot"
		ROBOT_STRATEGY 		| 815		| "r\u00FCckgewinnung der Daten"				| tc(id, name)	|	"815_ruckgewinnung_der_Daten.robot"
	}

	@Unroll("with strategy #strategy")
	def "should create a filename for a quite long test case"(){

		given :
		def testcase = tc(815, "Oh my God this test case has such a long name that I don't think it will fit under the filename length limit")
		def strat = strategy
		when :
		def filename = strat.createFilenameFor(testcase)

		then :
		filename.length() == ScriptToFileStrategy.FILENAME_MAX_SIZE
		filename == result

		where:
		strategy 			| result
		GHERKIN_STRATEGY 	| "815_Oh_my_God_this_test_case_has_such_a_long_name_that_I_don_t_think_it_will_fit_under_the_f.feature"
		ROBOT_STRATEGY 		| "815_Oh_my_God_this_test_case_has_such_a_long_name_that_I_don_t_think_it_will_fit_under_the_fil.robot"
	}

	@Unroll("with strategy #strategy")
	def "should create the shortest gherkin filename possible"(){

		given :
		def testcase = tc(815, 256.times {"A"}+"RRGHH!")
		def strat = strategy

		when :
		def filename = strat.backupFilenameFor(testcase)

		then:
		filename == result

		where:
		strategy			| result
		GHERKIN_STRATEGY	| "815.feature"
		ROBOT_STRATEGY		| "815.robot"
	}

	def "should build a pattern that will locate the filename that corresponds to a scripted test case"(){

		given:
		def strat = strategy

		expect :
		strat.buildFilenameMatchPattern(tc(815, "name irrelevant")) == result

		where:
		strategy			| result
		GHERKIN_STRATEGY	| "815(_.*)?\\.feature"
		ROBOT_STRATEGY		| "815(_.*)?\\.robot"

	}


	def tc(id, name){
		Mock(TestCase){
			getId() >> id
			getName() >> name
		}
	}


}
