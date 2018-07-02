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

import org.squashtest.tm.service.internal.chart.engine.InternalEntityType;
import static org.squashtest.tm.service.internal.chart.engine.InternalEntityType.*;
import org.squashtest.tm.service.internal.chart.engine.DetailedChartQuery;
import org.squashtest.tm.service.internal.chart.engine.DomainGraph;
import org.squashtest.tm.service.internal.chart.engine.QueryPlan;

import spock.lang.Specification
import spock.lang.Unroll;
import static org.squashtest.tm.domain.EntityType.*
import org.apache.commons.collections.Transformer

class DomainGraphTest extends Specification {

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
	static InternalEntityType TS = TEST_CASE_STEP
	static InternalEntityType US = ITERATION_TEST_PLAN_ASSIGNED_USER
	static InternalEntityType NAT = TEST_CASE_NATURE
	static InternalEntityType TYP = TEST_CASE_TYPE
	static InternalEntityType CAT = REQUIREMENT_VERSION_CATEGORY
	static InternalEntityType TCMIL = TEST_CASE_MILESTONE
	static InternalEntityType RVMIL = REQUIREMENT_VERSION_MILESTONE
	static InternalEntityType CMIL = CAMPAIGN_MILESTONE
	static InternalEntityType TATEST = AUTOMATED_TEST
	static InternalEntityType EXTEND = AUTOMATED_EXECUTION_EXTENDER




	/*
	 * General properties :
	 * 1/ the root node has no inbounds
	 * 2/ the other nodes has 1 inbound only
	 * 3/ all entities are traversed
	 */
	@Unroll("check general asumption about morphed graphs with #rootEntity (see comments)")
	def "check general asumption about morphed graphs (see comments)"(){

		expect :
		def domain = new DomainGraph(definition);
		domain.morphToQueryPlan();

		// root entity has no inbound connections
		countInbounds(domain, rootEntity) == 0

		// all other node have exactly one inbound connection
		domain.nodes.findAll{it.type != rootEntity} as Set == domain.nodes.findAll{countInbounds(domain, it.type) == 1 } as Set
		domain.nodes.collect{it.type} as Set == InternalEntityType.values() as Set


		where :
		rootEntity				|	definition
		REQUIREMENT				|	new DetailedChartQuery(rootEntity : REQ)
		REQUIREMENT_VERSION 	|	new DetailedChartQuery(rootEntity : RV)
		COV					 	|	new DetailedChartQuery(rootEntity : COV)
		TEST_CASE				|	new DetailedChartQuery(rootEntity : TC)
		ITEM_TEST_PLAN			|	new DetailedChartQuery(rootEntity : ITP)
		ITERATION				|	new DetailedChartQuery(rootEntity : IT)
		CAMPAIGN				|	new DetailedChartQuery(rootEntity : CP)
		EXECUTION				|	new DetailedChartQuery(rootEntity : EX)
		ISS						|	new DetailedChartQuery(rootEntity : ISS)

	}




	@Unroll
	def "should test many query plans"(){

		expect :
		def domain = new DomainGraph(new DetailedChartQuery(rootEntity : rootEntity, targetEntities : targets))
		def plan = domain.getQueryPlan()

		checkAllTreeHierarchy(plan, hierarchy)

		where :

		// let's use the abbreviations
		rootEntity	|	targets				|	hierarchy
		REQ			|	[REQ, TC]			|	[ REQ : [RV], RV : [COV], COV : [TC], TC : [] ]
		ISS			|	[ISS, TC, IT]		|	[ ISS : [EX], EX : [ITP], ITP : [TC, IT], TC : [], IT : []]
		IT			|	[IT, ISS]			|	[ IT : [ITP], ITP : [EX], EX : [ISS], ISS : []]
		CP			|	[REQ, ISS]			|	[ CP : [IT], IT : [ITP], ITP : [TC, EX], TC : [COV], COV : [RV], RV : [REQ], REQ : [], EX : [ISS], ISS : []]
		ITP			|	[REQ, CP, ISS]		|	[ ITP : [TC, IT, EX], TC : [COV], COV : [RV], RV: [REQ], REQ : [], IT : [CP], CP : [], EX : [ISS], ISS : []]
		TC			|	[TC, TCMIL, NAT]	|	[ TC : [TCMIL, NAT], TCMIL : [], NAT : []]
		TC			|	[TC, IT, TATEST]	|	[ TC : [ITP, TATEST], ITP : [IT], TATEST : [], IT : []]
	}

	@Unroll
	def "should convey the correct join metadata when creating the tree"(){

		expect :
		def domain = new DomainGraph(new DetailedChartQuery(rootEntity : rootEntity, targetEntities : targets))
		def plan = domain.getQueryPlan()

		checkAllTreeJoins(plan, joinInfos)

		where :

		// let's use the abbreviations
		rootEntity	|	targets				|	joinInfos
		REQ			|	[REQ, TC]			|	[ REQ : [RV:"versions"], RV : [COV : "requirementVersionCoverages"], COV : [TC:"verifyingTestCase"]]
		ISS			|	[ISS, TC, IT]		|	[ ISS : [EX : "execution"], EX : [ITP : "testPlan"], ITP : [TC:"referencedTestCase", IT:"iteration"]]
		IT			|	[IT, ISS]			|	[ IT : [ITP:"testPlans"], ITP : [EX:"executions"], EX : [ISS:"issues"]]
		CP			|	[REQ, ISS]			|	[ CP : [IT:"iterations"], IT : [ITP:"testPlans"], ITP : [TC:"referencedTestCase", EX:"executions"], TC : [COV:"requirementVersionCoverages"], COV : [RV:"verifiedRequirementVersion"], EX : [ISS:"issues"]]
		ITP			|	[REQ, CP, ISS]		|	[ ITP : [TC:"referencedTestCase", IT:"iteration", EX:"executions"], TC : [COV:"requirementVersionCoverages"], COV : [RV:"verifiedRequirementVersion"], RV: [REQ:"requirement"], IT : [CP :"campaign"], EX : [ISS:"issues"]]
		RV			|	[RV, TC, TCMIL, CAT]|	[ RV : [COV:"requirementVersionCoverages", CAT:"category"], COV : [TC:"verifyingTestCase"], TC : [TCMIL:"milestones"], CAT : [], TCMIL : []]

	}

	def "should morph to a directed graph and generate an oversized query plan"(){

		given :
		DetailedChartQuery definition =
				new DetailedChartQuery(rootEntity : TEST_CASE,
				targetEntities : [TEST_CASE, REQUIREMENT, CAMPAIGN])

		and :
		def domain = new DomainGraph(definition)

		when :
		def plan = domain.morphToQueryPlan();

		then :

		// check how the graph has been modified
		checkIsDirectedEdge domain, TEST_CASE, REQUIREMENT_VERSION_COVERAGE
		checkIsDirectedEdge domain, REQUIREMENT_VERSION_COVERAGE, REQUIREMENT_VERSION
		checkIsDirectedEdge domain, REQUIREMENT_VERSION, REQUIREMENT
		checkIsDirectedEdge domain, TEST_CASE, ITEM_TEST_PLAN
		checkIsDirectedEdge domain, ITEM_TEST_PLAN, ITERATION
		checkIsDirectedEdge domain, ITERATION, CAMPAIGN
		checkIsDirectedEdge domain, ITEM_TEST_PLAN, EXECUTION
		checkIsDirectedEdge domain, EXECUTION, ISS


		// check the resulting tree (remember it has not been trimmed yet)
		def allroots= plan.getRootNodes()
		allroots.size() == 1

		def root = allroots[0]
		root.key == TEST_CASE

		checkTreeHierarchy(plan, TEST_CASE, [ITEM_TEST_PLAN, REQUIREMENT_VERSION_COVERAGE, TCMIL, NAT, TYP, TS, TATEST]);
		checkTreeHierarchy(plan, TATEST, [])
		checkTreeHierarchy(plan, REQUIREMENT_VERSION_COVERAGE, [REQUIREMENT_VERSION]);
		checkTreeHierarchy(plan, REQUIREMENT_VERSION, [REQUIREMENT, RVMIL, CAT ]);
		checkTreeHierarchy(plan, REQUIREMENT, [])
		checkTreeHierarchy(plan, ITEM_TEST_PLAN, [ITERATION, EXECUTION, US])
		checkTreeHierarchy(plan, EXECUTION, [ISS, EXTEND])
		checkTreeHierarchy(plan, EXTEND, [])
		checkTreeHierarchy(plan, ISS, [])
		checkTreeHierarchy(plan, ITERATION, [CAMPAIGN])
		checkTreeHierarchy(plan, CAMPAIGN, [CMIL])
		checkTreeHierarchy(plan, TCMIL, [])
		checkTreeHierarchy(plan, RVMIL, [])
		checkTreeHierarchy(plan, US, [])
		checkTreeHierarchy(plan, NAT, [])
		checkTreeHierarchy(plan, TYP, [])
		checkTreeHierarchy(plan, CAT, [])

	}


	def "should find a query plan for root entity TestCase and other target entities : Requirement, Iteration"(){

		given :
		DetailedChartQuery definition =
				new DetailedChartQuery(rootEntity : TEST_CASE,
				targetEntities : [TEST_CASE, REQUIREMENT, CAMPAIGN])

		when :
		def domain = new DomainGraph(definition);
		QueryPlan plan = domain.getQueryPlan();

		then :

		def traversed = plan.collectKeys() as Set

		traversed as Set == [ REQUIREMENT, REQUIREMENT_VERSION, REQUIREMENT_VERSION_COVERAGE, TEST_CASE, ITEM_TEST_PLAN, ITERATION, CAMPAIGN ] as Set

		def root = plan.getRootNodes()[0];
		root.key == TEST_CASE

		checkTreeHierarchy(plan, TEST_CASE, [ITEM_TEST_PLAN, REQUIREMENT_VERSION_COVERAGE]);
		checkTreeHierarchy(plan, REQUIREMENT_VERSION_COVERAGE, [REQUIREMENT_VERSION]);
		checkTreeHierarchy(plan, REQUIREMENT_VERSION, [REQUIREMENT]);
		checkTreeHierarchy(plan, REQUIREMENT, [])
		checkTreeHierarchy(plan, ITEM_TEST_PLAN, [ITERATION])
		checkTreeHierarchy(plan, ITERATION, [CAMPAIGN])
		checkTreeHierarchy(plan, CAMPAIGN, [])

	}

	def "when requested, should generate a reversed query plan"(){

		given :
		DetailedChartQuery definition =
				new DetailedChartQuery(rootEntity : TEST_CASE, measuredEntity : CAMPAIGN,
				targetEntities : [TEST_CASE, REQUIREMENT, CAMPAIGN])
		when :

		DomainGraph domain = new DomainGraph(definition);
		domain.reversePlan();
		QueryPlan plan = domain.getQueryPlan();

		then :

		def traversed = plan.collectKeys() as Set

		traversed as Set == [ REQUIREMENT, REQUIREMENT_VERSION, REQUIREMENT_VERSION_COVERAGE, TEST_CASE, ITEM_TEST_PLAN, ITERATION, CAMPAIGN ] as Set

		def root = plan.getRootNodes()[0];
		root.key == CAMPAIGN

		checkTreeHierarchy(plan, CAMPAIGN, [ITERATION])
		checkTreeHierarchy(plan, ITERATION, [ITEM_TEST_PLAN])
		checkTreeHierarchy(plan, ITEM_TEST_PLAN, [TEST_CASE])
		checkTreeHierarchy(plan, TEST_CASE, [REQUIREMENT_VERSION_COVERAGE])
		checkTreeHierarchy(plan, REQUIREMENT_VERSION_COVERAGE, [REQUIREMENT_VERSION])
		checkTreeHierarchy(plan, REQUIREMENT_VERSION, [REQUIREMENT])
		checkTreeHierarchy(plan, REQUIREMENT, [])

	}


	def checkTreeHierarchy(QueryPlan tree, InternalEntityType nodetype, List<InternalEntityType> childrenTypes ){
		def node = tree.getNode(nodetype)
		return node.children.collect{it.key} as Set == childrenTypes as Set
	}



	def checkAllTreeHierarchy(QueryPlan tree, Map hierarchies){
		def checkall = true;

		hierarchies.each {k,v -> checkall = checkall &&  checkTreeHierarchy(tree, expand(k), v)}

		return checkall

	}

	def checkAllTreeJoins(QueryPlan tree, Map metas){
		def checkall = true

		metas.each {src, meta ->
			def node = tree.getNode(expand(src))
			meta.each { type, joinName ->
				checkall = checkall && (node.joinInfos[expand(type)].attribute == joinName)
			}
		}

		return checkall
	}

	// I have at heart to say that I first implemented the following
	// with fitter groovy closure that happen not to work

	// not so cool after all
	def countInbounds(DomainGraph graph, InternalEntityType type){
		List inbound = new ArrayList();
		graph.nodes.each { n ->
			n.joinInfos.each{ j ->
				if (j.dest == type){
					inbound.add(j.src)
				}
			}
		}
		if (inbound.size() > 1){
			println "gotone"
		}
		return inbound.size()

	}

	def checkIsDirectedEdge(DomainGraph graph, InternalEntityType srcType, InternalEntityType destType){
		return (
		hasEdge(graph, srcType, destType) &&
		! hasEdge(graph, destType, srcType)
		)
	}


	def hasEdge(DomainGraph graph, InternalEntityType srcType, InternalEntityType destType){
		def srcNode = graph.getNode srcType
		srcNode.joinInfos.any {it.dest == destType}
	}

	def expand(String shortname){
		switch(shortname){
			case "REQ" : return REQUIREMENT;
			case "RV" : return REQUIREMENT_VERSION
			case "COV" : return REQUIREMENT_VERSION_COVERAGE
			case "TC" : return TEST_CASE
			case "ITP" : return ITEM_TEST_PLAN
			case "IT" : return ITERATION
			case "CP" : return CAMPAIGN
			case "EX" : return EXECUTION
			case "ISS" : return ISSUE
			case "TS" : return TS
			case "TCMIL" : return TCMIL
			case "NAT" : return NAT
			case "TYP" : return TYP
			case "CAT" : return CAT
			case "US" : return US
			case "RVMIL" : return RVMIL
			case "TATEST" : return TATEST
		}
	}

}
