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

import org.springframework.stereotype.Component;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.bugtracker.Issue;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.service.internal.repository.TestSuiteDao;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

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
		return testSuiteDao.getOne(id);
	}

	@Override
	protected List<Pair<Execution, Issue>> findExecutionIssuePairs(TestSuite testSuite, PagingAndSorting sorter) {
		//return issueDao.findAllExecutionIssuePairsByTestSuite(testSuite, sorter);
		List<Pair<Execution, Issue>> listTmpExecutionIssuePairs = issueDao.findAllExecutionIssuePairsByTestSuite(testSuite, sorter);
		// TM-301:verifier si le bugtracker du projet de l'execution et le m$eme que celui de l issue
		List<Pair<Execution, Issue>> listExecutionIssuePairs = listTmpExecutionIssuePairs.stream()
			.filter(tmpExcIssue->(tmpExcIssue.left.getProject().getBugtrackerBinding().getBugtracker().equals(tmpExcIssue.right.getBugtracker())))
			.collect(Collectors.toList());
		return listExecutionIssuePairs;
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
