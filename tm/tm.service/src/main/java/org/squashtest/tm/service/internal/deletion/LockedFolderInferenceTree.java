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
package org.squashtest.tm.service.internal.deletion;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections.Closure;
import org.squashtest.tm.domain.library.structures.LibraryTree;
import org.squashtest.tm.domain.library.structures.TreeNode;

 public class LockedFolderInferenceTree extends LibraryTree<Long, LockedFolderInferenceTree.Node> {


	/**
	 * will populate the tree. Default status for nodes is 'deletable'.
	 * The input structure pair the id of parent and child nodes.
	 * 
	 * @param pairedIds
	 */
	public void build(List<Long[]> pairedIds){

		List<TreeNodePair> allData = new LinkedList<>();

		for (Long[] pair : pairedIds){
			Long parentKey = pair[0];
			Node childData = new Node(pair[1], true);

			allData.add(new TreeNodePair(parentKey, childData));
		}

		addNodes(allData);

	}


	public void resolveLockedFolders(){
		doBottomUp(new Closure(){
			@Override
			public void execute(Object input) {
				Node node = (Node) input;

				//if the node is already locked we skip it.
				if (! node.isDeletable()){ return;}

				//otherwise let's loop. A node is locked if all at least one child is locked.
				boolean isDeletable = true;

				for (Node child : node.getChildren()){
					if (! child.isDeletable()){
						isDeletable=false;
						break;
					}
				}
				node.setDeletable(isDeletable);
			}
		});

	}

	/**
	 * will mark the nodes identified by their key as locked
	 * 
	 * @param nodeKeys
	 * @param isDeletable
	 */
	public void markLockedNodes(List<Long> nodeKeys){
		List<Node> toUpdate = new LinkedList<>();

		for (Long key : nodeKeys){
			toUpdate.add(new Node(key, false));
		}

		merge(toUpdate);
	}

	/**
	 * will mark the nodes identified by their key as deletable
	 * 
	 * @param nodeKeys
	 * @param isDeletable
	 */
	public void markNodesAsDeletable(List<Long> nodeKeys){
		List<Node> toUpdate = new LinkedList<>();

		for (Long key : nodeKeys){
			toUpdate.add(new Node(key, true));
		}

		merge(toUpdate);
	}



	public List<Long> collectDeletableIds(){

		List<Long> deletableIds = new LinkedList<>();

		for (Node node : getAllNodes()){
			if (node.isDeletable()){
				deletableIds.add(node.getKey());
			}
		}

		return deletableIds;
	}

	public List<Long> collectLockedIds(){

		List<Long> lockedIds = new LinkedList<>();

		for (Node node : getAllNodes()){
			if (! node.isDeletable()){
				lockedIds.add(node.getKey());
			}
		}

		return lockedIds;
	}



	public static class Node extends TreeNode<Long, Node>{

		private Boolean deletable = null;

		public Boolean isDeletable(){
			return deletable;
		}

		public void setDeletable(Boolean isDeletable){
			this.deletable=isDeletable;
		}

		public Node(){
			super();
		}

		public Node(Long key){
			super(key);
		}

		public Node(Long key, Boolean deletable){
			super(key);
			this.deletable =  deletable;
		}


		@Override
		public void updateWith(LockedFolderInferenceTree.Node newData){
			this.deletable = newData.isDeletable();
		}



	}

}
