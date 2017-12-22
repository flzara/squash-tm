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

import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.ContextHierarchy
import org.springframework.transaction.annotation.Transactional
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.squashtest.it.stub.security.UserContextHelper
import org.squashtest.tm.domain.milestone.Milestone
import org.unitils.dbunit.annotation.DataSet

import spock.lang.Unroll
import spock.unitils.UnitilsSupport

@UnitilsSupport
@Transactional
@ContextHierarchy(
	// using an alternate stubbing for the permission evaluation
	@ContextConfiguration(name="permissioncontext", classes = [MilestoneTestingPermissionSpecConfig], inheritLocations=false)	

)
class MilestoneManagerAsProjectLeaderServiceIT extends DbunitServiceSpecification {
	@Inject
	MilestoneManagerService manager
	
	def setup(){
		UserContextHelper.setUsername("chef")
	}

	/*
	 * You're not prepared !
	 * Warning: the dataset is quite huge, edit the tests at your own risk !
	 * The CustomDbunitServiceSpecification used here is done so the current user is "chef"
	 * And he is project manager on odd project (-1, -3, -5 and -7 in the current dataset).
	 * Milestone list :
	 * -1	GLOBAL              admin          IN_PROGRESS
	 * -2    GLOBAL              admin          IN_PROGRESS
	 * -3    GLOBAL              admin          PLANNED
	 * -4    GLOBAL              admin          LOCKED
	 * -5    GLOBAL              admin          FINISHED
	 * -6    RESTRICTED          chef           IN_PROGRESS
	 * -7    RESTRICTED          chef           IN_PROGRESS
	 * -8    RESTRICTED          chef2          IN_PROGRESS
	 * -9    RESTRICTED          chef2          IN_PROGRESS
	 * -10   RESTRICTED          chef2         PLANNED
	 * -11   RESTRICTED          chef2         LOCKED
	 * -12   RESTRICTED          chef2         FINISHED
	 * -13   RESTRICTED          chef          IN_PROGRESS
	 *
	 *
	 * Milestone perimeter and projects :
	 *
	 * M-1, M-6, M-8 : P-1, P-2, P-3, P-4
	 * M-2, M-7, M-9 : P-3, P-4, P-5, P-6
	 *
	 * TC, CAMP and ReqV project appartenance
	 * P-1 : -1, -2
	 * P-2 : -3, -4
	 * P-3 : -5, -6
	 * P-4 : -7, -8
	 * P-5 : -9, -10
	 * P-6 : -11, -12
	 *
	 * TC, CAMP and ReqV milestone binding
	 * M-1, M-6, M-8 : -1, -3, -5, -7
	 * M-2, M-7, M-9 : -6, -8, -9, -11
	 */
	@DataSet("/org/squashtest/tm/service/milestone/MilestoneManagerService2IT.xml")
	def "should get all milestone i can see"(){
		given :
		when :
		def allICanSee = manager.findAllVisibleToCurrentManager();
		then :
		allICanSee*.id as Set == [-1, -2, -3, -4, -5, -6, -7, -8 , -9, -13] as Set
	}

	@DataSet("/org/squashtest/tm/service/milestone/MilestoneManagerService2IT.xml")
	def "should get all milestone i can edit"(){
		given :
		when :
		def editableMilestones = manager.findAllIdsOfEditableMilestone()
		then :
		editableMilestones as Set == [-6, -7, -13] as Set
	}

	@Unroll("should  synchronize for PM : source id : #sourceId, targetId :  #targetId union : #isUnion extendPerimeter : #extendPerimeter")
	@DataSet("/org/squashtest/tm/service/milestone/MilestoneManagerService2IT.xml")
	def "legal synchronize case for project manager"(){
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
		sourceId | targetId | extendPerimeter | isUnion ||  sourceProjectIds       |  targetProjectIds       |    sourceObjIds                           |    targetObjIds
		   -1    |     -7    |       false     |   false ||      [-1, -2, -3, -4]       |  [-3, -4, -5, -6]           |	  [-1, -3, -5, -7]               |     [-5, -6, -7, -8, -9, -11]
		   -1    |     -9    |       false     |   false ||      [-1, -2, -3, -4]       |  [-3, -4, -5, -6]           |	  [-1, -3, -5, -7]               |     [-5, -6, -8, -9, -11]
		   -6    |     -7    |       false     |   false ||      [-1, -2, -3, -4]       |  [-3, -4, -5, -6]           |	  [-1, -3, -5, -7]               |     [-5, -6, -7, -8, -9, -11]
		   -6    |     -9    |       false     |   false ||      [-1, -2, -3, -4]       |  [-3, -4, -5, -6]           |	  [-1, -3, -5, -7]               |     [-5, -6, -8, -9, -11]
		   -8    |     -7    |       false     |   false ||      [-1, -2, -3, -4]       |  [-3, -4, -5, -6]           |	  [-1, -3, -5, -7]               |     [-5, -6, -7, -8, -9, -11]
		   -8    |     -9    |       false     |   false ||      [-1, -2, -3, -4]       |  [-3, -4, -5, -6]           |	  [-1, -3, -5, -7]               |     [-5, -6, -8, -9, -11]
			

			-1    |     -7    |       true      |   false ||      [-1, -2, -3, -4]       |  [-1, -2, -3, -4, -5, -6]     |    [-1, -3, -5, -7]             |     [-1, -3, -5, -6, -7, -8, -9, -11]
			-1    |     -9    |       true      |   false ||      [-1, -2, -3, -4]       |  [-3, -4, -5, -6]           |    [-1, -3, -5, -7]               |     [-5, -6, -8, -9, -11]
			-6    |     -7    |       true      |   false ||      [-1, -2, -3, -4]       |  [-1, -2, -3, -4, -5, -6]     |    [-1, -3, -5, -7]             |     [-1, -3, -5, -6, -7, -8, -9, -11]
			-6    |     -9    |       true      |   false ||      [-1, -2, -3, -4]       |  [-3, -4, -5, -6]           |    [-1, -3, -5, -7]               |     [-5, -6, -8, -9, -11]
			-8    |     -7    |       true      |   false ||      [-1, -2, -3, -4]       |  [-1, -2, -3, -4, -5, -6]     |    [-1, -3, -5, -7]             |     [-1, -3, -5, -6, -7, -8, -9, -11]
			-8    |     -9    |       true      |   false ||      [-1, -2, -3, -4]       |  [-3, -4, -5, -6]           |    [-1, -3, -5, -7]               |     [-5, -6, -8, -9, -11]


			-6    |     -7    |       false     |   true  ||      [-1, -2, -3, -4] |  [-3, -4, -5, -6]     |    [-1, -3, -5, -6, -7, -8]        |     [-5, -6, -7, -8, -9, -11]
			-6    |     -9    |       false     |   true  ||      [-1, -2, -3, -4] |  [-3, -4, -5, -6]     |    [-1, -3, -5, -6, -7, -8]        |     [-5, -6, -8, -9, -11]
			-8    |     -7    |       false     |   true  ||      [-1, -2, -3, -4] |  [-3, -4, -5, -6]     |    [-1, -3, -5, -6, -7]            |     [-5, -6, -7, -8, -9, -11]
			-8    |     -9    |       false     |   true  ||      [-1, -2, -3, -4] |  [-3, -4, -5, -6]     |    [-1, -3, -5, -6, -7]            |     [-5, -6, -8, -9, -11]
	
	}



	@Unroll("should not synchronize with illegal condition for PM : source id : #sourceId, targetId :  #targetId union : #isUnion")
	@DataSet("/org/squashtest/tm/service/milestone/MilestoneManagerService2IT.xml")
	def "should not synchronize with illegal conditition for project manager "(){
		given :
		when :
		manager.synchronize(sourceId, targetId, false, isUnion)
		then :

		thrown(IllegalArgumentException)

		where :
		sourceId | targetId | isUnion ||  _
			-1    |     -2    | false   ||  _
			-1    |     -3    | false   ||  _
			-1    |     -4    | false   ||  _
			-1    |     -5    | false   ||  _
			-1    |     -10   | false   ||  _
			-1    |     -11   | false   ||  _
			-1    |     -2    | true    ||  _
			-1    |     -3    | true    ||  _
			-1    |     -4    | true    ||  _
			-1    |     -5    | true    ||  _
			-1    |     -10   | true    ||  _
			-1    |     -11   | true    ||  _
			-1    |     -12   | true    ||  _
			-1    |     -6    | true    ||  _
			-1    |     -7    | true    ||  _
			-1    |     -8    | true    ||  _
			-1    |     -9    | true    ||  _
			-6    |     -2    | true    ||  _
			-6    |     -3    | true    ||  _
			-6    |     -4    | true    ||  _
			-6    |     -5    | true    ||  _
			-6    |     -10   | true    ||  _
			-6    |     -11   | true    ||  _
		
			

	}

	@Unroll("should clone milestone for project manager motherId :#motherId   bindToRequirements: #bindToRequirements  bindToTestCases : #bindToTestCases  bindToCampaigns : #bindToCampaigns")
	@DataSet("/org/squashtest/tm/service/milestone/MilestoneManagerService2IT.xml")
	def "should clone milestone for project manager "(){
		given :
		Milestone template = manager.findById(-13)
		Milestone milestone = new Milestone(range: template.range, owner : template.owner, status : template.status, endDate : template.endDate, description : "", label:"clone")

		when :
			manager.cloneMilestone(motherId, milestone, bindToRequirements, bindToTestCases)

		then :
		milestone.perimeter*.id as Set == targetProjectIds as Set
		milestone.projects*.id as Set == targetProjectIds as Set
		milestone.testCases*.id as Set == targetTcIds as Set
		milestone.campaigns*.id as Set == targetCampIds as Set
		milestone.requirementVersions*.id as Set == targetReqVIds as Set


		where :
		motherId |   bindToRequirements | bindToTestCases   || targetProjectIds      | targetReqVIds      | targetTcIds       | targetCampIds
			-1L   |      false           |     false        ||   [-1, -3]            |  []                |     []            |     []
			-2L   |      false           |     false        ||   [-3, -5]            |  []                |     []            |     []
			-6L   |      false           |     false        ||   [-1, -2, -3, -4]    |  []                |     []            |     []
			-7L   |      false           |     false        ||   [-3, -4, -5, -6]    |  []                |     []            |     []
			-8L   |      false           |     false        ||   [-1, -3]            |  []                |     []            |     []
			-9L   |      false           |     false        ||   [-3, -5]            |  []                |     []            |     []
	 
			-1L   |       true           |      true        ||   [-1, -3]            |  [-1, -5]          |     [-1, -5]      |     []
			-2L   |       true           |      true        ||   [-3, -5]            |  [-6, -9]          |     [-6, -9]      |     []
			-6L   |       true           |      true        ||   [-1, -2, -3, -4]    |  [-1, -3 ,-5 ,-7]  | [-1, -3 ,-5 ,-7]  |     []
			-7L   |       true           |      true        ||   [-3, -4, -5, -6]    |  [-6, -8, -9, -11] | [-6, -8, -9, -11] |     []
			-8L   |       true           |      true        ||   [-1, -3]            |  [-1, -5]          |     [-1, -5]      |     []
			-9L   |       true           |      true        ||   [-3, -5]            |  [-6, -9]          |     [-6, -9]      |     []

        	-1L   |       true           |      true        ||   [-1, -3]            |  [-1, -5]          |     [-1, -5]      |     []
			-1L   |       true           |      false       ||   [-1, -3]            |  [-1, -5]          |     []            |     []
			-1L   |       false          |      true        ||   [-1, -3]            |  []                |     [-1, -5]      |     []
			-1L   |       false          |      false       ||   [-1, -3]            |  []                |     []            |     []
			-1L   |       false          |      true        ||   [-1, -3]            |  []                |     [-1, -5]      |     []
			-1L   |       true           |      false       ||   [-1, -3]            |  [-1, -5]          |     []            |     []
}

}
