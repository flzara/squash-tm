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
package org.squashtest.tm.service.internal.repository.hibernate;

import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.tm.service.internal.repository.CustomIterationTestPlanDao;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class IterationTestPlanDaoImpl implements CustomIterationTestPlanDao {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public List<IterationTestPlanItem> findAllByIterationIdWithTCAutomated(Long iterationId) {
		Query q = entityManager.createNamedQuery("IterationTestPlanItem.findAllByIterationIdWithTCAutomated");
		q.setParameter("iterationId", iterationId);
		List<IterationTestPlanItem> result = q.getResultList();
		return result;
	}

	@Override
	public List<IterationTestPlanItem> findAllByTestSuiteIdWithTCAutomated(Long testSuiteId) {
		Query q = entityManager.createNamedQuery("IterationTestPlanItem.findAllByTestSuiteIdWithTCAutomated");
		q.setParameter("testSuiteId", testSuiteId);
		List<IterationTestPlanItem> result = q.getResultList();
		return result;
	}

	@Override
	public List<IterationTestPlanItem> findAllByItemsIdWithTCAutomated(List<Long> itemsIds) {
		Query q = entityManager.createNamedQuery("IterationTestPlanItem.findAllByItemsIdWithTCAutomated");
		q.setParameter("itemsIds", itemsIds);
		List<IterationTestPlanItem> result = q.getResultList();
		return result;
	}

	@Override
	public List<IterationTestPlanItem> fetchForAutomatedExecutionCreation(Collection<Long> itemTestPlanIds) {
		List<IterationTestPlanItem> testPlanItems = fetchIterationTestPlanItems(itemTestPlanIds);

		//We order fetch ITPI according to itemTestPlanIds order because fetch request don't respect that order depending on the database.
		Map<Long, IterationTestPlanItem> itemMap = testPlanItems.stream().collect(Collectors.toMap(IterationTestPlanItem::getId, item -> item));
		List<IterationTestPlanItem> orderedTestPlanItems = itemTestPlanIds.stream().map(itemMap::get).collect(Collectors.toList());

		// fetching the associated steps at least directly executables. For call steps it will be N+1...
		// but call step should be fairly rare in automated executions...
		fetchTestStepsForAutomatedExecutionCreation(orderedTestPlanItems);
		return orderedTestPlanItems;
	}

	private List<IterationTestPlanItem> fetchIterationTestPlanItems(Collection<Long> itemTestPlanIds) {
		Query q = entityManager.createNamedQuery("IterationTestPlanItem.fetchForExecutionCreation");
		q.setParameter("itemTestPlanIds", itemTestPlanIds);
		return (List<IterationTestPlanItem>) q.getResultList();
	}

	private List<ActionTestStep> fetchTestStepsForAutomatedExecutionCreation(Collection<IterationTestPlanItem> testPlanItems) {
		Set<Long> testCaseIds = testPlanItems.stream()
			.map(IterationTestPlanItem::getReferencedTestCase)
			.map(TestCaseLibraryNode::getId)
			.collect(Collectors.toSet());
		Query q = entityManager.createNamedQuery("ActionTestSteps.fetchWithAttachmentReferences");
		q.setParameter("testCaseIds", testCaseIds);
		return (List<ActionTestStep>) q.getResultList();
	}

}
