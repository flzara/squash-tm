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
import org.squashtest.tm.service.servers.UserLiveCredentials

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


import spock.lang.Specification;


class UserLiveCredentialsPersistenceFilterTest extends Specification {
	UserLiveCredentialsPersistenceFilter filter = new UserLiveCredentialsPersistenceFilter()
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
		UserLiveCredentials context = Mock()
		sessionExistsAndHolds context

		when:
		filter.doFilter request, response, chain


		then:
		// chain should not be broken
		1 * chain.doFilter(request, response)
	}

	def sessionExistsAndHolds(def context) {
		request.getSession() >> session
		request.getSession(_) >> session
		session.getAttribute(UserLiveCredentialsPersistenceFilter.BUG_TRACKER_CONTEXT_SESSION_KEY) >> context
	}

	def "should retrieve context from session and set it in context holder"() {
		given:
		UserLiveCredentials credentials = Mock()
		sessionExistsAndHolds credentials

		when:
		filter.doFilter request, response, chain

		then:
		1 * credentialsProvider.restoreLiveCredentials(credentials)
	}

	def "should store context to session after filter chain processing"() {
		given:
		UserLiveCredentials credentials = Mock()
		sessionExistsAndHolds credentials

		when:
		filter.doFilter request, response, chain


		then:
		// context persisted to session
		1 * session.setAttribute(UserLiveCredentialsPersistenceFilter.BUG_TRACKER_CONTEXT_SESSION_KEY, credentials)
		// context holder cleared
		1 * credentialsProvider.clearLiveCredentials()

	}

	def "should not store context back to session when session has been invalidated"() {
		given: "session initially holds context"
		UserLiveCredentials credentials = Mock()
		request.getSession() >> session
		session.getAttribute(_) >> credentials

		and: "session invalidated at some point"
		request.getSession(false) >> null

		when:
		filter.doFilter request, response, chain

		then:
		// no context persistence
		0 * session.setAttribute(UserLiveCredentialsPersistenceFilter.BUG_TRACKER_CONTEXT_SESSION_KEY, credentials)

	}

	def "should create context when no previously available"() {
		given:
		sessionHoldsNoContext()

		when:
		filter.doFilter request, response, chain

		then:
		1 * credentialsProvider.restoreLiveCredentials(!null)
	}

	def sessionHoldsNoContext() {
		request.getSession() >> session
		request.getSession(_) >> session
	}

	def "should eagerly store context to session when no previously available"() {
		given:
		sessionHoldsNoContext()

		when:
		filter.doFilter request, response, chain


		then:
		2 * session.setAttribute(UserLiveCredentialsPersistenceFilter.BUG_TRACKER_CONTEXT_SESSION_KEY, !null)
	}
}
