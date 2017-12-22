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

import org.hibernate.exception.ConstraintViolationException
import org.spockframework.util.NotThreadSafe
import org.springframework.transaction.annotation.Transactional
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.squashtest.tm.domain.project.GenericProject
import org.squashtest.tm.domain.testautomation.TestAutomationProject
import org.squashtest.tm.domain.testautomation.TestAutomationServer
import org.unitils.dbunit.annotation.DataSet
import spock.unitils.UnitilsSupport

import javax.inject.Inject
import javax.persistence.FlushModeType
import javax.persistence.PersistenceException

@NotThreadSafe
@UnitilsSupport
@Transactional
public class TestAutomationProjectManagerServiceIT extends DbunitServiceSpecification {

	@Inject
	TestAutomationProjectManagerService service

	FlushModeType prevFlushMode

	def setup() {
		prevFlushMode = em.getFlushMode()
		em.setFlushMode(FlushModeType.COMMIT)
	}

	def cleanup() {
		em.setFlushMode(prevFlushMode)
	}

	@DataSet("TestAutomationService.sandbox.xml")
	def "should persist a new TestAutomationProject"() {

		given:
		def server = getServer(-1L)
		def project = new TestAutomationProject("roberto5", "Project Roberto 5", server)
		def tmproject = getProject(-1L)
		project.setTmProject(tmproject)

		when:
		service.persist(project)
		em.flush()

		then:
		project.id != null
		project.label == "Project Roberto 5"
		project.jobName == "roberto5"
		project.server.id == -1L
	}


	@DataSet("TestAutomationService.sandbox.xml")
	def "should say that a project label is ok"() {
		given:
		def server = getServer(-2L)
		def project = new TestAutomationProject("whatever", "Project Mike 2", server)
		def tmproject = getProject(-1L)
		project.setTmProject(tmproject)

		when:
		service.persist(project)
		em.flush()

		then:
		notThrown PersistenceException
	}

	@DataSet("TestAutomationService.sandbox.xml")
	def "should say that a project label is not unique"() {
		given:
		def server = getServer(-2L)
		def project = new TestAutomationProject("whatever", "Project Mike 2", server)
		def tmproject = getProject(-2L)
		project.setTmProject(tmproject)

		when:
		service.persist(project)
		em.flush()

		then:
		def ex = thrown PersistenceException
		ex.cause instanceof ConstraintViolationException
	}

	def getServer(id) {
		return em.getReference(TestAutomationServer.class, id)
	}

	def getProject(id) {
		return em.getReference(GenericProject.class, id)
	}
}
