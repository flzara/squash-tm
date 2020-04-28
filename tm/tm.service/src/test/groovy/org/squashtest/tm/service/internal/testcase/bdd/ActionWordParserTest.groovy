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
import org.squashtest.tm.domain.bdd.ActionWordText
import spock.lang.Specification

class ActionWordParserTest extends Specification {

	//*********** TEXT VALIDATION **************
	def "Should create an ActionWord without parameter whose text can contain any character except for double quote"() {
		when:
		ActionWord result = new ActionWordParser().generateActionWordFromTextWithParamValue("This_is @n act1on-word with ('.,?/!§)")

		then:
		result.getWord() == "This_is @n act1on-word with ('.,?/!§)"
		result.getToken() == "T-This_is @n act1on-word with ('.,?/!§)-"
		List<ActionWordFragment> fragments = result.getFragments()
		fragments.size() == 1
		def f1 = fragments.get(0)
		f1.class.is(ActionWordText)
		((ActionWordText) f1).getText() == "This_is @n act1on-word with ('.,?/!§)"
	}

	def "Should create an ActionWord without parameter, any multi-spaces in text will be replaced by a space"() {
		when:
		ActionWord result = new ActionWordParser().generateActionWordFromTextWithParamValue("This_is @n    act1on-word with    ('.,?/!§)")

		then:
		result.getWord() == "This_is @n    act1on-word with    ('.,?/!§)"
		result.getToken() == "T-This_is @n act1on-word with ('.,?/!§)-"
		List<ActionWordFragment> fragments = result.getFragments()
		fragments.size() == 1
		def f1 = fragments.get(0)
		f1.class.is(ActionWordText)
		((ActionWordText) f1).getText() == "This_is @n act1on-word with ('.,?/!§)"
	}

	//*********** PARAMETER VALUE VALIDATION **************
	def "Should create an ActionWord with a parameter value at the end"() {
		when:
		ActionWord result = new ActionWordParser().generateActionWordFromTextWithParamValue("This is an action word with \"param\"")

		then:
		result.getWord() == "This is an action word with \"param\""
		result.getToken() == "TP-This is an action word with -"
		List<ActionWordFragment> fragments = result.getFragments()
		fragments.size() == 2
		def f1 = fragments.get(0)
		f1.class.is(ActionWordText)
		((ActionWordText) f1).getText() == "This is an action word with "

		def f2 = fragments.get(1)
		f2.class.is(ActionWordParameter)
		ActionWordParameter parameter = (ActionWordParameter) f2
		parameter.getName() == "p1"
		parameter.getDefaultValue() == ""
		def values = parameter.getValues()
		values.size() == 1
		values.get(0).getValue() == "param"
	}

	def "Should create an ActionWord with a parameter value at the end but missing a double quote"() {
		when:
		ActionWord result = new ActionWordParser().generateActionWordFromTextWithParamValue("This is an action word with \"param")

		then:
		result.getWord() == "This is an action word with \"param\""
		result.getToken() == "TP-This is an action word with -"
		List<ActionWordFragment> fragments = result.getFragments()
		fragments.size() == 2
	}

	def "Should create an ActionWord with a parameter value whose content content has spaces and special characters and is removed extra-spaces"() {
		when:
		ActionWord result = new ActionWordParser().generateActionWordFromTextWithParamValue("This is an action word with \"     par@m   123    []   \"")

		then:
		result.getWord() == "This is an action word with \"     par@m   123    []   \""
		result.getToken() == "TP-This is an action word with -"
		List<ActionWordFragment> fragments = result.getFragments()
		fragments.size() == 2
		def f1 = fragments.get(0)
		f1.class.is(ActionWordText)
		((ActionWordText) f1).getText() == "This is an action word with "

		def f2 = fragments.get(1)
		f2.class.is(ActionWordParameter)
		ActionWordParameter parameter = (ActionWordParameter) f2
		parameter.getName() == "p1"
		def values = parameter.getValues()
		values.size() == 1
		values.get(0).getValue() == "par@m 123 []"
	}

	def "Should create an ActionWord with a parameter value at the beginning, in the middle and at the end"() {
		when:
		ActionWord result = new ActionWordParser().generateActionWordFromTextWithParamValue("\"This\" is   an \"action word\" with   \"param\"")

		then:
		result.getWord() == "\"This\" is   an \"action word\" with   \"param\""
		result.getToken() == "PTPTP- is an - with -"
		List<ActionWordFragment> fragments = result.getFragments()
		fragments.size() == 5
		def f1 = fragments.get(0)
		f1.class.is(ActionWordParameter)
		ActionWordParameter parameter1 = (ActionWordParameter) f1
		parameter1.getName() == "p1"
		def values1 = parameter1.getValues()
		values1.size() == 1
		values1.get(0).getValue() == "This"

		def f2 = fragments.get(1)
		f2.class.is(ActionWordText)
		((ActionWordText) f2).getText() == " is an "

		def f3 = fragments.get(2)
		f3.class.is(ActionWordParameter)
		ActionWordParameter parameter2 = (ActionWordParameter) f3
		parameter2.getName() == "p2"
		def values2 = parameter2.getValues()
		values2.size() == 1
		values2.get(0).getValue() == "action word"

		def f4 = fragments.get(3)
		f4.class.is(ActionWordText)
		((ActionWordText) f4).getText() == " with "

		def f5 = fragments.get(4)
		f5.class.is(ActionWordParameter)
		ActionWordParameter parameter3 = (ActionWordParameter) f5
		parameter3.getName() == "p3"
		def values3 = parameter3.getValues()
		values3.size() == 1
		values3.get(0).getValue() == "param"
	}

	def "Should create an ActionWord with a parameter value at the beginning, 2 parameter values in the middle which are next to each other and a parameter value at the end"() {
		when:
		ActionWord result = new ActionWordParser().generateActionWordFromTextWithParamValue("\"This\" is an\"action\"\"word\" with \"param\"")

		then:
		result.getWord() == "\"This\" is an\"action\"\"word\" with \"param\""
		result.getToken() == "PTPPTP- is an- with -"
		List<ActionWordFragment> fragments = result.getFragments()
		fragments.size() == 6
		def f1 = fragments.get(0)
		f1.class.is(ActionWordParameter)
		ActionWordParameter parameter1 = (ActionWordParameter) f1
		parameter1.getName() == "p1"
		def values1 = parameter1.getValues()
		values1.size() == 1
		values1.get(0).getValue() == "This"

		def f2 = fragments.get(1)
		f2.class.is(ActionWordText)
		((ActionWordText) f2).getText() == " is an"

		def f3 = fragments.get(2)
		f3.class.is(ActionWordParameter)
		ActionWordParameter parameter2 = (ActionWordParameter) f3
		parameter2.getName() == "p2"
		def values2 = parameter2.getValues()
		values2.size() == 1
		values2.get(0).getValue() == "action"

		def f4 = fragments.get(3)
		f4.class.is(ActionWordParameter)
		ActionWordParameter parameter3 = (ActionWordParameter) f4
		parameter3.getName() == "p3"
		def values3 = parameter3.getValues()
		values3.size() == 1
		values3.get(0).getValue() == "word"

		def f5 = fragments.get(4)
		f5.class.is(ActionWordText)
		((ActionWordText) f5).getText() == " with "

		def f6 = fragments.get(5)
		f6.class.is(ActionWordParameter)
		ActionWordParameter parameter4 = (ActionWordParameter) f6
		parameter4.getName() == "p4"
		def values4 = parameter4.getValues()
		values4.size() == 1
		values4.get(0).getValue() == "param"
	}

	def "Should create an ActionWord with a parameter value at the beginning, 2 parameter values in the middle which are separated by a space and a parameter value at the end"() {
		when:
		ActionWord result = new ActionWordParser().generateActionWordFromTextWithParamValue("\"This\" is an \"action\"    \"word\" with \"param\"")

		then:
		result.getWord() == "\"This\" is an \"action\"    \"word\" with \"param\""
		result.getToken() == "PTPTPTP- is an - - with -"
		List<ActionWordFragment> fragments = result.getFragments()
		fragments.size() == 7
		def f1 = fragments.get(0)
		f1.class.is(ActionWordParameter)
		ActionWordParameter parameter1 = (ActionWordParameter) f1
		parameter1.getName() == "p1"
		def values1 = parameter1.getValues()
		values1.size() == 1
		values1.get(0).getValue() == "This"

		def f2 = fragments.get(1)
		f2.class.is(ActionWordText)
		((ActionWordText) f2).getText() == " is an "

		def f3 = fragments.get(2)
		f3.class.is(ActionWordParameter)
		ActionWordParameter parameter2 = (ActionWordParameter) f3
		parameter2.getName() == "p2"
		def values2 = parameter2.getValues()
		values2.size() == 1
		values2.get(0).getValue() == "action"

		def f4 = fragments.get(3)
		f4.class.is(ActionWordText)
		((ActionWordText) f4).getText() == " "

		def f5 = fragments.get(4)
		f5.class.is(ActionWordParameter)
		ActionWordParameter parameter3 = (ActionWordParameter) f5
		parameter3.getName() == "p3"
		def values3 = parameter3.getValues()
		values3.size() == 1
		values3.get(0).getValue() == "word"

		def f6 = fragments.get(5)
		f6.class.is(ActionWordText)
		((ActionWordText) f6).getText() == " with "

		def f7 = fragments.get(6)
		f7.class.is(ActionWordParameter)
		ActionWordParameter parameter4 = (ActionWordParameter) f7
		parameter4.getName() == "p4"
		def values4 = parameter4.getValues()
		values4.size() == 1
		values4.get(0).getValue() == "param"
	}

	//*********** TEXT VALIDATION **************

	//*********** PARAMETER NAME VALIDATION **************
}
