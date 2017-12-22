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

import java.util.List;

import org.springframework.data.domain.Sort;
import org.squashtest.tm.core.foundation.collection.Filtering;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.domain.project.ProjectPermission;
import org.squashtest.tm.domain.users.Party;
import org.squashtest.tm.domain.users.PartyProjectPermissionsBean;
import org.squashtest.tm.security.acls.PermissionGroup;

/**
 * @author mpagnon
 *
 */
public interface ProjectsPermissionFinder {

	List<PermissionGroup> findAllPossiblePermission();

	List<ProjectPermission> findProjectPermissionByParty(long partyId);

	List<ProjectPermission> findProjectPermissionByUserLogin(String userLogin);

	PagedCollectionHolder<List<ProjectPermission>> findProjectPermissionByParty(long partyId, PagingAndSorting sorting, Filtering filtering);

	List<GenericProject> findProjectWithoutPermissionByParty(long partyId, Sort sorting);

	List<GenericProject> findProjectWithoutPermissionByParty(long partyId);

	List<PartyProjectPermissionsBean> findPartyPermissionsBeanByProject(long projectId);

	PagedCollectionHolder<List<PartyProjectPermissionsBean>> findPartyPermissionsBeanByProject(PagingAndSorting sorting, Filtering filtering, long projectId);

	List<Party> findPartyWithoutPermissionByProject(long projectId);
	/**
	 * @param userId
	 * @param projectId
	 */
	void removeProjectPermission(long userId, long projectId);

	boolean isInPermissionGroup(long partyId, Long projectId, String permissionGroup);

	boolean isInPermissionGroup(String userLogin, Long projectId, String permissionGroup);

	List<GenericProject> findProjectWithPermissionByParty(long partyId);
}
