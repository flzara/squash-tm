package org.squashtest.tm.service.internal.query
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


import org.squashtest.tm.service.internal.query.InternalEntityType;
import static org.squashtest.tm.service.internal.query.InternalEntityType.*;
import org.squashtest.tm.service.internal.query.InternalQueryModel;
import org.squashtest.tm.service.internal.query.DomainGraph;
import org.squashtest.tm.service.internal.query.QueryPlan;

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

	// since 1.20 :
	static InternalEntityType TC_ALST = TEST_CASE_ATTLIST
	static InternalEntityType TC_ATT = TEST_CASE_ATTACHMENT
	static InternalEntityType RV_ALST = REQUIREMENT_VERSION_ATTLIST
	static InternalEntityType RV_ATT = REQUIREMENT_VERSION_ATTACHMENT
	static InternalEntityType C_ALST = CAMPAIGN_ATTLIST
	static InternalEntityType C_ATT = CAMPAIGN_ATTACHMENT
	static InternalEntityType DS = DATASET
	static InternalEntityType PRM = PARAMETER
	static InternalEntityType AUTOM_REQ = AUTOMATION_REQUEST


	 /*
	 * General properties :
	 * 1/ the graph seed node has no inbounds
	 * 2/ the other nodes has 1 inbound only
	 * 3/ all entities are traversed
	 */

	@Unroll("check general assumption about morphed graphs with #graphSeed (see comments)")
	def "check general asumption about morphed graphs (see comments)"(){

		expect :
		def domain = new DomainGraph(internalQueryModel, graphSeed);
		domain.morphToQueryPlan();

		// root entity has no inbound connections
		countInbounds(domain, graphSeed) == 0

		// all other node have exactly one inbound connection
		domain.nodes.findAll{it.type != graphSeed} as Set == domain.nodes.findAll{countInbounds(domain, it.type) == 1 } as Set
		domain.nodes.collect{it.type} as Set == InternalEntityType.values() as Set


		where :
		graphSeed				|	internalQueryModel
		REQUIREMENT				|	new InternalQueryModel(rootEntity : REQ)
		REQUIREMENT_VERSION 	|	new InternalQueryModel(rootEntity : RV)
		COV					 	|	new InternalQueryModel(rootEntity : COV)
		TEST_CASE				|	new InternalQueryModel(rootEntity : TC)
		ITEM_TEST_PLAN			|	new InternalQueryModel(rootEntity : ITP)
		ITERATION				|	new InternalQueryModel(rootEntity : IT)
		CAMPAIGN				|	new InternalQueryModel(rootEntity : CP)
		EXECUTION				|	new InternalQueryModel(rootEntity : EX)
		ISS						|	new InternalQueryModel(rootEntity : ISS)

	}




	@Unroll
	def "should test many query plans"(){

		expect :
		def domain = new DomainGraph(new InternalQueryModel(targetEntities : targets), graphSeed)
		def plan = domain.getQueryPlan()

		checkAllTreeHierarchy(plan, hierarchy)

		where :

		// let's use the abbreviations
		graphSeed	|	targets				|	hierarchy
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
		def domain = new DomainGraph(new InternalQueryModel(targetEntities : targets), graphSeed)
		def plan = domain.getQueryPlan()

		checkAllTreeJoins(plan, joinInfos)

		where :

		// let's use the abbreviations
		graphSeed	|	targets				|	joinInfos
		REQ			|	[REQ, TC]			|	[ REQ : [RV:"versions"], RV : [COV : "requirementVersionCoverages"], COV : [TC:"verifyingTestCase"]]
		ISS			|	[ISS, TC, IT]		|	[ ISS : [EX : "execution"], EX : [ITP : "testPlan"], ITP : [TC:"referencedTestCase", IT:"iteration"]]
		IT			|	[IT, ISS]			|	[ IT : [ITP:"testPlans"], ITP : [EX:"executions"], EX : [ISS:"issues"]]
		CP			|	[REQ, ISS]			|	[ CP : [IT:"iterations"], IT : [ITP:"testPlans"], ITP : [TC:"referencedTestCase", EX:"executions"], TC : [COV:"requirementVersionCoverages"], COV : [RV:"verifiedRequirementVersion"], EX : [ISS:"issues"]]
		ITP			|	[REQ, CP, ISS]		|	[ ITP : [TC:"referencedTestCase", IT:"iteration", EX:"executions"], TC : [COV:"requirementVersionCoverages"], COV : [RV:"verifiedRequirementVersion"], RV: [REQ:"requirement"], IT : [CP :"campaign"], EX : [ISS:"issues"]]
		RV			|	[RV, TC, TCMIL, CAT]|	[ RV : [COV:"requirementVersionCoverages", CAT:"category"], COV : [TC:"verifyingTestCase"], TC : [TCMIL:"milestones"], CAT : [], TCMIL : []]

	}

	def "should morph to a directed graph and generate an oversized query plan"(){

		given :
		InternalQueryModel internalQueryModel =
				new InternalQueryModel(targetEntities : [TEST_CASE, REQUIREMENT, CAMPAIGN])

		and :
		def domain = new DomainGraph(internalQueryModel, TEST_CASE)

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

		// since 1.20 :
		checkIsDirectedEdge domain, TEST_CASE, TC_ALST
		checkIsDirectedEdge domain, TC_ALST, TC_ATT
		checkIsDirectedEdge domain, RV, RV_ALST
		checkIsDirectedEdge domain, RV_ALST, RV_ATT
		checkIsDirectedEdge domain, CAMPAIGN, C_ALST
		checkIsDirectedEdge domain, C_ALST, C_ATT
		checkIsDirectedEdge domain, TEST_CASE, DS
		checkIsDirectedEdge domain, TEST_CASE, PRM
		checkIsDirectedEdge domain, TEST_CASE, TEST_CASE_PROJECT
		checkIsDirectedEdge domain, REQUIREMENT, REQUIREMENT_PROJECT
		checkIsDirectedEdge domain, CAMPAIGN, CAMPAIGN_PROJECT
		checkIsDirectedEdge domain, ITEM_TEST_PLAN, ITEM_SUITE
		checkIsDirectedEdge domain, TEST_CASE, AUTOM_REQ


		// check the resulting tree (remember it has not been trimmed yet)
		def allroots= plan.getRootNodes()
		allroots.size() == 1

		def root = allroots[0]
		root.key == TEST_CASE

		checkTreeHierarchy(plan, TEST_CASE, [ITEM_TEST_PLAN, REQUIREMENT_VERSION_COVERAGE, TCMIL, NAT, TYP, TS, TATEST, TC_ALST, DS, PRM, TEST_CASE_PROJECT, AUTOM_REQ]);
		checkTreeHierarchy(plan, TATEST, [])
		checkTreeHierarchy(plan, REQUIREMENT_VERSION_COVERAGE, [REQUIREMENT_VERSION]);
		checkTreeHierarchy(plan, REQUIREMENT_VERSION, [REQUIREMENT, RVMIL, CAT, RV_ALST ]);
		checkTreeHierarchy(plan, REQUIREMENT, [REQUIREMENT_PROJECT])
		checkTreeHierarchy(plan, ITEM_TEST_PLAN, [ITERATION, EXECUTION, US, ITEM_SUITE])
		checkTreeHierarchy(plan, EXECUTION, [ISS, EXTEND])
		checkTreeHierarchy(plan, EXTEND, [])
		checkTreeHierarchy(plan, ISS, [])
		checkTreeHierarchy(plan, ITERATION, [CAMPAIGN])
		checkTreeHierarchy(plan, CAMPAIGN, [CMIL, C_ALST, CAMPAIGN_PROJECT])
		checkTreeHierarchy(plan, TCMIL, [])
		checkTreeHierarchy(plan, RVMIL, [])
		checkTreeHierarchy(plan, US, [])
		checkTreeHierarchy(plan, NAT, [])
		checkTreeHierarchy(plan, TYP, [])
		checkTreeHierarchy(plan, CAT, [])
		checkTreeHierarchy(plan, TC_ALST, [TC_ATT])
		checkTreeHierarchy(plan, TC_ATT, [])
		checkTreeHierarchy(plan, RV_ALST, [RV_ATT])
		checkTreeHierarchy(plan, RV_ATT, [])
		checkTreeHierarchy(plan, C_ALST, [C_ATT])
		checkTreeHierarchy(plan, C_ATT, [])
		checkTreeHierarchy(plan, TEST_CASE_PROJECT, [])
		checkTreeHierarchy(plan, REQUIREMENT_PROJECT, [])
		checkTreeHierarchy(plan, CAMPAIGN_PROJECT, [])
		checkTreeHierarchy(plan, ITEM_SUITE, [])

	}


	def "should find a query plan graph seed TestCase and other target entities : Requirement, Iteration"(){

		given :
		InternalQueryModel internalQueryModel =
				new InternalQueryModel(targetEntities : [TEST_CASE, REQUIREMENT, CAMPAIGN])

		when :
		def domain = new DomainGraph(internalQueryModel, TEST_CASE);
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


	// ************* test utils *****************************


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
