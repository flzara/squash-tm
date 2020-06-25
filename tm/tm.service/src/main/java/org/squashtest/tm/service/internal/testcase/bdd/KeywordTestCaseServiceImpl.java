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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.actionword.ConsumerForActionWordFragmentVisitor;
import org.squashtest.tm.domain.bdd.ActionWord;
import org.squashtest.tm.domain.bdd.ActionWordFragment;
import org.squashtest.tm.domain.bdd.ActionWordParameter;
import org.squashtest.tm.domain.bdd.ActionWordParameterValue;
import org.squashtest.tm.domain.testcase.KeywordTestCase;
import org.squashtest.tm.domain.testcase.KeywordTestStep;
import org.squashtest.tm.domain.testcase.TestCaseKind;
import org.squashtest.tm.domain.testcase.TestStep;
import org.squashtest.tm.service.testcase.bdd.KeywordTestCaseService;
import org.squashtest.tm.service.testcase.scripted.ScriptToFileStrategy;

import javax.inject.Inject;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
@Transactional
public class KeywordTestCaseServiceImpl implements KeywordTestCaseService {

	@Inject
	private MessageSource messageSource;

	@Override
	public String createFileName(KeywordTestCase keywordTestCase) {
		// Get techno from Project
		// Get corresponding Strategy
		ScriptToFileStrategy strategy = ScriptToFileStrategy.strategyFor(TestCaseKind.KEYWORD);
		// Create fileName
		return strategy.createFilenameFor(keywordTestCase);
	}

	@Override
	public String createBackupFileName(KeywordTestCase keywordTestCase) {
		// Get techno from Project
		// Get corresponding Strategy
		ScriptToFileStrategy strategy = ScriptToFileStrategy.strategyFor(TestCaseKind.KEYWORD);
		// Create fileName
		return strategy.backupFilenameFor(keywordTestCase);
	}

	@Override
	public String buildFilenameMatchPattern(KeywordTestCase keywordTestCase) {
		//TODO:refactoring me for all above methods!!!

		// Get techno from Project
		// Get corresponding Strategy
		ScriptToFileStrategy strategy = ScriptToFileStrategy.strategyFor(TestCaseKind.KEYWORD);
		// Create fileName
		return strategy.buildFilenameMatchPattern(keywordTestCase);
	}

	@Override
	public String writeScriptFromTestCase(KeywordTestCase keywordTestCase) {
		Locale locale = keywordTestCase.getProject().getBddScriptLanguage().getLocale();
		String language = locale.toLanguageTag();
		String testCaseName = keywordTestCase.getName();

		List<TestStep> testSteps = keywordTestCase.getSteps();

		String stepScript = generateStepScript(testSteps, testCaseName, locale);
		return generateScript(testCaseName, stepScript, language);
	}

	private String generateStepScript(List<TestStep> testSteps, String testCaseName, Locale locale) {
		if (testSteps.isEmpty()) {
			return "";
		}
		StringBuilder builder = new StringBuilder();
		builder.append("\n\n");
		builder.append("\tScenario: ").append(testCaseName).append("\n");
		for(TestStep step : testSteps) {
			KeywordTestStep keywordStep = (KeywordTestStep) step;
			String stepActionWordScript = generateScriptFromActionWord(keywordStep);
			String InternationalizedKeyword = messageSource.getMessage(keywordStep.getKeyword().i18nKeywordNameKey(), null, locale);
			builder
				.append("\t\t")
				.append(InternationalizedKeyword)
				.append(" ")
				.append(stepActionWordScript)
				.append("\n");
		}
		String testStepScript = builder.toString();
		//remove the last empty line
		return testStepScript.substring(0, testStepScript.length()-1);
	}

	private String generateScriptFromActionWord(KeywordTestStep keywordTestStep) {
		ActionWord actionWord = keywordTestStep.getActionWord();
		List<ActionWordFragment> fragments = actionWord.getFragments();
		List<ActionWordParameterValue> paramValues = keywordTestStep.getParamValues();
		return generateStepScriptFromActionWordFragments(fragments, paramValues);
	}

	private String generateStepScriptFromActionWordFragments(List<ActionWordFragment> fragments, List<ActionWordParameterValue> paramValues) {
		StringBuilder builder = new StringBuilder();
		Consumer<ActionWordParameter> consumer = parameter ->
			appendParamValueToGenerateScript(parameter, paramValues, builder);
		
		ConsumerForActionWordFragmentVisitor visitor = new ConsumerForActionWordFragmentVisitor(consumer, builder);

		for (ActionWordFragment fragment : fragments) {
			fragment.accept(visitor);
		}
		return builder.toString();
	}

	private void appendParamValueToGenerateScript(ActionWordParameter param, List<ActionWordParameterValue> paramValues, StringBuilder builder) {
		Optional<ActionWordParameterValue> paramValue =
			paramValues.stream().filter(pv -> pv.getActionWordParam() != null && pv.getActionWordParam().getId().equals(param.getId())).findAny();
		paramValue.ifPresent(
			actionWordParameterValue -> updateBuilderWithParamValue(builder, actionWordParameterValue)
		);
	}

	private StringBuilder updateBuilderWithParamValue(StringBuilder builder, ActionWordParameterValue actionWordParameterValue) {
		String paramValue = actionWordParameterValue.getValue();
		if ("\"\"".equals(paramValue)) {
			return builder.append(paramValue);
		}

		Pattern pattern = Pattern.compile("<[^\"]+>");
		Matcher matcher = pattern.matcher(paramValue);
		if (matcher.matches()) {
			//TODO-QUAN: to show the script content temporarily on page. To be removed when script is generated on file
			String replaceHTMLCharactersStr = StringEscapeUtils.escapeHtml4(paramValue);
			return builder.append(replaceHTMLCharactersStr);
		}
		return builder.append("\"")
			.append(paramValue)
			.append("\"");
	}

	private String generateScript(String testCaseName, String stepScript, String language) {
		StringBuilder builder = new StringBuilder();
		builder
			.append("# language: ").append(language).append("\n")
			.append("Feature: ").append(testCaseName)
			.append(stepScript);
		return builder.toString();
	}
}
