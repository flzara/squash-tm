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

import org.springframework.context.MessageSource;
import org.squashtest.tm.domain.actionword.ConsumerForActionWordFragmentVisitor;
import org.squashtest.tm.domain.bdd.ActionWord;
import org.squashtest.tm.domain.bdd.ActionWordFragment;
import org.squashtest.tm.domain.bdd.ActionWordParameter;
import org.squashtest.tm.domain.bdd.ActionWordParameterValue;
import org.squashtest.tm.domain.testcase.KeywordTestCase;
import org.squashtest.tm.domain.testcase.KeywordTestStep;
import org.squashtest.tm.domain.testcase.TestStep;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RobotScriptWriter implements BddScriptWriter {

	private static final char TAB_CHAR = '\t';
	private static final char SPACE_CHAR = ' ';
	private static final char NEW_LINE_CHAR = '\n';
	private static final char DOUBLE_QUOTE_CHAR = '\"';

	/**
	 * The implementation for Robot Framework
	 * <li>does not need any translation: MessageSource is not used and null is safe</li>
	 * <li>does not escape arrow symbols: escapeArrows is not used</li>
	 * @param testCase the test case
	 * @param messageSource unused message source
	 * @param escapeArrows whether to escape arrow symbols
	 * @return the bdd script of the given KeywordTestCase
	 */
	@Override
	public String writeBddScript(KeywordTestCase testCase, MessageSource messageSource, boolean escapeArrows) {
		StringBuilder stringBuilder = new StringBuilder();
		boolean testCaseUsesDataSets = appendTestCasesTable(stringBuilder, testCase.getName(), testCase.getSteps());
		boolean needToIncludeTfLibrary = !testCase.getDatasets().isEmpty() && testCaseUsesDataSets;
		prependSettingsTable(stringBuilder, needToIncludeTfLibrary);
		return stringBuilder.toString();
	}

	/**
	 * Append the test cases table to the script and return whether the test case uses data sets.
	 * @param stringBuilder string builder of the script
	 * @param testCaseName name of the test case
	 * @param steps set of steps in the test case
	 * @return whether the test case uses data sets
	 */
	private boolean appendTestCasesTable(StringBuilder stringBuilder, String testCaseName, List<TestStep> steps) {
		boolean hasTcParamInTestCase = false;
		stringBuilder.append("*** Test Cases ***\n");
		stringBuilder.append(testCaseName);
		if(!steps.isEmpty()) {
			stringBuilder.append(NEW_LINE_CHAR);
			for (TestStep step : steps) {
				KeywordTestStep keywordStep = (KeywordTestStep) step;
				String stepScript = writeBddStepScript(keywordStep,null, null, false);
				if(!hasTcParamInTestCase) {
					hasTcParamInTestCase = keywordStep.hasTCParam();
				}
				stringBuilder
					.append(TAB_CHAR)
					.append(stepScript)
					.append(NEW_LINE_CHAR);
			}
			stringBuilder.deleteCharAt(stringBuilder.length() - 1);
		}
		return hasTcParamInTestCase;
	}

	/**
	 * Prepend the settings table to the script.<br\>
	 * This happens after the appending of test cases table because
	 * this other process allow to know if the test case uses data sets and if it is needed to include
	 * the SquashTf associated library.
	 * @param stringBuilder string builder
	 * @param includeSquashTfLibrary whether the tf library has to be included in the script
	 */
	private void prependSettingsTable(StringBuilder stringBuilder, boolean includeSquashTfLibrary) {
		StringBuilder prependBuilder = new StringBuilder();
		prependBuilder.append("*** Settings ***\n");
		prependBuilder.append("Resource	squash_resources.resource\n");
		if(includeSquashTfLibrary) {
			prependBuilder.append("Library	squash_tf.TFParamService\n");
		}
		prependBuilder.append(NEW_LINE_CHAR);
		stringBuilder.insert(0, prependBuilder);
	}

	/**
	 * Implementation for Robot Framework
	 * <li>does not need any translation: MessageSource and Locale is not used and null is safe</li>
	 * <li>does not escape arrow symbols: escapeArrows is not used</li>
	 * @param testStep test step
	 * @param messageSource the message source for potential translation
	 * @param locale
	 * @param escapeArrows whether to escape arrow symbols
	 * @return
	 */
	@Override
	public String writeBddStepScript(KeywordTestStep testStep, MessageSource messageSource, Locale locale, boolean escapeArrows) {
		ActionWord actionWord = testStep.getActionWord();
		List<ActionWordFragment> fragments = actionWord.getFragments();
		List<ActionWordParameterValue> parameterValues = testStep.getParamValues();

		String keywordScript = testStep.getKeyword().getLabel();
		String actionWordScript = generateStepScriptFromActionWordFragments(fragments, parameterValues, escapeArrows);
		return keywordScript + SPACE_CHAR + actionWordScript;
	}

	private String generateStepScriptFromActionWordFragments(
		List<ActionWordFragment> fragments, List<ActionWordParameterValue> parameterValues, boolean escapeArrows) {
		StringBuilder stringBuilder = new StringBuilder();
		Consumer<ActionWordParameter> consumer =
			parameter -> appendParamValueToGenerateScript(parameter, parameterValues, stringBuilder, escapeArrows);
		ConsumerForActionWordFragmentVisitor visitor = new ConsumerForActionWordFragmentVisitor(consumer, stringBuilder);

		for(ActionWordFragment fragment : fragments) {
			fragment.accept(visitor);
		}
		return stringBuilder.toString();
	}

	private void appendParamValueToGenerateScript(ActionWordParameter parameter, List<ActionWordParameterValue> parameterValues, StringBuilder stringBuilder, boolean escapeArrows) {
		Optional<ActionWordParameterValue> paramValue =
			parameterValues.stream()
				.filter(pv ->
					pv.getActionWordParam() != null
					&& pv.getActionWordParam().getId().equals(parameter.getId()))
				.findAny();
		paramValue.ifPresent(
			actionWordParameterValue -> updateBuilderWithParamValue(stringBuilder, actionWordParameterValue, escapeArrows)
		);
	}

	private void updateBuilderWithParamValue(StringBuilder stringBuilder, ActionWordParameterValue actionWordParameterValue, boolean escapeArrows) {
		String paramValue = actionWordParameterValue.getValue();
		if("\"\"".equals(paramValue)) {
			stringBuilder.append(paramValue);
			return;
		}

		Pattern pattern = Pattern.compile("<[^\"]+>");
		Matcher matcher = pattern.matcher(paramValue);
		if(matcher.matches()) {
			String replacedCharactersString =
				paramValue.replace("<", "${").replace(">", "}");
			stringBuilder.append(replacedCharactersString);
			return;
		}

		stringBuilder.append(DOUBLE_QUOTE_CHAR + paramValue + DOUBLE_QUOTE_CHAR);
	}


}

