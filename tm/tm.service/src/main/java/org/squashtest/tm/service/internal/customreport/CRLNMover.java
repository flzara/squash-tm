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
package org.squashtest.tm.service.internal.customreport;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.customreport.CustomReportLibraryNode;
import org.squashtest.tm.domain.tree.TreeLibraryNode;
import org.squashtest.tm.service.security.PermissionEvaluationService;

/**
 * Created by jthebault on 29/02/2016.
 */
@Component
public class CRLNMover {

	private final String copySuffix = "-copy-";

	@Inject
	private NameResolver nameResolver;

	@Inject
	private PermissionEvaluationService permissionEvaluationService;

	public void moveNodes(List<CustomReportLibraryNode> nodes, CustomReportLibraryNode target) {
		if (userCanMoveNodesToTarget(target)) {
			moveFirstLayerNodes(nodes, target);
		}
	}

	private void moveFirstLayerNodes(List<CustomReportLibraryNode> nodes, CustomReportLibraryNode target) {
		for (CustomReportLibraryNode node : nodes) {
			if(targetIsNotOriginalParent(target, node) && targetIsNotSelf(target,node)){
				moveOneFirstLayerNode(target, node);
			}
		}
	}

	private void moveOneFirstLayerNode(CustomReportLibraryNode target, CustomReportLibraryNode node) {
		if (userCanDeleteMovedNode(node)) {
			TreeLibraryNode parent = node.getParent();
			nameResolver.resolveNewName(node,target);
			parent.removeChild(node);
			changeNodeLibrary(node, target);
			target.addChild(node);
			moveChild(node,target);
		}
	}

	private void moveChild(CustomReportLibraryNode node, CustomReportLibraryNode target) {
		changeNodeLibrary(node,target);
		for (TreeLibraryNode childNode : node.getChildren()) {
			moveChild((CustomReportLibraryNode) childNode,target);
		}
	}

	private void changeNodeLibrary(CustomReportLibraryNode node, CustomReportLibraryNode target) {
		node.setLibrary(target.getCustomReportLibrary());
		node.getEntity().setProject(target.getCustomReportLibrary().getProject());
	}

	private boolean userCanMoveNodesToTarget (CustomReportLibraryNode target){
		return permissionEvaluationService.hasRoleOrPermissionOnObject("ROLE_ADMIN","WRITE",target);
	}

	private boolean userCanDeleteMovedNode (CustomReportLibraryNode node){
		return permissionEvaluationService.hasRoleOrPermissionOnObject("ROLE_ADMIN","DELETE",node);
	}

	private boolean targetIsNotOriginalParent(CustomReportLibraryNode target, CustomReportLibraryNode node) {
		return !node.getParent().equals(target);
	}

	private boolean targetIsNotSelf(CustomReportLibraryNode target, CustomReportLibraryNode node) {
		return !target.equals(node);
	}

}
