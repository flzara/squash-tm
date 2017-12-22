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
package org.squashtest.csp.core.bugtracker.core.web

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.squashtest.csp.core.bugtracker.service.BugTrackerContext;
import org.squashtest.csp.core.bugtracker.service.BugTrackerContextHolder;
import org.squashtest.csp.core.bugtracker.web.BugTrackerContextPersistenceFilter;
import spock.lang.Specification;


class BugTrackerContextPersistenceFilterTest extends Specification {
	BugTrackerContextPersistenceFilter filter = new BugTrackerContextPersistenceFilter()
	BugTrackerContextHolder contextHolder = Mock()

	// container lifecycle stuff
	HttpServletRequest request = Mock()
	HttpServletResponse response = Mock()
	HttpSession session = Mock()
	FilterChain chain = Mock()

	def setup() {
		filter.contextHolder = contextHolder
	}


	def "filter should delegate to filter chain"() {
		given:
		BugTrackerContext context = Mock()
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
		session.getAttribute(BugTrackerContextPersistenceFilter.BUG_TRACKER_CONTEXT_SESSION_KEY) >> context
	}

	def "should retrieve context from session and set it in context holder"() {
		given:
		BugTrackerContext context = Mock()
		sessionExistsAndHolds context

		when:
		filter.doFilter request, response, chain

		then:
		1 * contextHolder.setContext(context)
	}

	def "should store context to session after filter chain processing"() {
		given:
		BugTrackerContext context = Mock()
		sessionExistsAndHolds context

		when:
		filter.doFilter request, response, chain


		then:
		// context persisted to session
		1 * session.setAttribute(BugTrackerContextPersistenceFilter.BUG_TRACKER_CONTEXT_SESSION_KEY, context)
		// context holder cleared
		1 * contextHolder.clearContext()

	}

	def "should not store context back to session when session has been invalidated"() {
		given: "session initially holds context"
		BugTrackerContext context = Mock()
		request.getSession() >> session
		session.getAttribute(_) >> context

		and: "session invalidated at some point"
		request.getSession(false) >> null

		when:
		filter.doFilter request, response, chain

		then:
		// no context persistence
		0 * session.setAttribute(BugTrackerContextPersistenceFilter.BUG_TRACKER_CONTEXT_SESSION_KEY, context)

	}

	def "should create context when no previously available"() {
		given:
		sessionHoldsNoContext()

		when:
		filter.doFilter request, response, chain

		then:
		1 * contextHolder.setContext(!null)
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
		2 * session.setAttribute(BugTrackerContextPersistenceFilter.BUG_TRACKER_CONTEXT_SESSION_KEY, !null)
	}
}
