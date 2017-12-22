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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.collections.Closure;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.Transformer;



/*
 * TODO : the current implementation is reaching its limits. Only few cases are implemented for now, but on the long run we may have to
 * implement one specific solution for each of them, unless we make a more generic solution.
 *
 * Basically detecting a network of locked nodes means :
 *
 * 1) build the network of entities that are relevant for the current problem,
 * 2) mark the first locked node (seeds),
 * 3) propagate to other nodes (the meaning of propagation may vary from one problem to another).
 *
 * So instead of using things like trees and graphs and tons of variations of these let's think of a generic and configurable class. That will be useful
 * the day we'll need to chain the results of multiple instances to resolve complex dependencies.
 */


/**
 * <p>
 * This tree can have multiple roots and internally its structure is layered. The details are :
 * <ul>
 *  <li> This is a layered tree : the layer <i>n</i> is the collection of nodes of depth <i>n</i>. Just like nodes, layers can be accessed via the proper methods. </li>
 *  <li> There may be more than one node at layer 0. ie, the root(s), </li>
 *  <li> The parent of a node of layer <i>n</i> belongs to layer <i>n-1</i>, except for layer 0, </li>
 *  <li> The order of two nodes within the same layer is undefined (weak ordering). </li>
 * </ul>
 *
 * The implementation is simple because its only purpose is to provide a structure to store data in. The structure is the very goal here so its not supposed to be structurally modified or
 * rebalanced : its built once and for all.
 *
 * @see TreeNode
 * </p>
 *
 * @param T : the type of the nodes this tree is made of
 * @param IDENT : the type of the key used by a node. As an identifier, it should be immutable and implement
 * equality/hashcode properly. If it's good for a HashSet, it's good for the job here too.
 *
 *  @author bsiri
 */

public class  LibraryTree<IDENT, T extends TreeNode<IDENT, T>>{

	protected final Map<Integer, List<T>> layers = new HashMap<>();


	/**
	 * Given an integer, returns the layer at the corresponding depth in the tree. That integer must be comprised within the acceptable bounds of that tree, ie 0 and {@link #getDepth()}.
	 * The layer is returned as a list of nodes.
	 *
	 * @param depth the depth of the layer we want an access to.
	 * @return the layer as a list of nodes.
	 * @throws IndexOutOfBoundsException
	 */
	public List<T> getLayer(Integer depth){

		if (depth<0){ throw new IndexOutOfBoundsException("Below lower bound : "+depth);}
		if (depth > Collections.max(layers.keySet())){ throw new IndexOutOfBoundsException("Above upper bound : "+depth);}

		return layers.get(depth);

	}

	/**
	 * Given a TreeNodePair (see documentation of the inner class for details), will add the child node to the tree.
	 * If the child node have no parents it will be added to layer 0 (ie, new root).
	 * Else, the child node will belong to the layer following its parent's layer.
	 *
	 * @param newPair a TreeNodePair with informations regarding parent and child node included.
	 * @throws NoSuchElementException if a parent node cannot be found.
	 */
	public void addNode(TreeNodePair newPair){

		T parent = getNode(newPair.getParentKey());
		T childNode = newPair.getChild();

		if (parent==null){

			if (layers.get(0)==null){ layers.put(0, new ArrayList<T>());}

			childNode.setParent(null);
			childNode.setTree(this);
			childNode.setDepth(0);
			layers.get(0).add(childNode);
		}
		else{
			parent.addChild(childNode);

			int layerIndex =childNode.getDepth();
			if (layers.get(layerIndex)==null){ layers.put(layerIndex, new ArrayList<T>());}

			layers.get(layerIndex).add(childNode);
		}

	}

	/**
	 * Same than {@link #addNode(TreeNodePair)}, but the TreeNodePair parameter will be built using the parameter provided here.
	 *
	 * @param parentKey the key designating the parent node.
	 * @param childNode the child we want eventually to insert.
	 */
	public void  addNode(IDENT parentKey, T childNode){
		addNode(new TreeNodePair(parentKey, childNode));
	}



	/**
	 * Accepts a list of TreeNodePair and will add all the nodes in that list (see TreeNodePair and TreeNode). Such list can be called a flat tree and passing one to this method
	 * is a convenient way for tree initialization.
	 * You do not need to pass the TreeNodePairs in any order : the method will take care of inserting them in the correct order (ie parents before children).
	 *
	 * @see {@link #sortData(List)}, TreeNode, TreeNodePair
	 *
	 * @param unsortedData the flat representation of the tree.
	 */
	public void addNodes(List<TreeNodePair> unsortedFlatTree){



		List<TreeNodePair> cleanPairs = cleanData(unsortedFlatTree);

		// first pass : create all the nodes
		Map<IDENT, T> newNodesByKey = new HashMap<>();
		Set<T> rootNodes = new HashSet<>();

		for (TreeNodePair pair : cleanPairs){
			newNodesByKey.put(pair.child.getKey(), pair.child);
			if (pair.parentKey == null){
				rootNodes.add(pair.child);
			}
			pair.child.setTree(this);
		}

		// second pass : bind nodes together
		for (TreeNodePair pair : cleanPairs){
			if (rootNodes.contains(pair.child)){
				continue;	// no parent to bind to
			}
			T parent = newNodesByKey.get(pair.parentKey);
			T child = newNodesByKey.get(pair.child.getKey());

			parent.addChild(child);
		}

		// third pass : integrate to the nodes
		integrateNodes(0, rootNodes);


	}

	private void integrateNodes(int layerindex, Collection<T> nodes){
		if (nodes.isEmpty()){
			return;
		}

		List<T> layer = getLayer(layerindex);

		Collection<T> nextLayer = new ArrayList<>();

		for (T node : nodes){
			node.setDepth(layerindex);
			layer.add(node);
			nextLayer.addAll(node.getChildren());
		}

		integrateNodes(layerindex+1, nextLayer);

	}

	private List<T> getLayer(int index){
		if (layers.get(index) == null){
			layers.put(index, new ArrayList<T>());
		}
		return layers.get(index);
	}



	/**
	 *
	 * <p>This method will clean up some wrong data, for instance the data might say that a given node have multiple parent. In this case the last non null parent will
	 * be selected and the other entries are discarded.</p>
	 *
	 * @param corruptData
	 * @return
	 */
	private List<TreeNodePair> cleanData(List<TreeNodePair> corruptData){

		Map<IDENT, TreeNodePair> pairByChildKey = new HashMap<>();

		for (TreeNodePair pair : corruptData){
			IDENT childKey = pair.getChild().getKey();
			TreeNodePair foundPair = pairByChildKey.get(childKey);

			// case one : there were no entries yet
			if (foundPair == null){
				pairByChildKey.put(childKey, pair);
			}
			// case two : the new entry replaces the previous one if the parent key is not null
			else if (pair.getParentKey() != null ){
				pairByChildKey.put(childKey,  pair);
			}

			// else the pair is discarded
		}

		return new ArrayList<>(pairByChildKey.values());

	}


	/**
	 *
	 * Accepts a identifier - aka key - and returns the corresponding node if found.
	 *
	 * @param key the key identifying a node
	 * @return the node if found
	 * @throws NoSuchElementException if the node was not found.
	 *
	 */
	public T getNode(IDENT key){

		if (key == null){ return null;}

		for (T node : getAllNodes()){
			if (node.getKey().equals(key)) {return node;}

		}

		throw new NoSuchElementException("No element tagged as "+ key.toString() + " found in this tree" );
	}


	/**
	 *
	 * <p>Accepts a {@link Closure} that will be applied on the nodes using bottom-up exploration. The method will walk up the tree :
	 * <ul>
	 *  <li>layer <i>n+1</i> will be treated before layer <i>n</i> (reverse order)</li>
	 *  <li>nodes within a given layer will be treated regardless their ordering</li>
	 * </ul>
	 * </p>
	 * @param closure code to apply on the nodes.
	 */
	public void doBottomUp(Closure closure){
		if (! layers.isEmpty()){
			Integer layerIndex = Collections.max(layers.keySet());

			while (layerIndex >=0){
				List<T> layer = new ArrayList<>(layers.get(layerIndex));
				CollectionUtils.forAllDo(layer, closure);
				layerIndex--;
			}
		}
	}



	/**
	 * <p>
	 * Accepts a {@link Closure} that will be applied on the nodes using top-down exploration. The method will walk down the tree :
	 * <ul>
	 * 	<li>the layer <i>n</i> will be treated before layer <i>n+1</i> (natural order)</li>
	 *  <li>nodes within a given layer will be treated regardless their ordering</li>
	 * </ul>
	 * </p>
	 * @param closure code to apply on the nodes.
	 */
	public void doTopDown(Closure closure){
		Integer layerIndex = 0;

		while (layerIndex <= Collections.max(layers.keySet())){
			List<T> layer = layers.get(layerIndex);
			CollectionUtils.forAllDo(layer, closure);
			layerIndex++;
		}
	}



	/**
	 * <p>
	 * Accepts a list of TreeNodes and use their data to update existing nodes data. The TreeNodes of the input list are merely carrying informations : the key property will identify
	 * actual nodes in the tree and the rest of their data will be used to update the found nodes.
	 * </p>
	 * <p>The particulars of how data will be merged depends on how the TreeNodes implement {@link TreeNode#updateWith(TreeNode)}.</p>
	 *
	 * @throws NoSuchElementException if one of the node was not found.
	 */
	public void merge(List<T> mergeData){

		for (T data : mergeData){
			T node = getNode(data.getKey());
			node.updateWith(data);
		}

	}

	/**
	 * <p>
	 * That method will gather arbitrary informations on every single nodes and return the list of the gathered informations. What will be gathered and how it is done is defined in the
	 * {@link Transformer} parameter. The tree will be processed top-down, ie, walked down (see {@link #doTopDown(Closure)}).
	 * </p>
	 *
	 * @param <X> the type of the data returned by the transformer.
	 * @param transformer the code to be applied over all the nodes.
	 * @return the list of the gathered data.
	 */
	@SuppressWarnings("unchecked")
	public <X> List<X> collect(Transformer transformer){

		return new ArrayList<>(CollectionUtils.collect(getAllNodes(), transformer));

	}

	/**
	 * <p>short hand for {@link #collect(Transformer)} with a Transformer returning the data.key for each nodes.</p>
	 *
	 * @return the list of the node keys.
	 */
	public List<IDENT> collectKeys(){
		return collect(new Transformer() {
			@Override
			public Object transform(Object input) {
				return ((T)input).getKey();
			}
		});

	}


	/**
	 * @return all the nodes.
	 */
	public List<T> getAllNodes(){
		List<T> result = new ArrayList<>();
		for (List<T> layer : layers.values()){
			result.addAll(layer);
		}
		return result;
	}

	public List<T> getRootNodes(){
		if (layers.isEmpty()){
			throw new IndexOutOfBoundsException("This tree has no root");
		}

		return new ArrayList<>(layers.get(0));
	}

	public List<T> getLeaves(){
		List<T> leaves = new ArrayList<>(getAllNodes());
		CollectionUtils.filter(leaves, new Predicate() {
			@Override
			public boolean evaluate(Object object) {
				return ((T)object).getChildren().isEmpty();
			}
		});
		return leaves;
	}


	/**
	 * Says whether the given node may be
	 * removed (ie it has no children)
	 *
	 * @param key
	 */
	public boolean mayRemove(IDENT key){
		T node = getNode(key);
		return node.getChildren().isEmpty();
	}

	/**
	 * Will remove the node having this key if it is childress.
	 * If it has children, throws a RuntimeException
	 *
	 * @param key
	 */
	public void remove(IDENT key){
		T node = getNode(key);
		if (node.getChildren().isEmpty()){

			Collection<T> layer = layers.get(node.getDepth());
			layer.remove(node);

			T parent = node.getParent();
			if (parent != null){
				parent.getChildren().remove(node);
			}

		}else{
			throw new IllegalArgumentException("Cannot remove node '"+key+"' : it has no children");
		}
	}

	/**
	 * removes a node and its subtree
	 *
	 * @param key
	 */
	public void cut(IDENT key){
		T node = getNode(key);

		T parent = node.getParent();
		if (parent != null){
			parent.getChildren().remove(node);
		}

		LinkedList<T> processing = new LinkedList<>();
		processing.add(node);

		while(! processing.isEmpty()){
			T current = processing.pop();
			List<T> layer = layers.get(current.getDepth());
			layer.remove(current);
			processing.addAll(current.getChildren());
		}


	}




	/**
	 * return the depth of the tree, ie how many layers does the tree count.
	 * @return the depth.
	 */
	public int getDepth(){
		return Collections.max(layers.keySet())+1;
	}


	/* ******************************** scaffolding stuffs ******************************* */


	protected T createNewNode(T parent, int depth, T newNode){
		newNode.setParent(parent);
		newNode.setDepth(depth);
		newNode.setTree(this);
		return newNode;
	}



	/**
	 * A TreeNodePair is a scaffolding class which is mainly used when initializing a tree. It simply pairs a child treeNode with the key of its parent. A child node having a null parent
	 * will be considered as a root node.
	 *
	 * @author bsiri
	 *
	 */
	public class TreeNodePair{
		private IDENT parentKey;
		private T child;

		public IDENT getParentKey() {
			return parentKey;
		}
		public void setParentKey(IDENT parentKey) {
			this.parentKey = parentKey;
		}
		public T getChild() {
			return child;
		}
		public void setChild(T child) {
			this.child = child;
		}

		public TreeNodePair(){

		}

		public TreeNodePair(IDENT parentKey, T child){
			this.parentKey = parentKey;
			this.child = child;
		}

		@Override
		public String toString(){
			return "["+this.parentKey+" : "+this.child.getKey()+"]";

		}


	}


	/**
	 * Returns a new instance of a TreeNodePair. Basically the same thing than calling TreeNodePair constructors, that method exists mainly for semantic reasons (it guarantees that the
	 * returned TreeNodePair instance is compatible with the tree (regarding generic types).
	 *
	 * @return a new instance of a TreeNodePair.
	 */
	public TreeNodePair newPair(){
		return new TreeNodePair();
	}


	/**
	 * An initializing version of {@link #newPair()}.
	 *
	 * @param parentKey the identifier of the parent node.
	 * @param child the child node.
	 * @return an initialized instance of TreeNodePair.
	 */
	public TreeNodePair newPair(IDENT parentKey, T child){
		return new TreeNodePair(parentKey, child);
	}



	// ********* Simple class in which a node is solely represented by its key. The key is still whatever you need. **********

	public static final class SimpleNode<T> extends TreeNode<T, SimpleNode<T>>{

		public SimpleNode() {
			super();
		}

		public SimpleNode(T key) {
			super(key);
		}

		@Override
		protected void updateWith(SimpleNode<T> newData) {
			// TODO Auto-generated method stub

		}

	}

}
