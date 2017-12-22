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
package org.squashtest.tm.web.internal.model.builder;

import org.squashtest.tm.web.internal.controller.generic.NodeBuildingSpecification

import javax.inject.Provider

import org.apache.commons.collections.MultiMap
import org.apache.commons.collections.map.MultiValueMap
import org.squashtest.tm.tools.unittest.reflection.ReflectionCategory
import org.squashtest.tm.domain.library.Library
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.milestone.MilestoneStatus;
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.testcase.TestCaseImportance
import org.squashtest.tm.domain.testcase.TestCaseLibrary
import org.squashtest.tm.domain.testcase.TestCaseStatus
import org.squashtest.tm.service.milestone.MilestoneMembershipFinder;
import org.squashtest.tm.service.requirement.VerifiedRequirementsManagerService
import org.squashtest.tm.service.security.PermissionEvaluationService
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper
import org.squashtest.tm.service.internal.dto.json.JsTreeNode
import org.squashtest.tm.service.internal.dto.json.JsTreeNode.State
import spock.lang.Unroll

class DriveNodeBuilderTest extends NodeBuildingSpecification {
	PermissionEvaluationService permissionEvaluationService = Mock()
    Map rights = Mock()
	VerifiedRequirementsManagerService verifiedRequirementsManagerService = Mock()
	Provider nodeBuilderPovider = Mock()
	DriveNodeBuilder builder = new DriveNodeBuilder(permissionEvaluationService, nodeBuilderPovider)
	MilestoneMembershipFinder milestoneMembershipFinder = Mock()
	InternationalizationHelper internationalizationHelper = Mock()
    Boolean hasRights
	def setup() {
		internationalizationHelper.internationalize(_,_)>> ""
		internationalizationHelper.internationalizeYesNo(false, _)>>"non"
		internationalizationHelper.internationalizeYesNo(true, _)>>"oui"
		internationalizationHelper.getMessage(_, _, _, _)>>"message"
		nodeBuilderPovider.get() >> {
			TestCaseLibraryTreeNodeBuilder builder = new TestCaseLibraryTreeNodeBuilder(permissionEvaluationService, verifiedRequirementsManagerService, internationalizationHelper)
			builder.setMilestoneMembershipFinder(milestoneMembershipFinder)
			return builder
		}

        permissionEvaluationService.hasRoleOrPermissionsOnObject(_, _, _) >> rights
        rights.get(_) >> { hasRights }
        hasRights = true

	}

	def "should build root node of test case library"() {
		given:
		def library = theTestCaseLibrary(10L).ofProject("foo")
		milestoneMembershipFinder.findAllMilestonesForTestCase(_) >> []

        and:
        hasRights = false

		when:
		JsTreeNode res = builder.setModel(library).build();

		then:
		res.attr['rel'] == "drive"
		res.attr['resId'] == "10"
		res.attr['resType'] == 'test-case-libraries'
		res.title == "foo"
		res.state == JsTreeNode.State.leaf.name()
		res.attr['editable'] == 'false'
	}

	def theTestCaseLibrary(long id) {
		TestCaseLibrary library = new TestCaseLibrary()

		use(ReflectionCategory) {
			TestCaseLibrary.set(field: "id", of:library, to: id)
		}

		return [ofProject: { ofProject library, it}]
	}

	def ofProject(TestCaseLibrary library, String name) {
		Project project = Mock(Project)
		project.getName() >> name
		project.getId() >> 10l
		library.project = project
		return library
	}

	def "should build editable node"() {
		given:
		permissionEvaluationService.hasRoleOrPermissionOnObject (_, _, _) >> true
		milestoneMembershipFinder.findAllMilestonesForTestCase(_) >> []

		and:
		def library = theTestCaseLibrary(10L).ofProject("foo")

		when:
		JsTreeNode res = builder.setModel(library).build();

		then:
		res.attr['editable'] == 'true'
	}

	def "node should reference authorized wizards"() {
		given:
		def library = theTestCaseLibrary(10L).ofProject("foo")
		library.enablePlugin("foo");
		library.enablePlugin("bar");
		milestoneMembershipFinder.findAllMilestonesForTestCase(_) >> []

		when:
		JsTreeNode res = builder.setModel(library).build();

		then:
		res.attr["wizards"].collect { it } as Set ==  ["foo", "bar"] as Set
	}

	def "should build an expanded node"() {
		given:
		Library library = theTestCaseLibrary(10L).ofProject("foo")
		TestCase tc = Mock()
		def visitor
		tc.accept({ visitor = it }) >> { visitor.visit(tc) }
		tc.getStatus() >> TestCaseStatus.WORK_IN_PROGRESS
		tc.getImportance() >> TestCaseImportance.LOW
		tc.getSteps()>>[]
		tc.getRequirementVersionCoverages() >> []
		tc.getId()>>23L


		Milestone m = Mock()
		m.getStatus() >> MilestoneStatus.IN_PROGRESS
		milestoneMembershipFinder.findAllMilestonesForTestCase(_) >> [m]

		library.addContent tc

		and:
		MultiMap expanded = new MultiValueMap()
		expanded.put("TestCaseLibrary", 10L);

		when:
		JsTreeNode res = builder.expand(expanded).setModel(library).build();

		then:
		res.state == State.open.name()
		res.children.size() == 1
		res.children[0].getAttr().get("milestones") == 1

	}

	@Unroll
	def "should candidate [#expandedType, #expandedId] be expanded : #expected"() {
		given:
		Library library = theTestCaseLibrary(10L).ofProject("foo")

		and:
		MultiMap expanded = new MultiValueMap()
		expanded.put(expandedType, expandedId);

		expect:
		builder.expand(expanded).setModel(library).shouldExpandModel() == expected

		where:
		expandedType      | expandedId | expected
		"TestCaseLibrary" | 10L        | true
		"TestCaseLibrary" | 20L        | false
		"Whatever"        | 10L        | false

	}
}
