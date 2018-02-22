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
package org.squashtest.tm.service.internal.infolist;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.infolist.InfoList;
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.service.infolist.InfoListBindingManagerService;
import org.squashtest.tm.service.internal.project.ProjectHelper;
import org.squashtest.tm.service.internal.repository.GenericProjectDao;
import org.squashtest.tm.service.internal.repository.InfoListDao;
import org.squashtest.tm.service.internal.repository.ProjectDao;

import java.util.Collection;

@Transactional
@Service("squashtest.tm.service.InfoListBindingManagerService")
public class InfoListBindingManagerServiceImpl implements InfoListBindingManagerService {

	@Inject
	private GenericProjectDao genericProjectDao;
	@Inject
	private ProjectDao projectDao;
	@Inject
	private InfoListDao infoListDao;

	@Override
	public void bindListToProjectReqCategory(long infoListId, long projectId) {
		GenericProject project = genericProjectDao.findOne(projectId);
		InfoList infoList = infoListDao.findOne(infoListId);
		InfoListItem defaultItem = infoList.getDefaultItem();
		project.setRequirementCategories(infoList);
		infoListDao.setDefaultCategoryForProject(projectId, defaultItem);

		/*If the GenericProject is a Template, modifications are propagated. */
		if(ProjectHelper.isTemplate(project)) {
			Collection<Project> boundProjects = projectDao.findAllBoundToTemplate(projectId);
			for(Project boundProject : boundProjects) {
				boundProject.setRequirementCategories(infoList);
				infoListDao.setDefaultCategoryForProject(boundProject.getId(), defaultItem);
			}
		}
	}

	@Override
	public void bindListToProjectTcNature(long infoListId, long projectId) {
		GenericProject project = genericProjectDao.findOne(projectId);
		InfoList infoList = infoListDao.findOne(infoListId);
		InfoListItem defaultItem = infoList.getDefaultItem();
		project.setTestCaseNatures(infoList);
		infoListDao.setDefaultNatureForProject(projectId, defaultItem);

		if(ProjectHelper.isTemplate(project)) {
			Collection<Project> boundProjects = projectDao.findAllBoundToTemplate(projectId);
			for(Project boundProject : boundProjects) {
				boundProject.setTestCaseNatures(infoList);
				infoListDao.setDefaultNatureForProject(boundProject.getId(), defaultItem);
			}
		}
	}

	@Override
	public void bindListToProjectTcType(long infoListId, long projectId) {
		GenericProject project = genericProjectDao.findOne(projectId);
		InfoList infoList = infoListDao.findOne(infoListId);
		InfoListItem defaultItem = infoList.getDefaultItem();
		project.setTestCaseTypes(infoList);
		infoListDao.setDefaultTypeForProject(projectId, defaultItem);

		if(ProjectHelper.isTemplate(project)) {
			Collection<Project> boundProjects = projectDao.findAllBoundToTemplate(projectId);
			for(Project boundProject : boundProjects) {
				boundProject.setTestCaseTypes(infoList);
				infoListDao.setDefaultTypeForProject(boundProject.getId(), defaultItem);
			}
		}
	}



}
