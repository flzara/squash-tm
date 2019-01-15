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

import java.io.IOException;
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
	 * Find the ScmRepositories contained in the ScmServer with the given Id formatted as a Page to comply the given Pageable.
	 * @param scmServerId The Id of the ScmServer containing the wanted ScmRepositories.
	 * @param pageable The Pageable against which the Page will be built.
	 * @return The Page of ScmRepositories built according to the given Pageable.
	 */
	Page<ScmRepository> findPagedScmRepositoriesByScmServer(Long scmServerId, Pageable pageable);
	/**
	 * Create a new ScmRepository with its attributes and bind it to the given ScmServer.
	 * This implies the creation of the local repository in the file system.
	 * @param scmServerId The Id of the ScmServer which contains the new ScmRepository.
	 * @param newScmRepository The ScmRepository with its attributes to create.
	 * @throws IOException If an error occurs when creating the local repository.
	 */
	void createNewScmRepository(long scmServerId, ScmRepository newScmRepository) throws IOException;
	/**
	 * Update the working branch of the ScmRepository with the given Id to the new given branch.
	 * This implies the modification of the local repository state.
	 * @param scmRepositoryId The Id of the ScmRepository which branch is to update.
	 * @param newBranch The new branch of the ScmRepository.
	 * @return The new branch of the ScmRepository.
	 * @throws IOException If an error occurs when modifying the local repository state.
	 */
	String updateBranch(long scmRepositoryId, String newBranch) throws IOException;
	/**
	 * Delete the ScmRepositories with the given Ids.
	 * @param scmRepositoriesIds The Ids of the ScmRepositories to delete.
	 */
	void deleteScmRepositories(Collection<Long> scmRepositoriesIds);
	/**
	 * Check if at least one of the ScmRepositories with the given Ids is bound to a Project.
	 * @param scmRepositoryIds The Ids of the ScmRepositories
	 * @return True if at least one of the given ScmRepository is bound to a Project.
	 * False otherwise.
	 */
	boolean isOneRepositoryBoundToProject(Collection<Long> scmRepositoryIds);
}
