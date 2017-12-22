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
package org.squashtest.tm.service.internal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.service.annotation.EmptyCollectionGuard;

import java.util.List;

public interface UserDao extends JpaRepository<User, Long>, CustomUserDao {

	@Query
	List<User> findAllUsersOrderedByLogin();

	@Query
	List<User> findAllActiveUsersOrderedByLogin();

	@Query
	User findUserByLogin(@Param("userLogin") String login);

	@Query
	@EmptyCollectionGuard
	List<User> findUsersByLoginList(@Param("logins") List<String> loginList);

	@Query
	List<User> findAllNonTeamMembers(@Param("teamId") long teamId);


	@Query
	int countAllTeamMembers(@Param("teamId") long teamId);

	/**
	 * Finds a user by her login using case-insensitive search
	 * @param login
	 * @return
	 */
	@Query
	User findUserByCiLogin(@Param("userLogin") String login);

	@Query
	@Modifying
	void unassignFromAllCampaignTestPlan(@Param("userId") long userId);

	@Query
	@Modifying
	void unassignFromAllIterationTestPlan(@Param("userId") long userId);
}
