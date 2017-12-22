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
package org.squashtest.tm.service.bugtracker;

import java.util.Collection;

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.tm.exception.DuplicateNameException;

@Transactional
public interface BugTrackerManagerService extends BugTrackerFinderService {

	/**
	 * add a new bugtracker in the database
	 * 
	 * @throws DuplicateNameException
	 * 
	 * @param bugTracker
	 */
	void addBugTracker(BugTracker bugTracker);

	/**
	 * Delete bugtracker(s), remove their binding to projects and delete all issues associated to them.
	 * 
	 * @param bugtrackerIds
	 *            collection of ids of the bugtrackers to be deleted
	 */
	void deleteBugTrackers(Collection<Long> bugtrackerIds);

}
