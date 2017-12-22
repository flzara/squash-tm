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
package org.squashtest.csp.core.bugtracker.web;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;
import org.squashtest.csp.core.bugtracker.service.BugTrackerContext;
import org.squashtest.csp.core.bugtracker.service.BugTrackerContextHolder;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.Principal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This filter is responsible for retrieving the {@link BugTrackerContext}, making it available to the current
 * request's thread and storing it for future use at the end of the request.
 *
 * It should be instantiated using Spring and accessed by the webapp through a DelegatingFilterProxy
 *
 * @author Gregory Fouquet
 *
 */
public final class BugTrackerContextPersistenceFilter extends OncePerRequestFilter {
	private static final Logger LOGGER = LoggerFactory.getLogger(BugTrackerContextPersistenceFilter.class);
	/**
	 * Key used do store BT context in http session.
	 */
	public static final String BUG_TRACKER_CONTEXT_SESSION_KEY = "squashtest.bugtracker.BugTrackerContext";

	private BugTrackerContextHolder contextHolder;
	private String excludePatterns;

	/**
	 * This callback method will try to load a previously existing {@link BugTrackerContext}, expose it to the current
	 * thread through {@link BugTrackerContextHolder} and store it after filter chain processing.
	 */
	@Override
	public void doFilterInternal(
		HttpServletRequest request, HttpServletResponse response, FilterChain chain)
		throws ServletException, IOException {
	    String url = request.getServletPath() + StringUtils.defaultString(request.getPathInfo());
        if (!matchExcludePatterns(url)) {
		BugTrackerContext context = loadContext(request);

		try {
			contextHolder.setContext(context);
			chain.doFilter(request, response);
		} finally {
			contextHolder.clearContext();
			storeContextInExistingSession(request, context);
		}   } else {
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
	private void storeContextInExistingSession(ServletRequest request, BugTrackerContext context) {
		HttpSession session = ((HttpServletRequest) request).getSession(false);

		if (session == null) {
			LOGGER.info("BugTrackerContextPersistenceFilter : Session was invalidated, BugTrackerContext will not be stored");
			return;
		}

		storeContext(session, context);
		LOGGER.debug("BugTrackerContextPersistenceFilter : BugTrackerContext stored to session");
	}

	private void storeContext(HttpSession session, BugTrackerContext context) {
		if (LOGGER.isTraceEnabled()){
			LOGGER.trace("BugTrackerContextPersistentFilter : storing context for session #{} with btcontext #{}", session.getId(), context.toString());
		}
		session.setAttribute(BUG_TRACKER_CONTEXT_SESSION_KEY, context);
	}

	private BugTrackerContext loadContext(ServletRequest request) {
		LOGGER.debug("BugTrackerContextPersistenceFilter : Loading BugTrackerContext from HTTP session");

		HttpSession session = ((HttpServletRequest) request).getSession();
		BugTrackerContext context = (BugTrackerContext) session.getAttribute(BUG_TRACKER_CONTEXT_SESSION_KEY);

		if (context == null) {
			LOGGER.info("BugTrackerContextPersistenceFilter : No BugTrackerContext available, will create it and eagerly store it in session");
			// TODO : once this module is moved into tm.web, look for the current username in the security context (fetchable with session.getAttribute("SPRINT_SECURITY_CONTEXT")) and 
			// use the constructor BugTrackerContext(String username)
			context = new BugTrackerContext();
			storeContext(session, context);
		}

		if (LOGGER.isTraceEnabled()){
			LOGGER.trace("BugTrackerContextPersistentFilter : Loading context for session #{} with btcontext #{}", session.getId(),context.toString());
		}

		return context;
	}

	public void setContextHolder(BugTrackerContextHolder contextHolder) {
		this.contextHolder = contextHolder;
	}

    public void setExcludePatterns(String excludePatterns) {
        this.excludePatterns = excludePatterns;
    }
    

}
