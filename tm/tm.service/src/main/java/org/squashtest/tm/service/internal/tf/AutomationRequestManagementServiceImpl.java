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

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.foundation.collection.ColumnFiltering;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.tf.automationrequest.AutomationRequest;
import org.squashtest.tm.domain.tf.automationrequest.AutomationRequestStatus;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.exception.tf.IllegalAutomationRequestStatusException;
import org.squashtest.tm.service.advancedsearch.IndexationService;
import org.squashtest.tm.service.campaign.IterationTestPlanFinder;
import org.squashtest.tm.service.internal.repository.AutomationRequestDao;
import org.squashtest.tm.service.internal.repository.TestCaseDao;
import org.squashtest.tm.service.internal.repository.UserDao;
import org.squashtest.tm.service.internal.tf.event.AutomationRequestStatusChangeEvent;
import org.squashtest.tm.service.project.ProjectFinder;
import org.squashtest.tm.service.security.Authorizations;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.security.PermissionsUtils;
import org.squashtest.tm.service.security.UserContextService;
import org.squashtest.tm.service.tf.AutomationRequestFinderService;
import org.squashtest.tm.service.tf.AutomationRequestModificationService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
	private IndexationService indexationService;

	@Inject
	private IterationTestPlanFinder iterationTestPlanFinder;

	@Inject
	TestCaseDao testCaseDao;


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
		List<TestCase> listTestCases = testCaseDao.findAllByIds(tcIds);
		for (TestCase tc: listTestCases) {
			eventPublisher.publishEvent(new AutomationRequestStatusChangeEvent(reqIds,automationRequestStatus, tc.getProject().getAutomationWorkflowType()));
		}

		indexationService.batchReindexAutomationRequest(reqIds);
		indexationService.batchReindexTc(tcIds);
		List<TestCase> testCases = testCaseDao.findAllByIds(tcIds);
		for (TestCase testCase: testCases) {
			reindexItpisReferencingTestCase(testCase);
		}
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

	// **************************** boiler plate code *************************************

	private void reindexItpisReferencingTestCase(TestCase testCase) {
		List<IterationTestPlanItem> itpis = iterationTestPlanFinder.findByReferencedTestCase(testCase);
		List<Long> itpiIds = new ArrayList();
		for (IterationTestPlanItem itpi : itpis) {
			itpiIds.add(itpi.getId());
		}
		indexationService.batchReindexItpi(itpiIds);
	}

}
