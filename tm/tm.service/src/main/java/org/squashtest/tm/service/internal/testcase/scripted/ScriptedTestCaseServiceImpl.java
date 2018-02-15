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
import org.squashtest.tm.domain.testcase.ScriptedTestCaseExtender;
import org.squashtest.tm.service.internal.repository.ScriptedTestCaseExtenderDao;
import org.squashtest.tm.service.testcase.scripted.ScriptedTestCaseService;

import javax.inject.Inject;

@Service
@Transactional
public class ScriptedTestCaseServiceImpl implements ScriptedTestCaseService {

	@Inject
	private ScriptedTestCaseExtenderDao scriptedTestCaseExtenderDao;

	@Override
	public void updateTcScript(Long testCaseId, String script) {
		ScriptedTestCaseExtender scriptExtender = scriptedTestCaseExtenderDao.findByTestCase_Id(testCaseId);
		scriptExtender.setScript(script);
	}
}
