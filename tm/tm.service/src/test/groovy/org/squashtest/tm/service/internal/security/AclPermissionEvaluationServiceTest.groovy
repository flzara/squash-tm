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
package org.squashtest.tm.service.internal.security

import org.springframework.security.acls.domain.BasePermission
import org.springframework.security.core.Authentication
import org.squashtest.tm.security.acls.CustomPermission
import org.squashtest.tm.security.acls.CustomPermissionFactory
import org.squashtest.tm.service.security.UserContextService
import spock.lang.Issue
import spock.lang.Specification
import spock.lang.Unroll

class AclPermissionEvaluationServiceTest extends Specification {
	AclPermissionEvaluationService service = new AclPermissionEvaluationService()

	UserContextService userContextService = Mock()

	AffirmativeBasedCompositePermissionEvaluator permissionEvaluator = Mock()
	Authentication currentUser = Mock()

	def setup() {
		service.userContextService = userContextService
		userContextService.getPrincipal() >> currentUser
		service.permissionEvaluator = permissionEvaluator
	}

	def "user should have permission based on his role"() {
		given:
		userContextService.hasRole("JOBBER") >> true

		when:
		def hasPermission = service.hasRoleOrPermissionOnObject("JOBBER", BasePermission.ADMINISTRATION, new Object())

		then:
		hasPermission
	}

	def "user should have permission based on his acls"() {
		given:
		Object constrainedObject = new Object()
		permissionEvaluator.hasPermission(currentUser, constrainedObject, BasePermission.ADMINISTRATION) >> true

		when:
		def hasPermission = service.hasRoleOrPermissionOnObject("UBER ADMIN", BasePermission.ADMINISTRATION, constrainedObject)

		then:
		hasPermission
	}

	@Issue("5991")
	def "Admin should have all rights"() {
		given:
		userContextService.hasRole("ROLE_ADMIN") >> true


		expect:
		service.permissionsOn("Foo", 10L) == AclPermissionEvaluationService.RIGHTS
	}

	@Unroll
	@Issue("5991")
	def "User's rights list should be #rights"() {
		given:
		permissionEvaluator.hasPermission((Authentication) _, 10L, "Foo", { perms.contains(it) }) >> true

		and:
		service.permissionFactory = new CustomPermissionFactory()

		expect:
		service.permissionsOn("Foo", 10L) == rights

		where:
		rights               | perms
		["READ"]             | [BasePermission.READ]
		["WRITE", "EXECUTE"] | [BasePermission.WRITE, CustomPermission.EXECUTE]
		[]                   | []

	}
}
