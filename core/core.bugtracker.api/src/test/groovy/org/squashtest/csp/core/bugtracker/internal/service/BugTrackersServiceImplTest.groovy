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
package org.squashtest.csp.core.bugtracker.internal.service

import org.squashtest.csp.core.bugtracker.core.BugTrackerConnectorFactory
import org.squashtest.csp.core.bugtracker.core.BugTrackerRemoteException
import org.squashtest.csp.core.bugtracker.core.UnsupportedAuthenticationModeException
import org.squashtest.csp.core.bugtracker.domain.BugTracker
import org.squashtest.csp.core.bugtracker.service.BugTrackerContext
import org.squashtest.csp.core.bugtracker.service.BugTrackerContextHolder
import org.squashtest.csp.core.bugtracker.service.BugTrackersServiceImpl
import org.squashtest.csp.core.bugtracker.service.InternalBugtrackerConnector
import org.squashtest.csp.core.bugtracker.service.WrongAuthenticationPolicyException
import org.squashtest.csp.core.bugtracker.spi.BugTrackerConnector
import org.squashtest.tm.domain.servers.BasicAuthenticationCredentials
import org.squashtest.tm.domain.servers.Credentials

import static org.squashtest.tm.domain.servers.AuthenticationPolicy.*
import org.squashtest.tm.service.servers.StoredCredentialsManager
import spock.lang.Specification

//@Ignore
class BugTrackersServiceImplTest extends Specification {
	BugTrackersServiceImpl service = new BugTrackersServiceImpl()

	BugTrackerContextHolder contextHolder = Mock()
	BugTrackerConnectorFactory connectorFactory = Mock()
	StoredCredentialsManager credentialsManager = Mock();

	BugTrackerContext btcontext = Mock()
	BugTracker bt = Mock()
	InternalBugtrackerConnector btconnector = Mock()

	def setup() {

		contextHolder.getContext() >> btcontext
		connectorFactory.createConnector(bt) >> btconnector
		bt.getId() >>10L

		service.contextHolder = contextHolder
		service.bugTrackerConnectorFactory = connectorFactory
		service.credentialsManager = credentialsManager
	}


	def "should tell credentials are needed"() {
		given:
		btcontext.hasCredentials(bt) >> false
		bt.getAuthenticationPolicy() >> USER

		when:
		def needsCredentials = service.isCredentialsNeeded(bt)

		then:
		needsCredentials
	}

	def "should tell credentials are not needed because credentials are set"() {
		given:
		btcontext.hasCredentials(bt) >>true

		when:
		def needsCredentials = service.isCredentialsNeeded(bt)

		then:
		!needsCredentials
	}

	def "should tell credentials are not needed because the bugtracker uses app-level auth"() {
		given:
		bt.getAuthenticationPolicy() >> APP_LEVEL
		btcontext.hasCredentials() >> false

		when:
		def needsCredentials = service.isCredentialsNeeded(bt)

		then:
		!needsCredentials
	}

	def "should store credentials in context when the authentication policy is USER and authentication is successful"() {

		given :
		def creds = mockCredentials()
		bt.getAuthenticationPolicy() >>USER

		when:
		service.setCredentials(creds, bt)

		then:
		notThrown BugTrackerRemoteException
		1 * btcontext.setCredentials(bt, creds)
	}

	def "should not store credentials in context when they are invalid"() {
		given:
		def creds = mockCredentials()
		bt.getAuthenticationPolicy() >>USER
		btconnector.checkCredentials (_)  >> {throw new BugTrackerRemoteException("wrong !", null);}

		when:
		service.setCredentials(creds, bt)

		then:
		thrown BugTrackerRemoteException
		0 * btcontext.setCredentials(bt, creds)
	}


	def "should not store credentials in context because the auth-policy is app-level"() {
		given:
		def creds = mockCredentials()
		bt.getAuthenticationPolicy() >> APP_LEVEL


		when:
		service.setCredentials(creds, bt)

		then:
		thrown WrongAuthenticationPolicyException
		0 * btcontext.setCredentials(bt, creds)
	}

	def "should authenticate a bugtracker with user-defined credentials"(){

		given :
			bt.getAuthenticationPolicy() >> USER
			btconnector.supports(_) >> true
		and:
			def creds = mockCredentials()
			btcontext.getCredentials(bt) >> creds

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
		credentialsManager.unsecuredFindCredentials(_) >> creds

		when :
		service.connect(bt)


		then :
		1 * btconnector.authenticate(creds)

	}

	def "should fail to authenticate because the connector does not support the choosen authentication protocol"(){
		given :
		def creds = mockCredentials()
		bt.getAuthenticationPolicy() >> USER
		btcontext.getCredentials(bt) >> creds

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
