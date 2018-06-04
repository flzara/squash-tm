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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.core.foundation.exception.ActionException;
import org.squashtest.tm.domain.servers.AuthenticationStatus;
import org.squashtest.tm.domain.servers.BasicAuthenticationCredentials;
import org.squashtest.tm.service.bugtracker.BugTrackersLocalService;
import org.squashtest.tm.service.servers.OAuth1aConsumerService;
import org.squashtest.tm.service.servers.OAuth1aTemporaryTokens;

// XSS OK - bflessel
@Controller
@RequestMapping("/servers")
public class ThirdPartyServersAuthenticationController {

	public static final String OAUTH_1_A_TEMP_TOKENS = "squashtest.servers.OAUTH_1_A_TEMP_TOKENS";
	@Inject
	private BugTrackersLocalService btService;

	@Inject
	private OAuth1aConsumerService oauth1aService;

	/**
	 * returns information about whether the user is authenticated or not
	 */
	@RequestMapping(value="/{serverId}/authentication", method = RequestMethod.GET, produces="application/json")
	@ResponseBody
	public AuthenticationStatus getAuthenticationStatus(@PathVariable("serverId") Long serverId){

		// for now we just cheat : all servers are bugtracker servers
		return btService.checkAuthenticationStatus(serverId);
	}

	// ********** Basic Authentication ************************

	/**
	 * tries to authenticate the current user against the given server using login/password. Status 200 means success (user is authenticated),
	 * an exception means failure.
	 */
	@ResponseBody
	@RequestMapping(value = "/{serverId}/authentication", method = RequestMethod.POST, consumes="application/json")
	public
	void authenticate(@RequestBody BasicAuthenticationCredentials credentials,
			@PathVariable("serverId") long serverId) {


		btService.validateCredentials(serverId, credentials, false);

	}

	// ********* OAuth1a authentication ************************


	/*
	 * here the HTTP method is 'GET', because we want to open a window for user authorization (instead of redirecting the current page)
	 */
	@RequestMapping(value = "/{serverId}/authentication/oauth1a", method = RequestMethod.GET)
	public String authenticateOauth1(HttpSession session, @PathVariable("serverId") long serverId){
		OAuth1aTemporaryTokens tokens = oauth1aService.requestTemporaryToken(serverId, "http://localhost:8080/squash/servers/"+serverId+"/authentication/oauth1a/callback");
		session.setAttribute(OAUTH_1_A_TEMP_TOKENS, tokens);
		return "redirect:" + tokens.getRedirectUrl();
	}


	@RequestMapping(value = "/{serverId}/authentication/oauth1a/callback", method = RequestMethod.GET)
	@ResponseBody
	public String callbackOAuth1(HttpSession session, @PathVariable("serverId") long serverId,
								 @RequestParam("oauth_token") String oauthToken,
								 @RequestParam("oauth_verifier") String oauthVerifier){

		String response="completed !";

		try {
			OAuth1aTemporaryTokens tempTokens = (OAuth1aTemporaryTokens) session.getAttribute(OAUTH_1_A_TEMP_TOKENS);

			if (tempTokens == null){
				response = "unexpected call to the oauth1 consumer callback, no temporary tokens found user session !";
			}

			if (! oauthToken.equals(tempTokens.getTempToken())){
				response = "wtf, received a oauth callback for server "+serverId+" but received token "+oauthToken+" while " +
							   "expecting "+tempTokens.getTempToken();
			}

			else {
				tempTokens.setVerifier(oauthVerifier);
				oauth1aService.authorize(serverId, tempTokens);
				session.removeAttribute(OAUTH_1_A_TEMP_TOKENS);
			}
		}
		catch(ClassCastException ex){
			response = "unexpected call to the oauth1 consumer callback, no temporary tokens found user session !";
		}

		return response;

	}



}
