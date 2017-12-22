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
package org.squashtest.tm.web.internal.controller.authentication;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.domain.servers.AuthenticationStatus;
import org.squashtest.tm.domain.servers.BasicAuthenticationCredentials;
import org.squashtest.tm.service.bugtracker.BugTrackersLocalService;


@Controller
@RequestMapping("/servers")
public class ThirdPartyServersAuthenticationController {

	@Inject
	private BugTrackersLocalService btService;

	/**
	 * returns information about whether the user is authenticated or not
	 */
	@RequestMapping(value="/{serverId}/authentication", method = RequestMethod.GET, produces="application/json")
	@ResponseBody
	public AuthenticationStatus getAuthenticationStatus(@PathVariable("serverId") Long serverId){

		// for now we just cheat : all servers are bugtracker servers
		return btService.checkAuthenticationStatus(serverId);
	}


	/**
	 * tries to authenticate the current user against the given server using login/password. Status 200 means success (user is authenticated),
	 * an exception means failure.
	 */
	@ResponseBody
	@RequestMapping(value = "/{serverId}/authentication", method = RequestMethod.POST, consumes="application/json")
	public
	void authenticate(@RequestBody BasicAuthenticationCredentials credentials,
			@PathVariable("serverId") long serverId) {

		btService.setCredentials(credentials.getUsername(), new String(credentials.getPassword()), serverId);

	}


}
