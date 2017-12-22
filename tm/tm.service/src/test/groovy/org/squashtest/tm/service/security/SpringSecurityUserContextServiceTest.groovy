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
package org.squashtest.tm.service.security

import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.squashtest.tm.service.internal.security.SpringSecurityUserContextService
import spock.lang.Specification

class SpringSecurityUserContextServiceTest extends Specification {
    SpringSecurityUserContextService service = new SpringSecurityUserContextService()
    Authentication currentUser = null

    def "shoud say user has role"() {
        given:
        anAuthenticatedUser()

        and: "current user has jobber role"
        GrantedAuthority jobberRole = Mock()
        jobberRole.getAuthority() >> "JOBBER"

        currentUser.authorities >> [jobberRole]

        when:
        def hasRole = service.hasRole("JOBBER")

        then:
        hasRole
    }

    def anAuthenticatedUser() {
        currentUser = Mock()
        SecurityContextHolder.getContext().setAuthentication currentUser
    }

    def noAuthenticatedUser() {
        SecurityContextHolder.getContext().setAuthentication null
    }

    def "unauthenticated user has no role"() {
        given:
        noAuthenticatedUser()

        when:
        def hasRole = service.hasRole("JOBBER")

        then:
        !hasRole
    }

    def "shoud return current user"() {
        given:
        anAuthenticatedUser()
        when:
        def auth = service.getPrincipal()

        then:
        auth == currentUser
    }

    def "shoud return null if no current user"() {
        given:
        noAuthenticatedUser()
        when:
        def auth = service.getPrincipal()

        then:
        auth == null
    }
}
