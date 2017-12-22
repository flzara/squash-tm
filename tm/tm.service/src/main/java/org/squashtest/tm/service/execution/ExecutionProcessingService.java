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
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.execution.ExecutionStatusReport;
import org.squashtest.tm.domain.execution.ExecutionStep;
import org.squashtest.tm.domain.testautomation.AutomatedExecutionExtender;
import org.squashtest.tm.exception.execution.ExecutionHasNoStepsException;


public interface ExecutionProcessingService {

	Execution findExecution(Long executionId);

	ExecutionStep findExecutionStep(Long executionStepId);

	/**
	 * Tells whether the execution is fresh new or not. Namely, that all its steps have a status
	 * READY.
	 *
	 * @param executionId
	 * @return
	 */
	boolean wasNeverRun(Long executionId);

	/**
	 *
	 * @param executionId
	 * @return the first occurence of a running or ready ExecutionStep
	 */
	@Transactional(readOnly = true)
	ExecutionStep findRunnableExecutionStep(long executionId) throws ExecutionHasNoStepsException;

	List<ExecutionStep> getExecutionSteps(Long executionId);

	/**
	 * Returns, for a given execution and for a given step index, the corresponding ExecutionStep Will create the next
	 * step if the index corresponds to the one immediately following the last step, similarly to "nextExecutionStep"
	 *
	 * @param executionId
	 * @param executionStepRank
	 * @return
	 */
	@Transactional(readOnly = true)
	ExecutionStep findStepAt(long executionId, int executionStepIndex);

	/***
	 * Method which modify the execution step status<br>
	 * It implies :<br>
	 * * execution status update<br>
	 * * item test plan status update<br>
	 * * last execution date and user update for step, execution and item test plan<br>
	 *
	 * @param executionStepId
	 *            the step id
	 * @param status
	 *            the new status
	 */
	void changeExecutionStepStatus(Long executionStepId, ExecutionStatus status);

	void setExecutionStatus(Long executionId, ExecutionStatus status);

	void setExecutionStatus(Long executionId, ExecutionStatusReport report);

	ExecutionStatusReport getExecutionStatusReport(Long executionId);

	void setExecutionStepComment(Long executionStepId, String comment);

	int findExecutionStepRank(Long executionStepId);

	int findTotalNumberSteps(Long executionId);

	void updateStepExecutionData(ExecutionStep executionStep);
	/***
	 * Asks an execution to update it's metadata (lastExecutionOn, lastExecutedBy)
	 * according to regular execution business rules.
	 *
	 * @param execution
	 *            the execution to update
	 */
	void updateExecutionMetadata(Execution execution);

	/***
	 * Asks an execution to update it's metadata (lastExecutionOn, lastExecutedBy)
	 * according to automated execution business rules. The said execution is the
	 * one referenced by the given extender.
	 *
	 * @param execution
	 *            the execution to update
	 */
	void updateExecutionMetadata(AutomatedExecutionExtender extender);

}
