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
package org.squashtest.tm.service.security.acls.domain

import javax.persistence.EntityManager;

import org.hibernate.Query
import org.springframework.security.acls.model.ObjectIdentity
import org.springframework.security.acls.model.ObjectIdentityRetrievalStrategy
import org.squashtest.tm.service.security.acls.domain.InheritableAclsObjectIdentityRetrievalStrategy
import org.squashtest.tm.security.annotation.InheritsAcls

import spock.lang.Specification

class InheritableAclsObjectIdentityRetrievalStrategyTest extends Specification {
	InheritableAclsObjectIdentityRetrievalStrategy strategy = new InheritableAclsObjectIdentityRetrievalStrategy()
	ObjectIdentityRetrievalStrategy delegate = Mock()
	EntityManager em = Mock()
	org.hibernate.Session session = Mock()
	Query query = Mock()

	def setup() {
		strategy.delegate = delegate
		strategy.em = em
		em.unwrap(_) >> session
	}

	def "should fetch parent and retrieve its object id from multi-valued heir"() {
		given:
		MultiValuedHeir heir = new MultiValuedHeir()

		and:
		MultiValuedParent parent = new MultiValuedParent()
		session.createQuery(_) >> query
		query.uniqueResult() >> parent
		and:
		ObjectIdentity id = Mock()
		delegate.getObjectIdentity(parent) >> id

		when:
		def oid = strategy.getObjectIdentity(heir)

		then:
		oid == id
	}

	def "should delegate for no inheritance object"() {
		given:
		NoInheritance domain = new NoInheritance()

		and:
		ObjectIdentity id = Mock()
		delegate.getObjectIdentity(domain) >> id

		when:
		def oid = strategy.getObjectIdentity(domain)

		then:
		oid == id
	}

	def "should fetch parent and retrieve its object id from single-valued heir"() {
		given:
		SingleValuedHeir heir = new SingleValuedHeir()

		and:
		SingleValuedParent parent = new SingleValuedParent()
		session.createQuery(_) >> query
		query.uniqueResult() >> parent

		and:
		ObjectIdentity id = Mock()
		delegate.getObjectIdentity(parent) >> id

		when:
		def oid = strategy.getObjectIdentity(heir)

		then:
		oid == id
	}

	def "should fetch parent and retrieve its object id from single-valued sub heir"() {
		given:
		SingleValuedSubHeir heir = new SingleValuedSubHeir()

		and:
		SingleValuedParent parent = new SingleValuedParent()
		session.createQuery(_) >> query
		query.uniqueResult() >> parent

		and:
		ObjectIdentity id = Mock()
		delegate.getObjectIdentity(parent) >> id

		when:
		def oid = strategy.getObjectIdentity(heir)

		then:
		oid == id
	}
}

class MultiValuedParent {
	private List<MultiValuedHeir> heirs
	Long id
}

@InheritsAcls(constrainedClass=MultiValuedParent, collectionName="heirs")
class MultiValuedHeir {
	Long id
}

class NoInheritance {
}

class SingleValuedParent {
	private SingleValuedHeir heir
	Long id
}

@InheritsAcls(constrainedClass=SingleValuedParent, collectionName="heir")
class SingleValuedHeir {
	Long id
}

class SingleValuedSubHeir extends SingleValuedHeir{
}
