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
package org.squashtest.tm.service.internal.repository;

import java.util.List;

import org.squashtest.tm.domain.customreport.CustomReportLibraryNode;
import org.squashtest.tm.domain.tree.TreeEntity;
import org.squashtest.tm.domain.tree.TreeLibraryNode;

public interface CustomCustomReportLibraryNodeDao {
	List<TreeLibraryNode> findChildren(Long parentId);
	List<Long> findAllDescendantIds(List<Long> nodesIds);
	List<CustomReportLibraryNode> findAllDescendants(List<Long> nodesIds);
	List<Long> findAllFirstLevelDescendantIds(Long nodeId);
	List<Long> findAllFirstLevelDescendantIds(List<Long> nodesIds);
	List<Long> findAncestorIds(Long nodeId);
	List<CustomReportLibraryNode> findAllConcreteLibraries();
	List<CustomReportLibraryNode> findAllConcreteLibraries(List<Long> projectIds);
	CustomReportLibraryNode findNodeFromEntity(TreeEntity treeEntity);
	Long countNodeFromEntity(TreeEntity treeEntity);

	List<Long> findAllNodeIdsForLibraryEntity(Long libraryId);
}
