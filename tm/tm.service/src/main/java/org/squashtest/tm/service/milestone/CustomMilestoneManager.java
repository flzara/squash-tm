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

import static org.squashtest.tm.service.security.Authorizations.HAS_ROLE_ADMIN;
import static org.squashtest.tm.service.security.Authorizations.MILESTONE_FEAT_ENABLED;

import java.util.Collection;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.milestone.MilestoneHolder;
import org.squashtest.tm.domain.project.GenericProject;

public interface CustomMilestoneManager extends MilestoneFinderService {

	void addMilestone(Milestone milestone);

	void changeLabel(long milestoneId, String newLabel);

	List<Milestone> findAll();

	void removeMilestones(Collection<Long> ids);




	/**
	 *
	 * @param milestoneId
	 *            the id of the milestone
	 * @return true if the user has rights to edit the milestone
	 */
	boolean canEditMilestone(long milestoneId);

	/**
	 * Throw exception if the user try do edit a milestone he can't
	 *
	 * @param milestoneId
	 *            the id of the milestone
	 */
	void verifyCanEditMilestone(long milestoneId);

	/**
	 * Throw exception if the user try do edit milestone range and can't
	 *
	 *            the id of the milestone
	 */
	void verifyCanEditMilestoneRange();

	/**
	 *
	 * @return list of Id of editable milestone for current user
	 */
	List<Long> findAllIdsOfEditableMilestone();

	/**
	 *
	 * @return returns the list of all milestone a user can see as a project manager
	 */
	List<Milestone> findAllVisibleToCurrentManager();

	boolean isBoundToATemplate(Long milestoneId);

	void cloneMilestone(long motherId, Milestone milestone, boolean bindToRequirements, boolean bindToTestCases);

	void synchronize(long sourceId, long targetId, boolean extendPerimeter, boolean isUnion);

	/**
	 * When a node has been copied to another project some milestones might no longer be available. This method will
	 * trim unbind the member from them.
	 *
	 * @param member
	 */
	void migrateMilestones(MilestoneHolder member);

	/**
	 * performs necessary operation when this feature is enabled. Should *not* persist the feature's state.
	 */
	void enableFeature();

	/**
	 * performs necessary operation when this feature is enabled. Should *not* persist the feature's state.
	 */
	void disableFeature();

	boolean isBoundToAtleastOneObject(long milestoneId);

	void unbindAllObjects(long milestoneId);

	boolean hasMilestone(List<Long> userdIds);

	boolean isMilestoneBoundToOneObjectOfProject(Milestone milestone, GenericProject project);

}
