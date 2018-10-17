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
package org.squashtest.tm.service.internal.repository


import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.squashtest.it.basespecs.DbunitDaoSpecification
import org.squashtest.tm.core.foundation.collection.ColumnFiltering
import org.squashtest.tm.core.foundation.collection.DefaultColumnFiltering
import org.squashtest.tm.core.foundation.collection.SimpleColumnFiltering
import org.squashtest.tm.domain.tf.automationrequest.AutomationRequest
import org.squashtest.tm.domain.tf.automationrequest.AutomationRequestStatus
import org.unitils.dbunit.annotation.DataSet
import spock.unitils.UnitilsSupport

import javax.inject.Inject


@UnitilsSupport
class AutomationRequestDaoIT extends DbunitDaoSpecification{


	@Inject
	private AutomationRequestDao requestDao;

	@DataSet("AutomationRequestDaoIT.sample.xml")
	def "should retrieve a request by id"(){

		expect :
			requestDao.getOne(-1L) != null

	}


	@DataSet("AutomationRequestDaoIT.sample.xml")
	def "should retrieve a request by test case id"(){

		expect :
			requestDao.findByTestCaseId(-4L).requestStatus == AutomationRequestStatus.VALID


	}


	// ****************************** paging *****************************************




	/*
	* Note : this test simply tests for the Spring Data Jpa autogenerated DAO. We can
	* assume that it works as expected, this test is present only for the sake of completion
	* and the satisfaction of another green bullet in the test report.
	*
	* The other tests will address the custom implementation of paged - sorted - filtered search.
	 */
	@DataSet("AutomationRequestDaoIT.sample.xml")
	def "should find sorted and paged with simple attributes "(){
		given :
		Pageable pageable = PageRequest.of(0, 10, Sort.Direction.ASC, "transmissionDate")

		when :
		Page<AutomationRequest> page = requestDao.findAll(pageable)

		then :
		page.totalElements == 4
		page.totalPages == 1

		page.content.collect {it.id } == [-2L, -1L, -4L, -3L]

	}


	@DataSet("AutomationRequestDaoIT.sample.xml")
	def "should find that the specified paging will partition the total in two pages (no sort, no filter)"(){
		given :
		Pageable pageable = PageRequest.of(1, 2, Sort.unsorted())

		and :
		ColumnFiltering filter = new DefaultColumnFiltering();

		when :
		Page<AutomationRequest> page = requestDao.findAll(pageable, filter)

		then :
		page.totalElements == 4
		page.totalPages == 2

	}

	// ************************ sorting ******************************

	@DataSet("AutomationRequestDaoIT.sample.xml")
	def "should find sorted by transmissionDate (ie with trivial comparison semantics)"(){
		given :
		Pageable pageable = PageRequest.of(0, 4, Sort.Direction.ASC, "transmissionDate")

		and :
		ColumnFiltering filter = new DefaultColumnFiltering();

		when :
		Page<AutomationRequest> page = requestDao.findAll(pageable, filter)

		then :

		page.content.collect {it.id } == [-2L, -1L, -4L, -3L]

	}

	@DataSet("AutomationRequestDaoIT.sample.xml")
	def "should find sorted by workflow status (ie with trivial comparison semantics), paged with a defined yet empty filter"(){
		given :
		Pageable pageable = PageRequest.of(0, 10, Sort.Direction.ASC, "requestStatus")

		and :
		ColumnFiltering filter = new DefaultColumnFiltering();

		when :
		Page<AutomationRequest> page = requestDao.findAll(pageable, filter)

		then :

		page.content.collect {it.id } == [-4L, -2L, -1L, -3L]

	}

	@DataSet("AutomationRequestDaoIT.sample.xml")
	def "should find sorted by assignee login (ie on attribute of a joined entity)"(){
		given :
		// we also sort by request id to ensure the order of the result
		Pageable pageable = PageRequest.of(0, 10, Sort.Direction.ASC, "assignedTo.login", "id")

		and :
		ColumnFiltering filter = new DefaultColumnFiltering();

		when :
		Page<AutomationRequest> page = requestDao.findAll(pageable, filter)

		then :

		page.content.collect {it.id } == [-4L, -3L, -1L, -2L]

	}


	@DataSet("AutomationRequestDaoIT.sample.xml")
	def "should find sorted by project name (ie on attribute of a far joined entity)"(){
		given :
		// we also sort by request id to ensure the order of the result
		Pageable pageable = PageRequest.of(0, 10, Sort.Direction.ASC, "testCase.project.name", "id")

		and :
		ColumnFiltering filter = new DefaultColumnFiltering();

		when :
		Page<AutomationRequest> page = requestDao.findAll(pageable, filter)

		then :

		page.content.collect {it.id } == [-3L, -2L, -4L, -1L]

	}


	// ************************* filtering *********************************

	@DataSet("AutomationRequestDaoIT.sample.xml")
	def "should filter by assignee name (ie with like)"(){
		given :
		Pageable pageable = PageRequest.of(0, 10, Sort.unsorted())

		and :
		ColumnFiltering filter = new SimpleColumnFiltering()
									.addFilter("assignedTo.login", "L")

		when :
		Page<AutomationRequest> page = requestDao.findAll(pageable, filter)

		then :
		page.totalElements == 2
		page.content.collect {it.id } as Set == [-1L, -2L] as Set
	}


	@DataSet("AutomationRequestDaoIT.sample.xml")
	def "should filter by request status (ie with enum equality)"(){
		given :
		Pageable pageable = PageRequest.of(0, 10, Sort.unsorted())

		and :
		ColumnFiltering filter = new SimpleColumnFiltering()
			.addFilter("requestStatus", "VALID")

		when :
		Page<AutomationRequest> page = requestDao.findAll(pageable, filter)

		then :
		page.totalElements == 1
		page.content.collect {it.id } as Set == [-4L] as Set
	}


	@DataSet("AutomationRequestDaoIT.sample.xml")
	def "should filter by priority (ie with integer equality)"(){
		given :
		Pageable pageable = PageRequest.of(0, 10, Sort.unsorted())

		and :
		ColumnFiltering filter = new SimpleColumnFiltering()
			.addFilter("automationPriority", "1000")

		when :
		Page<AutomationRequest> page = requestDao.findAll(pageable, filter)

		then :
		page.totalElements == 1
		page.content.collect {it.id } as Set == [-4L] as Set
	}


	@DataSet("AutomationRequestDaoIT.sample.xml")
	def "should filter by transmissionDate (between dates)"(){
		given :
		Pageable pageable = PageRequest.of(0, 10, Sort.unsorted())

		and :
		ColumnFiltering filter = new SimpleColumnFiltering()
			.addFilter("transmissionDate", "2018-10-11 - 2018-10-13")

		when :
		Page<AutomationRequest> page = requestDao.findAll(pageable, filter)

		then :
		page.totalElements == 3
		page.content.collect {it.id } as Set == [-4L, -2L, -1L] as Set
	}





}
