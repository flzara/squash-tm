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
package org.squashtest.tm.service.internal.milestone
import org.squashtest.tm.domain.milestone.Milestone
import org.squashtest.tm.service.internal.repository.MilestoneDao

import spock.lang.Specification
class CustomMilestoneManagerServiceImplTest extends Specification {

	CustomMilestoneManagerServiceImpl manager = new CustomMilestoneManagerServiceImpl()
	MilestoneDao milestoneDao= Mock()

	def setup(){
		manager.milestoneDao = milestoneDao
	}

	def "should delete milestones"(){

		given :
		def ids = [1L, 2L, 5L]
		def milestones = ids.collect{new Milestone(id:it)}
		milestones.each{milestoneDao.findOne(it.id) >> it}
		when :
		manager.removeMilestones(ids)
		then :
		milestones.each{1 * milestoneDao.delete(it)}

	}
}
