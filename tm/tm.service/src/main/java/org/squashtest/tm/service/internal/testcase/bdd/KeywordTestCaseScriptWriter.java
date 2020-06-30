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
import org.springframework.context.MessageSource;
import org.squashtest.tm.domain.actionword.ConsumerForActionWordFragmentVisitor;
import org.squashtest.tm.domain.bdd.ActionWord;
import org.squashtest.tm.domain.bdd.ActionWordFragment;
import org.squashtest.tm.domain.bdd.ActionWordParameter;
import org.squashtest.tm.domain.bdd.ActionWordParameterValue;
import org.squashtest.tm.domain.bdd.util.ActionWordUtil;
import org.squashtest.tm.domain.testcase.Dataset;
import org.squashtest.tm.domain.testcase.DatasetParamValue;
import org.squashtest.tm.domain.testcase.KeywordTestStep;
import org.squashtest.tm.domain.testcase.TestStep;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.squashtest.tm.domain.bdd.ActionWord.ACTION_WORD_DOUBLE_QUOTE;
import static org.squashtest.tm.domain.bdd.ActionWord.ACTION_WORD_UNDERSCORE;
import static org.squashtest.tm.domain.bdd.util.ActionWordUtil.isNumber;

/**
 * @author qtran - created on 24/06/2020
 */
public class KeywordTestCaseScriptWriter {
	private static final String TAB_CHAR = "\t";
	private static final String DOUBLE_TAB_CHAR = "\t\t";
	private static final String NEW_LINE_CHAR = "\n";
	private static final String SPACE_CHAR = " ";
	private static final String VERTICAL_BAR = "|";
	private static final String EXAMPLE = "Examples:";
	private static final String ACROBAT_CHAR = "@";

	private boolean hasTCParamInScript = false;

	private MessageSource messageSource;

	private StringBuilder builder = new StringBuilder();

	public KeywordTestCaseScriptWriter(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public String writeScript(List<TestStep> testSteps, Set<Dataset> datasetSet, String testCaseName, String language, Locale locale) {
		if (!testSteps.isEmpty()) {
			addAllStepsScriptWithoutScenarioToBuilder(testSteps, locale);
			addScenarioAndDatasetToBuilder(testCaseName, datasetSet);
			hasTCParamInScript = false;
		}
		addLanguageAndFeatureToBuilder(testCaseName, language);
		return builder.toString();
	}

	private void addLanguageAndFeatureToBuilder(String testCaseName, String language) {
		StringBuilder subBuilder =  new StringBuilder();
		subBuilder.append("# language: ")
			.append(language)
			.append(NEW_LINE_CHAR)
			.append("Feature: ")
			.append(testCaseName);
		builder.insert(0, subBuilder);
	}

	private void addAllStepsScriptWithoutScenarioToBuilder(List<TestStep> testSteps, Locale locale) {
		for(TestStep step : testSteps) {
			KeywordTestStep keywordStep = (KeywordTestStep) step;
			String stepActionWordScript = generateActionWordScript(keywordStep);
			String internationalizedKeyword = messageSource.getMessage(keywordStep.getKeyword().i18nKeywordNameKey(), null, locale);
			builder
				.append(DOUBLE_TAB_CHAR)
				.append(internationalizedKeyword)
				.append(SPACE_CHAR)
				.append(stepActionWordScript)
				.append(NEW_LINE_CHAR);
		}
		builder.deleteCharAt(builder.length()-1);
	}

	private String generateActionWordScript(KeywordTestStep keywordTestStep) {
		ActionWord actionWord = keywordTestStep.getActionWord();
		List<ActionWordFragment> fragments = actionWord.getFragments();
		List<ActionWordParameterValue> paramValues = keywordTestStep.getParamValues();
		return generateStepScriptFromActionWordFragments(fragments, paramValues);
	}

	private String generateStepScriptFromActionWordFragments(List<ActionWordFragment> fragments, List<ActionWordParameterValue> paramValues) {
		StringBuilder stepBuilder = new StringBuilder();
		Consumer<ActionWordParameter> consumer = parameter ->
			appendParamValueToGenerateScript(parameter, paramValues, stepBuilder);

		ConsumerForActionWordFragmentVisitor visitor = new ConsumerForActionWordFragmentVisitor(consumer, stepBuilder);

		for (ActionWordFragment fragment : fragments) {
			fragment.accept(visitor);
		}
		return stepBuilder.toString();
	}

	private void appendParamValueToGenerateScript(ActionWordParameter param, List<ActionWordParameterValue> paramValues, StringBuilder stepBuilder) {
		Optional<ActionWordParameterValue> paramValue =
			paramValues.stream().filter(pv -> pv.getActionWordParam() != null && pv.getActionWordParam().getId().equals(param.getId())).findAny();
		paramValue.ifPresent(
			actionWordParameterValue -> updateBuilderWithParamValue(stepBuilder, actionWordParameterValue)
		);
	}

	private void updateBuilderWithParamValue(StringBuilder builder, ActionWordParameterValue actionWordParameterValue) {
		String paramValue = actionWordParameterValue.getValue();
		if ("\"\"".equals(paramValue)) {
			builder.append(paramValue);
			return;
		}

		Pattern pattern = Pattern.compile("<[^\"]+>");
		Matcher matcher = pattern.matcher(paramValue);
		if (matcher.matches()) {
			hasTCParamInScript = true;
			//TODO-QUAN: to show the script content temporarily on page. To be removed when script is generated on file
			String replaceHTMLCharactersStr = StringEscapeUtils.escapeHtml4(paramValue);
			builder.append(replaceHTMLCharactersStr);
			return;
		}
		String updatedParamValue = updateNumberValue(paramValue);
		builder.append(updatedParamValue);
	}

	private void addScenarioAndDatasetToBuilder(String testCaseName, Set<Dataset> datasetSet) {
		if (!datasetSet.isEmpty() && hasTCParamInScript) {
			addScenarioToBuilder(testCaseName, "Scenario Outline: ");
			generateAllDatasetAndExamplesScript(datasetSet);
		} else {
			addScenarioToBuilder(testCaseName, "Scenario: ");
		}
	}

	private void addScenarioToBuilder(String testCaseName, String scenario) {
		StringBuilder preBuilder = new StringBuilder();
		preBuilder.append(NEW_LINE_CHAR)
			.append(NEW_LINE_CHAR)
			.append(TAB_CHAR)
			.append(scenario)
			.append(testCaseName)
			.append(NEW_LINE_CHAR);
		builder.insert(0, preBuilder);
	}

	private void generateAllDatasetAndExamplesScript(Set<Dataset> datasetSet) {
		for (Dataset dataset : datasetSet) {
			String datasetScript = generateDatasetAndExampleScript(dataset);
			builder.append(NEW_LINE_CHAR)
				.append(NEW_LINE_CHAR)
				.append(datasetScript);
		}
	}


	private String generateDatasetAndExampleScript(Dataset dataset) {
		String datasetTagLine = generateDatasetTagLine(dataset);
		String exampleLine = DOUBLE_TAB_CHAR + EXAMPLE + NEW_LINE_CHAR;
		String paramNameAndValueLines = generateDatasetParamNamesAndValues(dataset);
		return datasetTagLine +	exampleLine + paramNameAndValueLines;
	}

	private String generateDatasetTagLine(Dataset dataset) {
		String originalStr =  dataset.getName();
		String trimmedAndRemovedExtraSpacesStr = ActionWordUtil.replaceExtraSpacesInText(originalStr.trim());
		String replacedSpacesStr = trimmedAndRemovedExtraSpacesStr.replaceAll(SPACE_CHAR, ACTION_WORD_UNDERSCORE);
		return DOUBLE_TAB_CHAR + ACROBAT_CHAR + replacedSpacesStr + NEW_LINE_CHAR;
	}

	private String generateDatasetParamNamesAndValues(Dataset dataset) {
		Set<DatasetParamValue> datasetParamValues = dataset.getParameterValues();
		Map<String, String> paramNameValueMap = datasetParamValues.stream()
			.collect(Collectors.toMap(datasetParamValue -> datasetParamValue.getParameter().getName(),
				DatasetParamValue::getParamValue));

		TreeMap<String, String> sortedMap = new TreeMap<>(paramNameValueMap);
		return generateSortedDatasetParamNamesAndValues(sortedMap);
	}

	private String generateSortedDatasetParamNamesAndValues(TreeMap<String, String> paramNames) {
		StringBuilder lineBuilder1 = new StringBuilder();
		lineBuilder1.append(DOUBLE_TAB_CHAR);
		StringBuilder lineBuilder2 = new StringBuilder();
		lineBuilder2.append(DOUBLE_TAB_CHAR);

		paramNames.forEach((paramName, paramValue) -> addParamNameAndValueIntoTwoBuilders(lineBuilder1, lineBuilder2, paramName, paramValue));

		lineBuilder1.append(VERTICAL_BAR).append(NEW_LINE_CHAR);
		lineBuilder2.append(VERTICAL_BAR);
		return lineBuilder1.append(lineBuilder2).toString();
	}

	private void addParamNameAndValueIntoTwoBuilders(StringBuilder lineBuilder1, StringBuilder lineBuilder2, String paramName, String paramValue) {
		addInfoIntoBuilder(lineBuilder1, paramName);
		String trimmedParamValue = paramValue.trim();
		String updatedParamValue = updateNumberValue(trimmedParamValue);
		addInfoIntoBuilder(lineBuilder2, updatedParamValue);
	}

	private void addInfoIntoBuilder(StringBuilder lineBuilder, String info) {
		lineBuilder.append(VERTICAL_BAR)
			.append(SPACE_CHAR)
			.append(info)
			.append(SPACE_CHAR);
	}

	private String updateNumberValue(String paramValue) {
		if (isNumber(paramValue)){
			return paramValue;
		}
		return ACTION_WORD_DOUBLE_QUOTE + paramValue + ACTION_WORD_DOUBLE_QUOTE;
	}

}
