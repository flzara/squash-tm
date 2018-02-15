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
import org.squashtest.tm.domain.testcase.ScriptedTestCaseExtender;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.testcase.scripted.ScriptedTestCaseParser;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.function.Function;

@Component
public class ScriptedTestCaseExecutionHelper {

	@Inject
	@Named("scriptedTestCaseParserFactory")
	private Function<ScriptedTestCaseExtender, ScriptedTestCaseParser> parserFactory;

	public void createExecutionStepsForScriptedTestCase(Execution execution){
		//guard condition
		TestCase referencedTestCase = execution.getReferencedTestCase();
		if(referencedTestCase == null || !referencedTestCase.isScripted()){
			return;
		}
		//first we retrieve the good parser
		ScriptedTestCaseExtender scriptExtender = referencedTestCase.getScriptedTestCaseExtender();
		ScriptedTestCaseParser testCaseParser = parserFactory.apply(scriptExtender);

		//and we delegate to the parser
		testCaseParser.populateExecution(execution);
	}
}
