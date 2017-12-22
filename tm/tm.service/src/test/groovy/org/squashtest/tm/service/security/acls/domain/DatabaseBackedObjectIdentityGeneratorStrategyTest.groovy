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
package org.squashtest.tm.service.security.acls.domain;

import org.hibernate.SessionFactory

import javax.persistence.EntityManager;

import org.hibernate.Session;
import org.ietf.jgss.Oid;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.ObjectIdentityRetrievalStrategy;
import org.squashtest.tm.service.security.acls.domain.DatabaseBackedObjectIdentityGeneratorStrategy;

import spock.lang.Specification;


/**
 * @author Gregory Fouquet
 *
 */
class DatabaseBackedObjectIdentityGeneratorStrategyTest  extends Specification {
	DatabaseBackedObjectIdentityGeneratorStrategy objectIdentityGenerator = new DatabaseBackedObjectIdentityGeneratorStrategy()
	EntityManager em = Mock()
	Session session = Mock()
	ObjectIdentityRetrievalStrategy objectIdentityRetrievalStrategy = Mock()

	def setup() {
		em.unwrap(_) >> session

		objectIdentityGenerator.em = em
		objectIdentityGenerator.objectRetrievalStrategy = objectIdentityRetrievalStrategy
	}

	def "should fetch the entity and delegate object identity generation"() {
		given:
		Object entity = Mock()
		em.find(Object, 10L) >> entity

		and:
		ObjectIdentity expectedOid = Mock()

		when:
		def oid = objectIdentityGenerator.createObjectIdentity(10L, "java.lang.Object")

		then:
		1 * objectIdentityRetrievalStrategy.getObjectIdentity(entity) >> expectedOid
		oid == expectedOid
	}
	def "should return unknown OID on unknown entity"() {
		given:
		Object entity = Mock()
		session.get(Object, 10L) >> null

		when:
		def oid = objectIdentityGenerator.createObjectIdentity(10L, "java.lang.Object")

		then:
		0 * objectIdentityRetrievalStrategy.getObjectIdentity(entity)
		oid.type == "java.lang.Object:Unknown"
	}
}
