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
package org.squashtest.tm.search.bridge

import org.hibernate.Criteria
import org.hibernate.Filter
import org.hibernate.FlushMode
import org.hibernate.Query
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.hibernate.Transaction
import org.hibernate.collection.internal.PersistentList
import org.hibernate.engine.spi.SessionFactoryImplementor
import org.hibernate.engine.spi.SessionImplementor
import org.hibernate.internal.SessionImpl
import org.hibernate.type.LongType
import org.squashtest.tm.domain.Identified
import org.squashtest.tm.domain.search.CollectionSizeBridge
import spock.lang.Specification

class CollectionSizeBridgeTest extends Specification{

	CollectionSizeBridge bridge = new CollectionSizeBridge()

	MockSession session
	MockSessionFactory factory
	Transaction tx
	Session newSession
	Criteria criteria
	PersistentList collection

	def "when the collection is a plain collection, just return its size"(){

		expect :
			bridge.objectToString([1,2,3]) == "0000003"

	}

	def "should say that the collection is #humanmsg a hibernate collection"(){

		expect :
			bridge.isHibernate (list) == res


		where :
			res 	|	humanmsg	| list
			true	|	""			| new PersistentList(null)
			false	|	"not"		| [1,2,3]

	}

	def "hib collection : find that collection is alive and well"(){
		given :
			mockLiveSession()
			mockHibernateCollection()

		when :
			def response = bridge.hasLiveSession collection

		then :
			response == true
	}

	def "hib collection : find that collection is dead"(){
		given :
			mockDeadSession()
			mockHibernateCollection()

		when :
			def response = bridge.hasLiveSession collection

		then :
			response == false
	}

	def "hib collection : retrieve the session"(){

		given :
			mockLiveSession()
			mockHibernateCollection()

		when :
			def retrieved = bridge.getLiveSession collection

		then :
			retrieved == session

	}

	def "hib collection : create new session"(){

		given :
			mockDeadSession()
			mockHibernateCollection()

		when :
			def created = bridge.createNewSession collection

		then :
			created == newSession
			1 * newSession.setDefaultReadOnly(true)
			1 * newSession.setFlushMode(FlushMode.MANUAL)


	}

	def "hib collection : init the criteria data"(){

		given :
			mockDeadSession()
			mockHibernateCollection()

		and :
			// complicated way to assign collection.role
			collection.setSnapshot(1L, "org.whatever.Entity.stuff", [1,2,3])

		when :
			bridge.initCriteriaData collection

		then :
			bridge.entityClass == "org.whatever.Entity"
			bridge.collectionPath == "entity.stuff"

	}

	def "hib collection : create the criteria"(){

		given :
			mockDeadSession()
			mockHibernateCollection()

		and :
			collection.setOwner( [ getId : {10L}] as Identified)
			bridge.entityClass = "org.whatever.Entity"
			bridge.collectionPath = "entity.stuff"

		when :
			bridge.createCriteria(collection, newSession)

		then :
			// hmm, not sure I should test this after all
			1 * newSession.createCriteria("org.whatever.Entity", "entity") >> criteria
			1 * criteria.createAlias("entity.stuff", "coll") >> criteria
			1 * criteria.add({
				it.propertyName == "id" &&
				it.value == 10L &&
				it.op == "="
			}) >> criteria
	}

	def "hib collection : find by current session"(){
		given :
			mockLiveSession()
			mockHibernateCollection()

		and :
			collection.setOwner( [ getId : {10L}] as Identified)
			bridge.entityClass = "org.whatever.Entity"
			bridge.collectionPath = "entity.stuff"
			criteria.uniqueResult() >> 3L

		when :
			def res = bridge.countUsingCriteria collection

		then :
			res == 3
			// assert that criteria was created from the live, current session
			1 * session.createCriteria(_,_) >> criteria

	}


	def "hib collection : find by fallback"(){
		given :
			mockDeadSession()
			mockHibernateCollection()

		and :
			collection.setOwner( [ getId : {10L}] as Identified)
			bridge.entityClass = "org.whatever.Entity"
			bridge.collectionPath = "entity.stuff"
			criteria.uniqueResult() >> 3L

		when :
			def res = bridge.fallbackHibernate collection

		then :
			res == 3

			// assert that criteria was created from a new session
			1 * newSession.createCriteria(_,_) >> criteria

			// test that the method is exited cleanly too
			1 * newSession.close()
			1 * tx.commit()

	}

	def "hib collection : should cleanly exit the fallback even if an exception was thrown"(){
		given :
			mockDeadSession()
			mockHibernateCollection()

		and :
			collection.getOwner() >> {throw new NullPointerException("hey, no owner was set")}

		when :
			def res = bridge.fallbackHibernate collection

		then :
			thrown Exception
			1 * newSession.close()
			1 * tx.commit()

	}



	def "hib collection : just return the size if initialized"(){
		given :
			mockLiveSession()
			mockHibernateCollection()
			collection.setInitialized()
			collection.list = [1,2,3]

		when :
			def res = bridge.handleHibernateCollection collection

		then :
			res == "0000003"
	}


	def mockHibernateCollection(){
		collection = new PersistentList(session)
	}

	def mockLiveSession(){
		factory = Mock()
		session = Mock()
		criteria = Mock()


		session.isOpen() >> true
		session.isConnected() >> true
		session.getSessionFactory() >> factory

		session.createCriteria(_,_) >> criteria

		mockCriteria()

	}



	def mockDeadSession(){

		factory = Mock()
		session = Mock()
		tx = Mock()
		newSession = Mock()
		criteria = Mock()

		session.isOpen() >> false

		session.getFactory() >> factory
		factory.openSession() >> newSession


		newSession.isOpen() >> true
		newSession.isConnected() >> true
		newSession.beginTransaction() >> tx
		session.createCriteria(_,_) >> criteria

		mockCriteria()


	}

	def mockCriteria(){
		criteria.setReadOnly(_) >> criteria
		criteria.createAlias(_,_) >> criteria
		criteria.add(_) >> criteria
		criteria.setProjection(_) >> criteria
	}


	private static interface MockSessionFactory extends SessionFactory, SessionFactoryImplementor{}
	private static interface MockSession extends Session, SessionImplementor{}

}
