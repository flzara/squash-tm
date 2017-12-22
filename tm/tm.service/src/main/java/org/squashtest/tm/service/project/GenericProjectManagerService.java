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
package org.squashtest.tm.service.project;

import static org.squashtest.tm.service.security.Authorizations.HAS_ROLE_ADMIN;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.dynamicmanager.annotation.DynamicManager;
import org.squashtest.tm.domain.project.GenericProject;

/**
 * @author Gregory Fouquet
 *
 */
@Transactional
@DynamicManager(name="squashtest.tm.service.GenericProjectManagerService", entity = GenericProject.class)
public interface GenericProjectManagerService extends CustomGenericProjectManager, GenericProjectFinder {

	/*
	 * Issue 2341 : the test for manager permissions on project failed because 'GenericProject' is not a valid ACL_CLASS.classname in the database.
	 * So I had to split it and explicitly refer to the actual implementation 'Project'. A project manager cannot manage project templates anyway.
	 */
	String ADMIN_OR_PROJECT_MANAGER = HAS_ROLE_ADMIN + " or hasPermission(#arg0, 'org.squashtest.tm.domain.project.Project', 'MANAGEMENT') ";

	@PreAuthorize(ADMIN_OR_PROJECT_MANAGER)
	void changeDescription(long projectId, String newDescription);

	@PreAuthorize(ADMIN_OR_PROJECT_MANAGER)
	void changeLabel(long projectId, String newLabel);

	@PreAuthorize(HAS_ROLE_ADMIN)
	void changeActive(long projectId, boolean isActive);

	@PreAuthorize(ADMIN_OR_PROJECT_MANAGER)
	void changeAllowTcModifDuringExec(long projectId, boolean active);


}
