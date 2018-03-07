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
package org.squashtest.tm.service.internal.testcase.scripted.gherkin

import gherkin.AstBuilder
import gherkin.Parser
import gherkin.ast.GherkinDocument
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.squashtest.tm.domain.execution.Execution
import spock.lang.Specification

import static org.apache.commons.io.FileUtils.readFileToString

class GherkinStepGeneratorTest extends Specification {

	def "should generate a correct step number"() {

		given:
		GherkinDocument gherkinDocument = getGherkinDocument(file)
		GherkinStepGenerator stepGenerator = new GherkinStepGenerator()
		Execution execution = new Execution()

		when:
		stepGenerator.populateExecution(execution, gherkinDocument)

		then:
		execution.getSteps().size().equals(exepectedStepsNumber)

		where:
		file                              || exepectedStepsNumber
		"simple_script.feature"           || 2
		"scenario_outline_script.feature" || 6
		"background_script.feature"       || 2

	}

	GherkinDocument getGherkinDocument(String file) {
		Resource resource = new ClassPathResource("testcase/scripted/gherkin/" + file)
		String script = readFileToString(resource.getFile())
		Parser<GherkinDocument> parser = new Parser<>(new AstBuilder())
		GherkinDocument gherkinDocument = parser.parse(script)
		gherkinDocument
	}

	def "should create correct span"() {
		given:
		StringBuilder sb = new StringBuilder()
		GherkinStepGenerator stepGenerator = new GherkinStepGenerator()

		when:
		stepGenerator.appendClassSpan(sb, text, cssClasses as String[])

		then:
		sb.toString().equals(expectedSpan)

		where:
		text                 | cssClasses  || expectedSpan
		"toto"               | []          || "<span>toto </span>"
		"i'm a super string" | ['keyword'] || "<span class='keyword '>i'm a super string </span>"

	}
}
