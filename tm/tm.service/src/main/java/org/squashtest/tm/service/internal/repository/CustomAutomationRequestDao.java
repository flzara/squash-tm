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
package org.squashtest.tm.service.internal.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.squashtest.tm.core.foundation.collection.ColumnFiltering;
import org.squashtest.tm.domain.tf.automationrequest.AutomationRequest;
import org.squashtest.tm.domain.users.User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface CustomAutomationRequestDao {


	/**
	 * Will retrieve a list of automated requests, paged and sorted.
	 *
	 * @param pageable
	 * @param inProjectIds list of project ids the current user can read
	 * @return
	 */
	Page<AutomationRequest> findAll(Pageable pageable, Collection<Long> inProjectIds);

	/**
	 * Will retrieve a list of automated requests, paged filtered and sorted.
	 *
	 * @param pageable
	 * @param filtering
	 * @param inProjectIds list of project ids the current user can read
	 * @return
	 */
	Page<AutomationRequest> findAll(Pageable pageable, ColumnFiltering filtering, Collection<Long> inProjectIds);


	/**
	 * Retrieve a list of automated requests, paged filtered and sorted, where the
	 * user refered to by its username is the assignee (will force equality on the
	 * assignee username instead of using 'like').
	 *
	 * @param username
	 * @param pageable
	 * @param filtering
	 * @param inProjectIds list of project ids the current user can read
	 * @return
	 */
	Page<AutomationRequest> findAllForAssignee(String username, Pageable pageable, ColumnFiltering filtering, Collection<Long> inProjectIds);

	/**
	 * Retrieve a list of automated requests, paged filtered and sorted, where
	 * the status of automation request is Transmitted.
	 *
	 * @param pageable
	 * @param columnFiltering
	 * @param inProjectIds list of project ids the current user can read
	 * @return
	 */
	Page<AutomationRequest> findAllForTraitment(Pageable pageable, ColumnFiltering columnFiltering, Collection<Long> inProjectIds);

	/**
	 * Will retrieve a list of automated requests, paged filtered and sorted, where
	 * the status of automation request is 'TRANSMITTED', 'WORK_IN_PROGRESS' or 'EXECUTABLE'
	 * @param pageable
	 * @param columnFiltering
	 * @param inProjectIds list of project ids the current user can read
	 * @return
	 */
	Page<AutomationRequest> findAllForGlobal(Pageable pageable, ColumnFiltering filtering, Collection<Long> inProjectIds);

	/**
	 * Will retrieve a list of automated requests, paged filtered and sorted, where
	 * the status of automation request is 'VALID'
	 * @param pageable
	 * @param filtering
	 * @param inProjectIds list of project ids the current user can read
	 * @return
	 */
	Page<AutomationRequest> findAllValid(Pageable pageable, ColumnFiltering filtering, Collection<Long> inProjectIds);

	/**
	 * Will retrieve a list of automated requests, paged filtered and sorted, where
	 * the status of automation request is 'TO_VALIDATE'
	 * @param pageable
	 * @param filtering
	 * @param inProjectIds list of project ids the current user can read
	 * @return
	 */
	Page<AutomationRequest> findAllToValidate(Pageable pageable, ColumnFiltering filtering, Collection<Long> inProjectIds);

	/**
	 * Count Automation request to the current User.
	 * @param idUser
	 * @return
	 */
	Integer countAutomationRequestForCurrentUser(Long idUser);

	/**
	 * Retrieve all the 'transmitted by' users login mapped by id, filtered by automation request status
	 * if idUser is not null, we add the 'assigned to' condition to the request
	 * @param idUser
	 * @param requestStatus
	 * @return
	 */
	Map<Long, String> getTransmittedByForCurrentUser(Long idUser, List<String> requestStatus);

	Map<Long, String> getAssignedToForAutomationRequests();

	void updateAutomationRequestToAssigned(User user, List<Long> reqIds);

	void updateAutomationRequestNotAutomatable(List<Long> reqIds);

	void unassignedUser(List<Long> reqIds);

	void updateStatusToExecutable(List<Long> reqIds);

	void updatePriority(List<Long> tcIds, Integer priority);

	List<Long> getReqIdsByTcIds(List<Long> tcIds);

	void updateStatusToTransmitted(List<Long> reqIds, User transmittedBy);

	void updateStatusToValidate(List<Long> reqIds);

	void updateStatusToValide(List<Long> reqIds);

	void updateStatusToObsolete(List<Long> reqIds);

	Integer countAutomationRequestValid();

}
