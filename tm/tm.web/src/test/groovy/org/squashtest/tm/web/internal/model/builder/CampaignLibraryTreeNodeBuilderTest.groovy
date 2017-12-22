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
import org.squashtest.tm.domain.campaign.Campaign
import org.squashtest.tm.domain.campaign.CampaignFolder
import org.squashtest.tm.domain.campaign.CampaignLibraryNode
import org.squashtest.tm.domain.campaign.Iteration
import org.squashtest.tm.web.internal.controller.generic.NodeBuildingSpecification
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper
import org.squashtest.tm.service.internal.dto.json.JsTreeNode.State

class CampaignLibraryTreeNodeBuilderTest extends NodeBuildingSpecification {
	InternationalizationHelper internationalizationHelper = Mock()
	CampaignLibraryTreeNodeBuilder builder = new CampaignLibraryTreeNodeBuilder(permissionEvaluator(),internationalizationHelper)
	def "should build a tree node for a campaign folder"() {
		given:
		CampaignFolder node  = new CampaignFolder(name: "f")

		use (ReflectionCategory) {
			CampaignLibraryNode.set field: "id", of: node, to: 10L
		}

		when:
		def res = builder.setNode(node).build()

		then:
		res.title == node.name
		res.attr['resId'] == "${node.id}"
		res.attr['rel'] == "folder"
		res.attr['resType'] == "campaign-folders"
		res.state == State.leaf.name()
	}
	def "should build a Campaign node"() {
		given:
		Campaign node  = new Campaign(name: "r")

		use (ReflectionCategory) {
			CampaignLibraryNode.set field: "id", of: node, to: 10L
		}

		when:
		def res = builder.setNode(node).build()

		then:
		res.title == node.name
		res.attr['resId'] == "${node.id}"
		res.attr['resType'] == "campaigns"
		res.attr['rel'] == "campaign"
		res.state == State.leaf.name()
	}

	def "should build a folder with leaf state"(){
		given :
			CampaignFolder node = new CampaignFolder(name:"folder")

		when :
			def res = builder.setNode(node).build()

		then :
			res.state == State.leaf.name()

	}

	def "should build a folder with closed state"(){
		given :
			CampaignFolder node = new CampaignFolder(name:"folder")
			node.addContent(new CampaignFolder());

		when :
			def res = builder.setNode(node).build()

		then :
			res.state == State.closed.name()

	}

	def "should expand a folder"(){
		given :
			CampaignFolder node = new CampaignFolder(name:"folder")
			Campaign child = new Campaign(name:"folder child")
			node.addContent(child);

			use(ReflectionCategory) {
				CampaignLibraryNode.set field: "id", of: node, to: 10L
				CampaignLibraryNode.set field: "id", of: child, to: 100L
			}

		and:
		MultiMap expanded = new MultiValueMap()
		expanded.put("CampaignFolder", 10L)

		when :
			def res = builder.expand(expanded).setNode(node).build()

		then :
			res.state == State.open.name()
			res.children.size() == 1

	}

	def "should expand a campaign"(){
		given :
			Campaign node = new Campaign(name:"folder")
			Iteration child = new Iteration(name:"folder child")
			node.addContent(child);

			use(ReflectionCategory) {
				CampaignLibraryNode.set field: "id", of: node, to: 10L
			}

		and:
		MultiMap expanded = new MultiValueMap()
		expanded.put("Campaign", 10L)

		when :
			def res = builder.expand(expanded).setNode(node).build()

		then :
			res.state == State.open.name()
			res.children.size() == 1

	}
}
