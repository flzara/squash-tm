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

import javax.inject.Inject

import org.springframework.transaction.annotation.Transactional
import org.squashtest.it.basespecs.DbunitDaoSpecification;
import org.squashtest.tm.domain.testautomation.TestAutomationProject
import org.squashtest.tm.domain.testautomation.TestAutomationServer
import org.squashtest.tm.service.internal.repository.TestAutomationProjectDao
import org.squashtest.tm.service.internal.repository.TestAutomationServerDao
import org.squashtest.tm.service.internal.repository.hibernate.NonUniqueEntityException
import org.unitils.dbunit.annotation.DataSet

import spock.unitils.UnitilsSupport

@UnitilsSupport
@Transactional
class HibernateTestAutomationProjectDaoIT extends DbunitDaoSpecification {

	@Inject  TestAutomationServerDao serverDao
	@Inject	 TestAutomationProjectDao projectDao



	@DataSet("HibernateTestAutomationDao.sandbox.xml")
	def "should find a project by id"(){

		when :
		def res = projectDao.findById(-11L)

		then :
		res.id==-11L
		res.server.id==-1L
		res.jobName=="roberto1"

	}


	@DataSet("HibernateTestAutomationDao.sandbox.xml")
	def "should find a project by example"(){
		given :
		TestAutomationServer server = serverDao.findOne(-1L)
		TestAutomationProject project = new TestAutomationProject("roberto1", "Project Roberto 1",  server)

		when :
		def res = projectDao.findByExample(project)

		then :
		res.id==-11L
	}


	@DataSet("HibernateTestAutomationDao.sandbox.xml")
	def "should not find a project because of unmatched example"(){
		given :
		TestAutomationProject example = new TestAutomationProject()
		example.setJobName("roberto55")
		example.setLabel("Project Roberto-55")
		when :
		def res = projectDao.findByExample(example)

		then :
		res == null
	}


	@DataSet("HibernateTestAutomationDao.sandbox.xml")
	def "should rant because too many matches for the given example"(){
		given :
		TestAutomationServer server = serverDao.findOne(-1L)
		TestAutomationProject project = new TestAutomationProject(null, server)

		when :
		def res = projectDao.findByExample(project)

		then :
		thrown(NonUniqueEntityException)
	}

	@DataSet("HibernateTestAutomationDao.sandbox.xml")
	def "should list the automation projects hosted on a given server"(){

		when :
		def res = projectDao.findAllHostedProjects(-1L)

		then :
		res.size()==3
		res.collect{it.jobName} as Set == ["roberto1", "roberto2", "roberto3"] as Set
	}

}
