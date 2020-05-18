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
import org.squashtest.tm.domain.bdd.ActionWordParameter
import org.squashtest.tm.domain.bdd.ActionWordParameterValue
import org.squashtest.tm.domain.bdd.ActionWordText
import spock.lang.Specification

class ActionWordParserTest extends Specification {

	//*********** TEXT VALIDATION **************
	def "Should throw error when creating an ActionWord without text" () {
		when:
		new ActionWordParser().generateActionWordFromTextWithParamValue("\"This_is @n act1on-word\"")

		then:
		thrown(IllegalArgumentException)
	}

	def "Should create an ActionWord without parameter whose texts contain special characters except for double quote"() {
		when:
		ActionWord result = new ActionWordParser().generateActionWordFromTextWithParamValue("This_is @n act1on-word with ('.,?/!§)")

		then:
		result.getWord() == "This_is @n act1on-word with ('.,?/!§)"
		result.getToken() == "T-This_is @n act1on-word with ('.,?/!§)-"
		def fragments = result.getFragments()
		fragments.size() == 1

		def f1 = fragments.get(0)
		f1.class.is(ActionWordText)
		((ActionWordText) f1).getText() == "This_is @n act1on-word with ('.,?/!§)"
	}

	def "Should create an ActionWord without parameter, any multi-spaces will be removed"() {
		when:
		ActionWord result = new ActionWordParser().generateActionWordFromTextWithParamValue("This_is @n    act1on-word with    ('.,?/!§)")

		then:
		result.getWord() == "This_is @n    act1on-word with    ('.,?/!§)"
		result.getToken() == "T-This_is @n act1on-word with ('.,?/!§)-"
		def fragments = result.getFragments()
		fragments.size() == 1
		def f1 = fragments.get(0)
		f1.class.is(ActionWordText)
		((ActionWordText) f1).getText() == "This_is @n act1on-word with ('.,?/!§)"
	}

	//*********** PARAMETER VALIDATION **************
	def "Should create an ActionWord with a parameter value at the end"() {
		when:
		ActionWordParser parser = new ActionWordParser()
		ActionWord result = parser.generateActionWordFromTextWithParamValue("This is an action word with \"param\"")
		def values = parser.getParameterValues()

		then:
		result.getWord() == "This is an action word with \"param1\""
		result.getToken() == "TP-This is an action word with -"
		def fragments = result.getFragments()
		fragments.size() == 2

		def f1 = fragments.get(0)
		f1.class.is(ActionWordText)
		((ActionWordText) f1).getText() == "This is an action word with "

		def f2 = fragments.get(1)
		f2.class.is(ActionWordParameter)
		ActionWordParameter parameter = (ActionWordParameter) f2
		parameter.getName() == "param1"
		parameter.getDefaultValue() == ""

		values.size() == 1
		def value = values.get(0)
		value.getValue() == "param"
		value.getActionWordParam() == null
		value.getKeywordTestStep() == null
	}

	def "Should create an ActionWord with a parameter value at the end but missing a double quote"() {
		when:
		ActionWordParser parser = new ActionWordParser()
		ActionWord result = parser.generateActionWordFromTextWithParamValue("This is an action word with \"param")
		def values = parser.getParameterValues()

		then:
		result.getWord() == "This is an action word with \"param1\""
		result.getToken() == "TP-This is an action word with -"
		def fragments = result.getFragments()
		fragments.size() == 2

		def f1 = fragments.get(0)
		f1.class.is(ActionWordText)
		((ActionWordText) f1).getText() == "This is an action word with "

		def f2 = fragments.get(1)
		f2.class.is(ActionWordParameter)
		ActionWordParameter parameter = (ActionWordParameter) f2
		parameter.getName() == "param1"
		parameter.getDefaultValue() == ""

		values.size() == 1
		values.get(0).getValue() == "param"
	}

	def "Should create an ActionWord with a parameter value which has spaces, special characters"() {
		when:
		ActionWordParser parser = new ActionWordParser()
		ActionWord result = parser.generateActionWordFromTextWithParamValue("This is an action word with \"     par@m   123    []   \"")
		def values = parser.getParameterValues()

		then:
		result.getWord() == "This is an action word with \"param1\""
		result.getToken() == "TP-This is an action word with -"
		def fragments = result.getFragments()
		fragments.size() == 2

		def f1 = fragments.get(0)
		f1.class.is(ActionWordText)
		((ActionWordText) f1).getText() == "This is an action word with "

		def f2 = fragments.get(1)
		f2.class.is(ActionWordParameter)
		ActionWordParameter parameter = (ActionWordParameter) f2
		parameter.getName() == "param1"

		values.size() == 1
		values.get(0).getValue() == "par@m 123 []"
	}

	def "Should create an ActionWord with 3 parameter values: at the beginning, in the middle and at the end"() {
		when:
		ActionWordParser parser = new ActionWordParser()
		ActionWord result = parser.generateActionWordFromTextWithParamValue("\"This\" is   an \"action word\" with   \"param\"")
		def values = parser.getParameterValues()

		then:
		result.getWord() == "\"param1\" is an \"param2\" with \"param3\""
		result.getToken() == "PTPTP- is an - with -"
		def fragments = result.getFragments()
		fragments.size() == 5

		def f1 = fragments.get(0)
		f1.class.is(ActionWordParameter)
		ActionWordParameter parameter1 = (ActionWordParameter) f1
		parameter1.getName() == "param1"

		def f2 = fragments.get(1)
		f2.class.is(ActionWordText)
		((ActionWordText) f2).getText() == " is an "

		def f3 = fragments.get(2)
		f3.class.is(ActionWordParameter)
		ActionWordParameter parameter2 = (ActionWordParameter) f3
		parameter2.getName() == "param2"

		def f4 = fragments.get(3)
		f4.class.is(ActionWordText)
		((ActionWordText) f4).getText() == " with "

		def f5 = fragments.get(4)
		f5.class.is(ActionWordParameter)
		ActionWordParameter parameter3 = (ActionWordParameter) f5
		parameter3.getName() == "param3"

		values.size() == 3
		values.get(0).getValue() == "This"
		values.get(1).getValue() == "action word"
		values.get(2).getValue() == "param"

	}

	def "Should create an ActionWord with 4 parameter values: 1 at the beginning, 2 parameter values in the middle which are next to each other and 1 at the end"() {
		when:
		ActionWordParser parser = new ActionWordParser()
		ActionWord result = parser.generateActionWordFromTextWithParamValue("\"This\" is an\"action\"\"word\" with \"param\"")
		def values = parser.getParameterValues()

		then:
		result.getWord() == "\"param1\" is an\"param2\"\"param3\" with \"param4\""
		result.getToken() == "PTPPTP- is an- with -"
		def fragments = result.getFragments()
		fragments.size() == 6

		def f1 = fragments.get(0)
		f1.class.is(ActionWordParameter)
		ActionWordParameter parameter1 = (ActionWordParameter) f1
		parameter1.getName() == "param1"

		def f2 = fragments.get(1)
		f2.class.is(ActionWordText)
		((ActionWordText) f2).getText() == " is an"

		def f3 = fragments.get(2)
		f3.class.is(ActionWordParameter)
		ActionWordParameter parameter2 = (ActionWordParameter) f3
		parameter2.getName() == "param2"

		def f4 = fragments.get(3)
		f4.class.is(ActionWordParameter)
		ActionWordParameter parameter3 = (ActionWordParameter) f4
		parameter3.getName() == "param3"

		def f5 = fragments.get(4)
		f5.class.is(ActionWordText)
		((ActionWordText) f5).getText() == " with "

		def f6 = fragments.get(5)
		f6.class.is(ActionWordParameter)
		ActionWordParameter parameter4 = (ActionWordParameter) f6
		parameter4.getName() == "param4"

		values.size() == 4
		values.get(0).getValue() == "This"
		values.get(1).getValue() == "action"
		values.get(2).getValue() == "word"
		values.get(3).getValue() == "param"
	}

	def "Should create an ActionWord with 4 parameter values: 1 at the beginning, 2 parameter values in the middle which are separated by a space and 1 parameter value at the end"() {
		when:
		ActionWordParser parser = new ActionWordParser()
		ActionWord result = parser.generateActionWordFromTextWithParamValue("\"This\" is an \"action\"    \"word\" with \"param\"")
		def values = parser.getParameterValues()

		then:
		result.getWord() == "\"param1\" is an \"param2\" \"param3\" with \"param4\""
		result.getToken() == "PTPTPTP- is an - - with -"
		def fragments = result.getFragments()
		fragments.size() == 7

		def f1 = fragments.get(0)
		f1.class.is(ActionWordParameter)
		ActionWordParameter parameter1 = (ActionWordParameter) f1
		parameter1.getName() == "param1"

		def f2 = fragments.get(1)
		f2.class.is(ActionWordText)
		((ActionWordText) f2).getText() == " is an "

		def f3 = fragments.get(2)
		f3.class.is(ActionWordParameter)
		ActionWordParameter parameter2 = (ActionWordParameter) f3
		parameter2.getName() == "param2"

		def f4 = fragments.get(3)
		f4.class.is(ActionWordText)
		((ActionWordText) f4).getText() == " "

		def f5 = fragments.get(4)
		f5.class.is(ActionWordParameter)
		ActionWordParameter parameter3 = (ActionWordParameter) f5
		parameter3.getName() == "param3"

		def f6 = fragments.get(5)
		f6.class.is(ActionWordText)
		((ActionWordText) f6).getText() == " with "

		def f7 = fragments.get(6)
		f7.class.is(ActionWordParameter)
		ActionWordParameter parameter4 = (ActionWordParameter) f7
		parameter4.getName() == "param4"

		values.size() == 4
		values.get(0).getValue() == "This"
		values.get(1).getValue() == "action"
		values.get(2).getValue() == "word"
		values.get(3).getValue() == "param"
	}

}
