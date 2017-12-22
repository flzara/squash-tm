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


import org.squashtest.tm.domain.testcase.Parameter;
import org.squashtest.tm.service.internal.repository.CustomParameterDao;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


public class ParameterDaoImpl implements CustomParameterDao {

	@PersistenceContext
	private EntityManager em;


	@Override
	public List<Parameter> findAllParametersByTestCase(Long testcaseId) {

		List<Parameter> allParameters = new LinkedList<>();

		Set<Long> exploredTc = new HashSet<>();
		List<Long> srcTc = new LinkedList<>();
		List<Long> destTc;

		Query next = em.createNamedQuery("parameter.findTestCasesThatDelegatesParameters");

		srcTc.add(testcaseId);

		while (!srcTc.isEmpty()) {

			allParameters.addAll(findTestCaseParameters(srcTc));

			next.setParameter("srcIds", srcTc);
			destTc = next.getResultList();

			exploredTc.addAll(srcTc);
			srcTc = destTc;
			srcTc.removeAll(exploredTc);

		}

		return allParameters;

	}


	// note that this is the same queery than ParameterDao#findOwnParametersByTestCases
	// duplicate here because of the inheritance between the interfaces is what it is
	private List<Parameter> findTestCaseParameters(List<Long> testcaseIds) {

		Query query = em.createNamedQuery("Parameter.findOwnParametersByTestCases");
		query.setParameter("testCaseIds", testcaseIds);
		return query.getResultList();
	}

}
