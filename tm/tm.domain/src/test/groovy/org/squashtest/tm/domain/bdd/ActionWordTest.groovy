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
package org.squashtest.tm.domain.bdd

import spock.lang.Specification
import spock.lang.Unroll

class ActionWordTest extends Specification {

	@Unroll
	def "should create an ActionWord"() {
		when:
		ActionWord actionWord = new ActionWord(word)

		then:
		actionWord.getWord() == expectedWord
		actionWord.getToken() == "F"

		where:
		word 			|| expectedWord
		"hello" 		|| "hello"
		" hello   " 	|| "hello"
	}

	def "create an ActionWord with given token"() {
		when:
		ActionWord actionWord = new ActionWord("hello \"param\"", "FP")

		then:
		actionWord.getWord() == "hello \"param\""
		actionWord.getToken() == "FP"
	}

	@Unroll
	def "should reject invalid ActionWord"() {
		when:
		new ActionWord(word)

		then:
		thrown IllegalArgumentException

		where:
		word << [null, "", "   ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"]
	}
}
