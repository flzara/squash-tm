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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.domain.testautomation.AutomatedExecutionExtender;
import org.squashtest.tm.domain.testautomation.AutomatedTest;
import org.squashtest.tm.service.internal.repository.AutomatedTestDao;

@Repository
public class HibernateAutomatedTestDao implements AutomatedTestDao {

	@PersistenceContext
	private EntityManager em;

	@Override
	public AutomatedTest persistOrAttach(AutomatedTest newTest) {

		if (newTest.getId() != null && findById(newTest.getId()) != null) {
			return newTest;
		}

		AutomatedTest persisted = findByExample(newTest);
		if (persisted != null){
			return persisted;
		}
		else{
			em.persist(newTest);
			return newTest;
		}
	}




	@Override
	public void removeIfUnused(AutomatedTest test) {

		AutomatedTest persisted;

		if (test == null){
			return;
		}
		else if (test.getId() != null){
			persisted = test;
		}
		else{
			persisted = findByExample(test);
		}

		if (countReferences(persisted.getId()) == 0L){
			em.unwrap(Session.class).delete(persisted);
		}

	}


	@Override
	public void pruneOrphans(){

		Collection<AutomatedTest> orphans = em.createNamedQuery("automatedTest.findOrphans").getResultList();

		if (orphans.isEmpty()){
			return;
		}

		Query q = em.createNamedQuery("automatedTest.bulkDelete");
		q.setParameter("tests", orphans);
		q.executeUpdate();

	}


	@Override
	public long countReferences(long testId) {

		Query qCountTC = em.createNamedQuery("automatedTest.countReferencesByTestCases");
		qCountTC.setParameter("autoTestId", testId);
		long countTC = (Long)qCountTC.getSingleResult();

		Query qCountExt = em.createNamedQuery("automatedTest.countReferencesByExecutions");
		qCountExt.setParameter("autoTestId", testId);
		long countExt = (Long)qCountExt.getSingleResult();

		return countTC + countExt;
	}


	@Override
	public AutomatedTest findById(Long testId) {
		Session session = em.unwrap(Session.class);
		return (AutomatedTest) session.load(AutomatedTest.class, testId);
	}

	@Override
	public List<AutomatedTest> findByTestCases(Collection<Long> testCaseIds) {
		if (testCaseIds.isEmpty()){
			return Collections.emptyList();
		}

		Query query = em.createNamedQuery("automatedTest.findByTestCase");
		query.setParameter("testCaseIds", testCaseIds);
		return query.getResultList();
	}

	@Override
	public AutomatedTest findByExample(AutomatedTest example) {

		Criteria criteria = em.unwrap(Session.class).createCriteria(AutomatedTest.class);
		criteria = criteria.add(Example.create(example));
		criteria = criteria.add(Restrictions.eq("project", example.getProject()));

		List<?> res = criteria.list();

		if (res.isEmpty()) {
			return null;
		} else if (res.size() == 1) {
			return (AutomatedTest) res.get(0);
		} else {
			throw new NonUniqueEntityException();
		}
	}

	@Override
	public List<AutomatedTest> findAllByExtenderIds(List<Long> extenderIds) {

		if (extenderIds.isEmpty()){
			return Collections.emptyList();
		}

		Query query = em.createNamedQuery("automatedTest.findAllByExtenderIds");
		query.setParameter("extenderIds", extenderIds);
		return query.getResultList();

	}

	@Override
	public List<AutomatedTest> findAllByExtender(Collection<AutomatedExecutionExtender> extenders) {

		if (extenders.isEmpty()){
			return Collections.emptyList();
		}

		Query query = em.createNamedQuery("automatedTest.findAllByExtenders");
		query.setParameter("extenders", extenders);
		return query.getResultList();

	}


}
