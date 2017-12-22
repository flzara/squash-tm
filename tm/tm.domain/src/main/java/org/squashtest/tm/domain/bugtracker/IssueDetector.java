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
package org.squashtest.tm.domain.bugtracker;

import java.util.List;

import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.exception.NoBugTrackerBindingException;

public interface IssueDetector {

	/**
	 * 
	 * @return its IssueList
	 */
	IssueList getIssueList();

	/**
	 * will return the (Squash) project that entity belongs to
	 * 
	 * @return the project of that entity
	 */
	Project getProject();
	
	/**
	 * 
	 * @return the bugTracker the entity is associated to
	 * @throws NoBugTrackerBindingException if no BugTracker is found
	 */
	BugTracker getBugTracker();
	/**
	 * 
	 * @return the Id of its IssueList
	 */
	Long getIssueListId();


	/**
	 * @return the list of ids of its own IssueList and the result of getAllIssueListIds of other Bugged entities to
	 *         which that Bugged is bound to.
	 */
	List<Long> getAllIssueListId();

	
	/**
	 * removes the link between an issue and its issue detector.
	 */
	void detachIssue(Long id);
}
