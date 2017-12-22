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
import java.util.List;

import javax.validation.constraints.NotNull;

import org.hibernate.HibernateException;
import org.squashtest.tm.core.foundation.collection.Filtering;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.users.Team;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.domain.users.UsersGroup;
import org.squashtest.tm.exception.user.LoginAlreadyExistsException;

/**
 *
 *
 * @author bsiri
 * @author Gregory Fouquet
 *
 */
public interface UserManagerService {

	void modifyUserFirstName(long userId, String newName);

	void modifyUserLastName(long userId, String newName);

	void modifyUserLogin(long userId, String newLogin);

	void modifyUserEmail(long userId, String newEmail);

	AuthenticatedUser findUserById(long userId);

	User findByLogin(@NotNull String login);

	/**
	 * Given a login, returns the unique actual matching login without case
	 * sensisivity
	 *
	 * @param login
	 * @return actual login or `null` when the login was not found.
	 * @throws HibernateException
	 *             when more than one login is gound
	 */
	String findCaseAwareLogin(String login);

	List<User> findAllUsersOrderedByLogin();

	List<User> findAllActiveUsersOrderedByLogin();

	PagedCollectionHolder<List<User>> findAllUsersFiltered(PagingAndSorting sorter, Filtering filter);

	void addUser(User aUser, long groupId, String password);

	void setUserGroupAuthority(long userId, long groupId);

	void resetUserPassword(long userId, String newPassword);

	void deactivateUser(long userId);

	void activateUser(long userId);

	void deactivateUsers(Collection<Long> userIds);

	void activateUsers(Collection<Long> userIds);

	void deleteUsers(Collection<Long> userIds);

	/**
	 * Will remove user from teams members lists. <br>
	 * access restricted to admins
	 *
	 * @param userId
	 *            : the id of the concerned {@link User}
	 * @param teamIds
	 *            : ids of {@link Team}s to remove user from.
	 */
	void deassociateTeams(long userId, List<Long> teamIds);

	/**
	 * Will return an paged and filtered list of {@link Team}s that have the
	 * concerned user as a member. <br>
	 * access restricted to admins
	 *
	 * @param userId
	 *            : the id of the concerned user
	 * @param paging
	 *            : the {@link PagingAndSorting} criteria that the result has to
	 *            match
	 * @param filtering
	 *            : the {@link Filtering} criteria that the result has to match
	 * @return
	 */
	PagedCollectionHolder<List<Team>> findSortedAssociatedTeams(long userId, PagingAndSorting paging,
			Filtering filtering);

	/**
	 * Will return a list of all {@link Team} that do not have the concerned
	 * {@link User} as a member <br>
	 * access restricted to admins
	 *
	 * @param userId
	 *            : the id of the concerned {@link User}
	 * @return the list of all non associated {@link Team}s
	 */
	List<Team> findAllNonAssociatedTeams(long userId);

	/**
	 * Creates a stub {@link User} using the given login and returns it.
	 *
	 * This should throw an exception when the user already exists.
	 *
	 * @return the new User
	 * @throws LoginAlreadyExistsException
	 *             when user already exists
	 */
	User createUserFromLogin(@NotNull String login);

	/**
	 * Creates a user without credentials. This should be used when
	 * authentication is managed by an external provider only.
	 *
	 * @param user
	 * @param groupId
	 */
	void createUserWithoutCredentials(User user, long groupId);

	/**
	 * Creates authentication data for given user.
	 *
	 * @param userId
	 * @param newPassword
	 * @throws LoginAlreadyExistsException
	 *             when authentication data already exixts
	 */
	void createAuthentication(long userId, String newPassword) throws LoginAlreadyExistsException;

	List<User> findAllAdminOrManager();

	List<UsersGroup> findAllUsersGroupOrderedByQualifiedName();

	/**
	 * Creates a user with the given password. This new user shall have the
	 * ROLE_ADMIN role.
	 *
	 * This method should not have any security constraint because we might need
	 * it to provision the authentication system (ie there is no user yet).
	 *
	 * @param user
	 * @param password
	 */
	User createAdministrator(User user, String password);

	/**
	 * Checks if a user already exist with the same login in the database.<br/>
	 * If so, raises a {@linkplain LoginAlreadyExistsException}
	 *
	 * The login is checked <strong>according to the state of the
	 * case-sensitivity feature</strong>.
	 *
	 * @param login
	 */
	void checkLoginAvailability(String login);

	/**
	 * Logins are considered as duplicate when they are equal without
	 * case-sensitivity.
	 *
	 * @return the list of duplicate logins / username
	 */
	List<String> findAllDuplicateLogins();

}