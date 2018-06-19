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
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.squashtest.tm.service.servers.CredentialsProvider;
import org.squashtest.tm.service.servers.UserCredentialsCache;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This filter is responsible for retrieving the {@link UserCredentialsCache} if available, making it available to the current
 * request's thread and storing it for future use at the end of the request. Note however that it does not create it : most
 * probably BugTrackerAutoconnectCallback will.
 *
 * It should be instantiated using Spring and accessed by the webapp through a DelegatingFilterProxy.
 *
 * @author Gregory Fouquet
 *
 */
public final class UserCredentialsCachePersistenceFilter extends OncePerRequestFilter {
	private static final Logger LOGGER = LoggerFactory.getLogger(UserCredentialsCachePersistenceFilter.class);
	/**
	 * Key used do store BT context in http session.
	 */
	public static final String CREDENTIALS_CACHE_SESSION_KEY = "squashtest.servers.UserCredentialsCache";

	private CredentialsProvider credentialsProvider;

	private final List<String> excludePatterns = new ArrayList<>();

	private final PathMatcher pathMatcher = new AntPathMatcher();

	/**
	 * This callback method will try to load a previously existing {@link UserCredentialsCache}, expose it to the current
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
				UserCredentialsCache credentialsCache= loadFromSession(request);

				if (credentialsCache != null){
					// store in provider
					credentialsProvider.restoreCache(credentialsCache);
				}

				chain.doFilter(request, response);
			}
			finally {
				// fetch from provider
				UserCredentialsCache credentialsCache = credentialsProvider.getCache();

				// clean the credentials provider
				credentialsProvider.unloadCache();

				// store in session (null guarding just in case but not supposed to happen)
				if (credentialsCache != null) {
					storeCredentialsCacheInExistingSession(request, credentialsCache);
				}
			}
        }

        else {
            chain.doFilter(request, response);
        }
	}

	private boolean matchExcludePatterns(String url) {
	        boolean match = false;

	        for (String pattern : excludePatterns){
	        	if (pathMatcher.match(pattern, url)){
	        		match = true;
	        		break;
				}
			}

	        return match;
	}


	private void storeCredentialsCacheInExistingSession(ServletRequest request, UserCredentialsCache context) {
		HttpSession session = ((HttpServletRequest) request).getSession(false);

		if (session == null) {
			LOGGER.info("UserCredentialsCachePersistenceFilter : Session was invalidated, UserCredentialsCache will not be stored");
			return;
		}

		storeCredentialsCache(session, context);
		LOGGER.debug("UserCredentialsCachePersistenceFilter : UserCredentialsCache stored to session");
	}

	private void storeCredentialsCache(HttpSession session, UserCredentialsCache credentials) {
		if (LOGGER.isTraceEnabled()){
			LOGGER.trace("UserCredentialsCachePersistenceFilter : storing credentials cache for session #{} with credentials #{}", session.getId(), credentials.toString());
		}
		session.setAttribute(CREDENTIALS_CACHE_SESSION_KEY, credentials);
	}


	/*
		May return null if none were set.
	 */
	private UserCredentialsCache loadFromSession(ServletRequest request) {
		LOGGER.debug("UserCredentialsCachePersistenceFilter : Loading UserCredentialsCache from HTTP session");

		HttpSession session = ((HttpServletRequest) request).getSession();
		UserCredentialsCache credentialsCache = (UserCredentialsCache) session.getAttribute(CREDENTIALS_CACHE_SESSION_KEY);

		if (LOGGER.isTraceEnabled()){
			if (credentialsCache != null) {
				LOGGER.trace("UserCredentialsCachePersistenceFilter : Loading credentials for session #{} with credentials #{}", session.getId(), credentialsCache.toString());
			}
			else{
				LOGGER.trace("UserCredentialsCachePersistenceFilter : no credentials cache found for session #{}", session.getId());
			}
		}

		return credentialsCache;
	}

	public void setCredentialsProvider(CredentialsProvider credentialsProvider) {
		this.credentialsProvider = credentialsProvider;
	}

	/**
	 * Adds an exclusion pattern. Pattern style is Ant style.
	 *
	 * @param antPatterns
	 */
    public UserCredentialsCachePersistenceFilter addExcludePatterns(String... antPatterns) {
        this.excludePatterns.addAll(Arrays.asList(antPatterns));
        return this;
    }


}
