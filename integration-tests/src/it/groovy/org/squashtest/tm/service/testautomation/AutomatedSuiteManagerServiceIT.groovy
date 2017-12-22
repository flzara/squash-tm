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

import javax.inject.Inject

import org.spockframework.util.NotThreadSafe
import org.springframework.transaction.annotation.Transactional
import org.squashtest.tm.domain.execution.Execution
import org.squashtest.tm.domain.project.GenericProject
import org.squashtest.tm.domain.testautomation.TestAutomationServer
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.unitils.dbunit.annotation.DataSet

import spock.unitils.UnitilsSupport

@NotThreadSafe
@UnitilsSupport
@Transactional
class AutomatedSuiteManagerServiceIT extends DbunitServiceSpecification {

	@Inject
	AutomatedSuiteManagerService service

	@DataSet("TestAutomationService.sandbox.xml")
	def "should return executions associated to an automated test suite given its id"(){
		when:
		def res = service.findExecutionsByAutomatedTestSuiteId("suite1")
		then:
		res[0].id == -40L
		res[1].id == -41L
	}

	def getServer(id){
		return getSession().load(TestAutomationServer.class, id)
	}

	def getProject(id){
		return getSession().load(GenericProject.class, id)
	}
}
