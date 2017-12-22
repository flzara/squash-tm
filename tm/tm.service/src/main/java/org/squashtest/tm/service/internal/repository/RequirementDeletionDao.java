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

public interface RequirementDeletionDao extends DeletionDao {

	/* **************************************************
	 * 				Main methods
	 ****************************************************/

	void deleteVersions(List<Long> versionIds);

	/**
	 * Given a list of RequirementLibraryNode ids, will tell which ones are folder ids and which ones are requirements.
	 *
	 * @param originalIds the requirement library node ids we want to sort out.
	 * @return an array of list of ids : result[0] are the folder ids and result[1] are the requirement ids.
	 */
	List<Long>[] separateFolderFromRequirementIds(List<Long> originalIds);

	List<Long> findRequirementAttachmentListIds(List<Long> requirementIds);
	List<Long> findRequirementVersionAttachmentListIds(List<Long> versionIds);
	List<Long> findRequirementFolderAttachmentListIds(List<Long> folderIds);

	void removeFromVerifiedVersionsLists(List<Long> versionIds);
	void removeFromLinkedVersionsLists(List<Long> versionIds);
	void removeFromVerifiedRequirementLists(List<Long> requirementIds);

	void deleteRequirementAuditEvents(List<Long> requirementIds);
	void deleteRequirementVersionAuditEvents(List<Long> versionIds);

	List<Long> findVersionIds(List<Long> requirementIds);

	/**
	 * @param versionsIds
	 */
	void removeTestStepsCoverageByRequirementVersionIds(List<Long> versionsIds);

	List<Long> findRemainingRequirementIds(List<Long> originalIds);



	/* *************************************************************
	 *  			Methods for the milestone mode
	 ************************************************************ */

	/**
	 * Will set the attribute "currentVersion" of each requirement to "null"
	 *
	 * @param requirementIds
	 */
	void unsetRequirementCurrentVersion(List<Long> requirementIds);

	/**
	 * Will set the attribute "currentVersion" of each requirement to their latestest version
	 * @param requirementIds
	 */
	void resetRequirementCurrentVersion(List<Long> requirementIds);


	void unbindFromMilestone(List<Long> requirementIds, Long milestoneId);

	List<Long> findVersionIdsForMilestone(List<Long> requirementIds, Long milestoneId);


	// ================= advanced predicates =======================


	/**
	 * <p>Given a list of requirement ids, returns the version ids that should
	 * be deleted according to the milestone rule.</p>
	 *
	 *   <p>A version is milestone-deletable if :
	 *   	<ul>
	 *   		<li>it belong to the said milestone AND,</li>
	 *   		<li>it has no milestone that locks it AND,</li>
	 *   		<li>it doesn't belong to more any other milestone</li>
	 *   	</ul>
	 *   </p>
	 *
	 * @param requirementIds
	 * @param milestoneId
	 * @return
	 */
	List<Long> findDeletableVersions(List<Long> requirementIds, Long milestoneId);


	/**
	 * <p>
	 * 	Given a list of requirement ids, returns the version ids that should
	 * 	be unbound from that milestone.
	 * </p>
	 *
	 * <p> A version is milestone-unbindable if :
	 * 	<ul>
	 * 		<li>it belong to the said milestone AND</li>
	 * 		<li>it has no milestone that locks it AND</li>
	 * 		<li>it belong to more than one milestone</li>
	 * 	</ul>
	 *
	 * </p>
	 *
	 * @param requirementIds
	 * @param milestoneId
	 * @return
	 */
	List<Long> findUnbindableVersions(List<Long> requirementIds, Long milestoneId);


	List<Long> filterRequirementsHavingDeletableVersions(List<Long> requirementIds, Long milestoneId);

	List<Long> filterRequirementsHavingUnbindableVersions(List<Long> requirementIds, Long milestoneId);

	// ================= simple predicates =========================


	/**
	 * Given their ids, returns the ids of <strong>requirements</strong> that have at least
	 * one version that cannot be removed due to restriction on the milestone status.
	 *
	 * @param requirementIds
	 * @return
	 */
	List<Long> filterRequirementsIdsWhichMilestonesForbidsDeletion(List<Long> requirementIds);

	/**
	 * Given their ids, return the ids of <strong>requirement version</strong> one cannot remove
	 * due to restrictions on the status of their milestones
	 *
	 * @param originalId
	 * @return
	 */
	List<Long> filterVersionIdsWhichMilestonesForbidsDeletion(List<Long> versionIds);

	/**
	 * Given their id, return which of them have many milestones
	 *
	 * @param nodeIds
	 * @return
	 */
	List<Long> filterVersionIdsHavingMultipleMilestones(List<Long> versionIds);


}
