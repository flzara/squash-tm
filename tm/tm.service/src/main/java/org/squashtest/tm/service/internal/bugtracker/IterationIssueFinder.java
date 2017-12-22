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
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.service.internal.repository.IterationDao;

/**
 * @author Gregory Fouquet
 * @since 1.14.0  29/03/16
 */
@Component
class IterationIssueFinder extends IssueOwnershipFinderSupport<Iteration> {
	@Inject private IterationDao iterationDao;
	@Override
	protected Iteration findEntity(long id) {
		return iterationDao.findById(id);
	}

	@Override
	protected List<Pair<Execution, Issue>> findExecutionIssuePairs(Iteration iteration, PagingAndSorting sorter) {
		return issueDao.findAllExecutionIssuePairsByIteration(iteration, sorter);
	}

	@Override
	protected BugTracker findBugTracker(Iteration iteration) {
		return bugTrackerDao.findByIteration(iteration);
	}

	@Override
	protected long countIssues(Iteration iteration) {
		return issueDao.countByIteration(iteration);
	}
}
