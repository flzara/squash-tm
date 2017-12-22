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
import org.squashtest.tm.domain.testautomation.TestAutomationServer
import org.squashtest.tm.service.internal.repository.TestAutomationServerDao
import org.squashtest.tm.service.internal.repository.hibernate.NonUniqueEntityException
import org.unitils.dbunit.annotation.DataSet

import spock.unitils.UnitilsSupport

@UnitilsSupport
@Transactional
class TestAutomationServerDaoIT extends DbunitDaoSpecification {

	@Inject  TestAutomationServerDao serverDao


	@DataSet("HibernateTestAutomationDao.sandbox.xml")
	def "should find a server by id"(){

		when :
		def res = serverDao.findOne(-1L)

		then :
		res.id==-1L
		res.name == "Roberto-1"
		res.baseURL.equals(new URL("http://www.roberto.com"))
		res.login == "roberto"
		res.password == "passroberto"
		res.kind=="jenkins"

	}


	@DataSet("HibernateTestAutomationDao.sandbox.xml")
	def "should find a server by name"(){

		when :
		def res = serverDao.findByName("Roberto-1")

		then :
		res.id==-1L
		res.name == "Roberto-1"
		res.baseURL.equals(new URL("http://www.roberto.com"))
		res.login == "roberto"
		res.password == "passroberto"
		res.kind=="jenkins"

	}







}
