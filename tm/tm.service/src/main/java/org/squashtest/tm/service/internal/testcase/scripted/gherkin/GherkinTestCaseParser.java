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
import gherkin.ast.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStep;
import org.squashtest.tm.domain.testcase.ScriptedTestCaseExtender;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.testcase.scripted.ScriptedTestCaseParser;

import java.util.List;

public class GherkinTestCaseParser implements ScriptedTestCaseParser {

	private static final Logger LOGGER = LoggerFactory.getLogger(GherkinTestCaseParser.class);

	@Override
	public void populateExecution(Execution execution) {
		TestCase referencedTestCase = execution.getReferencedTestCase();
		ScriptedTestCaseExtender scriptExtender = referencedTestCase.getScriptedTestCaseExtender();
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Begin parsing of Test Case {} for Execution {}", referencedTestCase, execution);
		}
		Parser<GherkinDocument> parser = new Parser<>(new AstBuilder());
		GherkinDocument gherkinDocument = parser.parse(scriptExtender.getScript());
		List<ScenarioDefinition> scenarioDefinitions = gherkinDocument.getFeature().getChildren();

		if(scenarioDefinitions.isEmpty()){
			return;
		}

		//first, we get the first item of scenario definition witch can be the background
		ScenarioDefinition potentialBackground = scenarioDefinitions.get(0);
		Background background = null;

		if(potentialBackground instanceof Background){
			background = (Background) potentialBackground;
		}

		//now let's do the scenarios
		for (ScenarioDefinition scenarioDefinition : scenarioDefinitions) {
			ExecutionStep executionStep = new ExecutionStep();
			StringBuilder sb = new StringBuilder();
			//Sigh... i don't see any means to avoid this ugly instanceof
			//Can't use visitor because cannot change Gherking Parser source code, and haven't right to fork it...
			if(scenarioDefinition instanceof Scenario){
				Scenario scenario = (Scenario) scenarioDefinition;
				appendScenarioLine(scenarioDefinition,sb);
				if(background != null){
					includeBackground(background,sb);
				}
				List<Step> steps = scenario.getSteps();
				for (Step step : steps) {
					appendStepLine(step, sb);
				}

			} else if(scenarioDefinition instanceof ScenarioOutline){

			}

			executionStep.setAction(sb.toString());
			execution.getSteps().add(executionStep);
		}
	}

	private void includeBackground(Background background, StringBuilder sb) {
		List<Step> steps = background.getSteps();
		for (Step step : steps) {
			appendStepLine(step, sb);
		}
	}

	private void appendStepLine(Step step, StringBuilder sb) {
		sb.append(step.getKeyword());
		sb.append(step.getText());
		appendLineBreak(sb);
	}

	private void appendScenarioLine(ScenarioDefinition scenarioDefinition, StringBuilder sb) {
		sb.append(scenarioDefinition.getKeyword());
		sb.append(scenarioDefinition.getName());
		appendLineBreak(sb);
	}

	private void appendLineBreak(StringBuilder sb) {
		sb.append("</br>");
	}


}
