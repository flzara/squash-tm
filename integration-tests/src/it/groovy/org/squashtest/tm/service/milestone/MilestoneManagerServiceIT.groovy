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

import javax.inject.Inject

import org.springframework.transaction.annotation.Transactional
import org.squashtest.tm.domain.milestone.Milestone
import org.squashtest.tm.domain.milestone.MilestoneStatus
import org.squashtest.tm.exception.milestone.MilestoneLabelAlreadyExistsException
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.unitils.dbunit.annotation.DataSet

import spock.lang.Unroll
import spock.unitils.UnitilsSupport

@UnitilsSupport
@Transactional
class MilestoneManagerServiceIT extends DbunitServiceSpecification {

	@Inject
	MilestoneManagerService manager

	@DataSet("/org/squashtest/tm/service/milestone/MilestoneManagerServiceIT.xml")
	def "should find all milestones"(){
		given :

		when :
		def result = manager.findAll()
		then :
		result.size == 4
		result.collect{it.id} as Set == [-1, -2, -3, -4] as Set
		result.collect{it.label} as Set == ["My milestone", "My milestone 2", "My milestone 3", "My milestone 4"] as Set
		result.collect{it.status} as Set == [MilestoneStatus.PLANNED, MilestoneStatus.PLANNED, MilestoneStatus.IN_PROGRESS, MilestoneStatus.LOCKED] as Set
	}

	@DataSet("/org/squashtest/tm/service/milestone/MilestoneManagerServiceIT.xml")
	def "should change status"(){

		given :
		when :
		manager.changeStatus(-1L, MilestoneStatus.IN_PROGRESS)
		def milestone = manager.findById(-1L);
		then :
		milestone.status == MilestoneStatus.IN_PROGRESS
	}

	@DataSet("/org/squashtest/tm/service/milestone/MilestoneManagerServiceIT.xml")
	def "should delete milestone"(){

		given :
		def ids = [-1L, -4L]
		when :
		manager.removeMilestones(ids)
		def result = manager.findAll()
		then :
		result.size == 2
		result.collect{it.id} as Set == [-2, -3] as Set
		result.collect{it.label} as Set == ["My milestone 2", "My milestone 3"]  as Set
		result.collect{it.status}  as Set == [MilestoneStatus.PLANNED, MilestoneStatus.IN_PROGRESS]  as Set
	}

	@DataSet("/org/squashtest/tm/service/milestone/MilestoneManagerServiceIT.xml")
	def "label should be unique"(){
		given :
		def duplicateLabel = "My milestone 2"
		Milestone milestone = new Milestone(label:duplicateLabel)
		when :
		manager.addMilestone(milestone)
		then :
		thrown(MilestoneLabelAlreadyExistsException)
	}


	@DataSet("/org/squashtest/tm/service/milestone/MilestoneManagerServiceIT.xml")
	def "label should not contain forbiden characters"(){
		given :
		def badLabel = "Milestone |"
		Milestone milestone = new Milestone(label:badLabel)
		when :
		manager.addMilestone(milestone)
		def milestoneAdded = manager.findByName("Milestone |");
		then :
		thrown(javax.validation.ConstraintViolationException)
	}

	@Unroll("for project : #id is bound to template : #boundToTemplate")
	@DataSet("/org/squashtest/tm/service/milestone/MilestoneManagerServiceIT.xml")
	def "should know if the milestone is bound to a template"(){
		given :

		when :
		def result = manager.isBoundToATemplate(id)
		then :
		result == boundToTemplate
		where :
		id || boundToTemplate
		-1L || false
		-2L || true
		-3L || true
		-4L || false
	}

	@DataSet("/org/squashtest/tm/service/milestone/MilestoneManagerServiceIT.xml")
	def "should find milestones by names and status"() {
		given:
		def names = ["My milestone", "My milestone 3", "Whatever"]

		when:
		def res = manager.findAllByNamesAndStatus(names, MilestoneStatus.IN_PROGRESS);

		then:
		res*.label == ["My milestone 3"]
	}

	@DataSet("/org/squashtest/tm/service/milestone/MilestoneManagerServiceIT.xml")
	def "should find existing milestones names"() {
		given:
		def names = ["My milestone", "My milestone 3", "Whatever"]

		when:
		def res = manager.findExistingNames(names);

		then:
		res.containsAll(["My milestone", "My milestone 3"])
		res.size() == 2
	}

	@DataSet("/org/squashtest/tm/service/milestone/MilestoneManagerServiceIT.xml")
	def "should find existing, in preogress milestones names"() {
		given:
		def names = ["My milestone", "My milestone 3", "Whatever"]
		def status = MilestoneStatus.getAllStatusAllowingObjectBind()

		when:
		def res = manager.findBindableExistingNames(names, status);

		then:
		res == ["My milestone 3"]
	}

	/*
	 * Please see MilestoneManagerAsProjectLeaderServiceIT for informations about the dataset
	 */
	@Unroll("should  synchronize for admin : source id : #sourceId, targetId :  #targetId union : #isUnion")
	@DataSet("/org/squashtest/tm/service/milestone/MilestoneManagerService2IT.xml")
	def "should synchronize as Admin"(){
		given :
		when :
		manager.synchronize(sourceId, targetId, extendPerimeter, isUnion)
		def target = manager.findById(targetId)
		def source = manager.findById(sourceId)
		then :
		target.perimeter*.id as Set == targetProjectIds as Set
		target.projects*.id as Set == targetProjectIds as Set
		target.testCases*.id as Set == targetObjIds as Set

		target.requirementVersions*.id as Set == targetObjIds as Set

		source.perimeter*.id as Set == sourceProjectIds as Set
		source.projects*.id as Set == sourceProjectIds as Set
		source.testCases*.id as Set == sourceObjIds as Set

		source.requirementVersions*.id as Set == sourceObjIds as Set
		where :
		sourceId | targetId | extendPerimeter | isUnion  ||  sourceProjectIds           |  targetProjectIds           |    sourceObjIds                   |    targetObjIds
		-1    |     -2    |       false     |   false ||      [-1, -2, -3, -4]|  [-1, -2, -3, -4, -5, -6]|	  [-1, -3, -5, -7]|     [-1, -3, -5, -6, -7, -8, -9, -11]
		-1    |     -7    |       false     |   false ||      [-1, -2, -3, -4]|  [-3, -4, -5, -6]|	  [-1, -3, -5, -7]|     [-5, -6, -7, -8, -9, -11]
		-6    |     -7    |       false     |   false ||      [-1, -2, -3, -4]|  [-3, -4, -5, -6]|	  [-1, -3, -5, -7]|     [-5, -6, -7, -8, -9, -11]


		-1    |     -2    |       false     |   true  ||   [-1, -2, -3, -4, -5, -6]|  [-1, -2, -3, -4, -5, -6]| [-1, -3, -5, -6, -7, -8, -9, -11]|     [-1, -3, -5, -6, -7, -8, -9, -11]
		-1    |     -7    |       false     |   true  ||      [-1, -2, -3, -4]|  [-3, -4, -5, -6]|    [-1, -3, -5, -6, -7, -8]|     [-5, -6, -7, -8, -9, -11]
		-6    |     -7    |       false     |   true  ||      [-1, -2, -3, -4]|  [-3, -4, -5, -6]|    [-1, -3, -5, -6, -7, -8]|     [-5, -6, -7, -8, -9, -11]
	}
}
