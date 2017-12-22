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
package org.squashtest.tm.service.internal.dto;

import java.util.ArrayList;
import java.util.List;

public class UserDto {
	private String username;
	private Long userId;
	private List<Long> teamIds = new ArrayList<>();
	private boolean isAdmin;

	public UserDto(String username, Long userId, List<Long> teamIds, boolean isAdmin) {
		this.username = username;
		this.userId = userId;
		this.teamIds = teamIds;
		this.isAdmin = isAdmin;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public List<Long> getTeamIds() {
		return teamIds;
	}

	public void setTeamIds(List<Long> teamIds) {
		this.teamIds = teamIds;
	}

	/*
	 * Give the list of of party ids for this user, including it's own id and teams ids
	 */
	public List<Long> getPartyIds(){
		List<Long> partyIds = new ArrayList<>(teamIds);
		partyIds.add(userId);
		return partyIds;
	}

	public boolean isAdmin() {
		return isAdmin;
	}

	public boolean isNotAdmin() {
		return !isAdmin;
	}


	public void setAdmin(boolean admin) {
		isAdmin = admin;
	}
}

