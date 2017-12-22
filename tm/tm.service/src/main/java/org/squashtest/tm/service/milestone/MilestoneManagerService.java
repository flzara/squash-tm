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
package org.squashtest.tm.service.milestone;

import static org.squashtest.tm.service.security.Authorizations.MILESTONE_FEAT_ENABLED;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.dynamicmanager.annotation.DynamicManager;
import org.squashtest.tm.core.dynamicmanager.annotation.QueryParam;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.milestone.MilestoneRange;
import org.squashtest.tm.domain.milestone.MilestoneStatus;
import org.squashtest.tm.domain.users.User;

@Transactional
@DynamicManager(name = "squashtest.tm.service.MilestoneManagerService", entity = Milestone.class)
public interface MilestoneManagerService extends CustomMilestoneManager {
	@PreAuthorize(MILESTONE_FEAT_ENABLED)
	void changeDescription(long milestoneId, String newDescription);

	@PreAuthorize(MILESTONE_FEAT_ENABLED)
	void changeStatus(long milestoneId, MilestoneStatus newStatus);

	@PreAuthorize(MILESTONE_FEAT_ENABLED)
	void changeEndDate(long milestoneId, Date newEndDate);

	@PreAuthorize(MILESTONE_FEAT_ENABLED)
	void changeOwner(long milestoneId, User Owner);

	@PreAuthorize(MILESTONE_FEAT_ENABLED)
	void changeRange(long milestoneId, MilestoneRange newRange);

	/**
	 * Given a collection of milestone names, returns the names of
	 * the milestones which actually exist
	 *
	 * @param names
	 * @return
	 */
	List<String> findExistingNames(@QueryParam("names") Collection<String> names);

	/**
	 * Given a collection of milestone names, returns the names of
	 * the milestones which are in progress
	 *
	 * @param names
	 * @return
	 */
	List<String> findInProgressExistingNames(@QueryParam("names") Collection<String> names);
	
	/**
	 * Given a collection of milestone names, returns the names of
	 * the milestones which are bindable
	 *
	 * @param names
	 * @return
	 */
	List<String> findBindableExistingNames(@QueryParam("names") Collection<String> names, @QueryParam("status") List<MilestoneStatus> status);

	/**
	 * @param names
	 * @param status
	 * @return
	 */
	List<Milestone> findAllByNamesAndStatus(@QueryParam("names") Collection<String> names, @QueryParam("status") MilestoneStatus status);

}
