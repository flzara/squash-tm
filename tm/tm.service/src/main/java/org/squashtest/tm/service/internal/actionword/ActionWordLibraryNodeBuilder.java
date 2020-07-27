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
		builtNode.setName(actionWord.createWord());
	}

	private void linkToProject() {
		treeEntity.setProject(parentNode.getLibrary().getProject());
	}
}
