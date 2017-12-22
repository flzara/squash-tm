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

import org.squashtest.tm.bugtracker.definition.RemoteIssue;
import org.squashtest.tm.domain.bugtracker.Issue;
import org.squashtest.tm.domain.bugtracker.IssueDetector;
import org.squashtest.tm.domain.bugtracker.IssueOwnership;
import org.squashtest.tm.domain.bugtracker.RemoteIssueDecorator;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

final class IssueOwnershipFinderUtils {
	private IssueOwnershipFinderUtils() {
	}

	static List<String> collectRemoteIssueIds(Collection<? extends Pair<?, Issue>> pairs) {
		return pairs.stream().map(p -> p.right.getRemoteIssueId()).collect(Collectors.toList());
	}

	static Map<String, RemoteIssue> createRemoteIssueByRemoteIdMap(List<RemoteIssue> btIssues) {
		return btIssues.stream().collect(Collectors.toMap(RemoteIssue::getId, Function.identity()));
	}

	static List<IssueOwnership<RemoteIssueDecorator>> coerceIntoIssueOwnerships(List<? extends Pair<? extends IssueDetector, Issue>> pairs, Map<String, RemoteIssue> remoteIssueByRemoteId) {

		return pairs.stream().map(p -> {
			Issue ish = p.right;
			RemoteIssue remote = remoteIssueByRemoteId.get(ish.getRemoteIssueId());
			//update the remoteIssueId in the database
			if (remote.getNewKey() != null) {
				ish.setRemoteIssueId(remote.getNewKey());
			}
			return new IssueOwnership<>(new RemoteIssueDecorator(remote, ish.getId()), p.left);
		}).collect(Collectors.toList());



	}

	static List<IssueOwnership<RemoteIssueDecorator>> coerceIntoIssueOwnerships(IssueDetector holder, Collection<Issue> issues, Map<String, RemoteIssue> remoteIssueByRemoteId) {

		return issues.stream().map(issue -> {
			RemoteIssue remote = remoteIssueByRemoteId.get(issue.getRemoteIssueId());
			return new IssueOwnership<>(new RemoteIssueDecorator(remote, issue.getId()), holder);
		}).collect(Collectors.toList());

	}
}
