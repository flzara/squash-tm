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
package org.squashtest.tm.service.internal.chart.engine

import org.squashtest.tm.domain.testcase.QTestCase;
import org.squashtest.tm.service.internal.chart.engine.DetailedChartQuery;
import org.squashtest.tm.service.internal.chart.engine.QueryPlan;
import org.squashtest.tm.service.internal.chart.engine.QueryPlan.QueryPlanJoinIterator;
import org.squashtest.tm.service.internal.chart.engine.QueryPlan.TraversedEntity;
import org.squashtest.tm.service.internal.customfield.DefaultEditionStatusStrategy;

import com.querydsl.jpa.hibernate.HibernateQuery;

import spock.lang.Specification
import spock.lang.Unroll;
import static org.squashtest.tm.service.internal.chart.engine.InternalEntityType.*;

class QueryPlanTest extends Specification {


	// some abreviations

	static InternalEntityType REQ = REQUIREMENT
	static InternalEntityType RV = REQUIREMENT_VERSION
	static InternalEntityType COV = REQUIREMENT_VERSION_COVERAGE
	static InternalEntityType TC = TEST_CASE
	static InternalEntityType ITP = ITEM_TEST_PLAN
	static InternalEntityType IT = ITERATION
	static InternalEntityType CP = CAMPAIGN
	static InternalEntityType EX = EXECUTION
	static InternalEntityType ISS = ISSUE

	// *************** tree trim test ********************


	@Unroll
	def "should trim to fit the chart definition"(){

		expect :
		// given
		def tree = buildTree(rootEntity, nodes)
		def chartDef = new DetailedChartQuery(rootEntity : rootEntity, targetEntities : targetEntities)
		tree.trim(chartDef)

		// then
		tree.collectKeys() as Set == res as Set

		where :

		rootEntity 	|	targetEntities		| res					|	nodes
		TC			|	[TC, CP]			| [TC, ITP, IT, CP]		|	[[TC, COV], [COV, RV], [RV, REQ], [TC, ITP], [ITP, IT], [IT, CP], [ITP, EX], [EX, ISS]]
		ISS			|	[ISS, ITP]			| [ISS, EX, ITP]		|	[[ISS, EX], [EX, ITP], [ITP, TC],[TC, COV], [COV, RV], [RV, REQ], [ITP, IT], [IT, CP]]
		ITP			|	[ITP, TC, IT, EX]	| [ITP, TC, IT, EX]		|	[[ITP, TC], [TC, COV], [COV, RV], [RV, REQ], [ITP, EX], [EX, ISS], [ITP, IT], [IT, CP]]

	}


	// *************** iterator test ************************


	def "should init properly (1)"(){

		given :
		def traversedTypes = [ [ITP, TC], [TC, COV], [COV, RV], [RV, REQ], [ITP, EX], [EX, ISS]]
		def tree = buildTree(ITP, traversedTypes)

		when :
		def iter = tree.joinIterator()

		then :
		iter.plan == tree
		iter.currentParent.key == ITP
		iter.currentChild.key == TC
		iter.toProcess.collect{it.key} == [TC]
		iter.remainingChildren.collect { it.key} as Set == [EX] as Set
		iter.hasNext == true

	}

	def "should init properly (2)"(){

		given :
		def tree = buildTree(ITP, [])

		when :
		def iter = tree.joinIterator()

		then :
		iter.plan == tree
		iter.currentParent.key == ITP
		iter.currentChild == null
		iter.toProcess.size() == 0
		iter.remainingChildren.size() == 0
		iter.hasNext == false

	}

	def "should move to next element (next child)"(){
		given :
		def traversedTypes = [ [ITP, TC], [TC, COV], [COV, RV], [RV, REQ], [ITP, EX], [EX, ISS]]
		def tree = buildTree(ITP, traversedTypes)

		and :
		def iter = tree.joinIterator()
		setIteratorState(iter, EX, [ISS], [])

		when :
		iter.armNext()

		then :
		iter.currentParent.key == EX
		iter.currentChild.key == ISS
		iter.toProcess.collect{it.key}  ==  [ISS]
		iter.remainingChildren == []
		iter.hasNext == true

	}


	def "should move to next element (next parent)"(){
		given :
		def traversedTypes = [ [ITP, TC], [TC, COV], [COV, RV], [RV, REQ], [ITP, EX], [EX, ISS]]
		def tree = buildTree(ITP, traversedTypes)

		and :
		def iter = tree.joinIterator()
		setIteratorState(iter, EX, [], [RV])

		when :
		iter.armNext()

		then :
		iter.currentParent.key == RV
		iter.currentChild.key == REQ
		iter.toProcess.collect{it.key}  ==  [REQ]
		iter.remainingChildren == []
		iter.hasNext == true

	}

	def "should find no next element (no more parent nor children)"(){
		given :
		def traversedTypes = [ [ITP, TC], [ITP, EX], [ITP, IT]]
		def tree = buildTree(ITP, traversedTypes)

		and :
		def iter = tree.joinIterator()
		setIteratorState(iter, EX, [], [])

		when :
		iter.armNext()

		then :
		iter.hasNext == false

	}


	def "should find no next element (only childless parents left)"(){
		given :
		def traversedTypes = [ [ITP, TC], [ITP, EX], [ITP, IT]]
		def tree = buildTree(ITP, traversedTypes)

		and :
		def iter = tree.joinIterator()
		setIteratorState(iter, ITP, [], [TC, EX, IT])

		when :
		iter.armNext()

		then :
		iter.hasNext == false

	}

	def "should build a pair and move to next"(){
		given :
		def traversedTypes = [ [ITP, TC], [ITP, EX], [ITP, IT]]
		def tree = buildTree(ITP, traversedTypes)

		and :
		def iter = tree.joinIterator()
		setIteratorState(iter, ITP, [EX], [IT, TC])
		iter.currentChild = tree.getNode(TC)

		when :
		def n = iter.next()

		then :
		n.a1 == ITP
		n.a2 == TC

		iter.currentParent.key == ITP
		iter.currentChild.key == EX

		iter.toProcess.collect{it.key} as Set == [IT, TC, EX] as Set
		iter.remainingChildren as Set == [] as Set
		iter.hasNext == true


	}



	def "should iterate over the joins"(){

		given :

		def traversedTypes = [ [ITP, TC], [TC, COV], [COV, RV], [RV, REQ], [ITP, EX], [EX, ISS]]
		def expected = traversedTypes

		def tree = buildTree(ITP, traversedTypes)

		when :

		def joins = []
		def iter = tree.joinIterator()
		while (iter.hasNext()){ def j = iter.next(); joins << [j.src, j.dest] }

		then :

		joins as Set == expected as Set



	}

	// ************ scaffolding code *****************

	def buildTree(root, nodes){

		def tree = new QueryPlan()

		tree.addNode(null, new TraversedEntity(root))

		nodes.each{ tree.addNode(it[0], new TraversedEntity(it[1]), new PlannedJoin(it[0], it[1], "whatever") ) }


		return tree
	}

	def setIteratorState(iter, currentParent, remainingChildren, toProcess){
		iter.currentParent = iter.plan.getNode(currentParent)
		iter.toProcess = toProcess.collect { iter.plan.getNode(it) } as LinkedList
		iter.remainingChildren = remainingChildren.collect { iter.plan.getNode(it) } as LinkedList
	}




}
