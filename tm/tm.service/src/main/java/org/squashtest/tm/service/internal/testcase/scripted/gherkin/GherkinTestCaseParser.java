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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStep;
import org.squashtest.tm.domain.testcase.ScriptedTestCaseExtender;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.testcase.scripted.ScriptedTestCaseParser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

		if (scenarioDefinitions.isEmpty()) {
			return;
		}

		//first, we get the first item of scenario definition witch can be the background
		ScenarioDefinition potentialBackground = scenarioDefinitions.get(0);
		Background background = null;

		if (potentialBackground instanceof Background) {
			background = (Background) potentialBackground;
		}

		//now let's do the scenarios
		for (ScenarioDefinition scenarioDefinition : scenarioDefinitions) {
			//Sigh... i don't see any means to avoid this ugly instanceof
			//Can't use visitor because cannot change Gherking Parser source code to add accept method, and haven't right to fork ...
			if (scenarioDefinition instanceof Scenario) {
				StringBuilder sb = new StringBuilder();
				Scenario scenario = (Scenario) scenarioDefinition;
				appendScenarioLine(scenarioDefinition, sb);
				if (background != null) {
					includeBackground(background, sb);
				}
				List<Step> steps = scenario.getSteps();
				for (Step step : steps) {
					appendStepLine(step, sb);
				}

				ExecutionStep executionStep = new ExecutionStep();
				executionStep.setAction(sb.toString());
				execution.getSteps().add(executionStep);

			} else if (scenarioDefinition instanceof ScenarioOutline) {
				ScenarioOutline scenario = (ScenarioOutline) scenarioDefinition;
				List<Examples> examples = scenario.getExamples();

				for (Examples example : examples) {
					int count = example.getTableBody().size();
					List<String> headers = example.getTableHeader().getCells().stream().map(TableCell::getValue).collect(Collectors.toList());
					int nbColumn = headers.size();
					List<Step> steps = scenario.getSteps();
					for (int i = 0; i < count; i++) {
						StringBuilder sb = new StringBuilder();
						appendScenarioLine(scenarioDefinition, sb);
						if (background != null) {
							includeBackground(background, sb);
						}
						List<String> valuesForThisLine = example.getTableBody().get(i).getCells().stream().map(TableCell::getValue).collect(Collectors.toList());
						Map<String, String> valueByHeader = new HashMap<>();
						IntStream.range(0, nbColumn).forEach(j -> valueByHeader.put(headers.get(j), valuesForThisLine.get(j)));
						for (Step step : steps) {
							appendStepLine(step, valueByHeader, sb);
						}
						ExecutionStep executionStep = new ExecutionStep();
						executionStep.setAction(sb.toString());
						execution.getSteps().add(executionStep);
					}
				}
			}
		}
	}

	private void appendStepLine(Step step, Map<String, String> valueByHeader, StringBuilder sb) {
		sb.append(step.getKeyword());
		String text = step.getText();

		//now substitute each <param> by it's value, if not found inject a placeholder
		Pattern p = Pattern.compile("<[.*?[^>]]*>");
		Matcher m = p.matcher(text);
		while (m.find()) {
			String token = m.group();
			String header = token.substring(1, token.length() - 1);
			String value = valueByHeader.get(header);
			if(StringUtils.isBlank(value)){
				value = "<NO_DATA>";
			}
			text = text.replace(token, value);
		}

		sb.append(text);
		appendLineBreak(sb);
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
