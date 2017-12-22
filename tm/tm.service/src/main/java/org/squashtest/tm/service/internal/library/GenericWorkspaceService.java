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
package org.squashtest.tm.service.internal.library;

import static org.squashtest.tm.service.security.Authorizations.OR_HAS_ROLE_ADMIN;

import java.util.List;

import org.springframework.security.access.prepost.PostFilter;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.library.Library;
import org.squashtest.tm.domain.library.LibraryNode;
import org.squashtest.tm.domain.projectfilter.ProjectFilter;
import org.squashtest.tm.service.internal.repository.LibraryDao;
import org.squashtest.tm.service.library.WorkspaceService;
import org.squashtest.tm.service.project.ProjectFilterModificationService;

/**
 * Generic service for workspace access. This service must be configured through XML.
 * 
 * @author Gregory Fouquet
 * 
 * @param <LIBRARY>
 * @param <NODE>
 */
@Transactional(readOnly = true)
public class GenericWorkspaceService<LIBRARY extends Library<NODE>, NODE extends LibraryNode> implements
WorkspaceService<LIBRARY> {

	private final ProjectFilterModificationService projectFilterModificationService;

	private final LibraryDao<LIBRARY, NODE> libraryDao;

	private final LibrarySelectionStrategy<LIBRARY, NODE> libraryStrategy;

	public GenericWorkspaceService(ProjectFilterModificationService projectFilterModificationService, LibraryDao<LIBRARY, NODE> libraryDao, LibrarySelectionStrategy<LIBRARY, NODE> libraryStrategy) {
		this.projectFilterModificationService = projectFilterModificationService;
		this.libraryDao = libraryDao;
		this.libraryStrategy = libraryStrategy;
	}

	@Override
	@PostFilter("hasPermission(filterObject, 'READ')" + OR_HAS_ROLE_ADMIN)
	public List<LIBRARY> findAllLibraries() {
		ProjectFilter pf = projectFilterModificationService.findProjectFilterByUserLogin();
		if(pf.getActivated()){
			return libraryStrategy.getSpecificLibraries(pf.getProjects());
		}
		return libraryDao.findAll();
	}

	@Override
	@PostFilter("hasPermission(filterObject, 'WRITE')" + OR_HAS_ROLE_ADMIN)
	public List<LIBRARY> findAllEditableLibraries() {
		return libraryDao.findAll();
	}

	@Override
	@PostFilter("hasPermission(filterObject, 'IMPORT')" + OR_HAS_ROLE_ADMIN)
	public List<LIBRARY> findAllImportableLibraries() {
		return libraryDao.findAll();
	}
}
