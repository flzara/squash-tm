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
package org.squashtest.tm.service.internal.campaign;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.campaign.*;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.testcase.Dataset;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.exception.execution.ExecutionHasNoStepsException;
import org.squashtest.tm.exception.execution.ExecutionWasDeleted;
import org.squashtest.tm.exception.execution.TestPlanItemNotExecutableException;
import org.squashtest.tm.service.advancedsearch.IndexationService;
import org.squashtest.tm.service.annotation.*;
import org.squashtest.tm.service.campaign.CustomIterationModificationService;
import org.squashtest.tm.service.campaign.CustomTestSuiteModificationService;
import org.squashtest.tm.service.campaign.IterationStatisticsService;
import org.squashtest.tm.service.deletion.OperationReport;
import org.squashtest.tm.service.deletion.SuppressionPreviewReport;
import org.squashtest.tm.service.execution.ExecutionModificationService;
import org.squashtest.tm.service.internal.campaign.coercers.TestSuiteToIterationCoercerForArray;
import org.squashtest.tm.service.internal.campaign.coercers.TestSuiteToIterationCoercerForList;
import org.squashtest.tm.service.internal.campaign.coercers.TestSuiteToIterationCoercerForUniqueId;
import org.squashtest.tm.service.internal.customfield.PrivateCustomFieldValueService;
import org.squashtest.tm.service.internal.denormalizedField.PrivateDenormalizedFieldValueService;
import org.squashtest.tm.service.internal.library.PasteStrategy;
import org.squashtest.tm.service.internal.library.TreeNodeCopier;
import org.squashtest.tm.service.internal.repository.CampaignDao;
import org.squashtest.tm.service.internal.repository.ExecutionDao;
import org.squashtest.tm.service.internal.repository.IterationDao;
import org.squashtest.tm.service.internal.repository.IterationTestPlanDao;
import org.squashtest.tm.service.internal.repository.TestSuiteDao;
import org.squashtest.tm.service.milestone.MilestoneMembershipFinder;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.security.PermissionsUtils;
import org.squashtest.tm.service.security.SecurityCheckableObject;
import org.squashtest.tm.service.statistics.iteration.IterationStatisticsBundle;
import org.squashtest.tm.service.testcase.TestCaseCyclicCallChecker;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.squashtest.tm.service.security.Authorizations.OR_HAS_ROLE_ADMIN;

@Service("CustomIterationModificationService")
@Transactional
public class CustomIterationModificationServiceImpl implements CustomIterationModificationService,
	IterationTestPlanManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(CustomIterationModificationServiceImpl.class);
	private static final String PERMISSION_EXECUTE_ITEM = "hasPermission(#testPlanItemId, 'org.squashtest.tm.domain.campaign.IterationTestPlanItem', 'EXECUTE') ";

	@Inject
	private CampaignDao campaignDao;

	@Inject
	private IterationDao iterationDao;

	@Inject
	private TestSuiteDao suiteDao;

	@Inject
	private IterationTestPlanDao testPlanDao;

	@Inject
	private ExecutionDao executionDao;

	@Inject
	private TestCaseCyclicCallChecker testCaseCyclicCallChecker;

	@Inject
	private CampaignNodeDeletionHandler deletionHandler;

	@Inject
	private PermissionEvaluationService permissionService;

	@Inject
	private PrivateCustomFieldValueService customFieldValueService;

	@Inject
	private PrivateDenormalizedFieldValueService denormalizedFieldValueService;

	@Inject
	private IndexationService indexationService;

	@Inject
	private IterationStatisticsService statisticsService;

	@Inject
	private ExecutionModificationService executionModificationService;

	@Inject
	private MilestoneMembershipFinder milestoneService;

	@Inject
	@Qualifier("squashtest.tm.service.internal.PasteToIterationStrategy")
	private Provider<PasteStrategy<Iteration, TestSuite>> pasteToIterationStrategyProvider;

	@Inject
	private Provider<TreeNodeCopier> treeNodeCopierFactory;

	@Inject
	private CustomTestSuiteModificationService customTestSuiteModificationService;
	@Override
	@PreventConcurrent(entityType = CampaignLibraryNode.class)
	@PreAuthorize("hasPermission(#campaignId, 'org.squashtest.tm.domain.campaign.Campaign', 'CREATE') "
		+ OR_HAS_ROLE_ADMIN)
	public int addIterationToCampaign(Iteration iteration, @Id long campaignId, boolean copyTestPlan) {
		Campaign campaign = campaignDao.findById(campaignId);

		// copy the campaign test plan in the iteration

		List<CampaignTestPlanItem> campaignTestPlan = campaign.getTestPlan();

		if (copyTestPlan) {
			populateTestPlan(iteration, campaignTestPlan);
		}
		iterationDao.persistIterationAndTestPlan(iteration);
		campaign.addIteration(iteration);
		customFieldValueService.createAllCustomFieldValues(iteration, iteration.getProject());
		return campaign.getIterations().size() - 1;
	}

	/**
	 * populates an iteration's test plan from a campaign's test plan.
	 *
	 * @param iteration
	 * @param campaignTestPlan
	 */
	private void populateTestPlan(Iteration iteration, List<CampaignTestPlanItem> campaignTestPlan) {
		for (CampaignTestPlanItem campaignItem : campaignTestPlan) {

			TestCase testcase = campaignItem.getReferencedTestCase();
			Dataset dataset = campaignItem.getReferencedDataset();
			User assignee = campaignItem.getUser();

			IterationTestPlanItem item = new IterationTestPlanItem(testcase, dataset, assignee);

			iteration.addTestPlan(item);
		}
	}

	@Override
	@PreAuthorize("hasPermission(#campaignId, 'org.squashtest.tm.domain.campaign.Campaign', 'READ') "
		+ OR_HAS_ROLE_ADMIN)
	@Transactional(readOnly = true)
	public List<Iteration> findIterationsByCampaignId(long campaignId) {
		return campaignDao.findByIdWithInitializedIterations(campaignId).getIterations();
	}

	@Override
	@PostFilter("hasPermission(filterObject , 'READ')" + OR_HAS_ROLE_ADMIN)
	@Transactional(readOnly = true)
	public List<Iteration> findAllByIds(List<Long> iterationIds) {
		return iterationDao.findAllByIds(iterationIds);
	}

	@Override
	@PostAuthorize("hasPermission(returnObject, 'READ') " + OR_HAS_ROLE_ADMIN)
	@Transactional(readOnly = true)
	public Iteration findById(long iterationId) {
		return iterationDao.findById(iterationId);
	}

	@Override
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.tm.domain.campaign.Iteration', 'DELETE') "
		+ OR_HAS_ROLE_ADMIN)
	public String delete(long iterationId) {
		Iteration iteration = iterationDao.findById(iterationId);
		if (iteration == null) {
			return "ko";
		}

		iterationDao.removeFromCampaign(iteration);
		iterationDao.remove(iteration);

		return "ok";

	}

	@Override
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.tm.domain.campaign.Iteration', 'WRITE') "
		+ OR_HAS_ROLE_ADMIN)
	public void rename(long iterationId, String newName) {
		Iteration iteration = iterationDao.findById(iterationId);

		iteration.setName(newName);
	}

	/**
	 *
	 * @see org.squashtest.tm.service.campaign.CustomIterationModificationService#addExecution(long)
	 */
	@Override
	@PreAuthorize(PERMISSION_EXECUTE_ITEM + OR_HAS_ROLE_ADMIN)
	public Execution addExecution(long testPlanItemId) {
		IterationTestPlanItem item = testPlanDao.findById(testPlanItemId);
		return addExecution(item);
	}

	@Override
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.tm.domain.campaign.Iteration', 'READ') "
		+ OR_HAS_ROLE_ADMIN)
	@Transactional(readOnly = true)
	public List<Execution> findAllExecutions(long iterationId) {
		return iterationDao.findOrderedExecutionsByIterationId(iterationId);

	}

	@Override
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.tm.domain.campaign.Iteration', 'READ') "
		+ OR_HAS_ROLE_ADMIN)
	@Transactional(readOnly = true)
	public List<Execution> findExecutionsByTestPlan(long iterationId, long testPlanId) {
		return iterationDao.findOrderedExecutionsByIterationAndTestPlan(iterationId, testPlanId);

	}

	@Override
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.tm.domain.campaign.Iteration', 'READ') "
		+ OR_HAS_ROLE_ADMIN)
	@Transactional(readOnly = true)
	public List<TestCase> findPlannedTestCases(long iterationId) {
		Iteration iteration = iterationDao.findById(iterationId);
		return iteration.getPlannedTestCase();
	}

	@Override
	public List<SuppressionPreviewReport> simulateDeletion(List<Long> targetIds) {
		return deletionHandler.simulateIterationDeletion(targetIds);
	}

	@Override
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.tm.domain.campaign.Iteration', 'CREATE') "
		+ OR_HAS_ROLE_ADMIN)
	@PreventConcurrent(entityType = Iteration.class)
	public void addTestSuite(@Id long iterationId, TestSuite suite) {
		Iteration iteration = iterationDao.findById(iterationId);
		addTestSuite(iteration, suite);
	}

	@Override
	public void addTestSuite(Iteration iteration, TestSuite suite) {
		suiteDao.save(suite);
		iteration.addTestSuite(suite);
		customFieldValueService.createAllCustomFieldValues(suite, suite.getProject());
	}

	@Override
	@PostFilter("hasPermission(filterObject, 'READ')" + OR_HAS_ROLE_ADMIN)
	@Transactional(readOnly = true)
	public List<TestSuite> findAllTestSuites(long iterationId) {
		return iterationDao.findAllTestSuites(iterationId);
	}

	@Override
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.tm.domain.campaign.Iteration', 'CREATE') "
		+ OR_HAS_ROLE_ADMIN)
	@PreventConcurrents(
		simplesLocks = {@PreventConcurrent(entityType = Iteration.class, paramName = "iterationId"),
			@PreventConcurrent(entityType = Iteration.class, paramName = "testSuiteId", coercer = TestSuiteToIterationCoercerForUniqueId.class)}
	)
	public TestSuite copyPasteTestSuiteToIteration(@Id("testSuiteId") long testSuiteId, @Id("iterationId") long iterationId) {
		return createCopyToIterationStrategy().pasteNodes(iterationId, Arrays.asList(testSuiteId)).get(0);
	}

	private PasteStrategy<Iteration, TestSuite> createCopyToIterationStrategy() {
		PasteStrategy<Iteration, TestSuite> pasteStrategy = pasteToIterationStrategyProvider.get();
		pasteStrategy.setFirstLayerOperationFactory(treeNodeCopierFactory);
		pasteStrategy.setNextLayersOperationFactory(treeNodeCopierFactory);
		return pasteStrategy;
	}

	@Override
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.tm.domain.campaign.Iteration', 'CREATE') "
		+ OR_HAS_ROLE_ADMIN)
	@PreventConcurrents(
		simplesLocks = {@PreventConcurrent(entityType = Iteration.class, paramName = "iterationId")},
		batchsLocks = {@BatchPreventConcurrent(entityType = Iteration.class, paramName = "testSuiteIds", coercer = TestSuiteToIterationCoercerForArray.class)}
	)
	public List<TestSuite> copyPasteTestSuitesToIteration(@Ids("testSuiteIds") Long[] testSuiteIds, @Id("iterationId") long iterationId) {
		return createCopyToIterationStrategy().pasteNodes(iterationId, Arrays.asList(testSuiteIds));
	}

	@Override
	@BatchPreventConcurrent(entityType = Iteration.class, coercer = TestSuiteToIterationCoercerForList.class)
	public OperationReport removeTestSuites(@Ids List<Long> suitesIds) {
		List<TestSuite> testSuites = suiteDao.findAll(suitesIds);
		// check
		checkPermissionsForAll(testSuites, "DELETE");
		// proceed
		return deletionHandler.deleteSuites(suitesIds, false);

	}

	@Override
	public Execution addExecution(IterationTestPlanItem item) throws TestPlanItemNotExecutableException {

		Execution execution = createExec(item);
		item.addExecution(execution);
		for (TestSuite testSuite : item.getTestSuites()) {
			customTestSuiteModificationService.updateExecutionStatus(testSuite);
		}

		operationsAfterAddingExec(item, execution);
		return execution;
	}

	private Execution createExec(IterationTestPlanItem item) {
		TestCase testCase = item.getReferencedTestCase();
		testCaseCyclicCallChecker.checkNoCyclicCall(testCase);

		// if passes, let's move to the next step
		Execution execution = item.createExecution();

		// if we don't persist before we add, add will trigger an update of item.testPlan which fail because execution
		// has no id yet. this is caused by weird mapping (https://hibernate.onjira.com/browse/HHH-5732)
		executionDao.save(execution);
		return execution;
	}

	private void operationsAfterAddingExec(IterationTestPlanItem item, Execution execution) {
		createCustomFieldsForExecutionAndExecutionSteps(execution);
		createDenormalizedFieldsForExecutionAndExecutionSteps(execution);
		indexationService.reindexTestCase(item.getReferencedTestCase().getId());
	}

	private void createCustomFieldsForExecutionAndExecutionSteps(Execution execution) {
		customFieldValueService.createAllCustomFieldValues(execution, execution.getProject());
		customFieldValueService.createAllCustomFieldValues(execution.getSteps(), execution.getProject());
	}

	private void createDenormalizedFieldsForExecutionAndExecutionSteps(Execution execution) {
		LOGGER.debug("Create denormalized fields for Execution {}", execution.getId());

		TestCase sourceTC = execution.getReferencedTestCase();
		denormalizedFieldValueService.createAllDenormalizedFieldValues(sourceTC, execution);
		denormalizedFieldValueService.createAllDenormalizedFieldValuesForSteps(execution);

	}

	@Override
	public Execution addAutomatedExecution(IterationTestPlanItem item) throws TestPlanItemNotExecutableException {

		Execution execution = item.createAutomatedExecution();

		executionDao.save(execution);
		item.addExecution(execution);
		createDenormalizedFieldsForExecutionAndExecutionSteps(execution);

		return execution;

	}

	/* ************************* security ************************* */

	private void checkPermission(SecurityCheckableObject... checkableObjects) {
		PermissionsUtils.checkPermission(permissionService, checkableObjects);
	}

	/* ************************* private stuffs ************************* */

	private void checkPermissionsForAll(List<TestSuite> testSuites, String permission) {
		for (TestSuite testSuite : testSuites) {
			checkPermission(new SecurityCheckableObject(testSuite, permission));
		}

	}

	@Override
	@PreAuthorize("hasPermission(#testCaseId, 'org.squashtest.tm.domain.testcase.TestCase', 'READ') "
		+ OR_HAS_ROLE_ADMIN)
	public List<Iteration> findIterationContainingTestCase(long testCaseId) {
		return iterationDao.findAllIterationContainingTestCase(testCaseId);
	}

	@Override
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.tm.domain.campaign.Iteration', 'READ') "
		+ OR_HAS_ROLE_ADMIN)
	public IterationStatisticsBundle gatherIterationStatisticsBundle(long iterationId) {
		return statisticsService.gatherIterationStatisticsBundle(iterationId);
	}

	@Override
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.tm.domain.campaign.Iteration', 'READ') "
		+ OR_HAS_ROLE_ADMIN)
	public Collection<Milestone> findAllMilestones(long iterationId) {
		return milestoneService.findMilestonesForIteration(iterationId);
	}

	@Override
	public Execution updateExecutionFromTc(long executionId) {

		Execution exec = executionDao.findOne(executionId);
		if (exec == null) {
			throw new ExecutionWasDeleted();
		}

		if (exec.getReferencedTestCase() != null && exec.getReferencedTestCase().getSteps().isEmpty()) {
			throw new ExecutionHasNoStepsException();
		}

		int order = exec.getExecutionOrder();
		IterationTestPlanItem itpi = exec.getTestPlan();
		executionModificationService.deleteExecution(exec);

		Execution execution = createExec(itpi);
		itpi.addExecutionAtPos(execution, order);
		operationsAfterAddingExec(itpi, execution);
		return execution;
	}

}
