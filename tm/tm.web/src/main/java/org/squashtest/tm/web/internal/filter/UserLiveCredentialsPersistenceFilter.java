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
package org.squashtest.tm.web.internal.filter;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;
import org.squashtest.tm.service.servers.CredentialsProvider;
import org.squashtest.tm.service.servers.UserLiveCredentials;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This filter is responsible for retrieving the {@link UserLiveCredentials} if available, making it available to the current
 * request's thread and storing it for future use at the end of the request. Note however that it does not create it : most
 * probably BugTrackerAutoconnectCallback will.
 *
 * It should be instantiated using Spring and accessed by the webapp through a DelegatingFilterProxy.
 *
 * @author Gregory Fouquet
 *
 */
public final class UserLiveCredentialsPersistenceFilter extends OncePerRequestFilter {
	private static final Logger LOGGER = LoggerFactory.getLogger(UserLiveCredentialsPersistenceFilter.class);
	/**
	 * Key used do store BT context in http session.
	 */
	public static final String BUG_TRACKER_CONTEXT_SESSION_KEY = "squashtest.bugtracker.UserLiveCredentials";

	private CredentialsProvider credentialsProvider;
	private String excludePatterns;

	/**
	 * This callback method will try to load a previously existing {@link UserLiveCredentials}, expose it to the current
	 * thread through {@link CredentialsProvider} and store it after filter chain processing.
	 */
	@Override
	public void doFilterInternal(
		HttpServletRequest request, HttpServletResponse response, FilterChain chain)
		throws ServletException, IOException {
	    String url = request.getServletPath() + StringUtils.defaultString(request.getPathInfo());

	    if (!matchExcludePatterns(url)) {

			try {
				// fetch from session
				UserLiveCredentials liveCredentials = loadFromSession(request);

				if (liveCredentials != null){
					// store in provider
					credentialsProvider.restoreLiveCredentials(liveCredentials);
				}

				chain.doFilter(request, response);
			}
			finally {
				// fetch from provider
				UserLiveCredentials liveCredentials = credentialsProvider.getLiveCredentials();

				// clean the credentials provider
				credentialsProvider.clearLiveCredentials();

				// store in session
				storeLiveCredentialsInExistingSession(request, liveCredentials);
			}
        }

        else {
            chain.doFilter(request, response);
        }
	}

	private boolean matchExcludePatterns(String url) {
	        boolean result= false;
	        if (excludePatterns != null){
	            Pattern p = Pattern.compile(excludePatterns);
	            Matcher m = p.matcher(url);
	            result = m.matches();
	        }

	        return result;
	}


	private void storeLiveCredentialsInExistingSession(ServletRequest request, UserLiveCredentials context) {
		HttpSession session = ((HttpServletRequest) request).getSession(false);

		if (session == null) {
			LOGGER.info("UserLiveCredentialsPersistenceFilter : Session was invalidated, UserLiveCredentials will not be stored");
			return;
		}

		storeLiveCredentials(session, context);
		LOGGER.debug("UserLiveCredentialsPersistenceFilter : UserLiveCredentials stored to session");
	}

	private void storeLiveCredentials(HttpSession session, UserLiveCredentials credentials) {
		if (LOGGER.isTraceEnabled()){
			LOGGER.trace("UserLiveCredentialsPersistenceFilter : storing live credentials for session #{} with credentials #{}", session.getId(), credentials.toString());
		}
		session.setAttribute(BUG_TRACKER_CONTEXT_SESSION_KEY, credentials);
	}


	/*
		May return null if none were set.
	 */
	private UserLiveCredentials loadFromSession(ServletRequest request) {
		LOGGER.debug("UserLiveCredentialsPersistenceFilter : Loading UserLiveCredentials from HTTP session");

		HttpSession session = ((HttpServletRequest) request).getSession();
		UserLiveCredentials liveCredentials = (UserLiveCredentials) session.getAttribute(BUG_TRACKER_CONTEXT_SESSION_KEY);

		if (LOGGER.isTraceEnabled()){
			if (liveCredentials != null) {
				LOGGER.trace("UserLiveCredentialsPersistenceFilter : Loading credentials for session #{} with credentials #{}", session.getId(), liveCredentials.toString());
			}
			else{
				LOGGER.trace("UserLiveCredentialsPersistenceFilter : no live credentials found for session #{}", session.getId());
			}
		}

		return liveCredentials;
	}

	public void setCredentialsProvider(CredentialsProvider credentialsProvider) {
		this.credentialsProvider = credentialsProvider;
	}

    public void setExcludePatterns(String excludePatterns) {
        this.excludePatterns = excludePatterns;
    }


}
