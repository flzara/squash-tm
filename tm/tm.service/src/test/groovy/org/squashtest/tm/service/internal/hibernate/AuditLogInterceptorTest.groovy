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
package org.squashtest.tm.service.internal.hibernate;

import org.hibernate.SessionFactory
import org.hibernate.cfg.Configuration
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.squashtest.tm.tools.unittest.hibernate.HibernateOperationCategory
import org.squashtest.tm.domain.audit.AuditableEntity;
import org.squashtest.tm.domain.audit.AuditableSupport
import spock.lang.Specification

class AuditLogInterceptorTest extends Specification {
	AuditLogInterceptor interceptor = new AuditLogInterceptor()
	Authentication authentication = Mock()

	def setup() {
		authentication.name >> "nandemo"
		SecurityContextHolder.context.authentication = authentication
	}

	def "should set creation info on auditable object save"() {
		given:
		Auditable entity = new Auditable()

		and:
		Object[] state = [new AuditableSupport()]
		String[] propertyNames = ["audit"]

		when:
		def modifiedState = interceptor.onSave(entity, null, state, propertyNames, null)

		then:
		modifiedState == true
		state[0].createdOn != null
		state[0].createdBy ==  "nandemo"
	}

	def "should not modify non auditable object on save"() {
		given:
		Object entity = Mock()

		and:
		Object[] state = [null]
		String[] propertyNames = ["audit"]

		when:
		def modifiedState = interceptor.onSave(entity, null, state, propertyNames, null)

		then:
		state == [null]
		modifiedState == false
	}

	def "should persist creation data on entity persist"() {
		given:
		Configuration config = new Configuration()
		SessionFactory sf

		config.configure("auditable-hibernate.cfg.xml")
		config.addAnnotatedClass AuditableEntity
		config.setInterceptor interceptor

		sf = config.buildSessionFactory()

		when:
		AuditableEntity entity = new AuditableEntity()
		def res

		use (HibernateOperationCategory) {
			sf.doInSession { it.persist(entity) }
			res = sf.doInSession { it.get(AuditableEntity, entity.id) }
		}

		then:
		sf != null
		res.createdBy == "nandemo"
		res.createdOn != null

//		cleanup:
//		sf.close()
	}

	def "should set modification info on auditable object update"() {
		given:
		Auditable entity = new Auditable()

		and:
		Object[] state = [new AuditableSupport()]
		String[] propertyNames = ["audit"]

		when:
		def modifiedState = interceptor.onFlushDirty(entity, null, state, null, propertyNames, null)

		then:
		modifiedState == true
		state[0].lastModifiedOn != null
		state[0].lastModifiedBy == "nandemo"
	}

	def "should not modify non auditable object on persist"() {
		given:
		Object entity = Mock()

		and:
		Object[] state = [null]
		String[] propertyNames = ["audit"]

		when:
		def modifiedState = interceptor.onFlushDirty(entity, null, state, null, propertyNames, null)

		then:
		state == [null]
		modifiedState == false
	}

	def "should persist modification data on entity update"() {
		given:
		Configuration config = new Configuration()
		SessionFactory sf

		config.configure("auditable-hibernate.cfg.xml")
		config.addAnnotatedClass AuditableEntity
		config.setInterceptor interceptor

		sf = config.buildSessionFactory()

		and:
		AuditableEntity entity = new AuditableEntity()


		use (HibernateOperationCategory) {
			sf.doInSession { it.persist(entity) }
		}

		when:
		def res

		use (HibernateOperationCategory) {
			sf.doInSession {
				it.get(AuditableEntity, entity.id).dummy = "dummy"
			}
			res = sf.doInSession {
				it.get(AuditableEntity, entity.id)
			}
		}

		then:
		res.lastModifiedBy == "nandemo"
		res.lastModifiedOn != null

		cleanup:
		sf.close()
	}

	def "a SubAuditable should be auditable"() {
		given:
		def auditable = new SubAuditable()

		when:
		def res = interceptor.isAuditable(auditable)

		then:
		res == true
	}
}

@org.squashtest.tm.domain.audit.Auditable
class Auditable {
}

class SubAuditable extends Auditable {
}