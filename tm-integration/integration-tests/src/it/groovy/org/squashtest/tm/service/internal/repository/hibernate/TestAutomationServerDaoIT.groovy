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

import org.springframework.transaction.annotation.Transactional
import org.squashtest.it.basespecs.DbunitDaoSpecification
import org.squashtest.tm.service.internal.repository.TestAutomationServerDao
import org.unitils.dbunit.annotation.DataSet
import spock.unitils.UnitilsSupport

import javax.inject.Inject

@UnitilsSupport
@Transactional
class TestAutomationServerDaoIT extends DbunitDaoSpecification {

	@Inject  TestAutomationServerDao serverDao


	@DataSet("HibernateTestAutomationDao.sandbox.xml")
	def "should find a server by id"(){

		when :
		def res = serverDao.getOne(-1L)

		then :
		res.id==-1L
		res.name == "Roberto-1"
		res.url == "http://www.roberto.com"
		res.kind=="jenkins"

	}


	@DataSet("HibernateTestAutomationDao.sandbox.xml")
	def "should find a server by name"(){

		when :
		def res = serverDao.findByName("Roberto-1")

		then :
		res.id==-1L
		res.name == "Roberto-1"
		res.url == "http://www.roberto.com"
		res.kind=="jenkins"

	}







}
