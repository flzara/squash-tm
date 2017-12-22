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

import java.util.List;

import org.apache.commons.collections.MultiMap;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStep;



public interface ExecutionStepDao extends EntityDao<ExecutionStep> {
	int findExecutionStepRank(Long executionStepId);
	Execution findParentExecution(Long executionStepId);
	/**
	 * Look for {@link ExecutionStep}, to allow statistics computation. Return a {@link MultiMap}. Key : testStepIds, Value {@link ExecutionStep}
	 * @param testStepIds
	 * @param iterationIds The perimeter in sense of feat 5434
	 * @param iterationsIds 
	 * @return
	 */
	MultiMap findStepExecutionsStatus(List<Long> testCaseIds,List<Long> testStepIds);
}
