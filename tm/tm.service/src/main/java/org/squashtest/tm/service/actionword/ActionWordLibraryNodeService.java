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

import org.squashtest.tm.domain.actionword.ActionWordLibraryNode;
import org.squashtest.tm.domain.actionword.ActionWordTreeEntity;
import org.squashtest.tm.domain.tree.TreeEntity;
import org.squashtest.tm.exception.NameAlreadyInUseException;

public interface ActionWordLibraryNodeService {

	/**
	 * Add a new {@link ActionWordLibraryNode}.
	 * The caller is responsible for giving a not null, named {@link TreeEntity}.
	 * The service will persist the entity, create and persist the node and make links.
	 * <br/>
	 * <br/>
	 * WARNING :
	 * This method clear the hibernate session. The @any mapping in {@link ActionWordLibraryNode}
	 * requires a proper persist and reload to have an updated node and entity.
	 *
	 * @param parentId Id of parent node. Can't be null.
	 * @return The created node.
	 */
	ActionWordLibraryNode createNewNode(Long parentId, ActionWordTreeEntity entity) throws NameAlreadyInUseException;
	/**
	 * Find the {@link ActionWordLibraryNode} linked to a {@link ActionWordTreeEntity}.
	 * @param actionWordTreeEntity The ActionWordTreeEntity
	 * @return The requested ActionWordLibraryNode
	 */
	ActionWordLibraryNode findNodeFromEntity(ActionWordTreeEntity actionWordTreeEntity);
}
