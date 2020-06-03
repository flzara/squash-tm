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

import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Unroll

class ActionWordTest extends Specification {

	@Unroll
	@Ignore("Which class should be responsible for trimming ?")
	def "should create an ActionWord"() {
		given:
			def text = new ActionWordText(word)
		when:
			ActionWord actionWord = new ActionWord([text] as List)
		then:
			actionWord.createWord() == expectedWord
			actionWord.generateToken() == expectedToken
		where:
			word 						|| expectedWord						|| expectedToken
			"hello" 					|| "hello"							|| "T-hello-"
			" hello   is it   me ?   " 	|| "hello   is it   me ?"			|| "T-hello is it me ?-"
	}

	@Ignore("Irrelevant test ?")
	def "create an ActionWord with given token"() {
		when:
		ActionWord actionWord = new ActionWord("hello \"param\"", "TP-hello -")

		then:
		actionWord.getWord() == "hello \"param\""
		actionWord.getToken() == "TP-hello -"
	}

	@Unroll
	@Ignore("Move to ActionWordText test ?")
	def "should reject invalid ActionWord"() {
		given:
			def text = new ActionWordText(word)
		when:
			new ActionWord([text] as List)
		then:
			thrown IllegalArgumentException
		where:
			word << [null, "", "   ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"]
	}
}
