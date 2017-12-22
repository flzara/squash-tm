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

import org.squashtest.tm.bugtracker.definition.RemoteCategory;
import org.squashtest.tm.bugtracker.definition.RemoteIssue;
import org.squashtest.tm.bugtracker.definition.RemotePriority;
import org.squashtest.tm.bugtracker.definition.RemoteProject;
import org.squashtest.tm.bugtracker.definition.RemoteStatus;
import org.squashtest.tm.bugtracker.definition.RemoteUser;
import org.squashtest.tm.bugtracker.definition.RemoteVersion;

public class RemoteIssueDecorator implements RemoteIssue {

	protected final RemoteIssue issue;
	private final long issueId;

	public RemoteIssueDecorator(RemoteIssue remoteIssue, long issueId) {
		this.issue = remoteIssue;
		this.issueId = issueId;
	}

	public long getIssueId() {
		return issueId;
	}

	@Override
	public String getId(){
		return this.issue.getId();
	}

	@Override
	public boolean hasBlankId(){
		return this.issue.hasBlankId();
	}


	@Override
	public RemoteCategory getCategory() {
		return this.issue.getCategory();
	}


	@Override
	public String getSummary() {
		return this.issue.getSummary();
	}

	@Override
	public RemoteProject getProject() {
		return this.issue.getProject();
	}


	@Override
	public RemotePriority getPriority() {
		return this.issue.getPriority();
	}

	@Override
	public RemoteVersion getVersion() {
		return this.issue.getVersion();
	}

	@Override
	public RemoteStatus getStatus() {
		return this.issue.getStatus();
	}

	@Override
	public RemoteUser getAssignee() {
		return this.issue.getAssignee();
	}

	@Override
	public void setBugtracker(String btName) {
		this.issue.setBugtracker(btName);
	}

	@Override
	public String getBugtracker() {
		return this.issue.getBugtracker();
	}

	@Override
	public String getDescription() {
		return this.issue.getDescription();
	}

	@Override
	public void setDescription(String description) {
		this.issue.setDescription(description);
	}

	@Override
	public String getComment() {
		return this.issue.getComment();
	}

	@Override
	public void setComment(String comment) {
		this.issue.setComment(comment);
	}

	@Override
	public String getNewKey() {
		return this.issue.getNewKey();
	}
}
