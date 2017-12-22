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

import org.junit.runner.RunWith
import org.spockframework.runtime.Sputnik
import org.springframework.transaction.annotation.Transactional
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.squashtest.tm.service.testcase.TestCaseLibraryNavigationService
import org.unitils.dbunit.annotation.DataSet
import spock.lang.Ignore

import spock.unitils.UnitilsSupport


@UnitilsSupport
@Transactional
@RunWith(Sputnik)
class HibernateTestCaseLibraryNodeDaoIT extends DbunitServiceSpecification {

	@Inject
	TestCaseLibraryNavigationService service

	@DataSet("HibernateTestCaseLibraryNodeDaoIT.sample.xml")
	def "should fetch the pathes of some test case library nodes"(){

		given :
		def tclnids = [-242L,-237L,-257L]

		when :
		def res = service.getPathsAsString(tclnids)

		then :
		res == ["/Test Project-1/super 1/sub1/sub 11", "/Test Project-1/super 1", "/autre projet/mickaelito/roberto"]
	}


	@DataSet("HibernateTestCaseLibraryNodeDaoIT.sample.xml")
	def "should fetch the pathes of the same nodes, in a different order"(){

		given :
		def tclnids = [-237L,-257L, -242L]

		when :
		def res = service.getPathsAsString(tclnids)

		then :
		res == [ "/Test Project-1/super 1", "/autre projet/mickaelito/roberto", "/Test Project-1/super 1/sub1/sub 11"]
	}

	@DataSet("HibernateTestCaseLibraryNodeDaoIT.sample.xml")
	def "should escape the slashes in names having a slash"(){

		given :
		def tclnids = [-252L]

		when :
		def res = service.getPathsAsString(tclnids)

		then :
		res == [ "/Test Project-1/other folder/subother with slash \\/ here"]
	}


	@DataSet("HibernateTestCaseLibraryNodeDaoIT.sample.xml")
	def "should fetch the pathes of the same nodes, but one of them is null because the id do not exists"(){

		given :
		def tclnids = [-237L,-1L, -242L]

		when :
		def res = service.getPathsAsString(tclnids)

		then :
		res == [ "/Test Project-1/super 1", null, "/Test Project-1/super 1/sub1/sub 11"]
	}



	@DataSet("HibernateTestCaseLibraryNodeDaoIT.sample.xml")
	def "should find the ids of some nodes given their path"(){

		given :
		def tclnpaths = [ "/Test Project-1/super 1", "/autre projet/mickaelito/roberto", "/Test Project-1/super 1/sub1/sub 11"]

		when :
		def res = service.findNodeIdsByPath(tclnpaths)

		then :
		res == [-237L,-257L, -242L]
	}


	@DataSet("HibernateTestCaseLibraryNodeDaoIT.sample.xml")
	def "should find the ids of a path with an escaped slash in one folder name"(){

		given :
		def tclnpaths = [ "/Test Project-1/other folder/subother with slash \\/ here"]

		when :
		def res = service.findNodeIdsByPath(tclnpaths)

		then :
		res == [-252L]
	}

	@DataSet("HibernateTestCaseLibraryNodeDaoIT.sample.xml")
	def "should not find the ids of a path with an  slash in one folder name that wasn't escaped because now we care"(){

		given :
		def tclnpaths = [ "/Test Project-1/other folder/subother with slash / here"]

		when :
		def res = service.findNodeIdsByPath(tclnpaths)

		then :
		res == [null]
	}


	@DataSet("HibernateTestCaseLibraryNodeDaoIT.sample.xml")
	def "should find the ids of two node and return null for the second one because the path do not exists"(){

		given :
		def tclnpaths = [ "/Test Project-1/super 1", "inexistant", "/Test Project-1/super 1/sub1/sub 11"]

		when :
		def res = service.findNodeIdsByPath(tclnpaths)

		then :
		res == [-237L,null, -242L]
	}



	@DataSet("HibernateTestCaseLibraryNodeDaoIT.sample.xml")
	def "should load nodes by their paths"(){

		given :
		def tclnpaths = [ "/Test Project-1/super 1", "/Test Project-1/other folder/subother with slash \\/ here"]

		when :
		def res = service.findNodesByPath(tclnpaths)

		then :
		res*.id.containsAll([-237L,-252L])
	}



	@DataSet("HibernateTestCaseLibraryNodeDaoIT.sample.xml")
	def "should load nodes by their paths except the second one because it doesn't exist"(){

		given :
		def tclnpaths = [ "/Test Project-1/super 1", "inexistant", "/Test Project-1/super 1/sub1/sub 11"]

		when :
		def res = service.findNodesByPath(tclnpaths)

		then :
		res.size() == 3
		res[1] == null
		res.collect{ it?.id } == [-237L,null, -242L]
	}

	@DataSet("HibernateTestCaseLibraryNodeDaoIT.sample.xml")
	def "should not find test case by paths"(){
		given :
		def paths  = ["/Test Project-1/super 1/sub1/sub 11","/Test Project-1/sub 11"]

		when  :
		def res = service.findNodeIdsByPath(paths)

		then :
		res.collect{ it } == [-242L,null]
	}

	@DataSet("HibernateTestCaseLibraryNodeDaoIT.sample.xml")
	def "should not find test case by path"(){
		given :
		def path  = "/autre projet/larrynio"

		when  :
		def res = service.findNodeIdByPath(path)

		then :
		res == null;
	}

}
