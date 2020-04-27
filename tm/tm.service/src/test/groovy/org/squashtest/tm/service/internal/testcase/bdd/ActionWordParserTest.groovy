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

	//*********** TEXT VALIDATION **************
	def "Should create an ActionWord without parameter whose text can contain any character except for double quote"() {
		when:
		ActionWord result =  new ActionWordParser().generateActionWordFromTextWithParamValue("This_is @n act1on-word with ('.,?/!§)")

		then:
		result.getWord() == "This_is @n act1on-word with ('.,?/!§)"
		List<ActionWordFragment> fragments = result.getFragments()
		fragments.size() == 1
		fragments.get(0).class.is(ActionWordFragment)
		result.getToken() == ActionWord.ACTION_WORD_TEXT_TOKEN
	}

	def "Should create an ActionWord without parameter, any multi-spaces in text will be replaced by a space"() {
		when:
		ActionWord result =  new ActionWordParser().generateActionWordFromTextWithParamValue("This_is @n    act1on-word with    ('.,?/!§)")

		then:
		//TODO: result.getWord() == "This_is @n act1on-word with ('.,?/!§)"
		List<ActionWordFragment> fragments = result.getFragments()
		fragments.size() == 1
		fragments.get(0).class.is(ActionWordFragment)
		result.getToken() == ActionWord.ACTION_WORD_TEXT_TOKEN
	}

	//*********** PARAMETER VALUE VALIDATION **************
	def "Should create an ActionWord with a parameter value at the end"() {
		when:
		ActionWord result =  new ActionWordParser().generateActionWordFromTextWithParamValue("This is an action word with \"param\"")

		then:
		//TODO: result.getWord() == "This is an action word with \"p1\""
		result.getWord() == "This is an action word with \"param\""
		List<ActionWordFragment> fragments = result.getFragments()
		fragments.size() == 2
		fragments.get(0).class.is(ActionWordFragment)
		fragments.get(1).class.is(ActionWordFragment)
		result.getToken() == "TT"
	}

	def "Should create an ActionWord with a parameter value at the end but missing a double quote"() {
		when:
		ActionWord result =  new ActionWordParser().generateActionWordFromTextWithParamValue("This is an action word with \"param")

		then:
		//TODO: result.getWord() == "This is an action word with \"p1\""
		result.getWord() == "This is an action word with \"param\""
		List<ActionWordFragment> fragments = result.getFragments()
		fragments.size() == 2
		fragments.get(0).class.is(ActionWordFragment)
		fragments.get(1).class.is(ActionWordFragment)
		result.getToken() == "TT"
	}

	def "Should create an ActionWord with a parameter value whose content is removed extra-spaces"() {
		when:
		ActionWord result =  new ActionWordParser().generateActionWordFromTextWithParamValue("This is an action word with \"     param   is    me   \"")

		then:
		//TODO: result.getWord() == "This is an action word with \"p1\""
		//TODO : param1.getValue == "param is me"
		List<ActionWordFragment> fragments = result.getFragments()
		fragments.size() == 2
		fragments.get(0).class.is(ActionWordFragment)
		fragments.get(1).class.is(ActionWordFragment)
		result.getToken() == "TT"
	}

	def "Should create an ActionWord with a parameter value whose content has spaces and special characters"() {
		when:
		ActionWord result =  new ActionWordParser().generateActionWordFromTextWithParamValue("This is an action word with \"par@m 123 []\"")

		then:
		//TODO: result.getWord() == "This is an action word with \"p1\""
		//TODO : param1.getValue == "par@m 123 []"
		List<ActionWordFragment> fragments = result.getFragments()
		fragments.size() == 2
		fragments.get(0).class.is(ActionWordFragment)
		fragments.get(1).class.is(ActionWordFragment)
		result.getToken() == "TT"
	}

	def "Should create an ActionWord with a parameter value at the beginning, in the middle and at the end"() {
		when:
		ActionWord result =  new ActionWordParser().generateActionWordFromTextWithParamValue("\"This\" is an \"action word\" with \"param\"")

		then:
		//TODO: result.getWord() == "\"p1\" is an \"p2\" with \"p3\""
		List<ActionWordFragment> fragments = result.getFragments()
		fragments.size() == 5
		fragments.get(0).class.is(ActionWordFragment)
		fragments.get(1).class.is(ActionWordFragment)
		fragments.get(2).class.is(ActionWordFragment)
		fragments.get(3).class.is(ActionWordFragment)
		fragments.get(4).class.is(ActionWordFragment)
		result.getToken() == "TTTTT"
	}

	def "Should create an ActionWord with a parameter value at the beginning, 2 parameter values in the middle which are next to each other and a parameter value at the end"() {
		when:
		ActionWord result =  new ActionWordParser().generateActionWordFromTextWithParamValue("\"This\" is an \"action\"\"word\" with \"param\"")

		then:
		//TODO: result.getWord() == "\"p1\" is an \"p2\"\"p3\" with \"p4\""
		List<ActionWordFragment> fragments = result.getFragments()
		fragments.size() == 6
		fragments.get(0).class.is(ActionWordFragment)
		fragments.get(1).class.is(ActionWordFragment)
		fragments.get(2).class.is(ActionWordFragment)
		fragments.get(3).class.is(ActionWordFragment)
		fragments.get(4).class.is(ActionWordFragment)
		fragments.get(5).class.is(ActionWordFragment)
		result.getToken() == "TTTTTT"
	}

	def "Should create an ActionWord with a parameter value at the beginning, 2 parameter values in the middle which are separated by a space and a parameter value at the end"() {
		when:
		ActionWord result =  new ActionWordParser().generateActionWordFromTextWithParamValue("\"This\" is an \"action\" \"word\" with \"param\"")

		then:
		//TODO: result.getWord() == "\"p1\" is an \"p2\" \"p3\" with \"p4\""
		List<ActionWordFragment> fragments = result.getFragments()
		fragments.size() == 7
		fragments.get(0).class.is(ActionWordFragment)
		fragments.get(1).class.is(ActionWordFragment)
		fragments.get(2).class.is(ActionWordFragment)
		fragments.get(3).class.is(ActionWordFragment)
		fragments.get(4).class.is(ActionWordFragment)
		fragments.get(5).class.is(ActionWordFragment)
		fragments.get(6).class.is(ActionWordFragment)
		result.getToken() == "TTTTTTT"
	}
}
