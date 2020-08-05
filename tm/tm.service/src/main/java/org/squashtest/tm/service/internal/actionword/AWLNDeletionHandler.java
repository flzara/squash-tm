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

import org.apache.commons.collections.Closure;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.actionword.ActionWordLibraryNode;
import org.squashtest.tm.domain.actionword.ActionWordTreeLibraryNode;
import org.squashtest.tm.service.deletion.OperationReport;
import org.squashtest.tm.service.deletion.SuppressionPreviewReport;
import org.squashtest.tm.service.internal.customreport.TreeLibraryNodeDeletionHandler;
import org.squashtest.tm.service.internal.deletion.LockedFolderInferenceTree;
import org.squashtest.tm.service.internal.deletion.LockedFolderInferenceTree.Node;
import org.squashtest.tm.service.internal.repository.ActionWordLibraryNodeDao;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Component
public class AWLNDeletionHandler implements
        TreeLibraryNodeDeletionHandler<ActionWordLibraryNode> {

	@Inject
	private ActionWordLibraryNodeDao actionWordLibraryNodeDao;

	@Override
	public List<SuppressionPreviewReport> simulateDeletion(List<Long> targetIds) {
		return new ArrayList<>();
	}

	@Override
	public OperationReport deleteNodes(List<Long> targetIds) {
		final OperationReport operationReport = new OperationReport();
		LockedFolderInferenceTree tree = createTree(targetIds);
		Closure closure = input -> {
			Node node = (Node) input;
			doOneDelete(node.getKey(), operationReport);
		};

		tree.doBottomUp(closure);
		return operationReport;
	}

	private void doOneDelete(Long nodeId, OperationReport operationReport){
		ActionWordLibraryNode targetNode = actionWordLibraryNodeDao.getOne(nodeId);
		ActionWordTreeLibraryNode parentNode = targetNode.getParent();
		parentNode.removeChild(targetNode);
		AWLNDeletionVisitor visitor = new AWLNDeletionVisitor(operationReport, targetNode);
		targetNode.getEntity().accept(visitor);
		actionWordLibraryNodeDao.delete(targetNode);
	}


	private LockedFolderInferenceTree createTree(List<Long> targetIds){
		int descendantNumber = targetIds.size();
		List<Long> currentLayerIds = targetIds;
		LockedFolderInferenceTree tree = new LockedFolderInferenceTree();
		List<Long[]> pairedIds = new ArrayList<>();

		//adding the root nodes to tree
		addMultipleChildToParent(null,targetIds,pairedIds);

		while (descendantNumber>0) {
			for (Long id : currentLayerIds) {
				List<Long> descendantIds = actionWordLibraryNodeDao.findAllFirstLevelDescendantIds(id);
				addMultipleChildToParent(id, descendantIds, pairedIds);
				currentLayerIds = descendantIds;
				descendantNumber = descendantIds.size();
			}
		}
		tree.build(pairedIds);
		return tree;
	}

	private void addMultipleChildToParent(Long parentId, List<Long> childIds, List<Long[]> pairs){
		for (Long id : childIds) {
			addPairToList(parentId, id, pairs);
		}
	}

	private void addPairToList(Long parentId, Long childId, List<Long[]> pairs){
		Long[] pair = { parentId, childId };
		pairs.add(pair);
	}

}
