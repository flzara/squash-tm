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

import static java.util.stream.Collectors.toList;
import static org.squashtest.tm.domain.customfield.BindableEntity.CAMPAIGN;
import static org.squashtest.tm.domain.customfield.BindableEntity.EXECUTION;
import static org.squashtest.tm.domain.customfield.BindableEntity.EXECUTION_STEP;
import static org.squashtest.tm.domain.customfield.BindableEntity.ITERATION;
import static org.squashtest.tm.domain.customfield.BindableEntity.REQUIREMENT_VERSION;
import static org.squashtest.tm.domain.customfield.BindableEntity.TEST_CASE;
import static org.squashtest.tm.domain.customfield.BindableEntity.TEST_STEP;
import static org.squashtest.tm.domain.customfield.BindableEntity.TEST_SUITE;

import java.util.*;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.squashtest.tm.domain.customfield.*;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.project.ProjectTemplate;
import org.squashtest.tm.domain.project.ProjectVisitor;
import org.squashtest.tm.service.internal.repository.BoundEntityDao;
import org.squashtest.tm.service.internal.repository.GenericProjectDao;
import org.squashtest.tm.service.internal.repository.ParameterNames;

@Repository
public class HibernateBoundEntityDao implements BoundEntityDao {

	private static final String TEST_CASE_QUERY_NAME = "BoundEntityDao.findAllTestCasesIdsForProject";
	private static final String REQUIREMENT_QUERY_NAME = "BoundEntityDao.findAllReqVersionsIdsForProject";
	private static final String CAMPAIGN_QUERY_NAME = "BoundEntityDao.findAllCampaignsIdsForProject";
	private static final String ITERATION_QUERY_NAME = "BoundEntityDao.findAllIterationsIdsForProject";
	private static final String TEST_SUITE_QUERY_NAME = "BoundEntityDao.findAllTestSuitesIdsForProject";
	private static final String TEST_STEP_QUERY_NAME = "BoundEntityDao.findAllTestStepsIdsForProject";
	private static final String EXECUTION_QUERY_NAME = "BoundEntityDao.findAllExecutionsIdsForProject";
	private static final String EXECUTION_STEP_QUERY_NAME = "BoundEntityDao.findAllExecutionStepsIdsForProject";

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
		
		GenericProject project = customFieldBinding.getBoundProject();
		final List<BoundEntity> entities = new ArrayList<>();
		
		project.accept(new ProjectVisitor() {			
			@Override
			public void visit(ProjectTemplate projectTemplate) {
				// noop
			}
			
			@Override
			public void visit(Project project) {
				BindableEntity boundType = customFieldBinding.getBoundEntity();
				String queryName = BOUND_ENTITIES_IN_PROJECT_QUERY.get(boundType);
				Query q = em.createNamedQuery(queryName);
				q.setParameter(ParameterNames.PROJECT_ID, project.getId());

				List<Long> entityIds = q.getResultList();

				List<BoundEntity> collected = entityIds.stream()
					.map(entityId -> new BoundEntityImpl(entityId, boundType, project))
					.collect(toList());
				
				entities.addAll(collected);
			}
		});
		
		return entities;
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
