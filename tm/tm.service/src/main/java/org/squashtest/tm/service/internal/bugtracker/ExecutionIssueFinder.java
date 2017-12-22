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

import org.apache.commons.lang3.StringUtils;
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
import org.squashtest.tm.core.foundation.collection.SortOrder;
import org.squashtest.tm.domain.bugtracker.Issue;
import org.squashtest.tm.domain.bugtracker.IssueDetector;
import org.squashtest.tm.domain.bugtracker.IssueOwnership;
import org.squashtest.tm.domain.bugtracker.RemoteIssueDecorator;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStep;
import org.squashtest.tm.service.internal.repository.BugTrackerDao;
import org.squashtest.tm.service.internal.repository.ExecutionDao;
import org.squashtest.tm.service.internal.repository.IssueDao;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
/**
 * @author Gregory Fouquet
 * @since 1.14.0  29/03/16
 */
@Component
class ExecutionIssueFinder implements IssueOwnershipFinder {
	private static final Comparator<Pair<? extends IssueDetector, Issue>> ASC_PAIR_COMPARATOR = new Comparator<Pair<? extends IssueDetector, Issue>>() {
		@Override
		public int compare(Pair<? extends IssueDetector, Issue> p1, Pair<? extends IssueDetector, Issue> p2) {
			Issue i1 = p1.right;
			String r1 = i1 == null ? "" : StringUtils.defaultString(i1.getRemoteIssueId());

			Issue i2 = p2.right;
			String r2 = i1 == null ? "" : StringUtils.defaultString(i2.getRemoteIssueId());

			return r1.compareTo(r2);
		}
	};

	private static final Comparator<Pair<? extends IssueDetector, Issue>> DESC_PAIR_COMPARATOR = new Comparator<Pair<? extends IssueDetector, Issue>>() {
		@Override
		public int compare(Pair<? extends IssueDetector, Issue> o1, Pair<? extends IssueDetector, Issue> o2) {
			return - ASC_PAIR_COMPARATOR.compare(o1, o2);
		}
	};

	@Value("${squashtm.bugtracker.timeout:15}")
	private long timeout;
	@Inject
	private BugTrackersService remoteBugTrackersService;
	@Inject
	private BugTrackerContextHolder contextHolder;
	@Inject
	protected IssueDao issueDao;
	@Inject
	protected BugTrackerDao bugTrackerDao;
	@Inject
	private ExecutionDao executionDao;

	private List<? extends Pair<? extends IssueDetector, Issue>> findExecutionIssuePairs(Execution execution, PagingAndSorting sorter) {
		return issueDao.findAllDeclaredExecutionIssuePairsByExecution(execution, sorter);
	}

	private BugTracker findBugTracker(Execution execution) {
		return bugTrackerDao.findByExecution(execution);
	}

	@Override
	public final PagedCollectionHolder<List<IssueOwnership<RemoteIssueDecorator>>> findSorted(
		long entityId, PagingAndSorting sorter) {
		Execution execution = executionDao.findOne(entityId);

		List<Pair<? extends IssueDetector, Issue>> pairs = findAllPagedPairs(execution, sorter);

		BugTracker bugTracker = findBugTracker(execution);

		List<IssueOwnership<RemoteIssueDecorator>> ownerships = findRemoteIssues(pairs, bugTracker);

		long issuesCount = issueDao.countByExecutionAndSteps(execution);

		return new PagingBackedPagedCollectionHolder<>(sorter, issuesCount, ownerships);
	}

	private List<Pair<? extends IssueDetector, Issue>> findAllPagedPairs(Execution execution, PagingAndSorting sorter) {
		List<? extends Pair<? extends IssueDetector, Issue>> execPairs = findExecutionIssuePairs(execution, sorter);
		List<? extends Pair<? extends IssueDetector, Issue>> stepPairs = findExecutionStepIssuePairs(execution, sorter);

		List<Pair<? extends IssueDetector, Issue>> pairs = new ArrayList<>(execPairs);

		pairs.addAll(stepPairs);
		Collections.sort(pairs, comparator(sorter));

		return pairs;
	}

	private Comparator<Pair<? extends IssueDetector, Issue>> comparator(PagingAndSorting sorter) {
		return sorter.getSortOrder() == SortOrder.ASCENDING ?  ASC_PAIR_COMPARATOR : DESC_PAIR_COMPARATOR;
	}

	private List<Pair<ExecutionStep, Issue>> findExecutionStepIssuePairs(Execution execution, PagingAndSorting sorter) {
		return issueDao.findAllExecutionStepIssuePairsByExecution(execution, sorter);
	}

	/**
	 * Fetches remote issues from remote bugtracker
	 *
	 * @param pairs      execution-issue pairs which remote issues should be fetched
	 * @param bugTracker non-null bugtracker where to fetch issues
	 */
	private List<IssueOwnership<RemoteIssueDecorator>> findRemoteIssues(List<? extends Pair<? extends IssueDetector, Issue>> pairs, BugTracker bugTracker) {
		if (bugTracker == null || pairs.isEmpty()) {
			return Collections.emptyList();
		}

		List<IssueOwnership<RemoteIssueDecorator>> ownerships;
		List<String> remoteIssueIds = IssueOwnershipFinderUtils.collectRemoteIssueIds(pairs);

		try {
			Future<List<RemoteIssue>> futureIssues = remoteBugTrackersService.getIssues(remoteIssueIds, bugTracker, contextHolder.getContext(), LocaleContextHolder.getLocaleContext());
			List<RemoteIssue> btIssues = futureIssues.get(timeout, TimeUnit.SECONDS);

			Map<String, RemoteIssue> remoteById = IssueOwnershipFinderUtils.createRemoteIssueByRemoteIdMap(btIssues);

			ownerships = IssueOwnershipFinderUtils.coerceIntoIssueOwnerships(pairs, remoteById);

		} catch (TimeoutException | ExecutionException | InterruptedException ex) {
			throw new BugTrackerRemoteException(ex);
		}

		return ownerships;
	}

}
