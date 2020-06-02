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
package org.squashtest.tm.service.actionword;

import org.squashtest.tm.domain.actionword.ActionWordLibrary;
import org.squashtest.tm.domain.actionword.ActionWordLibraryNode;
import org.squashtest.tm.domain.actionword.ActionWordTreeEntity;
import org.squashtest.tm.domain.bdd.ActionWord;
import org.squashtest.tm.exception.DuplicateNameException;

public interface ActionWordLibraryNodeService {
	/**
	 * Finds an {@linkplain ActionWordLibraryNode} given its id.
	 * @param nodeId the id of the requested {@linkplain ActionWordLibraryNode}
	 * @return the requested {@linkplain ActionWordLibraryNode}
	 */
	ActionWordLibraryNode findActionWordLibraryNodeById(Long nodeId);
	/**
	 * Finds an {@linkplain ActionWordLibrary} given its node id
	 * (i.e. the id of the corresponding {@linkplain ActionWordLibraryNode}).
	 * @param nodeId the id of the {@linkplain ActionWordLibraryNode}
	 * @return the requested {@linkplain ActionWordLibrary}
	 */
	ActionWordLibrary findLibraryByNodeId(Long nodeId);
	/**
	 * Finds an {@linkplain ActionWord} given its node id
	 * (i.e. the id of the corresponding {@linkplain ActionWordLibraryNode}).
	 * @param nodeId the id of the {@linkplain ActionWordLibraryNode}
	 * @return the requested {@linkplain ActionWord}
	 */
	ActionWord findActionWordByNodeId(Long nodeId);
	/**
	 * Add a new {@link ActionWordLibraryNode}.
	 * The caller is responsible for giving a not null, named {@link ActionWordTreeEntity}.
	 * The service will persist the entity, create and persist the node and make links.
	 * @param parentId Id of parent node. Can't be null.
	 * @return The created node.
	 */
	ActionWordLibraryNode createNewNode(Long parentId, ActionWordTreeEntity entity) throws DuplicateNameException;
	/**
	 * Find the {@link ActionWordLibraryNode} linked to a {@link ActionWordTreeEntity}.
	 * @param actionWordTreeEntity The ActionWordTreeEntity
	 * @return The requested ActionWordLibraryNode
	 */
	ActionWordLibraryNode findNodeFromEntity(ActionWordTreeEntity actionWordTreeEntity);
	/**
	 * Given an ActionWord which Fragments were modified, rename its corresponding ActionWordLibraryNode.
	 * @param actionWord the given ActionWord
	 */
	void renameNodeFromActionWord(ActionWord actionWord);
}
