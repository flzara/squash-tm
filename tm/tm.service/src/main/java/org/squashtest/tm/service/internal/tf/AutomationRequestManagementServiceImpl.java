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
package org.squashtest.tm.service.internal.tf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.foundation.collection.ColumnFiltering;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.testautomation.AutomatedTest;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseAutomatable;
import org.squashtest.tm.domain.tf.automationrequest.AutomationRequest;
import org.squashtest.tm.domain.tf.automationrequest.AutomationRequestStatus;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.exception.tf.IllegalAutomationRequestStatusException;
import org.squashtest.tm.service.campaign.IterationTestPlanFinder;
import org.squashtest.tm.service.internal.repository.AutomationRequestDao;
import org.squashtest.tm.service.internal.repository.IterationTestPlanDao;
import org.squashtest.tm.service.internal.repository.TestCaseDao;
import org.squashtest.tm.service.internal.repository.UserDao;
import org.squashtest.tm.service.internal.testautomation.UnsecuredAutomatedTestManagerService;
import org.squashtest.tm.service.internal.tf.event.AutomationRequestStatusChangeEvent;
import org.squashtest.tm.service.project.ProjectFinder;
import org.squashtest.tm.service.security.Authorizations;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.security.PermissionsUtils;
import org.squashtest.tm.service.security.UserContextService;
import org.squashtest.tm.service.testautomation.model.TestAutomationProjectContent;
import org.squashtest.tm.service.testcase.TestCaseModificationService;
import org.squashtest.tm.service.tf.AutomationRequestFinderService;
import org.squashtest.tm.service.tf.AutomationRequestModificationService;

import javax.inject.Inject;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.squashtest.tm.domain.tf.automationrequest.AutomationRequestStatus.AUTOMATED;
import static org.squashtest.tm.domain.tf.automationrequest.AutomationRequestStatus.AUTOMATION_IN_PROGRESS;
import static org.squashtest.tm.domain.tf.automationrequest.AutomationRequestStatus.READY_TO_TRANSMIT;
import static org.squashtest.tm.domain.tf.automationrequest.AutomationRequestStatus.REJECTED;
import static org.squashtest.tm.domain.tf.automationrequest.AutomationRequestStatus.SUSPENDED;
import static org.squashtest.tm.domain.tf.automationrequest.AutomationRequestStatus.TRANSMITTED;
import static org.squashtest.tm.domain.tf.automationrequest.AutomationRequestStatus.WORK_IN_PROGRESS;

@Service
@Transactional
public class AutomationRequestManagementServiceImpl implements AutomationRequestFinderService, AutomationRequestModificationService {

	private static final String CAN_READ_REQUEST_OR_ADMIN = "hasPermission(#requestId, 'org.squashtest.tm.domain.tf.automationrequest.AutomationRequest', 'READ') " + Authorizations.OR_HAS_ROLE_ADMIN;

	private static final String CAN_READ_TESTCASE_OR_ADMIN = "hasPermission(#testCaseId, 'org.squashtest.tm.domain.testcase.TestCase' , 'READ')" + Authorizations.OR_HAS_ROLE_ADMIN;

	private static final String STATUS_NOT_PERMITTED = "Unknown status";

	private static final String WRITE_AS_FUNCTIONAL = "WRITE_AS_FUNCTIONAL";

	private static final String WRITE_AS_AUTOMATION = "WRITE_AS_AUTOMATION";

	private static final Logger LOGGER = LoggerFactory.getLogger(AutomationRequestManagementServiceImpl.class);

	@Inject
	private AutomationRequestDao requestDao;

	@Inject
	private UserDao userDao;

	@Inject
	private UserContextService userCtxt;

	@Inject
	private ProjectFinder projectFinder;

	@Inject
	private PermissionEvaluationService permissionEvaluationService;

	@Inject
	private ApplicationEventPublisher eventPublisher;

	@Inject
	private IterationTestPlanFinder iterationTestPlanFinder;

	@Inject
	private TestCaseDao testCaseDao;

	@Inject
	private TestCaseModificationService testCaseModificationService;

	@Inject
	private UnsecuredAutomatedTestManagerService taService;

	@Inject
	private IterationTestPlanDao iterationTestPlanDao;

	// *************** implementation of the finder interface *************************

	@Override
	@Transactional(readOnly = true)
	@PreAuthorize(CAN_READ_REQUEST_OR_ADMIN)
	public AutomationRequest findRequestById(long requestId) {
		return requestDao.getOne(requestId);
	}

	@Override
	@Transactional(readOnly = true)
	@PreAuthorize(CAN_READ_TESTCASE_OR_ADMIN)
	public AutomationRequest findRequestByTestCaseId(long testCaseId) {
		return requestDao.findByTestCaseId(testCaseId);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<AutomationRequest> findRequests(Pageable pageable) {
		List<Long> projectIds = projectFinder.findAllReadableIds();
		return requestDao.findAll(pageable, projectIds);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<AutomationRequest> findRequests(Pageable pageable, ColumnFiltering filtering) {
		List<Long> projectIds = projectFinder.findAllReadableIds();
		return requestDao.findAll(pageable, filtering, projectIds);
	}

	@Override
	public Page<AutomationRequest> findRequestsAssignedToCurrentUser(Pageable pageable, ColumnFiltering filtering) {
		List<Long> projectIds = projectFinder.findAllReadableIdsForAutomationWriter();
		String username = userCtxt.getUsername();
		return requestDao.findAllForAssignee(username, pageable, filtering, projectIds);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<AutomationRequest> findRequestsWithTransmittedStatus(Pageable pageble, ColumnFiltering filtering) {
		List<Long> projectIds = projectFinder.findAllReadableIdsForAutomationWriter();
		return requestDao.findAllTransmitted(pageble, filtering, projectIds);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<AutomationRequest> findRequestsForGlobal(Pageable pageable, ColumnFiltering filtering) {
		List<Long> projectIds = projectFinder.findAllReadableIdsForAutomationWriter();
		return requestDao.findAllForGlobal(pageable, filtering, projectIds);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<AutomationRequest> findRequestsToTransmitted(Pageable pageable, ColumnFiltering filtering) {
		List<Long> projectIds = projectFinder.findAllReadableIds();
		return requestDao.findAllValid(pageable, filtering, projectIds);
	}

	@Override
	public Page<AutomationRequest> findRequestsToValidate(Pageable pageable, ColumnFiltering filtering) {
		List<Long> projectIds = projectFinder.findAllReadableIds();
		return requestDao.findAllToValidate(pageable, filtering, projectIds);
	}

	@Override
	public Map<Long, String> getTcLastModifiedByToAutomationRequestNotAssigned(List<String> requestStatus) {
		List<Long> projectIds = projectFinder.findAllReadableIds();
		return requestDao.getTcLastModifiedByToAutomationRequestNotAssigned(requestStatus, projectIds);
	}

	// *************** implementation of the management interface *************************


	@Override
	@PreAuthorize(Authorizations.HAS_ROLE_ADMIN)
	public void deleteRequestByProjectId(long projectId) {
		requestDao.batchDeleteByProjectId(projectId);
	}

	@Override
	public void unassignRequests(List<Long> tcIds) {
		List<Long> requestIds = requestDao.getReqIdsByTcIds(tcIds);
		PermissionsUtils.checkPermission(permissionEvaluationService, requestIds, WRITE_AS_AUTOMATION, AutomationRequest.class.getName());
		requestDao.unassignRequests(requestIds);
	}

	@Override
	@Transactional(readOnly = true)
	public Map<Long, String> getTcLastModifiedByForCurrentUser(List<String> requestStatus) {
		String userName = userCtxt.getUsername();
		List<Long> projectIds = projectFinder.findAllReadableIds();
		return requestDao.getTransmittedByForCurrentUser(userDao.findUserByLogin(userName).getId(), requestStatus, projectIds);
	}

	@Override
	@Transactional(readOnly = true)
	public Map<Long, String> getTcLastModifiedByForAutomationRequests(List<String> requestStatus) {
		List<Long> projectIds = projectFinder.findAllReadableIds();
		return requestDao.getTransmittedByForCurrentUser(null, requestStatus, projectIds);
	}

	@Override
	@Transactional(readOnly = true)
	public Map<Long, String> getAssignedToForAutomationRequests() {
		List<Long> projectIds = projectFinder.findAllReadableIds();
		return requestDao.getAssignedToForAutomationRequests(projectIds);
	}

	@Override
	public Integer countAutomationRequestForCurrentUser() {
		String userName = userCtxt.getUsername();
		return requestDao.countAutomationRequestForCurrentUser(userDao.findUserByLogin(userName).getId());
	}

	@Override
	public void assignedToRequest(List<Long> tcIds) {
		String username = userCtxt.getUsername();
		User user = userDao.findUserByLogin(username);
		List<Long> requestIds = requestDao.getReqIdsByTcIds(tcIds);
		PermissionsUtils.checkPermission(permissionEvaluationService, requestIds, WRITE_AS_AUTOMATION, AutomationRequest.class.getName());
		requestDao.assignedToRequestIds(requestIds, user);
	}

	@Override
	public void changeStatus(List<Long> tcIds, AutomationRequestStatus automationRequestStatus) {
		String username = userCtxt.getUsername();
		User user = userDao.findUserByLogin(username);
		List<Long> reqIds = requestDao.getReqIdsByTcIds(tcIds);
		switch (automationRequestStatus) {
			case REJECTED:
				PermissionsUtils.checkPermission(permissionEvaluationService, reqIds, WRITE_AS_AUTOMATION, AutomationRequest.class.getName());
				requestDao.updateAutomationRequestStatus(reqIds, REJECTED, Arrays.asList(REJECTED, AUTOMATION_IN_PROGRESS, TRANSMITTED, AUTOMATED));
				break;
			case AUTOMATION_IN_PROGRESS:
				PermissionsUtils.checkPermission(permissionEvaluationService, reqIds, WRITE_AS_AUTOMATION, AutomationRequest.class.getName());
				requestDao.updateAutomationRequestStatus(reqIds, AUTOMATION_IN_PROGRESS, Arrays.asList(AUTOMATION_IN_PROGRESS, AUTOMATED, TRANSMITTED, REJECTED));
				break;
			case AUTOMATED:
				PermissionsUtils.checkPermission(permissionEvaluationService, reqIds, WRITE_AS_AUTOMATION, AutomationRequest.class.getName());
				requestDao.updateStatusToAutomated(reqIds, AUTOMATED, Arrays.asList(TRANSMITTED, AUTOMATION_IN_PROGRESS, AUTOMATED));
				break;
			case TRANSMITTED:
				PermissionsUtils.checkPermission(permissionEvaluationService, reqIds, WRITE_AS_FUNCTIONAL, AutomationRequest.class.getName());
				requestDao.updateStatusToTransmitted(reqIds, user);
				break;
			case WORK_IN_PROGRESS:
				PermissionsUtils.checkPermission(permissionEvaluationService, reqIds, WRITE_AS_FUNCTIONAL, AutomationRequest.class.getName());
				requestDao.updateAutomationRequestStatus(reqIds, WORK_IN_PROGRESS, Arrays.asList(AutomationRequestStatus.values()));
				break;
			case SUSPENDED:
				PermissionsUtils.checkPermission(permissionEvaluationService, reqIds, WRITE_AS_FUNCTIONAL, AutomationRequest.class.getName());
				requestDao.updateAutomationRequestStatus(reqIds, SUSPENDED, Arrays.asList(AutomationRequestStatus.values()));
				break;
			case READY_TO_TRANSMIT:
				PermissionsUtils.checkPermission(permissionEvaluationService, reqIds, WRITE_AS_FUNCTIONAL, AutomationRequest.class.getName());
				requestDao.updateAutomationRequestStatus(reqIds, READY_TO_TRANSMIT, Arrays.asList(AutomationRequestStatus.values()));
				break;
			default:
				throw new IllegalAutomationRequestStatusException(STATUS_NOT_PERMITTED);
		}

		eventPublisher.publishEvent(new AutomationRequestStatusChangeEvent(reqIds, automationRequestStatus));
	}

	@Override
	public void changePriority(List<Long> tcIds, Integer priority) {
		List<Long> reqIds = requestDao.getReqIdsByTcIds(tcIds);
		PermissionsUtils.checkPermission(permissionEvaluationService, reqIds, WRITE_AS_FUNCTIONAL, AutomationRequest.class.getName());
		requestDao.updatePriority(tcIds, priority);
	}

	@Override
	public Integer countAutomationRequestValid() {
		List<Long> projectIds = projectFinder.findAllReadableIds();
		return requestDao.countAutomationRequestValid(projectIds);
	}

	// **************************** TA script auto association section *************************************

	@Override
	public Map<Long, String> updateTAScript(List<Long> tcIds) {
		LOGGER.debug("Update TA script of the following test cases: {}", tcIds.toString());

		Map<Long, String> noTAScriptTestCases = new HashMap<>();

		// 1 - We fetch all the test cases from DB (with project and AutomationProject list)
		List<TestCase> testCases = testCaseDao.findAllByIdsWithProject(tcIds);

		// 2 - We extract all test automation projects for the given TM projects
		List<TestAutomationProject> testAutomationProjects = testCases.stream()
			.flatMap(tc -> tc.getProject().getTestAutomationProjects().stream())
			// 3 - We filter them to have one element only with the same combination remote server/jobName. This in order to make the minimum call to the automation server in step 4.
			.filter(distinctByKey(tap -> Arrays.asList(tap.getServer().getId(), tap.getJobName())))
			.collect(Collectors.toList());

		// 4 - We retrieve the TA scripts from the TA jobs
		Collection<TestAutomationProjectContent> taProjectContents = taService.listTestsFromRemoteServers(testAutomationProjects);

		testCases.forEach(tc -> {
			// if block to update only automatable test cases in a project with automation workflow allowed.
			if(tc.getProject().isAllowAutomationWorkflow()
				&& TestCaseAutomatable.Y.equals(tc.getAutomatable())) {
				// We extract the TestAutomationProjectContent relevant for the current test case
				List<TestAutomationProjectContent> testAutomationProjectContentsFilteredByTestCaseAutomationServer =
					taProjectContents.stream().filter(tapc -> tapc.getProject().getServer().getId().equals(tc.getProject().getTestAutomationServer().getId())).collect(Collectors.toList());

				// Finally we do the TA script association
				doTAScriptAssignation(tc, testAutomationProjectContentsFilteredByTestCaseAutomationServer, noTAScriptTestCases);
			} else {
				throw new IllegalArgumentException();
			}
		});

		return noTAScriptTestCases;
	}

	// Method allowed to retrieve distinct element from a list based on multiple attributes.
	// Thank you stack overflow: https://stackoverflow.com/questions/48165456/retrieve-distinct-element-based-on-multiple-attributes-of-java-object-using-java
	private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
		Set<Object> seen = ConcurrentHashMap.newKeySet();
		return t -> seen.add(keyExtractor.apply(t));
	}

	private void doTAScriptAssignation(TestCase testCase, Collection<TestAutomationProjectContent> testAutomationProjectContents, Map<Long, String> noTAScriptTestCases){
		List<AutomatedTest> assignableAutomatedTestList = extractAssignableAutomatedTestList(testCase, testAutomationProjectContents);

		if(assignableAutomatedTestList.size() > 0){
			if(assignableAutomatedTestList.size() == 1){
				addOrEditAutomatedScript(testCase,assignableAutomatedTestList.get(0));
			}
			else {
				manageConflictAssociation(testCase, assignableAutomatedTestList, noTAScriptTestCases);
			}
		} else {
			manageNoScript(testCase, noTAScriptTestCases);
		}
	}

	private List<AutomatedTest> extractAssignableAutomatedTestList (TestCase testCase, Collection<TestAutomationProjectContent> testAutomationProjectContents) {
		List<AutomatedTest> assignableAutomatedTestList = testAutomationProjectContents.stream()
			.map(TestAutomationProjectContent::getTests)
			.flatMap(Collection::stream)
			.filter(automatedTest -> automatedTest.getLinkedTC().contains(testCase.getUuid()))
			.collect(Collectors.toList());

		return assignableAutomatedTestList;
	}

	private void manageConflictAssociation(TestCase tc, List<AutomatedTest> automatedTestList, Map<Long, String> losingTAScriptTestCases){
		LOGGER.debug("Conflict of TA Script association detected for test case {}", tc.getId().toString());
			requestDao.updateIsManual(tc.getId(), false);

			if (tc.getAutomatedTest() != null) {
				testCaseModificationService.removeAutomation(tc.getId());

			}
			losingTAScriptTestCases.put(tc.getId(), tc.getName());
			StringJoiner stringJoiner = new StringJoiner("#");
			automatedTestList.stream().map(AutomatedTest::getFullName).forEach(stringJoiner::add);

			requestDao.updateConflictAssociation(tc.getId(), stringJoiner.toString());

	}

	private void addOrEditAutomatedScript(TestCase tc, AutomatedTest automatedTest){
		LOGGER.debug("Add TA Script {} to test case {}", automatedTest.getName(), tc.getId().toString());

		// Because we made the minimum call to automation server in updateTAScript method, the AutomatedTest in argument is not necessarily linked to the test case's AutomationProject.
		// Hence the stream on the list of TestAutomationProject of the test case's tm project.
		TestAutomationProject trueAutomationProject = tc.getProject().getTestAutomationProjects().stream().filter(tap -> tap.getJobName().equals(automatedTest.getProject().getJobName())).findFirst().get();

		requestDao.updateIsManual(tc.getId(), false);

		if(tc.getAutomationRequest().getConflictAssociation()!=null && !tc.getAutomationRequest().getConflictAssociation().isEmpty()){
			requestDao.updateConflictAssociation(tc.getId(), "");
		}

		testCaseModificationService.bindAutomatedTestAutomatically(tc.getId(), trueAutomationProject.getId(), automatedTest.getName());
	}

	private void manageNoScript(TestCase tc, Map<Long, String> noTAScriptTestCases){
		LOGGER.debug("No TA script associated with test case {}", tc.getId().toString());
		if(!tc.getAutomationRequest().isManual()){
			if (tc.getAutomatedTest()!=null ){
				testCaseModificationService.removeAutomation(tc.getId());
			}else if(tc.getAutomationRequest().getConflictAssociation()!=null && !tc.getAutomationRequest().getConflictAssociation().isEmpty()){
				requestDao.updateConflictAssociation(tc.getId(), "");
			}
			requestDao.updateIsManual(tc.getId(), false);
			noTAScriptTestCases.put(tc.getId(), tc.getName());
		} else if(tc.getAutomatedTest() == null){
			noTAScriptTestCases.put(tc.getId(), tc.getName());
		}
	}

	/*TM-13:update automatic script before execution */
	@Override
	public Map<Long, String> updateTAScriptForIteration(Long iterationId) {
		LOGGER.debug("Update TA script for following iteration's ITPI: {}", iterationId.toString());
		Map<Long, String> result = new HashMap<>();

		List<IterationTestPlanItem> items = iterationTestPlanDao.findAllByIterationIdWithTCAutomated(iterationId);
		if (!items.isEmpty()){
			result = doItpiTAScriptUpdate(items);
		}

		return result;
	}

	@Override
	public Map<Long, String> updateTAScriptForTestSuite(Long testSuiteId) {
		LOGGER.debug("Update TA script for following test suite's ITPI: {}", testSuiteId.toString());
		Map<Long, String> result = new HashMap<>();

		List<IterationTestPlanItem> items = iterationTestPlanDao.findAllByTestSuiteIdWithTCAutomated(testSuiteId);
		if (!items.isEmpty()){
			result = doItpiTAScriptUpdate(items);
		}

		return result;
	}

	@Override
	public Map<Long, String> updateTAScriptForItems(List<Long> testPlanIds) {
		LOGGER.debug("Update TA script of the following ITPI: {}", testPlanIds.toString());
		Map<Long, String> result = new HashMap<>();
		List<IterationTestPlanItem> items = iterationTestPlanDao.findAllByItemsIdWithTCAutomated(testPlanIds);
		if (!items.isEmpty()){
			result = doItpiTAScriptUpdate(items);
		}
		return result;
	}

	private Map<Long, String> doItpiTAScriptUpdate(List<IterationTestPlanItem> itpisToUpdate) {
		List<Long> tcIds = getListTcIdsFromListItems(itpisToUpdate);

		Map<Long, String> mapTcIdTcNameInConflict = updateTAScript(tcIds);
		return getListItpiIdNameTc(itpisToUpdate, mapTcIdTcNameInConflict);
	}

	private Map<Long, String>  getListItpiIdNameTc(List<IterationTestPlanItem> items, Map<Long, String> mapTcIdTcNameInConflict){
		Map<Long, String> mapItpiIdTcNameInConflict = new HashMap<>();
		items.forEach(itpi->{
			if(mapTcIdTcNameInConflict.containsKey(itpi.getReferencedTestCase().getId())){
				mapItpiIdTcNameInConflict.put(itpi.getId(),mapTcIdTcNameInConflict.get(itpi.getReferencedTestCase().getId()));
			}
		});
		return  mapItpiIdTcNameInConflict;
	}

	private List<Long> getListTcIdsFromListItems(List<IterationTestPlanItem> items){
		List<Long> listIds = items.stream()
			.map(itpi -> itpi.getReferencedTestCase().getId()).collect(Collectors.toList());
		return  listIds;
	}

	// **************************** boiler plate code *************************************

}
