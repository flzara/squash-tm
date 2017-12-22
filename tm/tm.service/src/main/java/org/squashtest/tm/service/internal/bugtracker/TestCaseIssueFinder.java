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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
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
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.internal.repository.BugTrackerDao;
import org.squashtest.tm.service.internal.repository.IssueDao;
import org.squashtest.tm.service.internal.repository.TestCaseDao;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Gregory Fouquet
 * @since 1.14.0  04/04/16
 */
@Component
public class TestCaseIssueFinder implements IssueOwnershipFinder {
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
	private TestCaseDao testCaseDao;

	private List<Pair<Execution, Issue>> findExecutionIssuePairs(TestCase testCase, PagingAndSorting sorter) {
		return issueDao.findAllExecutionIssuePairsByTestCase(testCase, sorter);
	}

	private long countIssues(TestCase testCase) {
		return issueDao.countByTestCase(testCase);
	}

	/**
	 * Mostly the same implementation as IssueOwnershipFinderSupport but with delayed BugTracker fetching : executions
	 * can come from different projects, which means issues can be declared in different bugtrackers.
	 */
	@Override
	public PagedCollectionHolder<List<IssueOwnership<RemoteIssueDecorator>>> findSorted(long entityId, PagingAndSorting sorter) {
		TestCase testCase = testCaseDao.findById(entityId);
		List<Pair<Execution, Issue>> pairs = findExecutionIssuePairs(testCase, sorter);

		return new PagingBackedPagedCollectionHolder<>(sorter, countIssues(testCase), findRemoteIssues(pairs));
	}

	/**
	 * Finds the remote issues from potentially several bugtrackers and rebuilds a list of [Exec, Remote issue] as
	 * IssueOwnership&lt;RemoteIshDecorator&gt; in the same order as the given [Exec, Issue] pairs
	 */
	protected List<IssueOwnership<RemoteIssueDecorator>> findRemoteIssues(List<Pair<Execution, Issue>> pairs) {
		if (pairs.isEmpty()) {
			return Collections.emptyList();
		}

		Map<Execution, BugTracker> bugtrackerByExecution = mapBugtrackerByExecution(collectExecutions(pairs));

		Map<Pair<Execution, Issue>, IssueOwnership<RemoteIssueDecorator>> ownershipByPair = fetchRemoteIssuesAndMapByPair(pairs, bugtrackerByExecution);

		return sortOwnershipsAsPairs(ownershipByPair, pairs);
	}

	private List<IssueOwnership<RemoteIssueDecorator>> sortOwnershipsAsPairs(Map<Pair<Execution, Issue>, IssueOwnership<RemoteIssueDecorator>> ownershipByPair, List<Pair<Execution, Issue>> pairs) {
		List<IssueOwnership<RemoteIssueDecorator>> res = new ArrayList<>(ownershipByPair.size());
		for (Pair<Execution, Issue> pair : pairs) {
			IssueOwnership<RemoteIssueDecorator> own = ownershipByPair.get(pair);
			if (own != null) {
				res.add(own);
			}
		}
		return res;
	}

	private Map<Pair<Execution, Issue>, IssueOwnership<RemoteIssueDecorator>> fetchRemoteIssuesAndMapByPair(List<Pair<Execution, Issue>> pairs, Map<Execution, BugTracker> bugtrackerByExecution) {
		Multimap<BugTracker, Pair<Execution, Issue>> pairsByBugtracker = mapPairsByBugTracker(pairs, bugtrackerByExecution);

		Map<Pair<Execution, Issue>, IssueOwnership<RemoteIssueDecorator>> ownershipByPair = new HashMap<>(pairs.size());

		for (Map.Entry<BugTracker, Collection<Pair<Execution, Issue>>> entry : pairsByBugtracker.asMap().entrySet()) {
			if (entry.getKey() == null || entry.getValue().isEmpty()) { // not fetchable or nothing to fetch
				continue;
			}

			ownershipByPair.putAll(findRemoteIssues(entry.getValue(), entry.getKey()));
		}
		return ownershipByPair;
	}

	/**
	 * Groups [Exec, Ish] pairs by bugtracker.
	 *
	 * @param pairs                 pairs to group
	 * @param bugtrackerByExecution bugtracker dictionary to perform the grouping
	 * @return Map(BT-&gt;List&lt;Pair&gt;). PAirs without any bugtracker are grouped under the null key
	 */
	private Multimap<BugTracker, Pair<Execution, Issue>> mapPairsByBugTracker(List<Pair<Execution, Issue>> pairs, Map<Execution, BugTracker> bugtrackerByExecution) {
		Multimap<BugTracker, Pair<Execution, Issue>> pairsByBugtracker = ArrayListMultimap.create();
		for (Pair<Execution, Issue> pair : pairs) {
			pairsByBugtracker.put(bugtrackerByExecution.get(pair.left), pair);
		}
		return pairsByBugtracker;
	}

	/**
	 * Fetches the executions' bugtrackers, mapped by execution so that they can be found when processing [Exec, Ish] pairs
	 *
	 * @return bugtracker mapped by execution. input executions without bugtracker won't appear in the map.
	 */
	private Map<Execution, BugTracker> mapBugtrackerByExecution(Set<Execution> executions) {
		List<Pair<Execution, BugTracker>> btExecPairs = bugTrackerDao.findAllPairsByExecutions(executions);

		Map<Execution, BugTracker> bugtrackerByExecution = new HashMap<>(btExecPairs.size());
		for (Pair<Execution, BugTracker> pair : btExecPairs) {
			bugtrackerByExecution.put(pair.left, pair.right);
		}
		return bugtrackerByExecution;
	}

	private Set<Execution> collectExecutions(List<Pair<Execution, Issue>> pairs) {
		Set<Execution> executions = new HashSet<>(pairs.size());
		for (Pair<Execution, Issue> pair : pairs) {
			executions.add(pair.left);
		}
		return executions;
	}

	private Map<Pair<Execution, Issue>, IssueOwnership<RemoteIssueDecorator>> findRemoteIssues(Collection<Pair<Execution, Issue>> pairs, BugTracker bugTracker) {
		Map<Pair<Execution, Issue>, IssueOwnership<RemoteIssueDecorator>> ownerships;
		List<String> remoteIssueIds = IssueOwnershipFinderUtils.collectRemoteIssueIds(pairs);

		try {
			Future<List<RemoteIssue>> futureIssues = remoteBugTrackersService.getIssues(remoteIssueIds, bugTracker, contextHolder.getContext(), LocaleContextHolder.getLocaleContext());
			List<RemoteIssue> btIssues = futureIssues.get(timeout, TimeUnit.SECONDS);

			Map<String, RemoteIssue> remoteById = IssueOwnershipFinderUtils.createRemoteIssueByRemoteIdMap(btIssues);

			ownerships = coerceIntoIssueOwnerships(pairs, remoteById);
		} catch (TimeoutException | ExecutionException | InterruptedException ex) {
			throw new BugTrackerRemoteException(ex);
		}

		return ownerships;
	}

	private Map<Pair<Execution, Issue>, IssueOwnership<RemoteIssueDecorator>> coerceIntoIssueOwnerships(Collection<Pair<Execution, Issue>> pairs, Map<String, RemoteIssue> remoteById) {
		Map<Pair<Execution, Issue>, IssueOwnership<RemoteIssueDecorator>> ownerships = new HashMap<>(pairs.size());

		for (Pair<Execution, Issue> pair : pairs) {
			Issue ish = pair.right;
			RemoteIssue remote = remoteById.get(ish.getRemoteIssueId());

			IssueOwnership<RemoteIssueDecorator> ownership = new IssueOwnership<>(new RemoteIssueDecorator(remote, ish.getId()), pair.left);
			ownerships.put(pair, ownership);
		}

		return ownerships;
	}

}
