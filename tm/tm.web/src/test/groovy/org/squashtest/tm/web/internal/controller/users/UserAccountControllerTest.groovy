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
package org.squashtest.tm.web.internal.controller.users

import org.squashtest.csp.core.bugtracker.domain.BugTracker
import org.squashtest.tm.domain.servers.AuthenticationProtocol
import org.squashtest.tm.service.internal.servers.ManageableBasicAuthCredentials
import org.squashtest.tm.service.internal.servers.UserOAuth1aToken
import org.squashtest.tm.service.servers.ManageableCredentials
import org.squashtest.tm.service.servers.StoredCredentialsManager
import org.squashtest.tm.service.user.UserAccountService
import spock.lang.Specification

class UserAccountControllerTest extends Specification{

	UserAccountController controller = new UserAccountController()

	UserAccountService userService = Mock()
	StoredCredentialsManager credManager = Mock()


	def setup(){
		controller.userService = userService
		controller.credManager = credManager
	}


	def "should create a default, empty instance of manageable basic auth credentials if the bugtracker is configured to authenticate with basic auth"(){

		given:
		BugTracker bt = Mock()
		bt.getAuthenticationProtocol() >> AuthenticationProtocol.BASIC_AUTH

		when:
		ManageableCredentials result = controller.createDefaultCredentials(bt)

		then:
		result instanceof ManageableBasicAuthCredentials
		result.username == ""
		result.password == "" as char[]

	}


	def "should create a default, empty instance of manageable OAuth tokens if the bugtracker is configured to authenticate with OAuth"(){

		given:
		BugTracker bt = Mock()
		bt.getAuthenticationProtocol()>> AuthenticationProtocol.OAUTH_1A

		when:
		ManageableCredentials result = controller.createDefaultCredentials(bt)

		then:
		result instanceof  UserOAuth1aToken
		result.token == ""
		result.tokenSecret == ""

	}


	def "for all bugtrackers accessible to the current user, map them to the appropriate credentials"(){

		given: "the bugtrackers"

		def bt1 = Mock(BugTracker){
			getId() >> 1L
			getAuthenticationProtocol() >> AuthenticationProtocol.BASIC_AUTH
		}

		def bt2 = Mock(BugTracker){
			getId() >> 2L
			getAuthenticationProtocol() >> AuthenticationProtocol.OAUTH_1A
		}

		and: "the credentials"

		def cred1 = new ManageableBasicAuthCredentials("Bob", "bob")
		def cred2 = new UserOAuth1aToken("123", "ABC")


		when:
		def result = controller.getPairedBugtrackerAndManagedCredentials()

		then:

		// interactions & behavior
		1 * userService.findAllUserBugTracker() >> [ bt1, bt2 ]

		1 * credManager.findCurrentUserCredentials(1L) >> cred1
		1 * credManager.findCurrentUserCredentials(2L) >> cred2


		// result
		result == [
			(bt1) : cred1,
			(bt2) : cred2
		]

	}


	def "for all bugtrackers accessible to the current user, map default credentials when none are defined for those bugtrackers"(){

		given: "the bugtrackers"

		def bt1 = Mock(BugTracker){
			getId()>> 1L
			getAuthenticationProtocol()>> AuthenticationProtocol.BASIC_AUTH
		}

		def bt2 = Mock(BugTracker){
			getId()>> 2L
			getAuthenticationProtocol()>> AuthenticationProtocol.OAUTH_1A
		}

		when:

		Map<BugTracker, ManageableCredentials> result = controller.getPairedBugtrackerAndManagedCredentials()

		then:
		1 * userService.findAllUserBugTracker() >> [ bt1, bt2 ]

		def cred1 = result.get(bt1)

		cred1 instanceof ManageableBasicAuthCredentials
		cred1.username == ""
		cred1.password == "" as char[]

		def cred2 = result.get(bt2)
		cred2 instanceof  UserOAuth1aToken
		cred2.token ==""
		cred2.tokenSecret ==""


	}




}
