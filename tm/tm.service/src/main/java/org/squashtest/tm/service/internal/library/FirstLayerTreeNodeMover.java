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
package org.squashtest.tm.service.internal.library;

import static org.squashtest.tm.domain.EntityType.CAMPAIGN;
import static org.squashtest.tm.domain.EntityType.CAMPAIGN_FOLDER;
import static org.squashtest.tm.domain.EntityType.REQUIREMENT;
import static org.squashtest.tm.domain.EntityType.REQUIREMENT_FOLDER;
import static org.squashtest.tm.domain.EntityType.TEST_CASE;
import static org.squashtest.tm.domain.EntityType.TEST_CASE_FOLDER;

import java.util.*;

import javax.inject.Inject;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.EntityType;
import org.squashtest.tm.domain.campaign.CampaignLibraryNode;
import org.squashtest.tm.domain.library.Library;
import org.squashtest.tm.domain.library.LibraryNode;
import org.squashtest.tm.domain.library.NodeContainer;
import org.squashtest.tm.domain.library.TreeNode;
import org.squashtest.tm.domain.library.WhichNodeVisitor;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.resource.Resource;
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.tm.exception.library.CannotMoveInHimselfException;
import org.squashtest.tm.service.internal.repository.CampaignFolderDao;
import org.squashtest.tm.service.internal.repository.CampaignLibraryDao;
import org.squashtest.tm.service.internal.repository.FolderDao;
import org.squashtest.tm.service.internal.repository.LibraryDao;
import org.squashtest.tm.service.internal.repository.LibraryNodeDao;
import org.squashtest.tm.service.internal.repository.RequirementDao;
import org.squashtest.tm.service.internal.repository.RequirementFolderDao;
import org.squashtest.tm.service.internal.repository.RequirementLibraryDao;
import org.squashtest.tm.service.internal.repository.TestCaseFolderDao;
import org.squashtest.tm.service.internal.repository.TestCaseLibraryDao;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.security.PermissionsUtils;
import org.squashtest.tm.service.security.SecurityCheckableObject;
/**
 * This class is called when moving nodes to another one, it is called only for the first nodes of moved hierarchies.
 * If the move changes project, next layer nodes will be updated (not need to move them)
 * with the {@link NextLayersTreeNodeMover}.
 *
 * @author mpagnon
 *
 */
@Component
@Scope("prototype")
public class FirstLayerTreeNodeMover implements PasteOperation, InitializingBean {
	private static final String READ = "READ";
	private static final String CREATE = "CREATE";

	private static final class NodeCollaborators {
		private final LibraryDao<?,?> libraryDao;
		private final FolderDao<?,?> folderDao;
		private final LibraryNodeDao<?> nodeDao;

		private NodeCollaborators(LibraryDao<?, ?> libraryDao, FolderDao<?, ?> folderDao, LibraryNodeDao<?> nodeDao) {
			super();
			this.libraryDao = libraryDao;
			this.folderDao = folderDao;
			this.nodeDao = nodeDao;
		}
	}

	@Inject
	@Qualifier("squashtest.tm.repository.RequirementLibraryNodeDao")
	private LibraryNodeDao<RequirementLibraryNode<Resource>> requirementLibraryNodeDao;
	@Inject
	@Qualifier("squashtest.tm.repository.TestCaseLibraryNodeDao")
	private LibraryNodeDao<TestCaseLibraryNode> testCaseLibraryNodeDao;
	@Inject
	@Qualifier("squashtest.tm.repository.CampaignLibraryNodeDao")
	private LibraryNodeDao<CampaignLibraryNode> campaignLibraryNodeDao;
	@Inject
	private RequirementFolderDao requirementFolderDao;
	@Inject
	private TestCaseFolderDao testCaseFolderDao;
	@Inject
	private RequirementDao requirementDao;
	@Inject
	private CampaignFolderDao campaignFolderDao;
	@Inject
	private RequirementLibraryDao requirementLibraryDao;
	@Inject
	private TestCaseLibraryDao testCaseLibraryDao;
	@Inject
	private TreeNodeUpdater treeNodeUpdater;
	@Inject
	private CampaignLibraryDao campaignLibraryDao;
	@Inject
	private PermissionEvaluationService permissionEvaluationService;
	private TreeNode movedNode;
	private NodeContainer<? extends TreeNode> destination;
	private boolean projectChanged = false;

	private WhichNodeVisitor whichVisitor = new WhichNodeVisitor();
	private Map<EntityType, NodeCollaborators> collaboratorsByType = new EnumMap<>(EntityType.class);

	//ids of moved test case for batch reindex in end of process
	private List<Long> movedTcIds = new ArrayList<>();

	//ids of moved requirement for batch reindex in end of process
	private List<Long> movedReqVersionIds = new ArrayList<>();

	@Override
	public void afterPropertiesSet() throws Exception {
		init();
	}

	public void init() {
		NodeCollaborators nc = new NodeCollaborators(campaignLibraryDao, campaignFolderDao, campaignLibraryNodeDao);
		collaboratorsByType.put(CAMPAIGN_FOLDER, nc);
		collaboratorsByType.put(CAMPAIGN, nc);

		nc = new NodeCollaborators(requirementLibraryDao, requirementFolderDao, requirementLibraryNodeDao);
		collaboratorsByType.put(REQUIREMENT_FOLDER, nc);
		collaboratorsByType.put(REQUIREMENT, nc);

		nc = new NodeCollaborators(testCaseLibraryDao, testCaseFolderDao, testCaseLibraryNodeDao);
		collaboratorsByType.put(TEST_CASE_FOLDER, nc);
		collaboratorsByType.put(TEST_CASE, nc);

		collaboratorsByType = Collections.unmodifiableMap(collaboratorsByType);
	}


	@Override
	public TreeNode performOperation(TreeNode toMove, NodeContainer<TreeNode> destination, Integer position) {
		//initialize attributes
		this.destination = destination;
		movedNode = null;
		//check destination's hierarchy doesn't contain node to move
		checkNotMovedInHimself(toMove);
		//project changed ?
		Project sourceProject = toMove.getProject();
		GenericProject destinationProject = destination.getProject();
		this.projectChanged = changedProject(sourceProject, destinationProject);

		//process
		processNodes(toMove, position);

		if(projectChanged){
			movedNode.accept(treeNodeUpdater);
		}
		return movedNode;
	}

	@Override
	public boolean isOkToGoDeeper() {
		return this.projectChanged;
	}


	protected void processNodes(TreeNode toMove, Integer position) { //NOSONAR the cyclomatic complexity here is perfectly manageable by a standard instance of homo computernicus
		EntityType visitedType = whichVisitor.getTypeOf(toMove);

		switch (visitedType) {
		case CAMPAIGN_FOLDER:
		case REQUIREMENT_FOLDER:
		case TEST_CASE_FOLDER:
		case CAMPAIGN:
		case TEST_CASE:
			NodeCollaborators nc = collaboratorsByType.get(visitedType);
			visitLibraryNode((LibraryNode) toMove, nc.libraryDao, nc.folderDao, position);
			movedTcIds.add(toMove.getId());
			break;
		case REQUIREMENT: // special
			visitWhenNodeIsRequirement((Requirement) toMove, position);
			List<Long> reqVersionIds = new ArrayList<>();
			List<RequirementVersion> requirementVersions =((Requirement) toMove).getRequirementVersions();
			for (RequirementVersion requirementVersion : requirementVersions) {
				reqVersionIds.add(requirementVersion.getId());
			}
			movedReqVersionIds.addAll(reqVersionIds);
			break;
		case ITERATION:
		case TEST_SUITE:
			break;
		default:
			throw new IllegalArgumentException("Libraries cannot be copied nor moved !");
		}


	}


	@SuppressWarnings("unchecked")
	private <LN extends LibraryNode> void visitLibraryNode(LN node, LibraryDao<?,?> libraryDao,
			FolderDao<?,?> folderDao, Integer position) {

		NodeContainer<LN> parent = findFolderOrLibraryParent(node, libraryDao, folderDao);

		PermissionsUtils.checkPermission(permissionEvaluationService, new SecurityCheckableObject(destination, CREATE), new SecurityCheckableObject(
				parent, "DELETE"), new SecurityCheckableObject(node, READ));

		node.notifyAssociatedWithProject((Project)destination.getProject());
		moveNode(node, (NodeContainer<LN>) destination, parent, position);
	}



	@SuppressWarnings("unchecked")
	private  <LN extends LibraryNode>  void visitWhenNodeIsRequirement(Requirement node, Integer position) {

		NodeContainer<Requirement> parent = findFolderOrLibraryParent(node, requirementLibraryDao, requirementFolderDao);
		if (parent == null){
			parent = requirementDao.findByContent(node);
		}

		PermissionsUtils.checkPermission(permissionEvaluationService, new SecurityCheckableObject(destination, CREATE), new SecurityCheckableObject(
				parent, "DELETE"), new SecurityCheckableObject(node, READ));

		node.notifyAssociatedWithProject((Project)destination.getProject());
		moveNode((LN)node, (NodeContainer<LN>) destination,(NodeContainer<LN>) parent, position);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <LN extends LibraryNode> NodeContainer<LN> findFolderOrLibraryParent(LN node, LibraryDao libraryDao,
			FolderDao folderDao) {
		Library<? extends LibraryNode> parentLib = libraryDao.findByRootContent(node);
		return parentLib != null ? (NodeContainer<LN>) parentLib : folderDao.findByContent(node);
	}



	private <TN extends TreeNode> void moveNode(TN toMove, NodeContainer<TN> destination, NodeContainer<TN> toMoveParent, Integer position) {
		requirementDao.flush();
		toMoveParent.removeContent(toMove);
		requirementDao.flush();
		if (position != null){
			destination.addContent(toMove, position);
		}
		else{
			destination.addContent(toMove);
		}
		movedNode = toMove;
	}

	/**
	 * Checks if node1's project is the same as node2's.
	 *
	 * @param sourceProject , the project of the source node
	 * @param destinationProject , the project of the destination
	 * @return true if the source and destination projects are the same.
	 *
	 */
	private boolean changedProject(Project sourceProject , GenericProject destinationProject) {
		return sourceProject != null && destinationProject != null && !sourceProject.getId().equals(destinationProject.getId());
	}

	/**
	 * Will check if the treeNode to move is not his destination and if it is not contained in the hierarchy of it's destination.
	 * @param toMove
	 * @return
	 */
	private void checkNotMovedInHimself(TreeNode toMove) {

		Long toMoveId = ((LibraryNode) toMove).getId();
		Long destinationId = destination.getId();

		if (toMove.equals(destination)) {
			throw new CannotMoveInHimselfException();
		}

		LibraryNodeDao<?> lnDao;
		EntityType destType = whichVisitor.getTypeOf(destination);

		switch (destType) {
		case CAMPAIGN_FOLDER:
		case REQUIREMENT_FOLDER:
		case TEST_CASE_FOLDER:
		case CAMPAIGN:
		case REQUIREMENT:
			lnDao = collaboratorsByType.get(destType).nodeDao;
			break;

		default:
			return; // the other cases cannot pose problems
		}

		// let's check to problematic cases

		List<Long> hierarchyIds = lnDao.getParentsIds(destinationId);
		for (Long id : hierarchyIds) {
			if (id.equals(toMoveId)) {
				throw new CannotMoveInHimselfException();
			}
		}

	}

	@Override
	public List<Long> getRequirementVersionToIndex() {
		return movedReqVersionIds;
	}

	@Override
	public List<Long> getTestCaseToIndex() {
		return movedTcIds;
	}

}
