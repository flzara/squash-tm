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

import gherkin.GherkinDialect;
import gherkin.GherkinDialectProvider;
import gherkin.ast.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStep;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

//This class is responsible for generating Squash TM executions steps, with their rich text field, from a Gherkin script.
public class GherkinStepGenerator {

	private static final Logger LOGGER = LoggerFactory.getLogger(GherkinStepGenerator.class);

	private static final String STEP_KEYWORD_CLASS_NAME = "step-keyword";
	private static final String GIVEN_KEYWORD_CLASS_NAME = "step-keyword-given";
	private static final String WHEN_KEYWORD_CLASS_NAME = "step-keyword-when";
	private static final String THEN_KEYWORD_CLASS_NAME = "step-keyword-then";

	private static final String STEP_DOC_STRING_CLASS_NAME = "step-doc-string";

	private static final String ARGUMENT_TABLE_CLASS_NAME = "step-table";
	private static final String ARGUMENT_TABLE_TR_CLASS_NAME = "step-table-tr";
	private static final String ARGUMENT_TABLE_TD_CLASS_NAME = "step-table-td";

	private static final String SCENARIO_KEYWORD_CLASS_NAME = "scenario-keyword";
	private static final String SCENARIO_DESCRIPTION_CLASS_NAME = "scenario-description";

	//HTML tags
	private static final String TABLE_TAG = "table";
	private static final String ROW_TAG = "tr";
	private static final String CELL_TAG = "td";
	private static final String SPAN_TAG = "span";

	private GherkinDialectProvider dialectProvider = new GherkinDialectProvider();

	private GherkinDialect dialect;

	private String currentStepClass = "";

	public void populateExecution(Execution execution, GherkinDocument gherkinDocument) {
		Feature feature = gherkinDocument.getFeature();
		initDialect(feature);

		List<ScenarioDefinition> scenarioDefinitions = feature.getChildren();
		if (scenarioDefinitions.isEmpty()) {
			return;
		}

		//first, we get the first item of scenario definition witch could be the background
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
				appendScenarioOutlineStep(execution, background, scenarioDefinition);
			}
		}
	}

	private void initDialect(Feature feature) {
		String language = feature.getLanguage();

		if (StringUtils.isNotBlank(language)) {
			dialect = dialectProvider.getDialect(language, null);
		} else {
			dialect = dialectProvider.getDefaultDialect();
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

	private void appendScenarioOutlineStep(Execution execution, Background background, ScenarioDefinition scenarioDefinition) {
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
		appendStepKeyword(step, sb);
		sb.append(step.getText());
		appendLineBreak(sb);
		appendArgument(step, sb);
	}

	private void appendArgument(Step step, StringBuilder sb) {
		Node argument = step.getArgument();

		if (argument == null) {
			return;
		}

		appendLineBreak(sb);
		if (argument instanceof DocString) {
			DocString docString = (DocString) argument;
			appendClassSpan(sb, docString.getContent(), STEP_DOC_STRING_CLASS_NAME);
		} else if (argument instanceof DataTable) {
			DataTable dataTable = (DataTable) argument;
			appendOpeningClassTab(sb, TABLE_TAG, ARGUMENT_TABLE_CLASS_NAME);
			for (TableRow tableRow : dataTable.getRows()) {
				appendOpeningClassTab(sb, ROW_TAG, ARGUMENT_TABLE_TR_CLASS_NAME);
				for (TableCell tableCell : tableRow.getCells()) {
					appendOpeningClassTab(sb, CELL_TAG, ARGUMENT_TABLE_TD_CLASS_NAME);
					sb.append(tableCell.getValue());
					appendClosingTab(sb, CELL_TAG);
				}
				appendClosingTab(sb, ROW_TAG);
			}
			appendClosingTab(sb, TABLE_TAG);
			appendLineBreak(sb);
		}
	}

	private void appendClassSpan(StringBuilder sb, String text, String... cssClass) {
		if (StringUtils.isNotBlank(text)) {
			appendOpeningClassTab(sb, SPAN_TAG, cssClass);
			sb.append(StringUtils.appendIfMissing(text, " "));
			appendClosingTab(sb, SPAN_TAG);
		}
	}

	private void appendClosingTab(StringBuilder sb, String tag) {
		sb.append("</")
			.append(tag)
			.append(">");
	}

	private void appendOpeningClassTab(StringBuilder sb, String tag, String... cssClass) {
		sb.append("<")
			.append(tag);

		if (cssClass.length > 0) {
			sb.append(" class='");
			sb.append(StringUtils.join(cssClass, " "));
			sb.append("'");
		}

		sb.append(">");

	}

	private void appendScenarioLine(ScenarioDefinition scenarioDefinition, StringBuilder sb) {
		String keyword = scenarioDefinition.getKeyword();
		appendClassSpan(sb, keyword, SCENARIO_KEYWORD_CLASS_NAME);
		sb.append(scenarioDefinition.getName());
//		appendLineBreak(sb);
		appendClassSpan(sb, scenarioDefinition.getDescription(), SCENARIO_DESCRIPTION_CLASS_NAME);
//		appendBlankLine(sb);
	}

	private void appendBlankLine(StringBuilder sb) {
		appendLineBreak(sb);
		appendLineBreak(sb);
	}

	private void appendLineBreak(StringBuilder sb) {
		sb.append("</br>");
	}

	//this method append steps lines in scenario outline mode (ie with Gherkin equivalent of dataset so we must do param substitution)
	private void appendStepLine(Step step, Map<String, String> valueByHeader, StringBuilder sb) {
		appendStepKeyword(step, sb);
		String text = step.getText();
		text = performParamSubstitution(valueByHeader, text);
		sb.append(text);
		appendLineBreak(sb);
	}

	private String performParamSubstitution(Map<String, String> valueByHeader, String text) {
		//now substitute each <param> by it's value, if not found inject a placeholder
		Pattern p = Pattern.compile("<[.*?[^>]]*>");
		Matcher m = p.matcher(text);
		while (m.find()) {
			String token = m.group();
			String header = token.substring(1, token.length() - 1);
			String value = valueByHeader.get(header);
			if (StringUtils.isBlank(value)) {
				value = "<NO_DATA>";
			}
			text = text.replace(token, value);
		}
		return text;
	}

	private void appendStepKeyword(Step step, StringBuilder sb) {
		String keyword = step.getKeyword();
		if (!isContinuousKeyword(keyword)) {
			appendLineBreak(sb);
			changeCurrentStepClass(keyword);
		}
		appendClassSpan(sb, keyword, STEP_KEYWORD_CLASS_NAME, currentStepClass);
	}

	private void changeCurrentStepClass(String keyword) {
		if (dialect.getGivenKeywords().contains(keyword)) {
			currentStepClass = GIVEN_KEYWORD_CLASS_NAME;
		} else if (dialect.getWhenKeywords().contains(keyword)) {
			currentStepClass = WHEN_KEYWORD_CLASS_NAME;
		} else if (dialect.getThenKeywords().contains(keyword)) {
			currentStepClass = THEN_KEYWORD_CLASS_NAME;
		} else {
			LOGGER.warn("No css class defined for Gherkin step keyword {} ", keyword);
			currentStepClass = "";
		}
	}

	private boolean isContinuousKeyword(String keyword) {
		return dialect.getAndKeywords().contains(keyword) || dialect.getButKeywords().contains(keyword);
	}

}
