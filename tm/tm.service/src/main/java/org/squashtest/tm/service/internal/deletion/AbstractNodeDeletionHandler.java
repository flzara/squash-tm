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
package org.squashtest.tm.service.internal.deletion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.squashtest.tm.domain.library.Folder;
import org.squashtest.tm.domain.library.LibraryNode;
import org.squashtest.tm.service.deletion.OperationReport;
import org.squashtest.tm.service.deletion.SuppressionPreviewReport;
import org.squashtest.tm.service.internal.library.NodeDeletionHandler;
import org.squashtest.tm.service.internal.repository.FolderDao;

/**
 *
 * <p> This class is a abstract and generic implementation of NodeDeletionHandler that implements a few basic rules. Regardless of the end domain object (TestCase, Campaign etc) the common
 * rules are :
 * <ul>
 * 	<li>both methods must cover the children of the selected nodes,</li>
 * 	<li>a folder is not removable if at least itself or one of its children is not removable (now the details of what is a non removable children is still to be implemented by subclasses).</li>
 * </ul>
 *
 * 	Basically this class covers the tasks related to the directory structure (subtree coverage and detection of locked folders) by implementing those two features. On the other hand subclasses
 * must determine, by implementing {@link #diagnoseSuppression(List)} and {@link #detectLockedNodes(List)}, which nodes are locked for other reasons. See the documentations of the subclasses
 * for more informations.
 * </p>
 *
 * @author bsiri
 *
 * @param <NODE> a kind of LibraryNode
 * @param <FOLDER> the corresponding Folder
 */

public abstract class AbstractNodeDeletionHandler<NODE extends LibraryNode, FOLDER extends Folder<NODE>>
implements NodeDeletionHandler<NODE, FOLDER>{


	/**
	 * The implemention should return which FolderDao to use depending on the end domain object.
	 *
	 * @return an appropriate FolderDao.
	 */
	protected abstract FolderDao<FOLDER, NODE> getFolderDao();


	/**
	 * {@link NodeDeletionHandler#simulateDeletion(List)}
	 */
	@Override
	public List<SuppressionPreviewReport> simulateDeletion(List<Long> targetIds) {
		List<Long> nodeIds = findNodeHierarchy(targetIds);
		return  diagnoseSuppression(nodeIds);
	}

	/**
	 * {@link NodeDeletionHandler#deleteNodes(List)}
	 */
	@Override
	public OperationReport deleteNodes(List<Long> targetIds) {

		if (! targetIds.isEmpty()){

			// create and resolve the tree of locked files and folder
			LockedFolderInferenceTree tree = createLockedFileInferenceTree(targetIds);

			// now we can collect which node ids are deletable
			List<Long> deletableNodeIds =  tree.collectDeletableIds();

			OperationReport deleteReport = batchDeleteNodes(deletableNodeIds);

			// phase 5 : if milestone mode, also unbind non deleted entities
			if (isMilestoneMode()) {
				List<Long> candidateNodeIds = tree.collectKeys();
				OperationReport unbindReport = batchUnbindFromMilestone(candidateNodeIds);
				deleteReport.mergeWith(unbindReport);
			}

			return deleteReport;
		}
		else{
			return new OperationReport();	//empty operations
		}
	}

	protected abstract boolean isMilestoneMode();


	protected LockedFolderInferenceTree createLockedFileInferenceTree(List<Long> targetIds) {
		//phase 1 : find all the nodes and build the tree
		List<Long[]> hierarchy = findPairedNodeHierarchy(targetIds);

		LockedFolderInferenceTree tree = new LockedFolderInferenceTree();
		tree.build(hierarchy);


		//phase 2 : find the nodes that aren't deletable and mark them as such in the tree
		List<Long> lockedNodeIds = detectLockedNodes(tree.collectKeys());


		//phase 3 : resolve which folders are locked with respect to the locked content.
		tree.markLockedNodes(lockedNodeIds);
		tree.resolveLockedFolders();
		return tree;
	}



	/**
	 * <p>Accepts a list of ids and returns themselves and their children as a list of pairs, each pair being an array of long (node ids) such as [ parent.id, child.id ].
	 * see {@link FolderDao#findPairedContentForList(List)} for details. The nodes input nodes will be paired with null (no parents), and the leaves will be be paired with null (for children).
	 *  </p>
	 *
	 * @param rootNodesIds the ids defining the upper level of the hierarchy.
	 * @return the rootNodeIds and the ids of their children, paired together as described above.
	 */
	/*
	 * TODO : refactor and make profit of the tables [TC,R,C]LN_RELATIONSHIP_CLOSURE]
	 */
	@SuppressWarnings("unchecked")
	protected List<Long[]> findPairedNodeHierarchy(List<Long> rootNodeIds){

		if (rootNodeIds.isEmpty()) {
			return new ArrayList<>();
		}

		List<Long[]> nodeHierarchy = new ArrayList<>();

		for (Long id : rootNodeIds){
			nodeHierarchy.add(new Long[]{null, id});
		}

		//now we loop over the folder structure.
		List<Long> currentLayer = rootNodeIds;

		//let's loop
		while (! currentLayer.isEmpty()){
			List<Long[]> nextPairedLayer = getFolderDao().findPairedContentForList(currentLayer);

			nodeHierarchy.addAll(nextPairedLayer);

			currentLayer = new LinkedList<>(CollectionUtils.collect(nextPairedLayer, new Transformer() {
				@Override
				public Object transform(Object input) {
					return ((Object[])input)[1];
				}
			}));
		}

		return nodeHierarchy;
	}


	/**
	 * <p>Accepts a list of node ids and returns themselves and their children as a flat list.</p>
	 *
	 * @param rootNodeIds rootNodesIds the ids defining the upper level of the hierarchy.
	 * @return the rootNodeIds and the ids of their children.
	 */
	/*
	 * TODO : refactor and make profit of the tables [TC,R,C]LN_RELATIONSHIP_CLOSURE]
	 */
	protected List<Long> findNodeHierarchy(List<Long> rootNodeIds){
		if (rootNodeIds.isEmpty()) {
			return Collections.emptyList();
		}

		List<Long> nodeHierarchy = new ArrayList<>();

		nodeHierarchy.addAll(rootNodeIds);

		//now we loop over the folder structure.
		List<Long> currentLayer = rootNodeIds;

		//let's loop
		while (! currentLayer.isEmpty()){
			List<Long> nextPairedLayer = getFolderDao().findContentForList(currentLayer);

			nodeHierarchy.addAll(nextPairedLayer);

			currentLayer = nextPairedLayer;
		}

		return nodeHierarchy;
	}



	/**
	 * <p>
	 * Given a list of node ids, returns a sublist corresponding to the ids of the nodes which cannot be deleted
	 * according to the specs. The input list includes all the nodes and their children in the directory structure. The
	 * implementation is responsible to fetch any other dependencies needed for the completion of its task. The
	 * implementation is not required to resolve which folders are locked : this abstract class will handle that on the
	 * basis of the returned value.
	 * </p>
	 *
	 * @param nodeIds
	 *            all the node ids.
	 *
	 * @return the sublist of node ids that should NOT be deleted.
	 */
	protected abstract List<Long> detectLockedNodes(List<Long> nodeIds);


	/**
	 * <p>
	 * Given their ids, that method should check the nodes and actually report the informations as specified in
	 * {@link NodeDeletionHandler#simulateDeletion(List)}. See {@link #detectLockedNodes(List)} for details regarding
	 * the input list.
	 * </p>
	 *
	 * @param nodeIds
	 *            the complete list of the nodes involved in that report.
	 *
	 * @return a list of reports summarizing in human readable format what will happen.
	 */
	protected abstract List<SuppressionPreviewReport> diagnoseSuppression(List<Long> nodeIds);


	/**
	 * Will delete the nodes identified by the ids parameter. Those nodes have been identified as legal for deletion and
	 * the implementation should only care of deleting them and unbinding them from the rest of the domain. If performed
	 * in milestone mode, the implementor must spare the elements that should not be removed according to those rules.
	 *
	 * @param ids
	 *            the doomed node ids.
	 *
	 * @return
	 */
	protected abstract OperationReport batchDeleteNodes(List<Long> ids);

	/**
	 * Will unbind the given nodes from the given milestone. The operation report must report such nodes as 'deleted',
	 * in order to notify the tree not to display them anymore.
	 *
	 * @param ids
	 *
	 * @return
	 */
	protected abstract OperationReport batchUnbindFromMilestone(List<Long> ids);


}
