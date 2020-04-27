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
package org.squashtest.tm.service.internal.testcase.bdd


import org.squashtest.tm.domain.bdd.ActionWord
import org.squashtest.tm.domain.bdd.ActionWordFragment
import spock.lang.Specification

class ActionWordParserTest extends Specification {

	def "Should create an ActionWord without parameter"() {
		when:
		ActionWord result =  new ActionWordParser().generateActionWordFromTextWithParamValue("This is an action word")

		then:
		result.getWord() == "This is an action word"
		List<ActionWordFragment> fragments = result.getFragments()
		fragments.size() == 1
		fragments.get(0).class.is(ActionWordFragment)
		result.getToken() == ActionWord.ACTION_WORD_TEXT_TOKEN
	}

	def "Should create an ActionWord with a parameter at the end"() {
		when:
		ActionWord result =  new ActionWordParser().generateActionWordFromTextWithParamValue("This is an action word with \"param\"")

		then:
		//result.getWord() == "This is an action word with \"p1\""
		result.getWord() == "This is an action word with \"param\""
		List<ActionWordFragment> fragments = result.getFragments()
		fragments.size() == 2
		fragments.get(0).class.is(ActionWordFragment)
		fragments.get(1).class.is(ActionWordFragment)
		result.getToken() == "TT"
	}

	def "Should create an ActionWord with a parameter at the end but missing a double quote"() {
		when:
		ActionWord result =  new ActionWordParser().generateActionWordFromTextWithParamValue("This is an action word with \"param")

		then:
		//result.getWord() == "This is an action word with \"p1\""
		result.getWord() == "This is an action word with \"param\""
		List<ActionWordFragment> fragments = result.getFragments()
		fragments.size() == 2
		fragments.get(0).class.is(ActionWordFragment)
		fragments.get(1).class.is(ActionWordFragment)
		result.getToken() == "TT"
	}

	def "Should create an ActionWord with a parameter at the end and a parameter in the middle"() {
		when:
		ActionWord result =  new ActionWordParser().generateActionWordFromTextWithParamValue("This is an \"action word\" with \"param\"")

		then:
		//result.getWord() == "This is an action word with \"p1\""
		result.getWord() == "This is an \"action word\" with \"param\""
		List<ActionWordFragment> fragments = result.getFragments()
		fragments.size() == 4
		fragments.get(0).class.is(ActionWordFragment)
		fragments.get(1).class.is(ActionWordFragment)
		fragments.get(2).class.is(ActionWordFragment)
		fragments.get(3).class.is(ActionWordFragment)
		result.getToken() == "TTTT"
	}
}
