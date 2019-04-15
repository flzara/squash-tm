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
package org.squashtest.tm.web.internal.filter

import org.squashtest.tm.service.servers.CredentialsProvider
import org.squashtest.tm.service.servers.UserCredentialsCache
import spock.lang.Specification

import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession

class UserCredentialsCachePersistenceFilterTest extends Specification {
	UserCredentialsCachePersistenceFilter filter = new UserCredentialsCachePersistenceFilter()
	CredentialsProvider credentialsProvider = Mock()

	// container lifecycle stuff
	HttpServletRequest request = Mock()
	HttpServletResponse response = Mock()
	HttpSession session = Mock()
	FilterChain chain = Mock()

	def setup() {
		filter.credentialsProvider = credentialsProvider
	}


	def "filter should delegate to filter chain"() {
		given:
		UserCredentialsCache cache = Mock()
		sessionExistsAndHolds cache

		when:
		filter.doFilter request, response, chain


		then:
		// chain should not be broken
		1 * chain.doFilter(request, response)
	}

	def sessionExistsAndHolds(def cache) {
		request.getSession() >> session
		request.getSession(_) >> session
		session.getAttribute(UserCredentialsCachePersistenceFilter.CREDENTIALS_CACHE_SESSION_KEY) >> cache
	}

	def "should retrieve cache from session and set it in the credentials provider"() {
		given:
		UserCredentialsCache cache = Mock()
		sessionExistsAndHolds cache

		when:
		filter.doFilter request, response, chain

		then:
		1 * credentialsProvider.restoreCache(cache)
	}

	def "should store cache to session after filter chain processing"() {
		given:
		UserCredentialsCache cache = Mock()
		sessionExistsAndHolds cache

		when:
		filter.doFilter request, response, chain


		then:
		// cache persisted to session
		1 * credentialsProvider.getCache() >> cache
		1 * session.setAttribute(UserCredentialsCachePersistenceFilter.CREDENTIALS_CACHE_SESSION_KEY, cache)
		// cache cleared
		1 * credentialsProvider.unloadCache()

	}

	def "should not store cache back to session when session has been invalidated"() {
		given: "session initially holds cache"
		UserCredentialsCache cache = Mock()
		request.getSession() >> session
		session.getAttribute(_) >> cache

		and: "session invalidated at some point"
		request.getSession(false) >> null

		when:
		filter.doFilter request, response, chain

		then:
		// no cache persistence
		0 * session.setAttribute(UserCredentialsCachePersistenceFilter.CREDENTIALS_CACHE_SESSION_KEY, cache)

	}

	def "should NOT create cache when no previously available"() {
		given:
		sessionHoldsNoCache()

		when:
		filter.doFilter request, response, chain

		then:
		0 * credentialsProvider.restoreCache(_)
	}

	def sessionHoldsNoCache() {
		request.getSession() >> session
		request.getSession(_) >> session
	}


	def "should find that the given url is excluded"(){

		given :
		def url = "/scripts/the-script.js"
		filter.addExcludePatterns "/scripts/**"

		when:
		def res = filter.matchExcludePatterns url

		then :
		res == true

	}

	def "should find that the given url is filtered"(){
		given :
		def url = "/test-cases/1"
		filter.addExcludePatterns "/scripts/**"

		when:
		def res = filter.matchExcludePatterns url

		then :
		res == false
	}

}
