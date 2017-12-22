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
package org.squashtest.csp.core.bugtracker.spi;

import java.net.URL;
import java.util.List;

import org.squashtest.csp.core.bugtracker.core.BugTrackerRemoteException;
import org.squashtest.csp.core.bugtracker.core.ProjectNotFoundException;
import org.squashtest.tm.bugtracker.advanceddomain.AdvancedIssue;
import org.squashtest.tm.bugtracker.advanceddomain.AdvancedProject;
import org.squashtest.tm.bugtracker.advanceddomain.DelegateCommand;
import org.squashtest.tm.bugtracker.advanceddomain.FieldValue;
import org.squashtest.tm.bugtracker.definition.Attachment;
import org.squashtest.tm.bugtracker.definition.RemoteIssue;

public interface AdvancedBugTrackerConnector extends BugtrackerConnectorBase {

	/**
	 * Must return the URL where one can browse the issue.
	 *
	 * @param issueId
	 * @return
	 */
	URL makeViewIssueUrl(String issueId);

	/**
	 * Must return a project, given its name, with metadata such as which versions or categories are defined in there.
	 *
	 * @param projectName
	 * @return
	 * @throws ProjectNotFoundException
	 * @throws BugTrackerRemoteException
	 */
	AdvancedProject findProject(String projectName) throws ProjectNotFoundException, BugTrackerRemoteException;

	/**
	 * @see #findProject(String), except that one uses the Id.
	 * @param projectId
	 * @return
	 * @throws ProjectNotFoundException
	 * @throws BugTrackerRemoteException
	 */
	AdvancedProject findProjectById(String projectId) throws ProjectNotFoundException, BugTrackerRemoteException;

	/**
	 * Must create an issue on the remote bugtracker, then return the 'persisted' version of it (ie, having its id)
	 *
	 * @param issue
	 * @return
	 * @throws BugTrackerRemoteException
	 */
	AdvancedIssue createIssue(RemoteIssue issue) throws BugTrackerRemoteException;


	/**
	 * Must return ready-to-fill issue, ie with empty fields and its project configured with as many metadata as
	 * possible related to issue creation.
	 *
	 * @param projectName
	 * @return
	 */
	RemoteIssue createReportIssueTemplate(String projectName);

	/**
	 * Retrieve a remote issue
	 *
	 * @param key
	 * @return
	 */
	AdvancedIssue findIssue(String key);

	/**
	 * Retrieve many remote issues
	 *
	 * @param issueKeyList
	 * @return
	 */
	List<AdvancedIssue> findIssues(List<String> issueKeyList);

	/**
	 * Post the given attachments to the issue identified by remoteIssueKey
	 *
	 * @param remoteIssueKey
	 * @param attachments
	 */
	void forwardAttachments(String remoteIssueKey, List<Attachment> attachments);

	/**
	 * <p>
	 * Executes a delegate command and may return a result. The resulting object must be string-serializable, as it will
	 * be jsonified and brought to the Squash UI.
	 * </p>
	 *
	 * <p>
	 * Note : the return type is free but {@link FieldValue} is preferred when applicable
	 * </p>
	 *
	 * @param command
	 * @return
	 */
	Object executeDelegateCommand(DelegateCommand command);

}
