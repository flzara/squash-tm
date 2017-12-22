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
package org.squashtest.tm.domain.bugtracker;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


@Entity
public class IssueList {

	@Id
	@Column(name = "ISSUE_LIST_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "issue_list_issue_list_id_seq")
	@SequenceGenerator(name = "issue_list_issue_list_id_seq", sequenceName = "issue_list_issue_list_id_seq", allocationSize = 1)
	private Long id;

	@OneToMany(mappedBy="issueList", cascade={CascadeType.REMOVE})
	private final List<Issue> issues = new ArrayList<>();

	public Long getId() {
		return id;
	}


	public void addIssue(Issue issue) {
		issues.add(issue);
		issue.setIssueList(this);
	}

	public void removeIssue(Issue issue) {
		removeIssue(issue.getId());
	}

	public void removeIssue(long issueId) {
		Iterator<Issue> iter = issues.iterator();
		while (iter.hasNext()) {
			Issue at = iter.next();
			if (at.getId() == issueId) {
				iter.remove();
				break;
			}
		}
	}


	public Issue findIssue(long issueId) {
		Issue result = null;

		for (Issue at : issues) {
			if (at.getId() == issueId) {
				result = at;
				break;
			}
		}

		return result;
	}

	public boolean hasIssues() {
		return !issues.isEmpty();
	}

	public List<Issue> getAllIssues() {
		return issues;
	}

	public int size() {
		return getAllIssues().size();
	}

	public boolean hasRemoteIssue(String remoteKey){
		for (Issue issue : issues){
			if (issue.getRemoteIssueId().equals(remoteKey)){
				return true;
			}
		}
		return false;
	}

}
