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
package org.squashtest.tm.service.campaign;

import java.util.List;
import java.util.Map;

import org.squashtest.tm.domain.EntityReference;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.CampaignFolder;
import org.squashtest.tm.domain.campaign.CampaignLibrary;
import org.squashtest.tm.domain.campaign.CampaignLibraryNode;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.campaign.export.CampaignExportCSVModel;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.customfield.RawValue;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.service.annotation.BatchPreventConcurrent;
import org.squashtest.tm.service.annotation.Id;
import org.squashtest.tm.service.annotation.Ids;
import org.squashtest.tm.service.annotation.PreventConcurrent;
import org.squashtest.tm.service.annotation.PreventConcurrents;
import org.squashtest.tm.service.deletion.OperationReport;
import org.squashtest.tm.service.deletion.SuppressionPreviewReport;
import org.squashtest.tm.service.internal.campaign.coercers.CLNAndParentIdsCoercerForArray;
import org.squashtest.tm.service.internal.campaign.coercers.CLNAndParentIdsCoercerForList;
import org.squashtest.tm.service.internal.campaign.coercers.CampaignLibraryIdsCoercerForArray;
import org.squashtest.tm.service.internal.campaign.coercers.CampaignLibraryIdsCoercerForList;
import org.squashtest.tm.service.internal.campaign.coercers.TestSuiteToIterationCoercerForList;
import org.squashtest.tm.service.library.LibraryNavigationService;

public interface CampaignLibraryNavigationService extends
LibraryNavigationService<CampaignLibrary, CampaignFolder, CampaignLibraryNode>, CampaignLibraryFinderService {

	/**
	 * Adds a Campaign to the root of the library. The custom fields will be created with their default value.
	 *
	 * @param libraryId
	 * @param campaign
	 */
	@PreventConcurrent(entityType = CampaignLibrary.class)
	void addCampaignToCampaignLibrary(@Id long libraryId, Campaign campaign);

	/**
	 * Adds a Campaign to the root of the Library, and its initial custom field values. The initial custom field values
	 * are passed as a Map<Long, String>, that maps the id of the {@link CustomField} to the values of the corresponding {@link CustomFieldValue}.
	 * Read that last sentence again.
	 *
	 * @param libraryId
	 * @param campaign
	 * @param customFieldValues
	 */
	@PreventConcurrent(entityType = CampaignLibrary.class)
	void addCampaignToCampaignLibrary(@Id long libraryId, Campaign campaign, Map<Long, RawValue> customFieldValues);


	/**
	 * Adds a campaign to a folder. The custom fields will be created with their default value.
	 *
	 * @param libraryId
	 * @param campaign
	 */
	@PreventConcurrent(entityType = CampaignLibraryNode.class)
	void addCampaignToCampaignFolder(@Id long folderId, Campaign campaign);

	/**
	 * Adds a campaign to a folder, and its initial custom field values. The initial custom field values
	 * are passed as a Map<Long, String>, that maps the id of the {@link CustomField} to the values of the corresponding {@link CustomFieldValue}.
	 * Read that last sentence again.
	 *
	 * @param libraryId
	 * @param campaign
	 * @param customFieldValues
	 */
	@PreventConcurrent(entityType = CampaignLibraryNode.class)
	void addCampaignToCampaignFolder(@Id long folderId, Campaign campaign, Map<Long, RawValue> customFieldValues);

	@PreventConcurrent(entityType=CampaignLibraryNode.class,paramName="destinationId")
	void moveIterationsWithinCampaign(@Id("destinationId")long destinationId, Long[] nodeIds, int position);

	/**
	 * Adds a new iteration to a campaign. Returns the index of the new iteration.
	 *
	 * @param iteration
	 * @param campaignId
	 * @return
	 */
	@PreventConcurrent(entityType = CampaignLibraryNode.class)
	int addIterationToCampaign(Iteration iteration, @Id long campaignId, boolean copyTestPlan);


	/**
	 * Adds a new iteration to a campaign. Returns the index of the new iteration. The initial custom field values
	 * are passed as a Map<Long, String>, that maps the id of the {@link CustomField} to the values of the corresponding {@link CustomFieldValue}.
	 * Read that last sentence again.
	 *
	 *
	 * @param iteration
	 * @param campaignId
	 * @param customFieldValues
	 * @return
	 */
	@PreventConcurrent(entityType = CampaignLibraryNode.class)
	int addIterationToCampaign(Iteration iteration, @Id long campaignId, boolean copyTestPlan, Map<Long, RawValue> customFieldValues);

	List<Iteration> findIterationsByCampaignId(long campaignId);

	@PreventConcurrent(entityType = CampaignLibraryNode.class)
	List<Iteration> copyIterationsToCampaign(@Id long campaignId, Long[] iterationsIds);

	//FIXME move to TestSuiteFinder
	List<TestSuite> findIterationContent(long iterationId);

	/**
	 * @param
	 * @return
	 */
	@Override
	List<CampaignLibrary> findLinkableCampaignLibraries();

	/**
	 * that method should investigate the consequences of the deletion request of iterations, and return a report about
	 * what will happen.
	 *
	 * @param targetIds
	 * @return
	 */
	List<SuppressionPreviewReport> simulateIterationDeletion(List<Long> targetIds);

	/**
	 * that method should delete the iterations. It still takes care of non deletable iterations so the implementation
	 * should filter out the ids who can't be deleted.
	 *
	 *
	 * @param targetIds
	 * @return
	 */
	@BatchPreventConcurrent(entityType = CampaignLibraryNode.class)
	OperationReport deleteIterations(@Ids List<Long> targetIds);
	/**
	 * that method should investigate the consequences of the deletion request of tes suites, and return a report about
	 * what will happen.
	 *
	 * @param targetIds
	 * @return
	 */
	List<SuppressionPreviewReport> simulateSuiteDeletion(List<Long> targetIds);

	/**
	 * that method should delete test suites, and remove its references in iteration and iteration test plan item
	 *
	 * @param removeFromIter
	 *
	 * @param testSuites
	 * @return
	 */
	@BatchPreventConcurrent(entityType=Iteration.class,coercer=TestSuiteToIterationCoercerForList.class)
	OperationReport deleteSuites(@Ids List<Long> suiteIds, boolean removeFromIter);

	/**
	 * given a campaign Id, returns a model. It's made of rows and cell, and have a row header, check the relevant methods.
	 * Note that the actual model will differ according to the export type : "L" (light), "S" (standard), "F" (full).
	 *
	 * @param campaignId
	 * @return
	 */
	CampaignExportCSVModel exportCampaignToCSV(Long campaignId, String exportType);

	List<String> getParentNodesAsStringList(EntityReference entityReference);

	List<Long> findAllCampaignIdsForMilestone(Milestone milestone);

	// ####################### PREVENT CONCURRENCY OVERIDES ############################

	@Override
	@PreventConcurrents(
			simplesLocks={@PreventConcurrent(entityType=CampaignLibraryNode.class,paramName="destinationId")},
			batchsLocks={@BatchPreventConcurrent(entityType=CampaignLibrary.class, paramName="sourceNodesIds",coercer=CampaignLibraryIdsCoercerForArray.class),
					@BatchPreventConcurrent(entityType=CampaignLibraryNode.class, paramName="sourceNodesIds",coercer=CLNAndParentIdsCoercerForArray.class)}
			)
	List<CampaignLibraryNode> copyNodesToFolder(@Id("destinationId") long destinationId,@Ids("sourceNodesIds") Long[] sourceNodesIds);

	@Override
	@PreventConcurrents(
			simplesLocks={@PreventConcurrent(entityType=CampaignLibrary.class,paramName="destinationId")},
			batchsLocks={@BatchPreventConcurrent(entityType=CampaignLibrary.class, paramName="targetId",coercer=CampaignLibraryIdsCoercerForArray.class),
					@BatchPreventConcurrent(entityType=CampaignLibraryNode.class, paramName="targetId",coercer=CLNAndParentIdsCoercerForArray.class)}
			)
	List<CampaignLibraryNode> copyNodesToLibrary(@Id("destinationId") long destinationId,@Ids("targetId") Long[] targetId);

	@Override
	@PreventConcurrents(
			simplesLocks={@PreventConcurrent(entityType=CampaignLibraryNode.class,paramName="destinationId")},
			batchsLocks={@BatchPreventConcurrent(entityType=CampaignLibrary.class, paramName="targetId",coercer=CampaignLibraryIdsCoercerForArray.class),
					@BatchPreventConcurrent(entityType=CampaignLibraryNode.class, paramName="targetId",coercer=CLNAndParentIdsCoercerForArray.class)}
			)
	void moveNodesToFolder(@Id("destinationId") long destinationId,@Ids("targetId") Long[] targetId);

	@Override
	@PreventConcurrents(
			simplesLocks={@PreventConcurrent(entityType=CampaignLibraryNode.class,paramName="destinationId")},
			batchsLocks={@BatchPreventConcurrent(entityType=CampaignLibrary.class, paramName="targetId",coercer=CampaignLibraryIdsCoercerForArray.class),
					@BatchPreventConcurrent(entityType=CampaignLibraryNode.class, paramName="targetId",coercer=CLNAndParentIdsCoercerForArray.class)}
			)
	void moveNodesToFolder(@Id("destinationId") long destinationId,@Ids("targetId") Long[] targetId, int position);

	@Override
	@PreventConcurrents(
			simplesLocks={@PreventConcurrent(entityType=CampaignLibrary.class,paramName="destinationId")},
			batchsLocks={@BatchPreventConcurrent(entityType=CampaignLibrary.class, paramName="targetId",coercer=CampaignLibraryIdsCoercerForArray.class),
					@BatchPreventConcurrent(entityType=CampaignLibraryNode.class, paramName="targetId",coercer=CLNAndParentIdsCoercerForArray.class)}
			)
	void moveNodesToLibrary(@Id("destinationId") long destinationId,@Ids("targetId") Long[] targetId);

	@Override
	@PreventConcurrents(
			simplesLocks={@PreventConcurrent(entityType=CampaignLibrary.class,paramName="destinationId")},
			batchsLocks={@BatchPreventConcurrent(entityType=CampaignLibrary.class, paramName="targetId",coercer=CampaignLibraryIdsCoercerForArray.class),
					@BatchPreventConcurrent(entityType=CampaignLibraryNode.class, paramName="targetId",coercer=CLNAndParentIdsCoercerForArray.class)}
			)
	void moveNodesToLibrary(@Id("destinationId") long destinationId,@Ids("targetId") Long[] targetId, int position);

	@Override
	@PreventConcurrents(
			batchsLocks={@BatchPreventConcurrent(entityType=CampaignLibrary.class, paramName="targetIds",coercer=CampaignLibraryIdsCoercerForList.class),
					@BatchPreventConcurrent(entityType=CampaignLibraryNode.class, paramName="targetIds",coercer=CLNAndParentIdsCoercerForList.class)}
			)
	OperationReport deleteNodes(@Ids("targetIds") List<Long> targetIds);

	// ###################### /PREVENT CONCURRENCY OVERIDES ############################

}
