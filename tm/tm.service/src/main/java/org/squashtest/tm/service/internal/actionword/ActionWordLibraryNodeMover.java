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
import org.squashtest.tm.domain.actionword.ActionWordTreeLibraryNode;
import org.squashtest.tm.service.security.PermissionEvaluationService;

import javax.inject.Inject;
import java.util.List;

@Component
public class ActionWordLibraryNodeMover {

	@Inject
	private PermissionEvaluationService permissionEvaluationService;

	public void moveNodes(List<ActionWordLibraryNode> nodes, ActionWordLibraryNode target) {
		if (userCanMoveNodesToTarget(target)) {
			moveFirstLayerNodes(nodes, target);
		}
	}

	private void moveFirstLayerNodes(List<ActionWordLibraryNode> nodes, ActionWordLibraryNode target) {
		for (ActionWordLibraryNode node : nodes) {
			if (targetIsNotOriginalParent(target, node) && targetIsNotSelf(target, node)) {
				moveOneFirstLayerNode(target, node);
			}
		}
	}

	private void moveOneFirstLayerNode(ActionWordLibraryNode target, ActionWordLibraryNode node) {
		if (!target.childNameAlreadyUsed(node.getName()) && userCanDeleteMovedNode(node)) {
			ActionWordTreeLibraryNode parent = node.getParent();
			parent.removeChild(node);
			changeNodeLibrary(node, target);
			target.addChild(node);
		}
	}

	private void changeNodeLibrary(ActionWordLibraryNode node, ActionWordLibraryNode target) {
		node.setLibrary(target.getLibrary());
		node.getEntity().setProject(target.getLibrary().getProject());
	}

	private boolean userCanDeleteMovedNode(ActionWordLibraryNode node) {
		return permissionEvaluationService.hasRoleOrPermissionOnObject("ROLE_ADMIN", "DELETE", node);
	}

	private boolean targetIsNotSelf(ActionWordLibraryNode target, ActionWordLibraryNode node) {
		return !target.equals(node);
	}

	private boolean targetIsNotOriginalParent(ActionWordLibraryNode target, ActionWordLibraryNode node) {
		return !node.getParent().equals(target);
	}

	private boolean userCanMoveNodesToTarget(ActionWordLibraryNode target) {
		return permissionEvaluationService.hasRoleOrPermissionOnObject("ROLE_ADMIN", "WRITE", target);
	}
}
