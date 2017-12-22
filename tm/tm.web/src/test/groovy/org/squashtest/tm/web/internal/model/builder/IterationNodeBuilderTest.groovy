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
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.Iteration
import org.squashtest.tm.domain.campaign.TestSuite
import org.squashtest.tm.web.internal.controller.generic.NodeBuildingSpecification
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper
import org.squashtest.tm.service.internal.dto.json.JsTreeNode.State

class IterationNodeBuilderTest extends NodeBuildingSpecification {
	InternationalizationHelper internationalizationHelper = Mock()
	IterationNodeBuilder builder = new IterationNodeBuilder(permissionEvaluator(), internationalizationHelper)

	def "should build root node of test case library"() {
		given:
		Campaign c = Mock()
		c.getMilestones() >> []
		c.doMilestonesAllowCreation() >> Boolean.TRUE
		c.doMilestonesAllowEdition() >> Boolean.TRUE
		Iteration iter = new Iteration(name: "it", campaign: c, reference: "ref")
		def id = 10L
		use(ReflectionCategory) {
			Iteration.set(field: "id", of: iter, to: id)
		}


		when:
		def res = builder.setModel(iter).setIndex(4).build();

		then:
		res.attr['rel'] == "iteration"
		res.attr['resId'] == "10"
		res.state == State.leaf.name()
		res.attr['resType'] == "iterations"
		res.title == "ref - it"
	}

	def "should expand iteration"() {
		given:
		Campaign c = Mock()
		c.getMilestones() >> []
		c.doMilestonesAllowCreation() >> Boolean.TRUE
		c.doMilestonesAllowEdition() >> Boolean.TRUE
		Iteration iter = new Iteration(name: "it", campaign: c)
		def id = 10L
		use(ReflectionCategory) {
			Iteration.set(field: "id", of: iter, to: id)
		}

		and:
		TestSuite ts = new TestSuite(iteration: iter)
		iter.testSuites << ts


		and:
		MultiMap expand = new MultiValueMap()
		expand.put("Iteration", 10L)

		when:
		def res = builder.expand(expand).setModel(iter).build();

		then:
		res.state == State.open.name()
		res.children.size() == 1
	}

}
