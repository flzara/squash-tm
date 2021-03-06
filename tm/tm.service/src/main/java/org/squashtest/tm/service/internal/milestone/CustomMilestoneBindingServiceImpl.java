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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.audit.AuditableMixin;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.milestone.MilestoneRange;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.service.audit.AuditModificationService;
import org.squashtest.tm.service.internal.repository.GenericProjectDao;
import org.squashtest.tm.service.internal.repository.MilestoneDao;
import org.squashtest.tm.service.internal.repository.ProjectTemplateDao;
import org.squashtest.tm.service.milestone.MilestoneBindingManagerService;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.security.UserContextService;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

import static org.squashtest.tm.service.security.Authorizations.OR_HAS_ROLE_ADMIN;

@Transactional
@Service("squashtest.tm.service.MilestoneBindingManagerService")
public class CustomMilestoneBindingServiceImpl implements MilestoneBindingManagerService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CustomMilestoneBindingServiceImpl.class);

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
	private AuditModificationService auditModificationService;

	@Override
	public List<Milestone> getAllBindableMilestoneForProject(Long projectId) {

		List<Milestone> milestoneBoundToProject = getAllBindedMilestoneForProject(projectId);
		List<Milestone> allMilestones = milestoneDao.findAll();
		allMilestones.removeAll(milestoneBoundToProject);
		GenericProject project = projectDao.getOne(projectId);
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
		GenericProject project = projectDao.getOne(projectId);
		List<Milestone> milestones = milestoneDao.findAllById(milestoneIds);
		project.bindMilestones(milestones);
		for (Milestone milestone : milestones) {
			milestone.addProjectToPerimeter(project);
			LOGGER.debug("Milestone binding: updating auditable milestone {}", milestone.getId());
			auditModificationService.updateAuditable((AuditableMixin)milestone);
		}

		LOGGER.debug("Milestone binding: updating auditable project {}", projectId);
		auditModificationService.updateAuditable((AuditableMixin)project);
	}

	@Override
	public void bindProjectsToMilestone(List<Long> projectIds, Long milestoneId) {
		List<GenericProject> projects = projectDao.findAllById(projectIds);
		Milestone milestone = milestoneDao.getOne(milestoneId);
		milestone.bindProjects(projects);
		milestone.addProjectsToPerimeter(projects);

		LOGGER.debug("Milestone binding: updating auditable milestone {}", milestoneId);
		auditModificationService.updateAuditable((AuditableMixin)milestone);

		LOGGER.debug("Milestone binding: updating multiple auditable projects");
		projects.forEach(project -> auditModificationService.updateAuditable((AuditableMixin)project));
	}

	@Override
	public List<Milestone> getAllBindedMilestoneForProject(Long projectId) {
		GenericProject project = projectDao.getOne(projectId);
		return project.getMilestones();
	}

	@Override
	@PostFilter("hasPermission(filterObject , 'MANAGEMENT')" + OR_HAS_ROLE_ADMIN)
	public List<GenericProject> getAllBindableProjectForMilestone(Long milestoneId) {

		List<GenericProject> projectBoundToMilestone = getAllProjectForMilestone(milestoneId);
		List<GenericProject> allProjects = projectDao.findAll(new Sort(Direction.ASC, "name"));

		Milestone milestone = milestoneDao.getOne(milestoneId);
		if (milestone.getRange() == MilestoneRange.RESTRICTED) {
			allProjects.removeAll(projectTemplateDao.findAll());
		}
		allProjects.removeAll(projectBoundToMilestone);

		return allProjects;
	}

	@Override
	public List<GenericProject> getAllProjectForMilestone(Long milestoneId) {
		Milestone milestone = milestoneDao.getOne(milestoneId);
		List<GenericProject> boundProject;
		if (milestone.getRange() == MilestoneRange.GLOBAL) {
			boundProject = milestone.getProjects();
		} else {
			boundProject = milestone.getPerimeter();
		}
		return boundProject;
	}


	@SuppressWarnings("unchecked")
	@Override
	public void unbindMilestonesFromProject(List<Long> milestoneIds, Long projectId) {

		GenericProject project = projectDao.getOne(projectId);
		List<Milestone> milestones = milestoneDao.findAllById(milestoneIds);
		unbindMilestonesFromProject(project, milestones);
	}

	private void unbindMilestonesFromProject(GenericProject project, List<Milestone> milestones) {

		project.unbindMilestones(milestones);

		LOGGER.debug("Milestone unbinding: updating auditable project {}", project.getId());
		auditModificationService.updateAuditable((AuditableMixin)project);

		// Remove the project in different for loop because milestoneDao.unbindAllObjectsForProject may clear the
		// session
		for (Milestone milestone : milestones) {
			milestone.removeProjectFromPerimeter(project);
			LOGGER.debug("Milestone unbinding: updating auditable milestone {}", milestone.getId());
			auditModificationService.updateAuditable((AuditableMixin)milestone);
		}

		for (Milestone milestone : milestones) {
			// that thing will probably clear the session, be careful
			milestoneDao.unbindAllObjectsForProject(milestone.getId(), project.getId());
		}
	}

	@Override
	public void unbindAllMilestonesFromProject(@NotNull GenericProject project) {
		unbindMilestonesFromProject(project, project.getMilestones());

	}


	@Override
	public void unbindProjectsFromMilestone(List<Long> projectIds, Long milestoneId) {
		Milestone milestone = milestoneDao.getOne(milestoneId);
		List<GenericProject> projects = projectDao.findAllById(projectIds);
		milestone.unbindProjects(projects);

		LOGGER.debug("Milestone unbinding: updating multiple auditable projects");
		projects.forEach(project -> auditModificationService.updateAuditable((AuditableMixin)project));

		milestone.removeProjectsFromPerimeter(projects);

		LOGGER.debug("Milestone unbinding: updating auditable milestone {}", milestone.getId());
		auditModificationService.updateAuditable((AuditableMixin)milestone);

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
		Milestone milestone = milestoneDao.getOne(milestoneId);
		List<GenericProject> projects = projectDao.findAllById(projectIds);
		milestone.unbindProjects(projects);

		LOGGER.debug("Milestone unbinding: updating auditable milestone {}", milestone.getId());
		auditModificationService.updateAuditable((AuditableMixin)milestone);

		LOGGER.debug("Milestone unbinding: updating multiple auditable project");
		projects.forEach(project -> auditModificationService.updateAuditable((AuditableMixin)project));

		milestoneDao.unbindAllObjectsForProjects(milestoneId, projectIds);
	}

	@Override
	public void unbindTemplateFrom(Long milestoneId) {
		Milestone milestone = milestoneDao.getOne(milestoneId);
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
