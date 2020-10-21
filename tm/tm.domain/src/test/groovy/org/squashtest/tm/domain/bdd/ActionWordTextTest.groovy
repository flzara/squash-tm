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

import org.squashtest.tm.exception.actionword.InvalidActionWordTextException
import spock.lang.Specification
import spock.lang.Unroll

class ActionWordTextTest extends Specification {

	@Unroll
	def "should create an ActionWordText"() {
		when:
		ActionWordText actionWordText = new ActionWordText(text)

		then:
		actionWordText.getText() == expectedText

		where:
		text 										|| expectedText
		"hello tod@y is Monday 27/04/2020 ^^" 		|| "hello tod@y is Monday 27/04/2020 ^^"
		"     hello        adf    df        " 		|| " hello adf df "
	}

	@Unroll
	def "should reject invalid ActionWordText"() {
		when:
		new ActionWordText(text)

		then:
		thrown InvalidActionWordTextException

		where:
		text << [null, "", "a b\"c1 24", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"]
	}

	def "should create a copy of an ActionWordText"() {
		given:
			def source = new ActionWordText("a simple action")
			source.setId(6L)
		and:
			def actionWord = new ActionWord([source])
			source.setActionWord(actionWord)
		when:
			ActionWordText copy = source.createCopy()
		then:
			copy.getId() == null
			copy.getActionWord() == null
			copy.getText() == "a simple action"
	}

}
