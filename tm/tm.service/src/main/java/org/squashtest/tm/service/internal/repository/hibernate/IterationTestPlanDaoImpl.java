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

import org.squashtest.tm.domain.attachment.Attachment;
import org.squashtest.tm.domain.attachment.AttachmentContent;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.campaign.TestPlanStatistics;
import org.squashtest.tm.service.internal.repository.CustomAttachmentDao;
import org.squashtest.tm.service.internal.repository.CustomIterationTestPlanDao;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

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
}