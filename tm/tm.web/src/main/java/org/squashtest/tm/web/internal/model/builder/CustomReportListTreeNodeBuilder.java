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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.customreport.CustomReportLibraryNode;
import org.squashtest.tm.domain.tree.TreeLibraryNode;
import org.squashtest.tm.service.internal.dto.json.JsTreeNode;

@Component
public class CustomReportListTreeNodeBuilder {

	@Inject
	@Named("customReport.nodeBuilder")
	private Provider<CustomReportTreeNodeBuilder> builderProvider;

	public CustomReportListTreeNodeBuilder() {
		super();
	}

	/**
	 * Build a list of {@link JsTreeNode} from a list of {@link CustomReportLibraryNode}
	 * @param nodes
	 * @return
	 */
	public List<JsTreeNode> build(List<TreeLibraryNode> nodes){
		List<JsTreeNode> builtNodes =new ArrayList<>();


		for (TreeLibraryNode tln : nodes) {
			CustomReportTreeNodeBuilder builder = builderProvider.get();
			builtNodes.add(builder.build((CustomReportLibraryNode) tln));//NOSONAR cast is safe
		}
		return builtNodes;
	}

	/**
	 * Build a list of {@link JsTreeNode} from a list of {@link CustomReportLibraryNode}. Will also
	 * build children if needed by looking inside openedNodesIds if a builded node is open. il a node is open,
	 * his children must be retrieved and converted in {@link JsTreeNode}.
	 * @param nodes
	 * @param openedNodesIds
	 * @return
	 */
	public List<JsTreeNode> buildWithOpenedNodes(List<TreeLibraryNode> nodes, Set<Long> openedNodesIds){
		List<JsTreeNode> builtNodes =new ArrayList<>();

		for (TreeLibraryNode tln : nodes) {
			CustomReportTreeNodeBuilder builder = builderProvider.get();
			builtNodes.add(builder.buildWithOpenedNodes((CustomReportLibraryNode) tln,openedNodesIds));//NOSONAR cast is safe
		}
		return builtNodes;
	}


}
