package org.squashtest.tm.domain.actionword;

import org.squashtest.tm.domain.tree.TreeEntity;

public interface ActionWordTreeEntity extends TreeEntity {
	void accept(ActionWordTreeEntityVisitor visitor);
	ActionWordTreeEntity createCopy();
}
