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
import org.springframework.context.MessageSource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.CampaignLibraryNode;
import org.squashtest.tm.domain.campaign.CampaignTestPlanItem;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.campaign.TestPlanStatistics;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStep;
import org.squashtest.tm.domain.execution.ExecutionVisitor;
import org.squashtest.tm.domain.execution.KeywordExecution;
import org.squashtest.tm.domain.execution.ScriptedExecution;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.testcase.ConsumerForScriptedTestCaseVisitor;
import org.squashtest.tm.domain.testcase.Dataset;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.exception.DuplicateNameException;
import org.squashtest.tm.exception.execution.ExecutionHasNoStepsException;
import org.squashtest.tm.exception.execution.ExecutionWasDeleted;
import org.squashtest.tm.exception.execution.TestPlanItemNotExecutableException;
import org.squashtest.tm.service.annotation.BatchPreventConcurrent;
import org.squashtest.tm.service.annotation.Id;
import org.squashtest.tm.service.annotation.Ids;
import org.squashtest.tm.service.annotation.PreventConcurrent;
import org.squashtest.tm.service.annotation.PreventConcurrents;
import org.squashtest.tm.service.attachment.AttachmentManagerService;
import org.squashtest.tm.service.campaign.CustomCampaignModificationService;
import org.squashtest.tm.service.campaign.CustomIterationModificationService;
import org.squashtest.tm.service.campaign.CustomTestSuiteModificationService;
import org.squashtest.tm.service.campaign.IterationStatisticsService;
import org.squashtest.tm.service.deletion.OperationReport;
import org.squashtest.tm.service.deletion.SuppressionPreviewReport;
import org.squashtest.tm.service.execution.ExecutionModificationService;
import org.squashtest.tm.service.internal.campaign.coercers.TestSuiteToIterationCoercerForArray;
import org.squashtest.tm.service.internal.campaign.coercers.TestSuiteToIterationCoercerForList;
import org.squashtest.tm.service.internal.campaign.coercers.TestSuiteToIterationCoercerForUniqueId;
import org.squashtest.tm.service.internal.campaign.scripted.ScriptedTestCaseExecutionHelper;
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
import org.squashtest.tm.service.user.UserAccountService;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.squashtest.tm.service.security.Authorizations.CREATE_CAMPAIGN_OR_ROLE_ADMIN;
import static org.squashtest.tm.service.security.Authorizations.CREATE_ITERATION_OR_ROLE_ADMIN;
import static org.squashtest.tm.service.security.Authorizations.DELETE_ITERATION_OR_ROLE_ADMIN;
import static org.squashtest.tm.service.security.Authorizations.EXECUTE_ITPI_OR_ROLE_ADMIN;
import static org.squashtest.tm.service.security.Authorizations.LINK_ITERATION_OR_ROLE_ADMIN;
import static org.squashtest.tm.service.security.Authorizations.OR_HAS_ROLE_ADMIN;
import static org.squashtest.tm.service.security.Authorizations.READ_CAMPAIGN_OR_ROLE_ADMIN;
import static org.squashtest.tm.service.security.Authorizations.READ_ITERATION_OR_ROLE_ADMIN;
import static org.squashtest.tm.service.security.Authorizations.READ_TC_OR_ROLE_ADMIN;
import static org.squashtest.tm.service.security.Authorizations.WRITE_ITERATION_OR_ROLE_ADMIN;

@Service("CustomIterationModificationService")
@Transactional
public class CustomIterationModificationServiceImpl implements CustomIterationModificationService,
	IterationTestPlanManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(CustomIterationModificationServiceImpl.class);
	private static final String ITERATION_ID = "iterationId";

	@Inject
	private MessageSource messageSource;

	@Inject
	private CampaignDao campaignDao;

	@Inject
	private CustomCampaignModificationService campaignModificationService;

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

	@Inject
	private ScriptedTestCaseExecutionHelper scriptedTestCaseExecutionHelper;

	@Inject
	private AttachmentManagerService attachmentManagerService;

	@Inject
	private UserAccountService userService;

	@Override
	@PreventConcurrent(entityType = CampaignLibraryNode.class)
	@PreAuthorize(CREATE_CAMPAIGN_OR_ROLE_ADMIN)
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
	@PreAuthorize(READ_CAMPAIGN_OR_ROLE_ADMIN)
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
	@PreAuthorize(DELETE_ITERATION_OR_ROLE_ADMIN)
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
	@PreAuthorize(WRITE_ITERATION_OR_ROLE_ADMIN)
	public void rename(long iterationId, String newName) {
		Iteration iteration = iterationDao.findById(iterationId);

		List<Iteration> list= iteration.getCampaign().getIterations();

		String trimedName = newName.trim();

		if (!campaignModificationService.checkIterationNameAvailable(trimedName, list)) {
			throw new DuplicateNameException("Cannot rename iteration " + iteration.getName() + " : new name " + trimedName
				+ " already exists in iteration " + campaignModificationService);
		}
		iteration.setName(trimedName);
	}



	/**
	 *
	 * @see org.squashtest.tm.service.campaign.CustomIterationModificationService#addExecution(long)
	 */
	@Override
	@PreAuthorize(EXECUTE_ITPI_OR_ROLE_ADMIN)
	@PreventConcurrent(entityType=IterationTestPlanItem.class)
	public Execution addExecution(@Id long testPlanItemId) {
		IterationTestPlanItem item = testPlanDao.findById(testPlanItemId);
		return addExecution(item);
	}

	@Override
	public Execution addExecution(long testPlanItemId, MessageSource messageSource) {
		IterationTestPlanItem item = testPlanDao.findById(testPlanItemId);
		Locale locale = item.getProject().getBddScriptLanguage().getLocale();
		return addExecution(item, messageSource, locale);
	}

	@Override
	@PreAuthorize(READ_ITERATION_OR_ROLE_ADMIN)
	@Transactional(readOnly = true)
	public List<Execution> findAllExecutions(long iterationId) {
		return iterationDao.findOrderedExecutionsByIterationId(iterationId);

	}

	@Override
	@PreAuthorize(READ_ITERATION_OR_ROLE_ADMIN)
	@Transactional(readOnly = true)
	public List<Execution> findExecutionsByTestPlan(long iterationId, long testPlanId) {
		return iterationDao.findOrderedExecutionsByIterationAndTestPlan(iterationId, testPlanId);

	}

	@Override
	@PreAuthorize(READ_ITERATION_OR_ROLE_ADMIN)
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
	@PreAuthorize(CREATE_ITERATION_OR_ROLE_ADMIN)
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
	@PreAuthorize(LINK_ITERATION_OR_ROLE_ADMIN)
	@PreventConcurrent(entityType = Iteration.class)
	public void changeTestSuitePosition(@Id long iterationId, int newIndex, List<Long> itemIds) {

		Iteration iteration = iterationDao.findById(iterationId);

		List<TestSuite> items = suiteDao.findAllById(itemIds);

		iteration.moveTestSuites(newIndex, items);
	}

	@Override
	@PreAuthorize(CREATE_ITERATION_OR_ROLE_ADMIN)
	@PreventConcurrents(
		simplesLocks = {@PreventConcurrent(entityType = Iteration.class, paramName = ITERATION_ID),
			@PreventConcurrent(entityType = Iteration.class, paramName = "testSuiteId", coercer = TestSuiteToIterationCoercerForUniqueId.class)}
	)
	public TestSuite copyPasteTestSuiteToIteration(@Id("testSuiteId") long testSuiteId, @Id(ITERATION_ID) long iterationId) {
		return createCopyToIterationStrategy().pasteNodes(iterationId, Arrays.asList(testSuiteId)).get(0);
	}

	private PasteStrategy<Iteration, TestSuite> createCopyToIterationStrategy() {
		PasteStrategy<Iteration, TestSuite> pasteStrategy = pasteToIterationStrategyProvider.get();
		pasteStrategy.setFirstLayerOperationFactory(treeNodeCopierFactory);
		pasteStrategy.setNextLayersOperationFactory(treeNodeCopierFactory);
		return pasteStrategy;
	}

	@Override
	@PreAuthorize(CREATE_ITERATION_OR_ROLE_ADMIN)
	@PreventConcurrents(
		simplesLocks = {@PreventConcurrent(entityType = Iteration.class, paramName = ITERATION_ID)},
		batchsLocks = {@BatchPreventConcurrent(entityType = Iteration.class, paramName = "testSuiteIds", coercer = TestSuiteToIterationCoercerForArray.class)})
	public List<TestSuite> copyPasteTestSuitesToIteration(@Ids("testSuiteIds") Long[] testSuiteIds, @Id(ITERATION_ID) long iterationId) {
		return createCopyToIterationStrategy().pasteNodes(iterationId, Arrays.asList(testSuiteIds));
	}

	@Override
	@BatchPreventConcurrent(entityType = Iteration.class, coercer = TestSuiteToIterationCoercerForList.class)
	public OperationReport removeTestSuites(@Ids List<Long> suitesIds) {
		List<TestSuite> testSuites = suiteDao.findAllById(suitesIds);
		// check
		checkPermissionsForAll(testSuites, "DELETE");
		// proceed
		return deletionHandler.deleteSuites(suitesIds, false);

	}

	@Override
	public Execution addExecution(IterationTestPlanItem item) throws TestPlanItemNotExecutableException {

		Execution execution = createExec(item, null, null);
		item.addExecution(execution);
		for (TestSuite testSuite : item.getTestSuites()) {
			customTestSuiteModificationService.updateExecutionStatus(testSuite);
		}

		operationsAfterAddingExec(execution);
		return execution;
	}

	@Override
	public Execution addExecution(IterationTestPlanItem item, MessageSource messageSource, Locale locale) throws TestPlanItemNotExecutableException {
		Execution execution = createExec(item, messageSource, locale);
		item.addExecution(execution);
		for (TestSuite testSuite : item.getTestSuites()) {
			customTestSuiteModificationService.updateExecutionStatus(testSuite);
		}

		operationsAfterAddingExec(execution);
		return execution;
	}

	private Execution createExec(IterationTestPlanItem item, MessageSource messageSource, Locale locale) {
		TestCase testCase = item.getReferencedTestCase();
		testCaseCyclicCallChecker.checkNoCyclicCall(testCase);

		// if passes, let's move to the next step
		Execution execution;
		if (messageSource != null && locale != null) {
			execution = item.createExecution(messageSource, locale);
		} else {
			execution = item.createExecution(null, null);
		}

		// if we don't persist before we add, add will trigger an update of item.testPlan which fail because execution
		// has no id yet. this is caused by weird mapping (https://hibernate.onjira.com/browse/HHH-5732)
		executionDao.save(execution);
		// we can now copy attachment contents of test case and test step,
		// witch is a NOOP in database attachment mode (blob copy handled by Hibernate)
		// but will actually do the blob copy in file system attachment mode
		attachmentManagerService.copyContentsOnExternalRepository(execution);
		for (ExecutionStep executionStep : execution.getSteps()) {
			attachmentManagerService.copyContentsOnExternalRepository(executionStep);
		}
		return execution;
	}

	private void operationsAfterAddingExec(Execution execution) {
		ExecutionVisitor executionVisitor = new ExecutionVisitor() {
			@Override
			public void visit(Execution execution) {
				createCustomFieldsForExecutionAndExecutionSteps(execution);
				createDenormalizedFieldsForExecutionAndExecutionSteps(execution);
			}

			@Override
			public void visit(ScriptedExecution scriptedExecution) {
				createCustomAndDenormalizedFieldsForExecution(scriptedExecution);
				createExecutionStepsForScriptedTestCase(scriptedExecution);
			}

			@Override
			public void visit(KeywordExecution keywordExecution) {
				createCustomAndDenormalizedFieldsForExecution(keywordExecution);
			}

		};
		execution.accept(executionVisitor);
	}

	//This method is responsible for create execution steps by parsing the script
	//For a standard test case we do that job directly in model but for scripted test case we can't
	//the model mustn't have a parser as dependency, and we don't want to hack the original tests case by detaching him from hibernate session and add virtual steps
	private void createExecutionStepsForScriptedTestCase(ScriptedExecution scriptedExecution) {

		ConsumerForScriptedTestCaseVisitor testCaseVisitor = new ConsumerForScriptedTestCaseVisitor(
			scriptedTestCase -> scriptedTestCaseExecutionHelper.createExecutionStepsForScriptedTestCase(scriptedExecution));

		scriptedExecution.getReferencedTestCase().accept(testCaseVisitor);
	}

	private void createCustomFieldsForExecutionAndExecutionSteps(Execution execution) {
		customFieldValueService.createAllCustomFieldValues(execution, execution.getProject());
		customFieldValueService.createAllCustomFieldValues(execution.getSteps(), execution.getProject());
	}

	//SQUASH-597 : no cuf in keyword step, only in execution
	private void createCustomAndDenormalizedFieldsForExecution(Execution execution) {
		customFieldValueService.createAllCustomFieldValues(execution, execution.getProject());
		createDenormalizedFieldsForExecution(execution);
	}

	private void createDenormalizedFieldsForExecutionAndExecutionSteps(Execution execution) {
		createDenormalizedFieldsForExecution(execution);
		denormalizedFieldValueService.createAllDenormalizedFieldValuesForSteps(execution);
	}

	private void createDenormalizedFieldsForExecution(Execution execution) {
		LOGGER.debug("Create denormalized fields for Execution {}", execution.getId());

		TestCase sourceTC = execution.getReferencedTestCase();
		denormalizedFieldValueService.createAllDenormalizedFieldValues(sourceTC, execution);
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
	@PreAuthorize(READ_TC_OR_ROLE_ADMIN)
	public List<Iteration> findIterationContainingTestCase(long testCaseId) {
		return iterationDao.findAllIterationContainingTestCase(testCaseId);
	}

	@Override
	@PreAuthorize(READ_ITERATION_OR_ROLE_ADMIN)
	public IterationStatisticsBundle gatherIterationStatisticsBundle(long iterationId) {
		return statisticsService.gatherIterationStatisticsBundle(iterationId);
	}

	@Override
	@PreAuthorize(READ_ITERATION_OR_ROLE_ADMIN)
	@Transactional(readOnly=true)
	public Collection<Milestone> findAllMilestones(long iterationId) {
		return milestoneService.findMilestonesForIteration(iterationId);
	}

	@Override
	public Execution updateExecutionFromTc(long executionId) {

		Optional<Execution> optExec = executionDao.findById(executionId);
		if (! optExec.isPresent()) {
			throw new ExecutionWasDeleted();
		}
		Execution exec = optExec.get();
		if (exec.getReferencedTestCase() != null && exec.getReferencedTestCase().getSteps().isEmpty()) {
			throw new ExecutionHasNoStepsException();
		}

		int order = exec.getExecutionOrder();
		IterationTestPlanItem itpi = exec.getTestPlan();
		executionModificationService.deleteExecution(exec);

		Execution execution = createExec(itpi, null, null);
		itpi.addExecutionAtPos(execution, order);
		operationsAfterAddingExec(execution);
		return execution;
	}

	@Override
	@PreAuthorize(READ_ITERATION_OR_ROLE_ADMIN)
	public TestPlanStatistics findIterationStatistics(long iterationId) {
		try {
			PermissionsUtils.checkPermission(permissionService, Arrays.asList(iterationId), "READ_UNASSIGNED", Iteration.class.getName());
			return iterationDao.getIterationStatistics(iterationId);

		} catch (AccessDeniedException ade) {
			LOGGER.error(ade.getMessage(), ade);
			String userLogin = userService.findCurrentUser().getLogin();
			return iterationDao.getIterationStatistics(iterationId, userLogin);

		}
	}

}
