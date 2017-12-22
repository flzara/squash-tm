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

import org.squashtest.tm.core.foundation.collection.Filtering;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.users.Team;
import org.squashtest.tm.domain.users.User;

import java.util.List;

public interface CustomTeamDao {
	List<Team> findSortedTeams(PagingAndSorting paging, Filtering filtering);

	/**
	 * Will find {@link Team}s associated to concerned user organized with the given pagin and filtering params.
	 *
	 * @param userId
	 *            : id of the concerned {@link User}
	 * @param paging
	 *            : {@link PagingAndSorting} according to which result will be organized.
	 * @param filtering
	 *            : {@link Filtering} according to which result will be organized.
	 * @return paged and filtered list of {@link Team}s associated to concerned {@link User}
	 */
	List<Team> findSortedAssociatedTeams(long userId, PagingAndSorting paging, Filtering filtering);

	/**
	 * Get the ids of the {@link org.squashtest.tm.domain.users.Team} witch include the {@link User} designed by th id
	 * @param userId id of the {@link User}
	 * @return List of the {@link org.squashtest.tm.domain.users.Team} or empty list
	 */
	List<Long> findTeamIds(Long userId);

}
