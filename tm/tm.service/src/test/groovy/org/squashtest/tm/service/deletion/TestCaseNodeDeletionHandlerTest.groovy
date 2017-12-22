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

import java.util.Optional
import org.squashtest.tm.domain.NamedReference;
import org.squashtest.tm.domain.library.structures.LibraryGraph
import org.squashtest.tm.domain.library.structures.LibraryGraph.SimpleNode
import org.squashtest.tm.domain.milestone.Milestone
import org.squashtest.tm.service.internal.deletion.TestCaseNodeDeletionHandlerImpl
import org.squashtest.tm.service.internal.repository.TestCaseDao
import org.squashtest.tm.service.internal.repository.TestCaseFolderDao
import org.squashtest.tm.service.internal.testcase.TestCaseCallTreeFinder
import org.squashtest.tm.service.internal.repository.TestCaseDeletionDao
import org.squashtest.tm.service.milestone.ActiveMilestoneHolder
import spock.lang.Specification

class TestCaseNodeDeletionHandlerTest extends Specification {

	TestCaseDao tcDao = Mock();
	TestCaseFolderDao fDao = Mock();
	TestCaseCallTreeFinder calltreeFinder = Mock();
	TestCaseDeletionDao deletionDao = Mock()
	ActiveMilestoneHolder activeMilestoneHolder = Mock()

	TestCaseNodeDeletionHandlerImpl handler = new TestCaseNodeDeletionHandlerImpl();



	def setup(){
		handler.leafDao = tcDao;
		handler.folderDao = fDao;
		handler.calltreeFinder = calltreeFinder;
		handler.deletionDao = deletionDao
		handler.activeMilestoneHolder = activeMilestoneHolder
		activeMilestoneHolder.getActiveMilestone() >> Optional.empty()

		deletionDao.findTestCasesWhichMilestonesForbidsDeletion(_) >> []
	}


	//if there is a groovy way to do that please tell me
	private boolean containsValue(List<Object[]> list, Object[] value){
		for (Object[] item : list){
			boolean match = true;
			for (int i=0;i<value.length;i++){
				if ( item[i] != value[i]){
					match=false;
					break;
				}
			}
			if (match) return true;
		}
		return false;

	}





	def "should return a report about which nodes cannot be deleted and why (#1)"(){
		given :
		calltreeFinder.getCallerGraph(_) >> testGraph1()

		when :
		def report = handler.previewLockedNodes([21l, 22l, 23l, 24l, 25l]);

		then :
		report.nodeNames == ["21", "22", "23", "24"] as Set
		report.why == ["11", "12", "1"] as Set


	}


	def "should return a report about which nodes cannot be deleted and why (#2)"(){
		given :
		calltreeFinder.getCallerGraph(_) >> testGraph2()

		when :
		def report = handler.previewLockedNodes([11l, 12l]);

		then :
		report.nodeNames == ["11", "12"] as Set
		report.why == ["1"] as Set


	}


	def "should return no report about which nodes cannot be deleted and why (#3)"(){
		given :
		calltreeFinder.getCallerGraph(_) >> testGraph2()

		when :
		def report = handler.previewLockedNodes([11l, 12l, 1l]);

		then :
		report == null
	}


	def "should return really non deletable nodes (#1)"(){
		given :
		calltreeFinder.getCallerGraph(_) >> testGraph1()


		when :
		def reallyNonDeletables = handler.detectLockedNodes([21l, 22l, 23l, 24l, 25l])

		then :
		reallyNonDeletables as Set == [21l, 22l, 23l, 24l] as Set


	}


	def "should return really non deletable nodes (#2)"(){
		given :
		calltreeFinder.getCallerGraph(_) >> testGraph2()

		when :
		def reallyNonDeletables = handler.detectLockedNodes([11l, 12l])

		then :
		reallyNonDeletables as Set == [11l, 12l] as Set

	}


	def "should return really non deletable nodes (#3)"(){
		given :
		calltreeFinder.getCallerGraph(_) >> testGraph2()

		when :
		def reallyNonDeletables = handler.detectLockedNodes([11l, 12l, 1l])

		then :
		reallyNonDeletables == []
	}


	// ********************************* private utilities

	NamedReference ref(id){
		return new NamedReference(id, id?.toString());
	}

	SimpleNode node(id){
		return (id != null) ? new SimpleNode(ref(id)) : null;
	}



	LibraryGraph testGraph1(){

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

	LibraryGraph testGraph2(){

		def layer0 = [ [ null, 1l ] ]
		def layer1 = [ [ 1l, 11l ], [ 1l, 12l ]  ]

		def allData = layer0 + layer1


		LibraryGraph g = new LibraryGraph()
		allData.each{ g.addEdge(node(it[0]), node(it[1])) }

		return g
	}
}
