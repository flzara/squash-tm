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
package org.squashtest.tm.service.execution;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.Paging;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStep;

@Transactional(readOnly = true)
public interface ExecutionFinder {
	Execution findById(long id);

	boolean exists(long id);

	List<ExecutionStep> findExecutionSteps(long executionId);

	ExecutionStep findExecutionStepById(long id);

	/**
	 * @param testCaseId
	 * @param paging
	 * @return
	 */
	List<Execution> findAllByTestCaseIdOrderByRunDate(long testCaseId, Paging paging);

	int findExecutionRank(Long executionId);

	/**
	 * Fetches all the executions which ran a given test case and matching the given paging and sorting instructions.
	 * 
	 * @param testCaseId
	 * @param pas
	 *            Paging and sorting data, should not be <code>null</code>
	 * @return a {@link PagedCollectionHolder} holding the results. Should never return <code>null</code>
	 */
	PagedCollectionHolder<List<Execution>> findAllByTestCaseId(long testCaseId, PagingAndSorting pas);
}
