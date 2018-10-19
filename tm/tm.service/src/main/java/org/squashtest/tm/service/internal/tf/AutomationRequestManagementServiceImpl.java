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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.foundation.collection.ColumnFiltering;
import org.squashtest.tm.core.foundation.collection.SimpleColumnFiltering;
import org.squashtest.tm.domain.tf.automationrequest.AutomationRequest;
import org.squashtest.tm.service.internal.repository.AutomationRequestDao;
import org.squashtest.tm.service.security.UserContextService;
import org.squashtest.tm.service.tf.AutomationRequestFinderService;

import javax.inject.Inject;
import java.util.Optional;

@Service
@Transactional
public class AutomationRequestManagementServiceImpl implements AutomationRequestFinderService {

	@Inject
	private AutomationRequestDao requestDao;

	@Inject
	private UserContextService userCtxt;


	// *************** implementation of the finder interface *************************

	@Override
	@Transactional(readOnly = true)
	public AutomationRequest findRequestById(long id) {
		return requestDao.getOne(id);
	}

	@Override
	@Transactional(readOnly = true)
	public AutomationRequest findRequestByTestCaseId(long testCaseId) {
		return requestDao.findByTestCaseId(testCaseId);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<AutomationRequest> findRequests(Pageable pageable) {
		return requestDao.findAll(pageable);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<AutomationRequest> findRequests(Pageable pageable, ColumnFiltering filtering) {
		return requestDao.findAll(pageable, filtering);
	}

	@Override
	public Page<AutomationRequest> findRequestsAssignedToCurrentUser(Pageable pageable, ColumnFiltering filtering) {
		String username = userCtxt.getUsername();

		ColumnFiltering forcedFilter = new SimpleColumnFiltering(filtering)
										   	.addFilter("assignedTo.login", username);

		return requestDao.findAllForAssignee(username, pageable, filtering);
	}

	// *************** implementation of the management interface *************************




	// **************************** boiler plate code *************************************



}
