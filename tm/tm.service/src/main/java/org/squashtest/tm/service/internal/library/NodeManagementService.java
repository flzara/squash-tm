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
package org.squashtest.tm.service.internal.library;

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.library.Folder;
import org.squashtest.tm.domain.library.LibraryNode;
import org.squashtest.tm.exception.DuplicateNameException;

public interface NodeManagementService<MANAGED extends LibraryNode, NODE extends LibraryNode, FOLDER extends Folder<NODE>> {

	/**
	 * Finds a node by its ID.
	 *
	 * @param nodeId
	 * @return
	 */
	@Transactional(readOnly = true)
	MANAGED findNode(long nodeId);

	/**
	 * Removes a node from repository by its ID.
	 *
	 * @param nodeId
	 */
	void removeNode(long nodeId);

	/**
	 * Renames a node by its ID after checking that there is no name clash in the node's container.
	 *
	 * @param nodeId
	 * @param newName
	 * @throws DuplicateNameException
	 *             if the node's container already contains a node with the new name.
	 */
	void renameNode(long nodeId, String newName) throws DuplicateNameException;

	/***
	 * This method updates a node description
	 *
	 * @param nodeId
	 *            the node id
	 * @param newDescription
	 *            the new description (String)
	 */
	void updateNodeDescription(long nodeId, String newDescription);

}