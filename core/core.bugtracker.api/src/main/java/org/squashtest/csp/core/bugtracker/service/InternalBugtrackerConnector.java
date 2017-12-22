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

import org.squashtest.csp.core.bugtracker.core.*;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.csp.core.bugtracker.net.AuthenticationCredentials;
import org.squashtest.csp.core.bugtracker.spi.BugTrackerInterfaceDescriptor;
import org.squashtest.csp.core.bugtracker.spi.BugtrackerConnectorBase;
import org.squashtest.tm.bugtracker.advanceddomain.DelegateCommand;
import org.squashtest.tm.bugtracker.definition.Attachment;
import org.squashtest.tm.bugtracker.definition.RemoteIssue;
import org.squashtest.tm.bugtracker.definition.RemoteProject;
import org.squashtest.tm.domain.servers.AuthenticationProtocol;
import org.squashtest.tm.domain.servers.Credentials;

/**
 * This interface declares how to wrap the various bugtracker connector types (simple, advanced and oslc) in a unified
 * set of methods that Squash will use for its internal needs.
 *
 */
public interface InternalBugtrackerConnector {


	/**
	 * Declares which authentication protocols are supported by this BugTrackerConnector.
	 * Default implementation returns [AuthenticationMode.USERNAME_PASSWORD]
	 *
	 * @return
	 */
	AuthenticationProtocol[] getSupportedAuthProtocols();

	/**
	 * Declares whether the given connector supports a given connection protocol.
	 *
	 * @param mode
	 */
	boolean supports(AuthenticationProtocol protocol);


	/**
	 * Authenticates to the bug tracker with the given credentials. If authentication does not fail, it should not be
	 * required again at least for the current thread.
	 *
	 * Default implementation delegates to the deprecated {@link #authenticate(AuthenticationCredentials)}
	 * if the connector supports the BASIC_AUTH mode
	 *
	 * @param credentials the credentials
	 * @throw UnsupportedAuthenticationModeException if the connector cannot use the given credentials
	 */
	void authenticate(Credentials credentials) throws UnsupportedAuthenticationModeException;


	/**
	 * will check if the current credentials are actually acknowledged by the bugtracker
	 *
	 * Default implementation delegates to the deprecated {@link #checkCredentials(AuthenticationCredentials)}
	 * if the connector supports the BASIC_AUTH mode
	 *
	 *
	 * @param credentials
	 * @return nothing
	 * @throw UnsupportedAuthenticationModeException if the connector cannot use the given credentials
	 * @throw {@link BugTrackerNoCredentialsException} if the credentials are invalid
	 * @throw {@link BugTrackerRemoteException} for other network exceptions.
	 */
	void checkCredentials(Credentials credentials) throws BugTrackerNoCredentialsException,
																	  BugTrackerRemoteException;



	/**
	 * Must set the credentials in the connector context for remote authentication challenges
	 *
	 * @Deprecated use {@link #authenticate(Credentials)} instead
	 * @param credentials
	 */
	@Deprecated
	void authenticate(AuthenticationCredentials credentials);

	/**
	 * Must set the credentials as in {@link #authenticate(AuthenticationCredentials)} and immediately test them
	 * against the endpoint to check their validity
	 *
	 * @Deprecated use {@link #checkCredentials(Credentials)} instead
	 * @param credentials
	 * @throws BugTrackerNoCredentialsException for null arguments
	 * @throws BugTrackerRemoteException for else.
	 */
	@Deprecated
	void checkCredentials(AuthenticationCredentials credentials) throws BugTrackerNoCredentialsException,
	BugTrackerRemoteException;


	/**
	 * Must return the URL where one can browse the issue.
	 *
	 * @param issueId
	 * @param bugTracker
	 * @return
	 */
	URL makeViewIssueUrl(BugTracker bugTracker, String issueId) ;


	/**
	 * Must return a project, given its name, with metadata such as which versions or categories are defined in there.
	 *
	 * @param projectName
	 * @return
	 * @throws ProjectNotFoundException
	 * @throws BugTrackerRemoteException
	 */
	RemoteProject findProject(String projectName) throws ProjectNotFoundException, BugTrackerRemoteException;


	/**
	 * @see #findProject(String), except that one uses the Id.
	 * @param projectId
	 * @return
	 * @throws ProjectNotFoundException
	 * @throws BugTrackerRemoteException
	 */
	RemoteProject findProjectById(String projectId) throws ProjectNotFoundException, BugTrackerRemoteException;

	/**
	 * Must create an issue on the remote bugtracker, then return the 'persisted' version of it (ie, having its id)
	 *
	 * @param issue
	 * @return
	 * @throws BugTrackerRemoteException
	 */
	RemoteIssue createIssue(RemoteIssue issue) throws BugTrackerRemoteException;

	/**
	 * Must return ready-to-fill issue, ie with empty fields and its project configured with as many metadata as possible related to issue creation.
	 *
	 * @param projectName
	 * @return
	 */
	RemoteIssue createReportIssueTemplate(String projectName);

	/**
	 * Returns an {@link BugTrackerInterfaceDescriptor}
	 *
	 * @return
	 */
	BugTrackerInterfaceDescriptor getInterfaceDescriptor();

	/**
	 * Retrieve a remote issue
	 *
	 * @param key
	 * @return
	 */
	RemoteIssue findIssue(String key);

	/**
	 * Retrieve many remote issues
	 *
	 * @param issueKeys
	 * @return
	 */
	List<RemoteIssue> findIssues(Collection<String> issueKeys);


	/**
	 * Given a remote issue key, will ask the bugtracker to attach the attachments to that issue.
	 *
	 * @param remoteIssueKey
	 * @param attachments
	 */
	void forwardAttachments(String remoteIssueKey, List<Attachment> attachments);


	/**
	 * Executes a delegate command and may return a result. The resulting object must be string-serializable, as it will be jsonified and brought to the
	 * Squash UI.
	 *
	 * @param command
	 * @return
	 */
	Object executeDelegateCommand(DelegateCommand command);

}
