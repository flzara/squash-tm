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
import org.squashtest.tm.domain.scm.ScmServer;

import java.util.Collection;
import java.util.List;

public interface ScmServerDao extends JpaRepository<ScmServer, Long> {

	/**
	 * Find all existing ScmServers ordered by name.
	 * @return The List of the ScmServers ordered by name.
	 */
	List<ScmServer> findAllByOrderByNameAsc();

	/**
	 * Check whether the given server name is already in use for another ScmServer.
	 * @param scmServerName The ScmServer name to check.
	 * @return True if the name is already in use, False otherwise.
	 */
	@Query
	boolean isServerNameAlreadyInUse(@Param("name") String scmServerName);

	/**
	 * Check whether at least one of the given ScmServers contains a ScmRepository which is bound to a Project.
	 * @param scmServerIds The Ids of the ScmServers.
	 * @return True if at least one of the given ScmServers contain a ScmRepository which is bound to a Project. False otherwise.
	 */
	@Query
	boolean isOneServerBoundToProject(@Param("scmServerIds") Collection<Long> scmServerIds);

	/**
	 * Release the ScmRepositories contained in the given ScmServers from their Projects.
	 * @param scmServerIds The Ids of the ScmServers which ScmRepositories are to release.
	 */
	@Query
	@Modifying
	void releaseContainedScmRepositoriesFromProjects(@Param("scmServerIds") Collection<Long> scmServerIds);
}
