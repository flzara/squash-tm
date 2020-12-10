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

import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.actionword.ActionWordLibraryNode;
import org.squashtest.tm.domain.actionword.ActionWordTreeEntity;
import org.squashtest.tm.domain.tree.TreeLibraryNode;

import java.util.ArrayList;
import java.util.List;

@Component
public class ActionWordLibraryNodeCopier {

	public boolean simulateCopyNodes(List<ActionWordLibraryNode> nodes, ActionWordLibraryNode target) {
		for (ActionWordLibraryNode node : nodes) {
			if (target.childNameAlreadyUsed(node.getName())) {
				return false;
			}
		}
		return true;
	}

	public List<ActionWordLibraryNode> copyNodes(List<ActionWordLibraryNode> nodes, ActionWordLibraryNode target) {
		List<ActionWordLibraryNode> copiedNodes = new ArrayList();
		for (ActionWordLibraryNode node : nodes) {
			if (!target.childNameAlreadyUsed(node.getName())) {
				ActionWordLibraryNode copy = createFirstLayerCopy(node, target);
				target.addChild(copy);
				copiedNodes.add(copy);
			}
		}
		return copiedNodes;
	}

	private ActionWordLibraryNode createFirstLayerCopy(ActionWordLibraryNode node, ActionWordLibraryNode target) {
		ActionWordLibraryNode copy = createBasicCopy(node, target);
		for (TreeLibraryNode child : node.getChildren()) {
			createSubTreeCopy((ActionWordLibraryNode) child,copy);
		}
		return copy;
	}

	private ActionWordLibraryNode createSubTreeCopy(ActionWordLibraryNode node, ActionWordLibraryNode target) {
		ActionWordLibraryNode copy = createBasicCopy(node, target);
		target.addChild(copy);
		for (TreeLibraryNode child : node.getChildren()) {
			createSubTreeCopy((ActionWordLibraryNode) child,copy);
		}
		return copy;
	}

	private ActionWordLibraryNode createBasicCopy(ActionWordLibraryNode node, ActionWordLibraryNode target) {
		ActionWordLibraryNode copy = new ActionWordLibraryNode();
		copy.setLibrary(target.getLibrary());
		copy.setName(node.getName());
		copyTreeEntity(node, copy);
		return copy;
	}

	private void copyTreeEntity(ActionWordLibraryNode node, ActionWordLibraryNode copy) {
		ActionWordTreeEntity treeEntity = node.getEntity().createCopy();
		treeEntity.setProject(copy.getLibrary().getProject());
		copy.setEntity(treeEntity);
		copy.setEntityType(node.getEntityType());
	}

}
