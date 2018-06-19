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
package org.squashtest.tm.service.internal.bugtracker

import org.squashtest.csp.core.bugtracker.core.UnsupportedAuthenticationModeException
import org.squashtest.csp.core.bugtracker.domain.BugTracker
import org.squashtest.tm.service.internal.bugtracker.adapter.InternalBugtrackerConnector
import org.squashtest.tm.domain.servers.BasicAuthenticationCredentials
import org.squashtest.tm.domain.servers.Credentials
import org.squashtest.tm.service.servers.CredentialsProvider
import org.squashtest.tm.service.servers.UserCredentialsCache

import static org.squashtest.tm.domain.servers.AuthenticationPolicy.*

import spock.lang.Specification

//@Ignore
class BugTrackersServiceImplTest extends Specification {
	BugTrackersServiceImpl service = new BugTrackersServiceImpl()

	CredentialsProvider credentialsProvider = Mock()
	BugTrackerConnectorFactory connectorFactory = Mock()

	UserCredentialsCache liveCredentials = Mock()
	BugTracker bt = Mock()
	InternalBugtrackerConnector btconnector = Mock()

	def setup() {

		credentialsProvider.getCache() >> liveCredentials
		connectorFactory.createConnector(bt) >> btconnector
		bt.getId() >>10L

		service.credentialsProvider = credentialsProvider
		service.bugTrackerConnectorFactory = connectorFactory
	}


	def "should tell credentials are needed"() {
		given:
		liveCredentials.hasCredentials(bt) >> false
		bt.getAuthenticationPolicy() >> USER

		when:
		def needsCredentials = service.isCredentialsNeeded(bt)

		then:
		needsCredentials
	}

	def "should tell user input are not needed because credentials are set"() {
		given:
		credentialsProvider.hasCredentials(bt) >>true

		when:
		def needsCredentials = service.isCredentialsNeeded(bt)

		then:
		!needsCredentials
	}

	def "should tell credentials are not needed because the bugtracker uses app-level auth"() {
		given:
		bt.getAuthenticationPolicy() >> APP_LEVEL
		credentialsProvider.hasCredentials() >> false

		when:
		def needsCredentials = service.isCredentialsNeeded(bt)

		then:
		!needsCredentials
	}



	def "should authenticate a bugtracker with user-defined credentials"(){

		given :
			bt.getAuthenticationPolicy() >> USER
			btconnector.supports(_) >> true
		and:
			def creds = mockCredentials()
			credentialsProvider.getCredentials(bt) >> {Optional.of(creds)}

		when :
			service.connect(bt)


		then :
		1 * btconnector.authenticate(creds)

	}


	def "should authenticate a bugtracker with app-level credentials"(){

		given :
		bt.getAuthenticationPolicy() >> APP_LEVEL
		def creds = mockCredentials()

		and:
		btconnector.supports(_) >> true
		credentialsProvider.getAppLevelCredentials(_) >> { Optional.of(creds) }

		when :
		service.connect(bt)


		then :
		1 * btconnector.authenticate(creds)

	}


	def "should fail to authenticate because the connector does not support the choosen authentication protocol"(){
		given :
		def creds = mockCredentials()
		bt.getAuthenticationPolicy() >> USER
		credentialsProvider.getCredentials(bt) >> { Optional.of(creds) }

		and :
		btconnector.supports(_) >> false

		when :
		service.connect(bt)

		then :
		thrown UnsupportedAuthenticationModeException
	}


	// ************** utilities ***************

	private Credentials  mockCredentials(){
		return new BasicAuthenticationCredentials("bob", "pwdbob".toCharArray())
	}


}
