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
package org.squashtest.csp.core.bugtracker.service;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

import org.springframework.context.i18n.LocaleContext;
import org.springframework.scheduling.annotation.Async;
import org.squashtest.csp.core.bugtracker.core.BugTrackerNotFoundException;
import org.squashtest.csp.core.bugtracker.core.BugTrackerRemoteException;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.csp.core.bugtracker.spi.BugTrackerInterfaceDescriptor;
import org.squashtest.tm.bugtracker.advanceddomain.DelegateCommand;
import org.squashtest.tm.bugtracker.definition.Attachment;
import org.squashtest.tm.bugtracker.definition.RemoteIssue;
import org.squashtest.tm.bugtracker.definition.RemoteProject;
import org.squashtest.tm.domain.servers.Credentials;


/**
 * Service / Facade to access the bug-trackers from the rest of the application.
 *
 * @author Gregory Fouquet
 *
 */
public interface BugTrackersService {


	/**
	 * Tell if this service should be given authentication credentials before being able to perform any BT operation.
	 *@param bugTracker : the concerned BugTracker
	 * @return
	 */
	boolean isCredentialsNeeded(BugTracker bugTracker);


	/**
	 * Sets the credentials to use for bug tracker authentication using basic authentication. Once set,
	 * {@link BugTrackersService#isCredentialsNeeded(BugTracker)} should no longer be <code>false</code> unless
	 * an authentication error happens at some point. That operation is not required if the
	 * bugtracker uses {@link org.squashtest.tm.domain.servers.AuthenticationPolicy#APP_LEVEL},
	 * since the user will always be considered as authenticated.
	 *
	 * @param credentials
	 * @param bugTracker the concerned BugTracker
	 * @return nothing
	 * @throws BugTrackerRemoteException if the credentials are invalid
	 * @throws WrongAuthenticationPolicyException if the bugtracker is configured to use
	 * {@link org.squashtest.tm.domain.servers.AuthenticationPolicy#APP_LEVEL}
	 * @throws org.squashtest.csp.core.bugtracker.core.UnsupportedAuthenticationModeException if the connector does not support the protocol
	 * {@link org.squashtest.tm.domain.servers.AuthenticationProtocol#BASIC_AUTH}
	 */
	void  setCredentials(Credentials credentials, BugTracker bugTracker);


	/**
	 * Same as {@link #setCredentials(Credentials, BugTracker)}, using {@link org.squashtest.tm.domain.servers.BasicAuthenticationCredentials}
	 * behind the scene for the protocol. The connector must support such mode of authentication.
	 *
	 * @deprecated use {@link #setCredentials(Credentials, BugTracker)} instead
	 * @param username
	 * @param password
	 * @param bugTracker the concerned BugTracker
	 * @return nothing
	 * @throws BugTrackerRemoteException if the credentials are invalid
	 * @throws WrongAuthenticationPolicyException if the bugtracker is configured to use
	 * {@link org.squashtest.tm.domain.servers.AuthenticationPolicy#APP_LEVEL}
	 * @throws org.squashtest.csp.core.bugtracker.core.UnsupportedAuthenticationModeException the connector does not support
	 * {@link org.squashtest.tm.domain.servers.AuthenticationProtocol#BASIC_AUTH}
	 */
	@Deprecated
	void  setCredentials(String username, String password, BugTracker bugTracker);


	/**
	 * Will test if connector accepts and validate these credentials.
	 *
	 * @throws org.squashtest.csp.core.bugtracker.core.UnsupportedAuthenticationModeException if the credentials are of the wrong type
	 * @throws org.squashtest.csp.core.bugtracker.core.BugTrackerNoCredentialsException if the credentials are rejected by the endpoint
	 *
	 * @param credentials
	 */
	void testCredentials(BugTracker bugTracker, Credentials credentials);


	/**
	 *
	 * returns a descriptor for the interface in TM
	 * @param bugtracker the concerned BugTracker
	 * @return just what I said
	 */
	BugTrackerInterfaceDescriptor getInterfaceDescriptor(BugTracker bugtracker);


	/**
	 * returns an url like for getBugTrackerUrl. That method will build an url pointing to the issue
	 * hosted on the remote bugtracker.
	 *
	 * @param issueId the ID of an issue that should already exist on the bugtracker (i.e., fed with an ID).
	 * @param bugTracker the concerned BugTracker
	 * @return the url if success, or null if no bugtracker is defined or if malformed.
	 */
	URL getViewIssueUrl(String issueId, BugTracker bugTracker);



	/**
	 * will return a project, matching by its name
	 *	 *
	 * @param name of the project
	 * @param bugTracker the concerned BugTracker
	 * @return the project if found, shipped with all known versions, categories and users.
	 * @throws various subclasses of BugTrackerManagerException
	 */
	RemoteProject findProject(String name, BugTracker bugTracker);


	/**
	 * will return a project, matching by its id. The id we look for is the one from the bugtracker, not from Squash.
	 *
	 * @param id of the project
	 * @param bugTracker the concerned BugTracker
	 * @return the project if found, shipped with all known versions, categories and users.
	 * @throws various subclasses of BugTrackerManagerException
	 */
	RemoteProject findProjectById(String id, BugTracker bugTracker);

	/**
	 * will send an issue to the bugtracker.
	 *
	 * @param issue a squash Issue
	 * @param bugTracker the concerned BugTracker
	 * @return the newly created issue
	 *
	 */
	RemoteIssue createIssue(RemoteIssue issue, BugTracker bugTracker);



	/**
	 * given a key, returns an issue
	 *
	 * @param key
	 * @param bugTracker the concerned BugTracker
	 * @return the issue
	 * @throws BugTrackerNotFoundException
	 */
	RemoteIssue getIssue(String key, BugTracker bugTracker);


	/***
	 * <p>This method returns a list of issues corresponding to the given Squash Issue List. This method
	 * returns a future so that the caller can abort if this takes too long. Technically it is done by having the
	 * current thread enqueue a new task in the TaskExecutor, the caller can then set a time limit.</p>
	 *
	 * <p>Because the credentials are usually passed using a {@link ThreadLocalBugTrackerContextHolder}, and that the task
	 * is performed in another thread, the code being executed will not find the credentials. That's why you have to
	 * provide them explicitly here.</p>
	 *
	 * @param issueKeyList
	 *            the Squash issue key List (List<String>)
	 * @param bugTracker the concerned BugTracker
	 * @param
	 * @return A future on the corresponding BTIssue List
	 */

	@Async
	Future<List<RemoteIssue>> getIssues(Collection<String> issueKeyList, BugTracker bugTracker, BugTrackerContext context, LocaleContext localeContext);


	/**
	 * Must return ready-to-fill issue, ie with empty fields and its project configured with as many metadata as possible related to issue creation.
	 *
	 * @param projectName
	 * @param BugTracker bugTracker
	 * @return
	 */
	RemoteIssue createReportIssueTemplate(String projectName, BugTracker bugTracker);


	/**
	 * Given a remote issue key, will ask the bugtracker to attach the attachments to that issue.
	 * Note that the specified bugtracker will be used for that purpose.
	 *
	 * @param remoteIssueKey
	 * @param bugtracker
	 * @param attachments
	 */
	void forwardAttachments(String remoteIssueKey, BugTracker bugtracker, List<Attachment> attachments);

	/**
	 * forwards a {@link DelegateCommand} to a connector
	 *
	 * @param command
	 * @return
	 */
	Object forwardDelegateCommand(DelegateCommand command, BugTracker bugtracker);


	Set<String> getProviderKinds();

}
