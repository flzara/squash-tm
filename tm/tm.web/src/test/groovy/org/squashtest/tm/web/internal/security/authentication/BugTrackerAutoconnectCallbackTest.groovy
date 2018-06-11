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

import org.springframework.security.core.context.SecurityContext
import org.squashtest.csp.core.bugtracker.core.BugTrackerRemoteException
import org.squashtest.csp.core.bugtracker.domain.BugTracker
import org.squashtest.tm.domain.project.GenericProject
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.domain.servers.AuthenticationPolicy
import org.squashtest.tm.domain.servers.AuthenticationProtocol
import org.squashtest.tm.domain.servers.BasicAuthenticationCredentials
import org.squashtest.tm.service.bugtracker.BugTrackerFinderService
import org.squashtest.tm.service.bugtracker.BugTrackersLocalService
import org.squashtest.tm.service.project.ProjectFinder
import org.squashtest.tm.service.servers.CredentialsProvider
import org.squashtest.tm.service.servers.UserCredentialsCache
import org.squashtest.tm.tools.unittest.reflection.ReflectionCategory
import spock.lang.Specification
import org.squashtest.tm.web.internal.security.authentication.BugTrackerAutoconnectCallback.AsynchronousBugTrackerAutoconnect

/**
 * Does not exactly test BugTrackerAutoconnectCallback but rather its asynchronous inner worker class,
 * AsynchronousBugTrackerAutoconnect
 *
 *
 */
class BugTrackerAutoconnectCallbackTest extends Specification{

	AsynchronousBugTrackerAutoconnect auto

	String user = "bob"
	def springsecCredentials = "eventpassword"
	UserCredentialsCache credentialsCache = Mock()

	BugTrackersLocalService bugTrackersLocalService  = Mock()
	ProjectFinder projectFinder = Mock()
	BugTrackerFinderService bugTrackerFinder = Mock()
	CredentialsProvider credentialsProvider = Mock()

	def setup(){

		auto = new AsynchronousBugTrackerAutoconnect(
			user : user,
			springsecCredentials: springsecCredentials,
			credentialsCache: credentialsCache,
			projectFinder: projectFinder,
			bugTrackerFinder: bugTrackerFinder,
			credentialsProvider: credentialsProvider,
			bugTrackersLocalService: bugTrackersLocalService
		)

	}

	def "should retrieve the bugtrackers that require autoconnection"(){

		given :
		def bt1 = new BugTracker()
		def bt2 = new BugTracker()

		and:
		projectFinder.findAllReadable() >> mockProjects((1L..3L))
		bugTrackerFinder.findDistinctBugTrackersForProjects(_) >> [bt1, bt2]

		when :
		def res = auto.findBugTrackers()

		then:
		res == [bt1, bt2]

	}

	def "the authentication event is usable for fallback credentials"(){

		given:
			BugTracker bt = new BugTracker(authenticationPolicy: AuthenticationPolicy.USER)

		when:
			def res = auto.canTryUsingEvent bt

		then:
			res == true

	}

	def "the authentication event is not usable for fallback because of auth policy"(){

		given:
		BugTracker bt = new BugTracker(authenticationPolicy: AuthenticationPolicy.APP_LEVEL)

		when:
		def res = auto.canTryUsingEvent bt

		then:
		res == false

	}

	def "the authentication event is not usable for fallback because evt credentials are not suitable"(){

		given:
		BugTracker bt = new BugTracker(authenticationPolicy: AuthenticationPolicy.USER)
		auto.springsecCredentials = new Object()

		when:
		def res = auto.canTryUsingEvent bt

		then:
		res == false

	}

	def "create credentials from the auth event"(){

		expect:
		auto.buildFromAuthenticationEvent().username == "bob" &&
		auto.buildFromAuthenticationEvent().password == "eventpassword" as char[]

	}


	def "should retrieve the credentials from the authentication provider (personal credentials)"(){

		given :
		BugTracker bt = new BugTracker(authenticationPolicy:  AuthenticationPolicy.USER)
		def creds = new BasicAuthenticationCredentials("bob", "storedsecret")

		when :
		def res = auto.fetchCredentialsOrNull bt

		then :
		1 * credentialsProvider.getCredentials(bt) >> Optional.of(creds)
		0 * credentialsProvider.getAppLevelCredentials(bt) >> Optional.empty()
		res == creds
	}


	def "should retrieve the credentials from the authentication provider (app-level credentials)"(){

		given :
		BugTracker bt = new BugTracker(authenticationPolicy:  AuthenticationPolicy.APP_LEVEL)
		def creds = new BasicAuthenticationCredentials("server", "serversecret")

		when :
		def res = auto.fetchCredentialsOrNull bt

		then :
		0 * credentialsProvider.getCredentials(bt) >> Optional.empty()
		1 * credentialsProvider.getAppLevelCredentials(bt) >> Optional.of(creds)
		res == creds
	}

	def "should retrieve the credentials using fallback"(){

		given :
		BugTracker bt = new BugTracker(authenticationPolicy:  AuthenticationPolicy.USER)
		credentialsProvider.getCredentials(bt) >> Optional.empty()

		when:
		def res = auto.fetchCredentialsOrNull(bt)

		then:
		res.username == "bob"
		res.password == "eventpassword" as char[]


	}


	def "attempts authentication on the given server"(){

		// note : here the event credentials will be used (bob, eventpassword)

		given :
		BugTracker bt = new BugTracker(
			authenticationPolicy: AuthenticationPolicy.USER,
			authenticationProtocol: 	AuthenticationProtocol.BASIC_AUTH
		)

		and:
		credentialsProvider.getCredentials(_) >> Optional.empty()

		when :
		auto.attemptAuthentication bt

		then :
		1 * bugTrackersLocalService.validateCredentials(bt, { credentials ->
			credentials instanceof BasicAuthenticationCredentials &&
				credentials.username == "bob" &&
				credentials.password == "eventpassword" as char[]

		}, true);

	}

	def "should not attempt authentication because no credentials could be found nor built"(){

		given :
		BugTracker bt = new BugTracker(
			authenticationPolicy: AuthenticationPolicy.APP_LEVEL,
			authenticationProtocol: 	AuthenticationProtocol.BASIC_AUTH
		)

		and:
		credentialsProvider.getAppLevelCredentials(_) >> Optional.empty()

		when :
		auto.attemptAuthentication bt

		then :
		0 * bugTrackersLocalService.validateCredentials(_)
	}


	def "should make a normal attempt for authenticating on the server"(){

		given :
		projectFinder.findAllReadable() >> mockProjects((1L..3L))
		bugTrackerFinder.findDistinctBugTrackersForProjects(_) >> [mockBt()]

		and :
		credentialsProvider.getCredentials(_) >> Optional.empty()

		when :
		auto.run()

		then :
		1 * credentialsProvider.restoreCache(credentialsCache)
		1 * credentialsProvider.unloadCache()
		notThrown(Exception)

	}



	def "authentication failed but the app flow should not crash for any exception (errors can crash it though)"(){

		given :
		projectFinder.findAllReadable() >> mockProjects((1L..3L))
		bugTrackerFinder.findDistinctBugTrackersForProjects(_) >> [mockBt()]

		and :
		credentialsProvider.getCredentials(_) >> Optional.empty()
		bugTrackersLocalService.validateCredentials(_) >> { new BugTrackerRemoteException()}

		when :
		auto.run()

		then :
		1 * credentialsProvider.restoreCache(credentialsCache)
		1 * credentialsProvider.unloadCache()
		notThrown(Exception)


	}


	// ****** utils *********

	def mockProjects(ids){
		ids.collect{ id ->
			Project p
			use(ReflectionCategory){
				p = new Project()
				GenericProject.set field: "id", of: p, to: id
			}
			return p
		}
	}

	def mockBt(){
		BugTracker bt = new BugTracker(
			authenticationPolicy: AuthenticationPolicy.USER,
			authenticationProtocol: AuthenticationProtocol.BASIC_AUTH
		)

		return bt
	}

}
