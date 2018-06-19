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

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Scope;
import org.springframework.core.task.TaskExecutor;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.squashtest.csp.core.bugtracker.core.BugTrackerNoCredentialsException;
import org.squashtest.csp.core.bugtracker.core.UnsupportedAuthenticationModeException;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.tm.domain.IdentifiedUtil;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.servers.AuthenticationPolicy;
import org.squashtest.tm.domain.servers.AuthenticationProtocol;
import org.squashtest.tm.domain.servers.BasicAuthenticationCredentials;
import org.squashtest.tm.domain.servers.Credentials;
import org.squashtest.tm.service.servers.CredentialsProvider;
import org.squashtest.tm.service.servers.UserCredentialsCache;
import org.squashtest.tm.service.bugtracker.BugTrackerFinderService;
import org.squashtest.tm.service.bugtracker.BugTrackersLocalService;
import org.squashtest.tm.service.project.ProjectFinder;
import org.squashtest.tm.web.internal.filter.UserCredentialsCachePersistenceFilter;

import java.util.List;
import java.util.Optional;


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
 * a UserCredentialsCache and store it into session, then proceed an asynchronous job that will test each known
 * credentials against the bugtrackers.
 *
 * This class was retro-fitted as an app listener in v1.13.0
 */
@Component
public class BugTrackerAutoconnectCallback implements ApplicationListener<InteractiveAuthenticationSuccessEvent> {

	private static final Logger LOGGER = LoggerFactory.getLogger(BugTrackerAutoconnectCallback.class);

	@Inject
	private TaskExecutor taskExecutor;

	@Inject
	private CredentialsProvider credentialsProvider;

	@Inject
	private Provider<AsynchronousBugTrackerAutoconnect> asyncProvider;



	@Override
	public void onApplicationEvent(InteractiveAuthenticationSuccessEvent event) {

		Authentication authentication = event.getAuthentication();
		HttpSession session = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getSession();

		// initialize the credentials cache
		UserCredentialsCache credentialsCache = initializeCredentialsCache(authentication.getName(), session);

		// start the autoconnection tester thread
		scheduleAutoconnection(authentication, credentialsCache);

	}

	// will create the user credentials and store them into the session, and the credentials provider
	private UserCredentialsCache initializeCredentialsCache(String username, HttpSession session){

		LOGGER.debug("BugTrackerAutoconnectCallback : initializing the credentials cache");

		UserCredentialsCache credentials = new UserCredentialsCache(username);

		session.setAttribute(UserCredentialsCachePersistenceFilter.CREDENTIALS_CACHE_SESSION_KEY, credentials);
		credentialsProvider.restoreCache(credentials);

		return credentials;

	}

	private void scheduleAutoconnection(Authentication authentication, UserCredentialsCache credentialsCache){

		LOGGER.debug("BugTrackerAutoconnectCallback : scheduling autoconnection for user '{}'", credentialsCache.getUser());

		AsynchronousBugTrackerAutoconnect autoconnector = asyncProvider.get();
		autoconnector.setUser(authentication.getName());
		autoconnector.setSpringsecCredentials(authentication.getCredentials());
		autoconnector.setCredentialsCache(credentialsCache);

		taskExecutor.execute(autoconnector);

	}



	// ********************* private worker class ******************************


	@Component
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	static class AsynchronousBugTrackerAutoconnect implements Runnable {

		// set by setters
		private String user;
		private Object springsecCredentials;
		private UserCredentialsCache credentialsCache;

		// set by the constructor
		final private SecurityContext secContext;

		// set by injection
		@Inject
		private BugTrackersLocalService bugTrackersLocalService;

		@Inject
		private ProjectFinder projectFinder;

		@Inject
		private BugTrackerFinderService bugTrackerFinder;

		@Inject
		private CredentialsProvider credentialsProvider;

		public AsynchronousBugTrackerAutoconnect(){
			//As Spring SecurityContext is ThreadLocal by default, we must get the main thread SecurityContext
			//and get a local reference pointing to this SecurityContext
			//Take care that SecurityContext have been correctly created and initialized by Spring Security
			//or you will have a race condition between spring security thread and this new thread
			//See Issue 6085.
			this.secContext = SecurityContextHolder.getContext();
		}


		/*
			XXX concurrency concern : the cache instance 'credentialsCache' is running concurrently in the main
			use thread and in this one. I doubt it will fail horribly though : concurrency only happens between at most
			two threads which essentially belong to the same user, might happen for a brief time only, and consequences
			of collision won't go farther than wrong label displayed in the UI.
		*/
		@Override
		public void run(){

			// restore the user context
			SecurityContextHolder.setContext(secContext);

			// restore the credentials cache
			credentialsProvider.restoreCache(credentialsCache);

			try{

				List<BugTracker> servers = findBugTrackers();

				for (BugTracker server: servers){

					try{
						LOGGER.debug("BugTrackerAutoconnectCallback : attempting authentication on server '{}'", server.getName());
						attemptAuthentication(server);
					}
					catch(BugTrackerNoCredentialsException | UnsupportedAuthenticationModeException ex){
						LOGGER.debug("BugTrackerAutoconnectCallback : Failed to connect user '{}' to the bugtracker {} with the supplied credentials. User will have to connect manually.", user, server);
						LOGGER.debug("BugTrackerAutoconnectCallback : original exception is : ", ex);
					}
					catch(Exception genericException){
						LOGGER.error("BugTrackerAutoconnectCallback : an unexpected error happened :", genericException);
					}

				}

			}
			finally{
				LOGGER.debug("BugTrackerAutoconnectCallback : completed autoconnection for user '{}'", credentialsCache.getUser());
				// clear the credentials from that thread.
				credentialsProvider.unloadCache();
			}

		}


		private void attemptAuthentication(BugTracker server) {

			AuthenticationPolicy policy = server.getAuthenticationPolicy();
			AuthenticationProtocol protocol = server.getAuthenticationProtocol();

			LOGGER.trace("server '{}' is set to authentication policy '{}' and protocol '{}'", policy, protocol);

			// find testable credentials if possible
			Credentials credentials = fetchCredentialsOrNull(server);

			// are the credentials absent ?
			if (credentials == null){
				LOGGER.debug("BugTrackerAutoconnectCallback : could not find suitable credentials, skipping");
			}
			// else attempt authentication
			else{

				// warn if credentials of unexpected type
				warnIfCredentialsOfWrongType(credentials, protocol);

				bugTrackersLocalService.validateCredentials(server, credentials, true);

				LOGGER.debug("BugTrackerAutoconnectCallback : credentials successfully tested for server '{}'", server.getName());
			}

		}



		private Credentials fetchCredentialsOrNull(BugTracker server){

			Credentials credentials = null;

			AuthenticationPolicy policy = server.getAuthenticationPolicy();

			// attempt to load from the provider
			Optional<Credentials> maybeCredentials = null;
			if (policy == AuthenticationPolicy.USER){
				maybeCredentials = credentialsProvider.getCredentials(server);
			}
			else{
				maybeCredentials = credentialsProvider.getAppLevelCredentials(server);
			}


			// use the credentials if present, or try fallback
			if (maybeCredentials.isPresent()){
				LOGGER.debug("BugTrackerAutoconnectCallback : found credentials from the provider");
				credentials = maybeCredentials.get();
			}
			else if (canTryUsingEvent(server)){
				LOGGER.debug("BugTrackerAutoconnectCallback : can create the credentials using the authentication event");
				credentials = buildFromAuthenticationEvent();
			}

			return credentials;
		}


		private List<BugTracker> findBugTrackers() {
			List<Project> readableProjects = projectFinder.findAllReadable();
			List<Long> projectIds = IdentifiedUtil.extractIds(readableProjects);
			return bugTrackerFinder.findDistinctBugTrackersForProjects(projectIds);
		}


		// for now we assume that only String credentials are suitable (as passwords),
		// and the server is set to auth policy USER
		private boolean canTryUsingEvent(BugTracker server){
			return (server.getAuthenticationPolicy() == AuthenticationPolicy.USER && 
					server.getAuthenticationProtocol() == AuthenticationProtocol.BASIC_AUTH &&
					springsecCredentials instanceof String);
		}

		// for now we assume that BasicAuthentication is what we need
		// the cast is safe thanks to canTryUsingEvent
		private Credentials buildFromAuthenticationEvent(){
			return new BasicAuthenticationCredentials(user, (String)springsecCredentials);
		}


		private void warnIfCredentialsOfWrongType(Credentials credentials, AuthenticationProtocol protocol){
			AuthenticationProtocol credsProtocol = credentials.getImplementedProtocol();
			if (credsProtocol != protocol){
				LOGGER.warn("BugTrackerAutoconnectCallback : attempting autoconnection with credentials for protocol '{}' "
						+ "while the configuration states that the (preferred) protocol is  '{}'. That doesn't mean the "
						+ "current credentials won't work (the connector still supports them) but this warning hints "
						+ "that they might be obsolete regarding your new preferred protocol.", credsProtocol, protocol);
			}
		}

		// ********** setters ********************

		public void setUser(String user) {
			this.user = user;
		}

		public void setSpringsecCredentials(Object springsecCredentials) {
			this.springsecCredentials = springsecCredentials;
		}

		public void setCredentialsCache(UserCredentialsCache credentialsCache) {
			this.credentialsCache = credentialsCache;
		}


	}

}
