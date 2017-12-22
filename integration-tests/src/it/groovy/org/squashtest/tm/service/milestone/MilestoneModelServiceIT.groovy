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
package org.squashtest.tm.service.milestone

import org.spockframework.util.NotThreadSafe
import org.springframework.transaction.annotation.Transactional
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.squashtest.tm.service.internal.dto.json.JsonMilestone
import org.squashtest.tm.service.internal.milestone.MilestoneModelServiceImpl
import org.unitils.dbunit.annotation.DataSet
import spock.unitils.UnitilsSupport

import javax.inject.Inject

@UnitilsSupport
@Transactional
@NotThreadSafe
@DataSet("MilestoneModelService.sandbox.xml")
class MilestoneModelServiceIT extends DbunitServiceSpecification {


	@Inject
	private MilestoneModelServiceImpl milestoneModelService


	def "should find used milestone ids"(){
		when:
		def usedMilestonesIds = milestoneModelService.findUsedMilestoneIds(projectIds)

		then:
		usedMilestonesIds.sort() == milestoneIds.sort()

		where:
		projectIds		||	milestoneIds
		[]				||	[]
		[-1L]			||	[-1L,-2L,-3L]
		[-1L,-2L]		||	[-1L,-2L,-3L]
		[-2L]			||	[-1L]
	}

	def "should find milestones models"() {
		given:
		List<Long> milestoneIds = [-1L, -2L, -3L, -4L]

		when:
		def milestoneModels = milestoneModelService.findJsonMilestones(milestoneIds)

		then:
		milestoneModels.size() == 4
		def milestone1 = milestoneModels.get(-1L)
		milestone1.getId() == -1L
		milestone1.getLabel() == "My milestone"
		!milestone1.canEdit
		!milestone1.canCreateDelete
		milestone1.getOwnerLogin() == "bob"

		def milestone3 = milestoneModels.get(-3L)
		milestone3.getId() == -3L
		milestone3.getLabel() == "My milestone 3"
		milestone3.canEdit
		milestone3.canCreateDelete
		milestone3.getOwnerLogin() == "bob"
	}

	def "should find milestone binding for projects"(){
		given:
		List<Long> projectIds = [-1L,-2L,-3L]

		when:
		def milestoneByProject = milestoneModelService.findMilestoneByProject(projectIds)

		then:
		milestoneByProject.size() == 3

		def jsonMilestones1 = milestoneByProject.get(-1L)
		jsonMilestones1.collect{it.id}.sort() == [-3L,-2L,-1L]

		def jsonMilestones2 = milestoneByProject.get(-2L)
		jsonMilestones2.collect{it.id}.sort() == [-1L]

		def jsonMilestones3 = milestoneByProject.get(-3L)
		jsonMilestones3.collect{it.id}.sort() == []
	}

}
