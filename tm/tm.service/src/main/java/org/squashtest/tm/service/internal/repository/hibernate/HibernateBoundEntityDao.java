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

import static org.squashtest.tm.domain.customfield.BindableEntity.CAMPAIGN;
import static org.squashtest.tm.domain.customfield.BindableEntity.EXECUTION;
import static org.squashtest.tm.domain.customfield.BindableEntity.EXECUTION_STEP;
import static org.squashtest.tm.domain.customfield.BindableEntity.ITERATION;
import static org.squashtest.tm.domain.customfield.BindableEntity.REQUIREMENT_VERSION;
import static org.squashtest.tm.domain.customfield.BindableEntity.TEST_CASE;
import static org.squashtest.tm.domain.customfield.BindableEntity.TEST_STEP;
import static org.squashtest.tm.domain.customfield.BindableEntity.TEST_SUITE;

import java.util.*;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.BoundEntity;
import org.squashtest.tm.domain.customfield.CustomFieldBinding;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.service.internal.repository.BoundEntityDao;
import org.squashtest.tm.service.internal.repository.ParameterNames;

@Repository
public class HibernateBoundEntityDao implements BoundEntityDao {

	private static final String TEST_CASE_QUERY_NAME = "BoundEntityDao.findAllTestCasesForProject";
	private static final String REQUIREMENT_QUERY_NAME = "BoundEntityDao.findAllReqVersionsForProject";
	private static final String CAMPAIGN_QUERY_NAME = "BoundEntityDao.findAllCampaignsForProject";
	private static final String ITERATION_QUERY_NAME = "BoundEntityDao.findAllIterationsForProject";
	private static final String TEST_SUITE_QUERY_NAME = "BoundEntityDao.findAllTestSuitesForProject";
	private static final String TEST_STEP_QUERY_NAME = "BoundEntityDao.findAllTestStepsForProject";
	private static final String EXECUTION_QUERY_NAME = "BoundEntityDao.findAllExecutionsForProject";
	private static final String EXECUTION_STEP_QUERY_NAME = "BoundEntityDao.findAllExecutionStepsForProject";

	private static final Map<BindableEntity, String> BOUND_ENTITIES_IN_PROJECT_QUERY;

	static {
		Map<BindableEntity, String> queriesByBindable = new EnumMap<>(BindableEntity.class);
		queriesByBindable.put(TEST_CASE, TEST_CASE_QUERY_NAME);
		queriesByBindable.put(REQUIREMENT_VERSION, REQUIREMENT_QUERY_NAME);
		queriesByBindable.put(CAMPAIGN, CAMPAIGN_QUERY_NAME);
		queriesByBindable.put(ITERATION, ITERATION_QUERY_NAME);
		queriesByBindable.put(TEST_SUITE, TEST_SUITE_QUERY_NAME);
		queriesByBindable.put(TEST_STEP, TEST_STEP_QUERY_NAME);
		queriesByBindable.put(EXECUTION, EXECUTION_QUERY_NAME);
		queriesByBindable.put(EXECUTION_STEP, EXECUTION_STEP_QUERY_NAME);

		BOUND_ENTITIES_IN_PROJECT_QUERY = Collections.unmodifiableMap(queriesByBindable);
	}

	@PersistenceContext
	private EntityManager em;


	@Override
	@SuppressWarnings("unchecked")
	public List<BoundEntity> findAllForBinding(CustomFieldBinding customFieldBinding) {
		BindableEntity boundType = customFieldBinding.getBoundEntity();
		String queryName = BOUND_ENTITIES_IN_PROJECT_QUERY.get(boundType);
		Query q = em.createNamedQuery(queryName);
		q.setParameter(ParameterNames.PROJECT_ID, customFieldBinding.getBoundProject().getId());

		return q.getResultList();
	}



	@Override
	public BoundEntity findBoundEntity(CustomFieldValue customFieldValue) {
		return findBoundEntity(customFieldValue.getBoundEntityId(), customFieldValue.getBoundEntityType());
	}

	@Override
	public BoundEntity findBoundEntity(Long boundEntityId, BindableEntity entityType) {

		Class<?> entityClass = entityType.getReferencedClass();
		return (BoundEntity) em.getReference(entityClass, boundEntityId);

	}

	@Override
	public boolean hasCustomField(Long boundEntityId, BindableEntity entityType) {

		Query query = em.createNamedQuery("BoundEntityDao.hasCustomFields");
		query.setParameter("boundEntityId", boundEntityId);
		query.setParameter("boundEntityType", entityType);

		return (Long)query.getSingleResult() != 0;

	}


}
