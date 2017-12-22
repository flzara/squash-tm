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
package org.squashtest.tm.service.internal.repository;

import org.squashtest.tm.service.internal.dto.UserDto;

import java.util.List;


/**
 * @author Gregory Fouquet
 *
 */
public interface CustomProjectDao {
	long countNonFoldersInProject(long projectId);

	List<String> findUsersWhoCreatedTestCases(List<Long> projectIds);

	List<String> findUsersWhoModifiedTestCases(List<Long> projectIds);

	List<String> findUsersWhoCreatedRequirementVersions(List<Long> projectIds);

	List<String> findUsersWhoModifiedRequirementVersions(List<Long> projectIds);

	List<Long> findAllProjectIds();

	/**
	 * Get the ids of readable {@link org.squashtest.tm.domain.project.Project} for the givens {@link org.squashtest.tm.domain.users.Party} ids
	 * Thea goal here is to have the projectIds pre fetched to avoid {@link org.springframework.security.access.prepost.PostFilter} expression.
	 * Performance testing have shown that {@link org.springframework.security.access.prepost.PostFilter} on a big collection has a HUGE performance cost...
	 * @param partyIds The ids of all concerned parties
	 * @return ths ids of {@link org.squashtest.tm.domain.project.Project} they can read. {@link org.squashtest.tm.domain.project.ProjectTemplate} are excluded.
	 */
	List<Long> findAllProjectIds(List<Long> partyIds);

}
