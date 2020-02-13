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

		//TODO: get Testcase's project

		//TODO: get project language
		String language = "en";

		//get test case name
		String testCaseName = testCase.getName();

		// get all keyword test steps in test case
		List<TestStep> testSteps = testCase.getSteps();

		// generate script content as String
		return generatedScript(testCaseName, language);
	}

	private String generatedScript(String testCaseName, String language) {
		StringBuilder builder = new StringBuilder();
		builder.append("# language: ").append(language).append("\n");
		builder.append("Feature: ").append(testCaseName);
		return builder.toString();
	}
}
