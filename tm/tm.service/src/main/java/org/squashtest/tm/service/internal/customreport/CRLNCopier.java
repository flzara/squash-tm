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
package org.squashtest.tm.service.internal.customreport;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.customreport.CustomReportLibraryNode;
import org.squashtest.tm.domain.customreport.CustomReportTreeDefinition;
import org.squashtest.tm.domain.tree.TreeEntity;
import org.squashtest.tm.domain.tree.TreeLibraryNode;

/**
 * Created by jthebault on 29/02/2016.
 */
@Component
public class CRLNCopier {

	@Inject
	private NameResolver nameResolver;

	public List<CustomReportLibraryNode> copyNodes(List<CustomReportLibraryNode> nodes, CustomReportLibraryNode target){
		List<CustomReportLibraryNode> copiedNodes = new ArrayList();
		for (CustomReportLibraryNode node : nodes) {
			CustomReportLibraryNode copy = createFirstLayerCopy(node, target);
			//resolve naming conflict only for first layer.
			nameResolver.resolveNewName(copy, target);
			target.addChild(copy);
			copiedNodes.add(copy);
		}
		return copiedNodes;
	}

	private CustomReportLibraryNode createFirstLayerCopy(CustomReportLibraryNode node, CustomReportLibraryNode target) {
		CustomReportLibraryNode copy = createBasicCopy(node, target);
		for (TreeLibraryNode child : node.getChildren()) {
			createSubTreeCopy((CustomReportLibraryNode) child,copy);
		}
		return copy;
	}

	private CustomReportLibraryNode createSubTreeCopy(CustomReportLibraryNode node, CustomReportLibraryNode target) {
		CustomReportLibraryNode copy = createBasicCopy(node, target);
		target.addChild(copy);
		for (TreeLibraryNode child : node.getChildren()) {
			createSubTreeCopy((CustomReportLibraryNode) child,copy);
		}
		return copy;
	}

	private CustomReportLibraryNode createBasicCopy(CustomReportLibraryNode node, CustomReportLibraryNode target) {
		CustomReportLibraryNode copy = new CustomReportLibraryNode();
		copy.setLibrary(target.getCustomReportLibrary());
		copy.setName(node.getName());
		copyTreeEntity(node, copy);
		return copy;
	}

	private void copyTreeEntity(CustomReportLibraryNode node, CustomReportLibraryNode copy) {
		TreeEntity treeEntity = node.getEntity().createCopy();
		treeEntity.setProject(copy.getCustomReportLibrary().getProject());
		copy.setEntity(treeEntity);
		copy.setEntityType((CustomReportTreeDefinition) node.getEntityType());
	}

}
