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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.foundation.collection.ColumnFiltering;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.tf.automationrequest.AutomationRequest;
import org.squashtest.tm.domain.tf.automationrequest.AutomationRequestStatus;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.service.internal.repository.AutomationRequestDao;
import org.squashtest.tm.service.internal.repository.UserDao;
import org.squashtest.tm.service.project.ProjectFinder;
import org.squashtest.tm.service.security.Authorizations;
import org.squashtest.tm.service.security.UserContextService;
import org.squashtest.tm.service.tf.AutomationRequestFinderService;
import org.squashtest.tm.service.tf.AutomationRequestModificationService;

import javax.inject.Inject;
import java.util.*;

@Service
@Transactional
public class AutomationRequestManagementServiceImpl implements AutomationRequestFinderService, AutomationRequestModificationService {

	private static final String CAN_READ_REQUEST_OR_ADMIN = "hasPermission(#requestId, 'org.squashtest.tm.domain.tf.automationrequest.AutomationRequest', 'READ') " + Authorizations.OR_HAS_ROLE_ADMIN;

	private static final String CAN_READ_TESTCASE_OR_ADMIN = "hasPermission(#testCaseId, 'org.squashtest.tm.domain.testcase.TestCase' , 'READ')" + Authorizations.OR_HAS_ROLE_ADMIN;

	@Inject
	private AutomationRequestDao requestDao;

	@Inject
	private UserDao userDao;

	@Inject
	private UserContextService userCtxt;

	@Inject
	private ProjectFinder projectFinder;


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
		List<Long> projectIds = projectFinder.findAllReadableIds();
		String username = userCtxt.getUsername();
		return requestDao.findAllForAssignee(username, pageable, filtering, projectIds);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<AutomationRequest> findRequestsWithTransmittedStatus(Pageable pageble, ColumnFiltering filtering) {
		List<Long> projectIds = projectFinder.findAllReadableIds();
		return requestDao.findAllForTraitment(pageble, filtering, projectIds);
	}

	@Override
	public Page<AutomationRequest> findRequestsForGlobal(Pageable pageable, ColumnFiltering filtering) {
		List<Long> projectIds = projectFinder.findAllReadableIds();
		return requestDao.findAllForGlobal(pageable, filtering, projectIds);
	}


	// *************** implementation of the management interface *************************


	@Override
	@PreAuthorize(Authorizations.HAS_ROLE_ADMIN)
	public void deleteRequestByProjectId(long projectId) {
		requestDao.batchDeleteByProjectId(projectId);
	}

	@Override
	public void unassignedUser(List<Long> requestIds) {
		requestDao.unassignedUser(requestIds);
	}

	@Override
	public Map<Long, String> getCreatedByForCurrentUser(List<String> requestStatus) {
		String userName = userCtxt.getUsername();
		return requestDao.getTransmittedByForCurrentUser(userDao.findUserByLogin(userName).getId(), requestStatus);
	}

	@Override
	public Map<Long, String> getCreatedByForAutomationRequests(List<String> requestStatus) {
		return requestDao.getTransmittedByForCurrentUser(null, requestStatus);
	}

	@Override
	public List<User> getAssignedToForAutomationRequests() {
		return requestDao.getAssignedToForAutomationRequests();
	}

	@Override
	public Integer countAutomationRequestForCurrentUser() {

		String userName = userCtxt.getUsername();
		return requestDao.countAutomationRequestForCurrentUser(userDao.findUserByLogin(userName).getId());
	}

	@Override
	public void changeStatus(List<Long> reqIds, AutomationRequestStatus automationRequestStatus) {
		String username = userCtxt.getUsername();
		User user = userDao.findUserByLogin(username);
		switch (automationRequestStatus) {
			case NOT_AUTOMATABLE:
				requestDao.updateAutomationRequestNotAutomatable(reqIds);
				break;
			case WORK_IN_PROGRESS:
				requestDao.updateAutomationRequestToAssigned(user, reqIds);
				break;
			case EXECUTABLE:
				requestDao.updateStatusToExecutable(reqIds);
				break;
				default:
					break;
		}
	}

	@Override
	public void changePriority(List<Long> reqIds, Integer priority) {
		requestDao.updatePriority(reqIds, priority);
	}

	// **************************** boiler plate code *************************************



}
