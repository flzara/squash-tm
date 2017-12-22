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
package org.squashtest.tm.service.event

import javax.persistence.EntityManager

import org.squashtest.tm.tools.unittest.reflection.ReflectionCategory
import org.squashtest.tm.domain.event.RequirementAuditEvent
import org.squashtest.tm.domain.event.RequirementCreation
import org.squashtest.tm.domain.event.RequirementPropertyChange
import org.squashtest.tm.domain.requirement.RequirementStatus
import org.squashtest.tm.domain.requirement.RequirementVersion
import org.squashtest.tm.service.internal.event.StatusBasedRequirementAuditor;

import spock.lang.Specification
import spock.lang.Unroll

class StatusBasedRequirementAuditorTest extends Specification {
	StatusBasedRequirementAuditor auditor= new StatusBasedRequirementAuditor()
	EntityManager entityManager = Mock()

	def setup() {
		auditor.entityManager = entityManager
	}

	@Unroll("should audit status change from #initialStatus to #newStatus")
	def "should audit any status change"() {
		given:
		RequirementPropertyChange event = new RequirementPropertyChange()

		use (ReflectionCategory) {
			RequirementPropertyChange.set field: "propertyName", of: event, to: "status"
			RequirementPropertyChange.set field: "oldValue", of: event, to: initialStatus.toString()
			RequirementPropertyChange.set field: "newValue", of: event, to: newStatus.toString()
		}

		when:
		auditor.notify(event)

		then:
		1 * entityManager.persist(event)

		where:
		initialStatus                      | newStatus
		RequirementStatus.WORK_IN_PROGRESS | RequirementStatus.UNDER_REVIEW
		RequirementStatus.UNDER_REVIEW     | RequirementStatus.APPROVED
		RequirementStatus.APPROVED         | RequirementStatus.OBSOLETE
	}

	def "should audit any requirement creation"() {
		given:
		RequirementCreation event = new RequirementCreation()

		when:
		auditor.notify(event)

		then:
		1 * entityManager.persist(event)
	}

	@Unroll("should audit #changedProperty property change of an 'under review' requirement")
	def "should audit any property change of an 'under review' requirement"() {
		given:
		RequirementVersion req = Mock()
		req.status >> RequirementStatus.UNDER_REVIEW

		and:
		RequirementPropertyChange event = new RequirementPropertyChange()

		use (ReflectionCategory) {
			RequirementPropertyChange.set field: "propertyName", of: event, to: changedProperty
			RequirementAuditEvent.set field: "requirementVersion", of: event, to: req
		}

		when:
		auditor.notify(event)

		then:
		1 * entityManager.persist(event)

		where:
		changedProperty << ["name", "reference", "description", "criticality"]
	}

	@Unroll("should not audit #changedProperty property change of a requirement not under review")
	def "should not audit any property change of a requirement not under review"() {
		given:
		RequirementVersion req = Mock()
		req.status >> requirementStatus

		and:
		RequirementPropertyChange event = new RequirementPropertyChange()

		use (ReflectionCategory) {
			RequirementPropertyChange.set field: "propertyName", of: event, to: changedProperty
			RequirementAuditEvent.set field: "requirementVersion", of: event, to: req
		}

		when:
		auditor.notify(event)

		then:
		0 * entityManager.persist(_)

		where:
		changedProperty | requirementStatus
		"name"          | RequirementStatus.APPROVED
		"reference"     | RequirementStatus.APPROVED
		"description"   | RequirementStatus.APPROVED
		"criticality"   | RequirementStatus.APPROVED
		"name"          | RequirementStatus.OBSOLETE
		"reference"     | RequirementStatus.OBSOLETE
		"description"   | RequirementStatus.OBSOLETE
		"criticality"   | RequirementStatus.OBSOLETE
		"name"          | RequirementStatus.WORK_IN_PROGRESS
		"reference"     | RequirementStatus.WORK_IN_PROGRESS
		"description"   | RequirementStatus.WORK_IN_PROGRESS
		"criticality"   | RequirementStatus.WORK_IN_PROGRESS
	}

}
