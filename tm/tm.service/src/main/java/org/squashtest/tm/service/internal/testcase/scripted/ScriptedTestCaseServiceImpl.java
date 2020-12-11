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
package org.squashtest.tm.service.internal.testcase.scripted;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.audit.AuditableMixin;
import org.squashtest.tm.domain.testcase.ScriptedTestCase;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.security.UserContextHolder;
import org.squashtest.tm.service.annotation.CheckLockedMilestone;
import org.squashtest.tm.service.annotation.Id;
import org.squashtest.tm.service.internal.repository.ScriptedTestCaseDao;
import org.squashtest.tm.service.internal.testcase.scripted.gherkin.GherkinStepGenerator;
import org.squashtest.tm.service.internal.testcase.scripted.gherkin.GherkinTestCaseParser;
import org.squashtest.tm.service.testcase.scripted.ScriptedTestCaseParser;
import org.squashtest.tm.service.testcase.scripted.ScriptedTestCaseService;

import javax.inject.Inject;
import java.util.Date;

@Service
@Transactional
public class ScriptedTestCaseServiceImpl implements ScriptedTestCaseService {

	@Inject
	private ScriptedTestCaseDao scriptedTestCaseDao;

	@Override
	@CheckLockedMilestone(entityType = TestCase.class)
	public void updateTcScript(@Id Long testCaseId, String script) {
		ScriptedTestCase scriptedTestCase = scriptedTestCaseDao.getOne(testCaseId);
		scriptedTestCase.setScript(script);
		//Audit on test case... No way to write 3 triggers for only one method call
		AuditableMixin auditable = (AuditableMixin)scriptedTestCase;
		auditable.setLastModifiedOn(new Date());
		auditable.setLastModifiedBy(UserContextHolder.getUsername());
	}

	@Override
	public void validateScript(String script) {
		ScriptedTestCase scriptedTestCase = new ScriptedTestCase();
		scriptedTestCase.setScript(script);
		ScriptedTestCaseParser parser = new GherkinTestCaseParser(new GherkinStepGenerator());
		parser.validateScript(scriptedTestCase);
	}
}
