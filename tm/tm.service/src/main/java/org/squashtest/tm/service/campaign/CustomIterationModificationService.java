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

import org.squashtest.tm.domain.campaign.CampaignLibraryNode;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.service.annotation.BatchPreventConcurrent;
import org.squashtest.tm.service.annotation.Id;
import org.squashtest.tm.service.annotation.Ids;
import org.squashtest.tm.service.annotation.PreventConcurrent;
import org.squashtest.tm.service.annotation.PreventConcurrents;
import org.squashtest.tm.service.deletion.OperationReport;
import org.squashtest.tm.service.deletion.SuppressionPreviewReport;
import org.squashtest.tm.service.internal.campaign.coercers.TestSuiteToIterationCoercerForArray;
import org.squashtest.tm.service.internal.campaign.coercers.TestSuiteToIterationCoercerForList;
import org.squashtest.tm.service.internal.campaign.coercers.TestSuiteToIterationCoercerForUniqueId;
import org.squashtest.tm.service.statistics.iteration.IterationStatisticsBundle;

/**
 * Iteration modification services which cannot be dynamically generated.
 *
 * @author Gregory Fouquet
 *
 */
public interface CustomIterationModificationService extends IterationFinder {

	/**
	 * Adds an iteration to the list of iterations of a campaign.
	 *
	 * @param iteration
	 * @param campaignId
	 * @return the index of the added iteration.
	 */
	@PreventConcurrent(entityType = CampaignLibraryNode.class)
	int addIterationToCampaign(Iteration iteration, @Id long campaignId, boolean copyTestPlan);

	String delete(long iterationId);

	void rename(long iterationId, String newName);

	Execution addExecution(long testPlanItemId);

	/**
	 * that method should investigate the consequences of the deletion request, and return a report about what will
	 * happen.
	 *
	 * @param targetIds
	 * @return
	 */
	List<SuppressionPreviewReport> simulateDeletion(List<Long> targetIds);

	@PreventConcurrent(entityType=Iteration.class)
	void addTestSuite(@Id long iterationId, TestSuite suite);

	List<TestSuite> findAllTestSuites(long iterationId);

	/**
	 * <p>
	 * That method will remove each test suite, leaving it's test plan items linked to no test_suite
	 * </p>
	 *
	 * @param suitesIds
	 * @return
	 */
	@BatchPreventConcurrent(entityType=Iteration.class,coercer=TestSuiteToIterationCoercerForList.class)
	OperationReport removeTestSuites(@Ids List<Long> suitesIds);

	/**
	 * <p>
	 * Will create a copy of the test suite and it's test plan , then associate it to the given iteration<br>
	 * will rename test suite if there is name conflict at destination
	 * </p>
	 *
	 * @param testSuiteId
	 *            = test suite to copy
	 * @param iterationId
	 *            = iteration where to add the copy of the test suite
	 * @return the copy of the test suite
	 */
	@PreventConcurrents(
			simplesLocks={@PreventConcurrent(entityType=Iteration.class,paramName="iterationId"),
					@PreventConcurrent(entityType=Iteration.class,paramName="testSuiteId",coercer=TestSuiteToIterationCoercerForUniqueId.class)
				}
			)
	TestSuite copyPasteTestSuiteToIteration(@Id("testSuiteId")long testSuiteId, @Id("iterationId") long iterationId);

	/**
	 * <p>
	 * will create a copy of the test suites and their test plan , then associate them to the given iteration<br>
	 * will rename test suites if there is name conflict at destination
	 * </p>
	 *
	 * @param testSuiteIds
	 *            = list of test suites to copy
	 * @param iterationId
	 *            = iteration where to add the copy of the test suite
	 * @return the list containing all the copies of the test suites
	 */
	@PreventConcurrents(
			simplesLocks={@PreventConcurrent(entityType=Iteration.class,paramName="iterationId")},
			batchsLocks={@BatchPreventConcurrent(entityType=Iteration.class,paramName="testSuiteIds",coercer=TestSuiteToIterationCoercerForArray.class)}
			)
	List<TestSuite> copyPasteTestSuitesToIteration(@Ids("testSuiteIds") Long[] testSuiteIds,@Id("iterationId") long iterationId);

	IterationStatisticsBundle gatherIterationStatisticsBundle(long iterationId);

	Execution updateExecutionFromTc(long executionId);
}
