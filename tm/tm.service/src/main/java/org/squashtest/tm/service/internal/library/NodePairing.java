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

import java.util.ArrayList;
import java.util.List;

import org.squashtest.tm.domain.library.NodeContainer;
import org.squashtest.tm.domain.library.TreeNode;

class NodePairing {
	
	private NodeContainer<TreeNode> container;
	private List<TreeNode> newContent = new ArrayList<>();
	
	NodePairing(NodeContainer<TreeNode> container){
		super();
		this.container = container;
	}
	
	NodePairing(NodeContainer<TreeNode> container, List<TreeNode> newContent){
		this(container);
		this.newContent = newContent;
	}
	
	
	void addContent(TreeNode node){
		this.newContent.add(node);
	}
	
	void cancelContent(TreeNode node){
		this.newContent.remove(node);
	}
	
	NodeContainer<TreeNode> getContainer(){
		return container;
	}
	
	List<TreeNode> getNewContent(){
		return newContent;
	}
	
}
