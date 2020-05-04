package org.squashtest.tm.domain.actionword;

import org.squashtest.tm.domain.bdd.ActionWord;

public interface ActionWordTreeEntityVisitor {
	void visit(ActionWordLibrary actionWordLibrary);
	void visit(ActionWord actionWord);
}
