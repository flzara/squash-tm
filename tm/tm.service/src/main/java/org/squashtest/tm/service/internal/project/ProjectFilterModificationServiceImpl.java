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

import static org.squashtest.tm.service.security.Authorizations.OR_HAS_ROLE_ADMIN;

import java.util.List;

import javax.inject.Inject;

import org.springframework.security.access.prepost.PostFilter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.projectfilter.ProjectFilter;
import org.squashtest.tm.service.internal.repository.ProjectDao;
import org.squashtest.tm.service.internal.repository.ProjectFilterDao;
import org.squashtest.tm.service.project.ProjectFilterModificationService;
import org.squashtest.tm.service.project.ProjectManagerService;
import org.squashtest.tm.service.security.UserContextService;

@Service("squashtest.tm.service.ProjectFilterModificationService")
@Transactional
public class ProjectFilterModificationServiceImpl implements ProjectFilterModificationService {

	@Inject
	private ProjectFilterDao projectFilterDao;

	@Inject
	private ProjectDao projectDao;

	@Inject
	private ProjectManagerService projectManager;

	@Inject
	private UserContextService userContextService;

	/***
	 * This method checks whether this user uses a project filter. If so, it returns it. If not, it returns a default - empty- one.
	 *
	 * @return what I just said
	 */
	@Override
	public ProjectFilter findProjectFilterByUserLogin() {
		ProjectFilter filter = findPersistentProjectFilter();
		if (filter != null) {
			return filter;
		}
		else{
			return createDefaultProjectFilter();
		}
	}

	@Override
	public void saveOrUpdateProjectFilter(List<Long> projectIdList, boolean isActive) {

		ProjectFilter projectFilter = findOrCreateProjectFilter();

		projectFilter.setProjects(projectDao.findByIdIn(projectIdList));
		projectFilter.setActivated(isActive);
	}

	@Override
	public void updateProjectFilterStatus(boolean status) {
		findOrCreateProjectFilter().setActivated(status);
	}



	@Override
	@PostFilter("hasPermission(filterObject, 'READ')" + OR_HAS_ROLE_ADMIN)
	@Transactional(readOnly = true)
	public List<Project> getAllProjects() {
		return projectManager.findAllOrderedByName();
	}


	// ****************************** private stuffs *******************************

	private ProjectFilter findPersistentProjectFilter(){
		String userLogin = userContextService.getUsername();
		return projectFilterDao.findByUserLogin(userLogin);
	}

	private ProjectFilter createDefaultProjectFilter(){
		ProjectFilter toReturn = new ProjectFilter();

		String userLogin = userContextService.getUsername();
		toReturn.setProjects(getAllProjects());
		toReturn.setUserLogin(userLogin);
		toReturn.setActivated(false);

		return toReturn;
	}

	private ProjectFilter findOrCreateProjectFilter(){

		ProjectFilter filter = findPersistentProjectFilter();

		if (filter == null) {
			filter = createDefaultProjectFilter();
			projectFilterDao.save(filter);
		}

		return filter;
	}

}
