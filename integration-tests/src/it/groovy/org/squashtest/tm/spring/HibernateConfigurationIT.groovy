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
package org.squashtest.tm.spring

import org.hibernate.Session
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.test.annotation.Rollback
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.squashtest.it.basespecs.DatasourceDependantSpecification;
import org.squashtest.it.config.DynamicServiceConfig
import org.squashtest.it.config.ServiceSpecConfig
import org.squashtest.tm.service.BugTrackerConfig
import org.squashtest.tm.service.RepositoryConfig
import org.squashtest.tm.service.SchedulerConfig
import org.squashtest.tm.service.TmServiceConfig
import spock.lang.Ignore
import spock.lang.Specification

import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import javax.transaction.Transaction

@ContextConfiguration(classes = [ServiceSpecConfig, DynamicServiceConfig, TmServiceConfig, BugTrackerConfig, SchedulerConfig], loader = SpringApplicationContextLoader.class)
@Rollback
@Ignore
@Deprecated
/**
 * @deprecated should be either retired or check the entitymanager / EMF
 */
class HibernateConfigurationIT extends DatasourceDependantSpecification {


	def "should have injected session factory"() {
		expect:
		sessionFactory != null
	}


	def "should open a session"() {
		when:
		def session = em.unwrap(Session.class)

		then:
		session != null

		cleanup:
		session?.close()
	}

	def "should open a transaction"() {
		given:
		Session session = em.unwrap(Session.class)

		when:
		Transaction tx = session.beginTransaction()

		then:
		tx != null;

		cleanup:
		tx?.rollback()
		session?.close()
	}
}
