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
package org.squashtest.tm.service.internal.servers


import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.context.SecurityContextImpl
import org.squashtest.tm.core.foundation.exception.NullArgumentException
import org.squashtest.tm.domain.servers.AuthenticationPolicy
import org.squashtest.tm.domain.servers.BasicAuthenticationCredentials
import org.squashtest.tm.domain.servers.Credentials
import org.squashtest.tm.domain.servers.OAuth1aCredentials
import org.squashtest.tm.domain.servers.ThirdPartyServer
import org.squashtest.tm.service.servers.ManageableCredentials
import org.squashtest.tm.service.servers.StoredCredentialsManager
import org.squashtest.tm.service.servers.UserCredentialsCache
import spock.lang.Specification

class CredentialsProviderImplTest extends Specification{

	CredentialsProviderImpl provider = new CredentialsProviderImpl()
	StoredCredentialsManager credentialsManager = Mock()

	UserCredentialsCache cache = new UserCredentialsCache("Mike (defined by general setup)")


	def setup(){
		provider.storedCredentialsManager = credentialsManager
		provider.threadedCache.set(cache)
	}



	// ***************** user cache lifecycle ******************************

	def "should create a new user cache based on the security context if none is present"(){

		given:
		// undo the setup clause
		provider.threadedCache.remove()
		// create a new sec context
		mockAuthContext("Bob (defined by specific setup)")

		when:
		def newCache = provider.createDefaultOrDie()

		then:
		newCache != cache
		newCache.user == "Bob (defined by specific setup)"

	}

	def "should fail to create a new user cache if the security context is not present"(){
		given:
		// undo the setup clause, but do not create a security context
		provider.threadedCache.remove()

		when:
		provider.createDefaultOrDie()

		then:
		thrown IllegalStateException
	}


	def "should return the current cache (when defined)"(){

		// the cache here is defined, see the test setup
		expect:
		provider.getCache() == cache

	}



	def "should return the current cache, creating it on the fly if not yet defined"(){

		given:
		// undo the setup clause
		provider.threadedCache.remove()
		// create a new sec context
		mockAuthContext("Bob (defined by specific setup)")

		when:
		def createdCache = provider.getCache()

		then:
		createdCache != cache
		createdCache.getUser() == "Bob (defined by specific setup)"

	}



	def "should set the given user cache as the current user context cache"(){

		given:
		def newCache = new UserCredentialsCache("Bob (defined by specific setup)")

		when:
		provider.restoreCache(newCache)

		then:
		provider.getCache() == newCache

	}

	def "testing guard clause : null argument in restoreCache"(){
		when:
		provider.restoreCache(null)

		then:
		thrown NullArgumentException
	}


	def "should remove the cache and clear the thread context"(){

		when:
		provider.unloadCache()
		
		then:
		provider.threadedCache.get() == null
		
	}


	// ***************** user cache usage **********************************

	def "should not cache the credentials despite cachability because the server auth policy is set to 'app level'"(){

		given:
		ThirdPartyServer server = server(AuthenticationPolicy.APP_LEVEL)

		def credentials = cachableCreds()

		when:
		provider.cacheCredentials(server, credentials)

		then:
		provider.getCredentialsFromCache(server) == null

	}


	def "should not cache the credentials despite server auth policy because credentials are not cachable"(){

		given:
		ThirdPartyServer server = server(AuthenticationPolicy.APP_LEVEL)
		def credentials = notCachableCreds()

		when:
		provider.cacheCredentials(server, credentials)

		then:
		provider.getCredentialsFromCache(server) == null

	}

	def "should remove credentials from the cache"(){

		given:
		ThirdPartyServer server = server()
		Credentials creds = cachableCreds()
		cache(server, creds)

		when:
		provider.uncacheCredentials(server)

		then:
		provider.hasCredentials(server) == false

	}


	def "should return the current user"(){
		expect:
		provider.currentUser() == "Mike (defined by general setup)"
	}

	// ****************** crdentials retrieval *****************************

	def "should say that the given server has no credentials available (for the current user)"(){
		expect:
		 provider.hasCredentials(server()) == false
	}


	def "should say that the given server has credentials available (for the current user)"(){

		given :
		ThirdPartyServer server = server()
		Credentials creds = cachableCreds()

		// this time there are credentials available (in the cache)
		cache(server, creds)

		when :
		def res = provider.hasCredentials(server)

		then :
		res == true

	}


	def "should say that the given server has no credentials available (app level)"(){
		expect:
		provider.hasAppLevelCredentials(server()) == false
	}

	def "should says that the given server has credentials available (app level)"(){

		given :
		ThirdPartyServer server = server()
		Credentials creds = cachableCreds()
		storeAppLevel(server.getId(), creds)

		when :
		def res = provider.hasAppLevelCredentials(server)

		then :
		res == true
	}


	def "should return the credentials for the current user from the cache"(){

		ThirdPartyServer server = server()
		Credentials creds = cachableCreds()
		cache(server, creds)

		when :
		def res = provider.getCredentialsFromCache(server)

		then :
		res.username == "Mike (defined by general setup)"

	}


	def "should return the credentials for the current user from the store"(){

		given :
		ThirdPartyServer server = server()
		Credentials creds = cachableCreds()
		storeForUser(server.getId(), creds)

		when :
		def res = provider.getUserCredentialsFromStore(server)

		then :
		res.username == "Mike (defined by general setup)"

	}



	// ******************* scaffolding **************

	def cache(server, credentials){
		provider.threadedCache.get().cache.put(server.getId(), credentials)
	}

	def storeForUser(serverId, credentials){
		def manageable = Mock(ManageableCredentials){
			build(_,_,_) >> credentials
		}
		credentialsManager.findUserCredentials(serverId, _) >> manageable
	}


	def storeAppLevel(serverId, credentials) {
		def manageable = Mock(ManageableCredentials) {
			build(_, _, _) >> credentials
		}
		credentialsManager.unsecuredFindAppLevelCredentials(serverId) >> manageable
	}

	def server(authpolicy){
		def server = Mock(ThirdPartyServer){
			getId() >> 10L
		}
		if (authpolicy != null){
			server.getAuthenticationPolicy() >> authpolicy
		}

		return server

	}

	def cachableCreds(){
		return new BasicAuthenticationCredentials("Mike (defined by general setup)", "PWD".toCharArray())
	}

	def notCachableCreds(){
		return new OAuth1aCredentials("consumer", "secret", "token", "tokensecret", OAuth1aCredentials.SignatureMethod.HMAC_SHA1)
	}


	def mockAuthContext(name){
		SecurityContextHolder.setContext(
			new SecurityContextImpl(Mock(Authentication){
				getName() >> name
			})
		)
	}


}
