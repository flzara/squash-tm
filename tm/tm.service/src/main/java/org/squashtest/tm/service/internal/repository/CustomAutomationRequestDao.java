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

import java.util.Collection;

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

}
