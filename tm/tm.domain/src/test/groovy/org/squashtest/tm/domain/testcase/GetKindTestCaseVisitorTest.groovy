package org.squashtest.tm.domain.testcase

import spock.lang.Specification

class GetKindTestCaseVisitorTest extends Specification {

	def "Should get kind of each visited test case"() {
		given:
		def standardTc = new TestCase()
		def scriptedTc = new ScriptedTestCase()
		def keywordTc = new KeywordTestCase()
		and:
		GetKindTestCaseVisitor visitor = new GetKindTestCaseVisitor()
		when:
		scriptedTc.accept(visitor)
		then:
		visitor.getKind() == TestCaseKind.GHERKIN
		when:
		standardTc.accept(visitor)
		then:
		visitor.getKind() == TestCaseKind.STANDARD
		when:
		keywordTc.accept(visitor)
		then:
		visitor.getKind() == TestCaseKind.KEYWORD
	}
}
