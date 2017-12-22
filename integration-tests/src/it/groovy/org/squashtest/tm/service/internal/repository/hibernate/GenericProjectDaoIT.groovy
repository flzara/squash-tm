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

import org.springframework.data.domain.Sort
import org.squashtest.it.basespecs.DbunitDaoSpecification;
import org.squashtest.tm.domain.project.GenericProject
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.service.internal.repository.GenericProjectDao
import org.unitils.dbunit.annotation.DataSet

import spock.lang.Unroll
import spock.unitils.UnitilsSupport

import javax.inject.Inject

import static org.springframework.data.domain.Sort.Direction.ASC
import static org.springframework.data.domain.Sort.Direction.DESC

/**
 * @author Gregory Fouquet
 *
 */
@UnitilsSupport
class GenericProjectDaoIT extends DbunitDaoSpecification {
	@Inject
	GenericProjectDao dao

	@Unroll
	@DataSet("GenericProjectDaoIT.xml")
	def "should return a list of existing project" () {
		given:
		Sort sort = new Sort(sortOrder, sortAttr)

		when:
		List<GenericProject> list = dao.findAll(sort)

		then:
		list*.name == expected

		where:
		sortAttr | sortOrder | expected
		"id"     | ASC       | ["ONE", "TWO", "THREE", "FOUR", "twobis"]
		"name"   | DESC      | ["twobis", "TWO", "THREE", "ONE", "FOUR"]

	}

	@DataSet("GenericProjectDaoIT.xml")
	def "should find project by id ordered by name"(){
		given :
		def ids = [100001L, 100002L, 100003L, 100004L, 100005L]
		when :
		List<GenericProject> result = dao.findAllByIdIn(ids, new Sort(ASC, "name"))
		then:
		result*.name == ["FOUR", "ONE", "THREE", "TWO", "twobis"]
	}

	@DataSet("GenericProjectDaoIT.xml")
	def "should count existing projects" () {
		expect:
		dao.count() == 5
	}

	@Unroll
	@DataSet("GenericProjectDaoIT.xml")
	def "should count #count projects for name #name" () {
		expect:
		dao.countByName(name) == count

		where:
		name       | count
		"whatever" | 0
		"ONE"      | 1
	}

	@DataSet("GenericProjectDaoIT.xml")
	def "should coerce template into a project" () {
		when:
		GenericProject res = dao.coerceTemplateIntoProject(100004)

		then:
		res instanceof Project
	}


	@DataSet("GenericProjectDaoIT.xml")
	@Unroll("asserting that project of id #pId is a template is #res")
	def "should tells that the given project is a project template"(){
		expect :
		res == dao.isProjectTemplate(pId)
		where :
		pId	| res
		100001L	| false
		100004L	| true
	}

	@DataSet("GenericProjectDaoIT.server.xml")
	def "should find a project's server"(){
		given:
		def pId = 100001L
		when :
		def res = dao.findTestAutomationServer(pId)
		then :
		res != null
		res.id == 1000011L
	}

	@DataSet("GenericProjectDaoIT.taprojects.xml")
	def "should find a project's taprojects jobNames"(){
		given:
		def pId = 100001L
		when :
		Collection<String> res = dao.findBoundTestAutomationProjectJobNames(pId)
		then :
		res.containsAll(["job-1", "job-2"])
	}



}
