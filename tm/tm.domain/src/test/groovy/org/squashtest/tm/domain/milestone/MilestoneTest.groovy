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
package org.squashtest.tm.domain.milestone

import org.squashtest.tm.domain.requirement.Requirement
import org.squashtest.tm.domain.requirement.RequirementVersion
import spock.lang.Specification

/**
 * @author Gregory Fouquet
 * @since 1.14.0
 */
class MilestoneTest extends Specification {
	def "version should not be already bound"() {
		given:
		RequirementVersion ver = new RequirementVersion()
		Requirement req = new Requirement(ver)

		and:
		Milestone m = new Milestone()

		expect:
		!m.isOneVersionAlreadyBound(ver)
	}

	def "some version should be bound"() {
		given:
		RequirementVersion ver = new RequirementVersion()
		Requirement req = new Requirement(ver)
		req.name = "yolo"

		and:
		req.increaseVersion()
		RequirementVersion latest = req.currentVersion

		and:
		Milestone m = new Milestone()
		m.bindRequirementVersion(latest)
		latest.getMilestones().add(m)

		expect:
		m.isOneVersionAlreadyBound(ver)
	}
}
