package org.squashtest.tm.service.internal.actionword;

import org.squashtest.tm.domain.actionword.ActionWordLibrary;
import org.squashtest.tm.domain.actionword.ActionWordLibraryNode;
import org.squashtest.tm.domain.actionword.ActionWordTreeEntity;
import org.squashtest.tm.domain.actionword.ActionWordTreeEntityVisitor;
import org.squashtest.tm.domain.bdd.ActionWord;

/**
 * Builder for new {@link ActionWordLibraryNode}.
 */
public class ActionWordLibraryNodeBuilder implements ActionWordTreeEntityVisitor {

	private ActionWordLibraryNode builtNode;
	private ActionWordLibraryNode parentNode;
	private ActionWordTreeEntity treeEntity;

	public ActionWordLibraryNodeBuilder(ActionWordLibraryNode parentNode, ActionWordTreeEntity treeEntity) {
		this.builtNode = new ActionWordLibraryNode();
		this.parentNode = parentNode;
		this.treeEntity = treeEntity;
	}

	public ActionWordLibraryNode build() {
		nameBuiltNode();
		linkEntity();
		linkToParent();
		treeEntity.accept(this);
		return builtNode;
	}

	private void nameBuiltNode() {
		builtNode.setName(treeEntity.getName());
	}

	private void linkEntity() {
		builtNode.setEntity(treeEntity);
	}

	private void linkToParent() {
		parentNode.addChild(builtNode);
		builtNode.setLibrary(parentNode.getLibrary());
	}

	/* Visitor methods */
	@Override
	public void visit(ActionWordLibrary actionWordLibrary) {
		// NOOP
	}

	@Override
	public void visit(ActionWord actionWord) {
		linkToProject();
	}

	private void linkToProject() {
		treeEntity.setProject(parentNode.getLibrary().getProject());
	}
}
