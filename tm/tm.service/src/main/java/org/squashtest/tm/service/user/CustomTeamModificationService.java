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

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.users.Team;
import org.squashtest.tm.exception.NameAlreadyInUseException;

/**
 * Non dynamically generated methods for {@link Team} Modification
 * @author mpagnon
 *
 */
@Transactional
public interface CustomTeamModificationService extends CustomTeamFinderService{
	
	/**
	 * Check if name is available and persist the new Team.
	 * 
	 * @param team
	 * @throws NameAlreadyInUseException if a team of the same name as the team param already exists in database
	 */
	void persist(Team team);
	
	/**
	 * Delete the team along with all it's acls.
	 * Do not delete it's associated users.
	 * 
	 */
	void deleteTeam(long teamId);
	
	/**
	 * Delete the team along with all it's acls. Do not delete it's associated users.
	 * 
	 */
	void deleteTeam(List<Long> teamId);

	/**
	 * Changes the name the team of the given id.
	 * 
	 * @param teamId
	 * @param name
	 * @throws NameAlreadyInUseException
	 *             if a team of the same name already exists in database
	 */
	void changeName(long teamId, String name);

	
	/**
	 * Add one member, identified by its login
	 * 
	 * @param teamId
	 * @param login
	 */
	void addMember(long teamId, String login);
	
	
	/**
	 * Add one or more members, identified by their login, to a team identified by its id.
	 * 
	 * @param teamId
	 * @param logins
	 */
	void addMembers(long teamId, List<String> logins);
	
	
	void removeMember(long teamId, long memberId);
	
	void removeMembers(long teamId, List<Long> memberIds);

	void removeMemberFromAllTeams(long memberId);

}
