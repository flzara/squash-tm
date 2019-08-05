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
import org.squashtest.tm.domain.campaign.CampaignFolder;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.service.internal.repository.CampaignFolderDao;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Gregory Fouquet
 * @since 1.14.0  30/03/16
 */
@Component
class CampaignFolderIssueFinder extends IssueOwnershipFinderSupport<CampaignFolder> {
	@Inject
	private CampaignFolderDao campaignFolderDao;

	@Override
	protected CampaignFolder findEntity(long id) {
		return campaignFolderDao.findById(id);
	}

	@Override
	protected List<Pair<Execution, Issue>> findExecutionIssuePairs(CampaignFolder folder, PagingAndSorting sorter) {
		//return issueDao.findAllExecutionIssuePairsByCampaignFolder(folder, sorter);
		List<Pair<Execution, Issue>> listTmpExecutionIssuePairs = issueDao.findAllExecutionIssuePairsByCampaignFolder(folder, sorter);

		// TM-301:verifier si le bugtracker du projet de l'execution et le m$eme que celui de l issue
		List<Pair<Execution, Issue>> listExecutionIssuePairs = listTmpExecutionIssuePairs.stream()
			.filter(tmpExcIssue->(tmpExcIssue.left.getProject().getBugtrackerBinding().getBugtracker().equals(tmpExcIssue.right.getBugtracker())))
			.collect(Collectors.toList());
		return listExecutionIssuePairs;
	}

	@Override
	protected BugTracker findBugTracker(CampaignFolder folder) {
		return bugTrackerDao.findByCampaignLibraryNode(folder);
	}

	@Override
	protected long countIssues(CampaignFolder folder) {
		return issueDao.countByCampaignFolder(folder);
	}
}
