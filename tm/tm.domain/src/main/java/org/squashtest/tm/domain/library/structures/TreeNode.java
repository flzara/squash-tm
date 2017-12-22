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
package org.squashtest.tm.domain.library.structures;


import java.util.ArrayList;
import java.util.List;

/**
 * 
 * <p>Please read also {@link LibraryTree}.</p>
 * 
 * <p>
 *  TreeNode is the type of node used by a LibraryTree. A TreeNode maintains informations regarding its position in the tree, ie its parent node, its layer/depth etc. Each node is identified
 *  by a key, that will be used to identify each node uniquely.
 * </p>
 * 
 * <p>
 *  Subclasses of TreeNode should be genericized <i>Enum</i>-style, i.e. generics of themselves, and implement {@link #updateWith(TreeNode)}.
 * </p>
 * 
 * 
 * @author bsiri
 *
 * @param <T> the type of the actual subclass.
 */

public abstract class TreeNode<IDENT, T extends TreeNode<IDENT, T>> {

	private final List<T> children = new ArrayList<>();
	private  T parent ;
	private LibraryTree<IDENT, T> tree ;

	private int depth;
	private IDENT key;


	/**
	 * 
	 * @return the children nodes of this node.
	 */
	public List<T> getChildren(){
		return children ;
	}


	public TreeNode(){

	}

	public TreeNode(IDENT key){
		this.key=key;
	}


	LibraryTree<IDENT, T> getTree(){
		return tree;
	}


	void setTree(LibraryTree<IDENT, T> tree){
		this.tree= tree;
	}


	public T getParent(){
		return parent;
	}


	public IDENT getKey() {
		return key;
	}

	public void setKey(IDENT key) {
		this.key = key;
	}

	/**
	 * 
	 * @return the depth of that node, i.e. the layer depth it belongs to.
	 */
	int getDepth(){
		return depth;
	}


	void setDepth(int depth){
		this.depth=depth;
	}

	/**
	 * @return the list of the ancestors of this node, from its parent to the ancestor root node (they come in reverse order).
	 */
	List<T> getHierarchy(){
		List<T> result = new ArrayList<>();

		T nodeIterator = (T)this;

		while(nodeIterator!=null){
			result.add(nodeIterator);
			nodeIterator=nodeIterator.getParent();
		}

		return result;

	}


	/**
	 * Resets and recomputes the depth of the node from scratch.
	 */
	void recomputeDepth(){
		depth = getHierarchy().size() - 1;
	}


	void setParent(T parent){
		this.parent=parent;
	}

	/**
	 * Adds a child to this node and wire their properties accordingly.
	 * 
	 * @param child the new child.
	 */
	void addChild(T child){
		child.setParent((T)this);
		child.setTree(tree);
		child.setDepth(depth+1);
		children.add(child);
	}


	/**
	 * strongly recommended to override/specialize that method when you subtype that class.
	 * @param newData
	 */
	protected abstract void updateWith(T newData);

}
