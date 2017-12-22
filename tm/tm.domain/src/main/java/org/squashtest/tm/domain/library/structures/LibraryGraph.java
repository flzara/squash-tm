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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.Transformer;


/**
 *
 *
 * @author bsiri
 *
 * @param acts like a primary key. It should be immutable and should be sufficient to identify a node. String, Long are examples of good keys.
 * @param <T>
 */
public class LibraryGraph<IDENT, T extends GraphNode<IDENT, T>> {

	private Set<T> nodes = new HashSet<>();


	public Collection<T> getNodes() {
		return nodes;
	}

	public void addNode(T node) {
		if (node != null && node.getKey() != null) {
			createIfNotExists(node);
		}
	}

	/**
	 * will create either the parent or the child if they didn't exist already
	 *
	 * @param parentData
	 * @param childData
	 */


	public void addEdge(T parentNode, T childNode) {

		T parent = null;
		T child = null;

		if (parentNode != null && parentNode.getKey() != null) {
			parent = createIfNotExists(parentNode);
		}

		if (childNode != null && childNode.getKey() != null) {
			child = createIfNotExists(childNode);
		}

		if (parent != null) {
			parent.addOutbound(child);
		}

		if (child != null) {
			child.addInbound(parent);
		}

	}


	public T getNode(IDENT key) {
		T toReturn = null;

		if (key != null) {

		// YEARGH ! O(n) called everywhere ! Cant we use a hashmap ?
			for (T node : nodes) {
				if (node.getKey().equals(key)) {
					toReturn = node;
				}
			}
		}
		return toReturn;
	}

	protected T createIfNotExists(T node) {

		if (!nodes.contains(node)) {
			nodes.add(node);
		}

		return getNode(node.getKey());

	}

	public boolean hasEdge(IDENT src, IDENT dest) {

		T srcNode = getNode(src);
		T destNode = getNode(dest);

		return srcNode != null && destNode != null && srcNode.getOutbounds().contains(destNode);
	}

	public int cardEdge(IDENT src, IDENT dest) {

		int cardinality = 0;

		T srcNode = getNode(src);
		T destNode = getNode(dest);

		if (srcNode != null && destNode != null) {
			for (T node : srcNode.getOutbounds()) {
				if (node.equals(destNode)) {
					cardinality++;
				}
			}
		}

		return cardinality;

	}


	public void removeNode(IDENT target) {
		T n = getNode(target);
		if (n != null) {
			for (T othernode : getNodes()) {
				if (!othernode.equals(n)) {
					othernode.disconnect(n);
					n.disconnect(othernode);
				}
			}
			getNodes().remove(n);
		}
	}

	/**
	 * remove one edge from src to dest, not all of them (in case of cardinality > 1)
	 */
	public void removeEdge(IDENT src, IDENT dest) {
		T srcNode = getNode(src);
		T destNode = getNode(dest);

		if (srcNode != null) {
			srcNode.getOutbounds().remove(destNode);
		}
		if (destNode != null) {
			destNode.getInbounds().remove(srcNode);
		}
	}

	/**
	 *  remove all edges from src to dest, reducing effectively the cardinality to 0
	 *  (one cannot navigate from src to dest anymore)
	 */
	public void removeAllEdges(IDENT src, IDENT dest) {
		T srcNode = getNode(src);
		T destNode = getNode(dest);

		if (srcNode != null && destNode != null) {
			srcNode.disconnect(destNode);
		}
	}


	/**
	 * totally remove any inbound/outbound edges between the two nodes
	 *
	 * @param src
	 * @param dest
	 */
	public void disconnect(IDENT src, IDENT dest) {
		T srcNode = getNode(src);
		T destNode = getNode(dest);

		if (srcNode != null && destNode != null) {
			srcNode.disconnect(destNode);
			destNode.disconnect(srcNode);
		}
	}


	public List<T> getOrphans() {
		List<T> copy = new LinkedList<>(getNodes());

		CollectionUtils.filter(copy, new Predicate() {
			@Override
			public boolean evaluate(Object object) {
				return ((T) object).getInbounds().isEmpty();
			}
		});

		return copy;
	}


	public List<T> getChildless() {
		List<T> copy = new LinkedList<>(getNodes());

		CollectionUtils.filter(copy, new Predicate() {
			@Override
			public boolean evaluate(Object object) {
				return ((T) object).getOutbounds().isEmpty();
			}
		});

		return copy;
	}


	public <X> List<X> collect(Transformer transformer) {
		return new ArrayList<>(CollectionUtils.collect(getNodes(), transformer));

	}

	public List<T> filter(Predicate predicate) {
		List<T> result = new ArrayList<>(getNodes());

		CollectionUtils.filter(result, predicate);
		return result;
	}


	/**
	 * first we'll filter, then we'll collect. So write your predicate and transformer carefully.
	 *
	 */
	public <X> List<X> filterAndcollect(Predicate predicate, Transformer transformer) {
		List<T> filtered = filter(predicate);

		return new ArrayList<>(CollectionUtils.collect(filtered, transformer));
	}


	/**
	 * <p> Will merge the structure of a graph into this graph. This means that nodes will be created if no equivalent exist already,
	 * same goes for inbound/outbound edges . You must provide an implementation of {@link NodeTransformer}
	 * in order to allow the conversion of a node from the other graph into a node acceptable for this graph.</p>
	 * <p> The merge Nodes and edges inserted that way will not erase existing data provided if nodes having same keys are already present.</p>
	 *
	 * <p>The generics are the following :
	 * 	<ul>
	 * 		<li>OIDENT : the class of the key of the other graph</li>
	 * 		<li>ON : the type definition of a node from the other graph</li>
	 * 		<li>OG : the type of the other graph </li>
	 * 	</ul>
	 * </p>
	 *
	 * @param othergraph
	 */
	public <OIDENT, ON extends GraphNode<OIDENT, ON>, OG extends LibraryGraph<OIDENT, ON>>
	void mergeGraph(OG othergraph, NodeTransformer<ON, T> transformer) {

		LinkedList<ON> processing = new LinkedList<>(othergraph.getOrphans());

		Set<ON> processed = new HashSet<>();

		while (!processing.isEmpty()) {

			ON current = processing.pop();
			T newParent = transformer.createFrom(current);

			for (ON child : current.getOutbounds()) {

				addEdge(newParent, transformer.createFrom(child));

				if (!processed.contains(child)) {
					processing.add(child);
					processed.add(child);
				}
			}

			// in case the node had no children it might be useful to add itself again
			addNode(newParent);
		}
	}


	/**
	 * Will remove from this graph any edge that exists in othergraph. If removeAll is
	 * set to true every connection between the source and destination node of such edges
	 * will be removed, is false only their cardinalities will be substracted.
	 *
	 *
	 * @param othergraph
	 * @param transformer
	 * @param removeAll
	 */
	public <OIDENT, ON extends GraphNode<OIDENT, ON>, OG extends LibraryGraph<OIDENT, ON>>
	void substractGraph(OG othergraph, NodeTransformer<ON, T> transformer, boolean removeAll) {

		LinkedList<ON> processing = new LinkedList<>(othergraph.getOrphans());

		Set<ON> processed = new HashSet<>();

		while (!processing.isEmpty()) {
			ON otherCurrent = processing.pop();
			IDENT thisCurrent = (IDENT) transformer.createKey(otherCurrent);

			for (ON otherChild : otherCurrent.getOutbounds()) {
				IDENT thisChild = (IDENT) transformer.createKey(otherChild);

				if (hasEdge(thisCurrent, thisChild)) {
					if (removeAll) {
						removeAllEdges(thisCurrent, thisChild);
					} else {
						removeEdge(thisCurrent, thisChild);
					}
				}

				if (!processed.contains(otherChild)) {
					processing.add(otherChild);
					processed.add(otherChild);
				}
			}
		}


	}


	// ********* Simple class in which a node is solely represented by its key. The key is still whatever you need. **********

	public static final class SimpleNode<T> extends GraphNode<T, SimpleNode<T>> {

		public SimpleNode() {
			super();
		}

		public SimpleNode(T key) {
			super(key);
		}

	}

	public static interface NodeTransformer<FORMER, NEW> {

		NEW createFrom(FORMER node);

		Object createKey(FORMER node);

	}

}
