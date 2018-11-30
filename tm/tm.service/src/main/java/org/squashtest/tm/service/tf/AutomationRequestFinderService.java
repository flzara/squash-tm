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
package org.squashtest.tm.service.tf;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.squashtest.tm.core.foundation.collection.ColumnFiltering;
import org.squashtest.tm.domain.tf.automationrequest.AutomationRequest;

import java.util.List;
import java.util.Map;

/**
 * Service for retrieval of {@link org.squashtest.tm.domain.tf.automationrequest.AutomationRequest}.
 *
 */
public interface AutomationRequestFinderService {

	/**
	 * WYSIWYG
	 *
	 * @param requestId
	 * @return an AutomationRequest if found
	 * @throws javax.persistence.EntityNotFoundException if not
	 */
	AutomationRequest findRequestById(long requestId);

	/**
	 * Same as {@link #findRequestById(long)}, except we do so by using the
	 * related test case id. Also it won't throw any exception if not found,
	 * because test cases without automation requests are quite common : null
	 * will be returned instead.
	 *
	 * @param testCaseId
	 * @return the request if found, or null if not found.
	 */
	AutomationRequest findRequestByTestCaseId(long testCaseId);


	/**
	 * Given the specified pagination and sorting, retrieve the corresponding requests.
	 *
	 * @param pageable
	 * @return
	 */
	Page<AutomationRequest> findRequests(Pageable pageable);

	/**
	 * Given the specified pagination, sorting and filtering, retrieve the corresponding
	 * requests.
	 *
	 * @param pageable
	 * @param filtering
	 * @return
	 */
	Page<AutomationRequest> findRequests(Pageable pageable, ColumnFiltering filtering);

	/**
	 * Given the specified pagination, sorting and filtering, retrieve the corresponding
	 * requests, restricted to the automated requests assigned to the current user.
	 *
	 * @param pageable
	 * @param filtering
	 * @return
	 */
	Page<AutomationRequest> findRequestsAssignedToCurrentUser(Pageable pageable, ColumnFiltering filtering);

	/**
	 * Given the specified pagination, sorting and filtering, retrieve the corresponding
	 * requests, restricted to the automated requests with status TRANSMITTED
	 * @param pageble
	 * @param filtering
	 * @return
	 */
	Page<AutomationRequest> findRequestsWithTransmittedStatus(Pageable pageble, ColumnFiltering filtering);

	/**
	 * Given the specified pagination, sorting and filtering, retrieve the corresponding
	 * requests, restricted to the automated requests with these status : TRANSMITTED, WORK_IN_PROGRESS, EXECUTABLE
	 *
	 * @param pageable
	 * @param filtering
	 * @return
	 */
	Page<AutomationRequest> findRequestsForGlobal(Pageable pageable, ColumnFiltering filtering);

	/**
	 * Given the specified pagination, sorting and filtering, retrieve the corresponding
	 * requests, restricted to the automated requests with status : VALIDATE
	 *
	 * @param pageable
	 * @param filtering
	 * @return
	 */
	Page<AutomationRequest> findRequestsToTransmitted(Pageable pageable, ColumnFiltering filtering);

	/**
	 * Given the specified pagination, sorting and filtering, retrieve the corresponding
	 * requests, restricted to the automated requests with status : TO_VALID
	 *
	 * @param pageable
	 * @param filtering
	 * @return
	 */
	Page<AutomationRequest> findRequestsToValidate(Pageable pageable, ColumnFiltering filtering);

	/**
	 * Count the number of AutomationRequests assignee to current user.
	 * @return
	 */
	Integer countAutomationRequestForCurrentUser();

	/**
	 * Get users who last modified TC for current user.
	 * @param requestStatus
	 * @return
	 */
	Map<Long, String> getTcLastModifiedByForCurrentUser(List<String> requestStatus);

	/**
	 * Get users who last modified TC.
	 * @param requestStatus
	 * @return
	 */
	Map<Long, String> getTcLastModifiedByForAutomationRequests(List<String> requestStatus);

	Map<Long, String> getAssignedToForAutomationRequests();

	/**
	 * Count the number of AurtomationRequest with Valid status.
	 * @return
	 */
	Integer countAutomationRequestValid();

}
