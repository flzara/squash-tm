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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.squashtest.tm.domain.scm.ScmRepository;

import java.util.Collection;
import java.util.List;

public interface ScmRepositoryDao extends JpaRepository<ScmRepository, Long> {

	/**
	 * Find the ScmRepositories contained in the ScmServer with the given Id ordered by path.
	 * @param scmServerId The Id of the ScmServer containing the wanted ScmRepositories.
	 * @return The List of the ScmRepositories contained in the given ScmServer ordered by path.
	 */
	List<ScmRepository> findByScmServerIdOrderByRepositoryPathAsc(Long scmServerId);
	/**
	 * Find the ScmRepositories contained in the ScmServer with the given Id oredered by path.
	 * @param scmServerId The Id of the ScmServer containing the wanted ScmRepositories.
	 * @param pageable The Pageable against which the Page will be built.
	 * @return The Page of the ScmRepositories contained in the given ScmServer built according the given Pageable.
	 */
	Page<ScmRepository> findByScmServerId(Long scmServerId, Pageable pageable);
	/**
	 * Delete the ScmRepositories with the given Ids.
	 * @param scmRepositoriesIds The Ids of the ScmRepositories to delete.
	 */
	@Query
	@Modifying
	void deleteByIds(@Param("scmRepositoriesIds") Collection<Long> scmRepositoriesIds);

}
