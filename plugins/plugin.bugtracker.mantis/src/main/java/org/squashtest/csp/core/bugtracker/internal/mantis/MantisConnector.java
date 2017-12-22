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
package org.squashtest.csp.core.bugtracker.internal.mantis;

import java.math.BigInteger;
import java.util.*;

import org.squashtest.tm.core.foundation.exception.NullArgumentException;
import org.squashtest.csp.core.bugtracker.core.BugTrackerNotFoundException;
import org.squashtest.csp.core.bugtracker.domain.BTIssue;
import org.squashtest.csp.core.bugtracker.domain.BTProject;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.csp.core.bugtracker.domain.Category;
import org.squashtest.csp.core.bugtracker.domain.Identifiable;
import org.squashtest.csp.core.bugtracker.domain.Permission;
import org.squashtest.csp.core.bugtracker.domain.Priority;
import org.squashtest.csp.core.bugtracker.domain.User;
import org.squashtest.csp.core.bugtracker.domain.Version;
import org.squashtest.csp.core.bugtracker.mantis.binding.AccountData;
import org.squashtest.csp.core.bugtracker.mantis.binding.IssueData;
import org.squashtest.csp.core.bugtracker.mantis.binding.ObjectRef;
import org.squashtest.csp.core.bugtracker.mantis.binding.ProjectData;
import org.squashtest.csp.core.bugtracker.mantis.binding.ProjectVersionData;
import org.squashtest.csp.core.bugtracker.net.AuthenticationCredentials;
import org.squashtest.csp.core.bugtracker.spi.BugTrackerConnector;
import org.squashtest.csp.core.bugtracker.spi.BugTrackerInterfaceDescriptor;

/**
 * Implementation of the {@link BugTrackerConnector} for Mantis. Delegates to a unique instance of
 *
 * @author Gregory Fouquet
 *
 */
public class MantisConnector implements BugTrackerConnector {

	private static final String MANTIS_ISSUE_SUFFIX = "/view.php";

	private final ThreadLocal<AuthenticationCredentials> credentialsHolder = new ThreadLocal<>();

	private final MantisAxis1SoapClient client;

	private MantisExceptionConverter exConverter;

	public MantisConnector(BugTracker bugTracker) {
		super();
		client = new MantisAxis1SoapClient(bugTracker);
	}

	private BugTrackerInterfaceDescriptor interfaceDescriptor;

	public void setInterfaceDescriptor(BugTrackerInterfaceDescriptor interfaceDescriptor) {
		this.interfaceDescriptor = interfaceDescriptor;
	}

	public void setExceptionConverter(MantisExceptionConverter converter) {
		this.exConverter = converter;
		client.setMantisExceptionConverter(converter);
	}

	@Override
	public BugTrackerInterfaceDescriptor getInterfaceDescriptor() {
		return interfaceDescriptor;
	}

	@Override
	public void authenticate(AuthenticationCredentials credentials) {
		if (credentials == null) {
			throw new NullArgumentException("credentials");
		}
		credentialsHolder.set(credentials);
	}

	@Override
	public String makeViewIssueUrlSuffix(String issueId) {
		return MANTIS_ISSUE_SUFFIX + "?id=" + issueId;
	}

	@Override
	public void checkCredentials(AuthenticationCredentials credentials) {
		// test any function of the client and check if it's okay
		client.getSeverities(credentials);
	}

	@Override
	public List<Priority> getPriorities() {
		ObjectRef[] priorities = client.getPriorities(credentialsHolder.get());
		return MantisEntityConverter.mantis2SquashPriority(priorities);
	}



	public List<Permission> getPermissions() {
		ObjectRef[] accessLevels = client.getAccessLevel(credentialsHolder.get());
		return MantisEntityConverter.mantis2SquashPermission(accessLevels);
	}


	@Override
	public BTProject findProject(String projectName) {
		ProjectData[] mantisProjects = client.findProjects(credentialsHolder.get());
		List<BTProject> projects = MantisEntityConverter.mantis2SquashProject(mantisProjects);

		BTProject found = findInListByName(projects, projectName);

		if (found != null) {
			return populateProject(found);
		} else {
			throw exConverter.makeProjectNotFound(projectName);
		}

	}

	@Override
	public BTProject findProjectById(String projectId) {
		ProjectData[] mantisProjects = client.findProjects(credentialsHolder.get());
		List<BTProject> projects = MantisEntityConverter.mantis2SquashProject(mantisProjects);

		BTProject found = findInListById(projects, projectId);

		if (found != null) {
			return populateProject(found);
		} else {
			throw exConverter.makeProjectNotFound(projectId);
		}
	}

	@Override
	public List<Version> findVersions(String projectName) {
		BTProject project = findProject(projectName);
		return findVersionsById(project.getId());
	}

	@Override
	public List<Version> findVersionsById(String projectId) {

		ProjectVersionData[] mantisVersions = client.findVersions(credentialsHolder.get(),
				MantisEntityConverter.squash2MantisId(projectId));

		return MantisEntityConverter.mantis2SquashVersion(mantisVersions);
	}

	@Override
	public List<Version> findVersions(BTProject project) {
		return findVersionsById(project.getId());
	}

	@Override
	public List<User> findUsers(String projectName) {
		BTProject project = findProject(projectName);
		List<Permission> permissions = getPermissions();
		return makeUserList(project.getId(), permissions);
	}

	@Override
	public List<User> findUsersById(String projectID) {
		List<Permission> permissions = getPermissions();
		return makeUserList(projectID, permissions);
	}

	@Override
	public List<User> findUsers(BTProject project) {
		List<Permission> permissions = getPermissions();
		return makeUserList(project.getId(), permissions);

	}

	@Override
	public List<Category> findCategories(BTProject project) {
		String[] categories = client.findCategories(credentialsHolder.get(),
				MantisEntityConverter.squash2MantisId(project.getId()));
		return MantisEntityConverter.mantis2SquashCategory(categories);
	}

	@Override
	public BTIssue createIssue(BTIssue issue) {
		IssueData data = MantisEntityConverter.squashToMantisIssue(issue);
		BigInteger issueId = client.createIssue(credentialsHolder.get(), data);

		issue.setId(MantisEntityConverter.mantis2SquashId(issueId));
		return issue;
	}


	@Override
	public BTIssue findIssue(String key){
		BigInteger remoteId;
		try{
			remoteId = MantisEntityConverter.squash2MantisId(key);
		}catch(NumberFormatException ex){
			throw exConverter.newIssueNotFoundException();
		}
		IssueData mantisIssue = client.getIssue(credentialsHolder.get(), remoteId);
		BTIssue issue = MantisEntityConverter.mantis2squashIssue(mantisIssue);
		BTProject project = findProject(issue.getProject().getName());


		//let's fill the holes left by mantis2squashIssue
		issue.setVersion(findInListByName(project.getVersions(), issue.getVersion().getName()));
		issue.setCategory(findInListByName(project.getCategories(), issue.getCategory().getName()));
		issue.setAssignee(findInListByName(project.getUsers(), issue.getAssignee().getName()));


		issue.setProject(project);
		return issue;
	}

	@Override
	public List<BTIssue> findIssues(List<String> issueKeyList) {
		List<BTIssue> toReturn = new ArrayList<>();
		for (String issueKey : issueKeyList) {
			// Get the mantis issue data....

			try{
			IssueData mantisIssue = client.getIssue(credentialsHolder.get(), MantisEntityConverter.squash2MantisId(issueKey));
			// ... and convert it
			BTIssue issue = MantisEntityConverter.mantis2squashIssue(mantisIssue);
			toReturn.add(issue);
			} catch (BugTrackerNotFoundException ex){ // NOSONAR : this exception is part of the nominal use case
				toReturn.add(MantisEntityConverter.issueNotFound(issueKey, exConverter));
			}
		}
		return toReturn;
	}

	/* ****************************private methods ****************** */


	private BTProject populateProject(BTProject project){
		project.addAllVersions(findVersions(project));
		project.addAllUsers(findUsers(project));
		project.addAllCategories(findCategories(project));
		project.addallPriorities(getPriorities());
		project.setDefaultIssuePriority(getDefaultPriority(project.getPriorities()));
		return project;
	}


	private Priority getDefaultPriority(List<Priority> projectPriorities) {
		String defaultPriorityId= client.getDefaultPriority(credentialsHolder.get());
		return findInListById(projectPriorities, defaultPriorityId);
	}

	private List<User> makeUserList(String projectId, List<Permission> permissions) {

		Map<String, User> userMap = new HashMap<>();

		for (Permission permission : permissions) {
			AccountData[] mantisUsers = client.findUsersForProject(credentialsHolder.get(),
					MantisEntityConverter.squash2MantisId(projectId),
					MantisEntityConverter.squash2MantisId(permission.getId()));
			List<User> subList = MantisEntityConverter.mantis2SquashUser(mantisUsers);

			// if the user wasn't already in we add him in the map, and anyway we add the current permission to him
			for (User user : subList) {
				if (userMap.get(user.getId()) == null) {
					userMap.put(user.getId(), user);
				}
				userMap.get(user.getId()).addPermission(permission);
			}

		}


		List<User> users = new LinkedList<>(userMap.values());
		Collections.sort(users, new Comparator<User>() {
			@Override
			public int compare(User o1, User o2) {
				return o1.getName().compareToIgnoreCase(o2.getName());
			}
		});


		return users;
	}


	/* ******************************* utilities ******************** */

	@SuppressWarnings("unchecked")
	private <X extends Identifiable> X findInListByName(List<X> identifiables, String name) {
		for (Identifiable identifiable : identifiables) {
			if (identifiable.getName().equals(name)) {
				return (X) identifiable;
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private <X extends Identifiable> X findInListById(List<X> identifiables, String id) {
		for (Identifiable identifiable : identifiables) {
			if (identifiable.getId().equals(id)) {
				return (X) identifiable;
			}
		}
		return null;
	}

}
