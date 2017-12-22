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

import org.squashtest.tm.domain.NamedReference;
import org.squashtest.tm.domain.NamedReferencePair;
import org.squashtest.tm.domain.library.structures.LibraryGraph;
import org.squashtest.tm.service.internal.repository.TestCaseDao;
import org.squashtest.tm.service.internal.testcase.TestCaseCallTreeFinder;

import spock.lang.Specification

class TestCaseCallTreeFinderTest extends Specification {
	TestCaseCallTreeFinder service = new TestCaseCallTreeFinder()
	TestCaseDao testCaseDao = Mock()


	def setup(){
		service.testCaseDao = testCaseDao;
	}

	def "should return the test case call tree of a test case"(){
		given :

		def firstLevel = [2l, 3l]
		def secondLevel = [4l, 5l]
		def thirdLevel = []

		testCaseDao.findAllDistinctTestCasesIdsCalledByTestCase (1L) >>  firstLevel
		testCaseDao.findAllTestCasesIdsCalledByTestCases(firstLevel ) >>  secondLevel
		testCaseDao.findAllTestCasesIdsCalledByTestCases(secondLevel) >>  thirdLevel

		when :
		def callTree = service.getTestCaseCallTree(1l)

		then :

		callTree.containsAll(firstLevel + secondLevel)
	}



	def "should return an extended graph"(){

		given :
		def A = new NamedReference(1l, "A")
		def B = new NamedReference(2l, "B")
		def C = new NamedReference(3l, "C")
		def D = new NamedReference(4l, "D")
		def E = new NamedReference(5l, "E")
		def F = new NamedReference(6l, "F")
		def G = new NamedReference(7l, "G")
		def H = new NamedReference(8l, "H")
		def I = new NamedReference(9l, "I" )
		def J = new NamedReference(10l, "J")
		def K = new NamedReference(11l, "K")


		and :
		testCaseDao.findTestCaseCallsUpstream(_) >>> [
			[ pair(B,C), pair(D,C) ],
			[ pair(null,B), pair(null,D), pair(C,F), pair(C,I) ],
			[ pair(F,G), pair(F,H), pair(I,J), pair(I,K), pair(I,K), pair(B,A), pair(D,E) ]
		]

		testCaseDao.findTestCaseCallsDownstream(_) >>> [
			[ pair(C,F), pair(C,I) ],
			[ pair(B,A), pair(B,C), pair(D,C), pair(D,E), pair(F,G), pair(F,H), pair(I,J), pair(I,K), pair(I,K) ],
			[ pair(G,null), pair(H,null), pair(J,null), pair(I,null), pair(K,null), pair(A,null), pair(E,null) ]
		]

		when :
		LibraryGraph graph = service.getExtendedGraph([5l])

		then :
		graph.hasEdge B, A
		graph.hasEdge B, C
		graph.hasEdge D, C
		graph.hasEdge D, E
		graph.hasEdge C, F
		graph.hasEdge C, I
		graph.hasEdge F, G
		graph.hasEdge F, H
		graph.hasEdge I, J
		graph.hasEdge I, K

		// also, I->K is present twice
		def nI = graph.getNode(I)
		def nK = graph.getNode(K)
		graph.getNode(I).outbounds.count { it.equals nK	} == 2
		graph.getNode(K).inbounds.count { it.equals nI } == 2


	}




	def pair(NamedReference A, NamedReference B){
		def a = (A!=null) ? A : new NamedReference(null,null)
		def b = (B!=null) ? B : new NamedReference(null,null)
		return new NamedReferencePair(a.id, a.name, b.id, b.name)
	}

}
