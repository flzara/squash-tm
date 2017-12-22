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

import java.util.Collection;

import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.users.Party;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.service.internal.dto.UserDto;

//TODO : same methods but with no parameters (UserContextService will give us the user)
public interface UserAccountService {

	/* ** services using an ID : the calling user is not the modified user ** */

	void modifyUserFirstName(long userId, String newName);

	void modifyUserLastName(long userId, String newName);

	void modifyUserLogin(long userId, String newLogin);

	void modifyUserEmail(long userId, String newEmail);

	void deactivateUser(long userId);

	void activateUser(long userId);

	void deleteUser(long userId);

	/* ** services using no ID : the modified user is the calling user ** */
	/**
	 * Fetches the {@link User} which matches the current authenticated username / principal.
	 *
	 * If one is authenticated (through a third party authentication provider) but no {@link User} is defined, this
	 * method returns <code>null</code>.
	 *
	 * @return the current {@link User} or <code>null</code>
	 */
	User findCurrentUser();

	UserDto findCurrentUserDto();

	Party getParty(Long id);

	void setCurrentUserEmail(String newEmail);

	void setCurrentUserPassword(String oldPasswd, String newPasswd);

	Collection<Milestone> findAllMilestonesForUser(long userId);

	// Feature 6763 - Add a new method for updating the user's last connection date at each authentication success.
	void updateUserLastConnectionDate();

}
