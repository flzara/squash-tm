package org.squashtest.tm.domain.actionword;

import org.squashtest.tm.domain.tree.TreeLibraryNode;
import org.squashtest.tm.exception.NameAlreadyInUseException;

import java.util.List;

public interface ActionWordTreeLibraryNode extends TreeLibraryNode {
	ActionWordTreeDefinition getEntityType();
	void setEntityType(ActionWordTreeDefinition entityType);

	ActionWordTreeEntity getEntity();
	void setEntity(ActionWordTreeEntity treeEntity);

	ActionWordLibrary getLibrary();
	void setLibrary(ActionWordLibrary library);

	ActionWordTreeLibraryNode getParent();
	void setParent(ActionWordTreeLibraryNode parent);

	List<ActionWordTreeLibraryNode> getChildren();

	void addChild(ActionWordTreeLibraryNode treeLibraryNode)
		throws UnsupportedOperationException, IllegalArgumentException, NameAlreadyInUseException;

	void removeChild(ActionWordTreeLibraryNode treeLibraryNode);
}
