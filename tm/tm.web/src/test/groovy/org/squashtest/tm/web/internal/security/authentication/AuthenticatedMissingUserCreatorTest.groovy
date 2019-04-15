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
package org.squashtest.tm.web.internal.security.authentication

import org.springframework.security.authentication.event.AuthenticationSuccessEvent
import org.springframework.security.core.Authentication
import org.springframework.security.provisioning.UserDetailsManager
import org.squashtest.tm.api.security.authentication.AuthenticationProviderFeatures
import org.squashtest.tm.api.security.authentication.ExtraAccountInformationAuthentication
import org.squashtest.tm.domain.users.UsersGroup
import org.squashtest.tm.exception.user.LoginAlreadyExistsException
import org.squashtest.tm.service.internal.security.AuthenticationProviderContext
import org.squashtest.tm.service.user.AdministrationService
import spock.lang.Specification

/**
 * @author Gregory Fouquet
 *
 */
class AuthenticatedMissingUserCreatorTest extends Specification {
	AuthenticatedMissingUserCreator listener = new AuthenticatedMissingUserCreator();
	AuthenticationProviderContext authProviderContext = Mock()
	AuthenticationProviderFeatures features = Mock()
	AdministrationService userAccountManager = Mock()
	Authentication principal = Mock();
	AuthenticationSuccessEvent authenticatedEvent = new AuthenticationSuccessEvent(principal)
	UserDetailsManager userDetailsManager = Mock()
	
	def setup() {
		
		authProviderContext.getProviderFeatures(principal) >> features
		
		listener.authProviderContext  = authProviderContext
		listener.userAccountManager = userAccountManager
		listener.userDetailsManager = userDetailsManager
		
		principal.getName() >> "chris.jericho"
	}

	def "should not do anything when auth provider does not ask to create user"() {
		given:
		features.shouldCreateMissingUser() >> false

		expect:
		listener.onApplicationEvent authenticatedEvent
	}

	def "should not do anything when user exists"() {
		given:
		features.shouldCreateMissingUser() >> true

		when:
		listener.onApplicationEvent authenticatedEvent

		then:
		1 * userAccountManager.checkLoginAvailability("chris.jericho") >> { throw new LoginAlreadyExistsException()  }
	}

	def "should create stub user when it does not exist"() {
		given:
		features.shouldCreateMissingUser() >> true

		and:
		userAccountManager.findByLogin("chris.jericho") >> null

		when:
		listener.onApplicationEvent authenticatedEvent

		then:
		1 * userAccountManager.createUserWithoutCredentials({
			it -> it.login == "chris.jericho"
		}, UsersGroup.USER)
	}

	def "should create stub user with extra information if it does not exist and extra are available"(){

		given :
		features.shouldCreateMissingUser() >> true

		// override the principal to make it implement ExtraAccountInformationAuthentication
		principal = Mock(ExtraAccountInformationAuthentication){
			getName() >> "chris.jericho"
			getFirstName() >> "Chris"
			getLastName() >> "Jericho"
			getEmail() >> "chris.jericho@awesome.com"
		}

		authenticatedEvent = new AuthenticationSuccessEvent(principal)
		authProviderContext.getProviderFeatures(principal) >> features

		and :
		userAccountManager.findByLogin("chris.jericho") >> null

		when :
		listener.onApplicationEvent authenticatedEvent

		then :
		1 * userAccountManager.createUserWithoutCredentials({
			it -> it.login == "chris.jericho" &&
				it.firstName == "Chris" &&
				it.lastName == "Jericho" &&
				it.email == "chris.jericho@awesome.com"
		}, UsersGroup.USER)
	}
}
