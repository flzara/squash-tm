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
import org.squashtest.tm.domain.scm.ScmServer;

import java.util.Collection;
import java.util.List;

public interface ScmServerManagerService {

	/**
	 * Find all ScmServers ordered by name.
	 * @return The List of ScmServers ordered by name.
	 */
	List<ScmServer> findAllOrderByName();
	/**
	 * Find all ScmServers sorted according to the given Pageable.
	 * @param pageable The Pageable against which the Page will be built.
	 * @return The Page of ScmServers sorted according to the given Pageable.
	 */
	Page<ScmServer> findAllSortedScmServers(Pageable pageable);
	/**
	 * Find the ScmServer with the given Id.
	 * @param scmServerId The Id of the ScmServer to find.
	 * @return The ScmServer with the given Id.
	 */
	ScmServer findScmServer(long scmServerId);
	/**
	 * Create a new ScmServer with its attributes.
	 * @param newScmServer The ScmServer with its attributes to create.
	 * @return The ScmServer newly created
	 */
	ScmServer createNewScmServer(ScmServer newScmServer);
	/**
	 * Update the name of the ScmServer with the given Id with the given name.
	 * @param scmServerId The Id of the ScmServer which name is to update.
	 * @param newName The new name to give the ScmServer.
	 * @return The new name of the ScmServer.
	 */
	String updateName(long scmServerId, String newName);
	/**
	 * Update the Url of the ScmServer with the given Id with the given Url.
	 * @param scmServerId The Id of the ScmServer which Url is to update.
	 * @param url The new Url to give to the scmServer
	 * @return The new Url of the ScmServer.
	 */
	String updateUrl(long scmServerId, String url);
	/**
	 * Update the kind of the ScmServer with the given Id with the given kind.
	 * @param scmServerId The Id of the ScmServer which kind is to update.
	 * @param kind The new kind to give to the scmServer
	 * @return The new kind of the ScmServer.
	 */
	String updateKind(long scmServerId, String kind);
	/**
	 * Delete the ScmServers with the given Ids. Also deletes the ScmRepositories contained in these ScmServers.
	 * @param scmServerIds The Ids of the ScmServers to delete.
	 */
	void deleteScmServers(Collection<Long> scmServerIds);

}
