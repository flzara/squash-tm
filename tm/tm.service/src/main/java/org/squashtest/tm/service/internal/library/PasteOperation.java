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
package org.squashtest.tm.service.internal.library;

import java.util.List;

import org.squashtest.tm.domain.library.NodeContainer;
import org.squashtest.tm.domain.library.TreeNode;

public interface PasteOperation {

	/**
	 * <p>
	 * Some operation involving a node and another node will happen, ie the source will be copied or moved to the destination. 
	 * Also returns a TreeNode resulting from this operation,  that will be either the copy of the source or the source itself.
	 * </p>
	 * 
	 * <p>
	 * If a position is specified, the source node will be copied/moved to its destination at this position. If position
	 * is left to null, it will be appended to the content of the destination.
	 * 
	 * </p>
	 */
	TreeNode performOperation(TreeNode source, NodeContainer<TreeNode> destination, Integer position);


	
	/**
	 * will say if the operation allows to go process node's childrens after operation.
	 * see {@link TreeNodeCopier#visit(org.squashtest.tm.domain.campaign.Iteration)} for an example of when it is not ok.
	 * 
	 * @return
	 */
	boolean isOkToGoDeeper();
	
	List<Long> getRequirementVersionToIndex();
	
	List<Long> getTestCaseToIndex();
	
}
