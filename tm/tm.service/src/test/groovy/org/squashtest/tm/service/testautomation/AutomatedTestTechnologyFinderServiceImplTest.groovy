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
package org.squashtest.tm.service.testautomation

import org.squashtest.tm.domain.testautomation.AutomatedTestTechnology
import org.squashtest.tm.service.internal.repository.AutomatedTestTechnologyDao
import org.squashtest.tm.service.internal.testautomation.AutomatedTestTechnologyFinderServiceImpl
import spock.lang.Specification

class AutomatedTestTechnologyFinderServiceImplTest extends Specification{

	AutomatedTestTechnologyFinderServiceImpl finder = new AutomatedTestTechnologyFinderServiceImpl()

	AutomatedTestTechnologyDao automatedTestTechnologyDao = Mock()

	def setup(){
		finder.automatedTestTechnologyDao = automatedTestTechnologyDao
	}

	def "should find an automated test technology by its id"(){
		given:
		def tech = Mock(AutomatedTestTechnology)

		when:
		def result = finder.findById(-1l)

		then:
		1 * automatedTestTechnologyDao.getOne(-1L) >> tech
		result == tech
	}

	def "should find an automated test technology by its name"(){
		given:
		def tech = Mock(AutomatedTestTechnology)
		String techName = "Cypress"

		when:
		def result = finder.findByName(techName)

		then:
		1 * automatedTestTechnologyDao.findByName(techName) >> tech
		result == tech
	}

	def "should find all automated test technologies"(){
		given:
		def tech1 = Mock(AutomatedTestTechnology)

		def tech2 = Mock(AutomatedTestTechnology)

		when:
		def result = finder.getAllAvailableAutomatedTestTechnology()

		then:
		1 * automatedTestTechnologyDao.findAll() >> [tech1, tech2]
		result.size() == 2
	}
}
