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
package org.squashtest.tm.service.scmserver;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.squashtest.tm.domain.scm.ScmRepository;

import java.util.Collection;
import java.util.List;

public interface ScmRepositoryManagerService {

	/**
	 * Find the ScmRepositories contained in the ScmServer with the given Id ordered by path.
	 * @param scmServerId The Id of the ScmServer containing the wanted ScmRepositories.
	 * @return The List of the ScmRepositories contained in the given ScmServer ordered by path.
	 */
	List<ScmRepository> findByScmServerOrderByPath(Long scmServerId);
	/**
	 * Find the ScmRepositories contained in the ScmServer with the given Id, formatted as a Page to comply the given Pageable.
	 * @param scmServerId The Id of the ScmServer containing the wanted ScmRepositories.
	 * @param pageable The Pageable against which the Page will be built.
	 * @return The Page of ScmRepositories built according to the given Pageable.
	 */
	Page<ScmRepository> findPagedScmRepositoriesByScmServer(Long scmServerId, Pageable pageable);
	/**
	 * Create a new ScmRepository with its attributes.
	 * @param newScmRepository The ScmRepository with its attributes to create.
	 * @return The ScmRepository newly created.
	 */
	ScmRepository createNewScmRepository(ScmRepository newScmRepository);
	/**
	 * Delete the ScmRepositories with the given Ids.
	 * @param scmRepositoriesIds The Ids of the ScmRepositories to delete.
	 */
	void deleteScmRepositories(Collection<Long> scmRepositoriesIds);
}
