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
import org.springframework.security.core.userdetails.User
import org.squashtest.tm.api.security.authentication.AuthenticationProviderFeatures
import org.squashtest.tm.service.SecurityConfig
import org.squashtest.tm.service.internal.security.AuthenticationProviderContext
import org.squashtest.tm.service.internal.security.InternalAuthenticationProviderFeatures
import org.squashtest.tm.service.security.AdministratorAuthenticationService
import spock.lang.Specification
import spock.lang.Unroll

class PasswordSchemeUpgraderTest extends Specification {

	private static final String PREFIX = "{"+ SecurityConfig.CURRENT_USER_PASSWORD_HASH_SCHEME+"}";

	PasswordSchemeUpgrader upgrader

	AuthenticationProviderContext authProviderContext = Mock()
	AdministratorAuthenticationService authService = Mock()

	def setup(){
		upgrader = new PasswordSchemeUpgrader()
		upgrader.authProviderContext = authProviderContext
		upgrader.authService = authService

	}

	// ********************* helper methods tests ***************************

	@Unroll("should #yesno extract a user from the authentication object because the principal is #reason")
	def "should extract a user from the authentication object when possible"(){

		expect:
		(upgrader.extractUserIfExists(auth) != null) == isFound

		where:

		auth			|	isFound	|	yesno	|	reason
		auth(user())	|	true	|	""		|	"indeed a user"
		auth(null)		|	false	|	"not"	|	"null"
		auth("Bob")		|	false	|	"not"	|	"something else"


	}

	@Unroll("should #yesno extract a cleartext password from the authentication because the password #reason")
	def "should extract a cleartext password from the authentication when possible"(){

		expect:
		(upgrader.extractClearPasswordIfExists(auth) != null) == isFound

		where:
		auth						|	isFound	|	yesno	|	reason
		auth(user(), "cleartext")	|	true	|	""		|	"is indeed a clear text password"
		auth(user(), null)			|	false	|	"not"	|	"is null"
		auth(user(), new Object())	|	false	|	"not"	|	"is no string"

	}

	@Unroll("the given password does #yesno require an upgrade because #reason")
	def "should assess whether the given password does require an upgrade"(){

		expect :
		upgrader.doesRequireUpgrade(user) == needsUpgrade

		where :
		user								| needsUpgrade	|	yesno	|	reason
		user("Bob", "anything")				| true			|	""		|	"it uses an obsolete scheme"
		user("Bob", null)					| false			|	"not"	| 	"it is null, and thus not upgradable"
		user("Bob", PREFIX+"\$2a\$10abc")	| false			|	"not"	|	"it already uses the correct scheme"

	}

	// ********************* main method test ******************************

	def "should not upgrade a password because the authenticated principal is not a user"(){

		given :
		def evt = event(auth("NotAUser"))
		setInternalFeatures()

		when :
		upgrader.onApplicationEvent(evt)

		then :
		0 * authService.resetUserPassword(_,_)

	}


	def "should not upgrade a password because the user has not authenticated via the internal auth provider"(){

		given :
			def evt = event(auth(user("Bob", "d0154fD"), "password"))

		and:
			setAlternateFeatures()


		when :
		upgrader.onApplicationEvent(evt)

		then :
		0 * authService.resetUserPassword(_,_)

	}

	def "should not upgrade a user password because it doesn't need it"(){

		given :
			def evt = event(auth(user("Bob", PREFIX+"\$2a\$10whatever"), "clearpassword"))
			setInternalFeatures()

		when :
		upgrader.onApplicationEvent(evt)

		then :
		0 * authService.resetUserPassword(_,_)

	}

	def "should not upgrade a user because the clear text password is not available"(){

		given :
			def evt = event(auth(user("Bob", "d0154fD"), null))
			setInternalFeatures()

		when :
		upgrader.onApplicationEvent(evt)

		then :
		0 * authService.resetUserPassword(_,_)


	}


	def "should upgrade a user password because all conditions are met"(){
		given :
		def evt = event(auth(user("Bob", "d0154fD"), "clear text password"))
		setInternalFeatures()

		when :
		upgrader.onApplicationEvent(evt)

		then :
		1 * authService.resetUserPassword("Bob","clear text password")

	}


	def "whatever happens, this method should not block the user from accessing the app"(){

		given :
			def evt = Mock(AuthenticationSuccessEvent){
				getAuthentication() >> { throw new RuntimeException()}
			}

		when :
		upgrader.onApplicationEvent(evt)

		then :
		notThrown Exception

	}


	// ***************** test helpers **************************************

	def event(auth){
		Mock(AuthenticationSuccessEvent){
			getAuthentication() >> auth
		}
	}

	def auth(principal = "--", password = "--"){
		Mock(Authentication){
			getPrincipal() >> principal
			getCredentials() >> password
		}
	}

	def user(username = "--", password = "--"){
		Mock(User){
			getUsername() >> username
			getPassword() >> password
		}
	}

	def setInternalFeatures(){
		authProviderContext.getProviderFeatures(_) >> InternalAuthenticationProviderFeatures.INSTANCE
	}

	def setAlternateFeatures(){
		authProviderContext.getProviderFeatures(_) >> Mock(AuthenticationProviderFeatures)
	}
}
