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

import org.squashtest.csp.core.bugtracker.core.BugTrackerNotFoundException;
import org.squashtest.csp.core.bugtracker.core.BugTrackerRemoteException;
import org.squashtest.csp.core.bugtracker.core.ProjectNotFoundException;
import org.squashtest.csp.core.bugtracker.domain.*;
import java.util.List;


/**
 * <p>Connector to a bug tracker.</p>
 *
 * <p>
 * 	This is a simple version of the BugTracker API, that uses a simple domain. See org.squashtest.csp.core.bugtracker.domain to see what domain
 * it is. This domain fixes the layout and properties of an issue, project etc. Using these entities, Squash will know how to treat and display them.
 * </p>
 *
 * <p>
 * <strong>Item lists : </strong> An issue has several characteristics (priority, version etc) that can be picked
 * from a list. The content of those lists may vary from bugtracker to bugtracker and from project A to another
 * project B. Among those listable item the BugTrackerConnecter focus on four of them :
 * <ul>
 * 	<li> {@link Priority} </li>
 *  <li>(assignable) {@link User} </li>
 *  <li> {@link Version} </li>
 *  <li> {@link Category} </li>
 * </ul>
 *
 * In some cases those lists may be empty (no assignable users for instance).
 * For such cases, an implementation of {@link BugTrackerConnector} should never return the empty list :
 * the returned list should contain a specific singleton. See {@link User#NO_USER} or
 * {@link Version#NO_VERSION} for instance.
 *
 * </p>
 *
 * @author Gregory Fouquet
 *
 */
public interface BugTrackerConnector extends BugtrackerConnectorBase{


	/**
	 * <p>Returns the path to an issue identified by 'issueId'. This suffix corresponds to the URL to
	 * that issue once the base URL of the bugtracker is removed. Since the base URL of the bugtracker includes
	 * the procotol, authority, hostname and port, most of the time the suffix is simply the path to the issue.
	 * </p>
	 *
	 * @param issueId the ID of an issue that is supposed to exist on the bugtracker (ie, with an not empty id)
	 * @return the path to the issue, relative to the host root directory.
	 */
	String makeViewIssueUrlSuffix(String issueId);

	/**
	 * Returns the list of priorities available on the remote bugtracker. As of Squash TM 1.5.1, this method is
	 * deprecated and the application will not call it anymore (it never did anyway). Throwing UnsupportedOperationException
	 * or returning whatever is fine.
	 *
	 * @return a list of Priority
	 * @throws BugTrackerRemoteException when something goes wrong with the remote call.
	 * @deprecated not called in the app, cannot be removed without breaking the api
	 */
	@Deprecated
	List<Priority> getPriorities() throws BugTrackerRemoteException;


	/**
	 * Returns a BTProject, identified by its name. The remote project name must perfectly match the argument.
	 * If found, the following attributes of the BTProject must be populated :
	 * <ul>
	 * <li>id</li>
	 * <li>name</li>
	 * <li>version list</li>
	 * <li>assignable users list (those that the requesting user can legally address)</li>
	 * <li>categories list</li>
	 * <li>priorities list</li>
	 * </ul>
	 *
	 * Note that the lists here must follow the rules stated in the top level documentation of this interface.
	 *
	 * @param projectName the name of the project.
	 * @return a project properly configured
	 * @throws ProjectNotFoundException if the project could not be found
	 * @throws BugTrackerRemoteException when something goes wrong with the remote call
	 */
	BTProject findProject(String projectName) throws ProjectNotFoundException, BugTrackerRemoteException;


	/**
	 * See {@link #findProject(String)}.
	 *
	 * @param projectId the id of the project
	 * @return a project properly configured
	 * @throws ProjectNotFoundException if the project could not be found
	 * @throws BugTrackerRemoteException when something goes wrong with the remote call
	 */
	BTProject findProjectById(String projectId) throws ProjectNotFoundException, BugTrackerRemoteException;


	/**
	 * Will return the list of the available versions of the given project (given its name).
	 *
	 * @param projectName is the name of the project
	 * @return the list of the versions if any, or a list only containing {@link Version#NO_VERSION} when
	 * none were found.
	 * @throws ProjectNotFoundException if the project doesn't exist
	 * @throws BugTrackerRemoteException when something goes wrong with the remote call.
	 */
	List<Version> findVersions(String projectName) throws ProjectNotFoundException, BugTrackerRemoteException;


	/**
	 * Will return the list of the available versions of the given project (given its id).
	 *
	 * @param projectId is the id of the project
	 * @return the list of the versions if any, or a list only containing {@link Version#NO_VERSION} when
	 * none were found.
	 * @throws ProjectNotFoundException if the project doesn't exist
	 * @throws BugTrackerRemoteException when something goes wrong with the remote call.
	 */
	List<Version> findVersionsById(String projectId) throws ProjectNotFoundException, BugTrackerRemoteException;


	/**
	 * Will return the list of the available versions of the given project (given the project itself).
	 *
	 * @param project being the project
	 * @return the list of the versions if any, or a list only containing {@link Version#NO_VERSION} when
	 * none were found.
	 * @throws ProjectNotFoundException if the project doesn't exist
	 * @throws BugTrackerRemoteException when something goes wrong with the remote call.
	 */
	List<Version> findVersions(BTProject project) throws ProjectNotFoundException, BugTrackerRemoteException;


	/**
	 * Will return the list of the assignable users for the given project and current user (given the project name).
	 * The users must be returned with their Permissions if they have some.
	 *
	 * @param projectName is the name of the project
	 * @return the list of the versions if any, or a list only containing {@link User#NO_USER} when
	 * none were found.
	 * @throws ProjectNotFoundException if the project doesn't exist
	 * @throws BugTrackerRemoteException when something goes wrong with the remote call.
	 */
	List<User> findUsers(String projectName) throws ProjectNotFoundException, BugTrackerRemoteException;


	/**
	 * Will return the list of the assignable users for the given project and current user (given the project id).
	 * The users must be returned with their Permissions if they have some.
	 *
	 * @param projectID is the name of the project
	 * @return the list of the versions if any, or a list only containing {@link User#NO_USER} when
	 * none were found.
	 * @throws ProjectNotFoundException if the project doesn't exist
	 * @throws BugTrackerRemoteException when something goes wrong with the remote call.
	 */
	List<User> findUsersById(String projectID) throws ProjectNotFoundException, BugTrackerRemoteException;


	/**
	 * Will return the list of the assignable users for the given project and current user (given the project itself).
	 * The users must be returned with their Permissions if they have some.
	 *
	 * @param project being the project.
	 * @return the list of the versions if any, or a list only containing {@link User#NO_USER} when
	 * none were found.
	 * @throws ProjectNotFoundException if the project doesn't exist
	 * @throws BugTrackerRemoteException when something goes wrong with the remote call.
	 */
	List<User> findUsers(BTProject project) throws ProjectNotFoundException, BugTrackerRemoteException;


	/**
	 * Will create an issue on the remote bugtracker. The detached issue, supplied as an argument,
	 * have no ID/key yet. Its {@link Priority}, {@link Version}, assignee ( {@link User} ) and {@link Category}
	 * will be set, possibly to {@link Version#NO_VERSION}, {@link User#NO_USER}, or {@link Category#NO_CATEGORY}.
	 * The summary, description or comment might be null or empty.
	 *
	 * @param issue a squash Issue
	 * @return the corresponding new remote Issue, of which the ID must be set.
	 * @throws BugTrackerRemoteException when something goes wrong with the remote call, including validation exception.
	 *
	 */
	BTIssue createIssue(BTIssue issue) throws BugTrackerRemoteException;


	/**
	 *
	 * Will return the list of the available categories for the given project (given the project itself).
	 *
	 * @return the list of the versions if any, or a list only containing {@link Category#NO_CATEGORY} when
	 * none were found.
	 * @throws ProjectNotFoundException if the project doesn't exist
	 * @throws BugTrackerRemoteException when something goes wrong with the remote call.
	 *
	 */
	List<Category> findCategories(BTProject project) throws ProjectNotFoundException, BugTrackerRemoteException;



	/**
	 * Returns a single issue. The returned issue must use {@link Version#NO_VERSION} and alike when the version etc aren't
	 * set, instead of null. Furthermore, the {@link BTProject} returned by {@link BTIssue#getProject()} MUST be completely
	 * configured, as documented in {@link #findProject(String)}.
	 *
	 *
	 * @param key the key of the issue.
	 * @return the issue from the remote bugtracker
	 * @throws BugTrackerNotFoundException when the issue wasn't found
	 *
	 */
	BTIssue findIssue(String key);


	/***
	 * Returns a list of BTIssue, identified by their key. The resulting list doesn't have to be sorted according to the
	 * input list. Returned issues must use {@link Version#NO_VERSION} and alike when the version etc aren't
	 * set, instead of null. Unlike {@link #findIssue(String)}, their associated BTProject only needs their 'id' and 'name'
	 * attributes, and can let their other attribute to unspecified values.
	 *
	 * @param issueKeyList
	 *            the given squash issue list (List<String>)
	 * @return the corresponding BTIssue list
	 */
	List<BTIssue> findIssues(List<String> issueKeyList);


}
