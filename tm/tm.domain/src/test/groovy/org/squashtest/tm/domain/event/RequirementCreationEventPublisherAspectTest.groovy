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
package org.squashtest.tm.domain.event;

import org.springframework.security.core.Authentication
import org.squashtest.tm.tools.unittest.reflection.ReflectionCategory
import org.squashtest.tm.domain.requirement.Requirement
import org.squashtest.tm.domain.requirement.RequirementVersion
import org.squashtest.tm.event.RequirementAuditor;
import org.squashtest.tm.security.UserContextHolder
import spock.lang.Ignore;
import spock.lang.Specification

/**
 * @author Gregory Fouquet
 *
 */
class RequirementCreationEventPublisherAspectTest extends Specification {
//	RequirementDao dao = new StubRequirementDao()
	RequirementAuditor auditor = Mock()
	Authentication authentication = Mock()

	def event

	def setup() {
		use (ReflectionCategory) {
			def aspect = RequirementCreationEventPublisherAspect.aspectOf()
			AbstractRequirementEventPublisher.set field: "auditor", of: aspect, to: auditor
		}

		UserContextHolder.context.authentication = authentication
		authentication.name >> "bruce dickinson"
	}
	@Ignore("deactivated by bsiri in 2013")
	def "should raise event when requirement is persisted"() {
		given:
		Requirement requirement = new Requirement(new RequirementVersion())

		when:
		dao.persist requirement

		then:
		1 * auditor.notify({event = it})
		event instanceof RequirementCreation
		event.requirementVersion == requirement.resource
		event.author == "bruce dickinson"
	}

	@Ignore("deactivated by bsiri in 2013")
	def "uninitialized auditor should not break dao usage"() {
		given:
		use (ReflectionCategory) {
			def aspect = RequirementCreationEventPublisherAspect.aspectOf()
			AbstractRequirementEventPublisher.set field: "auditor", of: aspect, to: null
		}

		and:

		when:
		dao.persist new Requirement()

		then:
		notThrown(NullPointerException)
	}

	@Ignore("deactivated by bsiri in 2013")
	def "uninitialized user context should generate 'unknown' event author"() {
		given:
		UserContextHolder.context.authentication = null
                                                                  s
		when:
		dao.persist new Requirement()

		then:
		notThrown(NullPointerException)
		1 * auditor.notify({ event = it })
		event.author == "unknown"
	}

}
