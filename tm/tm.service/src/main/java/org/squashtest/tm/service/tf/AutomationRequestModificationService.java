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

import org.squashtest.tm.domain.tf.automationrequest.AutomationRequestStatus;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface AutomationRequestModificationService extends AutomationRequestFinderService{

	void deleteRequestByProjectId(long projectId);

	/**
	 * Will unassign the automation requests, identified by their ids, from the user that handle them.
	 * @param tcIds
	 */
	void unassignRequests(List<Long> tcIds);

	void changeStatus(List<Long> tcIds, AutomationRequestStatus automationRequestStatus);

	void changePriority(List<Long> tcIds, Integer priority);

	void assignedToRequest(List<Long> tcIds);

	/**
	 * Given a list of test case's id, will try to update value of automation script for each test case
	 * @param tcIds : a list of test case ids
	 */
	Set<Long> updateTAScript(List<Long> tcIds);

}
