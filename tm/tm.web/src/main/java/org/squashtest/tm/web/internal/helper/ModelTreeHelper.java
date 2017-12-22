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
package org.squashtest.tm.web.internal.helper;

import java.util.List;
import javax.inject.Provider;

import org.apache.commons.collections.MultiMap;
import org.squashtest.tm.domain.library.Library;
import org.squashtest.tm.domain.library.LibraryNode;
import org.squashtest.tm.service.library.WorkspaceService;
import org.squashtest.tm.web.internal.model.builder.DriveNodeBuilder;
import org.squashtest.tm.web.internal.model.builder.JsTreeNodeListBuilder;
import org.squashtest.tm.service.internal.dto.json.JsTreeNode;

/**
 * Helper class to manipulate jsTree related data.
 *
 * @author Gregory Fouquet
 *
 */
public abstract class ModelTreeHelper {

	/**
	 *
	 */
	private ModelTreeHelper() {
		super();
	}

	/**
	 * Coerces an array of dom nodes ids (["#TestCase-10", "#TestCaseLibrary-20"]) into a map. The result maps entities
	 * ids by their short class name ([TestCase: [10], TestCaseLibrary: [20]]).
	 *
	 * @param domNodesIds
	 * @return
	 */
	public List<JsTreeNode> getRootModel(String[] openedNodes, String elementId) {
		List<Library<LibraryNode>> libraries = getWorkspaceService().findAllLibraries();
		String[] nodesToOpen;

		if (elementId == null || elementId.isEmpty()) {
			nodesToOpen = openedNodes;
		} else {
			Long id = Long.valueOf(elementId);
			nodesToOpen = getNodeParentsInWorkspace(id);
		}

		MultiMap expansionCandidates = mapIdsByType(nodesToOpen);

		DriveNodeBuilder<LibraryNode> nodeBuilder = driveNodeBuilderProvider().get();
		List<JsTreeNode> rootNodes = new JsTreeNodeListBuilder<>(nodeBuilder)
				.expand(expansionCandidates)
				.setModel(libraries).build();
		return rootNodes;
	}

	/**
	 * Should return a workspace service.
	 *
	 * @return
	 */
	protected abstract WorkspaceService<Library<LibraryNode>> getWorkspaceService();

	/**
	 * Returns the list of parents of a node given the id of an element
	 *
	 * @param elementId
	 * @return
	 */
	protected abstract String[] getNodeParentsInWorkspace(Long elementId);

	/**
	 * @param openedNodes
	 * @return
	 */
	protected MultiMap mapIdsByType(String[] openedNodes) {
		return JsTreeHelper.mapIdsByType(openedNodes);
	}

	/**
	 * Returns the appropriate drive node builder. Should never return null.
	 *
	 * @return
	 */
	protected abstract Provider<DriveNodeBuilder<LibraryNode>> driveNodeBuilderProvider();

}
