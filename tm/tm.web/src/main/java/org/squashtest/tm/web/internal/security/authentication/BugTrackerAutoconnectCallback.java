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
package org.squashtest.tm.web.internal.security.authentication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.core.task.TaskExecutor;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.squashtest.csp.core.bugtracker.core.BugTrackerRemoteException;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.csp.core.bugtracker.net.AuthenticationCredentials;
import org.squashtest.csp.core.bugtracker.service.BugTrackerContext;
import org.squashtest.csp.core.bugtracker.service.BugTrackerContextHolder;
import org.squashtest.csp.core.bugtracker.web.BugTrackerContextPersistenceFilter;
import org.squashtest.tm.domain.IdentifiedUtil;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.servers.AuthenticationPolicy;
import org.squashtest.tm.domain.servers.BasicAuthenticationCredentials;
import org.squashtest.tm.domain.servers.Credentials;
import org.squashtest.tm.service.bugtracker.BugTrackerFinderService;
import org.squashtest.tm.service.bugtracker.BugTrackersLocalService;
import org.squashtest.tm.service.project.ProjectFinder;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;
import java.util.List;

/*
 *
 * Warning : its job partly overlaps the one of BugTrackerContextPersistenceFilter because
 * it creates a BugtrackerContext.
 *
 * If you really want to know the reason is that when the hook is invoked we're still in the security chain,
 * not in the regular filter chain. So the context does not exist yet, therefore there is no place to store the
 * credentials even if the bugtracker auto auth is a success.
 *
 * This class was retro-fitted as an app listener in v1.13.0
 */
@Component
public class BugTrackerAutoconnectCallback implements ApplicationListener<InteractiveAuthenticationSuccessEvent> {

	private static final Logger LOGGER = LoggerFactory.getLogger(BugTrackerAutoconnectCallback.class);

	@Inject
	private BugTrackersLocalService bugTrackersLocalService;

	@Inject
	private ProjectFinder projectFinder;

	@Inject
	private BugTrackerFinderService bugTrackerFinder;

	@Inject
	private BugTrackerContextHolder contextHolder;

@Inject
	private TaskExecutor taskExecutor;

	private void onLoginSuccess(String username, String password, HttpSession session) {
		if (taskExecutor == null) {
			//skip if we cannot perform the operation asynchronously
			LOGGER.info("BugTrackerAutoconnectCallback : Threadpool service not ready. Skipping autologging.");
		} else if (bugTrackersLocalService == null) {
			//skip if the required service is not up yet
			LOGGER.info("BugTrackerAutoconnectCallback : no bugtracker available (service not ready yet). Skipping autologging.");
		} else {
			//let's do it.
			LOGGER.info("BugTrackerAutoconnectCallback : Autologging against known bugtrackers...");
			//creation of AsynchronousBugTrackerAutoconnect in this thread.
			Credentials credentials = new BasicAuthenticationCredentials(username, password.toCharArray());
			Runnable autoconnector = new AsynchronousBugTrackerAutoconnect(username, credentials, session);
			taskExecutor.execute(autoconnector);
		}
	}

	@Override
	public void onApplicationEvent(InteractiveAuthenticationSuccessEvent event) {
		try {
			String login = event.getAuthentication().getName();
			String password = (String) event.getAuthentication().getCredentials();
			HttpSession session = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getSession();
			onLoginSuccess(login, password, session);
		} catch (ClassCastException ex) {
			// Such errors should not break the app flow
			LOGGER.warn("BugTrackerAutoconnectCallback : The following exception was caught and ignored in BT autoconnector : {}. It does not prevent Squash from working, yet it is probably a bug.", ex.getMessage(), ex);
		}
	}


	private class AsynchronousBugTrackerAutoconnect implements Runnable {

		private final String user;
		private final Credentials credentials;
		private final HttpSession session;
		private final SecurityContext secContext;


		public AsynchronousBugTrackerAutoconnect(String user, Credentials credentials, HttpSession session) {
			super();
			this.user = user;
			this.credentials = credentials;
			this.session = session;
			//As Spring SecurityContext is ThreadLocal by default, we must get the main thread SecurityContext
			//and get a local reference pointing to this SecurityContext
			//Take care that SecurityContext have been correctly created and initialized by Spring Security
			//or you will have a race condition between spring security thread and this new thread
			//See Issue 6085.
			this.secContext = SecurityContextHolder.getContext();
		}


		@Override
		public void run() {

			BugTrackerContext newContext = new BugTrackerContext(user);

			//Setting the SecurityContext in the new thread with a reference to the original one.
			SecurityContextHolder.setContext(secContext);
			contextHolder.setContext(newContext);

			try{
				List<BugTracker> bugTrackers = findBugTrackers();

				for (BugTracker bugTracker : bugTrackers) {
					try {

						if (bugTracker.getAuthenticationPolicy() == AuthenticationPolicy.APP_LEVEL){
							LOGGER.debug("BugTrackerAutoconnectCallback : bugtracker {} uses the app-level authentication policy, credentials are assumed set and correct", bugTracker.getName());
						}
						else {
							LOGGER.debug("BugTrackerAutoconnectCallback : try connexion of bug-tracker : {}", bugTracker.getName());
							bugTrackersLocalService.setCredentials(credentials, bugTracker);
							// if success, store the credential in context
							LOGGER.debug("BugTrackerAutoconnectCallback : add credentials for bug-tracker : {}", bugTracker.getName());
							newContext.setCredentials(bugTracker, credentials);
						}
					} catch (BugTrackerRemoteException ex) {
						LOGGER.info("BugTrackerAutoconnectCallback : Failed to connect user '{}' to the bugtracker {} with the supplied credentials. User will have to connect manually.", user, bugTracker.getName());
						LOGGER.debug("BugTrackerAutoconnectCallback : Bugtracker autoconnector threw this exception : {}", ex.getMessage(), ex);
					}
				}

				// store context into session
				mergeIntoSession(newContext);
			}
			finally{
				contextHolder.clearContext();
			}

		}

		private List<BugTracker> findBugTrackers() {
			List<Project> readableProjects = projectFinder.findAllReadable();
			List<Long> projectIds = IdentifiedUtil.extractIds(readableProjects);
			return bugTrackerFinder.findDistinctBugTrackersForProjects(projectIds);
		}

		//This method deals with the (rare) case where the operation took so long that another request had created the bugtracker context in the mean time.
		//In this case, the data already present has precedence.
		private void mergeIntoSession(BugTrackerContext newContext) {

			BugTrackerContext existingContext = (BugTrackerContext) session.getAttribute(BugTrackerContextPersistenceFilter.BUG_TRACKER_CONTEXT_SESSION_KEY);

			if (existingContext == null) {
				//if no existing context was found the newContext is entirely stored
				session.setAttribute(BugTrackerContextPersistenceFilter.BUG_TRACKER_CONTEXT_SESSION_KEY, newContext);
				LOGGER.trace("BugTrackerAutoconnectCallback : storing into session #{} new context #{}", session.getId(),newContext.toString());
			} else {
				existingContext.absorb(newContext);
				LOGGER.trace("BugTrackerAutoconnectCallback : done merging into session #{} and context #{}", session.getId(),existingContext.toString());
			}

			//I don't understand why we put new content in session ? new content has been merged into existing content... why override it ?
			session.setAttribute(BugTrackerContextPersistenceFilter.BUG_TRACKER_CONTEXT_SESSION_KEY, newContext);
			LOGGER.debug("BugTrackerAutoconnectCallback : BugTrackerContext stored to session");
		}


	}


}
