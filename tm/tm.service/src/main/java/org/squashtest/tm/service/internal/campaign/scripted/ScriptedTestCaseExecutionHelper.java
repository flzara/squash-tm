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
package org.squashtest.tm.service.internal.campaign.scripted;

import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.testcase.KeywordTestCase;
import org.squashtest.tm.domain.testcase.ScriptedTestCase;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseVisitor;
import org.squashtest.tm.service.testcase.scripted.ScriptedTestCaseParser;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.function.Function;

import static java.util.Objects.nonNull;

@Component
public class ScriptedTestCaseExecutionHelper {

	@Inject
	@Named("scriptedTestCaseParserFactory")
	private Function<ScriptedTestCase, ScriptedTestCaseParser> parserFactory;

	public void createExecutionStepsForScriptedTestCase(Execution execution){
		//guard condition
		TestCase referencedTestCase = execution.getReferencedTestCase();
		if(nonNull(referencedTestCase)){
			TestCaseVisitor testCaseVisitor = new TestCaseVisitor(){

				@Override
				public void visit(TestCase testCase) {
					throw new IllegalArgumentException("ScriptedTestCaseExecutionHelper is dedicated to ScriptedTestCase.");
				}

				@Override
				public void visit(KeywordTestCase keywordTestCase) {
					throw new IllegalArgumentException("ScriptedTestCaseExecutionHelper is dedicated to ScriptedTestCase.");
				}

				@Override
				public void visit(ScriptedTestCase scriptedTestCase) {
					//creating execution extender
					execution.createScriptedExtender();

					//now we must do the step creation and everything that depend on script
					//first we retrieve the good parser
					ScriptedTestCaseParser testCaseParser = parserFactory.apply(scriptedTestCase);

					//and we delegate to the parser
					testCaseParser.populateExecution(execution);
				}
			};
			referencedTestCase.accept(testCaseVisitor);
		}



	}
}
