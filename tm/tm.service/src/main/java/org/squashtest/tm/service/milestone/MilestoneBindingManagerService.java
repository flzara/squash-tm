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

import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.project.GenericProject;


@Transactional
public interface MilestoneBindingManagerService {

	/**
	 * Bind multiple milestones to a project
	 * @param milestoneIds ids of the milestone to bind
	 * @param projectId id of the project milestone must be bound to
	 */
	void bindMilestonesToProject(List<Long> milestoneIds, Long projectId);

	/**
	 * Bind a milestone to multiples projects
	 * @param projectIds ids of project the milestone must be bound to
	 * @param milestoneId the milestone to bind
	 */
	void bindProjectsToMilestone(List<Long> projectIds, Long milestoneId);

	/**
	 * Get the list of all milestone bound to the project
	 * @param projectId the id of projec
	 * @return list of milestone bound to the project
	 */
	List<Milestone> getAllBindedMilestoneForProject(Long projectId);

	/**
	 * The list of milestone the current user can bind to the project
	 * @param projectId the id of project
	 * @return list of bindable milestone for the project (depend on user rights)
	 */
	List<Milestone> getAllBindableMilestoneForProject(Long projectId);

	/**
	 *  The list of milestone the current user can bind to the project
	 * @param projectId the id of project
	 * @param type the filter to be applied. If set to "global", will find only global milestone. If set to
	 *  "personal" will find non global milestone owned by current user. If any other value is supplied will
	 *  return all non global non owned milestone (that the user can see).
	 * @return list of bindable milestone for the project filtered by type (depend on user rights)
	 */
	List<Milestone> getAllBindableMilestoneForProject(Long projectId, String type);

	/**
	 *
	 * @param milestoneId the id of milestone
	 * @return list of all bindable project for milestone
	 */
	List<GenericProject> getAllBindableProjectForMilestone(Long milestoneId);

	/**
	 * If the milestone range is GLOBAL, the bound project are returned. If the milestone range is RESTRICTED
	 * then milestone projects in milestone perimeter are returned.
	 * @param milestoneId
	 * @return all project for the milestone
	 */
	List<GenericProject> getAllProjectForMilestone(Long milestoneId);


	/**
	 * Unbind multiple milestone from a project. REMOVE this project from all those milestone perimeter.
	 * @param milestoneIds ids of the milestone
	 * @param projectId id of the project
	 */
	void unbindMilestonesFromProject(List<Long> milestoneIds, Long projectId);

	/**
	 * Unbinds a project from all its Milestones if any. While we're at it, also remove the project from the milestones' perimeters
	 * @param project
	 */
	void unbindAllMilestonesFromProject(GenericProject project);

	/**
	 * Unbind multiple projects from a milestone. Also REMOVE all those projects from the milestone perimeter.
	 * @param projectIds ids of projects
	 * @param milestoneId id of the milestone
	 */
	void unbindProjectsFromMilestone(List<Long> projectIds, Long milestoneId);

	/**
	 * Unbind multiple projects from a milestone. But KEEP all those projects in the milestone perimeter.
	 * @param projectIds ids of projects
	 * @param milestoneId id of the milestone
	 */
	void unbindProjectsFromMilestoneKeepInPerimeter(List<Long> projectIds, Long milestoneId);

	void unbindTemplateFrom(Long milestoneId);

	void bindMilestonesToProjectAndBindObject(Long projectId, List<Long> milestoneIds);


}
