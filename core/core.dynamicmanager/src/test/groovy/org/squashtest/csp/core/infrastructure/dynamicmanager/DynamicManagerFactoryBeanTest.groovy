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
package org.squashtest.csp.core.infrastructure.dynamicmanager;


import javax.persistence.EntityManager;

import org.springframework.beans.factory.BeanFactory
import org.squashtest.tm.core.dynamicmanager.exception.UnsupportedMethodException
import org.squashtest.tm.core.dynamicmanager.factory.DynamicManagerFactoryBean

import spock.lang.Specification

import javax.persistence.Query

class DynamicManagerFactoryBeanTest extends Specification{

	DynamicManagerFactoryBean factory = new DynamicManagerFactoryBean()
	EntityManager em = Mock()
	BeanFactory beanFactory = Mock()

	def setup() {
		factory.beanFactory = beanFactory
		factory.lookupCustomImplementation = false
		factory.componentType = DummyManager
		factory.entityType = DummyEntity
		factory.entityManager = em
	}

	def "factory should create a unique dynamic DummyManager"() {
		when:
		factory.initializeFactory()

		then:
		factory.getObject() != null
		factory.getObject() instanceof DummyManager
		factory.getObject().equals(factory.getObject())
	}

	def "should fetch dummy entity by id and change its style"() {
		given:
		DummyEntity dummy = new DummyEntity(id: 10L, style: "mod")
		em.getReference(DummyEntity, 10L) >> dummy

		when:
		factory.initializeFactory()
		factory.object.changeStyle(10L, "new romantic")

		then:
		dummy.style == "new romantic"
	}

	def "should not talk gibberish"() {
		when:
		factory.initializeFactory()
		factory.object.talkGibberish(10L, "what?")

		then:
		thrown(UnsupportedMethodException)
	}

	def "should delegate methods of manager's superinterface"() {
		given:
		CustomDummyManager delegateManager = Mock()
		beanFactory.getBean("delegateManager") >> delegateManager
		factory.customImplementationBeanName = "delegateManager"

		when:
		factory.initializeFactory()
		factory.object.changeSomething(10L, "cool stuff")

		then:
		1 * delegateManager.changeSomething(10L, "cool stuff")
	}

	def "should not handle method of non-standard signature"() {
		when:
		factory.initializeFactory()
		factory.object.changeUnsupportedMethod(10L, "foo", "bar")

		then:
		thrown UnsupportedMethodException
	}

	def "should fetch dummy entity by id and change its shoes"() {
		given:
		DummyEntity dummy = new DummyEntity(id: 10L, shoes: "creepers")
		em.getReference(DummyEntity, 10L) >> dummy

		when:
		factory.initializeFactory()
		factory.object.changeShoes(10L, "dm's")

		then:
		dummy.shoes == "dm's"
	}

	def "should fetch dummy entity by id and change its boolean coolness"() {
		given:
		DummyEntity dummy = new DummyEntity(id: 10L, cool: false)
		em.getReference(DummyEntity, 10L) >> dummy

		when:
		factory.initializeFactory()
		factory.object.changeCool(10L, true)

		then:
		dummy.isCool()
	}

	def "should lookup the delegate manager in spring factory"() {
		given:
		CustomDummyManager delegateManager = Mock()
		beanFactory.getBean("CustomDummyManager") >> delegateManager

		and:
		factory.lookupCustomImplementation = true

		when:
		factory.initializeFactory()
		factory.object.changeSomething(10L, "cool stuff")

		then:
		1 * delegateManager.changeSomething(10L, "cool stuff")
	}

	def "should unwrap propagated reflection exceptions"() {
		given:
		DummyEntity dummy = Mock()
		em.getReference(DummyEntity, 10L) >> dummy

		and:
		dummy.setStyle(_) >> {throw new IllegalArgumentException()}

		when:
		factory.initializeFactory()
		factory.object.changeStyle(10L, "new romantic")

		then:
		thrown IllegalArgumentException
	}

	def "should fetch dummy entity by id and nullify its style"() {
		given:
		DummyEntity dummy = new DummyEntity(id: 10L, style: "mod")
		em.getReference(DummyEntity, 10L) >> dummy

		when:
		factory.initializeFactory()
		factory.object.changeStyle(10L, null)

		then:
		dummy.style == null
	}

	def "should dynamically find an entity by its id"() {
		given:
		DummyEntity entity = new DummyEntity()
		em.getReference(DummyEntity, 10L) >> entity

		when:
		factory.initializeFactory()
		def res = factory.object.findById(10L)

		then:
		res == entity
	}

	def "finder method should trigger an entity named query"() {
		given:
		Query query = Mock()
		em.createNamedQuery("DummyEntity.findByNameAndSuperpower") >> query

		and:
		DummyEntity entity = new DummyEntity()
		query.getSingleResult() >> entity

		when:
		factory.initializeFactory()
		def res = factory.object.findByNameAndSuperpower("summers", "optic blasts")

		then:
		1 * query.setParameter("1", "summers")
		1 * query.setParameter("2", "optic blasts")
		res == entity
	}

	def "finder method should trigger an entity list named query"() {
		given:
		Query query = Mock()
		em.createNamedQuery("DummyEntity.findAllByNameAndSuperpower") >> query

		and:
		DummyEntity entity = new DummyEntity()
		query.getResultList() >> [entity]

		when:
		factory.initializeFactory()
		List res = factory.object.findAllByNameAndSuperpower("summers", "optic blasts")

		then:
		1 * query.setParameter("1", "summers")
		1 * query.setParameter("2", "optic blasts")
		res == [entity]
	}

}
