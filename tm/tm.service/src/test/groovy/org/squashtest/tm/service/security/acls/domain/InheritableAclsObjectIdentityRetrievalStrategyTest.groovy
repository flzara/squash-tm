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

import org.springframework.security.acls.model.ObjectIdentity
import org.springframework.security.acls.model.ObjectIdentityRetrievalStrategy
import org.squashtest.tm.security.annotation.InheritsAcls
import spock.lang.Specification

import javax.persistence.EntityManager
import javax.persistence.Query

class InheritableAclsObjectIdentityRetrievalStrategyTest extends Specification {
	InheritableAclsObjectIdentityRetrievalStrategy strategy = new InheritableAclsObjectIdentityRetrievalStrategy()
	ObjectIdentityRetrievalStrategy delegate = Mock()
	EntityManager em = Mock()
	Query query = Mock()

	def setup() {
		strategy.delegate = delegate
		strategy.em = em
	}

	def "should fetch parent and retrieve its object id from multi-valued heir"() {
		given:
		MultiValuedHeir heir = new MultiValuedHeir()

		and:
		MultiValuedParent parent = new MultiValuedParent()
		em.createQuery(_) >> query
		query.getSingleResult() >> parent
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
		em.createQuery(_) >> query
		query.getSingleResult() >> parent

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
		em.createQuery(_) >> query
		query.getSingleResult() >> parent

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
