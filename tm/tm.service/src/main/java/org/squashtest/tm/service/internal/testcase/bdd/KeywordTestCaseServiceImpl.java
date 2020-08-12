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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.bdd.BddImplementationTechnology;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.testcase.KeywordTestCase;
import org.squashtest.tm.service.testcase.bdd.KeywordTestCaseService;
import org.squashtest.tm.service.testcase.scripted.KeywordTestCaseToFileStrategy;

import javax.inject.Inject;


@Service
@Transactional
public class KeywordTestCaseServiceImpl implements KeywordTestCaseService {

	@Inject
	private MessageSource messageSource;

	@Override
	public String createFileName(KeywordTestCase keywordTestCase) {
		return getKeywordTestCaseToFileStrategy(keywordTestCase).createFilenameFor(keywordTestCase);
	}

	@Override
	public String createBackupFileName(KeywordTestCase keywordTestCase) {
		return getKeywordTestCaseToFileStrategy(keywordTestCase).backupFilenameFor(keywordTestCase);
	}

	@Override
	public String buildFilenameMatchPattern(KeywordTestCase keywordTestCase) {
		return getKeywordTestCaseToFileStrategy(keywordTestCase).buildFilenameMatchPattern(keywordTestCase);
	}

	@Override
	public String writeScriptFromTestCase(KeywordTestCase keywordTestCase, boolean escapeArrows) {
		Project project = keywordTestCase.getProject();
		BddImplementationTechnology bddImplementationTechnology = project.getBddImplementationTechnology();
		KeywordTestCaseToFileStrategy strategy = KeywordTestCaseToFileStrategy.strategyFor(bddImplementationTechnology);
		return strategy.getWritableFileContent(keywordTestCase, messageSource, escapeArrows);
	}

	private KeywordTestCaseToFileStrategy getKeywordTestCaseToFileStrategy(KeywordTestCase keywordTestCase) {
		Project project = keywordTestCase.getProject();
		BddImplementationTechnology bddImplementationTechnology = project.getBddImplementationTechnology();
		return KeywordTestCaseToFileStrategy.strategyFor(bddImplementationTechnology);
	}

}
