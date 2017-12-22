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
package org.squashtest.tm.service.internal.batchimport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.squashtest.tm.core.foundation.lang.PathUtils;
import org.squashtest.tm.domain.library.structures.GraphNode;
import org.squashtest.tm.domain.library.structures.LibraryGraph;


/**
 * <p>Recreates a hierarchy of requirements and requirement folders and informs about their status.
 * 	This graph may have several root nodes, each of them represent a project. That's why it is a specialization of LibraryGraph instead of LibraryTree.
 * For more detailed informations you may get the Node itself, using {@link #getNode(RequirementTarget)}
 * </p>
 *
 * <p>While we're at it, here are the rules regarding the parent of a requirement and particularly deciding whether it is
 * a folder or a requirement. Let call this question "the type of the parent".</p>
 *
 * <p>Considering the path /project/parent/requirement, let be 'parent' the parent entity we need
 * to know the type of :
 * 	<ul>
 * 		<li>There is a RequirementVersionTarget referencing /projet/parent with a TargetStatus != NOT_EXISTS =&gt;
 * 			then parent is indeed a requirement</li>
 * 		<li>There is no such RequirementVersionTarget =&gt; then parent is actually a RequirementFolder</li>
 * 	</ul>
 * </p>
 *
 * <p>It follows that the order in which this tree is populated has consequences over the answer to "the type of the parent".
 * Consider the following Targets :
 * </p>
 *
 * <ol>
 * 	<li>/project/parent/requirement</li>
 * 	<li>/project/parent/requirement/childrequirement</li>
 * </ol>
 *
 * <p>
 *	Both targets are intended to refer to Requirements and let assume this graph doesn't hold such references yet.
 *	Now let's see what happens depending on the insertion order. If the targets are inserted in that order there
 *	are no problem. Target 1 has a parent named "parent", assumed to be a folder, and is inserted as a Requirement.
 *	Then Target 2 is inserted normally. </p>
 *
 *<p>
 *	However a different insertion order yields different results. Target 2 is inserted first.
 *	The model will consider that '/project/parent/requirement' is folder because it is not yet known.
 *	Then Target 1 is proposed for insertion. The model will then reject it because '/project/parent/requirement'
 *	is already known as a folder.
 * </p>
 *
 */
class ImportedRequirementTree extends LibraryGraph<RequirementTarget, ImportedRequirementTree.Node>{


	/**
	 * <p>Will add a a requirement with the given status at the location specified by the target. There are two possible scenarios :</p>
	 *
	 * <h6>No such node was found at the target location :</h6>
	 *
	 * <p>
	 * 	In that case the node is inserted with the corresponding informations. Also, any missing nodes back up to the project node will
	 *  be inserted. This step might be complex because parent nodes may be either folders, or other requirements.
	 *  Consider a new Requirement node inserted at position /project/a/b/c/d/newrequirement, and consider that nodes 'a' and 'b' exists
	 *  in the tree. If 'b' is a requirement, then nodes 'c' and 'd' must be created as requirements. If 'b' is a folder, then 'c' and 'd'
	 *  are created as folders.
	 * </p>
	 *
	 * <p>
	 * 	The requirement node that is inserted here is real. However, the other nodes are
	 * 	created by the algorithm for the sake of completedness, not because of an explicit request. Those nodes are called "virtual" for that
	 * 	reason. This may have an importanc in the second case we discuss below.
	 * </p>
	 *
	 *
	 * <h6>There is already a node at the target location :</h6>
	 *
	 * <p>
	 *  In that case the situation is the following :
	 * </p>
	 *
	 * <ul>
	 * 	<li>the node found is a requirement : we update its TargetStatus with the new one. It is no longer considered virtual if that was the case
	 * 		previously</li>
	 * 	<li>the node found is a virtual folder : my bad, it appears that the supposed virtual folder is actually a real requirement. Type is
	 * changed accordingly and all children nodes are now requirements (no longer folders)</li>
	 * 	<li>the node is folder for real (virtual == false) : throw a {@link IllegalStateException}. As per specification, no Requirement may be
	 * 	created in that place now. (see the class-level javadoc)</li>
	 * </ul>
	 *
	 *
	 * @param requirement
	 * @param status
	 *
	 * @throws IllegalStateException when attempting to add or update a node which happen to be actually a folder (see the class-level javadoc).
	 */
	public void addOrUpdateNode(RequirementTarget requirement, TargetStatus status) {
		Node existingRequirement = getNode(requirement);

		// case 1 : this is a new node
		if (existingRequirement == null){
			addNode(requirement, status);
		}
		//case 2 : there is a node at this location already
		else{
			updateNode(requirement, status);
		}
	}

	public void addOrUpdateNode(RequirementVersionTarget version, TargetStatus status){

		Node requirement = getNode(version.getRequirement());

		if ( requirement == null){
			addOrUpdateNode(version.getRequirement(), new TargetStatus(Existence.TO_BE_CREATED));
			requirement = getNode(version.getRequirement());
		}

		requirement.addVersion(version.getVersion(),status);

	}



	public boolean targetExists(RequirementTarget target){
		Node req = getNode(target);
		return req != null && req.getStatus().getStatus() != Existence.NOT_EXISTS;
	}

	public boolean targetAlreadyLoaded(RequirementTarget target){
		Node req = getNode(target);
		return req != null;
	}

	public boolean targetAlreadyLoaded(RequirementVersionTarget target){
		Node req = getNode(target.getRequirement());
		if (req==null) {
			return false;//If requirement isn't loaded, the requirement version can't be loaded
		}
		return req.versionAlreadyLoaded(target.getVersion());
	}


	public TargetStatus getStatus(RequirementTarget target){
		Node requirement = getNode(target);
		if (requirement != null){
			return requirement.getStatus();
		}
		else{
			return TargetStatus.NOT_EXISTS;
		}
	}


	public TargetStatus getStatus(RequirementVersionTarget target){
		Node requirement = getNode(target.getRequirement());
		return requirement.getVersionStatus(target.getVersion());
	}


	// ***************** sub routines *************************************

	/*
	 * behavior for this is commented in the javadoc on addOrUpdateNode
	 *
	 * How does it work :
	 *
	 * 1 - The requirement is created with the given status. Then, from there we walk up the hierarchy by chunking
	 * the target.
	 *
	 * 2 - Until a parent is found :
	 * 	2.1 : create the node, as a folder. keep track of it, we might need it later
	 * 	2.2 : bind this parent to the latest created node (being the node from step 1, or a previous iteration over 2.1)
	 *
	 * 3 - when a parent is found :
	 * 	3.1 : bind it to the latest created node
	 * 	3.2 : if that parent found happens to be a requirement -> fix all the created node saved in step 2.1 and mark them as requirements.
	 *
	 */
	private void addNode(RequirementTarget target, TargetStatus status){

		// first, create the node at the target location
		Node reqNode = new Node(target, status);
		addNode(reqNode);

		Node lastCreated = reqNode;
		List<Node> createdNodes = new ArrayList<>();

		// get all the parent paths.
		// We need to drop the last entry of the list
		// because it corresponds to the target we just created already
		List<String> allPaths = PathUtils.scanPath(target.getPath());
		allPaths.remove(allPaths.size()-1);


		Collections.reverse(allPaths);

		// now create the folder all the way up if we have to
		for (String middlePath : allPaths){

			RequirementTarget middleTarget = new RequirementTarget(middlePath);

			Node parent;

			// the target exists : that was our last iteration on the loop. Requesting break on completion.
			if (targetExists(middleTarget)){

				parent = getNode(middleTarget);

				// wire the parent with the latest created node
				addEdge(parent, lastCreated);

				// additional test : if the parent is a requirement -> all its children are requirements too
				// this would happen only if a parent is found
				if (parent.isRequirement()){
					for (Node n : createdNodes){
						n.setRequirement(true);
					}
				}

				// now we can exit
				break;
			}

			// job not over yet. Let create a new node for the current target
			else{
				parent = new Node(middleTarget, new TargetStatus(Existence.TO_BE_CREATED), false, true);
				addNode(parent);

				// wire the parent with the latest created node
				addEdge(parent, lastCreated);

				// keeping track of the chain and let go for another loop
				createdNodes.add(parent);
				lastCreated = parent;
			}
		}
	}



	// behavior for this is commented in the javadoc on addOrUpdateNode
	private void updateNode(RequirementTarget target, TargetStatus status){

		Node foundNode = getNode(target);

		// break if the node here is an actual folder. Virtual folders are fine though (read the documentation to know what 'virtual' is about).
		if (foundNode.isFolderAndReal()){
			throw new IllegalStateException("cannot insert requirement '"+target.getPath()+"' because there is a folder there already");
		}

		// else this node is a requirement
		foundNode.updateAsRequirement(target, status);

	}

	/**
	 * Set a RequirementVersionTarget status to not exists
	 * @param target
	 */
	public void setNotExists(RequirementVersionTarget target) {
		Node req = getNode(target.getRequirement());
		if (req!=null) {
			req.setNotExists(target.getVersion());
		}
	}


	public Long getNodeId(RequirementTarget requirement) {
		Node reqNode = getNode(requirement);
		if (reqNode!=null && reqNode.getStatus().getStatus() == Existence.EXISTS) {
			return reqNode.getStatus().getId();
		}
		return null;
	}

	public void bindMilestone(RequirementVersionTarget target, String milestone) {
		Node req = getNode(target.getRequirement());
		req.bindMilestoneToVersion(target.getVersion(), milestone);
	}

	public void bindMilestone(RequirementVersionTarget target,
			List<String> milestones) {
		for (String milestone : milestones) {
			bindMilestone(target, milestone);
		}
	}

	public boolean isMilestoneUsedByOneVersion(RequirementVersionTarget target, String milestone){
		Node req = getNode(target.getRequirement());
		return req.isMilestoneUsedByOneVersion(milestone);
	}

	public boolean isMilestoneLocked(RequirementVersionTarget target, String milestone){
		Node req = getNode(target.getRequirement());
		return req.isVersionMilestoneLocked(target.getVersion(),milestone);
	}

	public void milestoneLock(RequirementVersionTarget target){
		Node req = getNode(target.getRequirement());
		req.setVersionMilestoneLocked(target.getVersion());
	}


	public boolean isRequirementFolder(RequirementVersionTarget target) {
		Node req = getNode(target.getRequirement());
		if (req!=null) {
			return req.isRequirementFolder() && req.getStatus().getStatus()!=Existence.NOT_EXISTS;
		}
		return false;
	}

	// ***************** the class of the node ****************************

	public static class Node extends GraphNode<RequirementTarget, Node>{

		private TargetStatus status;
		private boolean isRequirement = true;
		private boolean virtual = false;
		private SortedMap<Integer,RequirementVersionModel> requirementVersions = new TreeMap<>();
		private Set<String> milestonesInVersion = new HashSet<>();

		public Node(RequirementTarget target, TargetStatus status) {
			super(target);
			this.status = status;
		}


		public void setNotExists(Integer version) {
			requirementVersions.put(version,new RequirementVersionModel(TargetStatus.NOT_EXISTS));
		}

		public Node(RequirementTarget target, TargetStatus status, boolean isRequirement, boolean virtual) {
			super(target);
			this.status = status;
			this.isRequirement = isRequirement;
			this.virtual = virtual;
		}

		boolean isRequirement(){
			return isRequirement;
		}

		public boolean isRequirementFolder() {
			return !isRequirement;
		}

		void setRequirement(boolean isReq){
			isRequirement = isReq;
		}

		void setVirtual(boolean virtual){
			this.virtual = virtual;
		}

		boolean isVirtual(){
			return virtual;
		}

		TargetStatus getStatus(){
			return status;
		}

		// this node represents a folder that is not virtual
		boolean isFolderAndReal(){
			return !(isRequirement || virtual);
		}

		void setStatus(TargetStatus status){
			this.status = status;
		}

		boolean versionAlreadyLoaded(Integer versionNo){
			return requirementVersions.containsKey(versionNo);
		}

		TargetStatus getVersionStatus(Integer versionNo){
			if (versionAlreadyLoaded(versionNo)) {
				return requirementVersions.get(versionNo).getStatus();
			}
			return null;//NPE is better than a false information from tree
		}



		void addVersion(Integer noVersion, TargetStatus status){
			requirementVersions.put(noVersion, new RequirementVersionModel(status));
		}

		boolean isMilestoneUsedByOneVersion(String milestone){
			return milestonesInVersion.contains(milestone);
		}

		boolean isVersionMilestoneLocked(Integer noVersion, String milestone){
			return requirementVersions.get(noVersion).isMilestoneLocked();
		}

		void bindMilestoneToVersion(Integer noVersion, String milestone){
			if (!isMilestoneUsedByOneVersion(milestone)) {
				RequirementVersionModel rvModel = requirementVersions.get(noVersion);
				rvModel.addMilestone(milestone);
				milestonesInVersion.add(milestone);
			}
		}

		public void setVersionMilestoneLocked(Integer noVersion) {
			RequirementVersionModel rvModel = requirementVersions.get(noVersion);
			rvModel.setMilestoneLocked(true);
		}

		void updateAsRequirement(RequirementTarget target, TargetStatus status){
			key = target;	// won't break the underlying hashmap because really their hash are the same
			this.status = status;
			this.virtual = false;
			this.isRequirement = true;

			// also, ensure that children are all requirements too
			for (Node n : outbounds){
				n.updateAsRequirement(n.getKey(), n.getStatus());
			}
		}

	}










}
