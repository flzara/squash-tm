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
package org.squashtest.tm.domain.library.structures

import org.squashtest.tm.domain.NamedReference;
import org.squashtest.tm.domain.library.structures.LibraryGraph.NodeTransformer;
import org.squashtest.tm.domain.library.structures.LibraryGraph.SimpleNode;

import spock.lang.Specification


class LibraryGraphTest extends Specification {


	def "should build a graph using caller/called details"(){

		given :
		def layer0 = [ [ null, 1l ] ]
		def layer1 = [ [ 1l, 11l ], [ 1l, 12l ]  ]
		def layer2 = [
			[ 11l, 21l ], [ 11l,  22l ], [ 11l, 23l ],
			[ 12l, 22l ], [ 12l,  23l ], [ 12l, 24l ],
			[ 23l, 24l ],
			[ null, 25l ]
		]

		def allData = layer0 + layer1 + layer2

		and :
		LibraryGraph graph = new LibraryGraph()
		allData.each{ graph.addEdge(node(it[0]), node(it[1])) }

		when :

		Collection nodes = graph.getNodes()

		then :

		nodes.size() == 8

		def node1 = graph.getNode(ref(1l))
		def node11 = graph.getNode(ref(11l))
		def node12 = graph.getNode(ref(12l))
		def node21 = graph.getNode(ref(21l))
		def node22 = graph.getNode(ref(22l))
		def node23 = graph.getNode(ref(23l))
		def node24 = graph.getNode(ref(24l))
		def node25 = graph.getNode(ref(25l))


		node1.inbounds.size() == 0
		node1.outbounds as Set == [node11, node12] as Set

		node11.inbounds as Set == [node1] as Set
		node11.outbounds as Set == [node21, node22, node23] as Set

		node12.inbounds as Set == [node1] as Set
		node12.outbounds as Set == [node22, node23, node24] as Set

		node21.inbounds as Set == [node11] as Set
		node21.outbounds as Set == [] as Set

		node22.inbounds as Set == [node11, node12] as Set
		node22.outbounds as Set == [] as Set

		node23.inbounds  as Set == [node11, node12] as Set
		node23.outbounds as Set  == [node24] as Set

		node24.inbounds as Set== [node12, node23] as Set
		node24.outbounds as Set == [] as Set

		node25.inbounds.size() == 0
		node25.outbounds as Set  == [] as Set


	}


	def "should merge a graph into another one"(){

		given :
		def nodes1 = [[null, 1l], [1l, 2l], [1l, 3l], [3l, 4l]]
		LibraryGraph<NamedReference, SimpleNode<NamedReference>> othergraph = new LibraryGraph()
		nodes1.each { othergraph.addEdge(node(it[0]), node(it[1])) }


		and :
		def nodes2 = [[null, 3l], [3l, 31l], [3l, 32l], [32l, 41l]]
		LibraryGraph<Long, CustomNode<Long>> thisgraph = new LibraryGraph()
		nodes2.each { thisgraph.addEdge( new CustomNode(it[0]), new CustomNode(it[1])) }

		when :
		thisgraph.mergeGraph(othergraph, new SimpleTransformer())


		then :
		def nodes = thisgraph.getNodes()
		nodes.size() == 7

		def node1 = thisgraph.getNode(1l)
		def node2 = thisgraph.getNode(2l)
		def node3 = thisgraph.getNode(3l)
		def node4 = thisgraph.getNode(4l)
		def node31 = thisgraph.getNode(31l)
		def node32 = thisgraph.getNode(32l)
		def node41 = thisgraph.getNode(41l)

		node1.inbounds.size() == 0
		node1.outbounds as Set== [node2, node3] as Set

		node2.inbounds as Set == [node1] as Set
		node2.outbounds.size() == 0

		node3.inbounds as Set == [node1] as Set
		node3.outbounds as Set == [node31, node32, node4] as Set

		node4.inbounds as Set  == [node3] as Set
		node4.outbounds.size() == 0

		node31.inbounds as Set  == [node3] as Set
		node31.outbounds.size() == 0

		node32.inbounds as Set  == [node3] as Set
		node32.outbounds as Set  == [node41] as Set

		node41.inbounds as Set  == [node32] as Set
		node41.outbounds.size() == 0

	}

	def "should substract a graph from this graph"(){

		given :
		def nodes1 = [[null, 1l], [1l, 2l], [1l, 3l], [3l, 4l]]
		LibraryGraph<NamedReference, SimpleNode<NamedReference>> othergraph = new LibraryGraph()
		nodes1.each { othergraph.addEdge(node(it[0]), node(it[1])) }


		and :
		def nodes2 = [[null, 1l], [1l, 2l], [1l, 3l], [3l, 4l], [4l, 5l]]
		LibraryGraph<Long, CustomNode<Long>> thisgraph = new LibraryGraph()
		nodes2.each { thisgraph.addEdge( new CustomNode(it[0]), new CustomNode(it[1])) }

		when :
		thisgraph.substractGraph(othergraph, new SimpleTransformer(), true)

		then :
		def node1 = thisgraph.getNode(1l)
		def node2 = thisgraph.getNode(2l)
		def node3 = thisgraph.getNode(3l)
		def node4 = thisgraph.getNode(4l)
		def node5 = thisgraph.getNode(5l)

		// expected result is that every edge has been removed except 4->5
		node1.inbounds as Set == [] as Set
		node1.outbounds as Set == [] as Set

		node2.inbounds as Set == [] as Set
		node2.outbounds as Set == [] as Set

		node3.inbounds as Set == [] as Set
		node3.outbounds as Set == [] as Set

		node4.inbounds as Set == [] as Set
		node4.outbounds as Set == [node5] as Set

		node5.inbounds  as Set == [node4] as Set
		node5.outbounds as Set == [] as Set

	}

	def "should disconnect a node (removing all connected edges)"(){

		given :
		def nodes1 = [[null, 1l], [1l, 2l], [1l, 3l], [3l, 4l]]
		LibraryGraph<NamedReference, SimpleNode<NamedReference>> graph = new LibraryGraph()
		nodes1.each { graph.addEdge(node(it[0]), node(it[1])) }

		when :
		graph.removeNode(ref(1l))

		then :
		def n1 = node(1l)
		graph.nodes.size() == 3
		graph.nodes.count { it.inbounds.contains(n1) || it.outbounds.contains(n1) } == 0
	}


	def "when two nodes are connected multiple times, the edge is represented as many times"(){

		when :
		def nodes = [[null, 1l], [1l, 2l], [1l, 2l], [1l, 3l], [3l, 4l]]
		LibraryGraph<NamedReference, SimpleNode<NamedReference>> graph = new LibraryGraph()
		nodes.each { graph.addEdge(node(it[0]), node(it[1])) }


		then :
		def node1 = graph.getNode(ref(1l))
		def node2 = graph.getNode(ref(2l))

		node1.outbounds.count { it.equals(node2) } == 2
		node2.inbounds.count { it.equals(node1) } == 2

	}

	def "when two nodes are connected multiple times, removing one edge do not remove the others"(){

		given :
		def nodes = [[null, 1l], [1l, 2l], [1l, 2l], [1l, 3l], [3l, 4l]]
		LibraryGraph<NamedReference, SimpleNode<NamedReference>> graph = new LibraryGraph()
		nodes.each { graph.addEdge(node(it[0]), node(it[1])) }


		when :
		graph.removeEdge(ref(1l), ref(2l))

		then :
		graph.hasEdge(ref(1l), ref(2l))
	}

	def "when two nodes are connected multiple times, removing multiple edges eventually disconnect the nodes"(){

		given :
		def nodes = [[null, 1l], [1l, 2l], [1l, 2l], [1l, 3l], [3l, 4l]]
		LibraryGraph<NamedReference, SimpleNode<NamedReference>> graph = new LibraryGraph()
		nodes.each { graph.addEdge(node(it[0]), node(it[1])) }


		when :
		graph.removeEdge(ref(1l), ref(2l))
		graph.removeEdge(ref(1l), ref(2l))

		then :
		graph.hasEdge(ref(1l), ref(2l)) == false
	}

	def "should remove all edges between two nodes"(){
		given :
		def nodes = [[null, 1l], [1l, 2l], [1l, 2l], [1l, 3l], [3l, 4l]]
		LibraryGraph<NamedReference, SimpleNode<NamedReference>> graph = new LibraryGraph()
		nodes.each { graph.addEdge(node(it[0]), node(it[1])) }


		when :
		graph.removeAllEdges(ref(1l), ref(2l))

		then :
		graph.hasEdge(ref(1l), ref(2l)) == false


	}

	def "should remove all edges from node 1 to node 2, but not the other way"(){
		given :
		def nodes = [[null, 1l], [1l, 2l], [1l, 2l], [2l, 1l]]
		LibraryGraph<NamedReference, SimpleNode<NamedReference>> graph = new LibraryGraph()
		nodes.each { graph.addEdge(node(it[0]), node(it[1])) }


		when :
		graph.removeAllEdges(ref(1l), ref(2l))

		then :
		graph.hasEdge(ref(1l), ref(2l)) == false
		graph.hasEdge(ref(2l), ref(1l)) == true


	}


	def "should tell the cardinality of an edge between twno nodes"(){


		given :
		def nodes = [[null, 1l], [1l, 2l], [1l, 2l], [1l, 3l], [3l, 4l]]
		LibraryGraph<NamedReference, SimpleNode<NamedReference>> graph = new LibraryGraph()
		nodes.each { graph.addEdge(node(it[0]), node(it[1])) }


		when :
		def res = graph.cardEdge(ref(1l), ref(2l))

		then :
		res == 2

	}


	NamedReference ref(id){
		return new NamedReference(id, id?.toString());
	}

	SimpleNode node(id){
		return (id != null) ? new SimpleNode(ref(id)) : null;
	}

	class CustomNode extends GraphNode<Long, CustomNode>{
		CustomNode(Long id){
			super(id);
		}
	}

	class SimpleTransformer implements NodeTransformer<SimpleNode<NamedReference>, CustomNode>{
		CustomNode createFrom(SimpleNode<NamedReference> othernode) {
			new CustomNode(othernode.key.id)
		};
		Object createKey(SimpleNode<NamedReference> othernode){
			othernode.key.id
		}
	}

}
