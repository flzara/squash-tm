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

import static org.squashtest.tm.domain.execution.ExecutionStatus.READY;
import static org.squashtest.tm.domain.execution.ExecutionStatus.RUNNING;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.hibernate.type.StringType;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.testautomation.AutomatedExecutionExtender;
import org.squashtest.tm.domain.testautomation.AutomatedSuite;
import org.squashtest.tm.service.internal.repository.AutomatedSuiteDao;

@Repository
public class HibernateAutomatedSuiteDao implements AutomatedSuiteDao {

	@PersistenceContext
	private EntityManager em;


	@Override
	public void delete(String id) {
		AutomatedSuite suite = findById(id);
		em.remove(suite);
	}


	@Override
	public void delete(AutomatedSuite suite) {
		em.remove(suite);
	}

	@Override
	public AutomatedSuite createNewSuite() {
		AutomatedSuite suite = new AutomatedSuite();
		em.persist(suite);
		return suite;
	}

	@Override
	public AutomatedSuite findById(String id) {
		return em.getReference(AutomatedSuite.class, id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<AutomatedSuite> findAll() {
		Query query = em.createNamedQuery("automatedSuite.findAll");
		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<AutomatedSuite> findAllByIds(Collection<String> ids) {
		if (ids.isEmpty()) {
			return Collections.emptyList();
		} else {
			Query query = em.createNamedQuery("automatedSuite.findAllById");
			query.setParameter("suiteIds", ids);
			return query.getResultList();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<AutomatedExecutionExtender> findAllExtenders(String suiteId) {
		Query query = em.createNamedQuery("automatedSuite.findAllExtenders");
		query.setParameter("suiteId", suiteId);
		return query.getResultList();
	}

	@Override
	public Collection<AutomatedExecutionExtender> findAllWaitingExtenders(String suiteId) {
		return findAllExtendersByStatus(suiteId, READY);
	}

	@Override
	public Collection<AutomatedExecutionExtender> findAllRunningExtenders(String suiteId) {
		return findAllExtendersByStatus(suiteId, RUNNING);
	}

	@Override
	public Collection<AutomatedExecutionExtender> findAllCompletedExtenders(String suiteId) {
		return findAllExtendersByStatus(suiteId, ExecutionStatus.getTerminatedStatusSet());
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<AutomatedExecutionExtender> findAllExtendersByStatus(final String suiteId,
			final Collection<ExecutionStatus> statusList) {

		Query query = em.createNamedQuery("automatedSuite.findAllExtendersHavingStatus");

		query.setParameter("suiteId", suiteId);

		query.setParameter("statusList", statusList);

		return query.getResultList();
	}

	public Collection<AutomatedExecutionExtender> findAllExtendersByStatus(String suiteId,
			ExecutionStatus... statusArray) {
		Collection<ExecutionStatus> statusList = Arrays.asList(statusArray);
		return findAllExtendersByStatus(suiteId, statusList);

	}

}
