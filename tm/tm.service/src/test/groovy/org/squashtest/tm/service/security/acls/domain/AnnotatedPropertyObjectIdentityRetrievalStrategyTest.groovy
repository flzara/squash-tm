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

import org.squashtest.tm.service.security.acls.domain.AnnotatedPropertyObjectIdentityRetrievalStrategy
import org.squashtest.tm.security.annotation.AclConstrainedObject

import spock.lang.Specification

class AnnotatedPropertyObjectIdentityRetrievalStrategyTest extends Specification {
	AnnotatedPropertyObjectIdentityRetrievalStrategy strategy = new AnnotatedPropertyObjectIdentityRetrievalStrategy()

	def "should return OID built from annotated property"() {
		given:
		Project project = new Project(id: 666)
		DomainObject annotatedDomainObject = new DomainObject(project: project)

		when:
		def oid = strategy.getObjectIdentity(annotatedDomainObject)

		then:
		oid.identifier == 666
		oid.type == Project.name
	}

	def "should return OID built from non annotated domain object"() {
		given:
		Project nonAnnotatedDomainObject = new Project(id: 666)

		when:
		def oid = strategy.getObjectIdentity(nonAnnotatedDomainObject)

		then:
		oid.identifier == 666
		oid.type == Project.name
	}

	def "should return OID built from annotated property in superclass"() {
		given:
		Project project = new Project(id: 666)
		SubDomainObject annotatedDomainObject = new SubDomainObject(project: project)

		when:
		def oid = strategy.getObjectIdentity(annotatedDomainObject)

		then:
		oid.identifier == 666
		oid.type == Project.name
	}

	def "should return OID built from annotated property in interface"() {
		given:
		Project project = new Project(id: 666)
		InterfacedDomainObject interfacedDomainObject = new InterfacedDomainObject(project : project)

		when:
		def oid = strategy.getObjectIdentity(interfacedDomainObject)

		then:
		oid.identifier == 666
		oid.type == Project.name
	}

	def "should return OID built from annotated property in interface of superclass"() {
		given:
		Project project = new Project(id: 666)
		SubInterfacedDomainObject interfacedDomainObject = new SubInterfacedDomainObject(project : project)

		when:
		def oid = strategy.getObjectIdentity(interfacedDomainObject)

		then:
		oid.identifier == 666
		oid.type == Project.name
	}
}


class DomainObject {
	def project

	@AclConstrainedObject
	Project getProject() {
		project
	}
}

class SubDomainObject extends DomainObject {
}

class Project {
	long id
}

interface Aclable {
	@AclConstrainedObject
	Project getProject()
}

interface SubAclable extends Aclable {
	int foo()
}

class InterfacedDomainObject implements SubAclable {
	def project

	Project getProject() {
		project
	}

	int foo() {
		1
	}
}

class SubInterfacedDomainObject extends InterfacedDomainObject {
	def project

	Project getProject() {
		project
	}

	int foo() {
		1
	}
}