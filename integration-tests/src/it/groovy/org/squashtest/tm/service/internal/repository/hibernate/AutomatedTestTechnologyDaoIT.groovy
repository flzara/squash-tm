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

import org.squashtest.it.basespecs.DbunitDaoSpecification
import org.squashtest.tm.domain.testautomation.AutomatedTestTechnology
import org.squashtest.tm.service.internal.repository.AutomatedTestTechnologyDao
import org.unitils.dbunit.annotation.DataSet
import spock.unitils.UnitilsSupport

import javax.inject.Inject

@DataSet
@UnitilsSupport
class AutomatedTestTechnologyDaoIT extends DbunitDaoSpecification {

	@Inject
	AutomatedTestTechnologyDao automatedTestTechnologyDao

	def "should find an automated test technology by its id"(){
		given:
		long techId = -1L

		when:
		AutomatedTestTechnology tech = automatedTestTechnologyDao.getOne(techId)

		then:
		tech.id == -1L
		tech.name == "Robot Framework"
		tech.actionProviderKey == "robotframework/execute@v1"
	}

	def "should find all available automated test technologies"(){

		when:
		List<AutomatedTestTechnology> techList = automatedTestTechnologyDao.findAll()

		then:
		techList.size() == 2
	}

	def "should find an existing automated test technology by its name"(){

		given:
		String techName = "Cypress"

		when:
		AutomatedTestTechnology tech = automatedTestTechnologyDao.findByName(techName)

		then:
		tech.id == -2L
		tech.name == "Cypress"
		tech.actionProviderKey == "cypress/execute@v1"
	}

	def "should not find an automated test technology given an unknown name"(){

		given:
		String techName = "toto"

		when:
		AutomatedTestTechnology tech = automatedTestTechnologyDao.findByName(techName)

		then:
		tech == null
	}
}
