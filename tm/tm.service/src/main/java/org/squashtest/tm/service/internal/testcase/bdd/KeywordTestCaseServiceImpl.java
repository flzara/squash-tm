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

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.testcase.KeywordTestStep;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestStep;
import org.squashtest.tm.service.internal.repository.TestCaseDao;
import org.squashtest.tm.service.testcase.bdd.KeywordTestCaseService;

import javax.inject.Inject;
import java.util.List;


@Service
@Transactional
public class KeywordTestCaseServiceImpl implements KeywordTestCaseService {

	@Inject
	private TestCaseDao testCaseDao;

	@Override
	public String writeScriptFromTestCase(Long keywordTestCaseId) {
		// get Testcase by Id
		TestCase testCase = testCaseDao.findById(keywordTestCaseId);

		//TODO: get Testcase's project for further functions: get project techno and language
		String language = "en";

		String testCaseName = testCase.getName();

		List<TestStep> testSteps = testCase.getSteps();

		String stepScript = generateStepScript(testSteps, testCaseName);
		return generateScript(testCaseName, stepScript, language);
	}

	private String generateStepScript(List<TestStep> testSteps, String testCaseName) {
		if (testSteps.isEmpty()) {
			return "";
		}
		StringBuilder builder = new StringBuilder();
		builder.append("\n\n");
		builder.append("\tScenario: ").append(testCaseName).append("\n");
		for(TestStep step : testSteps) {
			KeywordTestStep keywordStep = (KeywordTestStep) step;
			builder
				.append("\t\t")
				.append(keywordStep.getKeyword())
				.append(" ")
				.append(keywordStep.getActionWord().getWord())
				.append("\n");
		}
		String testStepScript = builder.toString();
		return testStepScript.substring(0, testStepScript.length()-1);
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
