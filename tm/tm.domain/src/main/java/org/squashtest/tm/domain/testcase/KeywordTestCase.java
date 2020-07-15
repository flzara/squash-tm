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
package org.squashtest.tm.domain.testcase;

import org.springframework.context.MessageSource;
import org.squashtest.tm.domain.bdd.util.ActionWordUtil;

import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Transient;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static org.squashtest.tm.domain.bdd.ActionWord.ACTION_WORD_UNDERSCORE;
import static org.squashtest.tm.domain.bdd.util.ActionWordUtil.updateNumberValue;

@Entity
@PrimaryKeyJoinColumn(name = "TCLN_ID")
public class KeywordTestCase extends TestCase {
	private static final String TAB_CHAR = "\t";
	private static final String DOUBLE_TAB_CHAR = "\t\t";
	private static final String NEW_LINE_CHAR = "\n";
	private static final String SPACE_CHAR = " ";
	private static final String VERTICAL_BAR = "|";
	private static final String ACROBAT_CHAR = "@";

	@Transient
	private boolean hasTCParamInTestCase = false;

	public static KeywordTestCase createBlankKeywordTestCase() {
		KeywordTestCase res = new KeywordTestCase();
		setAttributesAsNullForBlankTestCase(res);
		return res;
	}

	@Override
	public void accept(TestCaseVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public KeywordTestCase createCopy() {
		KeywordTestCase copy = new KeywordTestCase();
		populateCopiedTestCaseAttributes(copy);
		return copy;
	}

	public String writeTestCaseScript(MessageSource messageSource) {
		StringBuilder builder = new StringBuilder();
		List<TestStep> testSteps = getSteps();
		Set<Dataset> datasetSet = getDatasets();
		String testCaseName = getName();

		Locale locale = getProject().getBddScriptLanguage().getLocale();
		String language = locale.toLanguageTag();

		if (!testSteps.isEmpty()) {
			addAllStepsScriptWithoutScenarioToBuilder(builder, testSteps, locale, messageSource);
			addScenarioAndDatasetToBuilder(builder, testCaseName, datasetSet, locale, messageSource);
			hasTCParamInTestCase = false;
		}
		addLanguageAndFeatureToBuilder(builder, testCaseName, language, locale, messageSource);
		return builder.toString();
	}

	private void addAllStepsScriptWithoutScenarioToBuilder(StringBuilder builder, List<TestStep> testSteps, Locale locale, MessageSource messageSource) {
		for (TestStep step : testSteps) {
			KeywordTestStep keywordStep = (KeywordTestStep) step;
			String stepActionWordScript = keywordStep.writeTestStepActionWordScript();
			raiseHasTCParamFlag(keywordStep.hasTCParam());
			String internationalizedKeyword = messageSource.getMessage(keywordStep.getKeyword().i18nKeywordNameKey(), null, locale);
			builder
				.append(DOUBLE_TAB_CHAR)
				.append(internationalizedKeyword)
				.append(SPACE_CHAR)
				.append(stepActionWordScript)
				.append(NEW_LINE_CHAR);
		}
		builder.deleteCharAt(builder.length() - 1);
	}

	private void raiseHasTCParamFlag(boolean hasTCParam) {
		if (!hasTCParamInTestCase) {
			hasTCParamInTestCase = hasTCParam;
		}
	}

	private void addScenarioAndDatasetToBuilder(StringBuilder builder, String testCaseName, Set<Dataset> datasetSet, Locale locale, MessageSource messageSource) {
		if (!datasetSet.isEmpty() && hasTCParamInTestCase) {
			addScenarioToBuilder(builder, testCaseName, messageSource.getMessage("testcase.bdd.script.label.scenario-outline", null, locale));
			generateAllDatasetAndExamplesScript(builder, datasetSet, locale, messageSource);
		} else {
			addScenarioToBuilder(builder, testCaseName, messageSource.getMessage("testcase.bdd.script.label.scenario", null, locale));
		}
	}

	private void addScenarioToBuilder(StringBuilder builder, String testCaseName, String scenario) {
		StringBuilder preBuilder = new StringBuilder();
		preBuilder.append(NEW_LINE_CHAR)
			.append(NEW_LINE_CHAR)
			.append(TAB_CHAR)
			.append(scenario)
			.append(testCaseName)
			.append(NEW_LINE_CHAR);
		builder.insert(0, preBuilder);
	}

	private void generateAllDatasetAndExamplesScript(StringBuilder builder, Set<Dataset> datasetSet, Locale locale, MessageSource messageSource) {
		for (Dataset dataset : datasetSet) {
			String datasetScript = generateDatasetAndExampleScript(dataset, locale, messageSource);
			builder.append(NEW_LINE_CHAR)
				.append(NEW_LINE_CHAR)
				.append(datasetScript);
		}
	}

	private String generateDatasetAndExampleScript(Dataset dataset, Locale locale, MessageSource messageSource) {
		String datasetTagLine = generateDatasetTagLine(dataset);
		String exampleLine = DOUBLE_TAB_CHAR + messageSource.getMessage("testcase.bdd.script.label.examples", null, locale) + NEW_LINE_CHAR;
		String paramNameAndValueLines = generateDatasetParamNamesAndValues(dataset);
		return datasetTagLine + exampleLine + paramNameAndValueLines;
	}

	private String generateDatasetTagLine(Dataset dataset) {
		String originalStr = dataset.getName();
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

	private void addLanguageAndFeatureToBuilder(StringBuilder builder, String testCaseName, String language, Locale locale, MessageSource messageSource) {
		StringBuilder subBuilder = new StringBuilder();
		subBuilder.append(messageSource.getMessage("testcase.bdd.script.label.language", null, locale))
			.append(language)
			.append(NEW_LINE_CHAR)
			.append(messageSource.getMessage("testcase.bdd.script.label.feature", null, locale))
			.append(testCaseName);
		builder.insert(0, subBuilder);
	}
}
