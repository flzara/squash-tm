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
package org.squashtest.tm.service.internal.customreport;

import static org.squashtest.tm.service.security.Authorizations.OR_HAS_ROLE_ADMIN;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.springframework.security.access.prepost.PostFilter;
import org.springframework.stereotype.Service;
import org.squashtest.tm.domain.customreport.CustomReportLibrary;
import org.squashtest.tm.domain.customreport.CustomReportLibraryNode;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.projectfilter.ProjectFilter;
import org.squashtest.tm.domain.tree.TreeLibraryNode;
import org.squashtest.tm.service.customreport.CustomReportWorkspaceService;
import org.squashtest.tm.service.internal.repository.CustomReportLibraryDao;
import org.squashtest.tm.service.internal.repository.CustomReportLibraryNodeDao;
import org.squashtest.tm.service.project.ProjectFilterModificationService;

@Service("org.squashtest.tm.service.customreport.CustomReportWorkspaceService")
public class CustomReportWorkspaceServiceImpl implements
		CustomReportWorkspaceService {

	@Inject
	private CustomReportLibraryDao libraryDao;

	@Inject
	private CustomReportLibraryNodeDao crlnDao;

	@Inject
	private ProjectFilterModificationService projectFilterModificationService;

	@Override
	@PostFilter("hasPermission(filterObject, 'READ')" + OR_HAS_ROLE_ADMIN)
	public List<CustomReportLibrary> findAllLibraries() {
		return libraryDao.findAll();
	}

	@Override
	public List<CustomReportLibrary> findAllEditableLibraries() {
		throw new UnsupportedOperationException("IMPLEMENTS ME");
	}

	@Override
	public List<CustomReportLibrary> findAllImportableLibraries() {
		throw new UnsupportedOperationException("IMPLEMENTS ME");
	}

	@Override
	public List<TreeLibraryNode> findContent(Long libraryId) {
		return crlnDao.findChildren(libraryId);
	}

	@Override
	@PostFilter("hasPermission(filterObject, 'READ')" + OR_HAS_ROLE_ADMIN)
	public List<CustomReportLibraryNode> findRootNodes() {
		ProjectFilter projectFilter = projectFilterModificationService.findProjectFilterByUserLogin();
		List<Long> projectIds = new ArrayList<>();
		if (projectFilter.isEnabled()) {
			if (projectFilter.getProjects().isEmpty()) {
				return Collections.emptyList();
			}
			for (Project project : projectFilter.getProjects()) {
				projectIds.add(project.getId());
			}
			return crlnDao.findAllConcreteLibraries(projectIds);
		}
		else {
			return crlnDao.findAllConcreteLibraries();
		}
	}

}
