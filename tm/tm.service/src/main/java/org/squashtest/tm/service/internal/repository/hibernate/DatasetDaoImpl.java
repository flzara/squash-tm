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

import org.squashtest.tm.domain.testcase.Dataset;
import org.squashtest.tm.service.internal.repository.CustomDatasetDao;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.*;


public class DatasetDaoImpl implements CustomDatasetDao {

	@PersistenceContext
	private EntityManager em;

	@SuppressWarnings("unchecked")
	@Override
	public List<Dataset> findOwnDatasetsByTestCases(List<Long> testCaseIds) {
		if (!testCaseIds.isEmpty()) {
			Query query = em.createNamedQuery("Dataset.findOwnDatasetsByTestCases");
			query.setParameter("testCaseIds", testCaseIds);
			return query.getResultList();
		} else {
			return Collections.emptyList();
		}
	}


	@Override
	public List<Dataset> findAllDelegateDatasets(Long testCaseId) {
		List<Dataset> allDatasets = new LinkedList<>();

		Set<Long> exploredTc = new HashSet<>();
		List<Long> srcTc = new LinkedList<>();
		List<Long> destTc;

		Query next = em.createNamedQuery("dataset.findTestCasesThatInheritParameters");

		srcTc.add(testCaseId);

		while (!srcTc.isEmpty()) {

			next.setParameter("srcIds", srcTc);
			destTc = next.getResultList();

			if (!destTc.isEmpty()) {
				allDatasets.addAll(findOwnDatasetsByTestCases(destTc));
			}

			exploredTc.addAll(srcTc);
			srcTc = destTc;
			srcTc.removeAll(exploredTc);

		}

		return allDatasets;
	}

	@Override
	public void removeDatasetFromTestPlanItems(Long datasetId) {
		Query query = em.createNamedQuery("dataset.removeDatasetFromItsIterationTestPlanItems");
		query.setParameter("datasetId", datasetId);
		query.executeUpdate();

		Query query2 = em.createNamedQuery("dataset.removeDatasetFromItsCampaignTestPlanItems");
		query2.setParameter("datasetId", datasetId);
		query2.executeUpdate();
	}
}
