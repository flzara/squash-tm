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
package org.squashtest.csp.core.bugtracker.internal.core

import org.squashtest.csp.core.bugtracker.core.BugTrackerConnectorFactory
import org.squashtest.csp.core.bugtracker.core.UnknownConnectorKindException
import org.squashtest.csp.core.bugtracker.domain.BugTracker
import org.squashtest.csp.core.bugtracker.service.SimpleBugtrackerConnectorAdapter
import org.squashtest.csp.core.bugtracker.spi.BugTrackerConnector
import org.squashtest.csp.core.bugtracker.spi.BugTrackerConnectorProvider
import org.squashtest.tm.core.foundation.exception.NullArgumentException
import spock.lang.Specification

class BugTrackerConnectorFactoryTest extends Specification {
	BugTrackerConnectorFactory factory = new BugTrackerConnectorFactory()

	def "should create a connector of said kind"() {
		given: "a bugtracker definition"
		BugTracker bt = bugTracker()

		and: "a connector provider"
		BugTrackerConnector connector = Mock()

		BugTrackerConnectorProvider provider = Mock()
		provider.bugTrackerKind >> "foo"
		provider.createConnector(_) >> connector

		when:
		factory.providers = [provider]
		factory.registerBugTrackers()
		def res = factory.createConnector(bt)

		then:
		res instanceof SimpleBugtrackerConnectorAdapter
	}

	private BugTracker bugTracker() {
		BugTracker bt = new BugTracker(url: "http://foo", kind: "foo", name: "", iframeFriendly: true)
		return bt
	}

	def "should not register null kinded providers"() {
		given: "a null kinded provider"
		BugTrackerConnectorProvider provider = Mock()

		when:
		factory.providers = [provider]
		factory.registerBugTrackers()

		then:
		thrown(NullArgumentException)
	}

	def "should refuse to create a connector of unknown kind"() {
		given: "a bugtracker definition"
		BugTracker bt = bugTracker()

		when:
		def res = factory.createConnector(bt)

		then:
		thrown(UnknownConnectorKindException)
	}

}
