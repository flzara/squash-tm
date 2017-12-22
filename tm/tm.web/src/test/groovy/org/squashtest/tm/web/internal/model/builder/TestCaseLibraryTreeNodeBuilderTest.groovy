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
package org.squashtest.tm.web.internal.model.builder

import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.squashtest.tm.tools.unittest.reflection.ReflectionCategory
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.testcase.TestCaseFolder
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode
import org.squashtest.tm.service.milestone.MilestoneMembershipFinder;
import org.squashtest.tm.service.requirement.VerifiedRequirementsManagerService;
import org.squashtest.tm.service.security.PermissionEvaluationService
import org.squashtest.tm.web.internal.controller.generic.NodeBuildingSpecification
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.service.internal.dto.json.JsTreeNode.State

class TestCaseLibraryTreeNodeBuilderTest extends NodeBuildingSpecification {
	PermissionEvaluationService permissionEvaluationService = Mock()
	VerifiedRequirementsManagerService verifiedRequirementsManagerService = Mock()
	InternationalizationHelper internationalizationHelper = Mock()
    TestCaseLibraryTreeNodeBuilder builder = new TestCaseLibraryTreeNodeBuilder(permissionEvaluator(), verifiedRequirementsManagerService, internationalizationHelper)
	MilestoneMembershipFinder milestoneMembershipFinder = Mock()

	def setup() {
		internationalizationHelper.internationalize(_,_)>> ""
		internationalizationHelper.internationalizeYesNo(false, _)>>"non"
		internationalizationHelper.internationalizeYesNo(true, _)>>"oui"
		internationalizationHelper.getMessage(_, _, _, _)>>"message"

		builder.setMilestoneMembershipFinder(milestoneMembershipFinder)
		milestoneMembershipFinder.findAllMilestonesForTestCase(_) >> []
	}
	def "should build a TestCase tree node"() {
		given:
		TestCase node  = new TestCase(name: "tc")

		use (ReflectionCategory) {
			TestCaseLibraryNode.set field: "id", of: node, to: 10L
		}

		when:
		def res = builder.setNode(node).build()

		then:
		res.title == node.name
		res.attr['resId'] == "${node.id}"
		res.attr['rel'] == "test-case"
		res.attr['resType'] == "test-cases"
		res.state == State.leaf.name()
	}

	def "should build a TestCaseFolder node"() {
		given:
		TestCaseFolder node  = new TestCaseFolder(name: "tc")

		use (ReflectionCategory) {
			TestCaseLibraryNode.set field: "id", of: node, to: 10L
		}

		when:
		def res = builder.setNode(node).build()

		then:
		res.title == node.name
		res.attr['resId'] == "${node.id}"
		res.attr['rel'] == "folder"
		res.attr['resType'] == "test-case-folders"
		res.state == State.leaf.name()
	}

	def "should build a folder with leaf state"(){
		given :
		TestCaseFolder node = new TestCaseFolder(name:"folder")

		when :
		def res = builder.setNode(node).build()

		then :
		res.state == State.leaf.name()

	}

	def "should build a folder with closed state"(){
		given :
		TestCaseFolder node = new TestCaseFolder(name:"folder")
		node.addContent(new TestCaseFolder());

		when :
		def res = builder.setNode(node).build()

		then :
		res.state == State.closed.name()

	}

	def "should expand a folder "(){
		given :
		TestCaseFolder node = new TestCaseFolder(name:"folder")
		TestCaseFolder child = new TestCaseFolder(name:"folder child")
		node.addContent(child);

		use(ReflectionCategory) {
			TestCaseLibraryNode.set field: "id", of: node, to: 10L
			TestCaseLibraryNode.set field: "id", of: child, to: 100L
		}

		and:
		MultiMap expanded = new MultiValueMap()
		expanded.put("TestCaseFolder", 10L)

		when :
		def res = builder.expand(expanded).setNode(node).build()

		then :
		res.state == State.open.name()
		res.children.size() == 1

	}
}
