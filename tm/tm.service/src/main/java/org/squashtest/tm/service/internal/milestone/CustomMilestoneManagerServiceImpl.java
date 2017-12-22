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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.milestone.MilestoneHolder;
import org.squashtest.tm.domain.milestone.MilestoneRange;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.exception.milestone.MilestoneLabelAlreadyExistsException;
import org.squashtest.tm.service.internal.dto.UserDto;
import org.squashtest.tm.service.internal.repository.CustomMilestoneDao.HolderConsumer;
import org.squashtest.tm.service.internal.repository.MilestoneDao;
import org.squashtest.tm.service.milestone.CustomMilestoneManager;
import org.squashtest.tm.service.project.ProjectFinder;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.security.UserContextService;
import org.squashtest.tm.service.user.UserAccountService;

@Service("CustomMilestoneManager")
public class CustomMilestoneManagerServiceImpl implements CustomMilestoneManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(CustomMilestoneManagerServiceImpl.class);

// TODO replace by the app wide const
private static final String ADMIN_ROLE = "ROLE_ADMIN";

	@Inject
	private ProjectFinder projectFinder;

	@Inject
	private MilestoneDao milestoneDao;

	@Inject
	private UserContextService userContextService;

	@Inject
	private UserAccountService userService;

	@Inject
	private PermissionEvaluationService permissionEvaluationService;

	@PersistenceContext
	private EntityManager em;

	@Override
	public void addMilestone(Milestone milestone) {
		checkLabelAvailability(milestone.getLabel());
		milestone.setOwner(userService.findCurrentUser());
		milestoneDao.save(milestone);
	}

	@Override
	public void changeLabel(long milestoneId, String newLabel) {
		checkLabelAvailability(newLabel);
		Milestone m = milestoneDao.findOne(milestoneId);
		m.setLabel(newLabel);
	}

	private void checkLabelAvailability(String label) {
		if (milestoneDao.findByLabel(label) != null) {
			throw new MilestoneLabelAlreadyExistsException(label);
		}

	}
	@Override
	public List<Milestone> findAll() {
		return milestoneDao.findAll();
	}

	@Override
	public void removeMilestones(Collection<Long> ids) {
		for (final Long id : ids) {
			Milestone milestone = milestoneDao.findOne(id);
			deleteMilestoneBinding(milestone);
			deleteMilestone(milestone);
		}
	}

	private void deleteMilestoneBinding(final Milestone milestone) {
		List<GenericProject> projects = milestone.getProjects();
		for (GenericProject project : projects) {
			project.unbindMilestone(milestone);
		}
	}

	private void deleteMilestone(final Milestone milestone) {

		milestoneDao.delete(milestone);
	}

	@Override
	public Milestone findById(long milestoneId) {
		return milestoneDao.findOne(milestoneId);
	}

	@Override
	public List<Milestone> findAllByIds(List<Long> milestoneIds) {
		return milestoneDao.findAll(milestoneIds);
	}

	@Override
	public void verifyCanEditMilestone(long milestoneId) {
		if (!canEditMilestone(milestoneId)) {
			throw new IllegalAccessError("What are you doing here ?! You are not allowed. Go away");
		}
	}

	private boolean isGlobal(Milestone milestone) {
		return MilestoneRange.GLOBAL == milestone.getRange();
	}

	private boolean isCreatedBySelf(Milestone milestone) {
		String myName = userContextService.getUsername();
		return myName.equals(milestone.getOwner().getLogin());
	}

	@Override
	public void verifyCanEditMilestoneRange() {
		// only admin can edit range
		if (!permissionEvaluationService.hasRole(ADMIN_ROLE)) {
			throw new IllegalAccessError("What are you doing here ?! You are not allowed. Go away");
		}

	}

	@Override
	public boolean canEditMilestone(long milestoneId) {
		Milestone milestone = milestoneDao.findOne(milestoneId);
		// admin can edit all milestones
		if (!permissionEvaluationService.hasRole(ADMIN_ROLE)) {
			// project manager can't edit global milestone or milestone they don't own
			if (isGlobal(milestone) || !isCreatedBySelf(milestone)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public List<Long> findAllIdsOfEditableMilestone() {
		List<Milestone> milestones = findAll();
		List<Long> ids = new ArrayList<>();
		for (Milestone milestone : milestones) {
			if (canEditMilestone(milestone.getId())) {
				ids.add(milestone.getId());
			}
		}
		return ids;
	}

	@Override
	public List<Milestone> findAllVisibleToCurrentManager() {

		List<Milestone> allMilestones = findAll();
		List<Milestone> milestones = new ArrayList<>();

		if (permissionEvaluationService.hasRole(ADMIN_ROLE)) {
			milestones.addAll(allMilestones);
		} else {
			for (Milestone milestone : allMilestones) {
				if (isGlobal(milestone) || isCreatedBySelf(milestone) || isInAProjetICanManage(milestone)) {
					milestones.add(milestone);
				}
			}
		}
		return milestones;
	}

	
	@Override
	public List<Milestone> findAllVisibleToCurrentUser() {

		List<Long> milestoneIds = findAllIdsVisibleToCurrentUser();		
		return milestoneDao.findAll(milestoneIds);
	}
	
	
	@Override
	public List<Long> findAllIdsVisibleToCurrentUser() {
		UserDto user = userService.findCurrentUserDto();
		if (user.isAdmin()){
			return milestoneDao.findAllMilestoneIds();
		}
		else{
			return milestoneDao.findMilestoneIdsForUsers(user.getPartyIds());
		}
	}

	private boolean isInAProjetICanManage(Milestone milestone) {
		boolean isInAProjetICanManage = false;
		List<GenericProject> perimeter = milestone.getPerimeter();

		for (GenericProject project : perimeter) {
			if (canIManageThisProject(project)) {
				isInAProjetICanManage = true;
				break;
			}
		}
		return isInAProjetICanManage;
	}

	private boolean canIManageThisProject(GenericProject project) {
		return permissionEvaluationService.hasRoleOrPermissionOnObject("ADMIN", "MANAGEMENT", project);
	}

	private List<GenericProject> getProjectICanManage(Collection<GenericProject> projects) {

		List<GenericProject> manageableProjects = new ArrayList<>();

		for (GenericProject project : projects) {
			if (canIManageThisProject(project)) {
				manageableProjects.add(project);
			}
		}
		return manageableProjects;
	}

	@Override
	public boolean isBoundToATemplate(Long milestoneId) {
		Milestone milestone = findById(milestoneId);
		return milestone.isBoundToATemplate();
	}

	@Override
	public void cloneMilestone(long motherId, Milestone milestone, boolean bindToRequirements, boolean bindToTestCases) {
		Milestone mother = findById(motherId);
		boolean copyAllPerimeter = permissionEvaluationService.hasRole(ADMIN_ROLE)
				|| !isGlobal(mother)
			&& isCreatedBySelf(mother);

		bindProjectsAndPerimeter(mother, milestone, copyAllPerimeter);
		bindRequirements(mother, milestone, bindToRequirements, copyAllPerimeter);
		bindTestCases(mother, milestone, bindToTestCases, copyAllPerimeter);
		addMilestone(milestone);
	}

	@Override
	public void migrateMilestones(MilestoneHolder member) {

		Collection<Milestone> projectMilestones = member.getProject().getMilestones();
		Collection<Milestone> memberMilestones = member.getMilestones();

		Iterator<Milestone> memberIterator = memberMilestones.iterator();
		while (memberIterator.hasNext()) {
			Milestone m = memberIterator.next();
			if (!projectMilestones.contains(m)) {
				memberIterator.remove();
			}
		}
	}

	private void bindProjectsAndPerimeter(Milestone mother, Milestone milestone, boolean copyAllPerimeter) {

		if (copyAllPerimeter) {
			milestone.bindProjects(mother.getProjects());
			milestone.addProjectsToPerimeter(mother.getPerimeter());
		} else {

			List<GenericProject> projects = new ArrayList<>(mother.getProjects());
			projects.retainAll(projectFinder.findAllICanManage());

			List<GenericProject> perim = new ArrayList<>(mother.getPerimeter());
			perim.retainAll(projectFinder.findAllICanManage());

			milestone.bindProjects(projects);
			milestone.addProjectsToPerimeter(perim);

		}

	}


	private void bindTestCases(Milestone mother, Milestone milestone, boolean bindToTestCases, boolean copyAllPerimeter) {
		if (bindToTestCases) {
			for (TestCase tc : mother.getTestCases()) {
				if (copyAllPerimeter || canIManageThisProject(tc.getProject())) {
					milestone.bindTestCase(tc);
				}
			}
		}
	}

	private void bindRequirements(Milestone mother, Milestone milestone, boolean bindToRequirements,
	                              boolean copyAllPerimeter) {
		if (bindToRequirements) {
			for (RequirementVersion req : mother.getRequirementVersions()) {
				if (copyAllPerimeter || canIManageThisProject(req.getProject())) {
					milestone.bindRequirementVersion(req);
				}
			}
		}
	}

	@Override
	public void synchronize(long sourceId, long targetId, boolean extendPerimeter, boolean isUnion) {

		Milestone source = findById(sourceId);
		Milestone target = findById(targetId);
		verifyCanSynchronize(source, target, isUnion);
		synchronizePerimeterAndProjects(source, target, extendPerimeter, isUnion);
		synchronizeTestCases(source, target, isUnion, extendPerimeter);
		synchronizeRequirementVersions(source, target, isUnion, extendPerimeter);
	}

	private void verifyCanSynchronize(Milestone source, Milestone target, boolean isUnion) {

		if (isUnion
 && (!source.getStatus().isBindableToObject()
				|| !permissionEvaluationService.hasRole(ADMIN_ROLE)
						&& isGlobal(source))) {
			throw new IllegalArgumentException(
					"milestone can't be synchronized because it's status or range don't allow it");
		}

		if (!target.getStatus().isBindableToObject()
				|| !permissionEvaluationService.hasRole(ADMIN_ROLE)
				&& isGlobal(target)) {
			throw new IllegalArgumentException(
					"milestone can't be synchronized because it's status or range don't allow it");
		}

	}


	private void synchronizeRequirementVersions(Milestone source, Milestone target, boolean isUnion,
	                                            boolean extendPerimeter) {
		milestoneDao.synchronizeRequirementVersions(source.getId(), target.getId(),
			getProjectsToSynchronize(source, target, extendPerimeter, isUnion));
		if (isUnion) {
			milestoneDao.synchronizeRequirementVersions(target.getId(), source.getId(),
				getProjectsToSynchronize(target, source, extendPerimeter, isUnion));
		}
	}

	private void synchronizeTestCases(Milestone source, Milestone target, boolean isUnion, boolean extendPerimeter) {
		milestoneDao.synchronizeTestCases(source.getId(), target.getId(),
			getProjectsToSynchronize(source, target, extendPerimeter, isUnion));
		if (isUnion) {
			milestoneDao.synchronizeTestCases(target.getId(), source.getId(),
				getProjectsToSynchronize(target, source, extendPerimeter, isUnion));
		}
	}

	private Set<GenericProject> getProjectsToSynchronizeForProjectManager(Set<GenericProject> result, Milestone target,
	                                                                      boolean extendPerimeter) {
		if (extendPerimeter && isCreatedBySelf(target)) {
			result.addAll(target.getPerimeter());
		} else {
			result.retainAll(target.getPerimeter());

			if (!isCreatedBySelf(target)) {
				result.retainAll(getProjectICanManage(result));
			}
		}
		return result;
	}

	private List<Long> getProjectsToSynchronize(Milestone source, Milestone target, boolean extendPerimeter,
	                                            boolean isUnion) {

		Set<GenericProject> result = new HashSet<>(source.getPerimeter());

		if (permissionEvaluationService.hasRole(ADMIN_ROLE)) {

			result = getProjectsToSynchronizeForProjectForAdmin(result, source, target, isUnion);

		} else {
			result = getProjectsToSynchronizeForProjectManager(result, target, extendPerimeter);
		}

		List<Long> ids = new ArrayList<>();
		for (GenericProject p : result) {
			ids.add(p.getId());
		}
		return ids;
	}

	private Set<GenericProject> getProjectsToSynchronizeForProjectForAdmin(Set<GenericProject> result,
	                                                                       Milestone source, Milestone target, boolean isUnion) {

		if (isUnion && isGlobal(source) && isGlobal(target) || !isUnion && isGlobal(target)) {
			result.addAll(target.getPerimeter());
		} else {
			result.retainAll(target.getPerimeter());
		}

		return result;
	}

	private void adminSynchronizePerimeterAndProjects(Milestone source, Milestone target, boolean isUnion) {

		if (isUnion) {
			adminSynchronizePerimeterAndProjectsForUnion(source, target);
		} else {
			adminSynchronizePerimeterAndProjects(source, target);
		}
	}

	private void adminSynchronizePerimeterAndProjectsForUnion(Milestone source, Milestone target) {
		if (isGlobal(source) && isGlobal(target)) {
			adminSynchronizePerimeterAndProjects(source, target);
			adminSynchronizePerimeterAndProjects(target, source);
		}

	}

	private void adminSynchronizePerimeterAndProjects(Milestone source, Milestone target) {
		if (isGlobal(target)) {
			target.bindProjects(source.getProjects());
			target.addProjectsToPerimeter(source.getPerimeter());
		}
	}

	private void projectManagerSynchronizePerimeterAndProjects(Milestone source, Milestone target,
	                                                           boolean extendPerimeter) {

		if (isCreatedBySelf(target) && extendPerimeter) {
			// can extend perimeter only if own milestone
			target.bindProjects(source.getProjects());
			target.addProjectsToPerimeter(source.getPerimeter());
		}
	}

	private void synchronizePerimeterAndProjects(Milestone source, Milestone target, boolean extendPerimeter,
	                                             boolean isUnion) {

		if (permissionEvaluationService.hasRole("ROLE_ADMIN")) {
			adminSynchronizePerimeterAndProjects(source, target, isUnion);
		} else {
			projectManagerSynchronizePerimeterAndProjects(source, target, extendPerimeter);
		}
	}

	/**
	 * @see org.squashtest.tm.service.milestone.CustomMilestoneManager#enableFeature()
	 */
	@Override
	public void enableFeature() {
		// NOOP (AFAIK)

	}

	/**
	 * @see org.squashtest.tm.service.milestone.CustomMilestoneManager#disableFeature()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void disableFeature() {
		LOGGER.info("Disabling the Milestones feature : I am about to nuke all milestones from database");

		milestoneDao.performBatchUpdate(new HolderConsumer() {
			@Override
			public void consume(MilestoneHolder holder) {
				holder.unbindAllMilestones();
			}
		});

		Session session = em.unwrap(Session.class);
		List<Milestone> milestones = session.createQuery("from Milestone").list();

		for (Milestone milestone : milestones) {
			milestone.unbindAllProjects();
			milestone.clearPerimeter();
			session.delete(milestone);
		}
	}

	@Override
	public boolean isBoundToAtleastOneObject(long milestoneId) {
		return milestoneDao.isBoundToAtleastOneObject(milestoneId);
	}

	@Override
	public void unbindAllObjects(long milestoneId) {

		milestoneDao.unbindAllObjects(milestoneId);
		Milestone milestone = findById(milestoneId);
		milestone.clearObjects();
	}

	@Override
	public Milestone findByName(String name) {
		return milestoneDao.findByName(name);
	}

	@Override
	public boolean isMilestoneBoundToOneObjectOfProject(Milestone milestone, GenericProject project) {

		return milestoneDao.isMilestoneBoundToOneObjectOfProject(milestone.getId(), project.getId());
	}

	@Override
	public boolean hasMilestone(List<Long> userdIds) {
		long result = milestoneDao.countMilestonesForUsers(userdIds);
		return result > 0;
	}

}
