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
package org.squashtest.tm.service.internal.testcase.scripted.gherkin;

import gherkin.AstBuilder;
import gherkin.Parser;
import gherkin.ParserException;
import gherkin.ast.GherkinDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.domain.execution.ScriptedExecution;
import org.squashtest.tm.domain.testcase.ConsumerForScriptedTestCaseVisitor;
import org.squashtest.tm.domain.testcase.ScriptedTestCase;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.exception.testcase.ScriptParsingException;
import org.squashtest.tm.service.testcase.scripted.ScriptedTestCaseParser;

import java.util.function.Consumer;

import static java.util.Objects.nonNull;

public class GherkinTestCaseParser implements ScriptedTestCaseParser {

	private static final Logger LOGGER = LoggerFactory.getLogger(GherkinTestCaseParser.class);

	private GherkinStepGenerator stepGenerator;

	public GherkinTestCaseParser(GherkinStepGenerator stepGenerator) {
		this.stepGenerator = stepGenerator;
	}

	@Override
	public void populateExecution(ScriptedExecution scriptedExecution) {
		TestCase referencedTestCase = scriptedExecution.getReferencedTestCase();
		if(nonNull(referencedTestCase)){

			Consumer<ScriptedTestCase> consumer = scriptedTestCase -> {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Begin parsing of Test Case {} for Execution {}", referencedTestCase, scriptedExecution);
				}
				GherkinDocument gherkinDocument = parseToGherkinDocument(scriptedTestCase);
				stepGenerator.populateExecution(scriptedExecution, gherkinDocument);
			};

			ConsumerForScriptedTestCaseVisitor testCaseVisitor = new ConsumerForScriptedTestCaseVisitor(
				consumer,
				new IllegalArgumentException("GherkinTestCaseParser is dedicated to ScriptedTestCase."));
			referencedTestCase.accept(testCaseVisitor);
		}
	}

	@Override
	public void validateScript(ScriptedTestCase scriptedTestCase) {
		parseToGherkinDocument(scriptedTestCase);
	}

	public GherkinDocument parseToGherkinDocument(ScriptedTestCase scriptedTestCase) {
		Parser<GherkinDocument> parser = new Parser<>(new AstBuilder());
		GherkinDocument gherkinDocument = null;
		try {
			gherkinDocument = parser.parse(scriptedTestCase.getScript());
		} catch (ParserException e) {
			throw new ScriptParsingException(e);
		}
		return gherkinDocument;
	}
}
