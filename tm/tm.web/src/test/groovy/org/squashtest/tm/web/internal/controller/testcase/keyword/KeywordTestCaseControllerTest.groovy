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
package org.squashtest.tm.web.internal.controller.testcase.keyword

import org.squashtest.tm.domain.bdd.ActionWord
import org.squashtest.tm.service.actionword.ActionWordService
import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author qtran - created on 29/05/2020
 *
 */
class KeywordTestCaseControllerTest  extends Specification{
	KeywordTestCaseController controller = new KeywordTestCaseController()
	ActionWordService actionWordService = Mock()

	def setup() {
		controller.actionWordService = actionWordService
	}

	def "should find all action words that match searching word"(){
		given:
		Long projectId = -1L
		String searchInput = "an action word \"p1\" w"

		and:
		def actionWord1 = new ActionWord()
		actionWord1.setWord("an action word without param")

		def actionWord2 = new ActionWord()
		actionWord2.setWord("an action word with \"1\" param")

		def actionWord3 = new ActionWord()
		actionWord3.setWord("an action word with \"this\" param or \"that\" param")

		List<ActionWord> actionWordList = [actionWord1, actionWord2, actionWord3]

		and:
		actionWordService.findAllMatchingActionWords(projectId, searchInput) >> actionWordList

		when:
		Collection<String> result = controller.findAllMatchingActionWords(projectId, searchInput)

		then:
		result.size() == 3

	}
}
