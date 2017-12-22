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
package org.squashtest.tm.service.internal.bugtracker;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.bugtracker.Issue;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.service.internal.repository.TestSuiteDao;

/**
 * @author Gregory Fouquet
 * @since 1.14.0  30/03/16
 */
@Component
class TestSuiteIssueFinder extends IssueOwnershipFinderSupport<TestSuite> {
	@Inject
	private TestSuiteDao testSuiteDao;

	@Override
	protected TestSuite findEntity(long id) {
		return testSuiteDao.findOne(id);
	}

	@Override
	protected List<Pair<Execution, Issue>> findExecutionIssuePairs(TestSuite testSuite, PagingAndSorting sorter) {
		return issueDao.findAllExecutionIssuePairsByTestSuite(testSuite, sorter);
	}

	@Override
	protected BugTracker findBugTracker(TestSuite testSuite) {
		return bugTrackerDao.findByTestSuite(testSuite);
	}

	@Override
	protected long countIssues(TestSuite testSuite) {
		return issueDao.countByTestSuite(testSuite);
	}
}
