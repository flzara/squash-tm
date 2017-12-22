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
/**
 * This file is part of the Squash TM management services for SaaS / Squash On Demand (saas.management.fragment) project.
 * Copyright (C) 2015 - 2016 Henix, henix.fr - All Rights Reserved
 * <p>
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * <p>
 * (C)Henix. Tous droits réservés.
 * <p>
 * Avertissement : ce programme est protégé par la loi relative au droit d'auteur et par les conventions internationales. Toute reproduction ou distribution partielle ou totale du logiciel, par quelque moyen que ce soit, est strictement interdite.
 */
package org.squashtest.tm.service.internal.repository;

import org.squashtest.tm.core.foundation.collection.Paging;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.bugtracker.IssueDetector;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.execution.ExecutionStatusReport;
import org.squashtest.tm.domain.execution.ExecutionStep;

import java.util.List;

/**
 * @author Gregory Fouquet
 * @since 1.15.0  18/08/16
 */
public interface CustomExecutionDao {

	Execution findAndInit(long executionId);

	int findExecutionRank(long executionId);

	ExecutionStatusReport getStatusReport(long executionId);

	List<ExecutionStep> findStepsFiltered(Long executionId, Paging filter);

	List<ExecutionStep> findAllExecutionStepsWithStatus(Long projectId, ExecutionStatus source);

	List<IterationTestPlanItem> findAllIterationTestPlanItemsWithStatus(Long projectId, ExecutionStatus source);

	boolean projectUsesExecutionStatus(long projectId, ExecutionStatus executionStatus);

	List<IssueDetector> findAllIssueDetectorsForExecution(Long execId);

	/**
	 * @param testCaseId
	 * @param paging
	 * @return
	 */
	List<Execution> findAllByTestCaseIdOrderByRunDate(long testCaseId, Paging paging);

	/**
	 * Returns the executions which ran the given test case using the given paging and sorting data
	 *
	 * @param testCaseId
	 * @param pas
	 *            non null paging and sorting data
	 * @return non null list of executions
	 */
	List<Execution> findAllByTestCaseId(long testCaseId, PagingAndSorting pas);


}
