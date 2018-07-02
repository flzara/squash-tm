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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.Closure;
import org.squashtest.tm.domain.library.structures.LibraryTree;
import org.squashtest.tm.domain.library.structures.TreeNode;

public class SubRequirementRewiringTree extends LibraryTree<Long, SubRequirementRewiringTree.Node> {

	private List<Movement> movements = new ArrayList<>();

	// this method has to make sure no duplicate entries make it into the tree
	public void build(List<Long[]> pairedIds){

		Set<IdPair> allData = new HashSet<>();

		// make sure all pairs are unique
		for (Long[] pair : pairedIds){
			allData.add(new IdPair(pair[0], pair[1]));
		}



		List<TreeNodePair> nodes  = new ArrayList<>();
		for (IdPair p : allData){
			Long parentId = p.getId1();
			Long childId = p.getId2();
			nodes.add(new TreeNodePair(parentId, new Node(childId, false)));
		}

		addNodes(nodes);

	}


	public void markDeletableNodes(List<Long> nodeKeys){
		List<Node> toUpdate = new LinkedList<>();

		for (Long key : nodeKeys){
			toUpdate.add(new Node(key, true));
		}

		merge(toUpdate);
	}


	List<Movement> getNodeMovements(){
		return movements;
	}


	void resolveMovements(){
		doBottomUp(new MovementResolver(this));
	}


	/*
	 * This closure must execute bottom-up. This mean it will
	 * always execute on leaves first.
	 *
	 * The goal is for each node :
	 * 	- if deletable -> screw it
	 *  - if non deletable : find a parent where to attach
	 */
	private static final class MovementResolver implements Closure{

		private SubRequirementRewiringTree tree;

		MovementResolver(SubRequirementRewiringTree tree){
			this.tree = tree;
		}

		@Override
		public void execute(Object input) {
			Node node = (Node)input;

			// if deletable -> screw it
			if (node.isDeletable()){
				return;
			}

			// non deletable : find a parent where to bind
			Node parent = node.getParent();

			// test if we need to rewire
			if (parent != null && parent.isDeletable()){

				// it appears we need to rewire -> let's find a suitable parent
				Node newParent = parent.getParent();
				while (newParent != null && newParent.isDeletable()){
					parent = newParent;
					newParent = newParent.getParent();
				}

				Movement mouv;
				if (newParent != null){
					mouv = tree.findMovementToKnownParent(newParent);
				}
				else{
					mouv = tree.findMovementToUnknownParent(parent);
				}

				mouv.addChild(node.getKey());
			}

		}

	}




	private Movement findMovement(Node parent, boolean parentFlag){

		Movement found = null;
		Long parentId = parent.getKey();

		for (Movement m : movements){
			if (m.isTheParentOf() == parentFlag && m.getId().equals(parentId)){
				found = m;
				break;
			}
		}

		// create it on the fly if needed
		if (found == null){
			found = new Movement(parentFlag, parentId);
			movements.add(found);
		}

		return found;

	}


	// find a movement where the new parent is known to the graph
	private Movement findMovementToKnownParent(Node parent){
		return findMovement(parent, false);
	}

	// find a movement where the new parent is not yet known to the graph
	private Movement findMovementToUnknownParent(Node parent){
		return findMovement(parent, true);
	}



	public static class Movement{

		// this flag says "this refers to the parent of node #id, not the node #id itself"
		private boolean theParentOf = false;
		private Long id;
		private Set<Long> newChildren = new HashSet<>();

		public Movement(boolean theParentOf, Long id) {
			super();
			this.theParentOf = theParentOf;
			this.id = id;
		}

		Set<Long> getNewChildren(){
			return newChildren;
		}

		void addChild(Long id){
			newChildren.add(id);
		}

		boolean isTheParentOf() {
			return theParentOf;
		}

		Long getId() {
			return id;
		}

	}


	static class Node extends TreeNode<Long, Node>{

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
		protected void updateWith(Node newData) {
			this.deletable = newData.isDeletable();
		}


		public Boolean getDeletable() {
			return deletable;
		}



	}

	private static final class IdPair{
		private Long id1;
		private Long id2;

		IdPair(Long id1, Long id2){
			this.id1 = id1;
			this.id2 = id2;
		}

		public Long getId1() {
			return id1;
		}

		public Long getId2() {
			return id2;
		}

		// GENERATED:START
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (id1 == null ? 0 : id1.hashCode());
			result = prime * result + (id2 == null ? 0 : id2.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			IdPair other = (IdPair) obj;
			if (id1 == null) {
				if (other.id1 != null) {
					return false;
				}
			} else if (!id1.equals(other.id1)) {
				return false;
			}
			if (id2 == null) {
				if (other.id2 != null) {
					return false;
				}
			} else if (!id2.equals(other.id2)) {
				return false;
			}
			return true;
		}
		// GENERATED:END

	}

}
