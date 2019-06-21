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
package org.squashtest.tm.service.internal.user

import org.squashtest.csp.core.bugtracker.core.BugTrackerNoCredentialsException
import org.squashtest.csp.core.bugtracker.core.UnsupportedAuthenticationModeException
import org.squashtest.csp.core.bugtracker.domain.BugTracker
import org.squashtest.tm.domain.servers.AuthenticationProtocol
import org.squashtest.tm.domain.servers.Credentials
import org.squashtest.tm.service.bugtracker.BugTrackersService
import org.squashtest.tm.service.internal.repository.BugTrackerDao
import org.squashtest.tm.service.internal.servers.ManageableBasicAuthCredentials
import org.squashtest.tm.service.internal.servers.UserOAuth1aToken
import org.squashtest.tm.service.servers.ManageableCredentials
import spock.lang.Specification

class UserAccountServiceImplTest extends Specification {

	UserAccountServiceImpl service = new UserAccountServiceImpl()

	BugTrackersService bugTrackerService = Mock()
	BugTrackerDao bugTrackerDao = Mock()

	def setup(){

		service.bugTrackerService = bugTrackerService
		service.bugTrackerDao = bugTrackerDao
	}


	def "the test of user bugtracker credentials is a success when the credentials exist, have the right protocol and are accepted by the bugtracker"(){

		given: "the bugtrackers"
		BugTracker bt = Mock()
		bugTrackerDao.getOne(_) >> bt

		and: "the credentials"
		ManageableCredentials mc = Mock()
		mc.build(_,bt,_) >> Mock(Credentials)

		when:
		service.testCurrentUserCredentials(10, mc)

		then:
		notThrown(Exception)
	}

//A verifier

	def "the test of user butracker credentials fails when the credentials doesn't exist"(){

		given: "the bugtrackers"
		BugTracker bt =Mock()
		bugTrackerDao.getOne(_) >> bt

		and: "the credentials"
		ManageableCredentials mc = Mock()
		mc == null

		when:
		service.testCurrentUserCredentials(10, mc)

		then:
		thrown BugTrackerNoCredentialsException

	}


/*
	def "the test of user bugtracker credentials fails when the credentials exist, have the right protocol but are rejected by the bugtracker"(){

		given:

		bugTrackerService.testCredentials(_) >> { throw new BugTrackerNoCredentialsException("oh noooz")  }


		when:

		then:
		thrown BugTrackerNoCredentialsException

	}

 */
/*

	def "the test of user bugtracker credentials fails when the credentials exist, but have the wrong protocol"(){

		given: "the bugtracker"
		def bt1 = Mock(BugTracker){
			getAuthenticationProtocol() >> AuthenticationProtocol.BASIC_AUTH
		}

		def bt2 = Mock(BugTracker){
			getAuthenticationProtocol() >> AuthenticationProtocol.OAUTH_1A
		}

		and: "the credentials"
		def cred1 = new ManageableBasicAuthCredentials("Bob", "bob")
		def cred2 = new UserOAuth1aToken("123", "ABC")


		when:

		then:
		thrown UnsupportedAuthenticationModeException

	}
*/



}
