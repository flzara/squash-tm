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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import org.squashtest.csp.core.bugtracker.core.*;
import org.squashtest.csp.core.bugtracker.domain.BTIssue;
import org.squashtest.csp.core.bugtracker.domain.BTProject;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.csp.core.bugtracker.domain.Priority;
import org.squashtest.csp.core.bugtracker.spi.BugTrackerConnector;
import org.squashtest.tm.bugtracker.advanceddomain.DelegateCommand;
import org.squashtest.tm.bugtracker.definition.Attachment;
import org.squashtest.tm.bugtracker.definition.RemoteIssue;
import org.squashtest.tm.bugtracker.definition.RemoteProject;
import org.squashtest.tm.core.foundation.lang.CollectionUtils;

/**
 * Could also have been called LegacyBugtrackerConnectorAdapter
 *
 * @author bsiri
 *
 */

public class SimpleBugtrackerConnectorAdapter extends AbstractInternalConnectorAdapter {

	private BugTrackerConnector connector;

	public SimpleBugtrackerConnectorAdapter() {
		super();
	}

	public SimpleBugtrackerConnectorAdapter(BugTrackerConnector connector) {
		super();
		this.connector = connector;
	}

	public void setConnector(BugTrackerConnector connector) {
		this.connector = connector;
	}

	@Override
	public BugTrackerConnector getConnector() {
		return connector;
	}


	@Override
	public RemoteProject findProject(String projectName) throws ProjectNotFoundException, BugTrackerRemoteException {
		return connector.findProject(projectName);
	}

	@Override
	public RemoteProject findProjectById(String projectId) throws ProjectNotFoundException, BugTrackerRemoteException {
		return connector.findProject(projectId);
	}

	@Override
	public RemoteIssue createIssue(RemoteIssue issue) throws BugTrackerRemoteException {
		return connector.createIssue((BTIssue) issue);
	}


	@Override
	public RemoteIssue findIssue(String key) {
		return connector.findIssue(key);
	}

	/**
	 *
	 * @see org.squashtest.csp.core.bugtracker.service.InternalBugtrackerConnector#findIssues(java.util.Collection)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public List<RemoteIssue> findIssues(Collection<String> issueKeys) {
		return (List) connector.findIssues(coerceToList(issueKeys));
	}

	/**
	 * @param issueKeys
	 * @return
	 */
	private List<String> coerceToList(Collection<String> issueKeys) {

		return CollectionUtils.coerceToList(issueKeys);
	}

	@Override
	public RemoteIssue createReportIssueTemplate(String projectName) {
		RemoteProject project = connector.findProject(projectName);

		BTIssue emptyIssue = new BTIssue();
		BTProject btProject = (BTProject) project;
		emptyIssue.setProject(btProject);
		Priority defaultPriority = btProject.getDefaultIssuePriority();
		/*
		 * The dummy priority is the default value for BTProject defaultIssuePriority property. We want to fill the
		 * priority of the issue template only if a real value has been set for defaultIssuePriority property.
		 */
		if (!defaultPriority.equals(Priority.NO_PRIORITY)) {
			emptyIssue.setPriority(defaultPriority);
		}

		return emptyIssue;
	}

	@Override
	public URL makeViewIssueUrl(BugTracker bugTracker, String issueId) {
		URL url = null;
		URL baseUrl = bugTracker.getURL();
		try {
			if (baseUrl != null) {
				String suffix = connector.makeViewIssueUrlSuffix(issueId);
				url = new URL(baseUrl.toString() + suffix);
			} else {
				url = null;
			}
		} catch (MalformedURLException mue) {
			// XXX should throw an exception
			url = null;
		}

		return url;
	}

	@Override
	public void forwardAttachments(String remoteIssueKey, List<Attachment> attachments) {
		// NOOP : the old interface simply cannot do that. It cannot possibly be invoked anyway. Normally.
		throw new BugTrackerManagerException("Technical error : impossible to post attachments for issue '"
				+ remoteIssueKey + "' . This issue "
				+ "is managed by a simple connector that cannot handle such operation. As such yhis is likely a "
				+ "a programming error : file uploads should never have been available in the GUI in the first place. "
				+ "Please submit " + "your attachments using the bugtracker itself.");
	}

	@Override
	public Object executeDelegateCommand(DelegateCommand command) {
		// NOOP : the old interface simply cannot do that. It cannot possibly be invoked anyway. Normally.
		throw new BugTrackerManagerException("Technical error : impossible to execute a delegate command. This issue "
				+ "is managed by a simple connector that cannot handle such operation. As such yhis is likely a "
				+ "a programming error : file uploads should never have been available in the GUI in the first place");
	}

}
