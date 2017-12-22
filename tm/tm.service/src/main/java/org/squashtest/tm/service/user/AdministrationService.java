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
package org.squashtest.tm.service.user;

import java.util.List;

import org.squashtest.tm.domain.AdministrationStatistics;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.users.Team;
import org.squashtest.tm.domain.users.User;

/**
 *
 *
 * //TODO : should be split between USerAdminService and WhateverAdminService
 *
 *
 * Will handle CRUD on Squash user accounts, groups, permissions and the like. For all operations about user
 * authentication, the said operation will be delegated to the UserAuthenticationManagerService.
 *
 * Security should ensure that :
 *
 * - access to user informations (both reading and writing) are opened to the said user ROLE_ADMIN authority only, - the
 * rest requires ROLE_ADMIN authority only.
 *
 * @author bsiri
 *
 */

public interface AdministrationService extends UserManagerService {

	/**
	 * will ask database how much there is of some entities and return it in a {@link AdministrationStatistics} bean.
	 *
	 * @return {@link AdministrationStatistics} as result of counts in database.
	 */
	AdministrationStatistics findAdministrationStatistics();

	// TODO use a project finder where this method is used
	List<Project> findAllProjects();

	void modifyWelcomeMessage(String welcomeMessage);

	String findWelcomeMessage();

	void modifyLoginMessage(String loginMessage);

	String findLoginMessage();


	/**
	 * Will add user to teams members lists.<br>
	 * access restricted to admins
	 *
	 * @param userId
	 *            : the id of the concerned {@link User}
	 * @param teamIds
	 *            : ids of the {@link Team}s to add user to.
	 */
	void associateToTeams(long userId, List<Long> teamIds);

}
