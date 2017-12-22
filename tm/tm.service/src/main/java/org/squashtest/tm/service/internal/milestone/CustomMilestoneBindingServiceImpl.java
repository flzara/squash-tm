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
package org.squashtest.tm.service.internal.milestone;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.squashtest.tm.domain.Identified;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.milestone.MilestoneRange;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.service.advancedsearch.IndexationService;
import org.squashtest.tm.service.internal.repository.GenericProjectDao;
import org.squashtest.tm.service.internal.repository.MilestoneDao;
import org.squashtest.tm.service.internal.repository.ProjectTemplateDao;
import org.squashtest.tm.service.milestone.MilestoneBindingManagerService;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.security.UserContextService;
import org.squashtest.tm.service.testcase.TestCaseLibraryNavigationService;

@Service("squashtest.tm.service.MilestoneBindingManagerService")
public class CustomMilestoneBindingServiceImpl implements MilestoneBindingManagerService {

	private static final Transformer ID_COLLECTOR = new Transformer() {

		@Override
		public Object transform(Object input) {
			return ((Identified) input).getId();
		}
	};

	@Inject
	private MilestoneDao milestoneDao;

	@Inject
	private GenericProjectDao projectDao;

	@Inject
	private ProjectTemplateDao projectTemplateDao;

	@Inject
	private UserContextService userContextService;

	@Inject
	private PermissionEvaluationService permissionEvaluationService;

	@Inject
	private TestCaseLibraryNavigationService tcService;

	@Inject
	private IndexationService indexService;

	@Override
	public List<Milestone> getAllBindableMilestoneForProject(Long projectId) {

		List<Milestone> milestoneBoundToProject = getAllBindedMilestoneForProject(projectId);
		List<Milestone> allMilestones = milestoneDao.findAll();
		allMilestones.removeAll(milestoneBoundToProject);
		GenericProject project = projectDao.findById(projectId);
		return getMilestoneYouCanSee(allMilestones, project);
	}

	private List<Milestone> getMilestoneYouCanSee(List<Milestone> allMilestones, GenericProject project) {

		List<Milestone> filtered = new ArrayList<>();
		if (permissionEvaluationService.hasRole("ROLE_ADMIN")) {
			// admin can see all
			filtered = allMilestones;
		} else {

			for (Milestone milestone : allMilestones) {
				// project manager can see global, owned and milestone within the project perimeter
				if (!isRestricted(milestone) || isCreatedBySelf(milestone) || milestone.isInPerimeter(project)) {
					filtered.add(milestone);
				}
			}
		}
		return filtered;
	}

	@Override
	public void bindMilestonesToProject(List<Long> milestoneIds, Long projectId) {
		GenericProject project = projectDao.findById(projectId);
		List<Milestone> milestones = milestoneDao.findAll(milestoneIds);
		project.bindMilestones(milestones);
		for (Milestone milestone : milestones) {
			milestone.addProjectToPerimeter(project);
		}
	}

	@Override
	public void bindProjectsToMilestone(List<Long> projectIds, Long milestoneId) {
		List<GenericProject> projects = projectDao.findAll(projectIds);
		Milestone milestone = milestoneDao.findOne(milestoneId);
		milestone.bindProjects(projects);
		milestone.addProjectsToPerimeter(projects);
	}

	@Override
	public List<Milestone> getAllBindedMilestoneForProject(Long projectId) {
		GenericProject project = projectDao.findById(projectId);
		return project.getMilestones();
	}

	@Override
	public List<GenericProject> getAllBindableProjectForMilestone(Long milestoneId) {

		List<GenericProject> projectBoundToMilestone = getAllProjectForMilestone(milestoneId);
		List<GenericProject> allProjects = projectDao.findAll(new Sort(Direction.ASC, "name"));

		Milestone milestone = milestoneDao.findOne(milestoneId);
		if (milestone.getRange() == MilestoneRange.RESTRICTED) {
			allProjects.removeAll(projectTemplateDao.findAll());
		}
		allProjects.removeAll(projectBoundToMilestone);

		return allProjects;
	}

	@Override
	public List<GenericProject> getAllProjectForMilestone(Long milestoneId) {
		Milestone milestone = milestoneDao.findOne(milestoneId);
		List<GenericProject> bindedProject;
		if (milestone.getRange() == MilestoneRange.GLOBAL) {
			bindedProject = milestone.getProjects();
		} else {
			bindedProject = milestone.getPerimeter();
		}
		return bindedProject;
	}


	@SuppressWarnings("unchecked")
	@Override
	public void unbindMilestonesFromProject(List<Long> milestoneIds, Long projectId) {

		GenericProject project = projectDao.findOne(projectId);
		List<Milestone> milestones = milestoneDao.findAll(milestoneIds);
		unbindMilestonesFromProject(project, milestones);
	}

	private void unbindMilestonesFromProject(GenericProject project, List<Milestone> milestones) {

		project.unbindMilestones(milestones);

		// Remove the project in different for loop because milestoneDao.unbindAllObjectsForProject may clear the
		// session
		for (Milestone milestone : milestones) {
			milestone.removeProjectFromPerimeter(project);
		}
		// save the test case and requirement ids for reindexation later
		Collection<Long> milestoneIds = CollectionUtils.collect(milestones, ID_COLLECTOR);

		Collection<Long> tcIds = milestoneDao.findTestCaseIdsBoundToMilestones(milestoneIds);
		Collection<Long> reqIds = milestoneDao.findRequirementVersionIdsBoundToMilestones(milestoneIds);

		for (Milestone milestone : milestones) {
			// that thing will probably clear the session, be careful
			milestoneDao.unbindAllObjectsForProject(milestone.getId(), project.getId());
		}
		
		// reindex
		indexService.batchReindexTc(tcIds);
		indexService.batchReindexReqVersion(reqIds);
	}

	@Override
	public void unbindAllMilestonesFromProject(@NotNull GenericProject project) {
		unbindMilestonesFromProject(project, project.getMilestones());

	}


	@Override
	public void unbindProjectsFromMilestone(List<Long> projectIds, Long milestoneId) {
		Milestone milestone = milestoneDao.findOne(milestoneId);
		List<GenericProject> projects = projectDao.findAll(projectIds);
		milestone.unbindProjects(projects);
		milestone.removeProjectsFromPerimeter(projects);
		milestoneDao.unbindAllObjectsForProjects(milestoneId, projectIds);
	}

	@Override
	public List<Milestone> getAllBindableMilestoneForProject(Long projectId, String type) {
		List<Milestone> milestones = getAllBindableMilestoneForProject(projectId);

		return removeNonBindableStatus(filterByType(milestones, type));
	}

	private List<Milestone> removeNonBindableStatus(List<Milestone> milestones) {

		List<Milestone> filtered = new ArrayList<>();

		for (Milestone milestone : milestones) {
			if (milestone.getStatus().isBindableToProject()) {
				filtered.add(milestone);
			}
		}
		return filtered;
	}

	private List<Milestone> filterByType(List<Milestone> milestones, String type) {

		List<Milestone> filtered;
		if ("global".equals(type)) {
			// global milestone
			filtered = getGlobalMilestones(milestones);

		} else if ("personal".equals(type)) {
			// milestone created by the user
			filtered = getMilestoneCreatedBySelf(milestones);
		} else {
			// other milestone
			filtered = getOtherMilestones(milestones);
		}
		return filtered;
	}

	private List<Milestone> getOtherMilestones(List<Milestone> milestones) {
		List<Milestone> filtered = new ArrayList<>();

		for (Milestone milestone : milestones) {
			if (isRestricted(milestone) && !isCreatedBySelf(milestone)) {
				filtered.add(milestone);
			}
		}
		return filtered;
	}

	private List<Milestone> getMilestoneCreatedBySelf(List<Milestone> milestones) {
		List<Milestone> filtered = new ArrayList<>();
		for (Milestone milestone : milestones) {
			if (isRestricted(milestone) && isCreatedBySelf(milestone)) {
				filtered.add(milestone);
			}
		}
		return filtered;
	}

	private boolean isRestricted(Milestone milestone) {
		boolean isRestricted = false;
		if (milestone.getRange() == MilestoneRange.RESTRICTED) {
			isRestricted = true;
		}
		return isRestricted;
	}

	private boolean isCreatedBySelf(Milestone milestone) {
		boolean isCreatedBySelf = false;
		String myName = userContextService.getUsername();
		if (myName.equals(milestone.getOwner().getLogin())) {
			isCreatedBySelf = true;
		}
		return isCreatedBySelf;
	}

	private List<Milestone> getGlobalMilestones(List<Milestone> milestones) {
		List<Milestone> filtered = new ArrayList<>();
		for (Milestone milestone : milestones) {
			if (milestone.getRange() == MilestoneRange.GLOBAL) {
				filtered.add(milestone);
			}
		}
		return filtered;
	}

	@Override
	public void unbindProjectsFromMilestoneKeepInPerimeter(List<Long> projectIds, Long milestoneId) {
		Milestone milestone = milestoneDao.findOne(milestoneId);
		List<GenericProject> projects = projectDao.findAll(projectIds);
		milestone.unbindProjects(projects);
		milestoneDao.unbindAllObjectsForProjects(milestoneId, projectIds);
	}

	@Override
	public void unbindTemplateFrom(Long milestoneId) {
		Milestone milestone = milestoneDao.findOne(milestoneId);
		milestone.removeTemplates();
	}

	@Override
	public void bindMilestonesToProjectAndBindObject(Long projectId, List<Long> milestoneIds) {
		bindMilestonesToProject(milestoneIds, projectId);
		for (Long milestoneId : milestoneIds) {
			milestoneDao.bindMilestoneToProjectTestCases(projectId, milestoneId);
			milestoneDao.bindMilestoneToProjectRequirementVersions(projectId, milestoneId);

		}

	}
}
