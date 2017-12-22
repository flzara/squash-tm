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
package org.squashtest.tm.service.internal.repository.hibernate

import org.squashtest.it.basespecs.DbunitDaoSpecification;
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.service.internal.repository.EntityDao
import org.unitils.dbunit.annotation.DataSet
import spock.unitils.UnitilsSupport

import javax.inject.Inject

@UnitilsSupport
class EntityDaoIT extends DbunitDaoSpecification {

	//EntityDao is a generic class. Let's use the Project as the specific implementation.

	@Inject
	private ProjectDaoImpl projectDao;

	private EntityDao<Project> entityDao;

	def setup(){
		entityDao = projectDao;
	}


	@DataSet("EntityDaoIT.should find a list of entity.xml")
	def "should find a list of entity"(){

		when :
		def res = entityDao.findAllByIds([ -1L, -3L, -4L, -6L ])

		then :
		res.size() == 4
		res*.id.containsAll([-6L, -4L, -3L, -1L])
		res*.name.containsAll(["proj6", "proj4", "proj3", "proj1"])
	}


}
