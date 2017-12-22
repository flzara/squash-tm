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

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.requirement.RequirementSyncExtender;
import org.squashtest.tm.service.annotation.EmptyCollectionGuard;

import java.util.Collection;

/**
 * /!\ I (GRF) AM PRETTY SURE THIS CLASS IS USED AS AN API BY SOME PLUGIN - DON'T REMOVE ANY UNUSED METHOD !
 * I (JTH) CONFIRM YOUR DOUBTS, USED IN ALL JIRA SYNC PLUGINS... DON'T MODIFY/RENAME/REMOVE ANYTHING HERE YOUR NOT PROTECTED BY COMPILER
 * TODO Move this to some place which explicitly tells this is used as an API
 */
public interface RequirementSyncExtenderDao extends JpaRepository<RequirementSyncExtender, Long> {
	@Query
	RequirementSyncExtender retrieveByRemoteKey(@Param("id") String remoteId, @Param("pId") Long projectId);

	@Query
	RequirementSyncExtender retrieveByRemoteKeyAndSyncId(@Param("id") String remoteId, @Param("remoteSynchronisationId") Long remoteSyncId);

	@Query
	@EmptyCollectionGuard
	Collection<RequirementSyncExtender> retrieveAllByRemoteKey(@Param("ids") Collection<String> remoteId, @Param("pId") Long projectId);

	@Query
	Collection<RequirementSyncExtender> retrieveAllByRemoteProjectsAndFilter(@Param("remotePId") String remoteProjectId, @Param("filter") String filterName, @Param("pId") Long projectId);

	@Query
	Collection<RequirementSyncExtender> retrieveAllByServer(@Param("serverId") Long serverId);

	@Query
	@Modifying
	@Transactional
	void deleteAllByServer(@Param("serverId") Long serverId);

}
