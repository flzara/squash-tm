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

import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.core.task.TaskExecutor;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.squashtest.csp.core.bugtracker.core.BugTrackerRemoteException;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.tm.service.servers.CredentialsProvider;
import org.squashtest.tm.service.servers.UserLiveCredentials;
import org.squashtest.tm.domain.IdentifiedUtil;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.servers.AuthenticationPolicy;
import org.squashtest.tm.domain.servers.BasicAuthenticationCredentials;
import org.squashtest.tm.domain.servers.Credentials;
import org.squashtest.tm.service.bugtracker.BugTrackerFinderService;
import org.squashtest.tm.service.bugtracker.BugTrackersLocalService;
import org.squashtest.tm.service.project.ProjectFinder;
import org.squashtest.tm.web.internal.filter.UserLiveCredentialsPersistenceFilter;


/**
 * Provides with a pseudo-SSO that will pre-authenticate the user on each of the known bugtrackers.
 * In some cases we do so by reusing its credentials after successful authentication.
 * But arguably this is a terrible way to do it.
 *
 * @author bsiri
 * @since the dinosaurs
 */

/*
 * As an authentication event listener this class is called while still in the security filter chain (ie before the
 * application filter chain). It is invoked every time a user authenticates. Its first task is to create
 * a UserLiveCredentials and store it into session, then proceed an asynchronous job that will test each known
 * credentials against the bugtrackers.
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
	private CredentialsProvider credentialsProvider;

	@Inject
	private TaskExecutor taskExecutor;


	@Override
	public void onApplicationEvent(InteractiveAuthenticationSuccessEvent event) {

		Authentication authentication = event.getAuthentication();
		HttpSession session = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getSession();

		// initialize the live credentials
		UserLiveCredentials liveCredentials = initializeLiveCredentials(authentication.getName(), session);

		// start the autoconnection tester thread
		runAutoconnect(authentication, liveCredentials);

	}

	// will create the user credentials and store them into the session, and the credentials provider
	private UserLiveCredentials initializeLiveCredentials(String username, HttpSession session){
		LOGGER.debug("BugTrackerAutoconnectCallback : initializing the live credentials");

		UserLiveCredentials credentials = new UserLiveCredentials(username);

		session.setAttribute(UserLiveCredentialsPersistenceFilter.BUG_TRACKER_CONTEXT_SESSION_KEY, credentials);
		credentialsProvider.restoreLiveCredentials(credentials);

		return credentials;
	}

	private void runAutoconnect(Authentication authentication, UserLiveCredentials userLiveCredentials){
		LOGGER.debug("BugTrackerAutoconnectCallback : scheduling autologging");

		Runnable autoconnector = new AsynchronousBugTrackerAutoconnect(authentication.getName(), authentication.getCredentials(), userLiveCredentials);
		taskExecutor.execute(autoconnector);
	}



	// ********************* private worker class ******************************


	private class AsynchronousBugTrackerAutoconnect implements Runnable {

		private final String user;
		private final Object springsecCredentials;
		private final UserLiveCredentials userLiveCredentials;
		private final SecurityContext secContext;


		public AsynchronousBugTrackerAutoconnect(String user, Object springsecCredentials, UserLiveCredentials userLiveCredentials) {
			super();
			this.user = user;
			this.springsecCredentials = springsecCredentials;
			this.userLiveCredentials = userLiveCredentials;
			//As Spring SecurityContext is ThreadLocal by default, we must get the main thread SecurityContext
			//and get a local reference pointing to this SecurityContext
			//Take care that SecurityContext have been correctly created and initialized by Spring Security
			//or you will have a race condition between spring security thread and this new thread
			//See Issue 6085.
			this.secContext = SecurityContextHolder.getContext();
		}


		@Override
		public void run() {

			UserLiveCredentials newLiveCredentials = new UserLiveCredentials(user);

			//Setting the SecurityContext in the new thread with a reference to the original one.
			SecurityContextHolder.setContext(secContext);
			credentialsProvider.restoreLiveCredentials(newLiveCredentials);

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
							newLiveCredentials.setCredentials(bugTracker, credentials);
						}
					} catch (BugTrackerRemoteException ex) {
						LOGGER.info("BugTrackerAutoconnectCallback : Failed to connect user '{}' to the bugtracker {} with the supplied credentials. User will have to connect manually.", user, bugTracker);
						LOGGER.debug("BugTrackerAutoconnectCallback : Bugtracker autoconnector threw this exception : {}", ex.getMessage(), ex);
					}
				}

				// merge the live credentials
				mergeIntoSession(newLiveCredentials);
			}
			finally{
				// clear the credentials from that thread.
				credentialsProvider.clearLiveCredentials();
			}

		}

		private List<BugTracker> findBugTrackers() {
			List<Project> readableProjects = projectFinder.findAllReadable();
			List<Long> projectIds = IdentifiedUtil.extractIds(readableProjects);
			return bugTrackerFinder.findDistinctBugTrackersForProjects(projectIds);
		}

		//This method deals with the (rare) case where the operation took so long that another request had created the bugtracker context in the mean time.
		//In this case, the data already present has precedence.
		private void mergeIntoSession(UserLiveCredentials newCredentials) {

			UserLiveCredentials existingCredentials = (UserLiveCredentials) session.getAttribute(UserLiveCredentialsPersistenceFilter.BUG_TRACKER_CONTEXT_SESSION_KEY);

			if (existingCredentials == null) {
				//if no existing context was found the newContext is entirely stored
				session.setAttribute(UserLiveCredentialsPersistenceFilter.BUG_TRACKER_CONTEXT_SESSION_KEY, newCredentials);
				LOGGER.trace("BugTrackerAutoconnectCallback : storing into session #{} new context #{}", session.getId(),newCredentials.toString());
			} else {
				existingCredentials.absorb(newCredentials);
				LOGGER.trace("BugTrackerAutoconnectCallback : done merging into session #{} and context #{}", session.getId(),existingCredentials.toString());
			}

			//I don't understand why we put new content in session ? new content has been merged into existing content... why override it ?
			// A: Fair point.
			session.setAttribute(UserLiveCredentialsPersistenceFilter.BUG_TRACKER_CONTEXT_SESSION_KEY, newCredentials);
			LOGGER.debug("BugTrackerAutoconnectCallback : UserLiveCredentials stored to session");
		}


	}


}
