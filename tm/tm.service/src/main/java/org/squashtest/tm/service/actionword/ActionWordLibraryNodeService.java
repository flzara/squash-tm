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
import org.squashtest.tm.domain.customreport.CustomReportLibraryNode;
import org.squashtest.tm.domain.tree.TreeLibraryNode;
import org.squashtest.tm.exception.DuplicateNameException;
import org.squashtest.tm.service.deletion.OperationReport;

import java.util.List;

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
	 * Find the {@link ActionWordLibraryNode} linked to a {@link ActionWordTreeEntity}.
	 * @param actionWordTreeEntity The ActionWordTreeEntity
	 * @return The requested ActionWordLibraryNode
	 */
	ActionWordLibraryNode findNodeFromEntity(ActionWordTreeEntity actionWordTreeEntity);
	/**
	 * Get an Action word (not Library) Node path relative to its library/project
	 * @param nodeId current node id
	 * @return Action word node path
	 */
	String findActionWordLibraryNodePathById(Long nodeId);

	/**
	 * Add a new {@link ActionWordLibraryNode}.
	 * The caller is responsible for giving a not null, named {@link ActionWordTreeEntity}.
	 * The service will persist the entity, create and persist the node and make links.
	 * @param parentId Id of parent node. Can't be null.
	 * @return The created node.
	 */
	ActionWordLibraryNode createNewNode(Long parentId, ActionWordTreeEntity entity) throws DuplicateNameException;

	/**
	 * Simulate the copy of the ActionWordLibraryNodes matching the ids in the List into the target with the given id.
	 * @param nodeIds
	 * @param targetId
	 * @return True if all node can be copied. False if at least one node cannot be copied because another ActionWord
	 * with the same token already exists in the same Project.
	 */
	boolean simulateCopyNodes(List<Long> nodeIds, long targetId);
	/**
	 * Copy the ActionWordLibraryNodes matching the ids in the list into the target with the given id.
	 * @param nodeIds the ids of ActionWordLibraryNodes to copy
	 * @param targetId the id of the target container
	 * @return a list of the created ActionWordLibraryNodes
	 */
	List<ActionWordLibraryNode> copyNodes(List<Long> nodeIds, long targetId);

	/**
	 * Move the ActionWordLibraryNodes matching the ids in the list into the target with the given id.
	 * @param nodeIds the ids of the ActionWordLibraryNodes to move
	 * @param targetId the id of the target container
	 */
	void moveNodes(List<Long> nodeIds, long targetId);

	/**
	 * Given an ActionWord which Fragments were modified, rename its corresponding ActionWordLibraryNode.
	 * @param actionWord the given ActionWord
	 */
	void renameNodeFromActionWord(ActionWord actionWord);

	/**
	 * Delete one/many {@link ActionWordLibraryNode}
	 * @param nodeIds Node Ids to be deleted
	 */
	OperationReport delete(List<Long> nodeIds);

}
