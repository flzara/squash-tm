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
package org.squashtest.tm.service.internal.testcase;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.Paging;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.PagingBackedPagedCollectionHolder;
import org.squashtest.tm.core.foundation.lang.Couple;
import org.squashtest.tm.core.foundation.lang.PathUtils;
import org.squashtest.tm.domain.IdCollector;
import org.squashtest.tm.domain.Identified;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.customfield.BoundEntity;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.customfield.RawValue;
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.milestone.MilestoneStatus;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.testautomation.AutomatedTest;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.CallTestStep;
import org.squashtest.tm.domain.testcase.Parameter;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseAutomatable;
import org.squashtest.tm.domain.testcase.TestCaseFolder;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.tm.domain.testcase.TestStep;
import org.squashtest.tm.domain.testcase.TestStepVisitor;
import org.squashtest.tm.domain.tf.automationrequest.AutomationRequest;
import org.squashtest.tm.domain.tf.automationrequest.AutomationRequestStatus;
import org.squashtest.tm.domain.tf.automationrequest.RemoteAutomationRequestExtender;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.exception.DuplicateNameException;
import org.squashtest.tm.exception.InconsistentInfoListItemException;
import org.squashtest.tm.exception.UnallowedTestAssociationException;
import org.squashtest.tm.exception.testautomation.MalformedScriptPathException;
import org.squashtest.tm.service.advancedsearch.IndexationService;
import org.squashtest.tm.service.annotation.Id;
import org.squashtest.tm.service.annotation.PreventConcurrent;
import org.squashtest.tm.service.attachment.AttachmentManagerService;
import org.squashtest.tm.service.campaign.IterationTestPlanFinder;
import org.squashtest.tm.service.infolist.InfoListItemFinderService;
import org.squashtest.tm.service.internal.customfield.PrivateCustomFieldValueService;
import org.squashtest.tm.service.internal.library.NodeManagementService;
import org.squashtest.tm.service.internal.repository.ActionTestStepDao;
import org.squashtest.tm.service.internal.repository.AutomationRequestDao;
import org.squashtest.tm.service.internal.repository.LibraryNodeDao;
import org.squashtest.tm.service.internal.repository.TestCaseDao;
import org.squashtest.tm.service.internal.repository.TestCaseFolderDao;
import org.squashtest.tm.service.internal.repository.TestCaseLibraryDao;
import org.squashtest.tm.service.internal.repository.TestStepDao;
import org.squashtest.tm.service.internal.testautomation.UnsecuredAutomatedTestManagerService;
import org.squashtest.tm.service.internal.testcase.event.TestCaseNameChangeEvent;
import org.squashtest.tm.service.internal.testcase.event.TestCaseReferenceChangeEvent;
import org.squashtest.tm.service.internal.testcase.event.TestCaseScriptAutoChangeEvent;
import org.squashtest.tm.service.milestone.ActiveMilestoneHolder;
import org.squashtest.tm.service.milestone.MilestoneMembershipManager;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.security.PermissionsUtils;
import org.squashtest.tm.service.testautomation.model.TestAutomationProjectContent;
import org.squashtest.tm.service.testcase.CustomTestCaseModificationService;
import org.squashtest.tm.service.testcase.ParameterModificationService;
import org.squashtest.tm.service.testcase.TestCaseImportanceManagerService;
import org.squashtest.tm.service.testcase.TestCaseLibraryNavigationService;
import org.squashtest.tm.service.tf.AutomationRequestFinderService;
import org.squashtest.tm.service.tf.AutomationRequestModificationService;
import org.squashtest.tm.service.user.UserAccountService;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import static org.squashtest.tm.service.security.Authorizations.OR_HAS_ROLE_ADMIN;
import static org.squashtest.tm.service.security.Authorizations.READ_TC_OR_ROLE_ADMIN;
import static org.squashtest.tm.service.security.Authorizations.WRITE_PARENT_TC_OR_ROLE_ADMIN;
import static org.squashtest.tm.service.security.Authorizations.WRITE_TC_OR_ROLE_ADMIN;

/**
 * @author Gregory Fouquet
 */
@Service("CustomTestCaseModificationService")
@Transactional
public class CustomTestCaseModificationServiceImpl implements CustomTestCaseModificationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CustomTestCaseModificationServiceImpl.class);
	private static final int STEP_LAST_POS = -1;
	private static final Long NO_ACTIVE_MILESTONE_ID = -9000L;
	private static final String WRITE_AS_AUTOMATION = "WRITE_AS_AUTOMATION";

	@Inject
	private TestCaseDao testCaseDao;

	@Inject
	private AutomationRequestDao requestDao;

	@Inject
	@Qualifier("squashtest.tm.repository.TestCaseLibraryNodeDao")
	private LibraryNodeDao<TestCaseLibraryNode> testCaseLibraryNodeDao;

	@Inject
	private ActionTestStepDao actionStepDao;

	@Inject
	private TestCaseImportanceManagerService testCaseImportanceManagerService;

	@Inject
	private TestStepDao testStepDao;

	@Inject
	@Named("squashtest.tm.service.internal.TestCaseManagementService")
	private NodeManagementService<TestCase, TestCaseLibraryNode, TestCaseFolder> testCaseManagementService;

	@Inject
	private TestCaseNodeDeletionHandler deletionHandler;

	@Inject
	private UnsecuredAutomatedTestManagerService taService;

	@Inject
	protected PrivateCustomFieldValueService customFieldValuesService;

	@Inject
	private ParameterModificationService parameterModificationService;

	@Inject
	private InfoListItemFinderService infoListItemService;

	@Inject
	private MilestoneMembershipManager milestoneService;

	@Inject
	private TestCaseLibraryNavigationService libraryService;

	@Inject
	private ActiveMilestoneHolder activeMilestoneHolder;

	@Inject
	private IterationTestPlanFinder iterationTestPlanFinder;

	@Inject
	private IndexationService indexationService;

	@Inject
	private AttachmentManagerService attachmentManagerService;

	@Inject
	private UserAccountService userAccountService;

	@Inject
	private AutomationRequestFinderService automationRequestFinderService;

	@Inject
	private AutomationRequestModificationService automationRequestModificationService;

	@Inject
	private PermissionEvaluationService permissionEvaluationService;

	@Inject
	private ApplicationEventPublisher eventPublisher;

	@Inject
	private TestCaseFolderDao testCaseFolderDao;

	@Inject
	private TestCaseLibraryDao testCaseLibraryDao;


	/* *************** TestCase section ***************************** */

	@Override
	@PreAuthorize(WRITE_TC_OR_ROLE_ADMIN)
	public void rename(long testCaseId, String newName) throws DuplicateNameException {

		TestCase testCase = testCaseDao.findById(testCaseId);

		LOGGER.debug("changing test case #{} name from '{}' to '{}' ", testCase.getId(), testCase.getName(), newName);

		testCaseManagementService.renameNode(testCaseId, newName);

		eventPublisher.publishEvent(new TestCaseNameChangeEvent(testCaseId, newName));

		LOGGER.trace("reindexing");
		// [Issue 6337] sorry ma, they forced me to
		reindexItpisReferencingTestCase(testCase);
		// Issue #6776 : it seems that the more we fix it the more we break it...
		indexationService.batchReindexTc(Lists.newArrayList(testCase.getId()));
	}


	@Override
	@PreAuthorize(WRITE_TC_OR_ROLE_ADMIN)
	public void changeReference(long testCaseId, String reference) {

		TestCase testCase = testCaseDao.findById(testCaseId);

		LOGGER.debug("changing test case #{} reference from '{}' to '{}' ", testCase.getId(), testCase.getReference(), reference);

		testCase.setReference(reference);

		eventPublisher.publishEvent(new TestCaseReferenceChangeEvent(testCaseId, reference));

		LOGGER.trace("reindexing");
		// [Issue 6337] sorry ma, they forced me to
		reindexItpisReferencingTestCase(testCase);
		// Issue #6776 : it seems that the more we fix it the more we break it...
		indexationService.batchReindexTc(Lists.newArrayList(testCase.getId()));
	}

	@Override
	@PreAuthorize(WRITE_TC_OR_ROLE_ADMIN)
	public void changeImportance(long testCaseId, TestCaseImportance importance) {
		TestCase testCase = testCaseDao.findById(testCaseId);
		LOGGER.debug("changing test case #{} importance from '{}' to '{}' ", testCase.getId(), testCase.getImportance(), importance);
		testCase.setImportance(importance);
		reindexItpisReferencingTestCase(testCase);
		// Issue #6776 : it seems that the more we fix it the more we break it...
		indexationService.batchReindexTc(Lists.newArrayList(testCase.getId()));
	}

	private void reindexItpisReferencingTestCase(TestCase testCase) {
		List<IterationTestPlanItem> itpis = iterationTestPlanFinder.findByReferencedTestCase(testCase);
		List<Long> itpiIds = new ArrayList();
		for (IterationTestPlanItem itpi : itpis) {
			itpiIds.add(itpi.getId());
		}
		LOGGER.trace("reindexing");
		indexationService.batchReindexItpi(itpiIds);
	}

	@Override
	@PreAuthorize(READ_TC_OR_ROLE_ADMIN)
	@Transactional(readOnly = true)
	public List<TestStep> findStepsByTestCaseId(long testCaseId) {

		LOGGER.debug("retrieving test steps for test case #{}", testCaseId);

		List<TestStep> steps = testCaseDao.findTestSteps(testCaseId);

		traceResult(steps, "test steps");

		return steps;
	}

	/* *************** TestStep section ***************************** */

	@Override
	@PreAuthorize(WRITE_PARENT_TC_OR_ROLE_ADMIN)
	@PreventConcurrent(entityType = TestCase.class)
	public ActionTestStep addActionTestStep(@Id long parentTestCaseId, ActionTestStep newTestStep) {

		return addActionTestStep(parentTestCaseId, newTestStep, STEP_LAST_POS);

	}


	@Override
	@PreAuthorize(WRITE_PARENT_TC_OR_ROLE_ADMIN)
	@PreventConcurrent(entityType = TestCase.class)
	public ActionTestStep addActionTestStep(@Id long parentTestCaseId, ActionTestStep newTestStep, int index) {

		LOGGER.debug("adding a new action step to test case #{}", parentTestCaseId);
		TestCase parentTestCase = testCaseDao.findById(parentTestCaseId);

		testStepDao.persist(newTestStep);

		if (index == STEP_LAST_POS){
			parentTestCase.addStep(newTestStep);
		}
		else {
			parentTestCase.addStep(index, newTestStep);
		}

		LOGGER.trace("creating custom field values");
		customFieldValuesService.createAllCustomFieldValues(newTestStep, newTestStep.getProject());
		LOGGER.trace("processing parameters");
		parameterModificationService.createParamsForStep(newTestStep.getId());

		return newTestStep;
	}

	@Override
	@PreAuthorize(WRITE_PARENT_TC_OR_ROLE_ADMIN)
	@PreventConcurrent(entityType = TestCase.class)
	public ActionTestStep addActionTestStep(@Id long parentTestCaseId, ActionTestStep newTestStep,
											Map<Long, RawValue> customFieldValues) {

		ActionTestStep step = addActionTestStep(parentTestCaseId, newTestStep);
		initCustomFieldValues(step, customFieldValues);

		return step;
	}

	@Override
	@PreAuthorize(WRITE_PARENT_TC_OR_ROLE_ADMIN)
	@PreventConcurrent(entityType = TestCase.class)
	public ActionTestStep addActionTestStep(@Id long parentTestCaseId, ActionTestStep newTestStep,
											Map<Long, RawValue> customFieldValues, int index) {

		ActionTestStep step = addActionTestStep(parentTestCaseId, newTestStep, index);
		initCustomFieldValues(step, customFieldValues);

		return step;
	}

	@Override
	@PreAuthorize("hasPermission(#testStepId, 'org.squashtest.tm.domain.testcase.TestStep' , 'WRITE')" + OR_HAS_ROLE_ADMIN)
	public void updateTestStepAction(long testStepId, String newAction) {
		ActionTestStep testStep = actionStepDao.findById(testStepId);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("changing step #{} action to '{}'", testStepId, newAction.substring(0, 25));
		}

		testStep.setAction(newAction);
		parameterModificationService.createParamsForStep(testStepId);
	}

	@Override
	@PreAuthorize("hasPermission(#testStepId, 'org.squashtest.tm.domain.testcase.TestStep' , 'WRITE')" + OR_HAS_ROLE_ADMIN)
	public void updateTestStepExpectedResult(long testStepId, String newExpectedResult) {
		ActionTestStep testStep = actionStepDao.findById(testStepId);

		if (LOGGER.isDebugEnabled()){
			LOGGER.debug("changing step #{} expected result to '{}'", testStepId, newExpectedResult.substring(0, 25));
		}

		testStep.setExpectedResult(newExpectedResult);
		parameterModificationService.createParamsForStep(testStepId);
	}

	/**
	 * @deprecated does not seem to be used any longer
	 */
	@Override
	@Deprecated
	@PreAuthorize(WRITE_TC_OR_ROLE_ADMIN)
	@PreventConcurrent(entityType = TestCase.class)
	public void changeTestStepPosition(@Id long testCaseId, long testStepId, int newStepPosition) {
		TestCase testCase = testCaseDao.findById(testCaseId);
		int index = findTestStepInTestCase(testCase, testStepId);

		LOGGER.debug("moving step #{} to position : {}", testStepId, newStepPosition);

		testCase.moveStep(index, newStepPosition);
	}

	@Override
	@PreAuthorize(WRITE_TC_OR_ROLE_ADMIN)
	@PreventConcurrent(entityType = TestCase.class)
	public void changeTestStepsPosition(@Id long testCaseId, int newPosition, List<Long> stepIds) {

		TestCase testCase = testCaseDao.findById(testCaseId);
		List<TestStep> steps = testStepDao.findListById(stepIds);

		LOGGER.debug("moving steps #{} to position {}", stepIds, newPosition);

		testCase.moveSteps(newPosition, steps);

	}

	@Override
	@PreAuthorize(WRITE_TC_OR_ROLE_ADMIN)
	@PreventConcurrent(entityType = TestCase.class)
	public void removeStepFromTestCase(@Id long testCaseId, long testStepId) {
		LOGGER.debug("deleting step #{} from test case #{}", testStepId, testCaseId);
		TestCase testCase = testCaseDao.findById(testCaseId);
		TestStep testStep = testStepDao.findById(testStepId);
		deletionHandler.deleteStep(testCase, testStep);
	}

	@Override
	@PreAuthorize(WRITE_TC_OR_ROLE_ADMIN)
	@PreventConcurrent(entityType = TestCase.class)
	public void removeStepFromTestCaseByIndex(@Id long testCaseId, int index) {
		LOGGER.debug("deleting step at index {} from test case #{}", index, testCaseId);
		TestCase testCase = testCaseDao.findById(testCaseId);
		TestStep testStep = testCase.getSteps().get(index);
		deletionHandler.deleteStep(testCase, testStep);
	}

	/*
	 * given a TestCase, will search for a TestStep in the steps list (identified with its testStepId)
	 *
	 * returns : the index if found, -1 if not found or if the provided TestCase is null
	 */
	private int findTestStepInTestCase(TestCase testCase, long testStepId) {
		return testCase.getPositionOfStep(testStepId);
	}

	@Override
	@PostAuthorize("hasPermission(returnObject, 'READ')" + OR_HAS_ROLE_ADMIN)
	@Transactional(readOnly = true)
	public TestCase findTestCaseWithSteps(long testCaseId) {
		LOGGER.debug("loading test case #{}", testCaseId);
		return testCaseDao.findAndInit(testCaseId);
	}

	@Override
	@PreAuthorize(WRITE_TC_OR_ROLE_ADMIN)
	@PreventConcurrent(entityType = TestCase.class)
	public List<TestStep> removeListOfSteps(@Id long testCaseId, List<Long> testStepIds) {

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("deleting {} steps from test case #{}", testStepIds.size(), testCaseId);
			LOGGER.trace("deleted steps : {}", testStepIds);
		}
		TestCase testCase = testCaseDao.findById(testCaseId);

		for (Long id : testStepIds) {
			TestStep step = testStepDao.findById(id);
			deletionHandler.deleteStep(testCase, step);
		}
		return testCase.getSteps();
	}

	@Override
	@PreAuthorize(READ_TC_OR_ROLE_ADMIN)
	@Transactional(readOnly = true)
	public PagedCollectionHolder<List<TestStep>> findStepsByTestCaseIdFiltered(long testCaseId, Paging paging) {
		LOGGER.debug("loading paged list of steps for test case #{}", testCaseId);

		List<TestStep> list = testCaseDao.findAllStepsByIdFiltered(testCaseId, paging);
		long count = findStepsByTestCaseId(testCaseId).size();

		traceResult(list, "test steps");

		return new PagingBackedPagedCollectionHolder<>(paging, count, list);
	}


	@Override
	@PreAuthorize(WRITE_TC_OR_ROLE_ADMIN)
	@PreventConcurrent(entityType = TestCase.class)
	public boolean pasteCopiedTestStep(@Id long testCaseId, long idInsertion, long copiedTestStepId) {
		Integer position = testStepDao.findPositionOfStep(idInsertion) + 1;
		LOGGER.debug("copying step #{} of test case #{} and inserting at position {}", copiedTestStepId, testCaseId, position);
		return pasteTestStepAtPosition(testCaseId, Arrays.asList(copiedTestStepId), position);
	}

	@Override
	@PreAuthorize(WRITE_TC_OR_ROLE_ADMIN)
	@PreventConcurrent(entityType = TestCase.class)
	public boolean pasteCopiedTestSteps(@Id long testCaseId, long idInsertion, List<Long> copiedTestStepIds) {
		Integer position = testStepDao.findPositionOfStep(idInsertion) + 1;
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("copying {} steps of test case #{} and inserting at position {}", copiedTestStepIds.size(), testCaseId, position);
			LOGGER.trace("copied step ids : {}", copiedTestStepIds);
		}
		return pasteTestStepAtPosition(testCaseId, copiedTestStepIds, position);
	}

	@Override
	@PreAuthorize(WRITE_TC_OR_ROLE_ADMIN)
	@PreventConcurrent(entityType = TestCase.class)
	public boolean pasteCopiedTestStepToLastIndex(@Id long testCaseId, long copiedTestStepId) {
		LOGGER.debug("copying step #{} into test case #{} at last position", copiedTestStepId, testCaseId);
		return pasteTestStepAtPosition(testCaseId, Arrays.asList(copiedTestStepId), null);
	}

	@Override
	@PreAuthorize(WRITE_TC_OR_ROLE_ADMIN)
	@PreventConcurrent(entityType = TestCase.class)
	public boolean pasteCopiedTestStepToLastIndex(@Id long testCaseId, List<Long> copiedTestStepIds) {
		if (LOGGER.isDebugEnabled()){
			LOGGER.debug("copying {} steps into test case #{} at last position", copiedTestStepIds.size(), testCaseId);
			LOGGER.trace("step ids are : {}", copiedTestStepIds);
		}
		return pasteTestStepAtPosition(testCaseId, copiedTestStepIds, null);
	}


	// FIXME : check for potential cycle with call steps. For now it's being checked
	// on the controller but it is obviously less safe.
	// FIXME : Refactor the method for null and not null position... it shouldn't be in the same method.

	/**
	 * @param testCaseId
	 * @param copiedStepIds
	 * @param position
	 * @return true if copied step is instance of CallTestStep
	 */
	private boolean pasteTestStepAtPosition(long testCaseId, List<Long> copiedStepIds, Integer position) {

		boolean hasCallstep = false;

		List<TestStep> originals = testStepDao.findByIdOrderedByIndex(copiedStepIds);

		// Issue 6146
		// If position is null we add at the end of list, so the index is correct
		// If position is not null we add several time at the same index. The list push
		// the content to the right, so we need to invert the order...
		if (position != null) {
			Collections.reverse(originals);
		}

		// attach it to the test case
		TestCase testCase = testCaseDao.findById(testCaseId);

		TestStepVisitor visitor = new TestStepVisitor() {
			@Override
			public void visit(ActionTestStep visited) {
				attachmentManagerService.copyAttachments(visited);
			}

			@Override
			public void visit(CallTestStep visited) {

			}
		};

		for (TestStep original : originals) {

			LOGGER.trace("copying step #{}", original.getId());
			// first, create the step
			TestStep copyStep = original.createCopy();
			testStepDao.persist(copyStep);
			LOGGER.trace("new step #{} created", copyStep.getId());
			copyStep.accept(visitor);

			LOGGER.trace("adding step");
			if (position != null && position < testCase.getSteps().size()) {
				testCase.addStep(position, copyStep);
			} else {
				testCase.addStep(copyStep);
			}

			// now special treatment if the steps are from another source
			if (!testCase.getSteps().contains(original)) {
				if (LOGGER.isTraceEnabled()) {
					LOGGER.trace("this step originates from a different test case : #{}", original.getTestCase().getId());
					LOGGER.trace("checking whether the importance of the receiving test case needs reevaluation");
				}
				updateImportanceIfCallStep(testCase, copyStep);
				LOGGER.trace("checking for potential new parameters for the receiving test case");
				parameterModificationService.createParamsForStep(copyStep);
			}

			LOGGER.trace("copying custom fields");
			copyStep.accept(new TestStepCustomFieldCopier(original));

			// last, update that weird variable
			hasCallstep = hasCallstep || CallTestStep.class.isAssignableFrom(copyStep.getClass());
		}

		LOGGER.trace("job done, with returned flag hasCallstep = {}", hasCallstep);
		return hasCallstep;
	}

	private void updateImportanceIfCallStep(TestCase parentTestCase, TestStep copyStep) {
		if (CallTestStep.class.isAssignableFrom(copyStep.getClass())) {
			TestCase called = ((CallTestStep) copyStep).getCalledTestCase();
			LOGGER.trace("reevaluating importance for test case #{}", parentTestCase.getId());
			testCaseImportanceManagerService.changeImportanceIfCallStepAddedToTestCases(called, parentTestCase);
		}
	}


	@Override
	@Transactional(readOnly = true)
	public PagedCollectionHolder<List<TestCase>> findCallingTestCases(long testCaseId, PagingAndSorting sorting) {
		LOGGER.debug("paged search for test cases calling test case #{}", testCaseId);

		List<TestCase> callers = testCaseDao.findAllCallingTestCases(testCaseId, sorting);
		Long countCallers = testCaseDao.countCallingTestSteps(testCaseId);

		traceResult(callers, "calling test cases");

		return new PagingBackedPagedCollectionHolder<>(sorting, countCallers, callers);

	}

	@Override
	public PagedCollectionHolder<List<CallTestStep>> findCallingTestSteps(long testCaseId, PagingAndSorting sorting) {
		LOGGER.debug("paged search for test steps calling test case #{}", testCaseId);

		List<CallTestStep> callers = testCaseDao.findAllCallingTestSteps(testCaseId, sorting);
		Long countCallers = testCaseDao.countCallingTestSteps(testCaseId);

		traceResult(callers, "calling test steps");

		return new PagingBackedPagedCollectionHolder<>(sorting, countCallers, callers);
	}

	@Override
	public List<CallTestStep> findAllCallingTestSteps(long testCaseId) {
		LOGGER.debug("total search for test steps calling test case #{}", testCaseId);

		List<CallTestStep> steps = testCaseDao.findAllCallingTestSteps(testCaseId);

		traceResult(steps, "calling test steps");

		return steps;
	}

	@Override
	@PreAuthorize(WRITE_TC_OR_ROLE_ADMIN)
	public void changeImportanceAuto(long testCaseId, boolean auto) {
		LOGGER.debug("changing test case #{} importance auto flag to : {}", testCaseId, auto);

		TestCase testCase = testCaseDao.findById(testCaseId);
		testCase.setImportanceAuto(auto);

		LOGGER.trace("recalculating test case importance if required");
		testCaseImportanceManagerService.changeImportanceIfIsAuto(testCaseId);
	}

	@Override
	@PreAuthorize(WRITE_TC_OR_ROLE_ADMIN)
	@Transactional(readOnly = true)
	public Collection<TestAutomationProjectContent> findAssignableAutomationTests(long testCaseId) {
		LOGGER.debug("looking for assignable automated tests for test case #{}", testCaseId);

		TestCase testCase = testCaseDao.findById(testCaseId);

		Collection<TestAutomationProject> taProjects = testCase.getProject().getTestAutomationProjects();

		if (LOGGER.isTraceEnabled()){
			List<Long> taProjectIds = IdCollector.collect(taProjects);
			LOGGER.trace("involved test automation projects are : {}", taProjectIds);
		}

		return taService.listTestsInProjects(taProjects);
	}

	@Override
	@PreAuthorize(WRITE_TC_OR_ROLE_ADMIN)
	public AutomatedTest bindAutomatedTest(Long testCaseId, Long taProjectId, String testName) {
		LOGGER.debug("binding test case #{} to automated test '{}' (project #{}", testCaseId, testName, taProjectId);

		TestAutomationProject project = taService.findProjectById(taProjectId);

		AutomatedTest newTest = new AutomatedTest(testName, project);

		AutomatedTest persisted = taService.persistOrAttach(newTest);
		LOGGER.trace("created persistent automated test #{}", persisted.getId());

		TestCase testCase = testCaseDao.findById(testCaseId);
		AutomatedTest previousTest = testCase.getAutomatedTest();
		testCase.setAutomatedTest(persisted);

		if (previousTest != null) {
			LOGGER.trace("deleting previous automated test if exists and unused");
			taService.removeIfUnused(previousTest);
		}

		eventPublisher.publishEvent(new TestCaseScriptAutoChangeEvent(testCaseId, newTest.getFullName()));

		return newTest;
	}

	@Override
	@PreAuthorize(WRITE_TC_OR_ROLE_ADMIN)
	public AutomatedTest bindAutomatedTest(Long testCaseId, String testPath) {
		LOGGER.debug("binding test case #{} to automated test (path '{}')", testCaseId, testPath);

		if (StringUtils.isBlank(testPath)) {
			LOGGER.trace("path is blank -> resetting binding to null");
			removeAutomation(testCaseId);
			return null;
		} else {

			Couple<Long, String> projectAndTestname = extractAutomatedProjectAndTestName(testCaseId, testPath);

			// once it's okay we commit the test association
			return bindAutomatedTest(testCaseId, projectAndTestname.getA1(), projectAndTestname.getA2());
		}

	}

	@Override
	public void removeAutomation(long testCaseId) {
		LOGGER.debug("unbinding test case #{} from automated test", testCaseId);

		TestCase testCase = testCaseDao.findById(testCaseId);
		AutomatedTest previousTest = testCase.getAutomatedTest();
		testCase.removeAutomatedScript();

		LOGGER.trace("deleting unbound automated test if exists and unused");
		taService.removeIfUnused(previousTest);
	}

	/**
	 * initialCustomFieldValues maps the id of a CustomField to the value of the corresponding CustomFieldValues for
	 * that BoundEntity. read it again until it makes sense. it assumes that the CustomFieldValues instances already
	 * exists.
	 *
	 * @param entity
	 * @param initialCustomFieldValues
	 */
	protected void initCustomFieldValues(BoundEntity entity, Map<Long, RawValue> initialCustomFieldValues) {

		LOGGER.debug("initializing the custom field values for entity {}#{}", entity.getBoundEntityType(), entity.getBoundEntityId());

		List<CustomFieldValue> persistentValues = customFieldValuesService.findAllCustomFieldValues(entity);

		for (CustomFieldValue value : persistentValues) {
			Long customFieldId = value.getCustomField().getId();

			if (initialCustomFieldValues.containsKey(customFieldId)) {
				RawValue newValue = initialCustomFieldValues.get(customFieldId);
				newValue.setValueFor(value);
				LOGGER.trace("setting customfield '{}' to value '{}'", value.getCustomField().getCode(), newValue.getValue());
			}

		}
	}

	/**
	 * @see org.squashtest.tm.service.testcase.CustomTestCaseFinder# findAllByAncestorIds(java.util.List)
	 */
	@Override
	@PostFilter("hasPermission(filterObject , 'READ')" + OR_HAS_ROLE_ADMIN)
	public List<TestCase> findAllByAncestorIds(Collection<Long> folderIds) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("searching all test cases belonging to the subtrees of {} nodes", folderIds.size());
			LOGGER.trace("folder ids are : {}", folderIds);
		}

		List<TestCaseLibraryNode> nodes = testCaseLibraryNodeDao.findAllByIds(folderIds);

		List<TestCase> testCases = new TestCaseNodeWalker().walk(nodes);

		traceResult(testCases, "test cases");

		return testCases;
	}

	/**
	 * @see org.squashtest.tm.service.testcase.CustomTestCaseFinder#findAllCallingTestCases(long)
	 */
	@Override
	@PostFilter("hasPermission(filterObject , 'READ')" + OR_HAS_ROLE_ADMIN)
	public List<TestCase> findAllCallingTestCases(long calleeId) {
		LOGGER.debug("searching all test cases calling the test case #{}", calleeId);

		List<TestCase> callers = testCaseDao.findAllCallingTestCases(calleeId);

		traceResult(callers, "calling test cases");

		return callers;
	}

	@Override
	public TestCase findTestCaseFromStep(long testStepId) {
		LOGGER.debug("searching for the test case holding the step #{}", testStepId);
		TestCase tc = testCaseDao.findTestCaseByTestStepId(testStepId);
		LOGGER.trace("found test case : #{}", tc.getId());
		return tc;
	}

	/**
	 * @see org.squashtest.tm.service.testcase.CustomTestCaseFinder#findImpTCWithImpAuto(Collection)
	 */
	@Override
	public Map<Long, TestCaseImportance> findImpTCWithImpAuto(Collection<Long> testCaseIds) {
		LOGGER.debug("searching for importance of test case #{} (restricted to : having importance auto)", testCaseIds);
		return testCaseDao.findAllTestCaseImportanceWithImportanceAuto(testCaseIds);
	}

	/**
	 * @see org.squashtest.tm.service.testcase.CustomTestCaseFinder#findCallingTCids(long, Collection)
	 */
	@Override
	public Set<Long> findCallingTCids(long updatedId, Collection<Long> callingCandidates) {
		LOGGER.debug("searching for test cases calling test case #{}, and that belong to the following set : {}", updatedId, callingCandidates);

		List<Long> candidates = new ArrayList<>(callingCandidates);
		List<Long> currentLayer = testCaseDao
			.findAllTestCasesIdsCallingTestCases(Arrays.asList(updatedId));
		Set<Long> callingTCToUpdate = new HashSet<>();

		while (!currentLayer.isEmpty() && !candidates.isEmpty()) {
			LOGGER.trace("exploring ancestors");
			LOGGER.trace("current layer : {}", currentLayer);
			LOGGER.trace("remaining candidates : {}", candidates);
			// filter found calling test cases
			currentLayer.retainAll(candidates);
			// save
			callingTCToUpdate.addAll(currentLayer);
			// reduce test case of interest
			candidates.removeAll(currentLayer);
			// go next layer
			currentLayer = testCaseDao.findAllTestCasesIdsCallingTestCases(currentLayer);
		}

		LOGGER.trace("No more layers to explore or all candidates are found, job done");
		LOGGER.trace("found {} calling test cases, ids are : {}", callingTCToUpdate.size(), callingTCToUpdate);

		return callingTCToUpdate;
	}


	@Override
	// TODO : secure this
	public TestCase addNewTestCaseVersion(long originalTcId, TestCase newVersionData) {

		LOGGER.debug("creating new version of test case #{}", originalTcId);

		List<Long> milestoneIds = new ArrayList<>();

		Optional<Milestone> activeMilestone = activeMilestoneHolder.getActiveMilestone();
		if (activeMilestone.isPresent()) {
			Milestone milestone = activeMilestone.get();
			LOGGER.trace("active milestone detected : #{}", milestone.getId());
			milestoneIds.add(milestone.getId());
		}

		// copy the core attributes
		LOGGER.trace("copying test case");
		TestCase orig = testCaseDao.findById(originalTcId);
		TestCase newTC = orig.createCopy();
		LOGGER.trace("created new test case #{}", newTC.getId());

		LOGGER.trace("updating with new attributes");
		newTC.setName(newVersionData.getName());
		newTC.setReference(newVersionData.getReference());
		newTC.setDescription(newVersionData.getDescription());
		newTC.clearMilestones();

		// now we must insert that at the correct location
		TestCaseLibrary library = libraryService.findLibraryOfRootNodeIfExist(orig);
		if (library != null) {
			LOGGER.trace("inserting new test case in library #{}", library.getId());
			libraryService.addTestCaseToLibrary(library.getId(), newTC, null);
		} else {
			TestCaseFolder folder = libraryService.findParentIfExists(orig);
			LOGGER.trace("inserting new test case in folder #{}", folder.getId());
			libraryService.addTestCaseToFolder(folder.getId(), newTC, null);
		}

		// copy custom fields
		LOGGER.trace("copying the custom field values from original test case #{} into new test case #{}", orig.getId(), newTC.getId());
		customFieldValuesService.copyCustomFieldValuesContent(orig, newTC);

		Queue<ActionTestStep> origSteps = new LinkedList<>(orig.getActionSteps());
		Queue<ActionTestStep> newSteps = new LinkedList<>(newTC.getActionSteps());
		while (!origSteps.isEmpty()) {
			ActionTestStep oStep = origSteps.remove();
			ActionTestStep nStep = newSteps.remove();
			LOGGER.trace("copying custom field values from step #{} into new step #{}", oStep.getId(), nStep.getId());
			customFieldValuesService.copyCustomFieldValuesContent(oStep, nStep);
		}

		// manage the milestones
		LOGGER.trace("rebinding milestones");
		milestoneService.bindTestCaseToMilestones(newTC.getId(), milestoneIds);
		milestoneService.unbindTestCaseFromMilestones(originalTcId, milestoneIds);

		return newTC;
	}

	@Override
	@PreAuthorize(WRITE_TC_OR_ROLE_ADMIN)
	public void addParametersFromPrerequisite(long testCaseId) {
		LOGGER.debug("adding test case #{} parameters from its attribute 'prerequisite'", testCaseId);
		TestCase testCase = testCaseDao.findById(testCaseId);
		Set<String> parameters = testCase.findUsedParamsNamesInPrerequisite();

		for (String name : parameters) {
			Parameter parameter = testCase.findParameterByName(name);
			if (parameter == null) {
				LOGGER.trace("found new parameter '{}', adding it to test case", name);
				parameterModificationService.addNewParameterToTestCase(new Parameter(name), testCaseId);
			}
		}

	}


	@Override
	@PreAuthorize(WRITE_TC_OR_ROLE_ADMIN)
	public void changeNature(long testCaseId, String natureCode) {
		LOGGER.debug("changing test case #{} nature to '{}'", testCaseId, natureCode);
		TestCase testCase = testCaseDao.findById(testCaseId);
		InfoListItem nature = infoListItemService.findByCode(natureCode);

		if (infoListItemService.isNatureConsistent(testCase.getProject().getId(), natureCode)) {
			testCase.setNature(nature);
		} else {
			throw new InconsistentInfoListItemException("nature", natureCode);
		}

	}

	@Override
	@PreAuthorize(WRITE_TC_OR_ROLE_ADMIN)
	public void changeType(long testCaseId, String typeCode) {
		LOGGER.trace("changing test case #{} type to : '{}'", testCaseId, typeCode);
		TestCase testCase = testCaseDao.findById(testCaseId);
		InfoListItem type = infoListItemService.findByCode(typeCode);

		if (infoListItemService.isTypeConsistent(testCase.getProject().getId(), typeCode)) {
			testCase.setType(type);
		} else {
			throw new InconsistentInfoListItemException("type", typeCode);
		}
	}

	@Override
	public AutomatedTest bindAutomatedTestByAutomationProgrammer(Long testCaseId, String testPath) {


		LOGGER.debug("binding test case #{} to automated test (path '{}')", testCaseId, testPath);

		AutomationRequest automationRequest = automationRequestFinderService.findRequestByTestCaseId(testCaseId);

		if(automationRequest.getProject().isAllowAutomationWorkflow()
			&& TestCaseAutomatable.Y.equals(automationRequest.getTestCase().getAutomatable())) {
			PermissionsUtils.checkPermission(permissionEvaluationService, Collections.singletonList(automationRequest.getId()), WRITE_AS_AUTOMATION, AutomationRequest.class.getName());

			if (StringUtils.isBlank(testPath)) {
				LOGGER.trace("path is blank -> resetting binding to null");
				removeAutomation(testCaseId);
				return null;
			} else {

				Couple<Long, String> projectAndTestname = extractAutomatedProjectAndTestName(testCaseId, testPath);

				// once it's okay we commit the test association
				return bindAutomatedTest(testCaseId, projectAndTestname.getA1(), projectAndTestname.getA2());
			}

		} else {
			throw new IllegalArgumentException();
		}
	}

	@Override
	@Transactional(readOnly = true)
	public Collection<TestAutomationProjectContent> findAssignableAutomationTestsToAutomationProgramer(long testCaseId) {
		LOGGER.debug("looking for assignable automated tests for test case #{}", testCaseId);
		AutomationRequest automationRequest = automationRequestFinderService.findRequestByTestCaseId(testCaseId);
		Collection<TestAutomationProject> taProjects = null;

		if(automationRequest.getProject().isAllowAutomationWorkflow()
			&& TestCaseAutomatable.Y.equals(automationRequest.getTestCase().getAutomatable())) {
			PermissionsUtils.checkPermission(permissionEvaluationService, Collections.singletonList(automationRequest.getId()), WRITE_AS_AUTOMATION, AutomationRequest.class.getName());
			TestCase testCase = testCaseDao.findById(testCaseId);

			taProjects = testCase.getProject().getTestAutomationProjects();

			if (LOGGER.isTraceEnabled()) {
				List<Long> taProjectIds = IdCollector.collect(taProjects);
				LOGGER.trace("involved test automation projects are : {}", taProjectIds);
			}
		} else {
			throw new IllegalArgumentException();
		}

		return taService.listTestsInProjects(taProjects);
	}

	/* ********************************************************************************
	 *
	 * Milestones section
	 *
	 * *******************************************************************************
	 */

	@Override
	@PreAuthorize(WRITE_TC_OR_ROLE_ADMIN)
	public void bindMilestones(long testCaseId, Collection<Long> milestoneIds) {
		LOGGER.debug("binding test case #{} to milestones {}", testCaseId, milestoneIds);
		milestoneService.bindTestCaseToMilestones(testCaseId, milestoneIds);
	}

	@Override
	@PreAuthorize(WRITE_TC_OR_ROLE_ADMIN)
	public void unbindMilestones(long testCaseId, Collection<Long> milestoneIds) {
		LOGGER.debug("unbinding test case #{} from milestones {}", testCaseId, milestoneIds);
		milestoneService.unbindTestCaseFromMilestones(testCaseId, milestoneIds);
	}

	@Override
	@PreAuthorize(READ_TC_OR_ROLE_ADMIN)
	public Collection<Milestone> findAllMilestones(long testCaseId) {
		LOGGER.debug("searching milestones that test case #{} belongs to", testCaseId);
		Collection<Milestone> milestones = milestoneService.findAllMilestonesForTestCase(testCaseId);
		traceResult(milestones, "milestones");
		return milestones;
	}

	@Override
	@PreAuthorize(WRITE_TC_OR_ROLE_ADMIN)
	public Collection<Milestone> findAssociableMilestones(long testCaseId) {
		LOGGER.debug("searching milestones that test case #{} can bind to", testCaseId);
		Collection<Milestone> milestones = milestoneService.findAssociableMilestonesToTestCase(testCaseId);
		traceResult(milestones, "milestones");
		return milestones;
	}

	@Override
	public Collection<Milestone> findAssociableMilestonesForMassModif(List<Long> testCaseIds) {
		LOGGER.debug("searching milestones that all the following test cases can bind to : {}", testCaseIds);
		Collection<Milestone> milestones = null;

		for (Long testCaseId : testCaseIds) {
			List<Milestone> mil = testCaseDao.findById(testCaseId).getProject().getMilestones();
			if (milestones != null) {
				//keep only milestone that in ALL selected tc
				milestones.retainAll(mil);
			} else {
				//populate the collection for the first time
				milestones = new ArrayList<>(mil);
			}
		}
		LOGGER.trace("found {} candidates, now filtering according to status", milestones.size());
		filterLockedAndPlannedStatus(milestones);
		traceResult(milestones, "milestones");
		return milestones;
	}


	private void filterLockedAndPlannedStatus(Collection<Milestone> milestones) {
		CollectionUtils.filter(milestones, new Predicate() {
			@Override
			public boolean evaluate(Object milestone) {

				return ((Milestone) milestone).getStatus() != MilestoneStatus.LOCKED
					&& ((Milestone) milestone).getStatus() != MilestoneStatus.PLANNED;
			}
		});
	}


	@Override
	public Collection<Long> findBindedMilestonesIdForMassModif(List<Long> testCaseIds) {

		LOGGER.debug("searching for milestone ids that are bound to all the following test cases : {}", testCaseIds);

		LOGGER.trace("gathering the milestones");
		Collection<Milestone> milestones = null;

		for (Long testCaseId : testCaseIds) {
			Set<Milestone> mil = testCaseDao.findById(testCaseId).getMilestones();
			if (milestones != null) {
				//keep only milestone that in ALL selected tc
				milestones.retainAll(mil);
			} else {
				//populate the collection for the first time
				milestones = new ArrayList<>(mil);
			}
		}


		LOGGER.trace("filtering by status");

		filterLockedAndPlannedStatus(milestones);

		List<Long> milestoneIds = IdCollector.collect(milestones);

		if (LOGGER.isTraceEnabled()){
			LOGGER.trace("found {} milestones, ids are : {}", milestoneIds.size(), milestoneIds);
		}

		return milestoneIds;
	}


	@Override
	public boolean haveSamePerimeter(List<Long> testCaseIds) {
		LOGGER.debug("testing whether the following test cases have the same milestone perimeter : {}", testCaseIds);

		if (testCaseIds.size() > 1) {

			// XXX this implementation actually compares the perimeter of the first test case of the list
			// with that of each others, yet other's perimeters aren't compared as well
			// This isn't consistent with the method name and no javadoc explains what this method is supposed to do.
			Long first = testCaseIds.remove(0);
			List<Milestone> toCompare = testCaseDao.findById(first).getProject().getMilestones();

			for (Long testCaseId : testCaseIds) {
				List<Milestone> mil = testCaseDao.findById(testCaseId).getProject().getMilestones();

				if (mil.size() != toCompare.size() || !mil.containsAll(toCompare)) {
					return false;
				}
			}
		}

		return true;
	}

	@Override
	@PreAuthorize(WRITE_TC_OR_ROLE_ADMIN)
	public boolean changeAutomatable(TestCaseAutomatable automatable, Long testCaseId) {
		TestCase tc = testCaseDao.findById(testCaseId);
		if (tc.getProject().isAllowAutomationWorkflow()) {
			if (!automatable.equals(tc.getAutomatable())) {
				tc.setAutomatable(automatable);
			}

			if (automatable.equals(TestCaseAutomatable.Y) && tc.getAutomationRequest() == null) {
				createRequestForTestCase(testCaseId, null);
			}
		}
		reindexItpisReferencingTestCase(tc);
		indexationService.reindexTestCase(testCaseId);
		return tc.getProject().isAllowAutomationWorkflow();
	}

	@Override
	public Map<String, Object> transmitEligibleNodes(Map<String, List<Long>> selectedNodes) {
		Map<String, Object> result = new HashMap<>();

		List<Long> testCaseIds = selectedNodes.get("testcases");
		List<Long> folderIds = selectedNodes.get("folders");
		List<Long> libraryIds = selectedNodes.get("libraries");

		if (! libraryIds.isEmpty()) {
			List<TestCaseLibraryNode> rootLibraryNodes = new ArrayList<>();
			for (Long libraryId : libraryIds) {
				rootLibraryNodes.addAll(testCaseLibraryDao.findAllRootContentById(libraryId));
			}
			for (TestCaseLibraryNode node : rootLibraryNodes) {
				if ((TestCase.class).equals(node.getClass())) {
					testCaseIds.add(node.getId());
				} else {
					folderIds.add(node.getId());
				}
			}
		}
		if (! folderIds.isEmpty()) {
			testCaseIds.addAll(testCaseFolderDao.findAllTestCaseIdsFromFolderIds(folderIds));
		}

		Optional<Long> activeMilestoneId = activeMilestoneHolder.getActiveMilestoneId();
		if (activeMilestoneId.isPresent() && !NO_ACTIVE_MILESTONE_ID.equals(activeMilestoneId.get())) {
			testCaseIds = testCaseDao.findAllTCIdsForActiveMilestoneInList(activeMilestoneId.get(), testCaseIds);
		}
		List<Long> testCaseIdsWithLockedMilestone = findAllTCIdsWithLockedMilestone(testCaseIds);
		testCaseIds.removeAll(testCaseIdsWithLockedMilestone);

		List<Long> eligibleTestCaseIds = testCaseDao.findAllEligibleTestCaseIds(testCaseIds);
		if (! eligibleTestCaseIds.isEmpty()) {
			// Transmit all eligible test cases
			automationRequestModificationService.changeStatus(eligibleTestCaseIds, AutomationRequestStatus.TRANSMITTED);
		}
		boolean areAllEligible = eligibleTestCaseIds.size() == testCaseIds.size();
		result.put("areAllEligible", areAllEligible);
		result.put("eligibleTcIds", eligibleTestCaseIds);

		return result;
	}

/* *******************************************************
		private stuffs etc
	**********************************************************/

	private void traceResult(Collection<? extends Identified> collection, String qualifier){

		if (LOGGER.isTraceEnabled()){
			List<Long> ids = IdCollector.collect(collection);
			LOGGER.trace("found {} " + qualifier + ", ids are : {}", collection.size(), ids);
		}

	}


	// returns a tuple-2 with first element : project ID, second element : test name
	private Couple<Long, String> extractAutomatedProjectAndTestName(Long testCaseId, String testPath) {
		LOGGER.debug("extracting project and test name for automated test path '{}', in the context of test case #{}", testPath, testCaseId);

		// first we reject the operation if the script name is malformed
		if (!PathUtils.isPathWellFormed(testPath)) {
			LOGGER.error("automated test path '{}' is malformed !", testPath);
			throw new MalformedScriptPathException();
		}

		// now it's clear to go, let's find which TA project it is. The first slash must be removed because it doesn't
		// count.
		String path = testPath.replaceFirst("^/", "");
		int idxSlash = path.indexOf('/');

		String projectLabel = path.substring(0, idxSlash);
		String testName = path.substring(idxSlash + 1);

		TestCase tc = testCaseDao.findById(testCaseId);
		GenericProject tmproject = tc.getProject();

		Collection<TestAutomationProject> taProjects = tmproject.getTestAutomationProjects();

		if (LOGGER.isTraceEnabled()) {
			List<String> taProjectNames = taProjects.stream()
											  .map(TestAutomationProject::getJobName)
											  .collect(Collectors.toList());
			LOGGER.trace("available automation projects for test case #{} : {}", testCaseId, taProjectNames);
		}
		/*
		TestAutomationProject tap = (TestAutomationProject) CollectionUtils.find(tmproject.getTestAutomationProjects(),
			new HasSuchLabel(projectLabel));
		*/
		Optional<TestAutomationProject> tap = taProjects.stream().filter(taProj -> taProj.getLabel().equals(projectLabel)).findAny();

		// if the project couldn't be found we must also reject the operation
		if (! tap.isPresent()) {
			LOGGER.error("expected testautomation project '{}' but it appears that it doesn't belong to the TA projects within the scope of test case #{} ", projectLabel);
			throw new UnallowedTestAssociationException();
		}

		return new Couple<>(tap.get().getId(), testName);
	}


	private final class TestStepCustomFieldCopier implements TestStepVisitor {
		TestStep original;

		private TestStepCustomFieldCopier(TestStep original) {
			this.original = original;
		}

		@Override
		public void visit(ActionTestStep visited) {
			customFieldValuesService.copyCustomFieldValues((ActionTestStep) original, visited);
			Project origProject = original.getTestCase().getProject();
			Project newProject = visited.getTestCase().getProject();

			if (!origProject.equals(newProject)) {
				customFieldValuesService.migrateCustomFieldValues(visited);
			}
		}

		@Override
		public void visit(CallTestStep visited) {
			// NOPE
		}

	}

	private List<Long> findAllTCIdsWithLockedMilestone(List<Long> testCaseIds) {
		List<Long> result = new ArrayList<>();
		List<TestCase> testCases = testCaseDao.findAllByIds(testCaseIds);
		for (TestCase testCase : testCases) {
			for (Milestone milestone : testCase.getAllMilestones()) {
				if (MilestoneStatus.LOCKED.equals(milestone.getStatus())) {
					result.add(testCase.getId());
				}
			}
		}
		return result;
	}

	// ********************* Automation request *********************** */


	@Override
	@PreAuthorize(WRITE_TC_OR_ROLE_ADMIN)
	public void createRequestForTestCase(long testCaseId, AutomationRequestStatus automationRequestStatus) {
		TestCase testCase = testCaseDao.findById(testCaseId);
		Project project = testCase.getProject();

		AutomationRequest request = new AutomationRequest();
		testCase.setAutomationRequest(request);
		request.setTestCase(testCase);
		request.setProject(project);
		if(automationRequestStatus != null) {
			request.setRequestStatus(automationRequestStatus);
		}

		User currentUser = userAccountService.findCurrentUser();
		request.setCreatedBy(currentUser);

		requestDao.save(request);
		project.getAutomationRequestLibrary().addContent(request);
	}

	@Override
	@PreAuthorize(WRITE_TC_OR_ROLE_ADMIN)
	public void createRemoteRequestForTestCaseIfNotExist(long testCaseId) {
		TestCase testCase = testCaseDao.findById(testCaseId);
		AutomationRequest automationRequest = testCase.getAutomationRequest();
		// Create the remoteRequest only if does not exist yet
		if(automationRequest != null && automationRequest.getRemoteAutomationRequestExtender() == null) {
			RemoteAutomationRequestExtender remoteRequest = new RemoteAutomationRequestExtender();
			remoteRequest.setAutomationRequest(testCase.getAutomationRequest());
			testCase.getAutomationRequest().setRemoteAutomationRequestExtender(remoteRequest);
		}
	}

}
