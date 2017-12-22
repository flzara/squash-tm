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
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.squashtest.tm.domain.Identified;
import org.squashtest.tm.domain.campaign.CampaignLibraryNode;
import org.squashtest.tm.domain.library.Library;
import org.squashtest.tm.service.internal.dto.json.JsTreeNode;

/**
 * Decorates an instance of {@link GenericJsTreeNodeBuilder} to build a list of {@link JsTreeNode} from a list of items.
 *
 * @author Gregory Fouquet
 *
 * @param <ITEM>
 */
public class JsTreeNodeListBuilder<ITEM extends Identified> {
	private JsTreeNodeBuilder<? super ITEM, ?> nodeBuilder;
	private MultiMap expansionCandidates;

	private Collection<ITEM> model;

	public JsTreeNodeListBuilder(JsTreeNodeBuilder<? super ITEM, ?> nodeBuilder) {
		super();
		this.nodeBuilder = nodeBuilder;
	}

	public final JsTreeNodeListBuilder<ITEM> setModel(Collection<ITEM> model) {
		this.model = model;
		return this;
	}

	public final List<JsTreeNode> build() {
		List<JsTreeNode> nodes = new ArrayList<>(model.size());

		if (expansionCandidates == null) {
			expansionCandidates = new MultiValueMap();
		}

		int index = 0;
		for (ITEM item : model) {
			JsTreeNode builtNode = nodeBuilder.setIndex(index).expand(expansionCandidates).setModel(item).build();
			if (builtNode != null){
				nodes.add(builtNode);
				index ++;
			}
		}

		return nodes;
	}

	/**
	 * @param expansionCandidates
	 *            the ids of items to expand mapped by their type.
	 * @return
	 */
	public JsTreeNodeListBuilder<ITEM> expand(MultiMap expansionCandidates) {
		this.expansionCandidates = expansionCandidates;
		return this;
	}

	public JsTreeNodeListBuilder<ITEM> setModel(
			List<Library<CampaignLibraryNode>> findAllLibraries) {
		this.model = (Collection<ITEM>) findAllLibraries;
		return this;
	}

}
