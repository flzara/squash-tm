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

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.domain.project.ProjectTemplate;
import org.squashtest.tm.service.internal.repository.ProjectTemplateDao;
import org.squashtest.tm.service.project.CustomProjectTemplateManagerService;
import org.squashtest.tm.service.project.GenericProjectCopyParameter;
import org.squashtest.tm.service.project.GenericProjectManagerService;

/**
 * 
 * @author mpagnon
 * 
 */
@Service("CustomProjectTemplateManagerService")
@Transactional
public class CustomProjectTemplateManagerServiceImpl implements CustomProjectTemplateManagerService {
	@Inject
	private ProjectTemplateDao projectTemplateDao;
	
	@Inject
	private GenericProjectManagerService genericProjectManager;
	
	@Override
	@Transactional(readOnly = true)
	public List<ProjectTemplate> findAll() {
		return projectTemplateDao.findAll();
	}

	@Override
	public ProjectTemplate addTemplateFromProject(ProjectTemplate newTemplate,
			long sourceGenericProjectId, GenericProjectCopyParameter params) {

		genericProjectManager.persist(newTemplate);
		GenericProject source = genericProjectManager.findById(sourceGenericProjectId);
		
		genericProjectManager.synchronizeGenericProject(newTemplate, source, params);
		return newTemplate;
	}
}
