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
	public static final String STEP_KEYWORD_CLASS_NAME = "step-keyword";
	public static final String SCENARIO_KEYWORD_CLASS_NAME = "scenario-keyword";

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
				appendScenarioStep(execution, background, scenarioDefinition);

			} else if (scenarioDefinition instanceof ScenarioOutline) {
				appendScenarioOutilineStep(execution, background, scenarioDefinition);
			}
		}
	}

	private void appendScenarioStep(Execution execution, Background background, ScenarioDefinition scenarioDefinition) {
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

		appendExecutionStep(execution, sb);
	}

	private void appendScenarioOutilineStep(Execution execution, Background background, ScenarioDefinition scenarioDefinition) {
		ScenarioOutline scenario = (ScenarioOutline) scenarioDefinition;
		List<Examples> examples = scenario.getExamples();

		for (Examples example : examples) {
			appendExample(execution, background, scenario, example);
        }
	}

	private void appendExample(Execution execution, Background background, ScenarioOutline scenario, Examples example) {
		int count = example.getTableBody().size();
		List<String> headers = getExampleHeaders(example);
		int nbColumn = headers.size();
		List<Step> steps = scenario.getSteps();
		for (int i = 0; i < count; i++) {
            StringBuilder sb = new StringBuilder();
            appendScenarioLine(scenario, sb);
            if (background != null) {
                includeBackground(background, sb);
            }
            List<String> valuesForThisLine = getExampleLineValue(example, i);
            Map<String, String> valueByHeader = new HashMap<>();
            IntStream.range(0, nbColumn).forEach(j -> valueByHeader.put(headers.get(j), valuesForThisLine.get(j)));
            for (Step step : steps) {
                appendStepLine(step, valueByHeader, sb);
            }
			appendExecutionStep(execution, sb);
        }
	}

	private void appendExecutionStep(Execution execution, StringBuilder sb) {
		ExecutionStep executionStep = new ExecutionStep();
		executionStep.setAction(sb.toString());
		execution.getSteps().add(executionStep);
	}

	private List<String> getExampleLineValue(Examples example, int i) {
		return example.getTableBody().get(i).getCells().stream().map(TableCell::getValue).collect(Collectors.toList());
	}

	private List<String> getExampleHeaders(Examples example) {
		return example.getTableHeader().getCells().stream().map(TableCell::getValue).collect(Collectors.toList());
	}

	private void includeBackground(Background background, StringBuilder sb) {
		List<Step> steps = background.getSteps();
		for (Step step : steps) {
			appendStepLine(step, sb);
		}
	}

	private void appendStepLine(Step step, StringBuilder sb) {
		String keyword = step.getKeyword();
		appendClassSpan(sb, keyword, STEP_KEYWORD_CLASS_NAME);
		sb.append(step.getText());
		appendLineBreak(sb);
	}

	private void appendClassSpan(StringBuilder sb, String keyword, String cssClass) {
		sb.append("<span class='")
			.append(cssClass)
			.append("'>")
			.append(StringUtils.appendIfMissing(keyword," "))
			.append("</span>");
	}

	private void appendScenarioLine(ScenarioDefinition scenarioDefinition, StringBuilder sb) {
		String keyword = scenarioDefinition.getKeyword();
		appendClassSpan(sb,keyword, SCENARIO_KEYWORD_CLASS_NAME);
		sb.append(scenarioDefinition.getName());
		appendBlankLine(sb);
	}

	private void appendBlankLine(StringBuilder sb) {
		appendLineBreak(sb);
		appendLineBreak(sb);
	}

	private void appendLineBreak(StringBuilder sb) {
		sb.append("</br>");
	}

	//this method append steps lines in scenario outline mode (ie with dataset so we must do param substitution)
	private void appendStepLine(Step step, Map<String, String> valueByHeader, StringBuilder sb) {
		appendClassSpan(sb,step.getKeyword(), STEP_KEYWORD_CLASS_NAME);
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

}
