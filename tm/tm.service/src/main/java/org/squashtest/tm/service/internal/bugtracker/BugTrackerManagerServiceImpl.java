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

import org.apache.commons.collections.ListUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.tm.domain.bugtracker.BugTrackerBinding;
import org.squashtest.tm.domain.bugtracker.Issue;
import org.squashtest.tm.exception.NameAlreadyInUseException;
import org.squashtest.tm.exception.NameAlreadyInUseException.EntityType;
import org.squashtest.tm.service.bugtracker.BugTrackerManagerService;
import org.squashtest.tm.service.bugtracker.BugTrackerSystemManager;
import org.squashtest.tm.service.bugtracker.BugTrackersLocalService;
import org.squashtest.tm.service.internal.repository.BugTrackerBindingDao;
import org.squashtest.tm.service.internal.repository.BugTrackerDao;
import org.squashtest.tm.service.internal.repository.IssueDao;
import org.squashtest.tm.service.internal.repository.RequirementSyncExtenderDao;
import org.squashtest.tm.service.project.GenericProjectManagerService;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.squashtest.tm.service.security.Authorizations.HAS_ROLE_ADMIN;
import static org.squashtest.tm.service.security.Authorizations.HAS_ROLE_ADMIN_OR_PROJECT_MANAGER;

@Service("squashtest.tm.service.BugTrackerManagerService")
public class BugTrackerManagerServiceImpl implements BugTrackerManagerService, BugTrackerSystemManager {

	@Inject
	private BugTrackerDao bugTrackerDao;

	@Inject
	private BugTrackerBindingDao bugTrackerBindingDao;

	@Inject
	private GenericProjectManagerService genericProjectManagerService;

	@Inject
	private IssueDao issueDao;

	@Inject
	private BugTrackersLocalService bugtrackersLocalService;

	@Inject
	private RequirementSyncExtenderDao syncreqDao;


	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public void addBugTracker(BugTracker bugTracker) {
		String name = bugTracker.getName();
		BugTracker existing = bugTrackerDao.findByName(name);
		if (existing == null) {
			bugTrackerDao.save(bugTracker);
		} else {
			throw new NameAlreadyInUseException(NameAlreadyInUseException.EntityType.BUG_TRACKER, name);
		}
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN_OR_PROJECT_MANAGER)
	public List<BugTracker> findAll() {
		return bugTrackerDao.findAll();
	}

	@Override
	public List<BugTracker> findByKind(String kind) {
		return bugTrackerDao.findByKind(kind);
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public Page<BugTracker> findSortedBugtrackers(Pageable pageable) {
		return bugTrackerDao.findAll(pageable);
	}

	@Override
	public Set<String> findBugTrackerKinds() {
		return bugtrackersLocalService.getProviderKinds();
	}

	@Override
	public String findBugtrackerName(Long bugtrackerId) {
		return bugTrackerDao.findOne(bugtrackerId).getName();
	}

	@Override
	public BugTracker findById(long bugTrackerId) {
		return bugTrackerDao.findOne(bugTrackerId);
	}

	@Override
	public List<BugTracker> findDistinctBugTrackersForProjects(List<Long> projectIds) {
		if (projectIds.isEmpty()) {
			return ListUtils.EMPTY_LIST;
		}
		return bugTrackerDao.findDistinctBugTrackersForProjects(projectIds);
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public void deleteBugTrackers(final Collection<Long> bugtrackerIds) {

		for (final Long id : bugtrackerIds) {
			deleteBugtrackerToProjectBinding(id);
			deleteIssueLinkedToBugtracker(id);
			deleteLinkedSyncedRequirements(id);
			deleteBugTracker(id);
		}
	}

	private void deleteBugtrackerToProjectBinding(final Long bugtrackerId) {
		final List<BugTrackerBinding> bugtrackerBindings = bugTrackerBindingDao.findByBugtrackerId(bugtrackerId);
		for (final BugTrackerBinding bugtrackerBind : bugtrackerBindings) {
			genericProjectManagerService.removeBugTracker(bugtrackerBind.getProject().getId());
		}
	}

	private void deleteIssueLinkedToBugtracker(final long bugtrackerId) {
		final List<Issue> issues = issueDao.getAllIssueFromBugTrackerId(bugtrackerId);

		for (final Issue issue : issues) {
			issueDao.delete(issue);
		}
	}

	private void deleteLinkedSyncedRequirements(final Long bugtrackerId) {
		syncreqDao.deleteAllByServer(bugtrackerId);
	}

	private void deleteBugTracker(final long bugtrackerId) {
		bugTrackerDao.delete(bugtrackerId);
	}

	/**
	 * This is a system operation so there is no security constraint.
	 *
	 * @see org.squashtest.tm.service.bugtracker.BugTrackerSystemManager#createBugTracker(org.squashtest.csp.core.bugtracker.domain.BugTracker)
	 */
	@Override
	@Transactional
	public BugTracker createBugTracker(@NotNull BugTracker bugTracker) throws NameAlreadyInUseException {
		bugTracker.normalize();

		if (bugTrackerDao.findByName(bugTracker.getName()) != null) {
			throw new NameAlreadyInUseException(EntityType.BUG_TRACKER, bugTracker.getName());
		}
		bugTrackerDao.save(bugTracker);
		return bugTracker;
	}
}
