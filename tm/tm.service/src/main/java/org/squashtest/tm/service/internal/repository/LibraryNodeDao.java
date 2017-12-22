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

import java.util.List;

import org.squashtest.tm.domain.library.LibraryNode;

public interface LibraryNodeDao<NODE extends LibraryNode> extends EntityDao<NODE>{

	/**
	 * Returns the path of the given entity. The path is the concatenation of the ancestor names, sorted by ancestry. It does not begin with /&ltproject-name&gt;
	 * 
	 * @param entityId
	 * @return
	 */
	List<String> getParentsName(long entityId);

	/**
	 * Returns the ids path. The path is a list of ids sorted by ancestry: first = elder, last = younger.
	 * The list contains only ids of library nodes.
	 * 
	 * @param entityId
	 * @return ids of all entity parents sorted from elder to younger.
	 */
	List<Long> getParentsIds(long entityId);

	/**
	 * Returns the ids of the node for each path in list. 
	 * The paths must be the splits of an unique path. If a node doesn't exist, insert a null in the list
	 * @param paths The paths list. This method expect that the first path of the list is the project.
	 * @return ids of all entity sorted from elder to younger. The list size is @param paths size -1 because first path in list is the project.
	 */
	List<Long> findNodeIdsByPath(List<String> paths);

	Long findNodeIdByPath(String path);

}
