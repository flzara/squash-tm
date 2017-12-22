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
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.execution.ExecutionStep;
import org.squashtest.tm.service.deletion.SuppressionPreviewReport;

@Transactional(readOnly = false)
public interface ExecutionModificationService extends ExecutionFinder {

	Execution findAndInitExecution(Long executionId);

	// XXX : should be 'setExecutionComment' because that is how it appear in the interface
	void setExecutionDescription(Long executionId, String description);

	void setExecutionStatus(Long executionId, ExecutionStatus status);

	/*********************************** Steps methods *****************************************/

	PagedCollectionHolder<List<ExecutionStep>> findExecutionSteps(long executionId, Paging filter);

	void setExecutionStepComment(Long executionStepId, String comment);

	/**
	 * that method should investigate the consequences of the deletion of the given executions, and return a report
	 * about what will happen.
	 *
	 * @param targetIds
	 * @return
	 */
	List<SuppressionPreviewReport> simulateExecutionDeletion(Long execId);

	/**
	 * that method should delete the execution. It still takes care of non deletable executions so the implementation
	 * should abort if the execution can't be deleted.
	 *
	 *
	 * @param targetIds
	 * @throws RuntimeException
	 *             if the execution should not be deleted.
	 */
	void deleteExecution(Execution execution);

	/**
	 *
	 * @param executionId
	 *            the execution to be updated
	 * @return the index of the first modified (not deleted) step in the new execution or -1
	 */
	long updateSteps(long executionId);

}
