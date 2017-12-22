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
package org.squashtest.it.basespecs

import javax.inject.Inject;
import javax.persistence.EntityManager
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext
import javax.persistence.PersistenceUnit
import javax.persistence.SynchronizationType;
import javax.transaction.TransactionManager;

import org.hibernate.Session
import org.hibernate.SessionFactory;
import org.hibernate.Transaction
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.test.annotation.Rollback
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.squashtest.it.config.ServiceSpecConfig
import org.squashtest.it.utils.SkipAll;

import spock.lang.Specification

/**
 * Superclass for hibernate mapping integration tests.
 */
@Rollback
abstract class DbunitMappingSpecification extends DatasourceDependantSpecification {
	
	@PersistenceUnit
	EntityManagerFactory emf;
	

	/**
	 * Runs action closure in a new transaction created from a new session.
	 * @param action
	 * @return propagates closure result.
	 */
	
	def final doInTransaction(def action) {

		
		Session s = createManager()
		Transaction tx = s.beginTransaction()

		try {
			def res = action(s)

			s.flush()
			tx.commit()
			return res
		} 
		catch(Exception wtf){
			// that catch block is useful so that we can probe a breakpoint in there
			throw wtf;
		}
		finally {
			s?.close()
			destroyManager()
		}
	}

	/**
	 * Persists a fixture in a separate session / transaction
	 * @param fixture
	 * @return
	 */
	
	def final persistFixture(Object... fixtures) {
		doInTransaction { session ->
			fixtures.each { fixture -> 
				session.persist fixture 
			}
		}
	}
	/**
	 * Deletes a fixture in a separate session / transaction
	 * @param fixture
	 * @return
	 */
	
	def final deleteFixture(Object... fixtures) {
		doInTransaction { session ->
			fixtures.each { fixture ->
				if (fixture.id != null){							
					def persistent = session.load(fixture.class, fixture.id)
					if (persistent != null){
						session.delete persistent
					}
				} 
			}
		}
	}
	
	
	// the two methods below create an entity manager and bind them to 
	// the future transaction, so that other parties (like hibernate search bridges) 
	// know where to find the correct instance of the entity manager
	def createManager(){
		
		EntityManager localEm = emf.createEntityManager();
		EntityManagerHolder emHolder = new EntityManagerHolder(localEm);
		TransactionSynchronizationManager.bindResource(emf, emHolder);
		return localEm.unwrap(Session.class);
	}
	
	def destroyManager(){
		EntityManagerHolder emHolder = (EntityManagerHolder) TransactionSynchronizationManager.unbindResource(emf);
		EntityManagerFactoryUtils.closeEntityManager(emHolder.getEntityManager());
	}
}
