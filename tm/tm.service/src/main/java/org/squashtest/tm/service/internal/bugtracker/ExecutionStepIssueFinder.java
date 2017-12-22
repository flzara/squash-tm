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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.squashtest.csp.core.bugtracker.core.BugTrackerRemoteException;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.csp.core.bugtracker.service.BugTrackerContextHolder;
import org.squashtest.csp.core.bugtracker.service.BugTrackersService;
import org.squashtest.tm.bugtracker.definition.RemoteIssue;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.PagingBackedPagedCollectionHolder;
import org.squashtest.tm.domain.bugtracker.Issue;
import org.squashtest.tm.domain.bugtracker.IssueOwnership;
import org.squashtest.tm.domain.bugtracker.RemoteIssueDecorator;
import org.squashtest.tm.domain.execution.ExecutionStep;
import org.squashtest.tm.service.internal.repository.BugTrackerDao;
import org.squashtest.tm.service.internal.repository.ExecutionStepDao;
import org.squashtest.tm.service.internal.repository.IssueDao;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Gregory Fouquet
 * @since 1.14.0  06/04/16
 */
@Component
public class ExecutionStepIssueFinder implements IssueOwnershipFinder {
	@Value("${squashtm.bugtracker.timeout:15}")
	private long timeout;
	@Inject
	private BugTrackersService remoteBugTrackersService;
	@Inject
	private BugTrackerContextHolder contextHolder;
	@Inject
	private IssueDao issueDao;
	@Inject
	private BugTrackerDao bugTrackerDao;
	@Inject
	private ExecutionStepDao executionStepDao;

	@Override
	public PagedCollectionHolder<List<IssueOwnership<RemoteIssueDecorator>>> findSorted(long entityId, PagingAndSorting sorter) {
		ExecutionStep executionStep = executionStepDao.findById(entityId);
		List<Issue> issues = issueDao.findAllByExecutionStep(executionStep, sorter);
		BugTracker bugTracker = bugTrackerDao.findByExecutionStep(executionStep);

		List<IssueOwnership<RemoteIssueDecorator>> ownerships;
		ownerships = findRemoteIssues(executionStep, issues, bugTracker);

		long issuesCount = countIssues(executionStep);

		return new PagingBackedPagedCollectionHolder<>(sorter, issuesCount, ownerships);
	}

	private List<IssueOwnership<RemoteIssueDecorator>> findRemoteIssues(ExecutionStep executionStep, Collection<Issue> issues, BugTracker bugTracker) {
		if (issues.isEmpty() || bugTracker == null) {
			return Collections.emptyList();
		}

		List<String> remoteIssueIds = new ArrayList<>(issues.size());
		for (Issue issue : issues) {
			remoteIssueIds.add(issue.getRemoteIssueId());
		}

		try {
			Future<List<RemoteIssue>> futureIssues = remoteBugTrackersService.getIssues(remoteIssueIds, bugTracker, contextHolder.getContext(), LocaleContextHolder.getLocaleContext());
			List<RemoteIssue> btIssues = futureIssues.get(timeout, TimeUnit.SECONDS);

			Map<String, RemoteIssue> remoteById = IssueOwnershipFinderUtils.createRemoteIssueByRemoteIdMap(btIssues);

			return IssueOwnershipFinderUtils.coerceIntoIssueOwnerships(executionStep, issues, remoteById);

		} catch (TimeoutException | ExecutionException | InterruptedException ex) {
			throw new BugTrackerRemoteException(ex);
		}
	}

	private long countIssues(ExecutionStep executionStep) {
		return executionStep.getIssueList().size();
	}

}
