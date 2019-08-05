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

import org.springframework.data.repository.query.Param;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;

import java.util.List;

public interface CustomIterationTestPlanDao {

	/**
	 * Given an {@link org.squashtest.tm.domain.campaign.Iteration}'s id,
	 * return {@link IterationTestPlanItem} whom {@link org.squashtest.tm.domain.testcase.TestCase} is automated and is part of a project allowing automation workflow
	 * @param iterationId an {@link org.squashtest.tm.domain.campaign.Iteration}'s id
	 * @return a list of {@link IterationTestPlanItem}
	 */
	List<IterationTestPlanItem> findAllByIterationIdWithTCAutomated(@Param("iterationId") Long iterationId);

	/**
	 * Given an {@link org.squashtest.tm.domain.campaign.TestSuite}'s id,
	 * return {@link IterationTestPlanItem} whom {@link org.squashtest.tm.domain.testcase.TestCase} is automated and is part of a project allowing automation workflow
	 * @param testSuiteId a {@link org.squashtest.tm.domain.campaign.TestSuite}'s id
	 * @return a list of {@link IterationTestPlanItem}
	 */
	List<IterationTestPlanItem> findAllByTestSuiteIdWithTCAutomated(@Param("testSuiteId") Long testSuiteId);

	/**
	 * Given a list of {@link IterationTestPlanItem}'s id,
	 * return the ones whom {@link org.squashtest.tm.domain.testcase.TestCase} is automated and is part of a project allowing automation workflow
	 * @param itemsIds {@link IterationTestPlanItem}'s id list
	 * @return a list of {@link IterationTestPlanItem}
	 */
	List<IterationTestPlanItem> findAllByItemsIdWithTCAutomated(@Param("itemsIds") List<Long> itemsIds);

}
