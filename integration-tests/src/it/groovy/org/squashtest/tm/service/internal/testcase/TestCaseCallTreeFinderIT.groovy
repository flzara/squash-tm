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
package org.squashtest.tm.service.internal.testcase


import javax.inject.Inject

import org.spockframework.util.NotThreadSafe
import org.springframework.transaction.annotation.Transactional
import org.squashtest.tm.domain.NamedReference;
import org.squashtest.tm.domain.library.structures.LibraryGraph.SimpleNode;
import org.squashtest.tm.exception.CyclicStepCallException
import org.squashtest.it.basespecs.DbunitServiceSpecification;
import org.squashtest.tm.service.internal.testcase.TestCaseCallTreeFinder;
import org.squashtest.tm.service.testcase.CallStepManagerService
import org.squashtest.tm.service.testcase.TestCaseModificationService
import org.unitils.dbunit.annotation.DataSet

import spock.unitils.UnitilsSupport

@NotThreadSafe
@UnitilsSupport
@Transactional
class TestCaseCallTreefinderIT extends DbunitServiceSpecification {

	@Inject
	private TestCaseCallTreeFinder callTreeFinder

	def setupSpec(){
		Collection.metaClass.matches ={ arg ->
			delegate.containsAll(arg) && arg.containsAll(delegate)
		}
	}


	@DataSet("TestCaseCallTreeFinderIT.dataset.xml")
	def "should return the test case call tree of a test case"(){

		given :
		Set<Long> expectedTree = [-11L, -21L, -22L, -31L, -32L]

		when :
		Set<Long> callTree = callTreeFinder.getTestCaseCallTree(-1L);

		then :
		callTree.containsAll(expectedTree)
	}

	@DataSet("TestCaseCallTreeFinderIT.dataset.xml")
	def "should gather the caller graph of node 31"(){
		/*
		 * 1 -> 11
		 * 1 -> 31
		 * 11 -> 21
		 * 11 -> 22 *2
		 * 21 -> 31
		 * 22 -> 32
		 */

		when :
		def graph = callTreeFinder.getCallerGraph([-31L])

		then :
		def node1 = graph.getNode(ref(-1, "top test case"))
		def node11 = graph.getNode(ref(-11, "first level 1"))
		def node21 = graph.getNode(ref(-21, "second level 1"))
		def node31 = graph.getNode(ref(-31, "third level 1"))

		testcontent (node1, [], [node11, node31])
		testcontent (node11, [node1], [node21])
		testcontent (node21, [node11], [node31])
		testcontent (node31, [node21, node1], [])

	}

	@DataSet("TestCaseCallTreeFinderIT.dataset.xml")
	def "should gather the caller graph of node 32"(){
		/*
		 * 1 -> 11
		 * 1 -> 31
		 * 11 -> 21
		 * 11 -> 22 *2
		 * 21 -> 31
		 * 22 -> 32
		 */

		when :
		def graph = callTreeFinder.getCallerGraph([-32L])

		then :
		def node1 = graph.getNode(ref(-1, "top test case"))
		def node11 = graph.getNode(ref(-11, "first level 1"))
		def node22 = graph.getNode(ref(-22, "second level 2"))
		def node32 = graph.getNode(ref(-32, "third level 2"))

		testcontent (node1, [], [node11])
		testcontent (node11, [node1], [node22])
		testcontent (node22, [node11], [node32])
		testcontent (node32, [node22], [])

		node11.outbounds.count { it.equals (node22) } == 2
		node22.inbounds.count { it.equals (node11)  } == 2

		graph.cardEdge(ref(-11, "first level 1"), ref(-22, "second level 2")) == 2

	}



	@DataSet("TestCaseCallTreeFinderIT.dataset.xml")
	def "should find the extended call graph starting from 1 node"(){

		given :
		def seed = -21L

		when :
		def graph = callTreeFinder.getExtendedGraph([seed])

		then :
		def ref1 = ref(-1, "top test case")
		def ref11 = ref(-11, "first level 1")
		def ref21 = ref(-21, "second level 1")
		def ref22 = ref(-22, "second level 2")
		def ref31 = ref(-31, "third level 1")
		def ref32 = ref(-32, "third level 2")


		graph.cardEdge(ref1, ref11) == 1
		graph.cardEdge(ref1, ref31) == 1
		graph.cardEdge(ref11, ref21) == 1
		graph.cardEdge(ref11, ref22) == 2
		graph.cardEdge(ref21, ref31) == 1
		graph.cardEdge(ref22, ref32) == 1

		// some more controls
		graph.cardEdge(ref1, ref32) == 0
		graph.cardEdge(ref21, ref22) == 0
		graph.cardEdge(ref31, ref32) == 0
		graph.cardEdge(ref1, ref21) == 0



	}

	@DataSet("TestCaseCallTreeFinderIT.dataset.xml")
	def "should find the same extended call graph starting from 3 node"(){

		given :
		def seeds = [-21L, -1L, -22L ]

		when :
		def graph = callTreeFinder.getExtendedGraph(seeds)

		then :
		def ref1 = ref(-1, "top test case")
		def ref11 = ref(-11, "first level 1")
		def ref21 = ref(-21, "second level 1")
		def ref22 = ref(-22, "second level 2")
		def ref31 = ref(-31, "third level 1")
		def ref32 = ref(-32, "third level 2")


		graph.cardEdge(ref1, ref11) == 1
		graph.cardEdge(ref1, ref31) == 1
		graph.cardEdge(ref11, ref21) == 1
		graph.cardEdge(ref11, ref22) == 2
		graph.cardEdge(ref21, ref31) == 1
		graph.cardEdge(ref22, ref32) == 1

		// some more controls
		graph.cardEdge(ref1, ref32) == 0
		graph.cardEdge(ref21, ref22) == 0
		graph.cardEdge(ref31, ref32) == 0
		graph.cardEdge(ref1, ref21) == 0



	}


	// ********************* private **********************

	def testcontent = { node, inbounds, outbounds ->
		node.inbounds as Set == inbounds as Set &&
				node.outbounds as Set == outbounds as Set
	}

	def ref = { id, name -> new NamedReference(id, name)}

	def nodepair(callerid, callername, calledid, calledname){
		return [
			ref (callerid, callername),
			ref (calledid, calledname)
		]
	}

}
