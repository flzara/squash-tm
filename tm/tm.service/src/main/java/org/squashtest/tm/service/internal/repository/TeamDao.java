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
import org.springframework.data.jpa.repository.Query;
import org.squashtest.tm.domain.users.Team;

import java.util.List;

/**
 * Data access methods for {@link Team}s.
 *
 * @author mpagnon
 */

public interface TeamDao extends JpaRepository<Team, Long>, CustomTeamDao {
	/**
	 * Find all teams with name equals to the given name param.
	 *
	 * @return list of team with same name as param
	 */
	List<Team> findAllByName(String name);

	/**
	 * Will count the number of Teams where the concerned user is member.
	 *
	 * @param userId : id of the concerned user
	 * @return the total number of teams associated to the user
	 */
	@Query
	long countAssociatedTeams(long userId);

	/**
	 * Will return all {@link Team}s that don't have the concerned user as a member.
	 *
	 * @param userId : the id of the concerned user
	 */
	@Query
	List<Team> findAllNonAssociatedTeams(long userId);

}
