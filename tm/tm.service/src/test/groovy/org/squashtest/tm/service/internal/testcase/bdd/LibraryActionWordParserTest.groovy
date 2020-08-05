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
import org.squashtest.tm.domain.bdd.ActionWordParameter
import org.squashtest.tm.domain.bdd.ActionWordText
import org.squashtest.tm.exception.actionword.InvalidActionWordInputException
import org.squashtest.tm.exception.actionword.InvalidActionWordInputNameException
import org.squashtest.tm.exception.actionword.InvalidActionWordParameterNameException
import spock.lang.Specification
import spock.lang.Unroll

class LibraryActionWordParserTest extends Specification {

	//************** GENERAL **************
	@Unroll
	def "Should throw error when creating an empty ActionWord in AW library"() {
		when:
		new LibraryActionWordParser().createActionWordInLibrary(word)

		then:
		InvalidActionWordInputException exception = thrown()
		exception.message == "Action word cannot be empty."

		where:
		word << [null, ""]
	}

	def "Should throw error when creating an ActionWord exceeding 255 characters in AW library"() {
		when:
		new LibraryActionWordParser().createActionWordInLibrary("aaaaaaa \"ppp\" aaaaaaaaaaaaaaaaaaa \"ppppp\" aaaaaaaaaaaaaaaaaaaaaaaaaa \"pppppp\" aaaaaaaaaaaaaa \"ppp\" aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa \"pppp\" aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa \"ppp\" aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa \"pppp\" aaaaaaaaaaaaaaaaaaaaaaaaaaaa")

		then:
		InvalidActionWordInputException exception = thrown()
		exception.message == "Action word cannot exceed 255 characters."
	}

	@Unroll
	def "Should throw error when creating an ActionWord without text in AW library"() {
		when:
		new LibraryActionWordParser().createActionWordInLibrary(word)

		then:
		InvalidActionWordInputException exception = thrown()
		exception.message == "Action word must contain at least some texts."

		where:
		word << ["\"This_is An act1on-word\"", "3000\"abc\"", "3000",
				 "1\"2\"", "1,5\"2\"", "-1.5\"2\"", "\"9\"-1\"2\""]
	}


	//************** TEXT VALIDATION **************
	def "Should create an ActionWord without parameter whose texts contain special characters except for double quote and <, > in AW library"() {
		when:
		ActionWord result = new LibraryActionWordParser().createActionWordInLibrary("This_is @n act1on-word with ('.,?/!§)")

		then:
		result.createWord() == "This_is @n act1on-word with ('.,?/!§)"
		result.getToken() == "T-This_is @n act1on-word with ('.,?/!§)-"
		def fragments = result.getFragments()
		fragments.size() == 1

		def f1 = fragments.get(0)
		f1.class.is(ActionWordText)
		((ActionWordText) f1).getText() == "This_is @n act1on-word with ('.,?/!§)"
	}

	@Unroll
	def "Should throw error when creating an ActionWord with invalid character > in text in AW library"() {
		when:
		new LibraryActionWordParser().createActionWordInLibrary(word)

		then:
		InvalidActionWordInputNameException exception = thrown()
		exception.message == "Action word cannot contain '>' symbol."

		where:
		word << ["Invalid text with >", "Invalid text with > in the middle", "> at the beginning"]
	}

	@Unroll
	def "Should throw error when creating an ActionWord with invalid character < in text in AW library"() {
		when:
		new LibraryActionWordParser().createActionWordInLibrary(word)

		then:
		InvalidActionWordInputNameException exception = thrown()
		exception.message == "Action word cannot contain '<' symbol."

		where:
		word << ["Invalid text with <", "Invalid text with < in the middle", "< at the beginning"]
	}

	def "Should create an ActionWord without parameter, any multi-spaces will be removed in AW library"() {
		when:
		ActionWord result = new LibraryActionWordParser().createActionWordInLibrary("This_is @n    act1on-word with    ('.,?/!§)")

		then:
		result.createWord() == "This_is @n act1on-word with ('.,?/!§)"
		result.getToken() == "T-This_is @n act1on-word with ('.,?/!§)-"
		def fragments = result.getFragments()
		fragments.size() == 1
		def f1 = fragments.get(0)
		f1.class.is(ActionWordText)
		((ActionWordText) f1).getText() == "This_is @n act1on-word with ('.,?/!§)"
	}


	//************** NUMBER IN TEXT AS PARAM VALUE VALIDATION **************
	@Unroll
	def "Should create an ActionWord with 1 parameter value as number in text in AW library"(){
		when:
		def parser = new LibraryActionWordParser()
		ActionWord result = parser.createActionWordInLibrary(word+number)

		then:
		result.createWord() == word+"\"param1\""
		result.getToken() == "TP-"+word+"-"
		def fragments = result.getFragments()
		fragments.size() == 2

		def f1 = fragments.get(0)
		f1.class.is(ActionWordText)
		((ActionWordText) f1).getText() == word

		def f2 = fragments.get(1)
		f2.class.is(ActionWordParameter)
		ActionWordParameter parameter = (ActionWordParameter) f2
		parameter.getName() == "param1"
		parameter.getDefaultValue() == number

		where:
		word << ["1.5action word with ", "This is1.5action word with ", "this is 1.5action word with "]
		number << ["2", "1,5", "-1.6"]
	}


	//************** FREE VALUE PARAMETER VALIDATION **************
	@Unroll
	def "Should throw error when creating an ActionWord with invalid character > in param in AW library"() {
		when:
		new LibraryActionWordParser().createActionWordInLibrary(word)
		then:

		InvalidActionWordInputNameException exception = thrown()
		exception.message == "Action word cannot contain '>' symbol."

		where:
		word << ["This is an action word with \" > param\"", "This is an action word with \" a > param\"", "This is an action word with \" a param >\""]
	}

	@Unroll
	def "Should throw error when creating an ActionWord with invalid character < in param in AW library"() {
		when:
		new LibraryActionWordParser().createActionWordInLibrary(word)
		then:

		InvalidActionWordInputNameException exception = thrown()
		exception.message == "Action word cannot contain '<' symbol."

		where:
		word << ["This is an action word with \" < param\"", "This is an action word with \" a < param\"", "This is an action word with \" a param <\""]
	}

	@Unroll
	def "Should throw error when creating an ActionWord with invalid characters in param in AW library"() {
		when:
		new LibraryActionWordParser().createActionWordInLibrary(word)
		then:

		InvalidActionWordParameterNameException exception = thrown()
		exception.message == "Action word parameter name can contain only alphanumeric, - or _ characters."

		where:
		word << ["An action word with \" p@ram\"", "An action word with \" paramètre\"", "An action word with \" #a param.\""]
	}

	@Unroll
	def "Should throw error when creating an ActionWord with 1 parameter without name in AW library"() {
		when:
		new LibraryActionWordParser().createActionWordInLibrary(word)
		then:

		InvalidActionWordParameterNameException exception = thrown()
		exception.message == "Action word parameter name cannot be empty."

		where:
		word << ["An action word with \"\"", "An action word with \"   \""]
	}

	@Unroll
	def "Should throw error when creating an ActionWord with 2 parameters having the same name in AW library"() {
		when:
		new LibraryActionWordParser().createActionWordInLibrary(word)
		then:

		InvalidActionWordParameterNameException exception = thrown()
		exception.message == "Action word parameter name must be unique."

		where:
		word << ["1 action word with \"param1\"", "1 action word with 2 \"param2\"", "\"param\" action 1 word with \"param\"",
				 "\"param\" action 1 word with \"param2\"", "\"param2\" action 1 word"]
	}

	@Unroll
	def "Should create an ActionWord with a parameter as text between two double quotes in AW library"() {
		when:
		def parser = new LibraryActionWordParser()
		ActionWord result = parser.createActionWordInLibrary("This is an action word with \""+word+"\"")

		then:
		result.createWord() == "This is an action word with \""+word+"\""
		result.getToken() == "TP-This is an action word with -"
		def fragments = result.getFragments()
		fragments.size() == 2

		def f1 = fragments.get(0)
		f1.class.is(ActionWordText)
		((ActionWordText) f1).getText() == "This is an action word with "

		def f2 = fragments.get(1)
		f2.class.is(ActionWordParameter)
		ActionWordParameter parameter = (ActionWordParameter) f2
		parameter.getName() == word
		parameter.getDefaultValue() == ""

		where:
		word << ["param", "1_day", "abc-123", "abc555xyz"]
	}

	@Unroll
	def "Should create an ActionWord with 1 parameter as number between two double quotes in AW library"(){
		when:
		def parser = new LibraryActionWordParser()
		ActionWord result = parser.createActionWordInLibrary(word+number)

		then:
		result.createWord() == word+"\"param1\""
		result.getToken() == "TP-"+word+"-"
		def fragments = result.getFragments()
		fragments.size() == 2

		def f1 = fragments.get(0)
		f1.class.is(ActionWordText)
		((ActionWordText) f1).getText() == word

		def f2 = fragments.get(1)
		f2.class.is(ActionWordParameter)
		ActionWordParameter parameter = (ActionWordParameter) f2
		parameter.getName() == "param1"
		parameter.getDefaultValue() == expected

		where:
		word << ["1.5action word with ", "This is1.5action word with ", "this is 1.5action word with "]
		number << ["\"2\"", "\"   1,5\"", "\"   -1.6    \""]
		expected << ["2", "1,5", "-1.6"]
	}

	def "Should create an ActionWord with a parameter which has spaces in AW library"() {
		when:
		def parser = new LibraryActionWordParser()
		ActionWord result = parser.createActionWordInLibrary("This is an action word with \"     pa-r_m   123     \"")

		then:
		result.createWord() == "This is an action word with \"pa-r_m_123\""
		result.getToken() == "TP-This is an action word with -"
		def fragments = result.getFragments()
		fragments.size() == 2

		def f1 = fragments.get(0)
		f1.class.is(ActionWordText)
		((ActionWordText) f1).getText() == "This is an action word with "

		def f2 = fragments.get(1)
		f2.class.is(ActionWordParameter)
		ActionWordParameter parameter = (ActionWordParameter) f2
		parameter.getName() == "pa-r_m_123"
		parameter.getDefaultValue() == ""
	}

	def "Should create an ActionWord with a parameter at the end but missing a double quote"() {
		when:
		def parser = new LibraryActionWordParser()
		ActionWord result = parser.createActionWordInLibrary("This is an action word with \"param")

		then:
		result.createWord() == "This is an action word with \"param\""
		result.getToken() == "TP-This is an action word with -"
		def fragments = result.getFragments()
		fragments.size() == 2

		def f1 = fragments.get(0)
		f1.class.is(ActionWordText)
		((ActionWordText) f1).getText() == "This is an action word with "

		def f2 = fragments.get(1)
		f2.class.is(ActionWordParameter)
		ActionWordParameter parameter = (ActionWordParameter) f2
		parameter.getName() == "param"
		parameter.getDefaultValue() == ""
	}


	//************** COMPLEX COMBINATION VALIDATION **************
	def "Should create an ActionWord with 3 parameters in AW library : at the beginning, in the middle and at the end"() {
		when:
		def parser = new LibraryActionWordParser()
		ActionWord result = parser.createActionWordInLibrary("\"This\" is   an \"action word\" with   \"   param   \"")

		then:
		result.createWord() == "\"This\" is an \"action_word\" with \"param\""
		result.getToken() == "PTPTP- is an - with -"
		def fragments = result.getFragments()
		fragments.size() == 5

		def f1 = fragments.get(0)
		f1.class.is(ActionWordParameter)
		ActionWordParameter parameter1 = (ActionWordParameter) f1
		parameter1.getName() == "This"
		parameter1.getDefaultValue() == ""

		def f2 = fragments.get(1)
		f2.class.is(ActionWordText)
		((ActionWordText) f2).getText() == " is an "

		def f3 = fragments.get(2)
		f3.class.is(ActionWordParameter)
		ActionWordParameter parameter2 = (ActionWordParameter) f3
		parameter2.getName() == "action_word"
		parameter2.getDefaultValue() == ""

		def f4 = fragments.get(3)
		f4.class.is(ActionWordText)
		((ActionWordText) f4).getText() == " with "

		def f5 = fragments.get(4)
		f5.class.is(ActionWordParameter)
		ActionWordParameter parameter3 = (ActionWordParameter) f5
		parameter3.getName() == "param"
		parameter3.getDefaultValue() == ""
	}

	def "Should create an ActionWord with 4 parameters in AW library : 1 at the beginning, 2 parameter values in the middle which are next to each other and 1 at the end"() {
		when:
		def parser = new LibraryActionWordParser()
		ActionWord result = parser.createActionWordInLibrary("\"This\" is an\"action\"\"word\" with \"param\"")

		then:
		result.createWord() == "\"This\" is an\"action\"\"word\" with \"param\""
		result.getToken() == "PTPPTP- is an- with -"
		def fragments = result.getFragments()
		fragments.size() == 6

		def f1 = fragments.get(0)
		f1.class.is(ActionWordParameter)
		ActionWordParameter parameter1 = (ActionWordParameter) f1
		parameter1.getName() == "This"
		parameter1.getDefaultValue() == ""

		def f2 = fragments.get(1)
		f2.class.is(ActionWordText)
		((ActionWordText) f2).getText() == " is an"

		def f3 = fragments.get(2)
		f3.class.is(ActionWordParameter)
		ActionWordParameter parameter2 = (ActionWordParameter) f3
		parameter2.getName() == "action"
		parameter2.getDefaultValue() == ""

		def f4 = fragments.get(3)
		f4.class.is(ActionWordParameter)
		ActionWordParameter parameter3 = (ActionWordParameter) f4
		parameter3.getName() == "word"
		parameter3.getDefaultValue() == ""

		def f5 = fragments.get(4)
		f5.class.is(ActionWordText)
		((ActionWordText) f5).getText() == " with "

		def f6 = fragments.get(5)
		f6.class.is(ActionWordParameter)
		ActionWordParameter parameter4 = (ActionWordParameter) f6
		parameter4.getName() == "param"
		parameter4.getDefaultValue() == ""
	}

	def "Should create an ActionWord with 4 parameters in AW library : 1 at the beginning, 2 parameter values in the middle which are separated by a space and 1 parameter value at the end"() {
		when:
		def parser = new LibraryActionWordParser()
		ActionWord result = parser.createActionWordInLibrary("\"This\" is an \"action\"    \"word\" with \"param\"")

		then:
		result.createWord() == "\"This\" is an \"action\" \"word\" with \"param\""
		result.getToken() == "PTPTPTP- is an - - with -"
		def fragments = result.getFragments()
		fragments.size() == 7

		def f1 = fragments.get(0)
		f1.class.is(ActionWordParameter)
		ActionWordParameter parameter1 = (ActionWordParameter) f1
		parameter1.getName() == "This"
		parameter1.getDefaultValue() == ""

		def f2 = fragments.get(1)
		f2.class.is(ActionWordText)
		((ActionWordText) f2).getText() == " is an "

		def f3 = fragments.get(2)
		f3.class.is(ActionWordParameter)
		ActionWordParameter parameter2 = (ActionWordParameter) f3
		parameter2.getName() == "action"
		parameter2.getDefaultValue() == ""

		def f4 = fragments.get(3)
		f4.class.is(ActionWordText)
		((ActionWordText) f4).getText() == " "

		def f5 = fragments.get(4)
		f5.class.is(ActionWordParameter)
		ActionWordParameter parameter3 = (ActionWordParameter) f5
		parameter3.getName() == "word"
		parameter3.getDefaultValue() == ""

		def f6 = fragments.get(5)
		f6.class.is(ActionWordText)
		((ActionWordText) f6).getText() == " with "

		def f7 = fragments.get(6)
		f7.class.is(ActionWordParameter)
		ActionWordParameter parameter4 = (ActionWordParameter) f7
		parameter4.getName() == "param"
		parameter4.getDefaultValue() == ""
	}

	def "Should create an ActionWord with parameter complexe 1 in AW library"(){
		when:
		def parser = new LibraryActionWordParser()
		ActionWord result = parser.createActionWordInLibrary("1 day 3 with -2,5\"param4\"-3 month  1.5 year 0")
		def values = parser.getParameterValues()

		then:
		result.createWord() == "\"param1\" day \"param2\" with \"param3\"\"param4\"\"param5\" month \"param6\" year \"param7\""
		result.getToken() == "PTPTPPPTPTP- day - with - month - year -"
		def fragments = result.getFragments()
		fragments.size() == 11

		def f1 = fragments.get(0)
		f1.class.is(ActionWordParameter)
		ActionWordParameter parameter1 = (ActionWordParameter) f1
		parameter1.getName() == "param1"
		parameter1.getDefaultValue() == "1"

		def f2 = fragments.get(1)
		f2.class.is(ActionWordText)
		((ActionWordText) f2).getText() == " day "

		def f3 = fragments.get(2)
		f3.class.is(ActionWordParameter)
		ActionWordParameter parameter2 = (ActionWordParameter) f3
		parameter2.getName() == "param2"
		parameter2.getDefaultValue() == "3"

		def f4 = fragments.get(3)
		f4.class.is(ActionWordText)
		((ActionWordText) f4).getText() == " with "

		def f5 = fragments.get(4)
		f5.class.is(ActionWordParameter)
		ActionWordParameter parameter3 = (ActionWordParameter) f5
		parameter3.getName() == "param3"
		parameter3.getDefaultValue() == "-2,5"

		def f6 = fragments.get(5)
		f6.class.is(ActionWordParameter)
		ActionWordParameter parameter4 = (ActionWordParameter) f6
		parameter4.getName() == "param4"
		parameter4.getDefaultValue() == ""

		def f7 = fragments.get(6)
		f7.class.is(ActionWordParameter)
		ActionWordParameter parameter5 = (ActionWordParameter) f7
		parameter5.getName() == "param5"
		parameter5.getDefaultValue() == "-3"

		def f8 = fragments.get(7)
		f8.class.is(ActionWordText)
		((ActionWordText) f8).getText() == " month "

		def f9 = fragments.get(8)
		f9.class.is(ActionWordParameter)
		ActionWordParameter parameter6 = (ActionWordParameter) f9
		parameter6.getName() == "param6"
		parameter6.getDefaultValue() == "1.5"

		def f10 = fragments.get(9)
		f10.class.is(ActionWordText)
		((ActionWordText) f10).getText() == " year "

		def f11 = fragments.get(10)
		f11.class.is(ActionWordParameter)
		ActionWordParameter parameter7 = (ActionWordParameter) f11
		parameter7.getName() == "param7"
		parameter7.getDefaultValue() == "0"
	}

	@Unroll
	def "Should create an ActionWord with 2 parameter values, 1 as a number in text, other as a number between two double quotes in AW library"(){
		when:
		def parser = new LibraryActionWordParser()
		ActionWord result = parser.createActionWordInLibrary("1"+word+"\"2\"")

		then:
		result.createWord() == "\"param1\""+word+"\"param2\""
		result.getToken() == "PTP-"+word+"-"
		def fragments = result.getFragments()
		fragments.size() == 3

		def f1 = fragments.get(0)
		f1.class.is(ActionWordParameter)
		ActionWordParameter parameter1 = (ActionWordParameter) f1
		parameter1.getName() == "param1"
		parameter1.getDefaultValue() == "1"

		def f2 = fragments.get(1)
		f2.class.is(ActionWordText)
		((ActionWordText) f2).getText() == word

		def f3 = fragments.get(2)
		f3.class.is(ActionWordParameter)
		ActionWordParameter parameter2 = (ActionWordParameter) f3
		parameter2.getName() == "param2"
		parameter2.getDefaultValue() == "2"

		where:
		word << [" ", " .5 action word with "]
	}

}
