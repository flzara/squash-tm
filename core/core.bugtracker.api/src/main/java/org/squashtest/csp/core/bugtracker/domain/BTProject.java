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
package org.squashtest.csp.core.bugtracker.domain;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.squashtest.tm.bugtracker.definition.RemoteProject;

/**
 *
 * a BTProject has lists of Priority, Version, assignable User and Category. Those lists must never be empty : if such
 * list would be empty because their counterpart on the remote server do not exist, please use the dummy specified for
 * each of those classes, eg {@link Version#NO_VERSION} for empty version list.
 *
 * @author bsiri
 *
 */
public class BTProject implements Identifiable<BTProject>, RemoteProject {

	private String id;
	private String name;

	private List<Priority> priorities = new LinkedList<>();
	private List<Version> versions = new LinkedList<>();
	private List<User> users = new LinkedList<>();
	private List<Category> categories = new LinkedList<>();
	/* Set to the dummy priority by default */
	private Priority defaultIssuePriority = Priority.NO_PRIORITY;

	public BTProject() {
		//Default constructor
	}

	public BTProject(String id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setVersions(List<Version> versions) {
		this.versions = versions;
	}

	public List<Version> getVersions() {
		return versions;
	}

	public List<Category> getCategories() {
		return categories;
	}

	public void setCategories(List<Category> categories) {
		this.categories = categories;
	}

	public List<Priority> getPriorities() {
		return priorities;
	}

	public void setPriorities(List<Priority> priorities) {
		this.priorities = priorities;
	}

	/**
	 * Get the default issue priority
	 *
	 * @return The default issue priority
	 */
	public Priority getDefaultIssuePriority() {
		return defaultIssuePriority;
	}

	/**
	 * Set the default issue priority
	 *
	 * @param defaultIssuePriority
	 *            The new default issue priority
	 */
	public void setDefaultIssuePriority(Priority defaultPriority) {
		this.defaultIssuePriority = defaultPriority;
	}

	public void addVersion(Version version) {
		versions.add(version);
	}

	public void addAllVersions(Collection<Version> versions) {
		this.versions.addAll(versions);
	}

	public Version findVersionByName(String versionName) {
		for (Version version : versions) {
			if (version.getName().equals(versionName)) {
				return version;
			}
		}
		return null;
	}

	public Version findVersionById(String versionId) {
		for (Version version : versions) {
			if (version.getId().equals(versionId)) {
				return version;
			}
		}
		return null;
	}

	public void addAllCategories(Collection<Category> categories) {
		this.categories.addAll(categories);
	}

	public Category findCategoryByName(String categoryName) {
		for (Category category : categories) {
			if (category.getName().equals(categoryName)) {
				return category;
			}
		}
		return null;
	}

	public Category findCategoryById(String categoryId) {
		for (Category category : categories) {
			if (category.getId().equals(categoryId)) {
				return category;
			}
		}
		return null;
	}

	public void addallPriorities(Collection<Priority> priorities) {
		this.priorities.addAll(priorities);
	}

	public Priority findPriorityByName(String priorityName) {
		for (Priority priority : priorities) {
			if (priority.getName().equals(priorityName)) {
				return priority;
			}
		}
		return null;
	}

	public Priority findPriorityById(String priorityId) {
		for (Priority priority : priorities) {
			if (priority.getId().equals(priorityId)) {
				return priority;
			}
		}
		return null;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}

	public List<User> getUsers() {
		return users;
	}

	public void addUser(User user) {
		this.users.add(user);
	}

	public void addAllUsers(Collection<User> users) {
		this.users.addAll(users);
	}

	public User findUserByName(String userName) {
		for (User user : users) {
			if (user.getName().equals(userName)) {
				return user;
			}
		}
		return null;
	}

	public User findUserById(String userId) {
		for (User user : users) {
			if (user.getId().equals(userId)) {
				return user;
			}
		}
		return null;
	}

	/**
	 * is hopefully never a dummy
	 *
	 */
	@Override
	public boolean isDummy() {
		return false;
	}

	/** exists for the purpose of being java-bean compliant */
	public void setDummy(Boolean dummy) {
		//exists for the purpose of being javabean compliant
	}

	/**
	 * returns true if the user list is empty or if it contains only {@link User}.NO_USER
	 *
	 * @return
	 */
	public boolean canAssignUsers() {
		return !(users.isEmpty() || users.size() == 1 && users.get(0).isDummy());
	}
}
