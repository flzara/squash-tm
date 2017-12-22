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

import static org.squashtest.tm.service.security.Authorizations.OR_HAS_ROLE_ADMIN;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.Session;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.chart.ChartDefinition;
import org.squashtest.tm.domain.customreport.CustomReportDashboard;
import org.squashtest.tm.domain.customreport.CustomReportFolder;
import org.squashtest.tm.domain.customreport.CustomReportLibrary;
import org.squashtest.tm.domain.customreport.CustomReportLibraryNode;
import org.squashtest.tm.domain.customreport.CustomReportTreeDefinition;
import org.squashtest.tm.domain.report.ReportDefinition;
import org.squashtest.tm.domain.tree.TreeEntity;
import org.squashtest.tm.domain.tree.TreeLibraryNode;
import org.squashtest.tm.exception.DuplicateNameException;
import org.squashtest.tm.service.customreport.CustomReportLibraryNodeService;
import org.squashtest.tm.service.deletion.OperationReport;
import org.squashtest.tm.service.deletion.SuppressionPreviewReport;
import org.squashtest.tm.service.internal.repository.CustomReportLibraryNodeDao;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.security.PermissionsUtils;
import org.squashtest.tm.service.security.SecurityCheckableObject;

@Service("org.squashtest.tm.service.customreport.CustomReportLibraryNodeService")
@Transactional
public class CustomReportLibraryNodeServiceImpl implements
		CustomReportLibraryNodeService {

	@Inject
	protected PermissionEvaluationService permissionService;

	@Inject
	private CustomReportLibraryNodeDao customReportLibraryNodeDao;

	@PersistenceContext
	private EntityManager em;

	@Inject
	private CRLNDeletionHandler deletionHandler;

	@Inject
	private CRLNCopier nodeCopier;

	@Inject
	private CRLNMover nodeMover;

	@Override
	public CustomReportLibraryNode findCustomReportLibraryNodeById (Long id){
		return customReportLibraryNodeDao.findOne(id);
	}

	@Override
	@PreAuthorize("hasPermission(#treeNodeId, 'org.squashtest.tm.domain.customreport.CustomReportLibraryNode' ,'READ') "
			+ OR_HAS_ROLE_ADMIN)
	@Transactional(readOnly = true)
	public CustomReportLibrary findLibraryByTreeNodeId(Long treeNodeId) {
		TreeEntity entity = findEntityAndCheckType(treeNodeId, CustomReportTreeDefinition.LIBRARY);
		return (CustomReportLibrary) entity;//NOSONAR cast is checked by findEntityAndCheckType method
	}

	@Override
	@PostFilter("hasPermission(filterObject, 'READ')" + OR_HAS_ROLE_ADMIN)
	@Transactional(readOnly = true)
	public List<CustomReportLibraryNode> findAllCustomReportLibraryNodeById(List<Long> treeNodeIds) {
		return customReportLibraryNodeDao.findAll(treeNodeIds);
	}


	@Override
	@PreAuthorize("hasPermission(#treeNodeId, 'org.squashtest.tm.domain.customreport.CustomReportLibraryNode' ,'READ') "
			+ OR_HAS_ROLE_ADMIN)
	@Transactional(readOnly = true)
	public CustomReportFolder findFolderByTreeNodeId(Long treeNodeId) {
		TreeEntity entity = findEntityAndCheckType(treeNodeId, CustomReportTreeDefinition.FOLDER);
		return (CustomReportFolder) entity;//NOSONAR cast is checked by findEntityAndCheckType method
	}

	@Override
	@PreAuthorize("hasPermission(#treeNodeId, 'org.squashtest.tm.domain.customreport.CustomReportLibraryNode' ,'READ') "
			+ OR_HAS_ROLE_ADMIN)
	@Transactional(readOnly = true)
	public ChartDefinition findChartDefinitionByNodeId(Long treeNodeId) {
		TreeEntity entity = findEntityAndCheckType(treeNodeId, CustomReportTreeDefinition.CHART);
		return (ChartDefinition) entity;//NOSONAR cast is checked by findEntityAndCheckType method
	}

	@Override
	@PreAuthorize("hasPermission(#treeNodeId, 'org.squashtest.tm.domain.customreport.CustomReportLibraryNode' ,'READ') "
		+ OR_HAS_ROLE_ADMIN)
	@Transactional(readOnly = true)
	public ReportDefinition findReportDefinitionByNodeId(Long treeNodeId) {
		TreeEntity entity = findEntityAndCheckType(treeNodeId, CustomReportTreeDefinition.REPORT);
		return (ReportDefinition) entity;//NOSONAR cast is checked by findEntityAndCheckType method
	}

	@Override
	@PreAuthorize("hasPermission(#treeNodeId, 'org.squashtest.tm.domain.customreport.CustomReportLibraryNode' ,'READ') "
			+ OR_HAS_ROLE_ADMIN)
	@Transactional(readOnly = true)
	public CustomReportDashboard findCustomReportDashboardById(Long treeNodeId) {
		TreeEntity entity = findEntityAndCheckType(treeNodeId, CustomReportTreeDefinition.DASHBOARD);
		return (CustomReportDashboard) entity;//NOSONAR cast is checked by findEntityAndCheckType method
	}



	@Override
	@PreAuthorize("hasPermission(#parentId,'org.squashtest.tm.domain.customreport.CustomReportLibraryNode' ,'WRITE') "
			+ OR_HAS_ROLE_ADMIN)
	public CustomReportLibraryNode createNewNode(Long parentId, TreeEntity entity) {
		CustomReportLibraryNode parentNode = customReportLibraryNodeDao.findOne(parentId);
		if (parentNode == null) {
			throw new IllegalArgumentException("The node designed by parentId doesn't exist, can't add new node");
		}
		CustomReportLibraryNode newNode = new CustomReportLibraryNodeBuilder(parentNode, entity).build();
		customReportLibraryNodeDao.save(newNode);
		em.flush();
		em.clear();//needed to force hibernate to reload the persisted entities...
		return customReportLibraryNodeDao.findOne(newNode.getId());
	}

	@Override
	public List<SuppressionPreviewReport> simulateDeletion(List<Long> nodeIds) {
		return deletionHandler.simulateDeletion(nodeIds);
	}

	@Override
	public OperationReport delete(List<Long> nodeIds) {
		for (Long id : nodeIds) {
			TreeLibraryNode node = customReportLibraryNodeDao.findOne(id);
			checkPermission(new SecurityCheckableObject(node, "DELETE"));
		}
		return deletionHandler.deleteNodes(nodeIds);
	}


	@Override
	@PostFilter("hasPermission(filterObject, 'READ')" + OR_HAS_ROLE_ADMIN)
	public List<CustomReportLibraryNode> findDescendant(List<Long> nodeIds) {
		return customReportLibraryNodeDao.findAllDescendants(nodeIds);
	}

	@Override
	public List<Long> findDescendantIds(List<Long> nodeIds) {
		return customReportLibraryNodeDao.findAllDescendantIds(nodeIds);
	}

	@Override
	@PreAuthorize("hasPermission(#nodeId, 'org.squashtest.tm.domain.customreport.CustomReportLibraryNode' ,'CREATE') "
			+ OR_HAS_ROLE_ADMIN)
	public void renameNode(Long nodeId, String newName)
			throws DuplicateNameException {
		CustomReportLibraryNode crln = customReportLibraryNodeDao.findOne(nodeId);
		crln.renameNode(newName);
	}

	@Override
	public List<Long> findAncestorIds(Long nodeId) {
		return customReportLibraryNodeDao.findAncestorIds(nodeId);
	}

	@Override
	public CustomReportLibraryNode findNodeFromEntity(TreeEntity treeEntity) {
		return customReportLibraryNodeDao.findNodeFromEntity(treeEntity);
	}

	@Override
	@PreAuthorize("hasPermission(#target, 'org.squashtest.tm.domain.customreport.CustomReportLibraryNode' ,'WRITE') "
		+ OR_HAS_ROLE_ADMIN)
	public List<TreeLibraryNode> copyNodes(List<CustomReportLibraryNode> nodes, CustomReportLibraryNode target) {
		return makeCopy(nodes, target);
	}

	@Override
	@PreAuthorize("hasPermission(#targetId, 'org.squashtest.tm.domain.customreport.CustomReportLibraryNode' ,'WRITE') "
		+ OR_HAS_ROLE_ADMIN)
	public List<TreeLibraryNode> copyNodes(List<Long> nodeIds, Long targetId) {
		List<CustomReportLibraryNode> nodes = customReportLibraryNodeDao.findAll(nodeIds);
		CustomReportLibraryNode target = customReportLibraryNodeDao.findOne(targetId);
		return makeCopy(nodes, target);
	}

	@Override
	@PreAuthorize("hasPermission(#targetId, 'org.squashtest.tm.domain.customreport.CustomReportLibraryNode' ,'WRITE') "
		+ OR_HAS_ROLE_ADMIN)
	public void moveNodes(List<Long> nodeIds, Long targetId) {
		List<CustomReportLibraryNode> nodes = customReportLibraryNodeDao.findAll(nodeIds);
		CustomReportLibraryNode target = customReportLibraryNodeDao.findOne(targetId);
		nodeMover.moveNodes(nodes,target);
	}

	//--------------- PRIVATE METHODS --------------

	private TreeEntity findEntityAndCheckType(Long nodeId, CustomReportTreeDefinition entityDef){
		TreeLibraryNode node = findCustomReportLibraryNodeById(nodeId);

		if (node==null||node.getEntityType()!=entityDef) {
			String message = "the node for given id %d doesn't exist or doesn't represent a %s entity";
			throw new IllegalArgumentException(String.format(message, nodeId,entityDef.getTypeName()));
		}

		TreeEntity entity = node.getEntity();

		if (entity==null) {
			String message = "the node for given id %d represent a null entity";
			throw new IllegalArgumentException(String.format(message,nodeId));
		}
		return entity;
	}

	private void checkPermission(SecurityCheckableObject... checkableObjects) {
		PermissionsUtils.checkPermission(permissionService, checkableObjects);
	}



	private List<TreeLibraryNode> makeCopy(List<CustomReportLibraryNode> nodes, CustomReportLibraryNode target) {
		List<TreeLibraryNode> copies = new ArrayList<>();
		copies.addAll(nodeCopier.copyNodes(nodes, target));
		return copies;
	}


}
