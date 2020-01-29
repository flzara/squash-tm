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
package org.squashtest.tm.web.internal.controller.testcase.steps

import org.squashtest.tm.domain.bdd.BehaviorPhrase
import org.squashtest.tm.domain.bdd.Keyword
import org.squashtest.tm.domain.testcase.BehaviorTestStep
import org.squashtest.tm.domain.testcase.TestStep
import spock.lang.Specification

class BehaviorTestStepTableModelBuilderTest extends Specification {

	BehaviorTestStepTableModelBuilder builder = new BehaviorTestStepTableModelBuilder()

	def "should build an item from a BehaviorTestStep"() {
		given:
		def testStep = Mock(BehaviorTestStep)
		testStep.getId() >> 8
		testStep.getKeyword() >> Keyword.GIVEN
		testStep.getBehaviorPhrase() >> new BehaviorPhrase("goodbye")
		when:
		Map<String, String> resultItem = builder.buildItemData(testStep)
		then:
		resultItem.size() == 4
		resultItem.get("step-index") == "0"
		resultItem.get("step-id") == "8"
		resultItem.get("step-keyword") == "GIVEN"
		resultItem.get("step-phrase") == "goodbye"
	}
}
