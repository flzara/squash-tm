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

import java.util.Collection;
import java.util.List;

import org.squashtest.tm.core.foundation.collection.Filtering;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.project.AdministrableProject;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.domain.users.Party;
import org.squashtest.tm.domain.users.PartyProjectPermissionsBean;
import org.squashtest.tm.security.acls.PermissionGroup;
/**
 * Holder for non dynamically generated find methods for both Project and ProjectTemplate
 * @author mpagnon
 *
 */
public interface CustomGenericProjectFinder {


	AdministrableProject findAdministrableProjectById(long projectId);

	List<TestAutomationProject> findBoundTestAutomationProjects(long projectId);

	List<PartyProjectPermissionsBean> findPartyPermissionsBeansByProject(long projectId);

	PagedCollectionHolder<List<PartyProjectPermissionsBean>> findPartyPermissionsBeanByProject(
			PagingAndSorting sorting, Filtering filtering, long projectId);

	List<PermissionGroup> findAllPossiblePermission();

	List<Party> findPartyWithoutPermissionByProject(long projectId);

	/**
	 * Will return a list of TestAutomationProject (jobNames only) available for the server bound to the given project.
	 * The returned list will not contain already bound ta-projects.
	 *
	 * @param projectId
	 *            : the id of the {@link GenericProject} we want the available ta-projects for
	 * @return : the list of {@link TestAutomationProject} available and not already bound to the tm-project
	 */
	Collection<TestAutomationProject> findAllAvailableTaProjects(long projectId);
	/**
	 * Will return a list of TestAutomationProject (jobNames only) available on the server bound to the given project 
	 * and authorized for the Jenkins account defined with the login and the password given as parameters. 
	 * The returned list will not contain already bound ta-projects.
	 * @param projectId The Id of the {@link GenericProject} we want the available ta-projects for.
	 * @param login The login of the Jenkins account.
	 * @param password The password of the Jenkins account.
	 * @return The list of {@link TestAutomationProject} available on the server, 
	 * authorized for this Jenkins account and not already bound to the tm-project.
	 */
	Collection<TestAutomationProject> findAllAvailableTaProjectsWithCredentials(long projectId, String login, String password);
}
