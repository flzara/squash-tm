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
package org.squashtest.tm.service.customreport;

import java.util.List;

import org.squashtest.tm.domain.chart.ChartDefinition;
import org.squashtest.tm.domain.customreport.CustomReportDashboard;
import org.squashtest.tm.domain.customreport.CustomReportFolder;
import org.squashtest.tm.domain.customreport.CustomReportLibrary;
import org.squashtest.tm.domain.customreport.CustomReportLibraryNode;
import org.squashtest.tm.domain.report.ReportDefinition;
import org.squashtest.tm.domain.tree.TreeEntity;
import org.squashtest.tm.domain.tree.TreeLibraryNode;
import org.squashtest.tm.exception.DuplicateNameException;
import org.squashtest.tm.exception.NameAlreadyInUseException;
import org.squashtest.tm.service.deletion.OperationReport;
import org.squashtest.tm.service.deletion.SuppressionPreviewReport;

public interface CustomReportLibraryNodeService {

	/**
	 * Return a {@link CustomReportLibraryNode} given an id
	 * @param treeNodeId
	 * @return
	 */
	CustomReportLibraryNode findCustomReportLibraryNodeById(Long treeNodeId);

	/**
	 * Return a list of {@link CustomReportLibraryNode} given a list of ids
	 * @return
	 */
	List<CustomReportLibraryNode> findAllCustomReportLibraryNodeById(List<Long> treeNodeIds);

	/**
	 * Return a {@link CustomReportLibrary}. The given treeNodeId is the NODE's id, ie the id of the {@link CustomReportLibraryNode}
	 * representing this entity
	 * @param treeNodeId
	 * @return
	 */
	CustomReportLibrary findLibraryByTreeNodeId(Long treeNodeId);

	/**
	 * Return a {@link CustomReportFolder}. The given treeNodeId is the NODE's id, ie the id of the {@link CustomReportLibraryNode}
	 * representing this entity
	 * @param treeNodeId
	 * @return
	 */
	CustomReportFolder findFolderByTreeNodeId(Long treeNodeId);

	/**
	 * Return a {@link ChartDefinition}. The given treeNodeId is the NODE's id, ie the id of the {@link CustomReportLibraryNode}
	 * representing this entity
	 * @param treeNodeId
	 * @return
	 */
	ChartDefinition findChartDefinitionByNodeId(Long treeNodeId);

	/**
	 * Return a {@link ReportDefinition}. The given treeNodeId is the NODE's id, ie the id of the {@link CustomReportLibraryNode}
	 * representing this entity
	 * @param treeNodeId
	 * @return
	 */
	ReportDefinition findReportDefinitionByNodeId(Long treeNodeId);

	/**
	 * Return a {@link CustomReportDashboard}. The given treeNodeId is the NODE's id, ie the id of the {@link CustomReportLibraryNode}
	 * representing this entity
	 * @param treeNodeId
	 * @return
	 */
	CustomReportDashboard findCustomReportDashboardById(Long treeNodeId);

	/**
	 * Service to add a new {@link CustomReportLibraryNode}. The caller is responsible for giving a
	 * a not null, named {@link TreeEntity}. The service will persist the entity, create and persist the node and make links.
	 * <br/>
	 * <br/>
	 * WARNING :
	 * This method clear the hibernate session. The @any mapping in {@link CustomReportLibraryNode}
	 * require a proper persist and reload to have an updated node and entity.
	 *
	 * @param parentId Id of parent node. Can't be null.
	 * @return
	 */
	CustomReportLibraryNode createNewNode(Long parentId, TreeEntity entity) throws NameAlreadyInUseException;

	List<SuppressionPreviewReport> simulateDeletion (List<Long> nodeIds);

	OperationReport delete(List<Long> nodeIds);

	/**
	 * Find all descendant of a given list of node id's. The returned list will include the nodes witch id's are given in arguments
	 * @param nodeIds
	 * @return
	 */
	List<CustomReportLibraryNode> findDescendant(List<Long> nodeIds);

	/**
	 * Find all descendant id's of a given list of node id's. The returned list will include the nodes id's given in arguments
	 * @param nodeIds
	 * @return
	 */
	List<Long> findDescendantIds(List<Long> nodeIds);

	void renameNode(Long nodeId, String newName) throws DuplicateNameException;

	/**
	 * Return the list of all ancestor ids for a given node id
	 * @param nodeId
	 * @return
	 */
	List<Long> findAncestorIds(Long nodeId);

	/**
	 * Find the {@link CustomReportLibraryNode} linked to a {@link TreeEntity}.
	 * @param treeEntity
	 * @return
	 */
	CustomReportLibraryNode findNodeFromEntity(TreeEntity treeEntity);

	List<TreeLibraryNode> copyNodes(List<CustomReportLibraryNode> nodes, CustomReportLibraryNode target);

	List<TreeLibraryNode> copyNodes(List<Long> nodeIds, Long targetId);

	void moveNodes(List<Long> nodeIds, Long targetId);
}
