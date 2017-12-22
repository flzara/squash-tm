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
package org.squashtest.tm.service.internal.batchimport;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.squashtest.tm.domain.NamedReference;
import org.squashtest.tm.domain.library.structures.GraphNode;
import org.squashtest.tm.domain.library.structures.LibraryGraph;
import org.squashtest.tm.exception.CyclicStepCallException;


/**
 * Definitely NOT THREAD SAFE !
 *
 * @author bsiri
 *
 */
class TestCaseCallGraph extends LibraryGraph<TestCaseTarget, TestCaseCallGraph.Node> {

	/**
	 * the merge mode modifies the behavior of addEdges(src, dest). It is active only when we are merging
	 * a new graph with this one. It makes sure that local graph takes precedence over the graph being merged into it.
	 *
	 * That's how we prevent nodes and edges from the database to override changes that were made to the model.
	 */
	private boolean mergeMode = false;

	void addGraph(LibraryGraph<NamedReference, SimpleNode<NamedReference>> othergraph){

		mergeMode = true;

		mergeGraph(othergraph, new NodeTransformer<SimpleNode<NamedReference>, Node>() {
			@Override
			public Node createFrom(SimpleNode<NamedReference> node) {
				return new Node(new TestCaseTarget(node.getKey().getName()));
			}

			@Override
			public Object createKey(SimpleNode<NamedReference> node) {
				return new TestCaseTarget(node.getKey().getName());
			}
		});

		// now flag all new node as Live
		for (Node n : getNodes()){
			if (n.isNew()){
				n.state = NodeLifecycle.LIVE;
			}
		}

		mergeMode = false;
	}


	public boolean knowsNode(TestCaseTarget target){
		return getNodes().contains(new Node(target));
	}

	public void addEdge(TestCaseTarget parent, TestCaseTarget child){
		addEdge(new Node(parent), new Node(child));
	}

	public void addNode(TestCaseTarget target){
		addNode(new Node(target));
	}


	@Override
	public void removeNode(TestCaseTarget target){
		Node n = getNode(target);

		if (n != null){
			super.removeNode(target);
			n.state = NodeLifecycle.REMOVED;
			getNodes().add(n);
		}
	}


	@Override
	public void addEdge(Node src, Node dest) {

		if (checkShouldCreate(src, dest)){

			super.addEdge(src, dest);

		}

		if (! mergeMode){
			getNode(src.getKey()).state=NodeLifecycle.LIVE;
			getNode(dest.getKey()).state=NodeLifecycle.LIVE;
		}
	}


	private boolean checkShouldCreate(Node src, Node dest){

		boolean shouldCreate = true;

		Node iSrc = createIfNotExists(src);
		Node iDest = createIfNotExists(dest);

		// if merge mode, we don't want to add edges that already exist. That's why we ensure that at least one node is new.
		// Failure is non fatal since in merge mode there are nominal cases that may or may not pass this check.
		if (mergeMode && ! hasNewNodes(iSrc, iDest)) {
			shouldCreate = false;
		}

		// in any case, we don't want to add edges to of from a dead node. Failure is non fatal because there are nominal cases
		// in merge mode that may or may not pass this check.
		else if (hasRemovedNodes(iSrc, iDest)){
			shouldCreate = false;
		}

		// most of all we don't want to introduce cycles. Failure is fatal because this should never happen, as such we raise
		// an exception instead of aborting the operation.
		else if (wouldCreateCycle(iSrc.getKey(), iDest.getKey())){
			throw new CyclicStepCallException("cannot add to test case call graph an edge from '"+src.getKey().getPath()+"' to '"+dest.getKey().getPath()+"' : would create a cycle");
		}


		return shouldCreate;
	}

	/**
	 * says if the given target is called
	 *
	 * @param target
	 * @return
	 */
	boolean isCalled(TestCaseTarget target){
		Node n = getNode(target);
		if (n!=null){
			return !n.getInbounds().isEmpty();
		}
		else{
			return false;
		}
	}


	/**
	 * says whether that new edge would create a cycle in the graph.
	 *
	 * Namely, if the src node of the edge is already transitively called
	 * by the dest node. In other words, is dest node an ancestor of src node ?
	 *
	 * @return
	 */
	boolean wouldCreateCycle(TestCaseTarget src, TestCaseTarget dest){

		boolean createsCycle = false;

		// quick check : if one of either node doesn't exist it's always ok
		if (getNode(dest) == null || getNode(src) == null){
			createsCycle = false;
		}

		// quick check : exclude self calls
		else if (src.equals(dest)){
			createsCycle = true;
		}


		else{
			// else we walk down the call tree of the dest

			// we keep track of processed nodes. It has the benefit of preventing multiple exploration of the same node.
			// it also breaks infinite loop but this method exists precisely to prevent this to happen.
			Set<Node> processed = new HashSet<>();
			LinkedList<Node> nodes = new LinkedList<>();

			Node orig = getNode(dest);
			processed.add(orig);
			nodes.add(orig);

			do{
				Node current = nodes.pop();
				if (current.calls(src)){
					createsCycle = true;
					break;
				}
				else{
					for (Node child : current.getOutbounds()){
						if (! processed.contains(child)){
							nodes.add(child);
							processed.add(child);
						}
					}

				}

			}while(! nodes.isEmpty());
		}

		return createsCycle;

	}



	// ******************* predicates ****************


	private boolean hasNewNodes(Node n1, Node n2){
		return n1.isNew() || n2.isNew();
	}

	private boolean hasRemovedNodes(Node n1, Node n2){
		return n1.isRemoved() || n2.isRemoved();
	}




	static final class Node extends GraphNode<TestCaseTarget, Node>{

		private NodeLifecycle state = NodeLifecycle.NEW;

		Node(TestCaseTarget target){
			super(target);
		}

		Node(SimpleNode<NamedReference> othernode){
			super(new TestCaseTarget(othernode.getKey().getName()));
		}

		boolean isMe(TestCaseTarget target){
			return target.equals(key);
		}

		boolean calls(TestCaseTarget callee){
			for (Node n : outbounds){
				if (n.isMe(callee)){
					return true;
				}
			}
			return false;
		}

		boolean isNew(){
			return state == NodeLifecycle.NEW;
		}

		boolean isLive(){
			return state == NodeLifecycle.LIVE;
		}

		boolean isRemoved(){
			return state == NodeLifecycle.REMOVED;
		}

	}

	private enum NodeLifecycle{
		NEW,
		LIVE,
		REMOVED;
	}
}

