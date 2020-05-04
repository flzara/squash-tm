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
}
