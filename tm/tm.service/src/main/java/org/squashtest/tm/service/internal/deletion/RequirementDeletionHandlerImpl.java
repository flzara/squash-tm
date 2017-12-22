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
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.squashtest.tm.core.foundation.exception.ActionException;
import org.squashtest.tm.domain.EntityType;
import org.squashtest.tm.domain.Sizes;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.library.LibraryNode;
import org.squashtest.tm.domain.library.NodeContainer;
import org.squashtest.tm.domain.library.WhichNodeVisitor;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementFolder;
import org.squashtest.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.tm.service.deletion.BoundToLockedMilestonesReport;
import org.squashtest.tm.service.deletion.BoundToMultipleMilestonesReport;
import org.squashtest.tm.service.deletion.MilestoneModeNoFolderDeletion;
import org.squashtest.tm.service.deletion.Node;
import org.squashtest.tm.service.deletion.NodeMovement;
import org.squashtest.tm.service.deletion.OperationReport;
import org.squashtest.tm.service.deletion.SingleOrMultipleMilestonesReport;
import org.squashtest.tm.service.deletion.SuppressionPreviewReport;
import org.squashtest.tm.service.internal.customfield.PrivateCustomFieldValueService;
import org.squashtest.tm.service.internal.deletion.SubRequirementRewiringTree.Movement;
import org.squashtest.tm.service.internal.library.LibraryUtils;
import org.squashtest.tm.service.internal.repository.FolderDao;
import org.squashtest.tm.service.internal.repository.LibraryNodeDao;
import org.squashtest.tm.service.internal.repository.RequirementDao;
import org.squashtest.tm.service.internal.repository.RequirementDeletionDao;
import org.squashtest.tm.service.internal.repository.RequirementFolderDao;
import org.squashtest.tm.service.internal.requirement.RequirementNodeDeletionHandler;
import org.squashtest.tm.service.milestone.ActiveMilestoneHolder;

@Component("squashtest.tm.service.deletion.RequirementNodeDeletionHandler")
public class RequirementDeletionHandlerImpl extends
AbstractNodeDeletionHandler<RequirementLibraryNode, RequirementFolder> implements
RequirementNodeDeletionHandler {

	private static final String REQUIREMENTS_TYPE = "requirements";

	@Inject
	private RequirementFolderDao folderDao;

	@Inject
	private Provider<TestCaseImportanceManagerForRequirementDeletion> provider;

	@Inject
	private RequirementDao requirementDao;

	@Inject
	@Qualifier("squashtest.tm.repository.RequirementLibraryNodeDao")
	private LibraryNodeDao<RequirementLibraryNode> libraryNodeDao;

	@Inject
	private RequirementDeletionDao deletionDao;

	@Inject
	private PrivateCustomFieldValueService customValueService;

	@Inject
	private ActiveMilestoneHolder activeMilestoneHolder;

	@Override
	protected FolderDao<RequirementFolder, RequirementLibraryNode> getFolderDao() {
		return folderDao;
	}

	@PersistenceContext
	private EntityManager em;

	@Override
	protected List<SuppressionPreviewReport> diagnoseSuppression(List<Long> nodeIds) {

		List<SuppressionPreviewReport> preview = new LinkedList<>();

		// normal mode
		// milestone mode
		if (isMilestoneMode()) {
			// separate the folders from the requirements
			List<Long>[] separatedIds = deletionDao.separateFolderFromRequirementIds(nodeIds);
			List<Long> targetVersionIds = deletionDao.findVersionIdsForMilestone(separatedIds[1],
					getActiveMilestoneId());

			// check if there are some folders in the selection
			reportNoFoldersAllowed(separatedIds[0], preview);


			// check if some elements belong to milestones which status forbids that
			reportVersionLocksByMilestone(targetVersionIds, preview);

			// RG 3613-R2.05
			// check if some versions are bound to multiple milestones
			reportMultipleMilestoneBinding(targetVersionIds, preview);

		}

		// normal mode
		else {
			reportLocksByMilestone(nodeIds, preview);

		}

		return preview;
	}


	protected void reportMultipleMilestoneBinding(List<Long> targetVersionIds, List<SuppressionPreviewReport> preview) {
		List<Long> boundNodes = deletionDao.filterVersionIdsHavingMultipleMilestones(targetVersionIds);
		if (! boundNodes.isEmpty()){
			//case 1 : all the requirement versions belong to multiple milestones
			if (boundNodes.size() == targetVersionIds.size()){
				preview.add(new BoundToMultipleMilestonesReport(REQUIREMENTS_TYPE));
			}
			//case 2 : there is a mix of versions that must be unbound from the milestone or deleted outright
			else{
				preview.add(new SingleOrMultipleMilestonesReport(REQUIREMENTS_TYPE));
			}
		}
	}


	protected void reportVersionLocksByMilestone(List<Long> versionIds, List<SuppressionPreviewReport> preview) {
		List<Long> lockedNodes = deletionDao.filterVersionIdsWhichMilestonesForbidsDeletion(versionIds);
		if (! lockedNodes.isEmpty()){
			preview.add(new BoundToLockedMilestonesReport(REQUIREMENTS_TYPE));
		}
	}


	protected void reportNoFoldersAllowed(List<Long> folderIds, List<SuppressionPreviewReport> preview) {
		if (! folderIds.isEmpty()){
			preview.add(new MilestoneModeNoFolderDeletion(REQUIREMENTS_TYPE));
		}
	}


	protected void reportLocksByMilestone(List<Long> nodeIds, List<SuppressionPreviewReport> preview) {
		List<Long> lockedNodes = deletionDao.filterRequirementsIdsWhichMilestonesForbidsDeletion(nodeIds);
		if (! lockedNodes.isEmpty()){
			preview.add(new BoundToLockedMilestonesReport(REQUIREMENTS_TYPE));
		}
	}


	/*
	 * The milestone mode for requirement is a bit different from the other entities.
	 * It applies on the requirement versions, instead of the requirement themselves.
	 *
	 * However the abstract superclass expects to know which requirements cannot be deleted.
	 *
	 * So, we must return requirement ids, no version ids.
	 *
	 * Here is how we compute this : at the end of the day, a requirement will be deleted
	 * only if it has only one version and that version will be deleted because it
	 * belong to the given milestone only.
	 *
	 */
	@Override
	protected List<Long> detectLockedNodes(List<Long> nodeIds) {

		List<Long> lockedIds = new ArrayList<>();

		// milestone mode
		if (isMilestoneMode()) {
			List<Long>[] separateIds = deletionDao.separateFolderFromRequirementIds(nodeIds);

			// a) no folder shall be deleted
			List<Long> folderIds = separateIds[0];
			lockedIds.addAll(folderIds);

			/*
			 * b) a requirement that can be deleted must :
			 * 	1 - have a deletable version AND
			 *  2 - have only one version
			 *
			 *  Thus, non deletable requirements are the set of candidates minus requirement that can be
			 *  deleted.
			 */

			List<Long> requirementIds = separateIds[1];

			// 1 - have deletable version
			List<Long> deletableRequirements = deletionDao.filterRequirementsHavingDeletableVersions(requirementIds,
					getActiveMilestoneId());

			// 2 - have only one version
			List<Long> reqHavingManyVersions = requirementDao.filterRequirementHavingManyVersions(deletableRequirements);
			deletableRequirements.removeAll(reqHavingManyVersions);

			// 3 - finally : non deletable requirements are all the others
			requirementIds.removeAll(deletableRequirements);

			lockedIds.addAll(requirementIds);
		}

		// referential mode. Nodes locked by milestone are always locked
		else {

			List<Long> lockedRequirementIds = deletionDao.filterRequirementsIdsWhichMilestonesForbidsDeletion(nodeIds);
			lockedIds.addAll(lockedRequirementIds);

		}

		return lockedIds;
	}

	/**
	 * <p>
	 * 	Because nowaday deleting requirements is highly toxic for brain cells here is a method that will help out with
	 * 	deciding if a node should :
	 * </p>
	 *
	 * <ol>
	 * 	<li>be deleted as a folder (which is simpler)</li>
	 * 	<li>be deleted totally as a requirement (with all its versions). Note : a requirement that wont be deleted is said to be <strong>locked</strong></li>
	 * 	<li>rebind its subrequirements to its parent (usually because it has to be deleted)</li>
	 * 	<li>delete only a version which happen to belong to a given milestone</li>
	 * 	<li>unbind only a version from a given milestone</li>
	 * </ol>
	 *
	 * <p>
	 * 	The generic name for those different situations is <strong>contextual-deletion</strong>.
	 * 	Nodes that fall under one of those situations are thus referred to as
	 *  <strong>contextually-deleted</strong>.
	 * </p>
	 *
	 * <p>Another concern now : the fate of the selected nodes depend on what the user specifically picked.
	 * For each node picked by the user :
	 * 	<ul>
	 * 		<p><strong>rule D1</strong> : if it is a folder : <strong>contextual-delete</strong> the whole subtree </p>
	 * 		<p><strong>rule D2</strong> : if it is a requirement : <strong>contextual-delete</strong> that requirement alone</p>
	 * </ul>
	 * </p>
	 *
	 * <p>Then, we can safely proceed with peace in mind knowing which node requires which treatment.</p>
	 *
	 *
	 * (non-Javadoc)
	 *
	 * @see org.squashtest.tm.service.internal.deletion.AbstractNodeDeletionHandler#deleteNodes(java.util.List)
	 */
	protected TargetsSortedByAppropriatePunishment sortThatMess(List<Long> nodeIds) {

		List<Long> deletableFolderIds;
		List<Long> deletableRequirementIds;
		List<Long> requirementWithRewirableChildren;
		List<Long> requirementsWithOneDeletableVersion = null;
		List<Long> requirementsWithOneUnbindableVersion = null;

		List<Long>[] candidateIds = deletionDao.separateFolderFromRequirementIds(nodeIds);
		List<Long> candidateFolders = candidateIds[0];				// root nodes for rule D1 resolution
		List<Long> candidateRequirementIds = candidateIds[1];		// root nodes for rule D2 resolution

		// --------- find nodes deletable per rule D1 -------------

		LockedFolderInferenceTree folderTree = createLockedFileInferenceTree(candidateFolders);
		List<Long> treeNodeIds = folderTree.collectKeys();			// these are the whole node hierarchy

		// detect deletable folders. The tree can tell us that.
		List<Long> deletableNodeIds = folderTree.collectDeletableIds();
		deletableFolderIds = deletionDao.separateFolderFromRequirementIds(deletableNodeIds)[0];

		/*
		 * detect the locked requirements. Now, in this case the tree is not applicable
		 * because of subrequirements. Consider a requirement r1 and its subrequirement r2.
		 * The tree would wrongly considers that since r2 is locked (non deletable) then r1 should be locked too.
		 * However according to our spec this is not the case : r1 can still be deleted eventough r2 is deletable.
		 * In this case r2 will be attached to the parent of r1, then r1 will be deleted.
		 *
		 * So we need just recompute the whole thing : find which requirements are actually
		 * deletable on an individual basis.
		 */

		List<Long> rule1DeletableRequirementIds = deletionDao.separateFolderFromRequirementIds(treeNodeIds)[1];
		List<Long> lockedTreeRequirementIds = detectLockedNodes(rule1DeletableRequirementIds);
		rule1DeletableRequirementIds.removeAll(lockedTreeRequirementIds);

		deletableRequirementIds = new ArrayList<>(rule1DeletableRequirementIds);
		requirementWithRewirableChildren = new ArrayList<>(rule1DeletableRequirementIds);

		// ------- find deletable nodes per rule D2 ---------

		List<Long> lockedCandidateIds = detectLockedNodes(candidateRequirementIds);

		List<Long> rule2DeletableRequirementIds = new ArrayList<>(candidateRequirementIds);
		rule2DeletableRequirementIds.removeAll(lockedCandidateIds);

		deletableRequirementIds.addAll(rule2DeletableRequirementIds);
		requirementWithRewirableChildren.addAll(rule2DeletableRequirementIds);

		// ------- extra operations for milestone mode -------

		/*
		 * find the nodes which need special actions on
		 * their versions in milestone mode.
		 *
		 * Those, if applied, are performed on the requirements
		 * encompassed by the selection minus those that
		 * must be deleted
		 */
		if (isMilestoneMode()) {
			List<Long> allRequirementsEncompassed = deletionDao.separateFolderFromRequirementIds(folderTree.collectKeys())[1];
			allRequirementsEncompassed.removeAll(deletableRequirementIds);
			allRequirementsEncompassed.addAll(lockedCandidateIds);

			requirementsWithOneDeletableVersion = deletionDao
					.filterRequirementsHavingDeletableVersions(allRequirementsEncompassed, getActiveMilestoneId());
			requirementsWithOneUnbindableVersion = deletionDao
					.filterRequirementsHavingUnbindableVersions(allRequirementsEncompassed, getActiveMilestoneId());
		}

		// -------- now fill our object ---------

		TargetsSortedByAppropriatePunishment sortedTargets = new TargetsSortedByAppropriatePunishment();

		sortedTargets.setDeletableFolderIds(deletableFolderIds);
		sortedTargets.setDeletableRequirementIds(deletableRequirementIds);
		sortedTargets.setRequirementsWithRewirableChildren(requirementWithRewirableChildren);
		sortedTargets.setRequirementsWithOneDeletableVersion(requirementsWithOneDeletableVersion);
		sortedTargets.setRequirementsWithOneUnbindableVersion(requirementsWithOneUnbindableVersion);

		return sortedTargets;

	}



	/**
	 *
	 * <p>The following method is overridden from the abstract class because the business rule is special :
	 * for each node selected by the user :
	 * 	<ul>
	 * 		<li>a/ if it is a folder : proceed as usual,</li>
	 * 		<li>b/ if it is a requirement : delete it and bind its children to its parent.</li>
	 * </ul>
	 * </p>
	 *
	 * <p>Another concern is the milestone mode. When regular entities such as test cases are deleted,
	 * they are either deleted, either spared because of business or sec rules. For requirements the
	 * problem is a bit more complex because of their versions. A version can be deleted, and also
	 * if that deleted version was the only one in a requirement then only that requirement can
	 * be deleted.</p>
	 *
	 *
	 * <p>All of this is is handled for a good part by the logic in {@link #sortThatMess(List, Long)}</p>
	 *
	 * (non-Javadoc)
	 *
	 * @see org.squashtest.tm.service.internal.deletion.AbstractNodeDeletionHandler#deleteNodes(java.util.List)
	 */
	@Override
	public OperationReport deleteNodes(List<Long> targetIds) {

		OperationReport globalReport = new OperationReport();

		TargetsSortedByAppropriatePunishment sortedTargets = sortThatMess(targetIds);

		// rewire future orphan requirements
		List<Long> childrenRewirableRequirements = sortedTargets.getRequirementsWithRewirableChildren();
		OperationReport rewiredRequirementsReport = rewireChildrenRequirements(childrenRewirableRequirements);
		globalReport.mergeWith(rewiredRequirementsReport);


		// delete requirements
		List<Long> deletableRequirements = sortedTargets.getDeletableRequirementIds();
		OperationReport deletedRequirementsReport = batchDeleteRequirement(deletableRequirements);
		globalReport.mergeWith(deletedRequirementsReport);


		// delete folders
		List<Long> deletableFolderIds = sortedTargets.getDeletableFolderIds();
		OperationReport deletedFoldersReport = batchDeleteFolders(deletableFolderIds);
		globalReport.mergeWith(deletedFoldersReport);


		// milestone mode :
		if (isMilestoneMode()) {

			// delete just a version
			List<Long> requirementWithDeletableVersion = sortedTargets.getRequirementsWithOneDeletableVersion();
			OperationReport removedVersionsReport = batchRemoveMilestoneVersion(requirementWithDeletableVersion,
					getActiveMilestoneId());
			globalReport.mergeWith(removedVersionsReport);

			// unbind just a version
			List<Long> requirementWithUnbindableVersion = sortedTargets.getRequirementsWithOneUnbindableVersion();
			OperationReport unboundVerionsReport = batchUnbindFromMilestone(requirementWithUnbindableVersion);
			globalReport.mergeWith(unboundVerionsReport);


		}

		return globalReport;
	}



	// ****************************** atrocious boilerplate here ************************


	protected OperationReport batchDeleteFolders(List<Long> folderIds){

		OperationReport report = new OperationReport();

		if (!folderIds.isEmpty()) {
			deletionDao.removeEntities(folderIds);
			report.addRemoved(folderIds, "folder");

			deletionDao.flush();
		}

		return report;
	}

	protected OperationReport batchDeleteRequirement(List<Long> ids){

		OperationReport report = new OperationReport();

		if (! ids.isEmpty()) {

			// prepare the recomputation of test case automatic importances
			TestCaseImportanceManagerForRequirementDeletion testCaseImportanceManager = provider.get();
			testCaseImportanceManager.prepareRequirementDeletion(ids);

			// now let's remove the requirement versions
			// don't forget to first remove the reference a requirement
			// has to the current version (see Requirement#resource)
			deletionDao.unsetRequirementCurrentVersion(ids);
			List<Long> allVersionIds = deletionDao.findVersionIds(ids);
			batchDeleteVersions(allVersionIds);

			// remove the requirement audit event
			deletionDao.deleteRequirementAuditEvents(ids);

			// remove the requirements now
			deletionDao.removeEntities(ids);

			// notify the test cases
			testCaseImportanceManager.changeImportanceAfterRequirementDeletion();

			// fill the report
			report.addRemoved(ids, "requirement");

			deletionDao.flush();

		}

		return report;
	}


	protected OperationReport batchRemoveMilestoneVersion(List<Long> requirementIds, Long milestoneId){
		OperationReport report = new OperationReport();

		if (! requirementIds.isEmpty()){

			// prepare the recomputation of test case automatic importances
			TestCaseImportanceManagerForRequirementDeletion testCaseImportanceManager = provider.get();
			testCaseImportanceManager.prepareRequirementDeletion(requirementIds);

			// now let's remove the requirement versions
			// don't forget to first remove the reference a requirement
			// has to the current version (see Requirement#resource)
			deletionDao.unsetRequirementCurrentVersion(requirementIds);
			List<Long> versionIds = deletionDao.findDeletableVersions(requirementIds, milestoneId);
			batchDeleteVersions(versionIds);

			// now reset the latest version of those requirements
			deletionDao.resetRequirementCurrentVersion(requirementIds);

			// notify the test cases
			testCaseImportanceManager.changeImportanceAfterRequirementDeletion();

			report.addRemoved(requirementIds, "requirement");

			deletionDao.flush();

		}

		return report;

	}

	@Override
	protected OperationReport batchUnbindFromMilestone(List<Long> requirementIds) {
		OperationReport report = new OperationReport();

		if (! requirementIds.isEmpty()){

			List<Long> versionIds = deletionDao.findUnbindableVersions(requirementIds, getActiveMilestoneId());
			List<Long> unbindableRequirements = requirementDao.findByRequirementVersion(versionIds);

			deletionDao.unbindFromMilestone(unbindableRequirements, getActiveMilestoneId());

			report.addRemoved(requirementIds, "requirement");

			deletionDao.flush();
		}

		return report;
	}


	private OperationReport batchDeleteVersions(List<Long> versionIds){
		OperationReport report = new OperationReport();

		if (! versionIds.isEmpty()) {

			customValueService.deleteAllCustomFieldValues(BindableEntity.REQUIREMENT_VERSION, versionIds);

			// save the attachment list ids for later reference. We cannot rely on the cascade here
			// because the requirement deletion is made by HQL, which doesn't honor the cascades
			List<Long> versionsAttachmentIds = deletionDao.findRequirementVersionAttachmentListIds(versionIds);

			// remove the changelog
			deletionDao.deleteRequirementVersionAuditEvents(versionIds);

			// remove binds to other entities
			deletionDao.removeTestStepsCoverageByRequirementVersionIds(versionIds);
			deletionDao.removeFromVerifiedVersionsLists(versionIds);
			deletionDao.removeFromLinkedVersionsLists(versionIds);

			// remove the elements now
			deletionDao.deleteVersions(versionIds);
			deletionDao.removeAttachmentsLists(versionsAttachmentIds);

			deletionDao.flush();

		}

		return report;
	}


	private OperationReport rewireChildrenRequirements(List<Long> requirements){

		OperationReport report = new OperationReport();

		// first : find which node must move where
		List<Long[]> treeData = findPairedNodeHierarchy(requirements);

		SubRequirementRewiringTree rewirer = new SubRequirementRewiringTree();
		rewirer.build(treeData);
		rewirer.markDeletableNodes(requirements);

		rewirer.resolveMovements();

		Collection<Movement> movements = rewirer.getNodeMovements();

		// second : clear each deleted nodes from their content
		List<Requirement> deletedRequirements = requirementDao.findAllByIds(requirements);
		for (Requirement r : deletedRequirements){
			r.getContent().clear();
		}

		// third : perform the rewiring

		for (Movement mouv : movements){

			Long newParentId = mouv.getId();
			boolean isknown = ! mouv.isTheParentOf();

			NodeContainer<Requirement> newParent;

			if (isknown){
				newParent = (NodeContainer<Requirement>)libraryNodeDao.findById(newParentId);	// the cast is quite brutal indeed
			}
			else{
				List<Object[]> allParents = requirementDao.findAllParentsOf(Arrays.asList(newParentId));
				newParent = (NodeContainer<Requirement>)allParents.get(0)[0]; 					// quite brutal too
			}

			List<Requirement> rewiredRequirements = requirementDao.findAllByIds(mouv.getNewChildren());

			renameContentIfNeededThenAttach(newParent, rewiredRequirements, report);

		}

		return report;

	}

	private void renameContentIfNeededThenAttach(NodeContainer<Requirement> newParent, Collection<Requirement> rewired, OperationReport report){
		// abort if no operation is necessary
		if (rewired.isEmpty()) {
			return;
		}

		// init
		Collection<Requirement> children = new ArrayList<>(rewired);
		List<Node> movedNodesLog = new ArrayList<>(rewired.size());

		boolean needsRenaming;

		// renaming loop. Loop over each children, and for each of them ensure that they wont namecrash within their new
		// parent.
		// Log all these operations in the report object.
		for (Requirement child : children) {

			needsRenaming = false;
			String name = child.getName();

			while (!newParent.isContentNameAvailable(name)) {
				name = LibraryUtils.generateNonClashingName(name, newParent.getContentNames(), Sizes.NAME_MAX);
				needsRenaming = true;
			}

			// log the renaming operation if happened.
			if (needsRenaming) {
				child.setName(name);
				report.addRenamed("requirement", child.getId(), name);
			}

			// log the movement operation.
			movedNodesLog.add(new Node(child.getId(), "requirement"));

		}

		// attach the children to their new parent.
		// TODO : perhaps use the navigation service facilities instead? For now I believe it's fine enough.
		for (Requirement child : children) {
			newParent.addContent(child);
		}

		// fill the report
		EntityType type = new WhichNodeVisitor().getTypeOf(newParent);
		String strtype;
		switch (type) {
		case REQUIREMENT_LIBRARY:
			strtype = "drive";
			break;
		case REQUIREMENT_FOLDER:
			strtype = "folder";
			break;
		default:
			strtype = "requirement";
			break;
		}

		NodeMovement nodeMovement = new NodeMovement(new Node(newParent.getId(), strtype), movedNodesLog);
		report.addMoved(nodeMovement);
	}



	// *********************** inner classes *****************************************

	private static final class ImpossibleSuppression extends ActionException{

		/**
		 *
		 */
		private static final long serialVersionUID = 4901610054565947807L;
		private static final String impossibleSuppressionException = "squashtm.action.exception.impossiblerequirementsuppression.label";


		public ImpossibleSuppression(Exception ex){
			super(ex);
		}


		@Override
		public String getI18nKey() {
			return impossibleSuppressionException;
		}

	}


	private static final class TargetsSortedByAppropriatePunishment{

		/**
		 * those ids are deletable folder ids
		 */
		List<Long> deletableFolderIds;

		/**
		 * those ids are requirements that should be deleted
		 */
		List<Long> deletableRequirementIds;

		/**
		 * those ids are requirements we need to reassign the subrequirements to their grandparent first (before it is deleted)
		 */
		List<Long> requirementsWithRewirableChildren;

		/**
		 * those ids are requirements which have only one version that should be deleted
		 */
		List<Long> requirementsWithOneDeletableVersion;

		/**
		 * those ids are requirements which have one version that should be unbound from the milestone
		 */
		List<Long> requirementsWithOneUnbindableVersion;


		List<Long> getDeletableRequirementIds() {
			return deletableRequirementIds != null ? deletableRequirementIds : new ArrayList<>();
		}

		List<Long> getRequirementsWithOneDeletableVersion() {
			return requirementsWithOneDeletableVersion != null ? requirementsWithOneDeletableVersion : new ArrayList<>();
		}

		List<Long> getDeletableFolderIds() {
			return deletableFolderIds != null ? deletableFolderIds : new ArrayList<>();
		}

		List<Long> getRequirementsWithOneUnbindableVersion() {
			return requirementsWithOneUnbindableVersion != null ? requirementsWithOneUnbindableVersion : new ArrayList<>();
		}

		List<Long> getRequirementsWithRewirableChildren() {
			return requirementsWithRewirableChildren != null ? requirementsWithRewirableChildren : new ArrayList<>();
		}

		void setDeletableRequirementIds(List<Long> deletableRequirementIds) {
			this.deletableRequirementIds = deletableRequirementIds;
		}

		public void setDeletableFolderIds(List<Long> deletableFolderIds) {
			this.deletableFolderIds = deletableFolderIds;
		}

		void setRequirementsWithOneDeletableVersion(List<Long> requirementsWithOneDeletableVersion) {
			this.requirementsWithOneDeletableVersion = requirementsWithOneDeletableVersion;
		}

		void setRequirementsWithOneUnbindableVersion(List<Long> requirementsWithOneUnbindableVersion) {
			this.requirementsWithOneUnbindableVersion = requirementsWithOneUnbindableVersion;
		}

		void setRequirementsWithRewirableChildren(List<Long> requirementsWithRewirableChildren) {
			this.requirementsWithRewirableChildren = requirementsWithRewirableChildren;
		}

	}

	/* **************************************************************************************************************
	 * 												Legacy code
	 ************************************************************************************************************** */

	/**
	 * Removing a list of RequirementLibraryNodes means : - find all the attachment lists, - remove them, - remove the
	 * nodes themselves
	 *
	 * NOOP notice
	 *
	 * This method is deprecated because it is no longer called by the super class : the method #deleteNodes has
	 * been overridden in the present subclass, that now calls more specific methods.
	 *
	 * However the class must still provide an implementation. So we leave this code as history, but marked as deprecated.
	 * Note that it doesn't support the milestone mode nor some other specific rules regarding requirement rewiring etc.
	 *
	 */
	@Override
	protected OperationReport batchDeleteNodes(List<Long> ids) {
		// NOOP see javadoc
		return null;
	}
	@Override
	protected boolean isMilestoneMode() {
		return activeMilestoneHolder.getActiveMilestone().isPresent();
	}

	private Long getActiveMilestoneId() {
		return activeMilestoneHolder.getActiveMilestone().get().getId();
	}


}
