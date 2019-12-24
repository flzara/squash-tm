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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.squashtest.csp.core.bugtracker.core.BugTrackerRemoteException;
import org.squashtest.tm.api.widget.InternationalizedMenuItem;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.PagingBackedPagedCollectionHolder;
import org.squashtest.tm.domain.bugtracker.Issue;
import org.squashtest.tm.domain.bugtracker.IssueOwnership;
import org.squashtest.tm.domain.bugtracker.RemoteIssueDecorator;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.service.bugtracker.RequirementVersionIssueOwnership;
import org.squashtest.tm.service.internal.repository.IssueDao;
import org.squashtest.tm.service.requirement.RequirementVersionManagerService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
class RequirementVersionIssueFinder extends TestCaseIssueFinder {

	private static final Logger LOGGER = LoggerFactory.getLogger(InternationalizedMenuItem.class);

	@Inject
	private IssueDao issueDao;
	@Inject
	private RequirementVersionManagerService requirementVersionManagerService;

	static final String INFO = "info";

	@SuppressWarnings("squid:S1612")
	public PagedCollectionHolder<List<RequirementVersionIssueOwnership<RemoteIssueDecorator>>> findSorted(long entityId, String panelSource, PagingAndSorting sorter) {

		RequirementVersion currentReqVer = requirementVersionManagerService.findById(entityId);
		IssueOwnership<RemoteIssueDecorator> issueOwnership;
		List<RequirementVersion> versions = new ArrayList<>();
		List<RequirementVersionIssueOwnership<RemoteIssueDecorator>> requirementVersionIssueOwnerships = new ArrayList<>();

		if(!panelSource.equalsIgnoreCase(INFO)){
			Requirement currentReq = currentReqVer.getRequirement();
			List<Requirement> requirementList = getFatherChildrenRequirements(currentReq);
			versions = requirementList.stream().map(Requirement::getCurrentVersion).collect(Collectors.toList());
		}else {
			versions.add(currentReqVer);
		}

		//List<RequirementIssueSupport> executionIssuePairsByRequirementVersions = issueDao.findAllExecutionIssuePairsByRequirementVersions(versions, sorter);

		 List<RequirementIssueSupport> TmpExecutionIssuePairsByRequirementVersions = issueDao.findAllExecutionIssuePairsByRequirementVersions(versions, sorter);

		// TM-301:verifier si le bugtracker du projet de l'execution et le m$eme que celui de l issue
		List<RequirementIssueSupport> executionIssuePairsByRequirementVersions = TmpExecutionIssuePairsByRequirementVersions.stream()
									.filter(tmpExcIssueReq->(tmpExcIssueReq.getExecution().getProject().getBugtrackerBinding().getBugtracker().equals(tmpExcIssueReq.getIssue().getBugtracker())))
									.collect(Collectors.toList());


		for (RequirementIssueSupport support : executionIssuePairsByRequirementVersions) {
			Pair<Execution, Issue> pair = new Pair<>(support.getExecution(), support.getIssue());
			try{
				issueOwnership = findRemoteIssues(Arrays.asList(pair)).get(0);
			}catch(BugTrackerRemoteException e){
				LOGGER.debug("Cannot authenticate because no valid credentials were found for authentication on the remote server.");
				issueOwnership = null;
			}
			if(issueOwnership != null){
				requirementVersionIssueOwnerships.add(new RequirementVersionIssueOwnership<>(issueOwnership.getIssue(), issueOwnership.getOwner(), support.getRequirementVersion()));
			}

		}

		long nbIssues = versions.stream().mapToLong(rv -> countIssues(rv)).sum();
		return new PagingBackedPagedCollectionHolder<>(sorter, nbIssues, requirementVersionIssueOwnerships);
	}

	private List<Requirement> getFatherChildrenRequirements(Requirement currentRequirement) {
		List<Requirement> result = new ArrayList<>();
		result.add(currentRequirement);
		if (currentRequirement.hasContent()) {
			for (Requirement childrenRequirement : currentRequirement.getContent()) {
				result.addAll(getFatherChildrenRequirements(childrenRequirement));
			}
		}
		return result;
	}

	private long countIssues(RequirementVersion requirementVersion) {
		return issueDao.countByRequirementVersion(requirementVersion);
	}
}
