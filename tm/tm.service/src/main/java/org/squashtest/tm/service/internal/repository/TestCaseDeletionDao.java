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

public interface TestCaseDeletionDao extends DeletionDao {

	//data getters
	List<Long> findTestSteps(List<Long> testCaseIds);
	List<Long> findTestCaseAttachmentListIds(List<Long> testCaseIds);
	List<Long> findTestStepAttachmentListIds(List<Long> testStepIds);
	List<Long> findTestCaseFolderAttachmentListIds(List<Long> folderIds);

	//data removers
	void removeAllSteps(List<Long> testStepIds);
	void removeCampaignTestPlanInboundReferences(List<Long> testCaseIds);
	void removeOrSetIterationTestPlanInboundReferencesToNull(List<Long> testCaseIds);
	void setExecStepInboundReferencesToNull(List<Long> testStepIds);
	void setExecutionInboundReferencesToNull(List<Long> testCaseIds);
	void removeFromVerifyingTestCaseLists(List<Long> testCaseIds);
	void unbindFromMilestone(List<Long> testCaseIds, Long milestoneId);

	/**
	 * @param testStepIds
	 */
	void removeFromVerifyingTestStepsList(List<Long> testStepIds);


	/**
	 * Given a list of TestCaseLibraryNode ids, will tell which ones are folder ids and which ones are campaigns.
	 * 
	 * @param originalIds the requirement library node ids we want to sort out.
	 * @return an array of list of ids : result[0] are the folder ids and result[1] are the test case ids.
	 */
	List<Long>[] separateFolderFromTestCaseIds(List<Long> originalIds);

	/**
	 * Given a list of ids, retain only the ids of test cases that were not deleted
	 * 
	 * @param originalIds
	 * @return
	 */
	List<Long> findRemainingTestCaseIds(List<Long> originalIds);

	/**
	 * Given their ids, return the ids of test cases one cannot remove
	 * due to restrictions on the status of their milestones
	 * 
	 * @param originalId
	 * @return
	 */
	List<Long> findTestCasesWhichMilestonesForbidsDeletion(List<Long> originalId);
}
