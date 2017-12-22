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
package org.squashtest.tm.service.milestone;

import javax.inject.Inject;

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.it.basespecs.DbunitServiceSpecification

import spock.lang.Unroll;
import spock.unitils.UnitilsSupport;
import org.unitils.dbunit.annotation.DataSet

@UnitilsSupport
@Transactional
public class MilestoneBindingServiceIT extends DbunitServiceSpecification{

	@Inject
	MilestoneBindingManagerService manager
	
	@DataSet("/org/squashtest/tm/service/milestone/MilestoneBindingManagerServiceIT.xml")
	def "one project to find them all and in darkness bind them"(){
		given :
		def findThem = manager.getAllBindableMilestoneForProject(projectId)
		def findedIds = findThem.collect{it.id}
		when :
		def bindThem = manager.bindMilestonesToProject(findedIds, projectId)
		def findBinded = manager.getAllBindedMilestoneForProject(projectId)
		then :
		findBinded.collect{it.id} as Set == [-1, -2, -3, -4, -5] as Set
		where :
		projectId | _
		   -1L     | _
		   -2L     | _
		   -3L     | _
	}
	
	@DataSet("/org/squashtest/tm/service/milestone/MilestoneBindingManagerServiceIT.xml")
	def "one project to find them all (bindable)"(){
		given :

		when :
		def findThem = manager.getAllBindableMilestoneForProject(projectId)
		then :
		findThem.collect{it.id} as Set == ids as Set
		where :
		projectId || ids
		   -1L     || []
		   -2L     || [-4, -5]
		   -3L     || [-1, -2, -3, -4, -5]
	}
	
	@DataSet("/org/squashtest/tm/service/milestone/MilestoneBindingManagerServiceIT.xml")
	def "one milestone to find them all (bindable)"(){
		given :

		when :
		def findThem = manager.getAllBindableProjectForMilestone(milestoneId)
		then :
		findThem.collect{it.id} as Set == ids as Set
		where :
		milestoneId || ids
		   -1L     || [-3]
		   -2L     || [-3]
		   -3L     || [-3]
		   -4L     || [-2, -3]
	}
	
	@DataSet("/org/squashtest/tm/service/milestone/MilestoneBindingManagerServiceIT.xml")
	def "one project to find them all (binded)"(){
		given :

		when :
		def findThem = manager.getAllBindedMilestoneForProject(projectId)
		then :
		findThem.collect{it.id} as Set == ids as Set
		where :
		projectId || ids
		   -1L     || [-1, -2, -3, -4, -5]
		   -2L     || [-1, -2, -3]
		   -3L     || []
	}
	
	@DataSet("/org/squashtest/tm/service/milestone/MilestoneBindingManagerServiceIT.xml")
	def "one milestone to find them all (binded)"(){
		given :

		when :
		def findThem = manager.getAllProjectForMilestone(milestoneId)
		then :
		findThem.collect{it.id} as Set == ids as Set
		where :
		milestoneId || ids
		   -1L     || [-1, -2]
		   -2L     || [-1, -2]
		   -3L     || [-1, -2]
		   -4L     || [-1]
	}
	
	@Unroll
	@DataSet("/org/squashtest/tm/service/milestone/MilestoneBindingManagerServiceIT.xml")
	def "Free milestones from the evil project"(){
	
		given : 
		when :
		manager.unbindMilestonesFromProject(milestoneIds, projectId)
		def result = manager.getAllBindedMilestoneForProject(projectId)

		then :
		result.collect{it.id} as Set == ids as Set 
		
		where : 
		milestoneIds                | projectId     || ids
		[-1L]                       |     -1L       || [-2, -3 , -4, -5]
		[-1L, -2L]                  |    -1L        || [-3, -4, -5]
		[-1L, -3L]                  |     -1L       || [-2, -4, -5]
		[-1L, -2L, -3L]             |     -2L       || []
		[-1L, -2L, -5L, -4L, -3L]   |     -1L       || []
	}
}
