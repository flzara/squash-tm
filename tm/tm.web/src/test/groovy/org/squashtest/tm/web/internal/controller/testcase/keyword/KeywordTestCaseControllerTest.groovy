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
