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
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.scheduling.annotation.AsyncResult;
import org.squashtest.csp.core.bugtracker.core.BugTrackerConnectorFactory;
import org.squashtest.csp.core.bugtracker.core.UnsupportedAuthenticationModeException;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.csp.core.bugtracker.net.AuthenticationCredentials;
import org.squashtest.csp.core.bugtracker.spi.BugTrackerInterfaceDescriptor;
import org.squashtest.tm.bugtracker.advanceddomain.DelegateCommand;
import org.squashtest.tm.bugtracker.definition.Attachment;
import org.squashtest.tm.bugtracker.definition.RemoteIssue;
import org.squashtest.tm.bugtracker.definition.RemoteProject;
import org.squashtest.tm.domain.servers.AuthenticationPolicy;
import org.squashtest.tm.domain.servers.AuthenticationProtocol;
import org.squashtest.tm.domain.servers.BasicAuthenticationCredentials;
import org.squashtest.tm.domain.servers.Credentials;
import org.squashtest.tm.service.servers.StoredCredentialsManager;

/**
 * Basic implementation of {@link BugTrackersService}
 *
 * @author Gregory Fouquet
 *
 */
public class BugTrackersServiceImpl implements BugTrackersService {

	private BugTrackerContextHolder contextHolder;

	private BugTrackerConnectorFactory bugTrackerConnectorFactory;

	private StoredCredentialsManager credentialsManager;


	@Override
	public boolean isCredentialsNeeded(BugTracker bugTracker) {
		return !
		   (getBugTrackerContext().hasCredentials(bugTracker)	||
			(bugTracker.getAuthenticationPolicy() == AuthenticationPolicy.APP_LEVEL));
	}

	@Override
	public BugTrackerInterfaceDescriptor getInterfaceDescriptor(BugTracker bugTracker) {
		InternalBugtrackerConnector connector = bugTrackerConnectorFactory.createConnector(bugTracker);
		return connector.getInterfaceDescriptor();
	}

	@Override
	public URL getViewIssueUrl(String issueId, BugTracker bugTracker) {
		return connect(bugTracker).makeViewIssueUrl(bugTracker, issueId);
	}

	private BugTrackerContext getBugTrackerContext() {// TODO BugTrackersContext
		return contextHolder.getContext();
	}


	@Override
	public void setCredentials(Credentials credentials, BugTracker bugTracker) {
		AuthenticationPolicy policy = bugTracker.getAuthenticationPolicy();

		if (policy != AuthenticationPolicy.USER){
			throw new WrongAuthenticationPolicyException(policy);
		}

		InternalBugtrackerConnector connector = bugTrackerConnectorFactory.createConnector(bugTracker);

		// setcredentials to null first. If the operation succeed then we'll set them in the context.
		getBugTrackerContext().setCredentials(bugTracker, null);

		connector.checkCredentials(credentials);

		getBugTrackerContext().setCredentials(bugTracker, credentials);
	}

	@Override
	public void setCredentials(String username, String password, BugTracker bugTracker) {
		setCredentials(new BasicAuthenticationCredentials(username, password.toCharArray()), bugTracker);
	}

	@Override
	public void testCredentials(BugTracker bugTracker, Credentials credentials) {
		InternalBugtrackerConnector connector = bugTrackerConnectorFactory.createConnector(bugTracker);
		connector.checkCredentials(credentials);
	}

	@Override
	public RemoteProject findProject(String name, BugTracker bugTracker) {
		return connect(bugTracker).findProject(name);
	}

	@Override
	public RemoteProject findProjectById(String projectId, BugTracker bugTracker) {
		return connect(bugTracker).findProject(projectId);
	}

	private InternalBugtrackerConnector connect(BugTracker bugTracker) {
		InternalBugtrackerConnector connector = bugTrackerConnectorFactory.createConnector(bugTracker);
		Credentials creds = null;
		
		switch(bugTracker.getAuthenticationPolicy()){
			case USER:
				creds = getBugTrackerContext().getCredentials(bugTracker);
				break;

			case APP_LEVEL:
				creds = credentialsManager.unsecuredFindCredentials(bugTracker.getId());
				break;
				
			default : throw new RuntimeException("BugTrackerService#connect : forgot to implement policy "+bugTracker.getAuthenticationPolicy().toString());
		
		}

		AuthenticationProtocol protocol = creds.getImplementedProtocol();
		if (! connector.supports(protocol)){
			throw new UnsupportedAuthenticationModeException(protocol.toString());
		}
		
		connector.authenticate(creds);
		return connector;
	}

	@Override
	public RemoteIssue createIssue(RemoteIssue issue, BugTracker bugTracker) {
		RemoteIssue newissue = connect(bugTracker).createIssue(issue);
		newissue.setBugtracker(bugTracker.getName());
		return newissue;
	}

	@Override
	public RemoteIssue createReportIssueTemplate(String projectName, BugTracker bugTracker) {
		RemoteIssue issue = connect(bugTracker).createReportIssueTemplate(projectName);
		issue.setBugtracker(bugTracker.getName());
		return issue;
	}


	@Override
	public RemoteIssue getIssue(String key, BugTracker bugTracker) {
		RemoteIssue issue = connect(bugTracker).findIssue(key);
		issue.setBugtracker(bugTracker.getName());
		return issue;
	}

	@Override
	public Future<List<RemoteIssue>> getIssues(Collection<String> issueKeyList, BugTracker bugTracker, BugTrackerContext context, LocaleContext localeContext) {

		// reinstate the bugtrackercontext (since this method will execute in a different thread, see comments in the interface
		contextHolder.setContext(context);

         LocaleContextHolder.setLocaleContext(localeContext);

		List<RemoteIssue> issues = connect(bugTracker).findIssues(issueKeyList);

		String bugtrackerName = bugTracker.getName();

		for (RemoteIssue issue : issues) {
			issue.setBugtracker(bugtrackerName);
		}

		return new AsyncResult<>(issues);
	}

	@Override
	public void forwardAttachments(String remoteIssueKey, BugTracker bugtracker, List<Attachment> attachments) {
		connect(bugtracker).forwardAttachments(remoteIssueKey, attachments);
	}


	@Override
	public Set<String> getProviderKinds() {
		return bugTrackerConnectorFactory.getProviderKinds();
	}


	@Override
	public Object forwardDelegateCommand(DelegateCommand command,
			BugTracker bugtracker) {
		return connect(bugtracker).executeDelegateCommand(command);
	}


	/**
	 * @param contextHolder the contextHolder to set
	 */
	public void setContextHolder(BugTrackerContextHolder contextHolder) {
		this.contextHolder = contextHolder;
	}

	/**
	 * @param bugTrackerConnectorFactory the bugTrackerConnectorFactory to set
	 */
	public void setBugTrackerConnectorFactory(BugTrackerConnectorFactory bugTrackerConnectorFactory) {
		this.bugTrackerConnectorFactory = bugTrackerConnectorFactory;
	}

	public void setCredentialsManager(StoredCredentialsManager credentialsManager) {
		this.credentialsManager = credentialsManager;
	}
}
