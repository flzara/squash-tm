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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
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
import org.squashtest.tm.service.servers.OAuth1aConsumerService;
import org.squashtest.tm.service.servers.OAuth1aTemporaryTokens;
import org.squashtest.tm.web.internal.util.UriUtils;

// XSS OK - bflessel
@Controller
@RequestMapping("/servers")
public class ThirdPartyServersAuthenticationController {

	private static final String OAUTH_ERROR_PAGE = "servers/oauth1a-failure.html";
	private static final String OAUTH_SUCCESS_PAGE = "servers/oauth1a-success.html";

	private static final Logger LOGGER = LoggerFactory.getLogger(ThirdPartyServersAuthenticationController.class);

	public static final String OAUTH_1_A_TEMP_TOKENS = "squashtest.servers.OAUTH_1_A_TEMP_TOKENS";

	// the callback URL is so often overlooked that
	// we must rely on a fallback when its not set,
	// see below
	@Value("${tm.test.automation.server.callbackurl}")
	private String baseCallbackUrl;

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
	public String authenticateOauth1(HttpServletRequest request, HttpSession session, @PathVariable("serverId") long serverId){
		try{
			String callbackUrl = createCallbackUrl(request, serverId);
			OAuth1aTemporaryTokens tokens = oauth1aService.requestTemporaryToken(serverId, callbackUrl);
			session.setAttribute(OAUTH_1_A_TEMP_TOKENS, tokens);
			return "redirect:" + tokens.getRedirectUrl();
		}
		// I don't want to set up an exception handler just for this one unique situation and error page
		// so I handle it old-school style here
		catch(Exception ex){
			LOGGER.error("Exception encountered while fetching temporary credentials : ", ex);
			return OAUTH_ERROR_PAGE;
		}
	}


	@RequestMapping(value = "/{serverId}/authentication/oauth1a/callback", method = RequestMethod.GET)
	public String callbackOAuth1(HttpSession session, @PathVariable("serverId") long serverId,
								 @RequestParam("oauth_token") String oauthToken,
								 @RequestParam("oauth_verifier") String oauthVerifier){

		String view = OAUTH_SUCCESS_PAGE;

		try {
			OAuth1aTemporaryTokens tempTokens = (OAuth1aTemporaryTokens) session.getAttribute(OAUTH_1_A_TEMP_TOKENS);

			if (tempTokens == null){
				String user = findUsernameOrUndefined();
				LOGGER.error("oauth callback (user '{}', server '{}') : unexpected call to the oauth1 consumer callback, no temporary tokens found user session !", user, serverId);
				view = OAUTH_ERROR_PAGE;
			}

			if (! oauthToken.equals(tempTokens.getTempToken())){
				String user = findUsernameOrUndefined();
				LOGGER.error("oauth callback (user '{}', server '{}') : received token '{}' but expected '{}'", user, serverId, oauthToken, tempTokens.getTempToken());
				view = OAUTH_ERROR_PAGE;
			}

			else {
				tempTokens.setVerifier(oauthVerifier);
				oauth1aService.authorize(serverId, tempTokens);
				session.removeAttribute(OAUTH_1_A_TEMP_TOKENS);
			}
		}
		catch(ClassCastException ex){
			String user = findUsernameOrUndefined();
			LOGGER.error("oauth callback (user '{}', server '{}') : programmatic error, exception is ", user, serverId, ex);
			view = OAUTH_ERROR_PAGE;
		}

		return view;

	}



	// ******************* utilities *****************************


	private String findUsernameOrUndefined(){
		try{
			SecurityContext sec = SecurityContextHolder.getContext();
			String username = sec.getAuthentication().getName();
			return (username != null) ? username : "(unknown)";
		}
		catch(Exception ex){
			LOGGER.debug("attempted to retrieve the current username for debugging purposes but failed to retrieve one. "
					+ "Probable cause is that no user context is set. It is also likely that the error reported below was caused for that same reason.");
			return "(unknown)";
		}
	}


	/**
	 * Returns the official callback url if set, or extract one from the http request
	 * as a fallback
	 *
	 * @param request
	 * @return
	 */
	private String createCallbackUrl(HttpServletRequest request, long serverId){
		String base;
		if (isCallbackUrlSet()){
			base = baseCallbackUrl;
		}
		else{
			base = UriUtils.extractBaseUrl(request);
		}

		return base + "/servers/"+serverId+"/authentication/oauth1a/callback";
	}

	// callback URL is considered defined if
	// it is not blank and not referencing localhost
	private boolean isCallbackUrlSet(){
		return (!StringUtils.isBlank(baseCallbackUrl) &&
			! baseCallbackUrl.contains("localhost")
		);
	}

}
