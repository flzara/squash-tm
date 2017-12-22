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
package org.squashtest.tm.service.deletion

import org.squashtest.tm.domain.NamedReference;
import org.squashtest.tm.domain.library.structures.LibraryGraph
import org.squashtest.tm.domain.library.structures.LibraryGraph.SimpleNode
import org.squashtest.tm.service.internal.deletion.LockedFileInferenceGraph

import spock.lang.Specification

class LockedFileInferenceGraphTest extends Specification {

	
	private boolean areContentEquals(List<Long> list1, List<Long> list2){
		return ((list1.containsAll(list2)) && (list2.containsAll(list1)))
	}
	
	/*
	* structure : 1 calls 11 and 12,
	* 			   11 calls 21, 22 and 23,
	* 			   12 calls 22, 23 and 24
	* 			   23 calls 24
	*
	* 	21, 22, 24 and 25 call no one.
	*  1 and 25 are called by no one.
	*
	*/
	
	def "should build a graph using caller/called details"(){
		
		given :
			def g = testGraph()

			
		when :
			def graph = new LockedFileInferenceGraph();
			graph.init(g)
		
		then :
			def nodes = graph.getNodes()
			
			nodes.size() == 8
			
			def node1 = graph.getNode(ref(1l))
			def node11 = graph.getNode(ref(11l))
			def node12 = graph.getNode(ref(12l))
			def node21 = graph.getNode(ref(21l))
			def node22 = graph.getNode(ref(22l))
			def node23 = graph.getNode(ref(23l))
			def node24 = graph.getNode(ref(24l))
			def node25 = graph.getNode(ref(25l))
			
			node1.name == "1"
			node1.inbounds.size() == 0
			node1.outbounds as Set == [node11, node12] as Set
			
			node11.name == "11"
			node11.inbounds as Set == [node1] as Set
			node11.outbounds as Set == [node21, node22, node23] as Set
			
			node12.name == "12"
			node12.inbounds as Set == [node1] as Set
			node12.outbounds as Set == [node22, node23, node24] as Set
			
			node21.name == "21"
			node21.inbounds as Set == [node11] as Set
			node21.outbounds as Set == [] as Set
			
			node22.name == "22"
			node22.inbounds as Set== [node11, node12] as Set
			node22.outbounds as Set == [] as Set
			
			node23.name == "23"
			node23.inbounds as Set== [node11, node12] as Set
			node23.outbounds as Set == [node24] as Set
			
			node24.name == "24"
			node24.inbounds as Set== [node12, node23] as Set
			node24.outbounds as Set == [] as Set

			node25.name == "25"
			node25.inbounds as Set == [] as Set
			node25.outbounds as Set == [] as Set
		
	}
	
	
	
	
	
	def "should mark which nodes are deletable (#1)"(){
		given :
			def g = testGraph()
			def graph = new LockedFileInferenceGraph()
			graph.init(g)
			
		and :
			def candidates = [1l, 25l, 11l, 23l]
			def reallyDeletables = [1l, 25l, 11l ]
			def lockedExpected = [23l]
			
		when :
			graph.setCandidatesToDeletion(candidates)
			graph.resolveLockedFiles()
			
			def deletables = graph.collectDeletableNodes().collect{it.key.id};
			def locked = graph.collectLockedCandidates().collect{it.key.id};
		
		then :
			areContentEquals(deletables, reallyDeletables)
			areContentEquals(locked, lockedExpected)
			
	}
	
	
	def "should mark which nodes are deletable (#2)"(){
		given :
			def g = testGraph()
			def graph = new LockedFileInferenceGraph()
			graph.init(g)
			
			
		and :
			def candidates = [1l]
			def reallyDeletables = [1l]
			def lockedExpected = []
			
		when :
			graph.setCandidatesToDeletion(candidates)
			graph.resolveLockedFiles()
			
			def deletables = graph.collectDeletableNodes().collect{it.key.id};
			def locked = graph.collectLockedCandidates().collect{it.key.id};
		
		then :
			areContentEquals(deletables, reallyDeletables)
			areContentEquals(locked, lockedExpected)
	}
	
	
	
	def "should mark which nodes are deletable (#3)"(){
		given :
			def g = testGraph()
			def graph = new LockedFileInferenceGraph()
			graph.init(g)
			
			
		and :
			def candidates = [25l, 11l]
			def reallyDeletables = [25l]
			def lockedExpected = [11l]
			
		when :
			graph.setCandidatesToDeletion(candidates)
			graph.resolveLockedFiles()
			
			def deletables = graph.collectDeletableNodes().collect{it.key.id};
			def locked = graph.collectLockedCandidates().collect{it.key.id};
		
		then :
			areContentEquals(deletables, reallyDeletables)
			areContentEquals(locked, lockedExpected)
	}
	
	
	
	
	def "should mark which nodes are deletable (#4)"(){
		given :
			def g = testGraph()
			def graph = new LockedFileInferenceGraph()
			graph.init(g)
			
			
		and :
			def candidates = [24l]
			def reallyDeletables = [ ]
			def lockedExpected = [24l]
			
		when :
			graph.setCandidatesToDeletion(candidates)
			graph.resolveLockedFiles()
			
			def deletables = graph.collectDeletableNodes().collect{it.key.id};
			def locked = graph.collectLockedCandidates().collect{it.key.id};
		
		then :
			areContentEquals(deletables, reallyDeletables)
			areContentEquals(locked, lockedExpected)
	}
	
	
	
	def "should mark which nodes are deletable (#5)"(){
		given :
			def g = testGraph()
			def graph = new LockedFileInferenceGraph()
			graph.init(g)
			
			
		and :
			def candidates = [1l, 11l, 21l]
			def reallyDeletables = [ 1l, 11l, 21l]
			def lockedExpected = []
			
		when :
			graph.setCandidatesToDeletion(candidates)
			graph.resolveLockedFiles()
			
			def deletables = graph.collectDeletableNodes().collect{it.key.id};
			def locked = graph.collectLockedCandidates().collect{it.key.id};
		
		then :
			areContentEquals(deletables, reallyDeletables)
			areContentEquals(locked, lockedExpected)
	}
	
	
	
	def "should mark which nodes are deletable (#6)"(){
		given :
			def g = testGraph()
			def graph = new LockedFileInferenceGraph()
			graph.init(g)
			
			
		and :
			def candidates = [1l, 12l, 24l,]
			def reallyDeletables = [1l, 12l ]
			def lockedExpected = [24l]
			
		when :
			graph.setCandidatesToDeletion(candidates)
			graph.resolveLockedFiles()
			
			def deletables = graph.collectDeletableNodes().collect{it.key.id};
			def locked = graph.collectLockedCandidates().collect{it.key.id};
		
		then :
			areContentEquals(deletables, reallyDeletables)
			areContentEquals(locked, lockedExpected)
	}
	
	
	
	// ********************************* private utilities
	
	NamedReference ref(id){
		return new NamedReference(id, id?.toString());
	}
	
	SimpleNode node(id){
		return (id != null) ? new SimpleNode(ref(id)) : null;
	}
	
	
	
	LibraryGraph testGraph(){

		def layer0 = [ [ null, 1l ] ]
		def layer1 = [ [ 1l, 11l ], [ 1l, 12l ]  ]
		def layer2 = [
						[ 11l, 21l ], [ 11l,  22l ], [ 11l, 23l ],
						[ 12l, 22l ], [ 12l,  23l ], [ 12l, 24l ],
						[ 23l, 24l ],
						[ null, 25l ]
					 ]
		
		def allData = layer0 + layer1 + layer2
	

		LibraryGraph g = new LibraryGraph()
		allData.each{ g.addEdge(node(it[0]), node(it[1])) }
		
		return g
	}
	
}
