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
package org.squashtest.tm.service.user;

import java.util.List;

import org.squashtest.tm.core.foundation.collection.Filtering;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.users.Team;
import org.squashtest.tm.domain.users.User;

/**
 * Holder for non dynamically generated find methods for {@link Team}
 * 
 * @author mpagnon
 * 
 */
public interface CustomTeamFinderService {

	PagedCollectionHolder<List<Team>> findAllFiltered(PagingAndSorting sorting, Filtering filtering);

	List<User> findAllNonMemberUsers(long teamId);

	PagedCollectionHolder<List<User>> findAllTeamMembers(long teamId, PagingAndSorting sorting, Filtering filtering);

	/**
	 * Will count all Teams in the database.
	 * (could not manage to make this a dynamic method)
	 * 
	 * @return the total number of teams in the database.
	 */
	long countAll();
}
