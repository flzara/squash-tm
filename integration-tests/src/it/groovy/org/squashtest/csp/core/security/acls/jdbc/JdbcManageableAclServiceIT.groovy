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
package org.squashtest.csp.core.security.acls.jdbc

import javax.inject.Inject
import javax.sql.DataSource

import org.springframework.security.acls.domain.ObjectIdentityImpl
import org.springframework.security.acls.model.ObjectIdentity
import org.springframework.test.annotation.Rollback
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.ContextHierarchy
import org.springframework.transaction.annotation.Transactional
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.squashtest.it.config.EnabledAclSpecConfig
import org.squashtest.it.stub.security.UserContextHelper
import org.squashtest.tm.service.security.acls.jdbc.ManageableAclService
import org.squashtest.tm.service.security.acls.jdbc.UnknownAclClassException
import org.squashtest.tm.service.security.acls.model.ObjectAclService
import org.unitils.dbunit.annotation.DataSet
import org.unitils.dbunit.annotation.ExpectedDataSet

import spock.lang.IgnoreRest
import spock.unitils.UnitilsSupport


@UnitilsSupport
@Rollback
@Transactional
@ContextHierarchy([
	// enabling the ACL management that was disabled in DbunitServiceSpecification 
	@ContextConfiguration(name="aclcontext", classes = [EnabledAclSpecConfig], inheritLocations=false)	
])
class JdbcManageableAclServiceIT extends DbunitServiceSpecification {
	@Inject
	DataSource dataSource
	@Inject
	ManageableAclService manageableService
	@Inject
	ObjectAclService service

	def setup() {
		UserContextHelper.setUsername("Bob")
	}

	@IgnoreRest
	@DataSet("JdbcManageableAclServiceIT.should create OID for a project.xml")
	@ExpectedDataSet("JdbcManageableAclServiceIT.should create OID for a project.expected.xml")
	def "should create OID for a project"() {
		given:
		ObjectIdentity oid = new ObjectIdentityImpl("foo.Bar", -10L)

		when:
		manageableService.createObjectIdentity oid

		then:
		true // expected dataset constraint
	}

	@DataSet("JdbcManageableAclServiceIT.should delete OID for a project.xml")
	@ExpectedDataSet("JdbcManageableAclServiceIT.should delete OID for a project.expected.xml")
	def "should delete OID for a project"() {
		given:
		ObjectIdentity oid = new ObjectIdentityImpl("foo.Bar", -10L)

		when:
		manageableService.removeObjectIdentity(oid)

		then:
		true // expected dataset constraint
	}

	@DataSet(value = "JdbcManageableAclServiceIT.should create OID for a project.xml")
	def "should not create OID for an unknown class"() {
		given:
		ObjectIdentity oid = new ObjectIdentityImpl("foo.Unknown", -10L)

		when:
		manageableService.createObjectIdentity oid

		then:
		thrown(UnknownAclClassException)
	}

	@DataSet(value = "JdbcManageableAclServiceIT.should find permission groups by namespace.xml")
	def "should find permission groups by namespace"() {
		when:
		def groups = service.findAllPermissionGroupsByNamespace("foo")

		then:
		groups*.id.containsAll([-10L, -20L])
		groups*.qualifiedName.containsAll(["foo.Bar", "foo.Baz"])
	}

	@DataSet(value = "JdbcManageableAclServiceIT.should find permission groups by namespace.xml")
	def "should find no permission groups by unknown namespace"() {
		when:
		def groups = service.findAllPermissionGroupsByNamespace("unknown")

		then:
		groups == []
	}

	//	@DataSet("JdbcManageableAclServiceIT.should find object Identity for project.xml")
	//	def "should find object Identity for project"(){
	//		given:
	//			ObjectIdentity oid = new ObjectIdentityImpl("batmobile", -1000L)
	//		when:
	//		def res = manageableService.retrieveObjectIdentityPrimaryKey(oid)
	//
	//		then:
	//		res == 900
	//	}

	@DataSet("JdbcManageableAclServiceIT.should retrieve acl group user.xml")
	def "should retrieve acl group for user"() {
		when:
		def res = service.retrieveClassAclGroupFromPartyId(-10, "batmobile")


		then:
		res != null
		!res.isEmpty()
		res.collectAll { it[0] }.containsAll(-102L, -101L)
	}

	@DataSet("JdbcManageableAclServiceIT.should retrieve acl group user.xml")
	def "sould fiind object without permission"() {
		when:
		def res = service.findObjectWithoutPermissionByPartyId(-10, "batmobile")
		System.out.println(res.toString())

		then:
		res != null
		!res.isEmpty()
		res[0] == -103

	}

	@DataSet("JdbcManageableAclServiceIT.should find user with write permission on a specific object.xml")
	def "should find user with write permission on a specific object"() {
		given:
		ObjectIdentity oid = new ObjectIdentityImpl("batmobile", -1000L)
		List<ObjectIdentity> entityRefs = new ArrayList<ObjectIdentity>()
		entityRefs.add(oid)

		when:
		def res = service.findUsersWithWritePermission(entityRefs)


		then:
		res != null
		!res.isEmpty()
		res.size() == 1
		res[0] == "batman"
	}

}
