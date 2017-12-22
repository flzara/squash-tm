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
package org.squashtest.tm.service.internal.project;

import static org.squashtest.tm.service.security.Authorizations.HAS_ROLE_ADMIN;
import static org.squashtest.tm.service.security.Authorizations.OR_HAS_ROLE_ADMIN;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.project.ProjectTemplate;
import org.squashtest.tm.service.internal.dto.UserDto;
import org.squashtest.tm.exception.NameAlreadyInUseException;
import org.squashtest.tm.service.internal.repository.*;
import org.squashtest.tm.service.project.CustomProjectModificationService;
import org.squashtest.tm.service.project.GenericProjectCopyParameter;
import org.squashtest.tm.service.project.GenericProjectManagerService;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.user.UserAccountService;

/**
 * @author mpagnon
 */
@Service("CustomProjectModificationService")
@Transactional
public class CustomProjectModificationServiceImpl implements CustomProjectModificationService {
	@Inject
	private ProjectDeletionHandler projectDeletionHandler;
	@Inject
	private ProjectTemplateDao projectTemplateDao;
	@Inject
	private GenericProjectManagerService genericProjectManager;
	@Inject
	private ProjectDao projectDao;
	@Inject
	private PermissionEvaluationService permissionEvaluationService;
	@Inject
	private GenericProjectDao genericProjectDao;
	@Inject
	protected UserAccountService userAccountService;

	@Inject
	private UserDao userDao;

	@Inject
	private TeamDao teamDao;


	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public void deleteProject(long projectId) {
		projectDeletionHandler.deleteProject(projectId);
	}

	@Override
	@PostFilter("hasPermission(filterObject, 'READ')" + OR_HAS_ROLE_ADMIN)
	@Transactional(readOnly = true)
	public List<Project> findAllReadable() {
		return projectDao.findAll();
	}

	@Override
	public Project addProjectFromtemplate(Project newProject, long templateId,
										  GenericProjectCopyParameter params)
		throws NameAlreadyInUseException {
		genericProjectManager.persist(newProject);

		ProjectTemplate projectTemplate = projectTemplateDao.findOne(templateId);
		genericProjectManager.synchronizeGenericProject(newProject, projectTemplate, params);

		return newProject;
	}


	@Override
	public List<GenericProject> findAllICanManage() {
		List<GenericProject> projects = genericProjectDao.findAll();
		List<GenericProject> manageableProjects = new ArrayList<>();

		for (GenericProject project : projects) {
			if (permissionEvaluationService.hasRoleOrPermissionOnObject("ADMIN", "MANAGEMENT", project)) {
				manageableProjects.add(project);
			}
		}
		return manageableProjects;
	}

	/**
	 * Optimized implementation with SQL and no hibernate entities.
	 * @param userDto
	 */
	@Override
	public List<Long> findAllReadableIds(UserDto userDto) {
		if (userDto.isAdmin()) {
			return projectDao.findAllProjectIds();
		} else {
			return projectDao.findAllProjectIds(userDto.getPartyIds());
		}
	}

	@Override
	public List<Long> findAllReadableIds() {
		UserDto currentUser = userAccountService.findCurrentUserDto();
		return findAllReadableIds(currentUser);
	}
}
