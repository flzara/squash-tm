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
package org.squashtest.tm.service.internal.batchimport

import org.squashtest.tm.service.internal.batchimport.ImportedRequirementTree.Node
import spock.lang.Specification

class ImportedRequirementTreeTest extends Specification {

	def setupSpec(){
		String.metaClass.toReqTarget = {
			new RequirementTarget(delegate)
		}
	}

	def "should insert a requirement and all its hierarchy in the model"(){

		given :
		ImportedRequirementTree tree = new ImportedRequirementTree()

		RequirementTarget target = "/project/home/me/requirement".toReqTarget()
		TargetStatus status = new TargetStatus(Existence.TO_BE_CREATED)

		when :
		tree.addOrUpdateNode(target, status)

		then :
		def allNodes = tree.getNodes()

		allNodes.collect{ it.key.path } as Set == ["/project", "/project/home", "/project/home/me", "/project/home/me/requirement"] as Set

		Node node1 = tree.getNode("/project".toReqTarget())
		Node node2 = tree.getNode("/project/home".toReqTarget())
		Node node3 = tree.getNode("/project/home/me".toReqTarget())
		Node node4 = tree.getNode("/project/home/me/requirement".toReqTarget())

		node1.isRequirement() == false
		node1.isVirtual() == true
		checkConnections node1, [], [node2]

		node2.isRequirement() == false
		node2.isVirtual() == true
		checkConnections node2, [node1], [node3]

		node3.isRequirement() == false
		node3.isVirtual() == true
		checkConnections node3, [node2], [node4]

		node4.isRequirement() == true
		node4.isVirtual() == false
		checkConnections node4, [node3], []

	}


	def "should insert two requirements that totally different ancestry"(){

		given :
		ImportedRequirementTree tree = new ImportedRequirementTree()

		and :
		RequirementTarget target1 = "/project/home/requirement".toReqTarget()
		TargetStatus status1 = new TargetStatus(Existence.TO_BE_CREATED)

		and :
		RequirementTarget target2 = "/anotherproject/home/requirement".toReqTarget()
		TargetStatus status2 = new TargetStatus(Existence.TO_BE_CREATED)

		when :
		tree.addOrUpdateNode(target1, status1)
		tree.addOrUpdateNode(target2, status2)

		then :
		def allNodes = tree.getNodes()
		allNodes.size() == 6

		def node1 = tree.getNode("/project".toReqTarget())
		def node2 = tree.getNode("/project/home".toReqTarget())
		def node3 = tree.getNode("/project/home/requirement".toReqTarget())
		def node4 = tree.getNode("/anotherproject".toReqTarget())
		def node5 = tree.getNode("/anotherproject/home".toReqTarget())
		def node6 = tree.getNode("/anotherproject/home/requirement".toReqTarget())


		checkConnections node1, [], [node2]
		checkConnections node2, [node1], [node3]
		checkConnections node3, [node2], []
		checkConnections node4, [], [node5]
		checkConnections node5, [node4], [node6]
		checkConnections node6, [node5], []

	}


	def "should attach a subhierarchy of a requirement to an existing branch of the tree"(){

		given :
		ImportedRequirementTree tree = new ImportedRequirementTree()

		and :
		RequirementTarget target1 = "/project/home/me/requirement".toReqTarget()
		TargetStatus status1 = new TargetStatus(Existence.TO_BE_CREATED)

		and :
		RequirementTarget target2 = "/project/home/somewhere/deep/anotherrequirement".toReqTarget()
		TargetStatus status2 = new TargetStatus(Existence.TO_BE_CREATED)

		when :
		tree.addOrUpdateNode(target1, status1)
		tree.addOrUpdateNode(target2, status2)

		then :

		tree.getNodes().size() == 7

		def node1 = tree.getNode("/project".toReqTarget())
		def node2 = tree.getNode("/project/home".toReqTarget())
		def node3 = tree.getNode("/project/home/me".toReqTarget())
		def node4 = tree.getNode("/project/home/me/requirement".toReqTarget())
		def node5 = tree.getNode("/project/home/somewhere".toReqTarget())
		def node6 = tree.getNode("/project/home/somewhere/deep".toReqTarget())
		def node7 = tree.getNode("/project/home/somewhere/deep/anotherrequirement".toReqTarget())

		checkConnections node1, [], [node2]
		checkConnections node2, [node1], [node3, node5]
		checkConnections node3, [node2], [node4]
		checkConnections node4, [node3], []
		checkConnections node5, [node2], [node6]
		checkConnections node6, [node5], [node7]
		checkConnections node7, [node6], []

	}


	def "should create a requirement hierarchy nested under an existing requirement"(){

		given :
		ImportedRequirementTree tree = new ImportedRequirementTree()

		and :
		RequirementTarget target1 = "/project/home/requirement".toReqTarget()
		TargetStatus status1 = new TargetStatus(Existence.TO_BE_CREATED)

		and :
		RequirementTarget target2 = "/project/home/requirement/nestedrequirement/subrequirement".toReqTarget()
		TargetStatus status2 = new TargetStatus(Existence.TO_BE_CREATED)


		when :
		tree.addOrUpdateNode(target1, status1)
		tree.addOrUpdateNode(target2, status2)

		then :
		def nested = tree.getNode("/project/home/requirement/nestedrequirement".toReqTarget())
		def sub = tree.getNode("/project/home/requirement/nestedrequirement/subrequirement".toReqTarget())

		nested.isRequirement() == true
		sub.isRequirement() == true

	}

	def "should update an existing node"(){

		given :
		ImportedRequirementTree tree = new ImportedRequirementTree()

		and :
		RequirementTarget target1 = "/project/home/requirement".toReqTarget()
		TargetStatus status1 = new TargetStatus(Existence.TO_BE_CREATED)

		RequirementTarget target2 = "/project/home/requirement".toReqTarget()
		target2.order = 5
		TargetStatus status2 = new TargetStatus(Existence.EXISTS, 5l)

		and :
		tree.addOrUpdateNode(target1, status1)

		when :
		tree.addOrUpdateNode(target2, status2)

		then :

		def node = tree.getNode("/project/home/requirement".toReqTarget())
		node.key.order == 5
		node.getStatus().getStatus() == Existence.EXISTS

	}

	def "a node that was though to be a folder, happens to be a requirement. All its children become requirements."(){

		given :
		given :
		ImportedRequirementTree tree = new ImportedRequirementTree()

		and :
		RequirementTarget deepTarget = "/project/home/requirement/nested/deep".toReqTarget()
		TargetStatus deepStatus = new TargetStatus(Existence.TO_BE_CREATED)

		and :
		RequirementTarget middleTarget = "/project/home/requirement".toReqTarget()
		TargetStatus middleStatus = new TargetStatus(Existence.TO_BE_CREATED)


		tree.addOrUpdateNode deepTarget, deepStatus

		when :
		tree.addOrUpdateNode middleTarget, middleStatus

		then :
		def nested = tree.getNode("/project/home/requirement/nested".toReqTarget())
		nested.isRequirement() == true
	}

	def checkConnections = { node, ins, outs ->
		def bIns = (node.inbounds as Set == ins as Set)
		def bOuts = (node.outbounds as Set == outs as Set)
		bIns && bOuts
	}

}
