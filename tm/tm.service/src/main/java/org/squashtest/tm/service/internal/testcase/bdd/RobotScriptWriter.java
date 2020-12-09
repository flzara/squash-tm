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
package org.squashtest.tm.service.internal.testcase.bdd;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.context.MessageSource;
import org.squashtest.tm.domain.actionword.ConsumerForActionWordFragmentVisitor;
import org.squashtest.tm.domain.bdd.ActionWord;
import org.squashtest.tm.domain.bdd.ActionWordFragment;
import org.squashtest.tm.domain.bdd.ActionWordParameter;
import org.squashtest.tm.domain.bdd.ActionWordParameterValue;
import org.squashtest.tm.domain.testcase.KeywordTestCase;
import org.squashtest.tm.domain.testcase.KeywordTestStep;
import org.squashtest.tm.domain.testcase.TestStep;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

public class RobotScriptWriter implements BddScriptWriter {

	private static final char TAB_CHAR = '\t';
	private static final char SPACE_CHAR = ' ';
	private static final char NEW_LINE_CHAR = '\n';
	private static final char DOUBLE_QUOTE_CHAR = '\"';

	private static final String DATATABLE_PARAM_FORMAT = "${datatable_%s}";
	private static final String DATATABLE_ROW_PARAM_FORMAT = "${row_%s_%s}";
	private static final String DOCSTRING_PARAM_FORMAT = "${docstring_%s}";
	private static final String CREATE_LIST_KEYWORD = "Create List";
	private static final String SET_VARIABLE_KEYWORD = "Set Variable";


	/**
	 * The implementation for Robot Framework
	 * <li>does not need any translation: MessageSource is not used and null them is safe</li>
	 * <li>does not escape arrow symbols: escapeArrows is not used</li>
	 * @param testCase the test case
	 * @param messageSource unused message source
	 * @param escapeArrows whether to escape arrow symbols
	 * @return the Robot Script of the given KeywordTestCase
	 */
	@Override
	public String writeBddScript(KeywordTestCase testCase, MessageSource messageSource, boolean escapeArrows) {
		StringBuilder stringBuilder = new StringBuilder();
		boolean needToIncludeTfLibrary =
			!testCase.getDatasets().isEmpty() && testCase.containsStepsUsingTcParam();
		appendSettingsTable(stringBuilder, needToIncludeTfLibrary);
		appendTestCasesTable(stringBuilder, testCase.getName(), testCase.getSteps(), needToIncludeTfLibrary);
		return stringBuilder.toString();
	}

	/**
	 * Append the test cases table to the script.
	 * @param stringBuilder string builder of the script
	 * @param testCaseName name of the test case
	 * @param steps set of steps in the test case
	 * @return whether the test case uses data sets
	 */
	private void appendTestCasesTable(
		StringBuilder stringBuilder, String testCaseName, List<TestStep> steps, boolean needToIncludeTfLibrary) {

		stringBuilder.append("*** Test Cases ***\n");
		stringBuilder.append(testCaseName);
		stringBuilder.append(
			buildParamLines(steps, needToIncludeTfLibrary));
		stringBuilder.append(
			buildStepsLines(steps));

	}

	private StringBuilder buildStepsLines(List<TestStep> steps) {
		StringBuilder stepBuilder = new	StringBuilder();
		int dataTableCounter = 1;
		int docStringCounter = 1;
		if(!steps.isEmpty()) {
			stepBuilder.append(NEW_LINE_CHAR);
			for (TestStep step : steps) {
				KeywordTestStep keywordStep = (KeywordTestStep) step;
				String stepScript = writeBddStepScript(keywordStep, dataTableCounter, docStringCounter);
				if (nonNull(keywordStep.getDatatable()) && !Strings.isBlank(keywordStep.getDatatable())) {
					dataTableCounter++;
				} else if (Strings.isBlank(keywordStep.getDatatable()) && !Strings.isBlank(keywordStep.getDocstring())) {
					docStringCounter++;
				}
				stepBuilder
					.append(TAB_CHAR)
					.append(stepScript)
					.append(NEW_LINE_CHAR);
			}
			stepBuilder.deleteCharAt(stepBuilder.length() - 1);
		}
		return stepBuilder;
	}

	private StringBuilder buildParamLines(List<TestStep> steps, boolean needToIncludeTfLibrary) {
		StringBuilder paramBuilder = new StringBuilder();
		if (needToIncludeTfLibrary) {
			paramBuilder.append(
				buildTestCaseParametersLines(steps));
		}
		boolean usesDataTables = steps.stream()
			.anyMatch(step -> {
				String dataTable = ((KeywordTestStep) step).getDatatable();
				return nonNull(dataTable) && !Strings.isBlank(dataTable); });
		if (usesDataTables) {
			paramBuilder.append(
				buildDatatableParametersLines(steps));
		}
		boolean usesDocStrings = steps.stream()
			.anyMatch(step -> {
				String dataTable = ((KeywordTestStep) step).getDatatable();
				String docString = ((KeywordTestStep) step).getDocstring();
				return Strings.isBlank(dataTable) && !Strings.isBlank(docString);
			});
		if (usesDocStrings) {
			paramBuilder.append(
				buildDocStringParametersLines(steps));
		}
		return paramBuilder;
	}

	private StringBuilder buildTestCaseParametersLines(List<TestStep> steps) {
		StringBuilder testCaseParamBuilder = new StringBuilder();
		steps
			.stream()
			.flatMap(step -> ((KeywordTestStep) step).getParamValues().stream())
			.filter(distinctByKey(ActionWordParameterValue::getValue))
			.forEach(paramValue -> {
				if(paramValue.isLinkedToTestCaseParam()) {
					String value = paramValue.getValue();
					String paramName = value.substring(1, value.length()-1);
					testCaseParamBuilder.append(NEW_LINE_CHAR);
					testCaseParamBuilder.append("\t${" + paramName + "} =\tGet Test Param\tDS_" + paramName);
				}
			});
		testCaseParamBuilder.append(NEW_LINE_CHAR);
		return testCaseParamBuilder;
	}

	private StringBuilder buildDatatableParametersLines(List<TestStep> steps) {
		int tableNumber = 1;
		StringBuilder datatableParamBuilder = new StringBuilder();
		for (TestStep step : steps) {
			String datatable = ((KeywordTestStep) step).getDatatable();
			if (nonNull(datatable) && !Strings.isBlank(datatable)) {
				StringBuilder tableVariableBuilder = new StringBuilder();
				tableVariableBuilder
					.append(TAB_CHAR)
					.append(String.format(DATATABLE_PARAM_FORMAT, tableNumber))
					.append('=')
					.append(TAB_CHAR)
					.append(CREATE_LIST_KEYWORD);
				List<List<String>> rows = extractRowsFromDataTable(datatable);
				int rowNumber = 1;
				for (List<String> row : rows) {
					StringBuilder rowVariableBuilder = new StringBuilder();
					rowVariableBuilder
						.append(TAB_CHAR)
						.append(String.format(DATATABLE_ROW_PARAM_FORMAT, tableNumber, rowNumber))
						.append('=')
						.append(TAB_CHAR)
						.append(CREATE_LIST_KEYWORD);
					tableVariableBuilder
						.append(TAB_CHAR)
						.append(String.format(DATATABLE_ROW_PARAM_FORMAT, tableNumber, rowNumber));
					for (String content : row) {
						rowVariableBuilder
							.append(TAB_CHAR)
							.append(content);
					}
					datatableParamBuilder.append(NEW_LINE_CHAR).append(rowVariableBuilder);
					rowNumber++;
				}
				datatableParamBuilder.append(NEW_LINE_CHAR).append(tableVariableBuilder);
				tableNumber++;
			}
		}
		datatableParamBuilder.append(NEW_LINE_CHAR);
		return datatableParamBuilder;
	}

	private StringBuilder buildDocStringParametersLines(List<TestStep> steps) {
		int docStringCounter = 1;
		StringBuilder docStringParamBuilder = new StringBuilder()
			.append(NEW_LINE_CHAR);
		for (TestStep step : steps) {
			String dataTable = ((KeywordTestStep) step).getDatatable();
			String docString = ((KeywordTestStep) step).getDocstring();
			if (Strings.isBlank(dataTable) && !Strings.isBlank(docString)) {
				docStringParamBuilder
					.append(TAB_CHAR)
					.append(String.format(DOCSTRING_PARAM_FORMAT, docStringCounter))
					.append('=')
					.append(TAB_CHAR)
					.append(SET_VARIABLE_KEYWORD)
					.append(TAB_CHAR)
					.append(StringEscapeUtils.escapeJava(docString))
					.append(NEW_LINE_CHAR);
					docStringCounter++;
			}
		}
		return docStringParamBuilder;
	}

	private List<List<String>> extractRowsFromDataTable(String datatableAsString) {
		String[] rowsAsString = datatableAsString.split("\n");
		List<List<String>> dataTable = new ArrayList<>(rowsAsString.length);
		for (String rowAsString : rowsAsString) {
			String[] rowParts = rowAsString.split("\\|");
			List<String> dataRow =
				Arrays.stream(rowParts)
					.filter(part -> !Strings.isBlank(part))
					.map(part -> part.trim())
					.collect(Collectors.toList());
			dataTable.add(dataRow);
		}
		return dataTable;
	}

	/**
	 * Predicate used to filter a stream by distinct attribute.
	 */
	private <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
		Set<Object> seen = new HashSet<>();
		return t -> seen.add(keyExtractor.apply(t));
	}

	/**
	 * Prepend the settings table to the script.<br\>
	 * This happens after the appending of test cases table because
	 * this other process allow to know if the test case uses data sets and if it is needed to include
	 * the SquashTf associated library.
	 * @param stringBuilder string builder
	 * @param includeSquashTfLibrary whether the tf library has to be included in the script
	 */
	private void appendSettingsTable(StringBuilder stringBuilder, boolean includeSquashTfLibrary) {
		StringBuilder settingsBuilder = new StringBuilder();
		settingsBuilder.append("*** Settings ***\n");
		settingsBuilder.append("Resource\tsquash_resources.resource\n");
		if(includeSquashTfLibrary) {
			settingsBuilder.append("Library\t\tsquash_tf.TFParamService\n");
		}
		settingsBuilder.append(NEW_LINE_CHAR);
		stringBuilder.append(settingsBuilder);
	}

	public String writeBddStepScript(KeywordTestStep testStep, int dataTableCounter, int docStringCounter) {
		ActionWord actionWord = testStep.getActionWord();
		List<ActionWordFragment> fragments = actionWord.getFragments();
		List<ActionWordParameterValue> parameterValues = testStep.getParamValues();

		String keywordScript = testStep.getKeyword().getLabel();
		String actionWordScript = generateStepScriptFromActionWordFragments(fragments, parameterValues);
		StringBuilder stepBuilder = new StringBuilder();
		stepBuilder.append(keywordScript).append(SPACE_CHAR).append(actionWordScript);
		if (nonNull(testStep.getDatatable()) && !Strings.isBlank(testStep.getDatatable())) {
			stepBuilder
				.append(SPACE_CHAR)
				.append(DOUBLE_QUOTE_CHAR)
				.append(String.format(DATATABLE_PARAM_FORMAT, dataTableCounter))
				.append(DOUBLE_QUOTE_CHAR);
		} else if (Strings.isBlank(testStep.getDatatable()) && !Strings.isBlank(testStep.getDocstring())) {
			stepBuilder.append(SPACE_CHAR)
				.append(DOUBLE_QUOTE_CHAR)
				.append(String.format(DOCSTRING_PARAM_FORMAT, docStringCounter))
				.append(DOUBLE_QUOTE_CHAR);
		}
		return  stepBuilder.toString();
	}

	private String generateStepScriptFromActionWordFragments(
		List<ActionWordFragment> fragments, List<ActionWordParameterValue> parameterValues) {
		StringBuilder stringBuilder = new StringBuilder();
		Consumer<ActionWordParameter> consumer =
			parameter -> appendParamValueToGenerateScript(parameter, parameterValues, stringBuilder);
		ConsumerForActionWordFragmentVisitor visitor = new ConsumerForActionWordFragmentVisitor(consumer, stringBuilder);

		for(ActionWordFragment fragment : fragments) {
			fragment.accept(visitor);
		}
		return stringBuilder.toString();
	}

	private void appendParamValueToGenerateScript(ActionWordParameter parameter, List<ActionWordParameterValue> parameterValues, StringBuilder stringBuilder) {
		Optional<ActionWordParameterValue> paramValue =
			parameterValues.stream()
				.filter(pv ->
					pv.getActionWordParam() != null
						&& pv.getActionWordParam().getId().equals(parameter.getId()))
				.findAny();
		paramValue.ifPresent(
			actionWordParameterValue -> updateBuilderWithParamValue(stringBuilder, actionWordParameterValue)
		);
	}

	private void updateBuilderWithParamValue(StringBuilder stringBuilder, ActionWordParameterValue actionWordParameterValue) {
		String paramValue = actionWordParameterValue.getValue();
		if ("\"\"".equals(paramValue)) {
			stringBuilder.append(paramValue);
		} else if (actionWordParameterValue.isLinkedToTestCaseParam()) {
			String replacedCharactersString =
				paramValue.replace("<", "${").replace(">", "}");
			stringBuilder.append(replacedCharactersString);
		} else {
			stringBuilder.append(DOUBLE_QUOTE_CHAR + paramValue + DOUBLE_QUOTE_CHAR);
		}
	}
}

